package jdd.war.listener;

import jdd.war.War;
import jdd.war.data.PlayerDataService;
import jdd.war.game.GameService;
import jdd.war.game.ScoreboardManager;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;

public final class PlayerLifecycleListener implements Listener {
    private final War plugin;
    private final GameService gameService;
    private final PlayerDataService playerDataService;
    private final ScoreboardManager scoreboardManager;

    public PlayerLifecycleListener(War plugin, GameService gameService, PlayerDataService playerDataService, ScoreboardManager scoreboardManager) {
        this.plugin = plugin;
        this.gameService = gameService;
        this.playerDataService = playerDataService;
        this.scoreboardManager = scoreboardManager;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        playerDataService.loadPlayerAsync(player);
        scoreboardManager.setupBoard(player);
        gameService.sendToLobby(player);

        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            if (player.isOnline()) {
                jdd.war.gui.BrawlMenuGUI.open(player);
                gameService.refreshAllUi();
            }
        }, 5L);
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        gameService.handleQuit(player);
        playerDataService.unloadPlayer(player);
        Bukkit.getScheduler().runTask(plugin, gameService::refreshAllUi);
    }

    @EventHandler
    public void onRespawn(PlayerRespawnEvent event) {
        gameService.handleRespawn(event);
    }

    @EventHandler
    public void onChangedWorld(PlayerChangedWorldEvent event) {
        gameService.refreshAllUi();
    }
}
