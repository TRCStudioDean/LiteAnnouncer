package studio.trc.bukkit.liteannouncer.command;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import studio.trc.bukkit.liteannouncer.util.MessageUtil;
import studio.trc.bukkit.liteannouncer.util.PluginControl;
import studio.trc.bukkit.liteannouncer.util.tools.Announcement;

public class LiteAnnouncerCommand
    implements CommandExecutor, TabCompleter
{
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (command.getName().equalsIgnoreCase("la") || command.getName().equalsIgnoreCase("liteannouncer")) {
            if (args.length == 0) {
                MessageUtil.sendMessage(sender, "Command-Messages.Unknown-Command");
            } else {
                if (args[0].equalsIgnoreCase("help")) {
                    if (!PluginControl.hasPermission(sender, "Permissions.Commands.Help")) {
                        MessageUtil.sendMessage(sender, "No-Permission");
                        return true;
                    }
                    MessageUtil.sendMessage(sender, "Command-Messages.Help-Command");
                } else if (args[0].equalsIgnoreCase("reload")) {
                    if (!PluginControl.hasPermission(sender, "Permissions.Commands.Reload")) {
                        MessageUtil.sendMessage(sender, "No-Permission");
                        return true;
                    }
                    PluginControl.reload();
                    MessageUtil.sendMessage(sender, "Command-Messages.Reload");
                } else if (args[0].equalsIgnoreCase("view")) {
                    if (!PluginControl.hasPermission(sender, "Permissions.Commands.View")) {
                        MessageUtil.sendMessage(sender, "No-Permission");
                        return true;
                    }
                    if (args.length == 1) {
                        MessageUtil.sendMessage(sender, "Command-Messages.View.Help");
                        return true;
                    } else {
                        for (Announcement announcement: PluginControl.getAnnouncements()) {
                            if (args[1].equalsIgnoreCase(announcement.getName())) {
                                announcement.view(sender);
                                return true;
                            }
                        }
                        Map<String, String> placeholders = new HashMap();
                        placeholders.put("{announcement}", args[1]);
                        MessageUtil.sendMessage(sender, "Command-Messages.View.Not-Found", placeholders);
                        return true;
                    }
                } else if (args[0].equalsIgnoreCase("broadcast")) {
                    if (!PluginControl.hasPermission(sender, "Permissions.Commands.Broadcast")) {
                        MessageUtil.sendMessage(sender, "No-Permission");
                        return true;
                    }
                    if (args.length == 1) {
                        MessageUtil.sendMessage(sender, "Command-Messages.Broadcast.Help");
                        return true;
                    } else {
                        for (Announcement announcement: PluginControl.getAnnouncements()) {
                            if (args[1].equalsIgnoreCase(announcement.getName())) {
                                announcement.broadcast();
                                return true;
                            }
                        }
                        Map<String, String> placeholders = new HashMap();
                        placeholders.put("{announcement}", args[1]);
                        MessageUtil.sendMessage(sender, "Command-Messages.Broadcast.Not-Found", placeholders);
                        return true;
                    }
                } else if (args[0].equalsIgnoreCase("list")) {
                    if (!PluginControl.hasPermission(sender, "Permissions.Commands.List")) {
                        MessageUtil.sendMessage(sender, "No-Permission");
                        return true;
                    }
                    Map<String, String> placeholders = new HashMap();
                    List<String> name = new ArrayList();
                    for (Announcement announcement : PluginControl.getAnnouncements()) {
                        name.add(announcement.getName());
                    }
                    placeholders.put("{list}", name.toString().substring(1, name.toString().length() - 1));
                    MessageUtil.sendMessage(sender, "Command-Messages.List", placeholders);
                    return true;
                } else {
                    MessageUtil.sendMessage(sender, "Command-Messages.Unknown-Command");
                }
            }
        }
        return false;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length >= 1) {
            if (args[0].equalsIgnoreCase("view") && args.length == 2 && PluginControl.hasPermission(sender, "Permissions.Commands.View")) {
                return getAnnouncements(args[1]);
            }
            if (args[0].equalsIgnoreCase("broadcast") && args.length == 2 && PluginControl.hasPermission(sender, "Permissions.Commands.Broadcast")) {
                return getAnnouncements(args[1]);
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
        List<String> commands = Arrays.asList("help", "reload", "broadcast",  "view", "list");
        if (args != null) {
            List<String> names = new ArrayList();
            for (String command : commands) {
                if (command.startsWith(args.toLowerCase())) {
                    names.add(command);
                }
            }
            return names;
        }
        return commands;
    }
}
