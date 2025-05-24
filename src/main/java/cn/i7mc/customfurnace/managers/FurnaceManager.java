package cn.i7mc.customfurnace.managers;

import cn.i7mc.customfurnace.models.CustomFurnaceData;
import cn.i7mc.customfurnace.models.FurnaceLevel;
import cn.i7mc.customfurnace.utils.ItemBuilder;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 熔炉管理器
 */
public class FurnaceManager {
    private final JavaPlugin plugin;
    private final ConfigManager configManager;
    private final LangManager langManager;

    // 缓存各类型熔炉的各等级数据（按支付方式区分）
    private final Map<String, Map<String, Map<Integer, FurnaceLevel>>> furnaceLevels = new HashMap<>();

    // 支付类型常量
    public static final String PAYMENT_VAULT = "vault";
    public static final String PAYMENT_POINTS = "points";

    public FurnaceManager(JavaPlugin plugin, ConfigManager configManager, LangManager langManager) {
        this.plugin = plugin;
        this.configManager = configManager;
        this.langManager = langManager;

        loadFurnaceData();
    }

    /**
     * 加载所有熔炉数据
     */
    public void loadFurnaceData() {
        furnaceLevels.clear();

        FileConfiguration furnacesConfig = configManager.getFurnacesConfig();
        ConfigurationSection furnacesSection = furnacesConfig.getConfigurationSection("furnaces");

        if (furnacesSection == null) {
            plugin.getLogger().warning("熔炉配置不完整，无法加载熔炉数据！");
            return;
        }

        // 遍历所有熔炉类型
        for (String furnaceType : furnacesSection.getKeys(false)) {
            ConfigurationSection typeSection = furnacesSection.getConfigurationSection(furnaceType);
            if (typeSection == null) continue;

            // 创建该熔炉类型的支付方式映射
            Map<String, Map<Integer, FurnaceLevel>> paymentTypes = new HashMap<>();
            furnaceLevels.put(furnaceType, paymentTypes);

            // 处理金币升级
            loadPaymentTypeData(furnaceType, typeSection, PAYMENT_VAULT);

            // 处理点券升级
            loadPaymentTypeData(furnaceType, typeSection, PAYMENT_POINTS);
        }
    }

    /**
     * 加载指定支付方式的等级数据
     */
    private void loadPaymentTypeData(String furnaceType, ConfigurationSection typeSection, String paymentType) {
        ConfigurationSection paymentSection = typeSection.getConfigurationSection(paymentType);
        if (paymentSection == null) {
            plugin.getLogger().warning("熔炉 " + furnaceType + " 缺少 " + paymentType + " 配置");
            return;
        }

        ConfigurationSection speedSection = paymentSection.getConfigurationSection("speed");
        ConfigurationSection costSection = paymentSection.getConfigurationSection("upgrade-cost");
        int maxLevel = paymentSection.getInt("max-level", 1);

        if (speedSection == null || costSection == null) {
            plugin.getLogger().warning("熔炉 " + furnaceType + " 的 " + paymentType + " 配置不完整");
            return;
        }

        Map<Integer, FurnaceLevel> levels = new HashMap<>();
        furnaceLevels.get(furnaceType).put(paymentType, levels);

        // 加载所有等级数据
        for (String levelKey : speedSection.getKeys(false)) {
            try {
                int level = Integer.parseInt(levelKey);
                int cookingTime = speedSection.getInt(levelKey);

                // 获取升级成本
                int upgradeCost = 0;
                String costKey = level + "-" + (level + 1);
                if (level < maxLevel) {
                    upgradeCost = costSection.getInt(costKey, 0);
                }

                // 创建FurnaceLevel对象
                FurnaceLevel furnaceLevel = new FurnaceLevel(
                    furnaceType,
                    level,
                    cookingTime,
                    paymentType.equals(PAYMENT_VAULT) ? upgradeCost : 0,
                    paymentType.equals(PAYMENT_POINTS) ? upgradeCost : 0
                );

                levels.put(level, furnaceLevel);
            } catch (NumberFormatException e) {
                plugin.getLogger().warning("熔炉等级格式错误: " + levelKey);
            }
        }
    }

    /**
     * 获取熔炉等级信息
     */
    public FurnaceLevel getFurnaceLevel(String type, int level, String paymentType) {
        Map<String, Map<Integer, FurnaceLevel>> paymentTypes = furnaceLevels.get(type);
        if (paymentTypes != null) {
            Map<Integer, FurnaceLevel> levels = paymentTypes.get(paymentType);
            if (levels != null) {
                return levels.get(level);
            }
        }
        return null;
    }

    /**
     * 向后兼容的获取熔炉等级信息方法
     * @deprecated 使用 getFurnaceLevel(String, int, String) 代替
     */
    @Deprecated
    public FurnaceLevel getFurnaceLevel(String type, int level) {
        return getFurnaceLevel(type, level, PAYMENT_VAULT);
    }

    /**
     * 获取熔炉最高等级
     */
    public int getMaxLevel(String type, String paymentType) {
        Map<String, Map<Integer, FurnaceLevel>> paymentTypes = furnaceLevels.get(type);
        if (paymentTypes != null) {
            Map<Integer, FurnaceLevel> levels = paymentTypes.get(paymentType);
            if (levels != null && !levels.isEmpty()) {
                return levels.keySet().stream().max(Integer::compareTo).orElse(1);
            }
        }
        return 1;
    }

    /**
     * 向后兼容的获取最高等级方法
     * @deprecated 使用 getMaxLevel(String, String) 代替
     */
    @Deprecated
    public int getMaxLevel(String type) {
        return getMaxLevel(type, PAYMENT_VAULT);
    }

    /**
     * 创建熔炉物品
     */
    public ItemStack createFurnaceItem(String type, int level, String paymentType) {
        FurnaceLevel furnaceLevel = getFurnaceLevel(type, level, paymentType);
        if (furnaceLevel == null) {
            return null;
        }

        Material material;
        try {
            material = Material.valueOf(type.toUpperCase());
        } catch (IllegalArgumentException e) {
            material = Material.FURNACE;
        }

        // 获取自定义名字
        String customName = configManager.getFurnacesConfig().getString("furnaces." + type + ".display_name." + furnaceLevel.getLevel());
        if (customName == null) {
            customName = langManager.getRawMessage("furnace.name");
        }

        // 获取悬浮显示格式
        String paymentTypeText = langManager.getRawMessage("payment_types." + paymentType);
        String hoverDisplay = langManager.getRawMessage("furnace.hover_display")
                .replace("%display%", customName)
                .replace("%level%", String.valueOf(furnaceLevel.getLevel()))
                .replace("%payment_type%", paymentTypeText)
                .replace("%speed%", String.valueOf(furnaceLevel.getCookingTime()));

        List<String> lore = langManager.getLore("furnace.lore",
                "level", furnaceLevel.getLevel(),
                "speed", furnaceLevel.getCookingTime(),
                "payment_type", paymentTypeText);

        ItemStack item = new ItemBuilder(material)
                .name(langManager.colorize(customName))
                .lore(lore)
                .displayName(langManager.colorize(hoverDisplay))  // 设置悬浮显示
                .build();

        // 应用自定义数据，确保传递支付类型
        CustomFurnaceData data = new CustomFurnaceData(plugin, furnaceLevel, paymentType);
        return data.applyToItem(item);
    }

    /**
     * 向后兼容的创建熔炉物品方法
     * @deprecated 使用 createFurnaceItem(String, int, String) 代替
     */
    @Deprecated
    public ItemStack createFurnaceItem(String type, int level) {
        return createFurnaceItem(type, level, PAYMENT_VAULT);
    }

    /**
     * 检查是否可以升级
     */
    public boolean canUpgrade(String type, int currentLevel, String paymentType) {
        int maxLevel = getMaxLevel(type, paymentType);
        return currentLevel < maxLevel;
    }

    /**
     * 向后兼容的检查升级方法
     * @deprecated 使用 canUpgrade(String, int, String) 代替
     */
    @Deprecated
    public boolean canUpgrade(String type, int currentLevel) {
        return canUpgrade(type, currentLevel, PAYMENT_VAULT);
    }

    /**
     * 获取下一级熔炉信息
     */
    public FurnaceLevel getNextLevel(String type, int currentLevel, String paymentType) {
        if (!canUpgrade(type, currentLevel, paymentType)) {
            return null;
        }

        return getFurnaceLevel(type, currentLevel + 1, paymentType);
    }

    /**
     * 向后兼容的获取下一级方法
     * @deprecated 使用 getNextLevel(String, int, String) 代替
     */
    @Deprecated
    public FurnaceLevel getNextLevel(String type, int currentLevel) {
        return getNextLevel(type, currentLevel, PAYMENT_VAULT);
    }

    /**
     * 获取金币升级成本
     */
    public int getVaultUpgradeCost(String type, int currentLevel) {
        FurnaceLevel level = getFurnaceLevel(type, currentLevel, PAYMENT_VAULT);
        return level != null ? level.getVaultCost() : 0;
    }

    /**
     * 获取点券升级成本
     */
    public int getPointsUpgradeCost(String type, int currentLevel) {
        FurnaceLevel level = getFurnaceLevel(type, currentLevel, PAYMENT_POINTS);
        return level != null ? level.getPointsCost() : 0;
    }

    /**
     * 获取升级成本（向后兼容）
     * @deprecated 使用 getVaultUpgradeCost 或 getPointsUpgradeCost 代替
     */
    @Deprecated
    public int getUpgradeCost(String type, int currentLevel) {
        return getVaultUpgradeCost(type, currentLevel);
    }
}