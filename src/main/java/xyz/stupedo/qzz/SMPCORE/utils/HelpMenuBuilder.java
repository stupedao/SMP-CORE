package xyz.stupedo.qzz.SMPCORE.utils;

import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.hover.content.Text;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import xyz.stupedo.qzz.SMPCORE.SMPCORE;

import java.util.ArrayList;
import java.util.List;

public class HelpMenuBuilder {

    private final String headerColor;
    private final String commandColor;
    private final String descriptionColor;
    private final boolean clickable;
    private final List<HelpEntry> cachedEntries;
    private final String header;

    public HelpMenuBuilder() {
        this.headerColor = MessageUtils.color(ConfigUtils.getString("help-menu.header-color", "&6"));
        this.commandColor = MessageUtils.color(ConfigUtils.getString("help-menu.command-color", "&a"));
        this.descriptionColor = MessageUtils.color(ConfigUtils.getString("help-menu.description-color", "&7"));
        this.clickable = ConfigUtils.getBoolean("help-menu.clickable-commands", true);

        String version = "1.1.0";
        try {
            if (SMPCORE.getInstance() != null && SMPCORE.getInstance().getDescription() != null) {
                version = SMPCORE.getInstance().getDescription().getVersion();
            }
        } catch (Exception e) {
        }
        this.header = headerColor + "=== SMPCORE v" + version + " Help Menu ===";

        this.cachedEntries = buildHelpEntries();
    }

    private List<HelpEntry> buildHelpEntries() {
        List<HelpEntry> entries = new ArrayList<>();

        entries.add(new HelpEntry("/smp help", "Show this help menu", "smpcore.use"));
        entries.add(new HelpEntry("/smp addip <player>", "Add player to IP whitelist", "smpcore.admin"));
        entries.add(new HelpEntry("/smp removeip <player>", "Remove player from IP whitelist", "smpcore.admin"));
        entries.add(new HelpEntry("/smp bypassip add <player>", "Add player to IP bypass list", "smpcore.admin"));
        entries.add(new HelpEntry("/smp bypassip remove <player>", "Remove player from IP bypass list", "smpcore.admin"));
        entries.add(new HelpEntry("/smp bypassip list", "List all bypassed players", "smpcore.admin"));
        entries.add(new HelpEntry("/smp listip", "List all whitelisted IPs", "smpcore.admin"));
        entries.add(new HelpEntry("/smp autoip status", "Show auto-IP status", "smpcore.admin"));
        entries.add(new HelpEntry("/smp autoip enable", "Enable auto-IP registration", "smpcore.admin"));
        entries.add(new HelpEntry("/smp autoip disable", "Disable auto-IP registration", "smpcore.admin"));
        entries.add(new HelpEntry("/smp autoip reset", "Reset auto-IP for player", "smpcore.admin"));
        entries.add(new HelpEntry("/smp togglejoin [join|quit|both|show]", "Toggle join/quit messages", "smpcore.admin"));
        entries.add(new HelpEntry("/smp reload", "Reload plugin configuration", "smpcore.admin"));
        entries.add(new HelpEntry("/smp optimize", "Manual optimization trigger", "smpcore.admin"));
        entries.add(new HelpEntry("/smp optimize status", "Show optimization status", "smpcore.admin"));
        entries.add(new HelpEntry("/smp optimize tps", "Show current TPS", "smpcore.admin"));
        entries.add(new HelpEntry("/smp optimize entities", "Show entity counts", "smpcore.admin"));
        entries.add(new HelpEntry("/smp resources", "Show current RAM and CPU usage", "smpcore.admin"));
        entries.add(new HelpEntry("/smp resources history", "Show resource usage history", "smpcore.admin"));
        entries.add(new HelpEntry("/smp resources gc", "Force garbage collection", "smpcore.admin"));
        entries.add(new HelpEntry("/smp resources cleanup", "Force memory cleanup", "smpcore.admin"));
        entries.add(new HelpEntry("/smp resources stats", "Show detailed resource statistics", "smpcore.admin"));
        entries.add(new HelpEntry("/spark tps", "Show current TPS", "smpcore.spark"));
        entries.add(new HelpEntry("/spark gc", "Run garbage collection", "smpcore.spark"));
        entries.add(new HelpEntry("/spark enable", "Enable spark profiler", "smpcore.spark"));
        entries.add(new HelpEntry("/spark disable", "Disable spark profiler", "smpcore.spark"));
        entries.add(new HelpEntry("/ping", "Show your current ping", "smpcore.ping"));
        entries.add(new HelpEntry("/ping <player>", "Show player's ping", "smpcore.ping"));
        entries.add(new HelpEntry("/ping top", "Show highest ping players", "smpcore.ping"));
        entries.add(new HelpEntry("/ping stats", "Show ping statistics", "smpcore.ping"));

        return entries;
    }

    public void sendHelpMenu(CommandSender sender) {
        sender.sendMessage("");
        sender.sendMessage(header);
        sender.sendMessage("");

        for (HelpEntry entry : cachedEntries) {
            if (sender.hasPermission(entry.permission)) {
                if (clickable && sender instanceof Player) {
                    sendClickableEntry((Player) sender, entry);
                } else {
                    sendPlainEntry(sender, entry);
                }
            }
        }

        sender.sendMessage("");
        sender.sendMessage(headerColor + "========================");
        sender.sendMessage("");
    }

    private void sendClickableEntry(Player player, HelpEntry entry) {
        TextComponent message = new TextComponent(commandColor + entry.command + " ");
        message.setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, entry.command));
        message.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text(descriptionColor + entry.description)));

        TextComponent description = new TextComponent(descriptionColor + "- " + entry.description);

        player.spigot().sendMessage(message);
        player.spigot().sendMessage(description);
    }

    private void sendPlainEntry(CommandSender sender, HelpEntry entry) {
        sender.sendMessage(commandColor + entry.command + " " + descriptionColor + "- " + entry.description);
    }

    private static class HelpEntry {
        final String command;
        final String description;
        final String permission;

        HelpEntry(String command, String description, String permission) {
            this.command = command;
            this.description = description;
            this.permission = permission;
        }
    }
}