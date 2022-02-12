package studio.trc.bungee.liteannouncer.util.tools;

import lombok.Getter;

public class ActionBar
{
    @Getter
    private final String text;
    @Getter
    private final double delay;
    
    public ActionBar(String text, double delay) {
        this.delay = delay;
        this.text = text;
    }
}
