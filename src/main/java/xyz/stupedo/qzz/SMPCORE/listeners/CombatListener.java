package xyz.stupedo.qzz.SMPCORE.listeners;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.entity.Player;
import xyz.stupedo.qzz.SMPCORE.SMPCORE;

public class CombatListener implements Listener {

    private final SMPCORE plugin;

    public CombatListener(SMPCORE plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onEntityDamage(EntityDamageByEntityEvent event) {
        // Only handle player attacks
        if (!(event.getDamager() instanceof Player)) {
            return;
        }

        Player attacker = (Player) event.getDamager();

        // Always allow attacks - no rate limiting or blocking
        // This ensures hits register smoothly for all players regardless of ping
    }
}