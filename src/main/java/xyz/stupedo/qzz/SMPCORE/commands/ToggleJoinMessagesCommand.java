package xyz.stupedo.qzz.SMPCORE.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import xyz.stupedo.qzz.SMPCORE.SMPCORE;
import xyz.stupedo.qzz.SMPCORE.utils.ConfigUtils;
import xyz.stupedo.qzz.SMPCORE.utils.MessageUtils;

public class ToggleJoinMessagesCommand implements CommandExecutor {

    private final SMPCORE plugin;

    public ToggleJoinMessagesCommand(SMPCORE plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("smpcore.admin")) {
            MessageUtils.sendPrefixMessage(sender, "You don't have permission to use this command.");
            return true;
        }

        if (args.length == 0) {
            boolean currentState = ConfigUtils.getBoolean("join-quit-messages.hide-join-messages", false);
            ConfigUtils.getConfig().set("join-quit-messages.hide-join-messages", !currentState);
            ConfigUtils.saveConfig();

            if (!currentState) {
                MessageUtils.sendPrefixMessage(sender, "Join messages are now hidden.");
                plugin.getLogger().info("Join messages hidden by " + sender.getName());
            } else {
                MessageUtils.sendPrefixMessage(sender, "Join messages are now shown.");
                plugin.getLogger().info("Join messages shown by " + sender.getName());
            }
            return true;
        }

        String action = args[0].toLowerCase();

        switch (action) {
            case "join":
                boolean hideJoin = ConfigUtils.getBoolean("join-quit-messages.hide-join-messages", false);
                ConfigUtils.getConfig().set("join-quit-messages.hide-join-messages", !hideJoin);
                ConfigUtils.saveConfig();
                MessageUtils.sendPrefixMessage(sender, "Join messages are now " + (!hideJoin ? "hidden" : "shown") + ".");
                break;
            case "quit":
                boolean hideQuit = ConfigUtils.getBoolean("join-quit-messages.hide-quit-messages", false);
                ConfigUtils.getConfig().set("join-quit-messages.hide-quit-messages", !hideQuit);
                ConfigUtils.saveConfig();
                MessageUtils.sendPrefixMessage(sender, "Quit messages are now " + (!hideQuit ? "hidden" : "shown") + ".");
                break;
            case "both":
                ConfigUtils.getConfig().set("join-quit-messages.hide-join-messages", true);
                ConfigUtils.getConfig().set("join-quit-messages.hide-quit-messages", true);
                ConfigUtils.saveConfig();
                MessageUtils.sendPrefixMessage(sender, "Both join and quit messages are now hidden.");
                break;
            case "show":
                ConfigUtils.getConfig().set("join-quit-messages.hide-join-messages", false);
                ConfigUtils.getConfig().set("join-quit-messages.hide-quit-messages", false);
                ConfigUtils.saveConfig();
                MessageUtils.sendPrefixMessage(sender, "Both join and quit messages are now shown.");
                break;
            default:
                MessageUtils.sendPrefixMessage(sender, "Usage: /smp togglejoin [join|quit|both|show]");
                break;
        }

        return true;
    }
}