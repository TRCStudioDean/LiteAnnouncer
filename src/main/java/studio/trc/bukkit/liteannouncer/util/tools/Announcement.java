package studio.trc.bukkit.liteannouncer.util.tools;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import lombok.Getter;

import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import studio.trc.bukkit.liteannouncer.async.AnnouncerThread;
import studio.trc.bukkit.liteannouncer.util.ActionBarUtil;
import studio.trc.bukkit.liteannouncer.util.MessageUtil;
import studio.trc.bukkit.liteannouncer.util.PluginControl;
import studio.trc.bukkit.liteannouncer.util.TitleUtil;

public class Announcement
{
    @Getter
    private final String name;
    @Getter
    private final String permission;
    @Getter
    private final double delay;
    @Getter
    private final List<String> messages;
    @Getter
    private final List<TitleOfBroadcast> titlesOfBroadcast = new LinkedList();
    @Getter
    private final List<ActionBarOfBroadcast> actionBarsOfBroadcast = new LinkedList();
    
    public Announcement(String name, double delay, List<String> messages, String permission) {
        this.name = name;
        this.delay = delay;
        this.messages = messages;
        this.permission = permission;
    }
    
    public void setTitlesOfBroadcast(List<TitleOfBroadcast> titles) {
        titlesOfBroadcast.clear();
        titlesOfBroadcast.addAll(titles);
    }
    
    public void setActionBarsOfBroadcast(List<ActionBarOfBroadcast> actionbars) {
        actionBarsOfBroadcast.clear();
        actionBarsOfBroadcast.addAll(actionbars);
    }
    
    public void view(CommandSender viewer) {
        Map<String, BaseComponent> baseComponents = new HashMap();
        if (viewer instanceof Player) {
            for (String message : messages) {
                for (JsonComponent jsonComponent : PluginControl.getJsonComponents()) {
                    BaseComponent bc = new TextComponent(MessageUtil.toPlaceholderAPIResult(jsonComponent.getComponent().toPlainText(), viewer));
                    bc.setClickEvent(jsonComponent.getClickEvent());
                    bc.setHoverEvent(jsonComponent.getHoverEvent());
                    BaseComponent[] hover = bc.getHoverEvent().getValue();
                    for (int i = 0;i < hover.length;i++) {
                        hover[i] = new TextComponent(MessageUtil.toPlaceholderAPIResult(hover[i].toPlainText(), viewer));
                    }
                    baseComponents.put(jsonComponent.getPlaceholder(), bc);
                }
                MessageUtil.sendJsonMessage(viewer, message, baseComponents);
            }
            if (!titlesOfBroadcast.isEmpty()) {
                Thread thread = new Thread(() -> {
                    for (TitleOfBroadcast title : titlesOfBroadcast) {
                        TitleUtil.sendTitle((Player) viewer, title);
                        try {
                            Thread.sleep((long) (title.getDelay() * 1000));
                        } catch (InterruptedException ex) {
                            ex.printStackTrace();
                        }
                    }
                }, "LiteAnnouncer-TitleThread");
                thread.start();
            }
            if (!actionBarsOfBroadcast.isEmpty()) {
                Thread thread = new Thread(() -> {
                    for (ActionBarOfBroadcast actionbar : actionBarsOfBroadcast) {
                        ActionBarUtil.sendActionBar((Player) viewer, actionbar);
                        try {
                            Thread.sleep((long) (actionbar.getDelay() * 1000));
                        } catch (InterruptedException ex) {
                            ex.printStackTrace();
                        }
                    }
                }, "LiteAnnouncer-ActionBarThread");
                thread.start();
            }
        } else {
            for (String message : messages) {
                for (JsonComponent jsonComponent : PluginControl.getJsonComponents()) {
                    baseComponents.put(jsonComponent.getPlaceholder(), jsonComponent.getComponent());
                }
                MessageUtil.sendJsonMessage(viewer, message, baseComponents);
            }
        }
    }
    
    public void broadcast() {
        for (String message : messages) {
            Map<String, BaseComponent> baseComponents = new HashMap();
            for (Player player : Bukkit.getOnlinePlayers()) {
                if (!(permission != null ? player.hasPermission(permission) : true)) continue;
                baseComponents.clear();
                for (JsonComponent jsonComponent : PluginControl.getJsonComponents()) {
                    BaseComponent bc = new TextComponent(MessageUtil.toPlaceholderAPIResult(jsonComponent.getComponent().toPlainText(), player));
                    bc.setClickEvent(jsonComponent.getClickEvent());
                    bc.setHoverEvent(jsonComponent.getHoverEvent());
                    BaseComponent[] hover = bc.getHoverEvent().getValue();
                    for (int i = 0;i < hover.length;i++) {
                        hover[i] = new TextComponent(MessageUtil.toPlaceholderAPIResult(hover[i].toPlainText(), player));
                    }
                    baseComponents.put(jsonComponent.getPlaceholder(), bc);
                }
                MessageUtil.sendJsonMessage(player, message, baseComponents);
            }
            if (!titlesOfBroadcast.isEmpty() && !Bukkit.getOnlinePlayers().isEmpty()) {
                Thread thread = new Thread(() -> {
                    for (TitleOfBroadcast title : titlesOfBroadcast) {
                        for (Player player : Bukkit.getOnlinePlayers()) {
                            if (!(permission != null ? player.hasPermission(permission) : true)) continue;
                            TitleUtil.sendTitle(player, title);
                        }
                        try {
                            Thread.sleep((long) (title.getDelay() * 1000));
                        } catch (InterruptedException ex) {
                            ex.printStackTrace();
                        }
                    }
                }, "LiteAnnouncer-TitleThread");
                thread.start();
            }
            if (!actionBarsOfBroadcast.isEmpty() && !Bukkit.getOnlinePlayers().isEmpty()) {
                Thread thread = new Thread(() -> {
                    for (ActionBarOfBroadcast actionbar : actionBarsOfBroadcast) {
                        for (Player player : Bukkit.getOnlinePlayers()) {
                            if (!(permission != null ? player.hasPermission(permission) : true)) continue;
                            ActionBarUtil.sendActionBar(player, actionbar);
                        }
                        try {
                            Thread.sleep((long) (actionbar.getDelay() * 1000));
                        } catch (InterruptedException ex) {
                            ex.printStackTrace();
                        }
                    }
                }, "LiteAnnouncer-ActionBarThread");
                thread.start();
            }
            if (PluginControl.enabledConsoleBroadcast()) {
                baseComponents.clear();
                CommandSender console = Bukkit.getConsoleSender();
                for (JsonComponent jsonComponent : PluginControl.getJsonComponents()) {
                    baseComponents.put(jsonComponent.getPlaceholder(), jsonComponent.getComponent());
                }
                MessageUtil.sendJsonMessage(console, message, baseComponents);
            }
        }
    }
    
    public void broadcast(AnnouncerThread asyncThread) {
        try {
            if (!asyncThread.isRunning) return;
            Thread.sleep((long) (delay * 1000));
            if (!asyncThread.isRunning) return;
            broadcast();
        } catch (InterruptedException ex) {
            Logger.getLogger(Announcement.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
