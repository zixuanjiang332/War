package jdd.war.map;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import jdd.war.War;
import org.bukkit.configuration.file.YamlConfiguration;

public final class MapConfig {
    private final War plugin;
    private YamlConfiguration config;

    public MapConfig(War plugin) {
        this.plugin = plugin;
        load();
    }

    public void load() {
        File file = new File(plugin.getDataFolder(), "map_data.yml");
        if (!file.exists()) {
            plugin.saveResource("map_data.yml", false);
        }
        config = YamlConfiguration.loadConfiguration(file);
    }

    public List<String> getAvailableMaps() {
        return new ArrayList<>(config.getKeys(false));
    }

    public List<Double> getSpawnLocation(String mapName) {
        List<Double> values = config.getDoubleList(mapName + ".spawn-location");
        if (values.size() >= 3) {
            return values;
        }
        return List.of(0.5D, 100.0D, 0.5D);
    }
}
