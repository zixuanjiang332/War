package jdd.war.hero;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
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
import org.bukkit.inventory.ItemStack;
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
    private final Map<UUID, VampireBloodlustState> vampireBloodlust = new ConcurrentHashMap<>();
    private final Map<UUID, ThugChargeState> thugCharges = new ConcurrentHashMap<>();
    private final Map<UUID, BukkitTask> poisonStingTasks = new ConcurrentHashMap<>();
    private final Map<UUID, RobotBorrowedSkillState> robotBorrowedSkills = new ConcurrentHashMap<>();
    private final Set<UUID> spatialPearlImmunity = ConcurrentHashMap.newKeySet();

    public HeroStateTracker(War plugin) {
        this.plugin = plugin;
    }

    public void trackProjectile(Projectile projectile, ProjectileSkill skill) {
        projectileSkills.put(projectile.getUniqueId(), skill);
    }

    public ProjectileSkill consumeProjectileSkill(UUID projectileId) {
        return projectileSkills.remove(projectileId);
    }

    public ProjectileSkill getProjectileSkill(UUID projectileId) {
        return projectileSkills.get(projectileId);
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

    public boolean hasPrism(Player player) {
        return prismTasks.containsKey(player.getUniqueId());
    }

    public boolean consumePrism(Player player) {
        BukkitTask task = prismTasks.remove(player.getUniqueId());
        if (task == null) {
            return false;
        }
        task.cancel();
        return true;
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

    public void clearCooldownReadyTask(Player player, String cooldownKey) {
        BukkitTask task = cooldownReadyTasks.remove(player.getUniqueId() + ":" + cooldownKey);
        if (task != null) {
            task.cancel();
        }
    }

    public void activateVampireBloodlust(Player player, int slot, ItemStack originalWeapon, long durationTicks, Runnable onExpire) {
        clearVampireBloodlust(player);
        BukkitTask task = Bukkit.getScheduler().runTaskLater(plugin, onExpire, durationTicks);
        vampireBloodlust.put(player.getUniqueId(), new VampireBloodlustState(slot, originalWeapon.clone(), task));
    }

    public boolean hasVampireBloodlust(Player player) {
        return vampireBloodlust.containsKey(player.getUniqueId());
    }

    public VampireBloodlustState getVampireBloodlust(Player player) {
        return vampireBloodlust.get(player.getUniqueId());
    }

    public void clearVampireBloodlust(Player player) {
        VampireBloodlustState state = vampireBloodlust.remove(player.getUniqueId());
        if (state != null) {
            state.task().cancel();
        }
    }

    public void setThugChargeState(Player player, int slot, ItemStack originalWeapon, int stacks, int empoweredHits) {
        thugCharges.put(player.getUniqueId(), new ThugChargeState(slot, originalWeapon.clone(), stacks, empoweredHits));
    }

    public ThugChargeState getThugChargeState(Player player) {
        return thugCharges.get(player.getUniqueId());
    }

    public void clearThugChargeState(Player player) {
        thugCharges.remove(player.getUniqueId());
    }

    public void activatePoisonSting(Player player, long durationTicks) {
        clearPoisonSting(player);
        BukkitTask task = Bukkit.getScheduler().runTaskLater(plugin, () -> poisonStingTasks.remove(player.getUniqueId()), durationTicks);
        poisonStingTasks.put(player.getUniqueId(), task);
    }

    public boolean hasPoisonSting(Player player) {
        return poisonStingTasks.containsKey(player.getUniqueId());
    }

    public void clearPoisonSting(Player player) {
        BukkitTask task = poisonStingTasks.remove(player.getUniqueId());
        if (task != null) {
            task.cancel();
        }
    }

    public void setRobotBorrowedSkill(Player player, ItemStack baseItem, HeroClass sourceHero, HeroSkillBinding binding, ItemStack borrowedItem) {
        robotBorrowedSkills.put(player.getUniqueId(),
                new RobotBorrowedSkillState(baseItem.clone(), sourceHero, binding, borrowedItem.clone()));
    }

    public RobotBorrowedSkillState getRobotBorrowedSkill(Player player) {
        return robotBorrowedSkills.get(player.getUniqueId());
    }

    public void clearRobotBorrowedSkill(Player player) {
        robotBorrowedSkills.remove(player.getUniqueId());
    }

    public void armSpatialPearlImmunity(Player player) {
        spatialPearlImmunity.add(player.getUniqueId());
    }

    public boolean consumeSpatialPearlImmunity(Player player) {
        return spatialPearlImmunity.remove(player.getUniqueId());
    }

    public void clearPlayerState(Player player) {
        clearFlight(player);
        clearPrism(player);
        clearTimeAnchor(player);
        clearSummons(player);
        clearCooldownReadyTasks(player);
        clearVampireBloodlust(player);
        clearThugChargeState(player);
        clearPoisonSting(player);
        clearRobotBorrowedSkill(player);
        spatialPearlImmunity.remove(player.getUniqueId());
        projectileSkills.entrySet().removeIf(entry -> {
            Entity entity = Bukkit.getEntity(entry.getKey());
            return entity == null || (entity instanceof Projectile projectile && projectile.getShooter() == player);
        });
    }

    public enum ProjectileSkill {
        MAGE_SWAP,
        CLAW_WEB,
        SPATIAL_PEARL,
        DRAGON_KNIGHT_FIREBALL,
        ARTILLERIST_SHELL
    }

    public record VampireBloodlustState(int slot, ItemStack originalWeapon, BukkitTask task) {
    }

    public record ThugChargeState(int slot, ItemStack originalWeapon, int stacks, int empoweredHits) {
    }

    public record RobotBorrowedSkillState(ItemStack baseItem, HeroClass sourceHero, HeroSkillBinding binding, ItemStack borrowedItem) {
    }

    private record TimeAnchorState(Location location, BukkitTask task) {
    }
}

