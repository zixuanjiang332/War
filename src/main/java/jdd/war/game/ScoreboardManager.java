package jdd.war.game;

import java.util.Set;
import jdd.war.data.PlayerData;
import jdd.war.data.PlayerDataService;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Criteria;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;

public final class ScoreboardManager {
    private static final String SIDEBAR_OBJECTIVE = "class_war_sidebar";
    private static final String HEALTH_OBJECTIVE = "class_war_health";

    private final PlayerDataService playerDataService;
    private GameService gameService;

    public ScoreboardManager(PlayerDataService playerDataService) {
        this.playerDataService = playerDataService;
    }

    public void setGameService(GameService gameService) {
        this.gameService = gameService;
    }

    public void setupBoard(Player player) {
        Scoreboard scoreboard = Bukkit.getScoreboardManager().getNewScoreboard();
        Objective sidebar = scoreboard.registerNewObjective(SIDEBAR_OBJECTIVE, Criteria.DUMMY, "§9§lCntier");
        sidebar.setDisplaySlot(DisplaySlot.SIDEBAR);

        Objective health = scoreboard.registerNewObjective(HEALTH_OBJECTIVE, Criteria.HEALTH, "§b❤");
        health.setDisplaySlot(DisplaySlot.BELOW_NAME);

        player.setScoreboard(scoreboard);
        updateBoard(player);
    }

    public void updateBoard(Player player) {
        if (player.getScoreboard() == null || player.getScoreboard().getObjective(SIDEBAR_OBJECTIVE) == null) {
            setupBoard(player);
            return;
        }

        Scoreboard board = player.getScoreboard();
        Objective objective = board.getObjective(SIDEBAR_OBJECTIVE);
        if (objective == null) {
            return;
        }

        clearSidebarScores(board);
        renderSidebar(player, objective);
        updateTabBranding(player);
    }

    private void renderSidebar(Player player, Objective objective) {
        PlayerData data = playerDataService.getOrCreate(player);
        boolean inBattle = gameService != null && gameService.isParticipant(player);
        String currentMap = gameService != null ? gameService.getCurrentMapName() : "加载中";
        String countdown = gameService != null ? gameService.getRotationCountdownText() : "--:--";

        objective.getScore("§b职业战争").setScore(10);
        objective.getScore("§1§m----------------").setScore(9);

        if (inBattle) {
            String hero = gameService.getHeroDisplayName(player);
            String state = gameService.getStateDisplayName(player);
            objective.getScore("§f地图: §b" + currentMap).setScore(8);
            objective.getScore("§f职业: §3" + hero).setScore(7);
            objective.getScore("§f状态: §b" + state).setScore(6);
            objective.getScore("§f击杀: §b" + data.getKills()).setScore(5);
            objective.getScore("§f死亡: §b" + data.getDeaths()).setScore(4);
            objective.getScore("§fK/D: §b" + String.format("%.2f", data.getKd())).setScore(3);
            objective.getScore("§f换图: §b" + countdown).setScore(2);
            objective.getScore("§9cntier.club").setScore(1);
            return;
        }

        objective.getScore("§f状态: §b大厅").setScore(8);
        objective.getScore("§f地图: §b" + currentMap).setScore(7);
        objective.getScore("§f换图: §b" + countdown).setScore(6);
        objective.getScore("§f在线: §b" + Bukkit.getOnlinePlayers().size()).setScore(5);
        objective.getScore("§9cntier.club").setScore(4);
    }

    private void updateTabBranding(Player player) {
        if (gameService == null) {
            return;
        }

        String status = gameService.isParticipant(player) ? "战场" : "大厅";
        String header = "§9§lCntier\n"
                + "§b职业战争 §8| §f" + status + "\n"
                + "§7地图: §b" + gameService.getCurrentMapName();
        String footer = "§7下次换图: §b" + gameService.getRotationCountdownText() + "\n"
                + "§9cntier.club";

        player.setPlayerListHeader(header);
        player.setPlayerListFooter(footer);
    }

    private void clearSidebarScores(Scoreboard board) {
        for (String entry : Set.copyOf(board.getEntries())) {
            board.resetScores(entry);
        }
    }
}
