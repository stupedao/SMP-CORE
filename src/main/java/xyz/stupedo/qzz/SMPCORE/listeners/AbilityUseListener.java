package xyz.stupedo.qzz.SMPCORE.listeners;

import org.bukkit.entity.Player;
import org.bukkit.entity.Trident;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.event.player.PlayerToggleFlightEvent;
import org.bukkit.util.Vector;
import xyz.stupedo.qzz.SMPCORE.SMPCORE;

public class AbilityUseListener implements Listener {

    private final SMPCORE plugin;

    public AbilityUseListener(SMPCORE plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onTridentThrow(ProjectileLaunchEvent event) {
        if (plugin.getAbilityManager() == null || !plugin.getAbilityManager().isEnabled()) {
            return;
        }

        if (!(event.getEntity() instanceof Trident)) {
            return;
        }

        Trident trident = (Trident) event.getEntity();
        if (!(trident.getShooter() instanceof Player)) {
            return;
        }

        Player player = (Player) trident.getShooter();

        if (!plugin.getAbilityManager().canUseTrident(player)) {
            event.setCancelled(true);
        } else {
            plugin.getAbilityManager().onTridentUse(player);
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onElytraToggle(PlayerToggleFlightEvent event) {
        if (plugin.getAbilityManager() == null || !plugin.getAbilityManager().isEnabled()) {
            return;
        }

        if (!event.isFlying()) {
            return; // Only care about elytra flight
        }

        Player player = event.getPlayer();
        if (!player.isGliding()) {
            return; // Not using elytra
        }

        // Check elytra speed
        Vector velocity = player.getVelocity();
        double speed = velocity.length();

        if (!plugin.getAbilityManager().canUseElytra(player, speed)) {
            // Slow down the player
            velocity.normalize().multiply(plugin.getAbilityManager().getElytraSpeedLimit());
            player.setVelocity(velocity);
        } else {
            plugin.getAbilityManager().onElytraUse(player);
        }
    }
}