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
                ChatColor.GRAY + "/essence resetenergy <player|@a> [amount]"
            );
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
            sender.sendMessage(ChatColor.GRAY + "/essence withdraw (withdrawenergy) - Withdraw energy as crystal");
            sender.sendMessage(ChatColor.GRAY + "/essence withdrawessence - Withdraw your essence as an item");
            sender.sendMessage(ChatColor.GRAY + "/essence crystal [amount]");
            sender.sendMessage(ChatColor.GRAY + "/essence upgrader");
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
                    "/essence help " +
                    ChatColor.DARK_GRAY +
                    "- Detailed plugin guide and mechanics"
            );
            sender.sendMessage(
                ChatColor.GRAY +
                    "/essence team <create|disband|invite|accept|leave|kick|promote|demote|info|list|home|warp> ..."
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
            case "resetenergy":
                return handleResetEnergy(sender, args);
            case "info":
                return handleInfo(sender);
            case "tutorial":
                return handleTutorial(sender);
            case "help":
            case "guide":
                return handleHelp(sender, args);
            case "stats":
                return handleStats(sender, args);
            case "withdraw":
            case "withdrawenergy":
                return handleWithdrawEnergy(sender);
            case "withdrawessence":
                return handleWithdrawEssence(sender);
            case "crystal":
                return handleCrystal(sender, args);
            case "upgrader":
                return handleUpgrader(sender);
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

    private boolean handleResetEnergy(CommandSender sender, String[] args) {
        if (!sender.hasPermission("essencewars.admin")) {
            sender.sendMessage(ChatColor.RED + "No permission.");
            return true;
        }
        if (args.length < 2) {
            sender.sendMessage(
                ChatColor.RED + "Usage: /essence resetenergy <player|@a> [amount]"
            );
            return true;
        }

        // Determine the energy value to set
        int energyValue;
        if (args.length >= 3) {
            try {
                energyValue = Integer.parseInt(args[2]);
            } catch (NumberFormatException e) {
                sender.sendMessage(ChatColor.RED + "Amount must be a number.");
                return true;
            }
        } else {
            energyValue = plugin.getConfig().getInt("starting-energy", 5);
        }

        int maxEnergy = plugin.getConfig().getInt("max-energy", 10);
        energyValue = Math.min(Math.max(0, energyValue), maxEnergy);

        // Handle @a selector for all players
        if (args[1].equalsIgnoreCase("@a")) {
            int count = 0;
            for (Player p : Bukkit.getOnlinePlayers()) {
                PlayerEssenceData data = dataManager.getOrCreate(p);
                data.setEnergy(energyValue);
                plugin.getScoreboardManager().updateFor(p);
                p.sendMessage(
                    ChatColor.LIGHT_PURPLE +
                        "Your energy has been reset to " +
                        energyValue +
                        "."
                );
                count++;
            }
            sender.sendMessage(
                ChatColor.GREEN +
                    "Reset energy to " +
                    energyValue +
                    " for " +
                    count +
                    " player(s)."
            );
            return true;
        }

        // Handle individual player
        Player target = Bukkit.getPlayerExact(args[1]);
        if (target == null) {
            sender.sendMessage(ChatColor.RED + "Player not found.");
            return true;
        }

        PlayerEssenceData data = dataManager.getOrCreate(target);
        data.setEnergy(energyValue);
        plugin.getScoreboardManager().updateFor(target);

        sender.sendMessage(
            ChatColor.GREEN +
                "Reset " +
                target.getName() +
                "'s energy to " +
                energyValue +
                "."
        );
        target.sendMessage(
            ChatColor.LIGHT_PURPLE +
                "Your energy has been reset to " +
                energyValue +
                "."
        );
        return true;
    }

    private boolean handleInfo(CommandSender sender) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Players only.");
            return true;
        }

        // Open infuse recipes GUI
        plugin.getInfuseRecipesGUI().open(player);
        player.sendMessage(
            ChatColor.LIGHT_PURPLE + "INFUSE " + ChatColor.GOLD + "RECIPES" + ChatColor.GRAY + " - Opening..."
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

    private boolean handleHelp(CommandSender sender, String[] args) {
        String topic = args.length >= 2 ? args[1].toLowerCase() : "main";

        switch (topic) {
            case "main":
            case "index":
                return showMainHelp(sender);
            case "energy":
                return showEnergyHelp(sender);
            case "essences":
            case "essence":
                return showEssencesHelp(sender);
            case "tier2":
            case "tier":
            case "upgrade":
                return showTier2Help(sender);
            case "abilities":
            case "ability":
                return showAbilitiesHelp(sender);
            case "pvp":
            case "combat":
                return showPvPHelp(sender);
            case "teams":
            case "team":
                return showTeamsHelp(sender);
            case "crafting":
            case "craft":
                return showCraftingHelp(sender);
            default:
                sender.sendMessage(ChatColor.RED + "Unknown help topic. Use /essence help for main menu.");
                return true;
        }
    }

    private boolean showMainHelp(CommandSender sender) {
        sender.sendMessage("§5§m                                                ");
        sender.sendMessage("§5§l        ESSENCE WARS - COMPLETE GUIDE");
        sender.sendMessage("§5§m                                                ");
        sender.sendMessage("");
        sender.sendMessage("§d§lWhat is EssenceWars?");
        sender.sendMessage("§7A PvP-focused plugin where players claim powerful");
        sender.sendMessage("§7essences, manage energy, and use unique abilities!");
        sender.sendMessage("");
        sender.sendMessage("§e§lHelp Topics:");
        sender.sendMessage("§f/essence help energy §7- Energy system explained");
        sender.sendMessage("§f/essence help essences §7- All about essences");
        sender.sendMessage("§f/essence help tier2 §7- Tier II upgrades");
        sender.sendMessage("§f/essence help abilities §7- Using your powers");
        sender.sendMessage("§f/essence help pvp §7- Combat and PvP mechanics");
        sender.sendMessage("§f/essence help teams §7- Team system");
        sender.sendMessage("§f/essence help crafting §7- Crafting recipes");
        sender.sendMessage("");
        sender.sendMessage("§b§lQuick Start:");
        sender.sendMessage("§71. Get an essence by finding/crafting essence items");
        sender.sendMessage("§72. Right-click to absorb and claim ownership");
        sender.sendMessage("§73. Use Q (or commands) to cast abilities");
        sender.sendMessage("§74. Gain energy by killing players");
        sender.sendMessage("§75. Reach Tier II to unlock Divine essence!");
        sender.sendMessage("§5§m                                                ");
        return true;
    }

    private boolean showEnergyHelp(CommandSender sender) {
        sender.sendMessage("§5§m                                                ");
        sender.sendMessage("§5§l           ENERGY SYSTEM GUIDE");
        sender.sendMessage("§5§m                                                ");
        sender.sendMessage("");
        sender.sendMessage("§d§lWhat is Energy?");
        sender.sendMessage("§7Energy is your life force and ability resource.");
        sender.sendMessage("§7It determines your power level and energy state.");
        sender.sendMessage("");
        sender.sendMessage("§e§lHow to Get Energy:");
        sender.sendMessage("§a✓ §fStart with §a" + plugin.getConfig().getInt("starting-energy", 5) + " energy §7when you join");
        sender.sendMessage("§a✓ §fKill players: §a+1 energy");
        sender.sendMessage("§a✓ §fPick up energy crystals");
        sender.sendMessage("§a✓ §fRight-click energy crystals to absorb them");
        sender.sendMessage("§a✓ §fCraft energy crystals (see /essence help crafting)");
        sender.sendMessage("");
        sender.sendMessage("§c§lHow to Lose Energy:");
        sender.sendMessage("§c✗ §fDie: §c-1 energy");
        sender.sendMessage("§c✗ §fSome abilities cost energy to use");
        sender.sendMessage("");
        sender.sendMessage("§b§lEnergy States:");
        sender.sendMessage("§7Your energy determines your state and power:");
        sender.sendMessage("§a§l10-9 RADIANT §7- Full power");
        sender.sendMessage("§e§l8-7 DIMMED §7- Slight cooldown increase");
        sender.sendMessage("§6§l6-5 FRACTURED §7- Moderate cooldown increase");
        sender.sendMessage("§6§l4-3 FADING §7- Higher cooldown increase");
        sender.sendMessage("§c§l2-1 FRAGILE §7- Significant cooldown increase");
        sender.sendMessage("§4§l0 DEPLETED §7- Cannot use abilities!");
        sender.sendMessage("");
        sender.sendMessage("§f§lCommands:");
        sender.sendMessage("§f/essence withdraw §7- Convert energy to crystal");
        sender.sendMessage("§f/essence stats §7- View your energy and stats");
        sender.sendMessage("§5§m                                                ");
        return true;
    }

    private boolean showEssencesHelp(CommandSender sender) {
        sender.sendMessage("§5§m                                                ");
        sender.sendMessage("§5§l          ESSENCES GUIDE");
        sender.sendMessage("§5§m                                                ");
        sender.sendMessage("");
        sender.sendMessage("§d§lWhat are Essences?");
        sender.sendMessage("§7Essences are powerful forces that grant unique");
        sender.sendMessage("§7abilities. Only ONE player can own each essence!");
        sender.sendMessage("");
        sender.sendMessage("§e§lAvailable Essences (Tier I):");
        sender.sendMessage("§5§lVoid §7- Singularity pull + Guillotine execute");
        sender.sendMessage("§6§lInferno §7- Flame mines + Phoenix rebirth");
        sender.sendMessage("§a§lNature §7- Poison vines + Primal surge");
        sender.sendMessage("§b§lOracle §7- Prophetic vision + Fate's curse");
        sender.sendMessage("§8§lPhantom §7- Shadowmeld + Execution strike");
        sender.sendMessage("§7§lTitan §7- Seismic slam + Colossus form");
        sender.sendMessage("§d§lArcane §7- Arcane dice + Emergency teleport");
        sender.sendMessage("");
        sender.sendMessage("§6§l§nTier II Essence:");
        sender.sendMessage("§6§l§lDIVINE §7- Ultimate power (requires Tier II!)");
        sender.sendMessage("§7Omnipotent strike + Dragon ascension");
        sender.sendMessage("");
        sender.sendMessage("§c§lEssence Ownership:");
        sender.sendMessage("§7• Each essence can only be owned by ONE player");
        sender.sendMessage("§7• Right-click essence item to claim it");
        sender.sendMessage("§7• When you die, you DROP your essence!");
        sender.sendMessage("§7• Other players can pick it up and claim it");
        sender.sendMessage("§7• Compete to control the most powerful essences!");
        sender.sendMessage("");
        sender.sendMessage("§f§lHow to Get Essences:");
        sender.sendMessage("§f/essence help crafting §7- See crafting recipes");
        sender.sendMessage("§f/essence info §7- View all essence recipes");
        sender.sendMessage("§5§m                                                ");
        return true;
    }

    private boolean showTier2Help(CommandSender sender) {
        sender.sendMessage("§5§m                                                ");
        sender.sendMessage("§5§l          TIER II UPGRADE GUIDE");
        sender.sendMessage("§5§m                                                ");
        sender.sendMessage("");
        sender.sendMessage("§d§lWhat is Tier II?");
        sender.sendMessage("§7Tier II is an advanced upgrade that unlocks:");
        sender.sendMessage("§a✓ §7Access to the §6§lDivine Essence");
        sender.sendMessage("§a✓ §7Enhanced abilities for all essences");
        sender.sendMessage("§a✓ §7Stronger secondary abilities");
        sender.sendMessage("§a✓ §7Prestige and power over other players");
        sender.sendMessage("");
        sender.sendMessage("§e§lHow to Get Tier II:");
        sender.sendMessage("§7Craft the §6§lEssence Upgrader §7and right-click it!");
        sender.sendMessage("");
        sender.sendMessage("§6§lCrafting Recipe:");
        sender.sendMessage("§7  D = Diamond, E = Enchanted Golden Apple");
        sender.sendMessage("§7  G = Gold Block, N = Nether Star, B = Blaze Rod");
        sender.sendMessage("");
        sender.sendMessage("§f   [D] [E] [D]");
        sender.sendMessage("§f   [G] [N] [G]");
        sender.sendMessage("§f   [D] [B] [D]");
        sender.sendMessage("");
        sender.sendMessage("§c§lRequirements:");
        sender.sendMessage("§7  • Kill the Wither Boss (for Nether Star)");
        sender.sendMessage("§7  • Mine 4 Diamonds");
        sender.sendMessage("§7  • Craft 2 Gold Blocks");
        sender.sendMessage("§7  • Find an Enchanted Golden Apple");
        sender.sendMessage("§7  • Get a Blaze Rod from the Nether");
        sender.sendMessage("");
        sender.sendMessage("§a§lThis is challenging but achievable!");
        sender.sendMessage("§f/essence help crafting §7- More crafting info");
        sender.sendMessage("");
        sender.sendMessage("§8Admin: §f/essence upgrade <player> §7- Force upgrade");
        sender.sendMessage("");
        sender.sendMessage("§6§l§lDIVINE ESSENCE:");
        sender.sendMessage("§7The Divine Essence is the ultimate power!");
        sender.sendMessage("§7Only Tier II players can absorb it.");
        sender.sendMessage("");
        sender.sendMessage("§6Primary: §fOmnipotent Strike");
        sender.sendMessage("§7  Devastating AoE attack in a large radius");
        sender.sendMessage("§6Secondary: §fDragon Ascension");
        sender.sendMessage("§7  Transform into a dragon with:");
        sender.sendMessage("§7  • Massive damage multiplier");
        sender.sendMessage("§7  • Enhanced movement");
        sender.sendMessage("§7  • Increased kill rewards");
        sender.sendMessage("");
        sender.sendMessage("§c§lNote: §7Contact server admins to learn how");
        sender.sendMessage("§7to achieve Tier II on this server!");
        sender.sendMessage("§5§m                                                ");
        return true;
    }

    private boolean showAbilitiesHelp(CommandSender sender) {
        sender.sendMessage("§5§m                                                ");
        sender.sendMessage("§5§l         ABILITIES GUIDE");
        sender.sendMessage("§5§m                                                ");
        sender.sendMessage("");
        sender.sendMessage("§d§lUsing Your Abilities:");
        sender.sendMessage("§7Each essence has TWO abilities:");
        sender.sendMessage("§e§lPrimary §7- Main attack/utility");
        sender.sendMessage("§e§lSecondary §7- Ultimate/powerful ability");
        sender.sendMessage("");
        sender.sendMessage("§b§lHow to Cast Abilities:");
        sender.sendMessage("§f1. Offhand Key (F) §7(default):");
        sender.sendMessage("   §7Press F = Primary ability");
        sender.sendMessage("   §7Shift+F = Secondary ability");
        sender.sendMessage("§f2. Commands:");
        sender.sendMessage("   §f/essence primary §7- Cast primary");
        sender.sendMessage("   §f/essence secondary §7- Cast secondary");
        sender.sendMessage("§f3. Custom Hotkeys:");
        sender.sendMessage("   §f/essence hotkey primary §7- Bind primary");
        sender.sendMessage("   §f/essence hotkey secondary §7- Bind secondary");
        sender.sendMessage("   §7Then set in Minecraft keybinds!");
        sender.sendMessage("");
        sender.sendMessage("§c§lCooldowns:");
        sender.sendMessage("§7Abilities have cooldowns based on your energy state.");
        sender.sendMessage("§7Lower energy = longer cooldowns!");
        sender.sendMessage("§7Stay at high energy for maximum power.");
        sender.sendMessage("");
        sender.sendMessage("§e§lConfiguration:");
        sender.sendMessage("§f/essence config §7- Open settings GUI");
        sender.sendMessage("§7Customize ability parameters and offhand key behavior");
        sender.sendMessage("");
        sender.sendMessage("§f§lView All Abilities:");
        sender.sendMessage("§f/essence help essences §7- See all essence abilities");
        sender.sendMessage("§5§m                                                ");
        return true;
    }

    private boolean showPvPHelp(CommandSender sender) {
        sender.sendMessage("§5§m                                                ");
        sender.sendMessage("§5§l         PVP & COMBAT GUIDE");
        sender.sendMessage("§5§m                                                ");
        sender.sendMessage("");
        sender.sendMessage("§d§lCombat System:");
        sender.sendMessage("§7EssenceWars features competitive PvP where");
        sender.sendMessage("§7essences are at stake!");
        sender.sendMessage("");
        sender.sendMessage("§e§lWhat Happens When You Die:");
        sender.sendMessage("§c✗ §fLose §c1 energy");
        sender.sendMessage("§c✗ §fDROP YOUR ESSENCE §7(if you have one)");
        sender.sendMessage("§c✗ §fOthers can claim your essence!");
        sender.sendMessage("");
        sender.sendMessage("§a§lWhat Happens When You Kill:");
        sender.sendMessage("§a✓ §fGain §a+1 energy");
        sender.sendMessage("§a✓ §fCan claim dropped essences");
        sender.sendMessage("§a✓ §fSome abilities reward kills");
        sender.sendMessage("");
        sender.sendMessage("§b§lProtections:");
        sender.sendMessage("§7• §aTeam Protection: §7Cannot damage teammates");
        sender.sendMessage("");
        sender.sendMessage("§6§lCombat Tips:");
        sender.sendMessage("§7• Keep your energy HIGH for shorter cooldowns");
        sender.sendMessage("§7• Use abilities strategically");
        sender.sendMessage("§7• Protect your essence - you drop it on death!");
        sender.sendMessage("§7• Team up with others for safety");
        sender.sendMessage("§7• Master your essence's abilities");
        sender.sendMessage("");
        sender.sendMessage("§f§lCommands:");
        sender.sendMessage("§f/essence stats [player] §7- Check combat stats");
        sender.sendMessage("§5§m                                                ");
        return true;
    }

    private boolean showTeamsHelp(CommandSender sender) {
        sender.sendMessage("§5§m                                                ");
        sender.sendMessage("§5§l            TEAMS GUIDE");
        sender.sendMessage("§5§m                                                ");
        sender.sendMessage("");
        sender.sendMessage("§d§lWhat are Teams?");
        sender.sendMessage("§7Form teams with other players for:");
        sender.sendMessage("§a✓ §7Friendly fire protection");
        sender.sendMessage("§a✓ §7Shared team homes");
        sender.sendMessage("§a✓ §7Coordinated essence control");
        sender.sendMessage("§a✓ §7Social gameplay");
        sender.sendMessage("");
        sender.sendMessage("§e§lTeam Commands:");
        sender.sendMessage("§f/essence team create <name> §7- Create a team");
        sender.sendMessage("§f/essence team invite <player> §7- Invite someone");
        sender.sendMessage("§f/essence team accept <team> §7- Accept invitation");
        sender.sendMessage("§f/essence team leave §7- Leave your team");
        sender.sendMessage("§f/essence team disband §7- Disband (owner only)");
        sender.sendMessage("§f/essence team info [player] §7- View team info");
        sender.sendMessage("§f/essence team list §7- List all teams");
        sender.sendMessage("");
        sender.sendMessage("§b§lTeam Management:");
        sender.sendMessage("§f/essence team kick <player> §7- Remove member");
        sender.sendMessage("§f/essence team promote <player> §7- Make admin");
        sender.sendMessage("§f/essence team demote <player> §7- Remove admin");
        sender.sendMessage("");
        sender.sendMessage("§6§lTeam Homes:");
        sender.sendMessage("§f/essence team home create <name> §7- Set home");
        sender.sendMessage("§f/essence team home warp <name> §7- Teleport");
        sender.sendMessage("§f/essence team home list §7- List all homes");
        sender.sendMessage("§f/essence team home delete <name> §7- Remove home");
        sender.sendMessage("");
        sender.sendMessage("§7§lRanks:");
        sender.sendMessage("§6Owner §7- Full control, can disband team");
        sender.sendMessage("§eAdmin §7- Invite, kick, manage members");
        sender.sendMessage("§7Member §7- Regular team member");
        sender.sendMessage("§5§m                                                ");
        return true;
    }

    private boolean showCraftingHelp(CommandSender sender) {
        sender.sendMessage("§5§m                                                ");
        sender.sendMessage("§5§l          CRAFTING GUIDE");
        sender.sendMessage("§5§m                                                ");
        sender.sendMessage("");
        sender.sendMessage("§d§lEnergy Crystal Recipe:");
        sender.sendMessage("§7Craft energy crystals to store and trade energy!");
        sender.sendMessage("");
        sender.sendMessage("§f   [ ] [A] [ ]");
        sender.sendMessage("§f   [A] [B] [A]");
        sender.sendMessage("§f   [ ] [C] [ ]");
        sender.sendMessage("");
        sender.sendMessage("§b[A] §f= Amethyst Shard");
        sender.sendMessage("§d[B] §f= Ender Pearl");
        sender.sendMessage("§b[C] §f= Diamond");
        sender.sendMessage("");
        sender.sendMessage("§aResult: §fEnergy Crystal (" + plugin.getConfig().getInt("crystal-energy-value", 1) + " energy)");
        sender.sendMessage("§7Right-click to absorb the energy!");
        sender.sendMessage("");
        sender.sendMessage("§6§l§nEssence Upgrader Recipe:");
        sender.sendMessage("§7Upgrade to Tier II and unlock Divine Essence!");
        sender.sendMessage("");
        sender.sendMessage("§f   [D] [E] [D]");
        sender.sendMessage("§f   [G] [N] [G]");
        sender.sendMessage("§f   [D] [B] [D]");
        sender.sendMessage("");
        sender.sendMessage("§e[D] §f= Diamond  §6[E] §f= Enchanted Golden Apple");
        sender.sendMessage("§6[G] §f= Gold Block  §f[N] §f= Nether Star");
        sender.sendMessage("§6[B] §f= Blaze Rod");
        sender.sendMessage("");
        sender.sendMessage("§aResult: §6§lEssence Upgrader");
        sender.sendMessage("§7Right-click to become Tier II!");
        sender.sendMessage("§c§lVery difficult - requires killing the Wither!");
        sender.sendMessage("");
        sender.sendMessage("§e§lEssence Infusion:");
        sender.sendMessage("§7Each essence has its own infusion recipe.");
        sender.sendMessage("§7Use these commands to view recipes:");
        sender.sendMessage("");
        sender.sendMessage("§f/essence info §7- Open " + ChatColor.LIGHT_PURPLE + "INFUSE " + ChatColor.GOLD + "RECIPES " + ChatColor.GRAY + "GUI");
        sender.sendMessage("§7  §8(Interactive menu with all infusion recipes)");
        sender.sendMessage("");
        sender.sendMessage("§6§lCrafting Tips:");
        sender.sendMessage("§7• Essence items are rare and valuable");
        sender.sendMessage("§7• Only one person can own each essence");
        sender.sendMessage("§7• Craft energy crystals to trade energy");
        sender.sendMessage("§7• Use /essence withdraw to convert energy");
        sender.sendMessage("§7  §8(Must have at least " + plugin.getConfig().getInt("crystal-energy-value", 1) + " energy)");
        sender.sendMessage("");
        sender.sendMessage("§c§lNote: §7Essence recipes may be customized");
        sender.sendMessage("§7by server admins. Check /essence info!");
        sender.sendMessage("§5§m                                                ");
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

    private boolean handleWithdrawEnergy(CommandSender sender) {
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
        player.getInventory().addItem(EnergyCrystalItem.create(value));
        player.sendMessage(
            ChatColor.LIGHT_PURPLE +
                "Withdrew " +
                value +
                " energy as a crystal."
        );
        plugin.getScoreboardManager().updateFor(player);
        return true;
    }

    private boolean handleWithdrawEssence(CommandSender sender) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Players only.");
            return true;
        }

        PlayerEssenceData data = dataManager.getOrCreate(player);
        EssenceType currentEssence = data.getEssenceType();

        if (currentEssence == null) {
            player.sendMessage(
                ChatColor.RED + "You don't have an essence to withdraw!"
            );
            return true;
        }

        // Get the icon for this essence type
        Material icon;
        switch (currentEssence) {
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

        // Create the essence craft item
        ItemStack essenceCraft = plugin.getCraftManager().createEssenceCraft(currentEssence, icon);

        // Remove the player's essence and ownership
        data.setEssenceType(null);
        dataManager.save(data);
        plugin.removeEssenceOwner(currentEssence, player.getUniqueId());

        // Give the player the essence craft item
        player.getInventory().addItem(essenceCraft);

        player.sendMessage(
            ChatColor.LIGHT_PURPLE +
                "Withdrew your " +
                currentEssence.getDisplayName() +
                " Essence as an item!"
        );
        player.sendMessage(
            ChatColor.GRAY +
                "Right-click it to reclaim your essence, or trade it to another player."
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

    private boolean handleUpgrader(CommandSender sender) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Players only.");
            return true;
        }
        if (!sender.hasPermission("essencewars.admin")) {
            sender.sendMessage(ChatColor.RED + "No permission.");
            return true;
        }
        if (com.essencewars.items.EssenceUpgraderItem.hasUpgrader(player)) {
            player.sendMessage(
                ChatColor.RED + "You already have an essence upgrader."
            );
            return true;
        }
        player.getInventory().addItem(com.essencewars.items.EssenceUpgraderItem.create());
        sender.sendMessage(
            ChatColor.GREEN + "Gave essence upgrader to " + player.getName() + "."
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
                    "Usage: /essence team <create|disband|invite|accept|leave|kick|promote|demote|info|list|home|warp> ..."
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
            case "invite":
                if (args.length < 3) {
                    player.sendMessage(
                        ChatColor.RED + "Usage: /essence team invite <player>"
                    );
                    return true;
                }
                Team inviterTeam = teamManager.getTeam(player);
                if (inviterTeam == null) {
                    player.sendMessage(
                        ChatColor.RED + "You are not in a team."
                    );
                    return true;
                }
                if (!inviterTeam.isOwnerOrAdmin(player.getUniqueId())) {
                    player.sendMessage(
                        ChatColor.RED + "Only team owners and admins can invite players."
                    );
                    return true;
                }
                Player invitee = Bukkit.getPlayerExact(args[2]);
                if (invitee == null) {
                    player.sendMessage(ChatColor.RED + "Player not found.");
                    return true;
                }
                if (teamManager.getTeam(invitee) != null) {
                    player.sendMessage(
                        ChatColor.RED + "That player is already in a team."
                    );
                    return true;
                }
                if (teamManager.hasInvitation(invitee.getUniqueId(), inviterTeam)) {
                    player.sendMessage(
                        ChatColor.RED + "That player already has a pending invitation."
                    );
                    return true;
                }
                teamManager.invitePlayer(inviterTeam, invitee.getUniqueId());
                teamManager.save();
                player.sendMessage(
                    ChatColor.LIGHT_PURPLE + "Invited " + invitee.getName() + " to your team."
                );
                invitee.sendMessage(
                    ChatColor.LIGHT_PURPLE +
                        "You have been invited to join team " +
                        inviterTeam.getName() +
                        ". Use " +
                        ChatColor.GRAY +
                        "/essence team accept " +
                        inviterTeam.getName() +
                        ChatColor.LIGHT_PURPLE +
                        " to accept."
                );
                return true;
            case "accept":
                if (args.length < 3) {
                    player.sendMessage(
                        ChatColor.RED + "Usage: /essence team accept <team>"
                    );
                    return true;
                }
                if (teamManager.getTeam(player) != null) {
                    player.sendMessage(
                        ChatColor.RED + "You are already in a team."
                    );
                    return true;
                }
                Team teamToJoin = teamManager.getTeam(args[2]);
                if (teamToJoin == null) {
                    player.sendMessage(
                        ChatColor.RED + "Team not found."
                    );
                    return true;
                }
                if (!teamManager.acceptInvitation(player, teamToJoin)) {
                    player.sendMessage(
                        ChatColor.RED + "You do not have an invitation to that team."
                    );
                    return true;
                }
                teamManager.save();
                player.sendMessage(
                    ChatColor.LIGHT_PURPLE + "You joined team " + teamToJoin.getName() + "!"
                );
                // Notify team members
                for (java.util.UUID memberId : teamToJoin.getMembers()) {
                    Player member = Bukkit.getPlayer(memberId);
                    if (member != null && !member.equals(player)) {
                        member.sendMessage(
                            ChatColor.LIGHT_PURPLE +
                                "[Team] " +
                                ChatColor.GRAY +
                                player.getName() +
                                " has joined the team!"
                        );
                    }
                }
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
            case "kick":
                if (args.length < 3) {
                    player.sendMessage(
                        ChatColor.RED + "Usage: /essence team kick <player>"
                    );
                    return true;
                }
                Team kickerTeam = teamManager.getTeam(player);
                if (kickerTeam == null) {
                    player.sendMessage(
                        ChatColor.RED + "You are not in a team."
                    );
                    return true;
                }
                if (!kickerTeam.isOwnerOrAdmin(player.getUniqueId())) {
                    player.sendMessage(
                        ChatColor.RED + "Only team owners and admins can kick players."
                    );
                    return true;
                }
                Player kickTarget = Bukkit.getPlayerExact(args[2]);
                java.util.UUID kickTargetId = null;
                if (kickTarget != null) {
                    kickTargetId = kickTarget.getUniqueId();
                } else {
                    // Try to find offline player by name
                    for (java.util.UUID memberId : kickerTeam.getMembers()) {
                        Player member = Bukkit.getPlayer(memberId);
                        if (member != null && member.getName().equalsIgnoreCase(args[2])) {
                            kickTargetId = memberId;
                            kickTarget = member;
                            break;
                        }
                    }
                }
                if (kickTargetId == null) {
                    player.sendMessage(ChatColor.RED + "Player not found.");
                    return true;
                }
                if (!kickerTeam.isMember(kickTargetId)) {
                    player.sendMessage(
                        ChatColor.RED + "That player is not in your team."
                    );
                    return true;
                }
                if (kickTargetId.equals(kickerTeam.getOwner())) {
                    player.sendMessage(
                        ChatColor.RED + "You cannot kick the team owner."
                    );
                    return true;
                }
                // Admins cannot kick other admins, only owner can
                if (!kickerTeam.isOwner(player.getUniqueId()) &&
                    kickerTeam.isAdmin(kickTargetId)) {
                    player.sendMessage(
                        ChatColor.RED + "Only the owner can kick admins."
                    );
                    return true;
                }
                kickerTeam.removeMember(kickTargetId);
                teamManager.leaveTeam(Bukkit.getPlayer(kickTargetId));
                teamManager.save();
                player.sendMessage(
                    ChatColor.LIGHT_PURPLE + "Kicked " + args[2] + " from the team."
                );
                if (kickTarget != null) {
                    kickTarget.sendMessage(
                        ChatColor.RED + "You have been kicked from team " + kickerTeam.getName() + "."
                    );
                }
                return true;
            case "promote":
                if (args.length < 3) {
                    player.sendMessage(
                        ChatColor.RED + "Usage: /essence team promote <player>"
                    );
                    return true;
                }
                Team promoteTeam = teamManager.getTeam(player);
                if (promoteTeam == null) {
                    player.sendMessage(
                        ChatColor.RED + "You are not in a team."
                    );
                    return true;
                }
                if (!promoteTeam.isOwner(player.getUniqueId())) {
                    player.sendMessage(
                        ChatColor.RED + "Only the team owner can promote players."
                    );
                    return true;
                }
                Player promoteTarget = Bukkit.getPlayerExact(args[2]);
                if (promoteTarget == null) {
                    player.sendMessage(ChatColor.RED + "Player not found.");
                    return true;
                }
                if (!promoteTeam.isMember(promoteTarget.getUniqueId())) {
                    player.sendMessage(
                        ChatColor.RED + "That player is not in your team."
                    );
                    return true;
                }
                if (promoteTarget.getUniqueId().equals(promoteTeam.getOwner())) {
                    player.sendMessage(
                        ChatColor.RED + "That player is already the owner."
                    );
                    return true;
                }
                if (promoteTeam.isAdmin(promoteTarget.getUniqueId())) {
                    player.sendMessage(
                        ChatColor.RED + "That player is already an admin."
                    );
                    return true;
                }
                promoteTeam.setRank(promoteTarget.getUniqueId(), Team.TeamRank.ADMIN);
                teamManager.save();
                player.sendMessage(
                    ChatColor.LIGHT_PURPLE + "Promoted " + promoteTarget.getName() + " to admin."
                );
                promoteTarget.sendMessage(
                    ChatColor.LIGHT_PURPLE + "You have been promoted to admin in team " + promoteTeam.getName() + "!"
                );
                return true;
            case "demote":
                if (args.length < 3) {
                    player.sendMessage(
                        ChatColor.RED + "Usage: /essence team demote <player>"
                    );
                    return true;
                }
                Team demoteTeam = teamManager.getTeam(player);
                if (demoteTeam == null) {
                    player.sendMessage(
                        ChatColor.RED + "You are not in a team."
                    );
                    return true;
                }
                if (!demoteTeam.isOwner(player.getUniqueId())) {
                    player.sendMessage(
                        ChatColor.RED + "Only the team owner can demote players."
                    );
                    return true;
                }
                Player demoteTarget = Bukkit.getPlayerExact(args[2]);
                if (demoteTarget == null) {
                    player.sendMessage(ChatColor.RED + "Player not found.");
                    return true;
                }
                if (!demoteTeam.isMember(demoteTarget.getUniqueId())) {
                    player.sendMessage(
                        ChatColor.RED + "That player is not in your team."
                    );
                    return true;
                }
                if (demoteTarget.getUniqueId().equals(demoteTeam.getOwner())) {
                    player.sendMessage(
                        ChatColor.RED + "You cannot demote the owner."
                    );
                    return true;
                }
                if (!demoteTeam.isAdmin(demoteTarget.getUniqueId())) {
                    player.sendMessage(
                        ChatColor.RED + "That player is not an admin."
                    );
                    return true;
                }
                demoteTeam.setRank(demoteTarget.getUniqueId(), Team.TeamRank.MEMBER);
                teamManager.save();
                player.sendMessage(
                    ChatColor.LIGHT_PURPLE + "Demoted " + demoteTarget.getName() + " to member."
                );
                demoteTarget.sendMessage(
                    ChatColor.RED + "You have been demoted to member in team " + demoteTeam.getName() + "."
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
            case "warp":
                Team warpTeam = teamManager.getTeam(player);
                if (warpTeam == null) {
                    player.sendMessage(ChatColor.RED + "You are not in a team!");
                    return true;
                }
                if (args.length < 3) {
                    player.sendMessage(ChatColor.RED + "Usage: /essence team warp <name>");
                    return true;
                }
                String warpName = args[2];
                TeamHome warpTeamHome = teamManager.getTeamHome(warpTeam);
                Location warpLoc = warpTeamHome.getHome(warpName);
                if (warpLoc == null) {
                    player.sendMessage(ChatColor.RED + "Home '" + warpName + "' not found!");
                    return true;
                }
                player.teleport(warpLoc);
                player.sendMessage(ChatColor.GREEN + "Teleported to team home '" + warpName + "'!");
                return true;
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
            Team.TeamRank rank = team.getRank(id);
            String rankColor = switch (rank) {
                case OWNER -> ChatColor.GOLD.toString();
                case ADMIN -> ChatColor.YELLOW.toString();
                case MEMBER -> ChatColor.GRAY.toString();
            };
            members.append(rankColor).append(getName(id));
            if (rank != Team.TeamRank.MEMBER) {
                members.append(ChatColor.DARK_GRAY).append(" [").append(rank.getDisplayName()).append("]");
            }
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
                "resetenergy",
                "info",
                "tutorial",
                "stats",
                "withdraw",
                "withdrawenergy",
                "withdrawessence",
                "crystal",
                "upgrader",
                "config",
                "adminconfig",
                "team",
                "primary",
                "secondary",
                "hotkey",
                "craft"
            );
        }
        if (args.length == 2 && args[0].equalsIgnoreCase("hotkey")) {
            return Arrays.asList("primary", "secondary");
        }
        if (args.length == 2 && (args[0].equalsIgnoreCase("help") || args[0].equalsIgnoreCase("guide"))) {
            return Arrays.asList("energy", "essences", "tier2", "abilities", "pvp", "teams", "crafting");
        }
        if (args.length == 2 && args[0].equalsIgnoreCase("team")) {
            return Arrays.asList(
                "create",
                "disband",
                "invite",
                "accept",
                "leave",
                "kick",
                "promote",
                "demote",
                "info",
                "list",
                "home",
                "warp"
            );
        }
        if (args.length == 3 && args[0].equalsIgnoreCase("team")) {
            if (args[1].equalsIgnoreCase("accept")) {
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
            if (args[1].equalsIgnoreCase("invite") ||
                args[1].equalsIgnoreCase("kick") ||
                args[1].equalsIgnoreCase("promote") ||
                args[1].equalsIgnoreCase("demote")) {
                List<String> names = new ArrayList<>();
                for (Player p : Bukkit.getOnlinePlayers()) {
                    names.add(p.getName());
                }
                return names;
            }
            if (args[1].equalsIgnoreCase("warp")) {
                Player player = (Player) sender;
                Team team = teamManager.getTeam(player);
                if (team != null) {
                    TeamHome teamHome = teamManager.getTeamHome(team);
                    return new ArrayList<>(teamHome.getHomeNames());
                }
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
        if (args.length == 2 && args[0].equalsIgnoreCase("resetenergy")) {
            List<String> names = new ArrayList<>();
            names.add("@a");
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
