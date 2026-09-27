# 01. Architecture & Core Concepts

The TeamTalk Next plugin framework is built upon Android's standard dynamic code loading mechanism using `dalvik.system.DexClassLoader`.

---

## 🏛 System Architecture Overview

```
                ┌────────────────────────────────────────────────────────┐
                │                  TeamTalk Next Client                  │
                │  - MainActivity                                        │
                │  - TeamTalkService (Background VoIP Connection)        │
                │  - TeamTalkBase (C++ JNI SDK Engine)                   │
                └───────────────────────────┬────────────────────────────┘
                                            │
                                            ▼
                ┌────────────────────────────────────────────────────────┐
                │                 PluginManager (Singleton)              │
                │  - Plugin directory discovery                          │
                │  - DexClassLoader factory                              │
                │  - TeamTalk event multiplexing                         │
                │  - Slash command dispatcher (/cmd)                     │
                │  - State persistence (Enabled/Disabled in SharedPrefs) │
                └───────────────────────────┬────────────────────────────┘
                                            │
                  ┌─────────────────────────┴─────────────────────────┐
                  ▼                                                   ▼
    ┌───────────────────────────┐                       ┌───────────────────────────┐
    │     PluginContainer #1    │                       │     PluginContainer #2    │
    │  - PluginInfo (metadata)  │                       │  - PluginInfo (metadata)  │
    │  - DexClassLoader         │                       │  - DexClassLoader         │
    │  - Plugin instance        │                       │  - Plugin instance        │
    │  - Event Listeners        │                       │  - Event Listeners        │
    │  - Commands & Actions     │                       │  - Commands & Actions     │
    └───────────────────────────┘                       └───────────────────────────┘
```

---

## 🔄 Plugin Lifecycle

Each plugin transitions through 4 distinct lifecycle stages:

```
[Discovered .jar/.dex package]
           │
           ▼
        onLoad()        ───► Register commands, event listeners, load config
           │
           ▼
       onEnable()       ───► Plugin activated, events start flowing
           │ ◄──┐
           │    │ (User toggles switch in Plugin Manager UI)
           ▼    │
      onDisable()       ───► Plugin paused, events and commands suspended
           │
           ▼
       onUnload()       ───► Clean up open sockets, threads, release resources
```

1. **`onLoad(PluginContext context)`**:
   * Invoked once when the plugin is loaded into memory.
   * Receives `PluginContext`, granting access to TeamTalk services, JNI SDK, file storage, and logger.
   * Register slash commands (`registerCommand`) and listeners (`registerEventListener`) here.

2. **`onEnable()`**:
   * Invoked when the plugin is activated by the user or upon initial app startup.
   * The plugin starts receiving network and user events.

3. **`onDisable()`**:
   * Invoked when the plugin is turned off.
   * Events stop flowing to this plugin, and its slash commands are temporarily unregistered.

4. **`onUnload()`**:
   * Invoked before deleting the plugin or during a full reload.
   * Free any resources, cancel background threads, and close file handles.

---

## 🛡 Crash Resilience & Sandbox Safety

* **Crash Guard:**  
  Every call into plugin code (`onLoad`, `onEnable`, `onTextMessageReceived`, etc.) is guarded by `try-catch (Throwable t)`. Any unhandled exception is safely logged to `PluginLogger`, the plugin state is marked with an error badge, and **TeamTalk Next continues running smoothly without crashing**.
* **Isolated File Storage:**  
  `context.getDataFolder()` gives the plugin its own dedicated folder in the app's private files directory (`/data/data/org.nekit.ttproplus/files/plugins/<plugin_id>_data/`).
* **Isolated Preferences:**  
  `context.getPreferences()` gives an independent `SharedPreferences` file (`plugin_<plugin_id>.xml`) that never conflicts with host app preferences.
