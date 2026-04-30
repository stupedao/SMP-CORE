package xyz.stupedo.qzz.SMPCORE.commands;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import xyz.stupedo.qzz.SMPCORE.SMPCORE;
import xyz.stupedo.qzz.SMPCORE.utils.ConfigUtils;
import xyz.stupedo.qzz.SMPCORE.utils.MessageUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class BypassIPCommand implements CommandExecutor {

    private final SMPCORE plugin;

    public BypassIPCommand(SMPCORE plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("smpcore.admin")) {
            MessageUtils.sendPrefixMessage(sender, "You don't have permission to use this command.");
            return true;
        }

        if (args.length < 1) {
            MessageUtils.sendPrefixMessage(sender, "Usage: /smp bypassip <add|remove|list> [player]");
            return true;
        }

        String action = args[0].toLowerCase();

        switch (action) {
            case "add":
                if (args.length < 2) {
                    MessageUtils.sendPrefixMessage(sender, "Usage: /smp bypassip add <player>");
                    return true;
                }
                addBypass(sender, args[1]);
                break;
            case "remove":
                if (args.length < 2) {
                    MessageUtils.sendPrefixMessage(sender, "Usage: /smp bypassip remove <player>");
                    return true;
                }
                removeBypass(sender, args[1]);
                break;
            case "list":
                listBypassed(sender);
                break;
            default:
                MessageUtils.sendPrefixMessage(sender, "Usage: /smp bypassip <add|remove|list> [player]");
                break;
        }

        return true;
    }

    private void addBypass(CommandSender sender, String playerName) {
        UUID playerUUID = null;
        Player target = Bukkit.getPlayer(playerName);

        if (target != null) {
            playerUUID = target.getUniqueId();
        } else {
            var offlinePlayer = Bukkit.getOfflinePlayer(playerName);
            if (offlinePlayer.hasPlayedBefore()) {
                playerUUID = offlinePlayer.getUniqueId();
            } else {
                MessageUtils.sendPrefixMessage(sender, "Invalid player name: " + playerName);
                return;
            }
        }

        if (playerUUID == null) {
            MessageUtils.sendPrefixMessage(sender, "Could not resolve UUID for: " + playerName);
            return;
        }

        List<String> bypassList = getBypassList();

        if (bypassList.contains(playerUUID.toString())) {
            MessageUtils.sendPrefixMessage(sender, "Player " + playerName + " is already bypassing IP whitelist.");
            return;
        }

        bypassList.add(playerUUID.toString());
        saveBypassList(bypassList);

        MessageUtils.sendPrefixMessage(sender, "Added " + playerName + " to IP whitelist bypass.");
        plugin.getLogger().info("Added " + playerName + " (" + playerUUID + ") to IP whitelist bypass by " + sender.getName());
    }

    private void removeBypass(CommandSender sender, String playerName) {
        UUID playerUUID = null;
        Player target = Bukkit.getPlayer(playerName);

        if (target != null) {
            playerUUID = target.getUniqueId();
        } else {
            var offlinePlayer = Bukkit.getOfflinePlayer(playerName);
            if (offlinePlayer.hasPlayedBefore()) {
                playerUUID = offlinePlayer.getUniqueId();
            } else {
                MessageUtils.sendPrefixMessage(sender, "Invalid player name: " + playerName);
                return;
            }
        }

        if (playerUUID == null) {
            MessageUtils.sendPrefixMessage(sender, "Could not resolve UUID for: " + playerName);
            return;
        }

        List<String> bypassList = getBypassList();

        if (!bypassList.contains(playerUUID.toString())) {
            MessageUtils.sendPrefixMessage(sender, "Player " + playerName + " is not bypassing IP whitelist.");
            return;
        }

        bypassList.remove(playerUUID.toString());
        saveBypassList(bypassList);

        MessageUtils.sendPrefixMessage(sender, "Removed " + playerName + " from IP whitelist bypass.");
        plugin.getLogger().info("Removed " + playerName + " (" + playerUUID + ") from IP whitelist bypass by " + sender.getName());
    }

    private void listBypassed(CommandSender sender) {
        List<String> bypassList = getBypassList();

        if (bypassList.isEmpty()) {
            MessageUtils.sendPrefixMessage(sender, "No players are bypassing IP whitelist.");
            return;
        }

        MessageUtils.sendPrefixMessage(sender, "IP Whitelist Bypass List (" + bypassList.size() + " players):");

        for (String uuidString : bypassList) {
            try {
                UUID uuid = UUID.fromString(uuidString);
                Player player = Bukkit.getPlayer(uuid);
                String playerName = player != null ? player.getName() : "Unknown";
                sender.sendMessage("  " + playerName + " (" + uuidString + ")");
            } catch (IllegalArgumentException e) {
                sender.sendMessage("  Invalid UUID: " + uuidString);
            }
        }
    }

    private List<String> getBypassList() {
        List<String> bypassList = ConfigUtils.getConfig().getStringList("ip-whitelist.bypass-list");
        return bypassList != null ? new ArrayList<>(bypassList) : new ArrayList<>();
    }

    private void saveBypassList(List<String> bypassList) {
        ConfigUtils.getConfig().set("ip-whitelist.bypass-list", bypassList);
        ConfigUtils.saveConfig();
    }
}