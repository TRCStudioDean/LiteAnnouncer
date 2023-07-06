package studio.trc.bungee.liteannouncer.command;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;
import net.md_5.bungee.api.plugin.TabExecutor;
import net.md_5.bungee.config.Configuration;
import studio.trc.bungee.liteannouncer.configuration.ConfigurationType;
import studio.trc.bungee.liteannouncer.configuration.ConfigurationUtil;

import studio.trc.bungee.liteannouncer.util.MessageUtil;
import studio.trc.bungee.liteannouncer.util.PluginControl;
import studio.trc.bungee.liteannouncer.util.tools.Announcement;

public class LiteAnnouncerCommand
    extends Command
    implements TabExecutor
{

    public LiteAnnouncerCommand(String name) {
        super(name);
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (args.length == 0) {
            MessageUtil.sendMessage(sender, "Command-Messages.Unknown-Command");
        } else {
            if (args[0].equalsIgnoreCase("help")) {
                if (!PluginControl.hasPermission(sender, "Permissions.Commands.Help")) {
                    MessageUtil.sendMessage(sender, "No-Permission");
                    return;
                }
                MessageUtil.sendMessage(sender, "Command-Messages.Help-Command");
            } else if (args[0].equalsIgnoreCase("reload")) {
                if (!PluginControl.hasPermission(sender, "Permissions.Commands.Reload")) {
                    MessageUtil.sendMessage(sender, "No-Permission");
                    return;
                }
                PluginControl.reload();
                MessageUtil.sendMessage(sender, "Command-Messages.Reload");
            } else if (args[0].equalsIgnoreCase("view")) {
                if (!PluginControl.hasPermission(sender, "Permissions.Commands.View")) {
                    MessageUtil.sendMessage(sender, "No-Permission");
                    return;
                }
                if (args.length == 1) {
                    MessageUtil.sendMessage(sender, "Command-Messages.View.Help");
                } else {
                    for (Announcement announcement: PluginControl.getAnnouncements()) {
                        if (args[1].equalsIgnoreCase(announcement.getName())) {
                            announcement.view(sender);
                            return;
                        }
                    }
                    Map<String, String> placeholders = new HashMap();
                    placeholders.put("{announcement}", args[1]);
                    MessageUtil.sendMessage(sender, "Command-Messages.View.Not-Found", placeholders);
                }
            } else if (args[0].equalsIgnoreCase("broadcast")) {
                if (!PluginControl.hasPermission(sender, "Permissions.Commands.Broadcast")) {
                    MessageUtil.sendMessage(sender, "No-Permission");
                    return;
                }
                if (args.length == 1) {
                    MessageUtil.sendMessage(sender, "Command-Messages.Broadcast.Help");
                } else {
                    for (Announcement announcement: PluginControl.getAnnouncements()) {
                        if (args[1].equalsIgnoreCase(announcement.getName())) {
                            announcement.broadcast();
                            return;
                        }
                    }
                    Map<String, String> placeholders = new HashMap();
                    placeholders.put("{announcement}", args[1]);
                    MessageUtil.sendMessage(sender, "Command-Messages.Broadcast.Not-Found", placeholders);
                }
            } else if (args[0].equalsIgnoreCase("list")) {
                if (!PluginControl.hasPermission(sender, "Permissions.Commands.List")) {
                    MessageUtil.sendMessage(sender, "No-Permission");
                    return;
                }
                Map<String, String> placeholders = new HashMap();
                List<String> name = new ArrayList();
                PluginControl.getAnnouncements().stream().forEach((announcement) -> {
                    name.add(announcement.getName());
                });
                placeholders.put("{list}", name.toString().substring(1, name.toString().length() - 1));
                MessageUtil.sendMessage(sender, "Command-Messages.List", placeholders);
            } else if (args[0].equalsIgnoreCase("switch")) {
                if (!PluginControl.hasPermission(sender, "Permissions.Commands.Switch")) {
                    MessageUtil.sendMessage(sender, "No-Permission");
                    return;
                }
                if (args.length < 2) {
                    MessageUtil.sendMessage(sender, "Command-Messages.Switch.Help");
                    return;
                }
                ProxiedPlayer player = ProxyServer.getInstance().getPlayer(args[1]);
                Map<String, String> placeholders = new HashMap();
                if (player == null) {
                    placeholders.put("{player}", args[1]);
                    MessageUtil.sendMessage(sender, "Player-Not-Exist", placeholders);
                    return;
                }
                placeholders.put("{player}", player.getName());
                Configuration data = ConfigurationUtil.getFileConfiguration(ConfigurationType.PLAYER_DATA);
                List<String> list = data.get("PlayerData." + player.getUniqueId() + ".Ignored-Announcements") != null ? data.getStringList("PlayerData." + player.getUniqueId() + ".Ignored-Announcements") : new ArrayList();
                data.set("PlayerData." + player.getUniqueId() + ".Name", player.getName());
                if (list.contains("ALL")) {
                    list.remove("ALL");
                    data.set("PlayerData." + player.getUniqueId() + ".Ignored-Announcements", list);
                    ConfigurationUtil.getConfig(ConfigurationType.PLAYER_DATA).saveConfig();
                    MessageUtil.sendMessage(sender, "Command-Messages.Switch.Switch-On", placeholders);
                } else {
                    list.add("ALL");
                    data.set("PlayerData." + player.getUniqueId() + ".Ignored-Announcements", list);
                    ConfigurationUtil.getConfig(ConfigurationType.PLAYER_DATA).saveConfig();
                    MessageUtil.sendMessage(sender, "Command-Messages.Switch.Switch-Off", placeholders);
                }
            } else if (args[0].equalsIgnoreCase("ignore")) {
                if (!PluginControl.hasPermission(sender, "Permissions.Commands.Ignore")) {
                    MessageUtil.sendMessage(sender, "No-Permission");
                    return;
                }
                if (args.length < 3) {
                    MessageUtil.sendMessage(sender, "Command-Messages.Ignore.Help");
                    return;
                }
                ProxiedPlayer player = ProxyServer.getInstance().getPlayer(args[2]);
                Map<String, String> placeholders = new HashMap();
                if (player == null) {
                    placeholders.put("{player}", args[2]);
                    MessageUtil.sendMessage(sender, "Player-Not-Exist", placeholders);
                    return;
                }
                placeholders.put("{player}", player.getName());
                Announcement announcement = PluginControl.getAnnouncementsByPriority().stream().filter(announcement_ -> announcement_.getName().equalsIgnoreCase(args[1])).findFirst().orElse(null);
                if (announcement == null) {
                    placeholders.put("{announcement}", args[1]);
                    MessageUtil.sendMessage(sender, "Command-Messages.Ignore.Not-Found", placeholders);
                    return;
                }
                placeholders.put("{announcement}", announcement.getName());
                Configuration data = ConfigurationUtil.getFileConfiguration(ConfigurationType.PLAYER_DATA);
                List<String> list = data.get("PlayerData." + player.getUniqueId() + ".Ignored-Announcements") != null ? data.getStringList("PlayerData." + player.getUniqueId() + ".Ignored-Announcements") : new ArrayList();
                data.set("PlayerData." + player.getUniqueId() + ".Name", player.getName());
                if (list.contains(announcement.getName())) {
                    list.remove(announcement.getName());
                    data.set("PlayerData." + player.getUniqueId() + ".Ignored-Announcements", list);
                    ConfigurationUtil.getConfig(ConfigurationType.PLAYER_DATA).saveConfig();
                    MessageUtil.sendMessage(sender, "Command-Messages.Ignore.Ignore-On", placeholders);
                } else {
                    list.add(announcement.getName());
                    data.set("PlayerData." + player.getUniqueId() + ".Ignored-Announcements", list);
                    ConfigurationUtil.getConfig(ConfigurationType.PLAYER_DATA).saveConfig();
                    MessageUtil.sendMessage(sender, "Command-Messages.Ignore.Ignore-Off", placeholders);
                }
            } else {
                MessageUtil.sendMessage(sender, "Command-Messages.Unknown-Command");
            }
        }
    }

    @Override
    public Iterable<String> onTabComplete(CommandSender sender, String[] args) {
        if (args.length >= 1) {
            if (args[0].equalsIgnoreCase("view") && args.length == 2 && PluginControl.hasPermission(sender, "Permissions.Commands.View")) {
                return getAnnouncements(args[1]);
            }
            if (args[0].equalsIgnoreCase("broadcast") && args.length == 2 && PluginControl.hasPermission(sender, "Permissions.Commands.Broadcast")) {
                return getAnnouncements(args[1]);
            }
            if (args[0].equalsIgnoreCase("switch") && args.length == 2 && PluginControl.hasPermission(sender, "Permissions.Commands.Switch")) {
                return getTabPlayersName(args, 2);
            }
            if (args[0].equalsIgnoreCase("ignore") && args.length == 2 && PluginControl.hasPermission(sender, "Permissions.Commands.Ignore")) {
                return getAnnouncements(args[1]);
            }
            if (args[0].equalsIgnoreCase("ignore") && args.length == 3 && PluginControl.hasPermission(sender, "Permissions.Commands.Ignore")) {
                return getTabPlayersName(args, 3);
            }
            return getCommands(args[0]);
        } else {
            return getCommands(null);
        }
    }
    
    private List<String> getAnnouncements(String args) {
        if (args != null) {
            List<String> names = new ArrayList();
            PluginControl.getAnnouncements().stream().filter((announcement) -> (announcement.getName().toLowerCase().startsWith(args.toLowerCase()))).forEach((announcement) -> {
                names.add(announcement.getName());
            });
            return names;
        } 
        return new ArrayList();
    }
    
    private List<String> getCommands(String args) {
        List<String> commands = Arrays.asList("help", "reload", "broadcast",  "view", "list", "switch", "ignore");
        if (args != null) {
            List<String> names = new ArrayList();
            commands.stream().filter(command -> (command.startsWith(args.toLowerCase()))).forEach(command -> {
                names.add(command);
            });
            return names;
        }
        return commands;
    }
    
    private List<String> getTabPlayersName(String[] args, int length) {
        if (args.length == length) {
            List<String> onlines = ProxyServer.getInstance().getPlayers().stream().map(player -> player.getName()).collect(Collectors.toList());
            List<String> names = new ArrayList();
            onlines.stream().filter(command -> command.toLowerCase().startsWith(args[length - 1].toLowerCase())).forEach(command -> {
                names.add(command);
            });
            return names;
        }
        return new ArrayList();
    }
}
