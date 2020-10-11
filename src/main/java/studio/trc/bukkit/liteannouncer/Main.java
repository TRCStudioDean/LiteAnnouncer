package studio.trc.bukkit.liteannouncer;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import studio.trc.bukkit.liteannouncer.command.LiteAnnouncerCommand;
import studio.trc.bukkit.liteannouncer.util.LiteAnnouncerProperties;
import studio.trc.bukkit.liteannouncer.util.PluginControl;

public class Main
    extends JavaPlugin
{
    private static Main main;
    
    @Override
    public void onEnable() {
        main = this;
        
        LiteAnnouncerProperties.reloadProperties();
        
        if (!getDescription().getName().equals("LiteAnnouncer")) {
            LiteAnnouncerProperties.sendOperationMessage("PluginNameChange");
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }
        
        Bukkit.getPluginCommand("la").setExecutor(new LiteAnnouncerCommand());
        Bukkit.getPluginCommand("liteannouncer").setExecutor(new LiteAnnouncerCommand());
        Bukkit.getPluginCommand("la").setTabCompleter(new LiteAnnouncerCommand());
        Bukkit.getPluginCommand("liteannouncer").setTabCompleter(new LiteAnnouncerCommand());
        
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
