package cn.i7mc.customfurnace;

import cn.i7mc.customfurnace.commands.FurnaceCommand;
import cn.i7mc.customfurnace.listeners.FurnaceListener;
import cn.i7mc.customfurnace.listeners.InventoryListener;
import cn.i7mc.customfurnace.managers.ConfigManager;
import cn.i7mc.customfurnace.managers.DataManager;
import cn.i7mc.customfurnace.managers.EconomyManager;
import cn.i7mc.customfurnace.managers.FurnaceManager;
import cn.i7mc.customfurnace.managers.HologramManager;
import cn.i7mc.customfurnace.managers.LangManager;
import cn.i7mc.customfurnace.utils.MessageUtil;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

public class CustomFurnace extends JavaPlugin {
    private ConfigManager configManager;
    private LangManager langManager;
    private FurnaceManager furnaceManager;
    private DataManager dataManager;
    private MessageUtil messageUtil;
    private EconomyManager economyManager;
    private HologramManager hologramManager;

    // 监听器实例
    private FurnaceListener furnaceListener;
    private InventoryListener inventoryListener;

    @Override
    public void onEnable() {
        // 初始化配置管理器
        configManager = new ConfigManager(this);

        // 初始化语言管理器
        langManager = new LangManager(configManager);

        // 初始化消息工具
        messageUtil = new MessageUtil(langManager);

        // 初始化熔炉管理器
        furnaceManager = new FurnaceManager(this, configManager, langManager);

        // 初始化数据管理器
        dataManager = new DataManager(this);

        // 初始化经济管理器
        economyManager = new EconomyManager(this);

        // 初始化全息显示管理器
        hologramManager = new HologramManager(this);

        // 注册事件监听器
        registerListeners();

        // 注册命令
        registerCommands();

        // 应用所有已保存的熔炉数据
        dataManager.applyAllFurnaces();
    }

    /**
     * 注册所有事件监听器
     */
    private void registerListeners() {
        PluginManager pm = getServer().getPluginManager();

        // 初始化并注册熔炉监听器
        furnaceListener = new FurnaceListener(this);
        pm.registerEvents(furnaceListener, this);

        // 初始化并注册物品栏监听器
        inventoryListener = new InventoryListener(this);
        pm.registerEvents(inventoryListener, this);
    }

    /**
     * 注册命令
     */
    private void registerCommands() {
        FurnaceCommand furnaceCommand = new FurnaceCommand(this);
        getCommand("furnace").setExecutor(furnaceCommand);
        getCommand("furnace").setTabCompleter(furnaceCommand);
    }

    @Override
    public void onDisable() {
        // 清理所有TextDisplay全息显示
        if (hologramManager != null) {
            hologramManager.clearAllHolograms();
        }

        // 保存数据
        if (dataManager != null) {
            dataManager.shutdown();
        }

        // 保存配置
        if (configManager != null) {
            configManager.saveConfigs();
        }

        // 输出停止信息
        if (langManager != null) {
            Bukkit.getConsoleSender().sendMessage(ChatColor.DARK_AQUA + "[CustomFurnace] " + ChatColor.RED + langManager.getRawMessage("plugin.disable"));
        }
    }

    /**
     * 获取配置管理器
     */
    public ConfigManager getConfigManager() {
        return configManager;
    }

    /**
     * 获取语言管理器
     */
    public LangManager getLangManager() {
        return langManager;
    }

    /**
     * 获取熔炉管理器
     */
    public FurnaceManager getFurnaceManager() {
        return furnaceManager;
    }

    /**
     * 获取数据管理器
     */
    public DataManager getDataManager() {
        return dataManager;
    }

    /**
     * 获取消息工具
     */
    public MessageUtil getMessageUtil() {
        return messageUtil;
    }

    /**
     * 获取经济管理器
     */
    public EconomyManager getEconomyManager() {
        return economyManager;
    }

    /**
     * 获取物品栏监听器
     */
    public InventoryListener getInventoryListener() {
        return inventoryListener;
    }

    /**
     * 获取全息显示管理器
     */
    public HologramManager getHologramManager() {
        return hologramManager;
    }
}