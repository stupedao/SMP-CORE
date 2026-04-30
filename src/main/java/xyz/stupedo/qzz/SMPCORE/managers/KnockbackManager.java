package xyz.stupedo.qzz.SMPCORE.managers;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.event.PacketListenerAbstract;
import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientPong;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerPing;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerVelocityEvent;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;
import xyz.stupedo.qzz.SMPCORE.SMPCORE;
import xyz.stupedo.qzz.SMPCORE.utils.ConfigUtils;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class KnockbackManager {

    private static final long PING_OFFSET = 25;
    private static final Random RANDOM = new Random();

    private final SMPCORE plugin;
    private final Map<UUID, PlayerKnockbackData> playerDataMap;
    private boolean enabled;
    private boolean runnableEnabled;
    private long runnableInterval;
    private long combatTimer;
    private long spikeThreshold;
    private BukkitTask pingTask;

    public KnockbackManager(SMPCORE plugin) {
        this.plugin = plugin;
        this.playerDataMap = new ConcurrentHashMap<>();
        loadConfig();
        initPacketEvents();
    }

    private void loadConfig() {
        enabled = ConfigUtils.getBoolean("knockback-sync.enabled", true);
        runnableEnabled = ConfigUtils.getBoolean("knockback-sync.runnable.enabled", true);
        runnableInterval = ConfigUtils.getLong("knockback-sync.runnable.interval", 5L);
        combatTimer = ConfigUtils.getLong("knockback-sync.runnable.combat_timer", 30L);
        spikeThreshold = ConfigUtils.getLong("knockback-sync.spike_threshold", 20L);
    }

    private void initPacketEvents() {
        try {
            PacketEvents.getAPI().getEventManager().registerListener(new PingReceiveListener());
        } catch (Exception e) {
            plugin.getLogger().warning("Failed to initialize packetevents: " + e.getMessage());
        }
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public long getSpikeThreshold() {
        return spikeThreshold;
    }

    public long getCombatTimer() {
        return combatTimer;
    }

    public void onPlayerJoin(Player player) {
        playerDataMap.put(player.getUniqueId(), new PlayerKnockbackData(player));
    }

    public void onPlayerQuit(Player player) {
        PlayerKnockbackData data = playerDataMap.remove(player.getUniqueId());
        if (data != null && data.combatTask != null) {
            data.combatTask.cancel();
        }
    }

    public void onPlayerDamage(EntityDamageByEntityEvent event) {
        if (!enabled) return;

        Entity victim = event.getEntity();
        if (!(victim instanceof Player)) return;

        Player player = (Player) victim;
        PlayerKnockbackData data = playerDataMap.get(player.getUniqueId());
        if (data != null) {
            data.updateCombat(combatTimer);
        }
    }

    public void onVelocity(PlayerVelocityEvent event) {
        if (!enabled) return;
        if (event.isCancelled()) return;

        Player victim = event.getPlayer();
        PlayerKnockbackData data = playerDataMap.get(victim.getUniqueId());
        if (data == null) return;

        EntityDamageEvent entityDamageEvent = victim.getLastDamageCause();
        if (entityDamageEvent == null) return;

        EntityDamageEvent.DamageCause damageCause = entityDamageEvent.getCause();
        if (damageCause != EntityDamageEvent.DamageCause.ENTITY_ATTACK) return;

        Entity attacker = ((EntityDamageByEntityEvent) entityDamageEvent).getDamager();
        if (!(attacker instanceof Player)) return;

        Vector velocity = victim.getVelocity();
        Double verticalVelocity = data.getVerticalVelocity();
        if (verticalVelocity == null || !data.isOnGround(velocity.getY())) return;

        Long lastAdjustment = data.getLastAdjustment();
        if (lastAdjustment != null && System.currentTimeMillis() - lastAdjustment < 250) return;

        Vector adjustedVelocity = velocity.clone().setY(verticalVelocity);
        victim.setVelocity(adjustedVelocity);
        data.setLastAdjustment(System.currentTimeMillis());
    }

    public void setVerticalVelocity(Player victim, Player attacker) {
        PlayerKnockbackData data = playerDataMap.get(victim.getUniqueId());
        if (data != null) {
            data.setVerticalVelocity(data.calculateVerticalVelocity(attacker));
        }
    }

    public void updateCombat(Player player) {
        PlayerKnockbackData data = playerDataMap.get(player.getUniqueId());
        if (data != null) {
            data.updateCombat(combatTimer);
        }
    }

    public void updatePlayerPing(Player player, long ping) {
        PlayerKnockbackData data = playerDataMap.get(player.getUniqueId());
        if (data != null) {
            data.setPing(ping);
        }
    }

    public void startPingTask() {
        if (!enabled || !runnableEnabled || pingTask != null) return;

        pingTask = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            for (Player player : Bukkit.getOnlinePlayers()) {
                PlayerKnockbackData data = playerDataMap.get(player.getUniqueId());
                if (data != null && data.isInCombat()) {
                    sendPingPacket(player);
                }
            }
        }, 0L, runnableInterval);
    }

    public void stopPingTask() {
        if (pingTask != null) {
            pingTask.cancel();
            pingTask = null;
        }
    }

    public void reload() {
        stopPingTask();
        loadConfig();
        if (enabled && runnableEnabled) {
            startPingTask();
        }
    }

    public Map<String, Object> getStatistics() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("enabled", enabled);
        stats.put("tracked_players", playerDataMap.size());
        long inCombat = playerDataMap.values().stream().filter(PlayerKnockbackData::isInCombat).count();
        stats.put("in_combat", inCombat);
        return stats;
    }

    public class PlayerKnockbackData {

        private final Player player;
        private final Map<Integer, Long> timeline;
        private Long ping, previousPing, lastAdjustment;
        private Double verticalVelocity;
        private BukkitTask combatTask;

        public PlayerKnockbackData(Player player) {
            this.player = player;
            this.timeline = new HashMap<>();
        }

        public long getEstimatedPing() {
            long currentPing = (ping != null) ? ping : player.getPing();
            long lastPing = (previousPing != null) ? previousPing : player.getPing();
            long ping = (currentPing - lastPing > spikeThreshold) ? lastPing : currentPing;
            return Math.max(1, ping - PING_OFFSET);
        }

        public boolean isOnGround(double verticalVelocity) {
            Material material = player.getLocation().getBlock().getType();
            if (player.isGliding() || material == Material.WATER || material == Material.LAVA
                    || material == Material.COBWEB || material == Material.SCAFFOLDING)
                return false;

            if (ping == null || ping < PING_OFFSET)
                return false;

            double gDist = getDistanceToGround();
            if (gDist <= 0) return false;

            int tMax = verticalVelocity > 0 ? calculateTimeToMaxVelocity(verticalVelocity) : 0;
            double mH = verticalVelocity > 0 ? calculateDistanceTraveled(verticalVelocity, tMax) : 0;
            int tFall = calculateFallTime(verticalVelocity, mH + gDist);

            return getEstimatedPing() >= tMax + tFall / 20.0 * 1000 && gDist <= 1.3;
        }

        public double getDistanceToGround() {
            double collisionDist = 5;
            World world = player.getWorld();

            for (Location corner : getBBCorners()) {
                RayTraceResult result = world.rayTraceBlocks(corner, new Vector(0, -1, 0), 5, 
                    org.bukkit.FluidCollisionMode.NEVER, true);
                if (result == null || result.getHitBlock() == null) continue;
                collisionDist = Math.min(collisionDist, corner.getY() - result.getHitBlock().getY());
            }

            return collisionDist - 1;
        }

        public Location[] getBBCorners() {
            BoundingBox boundingBox = player.getBoundingBox();
            Location location = player.getLocation();
            World world = location.getWorld();
            double adjustment = 0.01;

            return new Location[] {
                new Location(world, boundingBox.getMinX() + adjustment, location.getY(), boundingBox.getMinZ() + adjustment),
                new Location(world, boundingBox.getMinX() + adjustment, location.getY(), boundingBox.getMaxZ() - adjustment),
                new Location(world, boundingBox.getMaxX() - adjustment, location.getY(), boundingBox.getMinZ() + adjustment),
                new Location(world, boundingBox.getMaxX() - adjustment, location.getY(), boundingBox.getMaxZ() - adjustment)
            };
        }

        public double calculateVerticalVelocity(Player attacker) {
            double yAxis = attacker.getAttackCooldown() > 0.848 ? 0.4 : 0.36080000519752503;

            if (!attacker.isSprinting()) {
                yAxis = 0.36080000519752503;
            }

            if (attacker.getInventory().getItemInMainHand().getEnchantmentLevel(Enchantment.KNOCKBACK) > 0) {
                yAxis = 0.4;
            }

            return yAxis;
        }

        public boolean isInCombat() {
            return combatTask != null;
        }

        public void updateCombat(long timer) {
            if (isInCombat()) {
                combatTask.cancel();
            }
            combatTask = Bukkit.getScheduler().runTaskLater(plugin, () -> {
                if (combatTask != null) {
                    combatTask.cancel();
                    combatTask = null;
                }
            }, timer);
        }

        private int calculateTimeToMaxVelocity(double velocity) {
            return (int) Math.ceil(Math.log(0.1 / Math.abs(velocity)) / Math.log(0.98));
        }

        private double calculateDistanceTraveled(double velocity, int ticks) {
            double distance = 0;
            double currentVelocity = velocity;
            for (int i = 0; i < ticks; i++) {
                distance += currentVelocity;
                currentVelocity *= 0.98;
            }
            return distance;
        }

        private int calculateFallTime(double initialVelocity, double distance) {
            return (int) Math.ceil(Math.sqrt(2 * distance / 0.08));
        }

        public void setPing(Long ping) {
            this.previousPing = this.ping;
            this.ping = ping;
        }

        public Long getPing() {
            return ping;
        }

        public void setLastAdjustment(Long lastAdjustment) {
            this.lastAdjustment = lastAdjustment;
        }

        public Long getLastAdjustment() {
            return lastAdjustment;
        }

        public void setVerticalVelocity(Double verticalVelocity) {
            this.verticalVelocity = verticalVelocity;
        }

        public Double getVerticalVelocity() {
            return verticalVelocity;
        }

        public Map<Integer, Long> getTimeline() {
            return timeline;
        }
    }

    public class PingReceiveListener extends PacketListenerAbstract {

        @Override
        public void onPacketReceive(PacketReceiveEvent event) {
            if (!enabled) return;

            if (event.getPacketType() != PacketType.Play.Client.PONG) return;

            Player player = event.getPlayer();
            if (player == null) return;

            PlayerKnockbackData playerData = playerDataMap.get(player.getUniqueId());
            if (playerData == null) return;

            try {
                int packetId = new WrapperPlayClientPong(event).getId();

                Long sendTime = playerData.getTimeline().get(packetId);
                if (sendTime == null) return;

                long ping = System.currentTimeMillis() - sendTime;

                playerData.getTimeline().remove(packetId);
                playerData.setPing(ping);
            } catch (Exception e) {
                // Ignore packet parse errors
            }
        }
    }

    public void sendPingPacket(Player player) {
        if (!enabled) return;

        PlayerKnockbackData data = playerDataMap.get(player.getUniqueId());
        if (data == null) return;

        Map<Integer, Long> timeline = data.getTimeline();
        if (timeline.size() >= 50) {
            Integer oldestKey = timeline.keySet().iterator().next();
            timeline.remove(oldestKey);
        }

        int packetId = RANDOM.nextInt(1, 10000);
        data.getTimeline().put(packetId, System.currentTimeMillis());

        try {
            WrapperPlayServerPing packet = new WrapperPlayServerPing(packetId);
            PacketEvents.getAPI().getPlayerManager().sendPacket(player, packet);
        } catch (Exception e) {
            // Ignore packet send errors
        }
    }
}