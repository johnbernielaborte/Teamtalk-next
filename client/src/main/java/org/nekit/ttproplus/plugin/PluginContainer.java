package org.nekit.ttproplus.plugin;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Internal container encapsulating a loaded plugin instance and its state.
 */
public class PluginContainer {
    private final PluginInfo info;
    private final Plugin plugin;
    private final ClassLoader classLoader;
    private final File sourceFile;
    private boolean enabled;
    private String lastError;

    private final List<PluginEventListener> eventListeners = new CopyOnWriteArrayList<>();
    private final List<String> registeredCommands = new ArrayList<>();
    private final List<String> registeredActions = new ArrayList<>();

    public PluginContainer(PluginInfo info, Plugin plugin, ClassLoader classLoader, File sourceFile) {
        this.info = info;
        this.plugin = plugin;
        this.classLoader = classLoader;
        this.sourceFile = sourceFile;
        this.enabled = false;
        this.lastError = null;
    }

    public PluginInfo getInfo() {
        return info;
    }

    public Plugin getPlugin() {
        return plugin;
    }

    public ClassLoader getClassLoader() {
        return classLoader;
    }

    public File getSourceFile() {
        return sourceFile;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getLastError() {
        return lastError;
    }

    public void setLastError(String lastError) {
        this.lastError = lastError;
    }

    public List<PluginEventListener> getEventListeners() {
        return eventListeners;
    }

    public void addEventListener(PluginEventListener listener) {
        if (listener != null && !eventListeners.contains(listener)) {
            eventListeners.add(listener);
        }
    }

    public void removeEventListener(PluginEventListener listener) {
        if (listener != null) {
            eventListeners.remove(listener);
        }
    }

    public List<String> getRegisteredCommands() {
        return registeredCommands;
    }

    public void addRegisteredCommand(String cmd) {
        if (cmd != null && !registeredCommands.contains(cmd.toLowerCase())) {
            registeredCommands.add(cmd.toLowerCase());
        }
    }

    public List<String> getRegisteredActions() {
        return registeredActions;
    }

    public void addRegisteredAction(String actionId) {
        if (actionId != null && !registeredActions.contains(actionId)) {
            registeredActions.add(actionId);
        }
    }
}
