package cn.i7mc.customfurnace.listeners;

import cn.i7mc.customfurnace.CustomFurnace;
import cn.i7mc.customfurnace.gui.FurnaceUpgradeGUI;
import cn.i7mc.customfurnace.models.CustomFurnaceData;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.FurnaceExtractEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.FurnaceInventory;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class InventoryListener
implements Listener {
    private final CustomFurnace plugin;
    private FurnaceUpgradeGUI upgradeGUI;

    public InventoryListener(CustomFurnace plugin) {
        this.plugin = plugin;
        this.upgradeGUI = new FurnaceUpgradeGUI(plugin);
    }

    @EventHandler(priority=EventPriority.MONITOR, ignoreCancelled=true)
    public void onFurnaceExtract(FurnaceExtractEvent event) {
        Player player = event.getPlayer();
        Block block = event.getBlock();
        BlockState state = block.getState();
        CustomFurnaceData data = CustomFurnaceData.fromBlockState(this.plugin, state);
        if (data == null) {
            return;
        }
        this.plugin.getMessageUtil().sendDebug(player, "furnace.extract", new Object[0]);
    }

    @EventHandler(priority=EventPriority.MONITOR, ignoreCancelled=true)
    public void onInventoryClick(InventoryClickEvent event) {
        Inventory inventory = event.getInventory();
        if (!(inventory instanceof FurnaceInventory) || inventory.getType() != InventoryType.FURNACE && inventory.getType() != InventoryType.BLAST_FURNACE && inventory.getType() != InventoryType.SMOKER) {
            return;
        }
        if (event.getWhoClicked() instanceof Player) {
            Player player = (Player)event.getWhoClicked();
            this.plugin.getMessageUtil().sendDebug(player, "furnace.inventory_interact", new Object[0]);
        }
    }

    @EventHandler(priority=EventPriority.HIGH)
    public void onUpgradeGUIClick(InventoryClickEvent event) {
        boolean success;
        ItemStack furnace;
        String guiTitle;
        if (!(event.getWhoClicked() instanceof Player)) {
            return;
        }
        Player player = (Player)event.getWhoClicked();
        String title = event.getView().getTitle();
        if (!title.equals(guiTitle = this.plugin.getLangManager().colorize(this.plugin.getLangManager().getRawMessage("gui.title")))) {
            return;
        }
        event.setCancelled(true);
        int slot = event.getRawSlot();
        if ((slot == 11 || slot == 15) && (furnace = event.getInventory().getItem(4)) != null && (success = this.upgradeGUI.handleUpgradeClick(player, furnace, slot))) {
            ItemStack newFurnace = player.getInventory().getItemInMainHand();
            event.getInventory().setItem(4, newFurnace);
            this.upgradeGUI.updateUpgradeButtons(event.getInventory(), newFurnace);
        }
    }

    public void openUpgradeGUI(Player player, ItemStack furnaceItem) {
        this.upgradeGUI.openGUI(player, furnaceItem);
    }
}

