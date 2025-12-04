package com.essencewars.items;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;

import com.essencewars.util.Keys;

public class EssenceUpgraderItem {

    private static final String NAME = ChatColor.GOLD + "" + ChatColor.BOLD + "Essence Upgrader";

    public static ItemStack create() {
        ItemStack stack = new ItemStack(Material.NETHER_STAR);
        stack.setAmount(1);
        ItemMeta meta = stack.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(NAME);
            meta.setLore(java.util.List.of(
                ChatColor.DARK_PURPLE + "A mystical artifact of great power.",
                "",
                ChatColor.YELLOW + "Right-click to upgrade to Tier II!",
                "",
                ChatColor.GRAY + "Unlocks access to the Divine Essence",
                ChatColor.GRAY + "and enhanced abilities."
            ));
            meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
            meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
            meta.getPersistentDataContainer().set(Keys.ESSENCE_UPGRADER, Keys.BOOLEAN, (byte) 1);
            stack.setItemMeta(meta);
        }
        return stack;
    }

    public static boolean isUpgrader(ItemStack stack) {
        if (stack == null || stack.getType() != Material.NETHER_STAR) {
            return false;
        }
        ItemMeta meta = stack.getItemMeta();
        if (meta == null || !meta.hasDisplayName()) {
            return false;
        }
        Byte isUpgrader = meta.getPersistentDataContainer().get(Keys.ESSENCE_UPGRADER, Keys.BOOLEAN);
        return isUpgrader != null && isUpgrader == 1;
    }

    public static boolean hasUpgrader(Player player) {
        if (player == null) {
            return false;
        }
        PlayerInventory inv = player.getInventory();
        for (ItemStack stack : inv.getContents()) {
            if (isUpgrader(stack)) {
                return true;
            }
        }
        ItemStack off = inv.getItemInOffHand();
        return isUpgrader(off);
    }
}
