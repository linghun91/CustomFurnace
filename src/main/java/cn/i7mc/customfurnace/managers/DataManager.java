package cn.i7mc.customfurnace.managers;

import cn.i7mc.customfurnace.CustomFurnace;
import cn.i7mc.customfurnace.managers.ConfigManager;
import cn.i7mc.customfurnace.managers.FurnaceManager;
import cn.i7mc.customfurnace.models.CustomFurnaceData;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Furnace;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.TextDisplay;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitTask;

public class DataManager {
    private final CustomFurnace plugin;
    private final ConfigManager configManager;
    private final Map<UUID, Map<String, Object>> furnaceData = new ConcurrentHashMap<UUID, Map<String, Object>>();
    private final Map<String, UUID> locationToUuid = new ConcurrentHashMap<String, UUID>();
    private File dataFile;
    private FileConfiguration dataConfig;
    private BukkitTask saveTask;

    public DataManager(CustomFurnace plugin) {
        this.plugin = plugin;
        this.configManager = plugin.getConfigManager();
        this.dataFile = new File(plugin.getDataFolder(), "data.yml");
        this.loadData();
        this.startSaveTask();
    }

    public void loadData() {
        this.furnaceData.clear();
        this.locationToUuid.clear();
        if (!this.dataFile.exists()) {
            try {
                this.dataFile.createNewFile();
            } catch (IOException e) {
                this.plugin.getLogger().severe("\u65e0\u6cd5\u521b\u5efa\u6570\u636e\u6587\u4ef6: " + e.getMessage());
                return;
            }
        }
        this.dataConfig = YamlConfiguration.loadConfiguration((File)this.dataFile);
        ConfigurationSection furnacesSection = this.dataConfig.getConfigurationSection("furnaces");
        if (furnacesSection == null) {
            return;
        }
        for (String string : furnacesSection.getKeys(false)) {
            ConfigurationSection furnaceSection;
            UUID uuid;
            try {
                uuid = UUID.fromString(string);
            } catch (IllegalArgumentException e) {
                uuid = UUID.randomUUID();
                if (this.configManager.isDebugEnabled()) {
                    this.plugin.getMessageUtil().logDebug("data.uuid_created_for_legacy", "location", string);
                }
            }
            if ((furnaceSection = furnacesSection.getConfigurationSection(string)) == null) continue;
            HashMap<String, Object> data = new HashMap<String, Object>();
            for (String property : furnaceSection.getKeys(false)) {
                data.put(property, furnaceSection.get(property));
            }
            this.furnaceData.put(uuid, data);
            if (!data.containsKey("world") || !data.containsKey("x") || !data.containsKey("y") || !data.containsKey("z")) continue;
            String world = (String)data.get("world");
            int x = ((Number)data.get("x")).intValue();
            int y = ((Number)data.get("y")).intValue();
            int z = ((Number)data.get("z")).intValue();
            String locationKey = world + "," + x + "," + y + "," + z;
            this.locationToUuid.put(locationKey, uuid);
        }
        for (Map.Entry entry : this.furnaceData.entrySet()) {
            try {
                UUID hologramUUID;
                Block block;
                Map data = (Map)entry.getValue();
                String world = (String)data.get("world");
                int x = ((Number)data.get("x")).intValue();
                int y = ((Number)data.get("y")).intValue();
                int z = ((Number)data.get("z")).intValue();
                World bukkitWorld = this.plugin.getServer().getWorld(world);
                if (bukkitWorld == null || !this.isFurnaceType((block = bukkitWorld.getBlockAt(x, y, z)).getType())) continue;
                String type = (String)data.get("type");
                int level = ((Number)data.get("level")).intValue();
                String paymentType = (String)data.getOrDefault("payment_type", "vault");
                FurnaceManager furnaceManager = this.plugin.getFurnaceManager();
                CustomFurnaceData furnaceData = new CustomFurnaceData(this.plugin, furnaceManager.getFurnaceLevel(type, level, paymentType), (UUID)entry.getKey(), paymentType);
                BlockState state = block.getState();
                furnaceData.applyToBlockState(state);
                if (state instanceof Furnace) {
                    Furnace furnace = (Furnace)state;
                    furnace.setCookTimeTotal(furnaceData.getLevel().getCookingTime());
                    furnace.update();
                }
                block.getWorld().getNearbyEntities(block.getLocation().add(0.5, 0.9, 0.5), 1.0, 1.0, 1.0).stream().filter(entity -> entity instanceof TextDisplay).map(entity -> (TextDisplay)entity).filter(textDisplay -> textDisplay.getText() != null).forEach(Entity::remove);
                if (this.configManager.isArmorstandHologramEnabled() && (hologramUUID = this.plugin.getTextDisplayUtil().createOrUpdateFurnaceDisplay(block, furnaceData, 0, 0)) != null) {
                    furnaceData.setHologramUUID(hologramUUID);
                }
                this.storeFurnaceData(block.getLocation(), furnaceData);
                if (!this.configManager.isDebugEnabled()) continue;
                this.plugin.getMessageUtil().logDebug("data.furnace_applied", "location", world + "," + x + "," + y + "," + z, "type", type, "level", level, "payment_type", paymentType, "uuid", ((UUID)entry.getKey()).toString(), "hologram_uuid", furnaceData.getHologramUUID().toString());
            } catch (Exception e) {
                this.plugin.getLogger().warning("\u52a0\u8f7d\u7194\u7089\u6570\u636e\u5931\u8d25: " + e.getMessage());
                if (!this.configManager.isDebugEnabled()) continue;
                e.printStackTrace();
            }
        }
        if (this.configManager.isDebugEnabled()) {
            this.plugin.getMessageUtil().logDebug("data.loaded", "count", this.furnaceData.size());
        }
    }

    public void saveData() {
        if (this.dataConfig == null) {
            this.dataConfig = new YamlConfiguration();
        }
        this.dataConfig.set("furnaces", null);
        for (Map.Entry<UUID, Map<String, Object>> entry : this.furnaceData.entrySet()) {
            UUID uuid = entry.getKey();
            Map<String, Object> data = entry.getValue();
            for (Map.Entry<String, Object> dataEntry : data.entrySet()) {
                this.dataConfig.set("furnaces." + uuid.toString() + "." + dataEntry.getKey(), dataEntry.getValue());
            }
        }
        try {
            this.dataConfig.save(this.dataFile);
            if (this.configManager.isDebugEnabled()) {
                this.plugin.getMessageUtil().logDebug("data.saved", "count", this.furnaceData.size());
            }
        } catch (IOException e) {
            this.plugin.getLogger().severe("\u4fdd\u5b58\u6570\u636e\u6587\u4ef6\u5931\u8d25: " + e.getMessage());
        }
    }

    private void startSaveTask() {
        int saveInterval = this.configManager.getConfig().getInt("save-interval", 300) * 20;
        if (this.saveTask != null) {
            this.saveTask.cancel();
        }
        this.saveTask = Bukkit.getScheduler().runTaskTimerAsynchronously((Plugin)this.plugin, this::saveData, (long)saveInterval, (long)saveInterval);
        if (this.configManager.isDebugEnabled()) {
            this.plugin.getMessageUtil().logDebug("data.autosave_started", "interval", saveInterval / 20);
        }
    }

    public void storeFurnaceData(Location location, CustomFurnaceData data) {
        String key = this.serializeLocation(location);
        HashMap<String, Object> furnaceData = new HashMap<String, Object>();
        furnaceData.put("type", data.getLevel().getType());
        furnaceData.put("level", data.getLevel().getLevel());
        furnaceData.put("uuid", data.getUuid().toString());
        furnaceData.put("payment_type", data.getPaymentType());
        furnaceData.put("world", location.getWorld().getName());
        furnaceData.put("x", location.getBlockX());
        furnaceData.put("y", location.getBlockY());
        furnaceData.put("z", location.getBlockZ());
        if (data.getHologramUUID() != null) {
            furnaceData.put("hologram_uuid", data.getHologramUUID().toString());
        }
        this.furnaceData.put(UUID.fromString(data.getUuid().toString()), furnaceData);
        this.locationToUuid.put(key, data.getUuid());
        this.saveData();
    }

    public void removeFurnaceData(Location location) {
        if (location == null) {
            return;
        }
        String locationKey = this.serializeLocation(location);
        UUID uuid = this.locationToUuid.remove(locationKey);
        if (uuid != null) {
            this.furnaceData.remove(uuid);
        }
    }

    public Map<String, Object> getFurnaceData(Location location) {
        if (location == null) {
            return null;
        }
        String locationKey = this.serializeLocation(location);
        UUID uuid = this.locationToUuid.get(locationKey);
        if (uuid == null) {
            return null;
        }
        return this.furnaceData.get(uuid);
    }

    public Map<String, Object> getFurnaceDataByUuid(UUID uuid) {
        if (uuid == null) {
            return null;
        }
        return this.furnaceData.get(uuid);
    }

    public void applyAllFurnaces() {
        ConfigurationSection furnacesSection;
        if (this.dataConfig == null) {
            return;
        }
        if (this.configManager.isArmorstandHologramEnabled()) {
            this.plugin.getServer().getWorlds().forEach(world -> world.getEntities().stream().filter(entity -> entity instanceof TextDisplay).map(entity -> (TextDisplay)entity).filter(textDisplay -> textDisplay.getText() != null).forEach(Entity::remove));
        }
        if ((furnacesSection = this.dataConfig.getConfigurationSection("furnaces")) == null) {
            return;
        }
        for (String uuidStr : furnacesSection.getKeys(false)) {
            ConfigurationSection furnaceSection = furnacesSection.getConfigurationSection(uuidStr);
            if (furnaceSection == null) continue;
            try {
                UUID hologramUUID;
                Block block;
                UUID uuid = UUID.fromString(uuidStr);
                HashMap<String, Object> data = new HashMap<String, Object>();
                for (String key : furnaceSection.getKeys(false)) {
                    data.put(key, furnaceSection.get(key));
                }
                String worldName = (String)data.get("world");
                int x = ((Number)data.get("x")).intValue();
                int y = ((Number)data.get("y")).intValue();
                int z = ((Number)data.get("z")).intValue();
                World world2 = Bukkit.getWorld((String)worldName);
                if (world2 == null || !this.isFurnaceType((block = world2.getBlockAt(x, y, z)).getType())) continue;
                String type = (String)data.get("type");
                int level = ((Number)data.get("level")).intValue();
                String paymentType = (String)data.get("payment_type");
                FurnaceManager furnaceManager = this.plugin.getFurnaceManager();
                CustomFurnaceData furnaceData = new CustomFurnaceData(this.plugin, furnaceManager.getFurnaceLevel(type, level, paymentType), uuid, paymentType);
                BlockState state = block.getState();
                furnaceData.applyToBlockState(state);
                if (state instanceof Furnace) {
                    Furnace furnace = (Furnace)state;
                    furnace.setCookTimeTotal(furnaceData.getLevel().getCookingTime());
                    furnace.update();
                }
                if (this.configManager.isArmorstandHologramEnabled() && (hologramUUID = this.plugin.getTextDisplayUtil().createOrUpdateFurnaceDisplay(block, furnaceData, 0, 0)) != null) {
                    furnaceData.setHologramUUID(hologramUUID);
                }
                this.storeFurnaceData(block.getLocation(), furnaceData);
                if (!this.configManager.isDebugEnabled()) continue;
                this.plugin.getMessageUtil().logDebug("data.furnace_applied", "location", worldName + "," + x + "," + y + "," + z, "type", type, "level", level, "payment_type", paymentType, "uuid", uuid.toString(), "hologram_uuid", furnaceData.getHologramUUID() != null ? furnaceData.getHologramUUID().toString() : "none");
            } catch (Exception e) {
                this.plugin.getLogger().warning("\u52a0\u8f7d\u7194\u7089\u6570\u636e\u5931\u8d25: " + e.getMessage());
            }
        }
    }

    private String serializeLocation(Location location) {
        return location.getWorld().getName() + "," + location.getBlockX() + "," + location.getBlockY() + "," + location.getBlockZ();
    }

    private boolean isFurnaceType(Material material) {
        return material == Material.FURNACE || material == Material.BLAST_FURNACE || material == Material.SMOKER;
    }

    public void shutdown() {
        if (this.saveTask != null) {
            this.saveTask.cancel();
        }
        this.saveData();
        if (this.configManager.isDebugEnabled()) {
            this.plugin.getMessageUtil().logDebug("data.shutdown", new Object[0]);
        }
    }

    private boolean isLegacyFurnace(Furnace furnace) {
        try {
            furnace.getCookTimeTotal();
            return false;
        } catch (NoSuchMethodError e) {
            return true;
        }
    }

    public void updateFurnaceProgressBars() {
        try {
            if (this.configManager.isDebugEnabled()) {
                this.plugin.getLogger().info("\u8c03\u8bd5 - \u5f00\u59cb\u66f4\u65b0\u7194\u7089\u8fdb\u5ea6\u6761\uff0c\u5171\u6709\u7194\u7089\u6570\u91cf: " + this.locationToUuid.size());
            }
            for (Map.Entry<String, UUID> entry : this.locationToUuid.entrySet()) {
                String locationKey = entry.getKey();
                UUID uuid = entry.getValue();
                try {
                    BlockState blockState;
                    Block block;
                    String[] parts = locationKey.split(",");
                    if (parts.length < 4) continue;
                    String worldName = parts[0];
                    int x = Integer.parseInt(parts[1]);
                    int y = Integer.parseInt(parts[2]);
                    int z = Integer.parseInt(parts[3]);
                    World world = this.plugin.getServer().getWorld(worldName);
                    if (world == null || !this.isFurnaceType((block = world.getBlockAt(x, y, z)).getType()) || !((blockState = block.getState()) instanceof Furnace)) continue;
                    Furnace furnaceBlock = (Furnace)blockState;
                    Map<String, Object> data = this.getFurnaceDataByUuid(uuid);
                    if (data == null || data.isEmpty()) continue;
                    short cookTime = furnaceBlock.getCookTime();
                    int cookTimeTotal = furnaceBlock.getCookTimeTotal();
                    short burnTime = furnaceBlock.getBurnTime();
                    if (this.configManager.isDebugEnabled()) {
                        this.plugin.getLogger().info("\u8c03\u8bd5 - \u7194\u7089\u72b6\u6001: \u4f4d\u7f6e=" + locationKey + ", \u70e7\u5236\u8fdb\u5ea6=" + cookTime + "/" + cookTimeTotal + ", \u71c3\u70e7\u65f6\u95f4=" + burnTime);
                    }
                    if (cookTimeTotal <= 0) {
                        if (burnTime > 0) {
                            cookTimeTotal = 200;
                            if (cookTime <= 0) {
                                cookTime = 1;
                            }
                        } else {
                            cookTime = 0;
                        }
                    }
                    if (!data.containsKey("hologram_uuid")) continue;
                    String hologramUuidStr = (String)data.get("hologram_uuid");
                    try {
                        UUID hologramUuid = UUID.fromString(hologramUuidStr);
                        boolean updated = this.plugin.getTextDisplayUtil().updateProgressBar(block, hologramUuid, cookTime, cookTimeTotal);
                        if (!updated || !this.configManager.isDebugEnabled()) continue;
                        this.plugin.getMessageUtil().logDebug("progress_bar.update_success", "location", locationKey, "progress", String.valueOf(cookTime), "total", String.valueOf(cookTimeTotal), "percentage", String.format("%.1f%%", cookTimeTotal > 0 ? (double)cookTime / (double)cookTimeTotal * 100.0 : 0.0));
                    } catch (IllegalArgumentException illegalArgumentException) {
                    }
                } catch (Exception e) {
                    if (!this.configManager.isDebugEnabled()) continue;
                    this.plugin.getLogger().warning("\u66f4\u65b0\u7194\u7089\u8fdb\u5ea6\u6761\u65f6\u51fa\u73b0\u9519\u8bef: " + e.getMessage());
                    e.printStackTrace();
                }
            }
        } catch (Exception e) {
            this.plugin.getLogger().warning("\u66f4\u65b0\u5168\u90e8\u7194\u7089\u8fdb\u5ea6\u6761\u65f6\u51fa\u73b0\u9519\u8bef: " + e.getMessage());
            if (this.configManager.isDebugEnabled()) {
                e.printStackTrace();
            }
        }
    }
}

