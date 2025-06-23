package cn.i7mc.customfurnace.listeners;

import cn.i7mc.customfurnace.CustomFurnace;
import cn.i7mc.customfurnace.managers.DataManager;
import cn.i7mc.customfurnace.managers.FurnaceManager;
import cn.i7mc.customfurnace.models.CustomFurnaceData;
import cn.i7mc.customfurnace.models.FurnaceLevel;
import java.util.UUID;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Furnace;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.entity.TextDisplay;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.inventory.FurnaceBurnEvent;
import org.bukkit.event.inventory.FurnaceSmeltEvent;
import org.bukkit.event.inventory.FurnaceStartSmeltEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.inventory.FurnaceInventory;
import org.bukkit.inventory.ItemStack;

public class FurnaceListener
implements Listener {
    private final CustomFurnace plugin;
    private final FurnaceManager furnaceManager;
    private final DataManager dataManager;

    public FurnaceListener(CustomFurnace plugin) {
        this.plugin = plugin;
        this.furnaceManager = plugin.getFurnaceManager();
        this.dataManager = plugin.getDataManager();
    }

    @EventHandler(priority=EventPriority.MONITOR, ignoreCancelled=true)
    public void onFurnacePlaced(BlockPlaceEvent event) {
        ItemStack item = event.getItemInHand();
        Block block = event.getBlock();
        if (!this.isFurnaceType(block.getType())) {
            return;
        }
        CustomFurnaceData data = CustomFurnaceData.fromItem(this.plugin, item);
        if (data == null) {
            return;
        }
        BlockState state = block.getState();
        if (state instanceof Furnace) {
            UUID hologramUUID;
            data.applyToBlockState(state);
            this.dataManager.storeFurnaceData(block.getLocation(), data);
            Furnace furnace = (Furnace)state;
            furnace.setCookTimeTotal(data.getLevel().getCookingTime());
            furnace.update();
            if (this.plugin.getConfigManager().isArmorstandHologramEnabled() && (hologramUUID = this.plugin.getTextDisplayUtil().createOrUpdateFurnaceDisplay(block, data, 0, 0)) != null) {
                data.setHologramUUID(hologramUUID);
                this.dataManager.storeFurnaceData(block.getLocation(), data);
            }
            if (this.plugin.getConfigManager().isDroppedItemHologramEnabled()) {
                // empty if block
            }
            Player player = event.getPlayer();
            this.plugin.getMessageUtil().sendDebug(player, "furnace.placed", "type", data.getLevel().getType(), "level", data.getLevel().getLevel(), "uuid", data.getUuid().toString());
        }
    }

    @EventHandler(priority=EventPriority.HIGH)
    public void onFurnaceBroken(BlockBreakEvent event) {
        Block block = event.getBlock();
        if (!this.isFurnaceType(block.getType())) {
            return;
        }
        BlockState state = block.getState();
        CustomFurnaceData data = CustomFurnaceData.fromBlockState(this.plugin, state);
        if (data == null) {
            return;
        }
        block.getWorld().getNearbyEntities(block.getLocation().add(0.5, 0.9, 0.5), 1.0, 1.0, 1.0).stream().filter(entity -> entity instanceof TextDisplay).map(entity -> (TextDisplay)entity).filter(textDisplay -> {
            if (data.getHologramUUID() != null && textDisplay.getUniqueId().equals(data.getHologramUUID())) {
                return true;
            }
            if (Math.abs(textDisplay.getLocation().getX() - (block.getLocation().getX() + 0.5)) < 0.01 && Math.abs(textDisplay.getLocation().getY() - (block.getLocation().getY() + 0.8)) < 0.01 && Math.abs(textDisplay.getLocation().getZ() - (block.getLocation().getZ() + 0.5)) < 0.01) {
                return true;
            }
            String customName = this.plugin.getConfigManager().getFurnacesConfig().getString("furnaces." + data.getLevel().getType() + ".display_name." + data.getLevel().getLevel());
            if (customName == null) {
                customName = this.plugin.getLangManager().getRawMessage("furnace.name");
            }
            String expectedDisplay = this.plugin.getLangManager().getRawMessage("furnace.hologram_hud").replace("%display%", customName).replace("%level%", String.valueOf(data.getLevel().getLevel())).replace("%payment_type%", data.getPaymentType().equals("vault") ? "\u91d1\u5e01" : "\u70b9\u5238").replace("%speed%", String.valueOf(data.getLevel().getCookingTime()));
            return textDisplay.getText() != null && this.plugin.getLangManager().colorize(expectedDisplay).equals(textDisplay.getText());
        }).forEach(textDisplay -> {
            textDisplay.remove();
            this.plugin.getMessageUtil().logDebug("furnace.hologram_removed", "location", String.format("%.2f,%.2f,%.2f", textDisplay.getLocation().getX(), textDisplay.getLocation().getY(), textDisplay.getLocation().getZ()));
        });
        this.dataManager.removeFurnaceData(block.getLocation());
        if (state instanceof Furnace) {
            ItemStack result;
            ItemStack smelting;
            Furnace furnace = (Furnace)state;
            FurnaceInventory inventory = furnace.getInventory();
            ItemStack fuel = inventory.getFuel();
            if (fuel != null && !fuel.getType().isAir()) {
                block.getWorld().dropItemNaturally(block.getLocation(), fuel);
            }
            if ((smelting = inventory.getSmelting()) != null && !smelting.getType().isAir()) {
                block.getWorld().dropItemNaturally(block.getLocation(), smelting);
            }
            if ((result = inventory.getResult()) != null && !result.getType().isAir()) {
                block.getWorld().dropItemNaturally(block.getLocation(), result);
            }
        }
        event.setDropItems(false);
        FurnaceLevel level = data.getLevel();
        String type = level.getType();
        int levelNum = level.getLevel();
        ItemStack customFurnace = this.furnaceManager.createFurnaceItem(type, levelNum, "vault");
        if (customFurnace == null) {
            customFurnace = this.furnaceManager.createFurnaceItem(type, levelNum, "points");
        }
        if (customFurnace == null && levelNum > 1) {
            customFurnace = this.furnaceManager.createFurnaceItem(type, 1, "vault");
        }
        if (customFurnace != null) {
            String customName = this.plugin.getConfigManager().getFurnacesConfig().getString("furnaces." + type + ".display_name." + levelNum);
            if (customName == null) {
                customName = this.plugin.getLangManager().getRawMessage("furnace.name");
            }
            String hoverDisplay = this.plugin.getLangManager().getRawMessage("furnace.dropped_item_hud").replace("%display%", customName).replace("%level%", String.valueOf(levelNum)).replace("%payment_type%", data.getPaymentType().equals("vault") ? "\u91d1\u5e01" : "\u70b9\u5238").replace("%speed%", String.valueOf(data.getLevel().getCookingTime()));
            Item droppedItem = block.getWorld().dropItemNaturally(block.getLocation(), customFurnace);
            droppedItem.setCustomName(this.plugin.getLangManager().colorize(hoverDisplay));
            droppedItem.setCustomNameVisible(true);
        }
        Player player = event.getPlayer();
        this.plugin.getMessageUtil().sendDebug(player, "furnace.broken", "type", type, "level", levelNum, "uuid", data.getUuid().toString());
    }

    @EventHandler(priority=EventPriority.MONITOR, ignoreCancelled=true)
    public void onFurnaceBurn(FurnaceBurnEvent event) {
        Block block = event.getBlock();
        BlockState state = block.getState();
        CustomFurnaceData data = CustomFurnaceData.fromBlockState(this.plugin, state);
        if (data == null) {
            return;
        }
        FurnaceLevel level = data.getLevel();
        int burnTime = event.getBurnTime();
        double multiplier = 1.0 + (double)(level.getLevel() - 1) * 0.2;
        event.setBurnTime((int)((double)burnTime * multiplier));
        this.plugin.getMessageUtil().logDebug("furnace.burn", new Object[0]);
    }

    @EventHandler(priority=EventPriority.MONITOR, ignoreCancelled=true)
    public void onFurnaceStartSmelt(FurnaceStartSmeltEvent event) {
        Block block = event.getBlock();
        BlockState state = block.getState();
        CustomFurnaceData data = CustomFurnaceData.fromBlockState(this.plugin, state);
        if (data == null) {
            return;
        }
        FurnaceLevel level = data.getLevel();
        event.setTotalCookTime(level.getCookingTime());
        if (state instanceof Furnace) {
            Furnace furnace = (Furnace)state;
            furnace.setCookTimeTotal(level.getCookingTime());
            furnace.update();
        }
        this.plugin.getMessageUtil().logDebug("furnace.start_smelt", "speed", level.getCookingTime());
    }

    @EventHandler(priority=EventPriority.MONITOR, ignoreCancelled=true)
    public void onFurnaceSmelt(FurnaceSmeltEvent event) {
        Block block = event.getBlock();
        BlockState state = block.getState();
        CustomFurnaceData data = CustomFurnaceData.fromBlockState(this.plugin, state);
        if (data == null) {
            return;
        }
        this.plugin.getMessageUtil().logDebug("furnace.smelt_complete", new Object[0]);
    }

    @EventHandler(priority=EventPriority.MONITOR, ignoreCancelled=true)
    public void onPlayerDropItem(PlayerDropItemEvent event) {
        Item droppedItem = event.getItemDrop();
        ItemStack item = droppedItem.getItemStack();
        if (!this.isFurnaceType(item.getType())) {
            return;
        }
        CustomFurnaceData data = CustomFurnaceData.fromItem(this.plugin, item);
        if (data == null) {
            return;
        }
        if (this.plugin.getConfigManager().isDroppedItemHologramEnabled()) {
            String customName = this.plugin.getConfigManager().getFurnacesConfig().getString("furnaces." + data.getLevel().getType() + ".display_name." + data.getLevel().getLevel());
            if (customName == null) {
                customName = this.plugin.getLangManager().getRawMessage("furnace.name");
            }
            String hoverDisplay = this.plugin.getLangManager().getRawMessage("furnace.dropped_item_hud").replace("%display%", customName).replace("%level%", String.valueOf(data.getLevel().getLevel())).replace("%payment_type%", data.getPaymentType().equals("vault") ? "\u91d1\u5e01" : "\u70b9\u5238").replace("%speed%", String.valueOf(data.getLevel().getCookingTime()));
            droppedItem.setCustomName(this.plugin.getLangManager().colorize(hoverDisplay));
            droppedItem.setCustomNameVisible(true);
        } else {
            droppedItem.setCustomNameVisible(false);
        }
    }

    private boolean isFurnaceType(Material material) {
        return material == Material.FURNACE || material == Material.BLAST_FURNACE || material == Material.SMOKER;
    }
}

