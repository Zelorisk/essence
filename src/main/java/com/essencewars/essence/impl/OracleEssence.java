package com.essencewars.essence.impl;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityRegainHealthEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import com.essencewars.EssenceWarsPlugin;
import com.essencewars.energy.EnergyState;
import com.essencewars.energy.PlayerEssenceData;
import com.essencewars.essence.Essence;
import com.essencewars.essence.EssenceTier;
import com.essencewars.essence.EssenceType;

public class OracleEssence extends Essence implements Listener {

    private final EssenceWarsPlugin plugin;
    private final Map<UUID, Long> cursed = new HashMap<>();

    public OracleEssence(EssenceWarsPlugin plugin) {
        super(EssenceType.ORACLE);
        this.plugin = plugin;
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @Override
    public String getDisplayName() {
        return "Oracle";
    }

    @Override
    public void usePrimary(Player player, PlayerEssenceData data) {
        if (isDepleted(data)) {
            player.sendMessage("§8[Essence] §7Your essence is depleted.");
            return;
        }
        String key = "oracle_primary";
        EnergyState state = data.getEnergyState();
        if (data.isOnCooldown(key)) {
            long ms = data.getRemainingCooldown(key);
            player.sendMessage("§8[Essence] §7Ability on cooldown: " + (ms / 1000.0) + "s");
            return;
        }
        double range = 50.0;
        Player closest = null;
        double best = Double.MAX_VALUE;
        int revealed = 0;
        for (Player other : player.getWorld().getPlayers()) {
            if (other.equals(player)) {
                continue;
            }
            if (plugin.getTeamManager().areTeammates(player, other)) {
                continue;
            }
            double distSq = other.getLocation().distanceSquared(player.getLocation());
            if (distSq > range * range) {
                continue;
            }
            other.addPotionEffect(new PotionEffect(PotionEffectType.GLOWING, 20 * 8, 0, false, false, true));
            revealed++;
            if (distSq < best) {
                best = distSq;
                closest = other;
            }
        }
        if (closest == null) {
            player.sendMessage("§8[Essence] §7No enemies in vision range.");
            return;
        }
        player.setCompassTarget(closest.getLocation());
        double distance = Math.sqrt(best);
        player.sendMessage(ChatColor.DARK_AQUA + "Prophetic Vision: " + closest.getName() + ChatColor.GRAY + " is " + String.format("%.1f", distance) + " blocks away. Revealed " + revealed + " enemies.");
        player.getWorld().playSound(player.getLocation(), Sound.BLOCK_ENCHANTMENT_TABLE_USE, 1.0F, 1.5F);
        player.getWorld().spawnParticle(Particle.END_ROD, player.getLocation().add(0, 1.8, 0), 24, 0.4, 0.4, 0.4, 0.05);
        long cd = scaleCooldown(state, 6000L);
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
        String key = "oracle_secondary";
        EnergyState state = data.getEnergyState();
        if (data.isOnCooldown(key)) {
            long ms = data.getRemainingCooldown(key);
            player.sendMessage("§8[Essence] §7Ability on cooldown: " + (ms / 1000.0) + "s");
            return;
        }
        Player target = null;
        double best = Double.MAX_VALUE;
        double range = 30.0;
        for (Player other : player.getWorld().getPlayers()) {
            if (other.equals(player)) {
                continue;
            }
            if (plugin.getTeamManager().areTeammates(player, other)) {
                continue;
            }
            double distSq = other.getLocation().distanceSquared(player.getLocation());
            if (distSq > range * range) {
                continue;
            }
            if (distSq < best) {
                best = distSq;
                target = other;
            }
        }
        if (target == null) {
            player.sendMessage("§8[Essence] §7No enemy to curse.");
            return;
        }
        long until = System.currentTimeMillis() + 12_000L;
        cursed.put(target.getUniqueId(), until);
        target.addPotionEffect(new PotionEffect(PotionEffectType.GLOWING, 20 * 12, 0, false, false, true));
        target.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 20 * 12, 0, false, false, true));
        player.sendMessage(ChatColor.DARK_AQUA + "Fate's Curse applied to " + target.getName() + ".");
        target.sendMessage(ChatColor.DARK_RED + "You have been cursed by an Oracle! You cannot heal and take extra damage.");
        Location c = target.getLocation();
        c.getWorld().playSound(c, Sound.BLOCK_BEACON_ACTIVATE, 1.0F, 1.0F);
        c.getWorld().spawnParticle(Particle.SPELL_INSTANT, c, 40, 1.2, 0.5, 1.2, 0.05);
        long cd = scaleCooldown(state, 15000L);
        data.setCooldown(key, cd);
    }

    @EventHandler
    public void onDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player p)) {
            return;
        }
        Long until = cursed.get(p.getUniqueId());
        if (until == null) {
            return;
        }
        if (System.currentTimeMillis() > until) {
            cursed.remove(p.getUniqueId());
            return;
        }
        event.setDamage(event.getDamage() * 1.25);
    }

    @EventHandler
    public void onRegen(EntityRegainHealthEvent event) {
        if (!(event.getEntity() instanceof Player p)) {
            return;
        }
        Long until = cursed.get(p.getUniqueId());
        if (until == null) {
            return;
        }
        if (System.currentTimeMillis() > until) {
            cursed.remove(p.getUniqueId());
            return;
        }
        event.setCancelled(true);
    }
}
