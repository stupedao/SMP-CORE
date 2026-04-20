package xyz.stupedo.qzz.SMPCORE.managers;

import org.bukkit.entity.Player;
import xyz.stupedo.qzz.SMPCORE.SMPCORE;
import xyz.stupedo.qzz.SMPCORE.utils.ConfigUtils;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class BotManager {

    private final SMPCORE plugin;
    private final Set<UUID> botUUIDs;
    private final Set<String> botNames;

    public BotManager(SMPCORE plugin) {
        this.plugin = plugin;
        this.botUUIDs = new HashSet<>();
        this.botNames = new HashSet<>();
        loadBotConfig();
    }

    private void loadBotConfig() {
        if (!ConfigUtils.getBoolean("bot-bypass.enabled", true)) {
            return;
        }

        List<String> nameList = ConfigUtils.getConfig().getStringList("bot-bypass.bot-names");
        if (nameList != null) {
            botNames.addAll(nameList);
        }

        List<String> uuidList = ConfigUtils.getConfig().getStringList("bot-bypass.bot-uuids");
        if (uuidList != null) {
            for (String uuidString : uuidList) {
                try {
                    botUUIDs.add(UUID.fromString(uuidString));
                } catch (IllegalArgumentException e) {
                    plugin.getLogger().warning("Invalid bot UUID: " + uuidString);
                }
            }
        }

        plugin.getLogger().info("Loaded " + botUUIDs.size() + " bot UUIDs and " + botNames.size() + " bot name patterns");
    }

    public boolean isBot(Player player) {
        if (!ConfigUtils.getBoolean("bot-bypass.enabled", true)) {
            return false;
        }

        UUID uuid = player.getUniqueId();
        String name = player.getName();

        if (botUUIDs.contains(uuid)) {
            return true;
        }

        for (String botName : botNames) {
            if (name.toLowerCase().contains(botName.toLowerCase())) {
                return true;
            }
        }

        return false;
    }

    public boolean shouldBypassIPWhitelist(Player player) {
        return isBot(player) && ConfigUtils.getBoolean("bot-bypass.bypass-ip-whitelist", true);
    }

    public boolean shouldBypassPingKick(Player player) {
        return isBot(player) && ConfigUtils.getBoolean("bot-bypass.bypass-ping-kick", true);
    }

    public boolean shouldBypassResourceLimits(Player player) {
        return isBot(player) && ConfigUtils.getBoolean("bot-bypass.bypass-resource-limits", false);
    }

    public void addBot(UUID uuid) {
        botUUIDs.add(uuid);
        saveBotConfig();
    }

    public void addBotName(String name) {
        botNames.add(name);
        saveBotConfig();
    }

    public void removeBot(UUID uuid) {
        botUUIDs.remove(uuid);
        saveBotConfig();
    }

    public void removeBotName(String name) {
        botNames.remove(name);
        saveBotConfig();
    }

    private void saveBotConfig() {
        ConfigUtils.getConfig().set("bot-bypass.bot-uuids", botUUIDs.stream().map(UUID::toString).toList());
        ConfigUtils.getConfig().set("bot-bypass.bot-names", new ArrayList<>(botNames));
        ConfigUtils.saveConfig();
    }

    public Set<UUID> getBotUUIDs() {
        return new HashSet<>(botUUIDs);
    }

    public Set<String> getBotNames() {
        return new HashSet<>(botNames);
    }

    public int getBotCount() {
        return botUUIDs.size();
    }
}