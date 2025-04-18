package cn.i7mc.customfurnace.listeners;

import cn.i7mc.customfurnace.CustomFurnace;
import cn.i7mc.customfurnace.managers.DataManager;
import cn.i7mc.customfurnace.managers.FurnaceManager;
import cn.i7mc.customfurnace.models.CustomFurnaceData;
import cn.i7mc.customfurnace.models.FurnaceLevel;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Furnace;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.inventory.FurnaceBurnEvent;
import org.bukkit.event.inventory.FurnaceSmeltEvent;
import org.bukkit.event.inventory.FurnaceStartSmeltEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.FurnaceInventory;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.EntityType;

/**
 * 监听所有熔炉相关事件
 */
public class FurnaceListener implements Listener {
    private final CustomFurnace plugin;
    private final FurnaceManager furnaceManager;
    private final DataManager dataManager;
    
    public FurnaceListener(CustomFurnace plugin) {
        this.plugin = plugin;
        this.furnaceManager = plugin.getFurnaceManager();
        this.dataManager = plugin.getDataManager();
    }
    
    /**
     * 当玩家放置熔炉时，检查是否为自定义熔炉
     */
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onFurnacePlaced(BlockPlaceEvent event) {
        ItemStack item = event.getItemInHand();
        Block block = event.getBlock();
        
        // 检查是否为熔炉类型
        if (!isFurnaceType(block.getType())) {
            return;
        }
        
        // 尝试从物品获取自定义熔炉数据
        CustomFurnaceData data = CustomFurnaceData.fromItem(plugin, item);
        if (data == null) {
            return;
        }
        
        BlockState state = block.getState();
        if (state instanceof Furnace) {
            // 应用自定义熔炉数据到方块
            data.applyToBlockState(state);
            
            // 保存熔炉数据到持久化存储
            dataManager.storeFurnaceData(block.getLocation(), data);
            
            // 强制更新烹饪时间
            Furnace furnace = (Furnace) state;
            furnace.setCookTimeTotal(data.getLevel().getCookingTime());
            furnace.update();
            
            // 检查是否启用盔甲架全息信息显示
            if (plugin.getConfigManager().isArmorstandHologramEnabled()) {
                // 获取自定义名字
                String customName = plugin.getConfigManager().getFurnacesConfig().getString("furnaces." + data.getLevel().getType() + ".display_name." + data.getLevel().getLevel());
                if (customName == null) {
                    customName = plugin.getLangManager().getRawMessage("furnace.name");
                }
                
                // 获取悬浮显示格式
                String hoverDisplay = plugin.getLangManager().getRawMessage("furnace.hover_display")
                        .replace("%display%", customName)
                        .replace("%level%", String.valueOf(data.getLevel().getLevel()))
                        .replace("%payment_type%", data.getPaymentType().equals(FurnaceManager.PAYMENT_VAULT) ? "金币" : "点券")
                        .replace("%speed%", String.valueOf(data.getLevel().getCookingTime()));
                
                // 创建ArmorStand显示悬浮文本
                ArmorStand hologram = (ArmorStand) block.getWorld().spawnEntity(block.getLocation().add(0.5, 0.9, 0.5), EntityType.ARMOR_STAND);
                hologram.setVisible(false);
                hologram.setGravity(false);
                hologram.setCanPickupItems(false);
                hologram.setCustomName(plugin.getLangManager().colorize(hoverDisplay));
                hologram.setCustomNameVisible(true);
                hologram.setMarker(true);
                hologram.setSmall(true);
                
                // 将ArmorStand的UUID保存到熔炉数据中
                data.setHologramUUID(hologram.getUniqueId());
            }
            
            // 检查是否启用掉落物悬浮标签显示
            if (plugin.getConfigManager().isDroppedItemHologramEnabled()) {
                // 这里可以添加掉落物悬浮标签的显示逻辑
            }
            
            Player player = event.getPlayer();
            plugin.getMessageUtil().sendDebug(player, "furnace.placed",
                    "type", data.getLevel().getType(),
                    "level", data.getLevel().getLevel(),
                    "uuid", data.getUuid().toString());
        }
    }
    
    /**
     * 当玩家破坏熔炉时，检查是否为自定义熔炉，并保留其等级
     */
    @EventHandler(priority = EventPriority.HIGH)
    public void onFurnaceBroken(BlockBreakEvent event) {
        Block block = event.getBlock();
        
        // 检查是否为熔炉类型
        if (!isFurnaceType(block.getType())) {
            return;
        }
        
        BlockState state = block.getState();
        // 尝试获取熔炉自定义数据
        CustomFurnaceData data = CustomFurnaceData.fromBlockState(plugin, state);
        if (data == null) {
            return;
        }
        
        // 移除悬浮文本 - 使用多重检查确保移除
        block.getWorld().getNearbyEntities(block.getLocation().add(0.5, 0.9, 0.5), 1, 1, 1).stream()
            .filter(entity -> entity instanceof ArmorStand)
            .map(entity -> (ArmorStand) entity)
            .filter(armorStand -> {
                // 1. 检查是否是隐形盔甲架
                if (armorStand.isVisible() || !armorStand.isCustomNameVisible()) {
                    return false;
                }
                
                // 2. 检查UUID匹配（如果有）
                if (data.getHologramUUID() != null && 
                    armorStand.getUniqueId().equals(data.getHologramUUID())) {
                    return true;
                }
                
                // 3. 检查位置精确匹配
                if (Math.abs(armorStand.getLocation().getX() - (block.getLocation().getX() + 0.5)) < 0.01 &&
                    Math.abs(armorStand.getLocation().getY() - (block.getLocation().getY() + 0.9)) < 0.01 &&
                    Math.abs(armorStand.getLocation().getZ() - (block.getLocation().getZ() + 0.5)) < 0.01) {
                    return true;
                }
                
                // 4. 检查自定义名称是否匹配
                String customName = plugin.getConfigManager().getFurnacesConfig()
                    .getString("furnaces." + data.getLevel().getType() + ".display_name." + data.getLevel().getLevel());
                if (customName == null) {
                    customName = plugin.getLangManager().getRawMessage("furnace.name");
                }
                String expectedDisplay = plugin.getLangManager().getRawMessage("furnace.hover_display")
                    .replace("%display%", customName)
                    .replace("%level%", String.valueOf(data.getLevel().getLevel()))
                    .replace("%payment_type%", data.getPaymentType().equals(FurnaceManager.PAYMENT_VAULT) ? "金币" : "点券")
                    .replace("%speed%", String.valueOf(data.getLevel().getCookingTime()));
                
                return armorStand.getCustomName() != null &&
                       plugin.getLangManager().colorize(expectedDisplay).equals(armorStand.getCustomName());
            })
            .forEach(armorStand -> {
                armorStand.remove();
                plugin.getMessageUtil().logDebug("furnace.hologram_removed",
                    "location", String.format("%.2f,%.2f,%.2f", 
                        armorStand.getLocation().getX(),
                        armorStand.getLocation().getY(),
                        armorStand.getLocation().getZ()));
            });
        
        // 从持久化存储中移除数据
        dataManager.removeFurnaceData(block.getLocation());
        
        // 获取熔炉内物品并掉落
        if (state instanceof Furnace) {
            Furnace furnace = (Furnace) state;
            FurnaceInventory inventory = furnace.getInventory();
            
            // 掉落燃料
            ItemStack fuel = inventory.getFuel();
            if (fuel != null && !fuel.getType().isAir()) {
                block.getWorld().dropItemNaturally(block.getLocation(), fuel);
            }
            
            // 掉落正在冶炼的物品
            ItemStack smelting = inventory.getSmelting();
            if (smelting != null && !smelting.getType().isAir()) {
                block.getWorld().dropItemNaturally(block.getLocation(), smelting);
            }
            
            // 掉落结果物品
            ItemStack result = inventory.getResult();
            if (result != null && !result.getType().isAir()) {
                block.getWorld().dropItemNaturally(block.getLocation(), result);
            }
        }
        
        // 取消默认掉落
        event.setDropItems(false);
        
        // 获取熔炉类型和等级
        FurnaceLevel level = data.getLevel();
        String type = level.getType();
        int levelNum = level.getLevel();
        
        // 创建对应等级的熔炉物品并掉落
        // 先尝试使用默认支付方式（vault）创建物品
        ItemStack customFurnace = furnaceManager.createFurnaceItem(type, levelNum, FurnaceManager.PAYMENT_VAULT);
        
        // 如果使用vault创建失败，尝试使用points创建
        if (customFurnace == null) {
            customFurnace = furnaceManager.createFurnaceItem(type, levelNum, FurnaceManager.PAYMENT_POINTS);
        }
        
        // 如果仍然失败，尝试创建1级熔炉作为兜底
        if (customFurnace == null && levelNum > 1) {
            customFurnace = furnaceManager.createFurnaceItem(type, 1, FurnaceManager.PAYMENT_VAULT);
        }
        
        // 掉落熔炉物品
        if (customFurnace != null) {
            // 获取自定义名字和悬浮显示格式
            String customName = plugin.getConfigManager().getFurnacesConfig().getString("furnaces." + type + ".display_name." + levelNum);
            if (customName == null) {
                customName = plugin.getLangManager().getRawMessage("furnace.name");
            }
            
            String hoverDisplay = plugin.getLangManager().getRawMessage("furnace.hover_display")
                    .replace("%display%", customName)
                    .replace("%level%", String.valueOf(levelNum))
                    .replace("%payment_type%", data.getPaymentType().equals(FurnaceManager.PAYMENT_VAULT) ? "金币" : "点券")
                    .replace("%speed%", String.valueOf(data.getLevel().getCookingTime()));
            
            // 使用dropItemNaturally方法并获取掉落的Item实体
            org.bukkit.entity.Item droppedItem = block.getWorld().dropItemNaturally(block.getLocation(), customFurnace);
            
            // 设置自定义显示名称
            droppedItem.setCustomName(plugin.getLangManager().colorize(hoverDisplay));
            droppedItem.setCustomNameVisible(true);
        }
        
        Player player = event.getPlayer();
        plugin.getMessageUtil().sendDebug(player, "furnace.broken",
                "type", type,
                "level", levelNum,
                "uuid", data.getUuid().toString());
    }
    
    /**
     * 当熔炉开始燃烧时，如果是自定义熔炉，根据等级加速燃烧
     */
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onFurnaceBurn(FurnaceBurnEvent event) {
        Block block = event.getBlock();
        
        // 尝试获取熔炉自定义数据
        BlockState state = block.getState();
        CustomFurnaceData data = CustomFurnaceData.fromBlockState(plugin, state);
        if (data == null) {
            return;
        }
        
        // 获取熔炉等级信息
        FurnaceLevel level = data.getLevel();
        
        // 调整燃烧时间 - 根据等级加速燃烧（提高燃料效率）
        int burnTime = event.getBurnTime();
        // 根据等级增加燃烧时间，使燃料更持久
        double multiplier = 1.0 + (level.getLevel() - 1) * 0.2; // 每级增加20%效率
        event.setBurnTime((int)(burnTime * multiplier));
        
        plugin.getMessageUtil().logDebug("furnace.burn");
    }
    
    /**
     * 当熔炉开始冶炼物品时，如果是自定义熔炉，根据等级调整冶炼速度
     */
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onFurnaceStartSmelt(FurnaceStartSmeltEvent event) {
        Block block = event.getBlock();
        
        // 尝试获取熔炉自定义数据
        BlockState state = block.getState();
        CustomFurnaceData data = CustomFurnaceData.fromBlockState(plugin, state);
        if (data == null) {
            return;
        }
        
        // 获取熔炉等级信息并设置冶炼时间
        FurnaceLevel level = data.getLevel();
        event.setTotalCookTime(level.getCookingTime());
        
        // 确保熔炉状态也设置了这个速度值
        if (state instanceof Furnace) {
            Furnace furnace = (Furnace) state;
            furnace.setCookTimeTotal(level.getCookingTime());
            furnace.update();
        }
        
        plugin.getMessageUtil().logDebug("furnace.start_smelt",
                "speed", level.getCookingTime());
    }
    
    /**
     * 当物品在熔炉中被成功冶炼时的处理
     */
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onFurnaceSmelt(FurnaceSmeltEvent event) {
        Block block = event.getBlock();
        
        // 尝试获取熔炉自定义数据
        BlockState state = block.getState();
        CustomFurnaceData data = CustomFurnaceData.fromBlockState(plugin, state);
        if (data == null) {
            return;
        }
        
        plugin.getMessageUtil().logDebug("furnace.smelt_complete");
    }
    
    /**
     * 当玩家丢弃熔炉物品时设置显示名称
     */
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerDropItem(PlayerDropItemEvent event) {
        org.bukkit.entity.Item droppedItem = event.getItemDrop();
        ItemStack item = droppedItem.getItemStack();
        
        // 检查是否为熔炉类型
        if (!isFurnaceType(item.getType())) {
            return;
        }
        
        // 尝试获取自定义熔炉数据
        CustomFurnaceData data = CustomFurnaceData.fromItem(plugin, item);
        if (data == null) {
            return;
        }
        
        // 检查是否启用掉落物悬浮标签显示
        if (plugin.getConfigManager().isDroppedItemHologramEnabled()) {
            // 获取自定义名字
            String customName = plugin.getConfigManager().getFurnacesConfig().getString("furnaces." + data.getLevel().getType() + ".display_name." + data.getLevel().getLevel());
            if (customName == null) {
                customName = plugin.getLangManager().getRawMessage("furnace.name");
            }
            
            // 获取悬浮显示格式
            String hoverDisplay = plugin.getLangManager().getRawMessage("furnace.hover_display")
                    .replace("%display%", customName)
                    .replace("%level%", String.valueOf(data.getLevel().getLevel()))
                    .replace("%payment_type%", data.getPaymentType().equals(FurnaceManager.PAYMENT_VAULT) ? "金币" : "点券")
                    .replace("%speed%", String.valueOf(data.getLevel().getCookingTime()));
            
            // 设置自定义显示名称
            droppedItem.setCustomName(plugin.getLangManager().colorize(hoverDisplay));
            droppedItem.setCustomNameVisible(true);
        } else {
            // 如果禁用，确保不显示悬浮标签
            droppedItem.setCustomNameVisible(false);
        }
    }
    
    /**
     * 检查物品是否为熔炉类型
     */
    private boolean isFurnaceType(Material material) {
        return material == Material.FURNACE || 
               material == Material.BLAST_FURNACE || 
               material == Material.SMOKER;
    }
} 