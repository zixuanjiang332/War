package jdd.war.data;

import java.util.List;

public record LeaderboardSnapshot(
        List<LeaderboardEntry> topKills,
        List<LeaderboardEntry> topDeaths,
        List<LeaderboardEntry> topKd
) {
    public static LeaderboardSnapshot empty() {
        return new LeaderboardSnapshot(List.of(), List.of(), List.of());
    }
}
