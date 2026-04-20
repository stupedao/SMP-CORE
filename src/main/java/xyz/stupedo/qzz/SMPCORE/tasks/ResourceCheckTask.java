package xyz.stupedo.qzz.SMPCORE.tasks;

import org.bukkit.scheduler.BukkitRunnable;
import xyz.stupedo.qzz.SMPCORE.SMPCORE;

public class ResourceCheckTask extends BukkitRunnable {

    private final SMPCORE plugin;

    public ResourceCheckTask(SMPCORE plugin) {
        this.plugin = plugin;
    }

    @Override
    public void run() {
        if (!plugin.getResourceManager().isEnabled()) {
            return;
        }

        plugin.getResourceManager().checkResources();

        if (plugin.getMemoryManager() != null && plugin.getMemoryManager().isEnabled()) {
            plugin.getMemoryManager().cleanup();
        }
    }
}