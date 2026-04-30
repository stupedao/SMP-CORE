package xyz.stupedo.qzz.SMPCORE.listeners;

import org.bukkit.Chunk;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntitySpawnEvent;
import xyz.stupedo.qzz.SMPCORE.SMPCORE;
import xyz.stupedo.qzz.SMPCORE.utils.ConfigUtils;

public class EntitySpawnListener implements Listener {

    private final SMPCORE plugin;

    public EntitySpawnListener(SMPCORE plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onEntitySpawn(EntitySpawnEvent event) {
        if (plugin.getEntityManager() == null || !plugin.getEntityManager().isEnabled()) {
            return;
        }

        if (event.isCancelled()) {
            return;
        }

        Entity entity = event.getEntity();
        if (entity instanceof Player) {
            return;
        }

        if (event.getLocation() == null) {
            return;
        }

        Chunk chunk = event.getLocation().getChunk();
        plugin.getEntityManager().updateEntityCount(chunk);

        if (plugin.getEntityManager().getEntityCount(chunk) > plugin.getEntityManager().getMaxEntitiesPerChunk()) {
            if (plugin.getEntityManager().shouldRemoveExcessEntities()) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onCreatureSpawn(CreatureSpawnEvent event) {
        if (plugin.getOptimizationManager() == null || !plugin.getOptimizationManager().isEnabled()) {
            return;
        }

        if (event.isCancelled()) {
            return;
        }

        if (!ConfigUtils.getBoolean("optimization.mob-spawn-control.enabled", true)) {
            return;
        }

        if (event.getSpawnReason() == CreatureSpawnEvent.SpawnReason.NATURAL) {
            int maxMobs = ConfigUtils.getInt("optimization.mob-spawn-control.max-mobs-per-player", 50);
            int radius = ConfigUtils.getInt("optimization.mob-spawn-control.spawn-radius-check", 64);

            int nearbyMobs = 0;
            for (Entity entity : event.getLocation().getWorld().getNearbyEntities(event.getLocation(), radius, radius, radius)) {
                if (entity instanceof org.bukkit.entity.LivingEntity && !(entity instanceof Player)) {
                    nearbyMobs++;
                }
            }

            if (nearbyMobs >= maxMobs) {
                event.setCancelled(true);
            }
        }
    }
}