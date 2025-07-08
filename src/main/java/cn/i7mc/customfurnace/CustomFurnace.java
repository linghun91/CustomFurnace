package cn.i7mc.customfurnace;

import cn.i7mc.customfurnace.commands.FurnaceCommand;
import cn.i7mc.customfurnace.listeners.ExplosionListener;
import cn.i7mc.customfurnace.listeners.FurnaceListener;
import cn.i7mc.customfurnace.listeners.InventoryListener;
import cn.i7mc.customfurnace.managers.ConfigManager;
import cn.i7mc.customfurnace.managers.DataManager;
import cn.i7mc.customfurnace.managers.EconomyManager;
import cn.i7mc.customfurnace.managers.FurnaceManager;
import cn.i7mc.customfurnace.managers.LangManager;
import cn.i7mc.customfurnace.utils.MessageUtil;
import cn.i7mc.customfurnace.utils.TextDisplayUtil;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Entity;
import org.bukkit.entity.TextDisplay;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

public class CustomFurnace
extends JavaPlugin {
    private ConfigManager configManager;
    private LangManager langManager;
    private FurnaceManager furnaceManager;
    private DataManager dataManager;
    private MessageUtil messageUtil;
    private EconomyManager economyManager;
    private TextDisplayUtil textDisplayUtil;
    private FurnaceListener furnaceListener;
    private InventoryListener inventoryListener;

    public void onEnable() {
        this.configManager = new ConfigManager(this);
        this.langManager = new LangManager(this.configManager);
        this.messageUtil = new MessageUtil(this.langManager);
        this.textDisplayUtil = new TextDisplayUtil(this);
        this.furnaceManager = new FurnaceManager(this, this.configManager, this.langManager);
        this.dataManager = new DataManager(this);
        this.economyManager = new EconomyManager(this);
        this.registerListeners();
        this.registerCommands();
        this.dataManager.applyAllFurnaces();
        Bukkit.getConsoleSender().sendMessage(String.valueOf(ChatColor.DARK_AQUA) + "[CustomFurnace] " + String.valueOf(ChatColor.GREEN) + this.langManager.getRawMessage("plugin.enable"));
        Bukkit.getConsoleSender().sendMessage(String.valueOf(ChatColor.DARK_AQUA) + "[CustomFurnace] " + String.valueOf(ChatColor.AQUA) + "QQ2680517068\u4e13\u5c5e\u7684\u5b9a\u5236\u63d2\u4ef6");
        if (this.configManager.isDebugEnabled()) {
            this.messageUtil.logDebug("plugin.debug_mode", new Object[0]);
        }
        if (this.configManager.isProgressBarEnabled()) {
            int updateInterval = this.configManager.getProgressBarUpdateInterval();
            this.getServer().getScheduler().runTaskTimer((Plugin)this, () -> this.dataManager.updateFurnaceProgressBars(), 20L, (long)updateInterval);
            if (this.configManager.isDebugEnabled()) {
                this.getLogger().info("\u8fdb\u5ea6\u6761\u66f4\u65b0\u4efb\u52a1\u5df2\u542f\u52a8\uff0c\u95f4\u9694\uff1a" + updateInterval + " ticks");
            }
        }
    }

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
        this.getCommand("furnace").setExecutor((CommandExecutor)furnaceCommand);
        this.getCommand("furnace").setTabCompleter((TabCompleter)furnaceCommand);
        if (this.configManager.isDebugEnabled()) {
            this.messageUtil.logDebug("plugin.commands_registered", new Object[0]);
        }
    }

    public void onDisable() {
        this.getServer().getScheduler().cancelTasks((Plugin)this);
        if (this.dataManager != null && this.configManager.isArmorstandHologramEnabled()) {
            this.getServer().getWorlds().forEach(world -> world.getEntities().stream().filter(entity -> entity instanceof TextDisplay).map(entity -> (TextDisplay)entity).filter(textDisplay -> textDisplay.getText() != null).forEach(Entity::remove));
        }
        if (this.dataManager != null) {
            this.dataManager.shutdown();
        }
        if (this.configManager != null) {
            this.configManager.saveConfigs();
        }
        if (this.langManager != null) {
            Bukkit.getConsoleSender().sendMessage(String.valueOf(ChatColor.DARK_AQUA) + "[CustomFurnace] " + String.valueOf(ChatColor.RED) + this.langManager.getRawMessage("plugin.disable"));
        }
    }

    public ConfigManager getConfigManager() {
        return this.configManager;
    }

    public LangManager getLangManager() {
        return this.langManager;
    }

    public FurnaceManager getFurnaceManager() {
        return this.furnaceManager;
    }

    public DataManager getDataManager() {
        return this.dataManager;
    }

    public MessageUtil getMessageUtil() {
        return this.messageUtil;
    }

    public EconomyManager getEconomyManager() {
        return this.economyManager;
    }

    public InventoryListener getInventoryListener() {
        return this.inventoryListener;
    }

    public TextDisplayUtil getTextDisplayUtil() {
        return this.textDisplayUtil;
    }
}

