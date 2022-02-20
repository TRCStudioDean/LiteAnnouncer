package studio.trc.bukkit.liteannouncer.async;

import java.util.ArrayList;

import studio.trc.bukkit.liteannouncer.util.PluginControl;

public class AnnouncerThread
    extends Thread
{
    public boolean isRunning = false;
    
    @Override
    public void run() {
        if (PluginControl.getAnnouncementsByPriority().isEmpty()) {
            return;
        }
        while (isRunning) {
            try {
                if (PluginControl.randomBroadcast()) {
                    if (isRunning) {
                        PluginControl.getAnnouncementByRandom().broadcast(this);
                    }
                } else {
                    new ArrayList<>(PluginControl.getAnnouncementsByPriority()).stream().filter(running -> isRunning).forEach(announcement -> {
                        announcement.broadcast(this);
                    });
                }
            } catch (Throwable t) {
                t.printStackTrace();
            }
        }
    }
}
