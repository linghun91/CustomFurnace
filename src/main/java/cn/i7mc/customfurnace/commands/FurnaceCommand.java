package cn.i7mc.customfurnace.commands;

import cn.i7mc.customfurnace.CustomFurnace;
import cn.i7mc.customfurnace.listeners.InventoryListener;
import cn.i7mc.customfurnace.managers.EconomyManager;
import cn.i7mc.customfurnace.managers.FurnaceManager;
import cn.i7mc.customfurnace.models.CustomFurnaceData;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class FurnaceCommand
implements CommandExecutor,
TabCompleter {
    private final CustomFurnace plugin;
    private final FurnaceManager furnaceManager;
    private static final List<String> FURNACE_TYPES = Arrays.asList("furnace", "blast_furnace", "smoker");
    private static final List<String> COMMANDS = Arrays.asList("help", "info", "upgrade", "give", "reload");

    public FurnaceCommand(CustomFurnace plugin) {
        this.plugin = plugin;
        this.furnaceManager = plugin.getFurnaceManager();
    }

    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!this.hasPermission(sender, "customfurnace.use")) {
            this.plugin.getMessageUtil().sendMessage(sender, "messages.no_permission", new Object[0]);
            return true;
        }
        if (args.length == 0) {
            this.showHelp(sender);
            return true;
        }
        switch (args[0].toLowerCase()) {
            case "help": {
                this.showHelp(sender);
                break;
            }
            case "info": {
                if (!(sender instanceof Player)) {
                    this.plugin.getMessageUtil().sendMessage(sender, "messages.player_only", new Object[0]);
                    return true;
                }
                this.showInfo((Player)sender);
                break;
            }
            case "upgrade": {
                if (!(sender instanceof Player)) {
                    this.plugin.getMessageUtil().sendMessage(sender, "messages.player_only", new Object[0]);
                    return true;
                }
                if (!this.hasPermission(sender, "customfurnace.upgrade")) {
                    this.plugin.getMessageUtil().sendMessage(sender, "messages.no_permission", new Object[0]);
                    return true;
                }
                this.openUpgradeGUI((Player)sender);
                break;
            }
            case "give": {
                if (!this.hasPermission(sender, "customfurnace.admin")) {
                    this.plugin.getMessageUtil().sendMessage(sender, "messages.no_permission", new Object[0]);
                    return true;
                }
                this.handleGiveCommand(sender, args);
                break;
            }
            case "reload": {
                if (!this.hasPermission(sender, "customfurnace.admin")) {
                    this.plugin.getMessageUtil().sendMessage(sender, "messages.no_permission", new Object[0]);
                    return true;
                }
                this.plugin.getConfigManager().reloadConfigs();
                this.furnaceManager.loadFurnaceData();
                this.plugin.getLangManager().reload();
                this.plugin.getDataManager().loadData();
                this.plugin.getDataManager().applyAllFurnaces();
                this.plugin.getMessageUtil().sendMessage(sender, "messages.reload_success", new Object[0]);
                break;
            }
            case "checkecon": {
                this.handleCheckEconomyCommand(sender);
                break;
            }
            default: {
                this.plugin.getMessageUtil().sendMessage(sender, "messages.unknown_command", new Object[0]);
            }
        }
        return true;
    }

    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        ArrayList<String> completions = new ArrayList<String>();
        if (args.length == 1) {
            String partial = args[0].toLowerCase();
            for (String cmd : COMMANDS) {
                if (!cmd.startsWith(partial) || !this.hasPermission(sender, this.getPermissionForCommand(cmd))) continue;
                completions.add(cmd);
            }
        } else if (args.length == 2) {
            if (args[0].equalsIgnoreCase("give") && this.hasPermission(sender, "customfurnace.admin")) {
                String partialName = args[1].toLowerCase();
                for (Player player : Bukkit.getOnlinePlayers()) {
                    if (!player.getName().toLowerCase().startsWith(partialName)) continue;
                    completions.add(player.getName());
                }
            }
        } else if (args.length == 3) {
            if (args[0].equalsIgnoreCase("give") && this.hasPermission(sender, "customfurnace.admin")) {
                String partialType = args[2].toLowerCase();
                for (String type : FURNACE_TYPES) {
                    if (!type.startsWith(partialType)) continue;
                    completions.add(type);
                }
            }
        } else if (args.length == 4) {
            String type;
            if (args[0].equalsIgnoreCase("give") && this.hasPermission(sender, "customfurnace.admin") && FURNACE_TYPES.contains(type = args[2])) {
                int maxLevelVault = this.furnaceManager.getMaxLevel(type, "vault");
                int maxLevelPoints = this.furnaceManager.getMaxLevel(type, "points");
                int maxLevel = Math.max(maxLevelVault, maxLevelPoints);
                for (int i = 1; i <= maxLevel; ++i) {
                    completions.add(String.valueOf(i));
                }
            }
        } else if (args.length == 5 && args[0].equalsIgnoreCase("give") && this.hasPermission(sender, "customfurnace.admin")) {
            String partialPayment = args[4].toLowerCase();
            if ("vault".startsWith(partialPayment)) {
                completions.add("vault");
            }
            if ("points".startsWith(partialPayment)) {
                completions.add("points");
            }
        }
        return completions;
    }

    private String getPermissionForCommand(String cmd) {
        switch (cmd) {
            case "give": 
            case "reload": {
                return "customfurnace.admin";
            }
            case "upgrade": {
                return "customfurnace.upgrade";
            }
        }
        return "customfurnace.use";
    }

    private boolean hasPermission(CommandSender sender, String permission) {
        return sender.hasPermission(permission) || sender.isOp();
    }

    private void showHelp(CommandSender sender) {
        this.plugin.getMessageUtil().sendMessage(sender, "messages.help_header", new Object[0]);
        this.plugin.getMessageUtil().sendMessage(sender, "messages.help_info", new Object[0]);
        this.plugin.getMessageUtil().sendMessage(sender, "messages.help_upgrade", new Object[0]);
        if (sender.hasPermission("customfurnace.admin")) {
            this.plugin.getMessageUtil().sendMessage(sender, "messages.help_give", new Object[0]);
            this.plugin.getMessageUtil().sendMessage(sender, "messages.help_reload", new Object[0]);
            this.plugin.getMessageUtil().sendMessage(sender, "messages.help_checkecon", new Object[0]);
        }
        this.plugin.getMessageUtil().sendMessage(sender, "messages.help_footer", new Object[0]);
    }

    private void showInfo(Player player) {
        ItemStack item = player.getInventory().getItemInMainHand();
        if (!this.isFurnaceType(item.getType())) {
            this.plugin.getMessageUtil().sendMessage(player, "messages.not_furnace", new Object[0]);
            return;
        }
        CustomFurnaceData data = CustomFurnaceData.fromItem(this.plugin, item);
        if (data == null) {
            this.plugin.getMessageUtil().sendMessage(player, "messages.not_custom_furnace", new Object[0]);
            return;
        }
        this.plugin.getMessageUtil().sendMessage(player, "messages.furnace_info_header", new Object[0]);
        this.plugin.getMessageUtil().sendMessage(player, "messages.furnace_info_type", "type", data.getLevel().getType());
        this.plugin.getMessageUtil().sendMessage(player, "messages.furnace_info_level", "level", data.getLevel().getLevel());
        this.plugin.getMessageUtil().sendMessage(player, "messages.furnace_info_speed", "speed", data.getLevel().getCookingTime());
        if (this.furnaceManager.canUpgrade(data.getLevel().getType(), data.getLevel().getLevel())) {
            int cost = this.furnaceManager.getUpgradeCost(data.getLevel().getType(), data.getLevel().getLevel());
            this.plugin.getMessageUtil().sendMessage(player, "messages.furnace_info_upgrade", "cost", cost);
        } else {
            this.plugin.getMessageUtil().sendMessage(player, "messages.furnace_info_max_level", new Object[0]);
        }
    }

    private void openUpgradeGUI(Player player) {
        ItemStack item = player.getInventory().getItemInMainHand();
        if (!this.isFurnaceType(item.getType())) {
            this.plugin.getMessageUtil().sendMessage(player, "messages.not_furnace", new Object[0]);
            return;
        }
        CustomFurnaceData data = CustomFurnaceData.fromItem(this.plugin, item);
        if (data == null) {
            this.plugin.getMessageUtil().sendMessage(player, "messages.not_custom_furnace", new Object[0]);
            return;
        }
        InventoryListener inventoryListener = this.plugin.getInventoryListener();
        inventoryListener.openUpgradeGUI(player, item);
    }

    private void handleGiveCommand(CommandSender sender, String[] args) {
        ItemStack furnace;
        String requestedPayment;
        String paymentType;
        int level;
        if (args.length < 4) {
            this.plugin.getMessageUtil().sendMessage(sender, "messages.give_usage", new Object[0]);
            return;
        }
        Player target = Bukkit.getPlayer((String)args[1]);
        if (target == null) {
            this.plugin.getMessageUtil().sendMessage(sender, "messages.player_not_found", "player", args[1]);
            return;
        }
        String type = args[2].toLowerCase();
        if (!FURNACE_TYPES.contains(type)) {
            this.plugin.getMessageUtil().sendMessage(sender, "messages.invalid_furnace_type", new Object[0]);
            return;
        }
        try {
            String requestedPayment2;
            level = Integer.parseInt(args[3]);
            paymentType = "vault";
            if (args.length >= 5 && ((requestedPayment2 = args[4].toLowerCase()).equals("points") || requestedPayment2.equals("point"))) {
                paymentType = "points";
            }
            int maxLevel = this.furnaceManager.getMaxLevel(type, paymentType);
            if (level < 1 || level > maxLevel) {
                this.plugin.getMessageUtil().sendMessage(sender, "messages.invalid_level", "max", maxLevel);
                return;
            }
        } catch (NumberFormatException e) {
            this.plugin.getMessageUtil().sendMessage(sender, "messages.invalid_level_number", new Object[0]);
            return;
        }
        paymentType = "vault";
        if (args.length >= 5 && ((requestedPayment = args[4].toLowerCase()).equals("points") || requestedPayment.equals("point"))) {
            paymentType = "points";
        }
        if ((furnace = this.furnaceManager.createFurnaceItem(type, level, paymentType)) != null) {
            target.getInventory().addItem(new ItemStack[]{furnace});
            this.plugin.getMessageUtil().sendMessage(sender, "messages.give_success", "player", target.getName(), "type", type, "level", level, "payment", paymentType.equals("vault") ? "\u91d1\u5e01" : "\u70b9\u5238");
        }
    }

    private boolean isFurnaceType(Material material) {
        return material == Material.FURNACE || material == Material.BLAST_FURNACE || material == Material.SMOKER;
    }

    private void handleCheckEconomyCommand(CommandSender sender) {
        if (!sender.hasPermission("customfurnace.admin")) {
            this.plugin.getMessageUtil().sendMessage(sender, "messages.no_permission", new Object[0]);
            return;
        }
        EconomyManager economyManager = this.plugin.getEconomyManager();
        sender.sendMessage("\u00a76=== \u7ecf\u6d4e\u7cfb\u7edf\u72b6\u6001 ===");
        sender.sendMessage("\u00a7fVault\u7ecf\u6d4e\u7cfb\u7edf: " + (economyManager.isVaultEnabled() ? "\u00a7a\u5df2\u542f\u7528" : "\u00a7c\u672a\u542f\u7528"));
        sender.sendMessage("\u00a7fPlayerPoints\u70b9\u5238\u7cfb\u7edf: " + (economyManager.isPointsEnabled() ? "\u00a7a\u5df2\u542f\u7528" : "\u00a7c\u672a\u542f\u7528"));
        if (sender instanceof Player) {
            Player player = (Player)sender;
            if (economyManager.isVaultEnabled()) {
                double balance = economyManager.getVaultBalance(player);
                sender.sendMessage("\u00a7f\u60a8\u7684\u91d1\u5e01\u4f59\u989d: \u00a7e" + balance);
            }
            if (economyManager.isPointsEnabled()) {
                int points = economyManager.getPointsBalance(player);
                sender.sendMessage("\u00a7f\u60a8\u7684\u70b9\u5238\u4f59\u989d: \u00a7b" + points);
            }
        }
        sender.sendMessage("\u00a76==================");
    }
}

