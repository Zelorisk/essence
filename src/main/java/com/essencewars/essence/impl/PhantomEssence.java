package com.essencewars.essence.impl;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import com.essencewars.EssenceWarsPlugin;
import com.essencewars.energy.EnergyState;
import com.essencewars.energy.PlayerEssenceData;
import com.essencewars.essence.Essence;
import com.essencewars.essence.EssenceTier;
import com.essencewars.essence.EssenceType;

public class PhantomEssence extends Essence implements Listener {

    private final EssenceWarsPlugin plugin;
    private final Map<UUID, ExecutionMark> marks = new HashMap<>();

    public PhantomEssence(EssenceWarsPlugin plugin) {
        super(EssenceType.PHANTOM);
        this.plugin = plugin;
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @Override
    public String getDisplayName() {
        return "Phantom";
    }

    @Override
    public void usePrimary(Player player, PlayerEssenceData data) {
        if (isDepleted(data)) {
            player.sendMessage("§8[Essence] §7Your essence is depleted.");
            return;
        }
        String key = "phantom_primary";
        EnergyState state = data.getEnergyState();
        if (data.isOnCooldown(key)) {
            long ms = data.getRemainingCooldown(key);
            player.sendMessage("§8[Essence] §7Ability on cooldown: " + (ms / 1000.0) + "s");
            return;
        }
        player.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, 20 * 6, 0, false, false, true));
        player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 20 * 6, 1, false, false, true));
        player.setMetadata("phantom_shadowmeld", new org.bukkit.metadata.FixedMetadataValue(plugin, System.currentTimeMillis() + 6000L));
        Location loc = player.getLocation();
        loc.getWorld().spawnParticle(Particle.SMOKE_LARGE, loc, 40, 0.8, 1.0, 0.8, 0.05);
        loc.getWorld().playSound(loc, Sound.ENTITY_PHANTOM_SWOOP, 1.0F, 1.2F);
        long cd = scaleCooldown(state, 7000L);
        data.setCooldown(key, cd);
    }

    @EventHandler
    public void onHit(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player player)) {
            return;
        }
        if (!(event.getEntity() instanceof LivingEntity target)) {
            return;
        }
        if (player.hasMetadata("phantom_shadowmeld")) {
            long until = player.getMetadata("phantom_shadowmeld").get(0).asLong();
            if (System.currentTimeMillis() <= until) {
                double extra = 9.0;
                event.setDamage(event.getDamage() + extra);
                player.removeMetadata("phantom_shadowmeld", plugin);
            }
        }
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
        String key = "phantom_secondary";
        EnergyState state = data.getEnergyState();
        if (data.isOnCooldown(key)) {
            long ms = data.getRemainingCooldown(key);
            player.sendMessage("§8[Essence] §7Ability on cooldown: " + (ms / 1000.0) + "s");
            return;
        }
        UUID id = player.getUniqueId();
        ExecutionMark mark = marks.get(id);
        long now = System.currentTimeMillis();
        if (mark != null && mark.expiresAt > now) {
            // Teleport strike
            Player target = plugin.getServer().getPlayer(mark.target);
            marks.remove(id);
            if (target == null || !target.isOnline()) {
                player.sendMessage("§8[Phantom] §7Marked target is no longer available.");
                return;
            }
            Location tLoc = target.getLocation();
            Location behind = tLoc.clone();
            behind.setYaw(tLoc.getYaw() + 180F);
            org.bukkit.util.Vector dir = tLoc.getDirection().normalize().multiply(-1.5);
            behind.add(dir.getX(), 0, dir.getZ());
            behind.setPitch(player.getLocation().getPitch());
            player.teleport(behind);
            double damage = scaleDamage(state, 12.0);
            double before = target.getHealth();
            target.damage(damage, player);
            behind.getWorld().spawnParticle(Particle.SMOKE_LARGE, tLoc, 40, 0.6, 1.0, 0.6, 0.05);
            behind.getWorld().playSound(tLoc, Sound.ENTITY_PLAYER_ATTACK_CRIT, 1.0F, 0.9F);
            long cd = scaleCooldown(state, 14000L);
            if (before <= damage) {
                cd = (long) (cd * 0.5);
            }
            data.setCooldown(key, cd);
        } else {
            // Apply mark
            LivingEntity target = findLowHealthTarget(player, 15.0);
            if (!(target instanceof Player p)) {
                player.sendMessage("§8[Phantom] §7No low-health enemy to execute.");
                return;
            }
            double max = p.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue();
            if (p.getHealth() >= max * 0.5) {
                player.sendMessage("§8[Phantom] §7Target must be below 50% health.");
                return;
            }
            p.addPotionEffect(new PotionEffect(PotionEffectType.GLOWING, 20 * 8, 0, false, false, true));
            marks.put(id, new ExecutionMark(p.getUniqueId(), now + 8000L));
            player.sendMessage("§5[Phantom] §dExecution Protocol armed on " + p.getName() + ".");
        }
    }

    private LivingEntity findLowHealthTarget(Player player, double range) {
        Location center = player.getLocation();
        LivingEntity best = null;
        double bestDist = Double.MAX_VALUE;
        for (Entity e : center.getWorld().getNearbyEntities(center, range, range, range)) {
            if (!(e instanceof LivingEntity le)) {
                continue;
            }
            if (le.equals(player)) {
                continue;
            }
            if (le instanceof Player other && plugin.getTeamManager().areTeammates(player, other)) {
                continue;
            }
            double dist = le.getLocation().distanceSquared(center);
            if (dist < bestDist) {
                bestDist = dist;
                best = le;
            }
        }
        return best;
    }

    private static class ExecutionMark {
        final UUID target;
        final long expiresAt;

        ExecutionMark(UUID target, long expiresAt) {
            this.target = target;
            this.expiresAt = expiresAt;
        }
    }
}
