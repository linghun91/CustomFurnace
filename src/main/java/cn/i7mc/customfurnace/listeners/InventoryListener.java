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

/**
 * 监听与熔炉相关的物品栏事件
 */
public class InventoryListener implements Listener {
    private final CustomFurnace plugin;
    private FurnaceUpgradeGUI upgradeGUI;
    
    public InventoryListener(CustomFurnace plugin) {
        this.plugin = plugin;
        this.upgradeGUI = new FurnaceUpgradeGUI(plugin);
    }
    
    /**
     * 当玩家从熔炉中取出物品时的处理
     */
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onFurnaceExtract(FurnaceExtractEvent event) {
        Player player = event.getPlayer();
        Block block = event.getBlock();
        
        // 尝试获取熔炉自定义数据
        BlockState state = block.getState();
        CustomFurnaceData data = CustomFurnaceData.fromBlockState(plugin, state);
        if (data == null) {
            return;
        }
        
        // 记录取出操作
        plugin.getMessageUtil().sendDebug(player, "furnace.extract");
    }
    
    /**
     * 监听玩家在熔炉界面中的点击操作
     */
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onInventoryClick(InventoryClickEvent event) {
        Inventory inventory = event.getInventory();
        
        // 只处理熔炉类型的物品栏
        if (!(inventory instanceof FurnaceInventory) || 
            (inventory.getType() != InventoryType.FURNACE && 
             inventory.getType() != InventoryType.BLAST_FURNACE && 
             inventory.getType() != InventoryType.SMOKER)) {
            return;
        }
        
        // 可以在这里添加一些额外的熔炉交互逻辑
        // 例如，防止某些物品放入特定等级的熔炉等
        
        if (event.getWhoClicked() instanceof Player) {
            Player player = (Player) event.getWhoClicked();
            plugin.getMessageUtil().sendDebug(player, "furnace.inventory_interact");
        }
    }
    
    /**
     * 监听升级GUI中的点击操作
     */
    @EventHandler(priority = EventPriority.HIGH)
    public void onUpgradeGUIClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) {
            return;
        }
        
        Player player = (Player) event.getWhoClicked();
        String title = event.getView().getTitle();
        String guiTitle = plugin.getLangManager().colorize(plugin.getLangManager().getRawMessage("gui.title"));
        
        // 检查是否是我们的GUI
        if (!title.equals(guiTitle)) {
            return;
        }
        
        event.setCancelled(true); // 阻止任何点击行为
        
        // 获取点击的槽位
        int slot = event.getRawSlot();
        
        // 检查是否点击了升级按钮（金币或点券）
        if (slot == FurnaceUpgradeGUI.VAULT_UPGRADE_SLOT || slot == FurnaceUpgradeGUI.POINTS_UPGRADE_SLOT) {
            ItemStack furnace = event.getInventory().getItem(FurnaceUpgradeGUI.FURNACE_SLOT);
            if (furnace != null) {
                // 处理升级
                boolean success = upgradeGUI.handleUpgradeClick(player, furnace, slot);
                if (success) {
                    // 获取升级后的熔炉（玩家手中的物品）
                    ItemStack newFurnace = player.getInventory().getItemInMainHand();
                    
                    // 更新GUI中的熔炉显示
                    event.getInventory().setItem(FurnaceUpgradeGUI.FURNACE_SLOT, newFurnace);
                    
                    // 刷新GUI内容
                    upgradeGUI.updateUpgradeButtons(event.getInventory(), newFurnace);
                }
            }
        }
    }
    
    /**
     * 打开熔炉升级界面
     */
    public void openUpgradeGUI(Player player, ItemStack furnaceItem) {
        upgradeGUI.openGUI(player, furnaceItem);
    }
} 