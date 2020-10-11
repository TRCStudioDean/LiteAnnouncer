package studio.trc.bungee.liteannouncer;

import net.md_5.bungee.api.plugin.Plugin;

import studio.trc.bungee.liteannouncer.command.LiteAnnouncerCommand;
import studio.trc.bungee.liteannouncer.util.LiteAnnouncerProperties;
import studio.trc.bungee.liteannouncer.util.PluginControl;

public class Main
    extends Plugin
{
    private static Main main;
    
    @Override
    public void onEnable() {
        main = this;
        
        LiteAnnouncerProperties.reloadProperties();
        
        if (!getDescription().getName().equals("LiteAnnouncer")) {
            LiteAnnouncerProperties.sendOperationMessage("PluginNameChange");
            return;
        }
        
        getProxy().getPluginManager().registerCommand(this, new LiteAnnouncerCommand("la"));
        getProxy().getPluginManager().registerCommand(this, new LiteAnnouncerCommand("liteannouncer"));
        
        PluginControl.reload();
    }

    @Override
    public void onDisable() {
        PluginControl.stopAnnouncer();
    }
    
    public static Main getInstance() {
        return main;
    }
}
