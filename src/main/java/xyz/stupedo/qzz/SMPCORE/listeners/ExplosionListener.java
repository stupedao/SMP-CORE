package xyz.stupedo.qzz.SMPCORE.listeners;

import org.bukkit.Location;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.block.BlockExplodeEvent;
import xyz.stupedo.qzz.SMPCORE.SMPCORE;

public class ExplosionListener implements Listener {

    private final SMPCORE plugin;

    public ExplosionListener(SMPCORE plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onEntityExplode(EntityExplodeEvent event) {
        if (plugin.getExplosionManager() == null || !plugin.getExplosionManager().isEnabled()) {
            return;
        }

        Location location = event.getLocation();
        float power = event.getYield();
        String entityType = event.getEntityType() != null ? event.getEntityType().name() : "unknown";

        if (plugin.getExplosionManager().shouldLimitExplosion(location, power, entityType)) {
            event.setCancelled(true);
        } else {
            // Limit the power if needed
            double maxPower = plugin.getExplosionManager().getMaxPowerForType(entityType);
            if (power > maxPower) {
                event.setYield((float) maxPower);
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onBlockExplode(BlockExplodeEvent event) {
        if (plugin.getExplosionManager() == null || !plugin.getExplosionManager().isEnabled()) {
            return;
        }

        Location location = event.getBlock().getLocation();
        float power = event.getYield();
        String explosionType = event.getBlock().getType().name();

        if (plugin.getExplosionManager().shouldLimitExplosion(location, power, explosionType)) {
            event.setCancelled(true);
        } else {
            // Limit the power if needed
            double maxPower = plugin.getExplosionManager().getMaxPowerForType(explosionType);
            if (power > maxPower) {
                event.setYield((float) maxPower);
            }
        }
    }
}