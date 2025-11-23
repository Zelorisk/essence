package com.essencewars.team;

import org.bukkit.Location;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class TeamHome {
    
    private final Team team;
    private final Map<String, Location> homes = new HashMap<>();
    
    public TeamHome(Team team) {
        this.team = team;
    }
    
    public void setHome(String name, Location location) {
        homes.put(name.toLowerCase(), location);
    }
    
    public Location getHome(String name) {
        return homes.get(name.toLowerCase());
    }
    
    public boolean removeHome(String name) {
        return homes.remove(name.toLowerCase()) != null;
    }
    
    public boolean hasHome(String name) {
        return homes.containsKey(name.toLowerCase());
    }
    
    public Set<String> getHomeNames() {
        return homes.keySet();
    }
    
    public Map<String, Location> getHomes() {
        return homes;
    }
    
    public int getHomeCount() {
        return homes.size();
    }
}
