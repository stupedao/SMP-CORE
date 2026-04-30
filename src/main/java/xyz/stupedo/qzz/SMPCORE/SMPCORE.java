package xyz.stupedo.qzz.SMPCORE;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import xyz.stupedo.qzz.SMPCORE.commands.*;
import xyz.stupedo.qzz.SMPCORE.listeners.*;
import xyz.stupedo.qzz.SMPCORE.managers.*;
import xyz.stupedo.qzz.SMPCORE.tasks.*;
import xyz.stupedo.qzz.SMPCORE.utils.ConfigUtils;
import com.github.retrooper.packetevents.PacketEvents;
import io.github.retrooper.packetevents.factory.spigot.SpigotPacketEventsBuilder;

public final class SMPCORE extends JavaPlugin {

    private static SMPCORE instance;

    private IPWhitelistManager ipWhitelistManager;
    private SleepManager sleepManager;
    private TPSMonitor tpsMonitor;
    private PingManager pingManager;
    private BotManager botManager;
    private MessageManager messageManager;
    private AutoIPManager autoIPManager;
    private OptimizationManager optimizationManager;
    private EntityManager entityManager;
    private ChunkManager chunkManager;
    private NetworkManager networkManager;
    private ResourceManager resourceManager;
    private MemoryManager memoryManager;
    private MobAiManager mobAiManager;
    private WorldCleanerManager worldCleanerManager;
    private LagShieldManager lagShieldManager;
    private ExplosionManager explosionManager;
    private ConsoleFilterManager consoleFilterManager;
    private VehicleManager vehicleManager;
    private AbilityManager abilityManager;
    private HopperManager hopperManager;
    private PlayerMovementManager playerMovementManager;
    private KnockbackManager knockbackManager;
    private SparkProfiler sparkProfiler;

    @Override
    public void onLoad() {
        PacketEvents.setAPI(SpigotPacketEventsBuilder.build(this));
        PacketEvents.getAPI().load();
    }

    @Override
    public void onEnable() {
        PacketEvents.getAPI().init();

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

        Bukkit.getScheduler().cancelTasks(this);

        if (autoIPManager != null) {
            autoIPManager.shutdown();
        }

        if (optimizationManager != null) {
            optimizationManager.shutdown();
        }

        if (resourceManager != null) {
            resourceManager.shutdown();
        }

        if (knockbackManager != null) {
            knockbackManager.stopPingTask();
        }

        PacketEvents.getAPI().terminate();
        getLogger().info("SMPCORE has been disabled successfully!");
    }

    private void initializeManagers() {
        ipWhitelistManager = new IPWhitelistManager(this);
        sleepManager = new SleepManager(this);
        tpsMonitor = new TPSMonitor(this);
        botManager = new BotManager(this);
        messageManager = new MessageManager(this);
        autoIPManager = new AutoIPManager(this);
        playerMovementManager = new PlayerMovementManager(this);

        if (ConfigUtils.getBoolean("optimization.enabled", true)) {
            optimizationManager = new OptimizationManager(this);
            entityManager = new EntityManager(this);
            chunkManager = new ChunkManager(this);
        }

        if (ConfigUtils.getBoolean("ping-optimization.enabled", true)) {
            pingManager = new PingManager(this);
        }

        if (ConfigUtils.getBoolean("ping-optimization.network-optimization.enabled", false)) {
            networkManager = new NetworkManager(this);
        }

        if (ConfigUtils.getBoolean("resource-optimization.enabled", false)) {
            resourceManager = new ResourceManager(this);
            memoryManager = new MemoryManager(this);
        }

        if (ConfigUtils.getBoolean("mob-ai-reducer.enabled", false)) {
            mobAiManager = new MobAiManager(this);
        }

        if (ConfigUtils.getBoolean("world-cleaner.items.enabled", false) ||
            ConfigUtils.getBoolean("world-cleaner.creatures.enabled", false) ||
            ConfigUtils.getBoolean("world-cleaner.projectiles.enabled", false) ||
            ConfigUtils.getBoolean("world-cleaner.vehicles.enabled", false)) {
            worldCleanerManager = new WorldCleanerManager(this);
        }

        if (ConfigUtils.getBoolean("lag-shield.enabled", false)) {
            lagShieldManager = new LagShieldManager(this);
        }

        if (ConfigUtils.getBoolean("explosion-optimizer.enabled", false)) {
            explosionManager = new ExplosionManager(this);
        }

        if (ConfigUtils.getBoolean("console-filter.enabled", false)) {
            consoleFilterManager = new ConsoleFilterManager(this);
        }

        if (ConfigUtils.getBoolean("vehicle-optimizer.enabled", false)) {
            vehicleManager = new VehicleManager(this);
        }

        if (ConfigUtils.getBoolean("ability-optimizer.enabled", false)) {
            abilityManager = new AbilityManager(this);
        }

        if (ConfigUtils.getBoolean("hopper-optimizer.enabled", false)) {
            hopperManager = new HopperManager(this);
        }

        if (ConfigUtils.getBoolean("knockback-sync.enabled", true)) {
            knockbackManager = new KnockbackManager(this);
        }

        if (ConfigUtils.getBoolean("spark-profiler.enabled", true)) {
            sparkProfiler = new SparkProfiler(this);
        }
    }

    private void registerCommands() {
        AddIPCommand addipCmd = new AddIPCommand(this);
        RemoveIPCommand removeipCmd = new RemoveIPCommand(this);
        BypassIPCommand bypassipCmd = new BypassIPCommand(this);
        ListIPCommand listipCmd = new ListIPCommand(this);
        ToggleJoinMessagesCommand togglejoinCmd = new ToggleJoinMessagesCommand(this);
        AutoIPCommand autoipCmd = new AutoIPCommand(this);
        ReloadCommand reloadCmd = new ReloadCommand(this);
        OptimizeCommand optimizeCmd = new OptimizeCommand(this);
        ResourceCommand resourceCmd = new ResourceCommand(this);

        getCommand("smp").setExecutor(new HelpCommand(this, addipCmd, removeipCmd, bypassipCmd, listipCmd, togglejoinCmd, autoipCmd, reloadCmd, optimizeCmd, resourceCmd));
        getCommand("smpcore").setExecutor(new HelpCommand(this, addipCmd, removeipCmd, bypassipCmd, listipCmd, togglejoinCmd, autoipCmd, reloadCmd, optimizeCmd, resourceCmd));
        getCommand("ping").setExecutor(new PingCommand(this));

        if (getCommand("spark") != null) {
            getCommand("spark").setExecutor(new SparkCommand(this));
        }
    }

    private void registerListeners() {
        Bukkit.getPluginManager().registerEvents(new PlayerJoinListener(this), this);
        Bukkit.getPluginManager().registerEvents(new PlayerQuitListener(this), this);

        if (ConfigUtils.getBoolean("player-movement-optimizer.enabled", true)) {
            Bukkit.getPluginManager().registerEvents(new PlayerMoveListener(this), this);
            Bukkit.getPluginManager().registerEvents(new CombatListener(this), this);
        }

        if (ConfigUtils.getBoolean("sleep-system.enabled", true)) {
            Bukkit.getPluginManager().registerEvents(new PlayerSleepListener(this), this);
        }

        if (optimizationManager != null) {
            Bukkit.getPluginManager().registerEvents(new EntitySpawnListener(this), this);
            Bukkit.getPluginManager().registerEvents(new ChunkLoadListener(this), this);
            Bukkit.getPluginManager().registerEvents(new RedstoneListener(this), this);
        }

        if (pingManager != null) {
            Bukkit.getPluginManager().registerEvents(new PingListener(this), this);
        }

        if (mobAiManager != null) {
            Bukkit.getPluginManager().registerEvents(new MobSpawnListener(this), this);
        }

        if (ConfigUtils.getBoolean("optimization.mob-spawn-control.enabled", false)) {
        }

        if (explosionManager != null) {
            Bukkit.getPluginManager().registerEvents(new ExplosionListener(this), this);
        }

        if (vehicleManager != null) {
            Bukkit.getPluginManager().registerEvents(new VehicleListener(this), this);
        }

        if (ConfigUtils.getBoolean("ability-optimizer.enabled", false)) {
            Bukkit.getPluginManager().registerEvents(new ProjectileListener(this), this);
            Bukkit.getPluginManager().registerEvents(new HopperListener(this), this);
            Bukkit.getPluginManager().registerEvents(new LeafDecayListener(this), this);
            Bukkit.getPluginManager().registerEvents(new AbilityUseListener(this), this);
        }

        if (knockbackManager != null) {
            Bukkit.getPluginManager().registerEvents(new KnockbackListener(this), this);
            knockbackManager.startPingTask();
        }
    }

    private void startTasks() {
        if (optimizationManager != null) {
            new OptimizationTask(this).runTaskTimer(this, 20L, 100L);
        }

        if (ConfigUtils.getBoolean("optimization.item-stacking.enabled", false)) {
            new ItemStackingTask(this).runTaskTimer(this, 100L, 100L);
        }

        if (tpsMonitor != null) {
            new TPSCheckTask(this).runTaskTimer(this, 100L, 600L);
        }

        if (pingManager != null) {
            new PingCheckTask(this).runTaskTimer(this, 200L, 600L);
        }

        if (resourceManager != null && ConfigUtils.getBoolean("resource-optimization.resource-monitoring.enabled", false)) {
            new ResourceCheckTask(this).runTaskTimer(this, 600L, 1200L);
        }

        if (mobAiManager != null) {
            new MobAiTask(this).runTaskTimer(this, 20L, 100L);
        }

        if (worldCleanerManager != null) {
            new WorldCleanerTask(this).runTaskTimer(this, 12000L, 12000L);
        }

        if (lagShieldManager != null) {
            new LagShieldTask(this).runTaskTimer(this, 100L, 200L);
        }

        if (hopperManager != null) {
            new HopperTask(this).runTaskTimer(this, 100L, 100L);
        }

        if (consoleFilterManager != null) {
            new ConsoleFilterTask(this).runTaskTimer(this, 20L, 200L);
        }
    }

    public void reloadManagers() {
        if (optimizationManager != null) {
            optimizationManager.shutdown();
        }

        if (resourceManager != null) {
            resourceManager.shutdown();
        }

        if (knockbackManager != null) {
            knockbackManager.stopPingTask();
        }

        initializeManagers();

        startTasks();

        getLogger().info("Managers reloaded successfully!");
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

    public MessageManager getMessageManager() {
        return messageManager;
    }

    public AutoIPManager getAutoIPManager() {
        return autoIPManager;
    }

    public MobAiManager getMobAiManager() {
        return mobAiManager;
    }

    public WorldCleanerManager getWorldCleanerManager() {
        return worldCleanerManager;
    }

    public LagShieldManager getLagShieldManager() {
        return lagShieldManager;
    }

    public ExplosionManager getExplosionManager() {
        return explosionManager;
    }

    public ConsoleFilterManager getConsoleFilterManager() {
        return consoleFilterManager;
    }

    public VehicleManager getVehicleManager() {
        return vehicleManager;
    }

    public AbilityManager getAbilityManager() {
        return abilityManager;
    }

    public HopperManager getHopperManager() {
        return hopperManager;
    }

    public PlayerMovementManager getPlayerMovementManager() {
        return playerMovementManager;
    }

    public KnockbackManager getKnockbackManager() {
        return knockbackManager;
    }

    public SparkProfiler getSparkProfiler() {
        return sparkProfiler;
    }
}