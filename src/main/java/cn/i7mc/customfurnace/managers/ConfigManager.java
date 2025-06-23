package cn.i7mc.customfurnace.managers;

import cn.i7mc.customfurnace.CustomFurnace;
import java.io.File;
import java.io.IOException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

public class ConfigManager {
    private final CustomFurnace plugin;
    private final File configFile;
    private final File messagesFile;
    private final File debugMessagesFile;
    private final File furnacesFile;
    private FileConfiguration config;
    private FileConfiguration messagesConfig;
    private FileConfiguration debugMessagesConfig;
    private FileConfiguration furnacesConfig;

    public ConfigManager(CustomFurnace plugin) {
        this.plugin = plugin;
        this.configFile = new File(plugin.getDataFolder(), "config.yml");
        this.messagesFile = new File(plugin.getDataFolder(), "message.yml");
        this.debugMessagesFile = new File(plugin.getDataFolder(), "debugmessage.yml");
        this.furnacesFile = new File(plugin.getDataFolder(), "furnaces.yml");
        this.saveDefaultConfigs();
        this.loadConfigs();
    }

    private void saveDefaultConfigs() {
        this.plugin.saveDefaultConfig();
        if (!this.messagesFile.exists()) {
            this.plugin.saveResource("message.yml", false);
        }
        if (!this.furnacesFile.exists()) {
            this.plugin.saveResource("furnaces.yml", false);
        }
        if (!this.debugMessagesFile.exists()) {
            this.plugin.saveResource("debugmessage.yml", false);
        }
    }

    private void loadConfigs() {
        this.config = this.plugin.getConfig();
        this.messagesConfig = YamlConfiguration.loadConfiguration((File)this.messagesFile);
        this.debugMessagesConfig = YamlConfiguration.loadConfiguration((File)this.debugMessagesFile);
        this.furnacesConfig = YamlConfiguration.loadConfiguration((File)this.furnacesFile);
    }

    public FileConfiguration getConfig() {
        return this.config;
    }

    public FileConfiguration getMessagesConfig() {
        return this.messagesConfig;
    }

    public FileConfiguration getDebugMessagesConfig() {
        return this.debugMessagesConfig;
    }

    public FileConfiguration getFurnacesConfig() {
        return this.furnacesConfig;
    }

    public File getMessagesFile() {
        return this.messagesFile;
    }

    public File getDebugMessagesFile() {
        return this.debugMessagesFile;
    }

    public void reloadConfigs() {
        this.plugin.reloadConfig();
        this.config = this.plugin.getConfig();
        this.messagesConfig = YamlConfiguration.loadConfiguration((File)this.messagesFile);
        this.debugMessagesConfig = YamlConfiguration.loadConfiguration((File)this.debugMessagesFile);
        this.furnacesConfig = YamlConfiguration.loadConfiguration((File)this.furnacesFile);
        this.plugin.getDataManager().applyAllFurnaces();
        this.plugin.getLangManager().reload();
    }

    public void saveConfigs() {
        try {
            this.config.save(this.configFile);
            this.messagesConfig.save(this.messagesFile);
            this.debugMessagesConfig.save(this.debugMessagesFile);
            this.furnacesConfig.save(this.furnacesFile);
        } catch (IOException e) {
            this.plugin.getLogger().severe("\u4fdd\u5b58\u914d\u7f6e\u6587\u4ef6\u5931\u8d25: " + e.getMessage());
        }
    }

    public boolean isDebugEnabled() {
        return this.config.getBoolean("debug", false);
    }

    public boolean isArmorstandHologramEnabled() {
        return this.config.getBoolean("display.text-display-hologram", true);
    }

    public boolean isDroppedItemHologramEnabled() {
        return this.config.getBoolean("display.dropped-item-hologram", true);
    }

    public String getTextDisplayAlignment() {
        return this.config.getString("display.text-display-settings.alignment", "CENTER");
    }

    public boolean isTextDisplayShadowed() {
        return this.config.getBoolean("display.text-display-settings.shadowed", true);
    }

    public boolean isTextDisplaySeeThrough() {
        return this.config.getBoolean("display.text-display-settings.see-through", true);
    }

    public String getTextDisplayBillboard() {
        return this.config.getString("display.text-display-settings.billboard", "CENTER");
    }

    public int[] getTextDisplayBackgroundColor() {
        String colorStr = this.config.getString("display.text-display-settings.background-color", "0,0,0,0");
        String[] parts = colorStr.split(",");
        int[] color = new int[4];
        try {
            for (int i = 0; i < 4 && i < parts.length; ++i) {
                color[i] = Integer.parseInt(parts[i].trim());
                color[i] = Math.max(0, Math.min(255, color[i]));
            }
        } catch (NumberFormatException e) {
            this.plugin.getLogger().warning("\u80cc\u666f\u989c\u8272\u683c\u5f0f\u9519\u8bef\uff0c\u4f7f\u7528\u9ed8\u8ba4\u503c: " + e.getMessage());
            return new int[]{0, 0, 0, 0};
        }
        return color;
    }

    public byte getTextDisplayOpacity() {
        int opacity = this.config.getInt("display.text-display-settings.text-opacity", -1);
        return (byte)Math.max(-1, Math.min(255, opacity));
    }

    public int getTextDisplayLineWidth() {
        return this.config.getInt("display.text-display-settings.line-width", 200);
    }

    public double getTextDisplayYOffset() {
        return this.config.getDouble("display.text-display-settings.y-offset", 0.8);
    }

    public boolean isProgressBarEnabled() {
        return this.config.getBoolean("display.progress-bar.enabled", true);
    }

    public int getProgressBarUpdateInterval() {
        return this.config.getInt("display.progress-bar.update-interval", 10);
    }

    public int getProgressBarLength() {
        return this.config.getInt("display.progress-bar.length", 15);
    }

    public String getProgressBarCharacter() {
        return this.config.getString("display.progress-bar.character", "I");
    }

    public String getProgressBarStartChar() {
        return this.config.getString("display.progress-bar.start-char", "[");
    }

    public String getProgressBarEndChar() {
        return this.config.getString("display.progress-bar.end-char", "]");
    }

    public String getProgressBarBorderColor() {
        return this.config.getString("display.progress-bar.border-color", "&8");
    }

    public String[] getProgressBarFilledColors() {
        if (this.config.isList("display.progress-bar.filled-colors")) {
            return this.config.getStringList("display.progress-bar.filled-colors").toArray(new String[0]);
        }
        return new String[]{"&a", "&e", "&c"};
    }

    public String getProgressBarEmptyColor() {
        return this.config.getString("display.progress-bar.empty-color", "&7");
    }
}

