package xyz.stupedo.qzz.SMPCORE.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import xyz.stupedo.qzz.SMPCORE.SMPCORE;
import xyz.stupedo.qzz.SMPCORE.utils.ConfigUtils;
import xyz.stupedo.qzz.SMPCORE.utils.MessageUtils;

public class AutoIPCommand implements CommandExecutor {

    private final SMPCORE plugin;

    public AutoIPCommand(SMPCORE plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("smpcore.admin")) {
            MessageUtils.sendPrefixMessage(sender, "You don't have permission to use this command.");
            return true;
        }

        if (args.length == 0) {
            // Toggle auto-IP
            boolean currentState = plugin.getAutoIPManager().isEnabled();
            ConfigUtils.getConfig().set("auto-ip.enabled", !currentState);
            ConfigUtils.saveConfig();

            if (!currentState) {
                MessageUtils.sendPrefixMessage(sender, "Auto-IP is now ENABLED. New players will be automatically added to the IP whitelist.");
                plugin.getLogger().info("Auto-IP enabled by " + sender.getName());
            } else {
                MessageUtils.sendPrefixMessage(sender, "Auto-IP is now DISABLED. Players must be manually added to the IP whitelist.");
                plugin.getLogger().info("Auto-IP disabled by " + sender.getName());
            }
            return true;
        }

        String action = args[0].toLowerCase();

        switch (action) {
            case "status":
                showStatus(sender);
                break;
            case "enable":
                enableAutoIP(sender);
                break;
            case "disable":
                disableAutoIP(sender);
                break;
            case "reset":
                resetCounter(sender);
                break;
            default:
                MessageUtils.sendPrefixMessage(sender, "Usage: /smp autoip [status|enable|disable|reset]");
                break;
        }

        return true;
    }

    private void showStatus(CommandSender sender) {
        boolean enabled = plugin.getAutoIPManager().isEnabled();
        String mode = plugin.getAutoIPManager().getMode();
        int totalAdded = plugin.getAutoIPManager().getTotalAutoAdded();

        sender.sendMessage("");
        sender.sendMessage(MessageUtils.color("&6=== Auto-IP Status ==="));
        sender.sendMessage("Enabled: " + (enabled ? "&aYes" : "&cNo"));
        sender.sendMessage("Mode: " + mode);
        sender.sendMessage("Total Auto-Added: " + totalAdded);
        sender.sendMessage(MessageUtils.color("&6======================"));
        sender.sendMessage("");
    }

    private void enableAutoIP(CommandSender sender) {
        ConfigUtils.getConfig().set("auto-ip.enabled", true);
        ConfigUtils.saveConfig();
        MessageUtils.sendPrefixMessage(sender, "Auto-IP has been enabled.");
        plugin.getLogger().info("Auto-IP enabled by " + sender.getName());
    }

    private void disableAutoIP(CommandSender sender) {
        ConfigUtils.getConfig().set("auto-ip.enabled", false);
        ConfigUtils.saveConfig();
        MessageUtils.sendPrefixMessage(sender, "Auto-IP has been disabled.");
        plugin.getLogger().info("Auto-IP disabled by " + sender.getName());
    }

    private void resetCounter(CommandSender sender) {
        plugin.getAutoIPManager().resetCounter();
        MessageUtils.sendPrefixMessage(sender, "Auto-IP counter has been reset.");
        plugin.getLogger().info("Auto-IP counter reset by " + sender.getName());
    }
}