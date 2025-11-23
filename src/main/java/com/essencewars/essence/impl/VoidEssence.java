package com.essencewars.essence.impl;

import com.essencewars.EssenceWarsPlugin;
import com.essencewars.energy.EnergyState;
import com.essencewars.energy.PlayerEssenceData;
import com.essencewars.essence.Essence;
import com.essencewars.essence.EssenceType;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

public class VoidEssence extends Essence {

    private final EssenceWarsPlugin plugin;

    public VoidEssence(EssenceWarsPlugin plugin) {
        super(EssenceType.VOID);
        this.plugin = plugin;
    }

    @Override
    public String getDisplayName() {
        return "Void";
    }

    @Override
    public void usePrimary(Player player, PlayerEssenceData data) {
        if (isDepleted(data)) {
            player.sendMessage("§8[Essence] §7Your essence is depleted.");
            return;
        }
        String key = "void_primary";
        EnergyState state = data.getEnergyState();
        if (data.isOnCooldown(key)) {
            long ms = data.getRemainingCooldown(key);
            player.sendMessage(
                "§8[Essence] §7Ability on cooldown: " + (ms / 1000.0) + "s"
            );
            return;
        }
        Player target = findNearestEnemy(player, 30.0);
        if (target == null) {
            player.sendMessage(
                "§8[Essence] §7No enemy in range for Singularity Pull."
            );
            return;
        }
        Location pLoc = player.getLocation();
        Location tLoc = target.getLocation();
        Vector toPlayer = pLoc.toVector().subtract(tLoc.toVector());
        double distance = toPlayer.length();
        if (distance < 2.0) {
            player.sendMessage("§8[Essence] §7Target is too close to pull.");
            return;
        }
        // Increased from 0.75 to 1.5 blocks/tick (much stronger pull)
        Vector velocity = toPlayer.normalize().multiply(1.5);
        target.setVelocity(velocity);
        target
            .getWorld()
            .spawnParticle(Particle.PORTAL, tLoc, 50, 0.8, 1.2, 0.8, 0.2);
        target
            .getWorld()
            .playSound(tLoc, Sound.ENTITY_ENDERMAN_HURT, 1.0F, 0.6F);
        player.sendMessage("§5[Void] §dPulling " + target.getName() + "...");

        double baseDamage = scaleDamage(state, 10.0);
        double extraDamage = scaleDamage(state, 6.0);

        new BukkitRunnable() {
            int ticks = 0;
            boolean applied = false;

            @Override
            public void run() {
                if (
                    !player.isOnline() || !target.isOnline() || target.isDead()
                ) {
                    finish();
                    return;
                }
                double d = player.getLocation().distance(target.getLocation());
                if (d <= 1.5) {
                    // Direct collision
                    target.damage(baseDamage + extraDamage, player);
                    player.addPotionEffect(
                        new PotionEffect(
                            PotionEffectType.CONFUSION,
                            40,
                            1,
                            false,
                            false,
                            true
                        )
                    );
                    target.addPotionEffect(
                        new PotionEffect(
                            PotionEffectType.CONFUSION,
                            40,
                            1,
                            false,
                            false,
                            true
                        )
                    );
                    target
                        .getWorld()
                        .spawnParticle(
                            Particle.EXPLOSION_LARGE,
                            target.getLocation(),
                            1
                        );
                    applied = true;
                    finish();
                    return;
                }
                if (d <= 3.0) {
                    // Pulled into close range but not a full collision
                    target.damage(baseDamage, player);
                    applied = true;
                    finish();
                    return;
                }
                ticks++;
                if (ticks > 30) {
                    // Extended tracking from 20 to 30
                    finish();
                }
            }

            private void finish() {
                long cd = scaleCooldown(state, 5000L);
                data.setCooldown(key, cd);
                cancel();
            }
        }
            .runTaskTimer(plugin, 2L, 1L);
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
        String key = "void_secondary";
        EnergyState state = data.getEnergyState();
        if (data.isOnCooldown(key)) {
            long ms = data.getRemainingCooldown(key);
            player.sendMessage(
                "§8[Essence] §7Ability on cooldown: " + (ms / 1000.0) + "s"
            );
            return;
        }
        Player target = findNearestEnemy(player, 20.0);
        if (target == null) {
            player.sendMessage(
                "§8[Essence] §7No enemy to mark for Void Guillotine."
            );
            return;
        }
        Location markLoc = target.getLocation().clone().add(0, 1.0, 0);
        target
            .getWorld()
            .spawnParticle(
                Particle.SPELL_WITCH,
                markLoc,
                40,
                0.5,
                0.7,
                0.5,
                0.1
            );
        target
            .getWorld()
            .playSound(markLoc, Sound.BLOCK_BEACON_AMBIENT, 0.8F, 1.6F);
        player.sendMessage(
            "§5[Void] §dVoid Guillotine placed on " + target.getName() + "."
        );
        target.sendMessage(
            "§5[Void] §dYou feel a void blade hanging above you..."
        );

        long cd = scaleCooldown(state, 18_000L);
        data.setCooldown(key, cd);

        new BukkitRunnable() {
            @Override
            public void run() {
                if (
                    !player.isOnline() || !target.isOnline() || target.isDead()
                ) {
                    return;
                }
                Location loc = target.getLocation().clone().add(0, 1.0, 0);
                loc
                    .getWorld()
                    .spawnParticle(
                        Particle.REVERSE_PORTAL,
                        loc,
                        60,
                        0.6,
                        0.8,
                        0.6,
                        0.1
                    );
                loc
                    .getWorld()
                    .playSound(loc, Sound.ENTITY_WITHER_SPAWN, 1.0F, 0.8F);

                double maxHealth = target
                    .getAttribute(Attribute.GENERIC_MAX_HEALTH)
                    .getValue();
                double current = target.getHealth();
                double ratio = current / maxHealth;
                if (ratio <= 0.4) {
                    // Execute: true kill ignoring armor / resistance
                    target
                        .getWorld()
                        .playSound(
                            target.getLocation(),
                            Sound.ENTITY_WITHER_DEATH,
                            1.0F,
                            0.5F
                        );
                    target.setHealth(0.0);
                } else {
                    double damage = scaleDamage(state, 12.0);
                    target.damage(damage, player);
                    target.addPotionEffect(
                        new PotionEffect(
                            PotionEffectType.WITHER,
                            20 * 5,
                            2,
                            false,
                            false,
                            true
                        )
                    );
                }
            }
        }
            .runTaskLater(plugin, 40L); // 2 second delay
    }

    private Player findNearestEnemy(Player player, double range) {
        Player best = null;
        double bestDistSq = Double.MAX_VALUE;
        double rangeSq = range * range;
        Location origin = player.getLocation();
        for (Player other : player.getWorld().getPlayers()) {
            if (other.equals(player)) {
                continue;
            }
            if (plugin.getTeamManager().areTeammates(player, other)) {
                continue;
            }
            double distSq = other.getLocation().distanceSquared(origin);
            if (distSq <= rangeSq && distSq < bestDistSq) {
                bestDistSq = distSq;
                best = other;
            }
        }
        return best;
    }
}
