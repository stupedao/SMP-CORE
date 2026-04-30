package xyz.stupedo.qzz.SMPCORE.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import xyz.stupedo.qzz.SMPCORE.SMPCORE;

public class SparkCommand implements CommandExecutor {

    private final SMPCORE plugin;

    public SparkCommand(SMPCORE plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (plugin.getSparkProfiler() != null) {
            plugin.getSparkProfiler().runProfilerCommand(sender, args);
        } else {
            sender.sendMessage("§cSpark profiler is not available.");
        }
        return true;
    }
}