package name.yumaa.xmttr.modules.jms;

import name.yumaa.xmttr.Scribe;

import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Victor Didenko
 * yumaa.verdin@gmail.com
 * 28.10.2014
 */
public class Client {

    public static final String DOT = ".";
    public static final String propStartWith = "xmttr.emitter";
    public static final String propFactory = "factory";
    public static final String propUrl = "url";
    public static final String propPrincipal = "principal";
    public static final String propCredentials = "credentials";
    public static final String propQueue = "queue";
    public static final String propConnection = "connection";

    public static final String defaultCtxName = "DEFAULT";
    private String defaultFactory;
    private String defaultUrl;
    private String defaultPrincipal;
    private String defaultCredentials;
    private String defaultQueue;
    private String defaultConnection;

    // Contexts
    private volatile ConcurrentHashMap<String,Context> ctx = new ConcurrentHashMap<String,name.yumaa.xmttr.modules.jms.Context>();

    /**
     * Return context by name
     * @param name    name
     * @return context
     */
    public Context get(final String name) {
        return ctx.get(name != null ? name : defaultCtxName);
    }

    /**
     * Return default context
     * @return context
     */
    public Context get() {
        return get(null);
    }

    /**
     * Add context by name
     * @param name          context name
     * @param properties    init properties
     * @return added context
     * @throws Exception
     */
    public synchronized Context add(String name, final Properties properties) throws Exception {
        String factory = properties.getProperty(propStartWith + DOT + name + DOT + propFactory, defaultFactory);
        String url = properties.getProperty(propStartWith + DOT + name + DOT + propUrl, defaultUrl);
        String principal = properties.getProperty(propStartWith + DOT + name + DOT + propPrincipal, defaultPrincipal);
        String credentials = properties.getProperty(propStartWith + DOT + name + DOT + propCredentials, defaultCredentials);
        String queue = properties.getProperty(propStartWith + DOT + name + DOT + propQueue, defaultQueue);
        String connection = properties.getProperty(propStartWith + DOT + name + DOT + propConnection, defaultConnection);

        Context context = new Context(factory, url, principal, credentials, queue, connection);
        ctx.put(name, context);
        return context;
    }

    /**
     * Add default context
     * @param properties    init properties
     * @return default context, if added
     */
    private synchronized Context add(final Properties properties) {
        defaultFactory = properties.getProperty(propStartWith + DOT + propFactory);
        defaultUrl = properties.getProperty(propStartWith + DOT + propUrl);
        defaultPrincipal = properties.getProperty(propStartWith + DOT + propPrincipal);
        defaultCredentials = properties.getProperty(propStartWith + DOT + propCredentials);
        defaultQueue = properties.getProperty(propStartWith + DOT + propQueue);
        defaultConnection = properties.getProperty(propStartWith + DOT + propConnection);

        // try to create default context
        Context context = null;
        try {
            context = new Context(defaultFactory, defaultUrl, defaultPrincipal, defaultCredentials, defaultQueue, defaultConnection);
            ctx.put(defaultCtxName, context);
        } catch (Exception e) {
            Scribe.log(this, 3, "Can't create default context: " + e);
        }
        return context;
    }

    /**
     * Constructor
     * @param properties    properties
     */
    public Client(final Properties properties) throws Exception {
        Scribe.log(this, 3, "Create JMS client");

        // add default context
        add(properties);

        // add named contexts
        Pattern pattern = Pattern.compile("^" + Pattern.quote(propStartWith) + "\\.(\\w+)\\.\\w+$");
        for (String name : properties.stringPropertyNames()) {
            Matcher matcher = pattern.matcher(name);
            if (matcher.find()) {
                add(matcher.group(1), properties);
            }
        }
    }

}
