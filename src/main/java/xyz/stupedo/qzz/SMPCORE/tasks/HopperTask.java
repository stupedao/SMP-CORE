package xyz.stupedo.qzz.SMPCORE.tasks;

import org.bukkit.scheduler.BukkitRunnable;
import xyz.stupedo.qzz.SMPCORE.SMPCORE;

public class HopperTask extends BukkitRunnable {

    private final SMPCORE plugin;

    public HopperTask(SMPCORE plugin) {
        this.plugin = plugin;
    }

    @Override
    public void run() {
        if (!plugin.getHopperManager().isEnabled()) {
            return;
        }

        plugin.getHopperManager().updateHopperCount();
    }
}