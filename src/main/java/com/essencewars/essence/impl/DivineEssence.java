package com.essencewars.essence.impl;

import com.essencewars.EssenceWarsPlugin;
import com.essencewars.energy.EnergyState;
import com.essencewars.energy.PlayerEssenceData;
import com.essencewars.essence.Essence;
import com.essencewars.essence.EssenceType;
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
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

public class DivineEssence extends Essence implements Listener {

    private final EssenceWarsPlugin plugin;
    private final Map<UUID, Long> godmodeUntil = new HashMap<>();
    private final Map<UUID, Integer> killStreak = new HashMap<>();
    private final Map<UUID, Long> dragonFormUntil = new HashMap<>();

    public DivineEssence(EssenceWarsPlugin plugin) {
        super(EssenceType.DIVINE);
        this.plugin = plugin;
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @Override
    public String getDisplayName() {
        return "§6§lDIVINE";
    }

    @Override
    public void usePrimary(Player player, PlayerEssenceData data) {
        if (isDepleted(data)) {
            player.sendMessage("§8[Essence] §7Your essence is depleted.");
            return;
        }
        String key = "divine_primary";
        EnergyState state = data.getEnergyState();
        if (data.isOnCooldown(key)) {
            long ms = data.getRemainingCooldown(key);
            player.sendMessage(
                "§8[Essence] §7Ability on cooldown: " + (ms / 1000.0) + "s"
            );
            return;
        }

        // DIVINE WRATH - Massive AOE devastation
        Location center = player.getLocation();
        player.sendMessage("§6[DIVINE] §e§lDIVINE WRATH UNLEASHED!");

        center
            .getWorld()
            .playSound(center, Sound.ENTITY_ENDER_DRAGON_GROWL, 2.0F, 0.5F);
        center
            .getWorld()
            .playSound(center, Sound.ENTITY_WITHER_SPAWN, 2.0F, 1.0F);

        // Levitate player briefly
        player.setVelocity(new Vector(0, 1.5, 0));
        player.addPotionEffect(
            new PotionEffect(
                PotionEffectType.DAMAGE_RESISTANCE,
                100,
                4,
                false,
                false,
                true
            )
        );
        player.addPotionEffect(
            new PotionEffect(
                PotionEffectType.REGENERATION,
                100,
                3,
                false,
                false,
                true
            )
        );

        new BukkitRunnable() {
            int wave = 0;

            @Override
            public void run() {
                if (wave >= 5 || !player.isOnline()) {
                    cancel();
                    return;
                }

                double radius = 8.0 + (wave * 4.0); // Expanding waves
                Location loc = center.clone();

                // Visual effects
                for (int i = 0; i < 360; i += 10) {
                    double angle = Math.toRadians(i);
                    double x = Math.cos(angle) * radius;
                    double z = Math.sin(angle) * radius;
                    Location particle = loc.clone().add(x, 0.5, z);
                    loc
                        .getWorld()
                        .spawnParticle(
                            Particle.DRAGON_BREATH,
                            particle,
                            3,
                            0.1,
                            0.1,
                            0.1,
                            0.01
                        );
                    loc
                        .getWorld()
                        .spawnParticle(
                            Particle.FLAME,
                            particle,
                            2,
                            0.1,
                            0.1,
                            0.1,
                            0.01
                        );
                    loc
                        .getWorld()
                        .spawnParticle(
                            Particle.END_ROD,
                            particle,
                            1,
                            0,
                            0,
                            0,
                            0
                        );
                }

                loc
                    .getWorld()
                    .playSound(
                        loc,
                        Sound.ENTITY_GENERIC_EXPLODE,
                        1.5F,
                        0.8F + (wave * 0.1F)
                    );

                // Damage and effects
                double damage = scaleDamage(state, 15.0 + (wave * 5.0));
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

                    le.damage(damage, player);
                    le.setFireTicks(20 * 10);
                    le.addPotionEffect(
                        new PotionEffect(
                            PotionEffectType.WITHER,
                            100,
                            2,
                            false,
                            false,
                            true
                        )
                    );
                    le.addPotionEffect(
                        new PotionEffect(
                            PotionEffectType.SLOW,
                            100,
                            3,
                            false,
                            false,
                            true
                        )
                    );
                    le.addPotionEffect(
                        new PotionEffect(
                            PotionEffectType.WEAKNESS,
                            100,
                            2,
                            false,
                            false,
                            true
                        )
                    );

                    // Massive knockback
                    Vector knock = le
                        .getLocation()
                        .toVector()
                        .subtract(loc.toVector())
                        .normalize()
                        .multiply(2.0);
                    knock.setY(1.2);
                    le.setVelocity(knock);
                }

                wave++;
            }
        }
            .runTaskTimer(plugin, 10L, 10L);

        long cd = scaleCooldown(state, 30000L); // 30s cooldown
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
        String key = "divine_secondary";
        EnergyState state = data.getEnergyState();
        if (data.isOnCooldown(key)) {
            long ms = data.getRemainingCooldown(key);
            player.sendMessage(
                "§8[Essence] §7Ability on cooldown: " + (ms / 1000.0) + "s"
            );
            return;
        }

        // ASCENSION - Become a god for 15 seconds
        UUID id = player.getUniqueId();
        long now = System.currentTimeMillis();
        godmodeUntil.put(id, now + 15000L);

        player.sendMessage("§6[DIVINE] §e§lYOU HAVE ASCENDED TO GODHOOD!");

        // God-tier buffs
        player.addPotionEffect(
            new PotionEffect(
                PotionEffectType.DAMAGE_RESISTANCE,
                300,
                4,
                false,
                false,
                true
            )
        );
        player.addPotionEffect(
            new PotionEffect(
                PotionEffectType.INCREASE_DAMAGE,
                300,
                3,
                false,
                false,
                true
            )
        );
        player.addPotionEffect(
            new PotionEffect(PotionEffectType.SPEED, 300, 3, false, false, true)
        );
        player.addPotionEffect(
            new PotionEffect(
                PotionEffectType.REGENERATION,
                300,
                5,
                false,
                false,
                true
            )
        );
        player.addPotionEffect(
            new PotionEffect(PotionEffectType.JUMP, 300, 3, false, false, true)
        );
        player.addPotionEffect(
            new PotionEffect(
                PotionEffectType.FIRE_RESISTANCE,
                300,
                0,
                false,
                false,
                true
            )
        );
        player.addPotionEffect(
            new PotionEffect(
                PotionEffectType.ABSORPTION,
                300,
                9,
                false,
                false,
                true
            )
        );
        player.addPotionEffect(
            new PotionEffect(
                PotionEffectType.GLOWING,
                300,
                0,
                false,
                false,
                true
            )
        );

        // Full health
        double max = player
            .getAttribute(Attribute.GENERIC_MAX_HEALTH)
            .getValue();
        player.setHealth(max);

        Location loc = player.getLocation();
        loc
            .getWorld()
            .playSound(loc, Sound.ENTITY_ENDER_DRAGON_GROWL, 2.0F, 1.5F);
        loc
            .getWorld()
            .spawnParticle(
                Particle.DRAGON_BREATH,
                loc,
                100,
                2.0,
                2.0,
                2.0,
                0.3
            );
        loc
            .getWorld()
            .spawnParticle(Particle.END_ROD, loc, 50, 1.0, 1.0, 1.0, 0.2);

        // Continuous particles during ascension
        new BukkitRunnable() {
            int ticks = 0;

            @Override
            public void run() {
                if (!player.isOnline() || ticks++ >= 300) {
                    godmodeUntil.remove(id);
                    player.sendMessage(
                        "§6[DIVINE] §7Your ascension has ended."
                    );
                    cancel();
                    return;
                }

                Location pLoc = player.getLocation().add(0, 1, 0);
                pLoc
                    .getWorld()
                    .spawnParticle(
                        Particle.END_ROD,
                        pLoc,
                        5,
                        0.5,
                        0.5,
                        0.5,
                        0.05
                    );
                pLoc
                    .getWorld()
                    .spawnParticle(
                        Particle.FLAME,
                        pLoc,
                        3,
                        0.3,
                        0.3,
                        0.3,
                        0.02
                    );
            }
        }
            .runTaskTimer(plugin, 0L, 5L);

        long cd = scaleCooldown(state, 120000L); // 2 minute cooldown
        data.setCooldown(key, cd);
    }

    @EventHandler
    public void onDivineHit(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player player)) {
            return;
        }

        PlayerEssenceData data = plugin
            .getPlayerDataManager()
            .get(player.getUniqueId());
        if (data == null || data.getEssenceType() != EssenceType.DIVINE) {
            return;
        }

        // During godmode, deal triple damage
        Long until = godmodeUntil.get(player.getUniqueId());
        if (until != null && System.currentTimeMillis() <= until) {
            event.setDamage(event.getDamage() * 3.0);

            if (event.getEntity() instanceof LivingEntity target) {
                target
                    .getWorld()
                    .spawnParticle(
                        Particle.CRIT_MAGIC,
                        target.getLocation().add(0, 1, 0),
                        20,
                        0.5,
                        0.5,
                        0.5,
                        0.1
                    );
            }
        }

        // Always have life-steal
        if (event.getEntity() instanceof LivingEntity) {
            double heal = event.getDamage() * 0.25; // 25% life-steal
            double max = player
                .getAttribute(Attribute.GENERIC_MAX_HEALTH)
                .getValue();
            player.setHealth(Math.min(max, player.getHealth() + heal));
        }
    }

    @EventHandler
    public void onDivineDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player player)) {
            return;
        }

        PlayerEssenceData data = plugin
            .getPlayerDataManager()
            .get(player.getUniqueId());
        if (data == null || data.getEssenceType() != EssenceType.DIVINE) {
            return;
        }

        // During godmode, take 90% less damage
        Long until = godmodeUntil.get(player.getUniqueId());
        if (until != null && System.currentTimeMillis() <= until) {
            event.setDamage(event.getDamage() * 0.1);

            Location loc = player.getLocation();
            loc
                .getWorld()
                .spawnParticle(
                    Particle.ENCHANTMENT_TABLE,
                    loc.add(0, 1, 0),
                    10,
                    0.5,
                    0.5,
                    0.5,
                    1
                );
            loc
                .getWorld()
                .playSound(loc, Sound.BLOCK_ENCHANTMENT_TABLE_USE, 0.5F, 2.0F);
        }

        // Passive: Divine players always have some damage reduction
        if (until == null || System.currentTimeMillis() > until) {
            event.setDamage(event.getDamage() * 0.75); // 25% damage reduction when not in godmode
        }
    }

    @EventHandler
    public void onKill(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player killer)) {
            return;
        }
        if (!(event.getEntity() instanceof Player victim)) {
            return;
        }

        PlayerEssenceData data = plugin
            .getPlayerDataManager()
            .get(killer.getUniqueId());
        if (data == null || data.getEssenceType() != EssenceType.DIVINE) {
            return;
        }

        if (victim.getHealth() - event.getFinalDamage() <= 0) {
            // Kill confirmed
            UUID id = killer.getUniqueId();
            int streak = killStreak.getOrDefault(id, 0) + 1;
            killStreak.put(id, streak);

            if (streak >= 3) {
                // Every 3 kills, reduce all cooldowns by 50%
                killer.sendMessage(
                    "§6[DIVINE] §e§lKILL STREAK! Cooldowns reduced!"
                );
                // This would need to be implemented in the plugin's cooldown system
            }

            // Heal on kill
            double max = killer
                .getAttribute(Attribute.GENERIC_MAX_HEALTH)
                .getValue();
            killer.setHealth(Math.min(max, killer.getHealth() + (max * 0.3)));

            killer
                .getWorld()
                .spawnParticle(
                    Particle.HEART,
                    killer.getLocation().add(0, 2, 0),
                    10,
                    0.5,
                    0.5,
                    0.5,
                    0.1
                );
        }
    }

    // Track dragon form kills for extended duration
    public void onDragonFormKill(Player player) {
        UUID id = player.getUniqueId();
        // Extend dragon form on kill
        Long until = dragonFormUntil.get(id);
        if (until != null && System.currentTimeMillis() <= until) {
            // Extend by 5 seconds per kill
            dragonFormUntil.put(id, until + 5000L);
            player.sendMessage("§6[DIVINE] §e§lDragon form extended!");
        }
    }

    // Get damage multiplier during dragon form
    public double getDragonFormDamageMultiplier(UUID playerId) {
        Long until = dragonFormUntil.get(playerId);
        if (until != null && System.currentTimeMillis() <= until) {
            return 2.0; // Double damage during dragon form
        }
        // During godmode, triple damage
        Long godUntil = godmodeUntil.get(playerId);
        if (godUntil != null && System.currentTimeMillis() <= godUntil) {
            return 3.0;
        }
        return 1.0;
    }
}
