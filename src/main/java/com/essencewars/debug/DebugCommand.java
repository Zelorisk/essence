package com.essencewars.debug;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.essencewars.EssenceWarsPlugin;

public class DebugCommand implements CommandExecutor {

    private final EssenceWarsPlugin plugin;
    private final AdvancedDebugLogger logger;

    public DebugCommand(EssenceWarsPlugin plugin, AdvancedDebugLogger logger) {
        this.plugin = plugin;
        this.logger = logger;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!sender.hasPermission("essencewars.debug") && !sender.hasPermission("essencewars.admin")) {
            sender.sendMessage("§cYou don't have permission!");
            return true;
        }

        if (args.length == 0) {
            sendHelp(sender);
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "level":
                if (args.length < 2) {
                    sender.sendMessage("§cUsage: /debug level <TRACE|DEBUG|INFO|WARN|ERROR|CRITICAL>");
                    return true;
                }
                try {
                    AdvancedDebugLogger.LogLevel level = AdvancedDebugLogger.LogLevel.valueOf(args[1].toUpperCase());
                    logger.setLogLevel(level);
                    sender.sendMessage("§aLog level set to: " + level.name());
                } catch (IllegalArgumentException e) {
                    sender.sendMessage("§cInvalid log level!");
                }
                break;
            case "track":
                if (args.length < 2) {
                    sender.sendMessage("§cUsage: /debug track <player>");
                    return true;
                }
                Player target = Bukkit.getPlayer(args[1]);
                if (target == null) {
                    sender.sendMessage("§cPlayer not found!");
                    return true;
                }
                logger.enablePlayerDebug(target);
                sender.sendMessage("§aNow tracking: " + target.getName());
                break;
            case "untrack":
                if (args.length < 2) {
                    sender.sendMessage("§cUsage: /debug untrack <player>");
                    return true;
                }
                target = Bukkit.getPlayer(args[1]);
                if (target == null) {
                    sender.sendMessage("§cPlayer not found!");
                    return true;
                }
                logger.disablePlayerDebug(target);
                sender.sendMessage("§aStopped tracking: " + target.getName());
                break;
            case "logs":
                if (args.length < 2) {
                    sender.sendMessage("§cUsage: /debug logs <player>");
                    return true;
                }
                target = Bukkit.getPlayer(args[1]);
                if (target == null) {
                    sender.sendMessage("§cPlayer not found!");
                    return true;
                }
                var logs = logger.getPlayerLogs(target);
                sender.sendMessage("§e=== Logs for " + target.getName() + " ===");
                logs.forEach(log -> sender.sendMessage("§7" + log));
                break;
            case "stats":
                logger.logStatistics();
                sender.sendMessage("§aStatistics logged to console and file!");
                break;
            case "clear":
                logger.clearFile();
                sender.sendMessage("§aDebug file cleared!");
                break;
            case "file":
                sender.sendMessage("§eDebug file: §f" + logger.getDebugFile().getAbsolutePath());
                break;
            case "test":
                if (sender instanceof Player) {
                    Player self = (Player) sender;
                    logger.logPlayer(self, AdvancedDebugLogger.LogLevel.INFO, "Test log message");
                    sender.sendMessage("§aTest log created!");
                }
                break;
            case "config":
                Map<String, Object> values = new HashMap<>();
                values.put("max-energy", plugin.getConfig().getInt("max-energy"));
                values.put("starting-energy", plugin.getConfig().getInt("starting-energy"));
                logger.logConfigLoad(values);
                sender.sendMessage("§aConfig dump written to debug log.");
                break;
            default:
                sendHelp(sender);
                break;
        }

        return true;
    }

    private void sendHelp(CommandSender sender) {
        sender.sendMessage("§e=== Debug Commands ===");
        sender.sendMessage("§6/debug level <level> §7- Set log level");
        sender.sendMessage("§6/debug track <player> §7- Track player actions");
        sender.sendMessage("§6/debug untrack <player> §7- Stop tracking player");
        sender.sendMessage("§6/debug logs <player> §7- View player logs");
        sender.sendMessage("§6/debug stats §7- Show event statistics");
        sender.sendMessage("§6/debug clear §7- Clear debug file");
        sender.sendMessage("§6/debug file §7- Get debug file path");
        sender.sendMessage("§6/debug test §7- Create test log entry");
        sender.sendMessage("§6/debug config §7- Log key config values");
    }
}
