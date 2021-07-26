package studio.trc.bungee.liteannouncer;

import net.md_5.bungee.api.plugin.Plugin;

import studio.trc.bungee.liteannouncer.command.LiteAnnouncerCommand;
import studio.trc.bungee.liteannouncer.util.LiteAnnouncerProperties;
import studio.trc.bungee.liteannouncer.util.PluginControl;
import studio.trc.bungee.liteannouncer.util.Metrics;
import studio.trc.bungee.liteannouncer.util.Updater;

public class Main
    extends Plugin
{
    private static Main main;
    private static Metrics metrics;
    
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
        getProxy().getPluginManager().registerListener(this, new Updater());
        
        PluginControl.reload();
        
        //It will run after the server is started.
        if (PluginControl.enableUpdater()) {
            Updater.checkUpdate();
        }
        
        //Metrics
        if (PluginControl.enableMetrics()) {
            metrics = new Metrics(main, 12152);
        }
    }

    @Override
    public void onDisable() {
        PluginControl.stopAnnouncer();
    }
    
    public static Main getInstance() {
        return main;
    }
    
    public static Metrics getMetrics() {
        return metrics;
    }
}
