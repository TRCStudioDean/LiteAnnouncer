package studio.trc.bungee.liteannouncer.configuration;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.config.ConfigurationProvider;
import net.md_5.bungee.config.YamlConfiguration;

import studio.trc.bungee.liteannouncer.Main;
import studio.trc.bungee.liteannouncer.util.LiteAnnouncerProperties;
import studio.trc.bungee.liteannouncer.message.MessageUtil;

public class ConfigurationUtil
{
    public static ConfigurationProvider provider = ConfigurationProvider.getProvider(YamlConfiguration.class);
    private final static Map<ConfigurationType, ConfigurationFile> cacheConfig = new HashMap();
    
    public static Configuration getFileConfiguration(ConfigurationType fileType) {
        return ConfigurationUtil.getConfig(fileType).getRawConfig();
    }
    
    public static ConfigurationFile getConfig(ConfigurationType fileType) {
        if (cacheConfig.containsKey(fileType)) {
            return cacheConfig.get(fileType);
        }
        ConfigurationFile config = new ConfigurationFile(new Configuration(), fileType);
        cacheConfig.put(fileType, config);
        return config;
    }
    
    public static void reloadConfig(ConfigurationType fileType) {
        saveResource(fileType);
        try (InputStreamReader config = new InputStreamReader(new FileInputStream("plugins/LiteAnnouncer/" + fileType.getFileName()), "UTF-8")) {
            getConfig(fileType).setConfig(provider.load(config));
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
                LiteAnnouncerProperties.sendOperationMessage("ConfigurationRepair", new HashMap());
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

    private static void saveResource(ConfigurationType fileType) {
        File dataFolder = new File("plugins/LiteAnnouncer/");
        if (!dataFolder.exists()) {
            dataFolder.mkdirs();
        }
        try {
            File configFile = new File(dataFolder, fileType.getFileName());
            if (!configFile.exists()) {
                configFile.createNewFile();
                InputStream is = Main.class.getResourceAsStream("/Languages/" + MessageUtil.Language.getLocaleLanguage().getFolderName() + "/Bungee/" + fileType.getFileName());
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
            provider.save(getFileConfiguration(file), new File("plugins/LiteAnnouncer/" + file.getFileName()));
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
}
