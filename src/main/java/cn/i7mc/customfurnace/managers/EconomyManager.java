package cn.i7mc.customfurnace.managers;

import cn.i7mc.customfurnace.CustomFurnace;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.economy.EconomyResponse;
import org.black_ixx.playerpoints.PlayerPoints;
import org.black_ixx.playerpoints.PlayerPointsAPI;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;

/**
 * 经济管理器 - 处理金币和点券操作
 */
public class EconomyManager {
    private final CustomFurnace plugin;
    private boolean vaultEnabled = false;
    private boolean pointsEnabled = false;
    private Economy economy = null;
    private PlayerPointsAPI pointsAPI = null;

    public EconomyManager(CustomFurnace plugin) {
        this.plugin = plugin;
        boolean vaultSuccess = setupEconomy();
        boolean pointsSuccess = setupPlayerPoints();

        // 输出初始化结果
        Bukkit.getConsoleSender().sendMessage(ChatColor.DARK_AQUA + "[CustomFurnace] " + ChatColor.GREEN + "经济系统初始化状态: " +
                                           (vaultSuccess ? ChatColor.GREEN + "Vault=成功" : ChatColor.RED + "Vault=失败") +
                                           ChatColor.GREEN + ", " +
                                           (pointsSuccess ? ChatColor.GREEN + "PlayerPoints=成功" : ChatColor.RED + "PlayerPoints=失败"));
    }

    /**
     * 初始化Vault经济系统
     */
    private boolean setupEconomy() {
        if (!plugin.getConfigManager().getConfig().getBoolean("economy.use-vault", true)) {
            Bukkit.getConsoleSender().sendMessage(ChatColor.DARK_AQUA + "[CustomFurnace] " + ChatColor.RED + "金币经济系统已禁用");
            return false;
        }

        if (Bukkit.getPluginManager().getPlugin("Vault") == null) {
            Bukkit.getConsoleSender().sendMessage(ChatColor.DARK_AQUA + "[CustomFurnace] " + ChatColor.RED + "未找到Vault插件，金币经济系统将不可用");
            vaultEnabled = false;
            return false;
        }

        Bukkit.getConsoleSender().sendMessage(ChatColor.DARK_AQUA + "[CustomFurnace] " + ChatColor.GREEN + "Vault插件已找到，尝试获取经济服务...");

        RegisteredServiceProvider<Economy> rsp = Bukkit.getServicesManager().getRegistration(Economy.class);
        if (rsp == null) {
            Bukkit.getConsoleSender().sendMessage(ChatColor.DARK_AQUA + "[CustomFurnace] " + ChatColor.RED + "未找到经济服务提供者，金币经济系统将不可用");
            vaultEnabled = false;
            return false;
        }

        economy = rsp.getProvider();
        vaultEnabled = (economy != null);

        if (vaultEnabled) {
            Bukkit.getConsoleSender().sendMessage(ChatColor.DARK_AQUA + "[CustomFurnace] " + ChatColor.GREEN + "成功连接到Vault经济系统: " + economy.getName());
        } else {
            Bukkit.getConsoleSender().sendMessage(ChatColor.DARK_AQUA + "[CustomFurnace] " + ChatColor.RED + "无法连接到Vault经济系统");
        }

        return vaultEnabled;
    }

    /**
     * 初始化PlayerPoints点券系统
     */
    private boolean setupPlayerPoints() {
        if (!plugin.getConfigManager().getConfig().getBoolean("economy.use-points", true)) {
            Bukkit.getConsoleSender().sendMessage(ChatColor.DARK_AQUA + "[CustomFurnace] " + ChatColor.RED + "点券系统已禁用");
            return false;
        }

        PlayerPoints pointsPlugin = (PlayerPoints) Bukkit.getPluginManager().getPlugin("PlayerPoints");
        if (pointsPlugin == null) {
            Bukkit.getConsoleSender().sendMessage(ChatColor.DARK_AQUA + "[CustomFurnace] " + ChatColor.RED + "未找到PlayerPoints插件，点券系统将不可用");
            pointsEnabled = false;
            return false;
        }

        Bukkit.getConsoleSender().sendMessage(ChatColor.DARK_AQUA + "[CustomFurnace] " + ChatColor.GREEN + "PlayerPoints插件已找到，尝试获取API...");

        try {
            pointsAPI = pointsPlugin.getAPI();
            pointsEnabled = (pointsAPI != null);

            if (pointsEnabled) {
                Bukkit.getConsoleSender().sendMessage(ChatColor.DARK_AQUA + "[CustomFurnace] " + ChatColor.GREEN + "成功连接到PlayerPoints点券系统");
            } else {
                Bukkit.getConsoleSender().sendMessage(ChatColor.DARK_AQUA + "[CustomFurnace] " + ChatColor.RED + "无法连接到PlayerPoints点券系统");
            }
        } catch (Exception e) {
            Bukkit.getConsoleSender().sendMessage(ChatColor.DARK_AQUA + "[CustomFurnace] " + ChatColor.RED + "连接PlayerPoints API时发生错误: " + e.getMessage());
            e.printStackTrace();
            pointsEnabled = false;
        }

        return pointsEnabled;
    }

    /**
     * 检查金币系统是否可用
     */
    public boolean isVaultEnabled() {
        return vaultEnabled && economy != null;
    }

    /**
     * 检查点券系统是否可用
     */
    public boolean isPointsEnabled() {
        return pointsEnabled && pointsAPI != null;
    }

    /**
     * 检查玩家是否有足够的金币
     */
    public boolean hasEnoughVaultBalance(Player player, double amount) {
        if (!isVaultEnabled()) {
            plugin.getLogger().warning("尝试检查金币余额，但Vault经济系统未启用");
            return false;
        }

        boolean hasEnough = economy.has(player, amount);
        return hasEnough;
    }

    /**
     * 检查玩家是否有足够的点券
     */
    public boolean hasEnoughPoints(Player player, int amount) {
        if (!isPointsEnabled()) {
            return false;
        }

        int balance = pointsAPI.look(player.getUniqueId());
        boolean hasEnough = balance >= amount;
        return hasEnough;
    }

    /**
     * 从玩家扣除金币
     */
    public boolean withdrawVault(Player player, double amount) {
        if (!isVaultEnabled()) {
            plugin.getLogger().warning("尝试扣除金币，但Vault经济系统未启用");
            return false;
        }

        if (!hasEnoughVaultBalance(player, amount)) {
            return false;
        }

        EconomyResponse response = economy.withdrawPlayer(player, amount);

        return response.transactionSuccess();
    }

    /**
     * 从玩家扣除点券
     */
    public boolean withdrawPoints(Player player, int amount) {
        if (!isPointsEnabled()) {
            return false;
        }

        if (!hasEnoughPoints(player, amount)) {
            return false;
        }

        boolean success = pointsAPI.take(player.getUniqueId(), amount);

        return success;
    }

    /**
     * 获取玩家金币余额
     */
    public double getVaultBalance(Player player) {
        if (!isVaultEnabled()) {
            return 0;
        }
        return economy.getBalance(player);
    }

    /**
     * 获取玩家点券余额
     */
    public int getPointsBalance(Player player) {
        if (!isPointsEnabled()) {
            return 0;
        }
        return pointsAPI.look(player.getUniqueId());
    }
}