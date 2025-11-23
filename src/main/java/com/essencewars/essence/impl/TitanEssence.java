package com.essencewars.essence.impl;

import com.essencewars.EssenceWarsPlugin;
import com.essencewars.energy.EnergyState;
import com.essencewars.energy.PlayerEssenceData;
import com.essencewars.essence.Essence;
import com.essencewars.essence.EssenceType;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityPotionEffectEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class TitanEssence extends Essence implements Listener {

    private final EssenceWarsPlugin plugin;
    private final Set<UUID> colossus = new HashSet<>();

    public TitanEssence(EssenceWarsPlugin plugin) {
        super(EssenceType.TITAN);
        this.plugin = plugin;
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @Override
    public String getDisplayName() {
        return "Titan";
    }

    @Override
    public void usePrimary(Player player, PlayerEssenceData data) {
        if (isDepleted(data)) {
            player.sendMessage("§8[Essence] §7Your essence is depleted.");
            return;
        }
        String key = "titan_primary";
        EnergyState state = data.getEnergyState();
        if (data.isOnCooldown(key)) {
            long ms = data.getRemainingCooldown(key);
            player.sendMessage(
                "§8[Essence] §7Ability on cooldown: " + (ms / 1000.0) + "s"
            );
            return;
        }
        Location start = player.getLocation();
        double max = plugin
            .getConfig()
            .getDouble("abilities.titan.slam-range", 12.0);
        double width = 3.0;
        double damage = scaleDamage(state, 8.0);
        for (double d = 1.0; d <= max; d += 1.0) {
            Location point = start
                .clone()
                .add(start.getDirection().normalize().multiply(d));
            start
                .getWorld()
                .spawnParticle(
                    Particle.BLOCK_CRACK,
                    point,
                    6,
                    0.5,
                    0.1,
                    0.5,
                    0.1,
                    Material.STONE.createBlockData()
                );
            for (Entity e : start
                .getWorld()
                .getNearbyEntities(point, width, 1.5, width)) {
                if (!(e instanceof LivingEntity le)) {
                    continue;
                }
                if (le.equals(player)) {
                    continue;
                }
                if (
                    le instanceof Player other &&
                    plugin.getTeamManager().areTeammates(player, other)
                ) {
                    continue;
                }
                le.damage(damage, player);
                org.bukkit.util.Vector kb = le
                    .getLocation()
                    .toVector()
                    .subtract(start.toVector())
                    .normalize()
                    .multiply(1.2);
                kb.setY(0.4);
                le.setVelocity(kb);
                le.addPotionEffect(
                    new PotionEffect(
                        PotionEffectType.SLOW,
                        20 * 3,
                        1,
                        false,
                        false,
                        true
                    )
                );
            }
        }
        start
            .getWorld()
            .playSound(start, Sound.ENTITY_IRON_GOLEM_ATTACK, 1.0F, 0.7F);
        int cooldownSeconds = plugin
            .getConfig()
            .getInt("cooldowns.titan.primary", 10);
        long cd = scaleCooldown(state, cooldownSeconds * 1000L);
        data.setCooldown(key, cd);
        player.sendMessage("§7[Titan] §fGround Slam!");
    }

    @Override
    public void useSecondary(Player player, PlayerEssenceData data) {
        if (!canUseSecondary(data)) {
            player.sendMessage(
                "§8[Essence] §7You must be Tier II to use this ability."
            );
            return;
        }
        if (isDepleted(data)) {
            player.sendMessage("§8[Essence] §7Your essence is depleted.");
            return;
        }
        String key = "titan_secondary";
        EnergyState state = data.getEnergyState();
        if (data.isOnCooldown(key)) {
            long ms = data.getRemainingCooldown(key);
            player.sendMessage(
                "§8[Essence] §7Ability on cooldown: " + (ms / 1000.0) + "s"
            );
            return;
        }
        UUID id = player.getUniqueId();
        colossus.add(id);
        int duration = plugin
            .getConfig()
            .getInt("abilities.titan.colossus-duration-seconds", 8);
        player.addPotionEffect(
            new PotionEffect(
                PotionEffectType.DAMAGE_RESISTANCE,
                20 * duration,
                1,
                false,
                false,
                true
            )
        );
        player.addPotionEffect(
            new PotionEffect(
                PotionEffectType.SLOW,
                20 * duration,
                2,
                false,
                false,
                true
            )
        );
        player
            .getWorld()
            .spawnParticle(
                Particle.BLOCK_CRACK,
                player.getLocation(),
                30,
                1.0,
                1.0,
                1.0,
                0.3,
                Material.IRON_BLOCK.createBlockData()
            );
        player
            .getWorld()
            .playSound(
                player.getLocation(),
                Sound.BLOCK_ANVIL_PLACE,
                1.0F,
                0.5F
            );
        player.sendMessage("§7[Titan] §fColossus Form activated!");
        plugin
            .getServer()
            .getScheduler()
            .runTaskLater(
                plugin,
                () -> {
                    colossus.remove(id);
                    Player p = plugin.getServer().getPlayer(id);
                    if (p != null && p.isOnline()) {
                        p.sendMessage("§7[Titan] §fColossus Form ended.");
                    }
                },
                20L * duration
            );
        int cooldownSeconds = plugin
            .getConfig()
            .getInt("cooldowns.titan.secondary", 20);
        long cd = scaleCooldown(state, cooldownSeconds * 1000L);
        data.setCooldown(key, cd);
    }

    @EventHandler
    public void onKnockback(EntityDamageByEntityEvent event) {
        if (!(event.getEntity() instanceof Player p)) {
            return;
        }

        PlayerEssenceData data = plugin.getPlayerDataManager().getOrCreate(p);
        if (data.getEssenceType() != EssenceType.TITAN) {
            return;
        }

        if (!colossus.contains(p.getUniqueId())) {
            return;
        }

        // Remove most of knockback during Colossus mode
        plugin
            .getServer()
            .getScheduler()
            .runTaskLater(
                plugin,
                () -> {
                    p.setVelocity(p.getVelocity().multiply(0.1));
                },
                1L
            );
    }

    @EventHandler
    public void onDamageReduction(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player p)) {
            return;
        }

        PlayerEssenceData data = plugin.getPlayerDataManager().getOrCreate(p);
        if (data.getEssenceType() != EssenceType.TITAN) {
            return;
        }

        // Passive: 10% damage reduction
        if (!event.isCancelled() && event.getDamage() > 0) {
            event.setDamage(event.getDamage() * 0.9);
        }

        // Extra reduction during Colossus mode handled by Resistance effect
    }

    @EventHandler
    public void onPotion(EntityPotionEffectEvent event) {
        if (!(event.getEntity() instanceof Player p)) {
            return;
        }
        if (!colossus.contains(p.getUniqueId())) {
            return;
        }
        if (event.getNewEffect() == null) {
            return;
        }
        PotionEffectType type = event.getNewEffect().getType();
        if (
            type.equals(PotionEffectType.SLOW) ||
            type.equals(PotionEffectType.WEAKNESS) ||
            type.equals(PotionEffectType.POISON) ||
            type.equals(PotionEffectType.WITHER)
        ) {
            event.setCancelled(true);
        }
    }
}
