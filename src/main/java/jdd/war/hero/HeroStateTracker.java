package jdd.war.hero;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import jdd.war.War;
import jdd.war.game.Branding;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.AbstractHorse;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.scheduler.BukkitTask;

public final class HeroStateTracker {
    private final War plugin;
    private final Map<UUID, ProjectileSkill> projectileSkills = new ConcurrentHashMap<>();
    private final Map<UUID, List<UUID>> summonedEntities = new ConcurrentHashMap<>();
    private final Map<UUID, BukkitTask> flightTasks = new ConcurrentHashMap<>();
    private final Map<UUID, BukkitTask> prismTasks = new ConcurrentHashMap<>();
    private final Map<UUID, TimeAnchorState> timeAnchors = new ConcurrentHashMap<>();
    private final Map<String, BukkitTask> cooldownReadyTasks = new ConcurrentHashMap<>();

    public HeroStateTracker(War plugin) {
        this.plugin = plugin;
    }

    public void trackProjectile(Projectile projectile, ProjectileSkill skill) {
        projectileSkills.put(projectile.getUniqueId(), skill);
    }

    public ProjectileSkill consumeProjectileSkill(UUID projectileId) {
        return projectileSkills.remove(projectileId);
    }

    public void startFlight(Player player, long durationTicks) {
        clearFlight(player);
        BukkitTask task = Bukkit.getScheduler().runTaskLater(plugin, () -> clearFlight(player), durationTicks);
        flightTasks.put(player.getUniqueId(), task);
    }

    public void clearFlight(Player player) {
        BukkitTask task = flightTasks.remove(player.getUniqueId());
        if (task != null) {
            task.cancel();
        }
        if (player.getGameMode() != GameMode.CREATIVE && player.getGameMode() != GameMode.SPECTATOR) {
            player.setFlying(false);
            player.setAllowFlight(false);
        }
    }

    public void activatePrism(Player player, long durationTicks) {
        clearPrism(player);
        BukkitTask task = Bukkit.getScheduler().runTaskLater(plugin, () -> prismTasks.remove(player.getUniqueId()), durationTicks);
        prismTasks.put(player.getUniqueId(), task);
    }

    public boolean consumePrism(Player player) {
        BukkitTask task = prismTasks.remove(player.getUniqueId());
        if (task == null) {
            return false;
        }
        task.cancel();
        return true;
    }

    public boolean hasPrism(Player player) {
        return prismTasks.containsKey(player.getUniqueId());
    }

    public void clearPrism(Player player) {
        BukkitTask task = prismTasks.remove(player.getUniqueId());
        if (task != null) {
            task.cancel();
        }
    }

    public void setTimeAnchor(Player player, Location location, long durationTicks, Runnable onExpire) {
        clearTimeAnchor(player);
        BukkitTask task = Bukkit.getScheduler().runTaskLater(plugin, onExpire, durationTicks);
        timeAnchors.put(player.getUniqueId(), new TimeAnchorState(location.clone(), task));
    }

    public Location getTimeAnchor(Player player) {
        TimeAnchorState state = timeAnchors.get(player.getUniqueId());
        return state == null ? null : state.location().clone();
    }

    public boolean hasTimeAnchor(Player player) {
        return timeAnchors.containsKey(player.getUniqueId());
    }

    public void clearTimeAnchor(Player player) {
        TimeAnchorState state = timeAnchors.remove(player.getUniqueId());
        if (state != null) {
            state.task().cancel();
        }
    }

    public void registerSummon(Player player, LivingEntity entity) {
        entity.setMetadata(HeroSkillHandler.SUMMON_OWNER_METADATA, new FixedMetadataValue(plugin, player.getUniqueId().toString()));
        summonedEntities.computeIfAbsent(player.getUniqueId(), ignored -> new ArrayList<>()).add(entity.getUniqueId());
    }

    public void clearSummons(Player player) {
        Collection<UUID> entities = summonedEntities.remove(player.getUniqueId());
        if (entities == null) {
            return;
        }
        for (UUID entityId : entities) {
            Entity entity = Bukkit.getEntity(entityId);
            if (entity != null && !entity.isDead()) {
                if (entity instanceof AbstractHorse horse && horse.getPassengers().contains(player)) {
                    player.leaveVehicle();
                }
                entity.remove();
            }
        }
    }

    public void scheduleCooldownReady(Player player, String cooldownKey, String displayName, long durationTicks) {
        String taskKey = player.getUniqueId() + ":" + cooldownKey;
        BukkitTask previous = cooldownReadyTasks.remove(taskKey);
        if (previous != null) {
            previous.cancel();
        }

        BukkitTask task = Bukkit.getScheduler().runTaskLater(plugin, () -> {
            cooldownReadyTasks.remove(taskKey);
            if (player.isOnline()) {
                player.sendMessage(Branding.PREFIX + "技能已就绪：§b" + displayName);
            }
        }, durationTicks);
        cooldownReadyTasks.put(taskKey, task);
    }

    public void clearCooldownReadyTasks(Player player) {
        String prefix = player.getUniqueId() + ":";
        cooldownReadyTasks.entrySet().removeIf(entry -> {
            if (!entry.getKey().startsWith(prefix)) {
                return false;
            }
            entry.getValue().cancel();
            return true;
        });
    }

    public void clearPlayerState(Player player) {
        clearFlight(player);
        clearPrism(player);
        clearTimeAnchor(player);
        clearSummons(player);
        clearCooldownReadyTasks(player);
        projectileSkills.entrySet().removeIf(entry -> {
            Entity entity = Bukkit.getEntity(entry.getKey());
            return entity == null || (entity instanceof Projectile projectile && projectile.getShooter() == player);
        });
    }

    public enum ProjectileSkill {
        MAGE_SWAP,
        CLAW_WEB
    }

    private record TimeAnchorState(Location location, BukkitTask task) {
    }
}
