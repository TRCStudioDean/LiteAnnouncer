package studio.trc.bungee.liteannouncer.util.tools;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import lombok.Getter;

import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;

import studio.trc.bungee.liteannouncer.async.AnnouncerThread;
import studio.trc.bungee.liteannouncer.util.MessageUtil;
import studio.trc.bungee.liteannouncer.util.PluginControl;

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
    private final List<String> whitelist;
    @Getter
    private final List<TitleOfBroadcast> titlesOfBroadcast = new LinkedList();
    @Getter
    private final List<ActionBarOfBroadcast> actionBarsOfBroadcast = new LinkedList();
    
    public Announcement(String name, double delay, List<String> messages, String permission, List<String> whitelist) {
        this.name = name;
        this.delay = delay;
        this.messages = messages;
        this.permission = permission;
        this.whitelist = whitelist;
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
        if (viewer instanceof ProxiedPlayer) {
            ProxiedPlayer player = (ProxiedPlayer) viewer;
            for (String message : messages) {
                for (JsonComponent jsonComponent : PluginControl.getJsonComponents()) {
                    BaseComponent bc = new TextComponent(MessageUtil.toLocallyPlaceholders(jsonComponent.getComponent().toPlainText(), player));
                    bc.setClickEvent(jsonComponent.getClickEvent());
                    bc.setHoverEvent(jsonComponent.getHoverEvent());
                    BaseComponent[] hover = bc.getHoverEvent().getValue();
                    for (int i = 0;i < hover.length;i++) {
                        hover[i] = new TextComponent(MessageUtil.toLocallyPlaceholders(hover[i].toPlainText(), player));
                    }
                    baseComponents.put(jsonComponent.getPlaceholder(), bc);
                }
                MessageUtil.sendJsonMessage(viewer, MessageUtil.toLocallyPlaceholders(message, player), baseComponents);
            }
            if (!titlesOfBroadcast.isEmpty()) {
                Thread thread = new Thread(() -> {
                    for (TitleOfBroadcast title : titlesOfBroadcast) {
                        ProxyServer.getInstance().createTitle()
                                .fadeIn((int) (title.getFadein() * 20))
                                .stay((int) (title.getStay() * 20))
                                .fadeOut((int) (title.getFadeout() * 20))
                                .title(new TextComponent(MessageUtil.toLocallyPlaceholders(title.getTitle(), player)))
                                .subTitle(new TextComponent(MessageUtil.toLocallyPlaceholders(title.getSubTitle(), player)))
                                .send(player);
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
                        player.sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(MessageUtil.toLocallyPlaceholders(actionbar.getText(), player)));
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
        ProxyServer proxy = ProxyServer.getInstance();
        Map<String, BaseComponent> baseComponents = new HashMap();
        for (String message : messages) {
            for (ProxiedPlayer player : proxy.getPlayers()) {
                if (!whitelist(player) || !(permission != null ? player.hasPermission(permission) : true)) continue;
                baseComponents.clear();
                for (JsonComponent jsonComponent : PluginControl.getJsonComponents()) {
                    BaseComponent bc = new TextComponent(MessageUtil.toLocallyPlaceholders(jsonComponent.getComponent().toPlainText(), player));
                    bc.setClickEvent(jsonComponent.getClickEvent());
                    bc.setHoverEvent(jsonComponent.getHoverEvent());
                    BaseComponent[] hover = bc.getHoverEvent().getValue();
                    for (int i = 0;i < hover.length;i++) {
                        hover[i] = new TextComponent(MessageUtil.toLocallyPlaceholders(hover[i].toPlainText(), player));
                    }
                    baseComponents.put(jsonComponent.getPlaceholder(), bc);
                }
                if (permission != null ? player.hasPermission(permission) : true) {
                    MessageUtil.sendJsonMessage(player, MessageUtil.toLocallyPlaceholders(message, player), baseComponents);
                }
            }
            if (!titlesOfBroadcast.isEmpty() && !proxy.getPlayers().isEmpty()) {
                Thread thread = new Thread(() -> {
                    for (TitleOfBroadcast title : titlesOfBroadcast) {
                        for (ProxiedPlayer player : proxy.getPlayers()) {
                            if (!whitelist(player) || !(permission != null ? player.hasPermission(permission) : true)) continue;
                            proxy.createTitle()
                                    .fadeIn((int) (title.getFadein() * 20))
                                    .stay((int) (title.getStay() * 20))
                                    .fadeOut((int) (title.getFadeout() * 20))
                                    .title(new TextComponent(MessageUtil.toLocallyPlaceholders(title.getTitle(), player)))
                                    .subTitle(new TextComponent(MessageUtil.toLocallyPlaceholders(title.getSubTitle(), player)))
                                    .send(player);
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
            if (!actionBarsOfBroadcast.isEmpty() && !proxy.getPlayers().isEmpty()) {
                Thread thread = new Thread(() -> {
                    for (ActionBarOfBroadcast actionbar : actionBarsOfBroadcast) {
                        for (ProxiedPlayer player : proxy.getPlayers()) {
                            if (!whitelist(player) || !(permission != null ? player.hasPermission(permission) : true)) continue;
                            player.sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(MessageUtil.toLocallyPlaceholders(actionbar.getText(), player)));
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
                for (JsonComponent jsonComponent : PluginControl.getJsonComponents()) {
                    baseComponents.put(jsonComponent.getPlaceholder(), jsonComponent.getComponent());
                }
                MessageUtil.sendJsonMessage(proxy.getConsole(), message, baseComponents);
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
    
    public boolean whitelist(ProxiedPlayer player) {
        String serverName = player.getServer().getInfo().getName();
        boolean inWhitelist = false;
        if (!getWhitelist().isEmpty()) {
            for (String whitelistServer : getWhitelist()) {
                if (serverName.toLowerCase().equalsIgnoreCase(whitelistServer)) {
                    inWhitelist = true;
                }
            }
        } else {
            inWhitelist = true;
        }
        return inWhitelist;
    }
}
