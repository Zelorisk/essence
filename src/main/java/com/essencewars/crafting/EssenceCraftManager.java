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
        // Void Essence - More expensive: Dragon Breath and Crying Obsidian
        registerEssenceCraft(EssenceType.VOID, Material.ENDER_PEARL, 
            "DDD", "DED", "DDD",
            'D', Material.CRYING_OBSIDIAN,
            'E', Material.DRAGON_BREATH);

        // Inferno Essence - Harder: Blaze Powder, Fire Charges, and Magma Blocks
        registerEssenceCraft(EssenceType.INFERNO, Material.BLAZE_ROD,
            "FMF", "MBM", "FMF",
            'F', Material.FIRE_CHARGE,
            'M', Material.MAGMA_BLOCK,
            'B', Material.BLAZE_POWDER);

        // Nature Essence - More resources: Heart of the Sea with rare plants
        registerEssenceCraft(EssenceType.NATURE, Material.OAK_SAPLING,
            "GVG", "VHV", "GSG",
            'G', Material.GLOW_BERRIES,
            'V', Material.VINE,
            'H', Material.HEART_OF_THE_SEA,
            'S', Material.SPORE_BLOSSOM);

        // Oracle Essence - Much harder: Nether Stars and Dragon's Breath
        registerEssenceCraft(EssenceType.ORACLE, Material.BEACON,
            "NSN", "DBD", "NSN",
            'N', Material.NETHER_STAR,
            'S', Material.SCULK_CATALYST,
            'D', Material.DRAGON_BREATH,
            'B', Material.BEACON);

        // Phantom Essence - Harder: Dragon Breath and Sculk Shriekers
        registerEssenceCraft(EssenceType.PHANTOM, Material.PHANTOM_MEMBRANE,
            "DSD", "SPS", "DSD",
            'D', Material.DRAGON_BREATH,
            'S', Material.SCULK_SHRIEKER,
            'P', Material.PHANTOM_MEMBRANE);

        // Titan Essence - Much harder: Ancient Debris and Netherite
        registerEssenceCraft(EssenceType.TITAN, Material.ANVIL,
            "NNN", "NAN", "NNN",
            'N', Material.NETHERITE_BLOCK,
            'A', Material.ANCIENT_DEBRIS);

        // Arcane Essence - Harder: Enchanted Golden Apples and End Crystals
        registerEssenceCraft(EssenceType.ARCANE, Material.ENCHANTED_BOOK,
            "NSN", "SES", "NSN",
            'N', Material.NETHER_STAR,
            'S', Material.ENCHANTED_GOLDEN_APPLE,
            'E', Material.END_CRYSTAL);

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
                recipe[0] = new ItemStack(Material.CRYING_OBSIDIAN);
                recipe[1] = new ItemStack(Material.CRYING_OBSIDIAN);
                recipe[2] = new ItemStack(Material.CRYING_OBSIDIAN);
                recipe[3] = new ItemStack(Material.CRYING_OBSIDIAN);
                recipe[4] = new ItemStack(Material.DRAGON_BREATH);
                recipe[5] = new ItemStack(Material.CRYING_OBSIDIAN);
                recipe[6] = new ItemStack(Material.CRYING_OBSIDIAN);
                recipe[7] = new ItemStack(Material.CRYING_OBSIDIAN);
                recipe[8] = new ItemStack(Material.CRYING_OBSIDIAN);
                break;
                
            case INFERNO:
                recipe[0] = new ItemStack(Material.FIRE_CHARGE);
                recipe[1] = new ItemStack(Material.MAGMA_BLOCK);
                recipe[2] = new ItemStack(Material.FIRE_CHARGE);
                recipe[3] = new ItemStack(Material.MAGMA_BLOCK);
                recipe[4] = new ItemStack(Material.BLAZE_POWDER);
                recipe[5] = new ItemStack(Material.MAGMA_BLOCK);
                recipe[6] = new ItemStack(Material.FIRE_CHARGE);
                recipe[7] = new ItemStack(Material.MAGMA_BLOCK);
                recipe[8] = new ItemStack(Material.FIRE_CHARGE);
                break;
                
            case NATURE:
                recipe[0] = new ItemStack(Material.GLOW_BERRIES);
                recipe[1] = new ItemStack(Material.VINE);
                recipe[2] = new ItemStack(Material.GLOW_BERRIES);
                recipe[3] = new ItemStack(Material.VINE);
                recipe[4] = new ItemStack(Material.HEART_OF_THE_SEA);
                recipe[5] = new ItemStack(Material.VINE);
                recipe[6] = new ItemStack(Material.GLOW_BERRIES);
                recipe[7] = new ItemStack(Material.SPORE_BLOSSOM);
                recipe[8] = new ItemStack(Material.GLOW_BERRIES);
                break;
                
            case ORACLE:
                recipe[0] = new ItemStack(Material.NETHER_STAR);
                recipe[1] = new ItemStack(Material.SCULK_CATALYST);
                recipe[2] = new ItemStack(Material.NETHER_STAR);
                recipe[3] = new ItemStack(Material.DRAGON_BREATH);
                recipe[4] = new ItemStack(Material.BEACON);
                recipe[5] = new ItemStack(Material.DRAGON_BREATH);
                recipe[6] = new ItemStack(Material.NETHER_STAR);
                recipe[7] = new ItemStack(Material.SCULK_CATALYST);
                recipe[8] = new ItemStack(Material.NETHER_STAR);
                break;
                
            case PHANTOM:
                recipe[0] = new ItemStack(Material.DRAGON_BREATH);
                recipe[1] = new ItemStack(Material.SCULK_SHRIEKER);
                recipe[2] = new ItemStack(Material.DRAGON_BREATH);
                recipe[3] = new ItemStack(Material.SCULK_SHRIEKER);
                recipe[4] = new ItemStack(Material.PHANTOM_MEMBRANE);
                recipe[5] = new ItemStack(Material.SCULK_SHRIEKER);
                recipe[6] = new ItemStack(Material.DRAGON_BREATH);
                recipe[7] = new ItemStack(Material.SCULK_SHRIEKER);
                recipe[8] = new ItemStack(Material.DRAGON_BREATH);
                break;
                
            case TITAN:
                recipe[0] = new ItemStack(Material.NETHERITE_BLOCK);
                recipe[1] = new ItemStack(Material.NETHERITE_BLOCK);
                recipe[2] = new ItemStack(Material.NETHERITE_BLOCK);
                recipe[3] = new ItemStack(Material.NETHERITE_BLOCK);
                recipe[4] = new ItemStack(Material.ANCIENT_DEBRIS);
                recipe[5] = new ItemStack(Material.NETHERITE_BLOCK);
                recipe[6] = new ItemStack(Material.NETHERITE_BLOCK);
                recipe[7] = new ItemStack(Material.NETHERITE_BLOCK);
                recipe[8] = new ItemStack(Material.NETHERITE_BLOCK);
                break;
                
            case ARCANE:
                recipe[0] = new ItemStack(Material.NETHER_STAR);
                recipe[1] = new ItemStack(Material.ENCHANTED_GOLDEN_APPLE);
                recipe[2] = new ItemStack(Material.NETHER_STAR);
                recipe[3] = new ItemStack(Material.ENCHANTED_GOLDEN_APPLE);
                recipe[4] = new ItemStack(Material.END_CRYSTAL);
                recipe[5] = new ItemStack(Material.ENCHANTED_GOLDEN_APPLE);
                recipe[6] = new ItemStack(Material.NETHER_STAR);
                recipe[7] = new ItemStack(Material.ENCHANTED_GOLDEN_APPLE);
                recipe[8] = new ItemStack(Material.NETHER_STAR);
                break;
                
            case DIVINE:
                // Divine requires all 7 other essence crafts + dragon egg + dragon head
                // Center: Dragon Egg, Corners: 4 essence crafts, Sides: 3 essence crafts, Top: Dragon Head
                recipe[0] = createEssenceCraft(EssenceType.VOID, Material.ENDER_PEARL);
                recipe[1] = new ItemStack(Material.DRAGON_HEAD);
                recipe[2] = createEssenceCraft(EssenceType.INFERNO, Material.BLAZE_ROD);
                recipe[3] = createEssenceCraft(EssenceType.NATURE, Material.OAK_SAPLING);
                recipe[4] = new ItemStack(Material.DRAGON_EGG);
                recipe[5] = createEssenceCraft(EssenceType.ORACLE, Material.BEACON);
                recipe[6] = createEssenceCraft(EssenceType.PHANTOM, Material.PHANTOM_MEMBRANE);
                recipe[7] = createEssenceCraft(EssenceType.TITAN, Material.ANVIL);
                recipe[8] = createEssenceCraft(EssenceType.ARCANE, Material.ENCHANTED_BOOK);
                break;
        }
        
        return recipe;
    }
}