package studio.trc.bukkit.liteannouncer.util;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import java.util.UUID;

import org.bukkit.entity.Player;

import studio.trc.bukkit.liteannouncer.util.tools.ActionBar;

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
            if (nmsVersion.startsWith("v1_17") || nmsVersion.startsWith("v1_18") || nmsVersion.startsWith("v1_19")) {
                chatMessageType = Class.forName("net.minecraft.network.chat.ChatMessageType");
                interfaceChatBaseComponent = Class.forName("net.minecraft.network.chat.IChatBaseComponent");
                packet = Class.forName("net.minecraft.network.protocol.Packet");
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
        } catch (Exception ex) {
            ex.printStackTrace();
        }
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
    
    public static void sendActionBar(Player player, ActionBar actionbar) {
        sendActionBar(player, actionbar.getText());
    }
    
    public static void sendPacket(Player player, Object packetObject) {
        try {
            Object entityPlayer = craftPlayer.getMethod("getHandle").invoke(craftPlayer.cast(player));
            Object connection;
            Field playerConnection = Arrays.stream(entityPlayer.getClass().getFields())
                    .filter(field -> 
                            field.getType().getSimpleName().equals("PlayerConnection"))
                    .findFirst().orElse(null);
            if (playerConnection != null) {
                connection = playerConnection.get(entityPlayer);
            } else {
                return;
            }
            Method sendPacket = Arrays.stream(connection.getClass().getMethods())
                    .filter(method -> 
                            method.getParameterTypes().length == 1 && method.getParameterTypes()[0].getSimpleName().equals("Packet") && method.getReturnType().getSimpleName().equals("void"))
                    .findFirst().orElse(null);
            if (sendPacket != null) {
                sendPacket.invoke(connection, packetObject);
            }
        } catch (SecurityException | NoSuchMethodException | IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
            ex.printStackTrace();
        }
    }
}
