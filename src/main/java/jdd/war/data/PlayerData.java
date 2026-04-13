package jdd.war.data;

import java.util.UUID;

public final class PlayerData {
    private final UUID uuid;
    private String name;
    private int kills;
    private int deaths;

    public PlayerData(UUID uuid, String name, int kills, int deaths) {
        this.uuid = uuid;
        this.name = name;
        this.kills = kills;
        this.deaths = deaths;
    }

    public UUID getUuid() {
        return uuid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getKills() {
        return kills;
    }

    public int getDeaths() {
        return deaths;
    }

    public void incrementKills() {
        kills++;
    }

    public void incrementDeaths() {
        deaths++;
    }

    public double getKd() {
        if (deaths == 0) {
            return kills;
        }
        return (double) kills / (double) deaths;
    }

    public PlayerData copy() {
        return new PlayerData(uuid, name, kills, deaths);
    }
}
