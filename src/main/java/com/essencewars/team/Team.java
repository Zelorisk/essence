package com.essencewars.team;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class Team {

    public enum TeamRank {
        OWNER,
        ADMIN,
        MEMBER;

        public String getDisplayName() {
            return switch (this) {
                case OWNER -> "Owner";
                case ADMIN -> "Admin";
                case MEMBER -> "Member";
            };
        }
    }

    private final String name;
    private final UUID owner;
    private final Set<UUID> members = new HashSet<>();
    private final Map<UUID, TeamRank> ranks = new HashMap<>();

    public Team(String name, UUID owner) {
        this.name = name;
        this.owner = owner;
        this.members.add(owner);
        this.ranks.put(owner, TeamRank.OWNER);
    }

    public String getName() {
        return name;
    }

    public UUID getOwner() {
        return owner;
    }

    public Set<UUID> getMembers() {
        return members;
    }

    public boolean isMember(UUID uuid) {
        return members.contains(uuid);
    }

    public void addMember(UUID uuid) {
        members.add(uuid);
        if (!ranks.containsKey(uuid)) {
            ranks.put(uuid, TeamRank.MEMBER);
        }
    }

    public void removeMember(UUID uuid) {
        members.remove(uuid);
        ranks.remove(uuid);
    }

    public TeamRank getRank(UUID uuid) {
        return ranks.getOrDefault(uuid, TeamRank.MEMBER);
    }

    public void setRank(UUID uuid, TeamRank rank) {
        if (isMember(uuid) && !uuid.equals(owner)) {
            ranks.put(uuid, rank);
        }
    }

    public boolean isOwner(UUID uuid) {
        return owner.equals(uuid);
    }

    public boolean isAdmin(UUID uuid) {
        return getRank(uuid) == TeamRank.ADMIN;
    }

    public boolean isOwnerOrAdmin(UUID uuid) {
        TeamRank rank = getRank(uuid);
        return rank == TeamRank.OWNER || rank == TeamRank.ADMIN;
    }
}
