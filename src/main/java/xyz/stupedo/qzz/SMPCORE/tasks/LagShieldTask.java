package xyz.stupedo.qzz.SMPCORE.tasks;

import org.bukkit.scheduler.BukkitRunnable;
import xyz.stupedo.qzz.SMPCORE.SMPCORE;

public class LagShieldTask extends BukkitRunnable {

    private final SMPCORE plugin;

    public LagShieldTask(SMPCORE plugin) {
        this.plugin = plugin;
    }

    @Override
    public void run() {
        if (!plugin.getLagShieldManager().isEnabled()) {
            return;
        }

        plugin.getLagShieldManager().checkAndAdjust();
    }
}