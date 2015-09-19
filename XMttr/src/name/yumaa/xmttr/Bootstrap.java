package name.yumaa.xmttr;

import name.yumaa.xmttr.modules.JarClassLoader;
import org.apache.commons.cli.*;

import java.io.*;
import java.util.Map;
import java.util.Properties;

/**
 * @author Victor Didenko
 * yumaa.verdin@gmail.com
 * 22.10.2014
 */
public class Bootstrap {

    /**
     * Merge properties
     * @param props    list of properties
     * @return result properties
     */
    public static Properties mergeProperties(Properties ... props) {
        Properties result = new Properties();
        for (Properties prop : props) {
            if (prop != null) {
                for (Map.Entry<Object, Object> entry : prop.entrySet()) {
                    String key = (String) entry.getKey();
                    String value = (String) entry.getValue();
                    result.setProperty(key, value);
                }
            }
        }
        return result;
    }

    /**
     * Get properties from resources
     * @param confFileName    properties file name
     * @return properties
     */
    public static Properties getResourceProperties(final String confFileName) {
        Properties properties = new Properties();

        // read configuration file from resources, if any
        InputStream in = null;
        try {
            in = Thread.currentThread().getContextClassLoader().getResourceAsStream(confFileName);
            if (in != null) {
                properties.load(in);
            }
        } catch (IOException e) {
            Scribe.log(null, 0, "Could not read " + confFileName + " file: " + e);
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException ignored) {}
            }
        }

        return properties;
    }

    /**
     * Get properties from file
     * @param confFileName    properties file name
     * @return properties
     */
    public static Properties getProperties(final String confFileName) {
        Properties properties = new Properties();

        // read configuration file, if any
        File confFile = new File(confFileName);
        if (confFile.exists() && !confFile.isDirectory()) {
            InputStream in = null;
            try {
                in = new FileInputStream(confFile);
                properties.load(in);
            } catch (IOException e) {
                Scribe.log(null, 0, "Could not read " + confFileName + " file: " + e);
            } finally {
                if (in != null) {
                    try {
                        in.close();
                    } catch (IOException ignored) {}
                }
            }
        }

        return properties;
    }

    /**
     * Enter point, program starts here
     * @param args    cli arguments
     */
    public static void main(final String[] args) {

        /*** read cli arguments ***/

        // add options
        Options options = new Options();
        options.addOption("c", "config", true, "path to configuration file,\ndefault \"conf/xmttr.properties\"");
        options.addOption("h", "help", false, "print this usage");
        options.addOption("v", "version", false, "print version");
        //noinspection AccessStaticViaInstance
        options.addOption(OptionBuilder.withArgName("module")
                                       .hasArgs()
                                       .withDescription("test module")
                                       .create("test"));
        //noinspection AccessStaticViaInstance
        options.addOption(OptionBuilder.withArgName("property=value")
                                       .hasArgs(2)
                                       .withValueSeparator()
                                       .withDescription("use value for given property\nhas highest priority")
                                       .create("D"));

        // parse command line
        CommandLineParser parser = new GnuParser();
        CommandLine cmd;
        try {
            cmd = parser.parse(options, args, false);
        } catch (ParseException e) {
            Scribe.log(null, 0, "Arguments parsing failed: " + e.getMessage() + "\n");
            printUsage(options);
            return;
        }

        // print usage help
        if (cmd == null || cmd.hasOption("h")) {
            printUsage(options);
            return;
        }

        // print version
        if (cmd.hasOption("v")) {
            printVersion();
            return;
        }

        // print usage if there is no plugin defined
        if (cmd.getArgs().length == 0 && !cmd.hasOption("test")) {
            Scribe.log(null, 0, "Define plugin to use!\n");
            printUsage(options);
            return;
        }

        /*** initialize XMttr ***/

        Thread.currentThread().setContextClassLoader(JarClassLoader.getInstance());
        new XMttr(cmd);
    }

    /**
     * Print version and release date
     */
    public static void printVersion() {
        System.out.println("XMttr v" + XMttr.RELEASE_VERSION + " (" + XMttr.RELEASE_DATE + ")");
    }

    /**
     * Print usage help
     * @param options    cli options
     */
    public static void printUsage(final Options options) {
        HelpFormatter formatter = new HelpFormatter();
        printVersion();
        formatter.printHelp("java -jar XMttr.jar <plugin>", options);
    }
}
