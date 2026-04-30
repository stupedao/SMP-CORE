package xyz.stupedo.qzz.SMPCORE.managers;

import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Minecart;
import org.bukkit.entity.Vehicle;
import xyz.stupedo.qzz.SMPCORE.SMPCORE;
import xyz.stupedo.qzz.SMPCORE.utils.ConfigUtils;

import java.util.HashMap;
import java.util.Map;

public class VehicleManager {

    private final SMPCORE plugin;
    private final Map<Chunk, Integer> vehicleCounts;
    private int vehiclesRemoved;
    private int unusedMinecartsRemoved;

    public VehicleManager(SMPCORE plugin) {
        this.plugin = plugin;
        this.vehicleCounts = new HashMap<>();
        this.vehiclesRemoved = 0;
        this.unusedMinecartsRemoved = 0;
    }

    public boolean isEnabled() {
        return ConfigUtils.getBoolean("vehicle-motion-reducer.enabled", true);
    }

    public boolean shouldRemoveUnusedMinecarts() {
        return ConfigUtils.getBoolean("vehicle-motion-reducer.remove-unused-minecarts", true);
    }

    public boolean shouldOptimizeBoatMovement() {
        return ConfigUtils.getBoolean("vehicle-motion-reducer.optimize-boat-movement", true);
    }

    public boolean shouldOptimizeMinecartMovement() {
        return ConfigUtils.getBoolean("vehicle-motion-reducer.optimize-minecart-movement", true);
    }

    public int getMaxVehiclesPerChunk() {
        return ConfigUtils.getInt("vehicle-motion-reducer.max-vehicles-per-chunk", 5);
    }

    public void updateVehicleCount(Chunk chunk) {
        if (!isEnabled()) {
            return;
        }

        int count = vehicleCounts.getOrDefault(chunk, 0);
        if (count == 0) {
            count = 0;
            for (Entity entity : chunk.getEntities()) {
                if (entity instanceof Vehicle) {
                    count++;
                }
            }
            vehicleCounts.put(chunk, count);
        }
    }

    public int getVehicleCount(Chunk chunk) {
        return vehicleCounts.getOrDefault(chunk, 0);
    }

    public void cleanupVehicles() {
        if (!isEnabled()) {
            return;
        }

        int maxVehicles = getMaxVehiclesPerChunk();
        int removed = 0;

        for (World world : Bukkit.getWorlds()) {
            for (Chunk chunk : world.getLoadedChunks()) {
                int count = vehicleCounts.getOrDefault(chunk, 0);

                if (count > maxVehicles) {
                    removed += removeExcessVehicles(chunk, count - maxVehicles);
                }

                // Remove unused minecarts
                if (shouldRemoveUnusedMinecarts()) {
                    removed += removeUnusedMinecarts(chunk);
                }
            }
        }

        if (removed > 0) {
            vehiclesRemoved += removed;
            plugin.getLogger().info("Removed " + removed + " excess vehicles");
        }
    }

    private int removeExcessVehicles(Chunk chunk, int toRemove) {
        int removed = 0;

        for (Entity entity : chunk.getEntities()) {
            if (!(entity instanceof Vehicle)) {
                continue;
            }

            if (removed >= toRemove) {
                break;
            }

            // Skip players and important vehicles
            if (entity instanceof org.bukkit.entity.Player) {
                continue;
            }

            entity.remove();
            removed++;
        }

        updateVehicleCount(chunk);
        return removed;
    }

    private int removeUnusedMinecarts(Chunk chunk) {
        int removed = 0;

        for (Entity entity : chunk.getEntities()) {
            if (!(entity instanceof Minecart)) {
                continue;
            }

            Minecart minecart = (Minecart) entity;

            // Check if minecart is empty (no passengers)
            if (minecart.isEmpty()) {
                minecart.remove();
                removed++;
                unusedMinecartsRemoved++;
            }
        }

        return removed;
    }

    public int getVehiclesRemoved() {
        return vehiclesRemoved;
    }

    public int getUnusedMinecartsRemoved() {
        return unusedMinecartsRemoved;
    }

    public Map<String, Integer> getStatistics() {
        Map<String, Integer> stats = new HashMap<>();
        stats.put("vehicles_removed", vehiclesRemoved);
        stats.put("unused_minecarts_removed", unusedMinecartsRemoved);
        stats.put("tracked_chunks", vehicleCounts.size());
        return stats;
    }

    public void resetStatistics() {
        vehiclesRemoved = 0;
        unusedMinecartsRemoved = 0;
        vehicleCounts.clear();
    }
}