package com.essencewars.energy;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import com.essencewars.essence.EssenceTier;
import com.essencewars.essence.EssenceType;

public class PlayerEssenceData {

    private final UUID uuid;
    private EssenceType essenceType;
    private EssenceTier tier;
    private int energy;
    private long respawnProtectionUntil;
    private long graceUntil;
    private boolean tutorialSeen;
    private final Map<String, Long> cooldowns = new HashMap<>();

    public PlayerEssenceData(UUID uuid, EssenceType essenceType, EssenceTier tier, int energy) {
        this.uuid = uuid;
        this.essenceType = essenceType;
        this.tier = tier;
        this.energy = energy;
    }

    public UUID getUuid() {
        return uuid;
    }

    public EssenceType getEssenceType() {
        return essenceType;
    }

    public void setEssenceType(EssenceType essenceType) {
        this.essenceType = essenceType;
    }

    public EssenceTier getTier() {
        return tier;
    }

    public void setTier(EssenceTier tier) {
        this.tier = tier;
    }

    public int getEnergy() {
        return energy;
    }

    public void setEnergy(int energy) {
        this.energy = Math.max(0, energy);
    }

    public void addEnergy(int amount, int maxEnergy) {
        this.energy = Math.max(0, Math.min(maxEnergy, this.energy + amount));
    }

    public EnergyState getEnergyState() {
        return EnergyState.fromEnergy(energy);
    }

    public long getRespawnProtectionUntil() {
        return respawnProtectionUntil;
    }

    public void setRespawnProtectionUntil(long respawnProtectionUntil) {
        this.respawnProtectionUntil = respawnProtectionUntil;
    }

    public long getGraceUntil() {
        return graceUntil;
    }

    public void setGraceUntil(long graceUntil) {
        this.graceUntil = graceUntil;
    }

    public boolean hasSeenTutorial() {
        return tutorialSeen;
    }

    public void setTutorialSeen(boolean tutorialSeen) {
        this.tutorialSeen = tutorialSeen;
    }

    public boolean isOnCooldown(String key) {
        Long until = cooldowns.get(key);
        return until != null && until > System.currentTimeMillis();
    }

    public long getRemainingCooldown(String key) {
        Long until = cooldowns.get(key);
        if (until == null) {
            return 0L;
        }
        return Math.max(0L, until - System.currentTimeMillis());
    }

    public void setCooldown(String key, long millis) {
        cooldowns.put(key, System.currentTimeMillis() + millis);
    }
}
