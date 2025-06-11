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

import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import studio.trc.bukkit.liteannouncer.Main;
import studio.trc.bukkit.liteannouncer.util.LiteAnnouncerProperties;
import studio.trc.bukkit.liteannouncer.message.MessageUtil;

public class ConfigurationUtil
{
    private final static Map<ConfigurationType, Configuration> cacheConfig = new HashMap();
    
    public static FileConfiguration getFileConfiguration(ConfigurationType fileType) {
        return ConfigurationUtil.getConfig(fileType).getRawConfig();
    }
    
    public static Configuration getConfig(ConfigurationType fileType) {
        if (cacheConfig.containsKey(fileType)) {
            return cacheConfig.get(fileType);
        }
        Configuration config = new Configuration(new YamlConfiguration(), fileType);
        cacheConfig.put(fileType, config);
        return config;
    }
    
    public static void reloadConfig(ConfigurationType fileType) {
        saveResource(fileType);
        try (InputStreamReader config = new InputStreamReader(new FileInputStream("plugins/LiteAnnouncer/" + fileType.getFileName()), "UTF-8")) {
            getFileConfiguration(fileType).load(config);
        } catch (IOException | InvalidConfigurationException ex) {
            File oldFile = new File("plugins/LiteAnnouncer/" + fileType.getFileName() + ".old");
            File file = new File("plugins/LiteAnnouncer/" + fileType.getFileName());
            Map<String, String> placeholders = MessageUtil.getDefaultPlaceholders();
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

    private static void saveResource(ConfigurationType fileType) {
        File dataFolder = new File("plugins/LiteAnnouncer/");
        if (!dataFolder.exists()) {
            dataFolder.mkdirs();
        }
        try {
            File configFile = new File(dataFolder, fileType.getFileName());
            if (!configFile.exists()) {
                configFile.createNewFile();
                InputStream is = Main.class.getResourceAsStream("/Languages/" + MessageUtil.Language.getLocaleLanguage().getFolderName() + "/Bukkit/" + fileType.getFileName());
                byte[] bytes = new byte[is.available()];
                for (int len = 0; len != bytes.length; len += is.read(bytes, len, bytes.length - len));
                try (OutputStream out = new FileOutputStream(configFile)) {
                    out.write(bytes);
                }
            }
        } catch (IOException ex) {}
    }
    
    public static void saveConfig() {
        for (ConfigurationType type : ConfigurationType.values()) {
            saveConfig(type);
        }
    }
    
    public static void saveConfig(ConfigurationType file) {
        try {
            ConfigurationUtil.getFileConfiguration(file).save("plugins/LiteAnnouncer/" + file.getFileName());
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
}
