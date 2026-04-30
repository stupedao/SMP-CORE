package xyz.stupedo.qzz.SMPCORE.listeners;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.LeavesDecayEvent;
import xyz.stupedo.qzz.SMPCORE.SMPCORE;
import xyz.stupedo.qzz.SMPCORE.utils.ConfigUtils;

public class LeafDecayListener implements Listener {

    private final SMPCORE plugin;

    public LeafDecayListener(SMPCORE plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onLeavesDecay(LeavesDecayEvent event) {
        if (plugin.getLagShieldManager() == null || !plugin.getLagShieldManager().isEnabled()) {
            return;
        }

        if (!plugin.getLagShieldManager().isFeatureEnabled("leaves_decay")) {
            event.setCancelled(true);
            return;
        }

        if (ConfigUtils.getBoolean("instant-leaf-decay.enabled", true)) {
            Block block = event.getBlock();
            if (block.getType() == Material.ACACIA_LEAVES ||
                block.getType() == Material.BIRCH_LEAVES ||
                block.getType() == Material.DARK_OAK_LEAVES ||
                block.getType() == Material.JUNGLE_LEAVES ||
                block.getType() == Material.OAK_LEAVES ||
                block.getType() == Material.SPRUCE_LEAVES ||
                block.getType() == Material.CHERRY_LEAVES ||
                block.getType() == Material.MANGROVE_LEAVES ||
                block.getType() == Material.PALE_OAK_LEAVES) {
                
                block.setType(Material.AIR);
                event.setCancelled(true);
            }
        }
    }
}
