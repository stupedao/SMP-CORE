package xyz.stupedo.qzz.SMPCORE.utils;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryUsage;
import java.text.DecimalFormat;

public class ResourceUtils {

    private static final DecimalFormat df = new DecimalFormat("0.00");

    public static long getUsedMemory() {
        MemoryMXBean memoryBean = ManagementFactory.getMemoryMXBean();
        MemoryUsage heapUsage = memoryBean.getHeapMemoryUsage();
        return heapUsage.getUsed();
    }

    public static long getMaxMemory() {
        MemoryMXBean memoryBean = ManagementFactory.getMemoryMXBean();
        MemoryUsage heapUsage = memoryBean.getHeapMemoryUsage();
        return heapUsage.getMax();
    }

    public static long getTotalMemory() {
        MemoryMXBean memoryBean = ManagementFactory.getMemoryMXBean();
        MemoryUsage heapUsage = memoryBean.getHeapMemoryUsage();
        return heapUsage.getCommitted();
    }

    public static double getMemoryUsagePercentage() {
        long used = getUsedMemory();
        long max = getMaxMemory();
        return max > 0 ? (used * 100.0) / max : 0;
    }

    public static double getFreeMemoryPercentage() {
        return 100.0 - getMemoryUsagePercentage();
    }

    public static long getFreeMemory() {
        return getMaxMemory() - getUsedMemory();
    }

    public static String formatMemory(long bytes) {
        if (bytes < 1024) {
            return bytes + " B";
        } else if (bytes < 1024 * 1024) {
            return df.format(bytes / 1024.0) + " KB";
        } else if (bytes < 1024 * 1024 * 1024) {
            return df.format(bytes / (1024.0 * 1024.0)) + " MB";
        } else {
            return df.format(bytes / (1024.0 * 1024.0 * 1024.0)) + " GB";
        }
    }

    public static String formatMemoryPercentage(double percentage) {
        return df.format(percentage) + "%";
    }

    public static double getProcessCpuLoad() {
        try {
            com.sun.management.OperatingSystemMXBean osBean = 
                (com.sun.management.OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean();
            return osBean.getProcessCpuLoad() * 100;
        } catch (Exception e) {
            return 0;
        }
    }

    public static double getSystemCpuLoad() {
        try {
            com.sun.management.OperatingSystemMXBean osBean = 
                (com.sun.management.OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean();
            return osBean.getSystemCpuLoad() * 100;
        } catch (Exception e) {
            return 0;
        }
    }

    public static int getAvailableProcessors() {
        return Runtime.getRuntime().availableProcessors();
    }

    public static void forceGarbageCollection() {
        System.gc();
    }

    public static String formatCpuLoad(double load) {
        return df.format(load) + "%";
    }

    public static String getResourceSummary() {
        StringBuilder sb = new StringBuilder();
        sb.append("Memory: ").append(formatMemory(getUsedMemory())).append(" / ");
        sb.append(formatMemory(getMaxMemory())).append(" (");
        sb.append(formatMemoryPercentage(getMemoryUsagePercentage())).append(" used)");
        sb.append("\n");
        sb.append("CPU Load: ").append(formatCpuLoad(getProcessCpuLoad()));
        sb.append(" (System: ").append(formatCpuLoad(getSystemCpuLoad())).append(")");
        return sb.toString();
    }
}