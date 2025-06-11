package studio.trc.bukkit.liteannouncer.util.tools;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import lombok.Getter;

import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;

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
    private final List<Title> titlesOfBroadcast = new LinkedList();
    @Getter
    private final List<ActionBar> actionBarsOfBroadcast = new LinkedList();
    
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
        Map<String, BaseComponent> baseComponents = new LinkedHashMap();
        if (viewer instanceof Player) {
            messages.stream().forEach(message -> {
                PluginControl.getJsonComponents().stream().forEach(jsonComponent -> {
                    BaseComponent bc = new TextComponent(MessageUtil.toPlaceholderAPIResult(jsonComponent.getComponent().toPlainText(), viewer));
                    bc.setClickEvent(jsonComponent.getClickEvent());
                    bc.setHoverEvent(jsonComponent.getHoverEvent());
                    BaseComponent[] hover = bc.getHoverEvent().getValue();
                    for (int i = 0;i < hover.length;i++) {
                        hover[i] = new TextComponent(MessageUtil.toPlaceholderAPIResult(hover[i].toPlainText(), viewer));
                    }
                    baseComponents.put(jsonComponent.getPlaceholder(), bc);
                });
                MessageUtil.sendJSONMessage(viewer, MessageUtil.createJsonMessage(viewer, message, baseComponents));
            });
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
        } else {
            messages.stream().forEach(message -> {
                PluginControl.getJsonComponents().stream().forEach(jsonComponent -> {
                    baseComponents.put(jsonComponent.getPlaceholder(), jsonComponent.getComponent());
                });
                MessageUtil.sendJSONMessage(viewer, MessageUtil.createJsonMessage(viewer, message, baseComponents));
            });
        }
    }
    
    public void broadcast() {
        Map<String, BaseComponent> baseComponents = new HashMap();
        messages.stream().forEach(message -> {
            Bukkit.getOnlinePlayers().stream().filter(player -> !ignoreAnnouncement(player) && (permission != null ? player.hasPermission(permission) : true)).forEach(player -> {
                baseComponents.clear();
                PluginControl.getJsonComponents().stream().forEach(jsonComponent -> {
                    BaseComponent bc = new TextComponent(MessageUtil.toPlaceholderAPIResult(jsonComponent.getComponent().toPlainText(), player));
                    bc.setClickEvent(jsonComponent.getClickEvent());
                    bc.setHoverEvent(jsonComponent.getHoverEvent());
                    BaseComponent[] hover = bc.getHoverEvent().getValue();
                    for (int i = 0;i < hover.length;i++) {
                        hover[i] = new TextComponent(MessageUtil.toPlaceholderAPIResult(hover[i].toPlainText(), player));
                    }
                    baseComponents.put(jsonComponent.getPlaceholder(), bc);
                });
                MessageUtil.sendJSONMessage(player, MessageUtil.createJsonMessage(player, message, baseComponents));
            });
        });
        CommandSender console = Bukkit.getConsoleSender();
        messages.stream().forEach(message -> {
            if (PluginControl.enabledConsoleBroadcast()) {
                baseComponents.clear();
                PluginControl.getJsonComponents().stream().forEach(jsonComponent -> {
                    baseComponents.put(jsonComponent.getPlaceholder(), jsonComponent.getComponent());
                });
                MessageUtil.sendJSONMessage(console, MessageUtil.createJsonMessage(console, message, baseComponents));
            }
        });
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
