package jdd.war.map;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import jdd.war.data.LeaderboardEntry;
import jdd.war.data.LeaderboardSnapshot;
import jdd.war.data.PlayerDataService;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.ArmorStand;

public final class SpawnLeaderboardManager {
    private static final int MAX_ROWS = 10;
    private static final String LEADERBOARD_TAG = "war_spawn_leaderboard";
    private static final double LEGACY_CLEANUP_RADIUS = 18.0D;
    private static final double ROW_STEP = 0.34D;

    private final PlayerDataService playerDataService;
    private World displayWorld;
    private final Map<ColumnType, DisplayColumn> columns = new EnumMap<>(ColumnType.class);

    public SpawnLeaderboardManager(PlayerDataService playerDataService) {
        this.playerDataService = playerDataService;
    }

    public void refresh(World world, Location spawn) {
        if (world == null || spawn == null) {
            clear();
            return;
        }

        if (shouldRebuild(world, spawn)) {
            rebuild(world, spawn);
        }

        LeaderboardSnapshot snapshot = playerDataService.getLeaderboardSnapshot();
        updateColumn(
                ColumnType.KILLS,
                ChatColor.DARK_BLUE + "" + ChatColor.BOLD + "生涯击杀榜",
                snapshot.topKills(),
                entry -> ChatColor.AQUA + String.valueOf(entry.kills())
        );
        updateColumn(
                ColumnType.DEATHS,
                ChatColor.BLUE + "" + ChatColor.BOLD + "生涯死亡榜",
                snapshot.topDeaths(),
                entry -> ChatColor.AQUA + String.valueOf(entry.deaths())
        );
        updateColumn(
                ColumnType.KD,
                ChatColor.AQUA + "" + ChatColor.BOLD + "K/D 榜",
                snapshot.topKd(),
                entry -> ChatColor.AQUA + String.format("%.2f", entry.kd())
        );
    }

    public void clear() {
        for (DisplayColumn column : columns.values()) {
            column.remove();
        }
        columns.clear();
        displayWorld = null;
    }

    private boolean shouldRebuild(World world, Location spawn) {
        if (displayWorld == null || !displayWorld.equals(world) || columns.size() != ColumnType.values().length) {
            return true;
        }
        if (hasLegacyAroundSpawn(world, spawn)) {
            return true;
        }

        Map<String, List<ArmorStand>> tagged = collectTagged(world);
        for (ColumnType type : ColumnType.values()) {
            if (!validateColumn(type, tagged)) {
                return true;
            }
        }

        for (DisplayColumn column : columns.values()) {
            if (!column.isValid()) {
                return true;
            }
        }
        return false;
    }

    private void rebuild(World world, Location spawn) {
        clear();
        purgeTagged(world);
        purgeLegacyAroundSpawn(world, spawn);
        displayWorld = world;

        columns.put(ColumnType.KILLS, createColumn(world, ColumnType.KILLS, spawn.clone().add(-6.0D, 4.6D, 3.5D)));
        columns.put(ColumnType.DEATHS, createColumn(world, ColumnType.DEATHS, spawn.clone().add(0.0D, 4.6D, 5.0D)));
        columns.put(ColumnType.KD, createColumn(world, ColumnType.KD, spawn.clone().add(6.0D, 4.6D, 3.5D)));
    }

    private DisplayColumn createColumn(World world, ColumnType type, Location top) {
        ArmorStand title = spawnStand(world, top, tag(type, "title"));
        ArmorStand info = spawnStand(world, top.clone().subtract(0.0D, ROW_STEP, 0.0D), tag(type, "info"));
        List<ArmorStand> rows = new ArrayList<>();
        Location baseLine = top.clone().subtract(0.0D, ROW_STEP * 2.0D, 0.0D);
        for (int i = 0; i < MAX_ROWS; i++) {
            rows.add(spawnStand(world, baseLine.clone().subtract(0.0D, ROW_STEP * i, 0.0D), tag(type, "row:" + i)));
        }
        return new DisplayColumn(type, title, info, rows);
    }

    private void updateColumn(
            ColumnType type,
            String title,
            List<LeaderboardEntry> entries,
            Function<LeaderboardEntry, String> valueFormatter
    ) {
        DisplayColumn column = columns.get(type);
        if (column == null) {
            return;
        }

        column.title().setCustomName(title);
        column.title().setCustomNameVisible(true);
        column.info().setCustomName(ChatColor.GRAY + "每10分钟刷新");
        column.info().setCustomNameVisible(true);

        if (entries.isEmpty()) {
            ArmorStand firstRow = column.rows().get(0);
            firstRow.setCustomName(ChatColor.GRAY + "暂无数据");
            firstRow.setCustomNameVisible(true);
            for (int i = 1; i < column.rows().size(); i++) {
                column.rows().get(i).setCustomNameVisible(false);
            }
            return;
        }

        int used = 0;
        for (; used < entries.size() && used < column.rows().size(); used++) {
            LeaderboardEntry entry = entries.get(used);
            ArmorStand row = column.rows().get(used);
            row.setCustomName(
                    ChatColor.AQUA + String.valueOf(used + 1) + ". "
                            + ChatColor.WHITE + trimName(entry.name(), type == ColumnType.KD ? 8 : 10)
                            + ChatColor.DARK_GRAY + " - "
                            + valueFormatter.apply(entry)
            );
            row.setCustomNameVisible(true);
        }

        for (int i = used; i < column.rows().size(); i++) {
            column.rows().get(i).setCustomNameVisible(false);
        }
    }

    private boolean validateColumn(ColumnType type, Map<String, List<ArmorStand>> tagged) {
        if (!isSingleValid(tagged, tag(type, "title")) || !isSingleValid(tagged, tag(type, "info"))) {
            return false;
        }
        for (int i = 0; i < MAX_ROWS; i++) {
            if (!isSingleValid(tagged, tag(type, "row:" + i))) {
                return false;
            }
        }
        return true;
    }

    private boolean isSingleValid(Map<String, List<ArmorStand>> tagged, String tag) {
        List<ArmorStand> stands = tagged.get(tag);
        return stands != null && stands.size() == 1 && stands.get(0).isValid();
    }

    private Map<String, List<ArmorStand>> collectTagged(World world) {
        Map<String, List<ArmorStand>> tagged = new HashMap<>();
        for (ArmorStand stand : world.getEntitiesByClass(ArmorStand.class)) {
            if (!stand.getScoreboardTags().contains(LEADERBOARD_TAG)) {
                continue;
            }
            for (String scoreboardTag : stand.getScoreboardTags()) {
                if (!scoreboardTag.startsWith(LEADERBOARD_TAG + ":")) {
                    continue;
                }
                tagged.computeIfAbsent(scoreboardTag, ignored -> new ArrayList<>()).add(stand);
            }
        }
        return tagged;
    }

    private ArmorStand spawnStand(World world, Location location, String logicalTag) {
        return world.spawn(location, ArmorStand.class, entity -> {
            entity.setVisible(false);
            entity.setGravity(false);
            entity.setMarker(true);
            entity.setSmall(true);
            entity.setInvulnerable(true);
            entity.setCustomNameVisible(false);
            entity.addScoreboardTag(LEADERBOARD_TAG);
            entity.addScoreboardTag(logicalTag);
        });
    }

    private void purgeTagged(World world) {
        world.getEntitiesByClass(ArmorStand.class).stream()
                .filter(entity -> entity.getScoreboardTags().contains(LEADERBOARD_TAG))
                .forEach(ArmorStand::remove);
    }

    private boolean hasLegacyAroundSpawn(World world, Location spawn) {
        for (ArmorStand stand : world.getEntitiesByClass(ArmorStand.class)) {
            if (stand.getLocation().distanceSquared(spawn) > LEGACY_CLEANUP_RADIUS * LEGACY_CLEANUP_RADIUS) {
                continue;
            }
            if (!stand.isMarker() || !stand.isSmall() || stand.isVisible() || stand.hasGravity()) {
                continue;
            }
            if (!stand.isInvulnerable() || stand.getCustomName() == null) {
                continue;
            }
            if (!stand.getScoreboardTags().contains(LEADERBOARD_TAG)) {
                return true;
            }
        }
        return false;
    }

    private void purgeLegacyAroundSpawn(World world, Location spawn) {
        for (ArmorStand stand : world.getEntitiesByClass(ArmorStand.class)) {
            if (stand.getLocation().distanceSquared(spawn) > LEGACY_CLEANUP_RADIUS * LEGACY_CLEANUP_RADIUS) {
                continue;
            }
            if (!stand.isMarker() || !stand.isSmall() || stand.isVisible() || stand.hasGravity()) {
                continue;
            }
            if (!stand.isInvulnerable() || stand.getCustomName() == null) {
                continue;
            }
            stand.remove();
        }
    }

    private String tag(ColumnType type, String suffix) {
        return LEADERBOARD_TAG + ":" + type.id() + ":" + suffix;
    }

    private String trimName(String name, int maxLength) {
        return name.length() <= maxLength ? name : name.substring(0, maxLength);
    }

    private enum ColumnType {
        KILLS("kills"),
        DEATHS("deaths"),
        KD("kd");

        private final String id;

        ColumnType(String id) {
            this.id = id;
        }

        public String id() {
            return id;
        }
    }

    private record DisplayColumn(ColumnType type, ArmorStand title, ArmorStand info, List<ArmorStand> rows) {
        private boolean isValid() {
            if (!title.isValid() || !info.isValid()) {
                return false;
            }
            for (ArmorStand row : rows) {
                if (!row.isValid()) {
                    return false;
                }
            }
            return true;
        }

        private void remove() {
            title.remove();
            info.remove();
            for (ArmorStand row : rows) {
                row.remove();
            }
        }
    }
}
