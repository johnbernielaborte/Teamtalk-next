package org.nekit.ttproplus.plugin;

/**
 * Main interface that all TeamTalk Next plugins must implement.
 */
public interface Plugin {

    /**
     * Called once when the plugin is loaded into memory.
     * Use this method to initialize configuration, register listeners, commands, and actions.
     */
    void onLoad(PluginContext context) throws Exception;

    /**
     * Called when the plugin is enabled.
     */
    void onEnable() throws Exception;

    /**
     * Called when the plugin is disabled by the user or system.
     */
    void onDisable() throws Exception;

    /**
     * Called when the plugin is being unloaded. Clean up resources here.
     */
    void onUnload() throws Exception;

    /**
     * Returns the metadata descriptor for this plugin.
     */
    PluginInfo getInfo();
}
