package org.nekit.ttproplus.plugin;

/**
 * Interface representing a sender executing a plugin command.
 */
public interface PluginCommandSender {
    /**
     * Send a response message back to the sender.
     */
    void sendMessage(String message);

    /**
     * Check if this sender is the local client user.
     */
    boolean isLocalUser();

    /**
     * Get the user ID of the sender.
     */
    int getSenderUserId();

    /**
     * Get the channel ID where the command was executed, or 0 if private.
     */
    int getChannelId();
}
