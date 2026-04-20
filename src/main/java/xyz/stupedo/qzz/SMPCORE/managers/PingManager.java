package xyz.stupedo.qzz.SMPCORE.managers;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import xyz.stupedo.qzz.SMPCORE.SMPCORE;
import xyz.stupedo.qzz.SMPCORE.utils.ConfigUtils;
import xyz.stupedo.qzz.SMPCORE.utils.MessageUtils;

import java.util.*;

public class PingManager {

    private final SMPCORE plugin;
    private final Map<UUID, Integer> playerPings;
    private final Map<UUID, List<Integer>> pingHistory;
    private int totalWarningsIssued;
    private int totalKicks;

    public PingManager(SMPCORE plugin) {
        this.plugin = plugin;
        this.playerPings = new HashMap<>();
        this.pingHistory = new HashMap<>();
        this.totalWarningsIssued = 0;
        this.totalKicks = 0;
    }

    public boolean isEnabled() {
        return ConfigUtils.getBoolean("ping-optimization.ping-monitoring.enabled", true);
    }

    public int getHighPingThreshold() {
        return ConfigUtils.getInt("ping-optimization.ping-monitoring.high-ping-threshold", 2000);
    }

    public boolean shouldKickHighPing() {
        return ConfigUtils.getBoolean("ping-optimization.high-ping-management.kick-high-ping", true);
    }

    public int getKickThreshold() {
        return ConfigUtils.getInt("ping-optimization.high-ping-management.kick-threshold", 2000);
    }

    public boolean shouldWarnBeforeKick() {
        return ConfigUtils.getBoolean("ping-optimization.high-ping-management.warn-before-kick", true);
    }

    public int getWarnThreshold() {
        return ConfigUtils.getInt("ping-optimization.high-ping-management.warn-threshold", 1500);
    }

    public void updatePlayerPing(Player player) {
        if (!isEnabled()) {
            return;
        }

        // Check if player is a bot and should bypass ping kick
        if (plugin.getBotManager().shouldBypassPingKick(player)) {
            return;
        }

        int ping = player.getPing();
        UUID playerId = player.getUniqueId();

        playerPings.put(playerId, ping);

        pingHistory.computeIfAbsent(playerId, k -> new ArrayList<>()).add(ping);
        List<Integer> history = pingHistory.get(playerId);
        if (history.size() > 60) {
            history.remove(0);
        }

        if (shouldKickHighPing() && ping >= getKickThreshold()) {
            kickPlayer(player, ping);
        } else if (shouldWarnBeforeKick() && ping >= getWarnThreshold()) {
            warnPlayer(player, ping);
        }
    }

    private void warnPlayer(Player player, int ping) {
        String message = ConfigUtils.getString("ping-optimization.high-ping-management.warning-message", 
            "&eWarning: Your ping is high! Consider improving your connection.");
        player.sendMessage(MessageUtils.color(message));
        totalWarningsIssued++;
    }

    private void kickPlayer(Player player, int ping) {
        String message = ConfigUtils.getString("ping-optimization.high-ping-management.kick-message", 
            "&cYour ping is too high! Please improve your connection.");
        player.kickPlayer(MessageUtils.color(message));
        totalKicks++;
        plugin.getLogger().info("Kicked player " + player.getName() + " for high ping: " + ping + "ms");
    }

    public int getPlayerPing(UUID playerId) {
        return playerPings.getOrDefault(playerId, -1);
    }

    public int getPlayerPing(Player player) {
        return getPlayerPing(player.getUniqueId());
    }

    public double getAveragePing(UUID playerId) {
        List<Integer> history = pingHistory.get(playerId);
        if (history == null || history.isEmpty()) {
            return 0;
        }
        double sum = 0;
        for (int ping : history) {
            sum += ping;
        }
        return sum / history.size();
    }

    public List<Map.Entry<UUID, Integer>> getTopPingPlayers(int limit) {
        List<Map.Entry<UUID, Integer>> sorted = new ArrayList<>(playerPings.entrySet());
        sorted.sort((e1, e2) -> e2.getValue().compareTo(e1.getValue()));
        return sorted.subList(0, Math.min(limit, sorted.size()));
    }

    public Map<String, Integer> getPingStats() {
        Map<String, Integer> stats = new HashMap<>();
        stats.put("total_players", playerPings.size());
        stats.put("total_warnings", totalWarningsIssued);
        stats.put("total_kicks", totalKicks);

        if (!playerPings.isEmpty()) {
            double sum = 0;
            for (int ping : playerPings.values()) {
                sum += ping;
            }
            stats.put("average_ping", (int) (sum / playerPings.size()));
            stats.put("highest_ping", Collections.max(playerPings.values()));
            stats.put("lowest_ping", Collections.min(playerPings.values()));
        }

        return stats;
    }

    public void removePlayer(UUID playerId) {
        playerPings.remove(playerId);
        pingHistory.remove(playerId);
    }

    public void resetStats() {
        totalWarningsIssued = 0;
        totalKicks = 0;
        playerPings.clear();
        pingHistory.clear();
    }

    public String getFormattedPing(UUID playerId) {
        int ping = getPlayerPing(playerId);
        return ping >= 0 ? MessageUtils.formatPing(ping) : "N/A";
    }

    public String getFormattedPing(Player player) {
        return getFormattedPing(player.getUniqueId());
    }
}