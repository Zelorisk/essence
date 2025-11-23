package com.essencewars.combat;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import com.essencewars.EssenceWarsPlugin;

public class CombatLogManager {

    private final EssenceWarsPlugin plugin;
    private final Map<UUID, Long> lastCombat = new HashMap<>();
    private final Set<UUID> loggedOutInCombat = new HashSet<>();

    public CombatLogManager(EssenceWarsPlugin plugin) {
        this.plugin = plugin;
    }

    private long getTagMillis() {
        int seconds = plugin.getConfig().getInt("combat-tag-seconds", 15);
        return seconds * 1000L;
    }

    public void tag(Player... players) {
        long now = System.currentTimeMillis();
        long tagMillis = getTagMillis();
        for (Player p : players) {
            if (p == null) {
                continue;
            }
            lastCombat.put(p.getUniqueId(), now);
            p.sendMessage(ChatColor.DARK_RED + "You are now in combat for " + (tagMillis / 1000) + "s.");
        }
    }

    public boolean isInCombat(Player player) {
        if (player == null) {
            return false;
        }
        Long last = lastCombat.get(player.getUniqueId());
        if (last == null) {
            return false;
        }
        return System.currentTimeMillis() - last <= getTagMillis();
    }

    public void handleQuit(Player player) {
        if (player == null) {
            return;
        }
        if (!plugin.getConfig().getBoolean("combat-log-punish-enabled", true)) {
            return;
        }
        if (isInCombat(player)) {
            loggedOutInCombat.add(player.getUniqueId());
            plugin.getLogger().info("Player " + player.getName() + " logged out while in combat; will be punished on join.");
        }
    }

    public void handleJoin(Player player) {
        if (player == null) {
            return;
        }
        if (!plugin.getConfig().getBoolean("combat-log-punish-enabled", true)) {
            return;
        }
        if (loggedOutInCombat.remove(player.getUniqueId())) {
            player.sendMessage(ChatColor.DARK_RED + "You logged out during combat and have been punished.");
            plugin.getLogger().info("Punishing combat logger " + player.getName() + " with death.");
            player.setHealth(0.0);
        }
    }
}
