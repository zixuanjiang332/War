package jdd.war.data;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import jdd.war.War;

public final class DatabaseManager {
    private final War plugin;
    private HikariDataSource dataSource;

    public DatabaseManager(War plugin) {
        this.plugin = plugin;
    }

    public void connect() {
        HikariConfig config = new HikariConfig();
        config.setPoolName("War-Hikari");
        config.setDriverClassName("com.mysql.cj.jdbc.Driver");
        config.setJdbcUrl(buildJdbcUrl());
        config.setUsername(plugin.getConfig().getString("mysql.username", ""));
        config.setPassword(plugin.getConfig().getString("mysql.password", ""));
        config.setMaximumPoolSize(10);
        config.setMinimumIdle(2);
        config.setConnectionTimeout(10000L);
        config.setIdleTimeout(600000L);
        config.setMaxLifetime(1800000L);
        config.addDataSourceProperty("cachePrepStmts", "true");
        config.addDataSourceProperty("prepStmtCacheSize", "250");
        config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");

        try {
            dataSource = new HikariDataSource(config);
            createTables();
        } catch (Exception exception) {
            plugin.getLogger().severe("数据库连接初始化失败，插件将以本地缓存兜底继续运行。");
            plugin.getLogger().severe(exception.getMessage());
            dataSource = null;
        }
    }

    private String buildJdbcUrl() {
        String host = plugin.getConfig().getString("mysql.host", "localhost");
        int port = plugin.getConfig().getInt("mysql.port", 3306);
        String database = plugin.getConfig().getString("mysql.database", "herobrawl");
        return "jdbc:mysql://" + host + ":" + port + "/" + database
                + "?useSSL=false&allowPublicKeyRetrieval=true&characterEncoding=utf8"
                + "&serverTimezone=UTC&autoReconnect=true";
    }

    private void createTables() {
        String sql = "CREATE TABLE IF NOT EXISTS war_data ("
                + "uuid VARCHAR(36) PRIMARY KEY,"
                + "name VARCHAR(16) NOT NULL,"
                + "kills INT NOT NULL DEFAULT 0,"
                + "deaths INT NOT NULL DEFAULT 0,"
                + "created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,"
                + "updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP"
                + ")";
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.executeUpdate();
        } catch (SQLException exception) {
            plugin.getLogger().severe("创建 war_data 表失败: " + exception.getMessage());
        }
    }

    public boolean isAvailable() {
        return dataSource != null && !dataSource.isClosed();
    }

    public Connection getConnection() throws SQLException {
        if (!isAvailable()) {
            throw new SQLException("Database is not available.");
        }
        return dataSource.getConnection();
    }

    public void disconnect() {
        if (isAvailable()) {
            dataSource.close();
        }
    }
}
