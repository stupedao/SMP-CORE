package xyz.stupedo.qzz.SMPCORE.managers;

import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.entity.*;
import xyz.stupedo.qzz.SMPCORE.SMPCORE;
import xyz.stupedo.qzz.SMPCORE.utils.ConfigUtils;

import java.util.*;

public class EntityManager {

    private final SMPCORE plugin;
    private final Map<Chunk, Integer> entityCounts;
    private int totalEntitiesRemoved;

    public EntityManager(SMPCORE plugin) {
        this.plugin = plugin;
        this.entityCounts = new HashMap<>();
        this.totalEntitiesRemoved = 0;
    }

    public boolean isEnabled() {
        return ConfigUtils.getBoolean("optimization.entity-limits.enabled", true);
    }

    public int getMaxEntitiesPerChunk() {
        return ConfigUtils.getInt("optimization.entity-limits.max-entities-per-chunk", 25);
    }

    public boolean shouldRemoveExcessEntities() {
        return ConfigUtils.getBoolean("optimization.entity-limits.remove-excess-entities", true);
    }

    public void updateEntityCount(Chunk chunk) {
        if (!isEnabled()) {
            return;
        }

        int count = 0;
        for (Entity entity : chunk.getEntities()) {
            if (shouldCountEntity(entity)) {
                count++;
            }
        }
        entityCounts.put(chunk, count);
    }

    private boolean shouldCountEntity(Entity entity) {
        return !(entity instanceof Player) && 
               !(entity instanceof Painting) && 
               !(entity instanceof ItemFrame);
    }

    public int getEntityCount(Chunk chunk) {
        return entityCounts.getOrDefault(chunk, 0);
    }

    public void cleanupExcessEntities() {
        if (!isEnabled() || !shouldRemoveExcessEntities()) {
            return;
        }

        int maxEntities = getMaxEntitiesPerChunk();
        int removed = 0;

        for (World world : Bukkit.getWorlds()) {
            for (Chunk chunk : world.getLoadedChunks()) {
                int count = getEntityCount(chunk);
                if (count > maxEntities) {
                    removed += removeExcessEntities(chunk, count - maxEntities);
                }
            }
        }

        if (removed > 0) {
            totalEntitiesRemoved += removed;
            plugin.getLogger().info("Removed " + removed + " excess entities");
        }
    }

    private int removeExcessEntities(Chunk chunk, int toRemove) {
        List<Entity> entities = new ArrayList<>();
        for (Entity entity : chunk.getEntities()) {
            if (shouldCountEntity(entity)) {
                entities.add(entity);
            }
        }

        entities.sort((e1, e2) -> getRemovalPriority(e1) - getRemovalPriority(e2));

        int removed = 0;
        for (Entity entity : entities) {
            if (removed >= toRemove) {
                break;
            }
            if (canRemoveEntity(entity)) {
                entity.remove();
                removed++;
            }
        }

        updateEntityCount(chunk);
        return removed;
    }

    private int getRemovalPriority(Entity entity) {
        if (entity instanceof Item) {
            return 1;
        } else if (entity instanceof Monster) {
            return 2;
        } else if (entity instanceof Animals) {
            return 3;
        } else if (entity instanceof Tameable) {
            return 4;
        } else if (entity instanceof Villager) {
            return 5;
        } else {
            return 6;
        }
    }

    private boolean canRemoveEntity(Entity entity) {
        if (entity instanceof Player) {
            return false;
        }
        if (entity instanceof Tameable) {
            Tameable tameable = (Tameable) entity;
            return !tameable.isTamed();
        }
        if (entity instanceof Villager) {
            return false;
        }
        return true;
    }

    public int getTotalEntitiesRemoved() {
        return totalEntitiesRemoved;
    }

    public void resetStats() {
        totalEntitiesRemoved = 0;
        entityCounts.clear();
    }

    public Map<String, Integer> getEntityStats() {
        Map<String, Integer> stats = new HashMap<>();
        stats.put("total_removed", totalEntitiesRemoved);
        stats.put("tracked_chunks", entityCounts.size());

        int totalEntities = 0;
        for (int count : entityCounts.values()) {
            totalEntities += count;
        }
        stats.put("total_entities", totalEntities);

        return stats;
    }
}