package studio.trc.bukkit.liteannouncer.util.tools;

import lombok.Getter;

public class ActionBarOfBroadcast
{
    @Getter
    private final String text;
    @Getter
    private final double delay;
    
    public ActionBarOfBroadcast(String text, double delay) {
        this.delay = delay;
        this.text = text;
    }
}
