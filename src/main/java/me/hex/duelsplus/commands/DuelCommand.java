package me.hex.duelsplus.commands;

import me.hex.duelsplus.DuelsPlus;
import me.hex.duelsplus.Quadruplet;
import me.hex.duelsplus.kits.Kit;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.UUID;

public class DuelCommand implements CommandExecutor {
    //              Sender, Receiver, Time, Kit -> #Quadruplet Object

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command,
                             @NotNull String label, @NotNull String[] args) {

        if (!command.getName().equalsIgnoreCase("duel")) return true;
        if (!(sender instanceof Player player)) {
            return true;
        }

        if (!player.hasPermission("duels.duel")) {
            player.sendMessage(ChatColor.AQUA + "You do not have the permission to perform this command.");
            return true;
        }
        if (args.length == 1) {
            Player target = Bukkit.getPlayer(args[0]);
            if (target == null) {
                player.sendMessage(ChatColor.AQUA + "There is no player such as " + args[0]);
                return true;
            }

            if (checkRequest(player)) {
                return true;
            }

            if (DuelsPlus.getKitsReader().getDefaultKit() != null) {
                invitePlayer(player, target, 60, DuelsPlus.getKitsReader().getDefaultKit());
            } else {
                Bukkit.getLogger().warning("There was no default_kit set, thus I used the first one.");
                invitePlayer(player, target, 60, DuelsPlus.getKitsReader().readKits().get(0));
            }

        } else if (args.length == 2) {
            Player target = Bukkit.getPlayer(args[0]);
            if (target == null) {
                player.sendMessage(ChatColor.AQUA + "There is no player such as " + args[0]);
                return true;
            }

            if (checkRequest(player)) {
                return true;
            }

            if (DuelsPlus.getKitsReader().doesKitExist(args[1])) {
                invitePlayer(player, target, 60, DuelsPlus.getKitsReader().valueOf(args[1]));
            } else {
                player.sendMessage(ChatColor.AQUA + "Kit with the name of " + args[1] + " does not exist.");
                return true;
            }
        } else {
            player.sendMessage(ChatColor.ITALIC + ChatColor.RED.toString() + "Usage (<> REQUIRED, [] Optional:" +
                    " /duel <Player> [Kit]");
        }

        return true;
    }

    public void invitePlayer(Player sender, Player receiver, int seconds, Kit kit) {
        if (seconds > 0) {
            Quadruplet<UUID, UUID, Long, Kit> quad = new Quadruplet<>(sender.getUniqueId()
                    , receiver.getUniqueId(), ((seconds * 1000L) + System.currentTimeMillis()), kit);

            DuelsPlus.getInvitedPlayers().add(quad);

            receiver.sendMessage(ChatColor.GOLD + sender.getDisplayName()
                    + " Has sent you a " + kit.getName() + " duel request, Type " +
                    "/accept to accept! Invite will expire in " + seconds);
            sender.sendMessage(ChatColor.GOLD + "Sent " + kit.getName()
                    + " Duel Request to " + receiver.getDisplayName());

        }
    }

    public boolean checkRequest(Player player) {
        for (Quadruplet<UUID, UUID, Long, Kit> q : DuelsPlus.getInvitedPlayers()) {
            if (player.getUniqueId().equals(q.getFirst())) {
                if (!(q.getThird() >= System.currentTimeMillis())) { //if it expired
                    DuelsPlus.getInvitedPlayers().remove(q);
                } else {
                    player.sendMessage(ChatColor.AQUA + "You already have a pending request to " +
                            Objects.requireNonNull(Bukkit.getPlayer(q.getSecond())).getDisplayName());
                    return true;
                }
            }
        }
        return false;
    }
}
