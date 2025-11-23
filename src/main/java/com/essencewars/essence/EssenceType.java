package com.essencewars.essence;

public enum EssenceType {
    VOID("void", "Void"),
    INFERNO("inferno", "Inferno"),
    NATURE("nature", "Nature"),
    ORACLE("oracle", "Oracle"),
    PHANTOM("phantom", "Phantom"),
    TITAN("titan", "Titan"),
    ARCANE("arcane", "Arcane"),
    DIVINE("divine", "§6§lDIVINE");

    private final String id;
    private final String displayName;

    EssenceType(String id, String displayName) {
        this.id = id;
        this.displayName = displayName;
    }

    public String getId() {
        return id;
    }

    public String getDisplayName() {
        return displayName;
    }

    public static EssenceType fromString(String input) {
        String lower = input.toLowerCase();
        for (EssenceType type : values()) {
            if (
                type.id.equalsIgnoreCase(lower) ||
                type.name().equalsIgnoreCase(lower)
            ) {
                return type;
            }
        }
        return null;
    }
}
