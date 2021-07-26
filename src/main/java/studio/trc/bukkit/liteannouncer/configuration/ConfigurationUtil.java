package studio.trc.bukkit.liteannouncer.configuration;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import studio.trc.bukkit.liteannouncer.Main;
import studio.trc.bukkit.liteannouncer.util.LiteAnnouncerProperties;
import studio.trc.bukkit.liteannouncer.util.MessageUtil;

public class ConfigurationUtil
{
    private final static FileConfiguration config = new YamlConfiguration();
    private final static FileConfiguration announcements = new YamlConfiguration();
    private final static FileConfiguration components = new YamlConfiguration();
    private final static FileConfiguration messages = new YamlConfiguration();
    
    public static FileConfiguration getFileConfiguration(ConfigurationType fileType) {
        switch (fileType) {
            case CONFIG: return config;
            case ANNOUNCEMENTS: return announcements;
            case COMPONENTS: return components;
            case MESSAGES: return messages;
            default: return null;
        }
    }
    
    private final static Map<ConfigurationType, Configuration> cacheConfig = new HashMap();
    
    public static Configuration getConfig(ConfigurationType fileType) {
        if (cacheConfig.containsKey(fileType)) {
            return cacheConfig.get(fileType);
        }
        switch (fileType) {
            case CONFIG: {
                Configuration file = new Configuration(config, fileType);
                cacheConfig.put(fileType, file);
                return file;
            }
            case ANNOUNCEMENTS: {
                Configuration file = new Configuration(announcements, fileType);
                cacheConfig.put(fileType, file);
                return file;
            }
            case COMPONENTS: {
                Configuration file = new Configuration(components, fileType);
                cacheConfig.put(fileType, file);
                return file;
            }
            case MESSAGES: {
                Configuration file = new Configuration(messages, fileType);
                cacheConfig.put(fileType, file);
                return file;
            }
            default: return null;
        }
    }
    
    public static void reloadConfig(ConfigurationType fileType) {
        saveResource(fileType);
        try (InputStreamReader Config = new InputStreamReader(new FileInputStream("plugins/LiteAnnouncer/" + fileType.getFileName()), "UTF-8")) {
            getFileConfiguration(fileType).load(Config);
        } catch (IOException | InvalidConfigurationException ex) {
            File oldFile = new File("plugins/LiteAnnouncer/" + fileType.getFileName() + ".old");
            File file = new File("plugins/LiteAnnouncer/" + fileType.getFileName());
            Map<String, String> placeholders = new HashMap();
            placeholders.put("{file}", fileType.getFileName());
            LiteAnnouncerProperties.sendOperationMessage("ConfigurationLoadingError", placeholders);
            if (oldFile.exists()) {
                oldFile.delete();
            }
            file.renameTo(oldFile);
            saveResource(fileType);
            try (InputStreamReader newConfig = new InputStreamReader(new FileInputStream(file), "UTF-8")) {
                getFileConfiguration(fileType).load(newConfig);
                LiteAnnouncerProperties.sendOperationMessage("ConfigurationRepair", new HashMap());
            } catch (IOException | InvalidConfigurationException ex1) {
                ex1.printStackTrace();
            }
        }
    }
    
    public static void reloadConfig() {
        for (ConfigurationType type : ConfigurationType.values()) {
            reloadConfig(type);
        }
    }

    private static void saveResource(ConfigurationType file) {
        if (!new File("plugins/LiteAnnouncer").exists()) {
            new File("plugins/LiteAnnouncer").mkdir();
        }
        switch (file) {
            case CONFIG: {
                try {
                    File configFile = new File("plugins/LiteAnnouncer/Config.yml");
                    if (!configFile.exists()) {
                        configFile.createNewFile();
                        InputStream is = Main.class.getResourceAsStream("/Languages/" + MessageUtil.Language.getLocaleLanguage().getFolderName() + "/Bukkit/Config.yml");
                        try (OutputStream out = new FileOutputStream(configFile)) {
                            int b;
                            while ((b = is.read()) != -1) {
                                out.write((char) b);
                            }
                        }
                    }
                } catch (IOException ex) {}
                break;
            }
            case ANNOUNCEMENTS: {
                try {
                    File configFile = new File("plugins/LiteAnnouncer/Announcements.yml");
                    if (!configFile.exists()) {
                        configFile.createNewFile();
                        InputStream is = Main.class.getResourceAsStream("/Languages/" + MessageUtil.Language.getLocaleLanguage().getFolderName() + "/Bukkit/Announcements.yml");
                        try (OutputStream out = new FileOutputStream(configFile)) {
                            int b;
                            while ((b = is.read()) != -1) {
                                out.write((char) b);
                            }
                        }
                    }
                } catch (IOException ex) {}
                break;
            }
            case COMPONENTS: {
                try {
                    File configFile = new File("plugins/LiteAnnouncer/Components.yml");
                    if (!configFile.exists()) {
                        configFile.createNewFile();
                        InputStream is = Main.class.getResourceAsStream("/Languages/" + MessageUtil.Language.getLocaleLanguage().getFolderName() + "/Bukkit/Components.yml");
                        try (OutputStream out = new FileOutputStream(configFile)) {
                            int b;
                            while ((b = is.read()) != -1) {
                                out.write((char) b);
                            }
                        }
                    }
                } catch (IOException ex) {}
                break;
            }
            case MESSAGES: {
                try {
                    File configFile = new File("plugins/LiteAnnouncer/Messages.yml");
                    if (!configFile.exists()) {
                        configFile.createNewFile();
                        InputStream is = Main.class.getResourceAsStream("/Languages/" + MessageUtil.Language.getLocaleLanguage().getFolderName() + "/Bukkit/Messages.yml");
                        try (OutputStream out = new FileOutputStream(configFile)) {
                            int b;
                            while ((b = is.read()) != -1) {
                                out.write((char) b);
                            }
                        }
                    }
                } catch (IOException ex) {}
                break;
            }
        }
    }
    
    public static void saveConfig() {
        for (ConfigurationType type : ConfigurationType.values()) {
            saveConfig(type);
        }
    }
    
    public static void saveConfig(ConfigurationType file) {
        try {
            switch (file) {
                case CONFIG: {
                    config.save("plugins/LiteAnnouncer/Config.yml");
                    break;
                }
                case ANNOUNCEMENTS: {
                    announcements.save("plugins/LiteAnnouncer/Announcements.yml");
                    break;
                }
                case COMPONENTS: {
                    components.save("plugins/LiteAnnouncer/Components.yml");
                    break;
                }
                case MESSAGES: {
                    messages.save("plugins/LiteAnnouncer/Messages.yml");
                    break;
                }
            }
        } catch (IOException ex) {
            Logger.getLogger(ConfigurationUtil.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
