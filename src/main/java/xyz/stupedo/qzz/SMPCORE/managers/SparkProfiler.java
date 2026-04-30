package xyz.stupedo.qzz.SMPCORE.managers;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import xyz.stupedo.qzz.SMPCORE.SMPCORE;
import xyz.stupedo.qzz.SMPCORE.utils.ConfigUtils;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class SparkProfiler {

    private final SMPCORE plugin;
    private final List<Double> tpsSamples;
    private double[] recentTps;
    private long lastTickTime;
    private int tickCount;
    private boolean enabled;
    private double[] tpsValues;

    private static final int TICK_INTERVAL = 20;
    private static final int TPS_HISTORY_SIZE = 60;

    private long serverStartTime;

    public SparkProfiler(SMPCORE plugin) {
        this.plugin = plugin;
        this.tpsSamples = Collections.synchronizedList(new ArrayList<>());
        this.recentTps = new double[]{20.0, 20.0, 20.0};
        this.lastTickTime = System.currentTimeMillis();
        this.tickCount = 0;
        this.serverStartTime = System.currentTimeMillis();
        loadConfig();
    }

    private void loadConfig() {
        enabled = ConfigUtils.getBoolean("spark-profiler.enabled", true);
        tpsValues = new double[TPS_HISTORY_SIZE];
        Arrays.fill(tpsValues, 20.0);
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public void tick() {
        if (!enabled) return;

        long now = System.currentTimeMillis();
        long diff = now - lastTickTime;

        if (diff > 0) {
            double tps = 1000.0 / diff * TICK_INTERVAL;
            tps = Math.min(tps, 20.0);

            tpsSamples.add(tps);
            if (tpsSamples.size() > TPS_HISTORY_SIZE) {
                tpsSamples.remove(0);
            }

            if (!tpsSamples.isEmpty()) {
                double avgTps = tpsSamples.stream().mapToDouble(Double::doubleValue).average().orElse(20.0);

                if (tickCount % 20 == 0) {
                    recentTps[0] = calculateTps(0);
                    recentTps[1] = calculateTps(1);
                    recentTps[2] = calculateTps(2);
                }
            }
        }

        lastTickTime = now;
        tickCount++;
    }

    private double calculateTps(int index) {
        if (tpsSamples.isEmpty()) return 20.0;

        int startIdx = Math.max(0, tpsSamples.size() - (index + 1) * 20);
        int endIdx = tpsSamples.size();

        if (startIdx >= endIdx) return 20.0;

        return tpsSamples.subList(startIdx, endIdx).stream()
            .mapToDouble(Double::doubleValue)
            .average()
            .orElse(20.0);
    }

    public double[] getTps() {
        return recentTps;
    }

    public double getTps1m() {
        return recentTps[0];
    }

    public double getTps5m() {
        return recentTps[1];
    }

    public double getTps15m() {
        return recentTps[2];
    }

    public void runProfilerCommand(CommandSender sender, String[] args) {
        if (!enabled) {
            sender.sendMessage("§cSpark profiler is disabled in config.");
            return;
        }

        if (args.length > 0) {
            String subCommand = args[0].toLowerCase();

            switch (subCommand) {
                case "enable":
                    setEnabled(true);
                    sender.sendMessage("§aSpark profiler enabled.");
                    return;
                case "disable":
                    setEnabled(false);
                    sender.sendMessage("§cSpark profiler disabled.");
                    return;
                case "gc":
                    System.gc();
                    sender.sendMessage("§aGarbage collection triggered.");
                    return;
                case "tps":
                    sendTps(sender);
                    return;
                default:
                    sendHelp(sender);
                    return;
            }
        }

        sendTps(sender);
        sendSystemInfo(sender);
    }

    private void sendTps(CommandSender sender) {
        double[] tps = getTps();
        String tps1m = formatTps(tps[0]);
        String tps5m = formatTps(tps[1]);
        String tps15m = formatTps(tps[2]);

        sender.sendMessage("§8§m---§r §6Spark Profiler §8§m---§r");
        sender.sendMessage("§7TPS: §e" + tps1m + " §7| §e" + tps5m + " §7| §e" + tps15m);
    }

    private void sendSystemInfo(CommandSender sender) {
        Runtime runtime = Runtime.getRuntime();
        long maxMemory = runtime.maxMemory() / 1024 / 1024;
        long totalMemory = runtime.totalMemory() / 1024 / 1024;
        long freeMemory = runtime.freeMemory() / 1024 / 1024;
        long usedMemory = totalMemory - freeMemory;

        int playerCount = Bukkit.getOnlinePlayers().size();
        int chunkCount = 0;
        int entityCount = 0;

        for (Player player : Bukkit.getOnlinePlayers()) {
            if (player.getWorld() != null) {
                chunkCount += player.getWorld().getLoadedChunks().length;
            }
        }

        for (org.bukkit.World world : Bukkit.getWorlds()) {
            entityCount += world.getEntities().size();
        }

        sender.sendMessage("§7Uptime: §e" + formatUptime(System.currentTimeMillis() - serverStartTime));
        sender.sendMessage("§7Memory: §e" + usedMemory + "§7/§e" + maxMemory + " §7MB");
        sender.sendMessage("§7Chunks: §e" + chunkCount);
        sender.sendMessage("§7Entities: §e" + entityCount);
        sender.sendMessage("§7Players: §e" + playerCount);
        sender.sendMessage("§8§m---------------------§r");
    }

    private void sendHelp(CommandSender sender) {
        sender.sendMessage("§8§m---§r §6Spark Profiler §8§m---§r");
        sender.sendMessage("§e/spark §7- Show TPS and system info");
        sender.sendMessage("§e/spark tps §7- Show only TPS");
        sender.sendMessage("§e/spark gc §7- Run garbage collection");
        sender.sendMessage("§e/spark enable §7- Enable profiler");
        sender.sendMessage("§e/spark disable §7- Disable profiler");
        sender.sendMessage("§8§m---------------------§r");
    }

    private String formatTps(double tps) {
        if (tps >= 19.5) return "§a" + String.format("%.1f", tps);
        if (tps >= 18.0) return "§e" + String.format("%.1f", tps);
        if (tps >= 15.0) return "§c" + String.format("%.1f", tps);
        return "§44" + String.format("%.1f", tps);
    }

    private String formatUptime(long millis) {
        long seconds = millis / 1000;
        long minutes = seconds / 60;
        long hours = minutes / 60;
        long days = hours / 24;

        if (days > 0) return days + "d " + (hours % 24) + "h";
        if (hours > 0) return hours + "h " + (minutes % 60) + "m";
        if (minutes > 0) return minutes + "m " + (seconds % 60) + "s";
        return seconds + "s";
    }

    public Map<String, Object> getStatistics() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("enabled", enabled);
        stats.put("tps_1m", getTps1m());
        stats.put("tps_5m", getTps5m());
        stats.put("tps_15m", getTps15m());
        stats.put("players", Bukkit.getOnlinePlayers().size());

        Runtime runtime = Runtime.getRuntime();
        long maxMemory = runtime.maxMemory() / 1024 / 1024;
        long totalMemory = runtime.totalMemory() / 1024 / 1024;
        long freeMemory = runtime.freeMemory() / 1024 / 1024;
        stats.put("memory_used", totalMemory - freeMemory);
        stats.put("memory_max", maxMemory);

        return stats;
    }
}