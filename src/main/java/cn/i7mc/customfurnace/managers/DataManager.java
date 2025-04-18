package cn.i7mc.customfurnace.managers;

import cn.i7mc.customfurnace.CustomFurnace;
import cn.i7mc.customfurnace.models.CustomFurnaceData;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.scheduler.BukkitTask;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 数据管理器 - 处理熔炉数据的持久化
 */
public class DataManager {
    private final CustomFurnace plugin;
    private final ConfigManager configManager;
    
    // 使用线程安全的Map存储熔炉数据，使用UUID作为键
    private final Map<UUID, Map<String, Object>> furnaceData = new ConcurrentHashMap<>();
    
    // 位置到UUID的映射，方便通过位置查找UUID
    private final Map<String, UUID> locationToUuid = new ConcurrentHashMap<>();
    
    // 数据文件
    private File dataFile;
    private FileConfiguration dataConfig;
    
    // 自动保存任务
    private BukkitTask saveTask;
    
    public DataManager(CustomFurnace plugin) {
        this.plugin = plugin;
        this.configManager = plugin.getConfigManager();
        
        // 初始化数据文件
        this.dataFile = new File(plugin.getDataFolder(), "data.yml");
        
        // 加载数据
        loadData();
        
        // 启动自动保存任务
        startSaveTask();
    }
    
    /**
     * 加载数据
     */
    public void loadData() {
        // 清空当前数据
        furnaceData.clear();
        locationToUuid.clear();
        
        // 如果数据文件不存在，创建空文件
        if (!dataFile.exists()) {
            try {
                dataFile.createNewFile();
            } catch (IOException e) {
                plugin.getLogger().severe("无法创建数据文件: " + e.getMessage());
                return;
            }
        }
        
        // 加载数据文件
        dataConfig = YamlConfiguration.loadConfiguration(dataFile);
        
        // 加载熔炉数据
        ConfigurationSection furnacesSection = dataConfig.getConfigurationSection("furnaces");
        if (furnacesSection == null) {
            return;
        }
        
        // 先加载所有数据到内存
        for (String key : furnacesSection.getKeys(false)) {
            UUID uuid;
            try {
                uuid = UUID.fromString(key);
            } catch (IllegalArgumentException e) {
                // 兼容处理老数据
                uuid = UUID.randomUUID();
                if (configManager.isDebugEnabled()) {
                    plugin.getMessageUtil().logDebug("data.uuid_created_for_legacy", "location", key);
                }
            }
            
            ConfigurationSection furnaceSection = furnacesSection.getConfigurationSection(key);
            if (furnaceSection != null) {
                Map<String, Object> data = new HashMap<>();
                for (String property : furnaceSection.getKeys(false)) {
                    data.put(property, furnaceSection.get(property));
                }
                
                furnaceData.put(uuid, data);
                
                // 创建位置到UUID的映射
                if (data.containsKey("world") && data.containsKey("x") && data.containsKey("y") && data.containsKey("z")) {
                    String world = (String) data.get("world");
                    int x = ((Number) data.get("x")).intValue();
                    int y = ((Number) data.get("y")).intValue();
                    int z = ((Number) data.get("z")).intValue();
                    String locationKey = world + "," + x + "," + y + "," + z;
                    locationToUuid.put(locationKey, uuid);
                }
            }
        }
        
        // 然后为每个熔炉重新创建全息显示
        for (Map.Entry<UUID, Map<String, Object>> entry : furnaceData.entrySet()) {
            try {
                Map<String, Object> data = entry.getValue();
                String world = (String) data.get("world");
                int x = ((Number) data.get("x")).intValue();
                int y = ((Number) data.get("y")).intValue();
                int z = ((Number) data.get("z")).intValue();
                
                // 获取世界
                org.bukkit.World bukkitWorld = plugin.getServer().getWorld(world);
                if (bukkitWorld == null) continue;
                
                // 获取方块
                org.bukkit.block.Block block = bukkitWorld.getBlockAt(x, y, z);
                if (!isFurnaceType(block.getType())) continue;
                
                // 获取熔炉数据
                String type = (String) data.get("type");
                int level = ((Number) data.get("level")).intValue();
                String paymentType = (String) data.getOrDefault("payment_type", FurnaceManager.PAYMENT_VAULT);
                
                // 创建熔炉数据对象
                FurnaceManager furnaceManager = plugin.getFurnaceManager();
                CustomFurnaceData furnaceData = new CustomFurnaceData(
                        plugin, 
                        furnaceManager.getFurnaceLevel(type, level, paymentType),
                        entry.getKey(),
                        paymentType
                );
                
                // 应用数据到方块
                BlockState state = block.getState();
                furnaceData.applyToBlockState(state);
                
                // 强制更新烹饪时间
                if (state instanceof org.bukkit.block.Furnace) {
                    org.bukkit.block.Furnace furnace = (org.bukkit.block.Furnace) state;
                    furnace.setCookTimeTotal(furnaceData.getLevel().getCookingTime());
                    furnace.update();
                }
                
                // 移除可能存在的旧全息显示
                block.getWorld().getNearbyEntities(block.getLocation().add(0.5, 0.9, 0.5), 1, 1, 1).stream()
                    .filter(entity -> entity instanceof org.bukkit.entity.ArmorStand)
                    .map(entity -> (org.bukkit.entity.ArmorStand) entity)
                    .filter(armorStand -> !armorStand.isVisible() && armorStand.isCustomNameVisible())
                    .forEach(org.bukkit.entity.ArmorStand::remove);
                
                // 检查是否启用盔甲架全息信息显示
                if (configManager.isArmorstandHologramEnabled()) {
                    // 获取自定义名字
                    String customName = plugin.getConfigManager().getFurnacesConfig()
                        .getString("furnaces." + type + ".display_name." + level);
                    if (customName == null) {
                        customName = plugin.getLangManager().getRawMessage("furnace.name");
                    }
                    
                    // 获取悬浮显示格式
                    String hoverDisplay = plugin.getLangManager().getRawMessage("furnace.hover_display")
                        .replace("%display%", customName)
                        .replace("%level%", String.valueOf(level))
                        .replace("%payment_type%", paymentType.equals(FurnaceManager.PAYMENT_VAULT) ? "金币" : "点券")
                        .replace("%speed%", String.valueOf(furnaceData.getLevel().getCookingTime()));
                    
                    // 创建ArmorStand显示悬浮文本
                    org.bukkit.entity.ArmorStand hologram = (org.bukkit.entity.ArmorStand) 
                        block.getWorld().spawnEntity(block.getLocation().add(0.5, 0.9, 0.5), org.bukkit.entity.EntityType.ARMOR_STAND);
                    hologram.setVisible(false);
                    hologram.setGravity(false);
                    hologram.setCanPickupItems(false);
                    hologram.setCustomName(plugin.getLangManager().colorize(hoverDisplay));
                    hologram.setCustomNameVisible(true);
                    hologram.setMarker(true);
                    hologram.setSmall(true);
                    
                    // 保存ArmorStand的UUID到熔炉数据中
                    furnaceData.setHologramUUID(hologram.getUniqueId());
                }
                
                // 更新数据到存储
                storeFurnaceData(block.getLocation(), furnaceData);
                
                if (configManager.isDebugEnabled()) {
                    plugin.getMessageUtil().logDebug("data.furnace_applied", 
                            "location", world + "," + x + "," + y + "," + z,
                            "type", type,
                            "level", level,
                            "payment_type", paymentType,
                            "uuid", entry.getKey().toString(),
                            "hologram_uuid", furnaceData.getHologramUUID().toString());
                }
            } catch (Exception e) {
                plugin.getLogger().warning("加载熔炉数据失败: " + e.getMessage());
                if (configManager.isDebugEnabled()) {
                    e.printStackTrace();
                }
            }
        }
        
        if (configManager.isDebugEnabled()) {
            plugin.getMessageUtil().logDebug("data.loaded", "count", furnaceData.size());
        }
    }
    
    /**
     * 保存数据
     */
    public void saveData() {
        if (dataConfig == null) {
            dataConfig = new YamlConfiguration();
        }
        
        // 清除旧数据
        dataConfig.set("furnaces", null);
        
        // 保存熔炉数据
        for (Map.Entry<UUID, Map<String, Object>> entry : furnaceData.entrySet()) {
            UUID uuid = entry.getKey();
            Map<String, Object> data = entry.getValue();
            
            for (Map.Entry<String, Object> dataEntry : data.entrySet()) {
                dataConfig.set("furnaces." + uuid.toString() + "." + dataEntry.getKey(), dataEntry.getValue());
            }
        }
        
        // 保存到文件
        try {
            dataConfig.save(dataFile);
            if (configManager.isDebugEnabled()) {
                plugin.getMessageUtil().logDebug("data.saved", "count", furnaceData.size());
            }
        } catch (IOException e) {
            plugin.getLogger().severe("保存数据文件失败: " + e.getMessage());
        }
    }
    
    /**
     * 启动自动保存任务
     */
    private void startSaveTask() {
        // 获取配置的保存间隔
        int saveInterval = configManager.getConfig().getInt("save-interval", 300) * 20; // 转换为tick
        
        // 取消已有任务
        if (saveTask != null) {
            saveTask.cancel();
        }
        
        // 创建新任务
        saveTask = Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, this::saveData, saveInterval, saveInterval);
        
        if (configManager.isDebugEnabled()) {
            plugin.getMessageUtil().logDebug("data.autosave_started", "interval", saveInterval / 20);
        }
    }
    
    /**
     * 存储熔炉数据
     */
    public void storeFurnaceData(Location location, CustomFurnaceData data) {
        String key = serializeLocation(location);
        Map<String, Object> furnaceData = new HashMap<>();
        
        furnaceData.put("type", data.getLevel().getType());
        furnaceData.put("level", data.getLevel().getLevel());
        furnaceData.put("uuid", data.getUuid().toString());
        furnaceData.put("payment_type", data.getPaymentType());
        
        // 保存位置信息
        furnaceData.put("world", location.getWorld().getName());
        furnaceData.put("x", location.getBlockX());
        furnaceData.put("y", location.getBlockY());
        furnaceData.put("z", location.getBlockZ());
        
        // 保存全息显示UUID
        if (data.getHologramUUID() != null) {
            furnaceData.put("hologram_uuid", data.getHologramUUID().toString());
        }
        
        this.furnaceData.put(UUID.fromString(data.getUuid().toString()), furnaceData);
        this.locationToUuid.put(key, data.getUuid());
        saveData();
    }
    
    /**
     * 移除熔炉数据
     */
    public void removeFurnaceData(Location location) {
        if (location == null) return;
        
        String locationKey = serializeLocation(location);
        UUID uuid = locationToUuid.remove(locationKey);
        if (uuid != null) {
            furnaceData.remove(uuid);
        }
    }
    
    /**
     * 获取熔炉数据
     */
    public Map<String, Object> getFurnaceData(Location location) {
        if (location == null) return null;
        
        String locationKey = serializeLocation(location);
        UUID uuid = locationToUuid.get(locationKey);
        if (uuid == null) return null;
        
        return furnaceData.get(uuid);
    }
    
    /**
     * 通过UUID获取熔炉数据
     */
    public Map<String, Object> getFurnaceDataByUuid(UUID uuid) {
        if (uuid == null) return null;
        return furnaceData.get(uuid);
    }
    
    /**
     * 应用所有存储的熔炉数据到世界
     * 用于服务器启动时恢复熔炉状态
     */
    public void applyAllFurnaces() {
        if (dataConfig == null) {
            return;
        }
        
        // 先清理所有现有的全息显示
        if (configManager.isArmorstandHologramEnabled()) {
            plugin.getServer().getWorlds().forEach(world -> {
                world.getEntities().stream()
                    .filter(entity -> entity instanceof org.bukkit.entity.ArmorStand)
                    .map(entity -> (org.bukkit.entity.ArmorStand) entity)
                    .filter(armorStand -> !armorStand.isVisible() && armorStand.isCustomNameVisible())
                    .forEach(org.bukkit.entity.ArmorStand::remove);
            });
        }
        
        ConfigurationSection furnacesSection = dataConfig.getConfigurationSection("furnaces");
        if (furnacesSection == null) {
            return;
        }
        
        for (String uuidStr : furnacesSection.getKeys(false)) {
            ConfigurationSection furnaceSection = furnacesSection.getConfigurationSection(uuidStr);
            if (furnaceSection == null) continue;
            
            try {
                UUID uuid = UUID.fromString(uuidStr);
                Map<String, Object> data = new HashMap<>();
                
                // 读取所有数据
                for (String key : furnaceSection.getKeys(false)) {
                    data.put(key, furnaceSection.get(key));
                }
                
                // 获取位置信息
                String worldName = (String) data.get("world");
                int x = ((Number) data.get("x")).intValue();
                int y = ((Number) data.get("y")).intValue();
                int z = ((Number) data.get("z")).intValue();
                
                World world = Bukkit.getWorld(worldName);
                if (world == null) continue;
                
                Block block = world.getBlockAt(x, y, z);
                if (!isFurnaceType(block.getType())) continue;
                
                String type = (String) data.get("type");
                int level = ((Number) data.get("level")).intValue();
                String paymentType = (String) data.get("payment_type");
                
                // 获取熔炉等级数据
                FurnaceManager furnaceManager = plugin.getFurnaceManager();
                CustomFurnaceData furnaceData = new CustomFurnaceData(
                        plugin, 
                        furnaceManager.getFurnaceLevel(type, level, paymentType),
                        uuid,
                        paymentType
                );
                
                // 应用数据到方块
                BlockState state = block.getState();
                furnaceData.applyToBlockState(state);
                
                // 强制更新烹饪时间
                if (state instanceof org.bukkit.block.Furnace) {
                    org.bukkit.block.Furnace furnace = (org.bukkit.block.Furnace) state;
                    furnace.setCookTimeTotal(furnaceData.getLevel().getCookingTime());
                    furnace.update();
                }
                
                // 检查是否启用盔甲架全息信息显示
                if (configManager.isArmorstandHologramEnabled()) {
                    // 获取自定义名字
                    String customName = plugin.getConfigManager().getFurnacesConfig()
                        .getString("furnaces." + type + ".display_name." + level);
                    if (customName == null) {
                        customName = plugin.getLangManager().getRawMessage("furnace.name");
                    }
                    
                    // 获取悬浮显示格式
                    String hoverDisplay = plugin.getLangManager().getRawMessage("furnace.hover_display")
                        .replace("%display%", customName)
                        .replace("%level%", String.valueOf(level))
                        .replace("%payment_type%", paymentType.equals(FurnaceManager.PAYMENT_VAULT) ? "金币" : "点券")
                        .replace("%speed%", String.valueOf(furnaceData.getLevel().getCookingTime()));
                    
                    // 创建ArmorStand显示悬浮文本
                    org.bukkit.entity.ArmorStand hologram = (org.bukkit.entity.ArmorStand) 
                        block.getWorld().spawnEntity(block.getLocation().add(0.5, 0.9, 0.5), org.bukkit.entity.EntityType.ARMOR_STAND);
                    hologram.setVisible(false);
                    hologram.setGravity(false);
                    hologram.setCanPickupItems(false);
                    hologram.setCustomName(plugin.getLangManager().colorize(hoverDisplay));
                    hologram.setCustomNameVisible(true);
                    hologram.setMarker(true);
                    hologram.setSmall(true);
                    
                    // 保存ArmorStand的UUID到熔炉数据中
                    furnaceData.setHologramUUID(hologram.getUniqueId());
                }
                
                // 更新数据到存储
                storeFurnaceData(block.getLocation(), furnaceData);
                
                if (configManager.isDebugEnabled()) {
                    plugin.getMessageUtil().logDebug("data.furnace_applied", 
                            "location", worldName + "," + x + "," + y + "," + z,
                            "type", type,
                            "level", level,
                            "payment_type", paymentType,
                            "uuid", uuid.toString(),
                            "hologram_uuid", furnaceData.getHologramUUID() != null ? furnaceData.getHologramUUID().toString() : "none");
                }
            } catch (Exception e) {
                plugin.getLogger().warning("加载熔炉数据失败: " + e.getMessage());
            }
        }
    }
    
    /**
     * 序列化位置为字符串键
     */
    private String serializeLocation(Location location) {
        return location.getWorld().getName() + "," + 
               location.getBlockX() + "," + 
               location.getBlockY() + "," + 
               location.getBlockZ();
    }
    
    /**
     * 检查物品是否为熔炉类型
     */
    private boolean isFurnaceType(Material material) {
        return material == Material.FURNACE || 
               material == Material.BLAST_FURNACE || 
               material == Material.SMOKER;
    }
    
    /**
     * 关闭数据管理器
     */
    public void shutdown() {
        // 取消自动保存任务
        if (saveTask != null) {
            saveTask.cancel();
            saveTask = null;
        }
        
        // 保存数据
        saveData();
    }
} 