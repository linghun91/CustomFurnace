package cn.i7mc.customfurnace.commands;

import cn.i7mc.customfurnace.CustomFurnace;
import cn.i7mc.customfurnace.listeners.InventoryListener;
import cn.i7mc.customfurnace.managers.EconomyManager;
import cn.i7mc.customfurnace.managers.FurnaceManager;
import cn.i7mc.customfurnace.models.CustomFurnaceData;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 熔炉命令处理器
 */
public class FurnaceCommand implements CommandExecutor, TabCompleter {
    private final CustomFurnace plugin;
    private final FurnaceManager furnaceManager;

    private static final List<String> FURNACE_TYPES = Arrays.asList("furnace", "blast_furnace", "smoker");
    private static final List<String> COMMANDS = Arrays.asList("help", "info", "upgrade", "give", "reload");

    public FurnaceCommand(CustomFurnace plugin) {
        this.plugin = plugin;
        this.furnaceManager = plugin.getFurnaceManager();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        // 检查是否有权限
        if (!hasPermission(sender, "customfurnace.use")) {
            plugin.getMessageUtil().sendMessage(sender, "messages.no_permission");
            return true;
        }

        // 如果没有参数，显示帮助
        if (args.length == 0) {
            showHelp(sender);
            return true;
        }

        // 处理子命令
        switch (args[0].toLowerCase()) {
            case "help":
                showHelp(sender);
                break;

            case "info":
                if (!(sender instanceof Player)) {
                    plugin.getMessageUtil().sendMessage(sender, "messages.player_only");
                    return true;
                }
                showInfo((Player) sender);
                break;

            case "upgrade":
                if (!(sender instanceof Player)) {
                    plugin.getMessageUtil().sendMessage(sender, "messages.player_only");
                    return true;
                }
                if (!hasPermission(sender, "customfurnace.upgrade")) {
                    plugin.getMessageUtil().sendMessage(sender, "messages.no_permission");
                    return true;
                }
                openUpgradeGUI((Player) sender);
                break;

            case "give":
                if (!hasPermission(sender, "customfurnace.admin")) {
                    plugin.getMessageUtil().sendMessage(sender, "messages.no_permission");
                    return true;
                }
                handleGiveCommand(sender, args);
                break;

            case "reload":
                if (!hasPermission(sender, "customfurnace.admin")) {
                    plugin.getMessageUtil().sendMessage(sender, "messages.no_permission");
                    return true;
                }
                // 重载所有配置
                plugin.getConfigManager().reloadConfigs();
                // 重载熔炉数据
                furnaceManager.loadFurnaceData();
                // 重载语言配置
                plugin.getLangManager().reload();
                // 重载数据
                plugin.getDataManager().loadData();
                // 重新应用所有熔炉
                plugin.getDataManager().applyAllFurnaces();
                // 发送重载成功消息
                plugin.getMessageUtil().sendMessage(sender, "messages.reload_success");
                break;

            case "checkecon":
                handleCheckEconomyCommand(sender);
                break;

            default:
                plugin.getMessageUtil().sendMessage(sender, "messages.unknown_command");
                break;
        }

        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();

        if (args.length == 1) {
            // 主命令补全
            String partial = args[0].toLowerCase();
            for (String cmd : COMMANDS) {
                if (cmd.startsWith(partial) && hasPermission(sender, getPermissionForCommand(cmd))) {
                    completions.add(cmd);
                }
            }
        } else if (args.length == 2) {
            // 给予命令的玩家名补全
            if (args[0].equalsIgnoreCase("give") && hasPermission(sender, "customfurnace.admin")) {
                String partialName = args[1].toLowerCase();
                for (Player player : Bukkit.getOnlinePlayers()) {
                    if (player.getName().toLowerCase().startsWith(partialName)) {
                        completions.add(player.getName());
                    }
                }
            }
        } else if (args.length == 3) {
            // 给予命令的熔炉类型补全
            if (args[0].equalsIgnoreCase("give") && hasPermission(sender, "customfurnace.admin")) {
                String partialType = args[2].toLowerCase();
                for (String type : FURNACE_TYPES) {
                    if (type.startsWith(partialType)) {
                        completions.add(type);
                    }
                }
            }
        } else if (args.length == 4) {
            // 给予命令的等级补全
            if (args[0].equalsIgnoreCase("give") && hasPermission(sender, "customfurnace.admin")) {
                String type = args[2];
                if (FURNACE_TYPES.contains(type)) {
                    // 使用两种支付方式中的最高等级
                    int maxLevelVault = furnaceManager.getMaxLevel(type, FurnaceManager.PAYMENT_VAULT);
                    int maxLevelPoints = furnaceManager.getMaxLevel(type, FurnaceManager.PAYMENT_POINTS);
                    int maxLevel = Math.max(maxLevelVault, maxLevelPoints);

                    for (int i = 1; i <= maxLevel; i++) {
                        completions.add(String.valueOf(i));
                    }
                }
            }
        } else if (args.length == 5) {
            // 给予命令的支付方式补全
            if (args[0].equalsIgnoreCase("give") && hasPermission(sender, "customfurnace.admin")) {
                String partialPayment = args[4].toLowerCase();
                if ("vault".startsWith(partialPayment)) {
                    completions.add("vault");
                }
                if ("points".startsWith(partialPayment)) {
                    completions.add("points");
                }
            }
        }

        return completions;
    }

    /**
     * 获取命令对应的权限
     */
    private String getPermissionForCommand(String cmd) {
        switch (cmd) {
            case "give":
            case "reload":
                return "customfurnace.admin";
            case "upgrade":
                return "customfurnace.upgrade";
            default:
                return "customfurnace.use";
        }
    }

    /**
     * 检查发送者是否有指定权限
     */
    private boolean hasPermission(CommandSender sender, String permission) {
        return sender.hasPermission(permission) || sender.isOp();
    }

    /**
     * 显示帮助信息
     */
    private void showHelp(CommandSender sender) {
        plugin.getMessageUtil().sendMessage(sender, "messages.help_header");
        plugin.getMessageUtil().sendMessage(sender, "messages.help_info");
        plugin.getMessageUtil().sendMessage(sender, "messages.help_upgrade");

        if (sender.hasPermission("customfurnace.admin")) {
            plugin.getMessageUtil().sendMessage(sender, "messages.help_give");
            plugin.getMessageUtil().sendMessage(sender, "messages.help_reload");
            plugin.getMessageUtil().sendMessage(sender, "messages.help_checkecon");
        }

        plugin.getMessageUtil().sendMessage(sender, "messages.help_footer");
    }

    /**
     * 显示手持熔炉信息
     */
    private void showInfo(Player player) {
        ItemStack item = player.getInventory().getItemInMainHand();

        // 检查是否为熔炉
        if (!isFurnaceType(item.getType())) {
            plugin.getMessageUtil().sendMessage(player, "messages.not_furnace");
            return;
        }

        // 尝试获取自定义熔炉数据
        CustomFurnaceData data = CustomFurnaceData.fromItem(plugin, item);
        if (data == null) {
            plugin.getMessageUtil().sendMessage(player, "messages.not_custom_furnace");
            return;
        }

        // 显示熔炉信息
        plugin.getMessageUtil().sendMessage(player, "messages.furnace_info_header");
        plugin.getMessageUtil().sendMessage(player, "messages.furnace_info_type",
                "type", data.getLevel().getType());
        plugin.getMessageUtil().sendMessage(player, "messages.furnace_info_level",
                "level", data.getLevel().getLevel());
        plugin.getMessageUtil().sendMessage(player, "messages.furnace_info_speed",
                "speed", data.getLevel().getCookingTime());

        // 检查是否可以升级 - 使用新的API方法，支持支付类型
        String paymentType = data.getPaymentType();
        if (furnaceManager.canUpgrade(data.getLevel().getType(), data.getLevel().getLevel(), paymentType)) {
            int cost;
            if (FurnaceManager.PAYMENT_VAULT.equals(paymentType)) {
                cost = furnaceManager.getVaultUpgradeCost(data.getLevel().getType(), data.getLevel().getLevel());
            } else {
                cost = furnaceManager.getPointsUpgradeCost(data.getLevel().getType(), data.getLevel().getLevel());
            }
            plugin.getMessageUtil().sendMessage(player, "messages.furnace_info_upgrade",
                    "cost", cost);
        } else {
            plugin.getMessageUtil().sendMessage(player, "messages.furnace_info_max_level");
        }
    }

    /**
     * 打开升级界面
     */
    private void openUpgradeGUI(Player player) {
        ItemStack item = player.getInventory().getItemInMainHand();

        // 检查是否为熔炉
        if (!isFurnaceType(item.getType())) {
            plugin.getMessageUtil().sendMessage(player, "messages.not_furnace");
            return;
        }

        // 尝试获取自定义熔炉数据
        CustomFurnaceData data = CustomFurnaceData.fromItem(plugin, item);
        if (data == null) {
            plugin.getMessageUtil().sendMessage(player, "messages.not_custom_furnace");
            return;
        }

        // 从主类获取InventoryListener实例
        InventoryListener inventoryListener = plugin.getInventoryListener();

        // 打开升级界面
        inventoryListener.openUpgradeGUI(player, item);
    }

    /**
     * 处理给予命令
     */
    private void handleGiveCommand(CommandSender sender, String[] args) {
        // 检查参数
        if (args.length < 4) {
            plugin.getMessageUtil().sendMessage(sender, "messages.give_usage");
            return;
        }

        // 获取玩家
        Player target = Bukkit.getPlayer(args[1]);
        if (target == null) {
            plugin.getMessageUtil().sendMessage(sender, "messages.player_not_found",
                    "player", args[1]);
            return;
        }

        // 获取熔炉类型
        String type = args[2].toLowerCase();
        if (!FURNACE_TYPES.contains(type)) {
            plugin.getMessageUtil().sendMessage(sender, "messages.invalid_furnace_type");
            return;
        }

        // 获取等级
        int level;
        try {
            level = Integer.parseInt(args[3]);

            // 获取支付方式，默认为金币
            String paymentType = FurnaceManager.PAYMENT_VAULT;
            if (args.length >= 5) {
                String requestedPayment = args[4].toLowerCase();
                if (requestedPayment.equals("points") || requestedPayment.equals("point")) {
                    paymentType = FurnaceManager.PAYMENT_POINTS;
                }
            }

            // 获取对应支付方式的最高等级
            int maxLevel = furnaceManager.getMaxLevel(type, paymentType);

            if (level < 1 || level > maxLevel) {
                plugin.getMessageUtil().sendMessage(sender, "messages.invalid_level",
                        "max", maxLevel);
                return;
            }

        } catch (NumberFormatException e) {
            plugin.getMessageUtil().sendMessage(sender, "messages.invalid_level_number");
            return;
        }

        // 获取支付方式，默认为金币
        String paymentType = FurnaceManager.PAYMENT_VAULT;
        if (args.length >= 5) {
            String requestedPayment = args[4].toLowerCase();
            if (requestedPayment.equals("points") || requestedPayment.equals("point")) {
                paymentType = FurnaceManager.PAYMENT_POINTS;
            }
        }

        // 创建并给予熔炉
        ItemStack furnace = furnaceManager.createFurnaceItem(type, level, paymentType);
        if (furnace != null) {
            target.getInventory().addItem(furnace);
            String paymentTypeText = plugin.getLangManager().getRawMessage("payment_types." + paymentType);
            plugin.getMessageUtil().sendMessage(sender, "messages.give_success",
                    "player", target.getName(),
                    "type", type,
                    "level", level,
                    "payment", paymentTypeText);
        }
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
     * 处理 checkeconomy 命令 - 检查经济系统状态
     */
    private void handleCheckEconomyCommand(CommandSender sender) {
        if (!sender.hasPermission("customfurnace.admin")) {
            plugin.getMessageUtil().sendMessage(sender, "messages.no_permission");
            return;
        }

        // 获取经济管理器
        EconomyManager economyManager = plugin.getEconomyManager();

        // 输出经济系统状态
        sender.sendMessage(plugin.getLangManager().colorize(plugin.getLangManager().getRawMessage("economy_status.header")));
        sender.sendMessage(plugin.getLangManager().colorize(plugin.getLangManager().getRawMessage(
            economyManager.isVaultEnabled() ? "economy_status.vault_enabled" : "economy_status.vault_disabled")));
        sender.sendMessage(plugin.getLangManager().colorize(plugin.getLangManager().getRawMessage(
            economyManager.isPointsEnabled() ? "economy_status.points_enabled" : "economy_status.points_disabled")));

        // 如果发送者是玩家，显示其余额
        if (sender instanceof Player) {
            Player player = (Player) sender;
            if (economyManager.isVaultEnabled()) {
                double balance = economyManager.getVaultBalance(player);
                sender.sendMessage(plugin.getLangManager().colorize(plugin.getLangManager().getRawMessage("economy_status.vault_balance")
                    .replace("%balance%", String.valueOf(balance))));
            }

            if (economyManager.isPointsEnabled()) {
                int points = economyManager.getPointsBalance(player);
                sender.sendMessage(plugin.getLangManager().colorize(plugin.getLangManager().getRawMessage("economy_status.points_balance")
                    .replace("%balance%", String.valueOf(points))));
            }
        }

        sender.sendMessage(plugin.getLangManager().colorize(plugin.getLangManager().getRawMessage("economy_status.footer")));
    }
}