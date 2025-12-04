package com.essencewars.ui;

import com.essencewars.EssenceWarsPlugin;
import com.essencewars.essence.EssenceType;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class InfuseRecipesGUI implements Listener {

    private final EssenceWarsPlugin plugin;
    private static final String TITLE = ChatColor.LIGHT_PURPLE + "INFUSE " + ChatColor.GOLD + "RECIPES";

    public InfuseRecipesGUI(EssenceWarsPlugin plugin) {
        this.plugin = plugin;
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    public void open(Player player) {
        openCategory(player, "all");
    }

    public void openCategory(Player player, String category) {
        Inventory inv = Bukkit.createInventory(null, 54, TITLE);

        if (category.equals("all")) {
            // Show all essence types as clickable sections
            displayAllEssencesList(inv, player);
        } else {
            // Display recipes for the specific category
            displayRecipes(inv, category, player);
        }

        // Close button
        inv.setItem(49, createButton(Material.BARRIER, ChatColor.RED + "Close", ""));

        player.openInventory(inv);
    }

    private void displayAllEssencesList(Inventory inv, Player player) {
        // Display all essence types as individual clickable cards
        int[] slots = {10, 11, 12, 13, 14, 15, 16, 19, 20, 21, 22, 23, 24, 25};
        int slotIndex = 0;

        for (EssenceType type : EssenceType.values()) {
            if (slotIndex >= slots.length) break;

            Material icon = getEssenceIcon(type);
            ItemStack card = createEssenceCard(type, icon, player);
            inv.setItem(slots[slotIndex], card);
            slotIndex++;
        }

        // Add back button
        inv.setItem(48, createButton(Material.ARROW, ChatColor.YELLOW + "← Back", ChatColor.GRAY + "Return to main menu"));
    }

    private void displayRecipes(Inventory inv, String category, Player player) {
        // Show individual essence recipes with craft buttons
        EssenceType[] essencesToShow = getEssencesForCategory(category);

        int startSlot = 10;
        for (int i = 0; i < essencesToShow.length && i < 2; i++) {
            EssenceType type = essencesToShow[i];
            int recipeSlot = startSlot + (i * 4);

            // Display recipe grid
            displayRecipeGrid(inv, recipeSlot, plugin.getCraftManager().getRecipeDisplay(type));

            // Arrow
            inv.setItem(recipeSlot + 3, createArrow());

            // Result item
            Material icon = getEssenceIcon(type);
            inv.setItem(recipeSlot + 3 + 9, plugin.getCraftManager().createEssenceCraft(type, icon));

            // Craft button below result
            inv.setItem(recipeSlot + 3 + 18, createCraftButton(type, player));
        }
    }

    private EssenceType[] getEssencesForCategory(String category) {
        return switch (category.toLowerCase()) {
            case "combat" -> new EssenceType[]{EssenceType.VOID, EssenceType.PHANTOM, EssenceType.TITAN};
            case "healing" -> new EssenceType[]{EssenceType.NATURE};
            case "utility" -> new EssenceType[]{EssenceType.ORACLE, EssenceType.ARCANE};
            case "magic" -> new EssenceType[]{EssenceType.ARCANE, EssenceType.ORACLE};
            case "nature" -> new EssenceType[]{EssenceType.NATURE};
            case "divine" -> new EssenceType[]{EssenceType.DIVINE};
            default -> new EssenceType[]{};
        };
    }

    private Material getEssenceIcon(EssenceType type) {
        return switch (type) {
            case VOID -> Material.ENDER_PEARL;
            case INFERNO -> Material.BLAZE_ROD;
            case NATURE -> Material.OAK_SAPLING;
            case ORACLE -> Material.BEACON;
            case PHANTOM -> Material.PHANTOM_MEMBRANE;
            case TITAN -> Material.ANVIL;
            case ARCANE -> Material.ENCHANTED_BOOK;
            case DIVINE -> Material.TOTEM_OF_UNDYING;
        };
    }

    private ItemStack createEssenceCard(EssenceType type, Material icon, Player player) {
        ItemStack card = new ItemStack(icon);
        ItemMeta meta = card.getItemMeta();
        if (meta != null) {
            String displayName = type == EssenceType.DIVINE
                ? ChatColor.GOLD + "" + ChatColor.BOLD + type.getDisplayName()
                : ChatColor.LIGHT_PURPLE + type.getDisplayName() + " Essence";

            meta.setDisplayName(displayName);

            List<String> lore = new ArrayList<>();
            lore.add("");
            lore.add(ChatColor.GRAY + "Click to view recipe");
            lore.add(ChatColor.GRAY + "and craft this essence!");
            lore.add("");

            // Show ownership info
            int ownerCount = plugin.getEssenceOwnerCount(type);
            int maxOwners = (type == EssenceType.DIVINE) ? 1 : 3;

            if (ownerCount >= maxOwners) {
                lore.add(ChatColor.RED + "Max owners: " + ownerCount + "/" + maxOwners);
            } else {
                lore.add(ChatColor.GREEN + "Owners: " + ownerCount + "/" + maxOwners);
            }
            lore.add(ChatColor.YELLOW + "Click to craft!");

            meta.setLore(lore);
            meta.getPersistentDataContainer().set(
                new NamespacedKey(plugin, "essence_card"),
                PersistentDataType.STRING,
                type.name()
            );
            card.setItemMeta(meta);
        }
        return card;
    }

    private ItemStack createCraftButton(EssenceType type, Player player) {
        Material buttonMaterial = Material.LIME_CONCRETE;
        String buttonName = ChatColor.GREEN + "Craft " + type.getDisplayName();

        ItemStack button = new ItemStack(buttonMaterial);
        ItemMeta meta = button.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(buttonName);

            List<String> lore = new ArrayList<>();
            lore.add(ChatColor.GRAY + "Click to craft this essence");
            lore.add(ChatColor.GRAY + "if you have the materials!");
            lore.add("");
            lore.add(ChatColor.YELLOW + "Materials will be consumed");

            meta.setLore(lore);
            meta.getPersistentDataContainer().set(
                new NamespacedKey(plugin, "craft_button"),
                PersistentDataType.STRING,
                type.name()
            );
            button.setItemMeta(meta);
        }
        return button;
    }

    private void displayRecipeGrid(Inventory inv, int startSlot, ItemStack[] recipe) {
        // Display 3x3 crafting grid starting at startSlot
        // Slots are arranged: 0 1 2
        //                     3 4 5
        //                     6 7 8
        // In inventory: startSlot, startSlot+1, startSlot+2
        //               startSlot+9, startSlot+10, startSlot+11
        //               startSlot+18, startSlot+19, startSlot+20

        if (recipe.length >= 9) {
            inv.setItem(startSlot, recipe[0]);
            inv.setItem(startSlot + 1, recipe[1]);
            inv.setItem(startSlot + 2, recipe[2]);
            inv.setItem(startSlot + 9, recipe[3]);
            inv.setItem(startSlot + 10, recipe[4]);
            inv.setItem(startSlot + 11, recipe[5]);
            inv.setItem(startSlot + 18, recipe[6]);
            inv.setItem(startSlot + 19, recipe[7]);
            inv.setItem(startSlot + 20, recipe[8]);
        }
    }

    private ItemStack createArrow() {
        return createButton(Material.ARROW, ChatColor.YELLOW + "→", ChatColor.GRAY + "Crafts into");
    }

    private ItemStack createCategoryIcon(Material material, String name, String category) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(name);
            meta.setLore(Arrays.asList(
                "",
                ChatColor.GRAY + "Click to view " + name + " recipes",
                ChatColor.DARK_GRAY + "Category: " + category
            ));
            item.setItemMeta(meta);
        }
        return item;
    }

    private ItemStack createButton(Material material, String name, String... lore) {
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
        if (!title.equals(TITLE)) {
            return;
        }

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

        // Back button
        if (slot == 48) {
            openCategory(player, "all");
            player.playSound(player.getLocation(), org.bukkit.Sound.UI_BUTTON_CLICK, 1.0f, 1.0f);
            return;
        }

        // Check for essence card click
        ItemMeta meta = clicked.getItemMeta();
        if (meta != null) {
            NamespacedKey cardKey = new NamespacedKey(plugin, "essence_card");
            if (meta.getPersistentDataContainer().has(cardKey, PersistentDataType.STRING)) {
                String typeStr = meta.getPersistentDataContainer().get(cardKey, PersistentDataType.STRING);
                try {
                    EssenceType type = EssenceType.valueOf(typeStr);
                    openEssenceDetailView(player, type);
                    player.playSound(player.getLocation(), org.bukkit.Sound.UI_BUTTON_CLICK, 1.0f, 1.0f);
                } catch (Exception e) {
                    // Invalid type
                }
                return;
            }

            // Check for craft button click
            NamespacedKey craftKey = new NamespacedKey(plugin, "craft_button");
            if (meta.getPersistentDataContainer().has(craftKey, PersistentDataType.STRING)) {
                String typeStr = meta.getPersistentDataContainer().get(craftKey, PersistentDataType.STRING);
                try {
                    EssenceType type = EssenceType.valueOf(typeStr);
                    handleCraftClick(player, type);
                } catch (Exception e) {
                    // Invalid type
                }
                return;
            }
        }
    }

    private void openEssenceDetailView(Player player, EssenceType type) {
        Inventory inv = Bukkit.createInventory(null, 54, TITLE);

        // Display the recipe grid
        displayRecipeGrid(inv, 10, plugin.getCraftManager().getRecipeDisplay(type));

        // Arrow
        inv.setItem(13, createArrow());

        // Result
        Material icon = getEssenceIcon(type);
        inv.setItem(22, plugin.getCraftManager().createEssenceCraft(type, icon));

        // Craft button
        inv.setItem(31, createCraftButton(type, player));

        // Info
        inv.setItem(40, createButton(Material.BOOK, ChatColor.YELLOW + "Recipe Info",
            ChatColor.GRAY + "Craft this recipe in a",
            ChatColor.GRAY + "crafting table, or click",
            ChatColor.GRAY + "the button above to craft!"));

        // Back button
        inv.setItem(48, createButton(Material.ARROW, ChatColor.YELLOW + "← Back", ChatColor.GRAY + "Return to essence list"));

        // Close button
        inv.setItem(49, createButton(Material.BARRIER, ChatColor.RED + "Close", ""));

        player.openInventory(inv);
    }

    private void handleCraftClick(Player player, EssenceType type) {
        // Check if player has materials and consume them
        if (consumeRecipeMaterials(player, type)) {
            Material icon = getEssenceIcon(type);
            ItemStack craft = plugin.getCraftManager().createEssenceCraft(type, icon);
            player.getInventory().addItem(craft);
            player.sendMessage(ChatColor.GREEN + "Successfully crafted " + type.getDisplayName() + " Essence!");
            player.playSound(player.getLocation(), org.bukkit.Sound.BLOCK_ANVIL_USE, 1.0f, 1.2f);
            player.closeInventory();
        } else {
            player.sendMessage(ChatColor.RED + "You don't have the required materials!");
            player.playSound(player.getLocation(), org.bukkit.Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
        }
    }

    private boolean consumeRecipeMaterials(Player player, EssenceType type) {
        ItemStack[] recipe = plugin.getCraftManager().getRecipeDisplay(type);
        org.bukkit.inventory.Inventory inv = player.getInventory();

        // First check if player has all materials
        java.util.Map<Material, Integer> required = new java.util.HashMap<>();
        for (ItemStack item : recipe) {
            if (item != null && item.getType() != Material.AIR) {
                // For essence crafts in DIVINE recipe, check if they're special essence items
                if (plugin.getCraftManager().isEssenceCraft(item)) {
                    // Check if player has this specific essence craft
                    boolean found = false;
                    for (ItemStack playerItem : inv.getContents()) {
                        if (plugin.getCraftManager().isEssenceCraft(playerItem) &&
                            plugin.getCraftManager().getEssenceType(playerItem) == plugin.getCraftManager().getEssenceType(item)) {
                            found = true;
                            break;
                        }
                    }
                    if (!found) return false;
                } else {
                    required.put(item.getType(), required.getOrDefault(item.getType(), 0) + item.getAmount());
                }
            }
        }

        // Check if player has all required materials
        for (java.util.Map.Entry<Material, Integer> entry : required.entrySet()) {
            if (!inv.containsAtLeast(new ItemStack(entry.getKey()), entry.getValue())) {
                return false;
            }
        }

        // Consume materials
        for (java.util.Map.Entry<Material, Integer> entry : required.entrySet()) {
            int toRemove = entry.getValue();
            ItemStack[] contents = inv.getContents();
            for (int i = 0; i < contents.length; i++) {
                ItemStack item = contents[i];
                if (item != null && item.getType() == entry.getKey()) {
                    int amount = item.getAmount();
                    if (amount <= toRemove) {
                        inv.setItem(i, null);
                        toRemove -= amount;
                    } else {
                        item.setAmount(amount - toRemove);
                        toRemove = 0;
                    }
                    if (toRemove == 0) break;
                }
            }
        }

        // Consume essence crafts for DIVINE recipe
        for (ItemStack recipeItem : recipe) {
            if (recipeItem != null && plugin.getCraftManager().isEssenceCraft(recipeItem)) {
                EssenceType requiredType = plugin.getCraftManager().getEssenceType(recipeItem);
                for (int i = 0; i < inv.getSize(); i++) {
                    ItemStack playerItem = inv.getItem(i);
                    if (plugin.getCraftManager().isEssenceCraft(playerItem) &&
                        plugin.getCraftManager().getEssenceType(playerItem) == requiredType) {
                        inv.setItem(i, null);
                        break;
                    }
                }
            }
        }

        return true;
    }
}
