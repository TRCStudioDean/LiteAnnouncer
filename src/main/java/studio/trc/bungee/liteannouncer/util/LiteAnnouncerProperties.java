package studio.trc.bungee.liteannouncer.util;

import java.io.IOException;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;

import studio.trc.bungee.liteannouncer.Main;

public class LiteAnnouncerProperties
{
    /**
     * System Language
     */
    public static Properties propertiesFile = new Properties();
    public static Locale lang = Locale.getDefault();
    
    public static void reloadProperties() {
        if (lang.equals(Locale.SIMPLIFIED_CHINESE) || lang.equals(Locale.CHINESE)) {
            try {
                propertiesFile.load(Main.class.getResourceAsStream("/Languages/Chinese.properties"));
            } catch (IOException ex) {}
        } else {
            try {
                propertiesFile.load(Main.class.getResourceAsStream("/Languages/English.properties"));
            } catch (IOException ex) {}
        }
        sendOperationMessage("LanguageLoaded");
    }
    
    public static void sendOperationMessage(String path) {
        CommandSender sender = ProxyServer.getInstance().getConsole();
        if (propertiesFile.containsKey(path)) {
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', propertiesFile.getProperty(path)));
        }
    }
    
    public static void sendOperationMessage(String path, boolean replacePrefix) {
        CommandSender sender = ProxyServer.getInstance().getConsole();
        if (propertiesFile.containsKey(path)) {
            if (replacePrefix) {
                sender.sendMessage(ChatColor.translateAlternateColorCodes('&', propertiesFile.getProperty(path).replace("{prefix}", PluginControl.getPrefix())));
            } else {
                sender.sendMessage(ChatColor.translateAlternateColorCodes('&', propertiesFile.getProperty(path)));
            }
        }
    }
    
    public static void sendOperationMessage(String path, Map<String, String> placeholders) {
        CommandSender sender = ProxyServer.getInstance().getConsole();
        if (propertiesFile.containsKey(path)) {
            String message = propertiesFile.getProperty(path);
            for (String placeholder : placeholders.keySet()) {
                message = message.replace(placeholder, placeholders.get(placeholder));
            }
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', message.replace("{prefix}", PluginControl.getPrefix())));
        }
    }
}
