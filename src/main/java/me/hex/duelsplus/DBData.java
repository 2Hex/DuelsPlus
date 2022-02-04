package me.hex.duelsplus;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class DBData {

    public void createTable() {
        Bukkit.getScheduler().runTaskAsynchronously(DuelsPlus.getInstance(), () -> {
            try (PreparedStatement preparedStatement = DuelsPlus.getSQL().getConnection()
                    .prepareStatement("CREATE TABLE IF NOT EXISTS stats " + "(NAME VARCHAR(100),UUID VARCHAR(100)"
                            + ",WINS INT,LOSS INT,KILLS INT,DEATHS INT,PRIMARY KEY (UUID))")) {

                preparedStatement.executeUpdate();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        });
    }

    public void addPlayerIfNotExist(Player player) {
        Bukkit.getScheduler().runTaskAsynchronously(DuelsPlus.getInstance(), () -> {
            UUID uuid = player.getUniqueId();

            if (!existsInDB(player).join()) {
                try (PreparedStatement preparedStatement = DuelsPlus.getSQL().getConnection()
                        .prepareStatement("INSERT IGNORE INTO stats (NAME, UUID) VALUES (?, ?)")) {

                    preparedStatement.setString(1, player.getName());
                    preparedStatement.setString(2, uuid.toString());
                    preparedStatement.executeUpdate();

                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public CompletableFuture<Boolean> existsInDB(Player player) {

        return CompletableFuture.supplyAsync(() -> {

            try (PreparedStatement ps = DuelsPlus.getSQL().getConnection()
                    .prepareStatement("SELECT * FROM stats WHERE UUID=?")) {

                UUID uuid = player.getUniqueId();
                ps.setString(1, uuid.toString());
                try (ResultSet resultSet = ps.executeQuery()) {
                    if (resultSet.next()) {
                        return true;
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
            return false;
        });
    }

    public void addStat(Player player, Stats statType, int value) {
        Bukkit.getScheduler().runTaskAsynchronously(DuelsPlus.getInstance(), () -> {
            try (PreparedStatement preparedStatement = DuelsPlus.getSQL().getConnection().prepareStatement(
                    "UPDATE stats SET " + statType.name() + "=? WHERE UUID=?")) {

                preparedStatement.setInt(1, (getStat(player, statType).join() + value));
                preparedStatement.setString(2, player.getUniqueId().toString());
                preparedStatement.executeUpdate();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        });
    }

    public void doSQL(String doMessage) {
        Bukkit.getScheduler().runTaskAsynchronously(DuelsPlus.getInstance(), () -> {
            try (PreparedStatement preparedStatement = DuelsPlus.getSQL().getConnection().prepareStatement(doMessage)) {
                preparedStatement.executeUpdate();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        });
    }

    public CompletableFuture<Integer> getStat(Player player, Stats statType) {

        return CompletableFuture.supplyAsync(() -> {

            try (PreparedStatement preparedStatement = DuelsPlus.getSQL().getConnection().prepareStatement(
                    "SELECT " + statType.name() + " FROM stats WHERE UUID=?")) {

                preparedStatement.setString(1, player.getUniqueId().toString());
                try (ResultSet resultSet = preparedStatement.executeQuery()) {
                    int stat;
                    if (resultSet.next()) {
                        stat = resultSet.getInt(statType.name());
                        return stat;
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }

            return 0;
        });
    }
}
