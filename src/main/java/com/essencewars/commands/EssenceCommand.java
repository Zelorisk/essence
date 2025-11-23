package com.essencewars.commands;

import com.essencewars.EssenceWarsPlugin;
import com.essencewars.energy.PlayerDataManager;
import com.essencewars.energy.PlayerEssenceData;
import com.essencewars.essence.Essence;
import com.essencewars.essence.EssenceTier;
import com.essencewars.essence.EssenceType;
import com.essencewars.essence.impl.ArcaneEssence;
import com.essencewars.items.EnergyCrystalItem;
import com.essencewars.team.Team;
import com.essencewars.team.TeamManager;
import com.essencewars.team.TeamHome;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class EssenceCommand implements CommandExecutor, TabCompleter {

    private final EssenceWarsPlugin plugin;
    private final PlayerDataManager dataManager;
    private final TeamManager teamManager;

    public EssenceCommand(EssenceWarsPlugin plugin) {
        this.plugin = plugin;
        this.dataManager = plugin.getPlayerDataManager();
        this.teamManager = plugin.getTeamManager();
    }

    @Override
    public boolean onCommand(
        CommandSender sender,
        Command command,
        String label,
        String[] args
    ) {
        if (args.length == 0) {
            sender.sendMessage(
                ChatColor.LIGHT_PURPLE +
                    "EssenceWars " +
                    ChatColor.GRAY +
                    "commands:"
            );
            sender.sendMessage(
                ChatColor.GRAY + "/essence give <player> <type>"
            );
            sender.sendMessage(ChatColor.GRAY + "/essence upgrade <player>");
            sender.sendMessage(
                ChatColor.GRAY +
                    "/essence energy <player> <set|add|remove> <amount>"
            );
            sender.sendMessage(ChatColor.GRAY + "/essence reset <player>");
            sender.sendMessage(
                ChatColor.GRAY +
                    "/essence info " +
                    ChatColor.DARK_GRAY +
                    "- View essence crafting recipes"
            );
            sender.sendMessage(
                ChatColor.GRAY +
                    "/essence stats [player] " +
                    ChatColor.DARK_GRAY +
                    "- View player statistics"
            );
            sender.sendMessage(ChatColor.GRAY + "/essence withdraw");
            sender.sendMessage(ChatColor.GRAY + "/essence crystal [amount]");
            sender.sendMessage(
                ChatColor.GRAY +
                    "/essence craft <type> " +
                    ChatColor.DARK_GRAY +
                    "- Get an essence craft item"
            );
            sender.sendMessage(
                ChatColor.GRAY +
                    "/essence primary " +
                    ChatColor.DARK_GRAY +
                    "- Cast your primary ability"
            );
            sender.sendMessage(
                ChatColor.GRAY +
                    "/essence secondary " +
                    ChatColor.DARK_GRAY +
                    "- Cast your secondary ability"
            );
            sender.sendMessage(
                ChatColor.GRAY +
                    "/essence hotkey <primary|secondary> " +
                    ChatColor.DARK_GRAY +
                    "- Cast via hotkey"
            );
            sender.sendMessage(
                ChatColor.GRAY +
                    "/essence config " +
                    ChatColor.DARK_GRAY +
                    "- Open settings GUI"
            );
            sender.sendMessage(
                ChatColor.GRAY +
                    "/essence tutorial " +
                    ChatColor.DARK_GRAY +
                    "- Open the tutorial guide"
            );
            sender.sendMessage(
                ChatColor.GRAY +
                    "/essence team <create|disband|join|leave|info|list|home> ..."
            );
            return true;
        }
        String sub = args[0].toLowerCase();
        switch (sub) {
            case "give":
                return handleGive(sender, args);
            case "upgrade":
                return handleUpgrade(sender, args);
            case "energy":
                return handleEnergy(sender, args);
            case "reset":
                return handleReset(sender, args);
            case "info":
                return handleInfo(sender);
            case "tutorial":
                return handleTutorial(sender);
            case "stats":
                return handleStats(sender, args);
            case "withdraw":
                return handleWithdraw(sender);
            case "crystal":
                return handleCrystal(sender, args);
            case "craft":
                return handleCraft(sender, args);
            case "config":
                return handleConfig(sender);
            case "adminconfig":
                return handleAdminConfig(sender);
            case "team":
                return handleTeam(sender, args);
            case "debug":
                return handleDebug(sender, args);
            case "primary":
                return handleCastPrimary(sender);
            case "secondary":
                return handleCastSecondary(sender);
            case "hotkey":
                return handleHotkey(sender, args);
            default:
                sender.sendMessage(ChatColor.RED + "Unknown subcommand.");
                return true;
        }
    }

    private boolean handleGive(CommandSender sender, String[] args) {
        if (!sender.hasPermission("essencewars.admin")) {
            sender.sendMessage(ChatColor.RED + "No permission.");
            return true;
        }
        if (args.length < 3) {
            sender.sendMessage(
                ChatColor.RED + "Usage: /essence give <player> <type>"
            );
            return true;
        }
        Player target = Bukkit.getPlayerExact(args[1]);
        if (target == null) {
            sender.sendMessage(ChatColor.RED + "Player not found.");
            return true;
        }
        EssenceType type = EssenceType.fromString(args[2]);
        if (type == null) {
            sender.sendMessage(ChatColor.RED + "Unknown essence type.");
            return true;
        }
        PlayerEssenceData data = dataManager.getOrCreate(target);
        data.setEssenceType(type);
        sender.sendMessage(
            ChatColor.GREEN +
                "Set " +
                target.getName() +
                "'s essence to " +
                type.getDisplayName() +
                "."
        );
        target.sendMessage(
            ChatColor.LIGHT_PURPLE +
                "Your essence is now " +
                type.getDisplayName() +
                "."
        );
        plugin.getScoreboardManager().updateFor(target);
        return true;
    }

    private boolean handleUpgrade(CommandSender sender, String[] args) {
        if (!sender.hasPermission("essencewars.admin")) {
            sender.sendMessage(ChatColor.RED + "No permission.");
            return true;
        }
        if (args.length < 2) {
            sender.sendMessage(
                ChatColor.RED + "Usage: /essence upgrade <player>"
            );
            return true;
        }
        Player target = Bukkit.getPlayerExact(args[1]);
        if (target == null) {
            sender.sendMessage(ChatColor.RED + "Player not found.");
            return true;
        }
        PlayerEssenceData data = dataManager.getOrCreate(target);
        data.setTier(EssenceTier.TIER2);
        sender.sendMessage(
            ChatColor.GREEN + "Upgraded " + target.getName() + " to Tier II."
        );
        target.sendMessage(
            ChatColor.LIGHT_PURPLE +
                "Your essence has been upgraded to Tier II."
        );
        plugin.getScoreboardManager().updateFor(target);
        return true;
    }

    private boolean handleEnergy(CommandSender sender, String[] args) {
        if (!sender.hasPermission("essencewars.admin")) {
            sender.sendMessage(ChatColor.RED + "No permission.");
            return true;
        }
        if (args.length < 4) {
            sender.sendMessage(
                ChatColor.RED +
                    "Usage: /essence energy <player> <set|add|remove> <amount>"
            );
            return true;
        }
        Player target = Bukkit.getPlayerExact(args[1]);
        if (target == null) {
            sender.sendMessage(ChatColor.RED + "Player not found.");
            return true;
        }
        String op = args[2].toLowerCase();
        int amount;
        try {
            amount = Integer.parseInt(args[3]);
        } catch (NumberFormatException e) {
            sender.sendMessage(ChatColor.RED + "Amount must be a number.");
            return true;
        }
        PlayerEssenceData data = dataManager.getOrCreate(target);
        int maxEnergy = plugin.getConfig().getInt("max-energy", 10);
        switch (op) {
            case "set":
                data.setEnergy(Math.min(Math.max(0, amount), maxEnergy));
                break;
            case "add":
                data.addEnergy(amount, maxEnergy);
                break;
            case "remove":
                data.addEnergy(-amount, maxEnergy);
                break;
            default:
                sender.sendMessage(
                    ChatColor.RED + "Unknown operation. Use set/add/remove."
                );
                return true;
        }
        sender.sendMessage(
            ChatColor.GREEN +
                "Energy for " +
                target.getName() +
                " is now " +
                data.getEnergy() +
                "."
        );
        plugin.getScoreboardManager().updateFor(target);
        return true;
    }

    private boolean handleReset(CommandSender sender, String[] args) {
        if (!sender.hasPermission("essencewars.admin")) {
            sender.sendMessage(ChatColor.RED + "No permission.");
            return true;
        }
        if (args.length < 2) {
            sender.sendMessage(
                ChatColor.RED + "Usage: /essence reset <player>"
            );
            return true;
        }
        Player target = Bukkit.getPlayerExact(args[1]);
        if (target == null) {
            sender.sendMessage(ChatColor.RED + "Player not found.");
            return true;
        }
        PlayerEssenceData data = dataManager.getOrCreate(target);
        int startEnergy = plugin.getConfig().getInt("starting-energy", 5);
        data.setEssenceType(null);
        data.setTier(EssenceTier.TIER1);
        data.setEnergy(startEnergy);
        dataManager.save(data);
        sender.sendMessage(
            ChatColor.GREEN +
                "Reset " +
                target.getName() +
                "'s essence and energy."
        );
        target.sendMessage(
            ChatColor.LIGHT_PURPLE + "Your essence data has been reset."
        );
        plugin.getScoreboardManager().updateFor(target);
        return true;
    }

    private boolean handleInfo(CommandSender sender) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Players only.");
            return true;
        }

        // Open crafting guide GUI
        plugin.getCraftingGuideGUI().open(player);
        player.sendMessage(
            ChatColor.LIGHT_PURPLE + "Opening Essence Crafting Guide..."
        );
        return true;
    }

    private boolean handleTutorial(CommandSender sender) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Players only.");
            return true;
        }

        // Open the tutorial
        plugin.getTutorialGUI().open(player, 0);
        player.sendMessage(
            ChatColor.LIGHT_PURPLE + "Opening EssenceWars tutorial..."
        );
        return true;
    }

    private boolean handleStats(CommandSender sender, String[] args) {
        if (args.length < 2) {
            if (!(sender instanceof Player)) {
                sender.sendMessage(
                    ChatColor.RED + "Usage: /essence stats <player>"
                );
                return true;
            }
            Player self = (Player) sender;
            return showStats(sender, self);
        }
        Player target = Bukkit.getPlayerExact(args[1]);
        if (target == null) {
            sender.sendMessage(ChatColor.RED + "Player not found.");
            return true;
        }
        return showStats(sender, target);
    }

    private boolean showStats(CommandSender viewer, Player target) {
        PlayerEssenceData data = dataManager.getOrCreate(target);
        viewer.sendMessage(
            ChatColor.LIGHT_PURPLE + "Stats for " + target.getName() + ":"
        );
        viewer.sendMessage(
            ChatColor.GRAY +
                " Essence: " +
                (data.getEssenceType() == null
                    ? "None"
                    : data.getEssenceType().getDisplayName())
        );
        viewer.sendMessage(
            ChatColor.GRAY +
                " Tier: " +
                (data.getTier() == null ? "-" : data.getTier().getDisplayName())
        );
        viewer.sendMessage(
            ChatColor.GRAY + " Energy: " + data.getEnergy() + "/10"
        );
        viewer.sendMessage(
            ChatColor.GRAY + " State: " + data.getEnergyState().getDisplayName()
        );
        return true;
    }

    private boolean handleWithdraw(CommandSender sender) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Players only.");
            return true;
        }
        PlayerEssenceData data = dataManager.getOrCreate(player);
        if (EnergyCrystalItem.hasCrystal(player)) {
            player.sendMessage(
                ChatColor.RED + "You already have an energy crystal."
            );
            return true;
        }
        int value = plugin.getConfig().getInt("crystal-energy-value", 1);
        if (data.getEnergy() < value) {
            player.sendMessage(
                ChatColor.RED + "Not enough energy to withdraw."
            );
            return true;
        }
        int maxEnergy = plugin.getConfig().getInt("max-energy", 10);
        data.addEnergy(-value, maxEnergy);
        player
            .getWorld()
            .dropItemNaturally(
                player.getLocation(),
                EnergyCrystalItem.create(value)
            );
        player.sendMessage(
            ChatColor.LIGHT_PURPLE +
                "Withdrew " +
                value +
                " energy as a crystal."
        );
        plugin.getScoreboardManager().updateFor(player);
        return true;
    }

    private boolean handleConfig(CommandSender sender) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Players only.");
            return true;
        }
        plugin.getConfigGUI().open(player);
        return true;
    }

    private boolean handleDebug(CommandSender sender, String[] args) {
        if (
            !sender.hasPermission("essencewars.debug") &&
            !sender.hasPermission("essencewars.admin")
        ) {
            sender.sendMessage(ChatColor.RED + "No permission.");
            return true;
        }

        if (args.length >= 2 && args[1].equalsIgnoreCase("help")) {
            sender.sendMessage(ChatColor.LIGHT_PURPLE + "Essence debug usage:");
            sender.sendMessage(
                ChatColor.GRAY +
                    "/essence debug state [player]" +
                    ChatColor.DARK_GRAY +
                    " - core essence/energy info"
            );
            return true;
        }

        String mode = args.length >= 2 ? args[1].toLowerCase() : "state";
        String targetName;
        if (args.length >= 3) {
            targetName = args[2];
        } else if (sender instanceof Player) {
            targetName = ((Player) sender).getName();
        } else {
            sender.sendMessage(
                ChatColor.RED + "Usage: /essence debug " + mode + " <player>"
            );
            return true;
        }

        Player target = Bukkit.getPlayerExact(targetName);
        if (target == null) {
            sender.sendMessage(ChatColor.RED + "Player not found.");
            return true;
        }

        return debugState(sender, target);
    }

    private boolean debugState(CommandSender viewer, Player target) {
        PlayerEssenceData data = dataManager.getOrCreate(target);

        viewer.sendMessage(
            ChatColor.DARK_PURPLE +
                "[EW-DEBUG] " +
                ChatColor.GRAY +
                "player=" +
                target.getName() +
                " essence=" +
                (data.getEssenceType() == null
                    ? "NONE"
                    : data.getEssenceType().name()) +
                " tier=" +
                (data.getTier() == null ? "-" : data.getTier().name()) +
                " energy=" +
                data.getEnergy() +
                " state=" +
                data.getEnergyState().name()
        );

        org.bukkit.Location loc = target.getLocation();
        viewer.sendMessage(
            ChatColor.DARK_PURPLE +
                "[EW-DEBUG] " +
                ChatColor.GRAY +
                "world=" +
                loc.getWorld().getName() +
                " loc=" +
                loc.getBlockX() +
                "," +
                loc.getBlockY() +
                "," +
                loc.getBlockZ()
        );

        return true;
    }

    private boolean handleHotkey(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("Players only.");
            return true;
        }
        if (args.length < 2) {
            sender.sendMessage(
                ChatColor.RED + "Usage: /essence hotkey <primary|secondary>"
            );
            return true;
        }
        String which = args[1].toLowerCase();
        switch (which) {
            case "primary":
                return handleCastPrimary(sender);
            case "secondary":
                return handleCastSecondary(sender);
            default:
                sender.sendMessage(
                    ChatColor.RED + "Usage: /essence hotkey <primary|secondary>"
                );
                return true;
        }
    }

    private boolean handleCastPrimary(CommandSender sender) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Players only.");
            return true;
        }
        PlayerEssenceData data = dataManager.getOrCreate(player);
        if (ArcaneEssence.isSilenced(player.getUniqueId())) {
            player.sendMessage(
                "§5[Arcane] §dYour essence is currently silenced and cannot be used."
            );
            return true;
        }
        EssenceType type = data.getEssenceType();
        if (type == null) {
            player.sendMessage(
                ChatColor.RED + "You do not have an essence selected."
            );
            return true;
        }
        Essence essence = plugin.getEssenceRegistry().get(type);
        if (essence == null) {
            player.sendMessage(
                ChatColor.RED + "Your essence is not available."
            );
            return true;
        }
        essence.usePrimary(player, data);
        plugin.getScoreboardManager().updateFor(player);
        return true;
    }

    private boolean handleCastSecondary(CommandSender sender) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Players only.");
            return true;
        }
        PlayerEssenceData data = dataManager.getOrCreate(player);
        if (ArcaneEssence.isSilenced(player.getUniqueId())) {
            player.sendMessage(
                "§5[Arcane] §dYour essence is currently silenced and cannot be used."
            );
            return true;
        }
        EssenceType type = data.getEssenceType();
        if (type == null) {
            player.sendMessage(
                ChatColor.RED + "You do not have an essence selected."
            );
            return true;
        }
        Essence essence = plugin.getEssenceRegistry().get(type);
        if (essence == null) {
            player.sendMessage(
                ChatColor.RED + "Your essence is not available."
            );
            return true;
        }
        essence.useSecondary(player, data);
        plugin.getScoreboardManager().updateFor(player);
        return true;
    }

    private boolean handleCrystal(CommandSender sender, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Players only.");
            return true;
        }
        if (!sender.hasPermission("essencewars.admin")) {
            sender.sendMessage(ChatColor.RED + "No permission.");
            return true;
        }
        if (EnergyCrystalItem.hasCrystal(player)) {
            player.sendMessage(
                ChatColor.RED + "You already have an energy crystal."
            );
            return true;
        }
        int amount = 1;
        if (args.length >= 2) {
            try {
                amount = Math.max(1, Integer.parseInt(args[1]));
            } catch (NumberFormatException ignored) {}
        }
        int value = plugin.getConfig().getInt("crystal-energy-value", 1);
        for (int i = 0; i < amount; i++) {
            if (EnergyCrystalItem.hasCrystal(player)) {
                break;
            }
            player.getInventory().addItem(EnergyCrystalItem.create(value));
        }
        sender.sendMessage(
            ChatColor.GREEN + "Gave crystal(s) to " + player.getName() + "."
        );
        return true;
    }

    private boolean handleCraft(CommandSender sender, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Players only.");
            return true;
        }
        if (!sender.hasPermission("essencewars.admin")) {
            sender.sendMessage(ChatColor.RED + "No permission.");
            return true;
        }
        if (args.length < 2) {
            sender.sendMessage(
                ChatColor.RED + "Usage: /essence craft <type>"
            );
            sender.sendMessage(
                ChatColor.GRAY + "Types: void, inferno, nature, oracle, phantom, titan, arcane, divine"
            );
            return true;
        }
        
        EssenceType type = EssenceType.fromString(args[1]);
        if (type == null) {
            sender.sendMessage(ChatColor.RED + "Unknown essence type.");
            return true;
        }
        
        if (plugin.getCraftManager().hasEssenceCraft(player)) {
            player.sendMessage(
                ChatColor.RED + "You already have an essence craft!"
            );
            return true;
        }
        
        Material icon;
        switch (type) {
            case VOID -> icon = Material.ENDER_PEARL;
            case INFERNO -> icon = Material.BLAZE_ROD;
            case NATURE -> icon = Material.OAK_SAPLING;
            case ORACLE -> icon = Material.BEACON;
            case PHANTOM -> icon = Material.PHANTOM_MEMBRANE;
            case TITAN -> icon = Material.ANVIL;
            case ARCANE -> icon = Material.ENCHANTED_BOOK;
            case DIVINE -> icon = Material.TOTEM_OF_UNDYING;
            default -> icon = Material.PAPER;
        }
        
        ItemStack craft = plugin.getCraftManager().createEssenceCraft(type, icon);
        player.getInventory().addItem(craft);
        player.sendMessage(
            ChatColor.GREEN + "Gave you " + type.getDisplayName() + " Essence Craft!"
        );
        return true;
    }

    private boolean handleTeam(CommandSender sender, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Players only.");
            return true;
        }
        if (args.length < 2) {
            player.sendMessage(
                ChatColor.RED +
                    "Usage: /essence team <create|disband|join|leave|info|list|home> ..."
            );
            return true;
        }
        String sub = args[1].toLowerCase();
        switch (sub) {
            case "create":
                if (args.length < 3) {
                    player.sendMessage(
                        ChatColor.RED + "Usage: /essence team create <name>"
                    );
                    return true;
                }
                String name = args[2];
                if (teamManager.getTeam(name) != null) {
                    player.sendMessage(
                        ChatColor.RED + "A team with that name already exists."
                    );
                    return true;
                }
                if (teamManager.getTeam(player) != null) {
                    player.sendMessage(
                        ChatColor.RED + "You are already in a team."
                    );
                    return true;
                }
                Team created = teamManager.createTeam(name, player);
                if (created == null) {
                    player.sendMessage(
                        ChatColor.RED + "Could not create team."
                    );
                    return true;
                }
                player.sendMessage(
                    ChatColor.LIGHT_PURPLE +
                        "Created team " +
                        created.getName() +
                        "."
                );
                return true;
            case "disband":
                if (!teamManager.disbandTeam(player)) {
                    player.sendMessage(
                        ChatColor.RED +
                            "You must be the team owner to disband your team."
                    );
                    return true;
                }
                player.sendMessage(
                    ChatColor.LIGHT_PURPLE + "Your team has been disbanded."
                );
                return true;
            case "join":
                if (args.length < 3) {
                    player.sendMessage(
                        ChatColor.RED + "Usage: /essence team join <name>"
                    );
                    return true;
                }
                if (teamManager.getTeam(player) != null) {
                    player.sendMessage(
                        ChatColor.RED + "You are already in a team."
                    );
                    return true;
                }
                if (!teamManager.joinTeam(args[2], player)) {
                    player.sendMessage(
                        ChatColor.RED + "Could not join that team."
                    );
                    return true;
                }
                player.sendMessage(
                    ChatColor.LIGHT_PURPLE +
                        "You joined team " +
                        teamManager.getTeam(player).getName() +
                        "."
                );
                return true;
            case "leave":
                if (!teamManager.leaveTeam(player)) {
                    player.sendMessage(
                        ChatColor.RED +
                            "You are not in a team or you are the owner. Owners must disband."
                    );
                    return true;
                }
                player.sendMessage(
                    ChatColor.LIGHT_PURPLE + "You left your team."
                );
                return true;
            case "info":
                if (args.length >= 3) {
                    Player target = Bukkit.getPlayerExact(args[2]);
                    if (target == null) {
                        player.sendMessage(ChatColor.RED + "Player not found.");
                        return true;
                    }
                    sendTeamInfo(player, target);
                    return true;
                }
                sendTeamInfo(player, player);
                return true;
            case "list":
                player.sendMessage(ChatColor.LIGHT_PURPLE + "Teams:");
                if (teamManager.getTeams().isEmpty()) {
                    player.sendMessage(ChatColor.GRAY + " (none)");
                    return true;
                }
                for (Team t : teamManager.getTeams()) {
                    player.sendMessage(
                        ChatColor.GRAY +
                            " - " +
                            t.getName() +
                            ChatColor.DARK_GRAY +
                            " (" +
                            t.getMembers().size() +
                            " members)"
                    );
                }
                return true;
            case "home":
                return handleTeamHome(player, args);
            default:
                player.sendMessage(ChatColor.RED + "Unknown team subcommand.");
                return true;
        }
    }

    private void sendTeamInfo(Player viewer, Player target) {
        Team team = teamManager.getTeam(target);
        if (team == null) {
            viewer.sendMessage(
                ChatColor.GRAY + target.getName() + " is not in a team."
            );
            return;
        }
        viewer.sendMessage(
            ChatColor.LIGHT_PURPLE + "Team info for " + target.getName() + ":"
        );
        viewer.sendMessage(ChatColor.GRAY + " Name: " + team.getName());
        viewer.sendMessage(
            ChatColor.GRAY + " Owner: " + getName(team.getOwner())
        );
        StringBuilder members = new StringBuilder();
        for (java.util.UUID id : team.getMembers()) {
            if (members.length() > 0) {
                members.append(", ");
            }
            members.append(getName(id));
        }
        viewer.sendMessage(ChatColor.GRAY + " Members: " + members);
    }

    private boolean handleAdminConfig(CommandSender sender) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Players only.");
            return true;
        }
        if (!player.isOp()) {
            player.sendMessage(ChatColor.RED + "You must be an operator to access this!");
            return true;
        }
        plugin.getAdminConfigGUI().open(player);
        return true;
    }

    private boolean handleTeamHome(Player player, String[] args) {
        Team team = teamManager.getTeam(player);
        if (team == null) {
            player.sendMessage(ChatColor.RED + "You are not in a team!");
            return true;
        }
        
        if (args.length < 3) {
            player.sendMessage(ChatColor.RED + "Usage: /essence team home <create|delete|warp|list> [name]");
            return true;
        }
        
        String action = args[2].toLowerCase();
        TeamHome teamHome = teamManager.getTeamHome(team);
        
        switch (action) {
            case "create", "set":
                if (args.length < 4) {
                    player.sendMessage(ChatColor.RED + "Usage: /essence team home create <name>");
                    return true;
                }
                if (!team.getOwner().equals(player.getUniqueId())) {
                    player.sendMessage(ChatColor.RED + "Only the team owner can create homes!");
                    return true;
                }
                String homeName = args[3];
                teamHome.setHome(homeName, player.getLocation());
                teamManager.save();
                player.sendMessage(ChatColor.GREEN + "Created team home '" + homeName + "' at your location!");
                // Announce to team
                for (java.util.UUID memberId : team.getMembers()) {
                    Player member = Bukkit.getPlayer(memberId);
                    if (member != null && !member.equals(player)) {
                        member.sendMessage(
                            ChatColor.LIGHT_PURPLE + "[Team] " + ChatColor.GRAY +
                            player.getName() + " created team home '" + homeName + "'"
                        );
                    }
                }
                return true;
                
            case "delete", "remove":
                if (args.length < 4) {
                    player.sendMessage(ChatColor.RED + "Usage: /essence team home delete <name>");
                    return true;
                }
                if (!team.getOwner().equals(player.getUniqueId())) {
                    player.sendMessage(ChatColor.RED + "Only the team owner can delete homes!");
                    return true;
                }
                String deleteHome = args[3];
                if (teamHome.removeHome(deleteHome)) {
                    teamManager.save();
                    player.sendMessage(ChatColor.GREEN + "Deleted team home '" + deleteHome + "'!");
                } else {
                    player.sendMessage(ChatColor.RED + "Home '" + deleteHome + "' not found!");
                }
                return true;
                
            case "warp", "tp", "teleport":
                if (args.length < 4) {
                    player.sendMessage(ChatColor.RED + "Usage: /essence team home warp <name>");
                    return true;
                }
                String warpName = args[3];
                Location homeLoc = teamHome.getHome(warpName);
                if (homeLoc == null) {
                    player.sendMessage(ChatColor.RED + "Home '" + warpName + "' not found!");
                    return true;
                }
                player.teleport(homeLoc);
                player.sendMessage(ChatColor.GREEN + "Teleported to team home '" + warpName + "'!");
                return true;
                
            case "list":
                if (teamHome.getHomeCount() == 0) {
                    player.sendMessage(ChatColor.GRAY + "Your team has no homes set.");
                    return true;
                }
                player.sendMessage(ChatColor.LIGHT_PURPLE + "Team Homes:");
                for (String name : teamHome.getHomeNames()) {
                    Location loc = teamHome.getHome(name);
                    player.sendMessage(
                        ChatColor.GRAY + " - " + ChatColor.YELLOW + name +
                        ChatColor.DARK_GRAY + " (" + loc.getWorld().getName() + ": " +
                        loc.getBlockX() + ", " + loc.getBlockY() + ", " + loc.getBlockZ() + ")"
                    );
                }
                return true;
                
            default:
                player.sendMessage(ChatColor.RED + "Usage: /essence team home <create|delete|warp|list> [name]");
                return true;
        }
    }

    private String getName(java.util.UUID uuid) {
        Player p = Bukkit.getPlayer(uuid);
        return p != null ? p.getName() : uuid.toString();
    }

    @Override
    public List<String> onTabComplete(
        CommandSender sender,
        Command command,
        String alias,
        String[] args
    ) {
        if (args.length == 1) {
            return Arrays.asList(
                "give",
                "upgrade",
                "energy",
                "reset",
                "info",
                "tutorial",
                "stats",
                "withdraw",
                "crystal",
                "config",
                "adminconfig",
                "team",
                "primary",
                "secondary",
                "hotkey"
            );
        }
        if (args.length == 2 && args[0].equalsIgnoreCase("hotkey")) {
            return Arrays.asList("primary", "secondary");
        }
        if (args.length == 2 && args[0].equalsIgnoreCase("team")) {
            return Arrays.asList(
                "create",
                "disband",
                "join",
                "leave",
                "info",
                "list",
                "home"
            );
        }
        if (args.length == 3 && args[0].equalsIgnoreCase("team")) {
            if (args[1].equalsIgnoreCase("join")) {
                List<String> names = new ArrayList<>();
                for (Team t : teamManager.getTeams()) {
                    names.add(t.getName());
                }
                return names;
            }
            if (args[1].equalsIgnoreCase("info")) {
                List<String> names = new ArrayList<>();
                for (Player p : Bukkit.getOnlinePlayers()) {
                    names.add(p.getName());
                }
                return names;
            }
        }
        if (
            args.length == 3 &&
            args[0].equalsIgnoreCase("team") &&
            args[1].equalsIgnoreCase("home")
        ) {
            return Arrays.asList("create", "delete", "warp", "list");
        }
        if (
            args.length == 4 &&
            args[0].equalsIgnoreCase("team") &&
            args[1].equalsIgnoreCase("home") &&
            args[2].equalsIgnoreCase("warp")
        ) {
            Player player = (Player) sender;
            Team team = teamManager.getTeam(player);
            if (team != null) {
                TeamHome teamHome = teamManager.getTeamHome(team);
                return new ArrayList<>(teamHome.getHomeNames());
            }
        }
        if (
            args.length == 2 &&
            Arrays.asList(
                "give",
                "upgrade",
                "energy",
                "reset",
                "stats"
            ).contains(args[0].toLowerCase())
        ) {
            List<String> names = new ArrayList<>();
            for (Player p : Bukkit.getOnlinePlayers()) {
                names.add(p.getName());
            }
            return names;
        }
        if (args.length == 3 && args[0].equalsIgnoreCase("give")) {
            List<String> types = new ArrayList<>();
            for (EssenceType type : EssenceType.values()) {
                types.add(type.getId());
            }
            return types;
        }
        if (args.length == 3 && args[0].equalsIgnoreCase("energy")) {
            return Arrays.asList("set", "add", "remove");
        }
        return java.util.Collections.emptyList();
    }
}
