# 02. Quick Start: Creating Your First Plugin

In this tutorial, we will build a simple "Echo Bot" plugin that intercepts private messages and automatically responds to the sender.

---

## 📁 Plugin Project Layout

A plugin consists of a `plugin.json` manifest and standard Java source files:

```text
my-echo-plugin/
├── plugin.json
└── src/
    └── org/
        └── example/
            └── echo/
                └── EchoBotPlugin.java
```

---

## 📝 Step 1. Create `plugin.json` Manifest

Create `plugin.json` in the root of your project:

```json
{
  "id": "org.example.echobot",
  "name": "Echo Bot",
  "version": "1.0.0",
  "versionCode": 1,
  "author": "Developer",
  "description": "Automatically replies to incoming private messages.",
  "mainClass": "org.example.echo.EchoBotPlugin",
  "minClientVersion": "5.28.6"
}
```

### Manifest Fields:
* `id`: Unique identifier (e.g. `com.company.plugin`).
* `name`: User-facing plugin name shown in the UI.
* `version`: Version string.
* `versionCode`: Integer revision code.
* `author`: Author name or handle.
* `description`: Summary of what the plugin does.
* `mainClass`: Fully qualified class name implementing `Plugin`.
* `minClientVersion`: Minimum required TeamTalk Next version (`5.28.6` or higher).

---

## 💻 Step 2. Write Plugin Java Code (`EchoBotPlugin.java`)

Extend `BasePlugin` to inherit lifecycle and event methods:

```java
package org.example.echo;

import org.nekit.ttproplus.plugin.BasePlugin;
import org.nekit.ttproplus.plugin.PluginContext;
import dk.bearware.TextMessage;
import dk.bearware.TextMsgType;

public class EchoBotPlugin extends BasePlugin {

    @Override
    public void onLoad(PluginContext context) throws Exception {
        super.onLoad(context);
        getLogger().info("Echo Bot loaded successfully!");

        // Register a status check slash command /echostatus
        registerCommand("echostatus", (sender, command, args) -> {
            sender.sendMessage("🟢 Echo Bot is active and running.");
            return true;
        });
    }

    @Override
    public void onEnable() throws Exception {
        getLogger().info("Echo Bot enabled.");
    }

    @Override
    public void onDisable() throws Exception {
        getLogger().info("Echo Bot disabled.");
    }

    @Override
    public boolean onTextMessageReceived(TextMessage message) {
        // Check if this is an incoming private message (MSGTYPE_USER)
        if (message.nMsgType == TextMsgType.MSGTYPE_USER && message.szMessage != null) {
            String incoming = message.szMessage.trim();

            // Reply if message is from another user
            if (getClient() != null && message.nFromUserID != getClient().getMyUserID()) {
                String reply = "Echo: " + incoming;
                sendUserMessage(message.nFromUserID, reply);
                getLogger().info("Replied to user #" + message.nFromUserID);
            }
        }
        // Return false to let the message show in the normal chat UI
        return false;
    }
}
```

---

## 🚀 Next Steps
Proceed to [**05. Building & Packaging**](05_building_and_packaging.md) to compile this into a `.jar` package ready to install on your Android device!
