package xyz.stupedo.qzz.SMPCORE.managers;

import org.bukkit.Bukkit;
import xyz.stupedo.qzz.SMPCORE.SMPCORE;
import xyz.stupedo.qzz.SMPCORE.utils.ConfigUtils;

import java.util.HashMap;
import java.util.Map;

public class OptimizationManager {

    private final SMPCORE plugin;
    private final Map<String, Boolean> featureStates;
    private boolean emergencyMode;
    private boolean shutdown;

    public OptimizationManager(SMPCORE plugin) {
        this.plugin = plugin;
        this.featureStates = new HashMap<>();
        this.emergencyMode = false;
        this.shutdown = false;
        initializeFeatureStates();
    }

    private void initializeFeatureStates() {
        featureStates.put("item-stacking", ConfigUtils.getBoolean("optimization.item-stacking.enabled", true));
        featureStates.put("ping-monitoring", ConfigUtils.getBoolean("ping-optimization.ping-monitoring.enabled", true));
        featureStates.put("detailed-logging", true);
    }

    public boolean isEnabled() {
        return ConfigUtils.getBoolean("optimization.enabled", true);
    }

    public boolean isAggressiveMode() {
        return ConfigUtils.getBoolean("optimization.aggressive-mode", true);
    }

    public boolean isFeatureEnabled(String feature) {
        return featureStates.getOrDefault(feature, false);
    }

    public void setFeatureEnabled(String feature, boolean enabled) {
        featureStates.put(feature, enabled);
    }

    public void enableEmergencyMode() {
        if (!emergencyMode) {
            emergencyMode = true;
            plugin.getLogger().warning("Emergency optimization mode activated");

            disableNonEssentialFeatures();
            reduceTaskFrequency();
        }
    }

    public void disableEmergencyMode() {
        if (emergencyMode) {
            emergencyMode = false;
            plugin.getLogger().info("Emergency optimization mode deactivated");

            restoreFeatures();
            restoreTaskFrequency();
        }
    }

    private void disableNonEssentialFeatures() {
        if (ConfigUtils.getBoolean("resource-optimization.resource-actions.disable-non-essential.enabled", true)) {
            featureStates.put("item-stacking", false);
            featureStates.put("ping-monitoring", false);
            featureStates.put("detailed-logging", false);
        }
    }

    private void restoreFeatures() {
        featureStates.put("item-stacking", ConfigUtils.getBoolean("optimization.item-stacking.enabled", true));
        featureStates.put("ping-monitoring", ConfigUtils.getBoolean("ping-optimization.ping-monitoring.enabled", true));
        featureStates.put("detailed-logging", true);
    }

    private void reduceTaskFrequency() {
        if (ConfigUtils.getBoolean("resource-optimization.resource-actions.reduce-task-frequency.enabled", true)) {
            double multiplier = ConfigUtils.getDouble("resource-optimization.resource-actions.reduce-task-frequency.frequency-multiplier", 2.0);
            plugin.getLogger().info("Reducing task frequency by factor of " + multiplier);
        }
    }

    private void restoreTaskFrequency() {
        plugin.getLogger().info("Restoring normal task frequency");
    }

    public boolean isEmergencyMode() {
        return emergencyMode;
    }

    public void triggerOptimization() {
        if (!isEnabled()) {
            return;
        }

        plugin.getLogger().info("Triggering manual optimization");

        if (plugin.getEntityManager() != null) {
            plugin.getEntityManager().cleanupExcessEntities();
        }

        if (plugin.getChunkManager() != null) {
            plugin.getChunkManager().unloadIdleChunks();
        }

        if (plugin.getMemoryManager() != null) {
            plugin.getMemoryManager().cleanup();
        }
    }

    public void shutdown() {
        shutdown = true;
        plugin.getLogger().info("Optimization manager shutting down");
    }

    public boolean isShutdown() {
        return shutdown;
    }

    public Map<String, Boolean> getFeatureStates() {
        return new HashMap<>(featureStates);
    }

    public String getStatus() {
        StringBuilder sb = new StringBuilder();
        sb.append("Optimization Status:\n");
        sb.append("  Enabled: ").append(isEnabled()).append("\n");
        sb.append("  Aggressive Mode: ").append(isAggressiveMode()).append("\n");
        sb.append("  Emergency Mode: ").append(isEmergencyMode()).append("\n");
        sb.append("  Features:\n");
        for (Map.Entry<String, Boolean> entry : featureStates.entrySet()) {
            sb.append("    ").append(entry.getKey()).append(": ").append(entry.getValue()).append("\n");
        }
        return sb.toString();
    }
}