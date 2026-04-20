package xyz.stupedo.qzz.SMPCORE.tasks;

import org.bukkit.scheduler.BukkitRunnable;
import xyz.stupedo.qzz.SMPCORE.SMPCORE;

public class TPSCheckTask extends BukkitRunnable {

    private final SMPCORE plugin;

    public TPSCheckTask(SMPCORE plugin) {
        this.plugin = plugin;
    }

    @Override
    public void run() {
        if (!plugin.getTpsMonitor().isEnabled()) {
            return;
        }

        plugin.getTpsMonitor().updateTPS();

        if (plugin.getTpsMonitor().isLowTPS() && plugin.getTpsMonitor().shouldAutoOptimize()) {
            plugin.getLogger().warning("Low TPS detected, triggering optimization");
            plugin.getOptimizationManager().triggerOptimization();
        }
    }
}