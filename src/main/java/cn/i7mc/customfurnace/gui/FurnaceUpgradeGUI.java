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
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;

public class FurnaceUpgradeGUI {
    private final CustomFurnace plugin;
    private final FurnaceManager furnaceManager;
    private final LangManager langManager;
    public static final int GUI_SIZE = 27;
    public static final int FURNACE_SLOT = 4;
    public static final int VAULT_UPGRADE_SLOT = 11;
    public static final int POINTS_UPGRADE_SLOT = 15;

    public FurnaceUpgradeGUI(CustomFurnace plugin) {
        this.plugin = plugin;
        this.furnaceManager = plugin.getFurnaceManager();
        this.langManager = plugin.getLangManager();
    }

    public void openGUI(Player player, ItemStack furnaceItem) {
        CustomFurnaceData data = CustomFurnaceData.fromItem(this.plugin, furnaceItem);
        if (data == null) {
            this.plugin.getMessageUtil().sendMessage(player, "messages.not_custom_furnace", new Object[0]);
            return;
        }
        FurnaceLevel level = data.getLevel();
        String type = level.getType();
        int currentLevel = level.getLevel();
        String title = this.langManager.colorize(this.langManager.getRawMessage("gui.title"));
        Inventory gui = Bukkit.createInventory((InventoryHolder)player, (int)27, (String)title);
        this.fillBackground(gui);
        gui.setItem(4, furnaceItem);
        boolean canUpgradeVault = this.furnaceManager.canUpgrade(type, currentLevel, "vault");
        ItemStack vaultButton = this.createUpgradeButton(type, currentLevel, canUpgradeVault, "vault");
        gui.setItem(11, vaultButton);
        boolean canUpgradePoints = this.furnaceManager.canUpgrade(type, currentLevel, "points");
        ItemStack pointsButton = this.createUpgradeButton(type, currentLevel, canUpgradePoints, "points");
        gui.setItem(15, pointsButton);
        player.openInventory(gui);
    }

    private ItemStack createUpgradeButton(String type, int currentLevel, boolean canUpgrade, String paymentType) {
        String buttonName;
        Material buttonMaterial;
        String costMessage = "";
        if (paymentType.equals("vault")) {
            buttonMaterial = canUpgrade ? Material.GOLD_BLOCK : Material.REDSTONE_BLOCK;
            buttonName = this.langManager.getRawMessage("gui.upgrade-vault");
            if (canUpgrade) {
                int cost = this.furnaceManager.getVaultUpgradeCost(type, currentLevel);
                costMessage = this.langManager.getRawMessage("gui.upgrade-cost-vault").replace("%cost%", String.valueOf(cost));
            }
        } else {
            buttonMaterial = canUpgrade ? Material.DIAMOND_BLOCK : Material.REDSTONE_BLOCK;
            buttonName = this.langManager.getRawMessage("gui.upgrade-points");
            if (canUpgrade) {
                int cost = this.furnaceManager.getPointsUpgradeCost(type, currentLevel);
                costMessage = this.langManager.getRawMessage("gui.upgrade-cost-points").replace("%cost%", String.valueOf(cost));
            }
        }
        ItemBuilder builder = new ItemBuilder(buttonMaterial);
        if (canUpgrade) {
            builder.name(this.langManager.colorize(buttonName)).lore(this.langManager.colorize(costMessage));
        } else {
            String maxLevel = this.langManager.getRawMessage("gui.max-level");
            builder.name(this.langManager.colorize(maxLevel));
        }
        return builder.build();
    }

    private void fillBackground(Inventory gui) {
        ItemStack filler = new ItemBuilder(Material.GRAY_STAINED_GLASS_PANE).name(" ").build();
        for (int i = 0; i < 27; ++i) {
            if (i == 4 || i == 11 || i == 15) continue;
            gui.setItem(i, filler);
        }
    }

    public boolean handleUpgradeClick(Player player, ItemStack furnaceItem, int slot) {
        String paymentType;
        CustomFurnaceData data = CustomFurnaceData.fromItem(this.plugin, furnaceItem);
        if (data == null) {
            return false;
        }
        FurnaceLevel level = data.getLevel();
        String type = level.getType();
        int currentLevel = level.getLevel();
        if (slot == 11) {
            paymentType = "vault";
        } else if (slot == 15) {
            paymentType = "points";
        } else {
            return false;
        }
        if (!this.furnaceManager.canUpgrade(type, currentLevel, paymentType)) {
            this.plugin.getMessageUtil().sendMessage(player, "messages.max-level", new Object[0]);
            return false;
        }
        boolean success = false;
        if (paymentType.equals("vault")) {
            double cost = this.furnaceManager.getVaultUpgradeCost(type, currentLevel);
            if (!this.plugin.getEconomyManager().isVaultEnabled()) {
                this.plugin.getMessageUtil().sendMessage(player, "messages.vault_not_found", new Object[0]);
                return false;
            }
            if (!this.plugin.getEconomyManager().hasEnoughVaultBalance(player, cost)) {
                this.plugin.getMessageUtil().sendMessage(player, "messages.upgrade-fail-vault", new Object[0]);
                return false;
            }
            success = this.plugin.getEconomyManager().withdrawVault(player, cost);
            if (!success) {
                this.plugin.getMessageUtil().sendMessage(player, "messages.upgrade-fail-vault", new Object[0]);
                return false;
            }
        } else {
            int cost = this.furnaceManager.getPointsUpgradeCost(type, currentLevel);
            if (!this.plugin.getEconomyManager().isPointsEnabled()) {
                this.plugin.getMessageUtil().sendMessage(player, "messages.points_not_found", new Object[0]);
                return false;
            }
            if (!this.plugin.getEconomyManager().hasEnoughPoints(player, cost)) {
                this.plugin.getMessageUtil().sendMessage(player, "messages.upgrade-fail-points", new Object[0]);
                return false;
            }
            success = this.plugin.getEconomyManager().withdrawPoints(player, cost);
            if (!success) {
                this.plugin.getMessageUtil().sendMessage(player, "messages.upgrade-fail-points", new Object[0]);
                return false;
            }
        }
        ItemStack newFurnace = this.furnaceManager.createFurnaceItem(type, currentLevel + 1, paymentType);
        player.getInventory().setItemInMainHand(newFurnace);
        this.plugin.getMessageUtil().sendMessage(player, "messages.upgrade-success", new Object[0]);
        if (paymentType.equals("vault")) {
            double balance = this.plugin.getEconomyManager().getVaultBalance(player);
            this.plugin.getMessageUtil().sendMessage(player, "messages.vault-balance-remaining", "balance", String.format("%.2f", balance));
        } else {
            int points = this.plugin.getEconomyManager().getPointsBalance(player);
            this.plugin.getMessageUtil().sendMessage(player, "messages.points-balance-remaining", "balance", String.valueOf(points));
        }
        return true;
    }

    public void updateUpgradeButtons(Inventory inventory, ItemStack furnaceItem) {
        CustomFurnaceData data = CustomFurnaceData.fromItem(this.plugin, furnaceItem);
        if (data == null) {
            return;
        }
        FurnaceLevel level = data.getLevel();
        String type = level.getType();
        int currentLevel = level.getLevel();
        boolean canUpgradeVault = this.furnaceManager.canUpgrade(type, currentLevel, "vault");
        ItemStack vaultButton = this.createUpgradeButton(type, currentLevel, canUpgradeVault, "vault");
        inventory.setItem(11, vaultButton);
        boolean canUpgradePoints = this.furnaceManager.canUpgrade(type, currentLevel, "points");
        ItemStack pointsButton = this.createUpgradeButton(type, currentLevel, canUpgradePoints, "points");
        inventory.setItem(15, pointsButton);
    }
}

