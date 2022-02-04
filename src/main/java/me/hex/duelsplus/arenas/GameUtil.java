package me.hex.duelsplus.arenas;

import me.hex.duelsplus.DuelsPlus;
import me.hex.duelsplus.Stats;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.UUID;

import static me.hex.duelsplus.DuelsPlus.*;

public class GameUtil {

    public static void win(Player killer, Player dead, boolean kill) {
        DuelsPlus.getData().addStat(dead, Stats.DEATHS, 1);
        DuelsPlus.getData().addStat(dead, Stats.LOSS, 1);
        if (kill)
            DuelsPlus.getData().addStat(killer, Stats.KILLS, 1);

        DuelsPlus.getData().addStat(killer, Stats.WINS, 1);

        getInGamePlayers().remove(dead.getUniqueId());
        getInGamePlayers().remove(killer.getUniqueId());

        Arena arena = DuelsPlus.getArenaReader().getArenaByPlayer(dead);

        if (arena != null) {
            arena.setOccupied(false);
            arena.clearPlayers();
        }

        ArrayList<Player> list = new ArrayList<>();
        list.add(killer);
        list.add(dead);

        for (Player p : list) {

            if (getDuelsInventories().get(p.getUniqueId()).length == 0) {
                Bukkit.getLogger().warning("length == 0 (debug)");
                p.getInventory().clear();
            } else if (getDuelsInventories().get(p.getUniqueId()) == null) {
                p.getInventory().clear();
                Bukkit.getLogger().warning("it is null (debug)");
            } else {
                p.getInventory().setContents(getDuelsInventories()
                        .get(p.getUniqueId()));
            }

        }
        dead.sendMessage(ChatColor.RED + "Duel completed," +
                " Winner is: " + ChatColor.RESET + killer.getDisplayName());
        killer.sendMessage(ChatColor.GREEN + "Duel completed," +
                " Winner is: " + ChatColor.RESET + killer.getDisplayName());

        getDuelsInventories().remove(killer.getUniqueId());
        getDuelsInventories().remove(dead.getUniqueId());

        for (ArrayList<UUID> uuids : getBackupArenaPlayers().keySet()) {
            if (uuids.contains(dead.getUniqueId())) {
                getBackupArenaPlayers().remove(uuids);
            }
        }
        if (getUuidScoreboard().containsKey(killer.getUniqueId())) {
            killer.setScoreboard(getUuidScoreboard().get(killer.getUniqueId()));
        }
        if (getUuidScoreboard().containsKey(dead.getUniqueId())) {
            killer.setScoreboard(getUuidScoreboard().get(dead.getUniqueId()));
        }
        getUuidScoreboard().remove(dead.getUniqueId());
        getUuidScoreboard().remove(killer.getUniqueId());

    }

}
