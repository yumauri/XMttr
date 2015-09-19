package name.yumaa.xmttr.scope;

import name.yumaa.xmttr.Scribe;
import name.yumaa.xmttr.modules.DriverShim;
import name.yumaa.xmttr.modules.JarClassLoader;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Victor Didenko
 * yumaa.verdin@gmail.com
 * 10.09.2014
 */
public class SQLRetriever {

    // JDBC credentials
    private String driver;
    private String url;
    private String user;
    private String password;

    // SQL queries
    private Map<String,String> sqls = new HashMap<String,String>();

    // query results
    private Map<String,Map<String,List<Object>>> results = new HashMap<String,Map<String,List<Object>>>();

    /**
     * Add query to sql map
     * @param name     query name
     * @param query    query itself
     */
    public synchronized void addQuery(String name, String query) {
        sqls.put(name, query);
    }

    /**
     * Fetch all unfetched queries
     */
    public synchronized void fetch() {
        if (sqls.isEmpty()) {
            return;
        }

        Connection connection;
        try {
            connection = DriverManager.getConnection(url, user, password);
        } catch (SQLException e) {
            Scribe.log(this, 0, "Connection failed: " + e);
            return;
        }

        if (connection != null) {
            Statement statement = null;
            try {
                statement = connection.createStatement();
            } catch (SQLException e) {
                Scribe.log(this, 0, "Statement creation failed: " + e);
            }

            if (statement != null) {
                long start = System.currentTimeMillis();
                Scribe.log(this, 1, "@ Retrieve values from database");
                for (String name : sqls.keySet()) {
                    if (!results.containsKey(name) || results.get(name) == null) {
                        results.put(name, new HashMap<String,List<Object>>());
                    }

                    String query = sqls.get(name);
                    ResultSet resultSet = null;

                    // execute query
                    try {
                        Scribe.log(this, 3, "@ Query: " + query);
                        resultSet = statement.executeQuery(query);
                        ResultSetMetaData metaData = resultSet.getMetaData();
                        int columnCount = metaData.getColumnCount();

                        // travers through rows
                        while (resultSet.next()) {
                            for (int i = 1; i < columnCount + 1; i++) {
                                String columnName = metaData.getColumnLabel(i);
                                Object columnValue = resultSet.getObject(i);
                                if (columnValue == null) {
                                    columnValue = "";
                                }

                                if (!results.get(name).containsKey(columnName) || results.get(name).get(columnName) == null) {
                                    results.get(name).put(columnName, new ArrayList<Object>());
                                }

                                // put value to results map
                                results.get(name).get(columnName).add(columnValue);
                            }
                        }
                    } catch (SQLException e) {
                        Scribe.log(this, 0, "Query \"" + query + "\" failed: " + e);
                    } finally {
                        try {
                            if (resultSet != null) {
                                resultSet.close();
                            }
                        } catch (SQLException ignored) {}
                    }
                }

                // all queries retrieved -> clear list
                sqls.clear();

                // close statement
                try {
                    statement.close();
                } catch (SQLException ignored) {}

                long stop = System.currentTimeMillis();
                if (stop - start > 0) {
                    Scribe.log(this, 1, "@ Values retrieved in " + (stop - start) + "ms");
                }
            }

            // close connection
            try {
                connection.close();
            } catch (SQLException ignored) {}
        }
    }

    /**
     * Check if list exists for specified SQL query name and specified column name
     * @param sqlName       query name
     * @param columnName    column
     * @return true if list exists, false otherwise
     */
    public synchronized boolean has(String sqlName, String columnName) {
        Map<String,List<Object>> sqlResults = results.get(sqlName);
        return sqlResults != null && (sqlResults.get(columnName) != null);
    }

    /**
     * Return list of values for specified SQL query name and specified column name
     * @param sqlName       query name
     * @param columnName    column
     * @return array
     */
    public synchronized List<Object> getList(String sqlName, String columnName) {
        Map<String,List<Object>> sqlResults = results.get(sqlName);
        if (sqlResults != null) {
            return sqlResults.get(columnName);
        }
        return null;
    }

    @SuppressWarnings("unused")
    public synchronized String getDriver() {
        return driver;
    }

    public synchronized SQLRetriever setDriver(String driver) {
        this.driver = driver;
        // load driver class
        try {
            Driver d = (Driver) Class.forName(driver, true, JarClassLoader.getInstance()).newInstance();
            DriverManager.registerDriver(new DriverShim(d));
            Scribe.log(this, 3, "JDBC driver " + driver + " successfully registered");
        } catch (ClassNotFoundException e) {
            Scribe.log(this, 0, "JDBC driver " + driver + " not found: " + e);
        } catch (InstantiationException e) {
            Scribe.log(this, 0, "JDBC driver " + driver + " cannot be instantiated: " + e);
        } catch (IllegalAccessException e) {
            Scribe.log(this, 0, "JDBC driver " + driver + " cannot be instantiated: " + e);
        } catch (SQLException e) {
            Scribe.log(this, 0, "JDBC driver " + driver + " registering error: " + e);
        }
        return this;
    }

    @SuppressWarnings("unused")
    public synchronized String getUrl() {
        return url;
    }

    public synchronized SQLRetriever setUrl(String url) {
        this.url = url;
        return this;
    }

    @SuppressWarnings("unused")
    public synchronized String getUser() {
        return user;
    }

    public synchronized SQLRetriever setUser(String user) {
        this.user = user;
        return this;
    }

    @SuppressWarnings("unused")
    public synchronized String getPassword() {
        return password;
    }

    public synchronized SQLRetriever setPassword(String password) {
        this.password = password;
        return this;
    }

    /////////////////////////////////////////////////////////////////////
    // Singleton 'On Demand Holder' pattern
    //  + thread-safe
    //  + lazy initialization
    //  + fast performance
    /////////////////////////////////////////////////////////////////////

    private static class SingletonHolder {
        static final SQLRetriever INSTANCE = new SQLRetriever();
        static {
            Scribe.log(INSTANCE, 3, "Init SQLRetriever");
        }
    }

    public static SQLRetriever getInstance() {
        return SingletonHolder.INSTANCE;
    }
}
