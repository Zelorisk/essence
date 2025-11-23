package com.essencewars;

import java.util.HashSet;
import java.util.Map;
import java.util.HashMap;
import java.util.Set;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.plugin.java.JavaPlugin;

import com.essencewars.commands.EssenceCommand;
import com.essencewars.combat.CombatLogManager;
import com.essencewars.crafting.EssenceCraftManager;
import com.essencewars.debug.AdvancedDebugLogger;
import com.essencewars.debug.DebugCommand;
import com.essencewars.energy.PlayerDataManager;
import com.essencewars.essence.EssenceRegistry;
import com.essencewars.essence.EssenceType;
import com.essencewars.items.EnergyCrystalItem;
import com.essencewars.listeners.EssenceCraftListener;
import com.essencewars.listeners.PlayerListener;
import com.essencewars.team.TeamManager;
import com.essencewars.ui.ScoreboardManager;
import com.essencewars.ui.TabListManager;
import com.essencewars.ui.ConfigGUI;
import com.essencewars.ui.GUIListener;
import com.essencewars.ui.TutorialGUI;
import com.essencewars.ui.CraftingGuideGUI;
import com.essencewars.ui.AdminConfigGUI;
import com.essencewars.ui.RecipeEditorGUI;
import com.essencewars.util.Keys;

public final class EssenceWarsPlugin extends JavaPlugin {

    private static EssenceWarsPlugin instance;

    private PlayerDataManager playerDataManager;
    private EssenceRegistry essenceRegistry;
    private ScoreboardManager scoreboardManager;
    private TabListManager tabListManager;
    private CombatLogManager combatLogManager;
    private TeamManager teamManager;
    private ConfigGUI configGUI;
    private TutorialGUI tutorialGUI;
    private CraftingGuideGUI craftingGuideGUI;
    private AdminConfigGUI adminConfigGUI;
    private RecipeEditorGUI recipeEditorGUI;
    private EssenceCraftManager craftManager;
    private AdvancedDebugLogger debugLogger;

    private final Set<UUID> tutorialImmune = new HashSet<>();
    private final Map<EssenceType, UUID> essenceOwners = new HashMap<>();

    public static EssenceWarsPlugin getInstance() {
        return instance;
    }

    @Override
    public void onEnable() {
        instance = this;
        saveDefaultConfig();

        Keys.init(this);

        boolean debugConsole = getConfig().getBoolean("debug.console", true);
        boolean debugFile = getConfig().getBoolean("debug.file", true);
        this.debugLogger = new AdvancedDebugLogger(this, debugConsole, debugFile);
        String levelName = getConfig().getString("debug.level", "DEBUG");
        try {
            AdvancedDebugLogger.LogLevel level = AdvancedDebugLogger.LogLevel.valueOf(levelName.toUpperCase());
            debugLogger.setLogLevel(level);
        } catch (IllegalArgumentException ignored) {
        }

        this.playerDataManager = new PlayerDataManager(this);
        this.essenceRegistry = new EssenceRegistry(this);
        this.scoreboardManager = new ScoreboardManager(this);
        this.tabListManager = new TabListManager(this);
        this.combatLogManager = new CombatLogManager(this);
        this.teamManager = new TeamManager(this);
        this.configGUI = new ConfigGUI(this);
        this.tutorialGUI = new TutorialGUI(this);
        this.craftingGuideGUI = new CraftingGuideGUI(this);
        this.adminConfigGUI = new AdminConfigGUI(this);
        this.recipeEditorGUI = new RecipeEditorGUI(this);
        this.craftManager = new EssenceCraftManager(this);

        registerRecipes();
        craftManager.registerRecipes();

        getServer().getPluginManager().registerEvents(new PlayerListener(this), this);
        getServer().getPluginManager().registerEvents(new GUIListener(this), this);
        getServer().getPluginManager().registerEvents(new EssenceCraftListener(this, craftManager), this);

        EssenceCommand essenceCommand = new EssenceCommand(this);
        if (getCommand("essence") != null) {
            getCommand("essence").setExecutor(essenceCommand);
            getCommand("essence").setTabCompleter(essenceCommand);
        }
        if (getCommand("debug") != null) {
            getCommand("debug").setExecutor(new DebugCommand(this, debugLogger));
        }

        playerDataManager.loadAll();
        scoreboardManager.start();
        tabListManager.start();
    }

    @Override
    public void onDisable() {
        scoreboardManager.stop();
        tabListManager.stop();
        if (debugLogger != null) {
            debugLogger.logStatistics();
        }
        playerDataManager.saveAll();
        teamManager.save();
    }

    public PlayerDataManager getPlayerDataManager() {
        return playerDataManager;
    }

    public EssenceRegistry getEssenceRegistry() {
        return essenceRegistry;
    }

    public ScoreboardManager getScoreboardManager() {
        return scoreboardManager;
    }

    public TabListManager getTabListManager() {
        return tabListManager;
    }

    public CombatLogManager getCombatLogManager() {
        return combatLogManager;
    }

    public TeamManager getTeamManager() {
        return teamManager;
    }

    public ConfigGUI getConfigGUI() {
        return configGUI;
    }

    public TutorialGUI getTutorialGUI() {
        return tutorialGUI;
    }

    public CraftingGuideGUI getCraftingGuideGUI() {
        return craftingGuideGUI;
    }

    public AdminConfigGUI getAdminConfigGUI() {
        return adminConfigGUI;
    }

    public RecipeEditorGUI getRecipeEditorGUI() {
        return recipeEditorGUI;
    }

    public EssenceCraftManager getCraftManager() {
        return craftManager;
    }

    public AdvancedDebugLogger getDebugLogger() {
        return debugLogger;
    }

    public void setTutorialImmune(Player player, boolean immune) {
        UUID id = player.getUniqueId();
        if (immune) {
            tutorialImmune.add(id);
        } else {
            tutorialImmune.remove(id);
        }
    }

    public boolean isTutorialImmune(Player player) {
        return tutorialImmune.contains(player.getUniqueId());
    }

    public boolean isEssenceOwned(EssenceType type) {
        UUID owner = essenceOwners.get(type);
        if (owner == null) return false;
        // Check if owner is still online
        Player ownerPlayer = Bukkit.getPlayer(owner);
        return ownerPlayer != null && ownerPlayer.isOnline();
    }

    public UUID getEssenceOwner(EssenceType type) {
        return essenceOwners.get(type);
    }

    public void setEssenceOwner(EssenceType type, UUID playerId) {
        if (playerId == null) {
            essenceOwners.remove(type);
        } else {
            essenceOwners.put(type, playerId);
        }
    }

    public void releaseEssence(EssenceType type) {
        essenceOwners.remove(type);
        getLogger().info("Released " + type.name() + " essence - now available for claiming");
    }

    private void registerRecipes() {
        int value = getConfig().getInt("crystal-energy-value", 1);
        ItemStack result = EnergyCrystalItem.create(value);
        NamespacedKey key = new NamespacedKey(this, "energy_crystal");
        ShapedRecipe recipe = new ShapedRecipe(key, result);
        recipe.shape(" A ", "ABA", " C ");
        recipe.setIngredient('A', Material.AMETHYST_SHARD);
        recipe.setIngredient('B', Material.ENDER_PEARL);
        recipe.setIngredient('C', Material.DIAMOND);
        Bukkit.addRecipe(recipe);
    }
}