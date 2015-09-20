import name.yumaa.xmttr.Scribe;
import name.yumaa.xmttr.XMttrEmitter;
import name.yumaa.xmttr.XMttrModule;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.Date;

/**
 * SQL emitter
 * @author Victor Didenko
 * yumaa.verdin@gmail.com
 * 23.10.2014
 */
@SuppressWarnings("unused")
public class SQL extends XMttrEmitter implements XMttrModule {

    private String template;

    // JDBC credentials
    private String driver;
    private String url;
    private String user;
    private String password;
    private boolean isSimulation;

    @Override
    public void init(Properties properties) throws Exception {

        String simulationValue = properties.getProperty("xmttr.emitter.jdbc.simulate", "false").toLowerCase();
        isSimulation = "true".equals(simulationValue) || "yes".equals(simulationValue) || "1".equals(simulationValue);

        // init JDBC credentials
        if (!isSimulation) {
            boolean isJDBCCredentialsInited = false;
            if (properties.containsKey("xmttr.emitter.jdbc.driver") && properties.containsKey("xmttr.emitter.jdbc.url")) {
                String driverValue = properties.getProperty("xmttr.emitter.jdbc.driver");
                String urlValue = properties.getProperty("xmttr.emitter.jdbc.url");
                if (driverValue != null && !driverValue.isEmpty() && urlValue != null && !urlValue.isEmpty()) {
                    driver = driverValue;
                    url = urlValue;
                    user = properties.getProperty("xmttr.emitter.jdbc.user");
                    password = properties.getProperty("xmttr.emitter.jdbc.password");

                    loadDriver();
                    isJDBCCredentialsInited = true;
                }
            }
            if (!isJDBCCredentialsInited) {
                throw new Exception("You must define JDBC credentials in order to use this emitter");
            }
        }

        // get template file
        if (properties.containsKey("xmttr.template")) {
            String templateValue = properties.getProperty("xmttr.template");
            if (templateValue != null && !templateValue.isEmpty()) {
                Scribe.log(this, 3, "Read SQL template");
                template = new String(Files.readAllBytes(Paths.get(templateValue)));
            }
        } else {
            Scribe.log(this, 3, "SQL template (xmttr.template) is not defined");
        }

    }

    /**
     * Emit text
     */
    public void emit(final Map<String,Object> scope) throws Exception {
        Map<String,Integer> positions = new LinkedHashMap<String,Integer>(scope.size()); // positions of SQLs
        for (String queryName : new TreeSet<String>(scope.keySet())) {
            Object scopeValue = scope.get(queryName);
            if (scopeValue != null && scopeValue instanceof List) {
                positions.put(queryName, template.indexOf(queryName));
            }
        }

        // cut queries from template file
        Map<String,String> queries = new LinkedHashMap<String,String>(positions.size());
        for (Map.Entry<String,Integer> e : positions.entrySet()) {
            String queryName = e.getKey();
            int start = e.getValue() + queryName.length();
            int end = findNextPosition(start, positions);
            if (end == -1) {
                end = template.length();
            }

            // cut and save query(ies)
            queries.put(e.getKey(), template.substring(start, end).replaceFirst("^\\s*>", ""));
        }

        Connection connection = null;
        try {
            if (!isSimulation) {
                connection = DriverManager.getConnection(url, user, password);
            }
            if (connection != null || isSimulation) {
                for (Map.Entry<String,String> e : queries.entrySet()) {
                    String queryName = e.getKey();
                    String[] query = e.getValue().split(";"); // split queries
                    for (String q : query) {
                        q = q.trim();
                        if (!q.isEmpty()) {
                            Scribe.log(this, 3, queryName + " =>\n" + q);

                            PreparedStatement statement = null;
                            if (connection != null) {
                                statement = connection.prepareStatement(q);
                            }

                            // bind variables to the query
                            @SuppressWarnings("unchecked")
                            List<Map<String,Object>> queryScope = (List<Map<String,Object>>) scope.get(queryName);
                            if (queryScope != null && !queryScope.isEmpty()) {
                                for (Map<String,Object> variables : queryScope) {
                                    Scribe.log(this, 3, "<= " + variables);

                                    // bind variables
                                    if (statement != null) {
                                        for (Map.Entry<String, Object> v : variables.entrySet()) {
                                            setVariable(statement, v.getKey(), v.getValue());
                                        }
                                    }

                                    // execute query with binded variables
                                    executeQuery(statement);
                                }
                            } else {
                                // execute query without variables
                                executeQuery(statement);
                            }
                        }
                    }
                }
            }
        } finally {
            if (connection != null) {
                connection.close();
            }
        }

    }

    /**
     * Executes queries
     * @param statement    PreparedStatement
     */
    private void executeQuery(PreparedStatement statement) throws Exception {
        if (statement != null && !isSimulation) {
            boolean resultFlag = statement.execute();

            // result is a ResultSet object
            if (resultFlag) {
                ResultSet resultSet = statement.getResultSet();
                int resultRowsCount = 0;
                while (resultSet.next()) {
                    resultRowsCount++;
                }
                Scribe.log(this, 3, "Retrieved " + resultRowsCount + " rows");

            // result is an update count or there is no result
            } else {
                Scribe.log(this, 3, "Updated " + statement.getUpdateCount() + " rows");
            }

            //statement.getMoreResults()
        } else {
            if (isSimulation) {
                Scribe.log(this, 3, "Query did't execute, simulation");
            } else {
                throw new Exception("Statement is null, nut this is not a simulation...");
            }
        }
    }

    /**
     * Set variable for PreparedStatement
     * @param statement    PreparedStatement
     * @param name         variable name (with type)
     * @param value        variable value
     */
    private void setVariable(PreparedStatement statement, String name, Object value) throws Exception {
        int place;
        String type = "string";
        String params = null;
        String strValue = value.toString();

        // choose variable type
        if (name.contains("|") || name.contains("!")) {
            String[] nameParts = name.split("[!|]");
            if (!nameParts[0].isEmpty()) {
                name = nameParts[0];
            }
            if (nameParts.length >= 2 && !nameParts[1].isEmpty()) {
                type = nameParts[1].toLowerCase();
            }
            if (nameParts.length >= 3 && !nameParts[2].isEmpty()) {
                params = nameParts[2];
            }
        }

        // get variable number
        try {
            place = Integer.parseInt(name.trim());
        } catch (NumberFormatException e) {
            // just silently skip named variables, do not throw exception
            //throw new NumberFormatException("Named prepared statements doesn't supported! Use numbers instead.");
            return;
        }

        if (place > 0 && place <= statement.getParameterMetaData().getParameterCount()) {

            if ("string".equals(type)) {
                statement.setString(place, strValue);
            } else

            if ("int".equals(type)) {
                if (strValue.isEmpty()) {
                    statement.setNull(place, Types.INTEGER);
                } else {
                    int intValue;
                    try {
                        intValue = (Integer) value; // try to cast to Integer first
                    } catch (Exception e) {
                        intValue = Integer.parseInt(strValue);
                    }
                    statement.setInt(place, intValue);
                }
            } else

            if ("boolean".equals(type)) {
                if (strValue.isEmpty()) {
                    statement.setNull(place, Types.BOOLEAN);
                } else {
                    boolean boolValue;
                    try {
                        boolValue = (Boolean) value; // try to cast to Boolean first
                    } catch (Exception e) {
                        boolValue = Boolean.parseBoolean(strValue);
                    }
                    statement.setBoolean(place, boolValue);
                }
            } else

            if ("date".equals(type)) {
                if (strValue.isEmpty()) {
                    statement.setNull(place, Types.DATE);
                } else {
                    DateFormat df = params == null ? new SimpleDateFormat() : new SimpleDateFormat(params);
                    Date dateValue;
                    try {
                        dateValue = (Date) value; // try to cast to Date first
                    } catch (Exception e) {
                        dateValue = df.parse(strValue);
                    }
                    statement.setDate(place, new java.sql.Date(dateValue.getTime()));
                }
            } else

            if ("double".equals(type)) {
                if (strValue.isEmpty()) {
                    statement.setNull(place, Types.DOUBLE);
                } else {
                    double doubleValue;
                    try {
                        doubleValue = (Double) value; // try to cast to Boolean first
                    } catch (Exception e) {
                        doubleValue = Double.parseDouble(strValue);
                    }
                    statement.setDouble(place, doubleValue);
                }
            } else

            {
                throw new Exception("Type " + type + " don't supported");
            }
        }
    }

    /**
     * Find position of next query
     * @param start        start of current query
     * @param positions    SQLs positions
     * @return position, -1 if not found (last query)
     */
    private int findNextPosition(int start, Map<String,Integer> positions) {
        int minmax = -1;
        for (int pos : positions.values()) {
            if (pos > start && ((minmax != -1 && pos < minmax) || minmax == -1)) {
                minmax = pos;
            }
        }
        return minmax;
    }

    @Override
    public void unload() throws Exception {}

    /**
     * Load driver class
     */
    public synchronized void loadDriver() throws Exception {
        Class.forName(driver);
        Scribe.log(this, 3, "JDBC driver " + driver + " successfully registered");
    }


}
