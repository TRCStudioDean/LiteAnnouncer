package studio.trc.bungee.liteannouncer.command;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.plugin.Command;
import net.md_5.bungee.api.plugin.TabExecutor;

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
                for (Announcement announcement : PluginControl.getAnnouncements()) {
                    name.add(announcement.getName());
                }
                placeholders.put("{list}", name.toString().substring(1, name.toString().length() - 1));
                MessageUtil.sendMessage(sender, "Command-Messages.List", placeholders);
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
