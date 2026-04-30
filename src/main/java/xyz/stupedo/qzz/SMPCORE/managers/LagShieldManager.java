package xyz.stupedo.qzz.SMPCORE.managers;

import org.bukkit.Bukkit;
import org.bukkit.World;
import xyz.stupedo.qzz.SMPCORE.SMPCORE;
import xyz.stupedo.qzz.SMPCORE.utils.ConfigUtils;

import java.util.*;

public class LagShieldManager {

    private final SMPCORE plugin;
    private final Map<String, Double> tpsThresholds;
    private final Map<String, Boolean> featureStates;
    private final Set<String> enabledWorlds;
    private boolean emergencyMode;

    public LagShieldManager(SMPCORE plugin) {
        this.plugin = plugin;
        this.tpsThresholds = new HashMap<>();
        this.featureStates = new HashMap<>();
        this.enabledWorlds = new HashSet<>();
        this.emergencyMode = false;
        loadConfiguration();
    }

    private void loadConfiguration() {
        // Load TPS thresholds
        tpsThresholds.put("entity_spawn", ConfigUtils.getDouble("lag-shield.tps_threshold.entity_spawn", 19.0));
        tpsThresholds.put("tick_hopper", ConfigUtils.getDouble("lag-shield.tps_threshold.tick_hopper", 18.0));
        tpsThresholds.put("redstone", ConfigUtils.getDouble("lag-shield.tps_threshold.redstone", 18.0));
        tpsThresholds.put("projectiles", ConfigUtils.getDouble("lag-shield.tps_threshold.projectiles", 15.0));
        tpsThresholds.put("leaves_decay", ConfigUtils.getDouble("lag-shield.tps_threshold.leaves_decay", 19.0));
        tpsThresholds.put("mobai", ConfigUtils.getDouble("lag-shield.tps_threshold.mobai", -1.0));
        tpsThresholds.put("liquid_flow", ConfigUtils.getDouble("lag-shield.tps_threshold.liquid_flow", 18.0));
        tpsThresholds.put("explosions", ConfigUtils.getDouble("lag-shield.tps_threshold.explosions", 18.5));
        tpsThresholds.put("fireworks", ConfigUtils.getDouble("lag-shield.tps_threshold.fireworks", 19.0));

        // Load enabled worlds
        List<String> worlds = ConfigUtils.getConfig().getStringList("lag-shield.worlds");
        if (worlds != null) {
            enabledWorlds.addAll(worlds);
        }

        // Initialize feature states
        featureStates.put("entity_spawn", true);
        featureStates.put("tick_hopper", true);
        featureStates.put("redstone", true);
        featureStates.put("projectiles", true);
        featureStates.put("leaves_decay", true);
        featureStates.put("mobai", true);
        featureStates.put("liquid_flow", true);
        featureStates.put("explosions", true);
        featureStates.put("fireworks", true);
    }

    public boolean isEnabled() {
        return ConfigUtils.getBoolean("lag-shield.enabled", true);
    }

    public boolean isWorldEnabled(String worldName) {
        return enabledWorlds.contains("*") || enabledWorlds.contains(worldName);
    }

    public void checkAndAdjust() {
        if (!isEnabled()) {
            return;
        }

        double currentTPS = plugin.getTpsMonitor().getCurrentTPS();

        // Check each feature against its threshold
        for (Map.Entry<String, Double> entry : tpsThresholds.entrySet()) {
            String feature = entry.getKey();
            double threshold = entry.getValue();

            if (threshold < 0) {
                continue; // Disabled feature
            }

            boolean shouldEnable = currentTPS >= threshold;
            boolean currentState = featureStates.getOrDefault(feature, true);

            if (shouldEnable != currentState) {
                setFeatureEnabled(feature, shouldEnable);
            }
        }

        // Adjust dynamic settings
        adjustDynamicSettings(currentTPS);

        // Check for emergency mode
        if (currentTPS < 15.0 && !emergencyMode) {
            enableEmergencyMode();
        } else if (currentTPS >= 18.0 && emergencyMode) {
            disableEmergencyMode();
        }
    }

    private void setFeatureEnabled(String feature, boolean enabled) {
        featureStates.put(feature, enabled);
        plugin.getLogger().info("LagShield: " + feature + " " + (enabled ? "enabled" : "disabled") + 
            " (TPS: " + String.format("%.2f", plugin.getTpsMonitor().getCurrentTPS()) + ")");
    }

    public boolean isFeatureEnabled(String feature) {
        return featureStates.getOrDefault(feature, true);
    }

    private void adjustDynamicSettings(double currentTPS) {
        // Adjust view distance
        if (ConfigUtils.getBoolean("lag-shield.dynamic_view_distance.enabled", false)) {
            List<String> thresholds = ConfigUtils.getConfig().getStringList("lag-shield.dynamic_view_distance.tps_thresholds");
            int viewDistance = calculateDynamicValue(currentTPS, thresholds, 10);
            setViewDistance(viewDistance);
        }

        // Adjust simulation distance
        if (ConfigUtils.getBoolean("lag-shield.dynamic_simulation_distance.enabled", true)) {
            List<String> thresholds = ConfigUtils.getConfig().getStringList("lag-shield.dynamic_simulation_distance.tps_thresholds");
            int simDistance = calculateDynamicValue(currentTPS, thresholds, 5);
            setSimulationDistance(simDistance);
        }

        // Adjust tick speed
        if (ConfigUtils.getBoolean("lag-shield.dynamic_tick_speed.enabled", true)) {
            List<String> thresholds = ConfigUtils.getConfig().getStringList("lag-shield.dynamic_tick_speed.tps_thresholds");
            int tickSpeed = calculateDynamicValue(currentTPS, thresholds, 3);
            setTickSpeed(tickSpeed);
        }
    }

    private int calculateDynamicValue(double currentTPS, List<String> thresholds, int defaultValue) {
        int value = defaultValue;

        for (String threshold : thresholds) {
            String[] parts = threshold.split(":");
            if (parts.length == 2) {
                try {
                    double tps = Double.parseDouble(parts[0]);
                    int settingValue = Integer.parseInt(parts[1]);

                    if (currentTPS <= tps) {
                        value = settingValue;
                    }
                } catch (NumberFormatException e) {
                    plugin.getLogger().warning("Invalid threshold format: " + threshold);
                }
            }
        }

        return value;
    }

    private void setViewDistance(int distance) {
    }

    private void setSimulationDistance(int distance) {
    }

    private void setTickSpeed(int speed) {
        for (World world : Bukkit.getWorlds()) {
            if (isWorldEnabled(world.getName())) {
                try {
                    world.setGameRuleValue("randomTickSpeed", String.valueOf(speed));
                } catch (Exception e) {
                    plugin.getLogger().warning("Failed to set tick speed for world " + world.getName());
                }
            }
        }
    }

    private void enableEmergencyMode() {
        emergencyMode = true;
        plugin.getLogger().warning("LagShield: Emergency mode activated (TPS: " + 
            String.format("%.2f", plugin.getTpsMonitor().getCurrentTPS()) + ")");

        // Disable all non-essential features
        for (String feature : featureStates.keySet()) {
            if (!feature.equals("entity_spawn")) { // Keep entity spawning
                featureStates.put(feature, false);
            }
        }

        // Set minimum settings
        setViewDistance(2);
        setSimulationDistance(1);
        setTickSpeed(0);
    }

    private void disableEmergencyMode() {
        emergencyMode = false;
        plugin.getLogger().info("LagShield: Emergency mode deactivated (TPS: " + 
            String.format("%.2f", plugin.getTpsMonitor().getCurrentTPS()) + ")");

        // Restore feature states based on current TPS
        checkAndAdjust();
    }

    public boolean isEmergencyMode() {
        return emergencyMode;
    }

    public Map<String, Object> getStatus() {
        Map<String, Object> status = new HashMap<>();
        status.put("enabled", isEnabled());
        status.put("emergency_mode", emergencyMode);
        status.put("current_tps", plugin.getTpsMonitor().getCurrentTPS());
        status.put("feature_states", new HashMap<>(featureStates));
        status.put("tps_thresholds", new HashMap<>(tpsThresholds));
        status.put("enabled_worlds", new ArrayList<>(enabledWorlds));
        return status;
    }
}