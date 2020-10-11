package studio.trc.bungee.liteannouncer.configuration;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import net.md_5.bungee.config.Configuration;

import studio.trc.bungee.liteannouncer.Main;
import studio.trc.bungee.liteannouncer.util.LiteAnnouncerProperties;

public class DefaultConfigurationFile
{
    private final static Map<ConfigurationType, Configuration> cacheDefaultConfig = new HashMap();
    private final static Map<ConfigurationType, Boolean> isDefaultConfigLoaded = new HashMap();
    
    public static Configuration getDefaultConfig(ConfigurationType type) {
        if (!isDefaultConfigLoaded.containsKey(type) || !isDefaultConfigLoaded.get(type)) {
            loadDefaultConfigurationFile(type);
            isDefaultConfigLoaded.put(type, true);
        }
        return cacheDefaultConfig.get(type);
    }
    
    public static void loadDefaultConfigurationFile(ConfigurationType type) {
        Locale lang = LiteAnnouncerProperties.lang;
        String jarPath = "English";
        if (lang.equals(Locale.SIMPLIFIED_CHINESE) || lang.equals(Locale.CHINESE)) {
            jarPath = "Chinese";
        }
        String fileName = type.getFileName();
        try (Reader config = new InputStreamReader(Main.getInstance().getClass().getResource("/Languages/" + jarPath + "/" + fileName).openStream(), "UTF-8")) {
            Configuration configFile = ConfigurationUtil.provider.load(config, ConfigurationUtil.getFileConfiguration(type));
            cacheDefaultConfig.put(type, configFile);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
}
