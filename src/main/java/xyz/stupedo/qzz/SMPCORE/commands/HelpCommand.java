package xyz.stupedo.qzz.SMPCORE.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import xyz.stupedo.qzz.SMPCORE.SMPCORE;
import xyz.stupedo.qzz.SMPCORE.utils.ConfigUtils;
import xyz.stupedo.qzz.SMPCORE.utils.HelpMenuBuilder;
import xyz.stupedo.qzz.SMPCORE.utils.MessageUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class HelpCommand implements CommandExecutor, TabCompleter {

    private final SMPCORE plugin;
    private final HelpMenuBuilder helpMenuBuilder;
    private final AddIPCommand addipCmd;
    private final RemoveIPCommand removeipCmd;
    private final BypassIPCommand bypassipCmd;
    private final ListIPCommand listipCmd;
    private final ToggleJoinMessagesCommand togglejoinCmd;
    private final AutoIPCommand autoipCmd;
    private final ReloadCommand reloadCmd;
    private final OptimizeCommand optimizeCmd;
    private final ResourceCommand resourceCmd;

    public HelpCommand(SMPCORE plugin, AddIPCommand addipCmd, RemoveIPCommand removeipCmd, BypassIPCommand bypassipCmd,
                       ListIPCommand listipCmd, ToggleJoinMessagesCommand togglejoinCmd, AutoIPCommand autoipCmd,
                       ReloadCommand reloadCmd, OptimizeCommand optimizeCmd, ResourceCommand resourceCmd) {
        this.plugin = plugin;
        this.helpMenuBuilder = new HelpMenuBuilder();
        this.addipCmd = addipCmd;
        this.removeipCmd = removeipCmd;
        this.bypassipCmd = bypassipCmd;
        this.listipCmd = listipCmd;
        this.togglejoinCmd = togglejoinCmd;
        this.autoipCmd = autoipCmd;
        this.reloadCmd = reloadCmd;
        this.optimizeCmd = optimizeCmd;
        this.resourceCmd = resourceCmd;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!ConfigUtils.getBoolean("help-menu.enabled", true)) {
            MessageUtils.sendPrefixMessage(sender, "Help menu is disabled.");
            return true;
        }

        if (args.length == 0) {
            if (ConfigUtils.getBoolean("help-menu.show-on-no-args", true)) {
                helpMenuBuilder.sendHelpMenu(sender);
            } else {
                MessageUtils.sendPrefixMessage(sender, "Use /smp help for commands.");
            }
            return true;
        }

        String subCommand = args[0].toLowerCase();

        switch (subCommand) {
            case "help":
                helpMenuBuilder.sendHelpMenu(sender);
                break;
            case "addip":
                addipCmd.onCommand(sender, command, label, Arrays.copyOfRange(args, 1, args.length));
                break;
            case "removeip":
                removeipCmd.onCommand(sender, command, label, Arrays.copyOfRange(args, 1, args.length));
                break;
            case "bypassip":
                bypassipCmd.onCommand(sender, command, label, Arrays.copyOfRange(args, 1, args.length));
                break;
            case "listip":
                listipCmd.onCommand(sender, command, label, Arrays.copyOfRange(args, 1, args.length));
                break;
            case "togglejoin":
                togglejoinCmd.onCommand(sender, command, label, Arrays.copyOfRange(args, 1, args.length));
                break;
            case "autoip":
                autoipCmd.onCommand(sender, command, label, Arrays.copyOfRange(args, 1, args.length));
                break;
            case "reload":
                reloadCmd.onCommand(sender, command, label, Arrays.copyOfRange(args, 1, args.length));
                break;
            case "optimize":
                optimizeCmd.onCommand(sender, command, label, Arrays.copyOfRange(args, 1, args.length));
                break;
            case "resources":
                resourceCmd.onCommand(sender, command, label, Arrays.copyOfRange(args, 1, args.length));
                break;
            default:
                MessageUtils.sendPrefixMessage(sender, "Unknown command. Use /smp help for available commands.");
                break;
        }

        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();

        if (args.length == 1) {
            completions.add("help");
            completions.add("addip");
            completions.add("removeip");
            completions.add("bypassip");
            completions.add("listip");
            completions.add("autoip");
            completions.add("togglejoin");
            completions.add("reload");
            completions.add("optimize");
            completions.add("resources");
        } else if (args.length == 2) {
            String subCommand = args[0].toLowerCase();
            if (subCommand.equals("addip") || subCommand.equals("removeip")) {
                for (org.bukkit.entity.Player player : plugin.getServer().getOnlinePlayers()) {
                    completions.add(player.getName());
                }
            } else if (subCommand.equals("bypassip")) {
                completions.add("add");
                completions.add("remove");
                completions.add("list");
            } else if (subCommand.equals("autoip")) {
                completions.add("status");
                completions.add("enable");
                completions.add("disable");
                completions.add("reset");
            } else if (subCommand.equals("togglejoin")) {
                completions.add("join");
                completions.add("quit");
                completions.add("both");
                completions.add("show");
            } else if (subCommand.equals("optimize")) {
                completions.add("status");
                completions.add("tps");
                completions.add("entities");
            } else if (subCommand.equals("resources")) {
                completions.add("history");
                completions.add("gc");
                completions.add("cleanup");
                completions.add("stats");
            }
        }

        return completions;
    }
}