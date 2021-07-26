package studio.trc.bungee.liteannouncer.util.tools;

import lombok.Getter;

import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;

public class JsonComponent
{
    @Getter
    private final String placeholder;
    @Getter
    private final BaseComponent component;
    
    public JsonComponent(String placeholder, BaseComponent component) {
        this.placeholder = placeholder;
        this.component = component;
    }
    
    public ClickEvent getClickEvent() {
        return component.getClickEvent();
    }
    
    public HoverEvent getHoverEvent() {
        return component.getHoverEvent();
    }
}
