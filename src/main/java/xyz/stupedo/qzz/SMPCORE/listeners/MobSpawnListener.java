package xyz.stupedo.qzz.SMPCORE.listeners;

import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import xyz.stupedo.qzz.SMPCORE.SMPCORE;

public class MobSpawnListener implements Listener {

    private final SMPCORE plugin;

    public MobSpawnListener(SMPCORE plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onCreatureSpawn(CreatureSpawnEvent event) {
        if (plugin.getMobAiManager() == null || !plugin.getMobAiManager().isEnabled()) {
            return;
        }

        if (event.isCancelled()) {
            return;
        }

        Entity entity = event.getEntity();
        if (!(entity instanceof LivingEntity)) {
            return;
        }

        // Check if this entity should be optimized
        if (plugin.getMobAiManager().shouldOptimizeEntity(entity)) {
            plugin.getMobAiManager().optimizeEntity(entity);
        }
    }
}