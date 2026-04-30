package xyz.stupedo.qzz.SMPCORE.managers;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.entity.Trident;
import xyz.stupedo.qzz.SMPCORE.SMPCORE;
import xyz.stupedo.qzz.SMPCORE.utils.ConfigUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class AbilityManager {

    private final SMPCORE plugin;
    private final Map<UUID, Long> tridentCooldowns;
    private final Map<UUID, Long> elytraLastUse;
    private int tridentUsesBlocked;
    private int elytraUsesBlocked;

    public AbilityManager(SMPCORE plugin) {
        this.plugin = plugin;
        this.tridentCooldowns = new HashMap<>();
        this.elytraLastUse = new HashMap<>();
        this.tridentUsesBlocked = 0;
        this.elytraUsesBlocked = 0;
    }

    public boolean isEnabled() {
        return ConfigUtils.getBoolean("ability-limiter.enabled", true);
    }

    public int getTridentCooldown() {
        return ConfigUtils.getInt("ability-limiter.trident-cooldown", 20);
    }

    public double getElytraSpeedLimit() {
        return ConfigUtils.getDouble("ability-limiter.elytra-speed-limit", 1.0);
    }

    public boolean shouldPreventChunkLoadingAbuse() {
        return ConfigUtils.getBoolean("ability-limiter.prevent-chunk-loading-abuse", true);
    }

    public int getMaxElytraSpeedChunks() {
        return ConfigUtils.getInt("ability-limiter.max-elytra-speed-chunks", 3);
    }

    public boolean canUseTrident(Player player) {
        if (!isEnabled()) {
            return true;
        }

        UUID playerId = player.getUniqueId();
        long currentTime = System.currentTimeMillis();
        long cooldown = getTridentCooldown() * 1000L; // Convert to milliseconds

        Long lastUse = tridentCooldowns.get(playerId);
        if (lastUse != null && (currentTime - lastUse) < cooldown) {
            tridentUsesBlocked++;
            return false;
        }

        return true;
    }

    public void onTridentUse(Player player) {
        if (!isEnabled()) {
            return;
        }

        UUID playerId = player.getUniqueId();
        tridentCooldowns.put(playerId, System.currentTimeMillis());
    }

    public boolean canUseElytra(Player player, double speed) {
        if (!isEnabled()) {
            return true;
        }

        // Check speed limit
        if (speed > getElytraSpeedLimit()) {
            elytraUsesBlocked++;
            return false;
        }

        // Check chunk loading abuse
        if (shouldPreventChunkLoadingAbuse()) {
            UUID playerId = player.getUniqueId();
            Long lastUse = elytraLastUse.get(playerId);

            if (lastUse != null) {
                long timeSinceLastUse = System.currentTimeMillis() - lastUse;
                // If using elytra too frequently, block it
                if (timeSinceLastUse < 100) { // 100ms between uses
                    elytraUsesBlocked++;
                    return false;
                }
            }
        }

        return true;
    }

    public void onElytraUse(Player player) {
        if (!isEnabled()) {
            return;
        }

        UUID playerId = player.getUniqueId();
        elytraLastUse.put(playerId, System.currentTimeMillis());
    }

    public int getTridentUsesBlocked() {
        return tridentUsesBlocked;
    }

    public int getElytraUsesBlocked() {
        return elytraUsesBlocked;
    }

    public Map<String, Integer> getStatistics() {
        Map<String, Integer> stats = new HashMap<>();
        stats.put("trident_uses_blocked", tridentUsesBlocked);
        stats.put("elytra_uses_blocked", elytraUsesBlocked);
        stats.put("trident_cooldown", getTridentCooldown());
        stats.put("elytra_speed_limit", (int) getElytraSpeedLimit());
        return stats;
    }

    public void resetStatistics() {
        tridentUsesBlocked = 0;
        elytraUsesBlocked = 0;
        tridentCooldowns.clear();
        elytraLastUse.clear();
    }
}