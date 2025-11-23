# EssenceWars

A Minecraft plugin featuring essence based combat abilities, energy management, and powers.

![Minecraft](https://img.shields.io/badge/Minecraft-1.20.4-brightgreen)
![Spigot](https://img.shields.io/badge/Spigot-API-orange)
![Java](https://img.shields.io/badge/Java-17-blue)

### Key Features

- **7 Unique Essences** - Each with primary and secondary abilities
- **Divine Essence** - Ultimate overpowered essence combining all others
- **Energy System** - Gain energy from kills, lose it from deaths
- **Dynamic Combat** - Energy states affect damage and cooldowns
- **Crafting System** - Difficult but achievable recipes for all essences
- **Team System** - Create teams and fight alongside friends
- **Tutorial System** - Interactive GUI teaching new players
- **Grace Period** - 30-minute protection for new players

## Requirements

- **Minecraft Server**: 1.20.4+
- **Server Software**: Spigot or Paper
- **Java**: 17 or higher
- **Maven**: 3.6+ (for building)

## Installation

1. Download the latest release from the [releases page](../../releases)
2. Place `EssenceWars-1.0.0-SNAPSHOT.jar` in your server's `plugins` folder
3. Restart your server
4. Configure the plugin in `plugins/EssenceWars/config.yml`

## Building from Source

```bash
git clone https://github.com/yourusername/EssenceWars.git
cd EssenceWars
mvn clean package
```

The compiled JAR will be in `target/EssenceWars-1.0.0-SNAPSHOT.jar`

## Essences

### Void Essence
**Theme**: Teleportation and displacement
- **Primary**: Singularity Pull - Pull enemies toward you with 2x stronger force
- **Secondary**: Void Guillotine - Execute targets below 40% HP

### Inferno Essence
**Theme**: Fire and destruction
- **Primary**: Flame Mines - Place up to 5 explosive fire traps
- **Secondary**: Infernal Rebirth - Cheat death and explode (bypasses fire resistance)

### Nature Essence
**Theme**: Poison and life-steal
- **Primary**: Venomous Strikes - Stack poison on enemies (passive)
- **Secondary**: Primal Surge - Become an unstoppable beast with buffs and life-steal

### Oracle Essence
**Theme**: Vision and debuffs
- **Primary**: Prophetic Vision - Reveal and track all nearby enemies
- **Secondary**: Fate's Curse - Curse enemy with damage amplification and no healing

### Phantom Essence
**Theme**: Stealth and assassination
- **Primary**: Shadow Meld - Become invisible with bonus damage
- **Secondary**: Execution Protocol - Mark and execute low-health targets

### Titan Essence
**Theme**: Defense and crowd control
- **Primary**: Seismic Slam - Ground-slam attack with knockback and slow
- **Secondary**: Colossus Form - Become an immovable tank for 8 seconds

### Arcane Essence
**Theme**: Randomness and chaos
- **Primary**: Dice Roll - Random powerful effect (buffs or debuffs)
- **Secondary**: Emergency Teleport - Random long-distance teleport (3-hour cooldown)

### ⚡ DIVINE ESSENCE ⚡
**Theme**: Ultimate overpowered godhood
- **Primary**: Divine Wrath - 5 expanding AOE waves dealing massive damage
- **Secondary**: Ascension - Become a god for 15 seconds
  - 90% damage reduction
  - 300% damage increase
  - 25% passive life-steal
  - Full heal on kills
- **Requires**: All 7 essences + Dragon Egg + Dragon Head

### Player Commands
```
/essence info           - View the tutorial guide
/essence stats [player] - View player statistics
/essence primary        - Cast your primary ability
/essence secondary      - Cast your secondary ability (Tier II required)
/essence withdraw       - Convert 1 energy into a crystal
/essence config         - Open settings GUI
/essence hotkey <type>  - Cast ability via hotkey
/essence team <action>  - Manage teams
```

### Team Commands
```
/essence team create <name>  - Create a new team
/essence team disband        - Disband your team (owner only)
/essence team join <name>    - Join a team
/essence team leave          - Leave your team
/essence team info [player]  - View team information
/essence team list           - List all teams
```

### Admin Commands
```
/essence give <player> <type>                 - Give essence to player
/essence upgrade <player>                     - Upgrade player to Tier II
/essence energy <player> <set|add|remove> <#> - Modify player energy
/essence reset <player>                       - Reset player data
/essence crystal [amount]                     - Give energy crystals
```

### Debug Commands
```
/debug level <level>      - Set log level (TRACE/DEBUG/INFO/WARN/ERROR)
/debug track <player>     - Track player actions
/debug untrack <player>   - Stop tracking player
/debug logs <player>      - View player logs
/debug stats              - Show event statistics
/debug clear              - Clear debug file
/debug file               - Get debug file path
```

## Keybinds

### Default Controls
- **Q Key (Drop)**: Cast abilities
  - Standing: Primary ability
  - Sneaking: Secondary ability

### Setting Up Hotkeys
1. Run `/essence hotkey primary` or `/essence hotkey secondary`
2. In Minecraft settings, bind these commands to any key
3. Example keybind: `X` → `/essence hotkey primary`
