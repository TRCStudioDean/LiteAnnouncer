package studio.trc.bungee.liteannouncer.configuration;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.config.Configuration;

import studio.trc.bungee.liteannouncer.util.MessageUtil;

/**
 * Used to manage configuration files.
 * @author Dean
 */
public class ConfigurationFile
{
    private final Configuration config;
    private final ConfigurationType type;
    
    public ConfigurationFile(Configuration config, ConfigurationType type) {
        this.config = config;
        this.type = type;
    }
    
    public void repairConfigurationSection(String path) {
        if (type.equals(ConfigurationType.ANNOUNCEMENTS) || type.equals(ConfigurationType.COMPONENTS)) return;
        Configuration defaultFile = DefaultConfigurationFile.getDefaultConfig(type);
        config.set(path, defaultFile.get(path) != null ? defaultFile.get(path) : "null");
        saveConfig();
        Map<String, String> placeholders = new HashMap();
        placeholders.put("{config}", type.getFileName());
        placeholders.put("{path}", path);
        MessageUtil.sendMessage(ProxyServer.getInstance().getConsole(), "Repaired-Config-Section", placeholders);
    }
    
    public Object get(String path) {
        return config.get(path);
    }

    public String getString(String path) {
        if (config.get(path) == null) {
            repairConfigurationSection(path);
            return config.getString(path);
        } else {
            return config.getString(path);
        }
    }

    public int getInt(String path) {
        if (config.get(path) == null) {
            repairConfigurationSection(path);
            return config.getInt(path);
        } else {
            return config.getInt(path);
        }
    }

    public double getDouble(String path) {
        if (config.get(path) == null) {
            repairConfigurationSection(path);
            return config.getDouble(path);
        } else {
            return config.getDouble(path);
        }
    }

    public long getLong(String path) {
        if (config.get(path) == null) {
            repairConfigurationSection(path);
            return config.getLong(path);
        } else {
            return config.getLong(path);
        }
    }

    public boolean getBoolean(String path) {
        if (config.get(path) == null) {
            return false;
        } else {
            return config.getBoolean(path);
        }
    }

    public List<String> getStringList(String path) {
        if (config.get(path) == null) {
            repairConfigurationSection(path);
            return config.getStringList(path);
        } else {
            return config.getStringList(path);
        }
    }
    
    public List getList(String path) {
        if (config.get(path) == null) {
            repairConfigurationSection(path);
            return config.getList(path);
        } else {
            return config.getList(path);
        }
    }

    public Configuration getConfigurationSection(String path) {
        if (config.get(path) == null) {
            repairConfigurationSection(path);
            return config.getSection(path);
        } else {
            return config.getSection(path);
        }
    }

    public boolean contains(String path) {
        return config.contains(path);
    }

    public void set(String path, Object obj) {
        config.set(path, obj);
    }
    
    public void saveConfig() {
        ConfigurationUtil.saveConfig(type);
    }
    
    public Configuration getRawConfig() {
        return config;
    }
    
    public ConfigurationType getConfigType() {
        return type;
    }
}
