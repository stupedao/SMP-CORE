package xyz.stupedo.qzz.SMPCORE.listeners;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import xyz.stupedo.qzz.SMPCORE.SMPCORE;
import xyz.stupedo.qzz.SMPCORE.utils.ConfigUtils;
import xyz.stupedo.qzz.SMPCORE.utils.MessageUtils;

public class PlayerQuitListener implements Listener {

    private final SMPCORE plugin;

    public PlayerQuitListener(SMPCORE plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerQuit(PlayerQuitEvent event) {
        event.setQuitMessage(null);
    }
}