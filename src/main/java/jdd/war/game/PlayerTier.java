package jdd.war.game;

import java.util.EnumMap;
import java.util.Map;
import org.bukkit.ChatColor;

public enum PlayerTier {
    LT5("LT5", 0, 300, ChatColor.DARK_GRAY + "[" + ChatColor.GRAY + "LT5" + ChatColor.DARK_GRAY + "] " + ChatColor.WHITE),
    HT5("HT5", 300, 800, ChatColor.DARK_BLUE + "[" + ChatColor.BLUE + "HT5" + ChatColor.DARK_BLUE + "] " + ChatColor.WHITE),
    LT4("LT4", 800, 2_000, ChatColor.BLUE + "[" + ChatColor.AQUA + "LT4" + ChatColor.BLUE + "] " + ChatColor.WHITE),
    HT4("HT4", 2_000, 4_000, ChatColor.DARK_AQUA + "[" + ChatColor.AQUA + "HT4" + ChatColor.DARK_AQUA + "] " + ChatColor.WHITE),
    LT3("LT3", 4_000, 8_000, ChatColor.AQUA + "[" + ChatColor.WHITE + "LT3" + ChatColor.AQUA + "] " + ChatColor.WHITE),
    HT3("HT3", 8_000, 15_000, ChatColor.BLUE + "" + ChatColor.BOLD + "[" + ChatColor.WHITE + "HT3" + ChatColor.BLUE + ChatColor.BOLD + "] " + ChatColor.WHITE),
    LT2("LT2", 15_000, 30_000, ChatColor.DARK_BLUE + "" + ChatColor.BOLD + "[" + ChatColor.AQUA + "LT2" + ChatColor.DARK_BLUE + ChatColor.BOLD + "] " + ChatColor.WHITE),
    HT2("HT2", 30_000, 60_000, ChatColor.AQUA + "" + ChatColor.BOLD + "[" + ChatColor.BLUE + "HT2" + ChatColor.AQUA + ChatColor.BOLD + "] " + ChatColor.WHITE),
    LT1("LT1", 60_000, 300_000, ChatColor.WHITE + "" + ChatColor.BOLD + "[" + ChatColor.BLUE + "LT1" + ChatColor.WHITE + ChatColor.BOLD + "] " + ChatColor.WHITE),
    HT1("HT1", 300_000, 300_000, ChatColor.BLUE + "" + ChatColor.BOLD + "[" + ChatColor.AQUA + "HT1" + ChatColor.BLUE + ChatColor.BOLD + "] " + ChatColor.WHITE);

    private final String displayName;
    private final int defaultRequiredKills;
    private final String prefix;
    private static final Map<PlayerTier, Integer> REQUIRED_KILLS = new EnumMap<>(PlayerTier.class);

    static {
        for (PlayerTier tier : values()) {
            REQUIRED_KILLS.put(tier, tier.defaultRequiredKills);
        }
    }

    PlayerTier(String displayName, int requiredKills, int nextRequiredKills, String prefix) {
        this.displayName = displayName;
        this.defaultRequiredKills = requiredKills;
        this.prefix = prefix;
    }

    public String getDisplayName() {
        return displayName;
    }

    public int getRequiredKills() {
        return REQUIRED_KILLS.getOrDefault(this, defaultRequiredKills);
    }

    public int getNextRequiredKills() {
        PlayerTier next = next();
        return next == null ? getRequiredKills() : next.getRequiredKills();
    }

    public String getPrefix() {
        return prefix;
    }

    public PlayerTier next() {
        int index = ordinal();
        return index == values().length - 1 ? null : values()[index + 1];
    }

    public static PlayerTier fromKills(int kills) {
        PlayerTier current = LT5;
        for (PlayerTier tier : values()) {
            if (kills >= tier.getRequiredKills()) {
                current = tier;
            } else {
                break;
            }
        }
        return current;
    }

    public static void configure(Map<PlayerTier, Integer> thresholds) {
        for (PlayerTier tier : values()) {
            REQUIRED_KILLS.put(tier, thresholds.getOrDefault(tier, tier.defaultRequiredKills));
        }
    }
}
