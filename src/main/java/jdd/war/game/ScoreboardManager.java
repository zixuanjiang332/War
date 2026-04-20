package jdd.war.game;

import fr.mrmicky.fastboard.FastBoard;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import jdd.war.data.PlayerData;
import jdd.war.data.PlayerDataService;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Criteria;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.RenderType;
import org.bukkit.scoreboard.Scoreboard;

public final class ScoreboardManager {
    private static final String HEALTH_OBJECTIVE_NAME = "war_health";

    private final PlayerDataService playerDataService;
    private final Map<UUID, FastBoard> boards = new HashMap<>();
    private final Map<UUID, String> lastTitles = new HashMap<>();
    private final Map<UUID, List<String>> lastSidebarLines = new HashMap<>();
    private final Map<UUID, String> lastHeaders = new HashMap<>();
    private final Map<UUID, String> lastFooters = new HashMap<>();
    private final Map<UUID, String> lastPlayerListNames = new HashMap<>();
    private GameService gameService;

    public ScoreboardManager(PlayerDataService playerDataService) {
        this.playerDataService = playerDataService;
    }

    public void setGameService(GameService gameService) {
        this.gameService = gameService;
    }

    public void setupBoard(Player player) {
        removeBoard(player);
        FastBoard board = new FastBoard(player);
        boards.put(player.getUniqueId(), board);
        ensureHealthDisplay(player);
        updateBoard(player);
        refreshPlayerListName(player);
    }

    public void updateBoard(Player player) {
        if (!player.isOnline()) {
            return;
        }

        FastBoard board = boards.computeIfAbsent(player.getUniqueId(), ignored -> new FastBoard(player));
        ensureHealthDisplay(player);
        String title = ChatColor.DARK_BLUE.toString() + ChatColor.BOLD + "Cntier";
        List<String> lines = buildSidebarLines(player);
        UUID playerId = player.getUniqueId();

        if (!title.equals(lastTitles.get(playerId))) {
            board.updateTitle(title);
            lastTitles.put(playerId, title);
        }

        if (!lines.equals(lastSidebarLines.get(playerId))) {
            board.updateLines(lines);
            lastSidebarLines.put(playerId, List.copyOf(lines));
        }

        updateTabBranding(player);
    }

    public void refreshPlayerListName(Player player) {
        if (!player.isOnline()) {
            return;
        }

        PlayerData data = playerDataService.getOrCreate(player);
        PlayerTier tier = PlayerTier.fromKills(data.getKills());
        String listName = tier.getPrefix() + player.getName();
        UUID playerId = player.getUniqueId();
        if (!listName.equals(lastPlayerListNames.get(playerId))) {
            player.setPlayerListName(listName);
            lastPlayerListNames.put(playerId, listName);
        }
    }

    public void removeBoard(Player player) {
        UUID playerId = player.getUniqueId();
        FastBoard board = boards.remove(playerId);
        if (board != null) {
            board.delete();
        }
        lastTitles.remove(playerId);
        lastSidebarLines.remove(playerId);
        lastHeaders.remove(playerId);
        lastFooters.remove(playerId);
        lastPlayerListNames.remove(playerId);
        player.setPlayerListName(player.getName());
        player.setPlayerListHeaderFooter("", "");
    }

    public void closeAll() {
        for (FastBoard board : boards.values()) {
            board.delete();
        }
        boards.clear();
        lastTitles.clear();
        lastSidebarLines.clear();
        lastHeaders.clear();
        lastFooters.clear();
        lastPlayerListNames.clear();
    }

    private void ensureHealthDisplay(Player player) {
        Scoreboard scoreboard = player.getScoreboard();
        Objective objective = scoreboard.getObjective(HEALTH_OBJECTIVE_NAME);
        if (objective == null) {
            objective = scoreboard.registerNewObjective(
                    HEALTH_OBJECTIVE_NAME,
                    Criteria.HEALTH,
                    ChatColor.AQUA + "❤",
                    RenderType.INTEGER
            );
        } else {
            objective.setDisplayName(ChatColor.AQUA + "❤");
            objective.setRenderType(RenderType.INTEGER);
        }
        if (objective.getDisplaySlot() != DisplaySlot.BELOW_NAME) {
            objective.setDisplaySlot(DisplaySlot.BELOW_NAME);
        }
    }

    private List<String> buildSidebarLines(Player player) {
        PlayerData data = playerDataService.getOrCreate(player);
        PlayerTier tier = PlayerTier.fromKills(data.getKills());
        boolean inBattle = gameService != null && gameService.isParticipant(player);
        String currentMap = gameService != null ? gameService.getCurrentMapName() : "加载中";
        String countdown = gameService != null ? gameService.getRotationCountdownText() : "--:--";
        String progress = formatProgress(data.getKills(), tier);

        List<String> lines = new ArrayList<>();
        lines.add(ChatColor.AQUA + "职业战争");
        lines.add(ChatColor.DARK_GRAY + "----------------");
        lines.add(ChatColor.WHITE + "段位: " + colorizeTier(tier));
        lines.add(ChatColor.WHITE + "进度: " + ChatColor.AQUA + progress);
        lines.add(ChatColor.WHITE + "地图: " + ChatColor.BLUE + currentMap);

        if (inBattle) {
            lines.add(ChatColor.WHITE + "职业: " + ChatColor.DARK_AQUA + gameService.getHeroDisplayName(player));
            lines.add(ChatColor.WHITE + "状态: " + ChatColor.AQUA + gameService.getStateDisplayName(player));
            lines.add(ChatColor.WHITE + "击杀: " + ChatColor.AQUA + data.getKills());
            lines.add(ChatColor.WHITE + "死亡: " + ChatColor.AQUA + data.getDeaths());
            lines.add(ChatColor.WHITE + "K/D: " + ChatColor.AQUA + String.format("%.2f", data.getKd()));
        } else {
            lines.add(ChatColor.WHITE + "状态: " + ChatColor.AQUA + "大厅");
            lines.add(ChatColor.WHITE + "在线: " + ChatColor.AQUA + Bukkit.getOnlinePlayers().size());
            lines.add(ChatColor.WHITE + "总击杀: " + ChatColor.AQUA + data.getKills());
        }

        lines.add(ChatColor.WHITE + "换图: " + ChatColor.AQUA + countdown);
        lines.add(ChatColor.BLUE + "cntier.club");
        return lines;
    }

    private void updateTabBranding(Player player) {
        if (gameService == null) {
            return;
        }

        PlayerData data = playerDataService.getOrCreate(player);
        PlayerTier tier = PlayerTier.fromKills(data.getKills());
        String status = gameService.isParticipant(player) ? "战场" : "大厅";
        String header = ChatColor.DARK_BLUE.toString() + ChatColor.BOLD + "Cntier\n"
                + ChatColor.AQUA + "职业战争 " + ChatColor.DARK_GRAY + "| " + ChatColor.WHITE + status + "\n"
                + ChatColor.GRAY + "地图: " + ChatColor.AQUA + gameService.getCurrentMapName();
        String footer = ChatColor.GRAY + "段位: " + colorizeTier(tier) + "\n"
                + ChatColor.GRAY + "换图: " + ChatColor.AQUA + gameService.getRotationCountdownText() + "\n"
                + ChatColor.BLUE + "cntier.club";

        UUID playerId = player.getUniqueId();
        if (!header.equals(lastHeaders.get(playerId))) {
            player.setPlayerListHeader(header);
            lastHeaders.put(playerId, header);
        }
        if (!footer.equals(lastFooters.get(playerId))) {
            player.setPlayerListFooter(footer);
            lastFooters.put(playerId, footer);
        }
    }

    private String colorizeTier(PlayerTier tier) {
        return switch (tier) {
            case LT5 -> ChatColor.GRAY + tier.getDisplayName();
            case HT5 -> ChatColor.BLUE + tier.getDisplayName();
            case LT4 -> ChatColor.AQUA + tier.getDisplayName();
            case HT4 -> ChatColor.DARK_AQUA + tier.getDisplayName();
            case LT3 -> ChatColor.WHITE + tier.getDisplayName();
            case HT3 -> ChatColor.BLUE.toString() + ChatColor.BOLD + tier.getDisplayName();
            case LT2 -> ChatColor.DARK_BLUE.toString() + ChatColor.BOLD + tier.getDisplayName();
            case HT2 -> ChatColor.AQUA.toString() + ChatColor.BOLD + tier.getDisplayName();
            case LT1 -> ChatColor.WHITE.toString() + ChatColor.BOLD + tier.getDisplayName();
            case HT1 -> ChatColor.BLUE.toString() + ChatColor.BOLD + tier.getDisplayName();
        };
    }

    private String formatProgress(int kills, PlayerTier currentTier) {
        PlayerTier nextTier = currentTier.next();
        int target = nextTier == null ? currentTier.getRequiredKills() : nextTier.getRequiredKills();
        return kills + "/" + target;
    }
}
