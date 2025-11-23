package com.essencewars.items;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;

import com.essencewars.util.Keys;

public class EnergyCrystalItem {

    private static final String PREFIX = ChatColor.LIGHT_PURPLE + "Energy Crystal";

    public static ItemStack create(int value) {
        ItemStack stack = new ItemStack(Material.PRISMARINE_CRYSTALS);
        stack.setAmount(1);
        ItemMeta meta = stack.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(PREFIX + ChatColor.GRAY + " (" + value + ")");
            meta.setLore(java.util.List.of(ChatColor.GRAY + "Right-click to absorb energy."));
            meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
            meta.getPersistentDataContainer().set(Keys.CRYSTAL_VALUE, Keys.INTEGER, value);
            stack.setItemMeta(meta);
        }
        return stack;
    }

    public static boolean isCrystal(ItemStack stack) {
        if (stack == null || stack.getType() != Material.PRISMARINE_CRYSTALS) {
            return false;
        }
        ItemMeta meta = stack.getItemMeta();
        if (meta == null || !meta.hasDisplayName()) {
            return false;
        }
        return meta.getDisplayName().startsWith(PREFIX);
    }

    public static int getEnergyValue(ItemStack stack) {
        ItemMeta meta = stack.getItemMeta();
        if (meta == null) {
            return 0;
        }
        Integer value = meta.getPersistentDataContainer().get(Keys.CRYSTAL_VALUE, Keys.INTEGER);
        return value == null ? 0 : value;
    }

    public static int countCrystals(Player player) {
        if (player == null) {
            return 0;
        }
        PlayerInventory inv = player.getInventory();
        int count = 0;
        for (ItemStack stack : inv.getContents()) {
            if (isCrystal(stack)) {
                count += stack.getAmount();
            }
        }
        ItemStack off = inv.getItemInOffHand();
        if (isCrystal(off)) {
            count += off.getAmount();
        }
        return count;
    }

    public static boolean hasCrystal(Player player) {
        return countCrystals(player) > 0;
    }

    public static void removeCrystals(Player player, int amount) {
        if (player == null || amount <= 0) {
            return;
        }
        PlayerInventory inv = player.getInventory();
        int remaining = amount;
        ItemStack[] contents = inv.getContents();
        for (int i = 0; i < contents.length && remaining > 0; i++) {
            ItemStack stack = contents[i];
            if (!isCrystal(stack)) {
                continue;
            }
            int take = Math.min(stack.getAmount(), remaining);
            stack.setAmount(stack.getAmount() - take);
            if (stack.getAmount() <= 0) {
                contents[i] = null;
            }
            remaining -= take;
        }
        inv.setContents(contents);
        if (remaining > 0) {
            ItemStack off = inv.getItemInOffHand();
            if (isCrystal(off)) {
                int take = Math.min(off.getAmount(), remaining);
                off.setAmount(off.getAmount() - take);
                if (off.getAmount() <= 0) {
                    inv.setItemInOffHand(null);
                } else {
                    inv.setItemInOffHand(off);
                }
            }
        }
    }
}
