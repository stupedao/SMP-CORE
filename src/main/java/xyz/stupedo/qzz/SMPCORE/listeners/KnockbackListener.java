package xyz.stupedo.qzz.SMPCORE.listeners;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerVelocityEvent;
import xyz.stupedo.qzz.SMPCORE.SMPCORE;

public class KnockbackListener implements Listener {

    private final SMPCORE plugin;

    public KnockbackListener(SMPCORE plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        if (plugin.getKnockbackManager() != null) {
            plugin.getKnockbackManager().onPlayerJoin(event.getPlayer());
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        if (plugin.getKnockbackManager() != null) {
            plugin.getKnockbackManager().onPlayerQuit(event.getPlayer());
        }
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onPlayerVelocity(PlayerVelocityEvent event) {
        if (plugin.getKnockbackManager() == null) return;
        plugin.getKnockbackManager().onVelocity(event);
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onEntityDamage(EntityDamageByEntityEvent event) {
        if (plugin.getKnockbackManager() == null) return;
        
        if (!(event.getEntity() instanceof Player victim) || !(event.getDamager() instanceof Player attacker)) {
            return;
        }

        plugin.getKnockbackManager().onPlayerDamage(event);
        
        plugin.getKnockbackManager().setVerticalVelocity(victim, attacker);
        plugin.getKnockbackManager().updateCombat(victim);
    }
}