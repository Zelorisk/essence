package com.essencewars.ui;

import com.essencewars.EssenceWarsPlugin;
import com.essencewars.energy.PlayerEssenceData;
import com.essencewars.essence.EssenceType;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

public class TabListManager {

    private final EssenceWarsPlugin plugin;
    private BukkitTask task;

    public TabListManager(EssenceWarsPlugin plugin) {
        this.plugin = plugin;
    }

    public void start() {
        if (!plugin.getConfig().getBoolean("tablist.enabled", true)) {
            return;
        }
        // Update every 3 seconds
        this.task = Bukkit.getScheduler().runTaskTimer(
            plugin,
            this::updateAll,
            20L,
            60L
        );
    }

    public void stop() {
        if (task != null) {
            task.cancel();
        }
    }

    private void updateAll() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            updateFor(player);
        }
    }

    public void updateFor(Player player) {
        if (!plugin.getConfig().getBoolean("tablist.enabled", true)) {
            return;
        }

        PlayerEssenceData data = plugin
            .getPlayerDataManager()
            .getOrCreate(player);

        StringBuilder displayName = new StringBuilder();

        // Essence color
        EssenceType essence = data.getEssenceType();
        if (essence != null) {
            displayName.append(getEssenceColor(essence));
        } else {
            displayName.append(ChatColor.GRAY);
        }

        // Player name
        displayName.append(player.getName());

        // Energy indicator
        int energy = data.getEnergy();
        if (energy >= 10) {
            displayName.append(" §6⚡");
        } else if (energy >= 5) {
            displayName.append(" §e⚡");
        } else if (energy >= 3) {
            displayName.append(" §7⚡");
        } else if (energy > 0) {
            displayName.append(" §c⚡");
        } else {
            displayName.append(" §4✖");
        }

        player.setPlayerListName(displayName.toString());
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
}
