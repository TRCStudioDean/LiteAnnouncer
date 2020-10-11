package studio.trc.bungee.liteannouncer.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;

import studio.trc.bungee.liteannouncer.async.AnnouncerThread;
import studio.trc.bungee.liteannouncer.configuration.ConfigurationFile;
import studio.trc.bungee.liteannouncer.configuration.ConfigurationType;
import studio.trc.bungee.liteannouncer.configuration.ConfigurationUtil;
import studio.trc.bungee.liteannouncer.util.tools.Announcement;
import studio.trc.bungee.liteannouncer.util.tools.JsonComponent;

public class PluginControl
{
    private static AnnouncerThread thread = null;
    private static final List<Announcement> cacheAnnouncement = new ArrayList();
    private static final List<JsonComponent> cacheJsonComponent = new ArrayList();
    
    public static String getPrefix() {
        return ConfigurationUtil.getConfig(ConfigurationType.CONFIG).getString("Prefix").replace("&", "§");
    }
    
    public static boolean hasPermission(CommandSender sender, String path) {
        if (ConfigurationUtil.getConfig(ConfigurationType.CONFIG).getBoolean(path + ".Default")) return true;
        return sender.hasPermission(ConfigurationUtil.getConfig(ConfigurationType.CONFIG).getString(path + ".Permission"));
    }
    
    public static void reload() {
        ConfigurationUtil.reloadConfig();
        
        reloadAnnouncements();
        reloadJsonComponents();
        
        restartAnnouncer();
    }
    
    public static void restartAnnouncer() {
        if (thread != null && thread.isAlive()) {
            thread.isRunning = false;
            LiteAnnouncerProperties.sendOperationMessage("AnnouncerWorkEnds", true);
            thread = new AnnouncerThread();
            thread.start();
            thread.isRunning = true;
            LiteAnnouncerProperties.sendOperationMessage("AnnouncerWorkBegins", true);
        } else {
            thread = new AnnouncerThread();
            thread.isRunning = true;
            thread.start();
            LiteAnnouncerProperties.sendOperationMessage("AnnouncerWorkBegins", true);
        }
    }

    public static void stopAnnouncer() {
        if (thread != null && thread.isAlive()) {
            thread.isRunning = false;
            LiteAnnouncerProperties.sendOperationMessage("AnnouncerWorkEnds", true);
        }
    }
    
    public static void reloadAnnouncements() {
        cacheAnnouncement.clear();
        ConfigurationFile config = ConfigurationUtil.getConfig(ConfigurationType.ANNOUNCEMENTS);
        for (String path : config.getStringList("Priority")) {
            try {
                String name = config.getString("Announcements." + path + ".Name");
                String permission = config.contains("Announcements." + path + ".Permission") ? config.getString("Announcements." + path + ".Permission") : null;
                double delay = config.getDouble("Announcements." + path + ".Delay");
                List<String> messages = config.getStringList("Announcements." + path + ".Messages");
                List<String> whitelist = new ArrayList();
                if (config.getBoolean("Announcements." + path + ".Whitelist-Server.Enabled")) {
                    whitelist.addAll(config.getStringList("Announcements." + path + ".Whitelist-Server.Servers"));
                }
                Announcement announcement = new Announcement(name, delay, messages, permission, whitelist);
                cacheAnnouncement.add(announcement);
            } catch (Exception ex) {
                Map<String, String> placeholders = new HashMap();
                placeholders.put("{exception}", ex.getLocalizedMessage() != null ? ex.getLocalizedMessage() : "null");
                LiteAnnouncerProperties.sendOperationMessage("LoadingAnnouncementFailed", placeholders);
                ex.printStackTrace();
            }
        }
        Map<String, String> placeholders = new HashMap();
        placeholders.put("{announcements}", String.valueOf(cacheAnnouncement.size()));
        LiteAnnouncerProperties.sendOperationMessage("LoadingAnnouncements", placeholders);
    }
    
    public static void reloadJsonComponents() {
        cacheJsonComponent.clear();
        ConfigurationFile config = ConfigurationUtil.getConfig(ConfigurationType.COMPONENTS);
        for (String path : config.getConfigurationSection("Json-Components").getKeys()) {
            try {
                String placeholder = config.getString("Json-Components." + path + ".Placeholder");
                HoverEvent he = null;
                ClickEvent ce = null;
                if (config.contains("Json-Components." + path + ".HoverEvent")) {
                    List<BaseComponent> hoverText = new ArrayList();
                    int end = 0;
                    List<String> array = config.getStringList("Json-Components." + path + ".HoverEvent.Hover-Values");
                    for (String hover : array) {
                        end++;
                        hoverText.add(new TextComponent(ChatColor.translateAlternateColorCodes('&', hover).replace("{prefix}", PluginControl.getPrefix())));
                        if (end != array.size()) {
                            hoverText.add(new TextComponent("\n"));
                        }
                    }
                    he = new HoverEvent(HoverEvent.Action.valueOf(config.getString("Json-Components." + path + ".HoverEvent.Action").toUpperCase()), hoverText.toArray(new BaseComponent[0]));
                }
                
                if (config.contains("Json-Components." + path + ".HoverEvent")) {
                    ce = new ClickEvent(ClickEvent.Action.valueOf(config.getString("Json-Components." + path + ".ClickEvent.Action").toUpperCase()), config.getString("Json-Components." + path + ".ClickEvent.Value"));
                }
                BaseComponent bc = new TextComponent(ChatColor.translateAlternateColorCodes('&', config.getString("Json-Components." + path + ".Text")).replace("{prefix}", PluginControl.getPrefix()));
                if (he != null) bc.setHoverEvent(he);
                if (ce != null) bc.setClickEvent(ce);
                JsonComponent jc = new JsonComponent(placeholder, bc);
                cacheJsonComponent.add(jc);
            } catch (Exception ex) {
                Map<String, String> placeholders = new HashMap();
                placeholders.put("{exception}", ex.getLocalizedMessage() != null ? ex.getLocalizedMessage() : "null");
                LiteAnnouncerProperties.sendOperationMessage("LoadingJsonComponentFailed", placeholders);
                ex.printStackTrace();
            }
        }
        Map<String, String> placeholders = new HashMap();
        placeholders.put("{components}", String.valueOf(cacheJsonComponent.size()));
        LiteAnnouncerProperties.sendOperationMessage("LoadingComponents", placeholders);
    }
    
    public static List<Announcement> getAnnouncements() {
        return cacheAnnouncement;
    }
    
    public static List<JsonComponent> getJsonComponents() {
        return cacheJsonComponent;
    }
}
