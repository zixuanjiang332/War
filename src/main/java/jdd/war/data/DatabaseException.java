package jdd.war.data;

/**
 * 数据库相关异常基类
 */
public abstract class DatabaseException extends Exception {
    public DatabaseException(String message) {
        super(message);
    }

    public DatabaseException(String message, Throwable cause) {
        super(message, cause);
    }
}

/**
 * 数据库初始化异常 - 通常由配置错误引起
 */
class DatabaseInitializationException extends DatabaseException {
    public DatabaseInitializationException(String message) {
        super("数据库初始化失败: " + message);
    }

    public DatabaseInitializationException(String message, Throwable cause) {
        super("数据库初始化失败: " + message, cause);
    }
}

/**
 * 数据库连接异常 - 通常由网络问题或服务不可用引起
 */
class DatabaseConnectionException extends DatabaseException {
    public DatabaseConnectionException(String message) {
        super("数据库连接失败: " + message);
    }

    public DatabaseConnectionException(String message, Throwable cause) {
        super("数据库连接失败: " + message, cause);
    }
}

/**
 * 数据库查询异常 - 由 SQL 错误或数据库约束冲突引起
 */
class DatabaseQueryException extends DatabaseException {
    public DatabaseQueryException(String message) {
        super("数据库查询失败: " + message);
    }

    public DatabaseQueryException(String message, Throwable cause) {
        super("数据库查询失败: " + message, cause);
    }
}
