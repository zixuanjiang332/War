package jdd.war.hero;

import java.io.File;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import java.util.logging.Logger;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

public final class HeroSkillConfig {
    private final JavaPlugin plugin;
    private final Logger logger;
    private final Set<String> warnedPaths = new HashSet<>();
    private YamlConfiguration configuration;

    public HeroSkillConfig(JavaPlugin plugin) {
        this.plugin = plugin;
        this.logger = plugin.getLogger();
        reload();
    }

    public void reload() {
        plugin.saveResource("heroes.yml", false);
        File file = new File(plugin.getDataFolder(), "heroes.yml");
        configuration = YamlConfiguration.loadConfiguration(file);
        validateHeroKeys();
    }

    public long cooldownMillis(HeroClass heroClass, String skillKey, double defaultSeconds) {
        return Math.round(value(heroClass, skillKey, "cooldown-seconds", defaultSeconds) * 1000.0D);
    }

    public double value(HeroClass heroClass, String skillKey, String field, double defaultValue) {
        String path = path(heroClass, skillKey, field);
        if (!configuration.isSet(path)) {
            warnOnce(path, defaultValue);
            return defaultValue;
        }
        return configuration.getDouble(path, defaultValue);
    }

    public int intValue(HeroClass heroClass, String skillKey, String field, int defaultValue) {
        String path = path(heroClass, skillKey, field);
        if (!configuration.isSet(path)) {
            warnOnce(path, defaultValue);
            return defaultValue;
        }
        return configuration.getInt(path, defaultValue);
    }

    private void validateHeroKeys() {
        ConfigurationSection section = configuration.getConfigurationSection("heroes");
        if (section == null) {
            logger.warning("heroes.yml 缺少 heroes 根节点，将回退到代码默认值。");
            return;
        }

        Set<String> validKeys = new HashSet<>(Arrays.stream(HeroClass.values())
                .map(this::heroKey)
                .toList());

        for (String key : section.getKeys(false)) {
            if (!validKeys.contains(key)) {
                logger.warning("heroes.yml 存在未知职业配置: " + key);
            }
        }
    }

    private String path(HeroClass heroClass, String skillKey, String field) {
        return "heroes." + heroKey(heroClass) + "." + skillKey + "." + field;
    }

    private String heroKey(HeroClass heroClass) {
        return heroClass.name().toLowerCase(Locale.ROOT);
    }

    private void warnOnce(String path, Object defaultValue) {
        if (warnedPaths.add(path)) {
            logger.warning("heroes.yml 缺少配置 " + path + "，已回退到默认值 " + defaultValue);
        }
    }
}
