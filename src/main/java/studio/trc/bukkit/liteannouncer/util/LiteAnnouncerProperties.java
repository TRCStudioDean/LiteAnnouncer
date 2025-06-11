package studio.trc.bukkit.liteannouncer.util;

import studio.trc.bukkit.liteannouncer.message.MessageUtil;
import java.io.IOException;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;

import studio.trc.bukkit.liteannouncer.Main;

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
        CommandSender sender = Bukkit.getConsoleSender();
        if (propertiesFile.containsKey(path)) {
            MessageUtil.sendMessage(sender, propertiesFile.getProperty(path));
        }
    }
    
    public static void sendOperationMessage(String path, Map<String, String> placeholders) {
        CommandSender sender = Bukkit.getConsoleSender();
        if (propertiesFile.containsKey(path)) {
            String message = propertiesFile.getProperty(path);
            MessageUtil.sendMessage(sender, message, placeholders);
        }
    }
}
