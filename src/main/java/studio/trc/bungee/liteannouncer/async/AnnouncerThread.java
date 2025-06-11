package studio.trc.bungee.liteannouncer.async;

import java.util.ArrayList;

import studio.trc.bungee.liteannouncer.util.PluginControl;

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
                    PluginControl.getAnnouncementByRandom().broadcast(this);
                } else {
                    new ArrayList<>(PluginControl.getAnnouncementsByPriority()).stream().forEach(announcement -> announcement.broadcast(this));
                }
            } catch (Throwable t) {
                t.printStackTrace();
            }
        }
    }
}
