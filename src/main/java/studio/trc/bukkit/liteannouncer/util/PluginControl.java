package studio.trc.bukkit.liteannouncer.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import lombok.Getter;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.Plugin;

import studio.trc.bukkit.liteannouncer.Main;
import studio.trc.bukkit.liteannouncer.util.tools.Announcement;
import studio.trc.bukkit.liteannouncer.async.AnnouncerThread;
import studio.trc.bukkit.liteannouncer.configuration.RobustConfiguration;
import studio.trc.bukkit.liteannouncer.configuration.ConfigurationType;
import studio.trc.bukkit.liteannouncer.configuration.ConfigurationUtil;
import studio.trc.bukkit.liteannouncer.message.JSONComponent;
import studio.trc.bukkit.liteannouncer.util.tools.ActionBar;
import studio.trc.bukkit.liteannouncer.util.tools.Title;
import studio.trc.bukkit.liteannouncer.message.MessageUtil;

public class PluginControl
{
    private static AnnouncerThread thread = null;
    private static final List<Announcement> cacheAnnouncement = new ArrayList();
    @Getter
    private static final Map<String, JSONComponent> cacheJSONComponent = new HashMap<>();
    
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
        MessageUtil.loadPlaceholders();
        MessageUtil.setAdventureAvailable();
        
        if (usePlaceholderAPI()) {
            if (Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) {
                MessageUtil.setEnabledPAPI(true);
                LiteAnnouncerProperties.sendOperationMessage("FindThePlaceholderAPI");
            } else {
                MessageUtil.setEnabledPAPI(false);
                LiteAnnouncerProperties.sendOperationMessage("PlaceholderAPINotFound");
            }
        }
        
        reloadAnnouncements();
        reloadJSONComponents();
        ActionBarUtil.initialize();
        TitleUtil.initialize();
        
        restartAnnouncer();
    }
    
    public static void restartAnnouncer() {
        runBukkitTask(() -> {
            if (thread != null && thread.isAlive()) {
                thread.isRunning = false;
                LiteAnnouncerProperties.sendOperationMessage("AnnouncerWorkEnds");
                thread = new AnnouncerThread();
                thread.start();
                thread.isRunning = true;
                Map<String, String> placeholders = MessageUtil.getDefaultPlaceholders();
                placeholders.put("{number}", String.valueOf(getAnnouncementsByPriority().size()));
                LiteAnnouncerProperties.sendOperationMessage("AnnouncerWorkBegins", placeholders);
            } else {
                thread = new AnnouncerThread();
                thread.isRunning = true;
                thread.start();
                Map<String, String> placeholders = MessageUtil.getDefaultPlaceholders();
                placeholders.put("{number}", String.valueOf(getAnnouncementsByPriority().size()));
                LiteAnnouncerProperties.sendOperationMessage("AnnouncerWorkBegins", placeholders);
            }
        }, 0);
    }

    public static void stopAnnouncer() {
        if (thread != null && thread.isAlive()) {
            thread.isRunning = false;
            LiteAnnouncerProperties.sendOperationMessage("AnnouncerWorkEnds");
        }
    }
    
    public static void reloadAnnouncements() {
        cacheAnnouncement.clear();
        RobustConfiguration config = ConfigurationUtil.getConfig(ConfigurationType.ANNOUNCEMENTS);
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
                            Map<String, String> placeholders = MessageUtil.getDefaultPlaceholders();
                            placeholders.put("{exception}", ex.getLocalizedMessage() != null ? ex.getLocalizedMessage() : "null");
                            placeholders.put("{title}", section);
                            LiteAnnouncerProperties.sendOperationMessage("LoadingTitleFailed", placeholders);
                            ex.printStackTrace();
                        }
                    });
                    List<Title> sequence = new ArrayList<>();
                    config.getStringList("Announcements." + path + ".Titles.Task-Sequence").stream().filter(title -> titles.get(title) != null).forEach(title -> sequence.add(titles.get(title)));
                    announcement.setTitlesOfBroadcast(sequence);
                }
                if (config.get("Announcements." + path + ".ActionBars.Task-Sequence") != null && config.getBoolean("Announcements." + path + ".ActionBars.Enabled")) {
                    List<ActionBar> actionbars = new ArrayList<>();
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
                            Map<String, String> placeholders = MessageUtil.getDefaultPlaceholders();
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
                Map<String, String> placeholders = MessageUtil.getDefaultPlaceholders();
                placeholders.put("{exception}", ex.getLocalizedMessage() != null ? ex.getLocalizedMessage() : "null");
                placeholders.put("{announcement}", path);
                LiteAnnouncerProperties.sendOperationMessage("LoadingAnnouncementFailed", placeholders);
                ex.printStackTrace();
            }
        }
        Map<String, String> placeholders = MessageUtil.getDefaultPlaceholders();
        placeholders.put("{announcements}", String.valueOf(cacheAnnouncement.size()));
        LiteAnnouncerProperties.sendOperationMessage("LoadingAnnouncements", placeholders);
    }
    
    public static void reloadJSONComponents() {
        cacheJSONComponent.clear();
        RobustConfiguration config = ConfigurationUtil.getConfig(ConfigurationType.COMPONENTS);
        config.getConfigurationSection("Json-Components").getKeys(false).stream().forEach(path -> {
            try {
                Map<String, String> placeholders = MessageUtil.getDefaultPlaceholders();
                JSONComponent component = new JSONComponent(
                    MessageUtil.replacePlaceholders(config.getString("Json-Components." + path + ".Text"), placeholders),
                    config.getStringList("Json-Components." + path + ".HoverEvent.Hover-Values").stream().map(line -> MessageUtil.replacePlaceholders(line, placeholders)).collect(Collectors.toList()),
                    config.getString("Json-Components." + path + ".ClickEvent.Action").toUpperCase(),
                    config.getString("Json-Components." + path + ".ClickEvent.Value")
                );
                cacheJSONComponent.put(config.getString("Json-Components." + path + ".Placeholder"), component);
            } catch (Exception ex) {
                Map<String, String> placeholders = MessageUtil.getDefaultPlaceholders();
                placeholders.put("{exception}", ex.getLocalizedMessage() != null ? ex.getLocalizedMessage() : "null");
                placeholders.put("{component}", path);
                LiteAnnouncerProperties.sendOperationMessage("LoadingJsonComponentFailed", placeholders);
                ex.printStackTrace();
            }
        });
        Map<String, String> placeholders = MessageUtil.getDefaultPlaceholders();
        placeholders.put("{components}", String.valueOf(cacheJSONComponent.size()));
        LiteAnnouncerProperties.sendOperationMessage("LoadingComponents", placeholders);
    }
    
    public static List<Announcement> getAnnouncements() {
        return cacheAnnouncement;
    }
    
    public static Announcement getAnnouncementByRandom() {
        List<Announcement> announcements = getAnnouncementsByPriority();
        return announcements.get(getRandom(1, announcements.size()) - 1);
    }
    
    public static int getRandom(int number1, int number2) {
        if (number1 == number2) {
            return number1;
        } else if (number1 > number2) {
            return new Random().nextInt(number1 - number2 + 1) + number2;
        } else if (number2 > number1) {
            return new Random().nextInt(number2 - number1 + 1) + number1;
        }
        return 0;
    }
    
    public static List<Announcement> getAnnouncementsByPriority() {
        return (List<Announcement>) ConfigurationUtil.getConfig(ConfigurationType.ANNOUNCEMENTS).getList("Priority").stream()
                .map(announcement -> cacheAnnouncement.stream().filter(loadedAnnouncement -> loadedAnnouncement.getConfigPath().equals(announcement)).findFirst().orElse(null))
                .filter(element -> element != null)
                .collect(Collectors.toList());
    }
    
    public static void runBukkitTask(Runnable task, long delay) {
        try {
            if (delay == 0) {
                Bukkit.getScheduler().runTask(Main.getInstance(), task);
            } else {
                Bukkit.getScheduler().runTaskLater(Main.getInstance(), task, delay);
            }
        } catch (UnsupportedOperationException ex) {
            //Folia suppport (test)
            Consumer runnable = run -> task.run();
            try {
                Object globalRegionScheduler = Bukkit.class.getMethod("getGlobalRegionScheduler").invoke(null);
                if (delay == 0) {
                    globalRegionScheduler.getClass().getMethod("run", Plugin.class, Consumer.class).invoke(globalRegionScheduler, Main.getInstance(), runnable);
                } else {
                    globalRegionScheduler.getClass().getMethod("runDelayed", Plugin.class, Consumer.class, long.class).invoke(globalRegionScheduler, Main.getInstance(), runnable, delay);
                }
            } catch (Exception e) {
                e.printStackTrace();
                task.run();
            }
        }
    }
}
