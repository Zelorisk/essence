package com.essencewars.listeners;

import com.essencewars.EssenceWarsPlugin;
import com.essencewars.energy.PlayerEssenceData;
import com.essencewars.essence.Essence;
import com.essencewars.essence.EssenceType;
import com.essencewars.essence.impl.ArcaneEssence;
import com.essencewars.items.EnergyCrystalItem;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;

public class PlayerListener implements Listener {

    private final EssenceWarsPlugin plugin;

    public PlayerListener(EssenceWarsPlugin plugin) {
        this.plugin = plugin;
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        PlayerEssenceData data = plugin
            .getPlayerDataManager()
            .getOrCreate(player);
        plugin.getScoreboardManager().updateFor(player);
        plugin.getTabListManager().updateFor(player);

        // Show tutorial if first time
        if (!data.hasSeenTutorial()) {
            plugin
                .getServer()
                .getScheduler()
                .runTaskLater(
                    plugin,
                    () -> {
                        if (player.isOnline()) {
                            plugin.getTutorialGUI().open(player);
                            plugin.setTutorialImmune(player, true);
                        }
                    },
                    40L
                ); // 2 seconds delay
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        PlayerEssenceData data = plugin
            .getPlayerDataManager()
            .get(player.getUniqueId());
        if (data != null) {
            plugin.getPlayerDataManager().save(data);
        }
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player victim = event.getEntity();
        Player killer = victim.getKiller();

        PlayerEssenceData victimData = plugin
            .getPlayerDataManager()
            .getOrCreate(victim);

        // Drop essence craft on death and release ownership
        EssenceType victimEssence = victimData.getEssenceType();
        if (victimEssence != null) {
            // Get the craft icon material
            Material icon;
            switch (victimEssence) {
                case VOID -> icon = Material.ENDER_PEARL;
                case INFERNO -> icon = Material.BLAZE_ROD;
                case NATURE -> icon = Material.OAK_SAPLING;
                case ORACLE -> icon = Material.BEACON;
                case PHANTOM -> icon = Material.PHANTOM_MEMBRANE;
                case TITAN -> icon = Material.ANVIL;
                case ARCANE -> icon = Material.ENCHANTED_BOOK;
                case DIVINE -> icon = Material.TOTEM_OF_UNDYING;
                default -> icon = Material.PAPER;
            }
            
            // Create and drop the essence craft
            ItemStack essenceCraft = plugin.getCraftManager().createEssenceCraft(victimEssence, icon);
            victim.getWorld().dropItemNaturally(victim.getLocation(), essenceCraft);
            
            // Release essence ownership
            plugin.releaseEssence(victimEssence);
            
            // Announce essence is now available
            String color = getEssenceColor(victimEssence);
            for (Player online : plugin.getServer().getOnlinePlayers()) {
                online.sendMessage(
                    color +
                        "[Essence] " +
                        ChatColor.GRAY +
                        "The " +
                        color +
                        victimEssence.getDisplayName() +
                        ChatColor.GRAY +
                        " Essence is now available for claiming!"
                );
            }
            
            // Remove essence from victim
            victimData.setEssenceType(null);
        }

        // Check grace period
        long now = System.currentTimeMillis();
        if (now < victimData.getGraceUntil()) {
            victim.sendMessage(
                "§a[Grace Period] §7You did not lose energy due to grace period protection."
            );
        } else {
            // Lose energy on death
            int maxEnergy = plugin.getConfig().getInt("max-energy", 10);
            victimData.addEnergy(-1, maxEnergy);
            plugin.getPlayerDataManager().save(victimData);

            if (victimData.getEnergy() <= 0) {
                victim.sendMessage(
                    "§c§l[DEPLETED] §7Your essence is now depleted! You cannot use abilities!"
                );
            } else {
                victim.sendMessage(
                    "§c[-1 Energy] §7Energy: " + victimData.getEnergy()
                );
            }
        }

        // Killer gains energy
        if (killer != null && !killer.equals(victim)) {
            if (!plugin.getTeamManager().areTeammates(killer, victim)) {
                PlayerEssenceData killerData = plugin
                    .getPlayerDataManager()
                    .getOrCreate(killer);
                int maxEnergy = plugin.getConfig().getInt("max-energy", 10);
                int before = killerData.getEnergy();
                killerData.addEnergy(1, maxEnergy);
                plugin.getPlayerDataManager().save(killerData);

                if (killerData.getEnergy() > before) {
                    killer.sendMessage(
                        "§a[+1 Energy] §7Energy: " + killerData.getEnergy()
                    );
                }

                // Check for Divine essence dragon form kill tracking
                if (killerData.getEssenceType() == EssenceType.DIVINE) {
                    Essence essence = plugin
                        .getEssenceRegistry()
                        .get(EssenceType.DIVINE);
                    if (
                        essence instanceof
                            com.essencewars.essence.impl.DivineEssence divine
                    ) {
                        divine.onDragonFormKill(killer);
                    }
                }

                plugin.getScoreboardManager().updateFor(killer);
                plugin.getTabListManager().updateFor(killer);
            }
        }

        plugin.getScoreboardManager().updateFor(victim);
        plugin.getTabListManager().updateFor(victim);
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

    @EventHandler
    public void onPlayerRespawn(PlayerRespawnEvent event) {
        Player player = event.getPlayer();
        PlayerEssenceData data = plugin
            .getPlayerDataManager()
            .getOrCreate(player);

        // Set respawn protection
        long protectionDuration =
            plugin.getConfig().getLong("respawn-protection-seconds", 5) * 1000L;
        data.setRespawnProtectionUntil(
            System.currentTimeMillis() + protectionDuration
        );

        plugin.getScoreboardManager().updateFor(player);
        plugin.getTabListManager().updateFor(player);
    }

    @EventHandler
    public void onPlayerDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;

        PlayerEssenceData data = plugin
            .getPlayerDataManager()
            .getOrCreate(player);
        long now = System.currentTimeMillis();

        // Respawn protection
        if (now < data.getRespawnProtectionUntil()) {
            event.setCancelled(true);
            return;
        }
    }

    @EventHandler
    public void onPlayerDamageByEntity(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player attacker)) return;
        if (!(event.getEntity() instanceof Player victim)) return;

        // Check if tutorial immune
        if (
            plugin.isTutorialImmune(victim) || plugin.isTutorialImmune(attacker)
        ) {
            event.setCancelled(true);
            return;
        }

        // Check respawn protection
        PlayerEssenceData victimData = plugin
            .getPlayerDataManager()
            .getOrCreate(victim);
        long now = System.currentTimeMillis();
        if (now < victimData.getRespawnProtectionUntil()) {
            event.setCancelled(true);
            attacker.sendMessage(
                "§c[PvP] §7That player has respawn protection!"
            );
            return;
        }

        // Check grace period
        if (now < victimData.getGraceUntil()) {
            event.setCancelled(true);
            attacker.sendMessage(
                "§a[Grace Period] §7That player is in their grace period!"
            );
            return;
        }

        PlayerEssenceData attackerData = plugin
            .getPlayerDataManager()
            .getOrCreate(attacker);
        if (now < attackerData.getGraceUntil()) {
            event.setCancelled(true);
            attacker.sendMessage(
                "§a[Grace Period] §7You cannot attack while in grace period!"
            );
            return;
        }

        // Friendly fire check
        if (plugin.getTeamManager().areTeammates(attacker, victim)) {
            event.setCancelled(true);
            attacker.sendMessage(
                "§c[Team] §7You cannot damage your teammates!"
            );
            return;
        }

        // Apply Divine essence damage multiplier if in dragon form
        if (attackerData.getEssenceType() == EssenceType.DIVINE) {
            Essence essence = plugin
                .getEssenceRegistry()
                .get(EssenceType.DIVINE);
            if (
                essence instanceof
                    com.essencewars.essence.impl.DivineEssence divine
            ) {
                double multiplier = divine.getDragonFormDamageMultiplier(
                    attacker.getUniqueId()
                );
                if (multiplier > 1.0) {
                    event.setDamage(event.getDamage() * multiplier);
                }
            }
        }
    }

    @EventHandler
    public void onPlayerDropItem(PlayerDropItemEvent event) {
        Player player = event.getPlayer();

        // Check if drop key is configured for abilities
        boolean useDropKeyForAbilities = plugin
            .getConfig()
            .getBoolean("use-drop-key-for-abilities", true);
        if (!useDropKeyForAbilities) {
            return; // Let items drop normally
        }

        // Check if player is silenced
        if (ArcaneEssence.isSilenced(player.getUniqueId())) {
            player.sendMessage(
                "§5[Arcane] §dYour essence is currently silenced and cannot be used."
            );
            event.setCancelled(true);
            return;
        }

        PlayerEssenceData data = plugin
            .getPlayerDataManager()
            .getOrCreate(player);
        EssenceType type = data.getEssenceType();

        if (type == null) {
            return; // No essence, let items drop
        }

        Essence essence = plugin.getEssenceRegistry().get(type);
        if (essence == null) {
            return;
        }

        // Cancel the drop and use ability instead
        event.setCancelled(true);

        // Check if sneaking for secondary
        if (player.isSneaking()) {
            essence.useSecondary(player, data);
        } else {
            essence.usePrimary(player, data);
        }

        plugin.getScoreboardManager().updateFor(player);
        plugin.getTabListManager().updateFor(player);
    }

    @EventHandler
    public void onPickupCrystal(
        org.bukkit.event.entity.EntityPickupItemEvent event
    ) {
        if (!(event.getEntity() instanceof Player player)) return;

        org.bukkit.inventory.ItemStack item = event.getItem().getItemStack();
        if (!EnergyCrystalItem.isCrystal(item)) return;

        int value = EnergyCrystalItem.getEnergyValue(item);
        PlayerEssenceData data = plugin
            .getPlayerDataManager()
            .getOrCreate(player);
        int maxEnergy = plugin.getConfig().getInt("max-energy", 10);

        int before = data.getEnergy();
        data.addEnergy(value, maxEnergy);
        plugin.getPlayerDataManager().save(data);

        if (data.getEnergy() > before) {
            int gained = data.getEnergy() - before;
            player.sendMessage(
                "§b[+Crystal] §7Absorbed " +
                    gained +
                    " energy! Total: " +
                    data.getEnergy()
            );
            player.playSound(
                player.getLocation(),
                org.bukkit.Sound.ENTITY_PLAYER_LEVELUP,
                1.0F,
                2.0F
            );
        }

        event.getItem().remove();
        event.setCancelled(true);

        plugin.getScoreboardManager().updateFor(player);
        plugin.getTabListManager().updateFor(player);
    }
}
