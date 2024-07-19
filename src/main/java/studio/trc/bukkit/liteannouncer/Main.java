package studio.trc.bukkit.liteannouncer;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import studio.trc.bukkit.liteannouncer.command.LiteAnnouncerCommand;
import studio.trc.bukkit.liteannouncer.util.LiteAnnouncerProperties;
import studio.trc.bukkit.liteannouncer.util.PluginControl;
import studio.trc.bukkit.liteannouncer.util.Metrics;
import studio.trc.bukkit.liteannouncer.util.Updater;

public class Main
    extends JavaPlugin
{
    private static Main main;
    private static Metrics metrics;
    
    @Override
    public void onEnable() {
        main = this;
        
        LiteAnnouncerProperties.reloadProperties();
        
        if (!getDescription().getName().equals("LiteAnnouncer")) {
            LiteAnnouncerProperties.sendOperationMessage("PluginNameChange");
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }
        
        LiteAnnouncerCommand commandExecutor = new LiteAnnouncerCommand();
        Bukkit.getPluginCommand("la").setExecutor(commandExecutor);
        Bukkit.getPluginManager().registerEvents(new Updater(), this);
        
        PluginControl.reload();
        
        //It will run after the server is started.
        PluginControl.runBukkitTask(() -> {
            if (PluginControl.enableUpdater()) {
                Updater.checkUpdate();
            }
        }, 0);
        
        //Metrics
        if (PluginControl.enableMetrics()) {
            metrics = new Metrics(main, 12151);
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
