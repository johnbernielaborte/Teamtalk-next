# TeamTalk Next Plugin System Documentation (EN)

The **TeamTalk Next Plugin System** allows third-party developers to create dynamically loaded modules in Java or Kotlin to extend the mobile client without modifying the application's base source code.

---

## 📚 Documentation Sections

1. [**01. Architecture & Core Concepts**](01_architecture.md)
   * `DexClassLoader` mechanism on Android ART / Dalvik.
   * Plugin lifecycle (`onLoad`, `onEnable`, `onDisable`, `onUnload`).
   * Isolated data storage and preferences.
   * Safety and crash resilience (Crash Guard).

2. [**02. Quick Start Guide**](02_quickstart.md)
   * Plugin project structure.
   * Writing your first plugin (`EchoBotPlugin`).
   * `plugin.json` manifest structure and fields.

3. [**03. API Reference**](03_api_reference.md)
   * `PluginContext` methods (messaging, channel management, SDK access).
   * `PluginEventListener` hooks (networking, messages, channels, users, audio).
   * Logging with `PluginLogger`.
   * Async tasks and periodic scheduler.

4. [**04. Slash Commands & UI Actions**](04_commands_and_ui.md)
   * Registering chat commands (starting with `/`).
   * `PluginCommandHandler` and `PluginCommandSender`.
   * Adding custom UI action buttons and dialogs (`PluginAction`).

5. [**05. Building & Packaging**](05_building_and_packaging.md)
   * Compiling Java sources to `.class` (`javac`).
   * Translating bytecode to Android DEX (`d8` tool).
   * Packaging into `.jar` or `.dex`.

6. [**06. Installation & Debugging**](06_installation_and_debug.md)
   * Installing via the built-in "Plugin Manager" UI.
   * Manual installation into app directory.
   * Debugging and reviewing logs via `adb logcat`.

---

## 🎯 Sample Plugins
Complete source code for example plugins and a build script can be found in the [`tests/`](../../tests/) directory.
