package studio.trc.bukkit.liteannouncer.util.tools;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import studio.trc.bukkit.liteannouncer.async.AnnouncerThread;
import studio.trc.bukkit.liteannouncer.util.MessageUtil;
import studio.trc.bukkit.liteannouncer.util.PluginControl;

public class Announcement
{
    private final String name;
    private final String permission;
    private final double delay;
    private final List<String> messages;
    
    public Announcement(String name, double delay, List<String> messages, String permission) {
        this.name = name;
        this.delay = delay;
        this.messages = messages;
        this.permission = permission;
    }
    
    public String getName() {
        return name;
    }
    
    public String getPermission() {
        return permission;
    }
    
    public double getDelay() {
        return delay;
    }
    
    public List<String> getMessages() {
        return messages;
    }
    
    public void view(CommandSender viewer) {
        Map<String, BaseComponent> baseComponents = new HashMap();
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
            if (permission != null ? viewer.hasPermission(permission) : true) {
                MessageUtil.sendJsonMessage(viewer, message, baseComponents);
            }
        }
    }
    
    public void broadcast(AnnouncerThread asyncThread) {
        try {
            if (!asyncThread.isRunning) return;
            Thread.sleep((long) (delay * 1000));
            if (!asyncThread.isRunning) return;
            for (String message : messages) {
                Map<String, BaseComponent> baseComponents = new HashMap();
                for (Player player : Bukkit.getOnlinePlayers()) {
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
                    if (permission != null ? player.hasPermission(permission) : true) {
                        MessageUtil.sendJsonMessage(player, message, baseComponents);
                    }
                }
                baseComponents.clear();
                CommandSender console = Bukkit.getConsoleSender();
                for (JsonComponent jsonComponent : PluginControl.getJsonComponents()) {
                    BaseComponent bc = new TextComponent(MessageUtil.toPlaceholderAPIResult(jsonComponent.getComponent().toPlainText(), console));
                    bc.setClickEvent(jsonComponent.getClickEvent());
                    bc.setHoverEvent(jsonComponent.getHoverEvent());
                    BaseComponent[] hover = bc.getHoverEvent().getValue();
                    for (int i = 0;i < hover.length;i++) {
                        hover[i] = new TextComponent(MessageUtil.toPlaceholderAPIResult(hover[i].toPlainText(), console));
                    }
                    baseComponents.put(jsonComponent.getPlaceholder(), bc);
                }
                MessageUtil.sendJsonMessage(console, message, baseComponents);
            }
        } catch (InterruptedException ex) {
            Logger.getLogger(Announcement.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
