package cn.i7mc.customfurnace.managers;

import cn.i7mc.customfurnace.managers.ConfigManager;
import cn.i7mc.customfurnace.managers.LangManager;
import cn.i7mc.customfurnace.models.CustomFurnaceData;
import cn.i7mc.customfurnace.models.FurnaceLevel;
import cn.i7mc.customfurnace.utils.ItemBuilder;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

public class FurnaceManager {
    private final JavaPlugin plugin;
    private final ConfigManager configManager;
    private final LangManager langManager;
    private final Map<String, Map<String, Map<Integer, FurnaceLevel>>> furnaceLevels = new HashMap<String, Map<String, Map<Integer, FurnaceLevel>>>();
    public static final String PAYMENT_VAULT = "vault";
    public static final String PAYMENT_POINTS = "points";

    public FurnaceManager(JavaPlugin plugin, ConfigManager configManager, LangManager langManager) {
        this.plugin = plugin;
        this.configManager = configManager;
        this.langManager = langManager;
        this.loadFurnaceData();
    }

    public void loadFurnaceData() {
        this.furnaceLevels.clear();
        FileConfiguration furnacesConfig = this.configManager.getFurnacesConfig();
        ConfigurationSection furnacesSection = furnacesConfig.getConfigurationSection("furnaces");
        if (furnacesSection == null) {
            this.plugin.getLogger().warning("\u7194\u7089\u914d\u7f6e\u4e0d\u5b8c\u6574\uff0c\u65e0\u6cd5\u52a0\u8f7d\u7194\u7089\u6570\u636e\uff01");
            return;
        }
        for (String furnaceType : furnacesSection.getKeys(false)) {
            ConfigurationSection typeSection = furnacesSection.getConfigurationSection(furnaceType);
            if (typeSection == null) continue;
            HashMap paymentTypes = new HashMap();
            this.furnaceLevels.put(furnaceType, paymentTypes);
            this.loadPaymentTypeData(furnaceType, typeSection, PAYMENT_VAULT);
            this.loadPaymentTypeData(furnaceType, typeSection, PAYMENT_POINTS);
        }
    }

    private void loadPaymentTypeData(String furnaceType, ConfigurationSection typeSection, String paymentType) {
        ConfigurationSection paymentSection = typeSection.getConfigurationSection(paymentType);
        if (paymentSection == null) {
            this.plugin.getLogger().warning("\u7194\u7089 " + furnaceType + " \u7f3a\u5c11 " + paymentType + " \u914d\u7f6e");
            return;
        }
        ConfigurationSection speedSection = paymentSection.getConfigurationSection("speed");
        ConfigurationSection costSection = paymentSection.getConfigurationSection("upgrade-cost");
        int maxLevel = paymentSection.getInt("max-level", 1);
        if (speedSection == null || costSection == null) {
            this.plugin.getLogger().warning("\u7194\u7089 " + furnaceType + " \u7684 " + paymentType + " \u914d\u7f6e\u4e0d\u5b8c\u6574");
            return;
        }
        HashMap<Integer, FurnaceLevel> levels = new HashMap<Integer, FurnaceLevel>();
        this.furnaceLevels.get(furnaceType).put(paymentType, levels);
        for (String levelKey : speedSection.getKeys(false)) {
            try {
                int level = Integer.parseInt(levelKey);
                int cookingTime = speedSection.getInt(levelKey);
                int upgradeCost = 0;
                String costKey = level + "-" + (level + 1);
                if (level < maxLevel) {
                    upgradeCost = costSection.getInt(costKey, 0);
                }
                FurnaceLevel furnaceLevel = new FurnaceLevel(furnaceType, level, cookingTime, paymentType.equals(PAYMENT_VAULT) ? upgradeCost : 0, paymentType.equals(PAYMENT_POINTS) ? upgradeCost : 0);
                levels.put(level, furnaceLevel);
            } catch (NumberFormatException e) {
                this.plugin.getLogger().warning("\u7194\u7089\u7b49\u7ea7\u683c\u5f0f\u9519\u8bef: " + levelKey);
            }
        }
    }

    public FurnaceLevel getFurnaceLevel(String type, int level, String paymentType) {
        Map<Integer, FurnaceLevel> levels;
        Map<String, Map<Integer, FurnaceLevel>> paymentTypes = this.furnaceLevels.get(type);
        if (paymentTypes != null && (levels = paymentTypes.get(paymentType)) != null) {
            return levels.get(level);
        }
        return null;
    }

    @Deprecated
    public FurnaceLevel getFurnaceLevel(String type, int level) {
        return this.getFurnaceLevel(type, level, PAYMENT_VAULT);
    }

    public int getMaxLevel(String type, String paymentType) {
        Map<Integer, FurnaceLevel> levels;
        Map<String, Map<Integer, FurnaceLevel>> paymentTypes = this.furnaceLevels.get(type);
        if (paymentTypes != null && (levels = paymentTypes.get(paymentType)) != null && !levels.isEmpty()) {
            return levels.keySet().stream().max(Integer::compareTo).orElse(1);
        }
        return 1;
    }

    @Deprecated
    public int getMaxLevel(String type) {
        return this.getMaxLevel(type, PAYMENT_VAULT);
    }

    public ItemStack createFurnaceItem(String type, int level, String paymentType) {
        Material material;
        FurnaceLevel furnaceLevel = this.getFurnaceLevel(type, level, paymentType);
        if (furnaceLevel == null) {
            return null;
        }
        try {
            material = Material.valueOf((String)type.toUpperCase());
        } catch (IllegalArgumentException e) {
            material = Material.FURNACE;
        }
        String customName = this.configManager.getFurnacesConfig().getString("furnaces." + type + ".display_name." + furnaceLevel.getLevel());
        if (customName == null) {
            customName = this.langManager.getRawMessage("furnace.name");
        }
        String hoverDisplay = this.langManager.getRawMessage("furnace.dropped_item_hud").replace("%display%", customName).replace("%level%", String.valueOf(furnaceLevel.getLevel())).replace("%payment_type%", paymentType.equals(PAYMENT_VAULT) ? "\u91d1\u5e01" : "\u70b9\u5238").replace("%speed%", String.valueOf(furnaceLevel.getCookingTime()));
        List<String> lore = this.langManager.getLore("furnace.lore", "level", furnaceLevel.getLevel(), "speed", furnaceLevel.getCookingTime(), "payment_type", paymentType.equals(PAYMENT_VAULT) ? "\u91d1\u5e01" : "\u70b9\u5238");
        ItemStack item = new ItemBuilder(material).name(this.langManager.colorize(customName)).lore(lore).displayName(this.langManager.colorize(hoverDisplay)).build();
        CustomFurnaceData data = new CustomFurnaceData(this.plugin, furnaceLevel, paymentType);
        return data.applyToItem(item);
    }

    @Deprecated
    public ItemStack createFurnaceItem(String type, int level) {
        return this.createFurnaceItem(type, level, PAYMENT_VAULT);
    }

    public boolean canUpgrade(String type, int currentLevel, String paymentType) {
        int maxLevel = this.getMaxLevel(type, paymentType);
        return currentLevel < maxLevel;
    }

    @Deprecated
    public boolean canUpgrade(String type, int currentLevel) {
        return this.canUpgrade(type, currentLevel, PAYMENT_VAULT);
    }

    public FurnaceLevel getNextLevel(String type, int currentLevel, String paymentType) {
        if (!this.canUpgrade(type, currentLevel, paymentType)) {
            return null;
        }
        return this.getFurnaceLevel(type, currentLevel + 1, paymentType);
    }

    @Deprecated
    public FurnaceLevel getNextLevel(String type, int currentLevel) {
        return this.getNextLevel(type, currentLevel, PAYMENT_VAULT);
    }

    public int getVaultUpgradeCost(String type, int currentLevel) {
        FurnaceLevel level = this.getFurnaceLevel(type, currentLevel, PAYMENT_VAULT);
        return level != null ? level.getVaultCost() : 0;
    }

    public int getPointsUpgradeCost(String type, int currentLevel) {
        FurnaceLevel level = this.getFurnaceLevel(type, currentLevel, PAYMENT_POINTS);
        return level != null ? level.getPointsCost() : 0;
    }

    @Deprecated
    public int getUpgradeCost(String type, int currentLevel) {
        return this.getVaultUpgradeCost(type, currentLevel);
    }
}

