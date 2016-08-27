// Copyright (c) 2016 Jae-jun Kang
// See the file LICENSE for details.

package x2.util;

import java.util.*;

import x2.*;

public final class Log {
    public static enum Level {
        All,
        Trace,
        Debug,
        Info,
        Warning,
        Error,
        None
    }

    public static interface Listener {
        void log(Level level, String message);
    }

    private static Level level = Level.Debug;
    private static ArrayList<Listener> listeners;

    // Private constructor to prohibit explicit initialization
    private Log() {}

    static {
        listeners = new ArrayList<Listener>();
    }

    public static Level level() {
        return level;
    }

    public static void level(Level value) {
        level = value;
    }

    public static void addListener(Listener listener) {
        listeners.add(listener);
    }

    private static void emit(Level level, String message) {
        if (listeners.isEmpty() || Log.level.ordinal() > level.ordinal()) {
            return;
        }
        for (int i = 0, count = listeners.size(); i < count; ++i) {
            listeners.get(i).log(level, message);
        }
    }

    public static void log(Level level, String message) {
        emit(level, message);
    }

    public static void log(Level level, String format, Object... args) {
        emit(level, String.format(format, args));
    }

    public static void trace(String message) {
        log(Level.Trace, message);
    }
    public static void trace(String format, Object... args) {
        log(Level.Trace, format, args);
    }

    public static void debug(String message) {
        log(Level.Debug, message);
    }
    public static void debugt(String format, Object... args) {
        log(Level.Debug, format, args);
    }

    public static void info(String message) {
        log(Level.Info, message);
    }
    public static void info(String format, Object... args) {
        log(Level.Info, format, args);
    }

    public static void warn(String message) {
        log(Level.Warning, message);
    }
    public static void warn(String format, Object... args) {
        log(Level.Warning, format, args);
    }

    public static void error(String message) {
        log(Level.Error, message);
    }
    public static void error(String format, Object... args) {
        log(Level.Error, format, args);
    }
}
