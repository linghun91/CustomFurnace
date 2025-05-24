package cn.i7mc.customfurnace.managers;

import cn.i7mc.customfurnace.CustomFurnace;
import cn.i7mc.customfurnace.models.CustomFurnaceData;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.TextDisplay;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

/**
 * TextDisplay全息显示管理器
 * 统一管理所有全息显示功能，使用现代化的TextDisplay API
 */
public class HologramManager {
    private final CustomFurnace plugin;
    private final ConfigManager configManager;
    private final LangManager langManager;
    private FileConfiguration hologramConfig;
    private File hologramFile;
    private final MiniMessage miniMessage;
    private final LegacyComponentSerializer legacySerializer;

    public HologramManager(CustomFurnace plugin) {
        this.plugin = plugin;
        this.configManager = plugin.getConfigManager();
        this.langManager = plugin.getLangManager();
        this.miniMessage = MiniMessage.miniMessage();
        this.legacySerializer = LegacyComponentSerializer.legacyAmpersand();

        // 初始化全息图配置
        initHologramConfig();
    }

    /**
     * 初始化全息图配置文件
     */
    private void initHologramConfig() {
        hologramFile = new File(plugin.getDataFolder(), "hologram.yml");
        if (!hologramFile.exists()) {
            plugin.saveResource("hologram.yml", false);
        }
        hologramConfig = YamlConfiguration.loadConfiguration(hologramFile);
    }

    /**
     * 重载全息图配置
     */
    public void reloadHologramConfig() {
        hologramConfig = YamlConfiguration.loadConfiguration(hologramFile);
    }

    /**
     * 保存全息图配置
     */
    public void saveHologramConfig() {
        try {
            hologramConfig.save(hologramFile);
        } catch (IOException e) {
            plugin.getLogger().severe("保存全息图配置文件失败: " + e.getMessage());
        }
    }

    /**
     * 创建熔炉全息显示
     */
    public TextDisplay createFurnaceHologram(Location location, CustomFurnaceData data) {
        if (!hologramConfig.getBoolean("hologram.enabled", true) ||
            !configManager.isTextDisplayHologramEnabled()) {
            return null;
        }

        // 获取显示文本
        Component displayText = createHologramText(data);

        // 从配置文件获取位置偏移
        double xOffset = hologramConfig.getDouble("hologram.position.x_offset", 0.5);
        double yOffset = hologramConfig.getDouble("hologram.position.y_offset", 0.9);
        double zOffset = hologramConfig.getDouble("hologram.position.z_offset", 0.5);

        // 创建全息图位置
        Location hologramLocation = location.clone().add(xOffset, yOffset, zOffset);
        TextDisplay textDisplay = (TextDisplay) location.getWorld().spawnEntity(
            hologramLocation,
            EntityType.TEXT_DISPLAY
        );

        // 配置TextDisplay属性
        configureTextDisplay(textDisplay, displayText);

        // 添加自定义NBT标签标记这是我们的全息图
        textDisplay.getPersistentDataContainer().set(
            new org.bukkit.NamespacedKey(plugin, "customfurnace_hologram"),
            org.bukkit.persistence.PersistentDataType.STRING,
            "true"
        );

        // 记录调试日志
        plugin.getMessageUtil().logDebug("hologram.created",
            "location", String.format("%.2f,%.2f,%.2f",
                hologramLocation.getX(), hologramLocation.getY(), hologramLocation.getZ()),
            "uuid", textDisplay.getUniqueId().toString());

        return textDisplay;
    }

    /**
     * 更新现有全息显示的文本内容
     */
    public void updateHologramText(TextDisplay textDisplay, CustomFurnaceData data) {
        if (textDisplay == null || textDisplay.isDead()) {
            return;
        }

        Component displayText = createHologramText(data);
        textDisplay.text(displayText);

        // 记录调试日志
        plugin.getMessageUtil().logDebug("hologram.text_updated",
            "uuid", textDisplay.getUniqueId().toString());
    }

    /**
     * 创建全息显示文本组件
     */
    private Component createHologramText(CustomFurnaceData data) {
        // 获取自定义名字
        String customName = configManager.getFurnacesConfig()
            .getString("furnaces." + data.getLevel().getType() + ".display_name." + data.getLevel().getLevel());
        if (customName == null) {
            customName = langManager.getRawMessage("furnace.name");
        }

        // 获取悬浮显示格式
        String paymentTypeText = langManager.getRawMessage("payment_types." + data.getPaymentType());
        String hoverDisplay = langManager.getRawMessage("furnace.hover_display")
            .replace("%display%", customName)
            .replace("%level%", String.valueOf(data.getLevel().getLevel()))
            .replace("%payment_type%", paymentTypeText)
            .replace("%speed%", String.valueOf(data.getLevel().getCookingTime()));

        // 不在文本中添加标识符，只使用NBT标签识别
        // 如果启用调试模式，才在文本中显示标识符
        boolean showMarker = hologramConfig.getBoolean("security.show_marker", false);
        if (showMarker) {
            String uniqueMarker = hologramConfig.getString("security.unique_marker", "[CF]");
            hoverDisplay = hoverDisplay + " " + uniqueMarker;
        }

        // 转换颜色代码并创建Component
        return parseColoredText(hoverDisplay);
    }

    /**
     * 配置TextDisplay的显示属性
     */
    private void configureTextDisplay(TextDisplay textDisplay, Component displayText) {
        // 设置文本内容
        textDisplay.text(displayText);

        // 从配置文件获取文本样式设置
        String alignment = hologramConfig.getString("text_style.alignment", "CENTER");
        textDisplay.setAlignment(TextDisplay.TextAlignment.valueOf(alignment.toUpperCase()));

        textDisplay.setLineWidth(hologramConfig.getInt("text_style.line_width", 200));
        textDisplay.setShadowed(hologramConfig.getBoolean("text_style.shadowed", true));
        textDisplay.setSeeThrough(hologramConfig.getBoolean("text_style.see_through", false));
        textDisplay.setDefaultBackground(hologramConfig.getBoolean("text_style.default_background", false));

        // 设置文本透明度
        int textOpacity = hologramConfig.getInt("text_style.text_opacity", 255);
        if (textOpacity != -1) {
            textDisplay.setTextOpacity((byte) Math.max(0, Math.min(255, textOpacity)));
        }

        // 设置背景颜色
        String bgColorStr = hologramConfig.getString("text_style.background_color");
        if (bgColorStr != null && !bgColorStr.equals("null")) {
            Color bgColor = parseColor(bgColorStr);
            if (bgColor != null) {
                textDisplay.setBackgroundColor(bgColor);
            }
        }

        // 从配置文件获取显示属性
        String billboard = hologramConfig.getString("display_properties.billboard", "CENTER");
        textDisplay.setBillboard(org.bukkit.entity.Display.Billboard.valueOf(billboard.toUpperCase()));

        textDisplay.setDisplayWidth((float) hologramConfig.getDouble("display_properties.display_width", 2.0));
        textDisplay.setDisplayHeight((float) hologramConfig.getDouble("display_properties.display_height", 0.5));
        textDisplay.setViewRange((float) hologramConfig.getDouble("display_properties.view_range", 64.0));

        // 设置发光颜色
        String glowColorStr = hologramConfig.getString("display_properties.glow_color_override");
        if (glowColorStr != null && !glowColorStr.equals("null")) {
            Color glowColor = parseColor(glowColorStr);
            if (glowColor != null) {
                textDisplay.setGlowColorOverride(glowColor);
            }
        }

        // 设置亮度 (解决字体颜色偏暗问题)
        if (hologramConfig.getBoolean("display_properties.brightness.enabled", true)) {
            int blockLight = hologramConfig.getInt("display_properties.brightness.block_light", 15);
            int skyLight = hologramConfig.getInt("display_properties.brightness.sky_light", 15);

            // 确保光照值在有效范围内 (0-15)
            blockLight = Math.max(0, Math.min(15, blockLight));
            skyLight = Math.max(0, Math.min(15, skyLight));

            // 创建亮度对象并设置
            org.bukkit.entity.Display.Brightness brightness =
                new org.bukkit.entity.Display.Brightness(blockLight, skyLight);
            textDisplay.setBrightness(brightness);

            plugin.getMessageUtil().logDebug("hologram.brightness_set",
                "block_light", String.valueOf(blockLight),
                "sky_light", String.valueOf(skyLight));
        }

        // 从配置文件获取阴影效果
        textDisplay.setShadowRadius((float) hologramConfig.getDouble("shadow.shadow_radius", 0.3));
        textDisplay.setShadowStrength((float) hologramConfig.getDouble("shadow.shadow_strength", 0.8));

        // 从配置文件获取动画插值
        textDisplay.setInterpolationDuration(hologramConfig.getInt("animation.interpolation_duration", 10));
        textDisplay.setInterpolationDelay(hologramConfig.getInt("animation.interpolation_delay", 0));

        // 从配置文件获取实体属性
        textDisplay.setGravity(hologramConfig.getBoolean("entity_properties.gravity", false));
        textDisplay.setPersistent(hologramConfig.getBoolean("entity_properties.persistent", true));
        textDisplay.setSilent(hologramConfig.getBoolean("entity_properties.silent", true));
        textDisplay.setGlowing(hologramConfig.getBoolean("entity_properties.glowing", false));
        textDisplay.setInvulnerable(hologramConfig.getBoolean("entity_properties.invulnerable", true));
    }

    /**
     * 移除指定位置附近的全息显示
     */
    public void removeHologramAt(Location location) {
        if (!hologramConfig.getBoolean("hologram.enabled", true) ||
            !configManager.isTextDisplayHologramEnabled()) {
            return;
        }

        // 从配置文件获取位置偏移，与创建时保持一致
        double xOffset = hologramConfig.getDouble("hologram.position.x_offset", 0.5);
        double yOffset = hologramConfig.getDouble("hologram.position.y_offset", 0.9);
        double zOffset = hologramConfig.getDouble("hologram.position.z_offset", 0.5);

        Location searchLocation = location.clone().add(xOffset, yOffset, zOffset);

        // 扩大搜索范围，确保能找到全息图
        double searchRadius = 2.0;
        location.getWorld().getNearbyEntities(searchLocation, searchRadius, searchRadius, searchRadius).stream()
            .filter(entity -> entity instanceof TextDisplay)
            .map(entity -> (TextDisplay) entity)
            .filter(textDisplay -> isCustomFurnaceHologram(textDisplay, searchLocation))
            .forEach(textDisplay -> {
                textDisplay.remove();
                plugin.getMessageUtil().logDebug("hologram.removed",
                    "location", String.format("%.2f,%.2f,%.2f",
                        textDisplay.getLocation().getX(),
                        textDisplay.getLocation().getY(),
                        textDisplay.getLocation().getZ()));
            });
    }

    /**
     * 通过UUID移除全息显示
     */
    public void removeHologramByUUID(UUID hologramUUID) {
        if (hologramUUID == null) {
            return;
        }

        boolean removed = false;
        for (org.bukkit.World world : plugin.getServer().getWorlds()) {
            for (org.bukkit.entity.Entity entity : world.getEntities()) {
                if (entity instanceof TextDisplay && entity.getUniqueId().equals(hologramUUID)) {
                    entity.remove();
                    removed = true;
                    plugin.getMessageUtil().logDebug("hologram.removed_by_uuid",
                        "uuid", hologramUUID.toString());
                    break;
                }
            }
            if (removed) break;
        }
    }

    /**
     * 强力清理指定位置的所有全息图（用于确保清理）
     */
    public void forceRemoveHologramsAt(Location location) {
        if (location == null || location.getWorld() == null) {
            return;
        }

        // 使用更大的搜索范围
        double searchRadius = 3.0;
        Location center = location.clone().add(0.5, 1.0, 0.5);

        location.getWorld().getNearbyEntities(center, searchRadius, searchRadius, searchRadius).stream()
            .filter(entity -> entity instanceof TextDisplay)
            .map(entity -> (TextDisplay) entity)
            .filter(this::isCustomFurnaceHologram)
            .forEach(textDisplay -> {
                textDisplay.remove();
                plugin.getMessageUtil().logDebug("hologram.force_removed",
                    "location", String.format("%.2f,%.2f,%.2f",
                        textDisplay.getLocation().getX(),
                        textDisplay.getLocation().getY(),
                        textDisplay.getLocation().getZ()));
            });
    }

    /**
     * 清理所有自定义熔炉的全息显示
     */
    public void clearAllHolograms() {
        plugin.getServer().getWorlds().forEach(world -> {
            world.getEntities().stream()
                .filter(entity -> entity instanceof TextDisplay)
                .map(entity -> (TextDisplay) entity)
                .filter(this::isCustomFurnaceHologram)
                .forEach(TextDisplay::remove);
        });
    }

    /**
     * 检查TextDisplay是否为自定义熔炉的全息显示
     * 使用多重验证确保只识别我们自己创建的全息图，绝对不会误删其他插件的全息图
     */
    private boolean isCustomFurnaceHologram(TextDisplay textDisplay) {
        // 方法1：检查NBT标签（最安全的方法）
        org.bukkit.NamespacedKey key = new org.bukkit.NamespacedKey(plugin, "customfurnace_hologram");
        if (textDisplay.getPersistentDataContainer().has(key, org.bukkit.persistence.PersistentDataType.STRING)) {
            String value = textDisplay.getPersistentDataContainer().get(key, org.bukkit.persistence.PersistentDataType.STRING);
            if ("true".equals(value)) {
                return true;
            }
        }

        // 方法2：检查文本标识符（备用方法，用于兼容旧版本）
        Component text = textDisplay.text();
        if (text != null) {
            // 将Component转换为纯文本进行检查
            String textContent = net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer.plainText().serialize(text);

            // 获取配置的唯一标识符（仅在调试模式下使用）
            String uniqueMarker = hologramConfig.getString("security.unique_marker", "[CF]");
            // 提取标识符中的关键部分（去掉颜色代码）
            String markerKey = uniqueMarker.replaceAll("§[0-9a-fk-or]", "");

            // 检查是否包含我们的唯一标识符
            if (textContent.contains(markerKey)) {
                return true;
            }
        }

        // 如果两种方法都没有匹配，则不是我们的全息图
        return false;
    }

    /**
     * 检查TextDisplay是否为指定位置的自定义熔炉全息显示
     */
    private boolean isCustomFurnaceHologram(TextDisplay textDisplay, Location expectedLocation) {
        // 首先检查是否为自定义熔炉全息显示
        if (!isCustomFurnaceHologram(textDisplay)) {
            return false;
        }

        // 检查位置是否匹配（允许较大误差，因为配置可能改变）
        Location displayLocation = textDisplay.getLocation();
        double tolerance = 1.5; // 增加容错范围
        return Math.abs(displayLocation.getX() - expectedLocation.getX()) < tolerance &&
               Math.abs(displayLocation.getY() - expectedLocation.getY()) < tolerance &&
               Math.abs(displayLocation.getZ() - expectedLocation.getZ()) < tolerance;
    }

    /**
     * 解析带颜色代码的文本为Component
     */
    private Component parseColoredText(String text) {
        if (text == null || text.isEmpty()) {
            return Component.empty();
        }

        // 检查是否启用颜色代码解析
        if (!hologramConfig.getBoolean("color_codes.enabled", true)) {
            return Component.text(text);
        }

        Component component;
        String format = hologramConfig.getString("color_codes.format", "LEGACY");

        try {
            if ("MINIMESSAGE".equalsIgnoreCase(format)) {
                // 使用MiniMessage格式解析
                component = miniMessage.deserialize(text);
            } else {
                // 使用传统&代码格式解析
                component = legacySerializer.deserialize(text);
            }
        } catch (Exception e) {
            // 解析失败时使用纯文本
            plugin.getMessageUtil().logDebug("hologram.color_parse_error",
                "text", text,
                "error", e.getMessage());
            component = Component.text(text.replaceAll("&[0-9a-fk-or]", ""));
        }

        // 应用文本格式化
        if (hologramConfig.getBoolean("text_formatting.bold", false)) {
            component = component.decoration(TextDecoration.BOLD, true);
        }
        if (hologramConfig.getBoolean("text_formatting.italic", false)) {
            component = component.decoration(TextDecoration.ITALIC, true);
        }
        if (hologramConfig.getBoolean("text_formatting.underlined", false)) {
            component = component.decoration(TextDecoration.UNDERLINED, true);
        }
        if (hologramConfig.getBoolean("text_formatting.strikethrough", false)) {
            component = component.decoration(TextDecoration.STRIKETHROUGH, true);
        }
        if (hologramConfig.getBoolean("text_formatting.obfuscated", false)) {
            component = component.decoration(TextDecoration.OBFUSCATED, true);
        }

        return component;
    }

    /**
     * 解析颜色字符串为Color对象
     */
    private Color parseColor(String colorStr) {
        if (colorStr == null || colorStr.trim().isEmpty()) {
            return null;
        }

        try {
            // 支持RGB格式: "255,255,255"
            if (colorStr.contains(",")) {
                String[] rgb = colorStr.split(",");
                if (rgb.length == 3) {
                    int r = Integer.parseInt(rgb[0].trim());
                    int g = Integer.parseInt(rgb[1].trim());
                    int b = Integer.parseInt(rgb[2].trim());
                    return Color.fromRGB(r, g, b);
                }
            }

            // 支持十六进制格式: "#FFFFFF" 或 "FFFFFF"
            if (colorStr.startsWith("#")) {
                colorStr = colorStr.substring(1);
            }
            if (colorStr.length() == 6) {
                int rgb = Integer.parseInt(colorStr, 16);
                return Color.fromRGB(rgb);
            }
        } catch (NumberFormatException e) {
            plugin.getLogger().warning("无效的颜色格式: " + colorStr);
        }

        return null;
    }
}
