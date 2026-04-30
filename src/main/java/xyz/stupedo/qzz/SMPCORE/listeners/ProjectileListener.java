package xyz.stupedo.qzz.SMPCORE.listeners;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import xyz.stupedo.qzz.SMPCORE.SMPCORE;

public class ProjectileListener implements Listener {

    private final SMPCORE plugin;

    public ProjectileListener(SMPCORE plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onProjectileLaunch(ProjectileLaunchEvent event) {
        if (plugin.getLagShieldManager() == null || !plugin.getLagShieldManager().isEnabled()) {
            return;
        }

        // Check if projectiles should be disabled due to low TPS
        if (!plugin.getLagShieldManager().isFeatureEnabled("projectiles")) {
            event.setCancelled(true);
            return;
        }

        Projectile projectile = event.getEntity();

        // Check if this is during low TPS
        if (plugin.getTpsMonitor().isLowTPS()) {
            // Limit projectile creation during low TPS
            event.setCancelled(true);
        }
    }
}