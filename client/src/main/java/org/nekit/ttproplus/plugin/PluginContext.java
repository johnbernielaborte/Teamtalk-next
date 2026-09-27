package org.nekit.ttproplus.plugin;

import android.content.Context;
import android.content.SharedPreferences;

import java.io.File;
import java.util.concurrent.ScheduledFuture;

import dk.bearware.TeamTalkBase;
import dk.bearware.TextMessage;
import org.nekit.ttproplus.backend.TeamTalkService;

/**
 * Execution context supplied to a plugin on load.
 * Provides full developer-level access to TeamTalk services, networking, scheduling,
 * file storage, event listeners, commands, and UI actions.
 */
public interface PluginContext {

    /**
     * Application Android Context.
     */
    Context getApplicationContext();

    /**
     * Active TeamTalkService instance (may be null if service has stopped).
     */
    TeamTalkService getService();

    /**
     * Active TeamTalkBase JNI client instance.
     */
    TeamTalkBase getClient();

    /**
     * Plugin metadata descriptor.
     */
    PluginInfo getInfo();

    /**
     * Dedicated logger for this plugin.
     */
    PluginLogger getLogger();

    /**
     * Private persistent storage directory for this plugin's files.
     */
    File getDataFolder();

    /**
     * Plugin private SharedPreferences.
     */
    SharedPreferences getPreferences();

    /**
     * Send a raw TextMessage through TeamTalk.
     */
    void sendTextMessage(TextMessage msg);

    /**
     * Send a channel text message to the current channel.
     */
    void sendChannelMessage(String text);

    /**
     * Send a private text message to a specific user ID.
     */
    void sendUserMessage(int userId, String text);

    /**
     * Send a server broadcast text message.
     */
    void sendBroadcastMessage(String text);

    /**
     * Join channel by its numeric ID and optional password.
     */
    void joinChannel(int channelId, String password);

    /**
     * Leave the current channel.
     */
    void leaveChannel();

    /**
     * Run action on the Android main/UI thread.
     */
    void runOnMainThread(Runnable action);

    /**
     * Run background task asynchronously on the plugin thread pool.
     */
    void runAsync(Runnable task);

    /**
     * Schedule a one-time delayed task.
     */
    ScheduledFuture<?> scheduleTask(Runnable task, long delayMs);

    /**
     * Schedule a recurring periodic task.
     */
    ScheduledFuture<?> schedulePeriodicTask(Runnable task, long initialDelayMs, long periodMs);

    /**
     * Register an event listener for TeamTalk events.
     */
    void registerEventListener(PluginEventListener listener);

    /**
     * Unregister an event listener.
     */
    void unregisterEventListener(PluginEventListener listener);

    /**
     * Register a chat slash command (e.g. "ping" for "/ping").
     */
    void registerCommand(String command, PluginCommandHandler handler);

    /**
     * Unregister a chat slash command.
     */
    void unregisterCommand(String command);

    /**
     * Register a custom UI action.
     */
    void registerAction(PluginAction action);

    /**
     * Unregister a custom UI action.
     */
    void unregisterAction(String actionId);
}
