package com.essencewars.essence.impl;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.World;
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

public class ArcaneEssence extends Essence implements Listener {

    private static final long FIFTEEN_MINUTES = 15L * 60L * 1000L;

    private final EssenceWarsPlugin plugin;
    private final Random random = new Random();
    private final Map<UUID, Long> damageDebuffUntil = new HashMap<>();
    private final Map<UUID, Long> silenceReadyUntil = new HashMap<>();
    private static final Map<UUID, Long> silencedUntil = new HashMap<>();

    public ArcaneEssence(EssenceWarsPlugin plugin) {
        super(EssenceType.ARCANE);
        this.plugin = plugin;
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @Override
    public String getDisplayName() {
        return "Arcane";
    }

    @Override
    public void usePrimary(Player player, PlayerEssenceData data) {
        if (isDepleted(data)) {
            player.sendMessage("§8[Essence] §7Your essence is depleted.");
            return;
        }
        String key = "arcane_primary";
        EnergyState state = data.getEnergyState();
        if (data.isOnCooldown(key)) {
            long ms = data.getRemainingCooldown(key);
            player.sendMessage("§8[Essence] §7Ability on cooldown: " + (ms / 1000.0) + "s");
            return;
        }
        UUID id = player.getUniqueId();
        long now = System.currentTimeMillis();
        damageDebuffUntil.remove(id);
        silenceReadyUntil.remove(id);

        int roll = random.nextInt(6) + 1;
        int durationTicks = (int) (FIFTEEN_MINUTES / 50L);
        switch (roll) {
            case 1: // Strength III
                player.addPotionEffect(new PotionEffect(PotionEffectType.INCREASE_DAMAGE, durationTicks, 2, false, false, true));
                player.sendMessage("§5[Arcane] §dDice rolled §l1§d: Strength III for 15 minutes.");
                break;
            case 2: // Speed IV
                player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, durationTicks, 3, false, false, true));
                player.sendMessage("§5[Arcane] §dDice rolled §l2§d: Speed IV for 15 minutes.");
                break;
            case 3: // Silence charge
                silenceReadyUntil.put(id, now + FIFTEEN_MINUTES);
                player.sendMessage("§5[Arcane] §dDice rolled §l3§d: your next hit on a player will silence their essence for 15 minutes.");
                break;
            case 4: // Hero of the Village
                player.addPotionEffect(new PotionEffect(PotionEffectType.HERO_OF_THE_VILLAGE, durationTicks, 0, false, false, true));
                player.sendMessage("§5[Arcane] §dDice rolled §l4§d: Hero of the Village for 15 minutes.");
                break;
            case 5: // Damage debuff
                damageDebuffUntil.put(id, now + FIFTEEN_MINUTES);
                player.sendMessage("§5[Arcane] §dDice rolled §l5§d: your damage is reduced by 35% for 15 minutes.");
                break;
            case 6: // Slowness debuff
                player.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, durationTicks, 0, false, false, true));
                player.sendMessage("§5[Arcane] §dDice rolled §l6§d: Slowness I for 15 minutes.");
                break;
        }
        player.getWorld().playSound(player.getLocation(), Sound.BLOCK_ENCHANTMENT_TABLE_USE, 1.0F, 0.7F);
        player.getWorld().spawnParticle(Particle.ENCHANTMENT_TABLE, player.getLocation().add(0, 1.0, 0), 40, 0.6, 0.8, 0.6, 0.1);
        long baseCd = 30L * 60L * 1000L;
        long cd = scaleCooldown(state, baseCd);
        data.setCooldown(key, cd);
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
        String key = "arcane_secondary";
        EnergyState state = data.getEnergyState();
        if (data.isOnCooldown(key)) {
            long ms = data.getRemainingCooldown(key);
            player.sendMessage("§8[Essence] §7Ability on cooldown: " + (ms / 1000.0) + "s");
            return;
        }
        World world = player.getWorld();
        Location center = world.getSpawnLocation();
        int radius = 10_000;
        Location dest = null;
        for (int i = 0; i < 20 && dest == null; i++) {
            double angle = random.nextDouble() * Math.PI * 2.0;
            double dist = random.nextDouble() * radius;
            int x = center.getBlockX() + (int) Math.round(Math.cos(angle) * dist);
            int z = center.getBlockZ() + (int) Math.round(Math.sin(angle) * dist);
            int y = world.getHighestBlockYAt(x, z) + 1;
            Location candidate = new Location(world, x + 0.5, y, z + 0.5);
            if (!candidate.getBlock().isLiquid()) {
                dest = candidate;
            }
        }
        if (dest == null) {
            player.sendMessage("§5[Arcane] §dTeleport failed to find a safe destination.");
            return;
        }
        Location from = player.getLocation();
        from.getWorld().spawnParticle(Particle.PORTAL, from, 60, 1.0, 1.0, 1.0, 0.2);
        from.getWorld().playSound(from, Sound.ENTITY_ENDERMAN_TELEPORT, 1.0F, 0.6F);
        player.teleport(dest);
        player.setFallDistance(0.0F);
        dest.getWorld().spawnParticle(Particle.PORTAL, dest, 80, 1.2, 1.2, 1.2, 0.3);
        dest.getWorld().playSound(dest, Sound.ENTITY_ENDERMAN_TELEPORT, 1.0F, 1.4F);
        long baseCd = 3L * 60L * 60L * 1000L;
        long cd = scaleCooldown(state, baseCd);
        data.setCooldown(key, cd);
        player.sendMessage("§5[Arcane] §dEmergency teleport used. Cooldown: 3 hours.");
    }

    @EventHandler
    public void onArcaneDamage(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player player)) {
            return;
        }
        if (event.isCancelled()) {
            return;
        }
        UUID id = player.getUniqueId();
        long now = System.currentTimeMillis();
        Long debuff = damageDebuffUntil.get(id);
        if (debuff != null) {
            if (now > debuff) {
                damageDebuffUntil.remove(id);
            } else {
                event.setDamage(event.getDamage() * 0.65);
            }
        }
        Long ready = silenceReadyUntil.get(id);
        if (ready != null) {
            if (now > ready) {
                silenceReadyUntil.remove(id);
            } else if (event.getEntity() instanceof Player target) {
                silenceReadyUntil.remove(id);
                silencedUntil.put(target.getUniqueId(), now + FIFTEEN_MINUTES);
                player.sendMessage("§5[Arcane] §d" + target.getName() + " is silenced for 15 minutes.");
                target.sendMessage("§5[Arcane] §dYour essence has been silenced for 15 minutes.");
            }
        }
    }

    public static boolean isSilenced(UUID id) {
        Long until = silencedUntil.get(id);
        if (until == null) {
            return false;
        }
        long now = System.currentTimeMillis();
        if (now > until) {
            silencedUntil.remove(id);
            return false;
        }
        return true;
    }
}
