package name.yumaa.xmttr;

import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author Victor Didenko
 * yumaa.verdin@gmail.com
 * 20.08.2014
 */
public class Scribe {

    /**
     * Log level
     * 0 -> reports
     * 1 -> +timing
     * 2 -> +xmls
     * 3 -> +full log
     */
    private static volatile AtomicInteger logLevel = new AtomicInteger(0);

    /**
     * Log file
     */
    private static volatile PrintWriter logFile = null;

    /**
     * Setting log level
     * @param logLevel    log level
     */
    public static void setLogLevel(int logLevel) {
        Scribe.logLevel.set(logLevel);
    }

    /**
     * Setter for log file name
     * @param logFileName    log file name
     */
    public static void setLogFile(String logFileName) {
        if (logFileName != null && !logFileName.isEmpty()) {
            synchronized (Scribe.class) {
                try {
                    logFile = new PrintWriter(new OutputStreamWriter(new FileOutputStream(logFileName, true), "UTF-8"), true);
                } catch (Exception e) {
                    Scribe.log(null, 0, "Can't open log file '" + logFileName + "': " + e);
                }
            }
        }
    }

    /**
     * Log message
     * @param str      string to print out
     */
    public static void log(String str) {
        log(null, 0, str);
    }

    /**
     * Log message
     * @param level    log level
     * @param str      string to print out
     */
    public static void log(int level, String str) {
        log(null, level, str);
    }

    /**
     * Log message
     * @param caller   caller object
     * @param level    log level
     * @param str      string to print out
     */
    public static void log(Object caller, int level, String str) {
        int lvl = logLevel.get();
        if (level > lvl) {
            return;
        }

        StringBuilder out = new StringBuilder();
        if (lvl >= 1) {
            out.append(System.currentTimeMillis())
               .append(" ");
        }
        if (lvl >= 3 && caller != null) {
            out.append(caller.toString())
               .append(" : ");
        }

        out.append(str);
        doLog(out.toString());
    }

    /**
     * Print out message, can be replaced with another log method
     * @param str    string to print out
     */
    private static void doLog(String str) {
        if (logFile == null) {
            System.out.println(str);
        } else {
            logFile.println(str);
        }
    }

    /**
     * Print message without line break
     * @param str    string to print out
     */
    public static void logLine(String str) {
        if (logFile == null) {
            System.out.print(str);
        } else {
            logFile.print(str);
        }
    }

    /**
     * Print line break
     * @param close    finish line
     */
    public static void logLine(boolean close) {
        if (close) {
            if (logFile == null) {
                System.out.println();
            } else {
                logFile.println();
            }
        }
    }

}
