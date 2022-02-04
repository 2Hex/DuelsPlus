package me.hex.duelsplus.arenas;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.util.ArrayList;


public class Arena {

    @Getter private final ArrayList<Player> players;
    @Getter private final Location firstSpawn;
    @Getter private final Location secondSpawn;
    @Getter private final Location arenaFirstLoc;
    @Getter private final Location arenaSecondLoc;
    @Getter private final World world;
    @Setter private boolean occupied;

    public Arena(
            Location firstSpawn, Location secondSpawn,
            World world,
            Location arenaFirstLoc, Location arenaSecondLoc, boolean occupied) {

        this.firstSpawn = firstSpawn;
        this.secondSpawn = secondSpawn;
        this.arenaFirstLoc = arenaFirstLoc;
        this.arenaSecondLoc = arenaSecondLoc;

        this.world = world;
        this.occupied = occupied;
        this.players = new ArrayList<>();
    }

    public boolean isOccupied() {
        return occupied;
    }

    public void addPlayer(Player player) {
        getPlayers().add(player);
    }

    public void clearPlayers() {
        getPlayers().clear();
    }

}
