package com.essencewars.energy;

public enum EnergyState {

    RADIANT_PLUS("Radiant +5", 1.6, 0.65, 12),
    RADIANT("Radiant", 1.3, 0.8, 10),
    DIMMED("Dimmed", 1.15, 0.9, 9),
    FRACTURED("Fractured", 1.0, 1.0, 8),
    FADING("Fading", 0.75, 1.25, 6),
    FRAGILE("Fragile", 0.5, 1.5, 4),
    DEPLETED("Depleted", 0.0, 1.8, 3);

    private final String displayName;
    private final double damageMultiplier;
    private final double cooldownMultiplier;
    private final int chargeDecaySeconds;

    EnergyState(String displayName, double damageMultiplier, double cooldownMultiplier, int chargeDecaySeconds) {
        this.displayName = displayName;
        this.damageMultiplier = damageMultiplier;
        this.cooldownMultiplier = cooldownMultiplier;
        this.chargeDecaySeconds = chargeDecaySeconds;
    }

    public String getDisplayName() {
        return displayName;
    }

    public double getDamageMultiplier() {
        return damageMultiplier;
    }

    public double getCooldownMultiplier() {
        return cooldownMultiplier;
    }

    public int getChargeDecaySeconds() {
        return chargeDecaySeconds;
    }

    public static EnergyState fromEnergy(int energy) {
        if (energy <= 0) {
            return DEPLETED;
        }
        if (energy == 1) {
            return FRAGILE;
        }
        if (energy == 2) {
            return FADING;
        }
        if (energy == 3) {
            return FRACTURED;
        }
        if (energy == 4) {
            return DIMMED;
        }
        if (energy >= 10) {
            return RADIANT_PLUS;
        }
        return RADIANT;
    }
}
