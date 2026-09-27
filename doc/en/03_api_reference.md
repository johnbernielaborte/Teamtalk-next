# 03. Plugin API Reference

Detailed technical reference for the core interfaces and methods available to TeamTalk Next plugin developers.

---

## 📌 `PluginContext` Interface

Delivered to `onLoad(PluginContext context)`. Accessible via `getContext()` inside `BasePlugin`.

### Core Getters:
* `Context getApplicationContext()`: Android application context.
* `TeamTalkService getService()`: Active `TeamTalkService` instance (audio streaming, background VoIP connection).
* `TeamTalkBase getClient()`: Native TeamTalk 5 C++ JNI SDK instance (invoke any client SDK method).
* `PluginInfo getInfo()`: Metadata descriptor of this plugin.
* `PluginLogger getLogger()`: Dedicated logger with `[TTPlugin/<Name>]` tag.
* `File getDataFolder()`: Dedicated persistent folder in app internal storage for configuration and databases.
* `SharedPreferences getPreferences()`: Isolated `SharedPreferences` for plugin settings.

### Messaging Methods:
* `void sendTextMessage(TextMessage msg)`: Send a raw TeamTalk message object.
* `void sendChannelMessage(String text)`: Send text to the current channel.
* `void sendUserMessage(int userId, String text)`: Send a private text message to a specific user.
* `void sendBroadcastMessage(String text)`: Send server-wide broadcast (requires operator rights).

### Channel Management:
* `void joinChannel(int channelId, String password)`: Join channel by ID with optional password.
* `void leaveChannel()`: Leave the current channel.

### Threading & Schedulers:
* `void runOnMainThread(Runnable action)`: Post execution to Android's UI thread (show Toasts, dialogs).
* `void runAsync(Runnable task)`: Execute asynchronously in the plugin thread pool.
* `ScheduledFuture<?> scheduleTask(Runnable task, long delayMs)`: One-shot delayed task.
* `ScheduledFuture<?> schedulePeriodicTask(Runnable task, long initialDelayMs, long periodMs)`: Recurring task.

### Listeners & Commands:
* `void registerEventListener(PluginEventListener listener)`: Subscribe to client events.
* `void unregisterEventListener(PluginEventListener listener)`: Unsubscribe from events.
* `void registerCommand(String command, PluginCommandHandler handler)`: Add a slash command.
* `void unregisterCommand(String command)`: Remove a slash command.
* `void registerAction(PluginAction action)`: Add a UI button to the plugin card.

---

## 👂 `PluginEventListener` Interface

All methods have `default` empty implementations; override only those needed.

### Networking & Authentication:
| Method | Description |
|---|---|
| `void onConnectSuccess()` | Successful TCP/UDP connection to TeamTalk server |
| `void onConnectFailed()` | Connection failure |
| `void onConnectionLost()` | Connection dropped |
| `void onLoggedOn(User myUser)` | User authenticated and logged in |
| `void onLoggedOut()` | User logged off |

### Text Messages:
* `boolean onTextMessageReceived(TextMessage message)`  
  Triggered when a text message is received.  
  *Return value:* `true` to consume and suppress the message (no sound, not saved to history); `false` to let normal processing proceed.
* `boolean onTextMessageSending(TextMessage message)`  
  Triggered before a message is transmitted to the server.  
  *Return value:* `true` to cancel sending; `false` to permit.

### Channels:
* `void onChannelJoined(Channel channel)`: Joined channel.
* `void onChannelLeft(Channel channel)`: Left channel.
* `void onChannelAdded(Channel channel)`: Channel created on server.
* `void onChannelUpdated(Channel channel)`: Channel properties updated.
* `void onChannelRemoved(Channel channel)`: Channel removed.

### Users:
* `void onUserJoined(User user)`: User entered server/channel.
* `void onUserLeft(User user)`: User left.
* `void onUserUpdated(User user)`: User nickname, status, or rights changed.
* `void onUserSpeaking(User user, boolean speaking)`: User voice activity started/stopped.

### Audio:
* `void onVoiceTransmissionStateChanged(boolean transmitting)`: Local microphone transmission state toggled (`true` = transmitting, `false` = muted).
