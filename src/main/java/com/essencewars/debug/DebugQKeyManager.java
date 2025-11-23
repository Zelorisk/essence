package com.essencewars.debug;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerDropItemEvent;

import com.essencewars.EssenceWarsPlugin;
import com.essencewars.energy.PlayerEssenceData;
import com.essencewars.essence.Essence;
import com.essencewars.essence.EssenceType;
import com.essencewars.essence.impl.ArcaneEssence;

public class DebugQKeyManager implements Listener {

    private final EssenceWarsPlugin plugin;
    private final AdvancedDebugLogger logger;
    private final HashMap<UUID, Long> keybindCooldowns = new HashMap<>();

    public DebugQKeyManager(EssenceWarsPlugin plugin, AdvancedDebugLogger logger) {
        this.plugin = plugin;
        this.logger = logger;
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        logger.log(AdvancedDebugLogger.LogLevel.INFO, "INIT", "Registered Q key listener via PlayerDropItemEvent");
    }

    @EventHandler
    public void onDrop(PlayerDropItemEvent event) {
        Player player = event.getPlayer();

        Map<String, Object> packetData = new HashMap<>();
        packetData.put("cause", "PlayerDropItemEvent");
        packetData.put("item", event.getItemDrop().getItemStack().getType().name());
        logger.logPacket("DROP", player, "Received", packetData);

        // Treat any item drop as Q-key ability trigger and cancel the physical drop
        event.setCancelled(true);
        logger.log(AdvancedDebugLogger.LogLevel.TRACE, "KEYBIND",
                "Drop cancelled (Q key mapped to abilities via drop event)");

        handleKeybindPress(player);
    }

    public void handleKeybindPress(Player player) {
        UUID uuid = player.getUniqueId();
        long now = System.currentTimeMillis();

        long throttleMs = 150L;
        Long last = keybindCooldowns.get(uuid);
        if (last != null && now - last < throttleMs) {
            logger.log(AdvancedDebugLogger.LogLevel.TRACE, "KEYBIND",
                    player.getName() + " Q key ignored (throttled)");
            return;
        }
        keybindCooldowns.put(uuid, now);

        PlayerEssenceData data = plugin.getPlayerDataManager().getOrCreate(player);
        EssenceType type = data.getEssenceType();
        if (type == null) {
            logger.logAbility("Q-Key", player, "BLOCKED", "No essence selected");
            return;
        }
        if (ArcaneEssence.isSilenced(player.getUniqueId())) {
            logger.logAbility("Q-Key", player, "BLOCKED", "Essence silenced");
            player.sendMessage("§5[Arcane] §dYour essence is currently silenced and cannot be used.");
            return;
        }

        Essence essence = plugin.getEssenceRegistry().get(type);
        if (essence == null) {
            logger.logAbility("Q-Key", player, "BLOCKED", "Essence not registered: " + type.name());
            return;
        }

        Map<String, Object> state = new HashMap<>();
        state.put("energy", data.getEnergy());
        state.put("state", data.getEnergyState().name());
        state.put("tier", data.getTier() == null ? "null" : data.getTier().name());
        logger.logPlayerState(player, state);

        boolean secondary = player.isSneaking();
        String abilityName = secondary ? "Secondary" : "Primary";
        logger.logAbilityStart("Q-" + abilityName, player);

        try {
            if (secondary) {
                essence.useSecondary(player, data);
            } else {
                essence.usePrimary(player, data);
            }
            plugin.getScoreboardManager().updateFor(player);
            logger.logAbilityEnd("Q-" + abilityName, player, true);
        } catch (Exception ex) {
            logger.logError("ABILITY", "Error while executing " + abilityName + " via Q for " + player.getName(), ex);
            logger.logAbilityEnd("Q-" + abilityName, player, false);
        }
    }
}
