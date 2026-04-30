package xyz.stupedo.qzz.SMPCORE.managers;

import org.bukkit.Bukkit;
import xyz.stupedo.qzz.SMPCORE.SMPCORE;
import xyz.stupedo.qzz.SMPCORE.utils.ConfigUtils;
import xyz.stupedo.qzz.SMPCORE.utils.MessageUtils;

import java.util.ArrayList;
import java.util.List;

public class TPSMonitor {

    private final SMPCORE plugin;
    private final List<Double> tpsHistory;
    private double currentTPS;

    public TPSMonitor(SMPCORE plugin) {
        this.plugin = plugin;
        this.tpsHistory = new ArrayList<>();
        this.currentTPS = 20.0;
    }

    public boolean isEnabled() {
        return ConfigUtils.getBoolean("optimization.tps-monitoring.enabled", true);
    }

    public double getTargetTPS() {
        return ConfigUtils.getDouble("optimization.tps-monitoring.target-tps", 20.0);
    }

    public double getLowTPSThreshold() {
        return ConfigUtils.getDouble("optimization.tps-monitoring.low-tps-threshold", 18.0);
    }

    public boolean shouldAutoOptimize() {
        return ConfigUtils.getBoolean("optimization.tps-monitoring.auto-optimize-on-low-tps", true);
    }

    public boolean shouldLogTPSDrops() {
        return ConfigUtils.getBoolean("optimization.tps-monitoring.log-tps-drops", true);
    }

    public void updateTPS() {
        try {
            double tps = 20.0;
            try {
                java.lang.reflect.Method m = org.bukkit.Server.class.getMethod("getTPS");
                Object result = m.invoke(Bukkit.getServer());
                if (result instanceof double[]) {
                    tps = ((double[]) result)[0];
                }
            } catch (Exception ignored) {
            }
            currentTPS = tps;
            tpsHistory.add(currentTPS);
            if (tpsHistory.size() > 60) {
                tpsHistory.remove(0);
            }
            if (shouldLogTPSDrops() && currentTPS < getLowTPSThreshold()) {
                plugin.getLogger().warning("Low TPS detected: " + String.format("%.2f", currentTPS));
            }
        } catch (Exception e) {
            plugin.getLogger().warning("Failed to get TPS: " + e.getMessage());
        }
    }

    public double getCurrentTPS() {
        return currentTPS;
    }

    public double getAverageTPS() {
        if (tpsHistory.isEmpty()) {
            return 20.0;
        }
        return tpsHistory.stream().mapToDouble(Double::doubleValue).average().orElse(20.0);
    }

    public boolean isLowTPS() {
        return currentTPS < getLowTPSThreshold();
    }

    public boolean isVeryLowTPS() {
        return currentTPS < 15.0;
    }

    public List<Double> getTPSHistory() {
        return new ArrayList<>(tpsHistory);
    }

    public String getFormattedTPS() {
        return MessageUtils.formatTPS(currentTPS);
    }

    public String getFormattedAverageTPS() {
        return MessageUtils.formatTPS(getAverageTPS());
    }

    public void reset() {
        tpsHistory.clear();
        currentTPS = 20.0;
    }
}