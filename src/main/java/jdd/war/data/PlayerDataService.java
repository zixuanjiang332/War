package jdd.war.data;

import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import jdd.war.War;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public final class PlayerDataService {
    private final War plugin;
    private final PlayerDataRepository repository;
    private final Map<UUID, PlayerData> cache = new ConcurrentHashMap<>();
    private final Set<UUID> dirtyPlayers = ConcurrentHashMap.newKeySet();
    private final Map<UUID, Integer> killStreaks = new ConcurrentHashMap<>();

    public PlayerDataService(War plugin, PlayerDataRepository repository) {
        this.plugin = plugin;
        this.repository = repository;
    }

    public void loadPlayerAsync(Player player) {
        UUID playerId = player.getUniqueId();
        String playerName = player.getName();
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            PlayerData loaded = repository.findByUuid(playerId).orElseGet(() -> repository.createIfAbsent(playerId, playerName));
            if (!playerName.equals(loaded.getName())) {
                repository.updateName(playerId, playerName);
            }
            loaded.setName(playerName);
            cache.put(playerId, loaded);

            Bukkit.getScheduler().runTask(plugin, () -> {
                if (player.isOnline()) {
                    plugin.getGameService().refreshScoreboard(player);
                }
            });
        });
    }

    public PlayerData getOrCreate(Player player) {
        return cache.computeIfAbsent(player.getUniqueId(), ignored -> new PlayerData(player.getUniqueId(), player.getName(), 0, 0));
    }

    public void addKill(Player player) {
        PlayerData data = getOrCreate(player);
        data.setName(player.getName());
        data.incrementKills();
        dirtyPlayers.add(player.getUniqueId());
        killStreaks.merge(player.getUniqueId(), 1, Integer::sum);
        plugin.getGameService().refreshScoreboard(player);
        savePlayerAsync(player);
    }

    public void addDeath(Player player) {
        PlayerData data = getOrCreate(player);
        data.setName(player.getName());
        data.incrementDeaths();
        dirtyPlayers.add(player.getUniqueId());
        killStreaks.remove(player.getUniqueId());
        plugin.getGameService().refreshScoreboard(player);
        savePlayerAsync(player);
    }

    public int getKillStreak(Player player) {
        return killStreaks.getOrDefault(player.getUniqueId(), 0);
    }

    public void savePlayerAsync(Player player) {
        PlayerData data = cache.get(player.getUniqueId());
        if (data == null) {
            return;
        }
        PlayerData snapshot = data.copy();
        dirtyPlayers.remove(player.getUniqueId());
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> repository.save(snapshot));
    }

    public void unloadPlayer(Player player) {
        UUID playerId = player.getUniqueId();
        PlayerData data = cache.remove(playerId);
        killStreaks.remove(playerId);
        dirtyPlayers.remove(playerId);
        if (data == null) {
            return;
        }

        PlayerData snapshot = data.copy();
        snapshot.setName(player.getName());
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> repository.save(snapshot));
    }

    public void flushAll() {
        for (PlayerData data : cache.values()) {
            repository.save(data.copy());
        }
        dirtyPlayers.clear();
    }

    public void shutdown() {
        flushAll();
        cache.clear();
        killStreaks.clear();
    }
}
