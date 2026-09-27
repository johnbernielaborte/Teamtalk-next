# 04. Slash Commands & UI Actions

TeamTalk Next plugins can register interactive commands in the chat input bar and add custom UI buttons and dialogs.

---

## ⌨️ Registering Chat Slash Commands

Slash commands offer quick interaction directly from channel or private chats.

### Registering a Command:
Call `registerCommand(name, handler)` inside `onLoad`:

```java
registerCommand("calc", (sender, command, args) -> {
    if (args.length < 3) {
        sender.sendMessage("Usage: /calc <num1> <+|-|*|/> <num2>");
        return true;
    }

    try {
        double a = Double.parseDouble(args[0]);
        String op = args[1];
        double b = Double.parseDouble(args[2]);
        double res = 0;

        switch (op) {
            case "+": res = a + b; break;
            case "-": res = a - b; break;
            case "*": res = a * b; break;
            case "/": res = b != 0 ? a / b : Double.NaN; break;
            default:
                sender.sendMessage("Unknown operator: " + op);
                return true;
        }

        sender.sendMessage("Result: " + a + " " + op + " " + b + " = " + res);
    } catch (NumberFormatException e) {
        sender.sendMessage("Error: invalid number format!");
    }
    return true;
});
```

### `PluginCommandSender` Interface:
When a command executes, the plugin receives a `sender` object:
* `void sendMessage(String message)`: Sends a feedback message back to the sender (displayed as Toast or notification).
* `boolean isLocalUser()`: Returns `true` if typed by the local device user.
* `int getSenderUserId()`: User ID of the command issuer.
* `int getChannelId()`: Channel ID where the command was typed.

---

## 🎨 UI Buttons & Dialogs (`PluginAction`)

Plugins can add custom action buttons to their card in the Plugin Manager screen:

```java
registerAction(new PluginAction() {
    @Override
    public String getId() {
        return "open_plugin_about";
    }

    @Override
    public String getTitle() {
        return "About Plugin";
    }

    @Override
    public void onActionClick(Context context) {
        new AlertDialog.Builder(context)
            .setTitle("About Plugin")
            .setMessage("Version: " + getInfo().getVersion() + "\nAuthor: " + getInfo().getAuthor())
            .setPositiveButton("OK", null)
            .show();
    }
});
```

The button with label `getTitle()` will automatically appear in the plugin card inside the **Plugin Manager** screen.
