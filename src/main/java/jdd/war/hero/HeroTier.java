package jdd.war.hero;

import java.util.Arrays;
import java.util.EnumMap;
import java.util.Map;

public enum HeroTier {
    TIER_5("Tier 5", 0),
    TIER_4("Tier 4", 100),
    TIER_3("Tier 3", 300),
    TIER_2("Tier 2", 800),
    TIER_1("Tier 1", 1600);

    private final String displayName;
    private final int defaultRequiredKills;
    private static final Map<HeroTier, Integer> REQUIRED_KILLS = new EnumMap<>(HeroTier.class);

    static {
        for (HeroTier tier : values()) {
            REQUIRED_KILLS.put(tier, tier.defaultRequiredKills);
        }
    }

    HeroTier(String displayName, int requiredKills) {
        this.displayName = displayName;
        this.defaultRequiredKills = requiredKills;
    }

    public String getDisplayName() {
        return displayName;
    }

    public int getRequiredKills() {
        return REQUIRED_KILLS.getOrDefault(this, defaultRequiredKills);
    }

    public boolean isUnlocked(int kills) {
        return kills >= getRequiredKills();
    }

    public HeroTier previous() {
        int index = ordinal();
        return index == 0 ? null : values()[index - 1];
    }

    public HeroTier next() {
        int index = ordinal();
        return index == values().length - 1 ? null : values()[index + 1];
    }

    public static HeroTier fromDisplayName(String value) {
        return Arrays.stream(values())
                .filter(tier -> tier.displayName.equalsIgnoreCase(value))
                .findFirst()
                .orElse(TIER_1);
    }

    public static HeroTier highestUnlocked(int kills) {
        HeroTier highest = TIER_5;
        for (HeroTier tier : values()) {
            if (kills >= tier.getRequiredKills()) {
                highest = tier;
            }
        }
        return highest;
    }

    public static void configure(Map<HeroTier, Integer> thresholds) {
        for (HeroTier tier : values()) {
            REQUIRED_KILLS.put(tier, thresholds.getOrDefault(tier, tier.defaultRequiredKills));
        }
    }
}
