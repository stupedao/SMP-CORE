package xyz.stupedo.qzz.SMPCORE.listeners;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Hopper;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.inventory.InventoryMoveItemEvent;
import org.bukkit.inventory.InventoryHolder;
import xyz.stupedo.qzz.SMPCORE.SMPCORE;

public class HopperListener implements Listener {

    private final SMPCORE plugin;

    public HopperListener(SMPCORE plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onHopperPlace(BlockPlaceEvent event) {
        if (plugin.getHopperManager() == null || !plugin.getHopperManager().isEnabled()) {
            return;
        }

        if (event.getBlockPlaced().getType() == Material.HOPPER) {
            plugin.getHopperManager().updateHopperCount();
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onInventoryMove(InventoryMoveItemEvent event) {
        if (plugin.getHopperManager() == null || !plugin.getHopperManager().isEnabled()) {
            return;
        }

        // Check if this is a hopper transfer
        if (!(event.getSource().getHolder() instanceof Hopper) && 
            !(event.getDestination().getHolder() instanceof Hopper)) {
            return;
        }

        Location hopperLocation = event.getSource().getLocation();

        // Check if this hopper should tick
        if (!plugin.getHopperManager().shouldTickHopper(hopperLocation)) {
            event.setCancelled(true);
            return;
        }

        // Optimize transfers
        if (plugin.getHopperManager().shouldOptimizeTransfers()) {
            // Limit transfers per tick
            // This is a simplified version - real implementation would need more complex logic
            plugin.getHopperManager().optimizeTransfers(hopperLocation);
        }
    }
}