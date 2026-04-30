package xyz.stupedo.qzz.SMPCORE.commands;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import xyz.stupedo.qzz.SMPCORE.SMPCORE;
import xyz.stupedo.qzz.SMPCORE.utils.MessageUtils;

import java.util.UUID;

public class AddIPCommand implements CommandExecutor {

    private final SMPCORE plugin;

    public AddIPCommand(SMPCORE plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("smpcore.admin")) {
            MessageUtils.sendPrefixMessage(sender, "You don't have permission to use this command.");
            return true;
        }

        if (args.length < 1) {
            MessageUtils.sendPrefixMessage(sender, "Usage: /smp addip <player>");
            return true;
        }

        String playerName = args[0];
        Player target = Bukkit.getPlayer(playerName);

        if (target == null) {
            MessageUtils.sendPrefixMessage(sender, "Player not found: " + playerName);
            return true;
        }

        UUID playerUUID = target.getUniqueId();
        java.net.InetSocketAddress addr = target.getAddress();
        if (addr == null) {
            MessageUtils.sendPrefixMessage(sender, "Failed to get player IP address.");
            return true;
        }
        String playerIP = addr.getAddress().getHostAddress();

        if (plugin.getIpWhitelistManager().addPlayer(playerUUID, playerIP)) {
            MessageUtils.sendPrefixMessage(sender, "Added " + target.getName() + " (" + playerIP + ") to IP whitelist.");
            plugin.getLogger().info("Added " + target.getName() + " (" + playerUUID + ") with IP " + playerIP + " to whitelist");
        } else {
            MessageUtils.sendPrefixMessage(sender, "Player is already in the IP whitelist.");
        }

        return true;
    }
}