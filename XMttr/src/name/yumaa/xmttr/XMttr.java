package name.yumaa.xmttr;

import name.yumaa.xmttr.modules.Loader;
import name.yumaa.xmttr.rhino.RhinoScope;
import org.apache.commons.cli.*;
import org.mozilla.javascript.*;

import java.io.File;
import java.io.FileReader;
import java.util.Properties;

/**
 * @author Victor Didenko
 * yumaa.verdin@gmail.com
 * 16.10.2014
 */
public class XMttr {

    public static final String RELEASE_VERSION = "1.2";
    public static final String RELEASE_DATE = "14/08/2015";

    // XMttr instance
    private static volatile XMttr INSTANCE;

    // constants
    public static final String CONF_DIRECTORY = "conf/";
    public static final String SCRIPTS_DIRECTORY = "scripts/";
    public static final String LIB_DIRECTORY = "lib/";
    public static final String MODULES_DIRECTORY = "modules/";
    public static final String CONFIG_FILE = CONF_DIRECTORY + "xmttr.properties";
    public static final String DEFAULT_CONFIG_FILE = "xmttr-defaults.properties";

    // properties
    private volatile Properties properties;
    private volatile Properties overridenProperties = new Properties();
    private volatile Properties setProperties = new Properties();

    // Rhino plugin scope
    private ScriptableObject scope;

    // text generator and emitter
    private XMttrGenerator generator;
    private XMttrEmitter emitter;

    /**
     * Get properties from file and extend them with cli properties
     * @param cmd    parsed command line
     * @return properties
     */
    private Properties getProperties(final CommandLine cmd) {
        // load default properties
        Properties properties = Bootstrap.getResourceProperties(DEFAULT_CONFIG_FILE);

        // get configuration file name from cli arguments, if exists
        String confFileName = CONFIG_FILE;
        if (cmd.hasOption("c")) {
            confFileName = cmd.getOptionValue("c");
        }

        // read configuration file, if any
        properties = Bootstrap.mergeProperties(properties, Bootstrap.getProperties(confFileName));

        // and extend config from command line
        if (cmd.hasOption("D")) {
            Properties args = cmd.getOptionProperties("D");
            for (String p : args.stringPropertyNames()) {
                properties.setProperty(p, args.getProperty(p));
                overridenProperties.setProperty(p, args.getProperty(p));
            }
        }

        return properties;
    }

    /**
     * Set XMttr properties
     * @param properties    properties
     */
    private synchronized void setProperties(final Properties properties) {
        this.properties = properties;

        // set log level
        if (properties.containsKey("xmttr.loglevel")) {
            setProperty("xmttr.loglevel", properties.getProperty("xmttr.loglevel", "0"), true);
        }

        // set log file
        if (properties.containsKey("xmttr.logfile")) {
            setProperty("xmttr.logfile", properties.getProperty("xmttr.logfile"), true);
        }
    }

    /**
     * Set properties
     * @param property    property name
     * @param value       property value
     * @param init        if true -> do not set this property to properties
     */
    private synchronized void setProperty(final String property, final String value, final boolean init) {
        if (!init) {
            // if property was set in command line -> skip setting it
            // command line has highest priority
            if (overridenProperties.containsKey(property)) {
                return;
            }

            // set property
            properties.setProperty(property, value);
            setProperties.setProperty(property, value);
        }

        // set log level
        if ("xmttr.loglevel".equals(property)) {
            try {
                Scribe.setLogLevel(Integer.parseInt(value));
            } catch (NumberFormatException e) {
                Scribe.setLogLevel(0);
            }
        } else

        // set log file
        if ("xmttr.logfile".equals(property)) {
            try {
                Scribe.setLogFile(value);
            } catch (NumberFormatException e) {
                Scribe.setLogFile(null);
            }
        }

    }

    /**
     * Set properties
     * @param property    property name
     * @param value       property value
     */
    public synchronized void setProperty(final String property, final String value) {
        setProperty(property, value, false);
    }

    /**
     * Get properties
     * @param property    property name
     * @return property value
     */
    public String getProperty(final String property) {
        return properties.getProperty(property);
    }

    /**
     * Get properties
     * @return properties
     */
    public Properties getProperties() {
        return properties;
    }

    /**
     * Get overriden properties
     * @return properties
     */
    public Properties getOverridenProperties() {
        return overridenProperties;
    }

    /**
     * Get set properties
     * @return properties
     */
    public Properties getSetProperties() {
        return setProperties;
    }

    /**
     * Load and run plugin(s)
     * @param cmd    parsed command line
     */
    private void loadPlugins(final CommandLine cmd) {
        File pluginFile;
        for (String pluginFileName : cmd.getArgs()) {
            pluginFile = new File(SCRIPTS_DIRECTORY + pluginFileName + (pluginFileName.endsWith(".js") ? "" : ".js"));
            if (!pluginFile.exists() || pluginFile.isDirectory()) {
                Scribe.log(this, 0, "Plugin " + pluginFileName + " doesn't exist");
                continue;
            }

            // load and execute JS script

            Context cx = new ContextFactory().enterContext();
            cx.setOptimizationLevel(9);
            cx.setLanguageVersion(Context.VERSION_1_7);
            FileReader pluginFileReader = null;

            try {
                // create new Rhino scope
                scope = RhinoScope.createRhinoScope(cx);

                // define global variable xMttr
                scope.defineProperty("xMttr", Context.javaToJS(this, scope), ScriptableObject.DONTENUM);

                // compile and evaluate plugin script
                pluginFileReader = new FileReader(pluginFile);
                Script scr = cx.compileReader(pluginFileReader, pluginFileName, 1, null);
                Object result = scr.exec(cx, scope);

                // log any script output
                if (result != null && result != Context.getUndefinedValue()) {
                    String resultStr = Context.toString(result);
                    if (resultStr != null && !resultStr.isEmpty()) {
                        Scribe.log(this, 0, resultStr);
                    }
                }

            } catch (Exception e) {
                Scribe.log(this, 0, "Error " + e.toString());
            } finally {
                if (pluginFileReader != null) {
                    try {
                        pluginFileReader.close();
                    } catch (Exception ignored) {}
                }
                Context.exit();
            }

        }
    }

    /**
     * Siege constructor
     * @param cmd    parsed command line
     */
    public XMttr(final CommandLine cmd) {
        if (INSTANCE != null) {
             Scribe.log(this, 0, "Do not initialize XMttr twice!");
             return;
        }
        INSTANCE = this;

        // set properties
        setProperties(getProperties(cmd));

        // test module
        if (cmd.hasOption("test")) {
            try {
                Loader.testModule(cmd.getOptionValue("test"));
            } catch (Exception e) {
                Scribe.log(this, 0, "Module testing FAIL: " + e.toString());
                return;
            }
            Scribe.log(this, 0, "Module is OK");
            return;
        }

        // load plugins
        loadPlugins(cmd);
    }

    /**
     * Get XMttr instance
     * @return XMttr
     */
    public static XMttr getInstance() {
        return INSTANCE;
    }

    public XMttrGenerator getGenerator() {
        return generator;
    }

    public void setGenerator(XMttrGenerator generator) {
        this.generator = generator;
    }

    public XMttrEmitter getEmitter() {
        return emitter;
    }

    public void setEmitter(XMttrEmitter emitter) {
        this.emitter = emitter;
    }

    public ScriptableObject getScope() {
        return scope;
    }
}
