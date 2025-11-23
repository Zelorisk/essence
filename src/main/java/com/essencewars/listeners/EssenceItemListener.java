package com.essencewars.listeners;

import com.essencewars.EssenceWarsPlugin;
import com.essencewars.crafting.EssenceCraftManager;
import com.essencewars.energy.PlayerEssenceData;
import com.essencewars.essence.EssenceType;
import java.util.UUID;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

public class EssenceItemListener implements Listener {

    private final EssenceWarsPlugin plugin;

    public EssenceItemListener(EssenceWarsPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPickup(org.bukkit.event.entity.EntityPickupItemEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;

        ItemStack item = event.getItem().getItemStack();
        if (!plugin.getCraftManager().isEssenceCraft(item)) return;

        // Check if player already has an essence item
        if (hasEssenceInInventory(player)) {
            event.setCancelled(true);
            player.sendMessage(
                ChatColor.RED + "You can only carry one essence at a time!"
            );
            return;
        }
    }

    @EventHandler
    public void onRightClick(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        ItemStack item = event.getItem();

        if (item == null || !plugin.getCraftManager().isEssenceCraft(item)) return;

        event.setCancelled(true);

        EssenceType type = plugin.getCraftManager().getEssenceType(item);
        if (type == null) {
            player.sendMessage(ChatColor.RED + "Invalid essence item!");
            return;
        }

        // Check if this essence is already owned by someone else
        if (plugin.isEssenceOwned(type)) {
            UUID ownerId = plugin.getEssenceOwner(type);
            if (!ownerId.equals(player.getUniqueId())) {
                Player owner = plugin.getServer().getPlayer(ownerId);
                String ownerName = owner != null ? owner.getName() : "someone";
                player.sendMessage(
                    ChatColor.RED + "This essence is already owned by " + ownerName + "!"
                );
                player.sendMessage(
                    ChatColor.GRAY + "You must wait for them to die before you can claim it."
                );
                return;
            }
        }

        PlayerEssenceData data = plugin
            .getPlayerDataManager()
            .getOrCreate(player);

        // Check if trying to absorb Divine essence without Tier II
        if (
            type == EssenceType.DIVINE &&
            data.getTier() != com.essencewars.essence.EssenceTier.TIER2
        ) {
            player.sendMessage(
                ChatColor.RED +
                    "You must be Tier II to absorb the Divine Essence!"
            );
            return;
        }

        // Set the essence and claim ownership
        data.setEssenceType(type);
        plugin.getPlayerDataManager().save(data);
        plugin.setEssenceOwner(type, player.getUniqueId());

        // Remove the item
        if (item.getAmount() > 1) {
            item.setAmount(item.getAmount() - 1);
        } else {
            player.getInventory().setItem(event.getHand(), null);
        }

        // Effects
        player
            .getWorld()
            .playSound(
                player.getLocation(),
                org.bukkit.Sound.ENTITY_PLAYER_LEVELUP,
                1.0F,
                1.5F
            );
        player
            .getWorld()
            .spawnParticle(
                org.bukkit.Particle.TOTEM,
                player.getLocation().add(0, 1, 0),
                50,
                0.5,
                0.5,
                0.5,
                0.1
            );

        String color = getEssenceColor(type);
        player.sendMessage(
            color +
                "§l✦" +
                ChatColor.GRAY +
                "You have absorbed the " +
                color +
                type.getDisplayName() +
                " Essence" +
                ChatColor.GRAY +
                "!"
        );
        player.sendMessage(
            ChatColor.GOLD + "You are now the sole owner of this essence!"
        );

        if (type == EssenceType.DIVINE) {
            player.sendMessage(
                ChatColor.GOLD +
                    "§l§m                                              "
            );
            player.sendMessage(
                ChatColor.GOLD + "§l  ⚠ DIVINE ESSENCE ABSORBED ⚠"
            );
            player.sendMessage(
                ChatColor.GOLD +
                    "§l§m                                              "
            );
            player.sendMessage(
                ChatColor.YELLOW +
                    "You wield the power of all essences combined!"
            );
            player.sendMessage(
                ChatColor.YELLOW + "The dragon's might flows through you!"
            );

            // Announce to all players
            for (Player online : plugin.getServer().getOnlinePlayers()) {
                if (!online.equals(player)) {
                    online.sendMessage(
                        ChatColor.GOLD +
                            "§l[DIVINE] " +
                            ChatColor.RED +
                            player.getName() +
                            " has obtained the DIVINE ESSENCE!"
                    );
                }
            }
        } else {
            // Announce to all players for other essences too
            for (Player online : plugin.getServer().getOnlinePlayers()) {
                if (!online.equals(player)) {
                    online.sendMessage(
                        color +
                            "[Essence] " +
                            ChatColor.GRAY +
                            player.getName() +
                            " has claimed the " +
                            color +
                            type.getDisplayName() +
                            ChatColor.GRAY +
                            " Essence!"
                    );
                }
            }
        }

        plugin.getScoreboardManager().updateFor(player);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;

        ItemStack cursor = event.getCursor();
        ItemStack current = event.getCurrentItem();

        // Check if trying to place an essence item when one already exists
        if (cursor != null && plugin.getCraftManager().isEssenceCraft(cursor)) {
            if (
                hasEssenceInInventory(player) &&
                !plugin.getCraftManager().isEssenceCraft(current)
            ) {
                event.setCancelled(true);
                player.sendMessage(
                    ChatColor.RED + "You can only carry one essence at a time!"
                );
            }
        }
    }

    private boolean hasEssenceInInventory(Player player) {
        PlayerInventory inv = player.getInventory();
        for (ItemStack item : inv.getContents()) {
            if (plugin.getCraftManager().isEssenceCraft(item)) {
                return true;
            }
        }
        ItemStack offhand = inv.getItemInOffHand();
        return plugin.getCraftManager().isEssenceCraft(offhand);
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
