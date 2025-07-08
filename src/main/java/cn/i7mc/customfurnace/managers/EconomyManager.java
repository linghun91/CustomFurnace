package cn.i7mc.customfurnace.managers;

import cn.i7mc.customfurnace.CustomFurnace;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.economy.EconomyResponse;
import org.black_ixx.playerpoints.PlayerPoints;
import org.black_ixx.playerpoints.PlayerPointsAPI;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;

public class EconomyManager {
    private final CustomFurnace plugin;
    private boolean vaultEnabled = false;
    private boolean pointsEnabled = false;
    private Economy economy = null;
    private PlayerPointsAPI pointsAPI = null;

    public EconomyManager(CustomFurnace plugin) {
        this.plugin = plugin;
        boolean vaultSuccess = this.setupEconomy();
        boolean pointsSuccess = this.setupPlayerPoints();
        Bukkit.getConsoleSender().sendMessage(String.valueOf(ChatColor.DARK_AQUA) + "[CustomFurnace] " + String.valueOf(ChatColor.GREEN) + "\u7ecf\u6d4e\u7cfb\u7edf\u521d\u59cb\u5316\u72b6\u6001: " + (vaultSuccess ? String.valueOf(ChatColor.GREEN) + "Vault=\u6210\u529f" : String.valueOf(ChatColor.RED) + "Vault=\u5931\u8d25") + String.valueOf(ChatColor.GREEN) + ", " + (pointsSuccess ? String.valueOf(ChatColor.GREEN) + "PlayerPoints=\u6210\u529f" : String.valueOf(ChatColor.RED) + "PlayerPoints=\u5931\u8d25"));
    }

    private boolean setupEconomy() {
        if (!this.plugin.getConfigManager().getConfig().getBoolean("economy.use-vault", true)) {
            Bukkit.getConsoleSender().sendMessage(String.valueOf(ChatColor.DARK_AQUA) + "[CustomFurnace] " + String.valueOf(ChatColor.RED) + "\u91d1\u5e01\u7ecf\u6d4e\u7cfb\u7edf\u5df2\u7981\u7528");
            return false;
        }
        if (Bukkit.getPluginManager().getPlugin("Vault") == null) {
            Bukkit.getConsoleSender().sendMessage(String.valueOf(ChatColor.DARK_AQUA) + "[CustomFurnace] " + String.valueOf(ChatColor.RED) + "\u672a\u627e\u5230Vault\u63d2\u4ef6\uff0c\u91d1\u5e01\u7ecf\u6d4e\u7cfb\u7edf\u5c06\u4e0d\u53ef\u7528");
            this.vaultEnabled = false;
            return false;
        }
        Bukkit.getConsoleSender().sendMessage(String.valueOf(ChatColor.DARK_AQUA) + "[CustomFurnace] " + String.valueOf(ChatColor.GREEN) + "Vault\u63d2\u4ef6\u5df2\u627e\u5230\uff0c\u5c1d\u8bd5\u83b7\u53d6\u7ecf\u6d4e\u670d\u52a1...");
        RegisteredServiceProvider rsp = Bukkit.getServicesManager().getRegistration(Economy.class);
        if (rsp == null) {
            Bukkit.getConsoleSender().sendMessage(String.valueOf(ChatColor.DARK_AQUA) + "[CustomFurnace] " + String.valueOf(ChatColor.RED) + "\u672a\u627e\u5230\u7ecf\u6d4e\u670d\u52a1\u63d0\u4f9b\u8005\uff0c\u91d1\u5e01\u7ecf\u6d4e\u7cfb\u7edf\u5c06\u4e0d\u53ef\u7528");
            this.vaultEnabled = false;
            return false;
        }
        this.economy = (Economy)rsp.getProvider();
        boolean bl = this.vaultEnabled = this.economy != null;
        if (this.vaultEnabled) {
            Bukkit.getConsoleSender().sendMessage(String.valueOf(ChatColor.DARK_AQUA) + "[CustomFurnace] " + String.valueOf(ChatColor.GREEN) + "\u6210\u529f\u8fde\u63a5\u5230Vault\u7ecf\u6d4e\u7cfb\u7edf: " + this.economy.getName());
        } else {
            Bukkit.getConsoleSender().sendMessage(String.valueOf(ChatColor.DARK_AQUA) + "[CustomFurnace] " + String.valueOf(ChatColor.RED) + "\u65e0\u6cd5\u8fde\u63a5\u5230Vault\u7ecf\u6d4e\u7cfb\u7edf");
        }
        return this.vaultEnabled;
    }

    private boolean setupPlayerPoints() {
        if (!this.plugin.getConfigManager().getConfig().getBoolean("economy.use-points", true)) {
            Bukkit.getConsoleSender().sendMessage(String.valueOf(ChatColor.DARK_AQUA) + "[CustomFurnace] " + String.valueOf(ChatColor.RED) + "\u70b9\u5238\u7cfb\u7edf\u5df2\u7981\u7528");
            return false;
        }
        PlayerPoints pointsPlugin = (PlayerPoints)Bukkit.getPluginManager().getPlugin("PlayerPoints");
        if (pointsPlugin == null) {
            Bukkit.getConsoleSender().sendMessage(String.valueOf(ChatColor.DARK_AQUA) + "[CustomFurnace] " + String.valueOf(ChatColor.RED) + "\u672a\u627e\u5230PlayerPoints\u63d2\u4ef6\uff0c\u70b9\u5238\u7cfb\u7edf\u5c06\u4e0d\u53ef\u7528");
            this.pointsEnabled = false;
            return false;
        }
        Bukkit.getConsoleSender().sendMessage(String.valueOf(ChatColor.DARK_AQUA) + "[CustomFurnace] " + String.valueOf(ChatColor.GREEN) + "PlayerPoints\u63d2\u4ef6\u5df2\u627e\u5230\uff0c\u5c1d\u8bd5\u83b7\u53d6API...");
        try {
            this.pointsAPI = pointsPlugin.getAPI();
            boolean bl = this.pointsEnabled = this.pointsAPI != null;
            if (this.pointsEnabled) {
                Bukkit.getConsoleSender().sendMessage(String.valueOf(ChatColor.DARK_AQUA) + "[CustomFurnace] " + String.valueOf(ChatColor.GREEN) + "\u6210\u529f\u8fde\u63a5\u5230PlayerPoints\u70b9\u5238\u7cfb\u7edf");
            } else {
                Bukkit.getConsoleSender().sendMessage(String.valueOf(ChatColor.DARK_AQUA) + "[CustomFurnace] " + String.valueOf(ChatColor.RED) + "\u65e0\u6cd5\u8fde\u63a5\u5230PlayerPoints\u70b9\u5238\u7cfb\u7edf");
            }
        } catch (Exception e) {
            Bukkit.getConsoleSender().sendMessage(String.valueOf(ChatColor.DARK_AQUA) + "[CustomFurnace] " + String.valueOf(ChatColor.RED) + "\u8fde\u63a5PlayerPoints API\u65f6\u53d1\u751f\u9519\u8bef: " + e.getMessage());
            e.printStackTrace();
            this.pointsEnabled = false;
        }
        return this.pointsEnabled;
    }

    public boolean isVaultEnabled() {
        return this.vaultEnabled && this.economy != null;
    }

    public boolean isPointsEnabled() {
        return this.pointsEnabled && this.pointsAPI != null;
    }

    public boolean hasEnoughVaultBalance(Player player, double amount) {
        if (!this.isVaultEnabled()) {
            this.plugin.getLogger().warning("\u5c1d\u8bd5\u68c0\u67e5\u91d1\u5e01\u4f59\u989d\uff0c\u4f46Vault\u7ecf\u6d4e\u7cfb\u7edf\u672a\u542f\u7528");
            return false;
        }
        boolean hasEnough = this.economy.has((OfflinePlayer)player, amount);
        return hasEnough;
    }

    public boolean hasEnoughPoints(Player player, int amount) {
        if (!this.isPointsEnabled()) {
            return false;
        }
        int balance = this.pointsAPI.look(player.getUniqueId());
        boolean hasEnough = balance >= amount;
        return hasEnough;
    }

    public boolean withdrawVault(Player player, double amount) {
        if (!this.isVaultEnabled()) {
            this.plugin.getLogger().warning("\u5c1d\u8bd5\u6263\u9664\u91d1\u5e01\uff0c\u4f46Vault\u7ecf\u6d4e\u7cfb\u7edf\u672a\u542f\u7528");
            return false;
        }
        if (!this.hasEnoughVaultBalance(player, amount)) {
            return false;
        }
        double before = this.economy.getBalance((OfflinePlayer)player);
        EconomyResponse response = this.economy.withdrawPlayer((OfflinePlayer)player, amount);
        double after = this.economy.getBalance((OfflinePlayer)player);
        return response.transactionSuccess();
    }

    public boolean withdrawPoints(Player player, int amount) {
        if (!this.isPointsEnabled()) {
            return false;
        }
        if (!this.hasEnoughPoints(player, amount)) {
            return false;
        }
        int before = this.pointsAPI.look(player.getUniqueId());
        boolean success = this.pointsAPI.take(player.getUniqueId(), amount);
        int after = this.pointsAPI.look(player.getUniqueId());
        return success;
    }

    public double getVaultBalance(Player player) {
        if (!this.isVaultEnabled()) {
            return 0.0;
        }
        return this.economy.getBalance((OfflinePlayer)player);
    }

    public int getPointsBalance(Player player) {
        if (!this.isPointsEnabled()) {
            return 0;
        }
        return this.pointsAPI.look(player.getUniqueId());
    }
}

