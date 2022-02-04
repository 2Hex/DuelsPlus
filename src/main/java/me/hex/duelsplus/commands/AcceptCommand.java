package me.hex.duelsplus.commands;

import me.hex.duelsplus.DuelsPlus;
import me.hex.duelsplus.Quadruplet;
import me.hex.duelsplus.Stats;
import me.hex.duelsplus.arenas.Arena;
import me.hex.duelsplus.kits.Kit;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.title.Title;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.attribute.Attribute;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Score;
import org.bukkit.scoreboard.Scoreboard;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Objects;
import java.util.Random;
import java.util.UUID;
import java.util.stream.Collectors;

import static me.hex.duelsplus.arenas.GameUtil.win;

public class AcceptCommand implements CommandExecutor {

    @Override
    public boolean onCommand(@NotNull CommandSender sender,
                             @NotNull Command command, @NotNull String label,
                             @NotNull String[] args) {

        if (!command.getName().equalsIgnoreCase("accept")) return true;
        if (!(sender instanceof Player player)) {
            return true;
        }

        if (!player.hasPermission("duels.accept")) {
            player.sendMessage(ChatColor.AQUA + "You do not have the permission to perform this command.");
            return true;
        }

        if (args.length == 1) {
            Player target = Bukkit.getPlayer(args[0]);
            if (target == null) {
                player.sendMessage(ChatColor.AQUA + "There is no player such as " + args[0]);
                return true;
            }

            Quadruplet<UUID, UUID, Long, Kit> quadruplet = null;

            for (Quadruplet<UUID, UUID, Long, Kit> quad : DuelsPlus.getInvitedPlayers()) {
                if ((quad.getSecond().equals(player.getUniqueId())) && (target.getUniqueId().equals(quad.getFirst()))) {
                    long inviteEnds = quad.getThird();
                    if (inviteEnds >= System.currentTimeMillis()) {
                        quadruplet = quad;
                    } else {
                        DuelsPlus.getInvitedPlayers().remove(quad);
                        player.sendMessage(ChatColor.RED + "Your invitation has expired. please send another " +
                                "duel request.");

                    }
                }
            }

            if (quadruplet != null) {
                startGame(quadruplet);
            } else {
                player.sendMessage(ChatColor.AQUA + "You do not have an invite from the player " + target.getDisplayName());
            }


            return true;
        }
        return true;
    }

    public void startGame(Quadruplet<UUID, UUID, Long, Kit> quadruplet) {

        if (DuelsPlus.getArenaReader().getFirstUnoccupiedArena() == null) {
            Objects.requireNonNull(Bukkit.getPlayer(quadruplet.getSecond())).
                    sendMessage(ChatColor.RED + "No Arena Left for Usage. Try again Later.");
            Objects.requireNonNull(Bukkit.getPlayer(quadruplet.getFirst())).
                    sendMessage(ChatColor.RED + "No Arena Left for Usage. Try again Later.");
            return;
        }

        Arena arena = DuelsPlus.getArenaReader().getFirstUnoccupiedArena();

        DuelsPlus.getInGamePlayers().add(quadruplet.getFirst());
        DuelsPlus.getInGamePlayers().add(quadruplet.getSecond());

        DuelsPlus.getInvitedPlayers().remove(quadruplet);

        arena.setOccupied(true);

        Player first = Bukkit.getPlayer(quadruplet.getFirst());
        Player second = Bukkit.getPlayer(quadruplet.getSecond());

        ArrayList<Player> gamePlayers = new ArrayList<>();
        gamePlayers.add(first);
        gamePlayers.add(second);

        prepareForBattle(gamePlayers, arena, quadruplet);

        assert first != null;
        assert second != null;

        final int[] index = {4};

        new BukkitRunnable() {

            @Override
            public void run() {
                if (index[0] == 0) {
                    cancel();
                    DuelsPlus.getInStartingState().remove(quadruplet.getFirst());
                    DuelsPlus.getInStartingState().remove(quadruplet.getSecond());
                    return;
                }

                first.showTitle(Title.title(Component.text(ChatColor.YELLOW + String.valueOf(index[0])),
                        Component.text(ChatColor.AQUA + "Get Ready!"), Title.DEFAULT_TIMES));
                second.showTitle(Title.title(Component.text(ChatColor.YELLOW + String.valueOf(index[0])),
                        Component.text(ChatColor.AQUA + "Get Ready!"), Title.DEFAULT_TIMES));

                DuelsPlus.getInStartingState().put(quadruplet.getFirst(), index[0]);
                DuelsPlus.getInStartingState().put(quadruplet.getSecond(), index[0]);
                index[0]--;
            }

        }.runTaskTimer(DuelsPlus.getInstance(), 20L, 20L);

        ArrayList<UUID> listOfPlayers = new ArrayList<>();
        gamePlayers.forEach((p) -> listOfPlayers.add(p.getUniqueId()));

        DuelsPlus.getBackupArenaPlayers().put(listOfPlayers, arena);

        for (Player player : gamePlayers) {
            DuelsPlus.getUuidScoreboard().put(player.getUniqueId(), player.getScoreboard());
        }

        DuelsPlus.getArenaScore().put(arena, DuelsPlus.getManager().getNewScoreboard());

        Bukkit.getScheduler().runTaskLater(DuelsPlus.getInstance(), () -> {

            Scoreboard board = DuelsPlus.getArenaScore().get(arena);
            Objective objective = board.registerNewObjective("timePassed",
                    "dummy", Component.text(ChatColor.AQUA + "Duels"));
            objective.setDisplaySlot(DisplaySlot.SIDEBAR);
            Score score = objective.getScore(ChatColor.GREEN + "Elapsed:");
            score.setScore(0);

            DuelsPlus.getUuidStat().put(first.getUniqueId(), DuelsPlus.getData().getStat(first, Stats.WINS).join());
            DuelsPlus.getUuidStat().put(second.getUniqueId(), DuelsPlus.getData().getStat(second, Stats.WINS).join());

            for (Player p : first.getWorld().getPlayers()) {
                p.setScoreboard(board);
            }

            new BukkitRunnable() {
                @Override
                public void run() {
                    /*
                    if (!DuelsPlus.getInGamePlayers().contains(first.getUniqueId())) {

                        if (!(DuelsPlus.getData().getStat(first, Stats.WINS).join() > DuelsPlus.getUuidStat().get(first.getUniqueId()))) {
                            win(second, first, false);
                        }
                        cancel();
                    } else if (!DuelsPlus.getInGamePlayers().contains(second.getUniqueId())) {
                        if (!(DuelsPlus.getData().getStat(second, Stats.WINS).join() > DuelsPlus.getUuidStat().get(second.getUniqueId()))) {
                            win(first, second, false);
                        }
                        cancel();
                    }
                    */
                    score.setScore(score.getScore() + 1);

                    if (score.getScore() > 900) {
                        int i = new Random().nextInt(2);
                        if (i == 0) {
                            second.setHealth(2);
                            second.damage(2);
                        } else {
                            first.setHealth(2);
                            first.damage(2);
                        }
                    }

                }
            }.runTaskTimer(DuelsPlus.getInstance(), 1L, 20L);
        }, 100L);

        new BukkitRunnable() {

            @Override
            public void run() {
                if (first.isDead()) {
                    if (DuelsPlus.getInGamePlayers().contains(first.getUniqueId())) {
                        win(second, first, false);
                    }
                } else if (second.isDead()) {
                    if (DuelsPlus.getInGamePlayers().contains(second.getUniqueId())) {
                        win(first, second, false);
                    }
                }
            }
        }.runTaskTimer(DuelsPlus.getInstance(), 1L, 1L);
    }

    private void prepareForBattle(ArrayList<Player> gamePlayers, Arena arena,
                                  Quadruplet<UUID, UUID, Long, Kit> quadruplet) {
        for (Player gamePlayer : gamePlayers) {

            arena.addPlayer(gamePlayer);
            DuelsPlus.getDuelsInventories().put(gamePlayer.getUniqueId(), gamePlayer.getInventory().getContents());
            gamePlayer.getInventory().clear();
            gamePlayer.setHealth(20);
            gamePlayer.setFoodLevel(20);

            switch (gamePlayers.indexOf(gamePlayer)) {
                case 0 -> gamePlayer.teleport(arena.getFirstSpawn());
                case 1 -> gamePlayer.teleport(arena.getSecondSpawn());
            }

            gamePlayer.getInventory().setHelmet(quadruplet.getFourth().getHelmet());
            gamePlayer.getInventory().setChestplate(quadruplet.getFourth().getChestPlate());
            gamePlayer.getInventory().setLeggings(quadruplet.getFourth().getLeggings());
            gamePlayer.getInventory().setBoots(quadruplet.getFourth().getBoots());

            int i = 0;

            for (ItemStack stack : quadruplet.getFourth().getInventoryContent()) {
                gamePlayer.getInventory().setItem(i, stack);
                i++;
            }

            try {
                gamePlayer.sendMessage(ChatColor.YELLOW + "Match Started! Opponent: " +
                        Objects.requireNonNull(Bukkit.getPlayer(
                                DuelsPlus.getInGamePlayers().stream()
                                        .filter(((b) -> !b.equals(gamePlayer.getUniqueId())))
                                        .collect(Collectors.toList()).get(0))).getDisplayName());
            } catch (NullPointerException e) {
                gamePlayer.sendMessage(ChatColor.YELLOW + "Match Started!");
            }

            gamePlayer.sendMessage(ChatColor.YELLOW + "You're playing on: " + arena.getWorld().getName());

            DuelsPlus.getInStartingState().put(gamePlayer.getUniqueId(), 5);
        }
    }

}
