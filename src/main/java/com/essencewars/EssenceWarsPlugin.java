package com.essencewars;

import java.util.Map;
import java.util.HashMap;
import java.util.HashSet;
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
import com.essencewars.ui.InfuseRecipesGUI;
import com.essencewars.ui.EssenceHotbarIndicator;
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
    private InfuseRecipesGUI infuseRecipesGUI;
    private EssenceHotbarIndicator hotbarIndicator;
    private EssenceCraftManager craftManager;
    private AdvancedDebugLogger debugLogger;

    private final Map<EssenceType, Set<UUID>> essenceOwners = new HashMap<>();

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
        this.infuseRecipesGUI = new InfuseRecipesGUI(this);
        this.hotbarIndicator = new EssenceHotbarIndicator(this);
        this.craftManager = new EssenceCraftManager(this);

        registerRecipes();
        craftManager.registerRecipes();

        getServer().getPluginManager().registerEvents(new PlayerListener(this), this);
        getServer().getPluginManager().registerEvents(new GUIListener(this), this);
        getServer().getPluginManager().registerEvents(new EssenceCraftListener(this, craftManager), this);
        getServer().getPluginManager().registerEvents(new com.essencewars.listeners.EssenceItemListener(this), this);

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
        hotbarIndicator.start();
    }

    @Override
    public void onDisable() {
        scoreboardManager.stop();
        tabListManager.stop();
        hotbarIndicator.stop();
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

    public InfuseRecipesGUI getInfuseRecipesGUI() {
        return infuseRecipesGUI;
    }

    public EssenceHotbarIndicator getHotbarIndicator() {
        return hotbarIndicator;
    }

    public EssenceCraftManager getCraftManager() {
        return craftManager;
    }

    public AdvancedDebugLogger getDebugLogger() {
        return debugLogger;
    }

    public boolean isEssenceOwned(EssenceType type) {
        Set<UUID> owners = essenceOwners.get(type);
        if (owners == null || owners.isEmpty()) return false;
        // Check if any owner is still online
        for (UUID owner : owners) {
            Player ownerPlayer = Bukkit.getPlayer(owner);
            if (ownerPlayer != null && ownerPlayer.isOnline()) {
                return true;
            }
        }
        return false;
    }

    public UUID getEssenceOwner(EssenceType type) {
        // For backwards compatibility, return the first owner (mainly used for Divine)
        Set<UUID> owners = essenceOwners.get(type);
        if (owners == null || owners.isEmpty()) return null;
        return owners.iterator().next();
    }

    public Set<UUID> getEssenceOwners(EssenceType type) {
        return essenceOwners.getOrDefault(type, new HashSet<>());
    }

    public int getEssenceOwnerCount(EssenceType type) {
        Set<UUID> owners = essenceOwners.get(type);
        if (owners == null) return 0;
        // Count only online owners
        int count = 0;
        for (UUID owner : owners) {
            Player p = Bukkit.getPlayer(owner);
            if (p != null && p.isOnline()) {
                count++;
            }
        }
        return count;
    }

    public boolean hasEssence(UUID playerId, EssenceType type) {
        Set<UUID> owners = essenceOwners.get(type);
        return owners != null && owners.contains(playerId);
    }

    public void setEssenceOwner(EssenceType type, UUID playerId) {
        if (playerId == null) {
            essenceOwners.remove(type);
        } else {
            essenceOwners.computeIfAbsent(type, k -> new HashSet<>()).add(playerId);
        }
    }

    public void removeEssenceOwner(EssenceType type, UUID playerId) {
        Set<UUID> owners = essenceOwners.get(type);
        if (owners != null) {
            owners.remove(playerId);
            if (owners.isEmpty()) {
                essenceOwners.remove(type);
            }
        }
    }

    public void releaseEssence(EssenceType type) {
        essenceOwners.remove(type);
        getLogger().info("Released " + type.name() + " essence - now available for claiming");
    }

    private void registerRecipes() {
        // Energy Crystal recipe
        int value = getConfig().getInt("crystal-energy-value", 1);
        ItemStack result = EnergyCrystalItem.create(value);
        NamespacedKey key = new NamespacedKey(this, "energy_crystal");
        ShapedRecipe recipe = new ShapedRecipe(key, result);
        recipe.shape(" A ", "ABA", " C ");
        recipe.setIngredient('A', Material.AMETHYST_SHARD);
        recipe.setIngredient('B', Material.ENDER_PEARL);
        recipe.setIngredient('C', Material.DIAMOND);
        Bukkit.addRecipe(recipe);

        // Essence Upgrader recipe
        ItemStack upgraderResult = com.essencewars.items.EssenceUpgraderItem.create();
        NamespacedKey upgraderKey = new NamespacedKey(this, "essence_upgrader");
        ShapedRecipe upgraderRecipe = new ShapedRecipe(upgraderKey, upgraderResult);
        upgraderRecipe.shape("DED", "GNG", "DBD");
        upgraderRecipe.setIngredient('D', Material.DIAMOND);
        upgraderRecipe.setIngredient('E', Material.ENCHANTED_GOLDEN_APPLE);
        upgraderRecipe.setIngredient('G', Material.GOLD_BLOCK);
        upgraderRecipe.setIngredient('N', Material.NETHER_STAR);
        upgraderRecipe.setIngredient('B', Material.BLAZE_ROD);
        Bukkit.addRecipe(upgraderRecipe);
    }
}