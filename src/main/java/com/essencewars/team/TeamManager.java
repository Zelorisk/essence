package com.essencewars.team;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.Location;
import org.bukkit.Bukkit;

import com.essencewars.EssenceWarsPlugin;

public class TeamManager {

    private final EssenceWarsPlugin plugin;
    private final Map<String, Team> teamsByName = new HashMap<>();
    private final Map<UUID, Team> teamsByPlayer = new HashMap<>();
    private final Map<Team, TeamHome> teamHomes = new HashMap<>();
    private final Map<Team, Set<UUID>> pendingInvitations = new HashMap<>();
    private final File file;
    private FileConfiguration config;

    public TeamManager(EssenceWarsPlugin plugin) {
        this.plugin = plugin;
        this.file = new File(plugin.getDataFolder(), "teams.yml");
        reload();
        load();
    }

    public void reload() {
        if (!file.exists()) {
            try {
                file.getParentFile().mkdirs();
                file.createNewFile();
            } catch (IOException ignored) {
            }
        }
        this.config = YamlConfiguration.loadConfiguration(file);
    }

    public void save() {
        config.set("teams", null);
        for (Team team : teamsByName.values()) {
            String base = "teams." + team.getName() + ".";
            config.set(base + "owner", team.getOwner().toString());
            config.set(base + "members", team.getMembers().stream().map(UUID::toString).toList());

            // Save ranks
            for (UUID member : team.getMembers()) {
                Team.TeamRank rank = team.getRank(member);
                if (rank != null && rank != Team.TeamRank.MEMBER) {
                    config.set(base + "ranks." + member.toString(), rank.name());
                }
            }

            // Save pending invitations
            Set<UUID> invites = pendingInvitations.get(team);
            if (invites != null && !invites.isEmpty()) {
                config.set(base + "invitations", invites.stream().map(UUID::toString).toList());
            }

            // Save team homes
            TeamHome teamHome = teamHomes.get(team);
            if (teamHome != null && !teamHome.getHomes().isEmpty()) {
                for (Map.Entry<String, Location> entry : teamHome.getHomes().entrySet()) {
                    String homePath = base + "homes." + entry.getKey() + ".";
                    Location loc = entry.getValue();
                    config.set(homePath + "world", loc.getWorld().getName());
                    config.set(homePath + "x", loc.getX());
                    config.set(homePath + "y", loc.getY());
                    config.set(homePath + "z", loc.getZ());
                    config.set(homePath + "yaw", loc.getYaw());
                    config.set(homePath + "pitch", loc.getPitch());
                }
            }
        }
        try {
            config.save(file);
        } catch (IOException ignored) {
        }
    }

    private void load() {
        teamsByName.clear();
        teamsByPlayer.clear();
        teamHomes.clear();
        pendingInvitations.clear();
        ConfigurationSection section = config.getConfigurationSection("teams");
        if (section == null) {
            return;
        }
        for (String name : section.getKeys(false)) {
            String base = name + ".";
            String ownerStr = section.getString(base + "owner");
            if (ownerStr == null) {
                continue;
            }
            UUID owner = UUID.fromString(ownerStr);
            Team team = new Team(name, owner);
            java.util.List<String> members = section.getStringList(base + "members");
            for (String m : members) {
                try {
                    UUID id = UUID.fromString(m);
                    team.addMember(id);
                } catch (IllegalArgumentException ignored) {
                }
            }

            // Load ranks
            ConfigurationSection ranksSection = section.getConfigurationSection(base + "ranks");
            if (ranksSection != null) {
                for (String uuidStr : ranksSection.getKeys(false)) {
                    try {
                        UUID memberId = UUID.fromString(uuidStr);
                        String rankStr = section.getString(base + "ranks." + uuidStr);
                        if (rankStr != null) {
                            Team.TeamRank rank = Team.TeamRank.valueOf(rankStr);
                            team.setRank(memberId, rank);
                        }
                    } catch (IllegalArgumentException ignored) {
                    }
                }
            }

            // Load pending invitations
            java.util.List<String> invitesList = section.getStringList(base + "invitations");
            if (!invitesList.isEmpty()) {
                Set<UUID> invites = new HashSet<>();
                for (String inviteStr : invitesList) {
                    try {
                        invites.add(UUID.fromString(inviteStr));
                    } catch (IllegalArgumentException ignored) {
                    }
                }
                pendingInvitations.put(team, invites);
            }

            register(team);

            // Load team homes
            TeamHome teamHome = new TeamHome(team);
            ConfigurationSection homesSection = section.getConfigurationSection(base + "homes");
            if (homesSection != null) {
                for (String homeName : homesSection.getKeys(false)) {
                    String homePath = base + "homes." + homeName + ".";
                    String worldName = section.getString(homePath + "world");
                    if (worldName != null && Bukkit.getWorld(worldName) != null) {
                        double x = section.getDouble(homePath + "x");
                        double y = section.getDouble(homePath + "y");
                        double z = section.getDouble(homePath + "z");
                        float yaw = (float) section.getDouble(homePath + "yaw");
                        float pitch = (float) section.getDouble(homePath + "pitch");
                        Location loc = new Location(Bukkit.getWorld(worldName), x, y, z, yaw, pitch);
                        teamHome.setHome(homeName, loc);
                    }
                }
            }
            teamHomes.put(team, teamHome);
        }
    }

    private void register(Team team) {
        teamsByName.put(team.getName().toLowerCase(), team);
        for (UUID member : team.getMembers()) {
            teamsByPlayer.put(member, team);
        }
    }

    public Team getTeam(String name) {
        if (name == null) {
            return null;
        }
        return teamsByName.get(name.toLowerCase());
    }

    public Team getTeam(Player player) {
        if (player == null) {
            return null;
        }
        return teamsByPlayer.get(player.getUniqueId());
    }

    public Team createTeam(String name, Player owner) {
        if (getTeam(name) != null) {
            return null;
        }
        if (getTeam(owner) != null) {
            return null;
        }
        Team team = new Team(name, owner.getUniqueId());
        register(team);
        teamHomes.put(team, new TeamHome(team));
        return team;
    }

    public boolean disbandTeam(Player owner) {
        Team team = getTeam(owner);
        if (team == null || !team.getOwner().equals(owner.getUniqueId())) {
            return false;
        }
        teamsByName.remove(team.getName().toLowerCase());
        for (UUID member : team.getMembers()) {
            teamsByPlayer.remove(member);
        }
        teamHomes.remove(team);
        return true;
    }

    public boolean joinTeam(String name, Player player) {
        Team team = getTeam(name);
        if (team == null) {
            return false;
        }
        if (getTeam(player) != null) {
            return false;
        }
        team.addMember(player.getUniqueId());
        teamsByPlayer.put(player.getUniqueId(), team);
        return true;
    }

    public boolean leaveTeam(Player player) {
        Team team = getTeam(player);
        if (team == null) {
            return false;
        }
        if (team.getOwner().equals(player.getUniqueId())) {
            return false;
        }
        team.removeMember(player.getUniqueId());
        teamsByPlayer.remove(player.getUniqueId());
        return true;
    }

    public boolean areTeammates(Player a, Player b) {
        if (a == null || b == null) {
            return false;
        }
        Team ta = getTeam(a);
        Team tb = getTeam(b);
        return ta != null && ta == tb;
    }

    public Collection<Team> getTeams() {
        return teamsByName.values();
    }
    
    public TeamHome getTeamHome(Team team) {
        return teamHomes.computeIfAbsent(team, TeamHome::new);
    }

    public void invitePlayer(Team team, UUID playerId) {
        pendingInvitations.computeIfAbsent(team, k -> new HashSet<>()).add(playerId);
    }

    public void removeInvitation(Team team, UUID playerId) {
        Set<UUID> invites = pendingInvitations.get(team);
        if (invites != null) {
            invites.remove(playerId);
            if (invites.isEmpty()) {
                pendingInvitations.remove(team);
            }
        }
    }

    public boolean hasInvitation(UUID playerId, Team team) {
        Set<UUID> invites = pendingInvitations.get(team);
        return invites != null && invites.contains(playerId);
    }

    public Set<Team> getInvitations(UUID playerId) {
        Set<Team> result = new HashSet<>();
        for (Map.Entry<Team, Set<UUID>> entry : pendingInvitations.entrySet()) {
            if (entry.getValue().contains(playerId)) {
                result.add(entry.getKey());
            }
        }
        return result;
    }

    public boolean acceptInvitation(Player player, Team team) {
        if (!hasInvitation(player.getUniqueId(), team)) {
            return false;
        }
        if (getTeam(player) != null) {
            return false;
        }
        team.addMember(player.getUniqueId());
        teamsByPlayer.put(player.getUniqueId(), team);
        removeInvitation(team, player.getUniqueId());
        return true;
    }
}
