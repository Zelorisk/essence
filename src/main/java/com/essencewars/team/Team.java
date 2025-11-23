package com.essencewars.team;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class Team {

    private final String name;
    private final UUID owner;
    private final Set<UUID> members = new HashSet<>();

    public Team(String name, UUID owner) {
        this.name = name;
        this.owner = owner;
        this.members.add(owner);
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
    }

    public void removeMember(UUID uuid) {
        members.remove(uuid);
    }
}
