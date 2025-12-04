package com.essencewars.util;

import org.bukkit.NamespacedKey;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;

public final class Keys {

    public static NamespacedKey CRYSTAL_VALUE;
    public static NamespacedKey ESSENCE_UPGRADER;
    public static final PersistentDataType<Integer, Integer> INTEGER = PersistentDataType.INTEGER;
    public static final PersistentDataType<Byte, Byte> BOOLEAN = PersistentDataType.BYTE;

    private Keys() {
    }

    public static void init(JavaPlugin plugin) {
        CRYSTAL_VALUE = new NamespacedKey(plugin, "crystal_value");
        ESSENCE_UPGRADER = new NamespacedKey(plugin, "essence_upgrader");
    }
}
