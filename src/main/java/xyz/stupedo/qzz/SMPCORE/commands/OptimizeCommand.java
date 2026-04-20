package xyz.stupedo.qzz.SMPCORE.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import xyz.stupedo.qzz.SMPCORE.SMPCORE;
import xyz.stupedo.qzz.SMPCORE.utils.MessageUtils;

import java.util.Map;

public class OptimizeCommand implements CommandExecutor {

    private final SMPCORE plugin;

    public OptimizeCommand(SMPCORE plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("smpcore.admin")) {
            MessageUtils.sendPrefixMessage(sender, "You don't have permission to use this command.");
            return true;
        }

        if (args.length == 0) {
            plugin.getOptimizationManager().triggerOptimization();
            MessageUtils.sendPrefixMessage(sender, "Manual optimization triggered.");
            return true;
        }

        String subCommand = args[0].toLowerCase();

        switch (subCommand) {
            case "status":
                showStatus(sender);
                break;
            case "tps":
                showTPS(sender);
                break;
            case "entities":
                showEntities(sender);
                break;
            default:
                MessageUtils.sendPrefixMessage(sender, "Usage: /smp optimize [status|tps|entities]");
                break;
        }

        return true;
    }

    private void showStatus(CommandSender sender) {
        sender.sendMessage("");
        sender.sendMessage(MessageUtils.color("&6=== Optimization Status ==="));
        sender.sendMessage(plugin.getOptimizationManager().getStatus());
        sender.sendMessage(MessageUtils.color("&6============================"));
        sender.sendMessage("");
    }

    private void showTPS(CommandSender sender) {
        sender.sendMessage("");
        sender.sendMessage(MessageUtils.color("&6=== TPS Information ==="));
        sender.sendMessage("Current TPS: " + plugin.getTpsMonitor().getFormattedTPS());
        sender.sendMessage("Average TPS: " + plugin.getTpsMonitor().getFormattedAverageTPS());
        sender.sendMessage("Target TPS: " + String.format("%.2f", plugin.getTpsMonitor().getTargetTPS()));
        sender.sendMessage("Low TPS: " + (plugin.getTpsMonitor().isLowTPS() ? "Yes" : "No"));
        sender.sendMessage(MessageUtils.color("&6======================"));
        sender.sendMessage("");
    }

    private void showEntities(CommandSender sender) {
        sender.sendMessage("");
        sender.sendMessage(MessageUtils.color("&6=== Entity Statistics ==="));
        Map<String, Integer> stats = plugin.getEntityManager().getEntityStats();
        for (Map.Entry<String, Integer> entry : stats.entrySet()) {
            sender.sendMessage(entry.getKey() + ": " + entry.getValue());
        }
        sender.sendMessage(MessageUtils.color("&6========================"));
        sender.sendMessage("");
    }
}