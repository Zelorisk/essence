package com.essencewars.ui;

import com.essencewars.EssenceWarsPlugin;
import java.util.Arrays;
import java.util.List;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class TutorialGUI implements Listener {

    private final EssenceWarsPlugin plugin;
    public static final String TITLE =
        ChatColor.LIGHT_PURPLE + "EssenceWars Tutorial";

    public TutorialGUI(EssenceWarsPlugin plugin) {
        this.plugin = plugin;
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    public void open(Player player) {
        open(player, 0);
    }

    public void open(Player player, int page) {
        Inventory inv = Bukkit.createInventory(
            null,
            54,
            TITLE + " - Page " + (page + 1)
        );

        switch (page) {
            case 0:
                populateWelcomePage(inv);
                break;
            case 1:
                populateEssencePage(inv);
                break;
            case 2:
                populateAbilitiesPage(inv);
                break;
            case 3:
                populateEnergyPage(inv);
                break;
            case 4:
                populateCommandsPage(inv);
                break;
        }

        // Navigation
        if (page > 0) {
            inv.setItem(
                45,
                createButton(
                    Material.ARROW,
                    ChatColor.GREEN + "« Previous Page",
                    "Click to go back"
                )
            );
        }
        if (page < 4) {
            inv.setItem(
                53,
                createButton(
                    Material.ARROW,
                    ChatColor.GREEN + "Next Page »",
                    "Click to continue"
                )
            );
        }

        // Close button
        inv.setItem(
            49,
            createButton(
                Material.BARRIER,
                ChatColor.RED + "Close Tutorial",
                "You can reopen this anytime with",
                ChatColor.GRAY + "/essence config"
            )
        );

        player.openInventory(inv);
    }

    private void populateWelcomePage(Inventory inv) {
        inv.setItem(
            4,
            createButton(
                Material.ENCHANTED_BOOK,
                ChatColor.LIGHT_PURPLE +
                    "" +
                    ChatColor.BOLD +
                    "Welcome to EssenceWars!",
                "",
                ChatColor.GRAY + "EssenceWars is a PvP plugin where you",
                ChatColor.GRAY + "harness powerful essences to fight.",
                "",
                ChatColor.GRAY + "This tutorial will teach you:",
                ChatColor.YELLOW + " • What essences are",
                ChatColor.YELLOW + " • How to use abilities",
                ChatColor.YELLOW + " • Energy management",
                ChatColor.YELLOW + " • Important commands",
                "",
                ChatColor.GREEN + "Click 'Next Page' to continue →"
            )
        );

        inv.setItem(
            20,
            createButton(
                Material.DRAGON_BREATH,
                ChatColor.LIGHT_PURPLE + "What is an Essence?",
                "",
                ChatColor.GRAY + "Essences are magical powers that grant",
                ChatColor.GRAY + "you unique combat abilities.",
                "",
                ChatColor.GRAY + "Each essence has:",
                ChatColor.YELLOW + " • A primary ability",
                ChatColor.YELLOW + " • A secondary ability (Tier II)",
                ChatColor.YELLOW + " • Unique playstyle"
            )
        );

        inv.setItem(
            22,
            createButton(
                Material.EXPERIENCE_BOTTLE,
                ChatColor.AQUA + "Energy System",
                "",
                ChatColor.GRAY + "Energy powers your abilities.",
                ChatColor.GRAY + "Gain energy from kills and crystals.",
                ChatColor.GRAY + "Lose energy from deaths.",
                "",
                ChatColor.RED + "At 0 energy: DEPLETED",
                ChatColor.GRAY + "You cannot use abilities!"
            )
        );

        inv.setItem(
            24,
            createButton(
                Material.NETHER_STAR,
                ChatColor.GOLD + "Getting Started",
                "",
                ChatColor.GRAY + "You start with a random essence.",
                ChatColor.GRAY + "Practice your abilities and learn",
                ChatColor.GRAY + "your playstyle!",
                "",
                ChatColor.GREEN +
                    "Tip: Use " +
                    ChatColor.WHITE +
                    "/essence info",
                ChatColor.GREEN + "to see your current stats."
            )
        );
    }

    private void populateEssencePage(Inventory inv) {
        inv.setItem(
            4,
            createButton(
                Material.ENCHANTED_BOOK,
                ChatColor.LIGHT_PURPLE + "" + ChatColor.BOLD + "Essence Types",
                "",
                ChatColor.GRAY + "There are 8 unique essences:"
            )
        );

        inv.setItem(
            10,
            createButton(
                Material.ENDER_PEARL,
                ChatColor.DARK_PURPLE + "Void Essence",
                "",
                ChatColor.GRAY + "Pull enemies and execute them",
                ChatColor.YELLOW + "Primary: Singularity Pull",
                ChatColor.YELLOW + "Secondary: Void Guillotine",
                ChatColor.GRAY + "Execute targets below 40% HP!"
            )
        );

        inv.setItem(
            11,
            createButton(
                Material.BLAZE_POWDER,
                ChatColor.GOLD + "Inferno Essence",
                "",
                ChatColor.GRAY + "Fire damage and flame mines",
                ChatColor.YELLOW + "Tier II: Death prevention"
            )
        );

        inv.setItem(
            12,
            createButton(
                Material.TOTEM_OF_UNDYING,
                ChatColor.GOLD + "" + ChatColor.BOLD + "DIVINE ESSENCE",
                "",
                ChatColor.GRAY + "The ultimate power - all essences",
                ChatColor.GRAY + "combined with the Dragon's might",
                "",
                ChatColor.RED + "" + ChatColor.BOLD + "OVERPOWERED",
                ChatColor.DARK_RED + "Requires all 7 essences + Dragon Egg"
            )
        );

        inv.setItem(
            13,
            createButton(
                Material.BEACON,
                ChatColor.DARK_AQUA + "Oracle Essence",
                "",
                ChatColor.GRAY + "Vision and enemy debuffs",
                ChatColor.YELLOW + "Track and curse enemies"
            )
        );

        inv.setItem(
            14,
            createButton(
                Material.PHANTOM_MEMBRANE,
                ChatColor.DARK_GRAY + "Phantom Essence",
                "",
                ChatColor.GRAY + "Stealth and assassination",
                ChatColor.YELLOW + "Execute low-health targets"
            )
        );

        inv.setItem(
            15,
            createButton(
                Material.OAK_SAPLING,
                ChatColor.GREEN + "Nature Essence",
                "",
                ChatColor.GRAY + "Poison and life-steal",
                ChatColor.YELLOW + "Primal Surge for sustain"
            )
        );

        inv.setItem(
            16,
            createButton(
                Material.BEDROCK,
                ChatColor.GRAY + "Titan Essence",
                "",
                ChatColor.GRAY + "Tank and crowd control",
                ChatColor.YELLOW + "Primary: Seismic Slam",
                ChatColor.YELLOW + "Secondary: Colossus Form",
                ChatColor.GRAY + "Become an unstoppable force!"
            )
        );

        inv.setItem(
            19,
            createButton(
                Material.NETHER_STAR,
                ChatColor.LIGHT_PURPLE + "Arcane Essence",
                "",
                ChatColor.GRAY + "Randomized powerful effects",
                ChatColor.YELLOW + "Roll the dice for buffs/debuffs",
                ChatColor.RED + "High risk, high reward!"
            )
        );

        inv.setItem(
            28,
            createButton(
                Material.BOOK,
                ChatColor.AQUA + "Void Details",
                "",
                ChatColor.YELLOW + "Singularity Pull:",
                ChatColor.GRAY + "Pull nearest enemy toward you",
                ChatColor.GRAY + "Deal damage on collision",
                "",
                ChatColor.YELLOW + "Void Guillotine (Tier II):",
                ChatColor.GRAY + "Mark enemy with delayed strike",
                ChatColor.GRAY + "Instant kill if below 40% HP!",
                ChatColor.GRAY + "Otherwise deals heavy damage"
            )
        );

        inv.setItem(
            34,
            createButton(
                Material.ANVIL,
                ChatColor.AQUA + "Titan Details",
                "",
                ChatColor.YELLOW + "Seismic Slam:",
                ChatColor.GRAY + "Ground-slam in a line",
                ChatColor.GRAY + "Knockback and slow enemies",
                "",
                ChatColor.YELLOW + "Colossus Form (Tier II):",
                ChatColor.GRAY + "8s of massive defense",
                ChatColor.GRAY + "Immune to knockback & debuffs",
                ChatColor.RED + "But you move slower!"
            )
        );
    }

    private void populateAbilitiesPage(Inventory inv) {
        inv.setItem(
            4,
            createButton(
                Material.DIAMOND_SWORD,
                ChatColor.LIGHT_PURPLE +
                    "" +
                    ChatColor.BOLD +
                    "Using Abilities",
                "",
                ChatColor.GRAY + "Essences have two abilities:"
            )
        );

        inv.setItem(
            20,
            createButton(
                Material.IRON_SWORD,
                ChatColor.YELLOW + "Primary Ability",
                "",
                ChatColor.GRAY + "Available at Tier I (always)",
                ChatColor.GRAY + "Your main combat tool",
                "",
                ChatColor.GREEN + "Use with:",
                ChatColor.WHITE + " • Offhand key (F) while standing",
                ChatColor.WHITE + " • /essence primary",
                ChatColor.WHITE + " • Hotkey binding"
            )
        );

        inv.setItem(
            22,
            createButton(
                Material.BOOK,
                ChatColor.AQUA + "Cooldowns",
                "",
                ChatColor.GRAY + "Abilities have cooldowns.",
                ChatColor.GRAY + "Your energy state affects cooldowns:",
                "",
                ChatColor.GREEN + "High energy = Faster cooldowns",
                ChatColor.RED + "Low energy = Slower cooldowns",
                "",
                ChatColor.YELLOW + "Depleted = Cannot use abilities!"
            )
        );

        inv.setItem(
            24,
            createButton(
                Material.DIAMOND_SWORD,
                ChatColor.LIGHT_PURPLE + "Secondary Ability",
                "",
                ChatColor.GRAY + "Requires Tier II upgrade",
                ChatColor.GRAY + "More powerful ultimate ability",
                "",
                ChatColor.GREEN + "Use with:",
                ChatColor.WHITE + " • Offhand key (F) while sneaking",
                ChatColor.WHITE + " • /essence secondary",
                ChatColor.WHITE + " • Hotkey binding"
            )
        );

        inv.setItem(
            31,
            createButton(
                Material.WRITABLE_BOOK,
                ChatColor.GOLD + "Hotkey Binding",
                "",
                ChatColor.GRAY + "Create aliases for quick casting:",
                ChatColor.WHITE + "/essence hotkey primary [name]",
                ChatColor.WHITE + "/essence hotkey secondary [name]",
                "",
                ChatColor.GRAY + "Then bind the alias in Minecraft",
                ChatColor.GRAY + "keybinds to any key!"
            )
        );
    }

    private void populateEnergyPage(Inventory inv) {
        inv.setItem(
            4,
            createButton(
                Material.EXPERIENCE_BOTTLE,
                ChatColor.LIGHT_PURPLE + "" + ChatColor.BOLD + "Energy System",
                "",
                ChatColor.GRAY + "Energy is your power source"
            )
        );

        inv.setItem(
            19,
            createButton(
                Material.EMERALD,
                ChatColor.GREEN + "Gaining Energy",
                "",
                ChatColor.YELLOW + "+1 energy per kill",
                ChatColor.YELLOW + "+1 energy from crystals",
                "",
                ChatColor.GRAY + "Max energy: 10",
                ChatColor.GRAY + "Starting energy: 3-5"
            )
        );

        inv.setItem(
            21,
            createButton(
                Material.REDSTONE,
                ChatColor.RED + "Losing Energy",
                "",
                ChatColor.YELLOW + "-1 energy per death",
                "",
                ChatColor.RED + "At 0 energy: DEPLETED",
                ChatColor.GRAY + "Cannot use abilities!",
                ChatColor.GRAY + "Heavily reduced damage multiplier"
            )
        );

        inv.setItem(
            23,
            createButton(
                Material.GLOWSTONE_DUST,
                ChatColor.AQUA + "Energy States",
                "",
                ChatColor.LIGHT_PURPLE + "10+ Energy: Radiant +",
                ChatColor.GRAY + "  Max damage, fast cooldowns",
                "",
                ChatColor.GREEN + "5-9 Energy: Radiant/Dimmed",
                ChatColor.GRAY + "  Normal effectiveness",
                "",
                ChatColor.YELLOW + "3-4 Energy: Fractured",
                ChatColor.GRAY + "  Standard performance",
                "",
                ChatColor.GOLD + "1-2 Energy: Fading/Fragile",
                ChatColor.GRAY + "  Reduced effectiveness",
                "",
                ChatColor.RED + "0 Energy: DEPLETED",
                ChatColor.GRAY + "  Cannot use abilities!"
            )
        );

        inv.setItem(
            25,
            createButton(
                Material.NETHER_STAR,
                ChatColor.YELLOW + "Energy Crystals",
                "",
                ChatColor.GRAY + "Withdraw energy as crystals:",
                ChatColor.WHITE + "/essence withdraw",
                "",
                ChatColor.GRAY + "Others can pick up your crystals!",
                ChatColor.GRAY + "Use them strategically."
            )
        );

        inv.setItem(
            31,
            createButton(
                Material.DIAMOND_SWORD,
                ChatColor.RED + "PvP & Combat",
                "",
                ChatColor.GRAY + "EssenceWars features competitive PvP!",
                "",
                ChatColor.YELLOW + "On Death:",
                ChatColor.RED + " • Lose 1 energy",
                ChatColor.RED + " • Drop your essence",
                "",
                ChatColor.GREEN + "On Kill:",
                ChatColor.GREEN + " • Gain 1 energy",
                ChatColor.GREEN + " • Claim dropped essences",
                "",
                ChatColor.GRAY + "Team protection: No friendly fire!"
            )
        );
    }

    private void populateCommandsPage(Inventory inv) {
        inv.setItem(
            4,
            createButton(
                Material.COMMAND_BLOCK,
                ChatColor.LIGHT_PURPLE +
                    "" +
                    ChatColor.BOLD +
                    "Essential Commands",
                "",
                ChatColor.GRAY + "Commands you need to know:"
            )
        );

        inv.setItem(
            19,
            createButton(
                Material.PAPER,
                ChatColor.YELLOW + "/essence info",
                "",
                ChatColor.GRAY + "View your current essence,",
                ChatColor.GRAY + "tier, energy, and state"
            )
        );

        inv.setItem(
            20,
            createButton(
                Material.WRITABLE_BOOK,
                ChatColor.YELLOW + "/essence primary",
                ChatColor.YELLOW + "/essence secondary",
                "",
                ChatColor.GRAY + "Cast your abilities via command",
                ChatColor.GRAY + "or use offhand key (F)!"
            )
        );

        inv.setItem(
            21,
            createButton(
                Material.NETHER_STAR,
                ChatColor.YELLOW + "/essence withdraw",
                "",
                ChatColor.GRAY + "Convert 1 energy into a crystal",
                ChatColor.GRAY + "that can be picked up"
            )
        );

        inv.setItem(
            22,
            createButton(
                Material.BOOK,
                ChatColor.YELLOW + "/essence config",
                "",
                ChatColor.GRAY + "Open the settings GUI",
                ChatColor.GRAY + "to customize your experience"
            )
        );

        inv.setItem(
            23,
            createButton(
                Material.NAME_TAG,
                ChatColor.YELLOW + "/essence team",
                "",
                ChatColor.GRAY + "Create and manage teams:",
                ChatColor.WHITE + " • /essence team create <name>",
                ChatColor.WHITE + " • /essence team join <name>",
                ChatColor.WHITE + " • /essence team leave",
                ChatColor.WHITE + " • /essence team info"
            )
        );

        inv.setItem(
            24,
            createButton(
                Material.COMPASS,
                ChatColor.YELLOW + "/essence hotkey",
                "",
                ChatColor.GRAY + "Bind abilities to custom keys:",
                ChatColor.WHITE + "/essence hotkey primary [name]",
                "",
                ChatColor.GRAY + "Then bind in Minecraft keybinds!"
            )
        );

        inv.setItem(
            31,
            createButton(
                Material.EMERALD,
                ChatColor.GREEN + "" + ChatColor.BOLD + "You're Ready!",
                "",
                ChatColor.GRAY + "You now know the basics of",
                ChatColor.GRAY + "EssenceWars. Good luck!",
                "",
                ChatColor.YELLOW + "Tips:",
                ChatColor.WHITE + " • Practice your abilities",
                ChatColor.WHITE + " • Manage your energy carefully",
                ChatColor.WHITE + " • Learn your essence's strengths",
                ChatColor.WHITE + " • Team up with friends",
                "",
                ChatColor.GOLD + "Have fun and dominate!"
            )
        );
    }

    private ItemStack createButton(
        Material material,
        String name,
        String... lore
    ) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(name);
            if (lore.length > 0) {
                meta.setLore(Arrays.asList(lore));
            }
            item.setItemMeta(meta);
        }
        return item;
    }

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) {
            return;
        }

        String title = event.getView().getTitle();
        if (!title.startsWith(TITLE)) {
            return;
        }

        event.setCancelled(true);

        ItemStack clicked = event.getCurrentItem();
        if (clicked == null || clicked.getType() == Material.AIR) {
            return;
        }

        // Extract current page from title
        int currentPage = 0;
        try {
            String pageStr = title.substring(title.lastIndexOf(" ") + 1);
            currentPage = Integer.parseInt(pageStr) - 1;
        } catch (Exception ignored) {}

        int slot = event.getSlot();

        // Next page
        if (slot == 53 && currentPage < 4) {
            open(player, currentPage + 1);
            player.playSound(
                player.getLocation(),
                org.bukkit.Sound.UI_BUTTON_CLICK,
                1.0f,
                1.0f
            );
        }
        // Previous page
        else if (slot == 45 && currentPage > 0) {
            open(player, currentPage - 1);
            player.playSound(
                player.getLocation(),
                org.bukkit.Sound.UI_BUTTON_CLICK,
                1.0f,
                1.0f
            );
        }
        // Close
        else if (slot == 49) {
            player.closeInventory();
            player.sendMessage(
                ChatColor.LIGHT_PURPLE +
                    "Tutorial closed. Reopen with " +
                    ChatColor.WHITE +
                    "/essence config"
            );

            // Mark tutorial as seen
            plugin
                .getPlayerDataManager()
                .getOrCreate(player)
                .setTutorialSeen(true);
        }
    }
}
