package com.essencewars.essence.impl;

import com.essencewars.EssenceWarsPlugin;
import com.essencewars.energy.EnergyState;
import com.essencewars.energy.PlayerEssenceData;
import com.essencewars.essence.Essence;
import com.essencewars.essence.EssenceTier;
import com.essencewars.essence.EssenceType;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

public class InfernoEssence extends Essence implements Listener {

    private final EssenceWarsPlugin plugin;

    private final Map<UUID, Deque<FlameMine>> mines = new HashMap<>();
    private final Map<UUID, Long> rebirthCooldownUntil = new HashMap<>();
    private final Map<UUID, RebirthState> rebirthing = new HashMap<>();

    public InfernoEssence(EssenceWarsPlugin plugin) {
        super(EssenceType.INFERNO);
        this.plugin = plugin;
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @Override
    public String getDisplayName() {
        return "Inferno";
    }

    @Override
    public void usePrimary(Player player, PlayerEssenceData data) {
        if (isDepleted(data)) {
            player.sendMessage("§8[Essence] §7Your essence is depleted.");
            return;
        }
        String key = "inferno_primary";
        EnergyState state = data.getEnergyState();
        if (data.isOnCooldown(key)) {
            long ms = data.getRemainingCooldown(key);
            player.sendMessage(
                "§8[Essence] §7Ability on cooldown: " + (ms / 1000.0) + "s"
            );
            return;
        }
        UUID id = player.getUniqueId();
        Deque<FlameMine> list = mines.computeIfAbsent(id, u ->
            new ArrayDeque<>()
        );
        if (list.size() >= 5) {
            // Increased from 3 to 5 mines
            FlameMine oldest = list.pollFirst();
            if (oldest != null) {
                oldest.cancel();
            }
        }
        Location loc = player
            .getLocation()
            .getBlock()
            .getLocation()
            .add(0.5, 0.0, 0.5);
        FlameMine mine = new FlameMine(player.getUniqueId(), loc);
        list.addLast(mine);
        mine.start();
        player.getWorld().playSound(loc, Sound.ITEM_FIRECHARGE_USE, 1.0F, 1.0F);
        player
            .getWorld()
            .spawnParticle(
                Particle.FLAME,
                loc.clone().add(0, 0.1, 0),
                20,
                0.4,
                0.1,
                0.4,
                0.02
            );
        player.sendMessage("§6[Inferno] §eMine placed (" + list.size() + "/5)");
        long cd = scaleCooldown(state, 6000L); // Reduced from 8s to 6s
        data.setCooldown(key, cd);
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
        player.sendMessage(
            "§6[Inferno] §7Infernal Rebirth is a passive that triggers on death."
        );
    }

    @EventHandler
    public void onInfernoDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player player)) {
            return;
        }
        PlayerEssenceData data = plugin
            .getPlayerDataManager()
            .get(player.getUniqueId());
        if (
            data == null ||
            data.getEssenceType() != EssenceType.INFERNO ||
            data.getTier() != EssenceTier.TIER2
        ) {
            return;
        }
        if (isDepleted(data)) {
            return;
        }
        UUID id = player.getUniqueId();
        if (rebirthing.containsKey(id)) {
            return;
        }
        double finalDamage = event.getFinalDamage();
        if (finalDamage < player.getHealth()) {
            return;
        }
        long now = System.currentTimeMillis();
        long baseCd = 90L * 60L * 1000L; // Reduced from 2 hours to 90 minutes
        Long until = rebirthCooldownUntil.get(id);
        if (until != null && until > now) {
            return;
        }
        event.setCancelled(true);
        double max = player
            .getAttribute(org.bukkit.attribute.Attribute.GENERIC_MAX_HEALTH)
            .getValue();
        player.setHealth(Math.max(1.0, max * 0.3)); // Increased from 20% to 30%
        player.setFireTicks(0);
        player.addPotionEffect(
            new org.bukkit.potion.PotionEffect(
                org.bukkit.potion.PotionEffectType.FIRE_RESISTANCE,
                20 * 8,
                0,
                false,
                false,
                true
            )
        );
        Location deathLoc = player.getLocation().clone();
        Player killer = null;
        if (
            event instanceof EntityDamageByEntityEvent ed &&
            ed.getDamager() instanceof Player k
        ) {
            killer = k;
        }
        if (killer != null) {
            killer.sendMessage(
                "§6[Inferno] §c" + player.getName() + " is regenerating... RUN!"
            );
        }
        player.sendMessage(
            "§6[Inferno] §eInfernal Rebirth activated! Regenerating..."
        );
        rebirthCooldownUntil.put(id, now + baseCd);
        rebirthing.put(id, new RebirthState(deathLoc, now + 3000L));
        deathLoc
            .getWorld()
            .spawnParticle(Particle.FLAME, deathLoc, 60, 1.2, 0.8, 1.2, 0.08);
        deathLoc
            .getWorld()
            .playSound(deathLoc, Sound.BLOCK_FIRE_AMBIENT, 1.5F, 0.6F);

        new BukkitRunnable() {
            @Override
            public void run() {
                RebirthState state = rebirthing.remove(id);
                if (state == null) {
                    return;
                }
                if (!player.isOnline()) {
                    return;
                }
                Location loc = state.location;
                player.teleport(loc);
                double restored = max * 0.6; // Increased from 50% to 60%
                player.setHealth(Math.max(1.0, Math.min(max, restored)));
                loc
                    .getWorld()
                    .spawnParticle(
                        Particle.EXPLOSION_LARGE,
                        loc,
                        50,
                        2.5,
                        1.5,
                        2.5,
                        0.1
                    );
                loc
                    .getWorld()
                    .playSound(loc, Sound.ENTITY_GENERIC_EXPLODE, 1.5F, 0.8F);

                double radius = 10.0; // Increased from 8.0
                double damage = 20.0; // Increased from 15.0
                for (Entity e : loc
                    .getWorld()
                    .getNearbyEntities(loc, radius, radius, radius)) {
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
                    // Direct damage application to bypass fire resistance
                    le.damage(damage, player);
                    le.setFireTicks(20 * 10); // Increased from 5s to 10s
                    // Knockback
                    Vector knock = le
                        .getLocation()
                        .toVector()
                        .subtract(loc.toVector())
                        .normalize()
                        .multiply(1.5);
                    knock.setY(0.8);
                    le.setVelocity(knock);
                }
                player.sendMessage(
                    "§6[Inferno] §eYou have risen from the flames!"
                );
            }
        }
            .runTaskLater(plugin, 60L);
    }

    private class FlameMine {

        private final UUID owner;
        private final Location location;
        private BukkitRunnable task;

        FlameMine(UUID owner, Location location) {
            this.owner = owner;
            this.location = location;
        }

        void start() {
            task = new BukkitRunnable() {
                int ticks = 0;

                @Override
                public void run() {
                    if (ticks++ >= 20 * 45) {
                        // Increased from 30s to 45s
                        cancel();
                        removeSelf();
                        return;
                    }
                    location
                        .getWorld()
                        .spawnParticle(
                            Particle.SMALL_FLAME,
                            location.clone().add(0, 0.1, 0),
                            6,
                            0.25,
                            0.05,
                            0.25,
                            0.01
                        );
                    for (Entity e : location
                        .getWorld()
                        .getNearbyEntities(location, 2.5, 1.5, 2.5)) {
                        // Increased radius
                        if (!(e instanceof LivingEntity le)) {
                            continue;
                        }
                        Player ownerPlayer = plugin
                            .getServer()
                            .getPlayer(owner);
                        if (ownerPlayer != null) {
                            if (le.equals(ownerPlayer)) {
                                continue;
                            }
                            if (
                                le instanceof Player other &&
                                plugin
                                    .getTeamManager()
                                    .areTeammates(ownerPlayer, other)
                            ) {
                                continue;
                            }
                        }
                        explode();
                        cancel();
                        removeSelf();
                        return;
                    }
                }
            };
            task.runTaskTimer(plugin, 0L, 4L);
        }

        void explode() {
            Location loc = location.clone().add(0, 0.1, 0);
            loc
                .getWorld()
                .spawnParticle(
                    Particle.EXPLOSION_LARGE,
                    loc,
                    30,
                    2.0,
                    1.2,
                    2.0,
                    0.1
                );
            loc
                .getWorld()
                .spawnParticle(Particle.FLAME, loc, 60, 2.0, 1.0, 2.0, 0.1);
            loc
                .getWorld()
                .playSound(loc, Sound.ENTITY_GENERIC_EXPLODE, 1.3F, 0.9F);
            Player ownerPlayer = plugin.getServer().getPlayer(owner);
            EnergyState state = EnergyState.RADIANT;
            if (ownerPlayer != null) {
                PlayerEssenceData data = plugin
                    .getPlayerDataManager()
                    .get(ownerPlayer.getUniqueId());
                if (data != null) {
                    state = data.getEnergyState();
                }
            }
            double damage = scaleDamage(state, 14.0); // Increased from 10.0
            for (Entity e : loc
                .getWorld()
                .getNearbyEntities(loc, 5.0, 2.5, 5.0)) {
                // Increased radius
                if (!(e instanceof LivingEntity le)) {
                    continue;
                }
                if (ownerPlayer != null) {
                    if (le.equals(ownerPlayer)) {
                        continue;
                    }
                    if (
                        le instanceof Player other &&
                        plugin.getTeamManager().areTeammates(ownerPlayer, other)
                    ) {
                        continue;
                    }
                }
                // Direct damage to bypass fire resistance
                le.damage(damage, ownerPlayer);
                le.setFireTicks(20 * 12); // Increased from 8s to 12s
                Vector v = le.getVelocity();
                if (v.getY() < 0.8) {
                    v.setY(0.8);
                }
                le.setVelocity(v);
            }
        }

        void cancel() {
            if (task != null) {
                task.cancel();
            }
        }

        void removeSelf() {
            Deque<FlameMine> list = mines.get(owner);
            if (list != null) {
                list.remove(this);
            }
        }
    }

    private static class RebirthState {

        final Location location;
        final long until;

        RebirthState(Location location, long until) {
            this.location = location;
            this.until = until;
        }
    }
}
