package xyz.stupedo.qzz.SMPCORE.managers;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import xyz.stupedo.qzz.SMPCORE.SMPCORE;
import xyz.stupedo.qzz.SMPCORE.utils.ConfigUtils;
import xyz.stupedo.qzz.SMPCORE.utils.ResourceUtils;

import java.util.*;

public class ResourceManager {

    private final SMPCORE plugin;
    private final List<ResourceSnapshot> resourceHistory;
    private boolean shutdown;

    public ResourceManager(SMPCORE plugin) {
        this.plugin = plugin;
        this.resourceHistory = new ArrayList<>();
        this.shutdown = false;
    }

    public boolean isEnabled() {
        return ConfigUtils.getBoolean("resource-optimization.resource-monitoring.enabled", true);
    }

    public int getCheckInterval() {
        return ConfigUtils.getInt("resource-optimization.resource-monitoring.check-interval-seconds", 30);
    }

    public boolean shouldLogResourceUsage() {
        return ConfigUtils.getBoolean("resource-optimization.resource-monitoring.log-resource-usage", true);
    }

    public boolean shouldNotifyOperators() {
        return ConfigUtils.getBoolean("resource-optimization.resource-monitoring.notify-operators", true);
    }

    public int getRAMWarningThreshold() {
        return ConfigUtils.getInt("resource-optimization.thresholds.ram-warning", 70);
    }

    public int getRAMCriticalThreshold() {
        return ConfigUtils.getInt("resource-optimization.thresholds.ram-critical", 85);
    }

    public int getCPUWarningThreshold() {
        return ConfigUtils.getInt("resource-optimization.thresholds.cpu-warning", 70);
    }

    public int getCPUCriticalThreshold() {
        return ConfigUtils.getInt("resource-optimization.thresholds.cpu-critical", 85);
    }

    public boolean shouldAutoManageResources() {
        return ConfigUtils.getBoolean("resource-optimization.auto-resource-management.enabled", true);
    }

    public boolean shouldTriggerOnWarning() {
        return ConfigUtils.getBoolean("resource-optimization.auto-resource-management.trigger-on-warning", true);
    }

    public void checkResources() {
        if (!isEnabled() || shutdown) {
            return;
        }

        double ramUsage = ResourceUtils.getMemoryUsagePercentage();
        double cpuUsage = ResourceUtils.getProcessCpuLoad();

        ResourceSnapshot snapshot = new ResourceSnapshot(
            System.currentTimeMillis(),
            ramUsage,
            cpuUsage,
            ResourceUtils.getUsedMemory(),
            ResourceUtils.getMaxMemory()
        );

        resourceHistory.add(snapshot);
        if (resourceHistory.size() > 100) {
            resourceHistory.remove(0);
        }

        if (shouldLogResourceUsage()) {
            plugin.getLogger().info("Resource Usage - RAM: " + ResourceUtils.formatMemoryPercentage(ramUsage) + 
                ", CPU: " + ResourceUtils.formatCpuLoad(cpuUsage));
        }

        boolean ramWarning = ramUsage >= getRAMWarningThreshold();
        boolean ramCritical = ramUsage >= getRAMCriticalThreshold();
        boolean cpuWarning = cpuUsage >= getCPUWarningThreshold();
        boolean cpuCritical = cpuUsage >= getCPUCriticalThreshold();

        if (ramCritical || cpuCritical) {
            handleCriticalResources(ramCritical, cpuCritical);
        } else if (ramWarning || cpuWarning) {
            handleWarningResources(ramWarning, cpuWarning);
        }

        if (shouldNotifyOperators() && (ramWarning || cpuWarning)) {
            notifyOperators(snapshot, ramWarning, cpuWarning);
        }
    }

    private void handleCriticalResources(boolean ramCritical, boolean cpuCritical) {
        plugin.getLogger().warning("Critical resource levels detected!");

        if (ramCritical) {
            if (plugin.getMemoryManager() != null) {
                plugin.getMemoryManager().cleanup();
            }
            ResourceUtils.forceGarbageCollection();
        }

        if (plugin.getOptimizationManager() != null) {
            plugin.getOptimizationManager().enableEmergencyMode();
        }
    }

    private void handleWarningResources(boolean ramWarning, boolean cpuWarning) {
        if (!shouldAutoManageResources() || !shouldTriggerOnWarning()) {
            return;
        }

        plugin.getLogger().warning("High resource usage detected");

        if (ramWarning) {
            if (ConfigUtils.getBoolean("resource-optimization.resource-actions.aggressive-chunk-unloading.enabled", true)) {
                if (plugin.getChunkManager() != null) {
                    plugin.getChunkManager().unloadIdleChunks();
                }
            }

            if (ConfigUtils.getBoolean("resource-optimization.resource-actions.force-garbage-collection.enabled", true)) {
                ResourceUtils.forceGarbageCollection();
            }
        }

        if (cpuWarning) {
            if (ConfigUtils.getBoolean("resource-optimization.resource-actions.reduce-task-frequency.enabled", true)) {
                if (plugin.getOptimizationManager() != null) {
                    plugin.getOptimizationManager().enableEmergencyMode();
                }
            }
        }
    }

    private void notifyOperators(ResourceSnapshot snapshot, boolean ramWarning, boolean cpuWarning) {
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (player.isOp()) {
                String message = "&e[SMPCORE] &cHigh resource usage: ";
                message += "RAM: " + ResourceUtils.formatMemoryPercentage(snapshot.ramUsage);
                if (ramWarning) {
                    message += " &c(HIGH)";
                }
                message += ", CPU: " + ResourceUtils.formatCpuLoad(snapshot.cpuUsage);
                if (cpuWarning) {
                    message += " &c(HIGH)";
                }
                player.sendMessage(org.bukkit.ChatColor.translateAlternateColorCodes('&', message));
            }
        }
    }

    public List<ResourceSnapshot> getResourceHistory() {
        return new ArrayList<>(resourceHistory);
    }

    public ResourceSnapshot getLatestSnapshot() {
        if (resourceHistory.isEmpty()) {
            return null;
        }
        return resourceHistory.get(resourceHistory.size() - 1);
    }

    public Map<String, Object> getResourceStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("current_ram_usage", ResourceUtils.formatMemoryPercentage(ResourceUtils.getMemoryUsagePercentage()));
        stats.put("current_cpu_usage", ResourceUtils.formatCpuLoad(ResourceUtils.getProcessCpuLoad()));
        stats.put("used_memory", ResourceUtils.formatMemory(ResourceUtils.getUsedMemory()));
        stats.put("max_memory", ResourceUtils.formatMemory(ResourceUtils.getMaxMemory()));
        stats.put("free_memory", ResourceUtils.formatMemory(ResourceUtils.getFreeMemory()));
        stats.put("available_processors", ResourceUtils.getAvailableProcessors());
        stats.put("history_size", resourceHistory.size());

        if (!resourceHistory.isEmpty()) {
            double avgRam = resourceHistory.stream().mapToDouble(s -> s.ramUsage).average().orElse(0);
            double avgCpu = resourceHistory.stream().mapToDouble(s -> s.cpuUsage).average().orElse(0);
            stats.put("average_ram_usage", ResourceUtils.formatMemoryPercentage(avgRam));
            stats.put("average_cpu_usage", ResourceUtils.formatCpuLoad(avgCpu));
        }

        return stats;
    }

    public void shutdown() {
        shutdown = true;
        plugin.getLogger().info("Resource manager shutting down");
    }

    public static class ResourceSnapshot {
        final long timestamp;
        final double ramUsage;
        final double cpuUsage;
        final long usedMemory;
        final long maxMemory;

        public ResourceSnapshot(long timestamp, double ramUsage, double cpuUsage, long usedMemory, long maxMemory) {
            this.timestamp = timestamp;
            this.ramUsage = ramUsage;
            this.cpuUsage = cpuUsage;
            this.usedMemory = usedMemory;
            this.maxMemory = maxMemory;
        }

        public long getTimestamp() {
            return timestamp;
        }

        public double getRamUsage() {
            return ramUsage;
        }

        public double getCpuUsage() {
            return cpuUsage;
        }

        public long getUsedMemory() {
            return usedMemory;
        }

        public long getMaxMemory() {
            return maxMemory;
        }
    }
}