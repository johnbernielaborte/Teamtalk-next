package org.nekit.plugin.echo;

import org.nekit.ttproplus.plugin.BasePlugin;
import org.nekit.ttproplus.plugin.PluginContext;
import dk.bearware.TextMessage;
import dk.bearware.TextMsgType;

public class EchoBotPlugin extends BasePlugin {

    @Override
    public void onLoad(PluginContext context) throws Exception {
        super.onLoad(context);
        getLogger().info("EchoBotPlugin initialized");

        registerCommand("echoping", (sender, command, args) -> {
            sender.sendMessage("Pong from EchoBotPlugin! Args count: " + args.length);
            return true;
        });
    }

    @Override
    public void onEnable() throws Exception {
        getLogger().info("EchoBotPlugin enabled");
    }

    @Override
    public void onDisable() throws Exception {
        getLogger().info("EchoBotPlugin disabled");
    }

    @Override
    public boolean onTextMessageReceived(TextMessage message) {
        if (message == null || message.szMessage == null) return false;

        // Auto-reply to private messages
        if (message.nMsgType == TextMsgType.MSGTYPE_USER) {
            if (getClient() != null && message.nFromUserID != getClient().getMyUserID()) {
                String reply = "Эхо: " + message.szMessage;
                sendUserMessage(message.nFromUserID, reply);
                getLogger().info("Отвечено пользователю #" + message.nFromUserID + ": " + reply);
            }
        }
        return false;
    }
}
