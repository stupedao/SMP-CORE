package xyz.stupedo.qzz.SMPCORE.tasks;

import org.bukkit.scheduler.BukkitRunnable;
import xyz.stupedo.qzz.SMPCORE.SMPCORE;

public class OptimizationTask extends BukkitRunnable {

    private final SMPCORE plugin;

    public OptimizationTask(SMPCORE plugin) {
        this.plugin = plugin;
    }

    @Override
    public void run() {
        if (!plugin.getOptimizationManager().isEnabled()) {
            return;
        }

        if (plugin.getOptimizationManager().isShutdown()) {
            return;
        }

        if (plugin.getTpsMonitor().isLowTPS()) {
            plugin.getOptimizationManager().triggerOptimization();
        }

        if (plugin.getEntityManager() != null) {
            plugin.getEntityManager().cleanupExcessEntities();
        }

        if (plugin.getChunkManager() != null) {
            plugin.getChunkManager().unloadIdleChunks();
        }
    }
}