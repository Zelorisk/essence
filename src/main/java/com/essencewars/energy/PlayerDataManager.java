package com.essencewars.energy;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import com.essencewars.EssenceWarsPlugin;
import com.essencewars.essence.EssenceTier;
import com.essencewars.essence.EssenceType;

public class PlayerDataManager {

    private final EssenceWarsPlugin plugin;
    private final Map<UUID, PlayerEssenceData> data = new HashMap<>();
    private final File dataFile;
    private FileConfiguration config;

    public PlayerDataManager(EssenceWarsPlugin plugin) {
        this.plugin = plugin;
        this.dataFile = new File(plugin.getDataFolder(), "playerdata.yml");
        reload();
    }

    public void reload() {
        if (!dataFile.exists()) {
            try {
                dataFile.getParentFile().mkdirs();
                dataFile.createNewFile();
            } catch (IOException ignored) {
            }
        }
        this.config = YamlConfiguration.loadConfiguration(dataFile);
    }

    public void loadAll() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            getOrCreate(player);
        }
    }

    public void saveAll() {
        for (PlayerEssenceData ped : data.values()) {
            save(ped);
        }
        try {
            config.save(dataFile);
        } catch (IOException ignored) {
        }
    }

    public PlayerEssenceData getOrCreate(Player player) {
        return getOrCreate(player.getUniqueId());
    }

    public PlayerEssenceData getOrCreate(UUID uuid) {
        PlayerEssenceData existing = data.get(uuid);
        if (existing != null) {
            return existing;
        }
        PlayerEssenceData loaded = load(uuid);
        if (loaded != null) {
            data.put(uuid, loaded);
            return loaded;
        }
        int firstEnergy = plugin.getConfig().getInt("starting-energy", 5);
        PlayerEssenceData created = new PlayerEssenceData(uuid, null, EssenceTier.TIER1, firstEnergy);
        data.put(uuid, created);
        save(created);
        return created;
    }

    public PlayerEssenceData get(UUID uuid) {
        return data.get(uuid);
    }

    public void changeEnergy(Player player, int delta) {
        PlayerEssenceData ped = getOrCreate(player);
        int max = plugin.getConfig().getInt("max-energy", 10);
        ped.addEnergy(delta, max);
        save(ped);
    }

    private PlayerEssenceData load(UUID uuid) {
        String base = "players." + uuid + ".";
        if (!config.contains(base + "energy")) {
            return null;
        }
        int energy = config.getInt(base + "energy");
        String essenceId = config.getString(base + "essence");
        String tierName = config.getString(base + "tier", EssenceTier.TIER1.name());
        EssenceType type = essenceId == null ? null : EssenceType.fromString(essenceId);
        EssenceTier tier = EssenceTier.valueOf(tierName);
        PlayerEssenceData ped = new PlayerEssenceData(uuid, type, tier, energy);
        boolean tutorialSeen = config.getBoolean(base + "tutorialSeen", false);
        ped.setTutorialSeen(tutorialSeen);
        return ped;
    }

    public void save(PlayerEssenceData ped) {
        String base = "players." + ped.getUuid() + ".";
        config.set(base + "energy", ped.getEnergy());
        config.set(base + "essence", ped.getEssenceType() == null ? null : ped.getEssenceType().getId());
        config.set(base + "tier", ped.getTier().name());
        config.set(base + "tutorialSeen", ped.hasSeenTutorial());
    }
}
