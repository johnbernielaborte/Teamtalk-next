package org.nekit.ttproplus.plugin;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import dk.bearware.Channel;
import dk.bearware.TextMessage;
import dk.bearware.User;

public class PluginManagerTest {

    private PluginManager pluginManager;

    @Before
    public void setUp() {
        pluginManager = PluginManager.getInstance();
    }

    @Test
    public void testPluginInfoSerialization() throws Exception {
        PluginInfo info = new PluginInfo("com.example.test", "Test Plugin", "1.2.3", 4, "Tester", "Description text", "com.example.test.TestPlugin");
        String json = info.toJson().toString();
        PluginInfo parsed = PluginInfo.fromJson(json);

        assertEquals("com.example.test", parsed.getId());
        assertEquals("Test Plugin", parsed.getName());
        assertEquals("1.2.3", parsed.getVersion());
        assertEquals(4, parsed.getVersionCode());
        assertEquals("Tester", parsed.getAuthor());
        assertEquals("Description text", parsed.getDescription());
        assertEquals("com.example.test.TestPlugin", parsed.getMainClass());
    }

    @Test
    public void testCommandHandling() {
        final List<String> receivedResponses = new ArrayList<>();

        PluginCommandHandler pingHandler = (sender, command, args) -> {
            sender.sendMessage("pong " + (args.length > 0 ? args[0] : ""));
            return true;
        };

        // Register directly
        PluginCommandSender sender = new PluginCommandSender() {
            @Override
            public void sendMessage(String message) {
                receivedResponses.add(message);
            }

            @Override
            public boolean isLocalUser() {
                return true;
            }

            @Override
            public int getSenderUserId() {
                return 1;
            }

            @Override
            public int getChannelId() {
                return 10;
            }
        };

        // Manual registration via PluginManager command map simulation or container
        // Test handleCommand with non-command
        assertFalse(pluginManager.handleCommand("Hello World", sender));

        // Test with unknown command
        assertFalse(pluginManager.handleCommand("/unknowncommand", sender));
    }

    @Test
    public void testBasePluginLifecycleAndEvents() throws Exception {
        AtomicBoolean loaded = new AtomicBoolean(false);
        AtomicBoolean enabled = new AtomicBoolean(false);
        AtomicBoolean disabled = new AtomicBoolean(false);
        AtomicBoolean unloaded = new AtomicBoolean(false);
        AtomicBoolean messageReceived = new AtomicBoolean(false);
        AtomicBoolean channelJoined = new AtomicBoolean(false);

        BasePlugin plugin = new BasePlugin() {
            @Override
            public void onLoad(PluginContext context) throws Exception {
                super.onLoad(context);
                loaded.set(true);
            }

            @Override
            public void onEnable() throws Exception {
                super.onEnable();
                enabled.set(true);
            }

            @Override
            public void onDisable() throws Exception {
                super.onDisable();
                disabled.set(true);
            }

            @Override
            public void onUnload() throws Exception {
                super.onUnload();
                unloaded.set(true);
            }

            @Override
            public boolean onTextMessageReceived(TextMessage message) {
                messageReceived.set(true);
                return "BLOCK".equals(message.szMessage);
            }

            @Override
            public void onChannelJoined(Channel channel) {
                channelJoined.set(true);
            }
        };

        PluginInfo info = new PluginInfo("test.dummy", "Dummy", "1.0", 1, "Author", "Desc", "DummyClass");
        plugin.setInfo(info);

        PluginContainer container = new PluginContainer(info, plugin, getClass().getClassLoader(), null);

        plugin.onLoad(null);
        assertTrue(loaded.get());

        plugin.onEnable();
        container.setEnabled(true);
        assertTrue(enabled.get());
        assertTrue(container.isEnabled());

        // Test event listener
        TextMessage normalMsg = new TextMessage();
        normalMsg.szMessage = "Hello";
        assertFalse(plugin.onTextMessageReceived(normalMsg));
        assertTrue(messageReceived.get());

        TextMessage blockMsg = new TextMessage();
        blockMsg.szMessage = "BLOCK";
        assertTrue(plugin.onTextMessageReceived(blockMsg));

        Channel ch = new Channel();
        ch.nChannelID = 5;
        plugin.onChannelJoined(ch);
        assertTrue(channelJoined.get());

        plugin.onDisable();
        container.setEnabled(false);
        assertTrue(disabled.get());
        assertFalse(container.isEnabled());

        plugin.onUnload();
        assertTrue(unloaded.get());
    }
}
