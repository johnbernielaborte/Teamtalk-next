package org.nekit.ttproplus.plugin;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import org.nekit.ttproplus.R;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import dalvik.system.DexClassLoader;
import dk.bearware.Channel;
import dk.bearware.TeamTalkBase;
import dk.bearware.TextMessage;
import dk.bearware.User;
import org.nekit.ttproplus.backend.TeamTalkService;

/**
 * Central manager orchestrating TeamTalk Next plugins.
 * Supports hot-loading, lifecycle management, event forwarding, command handling,
 * and isolated execution of Android Dalvik/ART .dex/.jar/.apk plugins.
 */
public class PluginManager {
    private static final String TAG = "PluginManager";
    private static final String PREF_NAME = "tt_plugins_pref";
    private static final String KEY_ENABLED_PREFIX = "enabled_";

    private static volatile PluginManager instance;

    private Context appContext;
    private TeamTalkService currentService;
    private Handler mainHandler;
    private final ScheduledExecutorService executorService = Executors.newScheduledThreadPool(4);

    private synchronized Handler getMainHandler() {
        if (mainHandler == null) {
            try {
                Looper looper = Looper.getMainLooper();
                if (looper != null) {
                    mainHandler = new Handler(looper);
                }
            } catch (Throwable ignored) {}
        }
        return mainHandler;
    }

    private final Map<String, PluginContainer> plugins = new ConcurrentHashMap<>();
    private final Map<String, PluginCommandHandler> commands = new ConcurrentHashMap<>();
    private final Map<String, PluginAction> actions = new ConcurrentHashMap<>();

    private final List<PluginLifecycleListener> lifecycleListeners = new ArrayList<>();

    public interface PluginLifecycleListener {
        void onPluginsChanged();
    }

    private PluginManager() {}

    public static PluginManager getInstance() {
        if (instance == null) {
            synchronized (PluginManager.class) {
                if (instance == null) {
                    instance = new PluginManager();
                }
            }
        }
        return instance;
    }

    /**
     * Initializes the plugin manager with application context and service.
     */
    public synchronized void init(Context context, TeamTalkService service) {
        if (context != null) {
            this.appContext = context.getApplicationContext();
        }
        if (service != null) {
            this.currentService = service;
        }

        ensurePluginDirectories();
        if (plugins.isEmpty()) {
            loadAllPlugins();
        }
    }

    public synchronized void setService(TeamTalkService service) {
        this.currentService = service;
    }

    public TeamTalkService getService() {
        if (currentService != null) {
            return currentService;
        }
        return TeamTalkService.getInstance();
    }

    public Context getContext() {
        return appContext;
    }

    /**
     * Internal plugins folder: /data/data/org.nekit.ttproplus/files/plugins/
     */
    public File getInternalPluginsDir() {
        if (appContext == null) return null;
        File dir = new File(appContext.getFilesDir(), "plugins");
        if (!dir.exists()) dir.mkdirs();
        return dir;
    }

    /**
     * External plugins folder: /sdcard/Android/data/org.nekit.ttproplus/files/plugins/
     */
    public File getExternalPluginsDir() {
        if (appContext == null) return null;
        File ext = appContext.getExternalFilesDir(null);
        if (ext == null) return getInternalPluginsDir();
        File dir = new File(ext, "plugins");
        if (!dir.exists()) dir.mkdirs();
        return dir;
    }

    private File getOptimizedDexDir() {
        if (appContext == null) return null;
        File dir = new File(appContext.getCodeCacheDir(), "plugins_opt");
        if (!dir.exists()) dir.mkdirs();
        return dir;
    }

    private void ensurePluginDirectories() {
        File internalDir = getInternalPluginsDir();
        File externalDir = getExternalPluginsDir();
        File optDir = getOptimizedDexDir();
        Log.i(TAG, "Plugin dirs initialized: internal=" + internalDir + ", external=" + externalDir + ", opt=" + optDir);
    }

    private SharedPreferences getPluginPrefs() {
        if (appContext == null) return null;
        return appContext.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    public boolean isPluginEnabled(String pluginId) {
        SharedPreferences prefs = getPluginPrefs();
        if (prefs == null) return true;
        return prefs.getBoolean(KEY_ENABLED_PREFIX + pluginId, true);
    }

    public void setPluginEnabledState(String pluginId, boolean enabled) {
        SharedPreferences prefs = getPluginPrefs();
        if (prefs != null) {
            prefs.edit().putBoolean(KEY_ENABLED_PREFIX + pluginId, enabled).apply();
        }
    }

    /**
     * Discovers and loads all plugins from both internal and external plugin directories.
     */
    public synchronized void loadAllPlugins() {
        Log.i(TAG, "Scanning for plugins...");
        List<File> pluginFiles = new ArrayList<>();

        File internalDir = getInternalPluginsDir();
        if (internalDir != null && internalDir.isDirectory()) {
            File[] files = internalDir.listFiles();
            if (files != null) {
                for (File f : files) {
                    if (isPluginFile(f)) pluginFiles.add(f);
                }
            }
        }

        File externalDir = getExternalPluginsDir();
        if (externalDir != null && externalDir.isDirectory()) {
            File[] files = externalDir.listFiles();
            if (files != null) {
                for (File f : files) {
                    if (isPluginFile(f) && !pluginFiles.contains(f)) pluginFiles.add(f);
                }
            }
        }

        for (File f : pluginFiles) {
            try {
                loadPluginFromFile(f);
            } catch (Throwable t) {
                Log.e(TAG, "Failed to load plugin from file: " + f.getName(), t);
            }
        }

        notifyListeners();
    }

    private boolean isPluginFile(File f) {
        if (f == null || !f.isFile()) return false;
        String name = f.getName().toLowerCase();
        return name.endsWith(".jar") || name.endsWith(".dex") || name.endsWith(".apk");
    }

    /**
     * Loads a single plugin from file.
     */
    public synchronized PluginContainer loadPluginFromFile(File file) throws Exception {
        if (!file.exists()) {
            throw new IllegalArgumentException("Plugin file does not exist: " + file.getAbsolutePath());
        }

        PluginInfo info = readPluginInfo(file);
        if (info == null) {
            String msg = appContext != null ? appContext.getString(R.string.plugin_err_no_plugin_json) : ("No plugin.json found in " + file.getName());
            throw new IllegalStateException(msg);
        }

        if (info.getId() == null || info.getId().trim().isEmpty()) {
            info.setId(file.getName().replace(".", "_"));
        }

        // Unload existing version if already loaded
        if (plugins.containsKey(info.getId())) {
            unloadPlugin(info.getId());
        }

        // Android 14+ (API 34+) Dynamic Code Loading requirement:
        // DEX/JAR files must be strictly read-only and in internal storage.
        File dexToLoad = file;
        if (appContext != null) {
            File activeDir = new File(appContext.getCodeCacheDir(), "active_plugins");
            if (!activeDir.exists()) activeDir.mkdirs();

            File safeFile = new File(activeDir, info.getId() + ".jar");
            if (safeFile.exists()) {
                safeFile.setWritable(true);
                safeFile.delete();
            }
            copyFile(file, safeFile);
            safeFile.setReadOnly();
            dexToLoad = safeFile;
        } else {
            file.setReadOnly();
        }

        File optDir = getOptimizedDexDir();
        ClassLoader parentLoader = PluginManager.class.getClassLoader();
        DexClassLoader dexLoader = new DexClassLoader(dexToLoad.getAbsolutePath(), optDir != null ? optDir.getAbsolutePath() : null, null, parentLoader);

        String mainClassName = info.getMainClass();
        if (mainClassName == null || mainClassName.trim().isEmpty()) {
            throw new IllegalStateException("Plugin " + info.getId() + " does not specify 'mainClass'");
        }

        Class<?> clazz;
        try {
            clazz = dexLoader.loadClass(mainClassName);
        } catch (ClassNotFoundException e) {
            String msg = appContext != null ? appContext.getString(R.string.plugin_err_main_class_not_found, mainClassName) : ("Main class not found: " + mainClassName);
            throw new ClassNotFoundException(msg);
        }

        if (!Plugin.class.isAssignableFrom(clazz)) {
            String msg = appContext != null ? appContext.getString(R.string.plugin_err_main_class_invalid, mainClassName) : ("Class does not implement Plugin: " + mainClassName);
            throw new IllegalStateException(msg);
        }

        Plugin plugin = (Plugin) clazz.getDeclaredConstructor().newInstance();
        PluginContainer container = new PluginContainer(info, plugin, dexLoader, file);

        PluginContext pluginContext = createPluginContext(container);
        try {
            plugin.onLoad(pluginContext);
        } catch (Throwable t) {
            container.setLastError("onLoad failed: " + t.getMessage());
            Log.e(TAG, "Error in onLoad for plugin " + info.getId(), t);
        }

        plugins.put(info.getId(), container);

        // Auto-enable if enabled in settings
        boolean shouldEnable = isPluginEnabled(info.getId());
        if (shouldEnable && container.getLastError() == null) {
            enablePlugin(info.getId());
        }

        Log.i(TAG, "Plugin loaded: " + info + " (enabled=" + container.isEnabled() + ")");
        return container;
    }

    /**
     * Reads plugin metadata (plugin.json) from inside archive or companion file.
     */
    private PluginInfo readPluginInfo(File file) throws Exception {
        String name = file.getName().toLowerCase();
        if (name.endsWith(".jar") || name.endsWith(".apk") || name.endsWith(".zip")) {
            try (ZipFile zip = new ZipFile(file)) {
                boolean hasDex = false;
                boolean hasClass = false;
                ZipEntry entry = zip.getEntry("plugin.json");
                if (entry == null) entry = zip.getEntry("assets/plugin.json");
                if (entry == null) entry = zip.getEntry("META-INF/plugin.json");

                Enumeration<? extends ZipEntry> en = zip.entries();
                while (en.hasMoreElements()) {
                    ZipEntry ze = en.nextElement();
                    String enName = ze.getName().toLowerCase();
                    if (enName.endsWith(".dex")) {
                        hasDex = true;
                    } else if (enName.endsWith(".class")) {
                        hasClass = true;
                    }
                    if (entry == null && enName.endsWith("plugin.json")) {
                        entry = ze;
                    }
                }

                if (!hasDex) {
                    if (hasClass) {
                        String msg = appContext != null ? appContext.getString(R.string.plugin_err_no_classes_dex) : "Archive contains .class files but no classes.dex. Compile with d8 tool.";
                        throw new IllegalStateException(msg);
                    } else {
                        String msg = appContext != null ? appContext.getString(R.string.plugin_err_no_classes_dex) : "Archive does not contain classes.dex";
                        throw new IllegalStateException(msg);
                    }
                }

                if (entry != null) {
                    try (InputStream is = zip.getInputStream(entry);
                         BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
                        StringBuilder sb = new StringBuilder();
                        String line;
                        while ((line = reader.readLine()) != null) {
                            sb.append(line).append("\n");
                        }
                        return PluginInfo.fromJson(sb.toString());
                    }
                } else {
                    String msg = appContext != null ? appContext.getString(R.string.plugin_err_no_plugin_json) : "Manifest plugin.json not found in archive";
                    throw new IllegalStateException(msg);
                }
            } catch (IllegalStateException ise) {
                throw ise;
            } catch (Exception e) {
                Log.w(TAG, "Error reading zip entries from " + file.getName() + ": " + e.getMessage());
            }
        }

        // Check companion json: e.g. "myplugin.jar" -> "myplugin.json" or "myplugin.dex" -> "myplugin.json"
        String baseName = file.getName();
        int dot = baseName.lastIndexOf('.');
        if (dot > 0) baseName = baseName.substring(0, dot);
        File companion = new File(file.getParentFile(), baseName + ".json");
        if (companion.exists() && companion.isFile()) {
            try {
                ByteArrayOutputStream bos = new ByteArrayOutputStream();
                try (InputStream is = new java.io.FileInputStream(companion)) {
                    byte[] buf = new byte[4096];
                    int r;
                    while ((r = is.read(buf)) != -1) {
                        bos.write(buf, 0, r);
                    }
                }
                return PluginInfo.fromJson(bos.toString("UTF-8"));
            } catch (Exception e) {
                Log.w(TAG, "Error reading companion JSON: " + companion.getName(), e);
            }
        }

        return null;
    }

    public synchronized boolean enablePlugin(String pluginId) {
        PluginContainer container = plugins.get(pluginId);
        if (container == null || container.isEnabled()) return false;

        try {
            container.getPlugin().onEnable();
            container.setEnabled(true);
            container.setLastError(null);
            setPluginEnabledState(pluginId, true);
            Log.i(TAG, "Plugin enabled: " + pluginId);
            notifyListeners();
            return true;
        } catch (Throwable t) {
            container.setLastError("onEnable failed: " + t.getMessage());
            Log.e(TAG, "Failed to enable plugin: " + pluginId, t);
            notifyListeners();
            return false;
        }
    }

    public synchronized boolean disablePlugin(String pluginId) {
        PluginContainer container = plugins.get(pluginId);
        if (container == null || !container.isEnabled()) return false;

        try {
            container.getPlugin().onDisable();
        } catch (Throwable t) {
            Log.e(TAG, "Error in onDisable for plugin: " + pluginId, t);
        } finally {
            container.setEnabled(false);
            setPluginEnabledState(pluginId, false);
            // Remove commands and actions registered by this plugin
            for (String cmd : container.getRegisteredCommands()) {
                commands.remove(cmd.toLowerCase());
            }
            for (String actionId : container.getRegisteredActions()) {
                actions.remove(actionId);
            }
            Log.i(TAG, "Plugin disabled: " + pluginId);
            notifyListeners();
        }
        return true;
    }

    public synchronized void unloadPlugin(String pluginId) {
        PluginContainer container = plugins.get(pluginId);
        if (container == null) return;

        if (container.isEnabled()) {
            disablePlugin(pluginId);
        }

        try {
            container.getPlugin().onUnload();
        } catch (Throwable t) {
            Log.e(TAG, "Error in onUnload for plugin: " + pluginId, t);
        }

        plugins.remove(pluginId);
        notifyListeners();
    }

    public synchronized void reloadPlugins() {
        for (String id : new ArrayList<>(plugins.keySet())) {
            unloadPlugin(id);
        }
        commands.clear();
        actions.clear();
        loadAllPlugins();
    }

    /**
     * Installs a plugin from an InputStream (e.g. from file picker or download).
     */
    public synchronized PluginContainer installPlugin(InputStream inputStream, String fileName) throws Exception {
        if (appContext == null) {
            throw new IllegalStateException("PluginManager is not initialized with Context");
        }
        File targetDir = getInternalPluginsDir();
        if (targetDir == null) {
            throw new IllegalStateException("Internal plugins directory is null");
        }
        if (!targetDir.exists()) {
            targetDir.mkdirs();
        }

        String safeName = new File(fileName).getName();
        if (!safeName.toLowerCase().endsWith(".jar") && !safeName.toLowerCase().endsWith(".dex") && !safeName.toLowerCase().endsWith(".apk") && !safeName.toLowerCase().endsWith(".zip")) {
            safeName += ".jar";
        }

        File destFile = new File(targetDir, safeName);
        if (destFile.exists()) {
            destFile.setWritable(true);
            destFile.delete();
        }

        try (FileOutputStream fos = new FileOutputStream(destFile)) {
            byte[] buf = new byte[8192];
            int len;
            while ((len = inputStream.read(buf)) != -1) {
                fos.write(buf, 0, len);
            }
            fos.flush();
        }

        return loadPluginFromFile(destFile);
    }

    /**
     * Uninstalls/deletes a plugin file and removes it from memory.
     */
    public synchronized boolean deletePlugin(String pluginId) {
        PluginContainer container = plugins.get(pluginId);
        if (container == null) return false;

        File sourceFile = container.getSourceFile();
        unloadPlugin(pluginId);

        if (sourceFile != null && sourceFile.exists()) {
            sourceFile.setWritable(true);
            sourceFile.delete();
        }

        if (appContext != null) {
            File safeFile = new File(new File(appContext.getCodeCacheDir(), "active_plugins"), pluginId + ".jar");
            if (safeFile.exists()) {
                safeFile.setWritable(true);
                safeFile.delete();
            }
        }
        return true;
    }

    private static void copyFile(File src, File dst) throws IOException {
        try (InputStream in = new FileInputStream(src);
             OutputStream out = new FileOutputStream(dst)) {
            byte[] buf = new byte[8192];
            int len;
            while ((len = in.read(buf)) != -1) {
                out.write(buf, 0, len);
            }
            out.flush();
        }
    }

    public List<PluginContainer> getLoadedPlugins() {
        return Collections.unmodifiableList(new ArrayList<>(plugins.values()));
    }

    public PluginContainer getPlugin(String pluginId) {
        return plugins.get(pluginId);
    }

    public Map<String, PluginAction> getRegisteredActions() {
        return Collections.unmodifiableMap(actions);
    }

    // -------------------------------------------------------------
    // Plugin Context Factory
    // -------------------------------------------------------------

    private PluginContext createPluginContext(final PluginContainer container) {
        final PluginInfo info = container.getInfo();
        final PluginLogger logger = new PluginLogger(info.getName());

        return new PluginContext() {
            @Override
            public Context getApplicationContext() {
                return appContext;
            }

            @Override
            public TeamTalkService getService() {
                return PluginManager.this.getService();
            }

            @Override
            public TeamTalkBase getClient() {
                TeamTalkService s = getService();
                return s != null ? s.getTTInstance() : null;
            }

            @Override
            public PluginInfo getInfo() {
                return info;
            }

            @Override
            public PluginLogger getLogger() {
                return logger;
            }

            @Override
            public File getDataFolder() {
                File dir = new File(getInternalPluginsDir(), info.getId() + "_data");
                if (!dir.exists()) dir.mkdirs();
                return dir;
            }

            @Override
            public SharedPreferences getPreferences() {
                if (appContext == null) return null;
                return appContext.getSharedPreferences("plugin_" + info.getId(), Context.MODE_PRIVATE);
            }

            @Override
            public void sendTextMessage(TextMessage msg) {
                TeamTalkBase client = getClient();
                if (client != null && msg != null) {
                    client.doTextMessage(msg);
                }
            }

            @Override
            public void sendChannelMessage(String text) {
                TeamTalkService s = getService();
                TeamTalkBase client = getClient();
                if (client != null && s != null && text != null) {
                    TextMessage msg = new TextMessage();
                    msg.nMsgType = dk.bearware.TextMsgType.MSGTYPE_CHANNEL;
                    msg.nChannelID = client.getMyChannelID();
                    msg.szMessage = text;
                    client.doTextMessage(msg);
                }
            }

            @Override
            public void sendUserMessage(int userId, String text) {
                TeamTalkBase client = getClient();
                if (client != null && text != null && userId > 0) {
                    TextMessage msg = new TextMessage();
                    msg.nMsgType = dk.bearware.TextMsgType.MSGTYPE_USER;
                    msg.nToUserID = userId;
                    msg.szMessage = text;
                    client.doTextMessage(msg);
                }
            }

            @Override
            public void sendBroadcastMessage(String text) {
                TeamTalkBase client = getClient();
                if (client != null && text != null) {
                    TextMessage msg = new TextMessage();
                    msg.nMsgType = dk.bearware.TextMsgType.MSGTYPE_BROADCAST;
                    msg.szMessage = text;
                    client.doTextMessage(msg);
                }
            }

            @Override
            public void joinChannel(int channelId, String password) {
                TeamTalkBase client = getClient();
                TeamTalkService s = getService();
                if (client != null && channelId > 0) {
                    client.doJoinChannelByID(channelId, password != null ? password : "");
                }
            }

            @Override
            public void leaveChannel() {
                TeamTalkBase client = getClient();
                if (client != null) {
                    client.doLeaveChannel();
                }
            }

            @Override
            public void runOnMainThread(Runnable action) {
                if (action == null) return;
                Handler h = getMainHandler();
                if (h != null) {
                    h.post(action);
                } else {
                    action.run();
                }
            }

            @Override
            public void runAsync(Runnable task) {
                if (task != null) executorService.execute(task);
            }

            @Override
            public ScheduledFuture<?> scheduleTask(Runnable task, long delayMs) {
                return executorService.schedule(task, delayMs, TimeUnit.MILLISECONDS);
            }

            @Override
            public ScheduledFuture<?> schedulePeriodicTask(Runnable task, long initialDelayMs, long periodMs) {
                return executorService.scheduleAtFixedRate(task, initialDelayMs, periodMs, TimeUnit.MILLISECONDS);
            }

            @Override
            public void registerEventListener(PluginEventListener listener) {
                container.addEventListener(listener);
            }

            @Override
            public void unregisterEventListener(PluginEventListener listener) {
                container.removeEventListener(listener);
            }

            @Override
            public void registerCommand(String command, PluginCommandHandler handler) {
                if (command != null && handler != null) {
                    String clean = command.toLowerCase().trim();
                    if (clean.startsWith("/")) clean = clean.substring(1);
                    commands.put(clean, handler);
                    container.addRegisteredCommand(clean);
                }
            }

            @Override
            public void unregisterCommand(String command) {
                if (command != null) {
                    String clean = command.toLowerCase().trim();
                    if (clean.startsWith("/")) clean = clean.substring(1);
                    commands.remove(clean);
                }
            }

            @Override
            public void registerAction(PluginAction action) {
                if (action != null && action.getId() != null) {
                    actions.put(action.getId(), action);
                    container.addRegisteredAction(action.getId());
                }
            }

            @Override
            public void unregisterAction(String actionId) {
                if (actionId != null) {
                    actions.remove(actionId);
                }
            }
        };
    }

    // -------------------------------------------------------------
    // Slash Command Dispatcher
    // -------------------------------------------------------------

    /**
     * Parses and executes a chat command (e.g. "/ping" or "/roll 100").
     * @return true if command was matched and handled by a plugin.
     */
    public boolean handleCommand(String line, PluginCommandSender sender) {
        if (line == null || !line.startsWith("/")) return false;

        String withoutSlash = line.substring(1).trim();
        if (withoutSlash.isEmpty()) return false;

        String[] parts = withoutSlash.split("\\s+");
        String cmdName = parts[0].toLowerCase();
        String[] args = new String[parts.length - 1];
        System.arraycopy(parts, 1, args, 0, args.length);

        PluginCommandHandler handler = commands.get(cmdName);
        if (handler != null) {
            try {
                return handler.onCommand(sender, cmdName, args);
            } catch (Throwable t) {
                Log.e(TAG, "Error executing plugin command /" + cmdName, t);
                if (sender != null) {
                    sender.sendMessage("Command error: " + t.getMessage());
                }
                return true;
            }
        }
        return false;
    }

    // -------------------------------------------------------------
    // Event Multiplexing to Enabled Plugins
    // -------------------------------------------------------------

    public void onConnectSuccess() {
        for (PluginContainer c : plugins.values()) {
            if (!c.isEnabled()) continue;
            for (PluginEventListener l : c.getEventListeners()) {
                try { l.onConnectSuccess(); } catch (Throwable t) { logListenerError(c, t); }
            }
        }
    }

    public void onConnectFailed() {
        for (PluginContainer c : plugins.values()) {
            if (!c.isEnabled()) continue;
            for (PluginEventListener l : c.getEventListeners()) {
                try { l.onConnectFailed(); } catch (Throwable t) { logListenerError(c, t); }
            }
        }
    }

    public void onConnectionLost() {
        for (PluginContainer c : plugins.values()) {
            if (!c.isEnabled()) continue;
            for (PluginEventListener l : c.getEventListeners()) {
                try { l.onConnectionLost(); } catch (Throwable t) { logListenerError(c, t); }
            }
        }
    }

    public void onLoggedOn(User myUser) {
        for (PluginContainer c : plugins.values()) {
            if (!c.isEnabled()) continue;
            for (PluginEventListener l : c.getEventListeners()) {
                try { l.onLoggedOn(myUser); } catch (Throwable t) { logListenerError(c, t); }
            }
        }
    }

    public void onLoggedOut() {
        for (PluginContainer c : plugins.values()) {
            if (!c.isEnabled()) continue;
            for (PluginEventListener l : c.getEventListeners()) {
                try { l.onLoggedOut(); } catch (Throwable t) { logListenerError(c, t); }
            }
        }
    }

    /**
     * Dispatch incoming message event.
     * @return true if consumed/suppressed by any plugin.
     */
    public boolean onTextMessageReceived(TextMessage message) {
        boolean consumed = false;
        for (PluginContainer c : plugins.values()) {
            if (!c.isEnabled()) continue;
            for (PluginEventListener l : c.getEventListeners()) {
                try {
                    if (l.onTextMessageReceived(message)) {
                        consumed = true;
                    }
                } catch (Throwable t) {
                    logListenerError(c, t);
                }
            }
        }
        return consumed;
    }

    /**
     * Dispatch outgoing message event.
     * @return true if canceled by any plugin.
     */
    public boolean onTextMessageSending(TextMessage message) {
        boolean canceled = false;
        for (PluginContainer c : plugins.values()) {
            if (!c.isEnabled()) continue;
            for (PluginEventListener l : c.getEventListeners()) {
                try {
                    if (l.onTextMessageSending(message)) {
                        canceled = true;
                    }
                } catch (Throwable t) {
                    logListenerError(c, t);
                }
            }
        }
        return canceled;
    }

    public void onChannelJoined(Channel channel) {
        for (PluginContainer c : plugins.values()) {
            if (!c.isEnabled()) continue;
            for (PluginEventListener l : c.getEventListeners()) {
                try { l.onChannelJoined(channel); } catch (Throwable t) { logListenerError(c, t); }
            }
        }
    }

    public void onChannelLeft(Channel channel) {
        for (PluginContainer c : plugins.values()) {
            if (!c.isEnabled()) continue;
            for (PluginEventListener l : c.getEventListeners()) {
                try { l.onChannelLeft(channel); } catch (Throwable t) { logListenerError(c, t); }
            }
        }
    }

    public void onChannelAdded(Channel channel) {
        for (PluginContainer c : plugins.values()) {
            if (!c.isEnabled()) continue;
            for (PluginEventListener l : c.getEventListeners()) {
                try { l.onChannelAdded(channel); } catch (Throwable t) { logListenerError(c, t); }
            }
        }
    }

    public void onChannelUpdated(Channel channel) {
        for (PluginContainer c : plugins.values()) {
            if (!c.isEnabled()) continue;
            for (PluginEventListener l : c.getEventListeners()) {
                try { l.onChannelUpdated(channel); } catch (Throwable t) { logListenerError(c, t); }
            }
        }
    }

    public void onChannelRemoved(Channel channel) {
        for (PluginContainer c : plugins.values()) {
            if (!c.isEnabled()) continue;
            for (PluginEventListener l : c.getEventListeners()) {
                try { l.onChannelRemoved(channel); } catch (Throwable t) { logListenerError(c, t); }
            }
        }
    }

    public void onUserJoined(User user) {
        for (PluginContainer c : plugins.values()) {
            if (!c.isEnabled()) continue;
            for (PluginEventListener l : c.getEventListeners()) {
                try { l.onUserJoined(user); } catch (Throwable t) { logListenerError(c, t); }
            }
        }
    }

    public void onUserLeft(User user) {
        for (PluginContainer c : plugins.values()) {
            if (!c.isEnabled()) continue;
            for (PluginEventListener l : c.getEventListeners()) {
                try { l.onUserLeft(user); } catch (Throwable t) { logListenerError(c, t); }
            }
        }
    }

    public void onUserUpdated(User user) {
        for (PluginContainer c : plugins.values()) {
            if (!c.isEnabled()) continue;
            for (PluginEventListener l : c.getEventListeners()) {
                try { l.onUserUpdated(user); } catch (Throwable t) { logListenerError(c, t); }
            }
        }
    }

    public void onUserSpeaking(User user, boolean speaking) {
        for (PluginContainer c : plugins.values()) {
            if (!c.isEnabled()) continue;
            for (PluginEventListener l : c.getEventListeners()) {
                try { l.onUserSpeaking(user, speaking); } catch (Throwable t) { logListenerError(c, t); }
            }
        }
    }

    public void onVoiceTransmissionStateChanged(boolean transmitting) {
        for (PluginContainer c : plugins.values()) {
            if (!c.isEnabled()) continue;
            for (PluginEventListener l : c.getEventListeners()) {
                try { l.onVoiceTransmissionStateChanged(transmitting); } catch (Throwable t) { logListenerError(c, t); }
            }
        }
    }

    private void logListenerError(PluginContainer c, Throwable t) {
        Log.e(TAG, "Plugin " + c.getInfo().getId() + " threw an unhandled exception in event listener", t);
    }

    public void registerLifecycleListener(PluginLifecycleListener listener) {
        if (listener != null && !lifecycleListeners.contains(listener)) {
            lifecycleListeners.add(listener);
        }
    }

    public void unregisterLifecycleListener(PluginLifecycleListener listener) {
        if (listener != null) {
            lifecycleListeners.remove(listener);
        }
    }

    private void notifyListeners() {
        Runnable r = () -> {
            for (PluginLifecycleListener l : lifecycleListeners) {
                try { l.onPluginsChanged(); } catch (Throwable ignored) {}
            }
        };
        Handler h = getMainHandler();
        if (h != null) {
            h.post(r);
        } else {
            r.run();
        }
    }
}
