package xyz.stupedo.qzz.SMPCORE.listeners;

import org.bukkit.Chunk;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Vehicle;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.vehicle.VehicleCreateEvent;
import org.bukkit.event.vehicle.VehicleMoveEvent;
import xyz.stupedo.qzz.SMPCORE.SMPCORE;

public class VehicleListener implements Listener {

    private final SMPCORE plugin;

    public VehicleListener(SMPCORE plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onVehicleCreate(VehicleCreateEvent event) {
        if (plugin.getVehicleManager() == null || !plugin.getVehicleManager().isEnabled()) {
            return;
        }

        Vehicle vehicle = event.getVehicle();
        Chunk chunk = vehicle.getLocation().getChunk();

        plugin.getVehicleManager().updateVehicleCount(chunk);

        // Check if we should prevent creation due to limit
        int maxVehicles = plugin.getVehicleManager().getMaxVehiclesPerChunk();

        if (plugin.getVehicleManager().getVehicleCount(chunk) >= maxVehicles) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onVehicleMove(VehicleMoveEvent event) {
        if (plugin.getVehicleManager() == null || !plugin.getVehicleManager().isEnabled()) {
            return;
        }

        Vehicle vehicle = event.getVehicle();

        // Optimize boat movement
        if (plugin.getVehicleManager().shouldOptimizeBoatMovement() &&
            vehicle.getType().name().contains("BOAT")) {
            // Reduce update frequency for boats
            // This is a simplified version - real implementation would need more complex logic
            return;
        }

        // Optimize minecart movement
        if (plugin.getVehicleManager().shouldOptimizeMinecartMovement() &&
            vehicle.getType().name().contains("MINECART")) {
            // Reduce update frequency for minecarts
            return;
        }
    }
}