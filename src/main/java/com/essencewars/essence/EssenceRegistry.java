package com.essencewars.essence;

import com.essencewars.EssenceWarsPlugin;
import com.essencewars.essence.impl.ArcaneEssence;
import com.essencewars.essence.impl.DivineEssence;
import com.essencewars.essence.impl.InfernoEssence;
import com.essencewars.essence.impl.NatureEssence;
import com.essencewars.essence.impl.OracleEssence;
import com.essencewars.essence.impl.PhantomEssence;
import com.essencewars.essence.impl.TitanEssence;
import com.essencewars.essence.impl.VoidEssence;
import java.util.EnumMap;
import java.util.Map;

public class EssenceRegistry {

    private final EssenceWarsPlugin plugin;
    private final Map<EssenceType, Essence> essences = new EnumMap<>(
        EssenceType.class
    );

    public EssenceRegistry(EssenceWarsPlugin plugin) {
        this.plugin = plugin;
        registerDefaults();
    }

    private void registerDefaults() {
        register(new VoidEssence(plugin));
        register(new InfernoEssence(plugin));
        register(new NatureEssence(plugin));
        register(new TitanEssence(plugin));
        register(new PhantomEssence(plugin));
        register(new OracleEssence(plugin));
        register(new ArcaneEssence(plugin));
        register(new DivineEssence(plugin));
    }

    public void register(Essence essence) {
        essences.put(essence.getType(), essence);
    }

    public Essence get(EssenceType type) {
        return type == null ? null : essences.get(type);
    }

    public DivineEssence getDivineEssence() {
        Essence essence = get(EssenceType.DIVINE);
        return essence instanceof DivineEssence
            ? (DivineEssence) essence
            : null;
    }
}
