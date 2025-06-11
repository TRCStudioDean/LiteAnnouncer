package studio.trc.bukkit.liteannouncer.util;

import studio.trc.bukkit.liteannouncer.message.MessageUtil;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import java.util.UUID;

import org.bukkit.Bukkit;
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
    public static Method sendPacket = null;
    
    public static void initialize() {
        try {
            if (Bukkit.getBukkitVersion().startsWith("1.17") || Bukkit.getBukkitVersion().startsWith("1.18") || Bukkit.getBukkitVersion().startsWith("1.19") || Bukkit.getBukkitVersion().startsWith("1.20") || Bukkit.getBukkitVersion().startsWith("1.21")) {
                chatMessageType = Class.forName("net.minecraft.network.chat.ChatMessageType");
                interfaceChatBaseComponent = Class.forName("net.minecraft.network.chat.IChatBaseComponent");
                packet = Class.forName("net.minecraft.network.protocol.Packet");
                clientboundSetActionBarTextPacket = Class.forName("net.minecraft.network.protocol.game.ClientboundSetActionBarTextPacket");
                craftChatMessage = Class.forName("org.bukkit.craftbukkit" + getPackagePath() + "util.CraftChatMessage");
            } else {
                interfaceChatBaseComponent = Class.forName("net.minecraft.server" + getPackagePath() + "IChatBaseComponent");
                packet = Class.forName("net.minecraft.server" + getPackagePath() + "Packet");
                packetPlayOutChat = Class.forName("net.minecraft.server" + getPackagePath() + "PacketPlayOutChat");
                chatComponentText = Class.forName("net.minecraft.server" + getPackagePath() + "ChatComponentText"); 
            }
            if (Bukkit.getBukkitVersion().startsWith("1.12") || Bukkit.getBukkitVersion().startsWith("1.13") || Bukkit.getBukkitVersion().startsWith("1.14") || Bukkit.getBukkitVersion().startsWith("1.15") || Bukkit.getBukkitVersion().startsWith("1.16")) {
                chatMessageType = Class.forName("net.minecraft.server" + getPackagePath() + "ChatMessageType");
            }
            craftPlayer = Class.forName("org.bukkit.craftbukkit" + getPackagePath() + "entity.CraftPlayer");
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
    
    public static void sendActionBar(Player player, String text) {
        if (Bukkit.getBukkitVersion().startsWith("1.7")) return;
        if (text == null) return;
        text = MessageUtil.replacePlaceholders(player, text, new HashMap());
        try {
            player.getClass().getMethod("sendActionBar", String.class).invoke(player, text);
        } catch (NoSuchMethodError ex) {
            try {
                Object actionbar;
                // 1.8 - 1.11.2
                if (Bukkit.getBukkitVersion().startsWith("1.8") || Bukkit.getBukkitVersion().startsWith("1.9") || Bukkit.getBukkitVersion().startsWith("1.10") || Bukkit.getBukkitVersion().startsWith("1.11")) {
                    actionbar = packetPlayOutChat.getConstructor(interfaceChatBaseComponent, byte.class).newInstance(
                            chatComponentText.getConstructor(String.class).newInstance(text),
                            (byte) 2);
                // 1.12 - 1.15.2
                } else if (Bukkit.getBukkitVersion().startsWith("1.12") || Bukkit.getBukkitVersion().startsWith("1.13") || Bukkit.getBukkitVersion().startsWith("1.14") || Bukkit.getBukkitVersion().startsWith("1.15")) {
                    actionbar = packetPlayOutChat.getConstructor(interfaceChatBaseComponent, chatMessageType).newInstance(
                            chatComponentText.getConstructor(String.class).newInstance(text),
                            chatMessageType.getMethod("a", byte.class).invoke(chatMessageType, (byte) 2));
                // 1.16 - 1.16.5
                } else if (Bukkit.getBukkitVersion().startsWith("1.16")) {
                    actionbar = packetPlayOutChat.getConstructor(interfaceChatBaseComponent, chatMessageType, UUID.class).newInstance(
                            chatComponentText.getConstructor(String.class).newInstance(text),
                            chatMessageType.getMethod("a", byte.class).invoke(chatMessageType, (byte) 2),
                            UUID.randomUUID());
                // 1.17 - 1.18
                } else if (Bukkit.getBukkitVersion().startsWith("1.17") || Bukkit.getBukkitVersion().startsWith("1.18")) {
                    actionbar = clientboundSetActionBarTextPacket.getConstructor(interfaceChatBaseComponent).newInstance(craftChatMessage.getMethod("fromStringOrNull", String.class).invoke(null, text));
                // 1.19 +
                } else {
                    actionbar = clientboundSetActionBarTextPacket.getConstructor(interfaceChatBaseComponent).newInstance(Array.get(craftChatMessage.getMethod("fromString", String.class).invoke(null, text), 0));
                }
                sendPacket(player, actionbar);
            } catch (Exception ex1) {
                ex.printStackTrace();
            }
        } catch (Exception ex) {
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
            if (sendPacket == null) {
                sendPacket = Arrays.stream(connection.getClass().getMethods())
                    .filter(method -> 
                            method.getParameterTypes().length == 1 && method.getParameterTypes()[0].getSimpleName().equals("Packet") && method.getReturnType().getSimpleName().equals("void"))
                    .findFirst().orElse(null);
            }
            if (sendPacket != null) {
                sendPacket.invoke(connection, packetObject);
            }
        } catch (SecurityException | NoSuchMethodException | IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
            ex.printStackTrace();
        }
    }
    
    public static String getPackagePath() {
        try {
            return "." + Bukkit.getServer().getClass().getPackage().getName().split("\\.")[3] + ".";
        } catch (ArrayIndexOutOfBoundsException ex) {
            return ".";
        }
    }
}
