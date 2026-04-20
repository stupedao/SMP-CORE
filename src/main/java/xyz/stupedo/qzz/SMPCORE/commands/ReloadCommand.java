package xyz.stupedo.qzz.SMPCORE.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import xyz.stupedo.qzz.SMPCORE.SMPCORE;
import xyz.stupedo.qzz.SMPCORE.utils.ConfigUtils;
import xyz.stupedo.qzz.SMPCORE.utils.MessageUtils;

public class ReloadCommand implements CommandExecutor {

    private final SMPCORE plugin;

    public ReloadCommand(SMPCORE plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("smpcore.admin")) {
            MessageUtils.sendPrefixMessage(sender, "You don't have permission to use this command.");
            return true;
        }

        MessageUtils.sendPrefixMessage(sender, "Reloading SMPCORE configuration...");

        try {
            ConfigUtils.reloadConfig();

            if (plugin.getIpWhitelistManager() != null) {
                plugin.getIpWhitelistManager().loadWhitelist();
            }

            if (plugin.getOptimizationManager() != null) {
                plugin.getOptimizationManager().shutdown();
            }

            if (plugin.getResourceManager() != null) {
                plugin.getResourceManager().shutdown();
            }

            MessageUtils.sendPrefixMessage(sender, "Configuration reloaded successfully!");
            plugin.getLogger().info("Configuration reloaded by " + sender.getName());
        } catch (Exception e) {
            MessageUtils.sendPrefixMessage(sender, "Error reloading configuration: " + e.getMessage());
            plugin.getLogger().severe("Error reloading configuration: " + e.getMessage());
            e.printStackTrace();
        }

        return true;
    }
}