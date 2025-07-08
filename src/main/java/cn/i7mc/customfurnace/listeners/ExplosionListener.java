package cn.i7mc.customfurnace.listeners;

import cn.i7mc.customfurnace.CustomFurnace;
import cn.i7mc.customfurnace.managers.DataManager;
import cn.i7mc.customfurnace.models.CustomFurnaceData;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockExplodeEvent;
import org.bukkit.event.entity.EntityExplodeEvent;

import java.util.Iterator;

/**
 * 爆炸保护监听器
 * 处理实体爆炸和方块爆炸事件，保护自定义熔炉免受爆炸破坏
 */
public class ExplosionListener implements Listener {
    
    private final CustomFurnace plugin;
    private final DataManager dataManager;
    
    public ExplosionListener(CustomFurnace plugin) {
        this.plugin = plugin;
        this.dataManager = plugin.getDataManager();
    }
    
    /**
     * 处理实体爆炸事件（如TNT、苦力怕等）
     * 
     * @param event 实体爆炸事件
     */
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onEntityExplode(EntityExplodeEvent event) {
        if (event.blockList().isEmpty()) {
            return;
        }
        
        // 遍历爆炸影响的方块列表，移除受保护的自定义熔炉
        Iterator<Block> blockIterator = event.blockList().iterator();
        while (blockIterator.hasNext()) {
            Block block = blockIterator.next();
            
            // 检查是否为熔炉类型方块
            if (!isFurnaceType(block.getType())) {
                continue;
            }
            
            // 检查是否为自定义熔炉
            CustomFurnaceData furnaceData = CustomFurnaceData.fromBlockState(plugin, block.getState());
            if (furnaceData == null) {
                continue;
            }
            
            // 检查该类型熔炉是否启用爆炸保护
            if (isExplosionProtectionEnabled(furnaceData.getLevel().getType())) {
                // 从爆炸列表中移除该方块，保护熔炉
                blockIterator.remove();
                
                // 记录调试信息
                plugin.getMessageUtil().logDebug("explosion.furnace_protected", 
                    "type", furnaceData.getLevel().getType(),
                    "level", furnaceData.getLevel().getLevel(),
                    "location", String.format("%.2f,%.2f,%.2f", 
                        block.getLocation().getX(), 
                        block.getLocation().getY(), 
                        block.getLocation().getZ()),
                    "explosion_type", "entity"
                );
            }
        }
    }
    
    /**
     * 处理方块爆炸事件（如TNT方块等）
     * 
     * @param event 方块爆炸事件
     */
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onBlockExplode(BlockExplodeEvent event) {
        if (event.blockList().isEmpty()) {
            return;
        }
        
        // 遍历爆炸影响的方块列表，移除受保护的自定义熔炉
        Iterator<Block> blockIterator = event.blockList().iterator();
        while (blockIterator.hasNext()) {
            Block block = blockIterator.next();
            
            // 检查是否为熔炉类型方块
            if (!isFurnaceType(block.getType())) {
                continue;
            }
            
            // 检查是否为自定义熔炉
            CustomFurnaceData furnaceData = CustomFurnaceData.fromBlockState(plugin, block.getState());
            if (furnaceData == null) {
                continue;
            }
            
            // 检查该类型熔炉是否启用爆炸保护
            if (isExplosionProtectionEnabled(furnaceData.getLevel().getType())) {
                // 从爆炸列表中移除该方块，保护熔炉
                blockIterator.remove();
                
                // 记录调试信息
                plugin.getMessageUtil().logDebug("explosion.furnace_protected", 
                    "type", furnaceData.getLevel().getType(),
                    "level", furnaceData.getLevel().getLevel(),
                    "location", String.format("%.2f,%.2f,%.2f", 
                        block.getLocation().getX(), 
                        block.getLocation().getY(), 
                        block.getLocation().getZ()),
                    "explosion_type", "block"
                );
            }
        }
    }
    
    /**
     * 检查是否为熔炉类型方块
     * 
     * @param material 方块材质
     * @return 是否为熔炉类型
     */
    private boolean isFurnaceType(Material material) {
        return material == Material.FURNACE || 
               material == Material.BLAST_FURNACE || 
               material == Material.SMOKER;
    }
    
    /**
     * 检查指定熔炉类型是否启用爆炸保护
     * 
     * @param furnaceType 熔炉类型
     * @return 是否启用爆炸保护
     */
    private boolean isExplosionProtectionEnabled(String furnaceType) {
        return plugin.getConfigManager().getFurnacesConfig()
            .getBoolean("furnaces." + furnaceType + ".explosion-protection", true);
    }
}
