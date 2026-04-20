package xyz.stupedo.qzz.SMPCORE.managers;

import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.World;
import xyz.stupedo.qzz.SMPCORE.SMPCORE;
import xyz.stupedo.qzz.SMPCORE.utils.ConfigUtils;

import java.util.*;

public class ChunkManager {

    private final SMPCORE plugin;
    private final Map<Chunk, Long> chunkActivity;
    private int totalChunksUnloaded;

    public ChunkManager(SMPCORE plugin) {
        this.plugin = plugin;
        this.chunkActivity = new HashMap<>();
        this.totalChunksUnloaded = 0;
    }

    public boolean isEnabled() {
        return ConfigUtils.getBoolean("optimization.chunk-optimization.enabled", true);
    }

    public int getMaxLoadedChunks() {
        return ConfigUtils.getInt("optimization.chunk-optimization.max-loaded-chunks", 5000);
    }

    public boolean shouldUnloadIdleChunks() {
        return ConfigUtils.getBoolean("optimization.chunk-optimization.unload-idle-chunks", true);
    }

    public int getIdleChunkTime() {
        return ConfigUtils.getInt("optimization.chunk-optimization.idle-chunk-time-seconds", 60);
    }

    public void updateChunkActivity(Chunk chunk) {
        if (!isEnabled()) {
            return;
        }
        chunkActivity.put(chunk, System.currentTimeMillis());
    }

    public void unloadIdleChunks() {
        if (!isEnabled() || !shouldUnloadIdleChunks()) {
            return;
        }

        long idleTime = getIdleChunkTime() * 1000L;
        long currentTime = System.currentTimeMillis();
        int unloaded = 0;

        for (World world : Bukkit.getWorlds()) {
            Chunk[] loadedChunks = world.getLoadedChunks();
            int totalLoaded = loadedChunks.length;
            int maxChunks = getMaxLoadedChunks();

            if (totalLoaded > maxChunks) {
                List<Chunk> chunksToUnload = new ArrayList<>();
                for (Chunk chunk : loadedChunks) {
                    Long lastActivity = chunkActivity.get(chunk);
                    if (lastActivity == null || (currentTime - lastActivity) > idleTime) {
                        chunksToUnload.add(chunk);
                    }
                }

                int toUnload = Math.min(chunksToUnload.size(), totalLoaded - maxChunks);
                for (int i = 0; i < toUnload; i++) {
                    Chunk chunk = chunksToUnload.get(i);
                    if (chunk.unload()) {
                        chunkActivity.remove(chunk);
                        unloaded++;
                    }
                }
            }
        }

        if (unloaded > 0) {
            totalChunksUnloaded += unloaded;
            plugin.getLogger().info("Unloaded " + unloaded + " idle chunks");
        }
    }

    public int getTotalLoadedChunks() {
        int total = 0;
        for (World world : Bukkit.getWorlds()) {
            total += world.getLoadedChunks().length;
        }
        return total;
    }

    public int getTotalChunksUnloaded() {
        return totalChunksUnloaded;
    }

    public void resetStats() {
        totalChunksUnloaded = 0;
        chunkActivity.clear();
    }

    public Map<String, Integer> getChunkStats() {
        Map<String, Integer> stats = new HashMap<>();
        stats.put("total_loaded", getTotalLoadedChunks());
        stats.put("total_unloaded", totalChunksUnloaded);
        stats.put("tracked_chunks", chunkActivity.size());
        return stats;
    }

    public void reduceChunkLimit(int percentage) {
        int currentLimit = getMaxLoadedChunks();
        int newLimit = currentLimit - (currentLimit * percentage / 100);
        plugin.getLogger().info("Reducing chunk limit from " + currentLimit + " to " + newLimit);
    }

    public void reduceIdleTime(int seconds) {
        plugin.getLogger().info("Reducing idle chunk time to " + seconds + " seconds");
    }
}