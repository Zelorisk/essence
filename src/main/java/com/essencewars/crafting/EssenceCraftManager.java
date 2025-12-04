package com.essencewars.crafting;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import com.essencewars.EssenceWarsPlugin;
import com.essencewars.essence.EssenceType;

import java.util.Arrays;

public class EssenceCraftManager {

    private final EssenceWarsPlugin plugin;
    private NamespacedKey essenceKey;

    public EssenceCraftManager(EssenceWarsPlugin plugin) {
        this.plugin = plugin;
        this.essenceKey = new NamespacedKey(plugin, "essence_type");
    }

    public void registerRecipes() {
        // Void Essence - Teleportation/Darkness theme
        registerEssenceCraft(EssenceType.VOID, Material.ENDER_PEARL,
            "OOO", "OEO", "OOO",
            'O', Material.OBSIDIAN,
            'E', Material.ENDER_PEARL);

        // Inferno Essence - Fire/Heat theme
        registerEssenceCraft(EssenceType.INFERNO, Material.BLAZE_ROD,
            "NLN", "LBL", "NLN",
            'N', Material.NETHERRACK,
            'L', Material.LAVA_BUCKET,
            'B', Material.BLAZE_POWDER);

        // Nature Essence - Plants/Life theme
        registerEssenceCraft(EssenceType.NATURE, Material.OAK_SAPLING,
            "SWS", "WBW", "SWS",
            'S', Material.OAK_SAPLING,
            'W', Material.WHEAT,
            'B', Material.BONE_MEAL);

        // Oracle Essence - Knowledge/Vision theme
        registerEssenceCraft(EssenceType.ORACLE, Material.BEACON,
            "LAL", "AGA", "LAL",
            'L', Material.LAPIS_LAZULI,
            'A', Material.AMETHYST_SHARD,
            'G', Material.GLASS);

        // Phantom Essence - Shadow/Stealth theme
        registerEssenceCraft(EssenceType.PHANTOM, Material.PHANTOM_MEMBRANE,
            "CSC", "SPS", "CSC",
            'C', Material.COAL,
            'S', Material.SOUL_SAND,
            'P', Material.PHANTOM_MEMBRANE);

        // Titan Essence - Strength/Durability theme
        registerEssenceCraft(EssenceType.TITAN, Material.ANVIL,
            "III", "IDI", "III",
            'I', Material.IRON_BLOCK,
            'D', Material.DIAMOND);

        // Arcane Essence - Magic/Enchantment theme
        registerEssenceCraft(EssenceType.ARCANE, Material.ENCHANTED_BOOK,
            "LAL", "AEA", "LAL",
            'L', Material.LAPIS_LAZULI,
            'A', Material.AMETHYST_SHARD,
            'E', Material.ENCHANTED_BOOK);

        // Divine Essence - Requires ALL essences + Dragon Egg + Dragon Head
        registerDivineEssence();
    }

    private void registerEssenceCraft(EssenceType type, Material icon, String row1, String row2, String row3, Object... ingredients) {
        ItemStack result = createEssenceCraft(type, icon);
        
        NamespacedKey key = new NamespacedKey(plugin, "essence_" + type.getId());
        ShapedRecipe shaped = new ShapedRecipe(key, result);
        
        shaped.shape(row1, row2, row3);
        
        for (int i = 0; i < ingredients.length; i += 2) {
            char c = (Character) ingredients[i];
            Material mat = (Material) ingredients[i + 1];
            shaped.setIngredient(c, mat);
        }
        
        plugin.getServer().addRecipe(shaped);
    }

    private void registerDivineEssence() {
        // Note: Divine essence cannot use custom items in shaped recipes in vanilla Bukkit
        // This would require custom crafting handlers or alternative approaches
        // For now, we'll comment out the custom recipe and rely on the crafting listener
        
        // Alternative: Use EssenceCraftListener to handle Divine essence crafting
        // Players can manually combine essence crafts in a crafting table
        // and the listener will detect and handle it
    }

    public ItemStack createEssenceCraft(EssenceType type, Material icon) {
        ItemStack item = new ItemStack(icon);
        ItemMeta meta = item.getItemMeta();
        
        if (meta != null) {
            String displayName = type == EssenceType.DIVINE 
                ? ChatColor.GOLD + "" + ChatColor.BOLD + "DIVINE ESSENCE CRAFT"
                : ChatColor.LIGHT_PURPLE + type.getDisplayName() + " Essence Craft";
            
            meta.setDisplayName(displayName);
            
            String[] lore = type == EssenceType.DIVINE 
                ? new String[] {
                    ChatColor.GRAY + "The ultimate essence.",
                    ChatColor.GRAY + "Combines all essences with dragon power.",
                    "",
                    ChatColor.RED + "" + ChatColor.BOLD + "OVERPOWERED",
                    ChatColor.DARK_RED + "Use this to become unstoppable.",
                    "",
                    ChatColor.YELLOW + "Right-click to learn this essence!"
                }
                : new String[] {
                    ChatColor.GRAY + "A crafted essence shard.",
                    ChatColor.GRAY + "Right-click to learn the",
                    ChatColor.GRAY + type.getDisplayName() + " essence.",
                    "",
                    ChatColor.YELLOW + "You can only carry one craft at a time!"
                };
            
            meta.setLore(Arrays.asList(lore));
            meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_ENCHANTS);
            meta.getPersistentDataContainer().set(essenceKey, PersistentDataType.STRING, type.name());
            
            // Add enchant glint
            meta.addEnchant(org.bukkit.enchantments.Enchantment.DURABILITY, 1, true);
            
            item.setItemMeta(meta);
        }
        
        return item;
    }

    public boolean isEssenceCraft(ItemStack item) {
        if (item == null || !item.hasItemMeta()) {
            return false;
        }
        return item.getItemMeta().getPersistentDataContainer().has(essenceKey, PersistentDataType.STRING);
    }

    public EssenceType getEssenceType(ItemStack item) {
        if (!isEssenceCraft(item)) {
            return null;
        }
        String typeName = item.getItemMeta().getPersistentDataContainer().get(essenceKey, PersistentDataType.STRING);
        try {
            return EssenceType.valueOf(typeName);
        } catch (Exception e) {
            return null;
        }
    }

    public boolean hasEssenceCraft(org.bukkit.entity.Player player) {
        for (ItemStack item : player.getInventory().getContents()) {
            if (isEssenceCraft(item)) {
                return true;
            }
        }
        return isEssenceCraft(player.getInventory().getItemInOffHand());
    }

    /**
     * Get the recipe display for the crafting GUI
     * Returns a 3x3 grid of ItemStacks representing the recipe
     */
    public ItemStack[] getRecipeDisplay(EssenceType type) {
        ItemStack[] recipe = new ItemStack[9];

        switch (type) {
            case VOID:
                recipe[0] = new ItemStack(Material.OBSIDIAN);
                recipe[1] = new ItemStack(Material.OBSIDIAN);
                recipe[2] = new ItemStack(Material.OBSIDIAN);
                recipe[3] = new ItemStack(Material.OBSIDIAN);
                recipe[4] = new ItemStack(Material.ENDER_PEARL);
                recipe[5] = new ItemStack(Material.OBSIDIAN);
                recipe[6] = new ItemStack(Material.OBSIDIAN);
                recipe[7] = new ItemStack(Material.OBSIDIAN);
                recipe[8] = new ItemStack(Material.OBSIDIAN);
                break;

            case INFERNO:
                recipe[0] = new ItemStack(Material.NETHERRACK);
                recipe[1] = new ItemStack(Material.LAVA_BUCKET);
                recipe[2] = new ItemStack(Material.NETHERRACK);
                recipe[3] = new ItemStack(Material.LAVA_BUCKET);
                recipe[4] = new ItemStack(Material.BLAZE_POWDER);
                recipe[5] = new ItemStack(Material.LAVA_BUCKET);
                recipe[6] = new ItemStack(Material.NETHERRACK);
                recipe[7] = new ItemStack(Material.LAVA_BUCKET);
                recipe[8] = new ItemStack(Material.NETHERRACK);
                break;

            case NATURE:
                recipe[0] = new ItemStack(Material.OAK_SAPLING);
                recipe[1] = new ItemStack(Material.WHEAT);
                recipe[2] = new ItemStack(Material.OAK_SAPLING);
                recipe[3] = new ItemStack(Material.WHEAT);
                recipe[4] = new ItemStack(Material.BONE_MEAL);
                recipe[5] = new ItemStack(Material.WHEAT);
                recipe[6] = new ItemStack(Material.OAK_SAPLING);
                recipe[7] = new ItemStack(Material.WHEAT);
                recipe[8] = new ItemStack(Material.OAK_SAPLING);
                break;

            case ORACLE:
                recipe[0] = new ItemStack(Material.LAPIS_LAZULI);
                recipe[1] = new ItemStack(Material.AMETHYST_SHARD);
                recipe[2] = new ItemStack(Material.LAPIS_LAZULI);
                recipe[3] = new ItemStack(Material.AMETHYST_SHARD);
                recipe[4] = new ItemStack(Material.GLASS);
                recipe[5] = new ItemStack(Material.AMETHYST_SHARD);
                recipe[6] = new ItemStack(Material.LAPIS_LAZULI);
                recipe[7] = new ItemStack(Material.AMETHYST_SHARD);
                recipe[8] = new ItemStack(Material.LAPIS_LAZULI);
                break;

            case PHANTOM:
                recipe[0] = new ItemStack(Material.COAL);
                recipe[1] = new ItemStack(Material.SOUL_SAND);
                recipe[2] = new ItemStack(Material.COAL);
                recipe[3] = new ItemStack(Material.SOUL_SAND);
                recipe[4] = new ItemStack(Material.PHANTOM_MEMBRANE);
                recipe[5] = new ItemStack(Material.SOUL_SAND);
                recipe[6] = new ItemStack(Material.COAL);
                recipe[7] = new ItemStack(Material.SOUL_SAND);
                recipe[8] = new ItemStack(Material.COAL);
                break;

            case TITAN:
                recipe[0] = new ItemStack(Material.IRON_BLOCK);
                recipe[1] = new ItemStack(Material.IRON_BLOCK);
                recipe[2] = new ItemStack(Material.IRON_BLOCK);
                recipe[3] = new ItemStack(Material.IRON_BLOCK);
                recipe[4] = new ItemStack(Material.DIAMOND);
                recipe[5] = new ItemStack(Material.IRON_BLOCK);
                recipe[6] = new ItemStack(Material.IRON_BLOCK);
                recipe[7] = new ItemStack(Material.IRON_BLOCK);
                recipe[8] = new ItemStack(Material.IRON_BLOCK);
                break;

            case ARCANE:
                recipe[0] = new ItemStack(Material.LAPIS_LAZULI);
                recipe[1] = new ItemStack(Material.AMETHYST_SHARD);
                recipe[2] = new ItemStack(Material.LAPIS_LAZULI);
                recipe[3] = new ItemStack(Material.AMETHYST_SHARD);
                recipe[4] = new ItemStack(Material.ENCHANTED_BOOK);
                recipe[5] = new ItemStack(Material.AMETHYST_SHARD);
                recipe[6] = new ItemStack(Material.LAPIS_LAZULI);
                recipe[7] = new ItemStack(Material.AMETHYST_SHARD);
                recipe[8] = new ItemStack(Material.LAPIS_LAZULI);
                break;

            case DIVINE:
                // Divine requires all 7 other essence crafts + nether star + golden apple
                recipe[0] = createEssenceCraft(EssenceType.VOID, Material.ENDER_PEARL);
                recipe[1] = new ItemStack(Material.NETHER_STAR);
                recipe[2] = createEssenceCraft(EssenceType.INFERNO, Material.BLAZE_ROD);
                recipe[3] = createEssenceCraft(EssenceType.NATURE, Material.OAK_SAPLING);
                recipe[4] = new ItemStack(Material.GOLDEN_APPLE);
                recipe[5] = createEssenceCraft(EssenceType.ORACLE, Material.BEACON);
                recipe[6] = createEssenceCraft(EssenceType.PHANTOM, Material.PHANTOM_MEMBRANE);
                recipe[7] = createEssenceCraft(EssenceType.TITAN, Material.ANVIL);
                recipe[8] = createEssenceCraft(EssenceType.ARCANE, Material.ENCHANTED_BOOK);
                break;
        }

        return recipe;
    }
}