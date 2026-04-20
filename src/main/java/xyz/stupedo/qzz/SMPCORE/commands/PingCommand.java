package xyz.stupedo.qzz.SMPCORE.commands;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import xyz.stupedo.qzz.SMPCORE.SMPCORE;
import xyz.stupedo.qzz.SMPCORE.utils.MessageUtils;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public class PingCommand implements CommandExecutor {

    private final SMPCORE plugin;

    public PingCommand(SMPCORE plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("smpcore.ping")) {
            MessageUtils.sendPrefixMessage(sender, "You don't have permission to use this command.");
            return true;
        }

        if (args.length == 0) {
            if (sender instanceof Player) {
                Player player = (Player) sender;
                showPlayerPing(sender, player);
            } else {
                MessageUtils.sendPrefixMessage(sender, "Usage: /ping <player>");
            }
            return true;
        }

        String arg = args[0].toLowerCase();

        if (arg.equals("top")) {
            showTopPing(sender);
        } else if (arg.equals("stats")) {
            showPingStats(sender);
        } else {
            Player target = Bukkit.getPlayer(arg);
            if (target != null) {
                showPlayerPing(sender, target);
            } else {
                MessageUtils.sendPrefixMessage(sender, "Player not found: " + arg);
            }
        }

        return true;
    }

    private void showPlayerPing(CommandSender sender, Player player) {
        int ping = player.getPing();
        String formattedPing = MessageUtils.formatPing(ping);
        MessageUtils.sendPrefixMessage(sender, player.getName() + "'s ping: " + formattedPing);
    }

    private void showTopPing(CommandSender sender) {
        List<Map.Entry<UUID, Integer>> topPlayers = plugin.getPingManager().getTopPingPlayers(5);

        if (topPlayers.isEmpty()) {
            MessageUtils.sendPrefixMessage(sender, "No ping data available.");
            return;
        }

        sender.sendMessage("");
        sender.sendMessage(MessageUtils.color("&6=== Top 5 Highest Ping ==="));
        for (int i = 0; i < topPlayers.size(); i++) {
            Map.Entry<UUID, Integer> entry = topPlayers.get(i);
            UUID uuid = entry.getKey();
            int ping = entry.getValue();

            Player player = Bukkit.getPlayer(uuid);
            String playerName = player != null ? player.getName() : "Unknown";

            sender.sendMessage((i + 1) + ". " + playerName + ": " + MessageUtils.formatPing(ping));
        }
        sender.sendMessage(MessageUtils.color("&6=========================="));
        sender.sendMessage("");
    }

    private void showPingStats(CommandSender sender) {
        Map<String, Integer> stats = plugin.getPingManager().getPingStats();

        sender.sendMessage("");
        sender.sendMessage(MessageUtils.color("&6=== Ping Statistics ==="));
        for (Map.Entry<String, Integer> entry : stats.entrySet()) {
            String key = entry.getKey();
            Integer value = entry.getValue();

            if (key.equals("average_ping") || key.equals("highest_ping") || key.equals("lowest_ping")) {
                sender.sendMessage(key + ": " + MessageUtils.formatPing(value));
            } else {
                sender.sendMessage(key + ": " + value);
            }
        }
        sender.sendMessage(MessageUtils.color("&6======================"));
        sender.sendMessage("");
    }
}