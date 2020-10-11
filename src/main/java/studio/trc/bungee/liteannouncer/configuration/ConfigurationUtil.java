package studio.trc.bungee.liteannouncer.configuration;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.config.ConfigurationProvider;
import net.md_5.bungee.config.YamlConfiguration;

import studio.trc.bungee.liteannouncer.Main;
import studio.trc.bungee.liteannouncer.util.LiteAnnouncerProperties;

public class ConfigurationUtil
{
    public static ConfigurationProvider provider = ConfigurationProvider.getProvider(YamlConfiguration.class);
    private static Configuration config = new Configuration();
    private static Configuration announcements = new Configuration();
    private static Configuration components = new Configuration();
    private static Configuration messages = new Configuration();
    
    public static Configuration getFileConfiguration(ConfigurationType fileType) {
        switch (fileType) {
            case CONFIG: return config;
            case ANNOUNCEMENTS: return announcements;
            case COMPONENTS: return components;
            case MESSAGES: return messages;
            default: return null;
        }
    }
    
    private final static Map<ConfigurationType, ConfigurationFile> cacheConfig = new HashMap();
    
    public static ConfigurationFile getConfig(ConfigurationType fileType) {
        if (cacheConfig.containsKey(fileType)) {
            return cacheConfig.get(fileType);
        }
        switch (fileType) {
            case CONFIG: {
                ConfigurationFile file = new ConfigurationFile(config, fileType);
                cacheConfig.put(fileType, file);
                return file;
            }
            case ANNOUNCEMENTS: {
                ConfigurationFile file = new ConfigurationFile(announcements, fileType);
                cacheConfig.put(fileType, file);
                return file;
            }
            case COMPONENTS: {
                ConfigurationFile file = new ConfigurationFile(components, fileType);
                cacheConfig.put(fileType, file);
                return file;
            }
            case MESSAGES: {
                ConfigurationFile file = new ConfigurationFile(messages, fileType);
                cacheConfig.put(fileType, file);
                return file;
            }
            default: return null;
        }
    }
    
    public static void reloadConfig(ConfigurationType fileType) {
        cacheConfig.remove(fileType);
        saveResource(fileType);
        try (InputStreamReader configReader = new InputStreamReader(new FileInputStream("plugins/LiteAnnouncer/" + fileType.getFileName()), "UTF-8")) {
            switch (fileType) {
                case ANNOUNCEMENTS: {
                    announcements = provider.load(configReader);
                    break;
                }
                case COMPONENTS: {
                    components = provider.load(configReader);
                    break;
                }
                case CONFIG: {
                    config = provider.load(configReader);
                    break;
                }
                case MESSAGES: {
                    messages = provider.load(configReader);
                    break;
                }
            }
        } catch (IOException ex) {
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
                provider.load(newConfig, getFileConfiguration(fileType));
                LiteAnnouncerProperties.sendOperationMessage("ConfigurationRepair", true);
            } catch (IOException ex1) {
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
        String lang = Locale.getDefault().toString();
        if (!new File("plugins/LiteAnnouncer").exists()) {
            new File("plugins/LiteAnnouncer").mkdir();
        }
        switch (file) {
            case CONFIG: {
                try {
                    File configFile = new File("plugins/LiteAnnouncer/Config.yml");
                    if (!configFile.exists()) {
                        configFile.createNewFile();
                        if (lang.equalsIgnoreCase("zh_cn")) {
                            InputStream is = Main.class.getResourceAsStream("/Languages/Chinese/Bungee/Config.yml");
                            try (OutputStream out = new FileOutputStream(configFile)) {
                                int b;
                                while ((b = is.read()) != -1) {
                                    out.write((char) b);
                                }
                            }
                        } else {
                            InputStream is = Main.class.getResourceAsStream("/Languages/English/Bungee/Config.yml");
                            try (OutputStream out = new FileOutputStream(configFile)) {
                                int b;
                                while ((b = is.read()) != -1) {
                                    out.write((char) b);
                                }
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
                        if (lang.equalsIgnoreCase("zh_cn")) {
                            InputStream is = Main.class.getResourceAsStream("/Languages/Chinese/Bungee/Announcements.yml");
                            try (OutputStream out = new FileOutputStream(configFile)) {
                                int b;
                                while ((b = is.read()) != -1) {
                                    out.write((char) b);
                                }
                            }
                        } else {
                            InputStream is = Main.class.getResourceAsStream("/Languages/English/Bungee/Announcements.yml");
                            try (OutputStream out = new FileOutputStream(configFile)) {
                                int b;
                                while ((b = is.read()) != -1) {
                                    out.write((char) b);
                                }
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
                        if (lang.equalsIgnoreCase("zh_cn")) {
                            InputStream is = Main.class.getResourceAsStream("/Languages/Chinese/Bungee/Components.yml");
                            try (OutputStream out = new FileOutputStream(configFile)) {
                                int b;
                                while ((b = is.read()) != -1) {
                                    out.write((char) b);
                                }
                            }
                        } else {
                            InputStream is = Main.class.getResourceAsStream("/Languages/English/Bungee/Components.yml");
                            try (OutputStream out = new FileOutputStream(configFile)) {
                                int b;
                                while ((b = is.read()) != -1) {
                                    out.write((char) b);
                                }
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
                        if (lang.equalsIgnoreCase("zh_cn")) {
                            InputStream is = Main.class.getResourceAsStream("/Languages/Chinese/Bungee/Messages.yml");
                            try (OutputStream out = new FileOutputStream(configFile)) {
                                int b;
                                while ((b = is.read()) != -1) {
                                    out.write((char) b);
                                }
                            }
                        } else {
                            InputStream is = Main.class.getResourceAsStream("/Languages/English/Bungee/Messages.yml");
                            try (OutputStream out = new FileOutputStream(configFile)) {
                                int b;
                                while ((b = is.read()) != -1) {
                                    out.write((char) b);
                                }
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
                    provider.save(config, new File("plugins/LiteAnnouncer/Config.yml"));
                    break;
                }
                case ANNOUNCEMENTS: {
                    provider.save(announcements, new File("plugins/LiteAnnouncer/Announcements.yml"));
                    break;
                }
                case COMPONENTS: {
                    provider.save(components, new File("plugins/LiteAnnouncer/Components.yml"));
                    break;
                }
                case MESSAGES: {
                    provider.save(messages, new File("plugins/LiteAnnouncer/Messages.yml"));
                    break;
                }
            }
        } catch (IOException ex) {
            Logger.getLogger(ConfigurationUtil.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
