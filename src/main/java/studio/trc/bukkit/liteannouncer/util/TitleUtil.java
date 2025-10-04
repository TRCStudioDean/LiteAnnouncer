package studio.trc.bukkit.liteannouncer.util;

import studio.trc.bukkit.liteannouncer.message.MessageUtil;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import studio.trc.bukkit.liteannouncer.util.tools.Title;

public class TitleUtil
{
    public static Class<?> enumTitleAction;
    public static Class<?> enumPlayerInfoAction;
    public static Class<?> craftChatMessage;
    public static Class<?> interfaceChatBaseComponent;
    public static Class<?> craftPlayer;
    public static Class<?> packetPlayOutTitle;
    public static Class<?> packet;
    public static Method sendPacket = null;
    
    public static void initialize() {
        try {
            Player.class.getMethod("sendTitle", String.class, String.class, int.class, int.class, int.class);
        } catch (Exception ex) {
            try {
                if (Bukkit.getBukkitVersion().startsWith("1.8-R0.1")) {
                    enumTitleAction = Class.forName("net.minecraft.server" + getPackagePath() + "EnumTitleAction");
                    enumPlayerInfoAction = Class.forName("net.minecraft.server" + getPackagePath() + "EnumPlayerInfoAction");
                } else if (Bukkit.getBukkitVersion().startsWith("1.8") || Bukkit.getBukkitVersion().startsWith("1.9") || Bukkit.getBukkitVersion().startsWith("1.10") || Bukkit.getBukkitVersion().startsWith("1.11") || Bukkit.getBukkitVersion().startsWith("1.12") || Bukkit.getBukkitVersion().startsWith("1.13") || Bukkit.getBukkitVersion().startsWith("1.14") || Bukkit.getBukkitVersion().startsWith("1.15") || Bukkit.getBukkitVersion().startsWith("1.16")) {
                    enumTitleAction = Class.forName("net.minecraft.server" + getPackagePath() + "PacketPlayOutTitle$EnumTitleAction");
                    enumPlayerInfoAction = Class.forName("net.minecraft.server" + getPackagePath() + "PacketPlayOutPlayerInfo$EnumPlayerInfoAction");
                }
                if (Bukkit.getBukkitVersion().startsWith("1.17") || Bukkit.getBukkitVersion().startsWith("1.18") || Bukkit.getBukkitVersion().startsWith("1.19") || Bukkit.getBukkitVersion().startsWith("1.20") || Bukkit.getBukkitVersion().startsWith("1.21")) {
                    interfaceChatBaseComponent = Class.forName("net.minecraft.network.chat.IChatBaseComponent");
                    packet = Class.forName("net.minecraft.network.protocol.Packet");
                    craftChatMessage = Class.forName("org.bukkit.craftbukkit" + getPackagePath() + "util.CraftChatMessage");
                } else if (!Bukkit.getBukkitVersion().startsWith("1.7")) {
                    interfaceChatBaseComponent = Class.forName("net.minecraft.server" + getPackagePath() + "IChatBaseComponent");
                    packet = Class.forName("net.minecraft.server" + getPackagePath() + "Packet");
                    packetPlayOutTitle = Class.forName("net.minecraft.server" + getPackagePath() + "PacketPlayOutTitle");
                    craftChatMessage = Class.forName("org.bukkit.craftbukkit" + getPackagePath() + "util.CraftChatMessage");
                }
                craftPlayer = Class.forName("org.bukkit.craftbukkit" + getPackagePath() + "entity.CraftPlayer");
            } catch (Exception exception) {
                exception.printStackTrace();
            }
        }
    }
    
    public static void sendTitle(Player player, String title, String subTitle, double fadein, double stay, double fadeout) {
        if (Bukkit.getBukkitVersion().startsWith("1.7")) return;
        title = MessageUtil.replacePlaceholders(player, title, new HashMap());
        subTitle = MessageUtil.replacePlaceholders(player, subTitle, new HashMap());
        try {
            player.sendTitle(title, subTitle, (int) (fadein * 20), (int) (stay * 20), (int) (fadeout * 20));
        } catch (NoSuchMethodError ex) {
            try {
                Object titleEnumPacket = enumTitleAction.getMethod("valueOf", String.class).invoke(enumTitleAction, "TITLE");
                Object subTitleEnumPacket = enumTitleAction.getMethod("valueOf", String.class).invoke(enumTitleAction, "SUBTITLE");
                Object animationPacket = packetPlayOutTitle.getConstructor(int.class, int.class, int.class).newInstance((int) (fadein * 20), (int) (stay * 20), (int) (fadeout * 20));
                sendPacket(player, animationPacket);
                if (title != null) {
                    Object titleMessagePacket = packetPlayOutTitle.getConstructor(enumTitleAction, interfaceChatBaseComponent).newInstance(titleEnumPacket, Array.get(craftChatMessage.getMethod("fromString", String.class).invoke(null, title), 0));
                    sendPacket(player, titleMessagePacket);
                }
                if (subTitle != null) {
                    Object subTitleMessagePacket = packetPlayOutTitle.getConstructor(enumTitleAction, interfaceChatBaseComponent).newInstance(subTitleEnumPacket, Array.get(craftChatMessage.getMethod("fromString", String.class).invoke(null, subTitle), 0));
                    sendPacket(player, subTitleMessagePacket);
                }
            } catch (Exception ex1) {
                ex1.printStackTrace();
            }
        }
    }
    
    public static void sendTitle(Player player, Title titleOfBroadcast) {
        sendTitle(player, titleOfBroadcast.getTitle(), titleOfBroadcast.getSubTitle(), titleOfBroadcast.getFadein(), titleOfBroadcast.getStay(), titleOfBroadcast.getFadeout());
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
