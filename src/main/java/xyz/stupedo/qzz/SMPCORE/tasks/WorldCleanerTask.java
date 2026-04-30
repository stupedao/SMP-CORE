package xyz.stupedo.qzz.SMPCORE.tasks;

import org.bukkit.scheduler.BukkitRunnable;
import xyz.stupedo.qzz.SMPCORE.SMPCORE;

public class WorldCleanerTask extends BukkitRunnable {

    private final SMPCORE plugin;

    public WorldCleanerTask(SMPCORE plugin) {
        this.plugin = plugin;
    }

    @Override
    public void run() {
        if (!plugin.getWorldCleanerManager().isEnabled()) {
            return;
        }

        plugin.getWorldCleanerManager().performCleanup();
    }
}