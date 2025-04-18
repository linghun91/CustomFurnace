package cn.i7mc.customfurnace.gui;

import cn.i7mc.customfurnace.CustomFurnace;
import cn.i7mc.customfurnace.managers.FurnaceManager;
import cn.i7mc.customfurnace.managers.LangManager;
import cn.i7mc.customfurnace.models.CustomFurnaceData;
import cn.i7mc.customfurnace.models.FurnaceLevel;
import cn.i7mc.customfurnace.utils.ItemBuilder;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

/**
 * 熔炉升级GUI实现
 */
public class FurnaceUpgradeGUI {
    private final CustomFurnace plugin;
    private final FurnaceManager furnaceManager;
    private final LangManager langManager;
    
    // GUI常量
    public static final int GUI_SIZE = 27; // 3行9列
    public static final int FURNACE_SLOT = 4; // 熔炉展示位置
    public static final int VAULT_UPGRADE_SLOT = 11; // 金币升级按钮位置
    public static final int POINTS_UPGRADE_SLOT = 15; // 点券升级按钮位置
    
    public FurnaceUpgradeGUI(CustomFurnace plugin) {
        this.plugin = plugin;
        this.furnaceManager = plugin.getFurnaceManager();
        this.langManager = plugin.getLangManager();
    }
    
    /**
     * 为玩家打开熔炉升级GUI
     */
    public void openGUI(Player player, ItemStack furnaceItem) {
        // 获取熔炉数据
        CustomFurnaceData data = CustomFurnaceData.fromItem(plugin, furnaceItem);
        if (data == null) {
            plugin.getMessageUtil().sendMessage(player, "messages.not_custom_furnace");
            return;
        }
        
        FurnaceLevel level = data.getLevel();
        String type = level.getType();
        int currentLevel = level.getLevel();
        
        // 创建GUI容器
        String title = langManager.colorize(langManager.getRawMessage("gui.title"));
        Inventory gui = Bukkit.createInventory(player, GUI_SIZE, title);
        
        // 填充GUI背景
        fillBackground(gui);
        
        // 放置熔炉展示
        gui.setItem(FURNACE_SLOT, furnaceItem);
        
        // 添加金币升级按钮
        boolean canUpgradeVault = furnaceManager.canUpgrade(type, currentLevel, FurnaceManager.PAYMENT_VAULT);
        ItemStack vaultButton = createUpgradeButton(type, currentLevel, canUpgradeVault, FurnaceManager.PAYMENT_VAULT);
        gui.setItem(VAULT_UPGRADE_SLOT, vaultButton);
        
        // 添加点券升级按钮
        boolean canUpgradePoints = furnaceManager.canUpgrade(type, currentLevel, FurnaceManager.PAYMENT_POINTS);
        ItemStack pointsButton = createUpgradeButton(type, currentLevel, canUpgradePoints, FurnaceManager.PAYMENT_POINTS);
        gui.setItem(POINTS_UPGRADE_SLOT, pointsButton);
        
        // 打开GUI
        player.openInventory(gui);
    }
    
    /**
     * 创建升级按钮
     */
    private ItemStack createUpgradeButton(String type, int currentLevel, boolean canUpgrade, String paymentType) {
        Material buttonMaterial;
        String buttonName;
        String costMessage = "";
        
        if (paymentType.equals(FurnaceManager.PAYMENT_VAULT)) {
            // 金币升级按钮
            buttonMaterial = canUpgrade ? Material.GOLD_BLOCK : Material.REDSTONE_BLOCK;
            buttonName = langManager.getRawMessage("gui.upgrade-vault");
            if (canUpgrade) {
                int cost = furnaceManager.getVaultUpgradeCost(type, currentLevel);
                costMessage = langManager.getRawMessage("gui.upgrade-cost-vault")
                        .replace("%cost%", String.valueOf(cost));
            }
        } else {
            // 点券升级按钮
            buttonMaterial = canUpgrade ? Material.DIAMOND_BLOCK : Material.REDSTONE_BLOCK;
            buttonName = langManager.getRawMessage("gui.upgrade-points");
            if (canUpgrade) {
                int cost = furnaceManager.getPointsUpgradeCost(type, currentLevel);
                costMessage = langManager.getRawMessage("gui.upgrade-cost-points")
                        .replace("%cost%", String.valueOf(cost));
            }
        }
        
        ItemBuilder builder = new ItemBuilder(buttonMaterial);
        
        if (canUpgrade) {
            builder.name(langManager.colorize(buttonName))
                   .lore(langManager.colorize(costMessage));
        } else {
            // 已达最高等级
            String maxLevel = langManager.getRawMessage("gui.max-level");
            builder.name(langManager.colorize(maxLevel));
        }
        
        return builder.build();
    }
    
    /**
     * 填充GUI背景
     */
    private void fillBackground(Inventory gui) {
        ItemStack filler = new ItemBuilder(Material.GRAY_STAINED_GLASS_PANE)
                .name(" ")
                .build();
        
        for (int i = 0; i < GUI_SIZE; i++) {
            // 跳过特殊位置
            if (i == FURNACE_SLOT || i == VAULT_UPGRADE_SLOT || i == POINTS_UPGRADE_SLOT) continue;
            gui.setItem(i, filler);
        }
    }
    
    /**
     * 处理升级点击操作
     * 
     * @return 升级是否成功
     */
    public boolean handleUpgradeClick(Player player, ItemStack furnaceItem, int slot) {
        // 获取熔炉数据
        CustomFurnaceData data = CustomFurnaceData.fromItem(plugin, furnaceItem);
        if (data == null) {
            return false;
        }
        
        FurnaceLevel level = data.getLevel();
        String type = level.getType();
        int currentLevel = level.getLevel();
        
        // 根据点击的按钮确定支付方式
        String paymentType;
        if (slot == VAULT_UPGRADE_SLOT) {
            paymentType = FurnaceManager.PAYMENT_VAULT;
        } else if (slot == POINTS_UPGRADE_SLOT) {
            paymentType = FurnaceManager.PAYMENT_POINTS;
        } else {
            return false;
        }
        
        // 检查是否可以升级
        if (!furnaceManager.canUpgrade(type, currentLevel, paymentType)) {
            plugin.getMessageUtil().sendMessage(player, "messages.max-level");
            return false;
        }
        
        boolean success = false;
        
        // 根据支付方式获取成本并扣费
        if (paymentType.equals(FurnaceManager.PAYMENT_VAULT)) {
            // 金币支付方式
            int cost = furnaceManager.getVaultUpgradeCost(type, currentLevel);
            
            // 检查玩家是否有足够的金币
            if (!plugin.getEconomyManager().isVaultEnabled()) {
                plugin.getMessageUtil().sendMessage(player, "messages.vault_not_found");
                return false;
            }
            
            if (!plugin.getEconomyManager().hasEnoughVaultBalance(player, cost)) {
                plugin.getMessageUtil().sendMessage(player, "messages.upgrade-fail-vault");
                return false;
            }
            
            // 扣除金币
            success = plugin.getEconomyManager().withdrawVault(player, cost);
            
            if (!success) {
                plugin.getMessageUtil().sendMessage(player, "messages.upgrade-fail-vault");
                return false;
            }
        } else {
            // 点券支付方式
            int cost = furnaceManager.getPointsUpgradeCost(type, currentLevel);
            
            // 检查玩家是否有足够的点券
            if (!plugin.getEconomyManager().isPointsEnabled()) {
                plugin.getMessageUtil().sendMessage(player, "messages.points_not_found");
                return false;
            }
            
            if (!plugin.getEconomyManager().hasEnoughPoints(player, cost)) {
                plugin.getMessageUtil().sendMessage(player, "messages.upgrade-fail-points");
                return false;
            }
            
            // 扣除点券
            success = plugin.getEconomyManager().withdrawPoints(player, cost);
            
            if (!success) {
                plugin.getMessageUtil().sendMessage(player, "messages.upgrade-fail-points");
                return false;
            }
        }
        
        // 创建新等级熔炉
        ItemStack newFurnace = furnaceManager.createFurnaceItem(type, currentLevel + 1, paymentType);
        
        // 替换玩家手中的物品
        player.getInventory().setItemInMainHand(newFurnace);
        
        // 发送成功消息
        plugin.getMessageUtil().sendMessage(player, "messages.upgrade-success");
        
        // 发送玩家剩余经济消息
        if (paymentType.equals(FurnaceManager.PAYMENT_VAULT)) {
            double balance = plugin.getEconomyManager().getVaultBalance(player);
            plugin.getMessageUtil().sendMessage(player, "messages.vault-balance-remaining", 
                    "balance", String.format("%.2f", balance));
        } else {
            int points = plugin.getEconomyManager().getPointsBalance(player);
            plugin.getMessageUtil().sendMessage(player, "messages.points-balance-remaining", 
                    "balance", String.valueOf(points));
        }
        
        return true;
    }
    
    /**
     * 更新GUI中的升级按钮
     * 
     * @param inventory 要更新的物品栏
     * @param furnaceItem 熔炉物品
     */
    public void updateUpgradeButtons(Inventory inventory, ItemStack furnaceItem) {
        CustomFurnaceData data = CustomFurnaceData.fromItem(plugin, furnaceItem);
        if (data == null) {
            return;
        }
        
        FurnaceLevel level = data.getLevel();
        String type = level.getType();
        int currentLevel = level.getLevel();
        
        // 更新金币升级按钮
        boolean canUpgradeVault = furnaceManager.canUpgrade(type, currentLevel, FurnaceManager.PAYMENT_VAULT);
        ItemStack vaultButton = createUpgradeButton(type, currentLevel, canUpgradeVault, FurnaceManager.PAYMENT_VAULT);
        inventory.setItem(VAULT_UPGRADE_SLOT, vaultButton);
        
        // 更新点券升级按钮
        boolean canUpgradePoints = furnaceManager.canUpgrade(type, currentLevel, FurnaceManager.PAYMENT_POINTS);
        ItemStack pointsButton = createUpgradeButton(type, currentLevel, canUpgradePoints, FurnaceManager.PAYMENT_POINTS);
        inventory.setItem(POINTS_UPGRADE_SLOT, pointsButton);
    }
} 