package cn.i7mc.customfurnace.managers;

import cn.i7mc.customfurnace.CustomFurnace;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;

/**
 * 配置管理器
 */
public class ConfigManager {
    private final CustomFurnace plugin;
    private final File configFile;
    private File messagesFile;
    private File debugMessagesFile;
    private final File furnacesFile;

    private FileConfiguration config;
    private FileConfiguration messagesConfig;
    private FileConfiguration debugMessagesConfig;
    private FileConfiguration furnacesConfig;

    public ConfigManager(CustomFurnace plugin) {
        this.plugin = plugin;
        this.configFile = new File(plugin.getDataFolder(), "config.yml");
        this.furnacesFile = new File(plugin.getDataFolder(), "furnaces.yml");

        // 保存默认配置
        saveDefaultConfigs();

        // 加载配置
        loadConfigs();

        // 根据语言配置设置消息文件路径
        updateLanguageFiles();
    }

    /**
     * 根据语言配置更新消息文件路径
     */
    private void updateLanguageFiles() {
        String language = getLanguage();
        this.messagesFile = new File(plugin.getDataFolder(), "message_" + language + ".yml");
        this.debugMessagesFile = new File(plugin.getDataFolder(), "debugmessage_" + language + ".yml");

        // 重新加载消息配置
        loadMessageConfigs();
    }

    /**
     * 获取当前语言设置
     */
    public String getLanguage() {
        return config.getString("language", "en");
    }

    /**
     * 保存默认配置
     */
    private void saveDefaultConfigs() {
        plugin.saveDefaultConfig();
        plugin.saveResource("furnaces.yml", false);

        // 保存默认语言文件
        plugin.saveResource("message_en.yml", false);
        plugin.saveResource("message_zh.yml", false);
        plugin.saveResource("debugmessage_en.yml", false);
        plugin.saveResource("debugmessage_zh.yml", false);
    }

    /**
     * 加载所有配置
     */
    private void loadConfigs() {
        // 加载主配置
        config = plugin.getConfig();
        furnacesConfig = YamlConfiguration.loadConfiguration(furnacesFile);
    }

    /**
     * 加载消息配置文件
     */
    private void loadMessageConfigs() {
        if (messagesFile != null && messagesFile.exists()) {
            messagesConfig = YamlConfiguration.loadConfiguration(messagesFile);
        }
        if (debugMessagesFile != null && debugMessagesFile.exists()) {
            debugMessagesConfig = YamlConfiguration.loadConfiguration(debugMessagesFile);
        }
    }

    /**
     * 获取主配置文件
     */
    public FileConfiguration getConfig() {
        return config;
    }

    /**
     * 获取消息配置文件
     */
    public FileConfiguration getMessagesConfig() {
        return messagesConfig;
    }

    /**
     * 获取调试消息配置文件
     */
    public FileConfiguration getDebugMessagesConfig() {
        return debugMessagesConfig;
    }

    /**
     * 获取熔炉配置文件
     */
    public FileConfiguration getFurnacesConfig() {
        return furnacesConfig;
    }

    /**
     * 获取消息配置文件
     */
    public File getMessagesFile() {
        return messagesFile;
    }

    /**
     * 获取调试消息配置文件
     */
    public File getDebugMessagesFile() {
        return debugMessagesFile;
    }

    /**
     * 重载所有配置
     */
    public void reloadConfigs() {
        // 重载主配置
        plugin.reloadConfig();
        config = plugin.getConfig();

        // 根据新的语言配置更新消息文件路径
        updateLanguageFiles();

        // 重载熔炉配置
        furnacesConfig = YamlConfiguration.loadConfiguration(furnacesFile);

        // 通知其他管理器重载
        plugin.getDataManager().applyAllFurnaces();
        plugin.getLangManager().reload();
        plugin.getHologramManager().reloadHologramConfig();
    }

    /**
     * 保存所有配置
     */
    public void saveConfigs() {
        try {
            config.save(configFile);
            messagesConfig.save(messagesFile);
            debugMessagesConfig.save(debugMessagesFile);
            furnacesConfig.save(furnacesFile);
        } catch (IOException e) {
            plugin.getLogger().severe("保存配置文件失败: " + e.getMessage());
        }
    }

    /**
     * 检查是否启用调试模式
     */
    public boolean isDebugEnabled() {
        return config.getBoolean("debug", false);
    }

    /**
     * 检查是否启用TextDisplay全息信息显示
     */
    public boolean isTextDisplayHologramEnabled() {
        return config.getBoolean("display.text-display-hologram", true);
    }

    /**
     * 向后兼容的方法名
     * @deprecated 使用 isTextDisplayHologramEnabled() 代替
     */
    @Deprecated
    public boolean isArmorstandHologramEnabled() {
        return isTextDisplayHologramEnabled();
    }

    /**
     * 检查是否启用掉落物悬浮标签显示
     */
    public boolean isDroppedItemHologramEnabled() {
        return config.getBoolean("display.dropped-item-hologram", true);
    }
}