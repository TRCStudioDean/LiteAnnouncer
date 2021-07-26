package studio.trc.bukkit.liteannouncer.util;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.bukkit.entity.Player;
import studio.trc.bukkit.liteannouncer.async.AnnouncerThread;
import studio.trc.bukkit.liteannouncer.util.tools.TitleOfBroadcast;

public class TitleUtil
{
    public static Class<?> enumTitleAction;
    public static Class<?> enumPlayerInfoAction;
    public static Class<?> clientboundSetTitlesAnimationPacket;
    public static Class<?> clientboundSetTitleTextPacket;
    public static Class<?> clientboundSetSubTitleTextPacket;
    public static Class<?> craftChatMessage;
    public static Class<?> chatComponentText;
    public static Class<?> interfaceChatBaseComponent;
    public static Class<?> craftPlayer;
    public static Class<?> packetPlayOutTitle;
    public static Class<?> packet;
    
    public static void initialize() {
        String nmsVersion = PluginControl.getNMSVersion();
        try {
            if (nmsVersion.equals("v1_8_R1")) {
                enumTitleAction = Class.forName("net.minecraft.server." + nmsVersion + ".EnumTitleAction");
                enumPlayerInfoAction = Class.forName("net.minecraft.server." + nmsVersion + ".EnumPlayerInfoAction");
            } else if (nmsVersion.startsWith("v1_8") || nmsVersion.startsWith("v1_9") || nmsVersion.startsWith("v1_10") || nmsVersion.startsWith("v1_11") || nmsVersion.startsWith("v1_12") || nmsVersion.startsWith("v1_13") || nmsVersion.startsWith("v1_14") || nmsVersion.startsWith("v1_15") || nmsVersion.startsWith("v1_16")) {
                enumTitleAction = Class.forName("net.minecraft.server." + nmsVersion + ".PacketPlayOutTitle$EnumTitleAction");
                enumPlayerInfoAction = Class.forName("net.minecraft.server." + nmsVersion + ".PacketPlayOutPlayerInfo$EnumPlayerInfoAction");
            }
            if (nmsVersion.startsWith("v1_17")) {
                chatComponentText = Class.forName("net.minecraft.network.chat.ChatComponentText");
                interfaceChatBaseComponent = Class.forName("net.minecraft.network.chat.IChatBaseComponent");
                packet = Class.forName("net.minecraft.network.protocol.Packet");
                clientboundSetTitlesAnimationPacket = Class.forName("net.minecraft.network.protocol.game.ClientboundSetTitlesAnimationPacket");
                clientboundSetTitleTextPacket = Class.forName("net.minecraft.network.protocol.game.ClientboundSetTitleTextPacket");
                clientboundSetSubTitleTextPacket = Class.forName("net.minecraft.network.protocol.game.ClientboundSetSubtitleTextPacket");
                craftChatMessage = Class.forName("org.bukkit.craftbukkit." + nmsVersion + ".util.CraftChatMessage");
            } else if (!nmsVersion.startsWith("v1_7")) {
                interfaceChatBaseComponent = Class.forName("net.minecraft.server." + nmsVersion + ".IChatBaseComponent");
                packet = Class.forName("net.minecraft.server." + nmsVersion + ".Packet");
                chatComponentText = Class.forName("net.minecraft.server." + nmsVersion + ".ChatComponentText");
                packetPlayOutTitle = Class.forName("net.minecraft.server." + nmsVersion + ".PacketPlayOutTitle");
                craftChatMessage = Class.forName("org.bukkit.craftbukkit." + nmsVersion + ".util.CraftChatMessage");
            }
            craftPlayer = Class.forName("org.bukkit.craftbukkit." + nmsVersion + ".entity.CraftPlayer");
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
    
    public static void sendTitle(Player player, String title, String subTitle, double fadein, double stay, double fadeout) {
        String nmsVersion = PluginControl.getNMSVersion();
        if (nmsVersion.startsWith("v1_7")) return;
        title = MessageUtil.toColor(MessageUtil.replacePlaceholders(player, title, new HashMap()));
        subTitle = MessageUtil.toColor(MessageUtil.replacePlaceholders(player, subTitle, new HashMap()));
        try {
            if (nmsVersion.startsWith("v1_17")) {
                Object animationPacket = clientboundSetTitlesAnimationPacket.getConstructor(int.class, int.class, int.class).newInstance((int) (fadein * 20), (int) (stay * 20), (int) (fadeout * 20));
                sendPacket(player, animationPacket);
                if (title != null) {
                    Object titleMessagePacket = clientboundSetTitleTextPacket.getConstructor(interfaceChatBaseComponent).newInstance(craftChatMessage.getMethod("fromStringOrNull", String.class).invoke(null, title));
                    sendPacket(player, titleMessagePacket);
                }
                if (subTitle != null) {
                    Object subTitleMessagePacket = clientboundSetSubTitleTextPacket.getConstructor(interfaceChatBaseComponent).newInstance(craftChatMessage.getMethod("fromStringOrNull", String.class).invoke(null, subTitle));
                    sendPacket(player, subTitleMessagePacket);
                }
            } else {
                Object titleEnumPacket = enumTitleAction.getMethod("valueOf", String.class).invoke(enumTitleAction, "TITLE");
                Object subTitleEnumPacket = enumTitleAction.getMethod("valueOf", String.class).invoke(enumTitleAction, "SUBTITLE");
                Object animationPacket = packetPlayOutTitle.getConstructor(int.class, int.class, int.class).newInstance((int) (fadein * 20), (int) (stay * 20), (int) (fadeout * 20));
                sendPacket(player, animationPacket);
                if (title != null) {
                    Object titleMessagePacket = packetPlayOutTitle.getConstructor(enumTitleAction, interfaceChatBaseComponent).newInstance(titleEnumPacket, craftChatMessage.getMethod("fromStringOrNull", String.class).invoke(null, title));
                    sendPacket(player, titleMessagePacket);
                }
                if (subTitle != null) {
                    Object subTitleMessagePacket = packetPlayOutTitle.getConstructor(enumTitleAction, interfaceChatBaseComponent).newInstance(subTitleEnumPacket, craftChatMessage.getMethod("fromStringOrNull", String.class).invoke(null, subTitle));
                    sendPacket(player, subTitleMessagePacket);
                }
            }
        } catch (SecurityException | IllegalArgumentException | IllegalAccessException | NoSuchMethodException | InvocationTargetException | InstantiationException ex) {
            ex.printStackTrace();
        }
    }
    
    public static void sendTitle(Player player, TitleOfBroadcast titleOfBroadcast) {
        sendTitle(player, titleOfBroadcast.getTitle(), titleOfBroadcast.getSubTitle(), titleOfBroadcast.getFadein(), titleOfBroadcast.getStay(), titleOfBroadcast.getFadeout());
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
