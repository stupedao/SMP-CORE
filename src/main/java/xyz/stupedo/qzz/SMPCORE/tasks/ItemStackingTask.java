package xyz.stupedo.qzz.SMPCORE.tasks;

import org.bukkit.Material;
import org.bukkit.entity.Item;
import org.bukkit.scheduler.BukkitRunnable;
import xyz.stupedo.qzz.SMPCORE.SMPCORE;
import xyz.stupedo.qzz.SMPCORE.utils.ConfigUtils;

import java.util.HashMap;
import java.util.Map;

public class ItemStackingTask extends BukkitRunnable {

    private final SMPCORE plugin;

    public ItemStackingTask(SMPCORE plugin) {
        this.plugin = plugin;
    }

    @Override
    public void run() {
        if (!ConfigUtils.getBoolean("optimization.item-stacking.enabled", true)) {
            return;
        }

        if (!plugin.getOptimizationManager().isFeatureEnabled("item-stacking")) {
            return;
        }

        int stackRadius = ConfigUtils.getInt("optimization.item-stacking.stack-radius", 3);
        int maxStackSize = ConfigUtils.getInt("optimization.item-stacking.max-stack-size", 64);

        plugin.getServer().getWorlds().forEach(world -> {
            Map<String, Item> itemsToStack = new HashMap<>();

            world.getEntitiesByClass(Item.class).forEach(item -> {
                if (item.isDead() || item.getItemStack().getAmount() >= maxStackSize) {
                    return;
                }

                Material material = item.getItemStack().getType();
                String key = material.name() + "_" + item.getLocation().getBlockX() + "_" + 
                            item.getLocation().getBlockY() + "_" + item.getLocation().getBlockZ();

                Item existingItem = itemsToStack.get(key);
                if (existingItem != null) {
                    int existingAmount = existingItem.getItemStack().getAmount();
                    int newAmount = item.getItemStack().getAmount();

                    if (existingAmount + newAmount <= maxStackSize) {
                        existingItem.getItemStack().setAmount(existingAmount + newAmount);
                        item.remove();
                    }
                } else {
                    itemsToStack.put(key, item);
                }
            });
        });
    }
}