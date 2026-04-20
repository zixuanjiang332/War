package jdd.war.game;

import java.io.File;
import java.util.EnumMap;
import java.util.Map;
import jdd.war.hero.HeroTier;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

public final class ProgressionConfig {
    private final JavaPlugin plugin;

    public ProgressionConfig(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    public void load() {
        plugin.saveResource("progression.yml", false);
        File file = new File(plugin.getDataFolder(), "progression.yml");
        YamlConfiguration config = YamlConfiguration.loadConfiguration(file);

        Map<HeroTier, Integer> heroThresholds = new EnumMap<>(HeroTier.class);
        heroThresholds.put(HeroTier.TIER_5, config.getInt("hero-tier-unlocks.tier-5", 0));
        heroThresholds.put(HeroTier.TIER_4, config.getInt("hero-tier-unlocks.tier-4", 100));
        heroThresholds.put(HeroTier.TIER_3, config.getInt("hero-tier-unlocks.tier-3", 300));
        heroThresholds.put(HeroTier.TIER_2, config.getInt("hero-tier-unlocks.tier-2", 800));
        heroThresholds.put(HeroTier.TIER_1, config.getInt("hero-tier-unlocks.tier-1", 1600));
        HeroTier.configure(heroThresholds);

        Map<PlayerTier, Integer> playerThresholds = new EnumMap<>(PlayerTier.class);
        playerThresholds.put(PlayerTier.LT5, config.getInt("player-ranks.lt5", 0));
        playerThresholds.put(PlayerTier.HT5, config.getInt("player-ranks.ht5", 300));
        playerThresholds.put(PlayerTier.LT4, config.getInt("player-ranks.lt4", 800));
        playerThresholds.put(PlayerTier.HT4, config.getInt("player-ranks.ht4", 2000));
        playerThresholds.put(PlayerTier.LT3, config.getInt("player-ranks.lt3", 4000));
        playerThresholds.put(PlayerTier.HT3, config.getInt("player-ranks.ht3", 8000));
        playerThresholds.put(PlayerTier.LT2, config.getInt("player-ranks.lt2", 15000));
        playerThresholds.put(PlayerTier.HT2, config.getInt("player-ranks.ht2", 30000));
        playerThresholds.put(PlayerTier.LT1, config.getInt("player-ranks.lt1", 60000));
        playerThresholds.put(PlayerTier.HT1, config.getInt("player-ranks.ht1", 300000));
        PlayerTier.configure(playerThresholds);
    }
}
