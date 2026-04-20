package xyz.stupedo.qzz.SMPCORE.listeners;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import xyz.stupedo.qzz.SMPCORE.SMPCORE;
import xyz.stupedo.qzz.SMPCORE.utils.ConfigUtils;
import xyz.stupedo.qzz.SMPCORE.utils.MessageUtils;

import java.util.List;

public class PlayerJoinListener implements Listener {

    private final SMPCORE plugin;

    public PlayerJoinListener(SMPCORE plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerJoin(PlayerJoinEvent event) {
        var player = event.getPlayer();
        var playerUUID = player.getUniqueId();
        var playerIP = player.getAddress().getAddress().getHostAddress();

        // Handle join messages
        if (ConfigUtils.getBoolean("join-quit-messages.enabled", true)) {
            if (ConfigUtils.getBoolean("join-quit-messages.hide-join-messages", false)) {
                event.setJoinMessage(null);
            } else {
                String customMessage = ConfigUtils.getString("join-quit-messages.custom-join-message", "&e%player% joined the game");
                if (customMessage != null && !customMessage.isEmpty()) {
                    String formattedMessage = customMessage.replace("%player%", player.getName());
                    event.setJoinMessage(MessageUtils.color(formattedMessage));
                }
            }
        }

        // IP Whitelist checks
        if (!plugin.getIpWhitelistManager().isEnabled()) {
            return;
        }

        // Check if player is in bypass list
        List<String> bypassList = ConfigUtils.getConfig().getStringList("ip-whitelist.bypass-list");
        if (bypassList != null && bypassList.contains(playerUUID.toString())) {
            plugin.getLogger().info("Allowing " + player.getName() + " to bypass IP whitelist (in bypass list)");
            plugin.getIpWhitelistManager().logJoinAttempt(player, playerIP, true);
            return;
        }

        // Check if player is a bot and should bypass IP whitelist
        if (plugin.getBotManager().shouldBypassIPWhitelist(player)) {
            plugin.getLogger().info("Allowing bot " + player.getName() + " to bypass IP whitelist");
            plugin.getIpWhitelistManager().logJoinAttempt(player, playerIP, true);
            return;
        }

        plugin.getIpWhitelistManager().logJoinAttempt(player, playerIP, false);

        if (!plugin.getIpWhitelistManager().isWhitelisted(playerUUID)) {
            String kickMessage = plugin.getIpWhitelistManager().getKickMessage();
            player.kickPlayer(MessageUtils.color(kickMessage));
            plugin.getLogger().warning("Blocked join attempt from " + player.getName() + " (" + playerUUID + ") - Not in whitelist");
            event.setJoinMessage(null);
            return;
        }

        if (!plugin.getIpWhitelistManager().isIPAllowed(playerUUID, playerIP)) {
            String kickMessage = plugin.getIpWhitelistManager().getKickMessage();
            player.kickPlayer(MessageUtils.color(kickMessage));
            plugin.getLogger().warning("Blocked join attempt from " + player.getName() + " (" + playerUUID + ") - IP mismatch. Expected: " + 
                plugin.getIpWhitelistManager().getPlayerIP(playerUUID) + ", Got: " + playerIP);
            event.setJoinMessage(null);
            return;
        }

        plugin.getIpWhitelistManager().logJoinAttempt(player, playerIP, true);
        plugin.getLogger().info("Allowed join from " + player.getName() + " (" + playerUUID + ") from IP " + playerIP);
    }
}