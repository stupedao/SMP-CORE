package xyz.stupedo.qzz.SMPCORE.managers;

import xyz.stupedo.qzz.SMPCORE.SMPCORE;
import xyz.stupedo.qzz.SMPCORE.utils.ConfigUtils;
import xyz.stupedo.qzz.SMPCORE.utils.ResourceUtils;

import java.util.HashMap;
import java.util.Map;

public class MemoryManager {

    private final SMPCORE plugin;
    private final Map<String, Object> cachedData;
    private int totalCleanups;

    public MemoryManager(SMPCORE plugin) {
        this.plugin = plugin;
        this.cachedData = new HashMap<>();
        this.totalCleanups = 0;
    }

    public boolean isEnabled() {
        return ConfigUtils.getBoolean("resource-optimization.memory-cleanup.enabled", false);
    }

    public int getCleanupInterval() {
        return ConfigUtils.getInt("resource-optimization.memory-cleanup.cleanup-interval-seconds", 120);
    }

    public boolean shouldClearCachedData() {
        return ConfigUtils.getBoolean("resource-optimization.memory-cleanup.clear-cached-data", true);
    }

    public boolean shouldClearUnusedReferences() {
        return ConfigUtils.getBoolean("resource-optimization.memory-cleanup.clear-unused-references", true);
    }

    public boolean shouldOptimizeCollections() {
        return ConfigUtils.getBoolean("resource-optimization.memory-cleanup.optimize-collections", true);
    }

    public void cleanup() {
        if (!isEnabled()) {
            return;
        }

        if (shouldClearCachedData()) {
            clearCachedData();
        }

        if (shouldClearUnusedReferences()) {
            clearUnusedReferences();
        }

        if (shouldOptimizeCollections()) {
            optimizeCollections();
        }

        totalCleanups++;
    }

    private void clearCachedData() {
        cachedData.clear();
    }

    private void clearUnusedReferences() {
        System.gc();
    }

    private void optimizeCollections() {
        if (plugin.getEntityManager() != null) {
            plugin.getEntityManager().resetStats();
        }
        if (plugin.getChunkManager() != null) {
            plugin.getChunkManager().resetStats();
        }
        if (plugin.getPingManager() != null) {
            plugin.getPingManager().resetStats();
        }
    }

    public void cacheData(String key, Object value) {
        cachedData.put(key, value);
    }

    public Object getCachedData(String key) {
        return cachedData.get(key);
    }

    public void removeCachedData(String key) {
        cachedData.remove(key);
    }

    public int getTotalCleanups() {
        return totalCleanups;
    }

    public Map<String, Object> getMemoryStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("total_cleanups", totalCleanups);
        stats.put("cached_entries", cachedData.size());
        stats.put("used_memory", ResourceUtils.formatMemory(ResourceUtils.getUsedMemory()));
        stats.put("max_memory", ResourceUtils.formatMemory(ResourceUtils.getMaxMemory()));
        stats.put("free_memory", ResourceUtils.formatMemory(ResourceUtils.getFreeMemory()));
        stats.put("memory_usage", ResourceUtils.formatMemoryPercentage(ResourceUtils.getMemoryUsagePercentage()));
        return stats;
    }

    public void reset() {
        totalCleanups = 0;
        cachedData.clear();
    }
}