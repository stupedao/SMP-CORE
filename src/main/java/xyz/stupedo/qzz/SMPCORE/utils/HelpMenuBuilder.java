package xyz.stupedo.qzz.SMPCORE.utils;

import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.hover.content.Text;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class HelpMenuBuilder {

    private final String headerColor;
    private final String commandColor;
    private final String descriptionColor;
    private final boolean clickable;

    public HelpMenuBuilder() {
        this.headerColor = MessageUtils.color(ConfigUtils.getString("help-menu.header-color", "&6"));
        this.commandColor = MessageUtils.color(ConfigUtils.getString("help-menu.command-color", "&a"));
        this.descriptionColor = MessageUtils.color(ConfigUtils.getString("help-menu.description-color", "&7"));
        this.clickable = ConfigUtils.getBoolean("help-menu.clickable-commands", true);
    }

    public void sendHelpMenu(CommandSender sender) {
        sender.sendMessage("");
        sender.sendMessage(headerColor + "=== SMPCORE Help Menu ===");
        sender.sendMessage("");

        List<HelpEntry> entries = getHelpEntries();

        for (HelpEntry entry : entries) {
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

    private List<HelpEntry> getHelpEntries() {
        List<HelpEntry> entries = new ArrayList<>();

        entries.add(new HelpEntry("/smp help", "Show this help menu", "smpcore.use"));
        entries.add(new HelpEntry("/smp addip <player>", "Add player to IP whitelist", "smpcore.admin"));
        entries.add(new HelpEntry("/smp removeip <player>", "Remove player from IP whitelist", "smpcore.admin"));
        entries.add(new HelpEntry("/smp bypassip <add|remove|list> [player]", "Manage IP whitelist bypass", "smpcore.admin"));
        entries.add(new HelpEntry("/smp listip", "List all whitelisted IPs", "smpcore.admin"));
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
        entries.add(new HelpEntry("/ping", "Show your current ping", "smpcore.ping"));
        entries.add(new HelpEntry("/ping <player>", "Show player's ping", "smpcore.ping"));
        entries.add(new HelpEntry("/ping top", "Show highest ping players", "smpcore.ping"));
        entries.add(new HelpEntry("/ping stats", "Show ping statistics", "smpcore.ping"));

        return entries;
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