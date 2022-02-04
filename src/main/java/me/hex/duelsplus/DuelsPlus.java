package me.hex.duelsplus;

import lombok.Getter;
import me.hex.duelsplus.arenas.Arena;
import me.hex.duelsplus.arenas.ArenaReader;
import me.hex.duelsplus.commands.AcceptCommand;
import me.hex.duelsplus.commands.DuelCommand;
import me.hex.duelsplus.commands.StatsCommand;
import me.hex.duelsplus.events.StatChangeEvents;
import me.hex.duelsplus.kits.Kit;
import me.hex.duelsplus.kits.KitReader;
import org.bukkit.Bukkit;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.ScoreboardManager;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.*;

public final class DuelsPlus extends JavaPlugin {

    @Getter private static final List<Quadruplet<UUID, UUID, Long, Kit>> invitedPlayers = new ArrayList<>();
    @Getter private static ScoreboardManager manager;@Getter private static ArrayList<UUID> inGamePlayers;
    @Getter private static MySQL SQL;
    @Getter private static DBData data;
    @Getter private static FileConfiguration kits;
    @Getter private static FileConfiguration arenas;
    @Getter private static KitReader kitsReader;
    @Getter private static ArenaReader arenaReader;
    @Getter private static DuelsPlus instance;
    @Getter private static HashMap<UUID, Integer> inStartingState;
    @Getter private static HashMap<UUID, ItemStack[]> duelsInventories;
    @Getter private static HashMap<ArrayList<UUID>, Arena> backupArenaPlayers;
    @Getter private static HashMap<Arena, Scoreboard> arenaScore;
    @Getter private static HashMap<UUID, Scoreboard> uuidScoreboard;
    @Getter private static HashMap<UUID, Integer> uuidStat;

    @Override
    public void onEnable() {
        initialize();

        try {
            SQL.connect();
        } catch (SQLException e) {
            getLogger().warning("Database Information Invalid, Please check your information.");
            e.printStackTrace();
        }

        if (SQL.isConnected()) {
            getLogger().info("Database Connected.");
            data.createTable();
        }

        registerCommands();
        registerListeners();
    }

    @Override
    public void onDisable() {
        SQL.disconnect();
    }

    private String getDBCred(String param) {
        if (getConfig().getConfigurationSection("MySQL") == null) {
            Bukkit.getLogger().warning("MySQL Section is null!");
            return "Invalid";
        }

        return Objects.requireNonNull(getConfig().getConfigurationSection("MySQL")).getString(param);
    }

    private int getPort() {
        return Objects.requireNonNull(getConfig().getConfigurationSection("MySQL")).getInt("port");
    }

    private void createKitsFile() {
        File kitsFile = new File(getDataFolder(), "kits.yml");

        if (!kitsFile.exists()) {
            //noinspection ResultOfMethodCallIgnored
            kitsFile.getParentFile().mkdirs();
            saveResource("kits.yml", false);
        }
        kits = new YamlConfiguration();

        try {
            kits.load(kitsFile);
        } catch (IOException | InvalidConfigurationException e) {
            e.printStackTrace();
        }
    }

    private void createArenasFile() {
        File arenasFile = new File(getDataFolder(), "arenas.yml");

        if (!arenasFile.exists()) {
            //noinspection ResultOfMethodCallIgnored
            arenasFile.getParentFile().mkdirs();
            saveResource("arenas.yml", false);
        }
        arenas = new YamlConfiguration();

        try {
            arenas.load(arenasFile);
        } catch (IOException | InvalidConfigurationException e) {
            e.printStackTrace();
        }
    }

    public void registerListeners() {
        getServer().getPluginManager().registerEvents(new StatChangeEvents(), this);
    }

    public void registerCommands() {
        Objects.requireNonNull(getCommand("duel")).setExecutor(new DuelCommand());
        Objects.requireNonNull(getCommand("accept")).setExecutor(new AcceptCommand());
        Objects.requireNonNull(getCommand("stats")).setExecutor(new StatsCommand());
    }

    private void initialize() {
        instance = this;
        manager = Bukkit.getScoreboardManager();
        saveDefaultConfig();
        createKitsFile();
        createArenasFile();

        SQL = new MySQL(getDBCred("host"), getPort(), getDBCred("username"),
                getDBCred("password"), getDBCred("database"));
        data = new DBData();
        kitsReader = new KitReader(kits);
        arenaReader = new ArenaReader(arenas);
        inStartingState = new HashMap<>();
        inGamePlayers = new ArrayList<>();
        duelsInventories = new HashMap<>();
        backupArenaPlayers = new HashMap<>();
        arenaScore = new HashMap<>();
        uuidScoreboard = new HashMap<>();
        uuidStat = new HashMap<>();
    }

}
