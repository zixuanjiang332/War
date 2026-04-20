package jdd.war;

import jdd.war.bootstrap.PluginConfigManager;
import jdd.war.command.LeaveCommand;
import jdd.war.data.DatabaseManager;
import jdd.war.data.PlayerDataRepository;
import jdd.war.data.PlayerDataService;
import jdd.war.game.GameService;
import jdd.war.game.ProgressionConfig;
import jdd.war.game.ScoreboardManager;
import jdd.war.hero.HeroRegistry;
import jdd.war.hero.HeroService;
import jdd.war.hero.HeroSkillConfig;
import jdd.war.hero.HeroSkillHandler;
import jdd.war.listener.CombatListener;
import jdd.war.listener.ChatListener;
import jdd.war.listener.HeroSkillListener;
import jdd.war.listener.LobbyProtectionListener;
import jdd.war.listener.MenuListener;
import jdd.war.listener.PlayerLifecycleListener;
import jdd.war.map.MapManager;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

public final class War extends JavaPlugin {
    private PluginConfigManager pluginConfigManager;
    private DatabaseManager databaseManager;
    private PlayerDataRepository playerDataRepository;
    private PlayerDataService playerDataService;
    private ScoreboardManager scoreboardManager;
    private ProgressionConfig progressionConfig;
    private MapManager mapManager;
    private HeroRegistry heroRegistry;
    private HeroService heroService;
    private GameService gameService;
    private HeroSkillConfig heroSkillConfig;
    private HeroSkillHandler heroSkillHandler;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        saveResource("heroes.yml", false);
        saveResource("progression.yml", false);

        pluginConfigManager = PluginConfigManager.init(this);
        progressionConfig = new ProgressionConfig(this);
        progressionConfig.load();
        databaseManager = new DatabaseManager(this);
        databaseManager.connect();
        playerDataRepository = new PlayerDataRepository(databaseManager, getLogger());
        playerDataService = new PlayerDataService(this, playerDataRepository);

        scoreboardManager = new ScoreboardManager(playerDataService);
        mapManager = new MapManager(this, pluginConfigManager, playerDataService);
        heroRegistry = new HeroRegistry();
        heroService = new HeroService(heroRegistry, playerDataService);
        heroSkillConfig = new HeroSkillConfig(this);
        gameService = new GameService(this, pluginConfigManager, mapManager, heroService, playerDataService, scoreboardManager);
        heroSkillHandler = new HeroSkillHandler(this, gameService, heroSkillConfig, heroRegistry);

        gameService.setHeroSkillHandler(heroSkillHandler);
        scoreboardManager.setGameService(gameService);
        mapManager.setGameService(gameService);
        playerDataService.startBackgroundTasks();
        gameService.startBackgroundTasks();
        mapManager.initMapRotation();
        playerDataService.preloadCacheAsync(() -> {
            mapManager.refreshSpawnLeaderboards();
            gameService.refreshAllScoreboards();
            for (var player : getServer().getOnlinePlayers()) {
                scoreboardManager.refreshPlayerListName(player);
            }
        });

        registerListeners();
        registerCommands();

        getLogger().info("职业战争已启动。");
    }

    @Override
    public void onDisable() {
        if (gameService != null) {
            gameService.shutdown();
        }
        if (playerDataService != null) {
            playerDataService.shutdown();
        }
        if (mapManager != null) {
            mapManager.shutdown();
        }
        if (databaseManager != null) {
            databaseManager.disconnect();
        }
    }

    private void registerListeners() {
        PluginManager pluginManager = getServer().getPluginManager();
        pluginManager.registerEvents(new PlayerLifecycleListener(this, gameService, playerDataService, scoreboardManager), this);
        pluginManager.registerEvents(new LobbyProtectionListener(pluginConfigManager), this);
        pluginManager.registerEvents(new MenuListener(gameService, heroService, heroRegistry), this);
        pluginManager.registerEvents(new HeroSkillListener(gameService, heroSkillHandler), this);
        pluginManager.registerEvents(new CombatListener(gameService), this);
        pluginManager.registerEvents(new ChatListener(playerDataService), this);
    }

    private void registerCommands() {
        if (getCommand("leave") != null) {
            getCommand("leave").setExecutor(new LeaveCommand(gameService));
        }
    }

    public GameService getGameService() {
        return gameService;
    }

    public MapManager getMapManager() {
        return mapManager;
    }
}
