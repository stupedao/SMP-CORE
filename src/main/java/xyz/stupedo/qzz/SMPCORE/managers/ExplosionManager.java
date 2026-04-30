package xyz.stupedo.qzz.SMPCORE.managers;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import xyz.stupedo.qzz.SMPCORE.SMPCORE;
import xyz.stupedo.qzz.SMPCORE.utils.ConfigUtils;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class ExplosionManager {

    private final SMPCORE plugin;
    private final Set<String> enabledWorlds;
    private int explosionsThisTick;
    private int totalExplosionsBlocked;
    private long lastTickUpdate;

    public ExplosionManager(SMPCORE plugin) {
        this.plugin = plugin;
        this.enabledWorlds = new HashSet<>();
        this.explosionsThisTick = 0;
        this.totalExplosionsBlocked = 0;
        this.lastTickUpdate = System.currentTimeMillis();
        loadConfiguration();
    }

    private void loadConfiguration() {
        List<String> worlds = ConfigUtils.getConfig().getStringList("explosion-optimizer.worlds");
        if (worlds != null) {
            enabledWorlds.addAll(worlds);
        }
    }

    public boolean isEnabled() {
        return ConfigUtils.getBoolean("explosion-optimizer.enabled", true);
    }

    public boolean isWorldEnabled(String worldName) {
        return enabledWorlds.contains("*") || enabledWorlds.contains(worldName);
    }

    public double getMaxTNTPower() {
        return ConfigUtils.getDouble("explosion-optimizer.max_tnt_power", 4.0);
    }

    public double getMaxCreeperPower() {
        return ConfigUtils.getDouble("explosion-optimizer.max_creeper_power", 3.0);
    }

    public double getMaxEndCrystalPower() {
        return ConfigUtils.getDouble("explosion-optimizer.max_end_crystal_power", 6.0);
    }

    public boolean shouldPreventChainReactions() {
        return ConfigUtils.getBoolean("explosion-optimizer.prevent_chain_reactions", true);
    }

    public int getChainReactionDelay() {
        return ConfigUtils.getInt("explosion-optimizer.chain_reaction_delay", 10);
    }

    public int getMaxExplosionsPerTick() {
        return ConfigUtils.getInt("explosion-optimizer.max_explosions_per_tick", 5);
    }

    public boolean shouldLimitExplosion(Location location, float power, String explosionType) {
        if (!isEnabled() || !isWorldEnabled(location.getWorld().getName())) {
            return false;
        }

        // Check max explosions per tick using time-based tracking
        long now = System.currentTimeMillis();
        if (now - lastTickUpdate > 50) { // ~1 tick in ms
            explosionsThisTick = 0;
            lastTickUpdate = now;
        }

        if (explosionsThisTick >= getMaxExplosionsPerTick()) {
            totalExplosionsBlocked++;
            return true;
        }

        // Check power limits
        double maxPower = getMaxPowerForType(explosionType);
        if (power > maxPower) {
            totalExplosionsBlocked++;
            return true;
        }

        // Check chain reactions
        if (shouldPreventChainReactions()) {
            // Check if there were recent explosions nearby
            if (hasRecentExplosionsNearby(location, getChainReactionDelay())) {
                totalExplosionsBlocked++;
                return true;
            }
        }

        explosionsThisTick++;
        return false;
    }

    public double getMaxPowerForType(String explosionType) {
        switch (explosionType.toLowerCase()) {
            case "tnt":
            case "primed_tnt":
                return getMaxTNTPower();
            case "creeper":
                return getMaxCreeperPower();
            case "end_crystal":
                return getMaxEndCrystalPower();
            default:
                return 10.0; // Default high limit
        }
    }

    private boolean hasRecentExplosionsNearby(Location location, int delaySeconds) {
        // This would require tracking explosion locations
        // For now, return false (can be enhanced later)
        return false;
    }

    public void onExplosion(Location location, float power, String explosionType) {
        if (shouldLimitExplosion(location, power, explosionType)) {
            plugin.getLogger().info("Blocked explosion at " + location.toString() + 
                " (power: " + power + ", type: " + explosionType + ")");
        }
    }

    public Map<String, Object> getStatistics() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("total_blocked", totalExplosionsBlocked);
        stats.put("this_tick", explosionsThisTick);
        stats.put("max_tnt_power", getMaxTNTPower());
        stats.put("max_creeper_power", getMaxCreeperPower());
        stats.put("max_end_crystal_power", getMaxEndCrystalPower());
        return stats;
    }

    public void resetStatistics() {
        totalExplosionsBlocked = 0;
        explosionsThisTick = 0;
    }
}