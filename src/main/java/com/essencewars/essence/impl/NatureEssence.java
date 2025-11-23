package com.essencewars.essence.impl;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.bukkit.ChatColor;
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
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import com.essencewars.EssenceWarsPlugin;
import com.essencewars.energy.EnergyState;
import com.essencewars.energy.PlayerEssenceData;
import com.essencewars.essence.Essence;
import com.essencewars.essence.EssenceTier;
import com.essencewars.essence.EssenceType;

public class NatureEssence extends Essence implements Listener {

    private final EssenceWarsPlugin plugin;

    private final Map<UUID, VenomState> venomStates = new HashMap<>();
    private final Map<UUID, Long> primalActiveUntil = new HashMap<>();

    public NatureEssence(EssenceWarsPlugin plugin) {
        super(EssenceType.NATURE);
        this.plugin = plugin;
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @Override
    public String getDisplayName() {
        return "Nature";
    }

    @Override
    public void usePrimary(Player player, PlayerEssenceData data) {
        player.sendMessage(ChatColor.GREEN + "Venomous Strikes is a passive that triggers on melee hits.");
    }

    @EventHandler
    public void onHit(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player player)) {
            return;
        }
        if (!(event.getEntity() instanceof LivingEntity target)) {
            return;
        }
        PlayerEssenceData data = plugin.getPlayerDataManager().get(player.getUniqueId());
        if (data == null || data.getEssenceType() != EssenceType.NATURE) {
            return;
        }
        if (isDepleted(data)) {
            return;
        }
        // Primal Surge life-steal during active window
        long now = System.currentTimeMillis();
        Long primalUntil = primalActiveUntil.get(player.getUniqueId());
        if (primalUntil != null && now <= primalUntil) {
            double heal = 3.0;
            double max = player.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue();
            player.setHealth(Math.min(max, player.getHealth() + heal));
        }

        // Venomous Strikes passive
        UUID attackerId = player.getUniqueId();
        UUID targetId = target.getUniqueId();
        VenomState state = venomStates.computeIfAbsent(attackerId, u -> new VenomState());
        if (!targetId.equals(state.target)) {
            state.target = targetId;
            state.hits = 0;
            state.stacks = 0;
            state.lastHitTime = 0L;
            state.lastProcTime = 0L;
        }
        if (now - state.lastHitTime > 5000L) {
            state.hits = 0;
            state.stacks = 0;
        }
        state.lastHitTime = now;
        state.hits++;
        if (state.hits % 3 != 0) {
            return;
        }
        if (now - state.lastProcTime < 10_000L) {
            return;
        }
        state.lastProcTime = now;
        if (state.stacks < 5) {
            state.stacks++;
        }
        int seconds = 6 + (state.stacks - 1) * 3;
        int ticks = seconds * 20;
        target.addPotionEffect(new PotionEffect(PotionEffectType.POISON, ticks, 1, false, false, true));
        if (target instanceof Player p) {
            p.sendMessage(ChatColor.DARK_GREEN + "You are poisoned (" + state.stacks + " stacks).");
        }
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_SPIDER_HURT, 1.0F, 0.8F);
        player.getWorld().spawnParticle(Particle.SLIME, target.getLocation().add(0, 1.0, 0), 10, 0.4, 0.4, 0.4, 0.05);
    }

    @Override
    public void useSecondary(Player player, PlayerEssenceData data) {
        if (!canUseSecondary(data)) {
            player.sendMessage("§8[Essence] §7You must be Tier II to use this ability.");
            return;
        }
        if (isDepleted(data)) {
            player.sendMessage("§8[Essence] §7Your essence is depleted.");
            return;
        }
        String key = "nature_secondary";
        EnergyState state = data.getEnergyState();
        if (data.isOnCooldown(key)) {
            long ms = data.getRemainingCooldown(key);
            player.sendMessage("§8[Essence] §7Ability on cooldown: " + (ms / 1000.0) + "s");
            return;
        }
        long now = System.currentTimeMillis();
        primalActiveUntil.put(player.getUniqueId(), now + 10_000L);
        player.addPotionEffect(new PotionEffect(PotionEffectType.ABSORPTION, 20 * 13, 3, false, false, true));
        player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 20 * 10, 2, false, false, true));
        player.addPotionEffect(new PotionEffect(PotionEffectType.JUMP, 20 * 10, 3, false, false, true));
        player.getWorld().spawnParticle(Particle.HEART, player.getLocation().add(0, 1.0, 0), 20, 0.6, 0.6, 0.6, 0.1);
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_WOLF_GROWL, 1.0F, 0.7F);
        // Wolves and full knockback immunity omitted for simplicity
        long cd = scaleCooldown(state, 45_000L);
        data.setCooldown(key, cd);
    }

    @EventHandler
    public void onNatureDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player player)) {
            return;
        }
        Long until = primalActiveUntil.get(player.getUniqueId());
        if (until == null) {
            return;
        }
        if (System.currentTimeMillis() > until) {
            primalActiveUntil.remove(player.getUniqueId());
            return;
        }
        // Reduce knockback by dampening velocity after damage
        plugin.getServer().getScheduler().runTask(plugin, () -> {
            org.bukkit.util.Vector v = player.getVelocity();
            player.setVelocity(new org.bukkit.util.Vector(0, v.getY(), 0));
        });
    }

    private static class VenomState {
        UUID target;
        int hits;
        int stacks;
        long lastHitTime;
        long lastProcTime;
    }
}
