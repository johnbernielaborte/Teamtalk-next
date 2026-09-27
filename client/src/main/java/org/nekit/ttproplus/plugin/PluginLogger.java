package org.nekit.ttproplus.plugin;

import android.util.Log;

/**
 * Scoped logger for TeamTalk Next plugins.
 */
public class PluginLogger {
    private final String tag;

    public PluginLogger(String pluginName) {
        this.tag = "TTPlugin[" + pluginName + "]";
    }

    public void info(String message) {
        Log.i(tag, message != null ? message : "null");
    }

    public void debug(String message) {
        Log.d(tag, message != null ? message : "null");
    }

    public void warn(String message) {
        Log.w(tag, message != null ? message : "null");
    }

    public void error(String message) {
        Log.e(tag, message != null ? message : "null");
    }

    public void error(String message, Throwable throwable) {
        Log.e(tag, message != null ? message : "null", throwable);
    }
}
