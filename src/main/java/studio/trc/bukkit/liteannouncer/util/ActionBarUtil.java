package studio.trc.bukkit.liteannouncer.util;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import org.bukkit.entity.Player;
import studio.trc.bukkit.liteannouncer.util.tools.ActionBarOfBroadcast;

public class ActionBarUtil
{
    public static Class<?> chatComponentText;
    public static Class<?> packetPlayOutChat;
    public static Class<?> interfaceChatBaseComponent;
    public static Class<?> chatMessageType;
    public static Class<?> clientboundSetActionBarTextPacket;
    public static Class<?> craftChatMessage;
    public static Class<?> craftPlayer;
    public static Class<?> packet;
    
    public static void initialize() {
        String nmsVersion = PluginControl.getNMSVersion();
        try {
            if (nmsVersion.startsWith("v1_17")) {
                chatMessageType = Class.forName("net.minecraft.network.chat.ChatMessageType");
                chatComponentText = Class.forName("net.minecraft.network.chat.ChatComponentText");
                interfaceChatBaseComponent = Class.forName("net.minecraft.network.chat.IChatBaseComponent");
                packet = Class.forName("net.minecraft.network.protocol.Packet");
                packetPlayOutChat = Class.forName("net.minecraft.network.protocol.game.PacketPlayOutChat");
                clientboundSetActionBarTextPacket = Class.forName("net.minecraft.network.protocol.game.ClientboundSetActionBarTextPacket");
                craftChatMessage = Class.forName("org.bukkit.craftbukkit." + nmsVersion + ".util.CraftChatMessage");
            } else {
                interfaceChatBaseComponent = Class.forName("net.minecraft.server." + nmsVersion + ".IChatBaseComponent");
                packet = Class.forName("net.minecraft.server." + nmsVersion + ".Packet");
                packetPlayOutChat = Class.forName("net.minecraft.server." + nmsVersion + ".PacketPlayOutChat");
                chatComponentText = Class.forName("net.minecraft.server." + nmsVersion + ".ChatComponentText"); 
            }
            if (nmsVersion.startsWith("v1_12") || nmsVersion.startsWith("v1_13") || nmsVersion.startsWith("v1_14") || nmsVersion.startsWith("v1_15") || nmsVersion.startsWith("v1_16")) {
                chatMessageType = Class.forName("net.minecraft.server." + nmsVersion + ".ChatMessageType");
            }
            craftPlayer = Class.forName("org.bukkit.craftbukkit." + nmsVersion + ".entity.CraftPlayer");
        } catch (Exception ex) {}
    }
    
    public static void sendActionBar(Player player, String text) {
        if (PluginControl.getNMSVersion().startsWith("v1_7")) return;
        if (text == null) return;
        String nmsVersion = PluginControl.getNMSVersion();
        text = MessageUtil.toColor(MessageUtil.replacePlaceholders(player, text, new HashMap()));
        try {
            Object actionbar;
            // 1.8 - 1.11.2
            if (nmsVersion.startsWith("v1_8") || nmsVersion.startsWith("v1_9") || nmsVersion.startsWith("v1_10") || nmsVersion.startsWith("v1_11")) {
                actionbar = packetPlayOutChat.getConstructor(interfaceChatBaseComponent, byte.class).newInstance(
                        chatComponentText.getConstructor(String.class).newInstance(text),
                        (byte) 2);
            // 1.12 - 1.15.2
            } else if (nmsVersion.startsWith("v1_12") || nmsVersion.startsWith("v1_13") || nmsVersion.startsWith("v1_14") || nmsVersion.startsWith("v1_15")) {
                actionbar = packetPlayOutChat.getConstructor(interfaceChatBaseComponent, chatMessageType).newInstance(
                        chatComponentText.getConstructor(String.class).newInstance(text),
                        chatMessageType.getMethod("a", byte.class).invoke(chatMessageType, (byte) 2));
            // 1.16 - 1.16.5
            } else if (nmsVersion.startsWith("v1_16")) {
                actionbar = packetPlayOutChat.getConstructor(interfaceChatBaseComponent, chatMessageType, UUID.class).newInstance(
                        chatComponentText.getConstructor(String.class).newInstance(text),
                        chatMessageType.getMethod("a", byte.class).invoke(chatMessageType, (byte) 2),
                        UUID.randomUUID());
            // 1.17+
            } else {
                actionbar = clientboundSetActionBarTextPacket.getConstructor(interfaceChatBaseComponent).newInstance(craftChatMessage.getMethod("fromStringOrNull", String.class).invoke(null, text));
            }
            sendPacket(player, actionbar);
        } catch (NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | InstantiationException ex) {
            ex.printStackTrace();
        }
    }
    
    public static void sendActionBar(Player player, ActionBarOfBroadcast actionbar) {
        sendActionBar(player, actionbar.getText());
    }
    
    public static void sendPacket(Player player, Object packetObject) {
        try {
            Object entityPlayer = craftPlayer.getMethod("getHandle").invoke(craftPlayer.cast(player));
            Object connection;
            if (PluginControl.getNMSVersion().startsWith("v1_17")) {
                List<String> fieldNames = new ArrayList();
                Arrays.asList(entityPlayer.getClass().getFields()).stream().forEach(field -> {fieldNames.add(field.getName());});
                if (fieldNames.contains("networkManager")) {
                    connection = entityPlayer.getClass().getField("networkManager").get(entityPlayer);
                } else {
                    connection = entityPlayer.getClass().getField("connection").get(entityPlayer);
                }
            } else {
                connection = entityPlayer.getClass().getField("playerConnection").get(entityPlayer);
            }
            connection.getClass().getMethod("sendPacket", packet).invoke(connection, packetObject);
        } catch (NoSuchFieldException | SecurityException | NoSuchMethodException | IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
            ex.printStackTrace();
        }
    }
}
