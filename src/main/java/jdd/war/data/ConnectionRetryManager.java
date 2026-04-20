package jdd.war.data;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Logger;

/**
 * 数据库连接重试管理器
 * 实现指数退避重试策略以提高连接可靠性
 */
public final class ConnectionRetryManager {
    private static final int MAX_RETRIES = 5;
    private static final long INITIAL_DELAY_MS = 1000L;  // 初始延迟1秒
    private static final long MAX_DELAY_MS = 32000L;    // 最大延迟32秒
    private static final double BACKOFF_MULTIPLIER = 2.0; // 指数退避倍数

    private final Logger logger;
    private final AtomicInteger retryCount = new AtomicInteger(0);
    private volatile long lastRetryTime = 0;

    public ConnectionRetryManager(Logger logger) {
        this.logger = logger;
    }

    /**
     * 获取当前重试次数
     */
    public int getRetryCount() {
        return retryCount.get();
    }

    /**
     * 检查是否可以进行重试
     */
    public boolean canRetry() {
        return retryCount.get() < MAX_RETRIES;
    }

    /**
     * 获取下次重试应该等待的延迟时间（毫秒）
     */
    public long getNextRetryDelayMs() {
        int attempt = retryCount.get();
        if (attempt >= MAX_RETRIES) {
            return 0;
        }
        
        // 计算延迟：初始延迟 * (退避倍数 ^ 当前尝试次数)
        long delay = (long) (INITIAL_DELAY_MS * Math.pow(BACKOFF_MULTIPLIER, attempt));
        
        // 确保不超过最大延迟
        return Math.min(delay, MAX_DELAY_MS);
    }

    /**
     * 记录一次重试尝试并返回是否应该继续重试
     */
    public boolean shouldRetry() {
        int currentRetry = retryCount.get();
        if (currentRetry >= MAX_RETRIES) {
            logger.warning("已达到最大重试次数(" + MAX_RETRIES + ")，放弃重试");
            return false;
        }

        long delay = getNextRetryDelayMs();
        logger.warning("数据库连接失败，" + (delay / 1000) + "秒后进行第 " + (currentRetry + 2) + " 次重试...");
        
        retryCount.incrementAndGet();
        lastRetryTime = System.currentTimeMillis();
        
        return true;
    }

    /**
     * 连接成功后，重置重试计数
     */
    public void reset() {
        int previousRetries = retryCount.getAndSet(0);
        if (previousRetries > 0) {
            logger.info("数据库连接成功，重试计数已重置。(之前尝试: " + (previousRetries + 1) + " 次)");
        }
    }

    /**
     * 获取上次重试的时间戳
     */
    public long getLastRetryTime() {
        return lastRetryTime;
    }

    /**
     * 检查是否应该尝试重连（用于后台检查）
     */
    public boolean isReadyForRetry() {
        if (!canRetry()) {
            return false;
        }

        int currentRetry = retryCount.get();
        if (currentRetry == 0) {
            // 第一次重试，立即尝试
            return true;
        }

        long delayMs = getNextRetryDelayMs();
        long timeSinceLastRetry = System.currentTimeMillis() - lastRetryTime;
        
        return timeSinceLastRetry >= delayMs;
    }
}
