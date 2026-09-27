package org.nekit.plugin.utility;

import org.nekit.ttproplus.plugin.BasePlugin;
import org.nekit.ttproplus.plugin.PluginContext;

public class UtilityCommandsPlugin extends BasePlugin {

    @Override
    public void onLoad(PluginContext context) throws Exception {
        super.onLoad(context);
        getLogger().info("UtilityCommandsPlugin loaded");

        // /ping
        registerCommand("ping", (sender, command, args) -> {
            dk.bearware.ClientStatistics stats = new dk.bearware.ClientStatistics();
            int ping = -1;
            if (getClient() != null && getClient().getClientStatistics(stats)) {
                ping = stats.nUdpPingTimeMs >= 0 ? stats.nUdpPingTimeMs : stats.nTcpPingTimeMs;
            }
            if (ping >= 0) {
                sender.sendMessage("🏓 Понг! Задержка сети: " + ping + " мс");
            } else {
                sender.sendMessage("🏓 Понг! (нет активного соединения)");
            }
            return true;
        });

        // /roll [max]
        registerCommand("roll", (sender, command, args) -> {
            int max = 100;
            if (args.length > 0) {
                try {
                    max = Integer.parseInt(args[0]);
                    if (max <= 0) max = 100;
                } catch (NumberFormatException ignored) {}
            }
            int rolled = (int) (Math.random() * max) + 1;
            sendChannelMessage("🎲 Бросок кубика: " + rolled + " (из " + max + ")");
            return true;
        });

        // /channelid
        registerCommand("channelid", (sender, command, args) -> {
            int chanId = sender.getChannelId();
            if (chanId <= 0 && getClient() != null) {
                chanId = getClient().getMyChannelID();
            }
            sender.sendMessage("📍 Текущий ID канала: #" + chanId);
            return true;
        });

        // /calc <a + b>
        registerCommand("calc", (sender, command, args) -> {
            if (args.length < 3) {
                sender.sendMessage("Использование: /calc <число1> <+|-|*|/> <число2>");
                return true;
            }
            try {
                double a = Double.parseDouble(args[0]);
                String op = args[1];
                double b = Double.parseDouble(args[2]);
                double res;
                switch (op) {
                    case "+": res = a + b; break;
                    case "-": res = a - b; break;
                    case "*": res = a * b; break;
                    case "/": res = b != 0 ? a / b : Double.NaN; break;
                    default:
                        sender.sendMessage("Неизвестная операция: " + op);
                        return true;
                }
                sendChannelMessage("🧮 " + a + " " + op + " " + b + " = " + res);
            } catch (NumberFormatException e) {
                sender.sendMessage("Ошибка: некорректный формат чисел");
            }
            return true;
        });
    }

    @Override
    public void onEnable() throws Exception {
        getLogger().info("UtilityCommandsPlugin enabled");
    }

    @Override
    public void onDisable() throws Exception {
        getLogger().info("UtilityCommandsPlugin disabled");
    }
}
