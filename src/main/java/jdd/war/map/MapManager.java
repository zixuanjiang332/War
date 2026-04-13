package jdd.war.map;

import io.papermc.paper.registry.RegistryAccess;
import io.papermc.paper.registry.RegistryKey;
import io.papermc.paper.registry.TypedKey;
import io.papermc.paper.registry.keys.GameRuleKeys;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import jdd.war.War;
import jdd.war.bootstrap.PluginConfigManager;
import jdd.war.game.GameService;
import org.bukkit.Bukkit;
import org.bukkit.GameRule;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

public final class MapManager {
    private static final long ROTATION_INTERVAL_MILLIS = 30L * 60L * 1000L;
    private static final List<RotationWarning> WARNINGS = List.of(
            new RotationWarning(15L * 60L * 1000L, "15 分钟", false),
            new RotationWarning(10L * 60L * 1000L, "10 分钟", false),
            new RotationWarning(5L * 60L * 1000L, "5 分钟", false),
            new RotationWarning(30L * 1000L, "30 秒", true)
    );

    private final War plugin;
    private final PluginConfigManager pluginConfigManager;
    private final MapConfig mapConfig;
    private final List<String> mapPool;
    private final Set<Long> sentWarnings = new HashSet<>();

    private int currentMapIndex;
    private long nextRotationAt;
    private String activeMapName;
    private World activeWorld;
    private Path activeWorldFolder;
    private GameService gameService;
    private BukkitTask rotationTicker;

    public MapManager(War plugin, PluginConfigManager pluginConfigManager) {
        this.plugin = plugin;
        this.pluginConfigManager = pluginConfigManager;
        this.mapConfig = new MapConfig(plugin);
        this.mapPool = new ArrayList<>(mapConfig.getAvailableMaps());
    }

    public void setGameService(GameService gameService) {
        this.gameService = gameService;
    }

    public void initMapRotation() {
        if (mapPool.isEmpty()) {
            plugin.getLogger().warning("map_data.yml 中没有任何地图配置。");
            return;
        }

        rotateMap();
        rotationTicker = Bukkit.getScheduler().runTaskTimer(plugin, this::tickRotation, 20L, 20L);
    }

    public void rotateMap() {
        if (mapPool.isEmpty()) {
            return;
        }

        String nextMapName = mapPool.get(currentMapIndex);
        LoadedMap loadedMap = loadMap(nextMapName);
        if (loadedMap == null) {
            return;
        }

        World previousWorld = activeWorld;
        Path previousFolder = activeWorldFolder;
        List<Player> participants = gameService != null ? new ArrayList<>(gameService.getOnlineParticipants()) : List.of();

        activeWorld = loadedMap.world();
        activeWorldFolder = loadedMap.folder();
        activeMapName = nextMapName;
        currentMapIndex = (currentMapIndex + 1) % mapPool.size();
        nextRotationAt = System.currentTimeMillis() + ROTATION_INTERVAL_MILLIS;
        sentWarnings.clear();

        if (gameService != null && !participants.isEmpty()) {
            gameService.moveParticipantsToNewMap(participants, nextMapName);
        }

        unloadAndCleanWorld(previousWorld, previousFolder);
        announceRotationComplete(nextMapName);

        if (gameService != null) {
            gameService.refreshAllUi();
        }
    }

    public Location getSpawnLocation() {
        if (activeWorld == null || activeMapName == null) {
            return null;
        }

        List<Double> values = mapConfig.getSpawnLocation(activeMapName);
        return new Location(activeWorld, values.get(0), values.get(1), values.get(2));
    }

    public World getActiveWorld() {
        return activeWorld;
    }

    public String getActiveMapName() {
        return activeMapName == null ? "加载中" : activeMapName;
    }

    public String getNextMapName() {
        if (mapPool.isEmpty()) {
            return "未配置";
        }
        return mapPool.get(currentMapIndex % mapPool.size());
    }

    public long getMillisUntilNextRotation() {
        if (nextRotationAt <= 0L) {
            return ROTATION_INTERVAL_MILLIS;
        }
        return Math.max(0L, nextRotationAt - System.currentTimeMillis());
    }

    public void shutdown() {
        if (rotationTicker != null) {
            rotationTicker.cancel();
        }
        unloadAndCleanWorld(activeWorld, activeWorldFolder);
        activeWorld = null;
        activeWorldFolder = null;
        activeMapName = null;
    }

    private void tickRotation() {
        if (activeWorld == null) {
            return;
        }

        long remaining = getMillisUntilNextRotation();
        for (RotationWarning warning : WARNINGS) {
            if (remaining <= warning.remainingMillis() && sentWarnings.add(warning.remainingMillis())) {
                announceRotationWarning(warning);
            }
        }

        if (remaining <= 0L) {
            rotateMap();
            return;
        }

        if (gameService != null) {
            gameService.refreshAllScoreboards();
        }
    }

    private void announceRotationWarning(RotationWarning warning) {
        String message = "§9§l[职业战争] §f地图将在 §b" + warning.label() + " §f后轮换至 §3" + getNextMapName() + "§f。";
        for (Player player : Bukkit.getOnlinePlayers()) {
            player.sendMessage(message);
            player.playSound(player.getLocation(), warning.useTitle() ? Sound.BLOCK_BELL_USE : Sound.BLOCK_NOTE_BLOCK_PLING, 0.8F, warning.useTitle() ? 0.9F : 1.2F);
            if (warning.useTitle()) {
                player.sendTitle("§9§l职业战争", "§f30 秒后切换到 §b" + getNextMapName(), 10, 40, 10);
            }
        }
    }

    private void announceRotationComplete(String mapName) {
        String message = "§9§l[职业战争] §f地图已切换至 §b" + mapName + "§f。";
        for (Player player : Bukkit.getOnlinePlayers()) {
            player.sendMessage(message);
            player.playSound(player.getLocation(), Sound.BLOCK_AMETHYST_BLOCK_RESONATE, 0.7F, 1.2F);
        }
    }

    private LoadedMap loadMap(String mapName) {
        Path sourceFolder = plugin.getDataFolder().toPath().resolve("maps").resolve(mapName);
        if (!Files.exists(sourceFolder)) {
            plugin.getLogger().severe("找不到地图源目录: " + sourceFolder);
            return null;
        }

        Path worldFolder = Bukkit.getWorldContainer().toPath().resolve(mapName + "_active_" + System.currentTimeMillis());
        try {
            copyDirectory(sourceFolder, worldFolder);
        } catch (IOException exception) {
            plugin.getLogger().severe("复制地图失败: " + exception.getMessage());
            return null;
        }

        World world = Bukkit.createWorld(new WorldCreator(worldFolder.getFileName().toString()));
        if (world == null) {
            plugin.getLogger().severe("加载地图失败: " + mapName);
            return null;
        }

        world.setAutoSave(false);
        world.setPVP(true);
        configureWorld(world);
        return new LoadedMap(world, worldFolder);
    }

    private void unloadAndCleanWorld(World world, Path folder) {
        if (world != null) {
            for (Player player : new ArrayList<>(world.getPlayers())) {
                player.teleport(pluginConfigManager.getLobbyLocation());
            }
            Bukkit.unloadWorld(world, false);
        }

        if (folder != null && Files.exists(folder)) {
            try {
                deleteDirectory(folder);
            } catch (IOException exception) {
                plugin.getLogger().warning("删除旧地图目录失败: " + exception.getMessage());
            }
        }
    }

    private void configureWorld(World world) {
        setRule(world, GameRuleKeys.KEEP_INVENTORY, true);
        setRule(world, GameRuleKeys.SHOW_DEATH_MESSAGES, true);
        setRule(world, GameRuleKeys.SPAWN_MONSTERS, false);
        setRule(world, GameRuleKeys.ADVANCE_TIME, false);
        setRule(world, GameRuleKeys.ADVANCE_WEATHER, false);
        setRule(world, GameRuleKeys.NATURAL_HEALTH_REGENERATION, true);
        setRule(world, GameRuleKeys.IMMEDIATE_RESPAWN, true);
    }

    private void copyDirectory(Path source, Path target) throws IOException {
        Files.walkFileTree(source, new SimpleFileVisitor<>() {
            @Override
            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                Path resolved = target.resolve(source.relativize(dir));
                Files.createDirectories(resolved);
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                Path resolved = target.resolve(source.relativize(file));
                Files.copy(file, resolved, StandardCopyOption.REPLACE_EXISTING);
                return FileVisitResult.CONTINUE;
            }
        });
    }

    private void deleteDirectory(Path target) throws IOException {
        Files.walk(target)
                .sorted(Comparator.reverseOrder())
                .forEach(path -> {
                    try {
                        Files.deleteIfExists(path);
                    } catch (IOException exception) {
                        throw new RuntimeException(exception);
                    }
                });
    }

    @SuppressWarnings("unchecked")
    private <T> void setRule(World world, TypedKey<GameRule<?>> key, T value) {
        GameRule<T> rule = (GameRule<T>) RegistryAccess.registryAccess()
                .getRegistry(RegistryKey.GAME_RULE)
                .get(key);
        if (rule != null) {
            world.setGameRule(rule, value);
        }
    }

    private record LoadedMap(World world, Path folder) {
    }

    private record RotationWarning(long remainingMillis, String label, boolean useTitle) {
    }
}
