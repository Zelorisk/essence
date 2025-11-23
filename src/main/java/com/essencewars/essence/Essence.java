package com.essencewars.essence;

import org.bukkit.entity.Player;

import com.essencewars.energy.EnergyState;
import com.essencewars.energy.PlayerEssenceData;

public abstract class Essence {

    private final EssenceType type;

    protected Essence(EssenceType type) {
        this.type = type;
    }

    public EssenceType getType() {
        return type;
    }

    public abstract String getDisplayName();

    public abstract void usePrimary(Player player, PlayerEssenceData data);

    public abstract void useSecondary(Player player, PlayerEssenceData data);

    protected boolean canUseSecondary(PlayerEssenceData data) {
        return data.getTier() == EssenceTier.TIER2;
    }

    protected boolean isDepleted(PlayerEssenceData data) {
        return data.getEnergyState() == EnergyState.DEPLETED;
    }

    protected long scaleCooldown(EnergyState state, long baseMillis) {
        return (long) (baseMillis * state.getCooldownMultiplier());
    }

    protected double scaleDamage(EnergyState state, double base) {
        return base * state.getDamageMultiplier();
    }
}
