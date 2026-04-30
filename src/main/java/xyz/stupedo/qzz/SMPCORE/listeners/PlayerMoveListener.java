package xyz.stupedo.qzz.SMPCORE.listeners;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import xyz.stupedo.qzz.SMPCORE.SMPCORE;

public class PlayerMoveListener implements Listener {

    private final SMPCORE plugin;

    public PlayerMoveListener(SMPCORE plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerMove(PlayerMoveEvent event) {
        if (plugin.getPlayerMovementManager() == null) {
            return;
        }

        // Just track the move - don't cancel anything
        // This helps with ping tracking and statistics
        plugin.getPlayerMovementManager().shouldOptimizeMove(event);
    }
}