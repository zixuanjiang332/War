package jdd.war.data;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;
import java.util.UUID;
import java.util.logging.Logger;

public final class PlayerDataRepository {
    private final DatabaseManager databaseManager;
    private final Logger logger;

    public PlayerDataRepository(DatabaseManager databaseManager, Logger logger) {
        this.databaseManager = databaseManager;
        this.logger = logger;
    }

    public Optional<PlayerData> findByUuid(UUID uuid) {
        String sql = "SELECT uuid, name, kills, deaths FROM war_data WHERE uuid = ?";
        if (!databaseManager.isAvailable()) {
            return Optional.empty();
        }

        try (Connection connection = databaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, uuid.toString());
            try (ResultSet resultSet = statement.executeQuery()) {
                if (!resultSet.next()) {
                    return Optional.empty();
                }

                return Optional.of(new PlayerData(
                        UUID.fromString(resultSet.getString("uuid")),
                        resultSet.getString("name"),
                        resultSet.getInt("kills"),
                        resultSet.getInt("deaths")
                ));
            }
        } catch (SQLException exception) {
            logger.warning("读取玩家数据失败: " + exception.getMessage());
            return Optional.empty();
        }
    }

    public PlayerData createIfAbsent(UUID uuid, String name) {
        PlayerData data = new PlayerData(uuid, name, 0, 0);
        upsert(data);
        return data;
    }

    public boolean save(PlayerData data) {
        return upsert(data);
    }

    public boolean upsert(PlayerData data) {
        String sql = "INSERT INTO war_data (uuid, name, kills, deaths, created_at, updated_at) "
                + "VALUES (?, ?, ?, ?, NOW(), NOW()) "
                + "ON DUPLICATE KEY UPDATE name = VALUES(name), kills = VALUES(kills), "
                + "deaths = VALUES(deaths), updated_at = NOW()";
        if (!databaseManager.isAvailable()) {
            return false;
        }

        try (Connection connection = databaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, data.getUuid().toString());
            statement.setString(2, data.getName());
            statement.setInt(3, data.getKills());
            statement.setInt(4, data.getDeaths());
            statement.executeUpdate();
            return true;
        } catch (SQLException exception) {
            logger.warning("保存玩家数据失败: " + exception.getMessage());
            return false;
        }
    }

    public boolean updateName(UUID uuid, String name) {
        String sql = "UPDATE war_data SET name = ?, updated_at = NOW() WHERE uuid = ?";
        if (!databaseManager.isAvailable()) {
            return false;
        }

        try (Connection connection = databaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, name);
            statement.setString(2, uuid.toString());
            statement.executeUpdate();
            return true;
        } catch (SQLException exception) {
            logger.warning("更新玩家名称失败: " + exception.getMessage());
            return false;
        }
    }
}
