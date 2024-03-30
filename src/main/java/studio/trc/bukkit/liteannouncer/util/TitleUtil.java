package studio.trc.bukkit.liteannouncer.util;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;

import org.bukkit.entity.Player;

import studio.trc.bukkit.liteannouncer.util.tools.Title;

public class TitleUtil
{
    public static Class<?> enumTitleAction;
    public static Class<?> enumPlayerInfoAction;
    public static Class<?> clientboundSetTitlesAnimationPacket;
    public static Class<?> clientboundSetTitleTextPacket;
    public static Class<?> clientboundSetSubTitleTextPacket;
    public static Class<?> craftChatMessage;
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
            if (nmsVersion.startsWith("v1_17") || nmsVersion.startsWith("v1_18") || nmsVersion.startsWith("v1_19") || nmsVersion.startsWith("v1_20")) {
                interfaceChatBaseComponent = Class.forName("net.minecraft.network.chat.IChatBaseComponent");
                packet = Class.forName("net.minecraft.network.protocol.Packet");
                clientboundSetTitlesAnimationPacket = Class.forName("net.minecraft.network.protocol.game.ClientboundSetTitlesAnimationPacket");
                clientboundSetTitleTextPacket = Class.forName("net.minecraft.network.protocol.game.ClientboundSetTitleTextPacket");
                clientboundSetSubTitleTextPacket = Class.forName("net.minecraft.network.protocol.game.ClientboundSetSubtitleTextPacket");
                craftChatMessage = Class.forName("org.bukkit.craftbukkit." + nmsVersion + ".util.CraftChatMessage");
            } else if (!nmsVersion.startsWith("v1_7")) {
                interfaceChatBaseComponent = Class.forName("net.minecraft.server." + nmsVersion + ".IChatBaseComponent");
                packet = Class.forName("net.minecraft.server." + nmsVersion + ".Packet");
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
        title = MessageUtil.replacePlaceholders(player, title, new HashMap());
        subTitle = MessageUtil.replacePlaceholders(player, subTitle, new HashMap());
        try {
            if (nmsVersion.startsWith("v1_17") || nmsVersion.startsWith("v1_18") || nmsVersion.startsWith("v1_19") || nmsVersion.startsWith("v1_20")) {
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
                    Object titleMessagePacket = packetPlayOutTitle.getConstructor(enumTitleAction, interfaceChatBaseComponent).newInstance(titleEnumPacket, Array.get(craftChatMessage.getMethod("fromString", String.class).invoke(null, title), 0));
                    sendPacket(player, titleMessagePacket);
                }
                if (subTitle != null) {
                    Object subTitleMessagePacket = packetPlayOutTitle.getConstructor(enumTitleAction, interfaceChatBaseComponent).newInstance(subTitleEnumPacket, Array.get(craftChatMessage.getMethod("fromString", String.class).invoke(null, subTitle), 0));
                    sendPacket(player, subTitleMessagePacket);
                }
            }
        } catch (SecurityException | IllegalArgumentException | IllegalAccessException | NoSuchMethodException | InvocationTargetException | InstantiationException ex) {
            ex.printStackTrace();
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
