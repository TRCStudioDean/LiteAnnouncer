package studio.trc.bungee.liteannouncer.async;

import java.util.ArrayList;

import studio.trc.bungee.liteannouncer.util.PluginControl;

public class AnnouncerThread
    extends Thread 
{
    public boolean isRunning = false;
    
    @Override
    public void run() {
        if (PluginControl.getAnnouncements().isEmpty()) {
            return;
        }
        while (isRunning) {
            try {
                new ArrayList<>(PluginControl.getAnnouncements()).stream().filter(running -> isRunning).forEach(announcement -> {
                    announcement.broadcast(this);
                });
            } catch (Throwable t) {
                t.printStackTrace();
            }
        }
    }
}
