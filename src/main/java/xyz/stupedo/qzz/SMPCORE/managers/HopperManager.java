package xyz.stupedo.qzz.SMPCORE.managers;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.data.type.Hopper;
import org.bukkit.inventory.InventoryHolder;
import xyz.stupedo.qzz.SMPCORE.SMPCORE;
import xyz.stupedo.qzz.SMPCORE.utils.ConfigUtils;

import java.util.HashMap;
import java.util.Map;

public class HopperManager {

    private final SMPCORE plugin;
    private final Map<Location, Integer> hopperTickCounters;
    private int transfersOptimized;
    private int totalHoppers;

    public HopperManager(SMPCORE plugin) {
        this.plugin = plugin;
        this.hopperTickCounters = new HashMap<>();
        this.transfersOptimized = 0;
        this.totalHoppers = 0;
    }

    public boolean isEnabled() {
        return ConfigUtils.getBoolean("hopper-optimizer.enabled", true);
    }

    public int getTickInterval() {
        return ConfigUtils.getInt("hopper-optimizer.tick-interval", 8);
    }

    public boolean shouldOptimizeTransfers() {
        return ConfigUtils.getBoolean("hopper-optimizer.optimize-transfers", true);
    }

    public int getMaxTransfersPerTick() {
        return ConfigUtils.getInt("hopper-optimizer.max-transfers-per-tick", 10);
    }

    public void updateHopperCount() {
        if (!isEnabled()) {
            return;
        }

        totalHoppers = 0;
        for (org.bukkit.World world : Bukkit.getWorlds()) {
            for (org.bukkit.Chunk chunk : world.getLoadedChunks()) {
                for (BlockState state : chunk.getTileEntities()) {
                    if (state instanceof org.bukkit.block.Hopper) {
                        totalHoppers++;
                    }
                }
            }
        }
    }

    public boolean shouldTickHopper(Location location) {
        if (!isEnabled()) {
            return true;
        }

        int tickInterval = getTickInterval();
        if (tickInterval <= 1) {
            return true;
        }

        Integer counter = hopperTickCounters.get(location);
        if (counter == null) {
            counter = 0;
        }

        counter++;
        hopperTickCounters.put(location, counter);

        return counter % tickInterval == 0;
    }

    public void resetHopperCounter(Location location) {
        hopperTickCounters.remove(location);
    }

    public int optimizeTransfers(Location hopperLocation) {
        if (!shouldOptimizeTransfers()) {
            return 0;
        }

        int maxTransfers = getMaxTransfersPerTick();
        int optimized = 0;

        try {
            Block block = hopperLocation.getBlock();
            if (block.getState() instanceof org.bukkit.block.Hopper) {
                org.bukkit.block.Hopper hopper = (org.bukkit.block.Hopper) block.getState();

                Block targetBlock = getTargetBlock(hopper);
                if (targetBlock != null && targetBlock.getState() instanceof InventoryHolder) {
                    optimized = Math.min(maxTransfers, 1);
                    transfersOptimized++;
                }
            }
        } catch (Exception e) {
            plugin.getLogger().warning("Error optimizing hopper at " + hopperLocation);
        }

        return optimized;
    }

    private Block getTargetBlock(org.bukkit.block.Hopper hopper) {
        return hopper.getBlock().getRelative(org.bukkit.block.BlockFace.DOWN);
    }

    public int getTotalHoppers() {
        return totalHoppers;
    }

    public int getTransfersOptimized() {
        return transfersOptimized;
    }

    public Map<String, Integer> getStatistics() {
        Map<String, Integer> stats = new HashMap<>();
        stats.put("total_hoppers", totalHoppers);
        stats.put("transfers_optimized", transfersOptimized);
        stats.put("tick_interval", getTickInterval());
        stats.put("max_transfers_per_tick", getMaxTransfersPerTick());
        return stats;
    }

    public void resetStatistics() {
        transfersOptimized = 0;
        totalHoppers = 0;
        hopperTickCounters.clear();
    }
}