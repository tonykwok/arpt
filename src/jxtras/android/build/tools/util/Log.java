/*
 * Copyright (C) 2022 Tony Guo. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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