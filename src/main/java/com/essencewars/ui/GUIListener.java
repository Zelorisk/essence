package com.essencewars.ui;

import com.essencewars.EssenceWarsPlugin;
import com.essencewars.energy.PlayerEssenceData;
import com.essencewars.essence.EssenceType;
import com.essencewars.ui.TutorialGUI;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class GUIListener implements Listener {

    private final EssenceWarsPlugin plugin;

    public GUIListener(EssenceWarsPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (event.getView() == null || event.getView().getTitle() == null) {
            return;
        }
        if (!"§5Essence Settings".equals(event.getView().getTitle())) {
            return;
        }

        event.setCancelled(true);

        if (event.getCurrentItem() == null) {
            return;
        }
        if (!(event.getWhoClicked() instanceof Player player)) {
            return;
        }

        PlayerEssenceData data = plugin
            .getPlayerDataManager()
            .getOrCreate(player);
        EssenceType type = data.getEssenceType();
        if (type == null) return;

        FileConfiguration config = plugin.getConfig();
        String basePath = "abilities." + type.getId() + ".";
        ClickType click = event.getClick();

        // Universal options
        if (event.getSlot() == 9) {
            // Offhand key toggle
            boolean current = config.getBoolean(
                "use-offhand-key-for-abilities",
                true
            );
            config.set("use-offhand-key-for-abilities", !current);
            plugin.saveConfig();
            player.sendMessage("§e§l[ESSENCE CONFIG]");
            player.sendMessage(
                "§e[Essence] §7Offhand key (F) now " +
                    (!current ? "§acasts abilities §7(Press F to use powers)" : "§cswaps items §7(Use /essence primary or /essence secondary for abilities)")
            );
            player.playSound(player.getLocation(), org.bukkit.Sound.BLOCK_NOTE_BLOCK_PLING, 1.0F, 1.0F);
            plugin.getConfigGUI().open(player);
            return;
        }

        // Essence-specific options
        handleEssenceSpecificClick(
            player,
            type,
            basePath,
            event.getSlot(),
            click
        );
    }

    private void handleEssenceSpecificClick(
        Player player,
        EssenceType type,
        String basePath,
        int slot,
        ClickType click
    ) {
        FileConfiguration config = plugin.getConfig();

        switch (type) {
            case VOID:
                handleVoidClick(player, basePath, slot, click);
                break;
            case INFERNO:
                handleInfernoClick(player, basePath, slot, click);
                break;
            case NATURE:
                handleNatureClick(player, basePath, slot, click);
                break;
            case TITAN:
                handleTitanClick(player, basePath, slot, click);
                break;
            case PHANTOM:
                handlePhantomClick(player, basePath, slot, click);
                break;
            case ORACLE:
                handleOracleClick(player, basePath, slot, click);
                break;
            case ARCANE:
                handleArcaneClick(player, basePath, slot, click);
                break;
            case DIVINE:
                handleDivineClick(player, basePath, slot, click);
                break;
        }
    }

    private void handleVoidClick(
        Player player,
        String path,
        int slot,
        ClickType click
    ) {
        FileConfiguration config = plugin.getConfig();
        if (slot == 11) {
            double current = config.getDouble(path + "pull-strength", 1.5);
            if (click == ClickType.LEFT) current += 0.1;
            else if (click == ClickType.RIGHT) current = Math.max(
                0.5,
                current - 0.1
            );
            config.set(path + "pull-strength", current);
            plugin.saveConfig();
            plugin.getConfigGUI().open(player);
        } else if (slot == 15) {
            double current = config.getDouble(path + "execute-threshold", 0.4);
            if (click == ClickType.LEFT) current = Math.min(
                1.0,
                current + 0.05
            );
            else if (click == ClickType.RIGHT) current = Math.max(
                0.1,
                current - 0.05
            );
            config.set(path + "execute-threshold", current);
            plugin.saveConfig();
            plugin.getConfigGUI().open(player);
        }
    }

    private void handleInfernoClick(
        Player player,
        String path,
        int slot,
        ClickType click
    ) {
        FileConfiguration config = plugin.getConfig();
        if (slot == 11) {
            int current = config.getInt(path + "max-mines", 5);
            if (click == ClickType.LEFT) current++;
            else if (click == ClickType.RIGHT) current = Math.max(
                1,
                current - 1
            );
            config.set(path + "max-mines", current);
            plugin.saveConfig();
            plugin.getConfigGUI().open(player);
        } else if (slot == 15) {
            int current = config.getInt(path + "rebirth-cooldown-minutes", 90);
            if (click == ClickType.LEFT) current += 5;
            else if (click == ClickType.RIGHT) current = Math.max(
                30,
                current - 5
            );
            config.set(path + "rebirth-cooldown-minutes", current);
            plugin.saveConfig();
            plugin.getConfigGUI().open(player);
        }
    }

    private void handleNatureClick(
        Player player,
        String path,
        int slot,
        ClickType click
    ) {
        FileConfiguration config = plugin.getConfig();
        if (slot == 11) {
            int current = config.getInt(path + "poison-level", 1);
            if (click == ClickType.LEFT) current = Math.min(4, current + 1);
            else if (click == ClickType.RIGHT) current = Math.max(
                0,
                current - 1
            );
            config.set(path + "poison-level", current);
            plugin.saveConfig();
            plugin.getConfigGUI().open(player);
        } else if (slot == 15) {
            int current = config.getInt(path + "primal-surge-seconds", 10);
            if (click == ClickType.LEFT) current++;
            else if (click == ClickType.RIGHT) current = Math.max(
                3,
                current - 1
            );
            config.set(path + "primal-surge-seconds", current);
            plugin.saveConfig();
            plugin.getConfigGUI().open(player);
        }
    }

    private void handleTitanClick(
        Player player,
        String path,
        int slot,
        ClickType click
    ) {
        FileConfiguration config = plugin.getConfig();
        if (slot == 11) {
            double current = config.getDouble(path + "slam-range", 12.0);
            if (click == ClickType.LEFT) current += 0.5;
            else if (click == ClickType.RIGHT) current = Math.max(
                5.0,
                current - 0.5
            );
            config.set(path + "slam-range", current);
            plugin.saveConfig();
            plugin.getConfigGUI().open(player);
        } else if (slot == 15) {
            int current = config.getInt(path + "colossus-duration-seconds", 8);
            if (click == ClickType.LEFT) current++;
            else if (click == ClickType.RIGHT) current = Math.max(
                3,
                current - 1
            );
            config.set(path + "colossus-duration-seconds", current);
            plugin.saveConfig();
            plugin.getConfigGUI().open(player);
        }
    }

    private void handlePhantomClick(
        Player player,
        String path,
        int slot,
        ClickType click
    ) {
        FileConfiguration config = plugin.getConfig();
        if (slot == 11) {
            int current = config.getInt(
                path + "shadowmeld-duration-seconds",
                6
            );
            if (click == ClickType.LEFT) current++;
            else if (click == ClickType.RIGHT) current = Math.max(
                2,
                current - 1
            );
            config.set(path + "shadowmeld-duration-seconds", current);
            plugin.saveConfig();
            plugin.getConfigGUI().open(player);
        } else if (slot == 15) {
            double current = config.getDouble(
                path + "execution-bonus-damage",
                12.0
            );
            if (click == ClickType.LEFT) current++;
            else if (click == ClickType.RIGHT) current = Math.max(
                5.0,
                current - 1
            );
            config.set(path + "execution-bonus-damage", current);
            plugin.saveConfig();
            plugin.getConfigGUI().open(player);
        }
    }

    private void handleOracleClick(
        Player player,
        String path,
        int slot,
        ClickType click
    ) {
        FileConfiguration config = plugin.getConfig();
        if (slot == 11) {
            double current = config.getDouble(path + "vision-range", 50.0);
            if (click == ClickType.LEFT) current += 5;
            else if (click == ClickType.RIGHT) current = Math.max(
                10.0,
                current - 5
            );
            config.set(path + "vision-range", current);
            plugin.saveConfig();
            plugin.getConfigGUI().open(player);
        } else if (slot == 15) {
            int current = config.getInt(path + "curse-duration-seconds", 12);
            if (click == ClickType.LEFT) current++;
            else if (click == ClickType.RIGHT) current = Math.max(
                5,
                current - 1
            );
            config.set(path + "curse-duration-seconds", current);
            plugin.saveConfig();
            plugin.getConfigGUI().open(player);
        }
    }

    private void handleArcaneClick(
        Player player,
        String path,
        int slot,
        ClickType click
    ) {
        FileConfiguration config = plugin.getConfig();
        if (slot == 11) {
            int current = config.getInt(path + "dice-duration-minutes", 15);
            if (click == ClickType.LEFT) current += 5;
            else if (click == ClickType.RIGHT) current = Math.max(
                5,
                current - 5
            );
            config.set(path + "dice-duration-minutes", current);
            plugin.saveConfig();
            plugin.getConfigGUI().open(player);
        } else if (slot == 15) {
            int current = config.getInt(path + "teleport-radius", 10000);
            if (click == ClickType.LEFT) current += 1000;
            else if (click == ClickType.RIGHT) current = Math.max(
                1000,
                current - 1000
            );
            config.set(path + "teleport-radius", current);
            plugin.saveConfig();
            plugin.getConfigGUI().open(player);
        }
    }

    private void handleDivineClick(
        Player player,
        String path,
        int slot,
        ClickType click
    ) {
        FileConfiguration config = plugin.getConfig();
        if (slot == 11) {
            double current = config.getDouble(
                path + "omnipotent-strike-radius",
                15.0
            );
            if (click == ClickType.LEFT) current++;
            else if (click == ClickType.RIGHT) current = Math.max(
                5.0,
                current - 1
            );
            config.set(path + "omnipotent-strike-radius", current);
            plugin.saveConfig();
            plugin.getConfigGUI().open(player);
        } else if (slot == 15) {
            int current = config.getInt(
                path + "dragon-form-duration-seconds",
                20
            );
            if (click == ClickType.LEFT) current += 2;
            else if (click == ClickType.RIGHT) current = Math.max(
                10,
                current - 2
            );
            config.set(path + "dragon-form-duration-seconds", current);
            plugin.saveConfig();
            plugin.getConfigGUI().open(player);
        }
    }

    @EventHandler
    public void onTutorialClick(InventoryClickEvent event) {
        if (event.getView() == null || event.getView().getTitle() == null) {
            return;
        }
        if (!TutorialGUI.TITLE.equals(event.getView().getTitle())) {
            return;
        }

        event.setCancelled(true);

        if (!(event.getWhoClicked() instanceof Player player)) {
            return;
        }
        if (event.getCurrentItem() == null) {
            return;
        }

        if (event.getSlot() == 22) {
            player.closeInventory();
        }
    }

    @EventHandler
    public void onTutorialClose(InventoryCloseEvent event) {
        if (event.getView() == null || event.getView().getTitle() == null) {
            return;
        }
        if (!TutorialGUI.TITLE.equals(event.getView().getTitle())) {
            return;
        }
        if (!(event.getPlayer() instanceof Player player)) {
            return;
        }
    }

    @EventHandler
    public void onAdminConfigClick(InventoryClickEvent event) {
        if (event.getView() == null || event.getView().getTitle() == null) {
            return;
        }
        String title = event.getView().getTitle();
        if (!(event.getWhoClicked() instanceof Player player)) {
            return;
        }
        if (!player.isOp()) {
            return;
        }
        
        // Admin Config menu
        if (title.equals(ChatColor.DARK_PURPLE + "Admin Config")) {
            event.setCancelled(true);
            if (event.getCurrentItem() == null) return;
            
            if (event.getSlot() == 11) {
                // Cooldown editor
                plugin.getAdminConfigGUI().openCooldownEditor(player);
            } else if (event.getSlot() == 15) {
                // Recipe editor
                plugin.getRecipeEditorGUI().open(player);
            }
            return;
        }
        
        // Cooldown Editor
        if (title.equals(ChatColor.DARK_PURPLE + "Cooldown Editor")) {
            event.setCancelled(true);
            if (event.getCurrentItem() == null) return;
            
            if (event.getSlot() == 49) {
                // Back button
                plugin.getAdminConfigGUI().open(player);
                return;
            }
            
            // Get essence type from slot
            EssenceType type = getEssenceTypeFromSlot(event.getSlot());
            if (type != null) {
                boolean shift = event.getClick().isShiftClick();
                boolean left = event.getClick().isLeftClick();
                int change = left ? -1 : 1;
                plugin.getAdminConfigGUI().adjustCooldown(player, type, !shift, change);
            }
            return;
        }
        
        // Recipe Editor main menu
        if (title.equals(ChatColor.DARK_PURPLE + "Recipe Editor")) {
            event.setCancelled(true);
            if (event.getCurrentItem() == null) return;
            
            EssenceType type = getEssenceTypeFromSlot(event.getSlot());
            if (type != null) {
                plugin.getRecipeEditorGUI().openRecipeEdit(player, type);
            }
            return;
        }
        
        // Recipe Edit screen
        if (title.startsWith(ChatColor.DARK_PURPLE + "Edit Recipe: ")) {
            if (event.getCurrentItem() == null) return;
            
            // Allow placing items in crafting grid
            int slot = event.getSlot();
            if (slot == 10 || slot == 11 || slot == 12 ||
                slot == 19 || slot == 20 || slot == 21 ||
                slot == 28 || slot == 29 || slot == 30) {
                // Allow editing crafting grid
                return;
            }
            
            event.setCancelled(true);
            
            if (slot == 48) {
                // Save button
                String typeName = title.replace(ChatColor.DARK_PURPLE + "Edit Recipe: ", "");
                EssenceType type = EssenceType.fromString(typeName.toLowerCase());
                if (type != null) {
                    plugin.getRecipeEditorGUI().saveRecipe(player, type, event.getInventory());
                }
            } else if (slot == 50) {
                // Cancel button
                plugin.getRecipeEditorGUI().open(player);
            }
            return;
        }
    }
    
    private EssenceType getEssenceTypeFromSlot(int slot) {
        return switch (slot) {
            case 10 -> EssenceType.VOID;
            case 11 -> EssenceType.INFERNO;
            case 12 -> EssenceType.NATURE;
            case 13 -> EssenceType.ORACLE;
            case 14 -> EssenceType.PHANTOM;
            case 15 -> EssenceType.TITAN;
            case 16 -> EssenceType.ARCANE;
            case 19 -> EssenceType.VOID;
            case 20 -> EssenceType.INFERNO;
            case 21 -> EssenceType.NATURE;
            case 22 -> EssenceType.ORACLE;
            case 23 -> EssenceType.PHANTOM;
            case 24 -> EssenceType.TITAN;
            case 25 -> EssenceType.ARCANE;
            case 28 -> EssenceType.DIVINE;
            case 29 -> EssenceType.DIVINE;
            default -> null;
        };
    }
}
