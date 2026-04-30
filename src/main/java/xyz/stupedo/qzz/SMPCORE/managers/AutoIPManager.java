package xyz.stupedo.qzz.SMPCORE.managers;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import xyz.stupedo.qzz.SMPCORE.SMPCORE;
import xyz.stupedo.qzz.SMPCORE.utils.ConfigUtils;

import java.util.UUID;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

public class AutoIPManager {

    private final SMPCORE plugin;
    private Set<String> bypassListCache;
    private final AtomicInteger totalAutoAdded;

    public AutoIPManager(SMPCORE plugin) {
        this.plugin = plugin;
        this.totalAutoAdded = new AtomicInteger(ConfigUtils.getInt("auto-ip.total-auto-added", 0));
        cacheBypassList();
    }

    private void cacheBypassList() {
        List<String> list = ConfigUtils.getConfig().getStringList("ip-whitelist.bypass-list");
        this.bypassListCache = list != null ? new HashSet<>(list) : new HashSet<>();
    }

    public void reloadCache() {
        cacheBypassList();
    }

    public boolean isEnabled() {
        return ConfigUtils.getBoolean("auto-ip.enabled", false);
    }

    public String getMode() {
        return ConfigUtils.getString("auto-ip.mode", "keep-first");
    }

    public boolean shouldLogAutoAdds() {
        return ConfigUtils.getBoolean("auto-ip.log-auto-adds", true);
    }

    public boolean shouldAutoAddPlayer(Player player) {
        if (!isEnabled()) {
            return false;
        }

        UUID playerUUID = player.getUniqueId();

        if (plugin.getBotManager().shouldBypassIPWhitelist(player)) {
            return false;
        }

        if (bypassListCache != null && bypassListCache.contains(playerUUID.toString())) {
            return false;
        }

        if (plugin.getIpWhitelistManager().isWhitelisted(playerUUID)) {
            if ("keep-first".equals(getMode())) {
                return false;
            }
        }

        return true;
    }

    public void autoAddPlayer(Player player) {
        if (!shouldAutoAddPlayer(player)) {
            return;
        }

        UUID playerUUID = player.getUniqueId();
        java.net.InetSocketAddress addr = player.getAddress();
        if (addr == null) {
            return;
        }
        String playerIP = addr.getAddress().getHostAddress();

        plugin.getIpWhitelistManager().addPlayer(playerUUID, playerIP);
        totalAutoAdded.incrementAndGet();

        if (shouldLogAutoAdds()) {
            plugin.getLogger().info("Auto-added " + player.getName() + " (" + playerUUID + ") with IP " + playerIP + " to whitelist");
        }
    }

    public int getTotalAutoAdded() {
        return totalAutoAdded.get();
    }

    public void resetCounter() {
        totalAutoAdded.set(0);
    }

    public void shutdown() {
        ConfigUtils.getConfig().set("auto-ip.total-auto-added", totalAutoAdded.get());
        ConfigUtils.saveConfig();
    }
}