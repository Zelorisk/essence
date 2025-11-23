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

public class AdminConfigGUI {
    
    private final EssenceWarsPlugin plugin;
    private static final String TITLE = ChatColor.DARK_PURPLE + "Admin Config";
    
    public AdminConfigGUI(EssenceWarsPlugin plugin) {
        this.plugin = plugin;
    }
    
    public void open(Player player) {
        if (!player.isOp()) {
            player.sendMessage(ChatColor.RED + "You must be an operator to access this!");
            return;
        }
        
        Inventory inv = Bukkit.createInventory(null, 27, TITLE);
        
        // Cooldown editor
        ItemStack cooldowns = new ItemStack(Material.CLOCK);
        ItemMeta cooldownMeta = cooldowns.getItemMeta();
        if (cooldownMeta != null) {
            cooldownMeta.setDisplayName(ChatColor.GOLD + "Edit Ability Cooldowns");
            cooldownMeta.setLore(Arrays.asList(
                ChatColor.GRAY + "Click to edit cooldown times",
                ChatColor.GRAY + "for all essence abilities"
            ));
            cooldowns.setItemMeta(cooldownMeta);
        }
        inv.setItem(11, cooldowns);
        
        // Recipe editor
        ItemStack recipes = new ItemStack(Material.CRAFTING_TABLE);
        ItemMeta recipeMeta = recipes.getItemMeta();
        if (recipeMeta != null) {
            recipeMeta.setDisplayName(ChatColor.GOLD + "Edit Essence Recipes");
            recipeMeta.setLore(Arrays.asList(
                ChatColor.GRAY + "Click to edit crafting recipes",
                ChatColor.GRAY + "for essence items"
            ));
            recipes.setItemMeta(recipeMeta);
        }
        inv.setItem(15, recipes);
        
        player.openInventory(inv);
    }
    
    public void openCooldownEditor(Player player) {
        if (!player.isOp()) {
            player.sendMessage(ChatColor.RED + "You must be an operator to access this!");
            return;
        }
        
        Inventory inv = Bukkit.createInventory(null, 54, ChatColor.DARK_PURPLE + "Cooldown Editor");
        
        int slot = 10;
        for (EssenceType type : EssenceType.values()) {
            ItemStack item = getEssenceIcon(type);
            ItemMeta meta = item.getItemMeta();
            if (meta != null) {
                int primaryCd = plugin.getConfig().getInt("cooldowns." + type.getId() + ".primary", 10);
                int secondaryCd = plugin.getConfig().getInt("cooldowns." + type.getId() + ".secondary", 20);
                
                meta.setDisplayName(getEssenceColor(type) + type.getDisplayName() + " Essence");
                meta.setLore(Arrays.asList(
                    ChatColor.GRAY + "Primary Cooldown: " + ChatColor.YELLOW + primaryCd + "s",
                    ChatColor.GRAY + "Secondary Cooldown: " + ChatColor.YELLOW + secondaryCd + "s",
                    "",
                    ChatColor.GREEN + "Left Click: " + ChatColor.GRAY + "Decrease Primary (-1s)",
                    ChatColor.GREEN + "Right Click: " + ChatColor.GRAY + "Increase Primary (+1s)",
                    ChatColor.GREEN + "Shift+Left: " + ChatColor.GRAY + "Decrease Secondary (-1s)",
                    ChatColor.GREEN + "Shift+Right: " + ChatColor.GRAY + "Increase Secondary (+1s)"
                ));
                item.setItemMeta(meta);
            }
            inv.setItem(slot, item);
            slot++;
            if (slot == 17) slot = 19;
            if (slot == 26) slot = 28;
        }
        
        // Back button
        ItemStack back = new ItemStack(Material.ARROW);
        ItemMeta backMeta = back.getItemMeta();
        if (backMeta != null) {
            backMeta.setDisplayName(ChatColor.RED + "Back");
            back.setItemMeta(backMeta);
        }
        inv.setItem(49, back);
        
        player.openInventory(inv);
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
    
    public void adjustCooldown(Player player, EssenceType type, boolean primary, int change) {
        String path = "cooldowns." + type.getId() + "." + (primary ? "primary" : "secondary");
        int current = plugin.getConfig().getInt(path, 10);
        int newValue = Math.max(0, current + change);
        
        plugin.getConfig().set(path, newValue);
        plugin.saveConfig();
        
        player.sendMessage(
            ChatColor.GREEN + "Set " + type.getDisplayName() + " " + 
            (primary ? "primary" : "secondary") + " cooldown to " + newValue + "s"
        );
        
        openCooldownEditor(player);
    }
}
