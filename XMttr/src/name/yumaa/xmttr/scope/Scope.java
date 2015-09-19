package name.yumaa.xmttr.scope;

import name.yumaa.xmttr.Scribe;
import name.yumaa.xmttr.XMttr;

import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.Map;
import java.util.Properties;

/**
 * @author Victor Didenko
 * yumaa.verdin@gmail.com
 * 18.08.2014
 */
public class Scope {

    // generated variables values
    private final Map<String,Object> vars;

    // variables container, accessible from many threads
    private static volatile ScopeVars variables;

    /**
     * Preload variables from file
     * @param properties Siege properties
     */
    public static void preloadVariables(final Properties properties) {
        Scribe.log("name.yumaa.xmttr.scope.Scope", 3, "Preload scope variables");
        long start = System.currentTimeMillis();

        // prepare SQLRetriever
        if (properties.containsKey("xmttr.variables.jdbc.driver") && properties.containsKey("xmttr.variables.jdbc.url")) {
            String driver = properties.getProperty("xmttr.variables.jdbc.driver");
            String url = properties.getProperty("xmttr.variables.jdbc.url");
            if (driver != null && !driver.isEmpty() && url != null && !url.isEmpty()) {
                SQLRetriever.getInstance()
                            .setDriver(driver)
                            .setUrl(url)
                            .setUser(properties.getProperty("xmttr.variables.jdbc.user"))
                            .setPassword(properties.getProperty("xmttr.variables.jdbc.password"));
            }
        }

        // preload variables
        ScopeVars localVariables = variables;
        if (localVariables == null) {
            synchronized (Scope.class) {
                localVariables = variables;
                if (localVariables == null) {
                    // read variables from file
                    Properties vars = new Properties();
                    if (properties.containsKey("xmttr.variables")) {
                        String varFile = properties.getProperty("xmttr.variables");
                        if (varFile != null && !varFile.isEmpty()) {
                            // read configuration file
                            try {
                                //vars.load(new FileReader(varFile)); <-- this loads variables in wrong ISO 8859-1 encoding
                                vars.load(new InputStreamReader(new FileInputStream(varFile), "UTF-8"));
                            } catch (Exception e) {
                                Scribe.log("name.yumaa.xmttr.scope.Scope", 0, "Could not read " + varFile + " file: " + e);
                                System.exit(1);
                            }
                        }
                    }
                    variables = new ScopeVars(vars);
                }
            }
        }

        long stop = System.currentTimeMillis();
        Scribe.log("name.yumaa.xmttr.scope.Scope", 1, "Scope variables preloaded in " + (stop - start) + "ms");
    }

    /**
     * Get static variables field
     * @return ScopeVars
     */
    public static ScopeVars getVariables() {
        ScopeVars localVariables = variables;
        if (localVariables == null) {
            synchronized (Scope.class) {
                localVariables = variables;
                if (localVariables == null) {
                    //throw new RuntimeException("You should preload variables first");
                    Scope.preloadVariables(XMttr.getInstance().getProperties());
                    localVariables = variables;
                }
            }
        }
        return localVariables;
    }

    /**
     * Get variables map
     * @return variables map
     */
    public Map<String,Object> get() {
        return vars;
    }

    /**
     * Constructor
     * Generate variables values
     */
    public Scope(Map<String,String> extra) {
        vars = getVariables().extendWith(extra).getValues();
    }
    public Scope() {
        vars = getVariables().getValues();
    }

}
