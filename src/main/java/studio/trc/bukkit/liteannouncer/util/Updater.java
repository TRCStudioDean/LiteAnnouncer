package studio.trc.bukkit.liteannouncer.util;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import studio.trc.bukkit.liteannouncer.configuration.ConfigurationType;
import studio.trc.bukkit.liteannouncer.configuration.ConfigurationUtil;
import studio.trc.bukkit.liteannouncer.message.JSONComponent;
import studio.trc.bukkit.liteannouncer.message.color.ColorUtils;
import studio.trc.bukkit.liteannouncer.message.MessageUtil;

public class Updater
    implements Listener
{
    private static boolean foundANewVersion = false;
    private static String newVersion;
    private static String link;
    private static String description;
    private static Thread checkUpdateThread;
    private static Date date = new Date();
    
    @EventHandler()
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        if (PluginControl.enableUpdater()) {
            String now = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
            String checkUpdateTime = new SimpleDateFormat("yyyy-MM-dd").format(Updater.getTimeOfLastCheckUpdate());
            if (!now.equals(checkUpdateTime)) {
                Updater.checkUpdate();
            }
        }
        if (Updater.isFoundANewVersion() && PluginControl.enableUpdater()) {
            if (PluginControl.hasPermission(player, "Permissions.Updater")) {
                String nowVersion = Bukkit.getPluginManager().getPlugin("LiteAnnouncer").getDescription().getVersion();
                Map<String, String> placeholders = MessageUtil.getDefaultPlaceholders();
                placeholders.put("{nowVersion}", nowVersion);
                placeholders.put("{version}", Updater.getNewVersion());
                placeholders.put("{link}", Updater.getLink());
                placeholders.put("{description}", Updater.getDescription());
                JSONComponent component = new JSONComponent(
                    ColorUtils.toColor(MessageUtil.getMessage("Updater.Link.Message")),
                    MessageUtil.getMessageList("Updater.Link.Hover-Text"),
                    "OPEN_URL",
                    Updater.getLink()
                );
                MessageUtil.getMessageList("Updater.Checked").stream().forEach(text -> {
                    MessageUtil.sendMessageWithJSONComponent(player, text, placeholders, "%link%", component);
                });
            }
        }
    }
    
    /**
     * Initialize programs.
     */
    public static void initialize() {
        checkUpdateThread = new Thread(() -> {
            try {
                URL url = new URL("https://api.trc.studio/resources/spigot/liteannouncer/update.yml");
                try (Reader reader = new InputStreamReader(url.openStream(), "UTF-8")) {
                    YamlConfiguration yaml = new YamlConfiguration();
                    yaml.load(reader);
                    String version = yaml.getString("latest-version");
                    String downloadLink = yaml.getString("link");
                    String description_ = "description.Default";
                    if (yaml.get("description." + MessageUtil.getLanguage()) != null) {
                        description_ = yaml.getString("description." + MessageUtil.getLanguage());
                    } else {
                        for (String languages : yaml.getConfigurationSection("description").getKeys(false)) {
                            if (MessageUtil.getLanguage().contains(languages)) {
                                description_ = yaml.getString("description." + MessageUtil.getLanguage());
                                break;
                            }
                        }
                    }
                    String nowVersion = Bukkit.getPluginManager().getPlugin("LiteAnnouncer").getDescription().getVersion();
                    if (!nowVersion.equalsIgnoreCase(version)) {
                        newVersion = version;
                        foundANewVersion = true;
                        link = downloadLink;
                        description = description_;
                        Map<String, String> placeholders = MessageUtil.getDefaultPlaceholders();
                        placeholders.put("{version}", version);
                        placeholders.put("%link%", downloadLink);
                        placeholders.put("{link}", downloadLink);
                        placeholders.put("{nowVersion}", nowVersion);
                        placeholders.put("{description}", description_);
                        MessageUtil.sendMessage(Bukkit.getConsoleSender(), ConfigurationUtil.getConfig(ConfigurationType.MESSAGES), "Updater.Checked", placeholders);
                    }
                } catch (InvalidConfigurationException | IOException ex) {
                    MessageUtil.sendMessage(Bukkit.getConsoleSender(), ConfigurationUtil.getConfig(ConfigurationType.MESSAGES), "Updater.Error");
                }
            } catch (MalformedURLException ex) {
                MessageUtil.sendMessage(Bukkit.getConsoleSender(), ConfigurationUtil.getConfig(ConfigurationType.MESSAGES), "Updater.Error");
            }
            date = new Date();
        });
    }
    
    /**
     * Start check updater.
     */
    public static void checkUpdate() {
        initialize();
        checkUpdateThread.start();
    }
    
    /**
     * Return whether found a new version.
     * @return 
     */
    public static boolean isFoundANewVersion() {
        return foundANewVersion;
    }
    
    /**
     * Get new version.
     * @return 
     */
    public static String getNewVersion() {
        return newVersion;
    }
    
    /**
     * Get download link.
     * @return 
     */
    public static String getLink() {
        return link;
    }
    
    /**
     * Get new version's update description.
     * @return 
     */
    public static String getDescription() {
        return description;
    }
    
    /**
     * Get the time of last check update.
     * @return 
     */
    public static Date getTimeOfLastCheckUpdate() {
        return date;
    }
}
