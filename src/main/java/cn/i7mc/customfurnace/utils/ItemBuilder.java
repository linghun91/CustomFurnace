package cn.i7mc.customfurnace.utils;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * 物品构建工具类
 */
public class ItemBuilder {
    private final ItemStack item;
    private final ItemMeta meta;
    
    /**
     * 从新的物品创建构建器
     */
    public ItemBuilder(Material material) {
        this.item = new ItemStack(material);
        this.meta = item.getItemMeta();
    }
    
    /**
     * 从现有物品创建构建器
     */
    public ItemBuilder(ItemStack item) {
        this.item = item.clone();
        this.meta = this.item.getItemMeta();
    }
    
    /**
     * 设置物品名称
     */
    public ItemBuilder name(String name) {
        if (meta != null) {
            meta.setDisplayName(name);
        }
        return this;
    }
    
    /**
     * 设置物品悬浮显示名称
     */
    public ItemBuilder displayName(String name) {
        if (meta != null) {
            meta.setDisplayName(name);
        }
        return this;
    }
    
    /**
     * 设置物品描述
     */
    public ItemBuilder lore(List<String> lore) {
        if (meta != null) {
            meta.setLore(lore);
        }
        return this;
    }
    
    /**
     * 设置物品描述
     */
    public ItemBuilder lore(String... lore) {
        return lore(Arrays.asList(lore));
    }
    
    /**
     * 添加物品描述行
     */
    public ItemBuilder addLore(String line) {
        if (meta != null) {
            List<String> lore = meta.getLore();
            if (lore == null) {
                lore = new ArrayList<>();
            }
            lore.add(line);
            meta.setLore(lore);
        }
        return this;
    }
    
    /**
     * 设置物品数量
     */
    public ItemBuilder amount(int amount) {
        item.setAmount(amount);
        return this;
    }
    
    /**
     * 添加附魔效果
     */
    public ItemBuilder enchant(Enchantment enchantment, int level) {
        meta.addEnchant(enchantment, level, true);
        return this;
    }
    
    /**
     * 添加多个附魔效果
     */
    public ItemBuilder enchant(Map<Enchantment, Integer> enchantments) {
        enchantments.forEach((enchantment, level) -> meta.addEnchant(enchantment, level, true));
        return this;
    }
    
    /**
     * 设置物品不可破坏
     */
    public ItemBuilder unbreakable(boolean unbreakable) {
        meta.setUnbreakable(unbreakable);
        return this;
    }
    
    /**
     * 添加物品标志
     */
    public ItemBuilder flag(ItemFlag... flags) {
        meta.addItemFlags(flags);
        return this;
    }
    
    /**
     * 隐藏所有属性
     */
    public ItemBuilder hideAll() {
        meta.addItemFlags(ItemFlag.values());
        return this;
    }
    
    /**
     * 存储字符串数据
     */
    public ItemBuilder data(JavaPlugin plugin, String key, String value) {
        NamespacedKey namespacedKey = new NamespacedKey(plugin, key);
        PersistentDataContainer container = meta.getPersistentDataContainer();
        container.set(namespacedKey, PersistentDataType.STRING, value);
        return this;
    }
    
    /**
     * 存储整数数据
     */
    public ItemBuilder data(JavaPlugin plugin, String key, int value) {
        NamespacedKey namespacedKey = new NamespacedKey(plugin, key);
        PersistentDataContainer container = meta.getPersistentDataContainer();
        container.set(namespacedKey, PersistentDataType.INTEGER, value);
        return this;
    }
    
    /**
     * 存储浮点数数据
     */
    public ItemBuilder data(JavaPlugin plugin, String key, double value) {
        NamespacedKey namespacedKey = new NamespacedKey(plugin, key);
        PersistentDataContainer container = meta.getPersistentDataContainer();
        container.set(namespacedKey, PersistentDataType.DOUBLE, value);
        return this;
    }
    
    /**
     * 存储布尔数据
     */
    public ItemBuilder data(JavaPlugin plugin, String key, boolean value) {
        NamespacedKey namespacedKey = new NamespacedKey(plugin, key);
        PersistentDataContainer container = meta.getPersistentDataContainer();
        container.set(namespacedKey, PersistentDataType.BYTE, value ? (byte) 1 : (byte) 0);
        return this;
    }
    
    /**
     * 设置物品是否发光
     */
    public ItemBuilder glow(boolean glow) {
        if (meta != null && glow) {
            // 添加发光效果(通常需要使用附魔和隐藏附魔的方式实现)
            // 这里简化处理，可根据需求扩展
        }
        return this;
    }
    
    /**
     * 构建最终物品
     */
    public ItemStack build() {
        if (meta != null) {
            item.setItemMeta(meta);
        }
        return item;
    }
} 