package xyz.stupedo.qzz.SMPCORE;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import xyz.stupedo.qzz.SMPCORE.commands.*;
import xyz.stupedo.qzz.SMPCORE.listeners.*;
import xyz.stupedo.qzz.SMPCORE.managers.*;
import xyz.stupedo.qzz.SMPCORE.tasks.*;

public final class SMPCORE extends JavaPlugin {

    private static SMPCORE instance;

    private IPWhitelistManager ipWhitelistManager;
    private SleepManager sleepManager;
    private OptimizationManager optimizationManager;
    private EntityManager entityManager;
    private ChunkManager chunkManager;
    private TPSMonitor tpsMonitor;
    private PingManager pingManager;
    private NetworkManager networkManager;
    private ResourceManager resourceManager;
    private MemoryManager memoryManager;
    private BotManager botManager;

    @Override
    public void onEnable() {
        instance = this;

        saveDefaultConfig();
        getLogger().info("SMPCORE v" + getDescription().getVersion() + " is enabling...");

        initializeManagers();
        registerCommands();
        registerListeners();
        startTasks();

        getLogger().info("SMPCORE has been enabled successfully!");
    }

    @Override
    public void onDisable() {
        getLogger().info("SMPCORE is disabling...");

        if (optimizationManager != null) {
            optimizationManager.shutdown();
        }

        if (resourceManager != null) {
            resourceManager.shutdown();
        }

        if (memoryManager != null) {
            memoryManager.cleanup();
        }

        getLogger().info("SMPCORE has been disabled successfully!");
    }

    private void initializeManagers() {
        ipWhitelistManager = new IPWhitelistManager(this);
        sleepManager = new SleepManager(this);
        tpsMonitor = new TPSMonitor(this);
        optimizationManager = new OptimizationManager(this);
        entityManager = new EntityManager(this);
        chunkManager = new ChunkManager(this);
        pingManager = new PingManager(this);
        networkManager = new NetworkManager(this);
        resourceManager = new ResourceManager(this);
        memoryManager = new MemoryManager(this);
        botManager = new BotManager(this);
    }

    private void registerCommands() {
        getCommand("smp").setExecutor(new HelpCommand(this));
        getCommand("smpcore").setExecutor(new HelpCommand(this));
        getCommand("ping").setExecutor(new PingCommand(this));

        new AddIPCommand(this);
        new RemoveIPCommand(this);
        new BypassIPCommand(this);
        new ListIPCommand(this);
        new ToggleJoinMessagesCommand(this);
        new ReloadCommand(this);
        new OptimizeCommand(this);
        new ResourceCommand(this);
    }

    private void registerListeners() {
        Bukkit.getPluginManager().registerEvents(new PlayerJoinListener(this), this);
        Bukkit.getPluginManager().registerEvents(new PlayerQuitListener(this), this);
        Bukkit.getPluginManager().registerEvents(new PlayerSleepListener(this), this);
        Bukkit.getPluginManager().registerEvents(new EntitySpawnListener(this), this);
        Bukkit.getPluginManager().registerEvents(new ChunkLoadListener(this), this);
        Bukkit.getPluginManager().registerEvents(new RedstoneListener(this), this);
        Bukkit.getPluginManager().registerEvents(new PingListener(this), this);
    }

    private void startTasks() {
        new OptimizationTask(this).runTaskTimer(this, 20L, 20L);
        new ItemStackingTask(this).runTaskTimer(this, 100L, 100L);
        new TPSCheckTask(this).runTaskTimer(this, 100L, 100L);
        new PingCheckTask(this).runTaskTimer(this, 200L, 200L);
        new ResourceCheckTask(this).runTaskTimer(this, 600L, 600L);
    }

    public static SMPCORE getInstance() {
        return instance;
    }

    public IPWhitelistManager getIpWhitelistManager() {
        return ipWhitelistManager;
    }

    public SleepManager getSleepManager() {
        return sleepManager;
    }

    public OptimizationManager getOptimizationManager() {
        return optimizationManager;
    }

    public EntityManager getEntityManager() {
        return entityManager;
    }

    public ChunkManager getChunkManager() {
        return chunkManager;
    }

    public TPSMonitor getTpsMonitor() {
        return tpsMonitor;
    }

    public PingManager getPingManager() {
        return pingManager;
    }

    public NetworkManager getNetworkManager() {
        return networkManager;
    }

    public ResourceManager getResourceManager() {
        return resourceManager;
    }

    public MemoryManager getMemoryManager() {
        return memoryManager;
    }

    public BotManager getBotManager() {
        return botManager;
    }
}