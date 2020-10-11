package studio.trc.bungee.liteannouncer.util.tools;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;

import studio.trc.bungee.liteannouncer.async.AnnouncerThread;
import studio.trc.bungee.liteannouncer.util.MessageUtil;
import studio.trc.bungee.liteannouncer.util.PluginControl;

public class Announcement
{
    private final String name;
    private final String permission;
    private final double delay;
    private final List<String> messages;
    private final List<String> whitelist;
    
    public Announcement(String name, double delay, List<String> messages, String permission, List<String> whitelist) {
        this.name = name;
        this.delay = delay;
        this.messages = messages;
        this.permission = permission;
        this.whitelist = whitelist;
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
    
    public List<String> getWhitelist() {
        return whitelist;
    }
    
    public void view(CommandSender viewer) {
        Map<String, BaseComponent> baseComponents = new HashMap();
        for (JsonComponent jsonComponent : PluginControl.getJsonComponents()) {
            baseComponents.put(jsonComponent.getPlaceholder(), jsonComponent.getComponent());
        }
        for (String message : messages) {
            MessageUtil.sendJsonMessage(viewer, message, baseComponents);
        }
    }
    
    public void broadcast(AnnouncerThread asyncThread) {
        ProxyServer proxy = ProxyServer.getInstance();
        try {
            if (!asyncThread.isRunning) return;
            Thread.sleep((long) (delay * 1000));
            if (!asyncThread.isRunning) return;
            Map<String, BaseComponent> baseComponents = new HashMap();
            for (JsonComponent jsonComponent : PluginControl.getJsonComponents()) {
                baseComponents.put(jsonComponent.getPlaceholder(), jsonComponent.getComponent());
            }
            for (String message : messages) {
                for (ProxiedPlayer player : proxy.getPlayers()) {
                    if (permission != null ? player.hasPermission(permission) : true) {
                        if (!whitelist.isEmpty()) {
                            for (String whitelistServer : whitelist) {
                                if (player.getServer().getInfo().getName().equalsIgnoreCase(whitelistServer)) {
                                    MessageUtil.sendJsonMessage(player, message, baseComponents);
                                }
                            }
                        } else {
                            MessageUtil.sendJsonMessage(player, message, baseComponents);
                        }
                    }
                }
                MessageUtil.sendJsonMessage(proxy.getConsole(), message, baseComponents);
            }
        } catch (InterruptedException ex) {
            Logger.getLogger(Announcement.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
