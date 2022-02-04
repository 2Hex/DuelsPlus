package me.hex.duelsplus.commands;

import me.hex.duelsplus.DuelsPlus;
import me.hex.duelsplus.Stats;
import org.apache.commons.lang.WordUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class StatsCommand implements CommandExecutor {

    @Override
    public boolean onCommand(@NotNull CommandSender sender,
                             @NotNull Command command, @NotNull String label,
                             @NotNull String[] args) {

        if (!command.getName().equalsIgnoreCase("stats")) return true;

        if (!sender.hasPermission("duels.stats")) {
            sender.sendMessage(ChatColor.AQUA + "You do not have the permission to perform this command.");
            return true;
        }

        if (args.length == 1) {
            Player target = Bukkit.getPlayer(args[0]);
            if (target == null) {
                sender.sendMessage(ChatColor.AQUA + "There is no player such as " + args[0]);
                return true;
            }

            for (Stats stat : Stats.values()) {
                sender.sendMessage(ChatColor.AQUA +
                        target.getDisplayName() + "'s " + WordUtils.capitalize(stat.name()) + ": " +
                        DuelsPlus.getData().getStat(target, stat).join());
            }
            return true;
        } else {
            if (sender instanceof Player) {
                for (Stats stat : Stats.values()) {
                    sender.sendMessage(ChatColor.AQUA +
                            ((Player) sender).getDisplayName() + " (You)'s " + WordUtils.capitalize(stat.name()) + ": " +
                            DuelsPlus.getData().getStat((Player) sender, stat).join());
                }

            } else {
                sender.sendMessage(ChatColor.RED + "This feature is only for players.");
            }
        }

        return true;
    }
}

