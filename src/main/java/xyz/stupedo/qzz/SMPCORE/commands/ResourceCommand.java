package xyz.stupedo.qzz.SMPCORE.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import xyz.stupedo.qzz.SMPCORE.SMPCORE;
import xyz.stupedo.qzz.SMPCORE.utils.MessageUtils;
import xyz.stupedo.qzz.SMPCORE.utils.ResourceUtils;

import java.util.List;
import java.util.Map;

public class ResourceCommand implements CommandExecutor {

    private final SMPCORE plugin;

    public ResourceCommand(SMPCORE plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("smpcore.admin")) {
            MessageUtils.sendPrefixMessage(sender, "You don't have permission to use this command.");
            return true;
        }

        if (args.length == 0) {
            showResources(sender);
            return true;
        }

        String subCommand = args[0].toLowerCase();

        switch (subCommand) {
            case "history":
                showHistory(sender);
                break;
            case "gc":
                forceGarbageCollection(sender);
                break;
            case "cleanup":
                forceCleanup(sender);
                break;
            case "stats":
                showStats(sender);
                break;
            default:
                MessageUtils.sendPrefixMessage(sender, "Usage: /smp resources [history|gc|cleanup|stats]");
                break;
        }

        return true;
    }

    private void showResources(CommandSender sender) {
        sender.sendMessage("");
        sender.sendMessage(MessageUtils.color("&6=== Current Resource Usage ==="));
        sender.sendMessage("RAM Usage: " + ResourceUtils.formatMemoryPercentage(ResourceUtils.getMemoryUsagePercentage()));
        sender.sendMessage("Used Memory: " + ResourceUtils.formatMemory(ResourceUtils.getUsedMemory()));
        sender.sendMessage("Max Memory: " + ResourceUtils.formatMemory(ResourceUtils.getMaxMemory()));
        sender.sendMessage("Free Memory: " + ResourceUtils.formatMemory(ResourceUtils.getFreeMemory()));
        sender.sendMessage("CPU Load: " + ResourceUtils.formatCpuLoad(ResourceUtils.getProcessCpuLoad()));
        sender.sendMessage("System CPU: " + ResourceUtils.formatCpuLoad(ResourceUtils.getSystemCpuLoad()));
        sender.sendMessage("Available Processors: " + ResourceUtils.getAvailableProcessors());
        sender.sendMessage(MessageUtils.color("&6============================"));
        sender.sendMessage("");
    }

    private void showHistory(CommandSender sender) {
        List<xyz.stupedo.qzz.SMPCORE.managers.ResourceManager.ResourceSnapshot> history = plugin.getResourceManager().getResourceHistory();

        if (history.isEmpty()) {
            MessageUtils.sendPrefixMessage(sender, "No resource history available.");
            return;
        }

        sender.sendMessage("");
        sender.sendMessage(MessageUtils.color("&6=== Resource History (Last " + history.size() + " checks) ==="));

        int showCount = Math.min(10, history.size());
        for (int i = history.size() - showCount; i < history.size(); i++) {
            xyz.stupedo.qzz.SMPCORE.managers.ResourceManager.ResourceSnapshot snapshot = history.get(i);
            long timeAgo = (System.currentTimeMillis() - snapshot.getTimestamp()) / 1000;
            sender.sendMessage((i + 1) + ". (" + timeAgo + "s ago) RAM: " + 
                ResourceUtils.formatMemoryPercentage(snapshot.getRamUsage()) + 
                ", CPU: " + ResourceUtils.formatCpuLoad(snapshot.getCpuUsage()));
        }

        sender.sendMessage(MessageUtils.color("&6=========================================="));
        sender.sendMessage("");
    }

    private void forceGarbageCollection(CommandSender sender) {
        MessageUtils.sendPrefixMessage(sender, "Forcing garbage collection...");
        ResourceUtils.forceGarbageCollection();
        MessageUtils.sendPrefixMessage(sender, "Garbage collection completed.");
        plugin.getLogger().info("Garbage collection forced by " + sender.getName());
    }

    private void forceCleanup(CommandSender sender) {
        MessageUtils.sendPrefixMessage(sender, "Forcing memory cleanup...");
        plugin.getMemoryManager().cleanup();
        MessageUtils.sendPrefixMessage(sender, "Memory cleanup completed.");
        plugin.getLogger().info("Memory cleanup forced by " + sender.getName());
    }

    private void showStats(CommandSender sender) {
        Map<String, Object> stats = plugin.getResourceManager().getResourceStats();

        sender.sendMessage("");
        sender.sendMessage(MessageUtils.color("&6=== Detailed Resource Statistics ==="));
        for (Map.Entry<String, Object> entry : stats.entrySet()) {
            sender.sendMessage(entry.getKey() + ": " + entry.getValue());
        }
        sender.sendMessage(MessageUtils.color("&6===================================="));
        sender.sendMessage("");
    }
}