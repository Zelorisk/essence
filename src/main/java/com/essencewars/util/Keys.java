package com.essencewars.util;

import org.bukkit.NamespacedKey;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;

public final class Keys {

    public static NamespacedKey CRYSTAL_VALUE;
    public static final PersistentDataType<Integer, Integer> INTEGER = PersistentDataType.INTEGER;

    private Keys() {
    }

    public static void init(JavaPlugin plugin) {
        CRYSTAL_VALUE = new NamespacedKey(plugin, "crystal_value");
    }
}
