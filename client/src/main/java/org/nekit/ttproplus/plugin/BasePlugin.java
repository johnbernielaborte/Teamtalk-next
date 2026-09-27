package org.nekit.ttproplus.plugin;

import android.content.Context;
import android.content.SharedPreferences;

import java.io.File;

import dk.bearware.TeamTalkBase;
import org.nekit.ttproplus.backend.TeamTalkService;

/**
 * Convenience base class for plugins implementing both Plugin and PluginEventListener.
 */
public abstract class BasePlugin implements Plugin, PluginEventListener {
    private PluginContext context;
    private PluginInfo info;

    @Override
    public void onLoad(PluginContext context) throws Exception {
        this.context = context;
        if (this.info == null && context != null) {
            this.info = context.getInfo();
        }
        // Auto-register this plugin instance as an event listener
        if (context != null) {
            context.registerEventListener(this);
        }
    }

    @Override
    public void onEnable() throws Exception {}

    @Override
    public void onDisable() throws Exception {}

    @Override
    public void onUnload() throws Exception {
        if (context != null) {
            context.unregisterEventListener(this);
        }
    }

    @Override
    public PluginInfo getInfo() {
        return info;
    }

    public void setInfo(PluginInfo info) {
        this.info = info;
    }

    public PluginContext getContext() {
        return context;
    }

    public Context getApplicationContext() {
        return context != null ? context.getApplicationContext() : null;
    }

    public TeamTalkService getService() {
        return context != null ? context.getService() : null;
    }

    public TeamTalkBase getClient() {
        return context != null ? context.getClient() : null;
    }

    public PluginLogger getLogger() {
        return context != null ? context.getLogger() : new PluginLogger(getClass().getSimpleName());
    }

    public File getDataFolder() {
        return context != null ? context.getDataFolder() : null;
    }

    public SharedPreferences getPreferences() {
        return context != null ? context.getPreferences() : null;
    }

    public void sendChannelMessage(String text) {
        if (context != null) context.sendChannelMessage(text);
    }

    public void sendUserMessage(int userId, String text) {
        if (context != null) context.sendUserMessage(userId, text);
    }

    public void sendBroadcastMessage(String text) {
        if (context != null) context.sendBroadcastMessage(text);
    }

    public void joinChannel(int channelId, String password) {
        if (context != null) context.joinChannel(channelId, password);
    }

    public void leaveChannel() {
        if (context != null) context.leaveChannel();
    }

    public void registerCommand(String command, PluginCommandHandler handler) {
        if (context != null) context.registerCommand(command, handler);
    }

    public void registerAction(PluginAction action) {
        if (context != null) context.registerAction(action);
    }

    public void runOnMainThread(Runnable action) {
        if (context != null) context.runOnMainThread(action);
    }

    public void runAsync(Runnable task) {
        if (context != null) context.runAsync(task);
    }
}
