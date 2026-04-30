package xyz.stupedo.qzz.SMPCORE.commands;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import xyz.stupedo.qzz.SMPCORE.SMPCORE;
import xyz.stupedo.qzz.SMPCORE.utils.MessageUtils;

import java.util.Map;
import java.util.UUID;

public class ListIPCommand implements CommandExecutor {

    private final SMPCORE plugin;

    public ListIPCommand(SMPCORE plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("smpcore.admin")) {
            MessageUtils.sendPrefixMessage(sender, "You don't have permission to use this command.");
            return true;
        }

        Map<UUID, String> whitelist = plugin.getIpWhitelistManager().getWhitelist();

        if (whitelist.isEmpty()) {
            MessageUtils.sendPrefixMessage(sender, "IP whitelist is empty.");
            return true;
        }

        MessageUtils.sendPrefixMessage(sender, "IP Whitelist (" + whitelist.size() + " entries):");

        for (Map.Entry<UUID, String> entry : whitelist.entrySet()) {
            UUID uuid = entry.getKey();
            String ip = entry.getValue();

            Player player = Bukkit.getPlayer(uuid);
            String playerName = player != null ? player.getName() : Bukkit.getOfflinePlayer(uuid).getName();
            if (playerName == null) {
                playerName = "Unknown";
            }

            sender.sendMessage("  " + playerName + " (" + uuid.toString() + "): " + ip);
        }

        return true;
    }
}