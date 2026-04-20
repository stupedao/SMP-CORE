package xyz.stupedo.qzz.SMPCORE.listeners;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import xyz.stupedo.qzz.SMPCORE.SMPCORE;

public class PingListener implements Listener {

    private final SMPCORE plugin;

    public PingListener(SMPCORE plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onPlayerQuit(PlayerQuitEvent event) {
        plugin.getPingManager().removePlayer(event.getPlayer().getUniqueId());
    }
}