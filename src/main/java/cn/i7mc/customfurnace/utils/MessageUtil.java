package cn.i7mc.customfurnace.utils;

import cn.i7mc.customfurnace.managers.LangManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class MessageUtil {
    private final LangManager langManager;

    public MessageUtil(LangManager langManager) {
        this.langManager = langManager;
    }

    public void sendMessage(CommandSender sender, String key, Object ... args) {
        if (sender == null) {
            return;
        }
        String message = this.langManager.getMessage(key, args);
        if (!message.isEmpty()) {
            sender.sendMessage(message);
        }
    }

    public void sendMessage(Player player, String key, Object ... args) {
        if (player == null || !player.isOnline()) {
            return;
        }
        this.sendMessage((CommandSender)player, key, args);
    }

    public void sendDebug(CommandSender sender, String key, Object ... args) {
        if (sender == null) {
            return;
        }
        if (!this.langManager.isDebugEnabled()) {
            return;
        }
        String message = this.langManager.getDebugMessage(key, args);
        if (!message.isEmpty()) {
            sender.sendMessage(String.valueOf(ChatColor.DARK_GRAY) + "[DEBUG] " + message);
        }
    }

    public void sendDebug(Player player, String key, Object ... args) {
        if (player == null || !player.isOnline()) {
            return;
        }
        this.sendDebug((CommandSender)player, key, args);
    }

    public void broadcast(String key, Object ... args) {
        String message = this.langManager.getMessage(key, args);
        if (!message.isEmpty()) {
            Bukkit.broadcastMessage((String)message);
        }
    }

    public void logDebug(String key, Object ... args) {
        if (!this.langManager.isDebugEnabled()) {
            return;
        }
        String message = this.langManager.getDebugMessage(key, args);
        if (!message.isEmpty()) {
            Bukkit.getLogger().info(String.valueOf(ChatColor.DARK_GRAY) + "[DEBUG] " + message);
        }
    }
}

