package cn.i7mc.customfurnace.models;

import cn.i7mc.customfurnace.CustomFurnace;
import cn.i7mc.customfurnace.managers.FurnaceManager;
import org.bukkit.NamespacedKey;
import org.bukkit.block.BlockState;
import org.bukkit.block.Furnace;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.UUID;

/**
 * 自定义熔炉数据模型
 */
public class CustomFurnaceData {
    private static final String LEVEL_KEY = "custom_furnace_level";
    private static final String TYPE_KEY = "custom_furnace_type";
    private static final String UUID_KEY = "custom_furnace_uuid";
    
    private final JavaPlugin plugin;
    private final FurnaceLevel level;
    private final UUID uuid;
    private final String paymentType;
    private UUID hologramUUID;  // 用于存储悬浮文本ArmorStand的UUID
    
    public CustomFurnaceData(JavaPlugin plugin, FurnaceLevel level, String paymentType) {
        this.plugin = plugin;
        this.level = level;
        this.uuid = UUID.randomUUID();
        this.paymentType = paymentType;
    }
    
    public CustomFurnaceData(JavaPlugin plugin, FurnaceLevel level, UUID uuid, String paymentType) {
        this.plugin = plugin;
        this.level = level;
        this.uuid = uuid;
        this.paymentType = paymentType;
    }
    
    /**
     * 从物品中获取自定义熔炉数据
     */
    public static CustomFurnaceData fromItem(JavaPlugin plugin, ItemStack item) {
        if (item == null || !item.hasItemMeta()) {
            return null;
        }
        
        ItemMeta meta = item.getItemMeta();
        PersistentDataContainer container = meta.getPersistentDataContainer();
        
        NamespacedKey levelKey = new NamespacedKey(plugin, LEVEL_KEY);
        NamespacedKey typeKey = new NamespacedKey(plugin, TYPE_KEY);
        NamespacedKey uuidKey = new NamespacedKey(plugin, UUID_KEY);
        NamespacedKey paymentKey = new NamespacedKey(plugin, "custom_furnace_payment");
        
        if (!container.has(levelKey, PersistentDataType.INTEGER) || 
            !container.has(typeKey, PersistentDataType.STRING)) {
            return null;
        }
        
        int level = container.get(levelKey, PersistentDataType.INTEGER);
        String type = container.get(typeKey, PersistentDataType.STRING);
        String paymentType = container.has(paymentKey, PersistentDataType.STRING) ? 
            container.get(paymentKey, PersistentDataType.STRING) : FurnaceManager.PAYMENT_VAULT;
        
        // 获取UUID，如果不存在则生成新的
        UUID uuid;
        if (container.has(uuidKey, PersistentDataType.STRING)) {
            try {
                uuid = UUID.fromString(container.get(uuidKey, PersistentDataType.STRING));
            } catch (IllegalArgumentException e) {
                uuid = UUID.randomUUID();
            }
        } else {
            uuid = UUID.randomUUID();
        }
        
        // 从FurnaceManager获取完整的熔炉等级数据
        FurnaceManager furnaceManager = ((CustomFurnace)plugin).getFurnaceManager();
        FurnaceLevel furnaceLevel = furnaceManager.getFurnaceLevel(type, level, paymentType);
        
        // 如果没有找到配置的等级数据，创建一个基本的
        if (furnaceLevel == null) {
            furnaceLevel = new FurnaceLevel(type, level, 200, 0, 0); // 默认冶炼时间和升级成本
        }
        
        return new CustomFurnaceData(plugin, furnaceLevel, uuid, paymentType);
    }
    
    /**
     * 从方块状态中获取自定义熔炉数据
     */
    public static CustomFurnaceData fromBlockState(JavaPlugin plugin, BlockState state) {
        if (!(state instanceof Furnace)) {
            return null;
        }
        
        Furnace furnace = (Furnace) state;
        PersistentDataContainer container = furnace.getPersistentDataContainer();
        
        NamespacedKey levelKey = new NamespacedKey(plugin, LEVEL_KEY);
        NamespacedKey typeKey = new NamespacedKey(plugin, TYPE_KEY);
        NamespacedKey uuidKey = new NamespacedKey(plugin, UUID_KEY);
        NamespacedKey paymentKey = new NamespacedKey(plugin, "custom_furnace_payment");
        
        if (!container.has(levelKey, PersistentDataType.INTEGER) || 
            !container.has(typeKey, PersistentDataType.STRING)) {
            return null;
        }
        
        int level = container.get(levelKey, PersistentDataType.INTEGER);
        String type = container.get(typeKey, PersistentDataType.STRING);
        String paymentType = container.has(paymentKey, PersistentDataType.STRING) ? 
            container.get(paymentKey, PersistentDataType.STRING) : FurnaceManager.PAYMENT_VAULT;
        
        // 获取UUID，如果不存在则生成新的
        UUID uuid;
        if (container.has(uuidKey, PersistentDataType.STRING)) {
            try {
                uuid = UUID.fromString(container.get(uuidKey, PersistentDataType.STRING));
            } catch (IllegalArgumentException e) {
                uuid = UUID.randomUUID();
            }
        } else {
            uuid = UUID.randomUUID();
        }
        
        // 从FurnaceManager获取完整的熔炉等级数据
        FurnaceManager furnaceManager = ((CustomFurnace)plugin).getFurnaceManager();
        FurnaceLevel furnaceLevel = furnaceManager.getFurnaceLevel(type, level, paymentType);
        
        // 如果没有找到配置的等级数据，创建一个基本的
        if (furnaceLevel == null) {
            furnaceLevel = new FurnaceLevel(type, level, 200, 0, 0); // 默认冶炼时间和升级成本
        }
        
        return new CustomFurnaceData(plugin, furnaceLevel, uuid, paymentType);
    }
    
    /**
     * 将自定义数据应用到物品上
     */
    public ItemStack applyToItem(ItemStack item) {
        if (item == null) {
            return null;
        }
        
        ItemMeta meta = item.getItemMeta();
        if (meta == null) {
            return item;
        }
        
        PersistentDataContainer container = meta.getPersistentDataContainer();
        
        NamespacedKey levelKey = new NamespacedKey(plugin, LEVEL_KEY);
        NamespacedKey typeKey = new NamespacedKey(plugin, TYPE_KEY);
        NamespacedKey uuidKey = new NamespacedKey(plugin, UUID_KEY);
        NamespacedKey paymentKey = new NamespacedKey(plugin, "custom_furnace_payment");
        
        container.set(levelKey, PersistentDataType.INTEGER, level.getLevel());
        container.set(typeKey, PersistentDataType.STRING, level.getType());
        container.set(uuidKey, PersistentDataType.STRING, uuid.toString());
        container.set(paymentKey, PersistentDataType.STRING, paymentType);
        
        item.setItemMeta(meta);
        return item;
    }
    
    /**
     * 将自定义数据应用到方块上
     */
    public void applyToBlockState(BlockState state) {
        if (!(state instanceof Furnace)) {
            return;
        }
        
        Furnace furnace = (Furnace) state;
        PersistentDataContainer container = furnace.getPersistentDataContainer();
        
        NamespacedKey levelKey = new NamespacedKey(plugin, LEVEL_KEY);
        NamespacedKey typeKey = new NamespacedKey(plugin, TYPE_KEY);
        NamespacedKey uuidKey = new NamespacedKey(plugin, UUID_KEY);
        NamespacedKey paymentKey = new NamespacedKey(plugin, "custom_furnace_payment");
        
        container.set(levelKey, PersistentDataType.INTEGER, level.getLevel());
        container.set(typeKey, PersistentDataType.STRING, level.getType());
        container.set(uuidKey, PersistentDataType.STRING, uuid.toString());
        container.set(paymentKey, PersistentDataType.STRING, paymentType);
        
        // 设置熔炉烧炼速度 - 确保这个值会被正确应用
        int cookingTime = level.getCookingTime();
        furnace.setCookTimeTotal(cookingTime);
        
        // 确保更新被应用到方块状态
        furnace.update(true, false);
    }
    
    public FurnaceLevel getLevel() {
        return level;
    }
    
    public UUID getUuid() {
        return uuid;
    }
    
    public String getPaymentType() {
        return paymentType;
    }

    public UUID getHologramUUID() {
        return hologramUUID;
    }

    public void setHologramUUID(UUID hologramUUID) {
        this.hologramUUID = hologramUUID;
    }
} 