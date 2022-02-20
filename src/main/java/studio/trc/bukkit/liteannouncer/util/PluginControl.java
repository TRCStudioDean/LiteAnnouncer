package studio.trc.bukkit.liteannouncer.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.scheduler.BukkitRunnable;

import studio.trc.bukkit.liteannouncer.Main;
import studio.trc.bukkit.liteannouncer.util.tools.Announcement;
import studio.trc.bukkit.liteannouncer.util.tools.JsonComponent;
import studio.trc.bukkit.liteannouncer.async.AnnouncerThread;
import studio.trc.bukkit.liteannouncer.configuration.Configuration;
import studio.trc.bukkit.liteannouncer.configuration.ConfigurationType;
import studio.trc.bukkit.liteannouncer.configuration.ConfigurationUtil;
import studio.trc.bukkit.liteannouncer.util.tools.ActionBar;
import studio.trc.bukkit.liteannouncer.util.tools.Title;

public class PluginControl
{
    private static AnnouncerThread thread = null;
    private static final List<Announcement> cacheAnnouncement = new ArrayList();
    private static final List<JsonComponent> cacheJsonComponent = new ArrayList();
    private static final String nmsVersion = Bukkit.getServer().getClass().getPackage().getName().split("\\.")[3];
    
    public static String getPrefix() {
        return MessageUtil.toColor(ConfigurationUtil.getConfig(ConfigurationType.CONFIG).getString("Prefix"));
    }
    
    public static String getNMSVersion() {
        return nmsVersion;
    }
    
    public static boolean hasPermission(CommandSender sender, String path) {
        if (ConfigurationUtil.getConfig(ConfigurationType.CONFIG).getBoolean(path + ".Default")) return true;
        return sender.hasPermission(ConfigurationUtil.getConfig(ConfigurationType.CONFIG).getString(path + ".Permission"));
    }
    
    public static boolean enabledConsoleBroadcast() {
        return ConfigurationUtil.getConfig(ConfigurationType.CONFIG).getBoolean("Console-Broadcast");
    }
    
    public static boolean usePlaceholderAPI() {
        return ConfigurationUtil.getConfig(ConfigurationType.CONFIG).getBoolean("Use-PlaceholderAPI");
    }
    
    public static boolean enableUpdater() {
        return ConfigurationUtil.getConfig(ConfigurationType.CONFIG).getBoolean("Updater");
    }
    
    public static boolean enableMetrics() {
        return ConfigurationUtil.getConfig(ConfigurationType.CONFIG).getBoolean("Metrics");
    }
    
    public static boolean randomBroadcast() {
        return ConfigurationUtil.getConfig(ConfigurationType.ANNOUNCEMENTS).getBoolean("Random-Broadcast");
    }
    
    public static void reload() {
        ConfigurationUtil.reloadConfig();
        
        if (usePlaceholderAPI()) {
            if (Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) {
                LiteAnnouncerProperties.sendOperationMessage("FindThePlaceholderAPI", new HashMap());
            } else {
                ConfigurationUtil.getConfig(ConfigurationType.CONFIG).set("Use-PlaceholderAPI", false);
                LiteAnnouncerProperties.sendOperationMessage("PlaceholderAPINotFound", new HashMap());
            }
        }
        
        reloadAnnouncements();
        reloadJsonComponents();
        ActionBarUtil.initialize();
        TitleUtil.initialize();
        
        restartAnnouncer();
    }
    
    public static void restartAnnouncer() {
        new BukkitRunnable() {
            @Override
            public void run() {
                if (thread != null && thread.isAlive()) {
                    thread.isRunning = false;
                    LiteAnnouncerProperties.sendOperationMessage("AnnouncerWorkEnds", new HashMap());
                    thread = new AnnouncerThread();
                    thread.start();
                    thread.isRunning = true;
                    Map<String, String> placeholders = new HashMap();
                    placeholders.put("{number}", String.valueOf(getAnnouncementsByPriority().size()));
                    LiteAnnouncerProperties.sendOperationMessage("AnnouncerWorkBegins", placeholders);
                } else {
                    thread = new AnnouncerThread();
                    thread.isRunning = true;
                    thread.start();
                    Map<String, String> placeholders = new HashMap();
                    placeholders.put("{number}", String.valueOf(getAnnouncementsByPriority().size()));
                    LiteAnnouncerProperties.sendOperationMessage("AnnouncerWorkBegins", placeholders);
                }
            }
        }.runTask(Main.getInstance());
    }

    public static void stopAnnouncer() {
        if (thread != null && thread.isAlive()) {
            thread.isRunning = false;
            LiteAnnouncerProperties.sendOperationMessage("AnnouncerWorkEnds", new HashMap());
        }
    }
    
    public static void reloadAnnouncements() {
        cacheAnnouncement.clear();
        Configuration config = ConfigurationUtil.getConfig(ConfigurationType.ANNOUNCEMENTS);
        for (String path : config.getConfigurationSection("Announcements").getKeys(false)) {
            try {
                String name = config.getString("Announcements." + path + ".Name");
                String permission = config.getString("Announcements." + path + ".Permission");
                double delay = config.getDouble("Announcements." + path + ".Delay");
                List<String> messages = config.getStringList("Announcements." + path + ".Messages");
                Announcement announcement = new Announcement(path, name, delay, messages, permission);
                if (config.get("Announcements." + path + ".Titles.Task-Sequence") != null && config.getBoolean("Announcements." + path + ".Titles.Enabled")) {
                    Map<String, Title> titles = new HashMap();
                    config.getConfigurationSection("Announcements." + path + ".Titles.Titles-Setting").getKeys(false).stream().forEach(section -> {
                        try {
                            double fadein = config.getDouble("Announcements." + path + ".Titles.Titles-Setting." + section + ".Fade-In");
                            double stay = config.getDouble("Announcements." + path + ".Titles.Titles-Setting." + section + ".Stay");
                            double fadeout = config.getDouble("Announcements." + path + ".Titles.Titles-Setting." + section + ".Fade-Out");
                            double titleDelay = config.getDouble("Announcements." + path + ".Titles.Titles-Setting." + section + ".Delay");
                            String title = config.getString("Announcements." + path + ".Titles.Titles-Setting." + section + ".Title");
                            String subTitle = config.getString("Announcements." + path + ".Titles.Titles-Setting." + section + ".Sub-Title");
                            titles.put(section, new Title(fadein, stay, fadeout, titleDelay, title, subTitle));
                        } catch (Exception ex) {
                            Map<String, String> placeholders = new HashMap();
                            placeholders.put("{exception}", ex.getLocalizedMessage() != null ? ex.getLocalizedMessage() : "null");
                            placeholders.put("{title}", section);
                            LiteAnnouncerProperties.sendOperationMessage("LoadingTitleFailed", placeholders);
                            ex.printStackTrace();
                        }
                    });
                    List<Title> sequence = new LinkedList();
                    config.getStringList("Announcements." + path + ".Titles.Task-Sequence").stream().filter(title -> titles.get(title) != null).forEach(title -> {
                        sequence.add(titles.get(title));
                    });
                    announcement.setTitlesOfBroadcast(sequence);
                }
                if (config.get("Announcements." + path + ".ActionBars.Task-Sequence") != null && config.getBoolean("Announcements." + path + ".ActionBars.Enabled")) {
                    List<ActionBar> actionbars = new LinkedList();
                    for (Map<String, Object> maps : (List<Map<String, Object>>) config.getList("Announcements." + path + ".ActionBars.Task-Sequence")) {
                        String actionbar = null;
                        try {
                            double actionbarDelay = 0;
                            for (String string : maps.keySet()) {
                                actionbarDelay = Double.valueOf(maps.get(string).toString());
                                actionbar = string;
                                break;
                            }
                            actionbars.add(new ActionBar(actionbar, actionbarDelay));
                        } catch (Exception ex) {
                            Map<String, String> placeholders = new HashMap();
                            placeholders.put("{exception}", ex.getLocalizedMessage() != null ? ex.getLocalizedMessage() : "null");
                            placeholders.put("{actionbar}", actionbar == null ? "null" : actionbar);
                            LiteAnnouncerProperties.sendOperationMessage("LoadingActionbarFailed", placeholders);
                            ex.printStackTrace();
                        }
                    }
                    announcement.setActionBarsOfBroadcast(actionbars);
                }
                cacheAnnouncement.add(announcement);
            } catch (Exception ex) {
                Map<String, String> placeholders = new HashMap();
                placeholders.put("{exception}", ex.getLocalizedMessage() != null ? ex.getLocalizedMessage() : "null");
                placeholders.put("{announcement}", path);
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
        Configuration config = ConfigurationUtil.getConfig(ConfigurationType.COMPONENTS);
        config.getConfigurationSection("Json-Components").getKeys(false).stream().forEach(path -> {
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
                        hoverText.add(new TextComponent(MessageUtil.prefix(hover)));
                        if (end != array.size()) {
                            hoverText.add(new TextComponent("\n"));
                        }
                    }
                    he = new HoverEvent(HoverEvent.Action.valueOf(config.getString("Json-Components." + path + ".HoverEvent.Action").toUpperCase()), hoverText.toArray(new BaseComponent[0]));
                }
                
                if (config.contains("Json-Components." + path + ".ClickEvent")) {
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
                placeholders.put("{component}", path);
                LiteAnnouncerProperties.sendOperationMessage("LoadingJsonComponentFailed", placeholders);
                ex.printStackTrace();
            }
        });
        Map<String, String> placeholders = new HashMap();
        placeholders.put("{components}", String.valueOf(cacheJsonComponent.size()));
        LiteAnnouncerProperties.sendOperationMessage("LoadingComponents", placeholders);
    }
    
    public static List<Announcement> getAnnouncements() {
        return cacheAnnouncement;
    }
    
    private static final Random random = new Random();
    
    public static Announcement getAnnouncementByRandom() {
        List<Announcement> announcements = getAnnouncementsByPriority();
        return announcements.get(random.nextInt(announcements.size() - 1));
    }
    
    public static List<Announcement> getAnnouncementsByPriority() {
        return (List<Announcement>) ConfigurationUtil.getConfig(ConfigurationType.ANNOUNCEMENTS).getList("Priority").stream()
                .map(announcement -> cacheAnnouncement.stream().filter(loadedAnnouncement -> loadedAnnouncement.getConfigPath().equals(announcement)).findFirst().orElse(null))
                .filter(element -> element != null)
                .collect(Collectors.toList());
    }
    
    public static List<JsonComponent> getJsonComponents() {
        return cacheJsonComponent;
    }
}
