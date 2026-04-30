package xyz.stupedo.qzz.SMPCORE.managers;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import xyz.stupedo.qzz.SMPCORE.SMPCORE;
import xyz.stupedo.qzz.SMPCORE.utils.ConfigUtils;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class MobAiManager {

    private final SMPCORE plugin;
    private final Map<UUID, Long> optimizedEntities;
    private final Set<String> enabledWorlds;
    private int purgeCounter;

    public MobAiManager(SMPCORE plugin) {
        this.plugin = plugin;
        this.optimizedEntities = new ConcurrentHashMap<>();
        this.enabledWorlds = new HashSet<>();
        this.purgeCounter = 0;
        loadConfiguration();
    }

    private void loadConfiguration() {
        List<String> worlds = ConfigUtils.getConfig().getStringList("mob-ai-reducer.worlds");
        if (worlds != null) {
            enabledWorlds.addAll(worlds);
        }
    }

    public boolean isEnabled() {
        return ConfigUtils.getBoolean("mob-ai-reducer.enabled", true);
    }

    public boolean isWorldEnabled(String worldName) {
        return enabledWorlds.contains("*") || enabledWorlds.contains(worldName);
    }

    public boolean shouldOptimizeEntity(Entity entity) {
        if (!isEnabled() || !isWorldEnabled(entity.getWorld().getName())) {
            return false;
        }

        // Check if entity is a living entity
        if (!(entity instanceof LivingEntity)) {
            return false;
        }

        LivingEntity living = (LivingEntity) entity;

        // Skip players
        if (living instanceof Player) {
            return false;
        }

        // Check if already optimized
        if (optimizedEntities.containsKey(entity.getUniqueId())) {
            return true;
        }

        // Check entity type settings
        String entityType = entity.getType().name();

        // Check list mode
        boolean listMode = ConfigUtils.getBoolean("mob-ai-reducer.entities.list_mode", false);
        List<String> list = ConfigUtils.getConfig().getStringList("mob-ai-reducer.entities.list");

        if (listMode) {
            // Only optimize listed entities
            return list.contains(entityType);
        } else {
            // Optimize all unlisted entities
            return !list.contains(entityType);
        }
    }

    public void optimizeEntity(Entity entity) {
        if (!shouldOptimizeEntity(entity)) {
            return;
        }

        UUID entityId = entity.getUniqueId();
        optimizedEntities.put(entityId, System.currentTimeMillis());

        // Apply AI optimization
        applyAiOptimization((LivingEntity) entity);
    }

    private void applyAiOptimization(LivingEntity entity) {
        // Disable AI
        entity.setAI(false);

        // Set collision based on config
        boolean collides = ConfigUtils.getBoolean("mob-ai-reducer.values.collides", true);
        entity.setCollidable(collides);

        // Set silent based on config
        boolean silent = ConfigUtils.getBoolean("mob-ai-reducer.values.silent", false);
        if (silent) {
            entity.setSilent(true);
        }
    }

    public void unoptimizeEntity(Entity entity) {
        UUID entityId = entity.getUniqueId();
        if (optimizedEntities.containsKey(entityId)) {
            optimizedEntities.remove(entityId);

            // Restore AI
            if (entity instanceof LivingEntity) {
                LivingEntity living = (LivingEntity) entity;
                living.setAI(true);
                living.setCollidable(true);
                living.setSilent(false);
            }
        }
    }

    public boolean isOptimized(Entity entity) {
        return optimizedEntities.containsKey(entity.getUniqueId());
    }

    public void purgeCache() {
        int purgeInterval = ConfigUtils.getInt("mob-ai-reducer.values.purge_interval", 30);
        long currentTime = System.currentTimeMillis();
        long expireTime = purgeInterval * 1000L;

        int purged = 0;
        Iterator<Map.Entry<UUID, Long>> iterator = optimizedEntities.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<UUID, Long> entry = iterator.next();
            if (currentTime - entry.getValue() > expireTime) {
                // Check if entity still exists
                Entity entity = Bukkit.getEntity(entry.getKey());
                if (entity == null || !entity.isValid()) {
                    iterator.remove();
                    purged++;
                }
            }
        }

        purgeCounter++;
        if (purged > 0) {
            plugin.getLogger().info("Purged " + purged + " expired entities from MobAI cache (purge #" + purgeCounter + ")");
        }
    }

    public int getOptimizedCount() {
        return optimizedEntities.size();
    }

    public void clearAllOptimizations() {
        for (UUID entityId : optimizedEntities.keySet()) {
            Entity entity = Bukkit.getEntity(entityId);
            if (entity != null && entity.isValid()) {
                unoptimizeEntity(entity);
            }
        }
        optimizedEntities.clear();
        plugin.getLogger().info("Cleared all MobAI optimizations");
    }

    public Map<String, Object> getStatistics() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("optimized_entities", optimizedEntities.size());
        stats.put("purge_count", purgeCounter);
        stats.put("enabled_worlds", enabledWorlds.size());
        return stats;
    }
}