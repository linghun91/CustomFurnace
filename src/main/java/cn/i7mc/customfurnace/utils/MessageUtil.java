package cn.i7mc.customfurnace.utils;

import cn.i7mc.customfurnace.managers.LangManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * 消息处理工具类
 */
public class MessageUtil {
    private final LangManager langManager;

    public MessageUtil(LangManager langManager) {
        this.langManager = langManager;
    }

    /**
     * 发送消息给命令发送者
     */
    public void sendMessage(CommandSender sender, String key, Object... args) {
        if (sender == null) return;

        String message = langManager.getMessage(key, args);
        if (!message.isEmpty()) {
            sender.sendMessage(message);
        }
    }

    /**
     * 发送消息给玩家
     */
    public void sendMessage(Player player, String key, Object... args) {
        if (player == null || !player.isOnline()) return;
        sendMessage((CommandSender) player, key, args);
    }

    /**
     * 发送调试消息给命令发送者
     */
    public void sendDebug(CommandSender sender, String key, Object... args) {
        if (sender == null) return;
        if (!langManager.isDebugEnabled()) return;

        String message = langManager.getDebugMessage(key, args);
        if (!message.isEmpty()) {
            sender.sendMessage(ChatColor.DARK_GRAY + "[DEBUG] " + message);
        }
    }

    /**
     * 发送调试消息给玩家
     */
    public void sendDebug(Player player, String key, Object... args) {
        if (player == null || !player.isOnline()) return;
        sendDebug((CommandSender) player, key, args);
    }

    /**
     * 发送全局消息
     */
    public void broadcast(String key, Object... args) {
        String message = langManager.getMessage(key, args);
        if (!message.isEmpty()) {
            Bukkit.broadcastMessage(message);
        }
    }

    /**
     * 记录调试日志到控制台
     */
    public void logDebug(String key, Object... args) {
        if (!langManager.isDebugEnabled()) return;

        String message = langManager.getDebugMessage(key, args);
        if (!message.isEmpty()) {
            Bukkit.getLogger().info("[DEBUG] " + message);
        }
    }


}