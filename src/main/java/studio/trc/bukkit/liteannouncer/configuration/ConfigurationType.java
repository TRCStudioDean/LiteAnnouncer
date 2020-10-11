package studio.trc.bukkit.liteannouncer.configuration;

public enum ConfigurationType
{
    /**
     * Config.yml
     */
    CONFIG("Config.yml"),
    
    /**
     * Components.yml
     */
    COMPONENTS("Components.yml"),
    
    /**
     * Messages.yml
     */
    MESSAGES("Messages.yml"),
    
    /**
     * Announcements.yml
     */
    ANNOUNCEMENTS("Announcements.yml");
    
    private final String fileName;
    
    private ConfigurationType(String fileName) {
        this.fileName = fileName;
    }
    
    public String getFileName() {
        return fileName;
    }
}
