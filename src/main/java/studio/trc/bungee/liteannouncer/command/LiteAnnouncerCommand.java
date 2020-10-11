package studio.trc.bungee.liteannouncer.command;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.plugin.Command;

import studio.trc.bungee.liteannouncer.util.MessageUtil;
import studio.trc.bungee.liteannouncer.util.PluginControl;
import studio.trc.bungee.liteannouncer.util.tools.Announcement;

public class LiteAnnouncerCommand
    extends Command
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
}
