package studio.trc.bungee.liteannouncer.util;

import java.io.IOException;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;

import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;

import studio.trc.bungee.liteannouncer.Main;
import studio.trc.bungee.liteannouncer.message.color.ColorUtils;
import studio.trc.bungee.liteannouncer.message.MessageUtil;

public class LiteAnnouncerProperties
{
    /**
     * System Language
     */
    public static Properties propertiesFile = new Properties();
    public static Locale lang = Locale.getDefault();
    
    public static void reloadProperties() {
        try {
            propertiesFile.load(Main.class.getResourceAsStream("/Languages/" + MessageUtil.Language.getLocaleLanguage().getFolderName() + ".properties"));
        } catch (IOException ex) {}
        sendOperationMessage("LanguageLoaded");
    }
    
    public static void sendOperationMessage(String path) {
        CommandSender sender = ProxyServer.getInstance().getConsole();
        if (propertiesFile.containsKey(path)) {
            sender.sendMessage(ColorUtils.toColor(propertiesFile.getProperty(path)));
        }
    }
    
    public static void sendOperationMessage(String path, Map<String, String> placeholders) {
        CommandSender sender = ProxyServer.getInstance().getConsole();
        if (propertiesFile.containsKey(path)) {
            String message = propertiesFile.getProperty(path);
            sender.sendMessage(ColorUtils.toColor(MessageUtil.replacePlaceholders(sender, message, placeholders)));
        }
    }
}
