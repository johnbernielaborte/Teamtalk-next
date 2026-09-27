package org.nekit.ttproplus.plugin;

/**
 * Handler interface for slash chat commands registered by plugins.
 */
@FunctionalInterface
public interface PluginCommandHandler {
    /**
     * Executes the registered command.
     *
     * @param sender command sender (local user or remote user)
     * @param command command name without slash (e.g. "ping")
     * @param args arguments passed after the command
     * @return true if command was handled, false otherwise
     */
    boolean onCommand(PluginCommandSender sender, String command, String[] args);
}
