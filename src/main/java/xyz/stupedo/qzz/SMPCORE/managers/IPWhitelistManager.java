package xyz.stupedo.qzz.SMPCORE.managers;

import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import xyz.stupedo.qzz.SMPCORE.SMPCORE;
import xyz.stupedo.qzz.SMPCORE.utils.ConfigUtils;
import xyz.stupedo.qzz.SMPCORE.utils.MessageUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;

public class IPWhitelistManager {

    private final SMPCORE plugin;
    private final Map<UUID, String> whitelist;

    public IPWhitelistManager(SMPCORE plugin) {
        this.plugin = plugin;
        this.whitelist = new HashMap<>();
        loadWhitelist();
    }

    public void loadWhitelist() {
        whitelist.clear();
        ConfigurationSection section = ConfigUtils.getConfig().getConfigurationSection("ip-whitelist.whitelist");
        if (section != null) {
            for (String uuidString : section.getKeys(false)) {
                try {
                    UUID uuid = UUID.fromString(uuidString);
                    String ip = section.getString(uuidString);
                    whitelist.put(uuid, ip);
                } catch (IllegalArgumentException e) {
                    plugin.getLogger().warning("Invalid UUID in whitelist: " + uuidString);
                }
            }
        }
        plugin.getLogger().info("Loaded " + whitelist.size() + " IP whitelist entries");
    }

    public void saveWhitelist() {
        ConfigUtils.getConfig().set("ip-whitelist.whitelist", null);
        for (Map.Entry<UUID, String> entry : whitelist.entrySet()) {
            ConfigUtils.getConfig().set("ip-whitelist.whitelist." + entry.getKey().toString(), entry.getValue());
        }
        ConfigUtils.saveConfig();
    }

    public boolean isWhitelisted(UUID uuid) {
        return whitelist.containsKey(uuid);
    }

    public boolean isIPAllowed(UUID uuid, String ip) {
        String allowedIP = whitelist.get(uuid);
        return allowedIP != null && allowedIP.equals(ip);
    }

    public boolean addPlayer(UUID uuid, String ip) {
        if (whitelist.containsKey(uuid)) {
            return false;
        }
        whitelist.put(uuid, ip);
        saveWhitelist();
        return true;
    }

    public boolean removePlayer(UUID uuid) {
        if (!whitelist.containsKey(uuid)) {
            return false;
        }
        whitelist.remove(uuid);
        saveWhitelist();
        return true;
    }

    public String getPlayerIP(UUID uuid) {
        return whitelist.get(uuid);
    }

    public Map<UUID, String> getWhitelist() {
        return new HashMap<>(whitelist);
    }

    public int getWhitelistSize() {
        return whitelist.size();
    }

    public void logJoinAttempt(Player player, String ip, boolean allowed) {
        if (ConfigUtils.getBoolean("ip-whitelist.log-attempts", true)) {
            String message = String.format("IP Join Attempt: %s (%s) from %s - %s",
                player.getName(),
                player.getUniqueId().toString(),
                ip,
                allowed ? "ALLOWED" : "DENIED"
            );
            plugin.getLogger().log(Level.INFO, message);
        }
    }

    public String getKickMessage() {
        return ConfigUtils.getString("ip-whitelist.kick-message", "&cYou are not authorized to join from this IP!");
    }

    public boolean isEnabled() {
        return ConfigUtils.getBoolean("ip-whitelist.enabled", true);
    }
}