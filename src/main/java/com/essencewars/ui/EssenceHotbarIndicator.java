package com.essencewars.ui;

import com.essencewars.EssenceWarsPlugin;
import com.essencewars.energy.PlayerEssenceData;
import com.essencewars.essence.EssenceType;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

public class EssenceHotbarIndicator {

    private final EssenceWarsPlugin plugin;
    private BukkitRunnable updateTask;

    public EssenceHotbarIndicator(EssenceWarsPlugin plugin) {
        this.plugin = plugin;
    }

    public void start() {
        updateTask = new BukkitRunnable() {
            @Override
            public void run() {
                for (Player player : plugin.getServer().getOnlinePlayers()) {
                    updateIndicator(player);
                }
            }
        };
        // Update every 20 ticks (1 second)
        updateTask.runTaskTimer(plugin, 0L, 20L);
    }

    public void stop() {
        if (updateTask != null) {
            updateTask.cancel();
        }
    }

    private void updateIndicator(Player player) {
        PlayerEssenceData data = plugin.getPlayerDataManager().get(player.getUniqueId());
        if (data == null) {
            return;
        }

        EssenceType type = data.getEssenceType();
        if (type == null) {
            // No essence - show empty or default message
            sendActionBar(player, ChatColor.GRAY + "No Essence");
            return;
        }

        // Create essence indicator with symbol and energy
        String symbol = getEssenceSymbol(type);
        String color = getEssenceColor(type);
        String name = type.getDisplayName();
        int energy = data.getEnergy();
        int maxEnergy = plugin.getConfig().getInt("max-energy", 10);

        // Build the indicator: [Symbol] Name | Energy: ■■■■■□□□□□
        StringBuilder indicator = new StringBuilder();
        indicator.append(color).append(ChatColor.BOLD).append(symbol).append(" ");
        indicator.append(color).append(name);
        indicator.append(ChatColor.GRAY).append(" | Energy: ");

        // Energy bar
        for (int i = 0; i < maxEnergy; i++) {
            if (i < energy) {
                indicator.append(color).append("■");
            } else {
                indicator.append(ChatColor.DARK_GRAY).append("□");
            }
        }

        sendActionBar(player, indicator.toString());
    }

    private void sendActionBar(Player player, String message) {
        player.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(message));
    }

    private String getEssenceSymbol(EssenceType type) {
        return switch (type) {
            case VOID -> "◈"; // Diamond symbol for void
            case INFERNO -> "⚡"; // Lightning for inferno
            case NATURE -> "❀"; // Flower for nature
            case ORACLE -> "✦"; // Star for oracle
            case PHANTOM -> "◆"; // Diamond for phantom
            case TITAN -> "⬟"; // Hexagon for titan
            case ARCANE -> "✹"; // Sparkle for arcane
            case DIVINE -> "⚜"; // Fleur-de-lis for divine
        };
    }

    private String getEssenceColor(EssenceType type) {
        return switch (type) {
            case VOID -> ChatColor.DARK_PURPLE + "";
            case INFERNO -> ChatColor.GOLD + "";
            case NATURE -> ChatColor.GREEN + "";
            case ORACLE -> ChatColor.AQUA + "";
            case PHANTOM -> ChatColor.DARK_GRAY + "";
            case TITAN -> ChatColor.GRAY + "";
            case ARCANE -> ChatColor.LIGHT_PURPLE + "";
            case DIVINE -> ChatColor.YELLOW + "" + ChatColor.BOLD;
        };
    }

    /**
     * Force update the indicator for a specific player
     */
    public void updateFor(Player player) {
        updateIndicator(player);
    }
}
