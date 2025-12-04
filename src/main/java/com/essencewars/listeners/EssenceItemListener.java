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

        // No longer limiting essence crafts in inventory - players can have multiple
    }

    @EventHandler
    public void onRightClick(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        ItemStack item = event.getItem();

        // Check if right-clicking an essence upgrader
        if (item != null && com.essencewars.items.EssenceUpgraderItem.isUpgrader(item)) {
            if (event.getAction() == org.bukkit.event.block.Action.RIGHT_CLICK_AIR ||
                event.getAction() == org.bukkit.event.block.Action.RIGHT_CLICK_BLOCK) {
                event.setCancelled(true);

                PlayerEssenceData data = plugin.getPlayerDataManager().getOrCreate(player);

                // Check if already Tier II
                if (data.getTier() == com.essencewars.essence.EssenceTier.TIER2) {
                    player.sendMessage("§c[Upgrader] §7You are already Tier II!");
                    return;
                }

                // Upgrade to Tier II
                data.setTier(com.essencewars.essence.EssenceTier.TIER2);
                plugin.getPlayerDataManager().save(data);

                // Remove the upgrader
                if (item.getAmount() > 1) {
                    item.setAmount(item.getAmount() - 1);
                } else {
                    player.getInventory().setItem(event.getHand(), null);
                }

                // Effects
                player.getWorld().playSound(
                    player.getLocation(),
                    org.bukkit.Sound.UI_TOAST_CHALLENGE_COMPLETE,
                    1.0F,
                    1.0F
                );
                player.getWorld().spawnParticle(
                    org.bukkit.Particle.TOTEM,
                    player.getLocation().add(0, 1, 0),
                    100,
                    0.5,
                    1.0,
                    0.5,
                    0.1
                );
                player.getWorld().spawnParticle(
                    org.bukkit.Particle.END_ROD,
                    player.getLocation().add(0, 1, 0),
                    50,
                    0.3,
                    0.5,
                    0.3,
                    0.05
                );

                // Messages
                player.sendMessage("§6§l§m                                              ");
                player.sendMessage("§6§l     ⚡ ESSENCE TIER UPGRADED ⚡");
                player.sendMessage("§6§l§m                                              ");
                player.sendMessage("§e You have ascended to §6§lTier II§e!");
                player.sendMessage("§e You can now claim the §6§lDivine Essence§e!");
                player.sendMessage("§e Your abilities are now enhanced!");
                player.sendMessage("§6§l§m                                              ");

                // Announce to all players
                for (Player online : plugin.getServer().getOnlinePlayers()) {
                    if (!online.equals(player)) {
                        online.sendMessage(
                            "§6[Tier II] §e" + player.getName() + " has ascended to Tier II!"
                        );
                    }
                }

                plugin.getScoreboardManager().updateFor(player);
                plugin.getTabListManager().updateFor(player);
                return;
            }
        }

        // Check if right-clicking an energy crystal
        if (item != null && com.essencewars.items.EnergyCrystalItem.isCrystal(item)) {
            if (event.getAction() == org.bukkit.event.block.Action.RIGHT_CLICK_AIR ||
                event.getAction() == org.bukkit.event.block.Action.RIGHT_CLICK_BLOCK) {
                event.setCancelled(true);

                int value = com.essencewars.items.EnergyCrystalItem.getEnergyValue(item);
                PlayerEssenceData data = plugin.getPlayerDataManager().getOrCreate(player);
                int maxEnergy = plugin.getConfig().getInt("max-energy", 10);

                int before = data.getEnergy();
                data.addEnergy(value, maxEnergy);
                plugin.getPlayerDataManager().save(data);

                if (data.getEnergy() > before) {
                    int gained = data.getEnergy() - before;
                    player.sendMessage(
                        "§b[+Crystal] §7Absorbed " + gained + " energy! Total: " + data.getEnergy()
                    );
                    player.playSound(
                        player.getLocation(),
                        org.bukkit.Sound.ENTITY_PLAYER_LEVELUP,
                        1.0F,
                        2.0F
                    );
                } else {
                    player.sendMessage("§c[Crystal] §7Your energy is already at maximum!");
                }

                // Remove the crystal
                if (item.getAmount() > 1) {
                    item.setAmount(item.getAmount() - 1);
                } else {
                    player.getInventory().setItem(event.getHand(), null);
                }

                plugin.getScoreboardManager().updateFor(player);
                plugin.getTabListManager().updateFor(player);
                return;
            }
        }

        if (item == null || !plugin.getCraftManager().isEssenceCraft(item)) return;

        event.setCancelled(true);

        EssenceType type = plugin.getCraftManager().getEssenceType(item);
        if (type == null) {
            player.sendMessage(ChatColor.RED + "Invalid essence item!");
            return;
        }

        // Check if this essence is already owned by someone else
        // Divine essence can only have 1 owner, basic essences can have up to 3
        int maxOwners = (type == EssenceType.DIVINE) ? 1 : 3;

        if (plugin.getEssenceOwnerCount(type) >= maxOwners) {
            if (!plugin.hasEssence(player.getUniqueId(), type)) {
                player.sendMessage(
                    ChatColor.RED + "This essence already has the maximum number of owners!"
                );
                if (type == EssenceType.DIVINE) {
                    UUID ownerId = plugin.getEssenceOwner(type);
                    Player owner = plugin.getServer().getPlayer(ownerId);
                    String ownerName = owner != null ? owner.getName() : "someone";
                    player.sendMessage(
                        ChatColor.GRAY + "The Divine Essence is owned by " + ownerName + "."
                    );
                } else {
                    player.sendMessage(
                        ChatColor.GRAY + "Up to 3 players can own " + type.getDisplayName() + " Essence."
                    );
                }
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

        // No longer limiting essence crafts in inventory
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
