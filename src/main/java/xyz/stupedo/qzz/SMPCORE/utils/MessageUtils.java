package xyz.stupedo.qzz.SMPCORE.utils;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

public class MessageUtils {

    public static String color(String message) {
        return ChatColor.translateAlternateColorCodes('&', message);
    }

    public static void sendMessage(CommandSender sender, String message) {
        sender.sendMessage(color(message));
    }

    public static void sendPrefixMessage(CommandSender sender, String message) {
        sender.sendMessage(color("&6[SMPCORE] &f" + message));
    }

    public static String formatBytes(long bytes) {
        if (bytes < 1024) {
            return bytes + " B";
        } else if (bytes < 1024 * 1024) {
            return String.format("%.2f KB", bytes / 1024.0);
        } else if (bytes < 1024 * 1024 * 1024) {
            return String.format("%.2f MB", bytes / (1024.0 * 1024.0));
        } else {
            return String.format("%.2f GB", bytes / (1024.0 * 1024.0 * 1024.0));
        }
    }

    public static String formatPing(int ping) {
        if (ping < 50) {
            return "&a" + ping + "ms";
        } else if (ping < 150) {
            return "&e" + ping + "ms";
        } else if (ping < 300) {
            return "&c" + ping + "ms";
        } else {
            return "&4" + ping + "ms";
        }
    }

    public static String formatTPS(double tps) {
        if (tps >= 19.5) {
            return "&a" + String.format("%.2f", tps);
        } else if (tps >= 18.0) {
            return "&e" + String.format("%.2f", tps);
        } else if (tps >= 15.0) {
            return "&c" + String.format("%.2f", tps);
        } else {
            return "&4" + String.format("%.2f", tps);
        }
    }
}