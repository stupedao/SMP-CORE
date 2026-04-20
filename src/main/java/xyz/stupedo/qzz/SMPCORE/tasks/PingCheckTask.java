package xyz.stupedo.qzz.SMPCORE.tasks;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import xyz.stupedo.qzz.SMPCORE.SMPCORE;

public class PingCheckTask extends BukkitRunnable {

    private final SMPCORE plugin;

    public PingCheckTask(SMPCORE plugin) {
        this.plugin = plugin;
    }

    @Override
    public void run() {
        if (!plugin.getPingManager().isEnabled()) {
            return;
        }

        if (!plugin.getOptimizationManager().isFeatureEnabled("ping-monitoring")) {
            return;
        }

        for (Player player : Bukkit.getOnlinePlayers()) {
            plugin.getPingManager().updatePlayerPing(player);
        }
    }
}