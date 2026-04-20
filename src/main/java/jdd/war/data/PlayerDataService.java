package jdd.war.data;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Consumer;
import jdd.war.War;
import jdd.war.game.PlayerTier;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

public final class PlayerDataService {
    private final War plugin;
    private final PlayerDataRepository repository;
    private final Map<UUID, PlayerData> cache = new ConcurrentHashMap<>();
    private final Set<UUID> dirtyPlayers = ConcurrentHashMap.newKeySet();
    private final Map<UUID, Integer> killStreaks = new ConcurrentHashMap<>();
    
    // 使用 ReadWriteLock 替代 AtomicBoolean
    private final ReadWriteLock leaderboardLock = new ReentrantReadWriteLock();
    private volatile boolean leaderboardDirty = false;
    private volatile boolean leaderboardRebuildRunning = false;
    
    // 排行榜更新延迟配置（毫秒）
    private static final long LEADERBOARD_UPDATE_DELAY_MS = 2000L;  // 2秒延迟聚合更新
    private volatile long lastLeaderboardUpdateTime = 0;
    
    private volatile LeaderboardSnapshot leaderboardSnapshot = LeaderboardSnapshot.empty();
    private BukkitTask leaderboardTask;
    private BukkitTask flushTask;
    private BukkitTask flushRunning = null;

    public PlayerDataService(War plugin, PlayerDataRepository repository) {
        this.plugin = plugin;
        this.repository = repository;
    }

    public void startBackgroundTasks() {
        if (leaderboardTask == null) {
            // 优化：从每 tick 检查一次（20L, 20L）改为每 100 tick 检查一次（2秒）
            leaderboardTask = Bukkit.getScheduler().runTaskTimerAsynchronously(
                plugin, 
                this::rebuildLeaderboardsIfNeeded, 
                100L,  // 初始延迟 5 秒
                100L   // 检查间隔 5 秒（原来是 1 秒）
            );
        }
        if (flushTask == null) {
            flushTask = Bukkit.getScheduler().runTaskTimerAsynchronously(
                plugin, 
                this::flushDirtyPlayersIfNeeded, 
                40L, 
                40L
            );
        }
    }

    public void preloadCacheAsync(Runnable callback) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            for (PlayerData data : repository.findAll()) {
                cache.put(data.getUuid(), data);
            }
            rebuildLeaderboardsNow();

            if (callback != null) {
                Bukkit.getScheduler().runTask(plugin, callback);
            }
        });
    }

    public void loadPlayerAsync(Player player) {
        UUID playerId = player.getUniqueId();
        String playerName = player.getName();
        PlayerData cached = cache.get(playerId);
        if (cached != null) {
            if (!playerName.equals(cached.getName())) {
                cached.setName(playerName);
                dirtyPlayers.add(playerId);
                markLeaderboardDirty();
            }
            Bukkit.getScheduler().runTask(plugin, () -> {
                if (player.isOnline()) {
                    plugin.getGameService().handlePlayerDataLoaded(player);
                }
            });
            return;
        }

        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            PlayerData loaded = repository.findByUuid(playerId).orElseGet(() -> repository.createIfAbsent(playerId, playerName));
            loaded.setName(playerName);
            cache.put(playerId, loaded);
            markLeaderboardDirty();

            Bukkit.getScheduler().runTask(plugin, () -> {
                if (player.isOnline()) {
                    plugin.getGameService().handlePlayerDataLoaded(player);
                }
            });
        });
    }

    public PlayerData getOrCreate(Player player) {
        return cache.computeIfAbsent(player.getUniqueId(), ignored -> {
            PlayerData data = new PlayerData(player.getUniqueId(), player.getName(), 0, 0);
            markLeaderboardDirty();
            dirtyPlayers.add(player.getUniqueId());
            return data;
        });
    }

    public void addKill(Player player) {
        PlayerData data = getOrCreate(player);
        PlayerTier beforeTier = PlayerTier.fromKills(data.getKills());
        data.setName(player.getName());
        data.incrementKills();
        dirtyPlayers.add(player.getUniqueId());
        killStreaks.merge(player.getUniqueId(), 1, Integer::sum);
        markLeaderboardDirty();
        PlayerTier afterTier = PlayerTier.fromKills(data.getKills());
        plugin.getGameService().handleKillStatChanged(player, beforeTier != afterTier);
    }

    public void addDeath(Player player) {
        PlayerData data = getOrCreate(player);
        data.setName(player.getName());
        data.incrementDeaths();
        dirtyPlayers.add(player.getUniqueId());
        killStreaks.remove(player.getUniqueId());
        markLeaderboardDirty();
        plugin.getGameService().handleDeathStatChanged(player);
    }

    public int getKillStreak(Player player) {
        return killStreaks.getOrDefault(player.getUniqueId(), 0);
    }

    public void savePlayerAsync(Player player) {
        PlayerData data = cache.get(player.getUniqueId());
        if (data == null) {
            return;
        }
        dirtyPlayers.add(player.getUniqueId());
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> repository.save(data.copy()));
    }

    public void unloadPlayer(Player player) {
        UUID playerId = player.getUniqueId();
        killStreaks.remove(playerId);

        PlayerData data = cache.get(playerId);
        if (data == null) {
            dirtyPlayers.remove(playerId);
            return;
        }

        data.setName(player.getName());
        dirtyPlayers.remove(playerId);
        repository.save(data.copy());
    }

    public void flushAll() {
        repository.saveAll(cache.values().stream().map(PlayerData::copy).toList());
        dirtyPlayers.clear();
    }

    public void shutdown() {
        if (leaderboardTask != null) {
            leaderboardTask.cancel();
        }
        if (flushTask != null) {
            flushTask.cancel();
        }
        if (flushRunning != null) {
            flushRunning.cancel();
        }
        flushAll();
        cache.clear();
        killStreaks.clear();
        
        leaderboardLock.writeLock().lock();
        try {
            leaderboardSnapshot = LeaderboardSnapshot.empty();
        } finally {
            leaderboardLock.writeLock().unlock();
        }
    }

    /**
     * 获取排行榜快照（使用读锁确保线程安全）
     */
    public LeaderboardSnapshot getLeaderboardSnapshot() {
        leaderboardLock.readLock().lock();
        try {
            return leaderboardSnapshot;
        } finally {
            leaderboardLock.readLock().unlock();
        }
    }

    public void loadLeaderboardsAsync(Consumer<LeaderboardSnapshot> callback) {
        LeaderboardSnapshot snapshot = getLeaderboardSnapshot();
        Bukkit.getScheduler().runTask(plugin, () -> callback.accept(snapshot));
    }

    /**
     * 标记排行榜需要更新，使用延迟聚合策略
     */
    private void markLeaderboardDirty() {
        long now = System.currentTimeMillis();
        long timeSinceLastUpdate = now - lastLeaderboardUpdateTime;
        
        // 只在延迟时间内没有重建过时才标记为脏数据
        if (timeSinceLastUpdate >= LEADERBOARD_UPDATE_DELAY_MS) {
            leaderboardDirty = true;
            lastLeaderboardUpdateTime = now;
        }
    }

    /**
     * 检查并重建排行榜（使用写锁）
     */
    private void rebuildLeaderboardsIfNeeded() {
        if (!leaderboardDirty) {
            return;
        }
        
        if (!leaderboardLock.writeLock().tryLock()) {
            // 如果无法获得写锁，下次重试
            return;
        }
        
        try {
            if (leaderboardRebuildRunning) {
                return;
            }
            
            // 再次检查延迟时间
            long now = System.currentTimeMillis();
            if (now - lastLeaderboardUpdateTime < LEADERBOARD_UPDATE_DELAY_MS / 2) {
                return;
            }
            
            leaderboardRebuildRunning = true;
            rebuildLeaderboardsNow();
            leaderboardDirty = false;
        } finally {
            leaderboardRebuildRunning = false;
            leaderboardLock.writeLock().unlock();
        }
    }

    private void rebuildLeaderboardsNow() {
        List<PlayerData> snapshot = new ArrayList<>(cache.values());
        List<LeaderboardEntry> topKills = snapshot.stream()
                .sorted(Comparator.comparingInt(PlayerData::getKills).reversed()
                        .thenComparingInt(PlayerData::getDeaths)
                        .thenComparing(PlayerData::getName, String.CASE_INSENSITIVE_ORDER))
                .limit(10)
                .map(this::toEntry)
                .toList();

        List<LeaderboardEntry> topDeaths = snapshot.stream()
                .sorted(Comparator.comparingInt(PlayerData::getDeaths).reversed()
                        .thenComparing(Comparator.comparingInt(PlayerData::getKills).reversed())
                        .thenComparing(PlayerData::getName, String.CASE_INSENSITIVE_ORDER))
                .limit(10)
                .map(this::toEntry)
                .toList();

        List<LeaderboardEntry> topKd = snapshot.stream()
                .filter(data -> data.getKills() > 0)
                .sorted(Comparator.comparingDouble(PlayerData::getKd).reversed()
                        .thenComparing(Comparator.comparingInt(PlayerData::getKills).reversed())
                        .thenComparingInt(PlayerData::getDeaths)
                        .thenComparing(PlayerData::getName, String.CASE_INSENSITIVE_ORDER))
                .limit(10)
                .map(this::toEntry)
                .toList();

        leaderboardSnapshot = new LeaderboardSnapshot(topKills, topDeaths, topKd);
    }

    private LeaderboardEntry toEntry(PlayerData data) {
        return new LeaderboardEntry(data.getName(), data.getKills(), data.getDeaths(), data.getKd());
    }

    private void flushDirtyPlayersIfNeeded() {
        if (dirtyPlayers.isEmpty()) {
            return;
        }
        
        List<PlayerData> dirtyData = dirtyPlayers.stream()
                .map(cache::get)
                .filter(data -> data != null)
                .map(PlayerData::copy)
                .toList();
        
        if (!dirtyData.isEmpty()) {
            Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
                repository.saveAll(dirtyData);
                dirtyPlayers.removeAll(
                    dirtyData.stream().map(PlayerData::getUuid).toList()
                );
            });
        }
    }
}
