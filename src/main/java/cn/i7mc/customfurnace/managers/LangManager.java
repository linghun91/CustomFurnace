package cn.i7mc.customfurnace.managers;

import cn.i7mc.customfurnace.managers.ConfigManager;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

public class LangManager {
    private final ConfigManager configManager;
    private FileConfiguration messagesConfig;
    private FileConfiguration debugMessagesConfig;

    public LangManager(ConfigManager configManager) {
        this.configManager = configManager;
    }

    public String getMessage(String key, Object ... args) {
        String prefix = this.getRawMessage("prefix");
        Object message = this.getRawMessage(key);
        if (((String)message).isEmpty()) {
            return "";
        }
        message = prefix + (String)message;
        if (args != null && args.length > 0) {
            for (int i = 0; i < args.length; i += 2) {
                if (i + 1 >= args.length) continue;
                String placeholder = "%" + String.valueOf(args[i]) + "%";
                String value = String.valueOf(args[i + 1]);
                message = ((String)message).replace(placeholder, value);
            }
        }
        return this.colorize((String)message);
    }

    public String getRawMessage(String key) {
        FileConfiguration messagesConfig = this.configManager.getMessagesConfig();
        String message = messagesConfig.getString(key, "");
        return message;
    }

    public String getDebugMessage(String key, Object ... args) {
        FileConfiguration debugConfig = this.configManager.getDebugMessagesConfig();
        String message = debugConfig.getString(key, "");
        if (message.isEmpty()) {
            return "";
        }
        if (args != null && args.length > 0) {
            for (int i = 0; i < args.length; i += 2) {
                if (i + 1 >= args.length) continue;
                String placeholder = "%" + String.valueOf(args[i]) + "%";
                String value = String.valueOf(args[i + 1]);
                message = message.replace(placeholder, value);
            }
        }
        return this.colorize(message);
    }

    public List<String> getLore(String key, Object ... args) {
        FileConfiguration messagesConfig = this.configManager.getMessagesConfig();
        List<String> lore = messagesConfig.getStringList(key);
        ArrayList<String> result = new ArrayList<String>();
        if (lore.isEmpty()) {
            return result;
        }
        for (String line : lore) {
            if (args != null && args.length > 0) {
                for (int i = 0; i < args.length; i += 2) {
                    if (i + 1 >= args.length) continue;
                    String placeholder = "%" + String.valueOf(args[i]) + "%";
                    String value = String.valueOf(args[i + 1]);
                    line = line.replace(placeholder, value);
                }
            }
            result.add(this.colorize(line));
        }
        return result;
    }

    public String colorize(String text) {
        if (text == null) {
            return "";
        }
        return ChatColor.translateAlternateColorCodes((char)'&', (String)text);
    }

    public boolean isDebugEnabled() {
        return this.configManager.isDebugEnabled();
    }

    public void reload() {
        this.messagesConfig = YamlConfiguration.loadConfiguration((File)this.configManager.getMessagesFile());
        this.debugMessagesConfig = YamlConfiguration.loadConfiguration((File)this.configManager.getDebugMessagesFile());
    }
}

