package studio.trc.bukkit.liteannouncer.configuration;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.HashMap;
import java.util.Map;

import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import studio.trc.bukkit.liteannouncer.Main;
import studio.trc.bukkit.liteannouncer.message.MessageUtil;

public class DefaultConfigurationFile
{
    private final static Map<ConfigurationType, FileConfiguration> cacheDefaultConfig = new HashMap();
    private final static Map<ConfigurationType, Boolean> isDefaultConfigLoaded = new HashMap();
    
    public static FileConfiguration getDefaultConfig(ConfigurationType type) {
        if (!isDefaultConfigLoaded.containsKey(type) || !isDefaultConfigLoaded.get(type)) {
            loadDefaultConfigurationFile(type);
            isDefaultConfigLoaded.put(type, true);
        }
        return cacheDefaultConfig.get(type);
    }
    
    public static void loadDefaultConfigurationFile(ConfigurationType type) {
        String jarPath = MessageUtil.Language.getLocaleLanguage().getFolderName();
        String fileName = type.getFileName();
        try (Reader Config = new InputStreamReader(Main.getInstance().getClass().getResource("/Languages/" + jarPath + "/Bungee/" + fileName).openStream(), "UTF-8")) {
            FileConfiguration configFile = new YamlConfiguration();
            configFile.load(Config);
            cacheDefaultConfig.put(type, configFile);
        } catch (IOException | InvalidConfigurationException ex) {
            ex.printStackTrace();
        }
    }
}
