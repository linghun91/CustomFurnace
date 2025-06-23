package cn.i7mc.customfurnace.utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

public class ItemBuilder {
    private final ItemStack item;
    private final ItemMeta meta;

    public ItemBuilder(Material material) {
        this.item = new ItemStack(material);
        this.meta = this.item.getItemMeta();
    }

    public ItemBuilder(ItemStack item) {
        this.item = item.clone();
        this.meta = this.item.getItemMeta();
    }

    public ItemBuilder name(String name) {
        if (this.meta != null) {
            this.meta.setDisplayName(name);
        }
        return this;
    }

    public ItemBuilder displayName(String name) {
        if (this.meta != null) {
            this.meta.setDisplayName(name);
        }
        return this;
    }

    public ItemBuilder lore(List<String> lore) {
        if (this.meta != null) {
            this.meta.setLore(lore);
        }
        return this;
    }

    public ItemBuilder lore(String ... lore) {
        return this.lore(Arrays.asList(lore));
    }

    public ItemBuilder addLore(String line) {
        if (this.meta != null) {
            List<String> loreList = this.meta.getLore();
            ArrayList<String> lore;
            if (loreList == null) {
                lore = new ArrayList<String>();
            } else {
                lore = new ArrayList<String>(loreList);
            }
            lore.add(line);
            this.meta.setLore(lore);
        }
        return this;
    }

    public ItemBuilder amount(int amount) {
        this.item.setAmount(amount);
        return this;
    }

    public ItemBuilder enchant(Enchantment enchantment, int level) {
        this.meta.addEnchant(enchantment, level, true);
        return this;
    }

    public ItemBuilder enchant(Map<Enchantment, Integer> enchantments) {
        enchantments.forEach((enchantment, level) -> this.meta.addEnchant(enchantment, level.intValue(), true));
        return this;
    }

    public ItemBuilder unbreakable(boolean unbreakable) {
        this.meta.setUnbreakable(unbreakable);
        return this;
    }

    public ItemBuilder flag(ItemFlag ... flags) {
        this.meta.addItemFlags(flags);
        return this;
    }

    public ItemBuilder hideAll() {
        this.meta.addItemFlags(ItemFlag.values());
        return this;
    }

    public ItemBuilder data(JavaPlugin plugin, String key, String value) {
        NamespacedKey namespacedKey = new NamespacedKey((Plugin)plugin, key);
        PersistentDataContainer container = this.meta.getPersistentDataContainer();
        container.set(namespacedKey, PersistentDataType.STRING, value);
        return this;
    }

    public ItemBuilder data(JavaPlugin plugin, String key, int value) {
        NamespacedKey namespacedKey = new NamespacedKey((Plugin)plugin, key);
        PersistentDataContainer container = this.meta.getPersistentDataContainer();
        container.set(namespacedKey, PersistentDataType.INTEGER, value);
        return this;
    }

    public ItemBuilder data(JavaPlugin plugin, String key, double value) {
        NamespacedKey namespacedKey = new NamespacedKey((Plugin)plugin, key);
        PersistentDataContainer container = this.meta.getPersistentDataContainer();
        container.set(namespacedKey, PersistentDataType.DOUBLE, value);
        return this;
    }

    public ItemBuilder data(JavaPlugin plugin, String key, boolean value) {
        NamespacedKey namespacedKey = new NamespacedKey((Plugin)plugin, key);
        PersistentDataContainer container = this.meta.getPersistentDataContainer();
        container.set(namespacedKey, PersistentDataType.BYTE, (byte)(value ? 1 : 0));
        return this;
    }

    public ItemBuilder glow(boolean glow) {
        if (this.meta == null || glow) {
            // empty if block
        }
        return this;
    }

    public ItemStack build() {
        if (this.meta != null) {
            this.item.setItemMeta(this.meta);
        }
        return this.item;
    }
}

