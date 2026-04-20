package xyz.stupedo.qzz.SMPCORE.managers;

import org.bukkit.World;
import org.bukkit.entity.Player;
import xyz.stupedo.qzz.SMPCORE.SMPCORE;
import xyz.stupedo.qzz.SMPCORE.utils.ConfigUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class SleepManager {

    private final SMPCORE plugin;
    private final Map<UUID, Integer> sleepingTasks;

    public SleepManager(SMPCORE plugin) {
        this.plugin = plugin;
        this.sleepingTasks = new HashMap<>();
    }

    public boolean isEnabled() {
        return ConfigUtils.getBoolean("sleep-system.enabled", true);
    }

    public int getDelaySeconds() {
        return ConfigUtils.getInt("sleep-system.delay-seconds", 3);
    }

    public void handlePlayerSleep(Player player) {
        if (!isEnabled()) {
            return;
        }

        World world = player.getWorld();
        long time = world.getTime();

        if (time < 12500 || time > 23000) {
            return;
        }

        UUID playerId = player.getUniqueId();

        if (sleepingTasks.containsKey(playerId)) {
            return;
        }

        int taskId = plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            sleepingTasks.remove(playerId);
            setDay(world);
        }, getDelaySeconds() * 20L).getTaskId();

        sleepingTasks.put(playerId, taskId);
    }

    public void handlePlayerWake(Player player) {
        UUID playerId = player.getUniqueId();
        Integer taskId = sleepingTasks.remove(playerId);
        if (taskId != null) {
            plugin.getServer().getScheduler().cancelTask(taskId);
        }
    }

    private void setDay(World world) {
        world.setTime(1000);
    }

    public void cancelAllTasks() {
        for (Integer taskId : sleepingTasks.values()) {
            plugin.getServer().getScheduler().cancelTask(taskId);
        }
        sleepingTasks.clear();
    }

    public int getSleepingCount() {
        return sleepingTasks.size();
    }
}