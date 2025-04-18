package cn.i7mc.customfurnace;

import cn.i7mc.customfurnace.commands.FurnaceCommand;
import cn.i7mc.customfurnace.listeners.FurnaceListener;
import cn.i7mc.customfurnace.listeners.InventoryListener;
import cn.i7mc.customfurnace.managers.ConfigManager;
import cn.i7mc.customfurnace.managers.DataManager;
import cn.i7mc.customfurnace.managers.EconomyManager;
import cn.i7mc.customfurnace.managers.FurnaceManager;
import cn.i7mc.customfurnace.managers.LangManager;
import cn.i7mc.customfurnace.utils.MessageUtil;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.entity.ArmorStand;

public class CustomFurnace extends JavaPlugin {
    private ConfigManager configManager;
    private LangManager langManager;
    private FurnaceManager furnaceManager;
    private DataManager dataManager;
    private MessageUtil messageUtil;
    private EconomyManager economyManager;
    
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
        
        // 注册事件监听器
        registerListeners();
        
        // 注册命令
        registerCommands();
        
        // 应用所有已保存的熔炉数据
        dataManager.applyAllFurnaces();
        
        // 输出启动信息
        Bukkit.getConsoleSender().sendMessage(ChatColor.DARK_AQUA + "[CustomFurnace] " + ChatColor.GREEN + langManager.getRawMessage("plugin.enable"));
        
        // 输出彩色启动信息
        Bukkit.getConsoleSender().sendMessage(ChatColor.DARK_AQUA + "[CustomFurnace] " + ChatColor.AQUA + "QQ2680517068专属的定制插件");
        
        // 如果调试模式开启，输出调试信息
        if (configManager.isDebugEnabled()) {
            messageUtil.logDebug("plugin.debug_mode");
        }
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
        
        // 输出调试信息
        if (configManager.isDebugEnabled()) {
            messageUtil.logDebug("plugin.listeners_registered");
        }
    }
    
    /**
     * 注册命令
     */
    private void registerCommands() {
        FurnaceCommand furnaceCommand = new FurnaceCommand(this);
        getCommand("furnace").setExecutor(furnaceCommand);
        getCommand("furnace").setTabCompleter(furnaceCommand);
        
        // 输出调试信息
        if (configManager.isDebugEnabled()) {
            messageUtil.logDebug("plugin.commands_registered");
        }
    }
    
    @Override
    public void onDisable() {
        // 清理所有悬浮文本
        if (dataManager != null && configManager.isArmorstandHologramEnabled()) {
            getServer().getWorlds().forEach(world -> {
                world.getEntities().stream()
                    .filter(entity -> entity instanceof ArmorStand)
                    .map(entity -> (ArmorStand) entity)
                    .filter(armorStand -> !armorStand.isVisible() && armorStand.isCustomNameVisible())
                    .forEach(ArmorStand::remove);
            });
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
} 