package jxtras.android.build.tools.util;

import java.io.PrintStream;

/**
 * Utility class only for arpt logging.
 * <p>
 * The log level can be set using for example -log:DEBUG on the arpt command line.
 */
public class Log {
    private static PrintStream out, err;

    public final static int WARN = 1;
    public final static int INFO = 2;
    public final static int TIMING = 3;
    public final static int DEBUG = 4;
    public final static int TRACE = 5;
    private static int level = WARN;

    static public void trace(String msg) {
        if (level >= TRACE) {
            out.println(msg);
        }
    }

    static public void debug(String msg) {
        if (level >= DEBUG) {
            out.println(msg);
        }
    }

    static public void timing(String msg) {
        if (level >= TIMING) {
            out.println(msg);
        }
    }

    static public void info(String msg) {
        if (level >= INFO) {
            out.println(msg);
        }
    }

    static public void warn(String msg) {
        err.println(msg);
    }

    static public void error(String msg) {
        err.println(msg);
    }

    static public void initializeLog(PrintStream o, PrintStream e) {
        out = o;
        err = e;
    }

    static public void setLogLevel(String l) {
        switch (l) {
            case "warn": level = WARN; break;
            case "info": level = INFO; break;
            case "timing": level = TIMING; break;
            case "debug": level = DEBUG; break;
            case "trace": level = TRACE; break;
            default:
                throw new IllegalArgumentException("No such log level \"" + l + "\"");
        }
    }

    static public boolean isTracing() {
        return level >= TRACE;
    }

    static public boolean isDebugging() {
        return level >= DEBUG;
    }
}