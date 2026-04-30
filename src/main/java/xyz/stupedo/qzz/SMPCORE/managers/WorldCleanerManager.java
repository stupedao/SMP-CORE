package xyz.stupedo.qzz.SMPCORE.managers;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.*;
import org.bukkit.inventory.ItemStack;
import xyz.stupedo.qzz.SMPCORE.SMPCORE;
import xyz.stupedo.qzz.SMPCORE.utils.ConfigUtils;
import xyz.stupedo.qzz.SMPCORE.utils.MessageUtils;

import java.util.*;
import java.util.logging.Level;

public class WorldCleanerManager {

    private final SMPCORE plugin;
    private final Set<String> enabledWorlds;
    private int itemsRemoved;
    private int creaturesRemoved;
    private int projectilesRemoved;

    public WorldCleanerManager(SMPCORE plugin) {
        this.plugin = plugin;
        this.enabledWorlds = new HashSet<>();
        this.itemsRemoved = 0;
        this.creaturesRemoved = 0;
        this.projectilesRemoved = 0;
        loadConfiguration();
    }

    private void loadConfiguration() {
        List<String> worlds = ConfigUtils.getConfig().getStringList("world-cleaner.worlds");
        if (worlds != null) {
            enabledWorlds.addAll(worlds);
        }
    }

    public boolean isEnabled() {
        return ConfigUtils.getBoolean("world-cleaner.enabled", true);
    }

    public boolean isWorldEnabled(String worldName) {
        return enabledWorlds.contains("*") || enabledWorlds.contains(worldName);
    }

    public int getInterval() {
        return ConfigUtils.getInt("world-cleaner.interval", 600);
    }

    public void performCleanup() {
        if (!isEnabled()) {
            return;
        }

        plugin.getLogger().info("Starting WorldCleaner cleanup...");

        int itemsCleaned = cleanItems();
        int creaturesCleaned = cleanCreatures();
        int projectilesCleaned = cleanProjectiles();

        itemsRemoved += itemsCleaned;
        creaturesRemoved += creaturesCleaned;
        projectilesRemoved += projectilesCleaned;

        plugin.getLogger().info("WorldCleaner cleanup completed: " + itemsCleaned + " items, " + 
            creaturesCleaned + " creatures, " + projectilesCleaned + " projectiles removed");

        sendAlerts(itemsCleaned, creaturesCleaned, projectilesCleaned);
    }

    private int cleanItems() {
        if (!ConfigUtils.getBoolean("world-cleaner.items.enabled", true)) {
            return 0;
        }

        int timeLived = ConfigUtils.getInt("world-cleaner.items.time_lived", 10000);
        Set<String> blacklist = new HashSet<>(ConfigUtils.getConfig().getStringList("world-cleaner.items.blacklist"));

        int removed = 0;

        for (World world : Bukkit.getWorlds()) {
            if (!isWorldEnabled(world.getName())) {
                continue;
            }

            for (Entity entity : world.getEntities()) {
                if (!(entity instanceof Item)) {
                    continue;
                }

                Item item = (Item) entity;
                ItemStack itemStack = item.getItemStack();

                if (blacklist != null && blacklist.contains(itemStack.getType().name())) {
                    continue;
                }

                int ticksLived = item.getTicksLived();
                if (ticksLived < timeLived / 50) {
                    continue;
                }

                item.remove();
                removed++;
            }
        }

        return removed;
    }

    private int cleanCreatures() {
        if (!ConfigUtils.getBoolean("world-cleaner.creatures.enabled", true)) {
            return 0;
        }

        boolean removeNamed = ConfigUtils.getBoolean("world-cleaner.creatures.named", false);
        boolean dropItems = ConfigUtils.getBoolean("world-cleaner.creatures.drop_items", false);
        boolean ignoreModels = ConfigUtils.getBoolean("world-cleaner.creatures.ignore_models", true);
        boolean listMode = ConfigUtils.getBoolean("world-cleaner.creatures.list_mode", true);
        List<String> list = ConfigUtils.getConfig().getStringList("world-cleaner.creatures.list");

        int removed = 0;

        for (World world : Bukkit.getWorlds()) {
            if (!isWorldEnabled(world.getName())) {
                continue;
            }

            for (Entity entity : world.getEntities()) {
                if (!(entity instanceof LivingEntity)) {
                    continue;
                }

                LivingEntity living = (LivingEntity) entity;

                // Skip players
                if (living instanceof Player) {
                    continue;
                }

                // Check named
                if (!removeNamed && living.getCustomName() != null) {
                    continue;
                }

                // Check model entities
                if (ignoreModels && isModelEntity(living)) {
                    continue;
                }

                String entityType = living.getType().name();
                if (list == null) continue;
                if (listMode) {
                    if (!list.contains(entityType)) {
                        continue;
                    }
                } else {
                    if (list.contains(entityType)) {
                        continue;
                    }
                }

                // Drop items if configured
                if (dropItems) {
                    for (ItemStack drop : living.getEquipment().getArmorContents()) {
                        if (drop != null && !drop.getType().isAir()) {
                            living.getWorld().dropItemNaturally(living.getLocation(), drop);
                        }
                    }
                }

                // Remove creature
                living.remove();
                removed++;
            }
        }

        return removed;
    }

    private int cleanProjectiles() {
        if (!ConfigUtils.getBoolean("world-cleaner.projectiles.enabled", true)) {
            return 0;
        }

        boolean listMode = ConfigUtils.getBoolean("world-cleaner.projectiles.list_mode", true);
        List<String> list = ConfigUtils.getConfig().getStringList("world-cleaner.projectiles.list");

        int removed = 0;

        for (World world : Bukkit.getWorlds()) {
            if (!isWorldEnabled(world.getName())) {
                continue;
            }

            for (Entity entity : world.getEntities()) {
                if (!(entity instanceof Projectile)) {
                    continue;
                }

                Projectile projectile = (Projectile) entity;
                String entityType = projectile.getType().name();

                // Check list mode
                if (listMode) {
                    if (!list.contains(entityType)) {
                        continue;
                    }
                } else {
                    if (list.contains(entityType)) {
                        continue;
                    }
                }

                // Remove projectile
                projectile.remove();
                removed++;
            }
        }

        return removed;
    }

    private boolean isModelEntity(LivingEntity entity) {
        // Check for common model entity indicators
        // This is a simplified check - real implementation would check for specific plugins
        return entity.hasMetadata("ModelEngine") || entity.hasMetadata("MythicMobs");
    }

    private void sendAlerts(int items, int creatures, int projectiles) {
        if (!ConfigUtils.getBoolean("world-cleaner.alerts.enabled", true)) {
            return;
        }

        boolean actionBar = ConfigUtils.getBoolean("world-cleaner.alerts.actionbar", false);
        boolean message = ConfigUtils.getBoolean("world-cleaner.alerts.message", true);
        String permission = ConfigUtils.getString("world-cleaner.alerts.permission", "");

        String alertMessage = MessageUtils.color("&e[WorldCleaner] &fRemoved " + items + " items, " +
            creatures + " creatures, " + projectiles + " projectiles");

        for (Player player : Bukkit.getOnlinePlayers()) {
            if (!permission.isEmpty() && !player.hasPermission(permission)) {
                continue;
            }

            if (message) {
                player.sendMessage(alertMessage);
            }

if (actionBar) {
                player.spigot().sendMessage(net.md_5.bungee.api.chat.TextComponent.fromLegacyText(alertMessage));
            }
        }
    }

    public Map<String, Integer> getStatistics() {
        Map<String, Integer> stats = new HashMap<>();
        stats.put("items_removed", itemsRemoved);
        stats.put("creatures_removed", creaturesRemoved);
        stats.put("projectiles_removed", projectilesRemoved);
        stats.put("total_removed", itemsRemoved + creaturesRemoved + projectilesRemoved);
        return stats;
    }

    public void resetStatistics() {
        itemsRemoved = 0;
        creaturesRemoved = 0;
        projectilesRemoved = 0;
    }
}