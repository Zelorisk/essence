package com.essencewars.ui;

import com.essencewars.EssenceWarsPlugin;
import com.essencewars.essence.EssenceType;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class CraftingGuideGUI implements Listener {

    private final EssenceWarsPlugin plugin;
    private static final String TITLE = ChatColor.LIGHT_PURPLE + "Essence Crafting Guide";
    private static final String RECIPE_TITLE = ChatColor.LIGHT_PURPLE + "Recipe: ";

    public CraftingGuideGUI(EssenceWarsPlugin plugin) {
        this.plugin = plugin;
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    public void open(Player player) {
        Inventory inv = Bukkit.createInventory(null, 54, TITLE);

        // Title
        inv.setItem(4, createInfoItem(
            Material.ENCHANTED_BOOK,
            ChatColor.LIGHT_PURPLE + "" + ChatColor.BOLD + "Essence Crafting",
            "",
            ChatColor.GRAY + "Click any essence to view its recipe!",
            ChatColor.YELLOW + "Recipe GUIs show actual crafting patterns."
        ));

        // Display essence icons as clickable buttons
        inv.setItem(10, createEssenceButton(EssenceType.VOID, Material.ENDER_PEARL));
        inv.setItem(11, createEssenceButton(EssenceType.INFERNO, Material.BLAZE_ROD));
        inv.setItem(12, createEssenceButton(EssenceType.NATURE, Material.OAK_SAPLING));
        inv.setItem(13, createEssenceButton(EssenceType.ORACLE, Material.BEACON));
        inv.setItem(14, createEssenceButton(EssenceType.PHANTOM, Material.PHANTOM_MEMBRANE));
        inv.setItem(15, createEssenceButton(EssenceType.TITAN, Material.ANVIL));
        inv.setItem(16, createEssenceButton(EssenceType.ARCANE, Material.ENCHANTED_BOOK));

        // Divine essence (special)
        inv.setItem(22, createEssenceButton(EssenceType.DIVINE, Material.TOTEM_OF_UNDYING));

        // Info
        inv.setItem(49, createInfoItem(
            Material.BARRIER,
            ChatColor.RED + "Close",
            "",
            ChatColor.GRAY + "Click to close"
        ));

        player.openInventory(inv);
    }

    public void openRecipe(Player player, EssenceType type) {
        String typeName = type.getDisplayName();
        Inventory inv = Bukkit.createInventory(null, 54, RECIPE_TITLE + typeName);

        // Get recipe from craft manager
        ItemStack[] recipe = plugin.getCraftManager().getRecipeDisplay(type);

        // Display crafting grid (3x3) in center
        // Slots 10-12, 19-21, 28-30 form a 3x3 grid
        inv.setItem(10, recipe[0]);
        inv.setItem(11, recipe[1]);
        inv.setItem(12, recipe[2]);
        inv.setItem(19, recipe[3]);
        inv.setItem(20, recipe[4]);
        inv.setItem(21, recipe[5]);
        inv.setItem(28, recipe[6]);
        inv.setItem(29, recipe[7]);
        inv.setItem(30, recipe[8]);

        // Arrow
        inv.setItem(23, createInfoItem(
            Material.ARROW,
            ChatColor.YELLOW + "Crafts Into →",
            "",
            ChatColor.GRAY + "Place these items in a",
            ChatColor.GRAY + "crafting table to create:"
        ));

        // Result
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
        inv.setItem(24, plugin.getCraftManager().createEssenceCraft(type, icon));

        // Back button
        inv.setItem(45, createInfoItem(
            Material.ARROW,
            ChatColor.GREEN + "← Back to Essence List",
            "",
            ChatColor.GRAY + "Return to crafting guide"
        ));

        // Get craft item (admin only)
        if (player.hasPermission("essencewars.admin")) {
            inv.setItem(53, createInfoItem(
                Material.NETHER_STAR,
                ChatColor.GOLD + "Get Craft Item",
                "",
                ChatColor.GRAY + "Click to receive this craft",
                ChatColor.DARK_GRAY + "(Admin only)"
            ));
        }

        // Close button
        inv.setItem(49, createInfoItem(
            Material.BARRIER,
            ChatColor.RED + "Close",
            "",
            ChatColor.GRAY + "Click to close"
        ));

        player.openInventory(inv);
    }

    private ItemStack createEssenceButton(EssenceType type, Material icon) {
        ItemStack item = new ItemStack(icon);
        org.bukkit.inventory.meta.ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(type.getDisplayName() + ChatColor.GRAY + " Essence");
            meta.setLore(java.util.Arrays.asList(
                "",
                ChatColor.YELLOW + "Click to view recipe!",
                ChatColor.GRAY + "See the crafting pattern"
            ));
            item.setItemMeta(meta);
        }
        return item;
    }

    private ItemStack createInfoItem(Material material, String name, String... lore) {
        ItemStack item = new ItemStack(material);
        org.bukkit.inventory.meta.ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(name);
            if (lore.length > 0) {
                meta.setLore(java.util.Arrays.asList(lore));
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
        
        // Main crafting guide menu
        if (title.equals(TITLE)) {
            event.setCancelled(true);

            ItemStack clicked = event.getCurrentItem();
            if (clicked == null || clicked.getType() == Material.AIR) {
                return;
            }

            int slot = event.getSlot();

            // Close button
            if (slot == 49) {
                player.closeInventory();
                return;
            }

            // Essence buttons - open recipe GUIs
            EssenceType type = null;
            switch (slot) {
                case 10 -> type = EssenceType.VOID;
                case 11 -> type = EssenceType.INFERNO;
                case 12 -> type = EssenceType.NATURE;
                case 13 -> type = EssenceType.ORACLE;
                case 14 -> type = EssenceType.PHANTOM;
                case 15 -> type = EssenceType.TITAN;
                case 16 -> type = EssenceType.ARCANE;
                case 22 -> type = EssenceType.DIVINE;
            }

            if (type != null) {
                openRecipe(player, type);
                player.playSound(player.getLocation(), org.bukkit.Sound.UI_BUTTON_CLICK, 1.0f, 1.0f);
            }
        }
        // Recipe view menu
        else if (title.startsWith(RECIPE_TITLE)) {
            event.setCancelled(true);

            ItemStack clicked = event.getCurrentItem();
            if (clicked == null || clicked.getType() == Material.AIR) {
                return;
            }

            int slot = event.getSlot();

            // Back button
            if (slot == 45) {
                open(player);
                player.playSound(player.getLocation(), org.bukkit.Sound.UI_BUTTON_CLICK, 1.0f, 1.0f);
                return;
            }

            // Close button
            if (slot == 49) {
                player.closeInventory();
                return;
            }

            // Get craft item (admin only)
            if (slot == 53 && player.hasPermission("essencewars.admin")) {
                // Extract essence type from title
                String essenceName = title.substring(RECIPE_TITLE.length()).replace(" Essence", "").trim();
                EssenceType type = getTypeFromDisplayName(essenceName);
                
                if (type != null) {
                    if (!plugin.getCraftManager().hasEssenceCraft(player)) {
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
                        player.sendMessage(ChatColor.GREEN + "Gave you " + type.getDisplayName() + " Essence Craft!");
                        player.playSound(player.getLocation(), org.bukkit.Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.0f);
                    } else {
                        player.sendMessage(ChatColor.RED + "You already have an essence craft!");
                    }
                }
            }
        }
    }

    private EssenceType getTypeFromDisplayName(String displayName) {
        for (EssenceType type : EssenceType.values()) {
            if (ChatColor.stripColor(type.getDisplayName()).equalsIgnoreCase(displayName)) {
                return type;
            }
        }
        return null;
    }
}
