package com.essencewars.essence;

public enum EssenceTier {
    TIER1,
    TIER2;

    public String getDisplayName() {
        return this == TIER1 ? "I" : "II";
    }
}
