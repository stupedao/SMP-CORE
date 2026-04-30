package xyz.stupedo.qzz.SMPCORE.tasks;

import org.bukkit.scheduler.BukkitRunnable;
import xyz.stupedo.qzz.SMPCORE.SMPCORE;

public class MobAiTask extends BukkitRunnable {

    private final SMPCORE plugin;
    private int tickCounter;

    public MobAiTask(SMPCORE plugin) {
        this.plugin = plugin;
        this.tickCounter = 0;
    }

    @Override
    public void run() {
        if (!plugin.getMobAiManager().isEnabled()) {
            return;
        }

        tickCounter++;

        // Optimize entities every tick
        plugin.getServer().getWorlds().forEach(world -> {
            world.getEntities().forEach(entity -> {
                if (plugin.getMobAiManager().shouldOptimizeEntity(entity)) {
                    plugin.getMobAiManager().optimizeEntity(entity);
                }
            });
        });

        // Purge cache every 30 seconds (600 ticks)
        if (tickCounter % 600 == 0) {
            plugin.getMobAiManager().purgeCache();
        }
    }
}