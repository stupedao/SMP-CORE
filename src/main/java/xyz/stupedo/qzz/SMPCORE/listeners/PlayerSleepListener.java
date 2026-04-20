package xyz.stupedo.qzz.SMPCORE.listeners;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerBedEnterEvent;
import org.bukkit.event.player.PlayerBedLeaveEvent;
import xyz.stupedo.qzz.SMPCORE.SMPCORE;

public class PlayerSleepListener implements Listener {

    private final SMPCORE plugin;

    public PlayerSleepListener(SMPCORE plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onPlayerBedEnter(PlayerBedEnterEvent event) {
        if (!plugin.getSleepManager().isEnabled()) {
            return;
        }

        if (event.isCancelled()) {
            return;
        }

        plugin.getSleepManager().handlePlayerSleep(event.getPlayer());
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onPlayerBedLeave(PlayerBedLeaveEvent event) {
        if (!plugin.getSleepManager().isEnabled()) {
            return;
        }

        plugin.getSleepManager().handlePlayerWake(event.getPlayer());
    }
}