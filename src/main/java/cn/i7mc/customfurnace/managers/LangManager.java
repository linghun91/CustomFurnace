package cn.i7mc.customfurnace.managers;

import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.ArrayList;
import java.util.List;

/**
 * 语言管理器
 */
public class LangManager {
    private final ConfigManager configManager;

    public LangManager(ConfigManager configManager) {
        this.configManager = configManager;
    }

    /**
     * 获取带前缀的消息
     */
    public String getMessage(String key, Object... args) {
        String prefix = getRawMessage("prefix");
        String message = getRawMessage(key);

        if (message.isEmpty()) {
            return "";
        }

        message = prefix + message;

        // 替换参数
        if (args != null && args.length > 0) {
            for (int i = 0; i < args.length; i += 2) {
                if (i + 1 < args.length) {
                    String placeholder = "%" + args[i] + "%";
                    String value = String.valueOf(args[i + 1]);
                    message = message.replace(placeholder, value);
                }
            }
        }

        return colorize(message);
    }

    /**
     * 获取原始消息（不带前缀）
     */
    public String getRawMessage(String key) {
        FileConfiguration messagesConfig = configManager.getMessagesConfig();
        String message = messagesConfig.getString(key, "");
        return message;
    }

    /**
     * 获取调试消息
     */
    public String getDebugMessage(String key, Object... args) {
        FileConfiguration debugConfig = configManager.getDebugMessagesConfig();
        String message = debugConfig.getString(key, "");

        if (message.isEmpty()) {
            return "";
        }

        // 替换参数
        if (args != null && args.length > 0) {
            for (int i = 0; i < args.length; i += 2) {
                if (i + 1 < args.length) {
                    String placeholder = "%" + args[i] + "%";
                    String value = String.valueOf(args[i + 1]);
                    message = message.replace(placeholder, value);
                }
            }
        }

        return colorize(message);
    }

    /**
     * 获取Lore列表
     */
    public List<String> getLore(String key, Object... args) {
        FileConfiguration messagesConfig = configManager.getMessagesConfig();
        List<String> lore = messagesConfig.getStringList(key);
        List<String> result = new ArrayList<>();

        if (lore.isEmpty()) {
            return result;
        }

        for (String line : lore) {
            // 替换参数
            if (args != null && args.length > 0) {
                for (int i = 0; i < args.length; i += 2) {
                    if (i + 1 < args.length) {
                        String placeholder = "%" + args[i] + "%";
                        String value = String.valueOf(args[i + 1]);
                        line = line.replace(placeholder, value);
                    }
                }
            }
            result.add(colorize(line));
        }

        return result;
    }

    /**
     * 颜色代码转换
     */
    public String colorize(String text) {
        if (text == null) return "";
        return ChatColor.translateAlternateColorCodes('&', text);
    }

    /**
     * 判断是否启用调试模式
     */
    public boolean isDebugEnabled() {
        return configManager.isDebugEnabled();
    }

    /**
     * 重载语言配置
     * 由于LangManager直接使用ConfigManager提供的配置，无需额外操作
     */
    public void reload() {
        // ConfigManager已经处理了配置重载，这里无需额外操作
        // 遵循统一方法原则，避免重复处理配置加载
    }
}