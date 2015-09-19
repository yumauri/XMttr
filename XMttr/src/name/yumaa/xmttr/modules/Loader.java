package name.yumaa.xmttr.modules;

import name.yumaa.xmttr.*;

import java.util.Properties;

/**
 * @author Victor Didenko
 * yumaa.verdin@gmail.com
 * 22.10.2014
 */
public class Loader {

    /**
     * Instantiate module
     * @param moduleName    module name
     * @return module
     * @throws Exception
     */
    public static XMttrModule instantiateModule(String moduleName) throws Exception {
        return (XMttrModule) Class.forName(moduleName, true, JarClassLoader.getInstance()).newInstance();
    }

    /**
     * Load module
     * @param moduleName    module name
     * @return module
     * @throws Exception
     */
    public static XMttrModule loadModule(String moduleName) throws Exception {
        XMttr xmttr = XMttr.getInstance();
        XMttrModule module;

        // try to instantiate module
        try {
            module = instantiateModule(moduleName);
        } catch (Exception e) {
            throw new Exception("Can't instantiate module " + moduleName + " : " + e.getMessage());
        }

        // set module as generator or emitter
        if (module != null) {
            if (module instanceof XMttrGenerator) {
                xmttr.setGenerator((XMttrGenerator) module);
            } else
            if (module instanceof XMttrEmitter) {
                xmttr.setEmitter((XMttrEmitter) module);
            }
        }

        return module;
    }

    /**
     * Initialize module
     * @param module    module
     */
    public static void initModule(XMttrModule module) throws Exception {
        XMttr xmttr = XMttr.getInstance();

        // global, all properties
        Properties properties = xmttr.getProperties();

        // properties from command line
        Properties overridenProperties = xmttr.getOverridenProperties();

        // properties from plugin
        Properties setProperties = xmttr.getSetProperties();

        // module properties
        String moduleName = module.getClass().getCanonicalName();
        String moduleDefaultConfName = moduleName + "-defaults.properties";
        Properties moduleDefaultProperties = Bootstrap.getResourceProperties(moduleDefaultConfName);

        // module properties
        Properties moduleProperties;
        String moduleConfName = moduleName + ".properties";
        if (properties.containsKey(moduleConfName)) {
            moduleConfName = properties.getProperty(moduleConfName);
            moduleProperties = Bootstrap.getProperties(moduleConfName);
        } else {
            moduleProperties = Bootstrap.getProperties(XMttr.CONF_DIRECTORY + moduleName + ".properties");
        }

        // merge properties
        moduleProperties = Bootstrap.mergeProperties(moduleDefaultProperties, properties, moduleProperties, setProperties, overridenProperties);

        // init module
        Scribe.log(null, 3, "Init module " + moduleName + " with properties: " + moduleProperties);
        module.init(moduleProperties);
    }

    /**
     * Test module
     * @param moduleName    module name
     * @throws Exception
     */
    public static void testModule(String moduleName) throws Exception {
        Scribe.setLogLevel(3);

        XMttrModule module;

        // try to instantiate module
        try {
            Scribe.log(null, 3, "Try to instantiate module " + moduleName);
            module = instantiateModule(moduleName);
        } catch (Exception e) {
            throw new Exception("Can't instantiate module " + moduleName + " : " + e.getMessage());
        }
        if (module == null) {
            throw new Exception("Can't instantiate module " + moduleName + ", returned null");
        }

        // set module as generator or emitter
        if (module instanceof XMttrGenerator) {
            Scribe.log(null, 3, "Module is generator (XMttrGenerator)");
        } else
        if (module instanceof XMttrEmitter) {
            Scribe.log(null, 3, "Module is emitter (XMttrEmitter)");
        } else {
            throw new Exception("Module " + moduleName + " neither generator (XMttrGenerator) nor emitter (XMttrEmitter)");
        }

        // try to init
        Scribe.log(null, 3, "Try to init module");
        initModule(module);
    }
}
