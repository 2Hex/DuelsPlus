package me.hex.duelsplus.arenas;

import me.hex.duelsplus.DuelsPlus;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Map;
import java.util.Objects;

public class ArenaReader {

    private final FileConfiguration config;

    public ArenaReader(FileConfiguration fileConfiguration) {
        config = fileConfiguration;
    }

    private static <K, V> K getKey(Map<K, V> map, V value) {
        for (K key : map.keySet()) {
            if (value.equals(map.get(key))) {
                return key;
            }
        }
        return null;
    }

    public ArrayList<Arena> readArenas() {

        ArrayList<Arena> arenas = new ArrayList<>();

        for (String arenaName : Objects.requireNonNull(config
                .getConfigurationSection("Arenas")).getKeys(false)) {

            ConfigurationSection arena = Objects.requireNonNull(config.getConfigurationSection("Arenas"))
                    .getConfigurationSection(arenaName);

            if (arena == null) {
                continue;
            }

            World world = Bukkit.getWorld(arena.getString("world", "world"));

            String[] firstSpawnLocations = Objects.requireNonNull(arena
                    .getString("firstSpawn")).split(",");
            String[] secondSpawnLocations = Objects.requireNonNull(arena
                    .getString("secondSpawn")).split(",");

            String[] arenaFirstLocations = Objects.requireNonNull(arena
                    .getString("arenaFirstLoc")).split(",");
            String[] arenaSecondLocations = Objects.requireNonNull(arena
                    .getString("arenaSecondLoc")).split(",");

            if (firstSpawnLocations.length != 3) {
                Bukkit.getLogger().warning("firstSpawn of " + arenaName + " should be in this format!" +
                        "\n \"1, 2, 3\" where 1 is x, 2 is y, 3 is z");
                continue;
            }
            if (secondSpawnLocations.length != 3) {
                Bukkit.getLogger().warning("secondSpawn of " + arenaName + " should be in this format!" +
                        "\n \"1, 2, 3\" where 1 is x, 2 is y, 3 is z");
                continue;
            }

            Location firstSpawn = new Location(world, Double.parseDouble(firstSpawnLocations[0]),
                    Double.parseDouble(firstSpawnLocations[1]), Double.parseDouble(firstSpawnLocations[2]));

            Location secondSpawn = new Location(world, Double.parseDouble(secondSpawnLocations[0]),
                    Double.parseDouble(secondSpawnLocations[1]), Double.parseDouble(secondSpawnLocations[2]));

            Location arenaFirstLoc = new Location(world, Double.parseDouble(arenaFirstLocations[0]),
                    Double.parseDouble(arenaFirstLocations[1]), Double.parseDouble(arenaFirstLocations[2]));

            Location arenaSecondLoc = new Location(world, Double.parseDouble(arenaSecondLocations[0]),
                    Double.parseDouble(arenaSecondLocations[1]), Double.parseDouble(arenaSecondLocations[2]));

            boolean occupied = arena.getBoolean("occupied");

            arenas.add(new Arena(firstSpawn, secondSpawn, world, arenaFirstLoc, arenaSecondLoc, occupied));

        }

        return arenas;
    }

    public Arena getFirstUnoccupiedArena() {
        for (Arena arena : readArenas()) {
            if (!arena.isOccupied()) {
                return arena;
            }
        }
        return null;
    }

    public Arena getArenaByPlayer(Player player) {
        for (Arena arena : readArenas()) {
            if (arena.getPlayers().contains(player)) {
                return arena;
            }

            if (getKey(DuelsPlus.getBackupArenaPlayers(), arena) == null) return null;
            if (DuelsPlus.getBackupArenaPlayers().get(getKey(DuelsPlus.getBackupArenaPlayers(), arena)) == null)
                return null;
            if (!DuelsPlus.getBackupArenaPlayers().get(getKey(
                    DuelsPlus.getBackupArenaPlayers(), arena)).getPlayers().contains(player)) return null;

            return arena;
        }
        return null;
    }
}
