package xyz.stupedo.qzz.SMPCORE.utils;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import xyz.stupedo.qzz.SMPCORE.SMPCORE;

import java.io.File;
import java.io.IOException;

public class ConfigUtils {

    public static boolean isEnabled(String path) {
        return SMPCORE.getInstance().getConfig().getBoolean(path + ".enabled", false);
    }

    public static int getInt(String path, int defaultValue) {
        return SMPCORE.getInstance().getConfig().getInt(path, defaultValue);
    }

    public static double getDouble(String path, double defaultValue) {
        return SMPCORE.getInstance().getConfig().getDouble(path, defaultValue);
    }

    public static String getString(String path, String defaultValue) {
        return SMPCORE.getInstance().getConfig().getString(path, defaultValue);
    }

    public static boolean getBoolean(String path, boolean defaultValue) {
        return SMPCORE.getInstance().getConfig().getBoolean(path, defaultValue);
    }

    public static void reloadConfig() {
        SMPCORE.getInstance().reloadConfig();
    }

    public static void saveConfig() {
        SMPCORE.getInstance().saveConfig();
    }

    public static FileConfiguration getConfig() {
        return SMPCORE.getInstance().getConfig();
    }

    public static void set(String path, Object value) {
        SMPCORE.getInstance().getConfig().set(path, value);
        saveConfig();
    }

    public static void createDefaultConfig() {
        SMPCORE.getInstance().saveDefaultConfig();
    }

    public static File getDataFolder() {
        return SMPCORE.getInstance().getDataFolder();
    }

    public static File getFile(String name) {
        return new File(getDataFolder(), name);
    }

    public static YamlConfiguration loadYaml(File file) {
        return YamlConfiguration.loadConfiguration(file);
    }

    public static void saveYaml(File file, YamlConfiguration config) {
        try {
            config.save(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}