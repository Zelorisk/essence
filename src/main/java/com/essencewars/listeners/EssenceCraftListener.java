package com.essencewars.listeners;

import com.essencewars.EssenceWarsPlugin;
import com.essencewars.crafting.EssenceCraftManager;
import com.essencewars.energy.PlayerEssenceData;
import com.essencewars.essence.EssenceType;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

public class EssenceCraftListener implements Listener {

    private final EssenceWarsPlugin plugin;
    private final EssenceCraftManager craftManager;

    public EssenceCraftListener(
        EssenceWarsPlugin plugin,
        EssenceCraftManager craftManager
    ) {
        this.plugin = plugin;
        this.craftManager = craftManager;
    }

    @EventHandler
    public void onCraft(CraftItemEvent event) {
        ItemStack result = event.getRecipe().getResult();

        if (!craftManager.isEssenceCraft(result)) {
            return;
        }

        if (!(event.getWhoClicked() instanceof Player player)) {
            return;
        }

        // Check if player already has an essence craft
        if (craftManager.hasEssenceCraft(player)) {
            event.setCancelled(true);
            player.sendMessage(
                ChatColor.RED +
                    "You can only carry one essence craft at a time!"
            );
            player.sendMessage(
                ChatColor.GRAY + "Use your current craft or drop it first."
            );
            return;
        }

        EssenceType type = craftManager.getEssenceType(result);
        player.sendMessage(
            ChatColor.LIGHT_PURPLE +
                "You have crafted the " +
                type.getDisplayName() +
                " essence!"
        );
        player.sendMessage(
            ChatColor.YELLOW + "Right-click to learn this essence."
        );
        player.playSound(
            player.getLocation(),
            Sound.BLOCK_ENCHANTMENT_TABLE_USE,
            1.0F,
            1.2F
        );
    }

    @EventHandler
    public void onUse(PlayerInteractEvent event) {
        if (
            event.getAction() != Action.RIGHT_CLICK_AIR &&
            event.getAction() != Action.RIGHT_CLICK_BLOCK
        ) {
            return;
        }

        ItemStack item = event.getItem();
        if (!craftManager.isEssenceCraft(item)) {
            return;
        }

        Player player = event.getPlayer();
        event.setCancelled(true);

        EssenceType type = craftManager.getEssenceType(item);
        if (type == null) {
            player.sendMessage(ChatColor.RED + "Invalid essence craft!");
            return;
        }

        PlayerEssenceData data = plugin
            .getPlayerDataManager()
            .getOrCreate(player);

        // Confirmation for Divine essence
        if (type == EssenceType.DIVINE) {
            player.sendMessage(
                ChatColor.GOLD +
                    "" +
                    ChatColor.BOLD +
                    "═══════════════════════════"
            );
            player.sendMessage(
                ChatColor.GOLD +
                    "" +
                    ChatColor.BOLD +
                    "  DIVINE ESSENCE OBTAINED"
            );
            player.sendMessage(
                ChatColor.GOLD +
                    "" +
                    ChatColor.BOLD +
                    "═══════════════════════════"
            );
            player.sendMessage("");
            player.sendMessage(
                ChatColor.YELLOW + "You have obtained the ultimate essence!"
            );
            player.sendMessage(
                ChatColor.RED +
                    "This essence is " +
                    ChatColor.BOLD +
                    "OVERPOWERED" +
                    ChatColor.RED +
                    "."
            );
            player.sendMessage("");
            player.sendMessage(
                ChatColor.GRAY + "Primary: " + ChatColor.WHITE + "Divine Wrath"
            );
            player.sendMessage(
                ChatColor.GRAY + "  Massive AOE devastation in waves"
            );
            player.sendMessage("");
            player.sendMessage(
                ChatColor.GRAY + "Secondary: " + ChatColor.WHITE + "Ascension"
            );
            player.sendMessage(
                ChatColor.GRAY + "  Become a god for 15 seconds"
            );
            player.sendMessage("");
            player.sendMessage(
                ChatColor.GOLD +
                    "" +
                    ChatColor.BOLD +
                    "═══════════════════════════"
            );

            player
                .getWorld()
                .playSound(
                    player.getLocation(),
                    Sound.ENTITY_ENDER_DRAGON_GROWL,
                    2.0F,
                    1.0F
                );
            player
                .getWorld()
                .playSound(
                    player.getLocation(),
                    Sound.UI_TOAST_CHALLENGE_COMPLETE,
                    1.0F,
                    1.0F
                );
        } else {
            player.sendMessage(
                ChatColor.LIGHT_PURPLE +
                    "You have learned the " +
                    type.getDisplayName() +
                    " essence!"
            );
            player.playSound(
                player.getLocation(),
                Sound.ENTITY_PLAYER_LEVELUP,
                1.0F,
                1.5F
            );
        }

        data.setEssenceType(type);
        plugin.getPlayerDataManager().save(data);
        plugin.getScoreboardManager().updateFor(player);

        // Remove the craft item
        item.setAmount(item.getAmount() - 1);

        // Show particles
        player
            .getWorld()
            .spawnParticle(
                org.bukkit.Particle.ENCHANTMENT_TABLE,
                player.getLocation().add(0, 1, 0),
                50,
                0.5,
                0.5,
                0.5,
                1
            );
    }
}
