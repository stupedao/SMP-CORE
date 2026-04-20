package xyz.stupedo.qzz.SMPCORE.listeners;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockRedstoneEvent;
import xyz.stupedo.qzz.SMPCORE.SMPCORE;
import xyz.stupedo.qzz.SMPCORE.utils.ConfigUtils;

import java.util.HashMap;
import java.util.Map;

public class RedstoneListener implements Listener {

    private final SMPCORE plugin;
    private final Map<Block, Integer> redstoneClocks;
    private final Map<Block, Long> lastUpdateTimes;

    public RedstoneListener(SMPCORE plugin) {
        this.plugin = plugin;
        this.redstoneClocks = new HashMap<>();
        this.lastUpdateTimes = new HashMap<>();
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onBlockRedstone(BlockRedstoneEvent event) {
        if (!ConfigUtils.getBoolean("optimization.redstone-optimization.enabled", true)) {
            return;
        }

        Block block = event.getBlock();

        if (!isRedstoneSource(block.getType())) {
            return;
        }

        long currentTime = System.currentTimeMillis();
        Long lastUpdateTime = lastUpdateTimes.get(block);

        if (lastUpdateTime != null) {
            long timeSinceLastUpdate = currentTime - lastUpdateTime;
            long minUpdateInterval = 1000 / ConfigUtils.getInt("optimization.redstone-optimization.max-clock-ticks-per-second", 10);

            if (timeSinceLastUpdate < minUpdateInterval) {
                event.setNewCurrent(event.getOldCurrent());
                return;
            }
        }

        lastUpdateTimes.put(block, currentTime);

        int count = redstoneClocks.getOrDefault(block, 0) + 1;
        redstoneClocks.put(block, count);

        if (count > 100) {
            event.setNewCurrent(event.getOldCurrent());
            redstoneClocks.remove(block);
            lastUpdateTimes.remove(block);
        }
    }

    private boolean isRedstoneSource(Material material) {
        return material == Material.REPEATER || 
               material == Material.COMPARATOR || 
               material == Material.REDSTONE_WIRE ||
               material == Material.REDSTONE_TORCH ||
               material == Material.LEVER ||
               material == Material.STONE_BUTTON ||
               material.name().contains("BUTTON");
    }

    public void clearTracking() {
        redstoneClocks.clear();
        lastUpdateTimes.clear();
    }
}