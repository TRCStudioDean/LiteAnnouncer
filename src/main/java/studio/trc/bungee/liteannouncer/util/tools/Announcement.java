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
    private final List<String> whitelist;
    @Getter
    private final List<Title> titlesOfBroadcast = new LinkedList();
    @Getter
    private final List<ActionBar> actionBarsOfBroadcast = new LinkedList();
    
    public Announcement(String configPath, String name, double delay, List<String> messages, String permission, List<String> whitelist) {
        this.configPath = configPath;
        this.name = name;
        this.delay = delay;
        this.messages = messages;
        this.permission = permission;
        this.whitelist = whitelist;
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
        Map<String, BaseComponent> baseComponents = new HashMap();
        if (viewer instanceof ProxiedPlayer) {
            ProxiedPlayer player = (ProxiedPlayer) viewer;
            messages.stream().forEach(message -> {
                PluginControl.getJsonComponents().stream().forEach(jsonComponent -> {
                    BaseComponent bc = new TextComponent(MessageUtil.toLocallyPlaceholders(jsonComponent.getComponent().toPlainText(), player));
                    bc.setClickEvent(jsonComponent.getClickEvent());
                    bc.setHoverEvent(jsonComponent.getHoverEvent());
                    BaseComponent[] hover = bc.getHoverEvent().getValue();
                    for (int i = 0;i < hover.length;i++) {
                        hover[i] = new TextComponent(MessageUtil.toLocallyPlaceholders(hover[i].toPlainText(), player));
                    }
                    baseComponents.put(jsonComponent.getPlaceholder(), bc);
                });
                MessageUtil.sendJsonMessage(viewer, MessageUtil.toLocallyPlaceholders(message, player), baseComponents);
            });
            if (!titlesOfBroadcast.isEmpty()) {
                Thread thread = new Thread(() -> {
                    titlesOfBroadcast.stream().map(title -> {
                        ProxyServer.getInstance().createTitle()
                                .fadeIn((int) (title.getFadein() * 20))
                                .stay((int) (title.getStay() * 20))
                                .fadeOut((int) (title.getFadeout() * 20))
                                .title(new TextComponent(MessageUtil.toLocallyPlaceholders(title.getTitle(), player)))
                                .subTitle(new TextComponent(MessageUtil.toLocallyPlaceholders(title.getSubTitle(), player)))
                                .send(player);
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
                        player.sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(MessageUtil.toLocallyPlaceholders(actionbar.getText(), player)));
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
        } else {
            messages.stream().forEach(message -> {
                PluginControl.getJsonComponents().stream().forEach(jsonComponent -> {
                    baseComponents.put(jsonComponent.getPlaceholder(), jsonComponent.getComponent());
                });
                MessageUtil.sendJsonMessage(viewer, message, baseComponents);
            });
        }
    }
    
    public void broadcast() {
        ProxyServer proxy = ProxyServer.getInstance();
        Map<String, BaseComponent> baseComponents = new HashMap();
        messages.stream().forEach(message -> {
            proxy.getPlayers().stream().filter(player -> whitelist(player) && (permission != null ? player.hasPermission(permission) : true)).forEach(player -> {
                baseComponents.clear();
                PluginControl.getJsonComponents().stream().forEach(jsonComponent -> {
                    BaseComponent bc = new TextComponent(MessageUtil.toLocallyPlaceholders(jsonComponent.getComponent().toPlainText(), player));
                    bc.setClickEvent(jsonComponent.getClickEvent());
                    bc.setHoverEvent(jsonComponent.getHoverEvent());
                    BaseComponent[] hover = bc.getHoverEvent().getValue();
                    for (int i = 0;i < hover.length;i++) {
                        hover[i] = new TextComponent(MessageUtil.toLocallyPlaceholders(hover[i].toPlainText(), player));
                    }
                    baseComponents.put(jsonComponent.getPlaceholder(), bc);
                });
                MessageUtil.sendJsonMessage(player, MessageUtil.toLocallyPlaceholders(message, player), baseComponents);
            });
        });
        messages.stream().forEach(message -> {
            if (PluginControl.enabledConsoleBroadcast()) {
                baseComponents.clear();
                PluginControl.getJsonComponents().stream().forEach(jsonComponent -> {
                    baseComponents.put(jsonComponent.getPlaceholder(), jsonComponent.getComponent());
                });
                MessageUtil.sendJsonMessage(proxy.getConsole(), message, baseComponents);
            }
        });
        if (!titlesOfBroadcast.isEmpty() && !proxy.getPlayers().isEmpty()) {
            Thread thread = new Thread(() -> {
                titlesOfBroadcast.stream().map(title -> {
                    proxy.getPlayers().stream().filter(player -> !(!whitelist(player) || !(permission != null ? player.hasPermission(permission) : true))).forEach(player -> {
                        proxy.createTitle()
                                .fadeIn((int) (title.getFadein() * 20))
                                .stay((int) (title.getStay() * 20))
                                .fadeOut((int) (title.getFadeout() * 20))
                                .title(new TextComponent(MessageUtil.toLocallyPlaceholders(title.getTitle(), player)))
                                .subTitle(new TextComponent(MessageUtil.toLocallyPlaceholders(title.getSubTitle(), player)))
                                .send(player);
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
        if (!actionBarsOfBroadcast.isEmpty() && !proxy.getPlayers().isEmpty()) {
            Thread thread = new Thread(() -> {
                actionBarsOfBroadcast.stream().map(actionbar -> {
                    proxy.getPlayers().stream().filter(player -> !(!whitelist(player) || !(permission != null ? player.hasPermission(permission) : true))).forEach(player -> {
                        player.sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(MessageUtil.toLocallyPlaceholders(actionbar.getText(), player)));
                    });
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
