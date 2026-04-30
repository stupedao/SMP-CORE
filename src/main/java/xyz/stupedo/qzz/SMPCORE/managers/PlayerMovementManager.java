package xyz.stupedo.qzz.SMPCORE.managers;

import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerMoveEvent;
import xyz.stupedo.qzz.SMPCORE.SMPCORE;
import xyz.stupedo.qzz.SMPCORE.utils.ConfigUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PlayerMovementManager {

    private final SMPCORE plugin;
    private final Map<UUID, Integer> playerPings;
    private int totalOptimized;
    private int totalAllowed;

    public PlayerMovementManager(SMPCORE plugin) {
        this.plugin = plugin;
        this.playerPings = new HashMap<>();
        this.totalOptimized = 0;
        this.totalAllowed = 0;
    }

    public boolean isEnabled() {
        return ConfigUtils.getBoolean("player-movement-optimizer.enabled", true);
    }

    public boolean shouldOptimizeMove(PlayerMoveEvent event) {
        if (!isEnabled()) {
            return false;
        }

        Player player = event.getPlayer();

        // Skip if player has bypass permission
        if (player.hasPermission("smpcore.bypass.movement")) {
            return false;
        }

        // Get player ping
        int ping = player.getPing();
        int highPingThreshold = ConfigUtils.getInt("player-movement-optimizer.high-ping-threshold", 100);

        // Track player ping
        playerPings.put(player.getUniqueId(), ping);

        // HIGH PING PLAYERS (100+ms) - DON'T OPTIMIZE, LET THEM MOVE FREELY
        if (ping >= highPingThreshold) {
            totalAllowed++;
            return false; // Don't cancel - let them move smoothly
        }

        // LOW PING PLAYERS (<100ms) - Also don't optimize (cancelled events cause teleporting back)
        // Instead, we just track them but don't interfere
        totalAllowed++;
        return false;
    }

    public boolean shouldAllowAttack(Player player) {
        // Always allow attacks - no rate limiting
        return true;
    }

    public int getPlayerPing(Player player) {
        return playerPings.getOrDefault(player.getUniqueId(), player.getPing());
    }

    public int getTotalOptimized() {
        return totalOptimized;
    }

    public int getTotalAllowed() {
        return totalAllowed;
    }

    public void cleanup() {
        // Clean up disconnected players
        for (Player player : plugin.getServer().getOnlinePlayers()) {
            if (!player.isOnline()) {
                playerPings.remove(player.getUniqueId());
            }
        }
    }

    public Map<String, Object> getStatistics() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("total_optimized", totalOptimized);
        stats.put("total_allowed", totalAllowed);
        stats.put("tracked_players", playerPings.size());
        return stats;
    }
}