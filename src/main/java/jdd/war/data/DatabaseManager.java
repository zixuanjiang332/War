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
    private final ConnectionRetryManager retryManager;
    private volatile boolean isInitialized = false;

    public DatabaseManager(War plugin) {
        this.plugin = plugin;
        this.retryManager = new ConnectionRetryManager(plugin.getLogger());
    }

    /**
     * 建立数据库连接，包含重试机制
     */
    public void connect() {
        try {
            validateConfiguration();
            attemptConnection();
            retryManager.reset();
            isInitialized = true;
        } catch (Exception exception) {
            handleConnectionFailure(exception);
        }
    }

    /**
     * 验证数据库配置是否完整
     */
    private void validateConfiguration() throws DatabaseException {
        String host = plugin.getConfig().getString("mysql.host", "");
        String username = plugin.getConfig().getString("mysql.username", "");
        String password = plugin.getConfig().getString("mysql.password", "");
        String database = plugin.getConfig().getString("mysql.database", "");

        if (host.isBlank()) {
            throw new DatabaseInitializationException("MySQL host 未配置");
        }
        if (username.isBlank()) {
            throw new DatabaseInitializationException("MySQL username 未配置");
        }
        if (database.isBlank()) {
            throw new DatabaseInitializationException("MySQL database 未配置");
        }
    }

    /**
     * 尝试建立数据库连接
     */
    private void attemptConnection() throws DatabaseException {
        HikariConfig config = new HikariConfig();
        config.setPoolName("War-Hikari");
        config.setDriverClassName("com.mysql.cj.jdbc.Driver");
        config.setJdbcUrl(buildJdbcUrl());
        config.setUsername(plugin.getConfig().getString("mysql.username", ""));
        config.setPassword(plugin.getConfig().getString("mysql.password", ""));

        // 根据在线玩家数动态调整连接池大小
        int playerCount = Math.max(plugin.getServer().getOnlinePlayers().size(), 1);
        config.setMaximumPoolSize(Math.max(10, playerCount / 2 + 5));
        config.setMinimumIdle(Math.max(2, playerCount / 4 + 1));
        config.setConnectionTimeout(5000L);  // 缩短超时时间
        config.setIdleTimeout(600000L);
        config.setMaxLifetime(1800000L);
        config.addDataSourceProperty("cachePrepStmts", "true");
        config.addDataSourceProperty("prepStmtCacheSize", "250");
        config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
        config.addDataSourceProperty("serverTimezone", "UTC");
        config.setAutoCommit(true);

        try {
            dataSource = new HikariDataSource(config);
            testConnection();
            createTables();
            plugin.getLogger().info("✓ 数据库连接建立成功");
        } catch (SQLException exception) {
            if (dataSource != null) {
                dataSource.close();
                dataSource = null;
            }
            
            if (isNetworkError(exception)) {
                throw new DatabaseConnectionException("网络连接失败", exception);
            } else {
                throw new DatabaseQueryException("SQL 错误", exception);
            }
        }
    }

    /**
     * 测试数据库连接
     */
    private void testConnection() throws SQLException {
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement("SELECT 1")) {
            stmt.executeQuery();
        }
    }

    /**
     * 判断是否是网络相关的错误
     */
    private boolean isNetworkError(SQLException exception) {
        String message = exception.getMessage().toLowerCase();
        return message.contains("connect") || message.contains("timeout") || 
               message.contains("refused") || message.contains("unknown host") ||
               message.contains("communications link failure");
    }

    /**
     * 处理连接失败
     */
    private void handleConnectionFailure(Exception exception) {
        if (exception instanceof DatabaseInitializationException) {
            plugin.getLogger().severe("❌ " + exception.getMessage());
            plugin.getLogger().severe("请检查 config.yml 中的 MySQL 配置");
            isInitialized = false;
        } else if (exception instanceof DatabaseConnectionException) {
            plugin.getLogger().warning("⚠ " + exception.getMessage());
            
            if (retryManager.shouldRetry()) {
                scheduleRetry();
            } else {
                plugin.getLogger().severe("❌ 已达到最大重试次数，将以本地缓存兜底继续运行");
                isInitialized = false;
            }
        } else {
            plugin.getLogger().severe("❌ 数据库连接初始化失败: " + exception.getMessage());
            if (exception.getCause() != null) {
                plugin.getLogger().severe("原因: " + exception.getCause().getMessage());
            }
            isInitialized = false;
        }
    }

    /**
     * 计划后续重试连接
     */
    private void scheduleRetry() {
        long delayMs = retryManager.getNextRetryDelayMs();
        long delayTicks = delayMs / 50;  // 将毫秒转换为 tick（每 tick 50ms）
        
        plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, this::connect, delayTicks);
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
                + "created_at DATETIME NOT NULL,"
                + "updated_at DATETIME NOT NULL"
                + ")";
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.executeUpdate();
        } catch (SQLException exception) {
            plugin.getLogger().warning("⚠ 创建 war_data 表失败: " + exception.getMessage());
        }
    }

    public boolean isAvailable() {
        return dataSource != null && !dataSource.isClosed();
    }

    public boolean isInitialized() {
        return isInitialized;
    }

    public Connection getConnection() throws SQLException {
        if (!isAvailable()) {
            throw new SQLException("Database is not available.");
        }
        return dataSource.getConnection();
    }

    public void disconnect() {
        if (isAvailable()) {
            try {
                dataSource.close();
                plugin.getLogger().info("✓ 数据库连接已关闭");
            } catch (Exception exception) {
                plugin.getLogger().warning("关闭数据库连接时出错: " + exception.getMessage());
            }
        }
    }
}
