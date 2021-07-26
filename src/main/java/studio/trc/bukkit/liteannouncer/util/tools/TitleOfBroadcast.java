package studio.trc.bukkit.liteannouncer.util.tools;

import lombok.Getter;

public class TitleOfBroadcast
{
    @Getter
    private final double fadein;
    @Getter
    private final double stay;
    @Getter
    private final double fadeout;
    @Getter
    private final double delay;
    @Getter
    private final String title;
    @Getter
    private final String subTitle;
    
    public TitleOfBroadcast(double fadein, double stay, double fadeout, double delay, String title, String subTitle) {
        this.fadein = fadein;
        this.stay = stay;
        this.fadeout = fadeout;
        this.delay = delay;
        this.title = title;
        this.subTitle = subTitle;
    }
}
