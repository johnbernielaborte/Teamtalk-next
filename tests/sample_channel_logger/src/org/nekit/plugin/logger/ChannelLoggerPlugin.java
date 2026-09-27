package org.nekit.plugin.logger;

import org.nekit.ttproplus.plugin.BasePlugin;
import org.nekit.ttproplus.plugin.PluginContext;
import dk.bearware.Channel;
import dk.bearware.User;

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class ChannelLoggerPlugin extends BasePlugin {

    private File logFile;

    @Override
    public void onLoad(PluginContext context) throws Exception {
        super.onLoad(context);
        File dataDir = getDataFolder();
        if (dataDir != null) {
            logFile = new File(dataDir, "channel_events.log");
        }
        getLogger().info("ChannelLoggerPlugin loaded, log file: " + logFile);

        registerCommand("logstatus", (sender, command, args) -> {
            long size = (logFile != null && logFile.exists()) ? logFile.length() : 0;
            sender.sendMessage("📝 Логгер канала активен. Размер лог-файла: " + size + " байт");
            return true;
        });
    }

    private void appendLog(String event) {
        if (logFile == null) return;
        runAsync(() -> {
            try (FileWriter fw = new FileWriter(logFile, true);
                 PrintWriter pw = new PrintWriter(fw)) {
                String time = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US).format(new Date());
                pw.println("[" + time + "] " + event);
            } catch (Exception e) {
                getLogger().error("Failed to write to log file", e);
            }
        });
    }

    @Override
    public void onChannelJoined(Channel channel) {
        if (channel != null) {
            String msg = "Вход в канал: " + channel.szName + " (#" + channel.nChannelID + ")";
            getLogger().info(msg);
            appendLog(msg);
        }
    }

    @Override
    public void onChannelLeft(Channel channel) {
        if (channel != null) {
            String msg = "Выход из канала: " + channel.szName + " (#" + channel.nChannelID + ")";
            getLogger().info(msg);
            appendLog(msg);
        }
    }

    @Override
    public void onUserJoined(User user) {
        if (user != null) {
            String msg = "Пользователь вошел: " + user.szNickname + " (#" + user.nUserID + ") в канал #" + user.nChannelID;
            getLogger().info(msg);
            appendLog(msg);
        }
    }

    @Override
    public void onUserLeft(User user) {
        if (user != null) {
            String msg = "Пользователь вышел: " + user.szNickname + " (#" + user.nUserID + ")";
            getLogger().info(msg);
            appendLog(msg);
        }
    }
}
