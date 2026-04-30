package xyz.stupedo.qzz.SMPCORE.listeners;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.event.world.ChunkUnloadEvent;
import xyz.stupedo.qzz.SMPCORE.SMPCORE;

public class ChunkLoadListener implements Listener {

    private final SMPCORE plugin;

    public ChunkLoadListener(SMPCORE plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onChunkLoad(ChunkLoadEvent event) {
        if (plugin.getChunkManager() == null || !plugin.getChunkManager().isEnabled()) {
            return;
        }

        plugin.getChunkManager().updateChunkActivity(event.getChunk());
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onChunkUnload(ChunkUnloadEvent event) {
        if (plugin.getChunkManager() == null || !plugin.getChunkManager().isEnabled()) {
            return;
        }

        plugin.getChunkManager().updateChunkActivity(event.getChunk());
    }
}