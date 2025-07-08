package cn.i7mc.customfurnace.models;

import cn.i7mc.customfurnace.CustomFurnace;
import cn.i7mc.customfurnace.managers.FurnaceManager;
import cn.i7mc.customfurnace.models.FurnaceLevel;
import java.util.UUID;
import org.bukkit.NamespacedKey;
import org.bukkit.block.BlockState;
import org.bukkit.block.Furnace;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

public class CustomFurnaceData {
    private static final String LEVEL_KEY = "custom_furnace_level";
    private static final String TYPE_KEY = "custom_furnace_type";
    private static final String UUID_KEY = "custom_furnace_uuid";
    private final JavaPlugin plugin;
    private final FurnaceLevel level;
    private final UUID uuid;
    private final String paymentType;
    private UUID hologramUUID;

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

    public static CustomFurnaceData fromItem(JavaPlugin plugin, ItemStack item) {
        FurnaceManager furnaceManager;
        FurnaceLevel furnaceLevel;
        UUID uuid;
        String paymentType;
        if (item == null || !item.hasItemMeta()) {
            return null;
        }
        ItemMeta meta = item.getItemMeta();
        PersistentDataContainer container = meta.getPersistentDataContainer();
        NamespacedKey levelKey = new NamespacedKey((Plugin)plugin, LEVEL_KEY);
        NamespacedKey typeKey = new NamespacedKey((Plugin)plugin, TYPE_KEY);
        NamespacedKey uuidKey = new NamespacedKey((Plugin)plugin, UUID_KEY);
        NamespacedKey paymentKey = new NamespacedKey((Plugin)plugin, "custom_furnace_payment");
        if (!container.has(levelKey, PersistentDataType.INTEGER) || !container.has(typeKey, PersistentDataType.STRING)) {
            return null;
        }
        int level = (Integer)container.get(levelKey, PersistentDataType.INTEGER);
        String type = (String)container.get(typeKey, PersistentDataType.STRING);
        String string = paymentType = container.has(paymentKey, PersistentDataType.STRING) ? (String)container.get(paymentKey, PersistentDataType.STRING) : "vault";
        if (container.has(uuidKey, PersistentDataType.STRING)) {
            try {
                uuid = UUID.fromString((String)container.get(uuidKey, PersistentDataType.STRING));
            } catch (IllegalArgumentException e) {
                uuid = UUID.randomUUID();
            }
        } else {
            uuid = UUID.randomUUID();
        }
        if ((furnaceLevel = (furnaceManager = ((CustomFurnace)plugin).getFurnaceManager()).getFurnaceLevel(type, level, paymentType)) == null) {
            furnaceLevel = new FurnaceLevel(type, level, 200, 0, 0);
        }
        return new CustomFurnaceData(plugin, furnaceLevel, uuid, paymentType);
    }

    public static CustomFurnaceData fromBlockState(JavaPlugin plugin, BlockState state) {
        FurnaceManager furnaceManager;
        FurnaceLevel furnaceLevel;
        UUID uuid;
        String paymentType;
        if (!(state instanceof Furnace)) {
            return null;
        }
        Furnace furnace = (Furnace)state;
        PersistentDataContainer container = furnace.getPersistentDataContainer();
        NamespacedKey levelKey = new NamespacedKey((Plugin)plugin, LEVEL_KEY);
        NamespacedKey typeKey = new NamespacedKey((Plugin)plugin, TYPE_KEY);
        NamespacedKey uuidKey = new NamespacedKey((Plugin)plugin, UUID_KEY);
        NamespacedKey paymentKey = new NamespacedKey((Plugin)plugin, "custom_furnace_payment");
        if (!container.has(levelKey, PersistentDataType.INTEGER) || !container.has(typeKey, PersistentDataType.STRING)) {
            return null;
        }
        int level = (Integer)container.get(levelKey, PersistentDataType.INTEGER);
        String type = (String)container.get(typeKey, PersistentDataType.STRING);
        String string = paymentType = container.has(paymentKey, PersistentDataType.STRING) ? (String)container.get(paymentKey, PersistentDataType.STRING) : "vault";
        if (container.has(uuidKey, PersistentDataType.STRING)) {
            try {
                uuid = UUID.fromString((String)container.get(uuidKey, PersistentDataType.STRING));
            } catch (IllegalArgumentException e) {
                uuid = UUID.randomUUID();
            }
        } else {
            uuid = UUID.randomUUID();
        }
        if ((furnaceLevel = (furnaceManager = ((CustomFurnace)plugin).getFurnaceManager()).getFurnaceLevel(type, level, paymentType)) == null) {
            furnaceLevel = new FurnaceLevel(type, level, 200, 0, 0);
        }
        return new CustomFurnaceData(plugin, furnaceLevel, uuid, paymentType);
    }

    public ItemStack applyToItem(ItemStack item) {
        if (item == null) {
            return null;
        }
        ItemMeta meta = item.getItemMeta();
        if (meta == null) {
            return item;
        }
        PersistentDataContainer container = meta.getPersistentDataContainer();
        NamespacedKey levelKey = new NamespacedKey((Plugin)this.plugin, LEVEL_KEY);
        NamespacedKey typeKey = new NamespacedKey((Plugin)this.plugin, TYPE_KEY);
        NamespacedKey uuidKey = new NamespacedKey((Plugin)this.plugin, UUID_KEY);
        NamespacedKey paymentKey = new NamespacedKey((Plugin)this.plugin, "custom_furnace_payment");
        container.set(levelKey, PersistentDataType.INTEGER, this.level.getLevel());
        container.set(typeKey, PersistentDataType.STRING, this.level.getType());
        container.set(uuidKey, PersistentDataType.STRING, this.uuid.toString());
        container.set(paymentKey, PersistentDataType.STRING, this.paymentType);
        item.setItemMeta(meta);
        return item;
    }

    public void applyToBlockState(BlockState state) {
        if (!(state instanceof Furnace)) {
            return;
        }
        Furnace furnace = (Furnace)state;
        PersistentDataContainer container = furnace.getPersistentDataContainer();
        NamespacedKey levelKey = new NamespacedKey((Plugin)this.plugin, LEVEL_KEY);
        NamespacedKey typeKey = new NamespacedKey((Plugin)this.plugin, TYPE_KEY);
        NamespacedKey uuidKey = new NamespacedKey((Plugin)this.plugin, UUID_KEY);
        NamespacedKey paymentKey = new NamespacedKey((Plugin)this.plugin, "custom_furnace_payment");
        container.set(levelKey, PersistentDataType.INTEGER, this.level.getLevel());
        container.set(typeKey, PersistentDataType.STRING, this.level.getType());
        container.set(uuidKey, PersistentDataType.STRING, this.uuid.toString());
        container.set(paymentKey, PersistentDataType.STRING, this.paymentType);
        int cookingTime = this.level.getCookingTime();
        furnace.setCookTimeTotal(cookingTime);
        furnace.update(true, false);
    }

    public FurnaceLevel getLevel() {
        return this.level;
    }

    public UUID getUuid() {
        return this.uuid;
    }

    public String getPaymentType() {
        return this.paymentType;
    }

    public UUID getHologramUUID() {
        return this.hologramUUID;
    }

    public void setHologramUUID(UUID hologramUUID) {
        this.hologramUUID = hologramUUID;
    }
}

