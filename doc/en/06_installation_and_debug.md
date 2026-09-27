# 06. Installation & Debugging

Once your `.jar` plugin is built, it can be installed and verified on a physical device or emulator.

---

## 📲 Installation Methods

### Method 1: In-App UI (Recommended)
1. Open **TeamTalk Next**.
2. Tap the overflow menu (three dots in top right) and select **"Plugins"**.
3. Tap **"Install Plugin"** at the bottom of the screen.
4. Select your compiled `.jar` or `.dex` file from your device file manager.
5. The app verifies the manifest, initializes the `DexClassLoader`, and enables the plugin immediately!
6. A success message confirms installation:  
   *"Plugin 'Plugin Name' installed successfully"*.

### Method 2: Direct File Copy
1. Copy the `.jar` plugin file into the app's external files directory:  
   `/sdcard/Android/data/org.nekit.ttproplus/files/plugins/`  
   *(Tap the top banner in the Plugin Manager screen to copy this path to your clipboard)*.
2. In the Plugin Manager screen, tap the **"Reload"** icon in the toolbar.
3. The plugin will be discovered and loaded.

---

## 🔍 Debugging & Logs

All plugin messages logged via `PluginLogger` appear with the tag:  
`TTPlugin[<PluginName>]`

Use `adb logcat` to monitor in real-time:

```bash
# Filter plugin logs
adb logcat -s "PluginManager" "TTPlugin*"

# Check for plugin errors
adb logcat *:E | grep -i "plugin"
```

### Common Errors:
* **`No plugin.json or valid manifest found`**: Ensure `plugin.json` is at the root of the `.jar` archive alongside `classes.dex`.
* **`Class ... does not implement Plugin`**: Ensure your class implements `org.nekit.ttproplus.plugin.Plugin` or extends `BasePlugin`.
* **`onLoad failed` / `onEnable failed`**: Exception inside plugin code; view the full stack trace in `adb logcat` and in the plugin card's red error label in the UI.
