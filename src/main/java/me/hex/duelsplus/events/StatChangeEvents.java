package me.hex.duelsplus.events;

import me.hex.duelsplus.DuelsPlus;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;

import java.util.ArrayList;
import java.util.UUID;

import static me.hex.duelsplus.arenas.GameUtil.win;

public class StatChangeEvents implements Listener {

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        if (!DuelsPlus.getInGamePlayers().contains(event.getPlayer().getUniqueId())) return;

        event.getDrops().clear();

        Player killer = event.getPlayer().getKiller();

        if (killer == null) {
            if (DuelsPlus.getArenaReader().getArenaByPlayer(event.getPlayer()) != null) {
                for (Player player : DuelsPlus.getArenaReader().getArenaByPlayer(event.getPlayer()).getPlayers()) {
                    if (!player.getUniqueId().equals(event.getPlayer().getUniqueId())) {
                        killer = player;
                    }
                }
            } else {
                for (ArrayList<UUID> uuids : DuelsPlus.getBackupArenaPlayers().keySet()) {
                    if (uuids.contains(event.getPlayer().getUniqueId())) {
                        for (UUID uuid : uuids) {
                            if (!uuid.equals(event.getPlayer().getUniqueId()))
                                killer = Bukkit.getPlayer(uuid);
                        }
                    }
                }
            }
        }
        win(killer, event.getPlayer(), true);
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        DuelsPlus.getData().addPlayerIfNotExist(event.getPlayer());
    }

    @EventHandler
    public void onMove(PlayerMoveEvent event) {
        if (!DuelsPlus.getInStartingState().containsKey(event.getPlayer().getUniqueId())) return;

        event.setCancelled(true);
    }

}
