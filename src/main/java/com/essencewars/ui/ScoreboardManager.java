package com.essencewars.ui;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Score;
import org.bukkit.scoreboard.Scoreboard;

import com.essencewars.EssenceWarsPlugin;
import com.essencewars.energy.PlayerDataManager;
import com.essencewars.energy.PlayerEssenceData;
import com.essencewars.energy.EnergyState;

public class ScoreboardManager {

    private final EssenceWarsPlugin plugin;
    private final PlayerDataManager dataManager;
    private BukkitTask task;
    private final Map<Player, Scoreboard> boards = new HashMap<>();

    public ScoreboardManager(EssenceWarsPlugin plugin) {
        this.plugin = plugin;
        this.dataManager = plugin.getPlayerDataManager();
    }

    public void start() {
        if (!plugin.getConfig().getBoolean("scoreboard.enabled", true)) {
            return;
        }
        this.task = Bukkit.getScheduler().runTaskTimer(plugin, this::updateAll, 20L, 40L);
    }

    public void stop() {
        if (task != null) {
            task.cancel();
        }
        boards.clear();
    }

    private void updateAll() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            updateFor(player);
        }
    }

    public void updateFor(Player player) {
        if (!plugin.getConfig().getBoolean("scoreboard.enabled", true)) {
            return;
        }
        PlayerEssenceData data = dataManager.getOrCreate(player);
        Scoreboard board = boards.computeIfAbsent(player, p -> Bukkit.getScoreboardManager().getNewScoreboard());

        Objective obj = board.getObjective("essencewars");
        if (obj == null) {
            obj = board.registerNewObjective("essencewars", "dummy", ChatColor.DARK_PURPLE + "Essence Wars");
            obj.setDisplaySlot(DisplaySlot.SIDEBAR);
        }

        obj.setDisplayName(ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("scoreboard.title", "&5&lEssence &d&lWars")));

        // Clear previous lines to avoid duplicates when data changes (e.g. /essence reset)
        for (String entry : board.getEntries()) {
            board.resetScores(entry);
        }

        String essenceName = data.getEssenceType() == null ? "None" : data.getEssenceType().getDisplayName();
        String tierName = data.getTier() == null ? "-" : data.getTier().getDisplayName();
        int energy = data.getEnergy();
        int maxEnergy = plugin.getConfig().getInt("max-energy", 10);
        EnergyState state = data.getEnergyState();

        java.util.List<String> lines = plugin.getConfig().getStringList("scoreboard.lines");
        if (lines.isEmpty()) {
            lines = java.util.List.of(
                    "&8&m----------------",
                    "&7Essence: &d{essence}",
                    "&7Tier: &d{tier}",
                    "&7Energy: &b{energy}&7/&b{max_energy}",
                    "&7State: &d{state}",
                    "&8&m----------------");
        }

        int scoreVal = lines.size();
        for (String raw : lines) {
            String line = raw
                    .replace("{essence}", essenceName)
                    .replace("{tier}", tierName)
                    .replace("{energy}", String.valueOf(energy))
                    .replace("{max_energy}", String.valueOf(maxEnergy))
                    .replace("{state}", state.getDisplayName());
            line = ChatColor.translateAlternateColorCodes('&', line);
            Score score = obj.getScore(line);
            score.setScore(scoreVal--);
        }

        player.setScoreboard(board);
    }
}
