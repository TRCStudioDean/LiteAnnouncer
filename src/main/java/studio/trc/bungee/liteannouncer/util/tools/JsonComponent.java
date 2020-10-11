package studio.trc.bungee.liteannouncer.util.tools;

import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;

public class JsonComponent
{
    private final String placeholder;
    private final BaseComponent bc;
    
    public JsonComponent(String placeholder, BaseComponent bc) {
        this.placeholder = placeholder;
        this.bc = bc;
    }
    
    public String getPlaceholder() {
        return placeholder;
    }
    
    public BaseComponent getComponent() {
        return bc;
    }
    
    public ClickEvent getClickEvent() {
        return bc.getClickEvent();
    }
    
    public HoverEvent getHoverEvent() {
        return bc.getHoverEvent();
    }
}
