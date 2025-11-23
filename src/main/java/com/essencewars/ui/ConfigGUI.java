package com.essencewars.ui;

import com.essencewars.EssenceWarsPlugin;
import com.essencewars.energy.PlayerEssenceData;
import com.essencewars.essence.EssenceType;
import java.util.Arrays;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class ConfigGUI {

    private final EssenceWarsPlugin plugin;

    public ConfigGUI(EssenceWarsPlugin plugin) {
        this.plugin = plugin;
    }

    public void open(Player player) {
        PlayerEssenceData data = plugin
            .getPlayerDataManager()
            .getOrCreate(player);
        EssenceType type = data.getEssenceType();

        Inventory gui = Bukkit.createInventory(null, 27, "§5Essence Settings");
        FileConfiguration config = plugin.getConfig();

        // Background
        ItemStack filler = new ItemStack(Material.BLACK_STAINED_GLASS_PANE);
        ItemMeta fillerMeta = filler.getItemMeta();
        if (fillerMeta != null) {
            fillerMeta.setDisplayName(" ");
            fillerMeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
            filler.setItemMeta(fillerMeta);
        }
        for (int i = 0; i < gui.getSize(); i++) {
            gui.setItem(i, filler);
        }

        // Universal options
        addDropKeyToggle(gui, config);
        addHotkeyInfo(gui);

        // Essence-specific options
        if (type != null) {
            addEssenceSpecificOptions(gui, config, type);
        } else {
            ItemStack noEssence = new ItemStack(Material.BARRIER);
            ItemMeta noMeta = noEssence.getItemMeta();
            if (noMeta != null) {
                noMeta.setDisplayName("§cNo Essence Selected");
                noMeta.setLore(
                    Arrays.asList("§7You don't have an essence yet!")
                );
                noEssence.setItemMeta(noMeta);
            }
            gui.setItem(13, noEssence);
        }

        player.openInventory(gui);
    }

    private void addDropKeyToggle(Inventory gui, FileConfiguration config) {
        boolean useDropKey = config.getBoolean(
            "use-drop-key-for-abilities",
            true
        );
        ItemStack toggle = new ItemStack(
            useDropKey ? Material.LIME_DYE : Material.GRAY_DYE
        );
        ItemMeta meta = toggle.getItemMeta();
        if (meta != null) {
            meta.setDisplayName("§eQ Key Function");
            meta.setLore(
                Arrays.asList(
                    "§7Current: " +
                        (useDropKey ? "§aAbilities" : "§cDrop Items"),
                    "",
                    "§7Q = Primary Ability",
                    "§7Shift+Q = Secondary Ability",
                    "",
                    "§eClick to toggle"
                )
            );
            toggle.setItemMeta(meta);
        }
        gui.setItem(9, toggle);
    }

    private void addHotkeyInfo(Inventory gui) {
        ItemStack info = new ItemStack(Material.BOOK);
        ItemMeta meta = info.getItemMeta();
        if (meta != null) {
            meta.setDisplayName("§eHotkey Setup");
            meta.setLore(
                Arrays.asList(
                    "§7Create custom hotkeys:",
                    "§f/essence hotkey primary [name]",
                    "§f/essence hotkey secondary [name]",
                    "",
                    "§7Then bind the name in",
                    "§7Minecraft keybinds!"
                )
            );
            info.setItemMeta(meta);
        }
        gui.setItem(17, info);
    }

    private void addEssenceSpecificOptions(
        Inventory gui,
        FileConfiguration config,
        EssenceType type
    ) {
        String basePath = "abilities." + type.getId() + ".";

        switch (type) {
            case VOID:
                addVoidOptions(gui, config, basePath);
                break;
            case INFERNO:
                addInfernoOptions(gui, config, basePath);
                break;
            case NATURE:
                addNatureOptions(gui, config, basePath);
                break;
            case TITAN:
                addTitanOptions(gui, config, basePath);
                break;
            case PHANTOM:
                addPhantomOptions(gui, config, basePath);
                break;
            case ORACLE:
                addOracleOptions(gui, config, basePath);
                break;
            case ARCANE:
                addArcaneOptions(gui, config, basePath);
                break;
            case DIVINE:
                addDivineOptions(gui, config, basePath);
                break;
        }
    }

    private void addVoidOptions(
        Inventory gui,
        FileConfiguration config,
        String path
    ) {
        // Pull strength
        double pullStrength = config.getDouble(path + "pull-strength", 1.5);
        ItemStack pull = new ItemStack(Material.FISHING_ROD);
        ItemMeta meta = pull.getItemMeta();
        if (meta != null) {
            meta.setDisplayName("§5Singularity Pull Strength");
            meta.setLore(
                Arrays.asList(
                    "§7Current: §f" + String.format("%.1f", pullStrength),
                    "",
                    "§eLeft-click: §7+0.1",
                    "§eRight-click: §7-0.1"
                )
            );
            pull.setItemMeta(meta);
        }
        gui.setItem(11, pull);

        // Execute threshold
        double threshold = config.getDouble(path + "execute-threshold", 0.4);
        ItemStack exec = new ItemStack(Material.DIAMOND_SWORD);
        ItemMeta execMeta = exec.getItemMeta();
        if (execMeta != null) {
            execMeta.setDisplayName("§5Guillotine Execute %");
            execMeta.setLore(
                Arrays.asList(
                    "§7Current: §f" + (int) (threshold * 100) + "%",
                    "",
                    "§eLeft-click: §7+5%",
                    "§eRight-click: §7-5%"
                )
            );
            exec.setItemMeta(execMeta);
        }
        gui.setItem(15, exec);
    }

    private void addInfernoOptions(
        Inventory gui,
        FileConfiguration config,
        String path
    ) {
        // Mine count
        int maxMines = config.getInt(path + "max-mines", 5);
        ItemStack mines = new ItemStack(Material.TNT);
        ItemMeta meta = mines.getItemMeta();
        if (meta != null) {
            meta.setDisplayName("§6Max Flame Mines");
            meta.setLore(
                Arrays.asList(
                    "§7Current: §f" + maxMines,
                    "",
                    "§eLeft-click: §7+1",
                    "§eRight-click: §7-1"
                )
            );
            mines.setItemMeta(meta);
        }
        gui.setItem(11, mines);

        // Rebirth cooldown - Use BLAZE_SPAWN_EGG instead of PHOENIX
        int rebirthCD = config.getInt(path + "rebirth-cooldown-minutes", 90);
        ItemStack rebirth = new ItemStack(Material.BLAZE_SPAWN_EGG);
        ItemMeta rebirthMeta = rebirth.getItemMeta();
        if (rebirthMeta != null) {
            rebirthMeta.setDisplayName("§6Rebirth Cooldown");
            rebirthMeta.setLore(
                Arrays.asList(
                    "§7Current: §f" + rebirthCD + " minutes",
                    "",
                    "§eLeft-click: §7+5 min",
                    "§eRight-click: §7-5 min"
                )
            );
            rebirth.setItemMeta(rebirthMeta);
        }
        gui.setItem(15, rebirth);
    }

    private void addNatureOptions(
        Inventory gui,
        FileConfiguration config,
        String path
    ) {
        // Poison damage
        int poisonLevel = config.getInt(path + "poison-level", 1);
        ItemStack poison = new ItemStack(Material.SPIDER_EYE);
        ItemMeta meta = poison.getItemMeta();
        if (meta != null) {
            meta.setDisplayName("§aPoison Level");
            meta.setLore(
                Arrays.asList(
                    "§7Current: §f" + (poisonLevel + 1),
                    "",
                    "§eLeft-click: §7+1",
                    "§eRight-click: §7-1"
                )
            );
            poison.setItemMeta(meta);
        }
        gui.setItem(11, poison);

        // Primal surge duration
        int surgeDuration = config.getInt(path + "primal-surge-seconds", 10);
        ItemStack surge = new ItemStack(Material.WOLF_SPAWN_EGG);
        ItemMeta surgeMeta = surge.getItemMeta();
        if (surgeMeta != null) {
            surgeMeta.setDisplayName("§aPrimal Surge Duration");
            surgeMeta.setLore(
                Arrays.asList(
                    "§7Current: §f" + surgeDuration + "s",
                    "",
                    "§eLeft-click: §7+1s",
                    "§eRight-click: §7-1s"
                )
            );
            surge.setItemMeta(surgeMeta);
        }
        gui.setItem(15, surge);
    }

    private void addTitanOptions(
        Inventory gui,
        FileConfiguration config,
        String path
    ) {
        // Slam range
        double slamRange = config.getDouble(path + "slam-range", 12.0);
        ItemStack slam = new ItemStack(Material.IRON_BLOCK);
        ItemMeta meta = slam.getItemMeta();
        if (meta != null) {
            meta.setDisplayName("§7Seismic Slam Range");
            meta.setLore(
                Arrays.asList(
                    "§7Current: §f" +
                        String.format("%.1f", slamRange) +
                        " blocks",
                    "",
                    "§eLeft-click: §7+0.5",
                    "§eRight-click: §7-0.5"
                )
            );
            slam.setItemMeta(meta);
        }
        gui.setItem(11, slam);

        // Colossus duration
        int colossusDuration = config.getInt(
            path + "colossus-duration-seconds",
            8
        );
        ItemStack colossus = new ItemStack(Material.BEDROCK);
        ItemMeta colossusMeta = colossus.getItemMeta();
        if (colossusMeta != null) {
            colossusMeta.setDisplayName("§7Colossus Form Duration");
            colossusMeta.setLore(
                Arrays.asList(
                    "§7Current: §f" + colossusDuration + "s",
                    "",
                    "§eLeft-click: §7+1s",
                    "§eRight-click: §7-1s"
                )
            );
            colossus.setItemMeta(colossusMeta);
        }
        gui.setItem(15, colossus);
    }

    private void addPhantomOptions(
        Inventory gui,
        FileConfiguration config,
        String path
    ) {
        // Invisibility duration
        int invisDuration = config.getInt(
            path + "shadowmeld-duration-seconds",
            6
        );
        ItemStack invis = new ItemStack(Material.GLASS);
        ItemMeta meta = invis.getItemMeta();
        if (meta != null) {
            meta.setDisplayName("§8Shadowmeld Duration");
            meta.setLore(
                Arrays.asList(
                    "§7Current: §f" + invisDuration + "s",
                    "",
                    "§eLeft-click: §7+1s",
                    "§eRight-click: §7-1s"
                )
            );
            invis.setItemMeta(meta);
        }
        gui.setItem(11, invis);

        // Execution damage bonus
        double execBonus = config.getDouble(
            path + "execution-bonus-damage",
            12.0
        );
        ItemStack exec = new ItemStack(Material.DIAMOND_SWORD);
        ItemMeta execMeta = exec.getItemMeta();
        if (execMeta != null) {
            execMeta.setDisplayName("§8Execution Strike Damage");
            execMeta.setLore(
                Arrays.asList(
                    "§7Current: §f" + String.format("%.1f", execBonus),
                    "",
                    "§eLeft-click: §7+1",
                    "§eRight-click: §7-1"
                )
            );
            exec.setItemMeta(execMeta);
        }
        gui.setItem(15, exec);
    }

    private void addOracleOptions(
        Inventory gui,
        FileConfiguration config,
        String path
    ) {
        // Vision range
        double visionRange = config.getDouble(path + "vision-range", 50.0);
        ItemStack vision = new ItemStack(Material.ENDER_EYE);
        ItemMeta meta = vision.getItemMeta();
        if (meta != null) {
            meta.setDisplayName("§bProphetic Vision Range");
            meta.setLore(
                Arrays.asList(
                    "§7Current: §f" +
                        String.format("%.0f", visionRange) +
                        " blocks",
                    "",
                    "§eLeft-click: §7+5",
                    "§eRight-click: §7-5"
                )
            );
            vision.setItemMeta(meta);
        }
        gui.setItem(11, vision);

        // Curse duration
        int curseDuration = config.getInt(path + "curse-duration-seconds", 12);
        ItemStack curse = new ItemStack(Material.WITHER_SKELETON_SKULL);
        ItemMeta curseMeta = curse.getItemMeta();
        if (curseMeta != null) {
            curseMeta.setDisplayName("§bFate's Curse Duration");
            curseMeta.setLore(
                Arrays.asList(
                    "§7Current: §f" + curseDuration + "s",
                    "",
                    "§eLeft-click: §7+1s",
                    "§eRight-click: §7-1s"
                )
            );
            curse.setItemMeta(curseMeta);
        }
        gui.setItem(15, curse);
    }

    private void addArcaneOptions(
        Inventory gui,
        FileConfiguration config,
        String path
    ) {
        // Dice duration
        int diceDuration = config.getInt(path + "dice-duration-minutes", 15);
        ItemStack dice = new ItemStack(Material.ENCHANTED_BOOK);
        ItemMeta meta = dice.getItemMeta();
        if (meta != null) {
            meta.setDisplayName("§dArcane Dice Effect Duration");
            meta.setLore(
                Arrays.asList(
                    "§7Current: §f" + diceDuration + " minutes",
                    "",
                    "§eLeft-click: §7+5 min",
                    "§eRight-click: §7-5 min"
                )
            );
            dice.setItemMeta(meta);
        }
        gui.setItem(11, dice);

        // Teleport radius
        int tpRadius = config.getInt(path + "teleport-radius", 10000);
        ItemStack tp = new ItemStack(Material.ENDER_PEARL);
        ItemMeta tpMeta = tp.getItemMeta();
        if (tpMeta != null) {
            tpMeta.setDisplayName("§dEmergency Teleport Radius");
            tpMeta.setLore(
                Arrays.asList(
                    "§7Current: §f" + tpRadius + " blocks",
                    "",
                    "§eLeft-click: §7+1000",
                    "§eRight-click: §7-1000"
                )
            );
            tp.setItemMeta(tpMeta);
        }
        gui.setItem(15, tp);
    }

    private void addDivineOptions(
        Inventory gui,
        FileConfiguration config,
        String path
    ) {
        // Omnipotent Strike radius
        double strikeRadius = config.getDouble(
            path + "omnipotent-strike-radius",
            15.0
        );
        ItemStack strike = new ItemStack(Material.NETHER_STAR);
        ItemMeta meta = strike.getItemMeta();
        if (meta != null) {
            meta.setDisplayName("§6§lOmnipotent Strike Radius");
            meta.setLore(
                Arrays.asList(
                    "§7Current: §f" +
                        String.format("%.1f", strikeRadius) +
                        " blocks",
                    "",
                    "§eLeft-click: §7+1",
                    "§eRight-click: §7-1"
                )
            );
            strike.setItemMeta(meta);
        }
        gui.setItem(11, strike);

        // Dragon form duration
        int dragonDuration = config.getInt(
            path + "dragon-form-duration-seconds",
            20
        );
        ItemStack dragon = new ItemStack(Material.DRAGON_HEAD);
        ItemMeta dragonMeta = dragon.getItemMeta();
        if (dragonMeta != null) {
            dragonMeta.setDisplayName("§6§lDragon Ascension Duration");
            dragonMeta.setLore(
                Arrays.asList(
                    "§7Current: §f" + dragonDuration + "s",
                    "",
                    "§eLeft-click: §7+2s",
                    "§eRight-click: §7-2s"
                )
            );
            dragon.setItemMeta(dragonMeta);
        }
        gui.setItem(15, dragon);
    }
}
