package com.essencewars.ui;

import com.essencewars.EssenceWarsPlugin;
import com.essencewars.essence.EssenceType;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class RecipeEditorGUI {
    
    private final EssenceWarsPlugin plugin;
    private static final String TITLE = ChatColor.DARK_PURPLE + "Recipe Editor";
    private static final String EDIT_TITLE = ChatColor.DARK_PURPLE + "Edit Recipe: ";
    
    // Store current recipe being edited per player
    private final Map<Player, ItemStack[]> editingRecipes = new HashMap<>();
    
    public RecipeEditorGUI(EssenceWarsPlugin plugin) {
        this.plugin = plugin;
    }
    
    public void open(Player player) {
        if (!player.isOp()) {
            player.sendMessage(ChatColor.RED + "You must be an operator to access this!");
            return;
        }
        
        Inventory inv = Bukkit.createInventory(null, 54, TITLE);
        
        int slot = 10;
        for (EssenceType type : EssenceType.values()) {
            ItemStack item = getEssenceIcon(type);
            ItemMeta meta = item.getItemMeta();
            if (meta != null) {
                meta.setDisplayName(getEssenceColor(type) + type.getDisplayName() + " Essence");
                meta.setLore(Arrays.asList(
                    ChatColor.GRAY + "Click to edit this recipe",
                    ChatColor.YELLOW + "Current recipe loaded from config"
                ));
                item.setItemMeta(meta);
            }
            inv.setItem(slot, item);
            slot++;
            if (slot == 17) slot = 19;
            if (slot == 26) slot = 28;
        }
        
        // Info
        ItemStack info = new ItemStack(Material.BOOK);
        ItemMeta infoMeta = info.getItemMeta();
        if (infoMeta != null) {
            infoMeta.setDisplayName(ChatColor.GOLD + "Recipe Editor");
            infoMeta.setLore(Arrays.asList(
                ChatColor.GRAY + "Click an essence to edit its recipe",
                ChatColor.GRAY + "Changes are saved to config.yml"
            ));
            info.setItemMeta(infoMeta);
        }
        inv.setItem(49, info);
        
        player.openInventory(inv);
    }
    
    public void openRecipeEdit(Player player, EssenceType type) {
        if (!player.isOp()) {
            player.sendMessage(ChatColor.RED + "You must be an operator to access this!");
            return;
        }
        
        Inventory inv = Bukkit.createInventory(null, 54, EDIT_TITLE + type.getDisplayName());
        
        // Load current recipe
        ItemStack[] recipe = plugin.getCraftManager().getRecipeDisplay(type);
        editingRecipes.put(player, recipe);
        
        // Display 3x3 crafting grid
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
        ItemStack arrow = new ItemStack(Material.ARROW);
        inv.setItem(23, arrow);
        
        // Result
        ItemStack result = plugin.getCraftManager().createEssenceCraft(type, recipe[4] != null ? recipe[4].getType() : Material.PAPER);
        inv.setItem(24, result);
        
        // Instructions
        ItemStack instructions = new ItemStack(Material.WRITABLE_BOOK);
        ItemMeta instrMeta = instructions.getItemMeta();
        if (instrMeta != null) {
            instrMeta.setDisplayName(ChatColor.GOLD + "Instructions");
            instrMeta.setLore(Arrays.asList(
                ChatColor.GRAY + "Place items in the 3x3 grid above",
                ChatColor.GRAY + "to define the recipe pattern",
                ChatColor.YELLOW + "Click SAVE when done!"
            ));
            instructions.setItemMeta(instrMeta);
        }
        inv.setItem(45, instructions);
        
        // Save button
        ItemStack save = new ItemStack(Material.EMERALD_BLOCK);
        ItemMeta saveMeta = save.getItemMeta();
        if (saveMeta != null) {
            saveMeta.setDisplayName(ChatColor.GREEN + "" + ChatColor.BOLD + "SAVE RECIPE");
            saveMeta.setLore(Arrays.asList(
                ChatColor.GRAY + "Click to save this recipe",
                ChatColor.GRAY + "to the config file"
            ));
            save.setItemMeta(saveMeta);
        }
        inv.setItem(48, save);
        
        // Cancel button
        ItemStack cancel = new ItemStack(Material.REDSTONE_BLOCK);
        ItemMeta cancelMeta = cancel.getItemMeta();
        if (cancelMeta != null) {
            cancelMeta.setDisplayName(ChatColor.RED + "" + ChatColor.BOLD + "CANCEL");
            cancelMeta.setLore(Arrays.asList(
                ChatColor.GRAY + "Discard changes and go back"
            ));
            cancel.setItemMeta(cancelMeta);
        }
        inv.setItem(50, cancel);
        
        player.openInventory(inv);
    }
    
    public void saveRecipe(Player player, EssenceType type, Inventory inv) {
        // Get items from crafting grid
        ItemStack[] recipe = new ItemStack[9];
        recipe[0] = inv.getItem(10);
        recipe[1] = inv.getItem(11);
        recipe[2] = inv.getItem(12);
        recipe[3] = inv.getItem(19);
        recipe[4] = inv.getItem(20);
        recipe[5] = inv.getItem(21);
        recipe[6] = inv.getItem(28);
        recipe[7] = inv.getItem(29);
        recipe[8] = inv.getItem(30);
        
        // Save to config
        String path = "recipes." + type.getId();
        for (int i = 0; i < 9; i++) {
            if (recipe[i] != null && recipe[i].getType() != Material.AIR) {
                plugin.getConfig().set(path + ".slot" + i, recipe[i].getType().name());
            } else {
                plugin.getConfig().set(path + ".slot" + i, null);
            }
        }
        
        plugin.saveConfig();
        editingRecipes.remove(player);
        
        player.sendMessage(ChatColor.GREEN + "Saved recipe for " + type.getDisplayName() + " Essence!");
        player.sendMessage(ChatColor.YELLOW + "Note: Server reload required for changes to take effect");
        player.closeInventory();
    }
    
    private ItemStack getEssenceIcon(EssenceType type) {
        return switch (type) {
            case VOID -> new ItemStack(Material.ENDER_PEARL);
            case INFERNO -> new ItemStack(Material.BLAZE_ROD);
            case NATURE -> new ItemStack(Material.OAK_SAPLING);
            case ORACLE -> new ItemStack(Material.BEACON);
            case PHANTOM -> new ItemStack(Material.PHANTOM_MEMBRANE);
            case TITAN -> new ItemStack(Material.ANVIL);
            case ARCANE -> new ItemStack(Material.ENCHANTED_BOOK);
            case DIVINE -> new ItemStack(Material.TOTEM_OF_UNDYING);
        };
    }
    
    private String getEssenceColor(EssenceType type) {
        return switch (type) {
            case VOID -> "§5";
            case INFERNO -> "§6";
            case NATURE -> "§a";
            case TITAN -> "§7";
            case PHANTOM -> "§8";
            case ORACLE -> "§b";
            case ARCANE -> "§d";
            case DIVINE -> "§6§l";
        };
    }
}
