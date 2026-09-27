package org.nekit.ttproplus.plugin;

import dk.bearware.Channel;
import dk.bearware.TextMessage;
import dk.bearware.User;

/**
 * Event listener interface with default implementations for plugins.
 */
public interface PluginEventListener {

    // Connection events
    default void onConnectSuccess() {}
    default void onConnectFailed() {}
    default void onConnectionLost() {}
    default void onLoggedOn(User myUser) {}
    default void onLoggedOut() {}

    // Message events
    /**
     * Called when an incoming text message is received.
     * @param message TextMessage object
     * @return true if the event should be consumed/suppressed, false to let it pass through.
     */
    default boolean onTextMessageReceived(TextMessage message) {
        return false;
    }

    /**
     * Called when an outgoing text message is about to be sent.
     * @param message TextMessage object
     * @return true if sending should be cancelled, false to allow sending.
     */
    default boolean onTextMessageSending(TextMessage message) {
        return false;
    }

    // Channel events
    default void onChannelJoined(Channel channel) {}
    default void onChannelLeft(Channel channel) {}
    default void onChannelAdded(Channel channel) {}
    default void onChannelUpdated(Channel channel) {}
    default void onChannelRemoved(Channel channel) {}

    // User events
    default void onUserJoined(User user) {}
    default void onUserLeft(User user) {}
    default void onUserUpdated(User user) {}
    default void onUserSpeaking(User user, boolean speaking) {}

    // Voice & Audio
    default void onVoiceTransmissionStateChanged(boolean transmitting) {}
}
