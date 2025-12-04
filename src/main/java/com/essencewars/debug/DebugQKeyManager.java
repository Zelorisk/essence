package com.essencewars.debug;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;

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
        logger.log(AdvancedDebugLogger.LogLevel.INFO, "INIT", "Registered offhand swap key listener via PlayerSwapHandItemsEvent");
    }

    @EventHandler
    public void onSwapHandItems(PlayerSwapHandItemsEvent event) {
        Player player = event.getPlayer();

        Map<String, Object> packetData = new HashMap<>();
        packetData.put("cause", "PlayerSwapHandItemsEvent");
        packetData.put("mainHand", event.getMainHandItem() != null ? event.getMainHandItem().getType().name() : "null");
        packetData.put("offHand", event.getOffHandItem() != null ? event.getOffHandItem().getType().name() : "null");
        logger.logPacket("OFFHAND_SWAP", player, "Received", packetData);

        // Treat offhand swap as ability trigger and cancel the physical swap
        event.setCancelled(true);
        logger.log(AdvancedDebugLogger.LogLevel.TRACE, "KEYBIND",
                "Offhand swap cancelled (offhand key mapped to abilities)");

        handleKeybindPress(player);
    }

    public void handleKeybindPress(Player player) {
        UUID uuid = player.getUniqueId();
        long now = System.currentTimeMillis();

        long throttleMs = 150L;
        Long last = keybindCooldowns.get(uuid);
        if (last != null && now - last < throttleMs) {
            logger.log(AdvancedDebugLogger.LogLevel.TRACE, "KEYBIND",
                    player.getName() + " offhand key ignored (throttled)");
            return;
        }
        keybindCooldowns.put(uuid, now);

        PlayerEssenceData data = plugin.getPlayerDataManager().getOrCreate(player);
        EssenceType type = data.getEssenceType();
        if (type == null) {
            logger.logAbility("Offhand-Key", player, "BLOCKED", "No essence selected");
            return;
        }
        if (ArcaneEssence.isSilenced(player.getUniqueId())) {
            logger.logAbility("Offhand-Key", player, "BLOCKED", "Essence silenced");
            player.sendMessage("§5[Arcane] §dYour essence is currently silenced and cannot be used.");
            return;
        }

        Essence essence = plugin.getEssenceRegistry().get(type);
        if (essence == null) {
            logger.logAbility("Offhand-Key", player, "BLOCKED", "Essence not registered: " + type.name());
            return;
        }

        Map<String, Object> state = new HashMap<>();
        state.put("energy", data.getEnergy());
        state.put("state", data.getEnergyState().name());
        state.put("tier", data.getTier() == null ? "null" : data.getTier().name());
        logger.logPlayerState(player, state);

        boolean secondary = player.isSneaking();
        String abilityName = secondary ? "Secondary" : "Primary";
        logger.logAbilityStart("Offhand-" + abilityName, player);

        try {
            if (secondary) {
                essence.useSecondary(player, data);
            } else {
                essence.usePrimary(player, data);
            }
            plugin.getScoreboardManager().updateFor(player);
            logger.logAbilityEnd("Offhand-" + abilityName, player, true);
        } catch (Exception ex) {
            logger.logError("ABILITY", "Error while executing " + abilityName + " via offhand for " + player.getName(), ex);
            logger.logAbilityEnd("Offhand-" + abilityName, player, false);
        }
    }
}
