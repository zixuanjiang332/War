package jdd.war.bootstrap;

import java.util.Objects;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

public final class PluginConfigManager {
    private final JavaPlugin plugin;
    private final FileConfiguration config;

    private PluginConfigManager(JavaPlugin plugin) {
        this.plugin = plugin;
        this.config = plugin.getConfig();
    }

    public static PluginConfigManager init(JavaPlugin plugin) {
        plugin.reloadConfig();
        return new PluginConfigManager(plugin);
    }

    public Location getLobbyLocation() {
        Location location = config.getLocation("lobby-settings.lobby-location");
        if (location != null && location.getWorld() != null) {
            return location;
        }
        World fallbackWorld = Bukkit.getWorlds().isEmpty() ? null : Bukkit.getWorlds().get(0);
        if (fallbackWorld == null) {
            throw new IllegalStateException("No worlds are loaded; cannot resolve lobby location.");
        }
        plugin.getLogger().warning("config.yml 中缺少有效大厅坐标，已回退到主世界出生点。");
        return fallbackWorld.getSpawnLocation();
    }

    public boolean isInvulnerableInLobby() {
        return config.getBoolean("lobby-settings.invulnerable-in-lobby", true);
    }

    public boolean canPickUpItemsInLobby() {
        return config.getBoolean("lobby-settings.can-pickup-items", false);
    }

    public boolean canDropItemsInLobby() {
        return config.getBoolean("lobby-settings.can-drop-items", false);
    }

    public boolean canPlaceBlocksInLobby() {
        return config.getBoolean("lobby-settings.can-place-blocks", false);
    }

    public boolean canBreakBlocksInLobby() {
        return config.getBoolean("lobby-settings.can-break-blocks", false);
    }

    public boolean canPickupExpOrbsInLobby() {
        return config.getBoolean("lobby-settings.can-pickup-exp", false);
    }

    public boolean sendActionFailedMessageInLobby() {
        return config.getBoolean("lobby-settings.send-action-failed-message", false);
    }

    public Material getCageMaterial() {
        String raw = config.getString("cage-material", Material.GLASS.name());
        try {
            return Material.valueOf(Objects.requireNonNull(raw).toUpperCase());
        } catch (IllegalArgumentException exception) {
            plugin.getLogger().warning("无效 cage-material 配置: " + raw + "，已回退为 GLASS。");
            return Material.GLASS;
        }
    }
}
