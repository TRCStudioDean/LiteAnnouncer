package studio.trc.bukkit.liteannouncer.util.tools;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import lombok.Getter;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import studio.trc.bukkit.liteannouncer.async.AnnouncerThread;
import studio.trc.bukkit.liteannouncer.configuration.ConfigurationType;
import studio.trc.bukkit.liteannouncer.configuration.ConfigurationUtil;
import studio.trc.bukkit.liteannouncer.util.ActionBarUtil;
import studio.trc.bukkit.liteannouncer.message.MessageUtil;
import studio.trc.bukkit.liteannouncer.util.PluginControl;
import studio.trc.bukkit.liteannouncer.util.TitleUtil;

public class Announcement
{
    @Getter
    private final String configPath;
    @Getter
    private final String name;
    @Getter
    private final String permission;
    @Getter
    private final double delay;
    @Getter
    private final List<String> messages;
    @Getter
    private final List<Title> titlesOfBroadcast = new ArrayList<>();
    @Getter
    private final List<ActionBar> actionBarsOfBroadcast = new ArrayList<>();
    
    public Announcement(String configPath, String name, double delay, List<String> messages, String permission) {
        this.configPath = configPath;
        this.name = name;
        this.delay = delay;
        this.messages = messages;
        this.permission = permission;
    }
    
    public void setTitlesOfBroadcast(List<Title> titles) {
        titlesOfBroadcast.clear();
        titlesOfBroadcast.addAll(titles);
    }
    
    public void setActionBarsOfBroadcast(List<ActionBar> actionbars) {
        actionBarsOfBroadcast.clear();
        actionBarsOfBroadcast.addAll(actionbars);
    }
    
    public void view(CommandSender viewer) {
        Map<String, String> placeholders = MessageUtil.getDefaultPlaceholders();
        messages.stream().forEach(message -> MessageUtil.sendMixedMessage(viewer, message, placeholders, PluginControl.getCacheJSONComponent(), placeholders));
        if (viewer instanceof Player) {
            if (!titlesOfBroadcast.isEmpty()) {
                Thread thread = new Thread(() -> {
                    titlesOfBroadcast.stream().map(title -> {
                        TitleUtil.sendTitle((Player) viewer, title);
                        return title;
                    }).forEach(title -> {
                        try {
                            Thread.sleep((long) (title.getDelay() * 1000));
                        } catch (InterruptedException ex) {
                            ex.printStackTrace();
                        }
                    });
                }, "LiteAnnouncer-TitleThread");
                thread.start();
            }
            if (!actionBarsOfBroadcast.isEmpty()) {
                Thread thread = new Thread(() -> {
                    actionBarsOfBroadcast.stream().map(actionbar -> {
                        ActionBarUtil.sendActionBar((Player) viewer, actionbar);
                        return actionbar;
                    }).forEach(actionbar -> {
                        try {
                            Thread.sleep((long) (actionbar.getDelay() * 1000));
                        } catch (InterruptedException ex) {
                            ex.printStackTrace();
                        }
                    });
                }, "LiteAnnouncer-ActionBarThread");
                thread.start();
            }
        }
    }
    
    public void broadcast() {
        Map<String, String> placeholders = MessageUtil.getDefaultPlaceholders();
        Bukkit.getOnlinePlayers().stream()
            .filter(player -> !ignoreAnnouncement(player) && (permission != null ? player.hasPermission(permission) : true))
            .forEach(player -> messages.stream().forEach(message -> MessageUtil.sendMixedMessage(player, message, placeholders, PluginControl.getCacheJSONComponent(), placeholders)));
        if (PluginControl.enabledConsoleBroadcast()) {
            messages.stream().forEach(message -> MessageUtil.sendMixedMessage(Bukkit.getConsoleSender(), message, placeholders, PluginControl.getCacheJSONComponent(), placeholders));
        }
        if (!titlesOfBroadcast.isEmpty() && !Bukkit.getOnlinePlayers().isEmpty()) {
            Thread thread = new Thread(() -> {
                titlesOfBroadcast.stream().map(title -> {
                    Bukkit.getOnlinePlayers().stream().filter(player -> !ignoreAnnouncement(player) && permission != null ? player.hasPermission(permission) : true).forEach(player -> {
                        TitleUtil.sendTitle(player, title);
                    });
                    return title;
                }).forEach(title -> {
                    try {
                        Thread.sleep((long) (title.getDelay() * 1000));
                    } catch (InterruptedException ex) {
                        ex.printStackTrace();
                    }
                });
            }, "LiteAnnouncer-TitleThread");
            thread.start();
        }
        if (!actionBarsOfBroadcast.isEmpty() && !Bukkit.getOnlinePlayers().isEmpty()) {
            Thread thread = new Thread(() -> {
                actionBarsOfBroadcast.stream().map(actionbar -> {
                    Bukkit.getOnlinePlayers().stream().filter(player -> !ignoreAnnouncement(player) && permission != null ? player.hasPermission(permission) : true).forEach((player) -> {
                        ActionBarUtil.sendActionBar(player, actionbar);
                    });
                    return actionbar;
                }).forEach((actionbar) -> {
                    try {
                        Thread.sleep((long) (actionbar.getDelay() * 1000));
                    } catch (InterruptedException ex) {
                        ex.printStackTrace();
                    }
                });
            }, "LiteAnnouncer-ActionBarThread");
            thread.start();
        }
    }
    
    public void broadcast(AnnouncerThread asyncThread) {
        try {
            if (!asyncThread.isRunning) return;
            Thread.sleep((long) (delay * 1000));
            if (!asyncThread.isRunning) return;
            broadcast();
        } catch (InterruptedException ex) {
            ex.printStackTrace();
        }
    }
    
    public boolean ignoreAnnouncement(Player player) {
        FileConfiguration data = ConfigurationUtil.getFileConfiguration(ConfigurationType.PLAYER_DATA);
        if (data.get("PlayerData." + player.getUniqueId()) == null || data.get("PlayerData." + player.getUniqueId() + ".Ignored-Announcements") == null) return false;
        if (data.getStringList("PlayerData." + player.getUniqueId() + ".Ignored-Announcements").stream().anyMatch(announcementName -> announcementName.equalsIgnoreCase("ALL"))) return true;
        return data.getStringList("PlayerData." + player.getUniqueId() + ".Ignored-Announcements").contains(name);
    }
}
