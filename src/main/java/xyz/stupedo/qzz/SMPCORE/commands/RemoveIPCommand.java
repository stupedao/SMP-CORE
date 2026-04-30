package xyz.stupedo.qzz.SMPCORE.commands;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import xyz.stupedo.qzz.SMPCORE.SMPCORE;
import xyz.stupedo.qzz.SMPCORE.utils.MessageUtils;

import java.util.UUID;

public class RemoveIPCommand implements CommandExecutor {

    private final SMPCORE plugin;

    public RemoveIPCommand(SMPCORE plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("smpcore.admin")) {
            MessageUtils.sendPrefixMessage(sender, "You don't have permission to use this command.");
            return true;
        }

        if (args.length < 1) {
            MessageUtils.sendPrefixMessage(sender, "Usage: /smp removeip <player>");
            return true;
        }

        String playerName = args[0];
        Player target = Bukkit.getPlayer(playerName);

        UUID playerUUID;
        if (target != null) {
            playerUUID = target.getUniqueId();
        } else {
            var offlinePlayer = Bukkit.getOfflinePlayer(playerName);
            if (offlinePlayer.hasPlayedBefore()) {
                playerUUID = offlinePlayer.getUniqueId();
            } else {
                MessageUtils.sendPrefixMessage(sender, "Player not found: " + playerName);
                return true;
            }
        }

        if (plugin.getIpWhitelistManager().removePlayer(playerUUID)) {
            MessageUtils.sendPrefixMessage(sender, "Removed " + playerName + " from IP whitelist.");
            plugin.getLogger().info("Removed " + playerName + " (" + playerUUID + ") from whitelist");
        } else {
            MessageUtils.sendPrefixMessage(sender, "Player is not in the IP whitelist.");
        }

        return true;
    }
}