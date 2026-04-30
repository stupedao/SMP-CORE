package xyz.stupedo.qzz.SMPCORE.managers;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import xyz.stupedo.qzz.SMPCORE.SMPCORE;
import xyz.stupedo.qzz.SMPCORE.utils.ConfigUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class MessageManager {

    private final SMPCORE plugin;
    private final Map<UUID, PlayerMessageSettings> playerSettings;
    private boolean temporaryHide;

    public MessageManager(SMPCORE plugin) {
        this.plugin = plugin;
        this.playerSettings = new HashMap<>();
        this.temporaryHide = false;
    }

    public boolean isEnabled() {
        return ConfigUtils.getBoolean("join-quit-messages.enabled", true);
    }

    public boolean shouldHideJoinMessage(Player player) {
        if (!isEnabled()) {
            return true;
        }

        // Check temporary hide (for low TPS, etc.)
        if (temporaryHide) {
            return true;
        }

        // Check global settings
        if (ConfigUtils.getBoolean("join-quit-messages.global.hide-join-messages", true)) {
            return true;
        }

        // Check per-player settings
        if (ConfigUtils.getBoolean("join-quit-messages.per-player.enabled", false)) {
            PlayerMessageSettings settings = playerSettings.get(player.getUniqueId());
            if (settings != null && settings.hideJoin) {
                return true;
            }
        }

        return false;
    }

    public boolean shouldHideQuitMessage(Player player) {
        if (!isEnabled()) {
            return true;
        }

        // Check temporary hide
        if (temporaryHide) {
            return true;
        }

        // Check global settings
        if (ConfigUtils.getBoolean("join-quit-messages.global.hide-quit-messages", true)) {
            return true;
        }

        // Check per-player settings
        if (ConfigUtils.getBoolean("join-quit-messages.per-player.enabled", false)) {
            PlayerMessageSettings settings = playerSettings.get(player.getUniqueId());
            if (settings != null && settings.hideQuit) {
                return true;
            }
        }

        return false;
    }

    public String formatJoinMessage(Player player) {
        String template = ConfigUtils.getString("join-quit-messages.global.custom-join-message", "");
        if (template == null || template.isEmpty()) {
            return null;
        }
        return formatMessage(player, template);
    }

    public String formatQuitMessage(Player player) {
        String template = ConfigUtils.getString("join-quit-messages.global.custom-quit-message", "");
        if (template == null || template.isEmpty()) {
            return null;
        }
        return formatMessage(player, template);
    }

    private String formatMessage(Player player, String template) {
        if (template == null || template.isEmpty()) {
            return null;
        }

        String formatted = template.replace("%player%", player.getName());

        // Add prefix if enabled
        if (ConfigUtils.getBoolean("join-quit-messages.formatting.use-prefix", true)) {
            String prefix = ConfigUtils.getString("join-quit-messages.formatting.prefix", "&8[&e&l⚡&8] ");
            formatted = prefix + formatted;
        }

        // Add suffix if configured
        String suffix = ConfigUtils.getString("join-quit-messages.formatting.suffix", "");
        if (!suffix.isEmpty()) {
            formatted = formatted + suffix;
        }

        return formatted;
    }

    public boolean isPlayerExempt(Player player) {
        if (!ConfigUtils.getBoolean("join-quit-messages.per-player.enabled", false)) {
            return false;
        }

        String permission = ConfigUtils.getString("join-quit-messages.per-player.exempt-permission", "");
        if (!permission.isEmpty() && player.hasPermission(permission)) {
            return true;
        }

        PlayerMessageSettings settings = playerSettings.get(player.getUniqueId());
        return settings != null && settings.exemptFromGlobal;
    }

    public void setPlayerExempt(UUID uuid, boolean exempt) {
        PlayerMessageSettings settings = playerSettings.computeIfAbsent(uuid, k -> new PlayerMessageSettings());
        settings.exemptFromGlobal = exempt;
    }

    public void setPlayerHideJoin(UUID uuid, boolean hide) {
        PlayerMessageSettings settings = playerSettings.computeIfAbsent(uuid, k -> new PlayerMessageSettings());
        settings.hideJoin = hide;
    }

    public void setPlayerHideQuit(UUID uuid, boolean hide) {
        PlayerMessageSettings settings = playerSettings.computeIfAbsent(uuid, k -> new PlayerMessageSettings());
        settings.hideQuit = hide;
    }

    public void setTemporaryHide(boolean hide) {
        this.temporaryHide = hide;
    }

    public void removePlayerSettings(UUID uuid) {
        playerSettings.remove(uuid);
    }

    public PlayerMessageSettings getPlayerSettings(UUID uuid) {
        return playerSettings.get(uuid);
    }

    public Map<UUID, PlayerMessageSettings> getAllPlayerSettings() {
        return new HashMap<>(playerSettings);
    }

    public static class PlayerMessageSettings {
        public boolean hideJoin;
        public boolean hideQuit;
        public boolean exemptFromGlobal;
        public boolean useCustomFormat;
        public String customJoinMessage;
        public String customQuitMessage;
    }
}