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
import jdd.war.game.GameService;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.FluidCollisionMode;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.damage.DamageType;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.AbstractHorse;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.EnderPearl;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Fireball;
import org.bukkit.entity.Horse;
import org.bukkit.entity.IronGolem;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Mob;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.Skeleton;
import org.bukkit.entity.Snowball;
import org.bukkit.entity.Wolf;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityRegainHealthEvent;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.entity.ExplosionPrimeEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.HorseInventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;

public final class HeroSkillHandler {
    private static final double DEFAULT_SKILL_RANGE = 5.0D;
    private static final double TRUE_DAMAGE_REBALANCE_THRESHOLD = 10.0D;
    private static final double TRUE_DAMAGE_REBALANCE_CAP = 9.0D;
    private static final double SUMMON_TRUE_DAMAGE_CAP = 5.0D;
    public static final String SUMMON_OWNER_METADATA = "class_war_summon_owner";

    private final War plugin;
    private final GameService gameService;
    private final HeroSkillConfig skillConfig;
    private final HeroRegistry heroRegistry;
    private final HeroStateTracker stateTracker;
    private final Map<UUID, List<UUID>> summonedEntities = new ConcurrentHashMap<>();
    private final Map<UUID, BukkitTask> flightTasks = new ConcurrentHashMap<>();
    private final Map<UUID, BukkitTask> phantomSpectatorTasks = new ConcurrentHashMap<>();
    private final Map<UUID, BukkitTask> homelanderLaserTasks = new ConcurrentHashMap<>();
    private final Map<UUID, BukkitTask> summonTargetTasks = new ConcurrentHashMap<>();
    private final Map<UUID, Long> summonAttackCooldowns = new ConcurrentHashMap<>();
    private final Set<UUID> achillesFallImmunity = ConcurrentHashMap.newKeySet();
    private final Set<UUID> phantomFallImmunity = ConcurrentHashMap.newKeySet();

    public HeroSkillHandler(War plugin, GameService gameService, HeroSkillConfig skillConfig, HeroRegistry heroRegistry) {
        this.plugin = plugin;
        this.gameService = gameService;
        this.skillConfig = skillConfig;
        this.heroRegistry = heroRegistry;
        this.stateTracker = new HeroStateTracker(plugin);
    }

    public void handleInteract(PlayerInteractEvent event) {
        if (event.getHand() != EquipmentSlot.HAND) {
            return;
        }

        Player player = event.getPlayer();
        HeroClass heroClass = gameService.getSelectedHero(player);
        if (heroClass == null) {
            return;
        }

        ItemStack item = event.getItem();
        if (item == null || item.getType() == Material.AIR) {
            return;
        }

        if (item.getType() == Material.MUSHROOM_STEW && event.getAction().isRightClick()) {
            useStew(player, item, event);
            return;
        }

        if (heroClass == HeroClass.THUG) {
            handleThug(event, player, item);
            if (event.isCancelled()) {
                return;
            }
        }

        switch (heroClass) {
            case CLAW -> handleClaw(event, player, item);
            case DESTROYER -> handleDestroyer(event, player, item);
            case SUMMONER -> handleSummoner(event, player, item);
            case INFERNO_GUARD -> handleInfernoGuard(event, player, item);
            case THOR -> handleThor(event, player, item);
            case VAMPIRE -> handleVampire(event, player, item);
            case PHANTOM -> handlePhantom(event, player, item);
            case SHACKLE -> handleShackle(event, player, item);
            case CAVALRY -> handleCavalry(event, player, item);
            case ACHILLES -> handleAchilles(event, player, item);
            case DRAGON_KNIGHT -> handleDragonKnight(event, player, item);
            case DRAGON_BREATH -> handleDragonBreath(event, player, item);
            case ASURA -> handleAsura(event, player, item);
            case TOXIC_LIZARD -> handleToxicLizard(event, player, item);
            case POISON_STINGER -> handlePoisonStinger(event, player, item);
            case ROBOT -> handleRobot(event, player, item);
            case SHADOW_BINDER -> handleShadowBinder(event, player, item);
            case BLOOD_KNIGHT -> handleBloodKnight(event, player, item);
            case ARTILLERIST -> handleArtillerist(event, player, item);
            case GRAVEKEEPER -> handleGravekeeper(event, player, item);
            case SACRED_WAR -> handleSacredWar(event, player, item);
            case WINDWALKER -> handleWindwalker(event, player, item);
            case HOMELANDER -> handleHomelander(event, player, item);
            case VOID_WALKER -> handleVoidWalker(event, player, item);
            case SPATIAL_MAGE -> handleSpatialMage(event, player, item);
            case ENGINEER -> handleEngineer(event, player, item);
            case TIDE -> handleTide(event, player, item);
            case PRISM -> handlePrism(event, player, item);
            case GEOMANCER -> handleGeomancer(event, player, item);
            case RIFT -> handleRift(event, player, item);
            case FROSTMARK -> handleFrostmark(event, player, item);
            case RAZOR -> handleRazor(event, player, item);
            default -> {
            }
        }
    }

    public void handleDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player player) || !gameService.isParticipant(player)) {
            return;
        }

        HeroClass heroClass = gameService.getSelectedHero(player);
        if (heroClass == null) {
            return;
        }

        DamageType damageType = event.getDamageSource().getDamageType();
        if (damageType == DamageType.ENDER_PEARL && stateTracker.consumeSpatialPearlImmunity(player)) {
            event.setCancelled(true);
            return;
        }

        if (heroClass == HeroClass.DRAGON_KNIGHT && isDragonKnightImmune(event, damageType)) {
            event.setCancelled(true);
            player.setFireTicks(0);
            return;
        }

        if (gameService.isInSafeZone(player)) {
            return;
        }

        if (heroClass == HeroClass.BERSERKER && !event.isCancelled()) {
            schedulePassiveRefresh(player);
        }

        if (event.getCause() != EntityDamageEvent.DamageCause.FALL) {
            return;
        }

        if (heroClass == HeroClass.HOMELANDER) {
            event.setCancelled(true);
            return;
        }

        if (achillesFallImmunity.remove(player.getUniqueId())) {
            event.setCancelled(true);
            return;
        }

        if (phantomFallImmunity.remove(player.getUniqueId())) {
            event.setCancelled(true);
            return;
        }

        if (heroClass == HeroClass.HEAD_REAPER) {
            handleLandingSlam(
                    player,
                    event,
                    skillConfig.value(HeroClass.HEAD_REAPER, "landing_slam", "radius", 3.5D),
                    Math.min(11.0D, Math.max(1.0D, player.getFallDistance() * 0.42D))
            );
        }
    }

    public void handleRegainHealth(EntityRegainHealthEvent event) {
        if (!(event.getEntity() instanceof Player player) || !gameService.isParticipant(player)) {
            return;
        }
        if (gameService.getSelectedHero(player) == HeroClass.BERSERKER) {
            schedulePassiveRefresh(player);
        }
    }

    public void handleBowShoot(EntityShootBowEvent event) {
        if (!(event.getEntity() instanceof Player player) || !gameService.isParticipant(player)) {
            return;
        }
        if (gameService.getSelectedHero(player) != HeroClass.BIRDMAN || !(event.getProjectile() instanceof Arrow arrow)) {
            return;
        }
        if (onCooldown(player, "birdman_dash", 10_000L)) {
            return;
        }

        Vector push = arrow.getVelocity().normalize().multiply(1.5D).setY(0.75D);
        player.setVelocity(push);
        player.setFallDistance(0.0F);
        player.playSound(player.getLocation(), Sound.ENTITY_ENDER_DRAGON_FLAP, 0.8F, 1.5F);
    }

    public void handleProjectileLaunch(ProjectileLaunchEvent event) {
        if (!(event.getEntity().getShooter() instanceof Player player)) {
            return;
        }
        if (!gameService.isParticipant(player)) {
            return;
        }

        if (event.getEntity() instanceof Snowball snowball && gameService.getSelectedHero(player) == HeroClass.MAGE) {
            stateTracker.trackProjectile(snowball, HeroStateTracker.ProjectileSkill.MAGE_SWAP);
            return;
        }

        if (event.getEntity() instanceof EnderPearl pearl && gameService.getSelectedHero(player) == HeroClass.SPATIAL_MAGE) {
            stateTracker.trackProjectile(pearl, HeroStateTracker.ProjectileSkill.SPATIAL_PEARL);
        }
    }

    public void handleProjectileHit(ProjectileHitEvent event) {
        HeroStateTracker.ProjectileSkill skill = stateTracker.consumeProjectileSkill(event.getEntity().getUniqueId());
        if (skill == null || !(event.getEntity().getShooter() instanceof Player player) || !player.isOnline()) {
            return;
        }

        if (skill == HeroStateTracker.ProjectileSkill.MAGE_SWAP && event.getHitEntity() instanceof Player target) {
            if (target.equals(player) || gameService.isInSafeZone(target)) {
                return;
            }
            Location source = player.getLocation().clone();
            Location destination = target.getLocation().clone();
            player.teleport(destination);
            target.teleport(source);
            player.playSound(player.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 1.0F, 1.0F);
            target.playSound(target.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 1.0F, 1.0F);
            return;
        }

        if (skill == HeroStateTracker.ProjectileSkill.CLAW_WEB) {
            Location center = event.getHitEntity() != null
                    ? event.getHitEntity().getLocation()
                    : event.getHitBlock() != null
                    ? event.getHitBlock().getLocation().add(0.5D, 0.5D, 0.5D)
                    : event.getEntity().getLocation();
            spawnTemporaryWebCross(center);
            return;
        }

        if (skill == HeroStateTracker.ProjectileSkill.SPATIAL_PEARL) {
            Location center = event.getHitEntity() != null
                    ? event.getHitEntity().getLocation()
                    : event.getHitBlock() != null
                    ? event.getHitBlock().getLocation().add(0.5D, 0.5D, 0.5D)
                    : event.getEntity().getLocation();
            stateTracker.armSpatialPearlImmunity(player);
            center.getWorld().spawnParticle(Particle.PORTAL, center, 36, 0.5D, 0.7D, 0.5D, 0.15D);
            center.getWorld().playSound(center, Sound.ENTITY_ENDERMAN_TELEPORT, 0.8F, 1.15F);
            damageNearbyPlayersAt(
                    player,
                    center,
                    skillConfig.value(HeroClass.SPATIAL_MAGE, "spatial_pearl", "aoe-radius", 3.0D),
                    skillConfig.value(HeroClass.SPATIAL_MAGE, "spatial_pearl", "aoe-damage", 4.0D)
            );
            return;
        }

        if (skill == HeroStateTracker.ProjectileSkill.ARTILLERIST_SHELL) {
            Location center = event.getHitEntity() != null
                    ? event.getHitEntity().getLocation()
                    : event.getHitBlock() != null
                    ? event.getHitBlock().getLocation().add(0.5D, 0.5D, 0.5D)
                    : event.getEntity().getLocation();
            event.getEntity().remove();
            triggerArtilleryShell(player, center);
            return;
        }

        if (skill == HeroStateTracker.ProjectileSkill.DRAGON_KNIGHT_FIREBALL) {
            Location center = event.getHitEntity() != null
                    ? event.getHitEntity().getLocation()
                    : event.getHitBlock() != null
                    ? event.getHitBlock().getLocation().add(0.5D, 0.5D, 0.5D)
                    : event.getEntity().getLocation();
            event.getEntity().remove();
            triggerDragonKnightFireball(player, center);
        }
    }

    public void handleExplosionPrime(ExplosionPrimeEvent event) {
        if (stateTracker.getProjectileSkill(event.getEntity().getUniqueId()) == HeroStateTracker.ProjectileSkill.DRAGON_KNIGHT_FIREBALL) {
            event.setCancelled(true);
            event.setRadius(0.0F);
        }
    }

    public void handleDamageByPlayer(EntityDamageByEntityEvent event) {
        if (event.getEntity() instanceof Player victim
                && gameService.isParticipant(victim)
                && isInterceptableProjectile(event.getDamager())
                && stateTracker.consumePrism(victim)) {
            event.setCancelled(true);
            if (event.getDamager() instanceof Projectile projectile) {
                if (projectile.getShooter() instanceof Player attacker && !gameService.isInSafeZone(attacker)) {
                    double damage = skillConfig.value(HeroClass.PRISM, "prism_stance", "retaliation-damage", 4.0D);
                    dealTrueSkillDamage(victim, attacker, damage);
                }
                projectile.remove();
            }
            victim.getWorld().spawnParticle(Particle.ENCHANT, victim.getLocation().add(0.0D, 1.0D, 0.0D), 18, 0.5D, 0.7D, 0.5D, 0.01D);
            victim.playSound(victim.getLocation(), Sound.BLOCK_AMETHYST_CLUSTER_BREAK, 1.0F, 1.2F);
            return;
        }

        if (handleSummonDamage(event)) {
            return;
        }

        if (event.getEntity() instanceof Player victim
                && gameService.isParticipant(victim)
                && victim.isBlocking()
                && (victim.getInventory().getItemInOffHand().getType() == Material.SHIELD
                || victim.getInventory().getItemInMainHand().getType() == Material.SHIELD)) {
            Player attacker = null;
            if (event.getDamager() instanceof Player player) {
                attacker = player;
            } else if (event.getDamager() instanceof Projectile projectile && projectile.getShooter() instanceof Player player) {
                attacker = player;
            }
            if (attacker != null
                    && !attacker.equals(victim)
                    && gameService.isParticipant(attacker)
                    && !gameService.isInSafeZone(attacker)
                    && !gameService.isInSafeZone(victim)) {
                victim.setCooldown(Material.SHIELD, 60);
                victim.getWorld().playSound(victim.getLocation(), Sound.ITEM_SHIELD_BREAK, 0.9F, 1.0F);
            }
        }

        if (!(event.getEntity() instanceof Player victim)
                || !(event.getDamager() instanceof Player attacker)
                || attacker.equals(victim)
                || !gameService.isParticipant(attacker)
                || !gameService.isParticipant(victim)
                || gameService.isInSafeZone(attacker)
                || gameService.isInSafeZone(victim)) {
            return;
        }

        HeroClass attackerClass = gameService.getSelectedHero(attacker);
        if (attackerClass == null) {
            return;
        }

        if (attackerClass == HeroClass.LUMBERJACK
                && isAxe(attacker.getInventory().getItemInMainHand())
                && Math.random() < skillConfig.value(HeroClass.LUMBERJACK, "lumberjack_passive", "proc-chance", 0.2D)) {
            victim.addPotionEffect(new PotionEffect(
                    PotionEffectType.SLOWNESS,
                    (int) Math.round(skillConfig.value(HeroClass.LUMBERJACK, "lumberjack_passive", "duration-seconds", 2.0D) * 20.0D),
                    skillConfig.intValue(HeroClass.LUMBERJACK, "lumberjack_passive", "amplifier", 1),
                    false,
                    true
            ));
        }

        if (attackerClass == HeroClass.VOID_WALKER
                && Math.random() < skillConfig.value(HeroClass.VOID_WALKER, "void_strike", "proc-chance", 0.2D)) {
            int durationTicks = (int) Math.round(skillConfig.value(HeroClass.VOID_WALKER, "void_strike", "duration-seconds", 2.0D) * 20.0D);
            victim.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, durationTicks, 0, false, true));
            victim.addPotionEffect(new PotionEffect(
                    PotionEffectType.WITHER,
                    durationTicks,
                    skillConfig.intValue(HeroClass.VOID_WALKER, "void_strike", "wither-amplifier", 0),
                    false,
                    true
            ));
        }

        if (stateTracker.hasVampireBloodlust(attacker)) {
            attacker.setHealth(Math.min(getMaxHealth(attacker),
                    attacker.getHealth() + (event.getFinalDamage() * skillConfig.value(HeroClass.VAMPIRE, "vampire_bloodlust", "lifesteal-ratio", 0.5D))));
        }

        if (attackerClass == HeroClass.POISON_STINGER && stateTracker.hasPoisonSting(attacker) && event.getFinalDamage() > 0.0D) {
            victim.addPotionEffect(new PotionEffect(
                    PotionEffectType.POISON,
                    (int) Math.round(skillConfig.value(HeroClass.POISON_STINGER, "poison_blade", "poison-seconds", 2.0D) * 20.0D),
                    skillConfig.intValue(HeroClass.POISON_STINGER, "poison_blade", "poison-amplifier", 2),
                    false,
                    true
            ));
            attacker.addPotionEffect(new PotionEffect(
                    PotionEffectType.REGENERATION,
                    (int) Math.round(skillConfig.value(HeroClass.POISON_STINGER, "poison_blade", "regen-seconds", 2.0D) * 20.0D),
                    skillConfig.intValue(HeroClass.POISON_STINGER, "poison_blade", "regen-amplifier", 1),
                    false,
                    true
            ));
        }

        if (attackerClass == HeroClass.THUG) {
            HeroStateTracker.ThugChargeState state = stateTracker.getThugChargeState(attacker);
            if (state != null && event.getFinalDamage() > 0.0D) {
                int remainingHits = state.empoweredHits() - 1;
                if (remainingHits <= 0) {
                    resetThugCharge(attacker, true);
                } else {
                    stateTracker.setThugChargeState(attacker, state.slot(), state.originalWeapon(), state.stacks(), remainingHits);
                }
            }
        }
    }

    public void handleFish(PlayerFishEvent event) {
        Player player = event.getPlayer();
        if (!gameService.isParticipant(player) || gameService.getSelectedHero(player) != HeroClass.FISHERMAN) {
            return;
        }
        if (gameService.isInSafeZone(player)) {
            event.setCancelled(true);
            return;
        }
        if (event.getCaught() instanceof Player target && isValidCombatTarget(player, target)) {
            Vector pull = player.getLocation().toVector().subtract(target.getLocation().toVector()).normalize().multiply(1.4D);
            pull.setY(0.35D);
            target.setVelocity(pull);
            target.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 40, 1, false, true));
            target.playSound(target.getLocation(), Sound.ENTITY_FISHING_BOBBER_RETRIEVE, 1.0F, 1.2F);
        }
    }

    public void handleSneak(PlayerToggleSneakEvent event) {
        Player player = event.getPlayer();
        if (!event.isSneaking() || player.getVehicle() == null) {
            return;
        }
        List<UUID> owned = summonedEntities.get(player.getUniqueId());
        if (owned != null && owned.contains(player.getVehicle().getUniqueId())) {
            player.leaveVehicle();
        }
    }

    public void refreshPassiveEffects(Player player) {
        if (!player.isOnline() || !gameService.isParticipant(player)) {
            return;
        }
        HeroClass heroClass = gameService.getSelectedHero(player);
        if (heroClass == null) {
            return;
        }
        switch (heroClass) {
            case BERSERKER -> updateBerserkerState(player);
            case ROBOT -> player.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE, PotionEffect.INFINITE_DURATION, 0, false, false));
            case DRAGON_KNIGHT -> player.setFireTicks(0);
            default -> {
            }
        }
    }

    public void clearPlayerState(Player player) {
        restoreVampireWeapon(player, false);
        resetThugCharge(player, false);
        restoreRobotBorrowedSkill(player, false);
        cancelHomelanderLaser(player);
        stateTracker.clearPlayerState(player);
        achillesFallImmunity.remove(player.getUniqueId());
        phantomFallImmunity.remove(player.getUniqueId());
        clearPhantomSpectator(player, false);
        clearFlight(player);
        clearSummons(player);
    }

    private void handleClaw(PlayerInteractEvent event, Player player, ItemStack item) {
        if (item.getType() != Material.COBWEB) {
            return;
        }
        if (denySafeZoneOffense(player, event)) {
            return;
        }
        event.setCancelled(true);
        if (onCooldown(player, "claw_web", skillConfig.cooldownMillis(HeroClass.CLAW, "claw_web", 12.0D))) {
            return;
        }
        Snowball snowball = player.launchProjectile(Snowball.class);
        snowball.setVelocity(player.getEyeLocation().getDirection().multiply(1.7D));
        stateTracker.trackProjectile(snowball, HeroStateTracker.ProjectileSkill.CLAW_WEB);
        player.playSound(player.getLocation(), Sound.ENTITY_SPIDER_AMBIENT, 0.8F, 1.2F);
    }

    private void handleDestroyer(PlayerInteractEvent event, Player player, ItemStack item) {
        if (!event.getAction().isRightClick() || item.getType() != Material.CREEPER_HEAD) {
            return;
        }
        if (denySafeZoneOffense(player, event)) {
            return;
        }
        event.setCancelled(true);
        if (onCooldown(player, "destroyer_blast", skillConfig.cooldownMillis(HeroClass.DESTROYER, "destroyer_blast", 16.0D))) {
            return;
        }
        double radius = skillConfig.value(HeroClass.DESTROYER, "destroyer_blast", "radius", 4.5D);
        double damage = skillConfig.value(HeroClass.DESTROYER, "destroyer_blast", "damage", 6.0D);
        double knockback = skillConfig.value(HeroClass.DESTROYER, "destroyer_blast", "knockback-strength", 2.2D);
        double vertical = skillConfig.value(HeroClass.DESTROYER, "destroyer_blast", "vertical-knockback", 0.65D);
        for (Entity entity : player.getNearbyEntities(radius, radius, radius)) {
            if (entity instanceof Player target && dealTrueSkillDamage(player, target, damage) > 0.0D) {
                Vector push = target.getLocation().toVector().subtract(player.getLocation().toVector()).normalize().multiply(knockback);
                push.setY(Math.max(push.getY(), vertical));
                target.setVelocity(push);
            }
        }
        player.getWorld().spawnParticle(Particle.EXPLOSION, player.getLocation(), 2, 0.4D, 0.2D, 0.4D, 0.0D);
        player.playSound(player.getLocation(), Sound.ENTITY_GENERIC_EXPLODE, 0.9F, 1.0F);
    }

    private void handleSummoner(PlayerInteractEvent event, Player player, ItemStack item) {
        if (!event.getAction().isRightClick() || item.getType() != Material.IRON_BLOCK) {
            return;
        }
        if (denySafeZoneOffense(player, event)) {
            return;
        }
        event.setCancelled(true);
        if (onCooldown(player, "summoner_golem", skillConfig.cooldownMillis(HeroClass.SUMMONER, "summoner_golem", 24.0D))) {
            return;
        }
        clearSummons(player);
        IronGolem golem = player.getWorld().spawn(player.getLocation(), IronGolem.class);
        golem.setPlayerCreated(false);
        AttributeInstance attackDamage = golem.getAttribute(Attribute.ATTACK_DAMAGE);
        if (attackDamage != null) {
            attackDamage.setBaseValue(SUMMON_TRUE_DAMAGE_CAP);
        }
        golem.addPassenger(player);
        registerSummon(player, golem);
        startSummonTargeting(player, golem, 18.0D);
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            if (golem.isValid() && !golem.isDead()) {
                Entity vehicle = player.getVehicle();
                if (vehicle != null && vehicle.getUniqueId().equals(golem.getUniqueId())) {
                    player.leaveVehicle();
                }
                golem.remove();
            }
        }, 15L * 20L);
        player.playSound(player.getLocation(), Sound.ENTITY_IRON_GOLEM_REPAIR, 1.0F, 1.0F);
    }

    private void handleInfernoGuard(PlayerInteractEvent event, Player player, ItemStack item) {
        if (!event.getAction().isRightClick() || item.getType() != Material.BONE) {
            return;
        }
        if (denySafeZoneOffense(player, event)) {
            return;
        }
        event.setCancelled(true);
        if (onCooldown(player, "inferno_hounds", skillConfig.cooldownMillis(HeroClass.INFERNO_GUARD, "inferno_hounds", 28.0D))) {
            return;
        }
        clearSummons(player);
        int count = skillConfig.intValue(HeroClass.INFERNO_GUARD, "inferno_hounds", "count", 3);
        double wolfHealth = skillConfig.value(HeroClass.INFERNO_GUARD, "inferno_hounds", "wolf-health", 16.0D);
        double wolfDamage = Math.min(
                SUMMON_TRUE_DAMAGE_CAP,
                skillConfig.value(HeroClass.INFERNO_GUARD, "inferno_hounds", "wolf-damage", 3.0D)
        );
        double targetRange = skillConfig.value(HeroClass.INFERNO_GUARD, "inferno_hounds", "target-range", 16.0D);
        for (int i = 0; i < count; i++) {
            Wolf wolf = player.getWorld().spawn(player.getLocation(), Wolf.class);
            wolf.setOwner(player);
            wolf.setCustomName("地狱猎犬");
            wolf.addPotionEffect(new PotionEffect(PotionEffectType.FIRE_RESISTANCE, 20 * 20, 0));
            wolf.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 20 * 20, 0));
            AttributeInstance maxHealth = wolf.getAttribute(Attribute.MAX_HEALTH);
            if (maxHealth != null) {
                maxHealth.setBaseValue(wolfHealth);
                wolf.setHealth(wolfHealth);
            }
            AttributeInstance attackDamage = wolf.getAttribute(Attribute.ATTACK_DAMAGE);
            if (attackDamage != null) {
                attackDamage.setBaseValue(wolfDamage);
            }
            registerSummon(player, wolf);
            startSummonTargeting(player, wolf, targetRange);
        }
        player.playSound(player.getLocation(), Sound.ENTITY_WOLF_AMBIENT, 1.0F, 0.7F);
    }

    private void handleThor(PlayerInteractEvent event, Player player, ItemStack item) {
        if (!event.getAction().isRightClick() || item.getType() != Material.WOODEN_AXE) {
            return;
        }
        if (denySafeZoneOffense(player, event)) {
            return;
        }
        event.setCancelled(true);
        if (onCooldown(player, "thor_lightning", skillConfig.cooldownMillis(HeroClass.THOR, "thor_lightning", 15.0D))) {
            return;
        }

        double radius = skillConfig.value(HeroClass.THOR, "thor_lightning", "radius", 3.5D);
        double damage = skillConfig.value(HeroClass.THOR, "thor_lightning", "damage", 7.0D);
        for (Entity entity : player.getNearbyEntities(radius, radius, radius)) {
            if (entity instanceof Player target && dealTrueSkillDamage(player, target, damage) > 0.0D) {
                target.getWorld().strikeLightningEffect(target.getLocation());
            }
        }
        player.playSound(player.getLocation(), Sound.ENTITY_LIGHTNING_BOLT_THUNDER, 0.9F, 1.0F);
    }

    private void handleVampire(PlayerInteractEvent event, Player player, ItemStack item) {
        if (!event.getAction().isRightClick() || item.getType() != Material.REDSTONE) {
            return;
        }
        if (denySafeZoneOffense(player, event)) {
            return;
        }
        event.setCancelled(true);
        if (onCooldown(player, "vampire_bloodlust", skillConfig.cooldownMillis(HeroClass.VAMPIRE, "vampire_bloodlust", 35.0D))) {
            return;
        }

        int weaponSlot = findPrimaryWeaponSlot(player);
        if (weaponSlot < 0) {
            return;
        }

        ItemStack originalWeapon = player.getInventory().getItem(weaponSlot);
        if (originalWeapon == null || originalWeapon.getType() == Material.AIR) {
            return;
        }

        restoreVampireWeapon(player, false);

        ItemStack bloodlustWeapon = originalWeapon.clone();
        ItemMeta meta = bloodlustWeapon.getItemMeta();
        meta.displayName(Component.text("嗜血"));
        bloodlustWeapon.setItemMeta(meta);
        player.getInventory().setItem(weaponSlot, bloodlustWeapon);

        long durationTicks = Math.round(skillConfig.value(HeroClass.VAMPIRE, "vampire_bloodlust", "duration-seconds", 10.0D) * 20.0D);
        stateTracker.activateVampireBloodlust(player, weaponSlot, originalWeapon, durationTicks, () -> restoreVampireWeapon(player, true));
        player.getWorld().spawnParticle(Particle.DAMAGE_INDICATOR, player.getLocation().add(0.0D, 1.0D, 0.0D), 14, 0.35D, 0.45D, 0.35D, 0.02D);
        player.playSound(player.getLocation(), Sound.ENTITY_WARDEN_HEARTBEAT, 1.0F, 1.35F);
    }

    private void handleThug(PlayerInteractEvent event, Player player, ItemStack item) {
        if (!event.getAction().isRightClick() || !isWeapon(item)) {
            return;
        }
        if (denySafeZoneOffense(player, event)) {
            return;
        }
        event.setCancelled(true);

        int maxStacks = skillConfig.intValue(HeroClass.THUG, "thug_charge", "max-stacks", 5);
        HeroStateTracker.ThugChargeState currentState = stateTracker.getThugChargeState(player);
        if (currentState != null && currentState.stacks() >= maxStacks) {
            player.sendMessage(Branding.PREFIX + "蓄力已经达到上限。");
            return;
        }

        if (onCooldown(player, "thug_charge", skillConfig.cooldownMillis(HeroClass.THUG, "thug_charge", 5.0D))) {
            return;
        }

        int heldSlot = player.getInventory().getHeldItemSlot();
        ItemStack originalWeapon = currentState != null ? currentState.originalWeapon() : item.clone();
        if (currentState != null && currentState.slot() != heldSlot) {
            resetThugCharge(player, false);
            originalWeapon = item.clone();
        }

        int stacks = Math.min(maxStacks, currentState == null ? 1 : currentState.stacks() + 1);
        int empoweredHits = skillConfig.intValue(HeroClass.THUG, "thug_charge", "empowered-hits", 2);
        player.getInventory().setItem(heldSlot, buildThugWeapon(originalWeapon, stacks));
        stateTracker.setThugChargeState(player, heldSlot, originalWeapon, stacks, empoweredHits);
        player.playSound(player.getLocation(), Sound.ITEM_FIRECHARGE_USE, 0.9F, 1.15F + (stacks * 0.05F));
    }

    private void handlePhantom(PlayerInteractEvent event, Player player, ItemStack item) {
        if (!event.getAction().isRightClick() || item.getType() != Material.BOOK) {
            return;
        }
        if (denySafeZoneOffense(player, event)) {
            return;
        }
        event.setCancelled(true);
        if (onCooldown(player, "phantom_flight", skillConfig.cooldownMillis(HeroClass.PHANTOM, "phantom_flight", 20.0D))) {
            return;
        }
        player.playSound(player.getLocation(), Sound.ENTITY_ENDERMAN_AMBIENT, 0.8F, 1.2F);
        startPhantomSpectator(player, Math.round(skillConfig.value(HeroClass.PHANTOM, "phantom_flight", "duration-seconds", 4.0D) * 20.0D));
    }

    private void handleShackle(PlayerInteractEvent event, Player player, ItemStack item) {
        if (!event.getAction().isRightClick() || item.getType() != Material.BELL) {
            return;
        }
        if (denySafeZoneOffense(player, event)) {
            return;
        }
        event.setCancelled(true);
        if (onCooldown(player, "shackle_bell", skillConfig.cooldownMillis(HeroClass.SHACKLE, "shackle_bell", 18.0D))) {
            return;
        }
        double radius = skillConfig.value(HeroClass.SHACKLE, "shackle_bell", "radius", 6.0D);
        int durationTicks = (int) Math.round(skillConfig.value(HeroClass.SHACKLE, "shackle_bell", "duration-seconds", 4.0D) * 20.0D);
        int amplifier = skillConfig.intValue(HeroClass.SHACKLE, "shackle_bell", "amplifier", 4);
        int witherTicks = (int) Math.round(skillConfig.value(HeroClass.SHACKLE, "shackle_bell", "wither-seconds", 2.0D) * 20.0D);
        int witherAmplifier = skillConfig.intValue(HeroClass.SHACKLE, "shackle_bell", "wither-amplifier", 0);
        for (Entity entity : player.getNearbyEntities(radius, radius, radius)) {
            if (entity instanceof Player target && !target.equals(player) && !gameService.isInSafeZone(target)) {
                target.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, durationTicks, amplifier, false, true));
                target.addPotionEffect(new PotionEffect(PotionEffectType.WITHER, witherTicks, witherAmplifier, false, true));
            }
        }
        player.playSound(player.getLocation(), Sound.BLOCK_BELL_USE, 1.0F, 0.8F);
    }

    private void handleShadowBinder(PlayerInteractEvent event, Player player, ItemStack item) {
        if (!event.getAction().isRightClick() || item.getType() != Material.LEAD) {
            return;
        }
        if (denySafeZoneOffense(player, event)) {
            return;
        }
        event.setCancelled(true);
        Player target = findTargetInSight(player, skillConfig.value(HeroClass.SHADOW_BINDER, "shadow_bind", "range", 12.0D), 0.6D);
        if (target == null) {
            return;
        }
        if (onCooldown(player, "shadow_bind", skillConfig.cooldownMillis(HeroClass.SHADOW_BINDER, "shadow_bind", 18.0D))) {
            return;
        }
        int durationTicks = (int) Math.round(skillConfig.value(HeroClass.SHADOW_BINDER, "shadow_bind", "duration-seconds", 3.0D) * 20.0D);
        target.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, durationTicks, skillConfig.intValue(HeroClass.SHADOW_BINDER, "shadow_bind", "slow-amplifier", 5), false, true));
        target.addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS, durationTicks, skillConfig.intValue(HeroClass.SHADOW_BINDER, "shadow_bind", "weakness-amplifier", 0), false, true));
        target.addPotionEffect(new PotionEffect(PotionEffectType.WITHER, durationTicks, skillConfig.intValue(HeroClass.SHADOW_BINDER, "shadow_bind", "wither-amplifier", 0), false, true));
        player.getWorld().spawnParticle(Particle.SMOKE, target.getLocation().add(0.0D, 1.0D, 0.0D), 24, 0.4D, 0.6D, 0.4D, 0.02D);
        player.playSound(player.getLocation(), Sound.ENTITY_ENDERMAN_SCREAM, 0.6F, 1.3F);
    }

    private void handleBloodKnight(PlayerInteractEvent event, Player player, ItemStack item) {
        if (!event.getAction().isRightClick() || item.getType() != Material.NETHER_WART) {
            return;
        }
        if (denySafeZoneOffense(player, event)) {
            return;
        }
        event.setCancelled(true);
        if (onCooldown(player, "blood_rite", skillConfig.cooldownMillis(HeroClass.BLOOD_KNIGHT, "blood_rite", 24.0D))) {
            return;
        }
        double healthCost = skillConfig.value(HeroClass.BLOOD_KNIGHT, "blood_rite", "health-cost", 6.0D);
        if (player.getHealth() <= healthCost + 1.0D) {
            player.sendMessage(Branding.PREFIX + "当前生命不足，无法发动血祭。");
            gameService.clearCooldown(player, "blood_rite");
            stateTracker.clearCooldownReadyTask(player, "blood_rite");
            return;
        }
        player.setHealth(Math.max(1.0D, player.getHealth() - healthCost));
        int durationTicks = (int) Math.round(skillConfig.value(HeroClass.BLOOD_KNIGHT, "blood_rite", "duration-seconds", 8.0D) * 20.0D);
        player.addPotionEffect(new PotionEffect(PotionEffectType.STRENGTH, durationTicks, skillConfig.intValue(HeroClass.BLOOD_KNIGHT, "blood_rite", "strength-amplifier", 0), false, true));
        player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, durationTicks, skillConfig.intValue(HeroClass.BLOOD_KNIGHT, "blood_rite", "speed-amplifier", 0), false, true));
        player.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE, durationTicks, skillConfig.intValue(HeroClass.BLOOD_KNIGHT, "blood_rite", "resistance-amplifier", 0), false, true));
        player.getWorld().spawnParticle(Particle.DAMAGE_INDICATOR, player.getLocation().add(0.0D, 1.0D, 0.0D), 16, 0.35D, 0.45D, 0.35D, 0.02D);
        player.playSound(player.getLocation(), Sound.ENTITY_WARDEN_HEARTBEAT, 1.0F, 1.15F);
    }

    private void handleArtillerist(PlayerInteractEvent event, Player player, ItemStack item) {
        if (!event.getAction().isRightClick() || item.getType() != Material.FIREWORK_STAR) {
            return;
        }
        if (denySafeZoneOffense(player, event)) {
            return;
        }
        event.setCancelled(true);
        if (onCooldown(player, "artillery_shell", skillConfig.cooldownMillis(HeroClass.ARTILLERIST, "artillery_shell", 16.0D))) {
            return;
        }
        Snowball shell = player.launchProjectile(Snowball.class);
        shell.setVelocity(player.getEyeLocation().getDirection().multiply(skillConfig.value(HeroClass.ARTILLERIST, "artillery_shell", "velocity", 1.1D)));
        stateTracker.trackProjectile(shell, HeroStateTracker.ProjectileSkill.ARTILLERIST_SHELL);
        player.playSound(player.getLocation(), Sound.ENTITY_FIREWORK_ROCKET_LAUNCH, 0.8F, 0.8F);
    }

    private void handleGravekeeper(PlayerInteractEvent event, Player player, ItemStack item) {
        if (!event.getAction().isRightClick() || item.getType() != Material.SKELETON_SKULL) {
            return;
        }
        if (denySafeZoneOffense(player, event)) {
            return;
        }
        event.setCancelled(true);
        if (onCooldown(player, "grave_legion", skillConfig.cooldownMillis(HeroClass.GRAVEKEEPER, "grave_legion", 24.0D))) {
            return;
        }
        clearSummons(player);
        int count = skillConfig.intValue(HeroClass.GRAVEKEEPER, "grave_legion", "count", 2);
        double summonHealth = skillConfig.value(HeroClass.GRAVEKEEPER, "grave_legion", "summon-health", 14.0D);
        double targetRange = skillConfig.value(HeroClass.GRAVEKEEPER, "grave_legion", "target-range", 18.0D);
        for (int i = 0; i < count; i++) {
            Skeleton skeleton = player.getWorld().spawn(player.getLocation(), Skeleton.class);
            skeleton.setShouldBurnInDay(false);
            AttributeInstance maxHealth = skeleton.getAttribute(Attribute.MAX_HEALTH);
            if (maxHealth != null) {
                maxHealth.setBaseValue(summonHealth);
                skeleton.setHealth(summonHealth);
            }
            if (skeleton.getEquipment() != null) {
                skeleton.getEquipment().setHelmet(HeroItems.unbreakable(Material.IRON_HELMET));
                skeleton.getEquipment().setHelmetDropChance(0.0F);
                skeleton.getEquipment().setItemInMainHand(HeroItems.unbreakable(Material.BOW));
            }
            registerSummon(player, skeleton);
            startSummonTargeting(player, skeleton, targetRange);
        }
        player.playSound(player.getLocation(), Sound.ENTITY_SKELETON_AMBIENT, 1.0F, 0.9F);
    }

    private void handleEngineer(PlayerInteractEvent event, Player player, ItemStack item) {
        if (!event.getAction().isRightClick() || item.getType() != Material.DISPENSER) {
            return;
        }
        if (denySafeZoneOffense(player, event)) {
            return;
        }
        event.setCancelled(true);

        Location placement = findEngineerTurretPlacement(player, 8.0D);
        if (placement == null) {
            player.sendMessage(Branding.PREFIX + "没有可部署的地面位置。");
            return;
        }
        if (onCooldown(player, "engineer_turret", skillConfig.cooldownMillis(HeroClass.ENGINEER, "engineer_turret", 20.0D))) {
            return;
        }

        clearSummons(player);
        Skeleton turret = player.getWorld().spawn(placement, Skeleton.class);
        turret.setCanPickupItems(false);
        turret.setRemoveWhenFarAway(false);

        double summonHealth = skillConfig.value(HeroClass.ENGINEER, "engineer_turret", "summon-health", 16.0D);
        AttributeInstance maxHealth = turret.getAttribute(Attribute.MAX_HEALTH);
        if (maxHealth != null) {
            maxHealth.setBaseValue(summonHealth);
            turret.setHealth(summonHealth);
        }

        double targetRange = skillConfig.value(HeroClass.ENGINEER, "engineer_turret", "target-range", 18.0D);
        AttributeInstance followRange = turret.getAttribute(Attribute.FOLLOW_RANGE);
        if (followRange != null) {
            followRange.setBaseValue(targetRange);
        }
        AttributeInstance movementSpeed = turret.getAttribute(Attribute.MOVEMENT_SPEED);
        if (movementSpeed != null) {
            movementSpeed.setBaseValue(0.0D);
        }

        if (turret.getEquipment() != null) {
            ItemStack bow = HeroItems.unbreakable(Material.BOW);
            int bowPower = Math.max(0, skillConfig.intValue(HeroClass.ENGINEER, "engineer_turret", "bow-power", 1));
            if (bowPower > 0) {
                HeroItems.addEnchant(bow, Enchantment.POWER, bowPower);
            }
            turret.getEquipment().setItemInMainHand(bow);
            turret.getEquipment().setItemInMainHandDropChance(0.0F);
            turret.getEquipment().setHelmet(HeroItems.unbreakable(Material.IRON_HELMET));
            turret.getEquipment().setHelmetDropChance(0.0F);
        }

        registerSummon(player, turret);
        startSummonTargeting(player, turret, targetRange);

        long durationTicks = Math.max(1L, Math.round(skillConfig.value(HeroClass.ENGINEER, "engineer_turret", "duration-seconds", 18.0D) * 20.0D));
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            if (turret.isValid() && !turret.isDead()) {
                turret.remove();
            }
        }, durationTicks);

        player.getWorld().spawnParticle(Particle.CRIT, placement.clone().add(0.0D, 1.0D, 0.0D), 12, 0.3D, 0.5D, 0.3D, 0.02D);
        player.playSound(player.getLocation(), Sound.BLOCK_DISPENSER_DISPENSE, 1.0F, 1.0F);
    }

    private void handleCavalry(PlayerInteractEvent event, Player player, ItemStack item) {
        if (!event.getAction().isRightClick() || item.getType() != Material.DIAMOND_HORSE_ARMOR) {
            return;
        }
        if (denySafeZoneOffense(player, event)) {
            return;
        }
        event.setCancelled(true);
        if (onCooldown(player, "cavalry_horse", skillConfig.cooldownMillis(HeroClass.CAVALRY, "cavalry_horse", 20.0D))) {
            return;
        }
        clearSummons(player);
        Horse horse = player.getWorld().spawn(player.getLocation(), Horse.class);
        horse.setTamed(true);
        horse.setOwner(player);
        AttributeInstance maxHealth = horse.getAttribute(Attribute.MAX_HEALTH);
        if (maxHealth != null) {
            double horseHealth = skillConfig.value(HeroClass.CAVALRY, "cavalry_horse", "horse-health", 30.0D);
            maxHealth.setBaseValue(horseHealth);
            horse.setHealth(horseHealth);
        }
        AttributeInstance movementSpeed = horse.getAttribute(Attribute.MOVEMENT_SPEED);
        if (movementSpeed != null) {
            movementSpeed.setBaseValue(skillConfig.value(HeroClass.CAVALRY, "cavalry_horse", "horse-speed", 0.34D));
        }
        horse.setJumpStrength(skillConfig.value(HeroClass.CAVALRY, "cavalry_horse", "horse-jump-strength", 0.85D));
        HorseInventory inventory = horse.getInventory();
        inventory.setSaddle(new ItemStack(Material.SADDLE));
        inventory.setArmor(new ItemStack(Material.DIAMOND_HORSE_ARMOR));
        horse.addPassenger(player);
        registerSummon(player, horse);
        player.playSound(player.getLocation(), Sound.ENTITY_HORSE_SADDLE, 1.0F, 1.0F);
    }

    private void handleAchilles(PlayerInteractEvent event, Player player, ItemStack item) {
        if (!event.getAction().isRightClick() || item.getType() != Material.FIREWORK_ROCKET) {
            return;
        }
        if (denySafeZoneOffense(player, event)) {
            return;
        }
        event.setCancelled(true);
        if (onCooldown(player, "achilles_leap", skillConfig.cooldownMillis(HeroClass.ACHILLES, "achilles_leap", 12.0D))) {
            return;
        }
        Vector launch = player.getLocation().getDirection().normalize()
                .multiply(skillConfig.value(HeroClass.ACHILLES, "achilles_leap", "horizontal-strength", 1.35D))
                .setY(skillConfig.value(HeroClass.ACHILLES, "achilles_leap", "vertical-strength", 0.9D));
        player.setVelocity(launch);
        achillesFallImmunity.add(player.getUniqueId());
        player.setFallDistance(0.0F);
        player.playSound(player.getLocation(), Sound.ENTITY_FIREWORK_ROCKET_LAUNCH, 1.0F, 1.1F);
    }

    private void handleDragonKnight(PlayerInteractEvent event, Player player, ItemStack item) {
        if (!event.getAction().isRightClick() || item.getType() != Material.BLAZE_ROD) {
            return;
        }
        if (denySafeZoneOffense(player, event)) {
            return;
        }
        event.setCancelled(true);
        if (onCooldown(player, "dragon_knight_fireball", skillConfig.cooldownMillis(HeroClass.DRAGON_KNIGHT, "dragon_knight_fireball", 15.0D))) {
            return;
        }

        Fireball fireball = player.launchProjectile(Fireball.class);
        fireball.setVelocity(player.getEyeLocation().getDirection().multiply(1.1D));
        fireball.setYield(0.0F);
        fireball.setIsIncendiary(false);
        stateTracker.trackProjectile(fireball, HeroStateTracker.ProjectileSkill.DRAGON_KNIGHT_FIREBALL);
        player.playSound(player.getLocation(), Sound.ENTITY_GHAST_SHOOT, 1.0F, 0.95F);
    }

    private void handleDragonBreath(PlayerInteractEvent event, Player player, ItemStack item) {
        if (!event.getAction().isRightClick() || item.getType() != Material.FIRE_CHARGE) {
            return;
        }
        if (denySafeZoneOffense(player, event)) {
            return;
        }
        event.setCancelled(true);
        if (onCooldown(player, "dragon_breath", skillConfig.cooldownMillis(HeroClass.DRAGON_BREATH, "dragon_breath", 20.0D))) {
            return;
        }
        double radius = skillConfig.value(HeroClass.DRAGON_BREATH, "dragon_breath", "radius", 3.5D);
        double damage = skillConfig.value(HeroClass.DRAGON_BREATH, "dragon_breath", "damage", 7.0D);
        int fireTicks = (int) Math.round(skillConfig.value(HeroClass.DRAGON_BREATH, "dragon_breath", "fire-seconds", 5.0D) * 20.0D);
        performDragonBreathBurst(player, radius, damage, fireTicks);
        long secondDelayTicks = Math.max(1L, Math.round(skillConfig.value(HeroClass.DRAGON_BREATH, "dragon_breath", "second-delay-seconds", 1.0D) * 20.0D));
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            if (player.isOnline() && gameService.isParticipant(player)) {
                performDragonBreathBurst(player, radius, damage, fireTicks);
            }
        }, secondDelayTicks);
    }

    private void handleAsura(PlayerInteractEvent event, Player player, ItemStack item) {
        if (!event.getAction().isRightClick() || item.getType() != Material.SLIME_BALL) {
            return;
        }
        if (denySafeZoneOffense(player, event)) {
            return;
        }
        event.setCancelled(true);
        Player target = findRandomTarget(player, skillConfig.value(HeroClass.ASURA, "asura_step", "range", 12.0D));
        if (target == null) {
            return;
        }
        if (onCooldown(player, "asura_step", skillConfig.cooldownMillis(HeroClass.ASURA, "asura_step", 16.0D))) {
            return;
        }
        player.teleport(target.getLocation().clone().add(1.0D, 0.0D, 0.0D));
        player.playSound(player.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 1.0F, 1.0F);
    }

    private void handleToxicLizard(PlayerInteractEvent event, Player player, ItemStack item) {
        if (!event.getAction().isRightClick() || item.getType() != Material.ENDER_EYE) {
            return;
        }
        if (denySafeZoneOffense(player, event)) {
            return;
        }
        event.setCancelled(true);
        if (onCooldown(player, "toxic_wave", skillConfig.cooldownMillis(HeroClass.TOXIC_LIZARD, "toxic_wave", 16.0D))) {
            return;
        }
        int durationTicks = (int) Math.round(skillConfig.value(HeroClass.TOXIC_LIZARD, "toxic_wave", "duration-seconds", 7.0D) * 20.0D);
        int amplifier = skillConfig.intValue(HeroClass.TOXIC_LIZARD, "toxic_wave", "amplifier", 0);
        for (Entity entity : player.getNearbyEntities(DEFAULT_SKILL_RANGE, DEFAULT_SKILL_RANGE, DEFAULT_SKILL_RANGE)) {
            if (entity instanceof Player target && !target.equals(player) && !gameService.isInSafeZone(target)) {
                target.addPotionEffect(new PotionEffect(PotionEffectType.POISON, durationTicks, amplifier, false, true));
            }
        }
        player.playSound(player.getLocation(), Sound.ENTITY_SILVERFISH_HURT, 1.0F, 0.8F);
    }

    private void handlePoisonStinger(PlayerInteractEvent event, Player player, ItemStack item) {
        if (!event.getAction().isRightClick() || !isPoisonSword(item)) {
            return;
        }
        if (denySafeZoneOffense(player, event)) {
            return;
        }
        event.setCancelled(true);
        if (onCooldown(player, "poison_blade", skillConfig.cooldownMillis(HeroClass.POISON_STINGER, "poison_blade", 30.0D))) {
            return;
        }
        long durationTicks = Math.round(skillConfig.value(HeroClass.POISON_STINGER, "poison_blade", "duration-seconds", 10.0D) * 20.0D);
        stateTracker.activatePoisonSting(player, durationTicks);
        player.playSound(player.getLocation(), Sound.ENTITY_SPIDER_AMBIENT, 0.8F, 1.25F);
        player.getWorld().spawnParticle(Particle.SNEEZE, player.getLocation().add(0.0D, 1.0D, 0.0D), 18, 0.35D, 0.45D, 0.35D, 0.02D);
    }

    private void handleRobot(PlayerInteractEvent event, Player player, ItemStack item) {
        HeroStateTracker.RobotBorrowedSkillState borrowedSkill = stateTracker.getRobotBorrowedSkill(player);
        if (borrowedSkill != null && isRobotBorrowedItem(item, borrowedSkill)) {
            if (denySafeZoneOffense(player, event)) {
                return;
            }
            useRobotBorrowedSkill(event, player, item, borrowedSkill);
            return;
        }

        if (!event.getAction().isRightClick() || item.getType() != Material.COMPARATOR) {
            return;
        }
        event.setCancelled(true);

        List<BorrowedSkillChoice> pool = getRobotSkillPool();
        if (pool.isEmpty()) {
            player.sendMessage(Branding.PREFIX + "当前没有可借用的技能。");
            return;
        }
        if (onCooldown(player, "smart_ai", skillConfig.cooldownMillis(HeroClass.ROBOT, "smart_ai", 30.0D))) {
            return;
        }

        BorrowedSkillChoice choice = pool.get((int) (Math.random() * pool.size()));
        ItemStack baseItem = buildRobotAiItem();
        ItemStack borrowedItem = buildRobotBorrowedItem(choice);
        player.getInventory().setItem(player.getInventory().getHeldItemSlot(), borrowedItem);
        stateTracker.setRobotBorrowedSkill(player, baseItem, choice.heroClass(), choice.binding(), borrowedItem);
        player.playSound(player.getLocation(), Sound.BLOCK_COMPARATOR_CLICK, 1.0F, 1.25F);
        player.sendMessage(Branding.PREFIX + "智能性AI 已借用 " + choice.heroName() + " 的技能。");
    }

    private void handleSacredWar(PlayerInteractEvent event, Player player, ItemStack item) {
        if (!event.getAction().isRightClick() || item.getType() != Material.GOLD_NUGGET) {
            return;
        }
        if (denySafeZoneOffense(player, event)) {
            return;
        }
        event.setCancelled(true);
        if (onCooldown(player, "sacred_war_guard", skillConfig.cooldownMillis(HeroClass.SACRED_WAR, "sacred_war_guard", 15.0D))) {
            return;
        }
        player.setAbsorptionAmount(Math.max(player.getAbsorptionAmount(),
                Math.max(8.0D, skillConfig.value(HeroClass.SACRED_WAR, "sacred_war_guard", "absorption", 8.0D))));
        player.playSound(player.getLocation(), Sound.ITEM_TOTEM_USE, 0.9F, 1.25F);
        player.playSound(player.getLocation(), Sound.BLOCK_AMETHYST_BLOCK_RESONATE, 0.6F, 1.5F);
    }

    private void handleWindwalker(PlayerInteractEvent event, Player player, ItemStack item) {
        if (!event.getAction().isRightClick()) {
            return;
        }

        if (item.getType() == Material.FEATHER) {
            event.setCancelled(true);
            if (denySafeZoneOffense(player, event)) {
                return;
            }
            if (onCooldown(player, "windwalker_rise", skillConfig.cooldownMillis(HeroClass.WINDWALKER, "windwalker_rise", 30.0D))) {
                return;
            }
            damageNearbyPlayers(
                    player,
                    skillConfig.value(HeroClass.WINDWALKER, "windwalker_rise", "radius", 3.0D),
                    skillConfig.value(HeroClass.WINDWALKER, "windwalker_rise", "damage", 3.0D)
            );
            player.setVelocity(new Vector(0.0D, skillConfig.value(HeroClass.WINDWALKER, "windwalker_rise", "vertical-velocity", 1.0D), 0.0D));
            player.setFallDistance(0.0F);
            player.addPotionEffect(new PotionEffect(PotionEffectType.SLOW_FALLING,
                    (int) Math.round(skillConfig.value(HeroClass.WINDWALKER, "windwalker_rise", "slow-falling-seconds", 2.0D) * 20.0D),
                    0, false, false));
            player.playSound(player.getLocation(), Sound.ENTITY_BREEZE_WIND_BURST, 1.0F, 1.2F);
            return;
        }

        if (item.getType() == Material.SUGAR) {
            event.setCancelled(true);
            if (denySafeZoneOffense(player, event)) {
                return;
            }
            if (onCooldown(player, "windwalker_dash", skillConfig.cooldownMillis(HeroClass.WINDWALKER, "windwalker_dash", 30.0D))) {
                return;
            }
            Vector launch = player.getLocation().getDirection().setY(0.0D).normalize()
                    .multiply(skillConfig.value(HeroClass.WINDWALKER, "windwalker_dash", "horizontal-velocity", 1.6D))
                    .setY(skillConfig.value(HeroClass.WINDWALKER, "windwalker_dash", "vertical-velocity", 0.2D));
            player.setVelocity(launch);
            player.setFallDistance(0.0F);
            player.playSound(player.getLocation(), Sound.ENTITY_WIND_CHARGE_WIND_BURST, 1.0F, 1.0F);
            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                if (player.isOnline() && gameService.isParticipant(player)) {
                    damageNearbyPlayersAt(
                            player,
                            player.getLocation(),
                            skillConfig.value(HeroClass.WINDWALKER, "windwalker_dash", "radius", 3.0D),
                            skillConfig.value(HeroClass.WINDWALKER, "windwalker_dash", "damage", 3.0D)
                    );
                    player.getWorld().spawnParticle(Particle.CLOUD, player.getLocation().add(0.0D, 0.5D, 0.0D), 18, 0.5D, 0.2D, 0.5D, 0.02D);
                }
            }, Math.round(skillConfig.value(HeroClass.WINDWALKER, "windwalker_dash", "impact-delay-ticks", 6.0D)));
        }
    }

    private void handleHomelander(PlayerInteractEvent event, Player player, ItemStack item) {
        if (!event.getAction().isRightClick()) {
            return;
        }

        if (item.getType() == Material.PHANTOM_MEMBRANE) {
            event.setCancelled(true);
            if (denySafeZoneOffense(player, event)) {
                return;
            }
            if (onCooldown(player, "homelander_flight", skillConfig.cooldownMillis(HeroClass.HOMELANDER, "homelander_flight", 30.0D))) {
                return;
            }
            player.setFallDistance(0.0F);
            player.setVelocity(player.getLocation().getDirection().multiply(0.25D).setY(0.35D));
            player.playSound(player.getLocation(), Sound.ENTITY_ENDER_DRAGON_FLAP, 1.0F, 1.2F);
            startFlight(player, Math.round(skillConfig.value(HeroClass.HOMELANDER, "homelander_flight", "duration-seconds", 6.0D) * 20.0D));
            return;
        }

        if (item.getType() != Material.REDSTONE_TORCH) {
            return;
        }
        if (denySafeZoneOffense(player, event)) {
            return;
        }

        event.setCancelled(true);
        if (homelanderLaserTasks.containsKey(player.getUniqueId())) {
            return;
        }

        Player target = findTargetInSight(player, skillConfig.value(HeroClass.HOMELANDER, "homelander_laser", "range", 15.0D), 0.6D);
        if (target == null) {
            return;
        }
        if (onCooldown(player, "homelander_laser", skillConfig.cooldownMillis(HeroClass.HOMELANDER, "homelander_laser", 20.0D))) {
            return;
        }

        long lockTicks = Math.round(skillConfig.value(HeroClass.HOMELANDER, "homelander_laser", "lock-seconds", 2.0D) * 20.0D);
        player.playSound(player.getLocation(), Sound.BLOCK_BEACON_POWER_SELECT, 0.8F, 1.4F);
        BukkitTask task = Bukkit.getScheduler().runTaskLater(plugin, () -> {
            homelanderLaserTasks.remove(player.getUniqueId());
            if (!player.isOnline() || !target.isOnline() || !gameService.isParticipant(player) || !isValidCombatTarget(player, target)) {
                return;
            }

            Player lockedTarget = findTargetInSight(player, skillConfig.value(HeroClass.HOMELANDER, "homelander_laser", "range", 15.0D), 0.8D);
            if (!target.equals(lockedTarget)) {
                return;
            }

            spawnBeam(player.getEyeLocation(), target.getEyeLocation(), Particle.CRIT);
            dealTrueSkillDamage(player, target, skillConfig.value(HeroClass.HOMELANDER, "homelander_laser", "damage", 9.0D));
            target.playSound(target.getLocation(), Sound.ENTITY_GENERIC_BURN, 1.0F, 0.8F);
            player.playSound(player.getLocation(), Sound.ENTITY_GUARDIAN_ATTACK, 1.0F, 1.4F);
        }, lockTicks);
        homelanderLaserTasks.put(player.getUniqueId(), task);
    }

    private void handleVoidWalker(PlayerInteractEvent event, Player player, ItemStack item) {
        if (!event.getAction().isRightClick() || item.getType() != Material.CHORUS_FRUIT) {
            return;
        }
        if (denySafeZoneOffense(player, event)) {
            return;
        }
        event.setCancelled(true);
        if (onCooldown(player, "void_walk", skillConfig.cooldownMillis(HeroClass.VOID_WALKER, "void_walk", 12.0D))) {
            return;
        }

        double maxDistance = skillConfig.value(HeroClass.VOID_WALKER, "void_walk", "max-distance", 15.0D);
        Location target = findVoidWalkDestination(player, maxDistance);
        if (target == null) {
            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 0.7F, 0.6F);
            return;
        }

        player.teleport(target);
        player.setFallDistance(0.0F);
        player.getWorld().spawnParticle(Particle.PORTAL, target.clone().add(0.0D, 1.0D, 0.0D), 30, 0.4D, 0.8D, 0.4D, 0.2D);
        player.playSound(player.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 1.0F, 0.9F);
    }

    private void handleSpatialMage(PlayerInteractEvent event, Player player, ItemStack item) {
        if (!event.getAction().isRightClick() || item.getType() != Material.ENDER_PEARL) {
            return;
        }
        if (denySafeZoneOffense(player, event)) {
            return;
        }
        event.setCancelled(true);
        if (onCooldown(player, "spatial_pearl", skillConfig.cooldownMillis(HeroClass.SPATIAL_MAGE, "spatial_pearl", 5.0D))) {
            return;
        }

        EnderPearl pearl = player.launchProjectile(EnderPearl.class);
        pearl.setVelocity(player.getEyeLocation().getDirection().multiply(1.5D));
        stateTracker.trackProjectile(pearl, HeroStateTracker.ProjectileSkill.SPATIAL_PEARL);
        player.playSound(player.getLocation(), Sound.ENTITY_ENDER_PEARL_THROW, 1.0F, 1.0F);
    }

    private void handleTide(PlayerInteractEvent event, Player player, ItemStack item) {
        if (!event.getAction().isRightClick() || item.getType() != Material.NAUTILUS_SHELL) {
            return;
        }
        if (denySafeZoneOffense(player, event)) {
            return;
        }
        event.setCancelled(true);
        Player target = findNearestTarget(player, skillConfig.value(HeroClass.TIDE, "tide_pull", "range", 5.0D));
        if (target == null) {
            return;
        }
        if (onCooldown(player, "tide_pull", skillConfig.cooldownMillis(HeroClass.TIDE, "tide_pull", 22.0D))) {
            return;
        }
        Vector pull = player.getLocation().toVector().subtract(target.getLocation().toVector()).normalize()
                .multiply(skillConfig.value(HeroClass.TIDE, "tide_pull", "pull-strength", 1.15D));
        pull.setY(0.25D);
        target.setVelocity(pull);
        dealTrueSkillDamage(player, target, skillConfig.value(HeroClass.TIDE, "tide_pull", "damage", 3.0D));
        target.addPotionEffect(new PotionEffect(
                PotionEffectType.SLOWNESS,
                (int) Math.round(skillConfig.value(HeroClass.TIDE, "tide_pull", "slow-seconds", 2.0D) * 20.0D),
                skillConfig.intValue(HeroClass.TIDE, "tide_pull", "slow-amplifier", 1),
                false,
                true
        ));
        target.getWorld().spawnParticle(Particle.SPLASH, target.getLocation().add(0.0D, 1.0D, 0.0D), 18, 0.5D, 0.4D, 0.5D, 0.15D);
        player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_SPLASH, 1.0F, 1.1F);
    }

    private void handlePrism(PlayerInteractEvent event, Player player, ItemStack item) {
        if (!event.getAction().isRightClick() || item.getType() != Material.AMETHYST_SHARD) {
            return;
        }
        if (denySafeZoneOffense(player, event)) {
            return;
        }
        event.setCancelled(true);
        if (stateTracker.hasPrism(player)) {
            return;
        }
        if (onCooldown(player, "prism_stance", skillConfig.cooldownMillis(HeroClass.PRISM, "prism_stance", 28.0D))) {
            return;
        }
        stateTracker.activatePrism(player, Math.round(skillConfig.value(HeroClass.PRISM, "prism_stance", "duration-seconds", 4.0D) * 20.0D));
        player.getWorld().spawnParticle(Particle.ENCHANT, player.getLocation().add(0.0D, 1.0D, 0.0D), 20, 0.6D, 0.8D, 0.6D, 0.01D);
        player.playSound(player.getLocation(), Sound.BLOCK_AMETHYST_BLOCK_CHIME, 1.0F, 1.3F);
    }

    private void handleGeomancer(PlayerInteractEvent event, Player player, ItemStack item) {
        if (!event.getAction().isRightClick() || item.getType() != Material.MAGMA_CREAM) {
            return;
        }
        if (denySafeZoneOffense(player, event)) {
            return;
        }
        event.setCancelled(true);
        if (onCooldown(player, "geomancer_quake", skillConfig.cooldownMillis(HeroClass.GEOMANCER, "geomancer_quake", 20.0D))) {
            return;
        }
        Location center = player.getLocation().clone().add(player.getLocation().getDirection().normalize()
                .multiply(skillConfig.value(HeroClass.GEOMANCER, "geomancer_quake", "range", 5.0D)));
        center.setY(player.getLocation().getY());
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            if (!player.isOnline() || !gameService.isParticipant(player)) {
                return;
            }
            double radius = skillConfig.value(HeroClass.GEOMANCER, "geomancer_quake", "radius", 3.5D);
            center.getWorld().spawnParticle(Particle.BLOCK, center, 30, radius / 2.0D, 0.2D, radius / 2.0D, 0.0D, Material.STONE.createBlockData());
            center.getWorld().playSound(center, Sound.ENTITY_GENERIC_EXPLODE, 0.8F, 0.7F);
            for (Entity entity : center.getWorld().getNearbyEntities(center, radius, radius, radius)) {
                if (entity instanceof Player target
                        && dealTrueSkillDamage(player, target,
                        skillConfig.value(HeroClass.GEOMANCER, "geomancer_quake", "damage", 4.0D)) > 0.0D) {
                    Vector velocity = target.getVelocity();
                    velocity.setY(Math.max(velocity.getY(), skillConfig.value(HeroClass.GEOMANCER, "geomancer_quake", "launch-y", 0.55D)));
                    target.setVelocity(velocity);
                }
            }
        }, Math.round(skillConfig.value(HeroClass.GEOMANCER, "geomancer_quake", "delay-seconds", 1.2D) * 20.0D));
    }

    private void handleRift(PlayerInteractEvent event, Player player, ItemStack item) {
        if (!event.getAction().isRightClick() || item.getType() != Material.CLOCK) {
            return;
        }
        if (denySafeZoneOffense(player, event)) {
            return;
        }
        event.setCancelled(true);
        if (stateTracker.hasTimeAnchor(player)) {
            returnToAnchor(player);
            return;
        }
        if (onCooldown(player, "rift_anchor", skillConfig.cooldownMillis(HeroClass.RIFT, "rift_anchor", 30.0D))) {
            return;
        }
        stateTracker.setTimeAnchor(player, player.getLocation().clone(),
                Math.round(skillConfig.value(HeroClass.RIFT, "rift_anchor", "anchor-seconds", 6.0D) * 20.0D),
                () -> {
                    if (player.isOnline()) {
                        returnToAnchor(player);
                    }
                });
        player.sendMessage(Branding.PREFIX + "已记录当前位置，效果结束后会回到这里。");
        player.playSound(player.getLocation(), Sound.BLOCK_RESPAWN_ANCHOR_SET_SPAWN, 0.8F, 1.2F);
    }

    private void handleFrostmark(PlayerInteractEvent event, Player player, ItemStack item) {
        if (!event.getAction().isRightClick() || item.getType() != Material.SNOWBALL) {
            return;
        }
        if (denySafeZoneOffense(player, event)) {
            return;
        }
        event.setCancelled(true);
        if (onCooldown(player, "frostmark_breath", skillConfig.cooldownMillis(HeroClass.FROSTMARK, "frostmark_breath", 18.0D))) {
            return;
        }
        Vector facing = player.getEyeLocation().getDirection().normalize();
        Location origin = player.getEyeLocation();
        double range = skillConfig.value(HeroClass.FROSTMARK, "frostmark_breath", "range", 5.0D);
        double damage = skillConfig.value(HeroClass.FROSTMARK, "frostmark_breath", "damage", 2.0D);
        double coneDot = skillConfig.value(HeroClass.FROSTMARK, "frostmark_breath", "cone-dot", 0.45D);
        int durationTicks = (int) Math.round(skillConfig.value(HeroClass.FROSTMARK, "frostmark_breath", "slow-seconds", 2.5D) * 20.0D);
        int amplifier = skillConfig.intValue(HeroClass.FROSTMARK, "frostmark_breath", "slow-amplifier", 2);
        for (Entity entity : player.getNearbyEntities(range, range, range)) {
            if (!(entity instanceof Player target) || target.equals(player) || gameService.isInSafeZone(target)) {
                continue;
            }
            Vector toTarget = target.getEyeLocation().toVector().subtract(origin.toVector()).normalize();
            if (facing.dot(toTarget) >= coneDot) {
                dealTrueSkillDamage(player, target, damage);
                target.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, durationTicks, amplifier, false, true));
            }
        }
        player.getWorld().spawnParticle(Particle.SNOWFLAKE, player.getEyeLocation(), 30, 1.2D, 0.6D, 1.2D, 0.02D);
        player.playSound(player.getLocation(), Sound.BLOCK_POWDER_SNOW_BREAK, 0.9F, 1.2F);
    }

    private void handleRazor(PlayerInteractEvent event, Player player, ItemStack item) {
        if (!event.getAction().isRightClick() || item.getType() != Material.WHITE_BANNER) {
            return;
        }
        if (denySafeZoneOffense(player, event)) {
            return;
        }
        event.setCancelled(true);
        if (onCooldown(player, "razor_dash", skillConfig.cooldownMillis(HeroClass.RAZOR, "razor_dash", 25.0D))) {
            return;
        }
        Vector direction = player.getLocation().getDirection().setY(0.0D).normalize();
        player.setVelocity(direction.clone().multiply(skillConfig.value(HeroClass.RAZOR, "razor_dash", "velocity", 1.8D)).setY(0.15D));
        player.setFallDistance(0.0F);
        RayTraceResult result = player.getWorld().rayTraceEntities(
                player.getEyeLocation(),
                direction,
                skillConfig.value(HeroClass.RAZOR, "razor_dash", "distance", 6.0D),
                1.2D,
                entity -> entity instanceof Player target && !target.equals(player) && !gameService.isInSafeZone(target)
        );
        if (result != null && result.getHitEntity() instanceof Player target) {
            dealTrueSkillDamage(player, target, skillConfig.value(HeroClass.RAZOR, "razor_dash", "damage", 5.0D));
            target.addPotionEffect(new PotionEffect(
                    PotionEffectType.WEAKNESS,
                    (int) Math.round(skillConfig.value(HeroClass.RAZOR, "razor_dash", "weakness-seconds", 3.0D) * 20.0D),
                    skillConfig.intValue(HeroClass.RAZOR, "razor_dash", "weakness-amplifier", 0),
                    false,
                    true
            ));
        }
        player.playSound(player.getLocation(), Sound.ITEM_TRIDENT_RIPTIDE_1, 0.8F, 1.25F);
    }

    private void returnToAnchor(Player player) {
        Location anchor = stateTracker.getTimeAnchor(player);
        stateTracker.clearTimeAnchor(player);
        if (anchor == null || anchor.getWorld() == null || !player.isOnline()) {
            return;
        }
        if (!anchor.getWorld().equals(player.getWorld()) || !isSafeTeleport(anchor)) {
            return;
        }
        player.teleport(anchor);
        double max = player.getAttribute(org.bukkit.attribute.Attribute.MAX_HEALTH) != null
                ? player.getAttribute(org.bukkit.attribute.Attribute.MAX_HEALTH).getValue()
                : 20.0D;
        player.setHealth(Math.min(max, player.getHealth() + skillConfig.value(HeroClass.RIFT, "rift_anchor", "heal", 4.0D)));
        player.playSound(player.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 1.0F, 1.2F);
    }

    private boolean isSafeTeleport(Location location) {
        Block feet = location.getBlock();
        Block head = feet.getRelative(0, 1, 0);
        Block below = feet.getRelative(0, -1, 0);
        return feet.isPassable() && head.isPassable() && below.getType().isSolid();
    }

    private void useStew(Player player, ItemStack stew, PlayerInteractEvent event) {
        double maxHealth = getMaxHealth(player);
        if (player.getHealth() >= maxHealth) {
            return;
        }
        event.setCancelled(true);
        HeroClass heroClass = gameService.getSelectedHero(player);
        double healPercent = switch (heroClass) {
            case IMMORTAL -> 0.15D;
            case MEDIC -> 0.50D;
            default -> 0.35D;
        };
        player.setHealth(Math.min(maxHealth, player.getHealth() + (maxHealth * healPercent)));
        stew.setAmount(Math.max(0, stew.getAmount() - 1));
        player.playSound(player.getLocation(), Sound.ENTITY_GENERIC_DRINK, 1.0F, 1.0F);
    }

    private void performDragonBreathBurst(Player player, double radius, double damage, int fireTicks) {
        for (Entity entity : player.getNearbyEntities(radius, radius, radius)) {
            if (entity instanceof Player target && dealTrueSkillDamage(player, target, damage) > 0.0D) {
                target.setFireTicks(fireTicks);
            }
        }
        player.getWorld().spawnParticle(Particle.DRAGON_BREATH, player.getLocation().add(0.0D, 1.0D, 0.0D), 28, radius / 2.5D, 0.4D, radius / 2.5D, 0.03F);
        player.playSound(player.getLocation(), Sound.ENTITY_GHAST_SHOOT, 1.0F, 1.0F);
    }

    private void handleLandingSlam(Player player, EntityDamageEvent event, double radius, double damage) {
        if (player.getFallDistance() < 3.0F) {
            event.setCancelled(true);
            return;
        }
        event.setCancelled(true);
        player.getWorld().spawnParticle(Particle.EXPLOSION, player.getLocation(), 1);
        player.playSound(player.getLocation(), Sound.ENTITY_GENERIC_EXPLODE, 0.9F, 1.0F);
        for (Entity entity : player.getNearbyEntities(radius, radius, radius)) {
            if (entity instanceof Player target) {
                dealTrueSkillDamage(player, target, damage, false);
            }
        }
    }

    private void spawnTemporaryWebCross(Location center) {
        List<Block> blocks = new ArrayList<>();
        Block base = center.getBlock();
        blocks.add(base);
        blocks.add(base.getRelative(1, 0, 0));
        blocks.add(base.getRelative(-1, 0, 0));
        blocks.add(base.getRelative(0, 0, 1));
        blocks.add(base.getRelative(0, 0, -1));

        for (Block block : blocks) {
            if (block.getType().isAir()) {
                block.setType(Material.COBWEB, false);
            }
        }

        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            for (Block block : blocks) {
                if (block.getType() == Material.COBWEB) {
                    block.setType(Material.AIR, false);
                }
            }
        }, 100L);
    }

    private boolean onCooldown(Player player, String key, long cooldownMillis) {
        long remaining = gameService.tryUseCooldown(player, key, cooldownMillis);
        if (remaining > 0L) {
            long seconds = (remaining + 999L) / 1000L;
            player.sendMessage(Branding.PREFIX + "技能冷却中，还需 " + seconds + " 秒。");
            return true;
        }
        stateTracker.scheduleCooldownReady(player, key, skillDisplayName(key), Math.max(1L, cooldownMillis / 50L));
        return false;
    }

    private String skillDisplayName(String key) {
        return switch (key) {
            case "birdman_dash" -> "飞羽突进";
            case "claw_web" -> "蛛网弹";
            case "destroyer_blast" -> "爆震头颅";
            case "summoner_golem" -> "铁傀儡召唤";
            case "inferno_hounds" -> "地狱猎犬";
            case "thor_lightning" -> "落雷";
            case "vampire_bloodlust" -> "嗜血";
            case "thug_charge" -> "熔拳蓄力";
            case "phantom_flight" -> "幻行";
            case "shackle_bell" -> "禁铃";
            case "shadow_bind" -> "影缚";
            case "blood_rite" -> "血祭";
            case "artillery_shell" -> "炮击弹";
            case "grave_legion" -> "墓军";
            case "cavalry_horse" -> "战马召唤";
            case "achilles_leap" -> "跟腱弹射";
            case "dragon_knight_fireball" -> "龙焰火球";
            case "dragon_breath" -> "龙息喷发";
            case "asura_step" -> "阿修罗突进";
            case "toxic_wave" -> "毒雾扩散";
            case "poison_blade" -> "涂毒";
            case "smart_ai" -> "智能性AI";
            case "sacred_war_guard" -> "圣战护盾";
            case "windwalker_rise" -> "风行升空";
            case "windwalker_dash" -> "风行突进";
            case "homelander_flight" -> "祖国人飞行";
            case "homelander_laser" -> "镭射眼";
            case "void_walk" -> "虚空行走";
            case "spatial_pearl" -> "空间珍珠";
            case "tide_pull" -> "潮汐牵引";
            case "prism_stance" -> "棱镜姿态";
            case "geomancer_quake" -> "裂地冲击";
            case "rift_anchor" -> "时隙锚点";
            case "frostmark_breath" -> "冰息喷吐";
            case "razor_dash" -> "裂锋突进";
            case "engineer_turret" -> "哨戒炮台";
            default -> "未知技能";
        };
    }
    private boolean isInterceptableProjectile(Entity entity) {
        if (!(entity instanceof Projectile projectile)) {
            return false;
        }
        return projectile instanceof Arrow
                || projectile instanceof Snowball
                || entity.getType().name().contains("FISHING")
                || entity.getType().name().contains("WIND_CHARGE")
                || projectile instanceof EnderPearl;
    }

    private void damageNearbyPlayers(Player player, double radius, double damage) {
        for (Entity entity : player.getNearbyEntities(radius, radius, radius)) {
            if (entity instanceof Player target) {
                dealTrueSkillDamage(player, target, damage);
            }
        }
    }

    private void damageNearbyPlayersAt(Player source, Location center, double radius, double damage) {
        for (Entity entity : center.getWorld().getNearbyEntities(center, radius, radius, radius)) {
            if (entity instanceof Player target) {
                dealTrueSkillDamage(source, target, damage);
            }
        }
    }

    private Player findNearestTarget(Player player, double radius) {
        Player nearest = null;
        double nearestDistance = Double.MAX_VALUE;
        for (Entity entity : player.getNearbyEntities(radius, radius, radius)) {
            if (!(entity instanceof Player target) || !isValidCombatTarget(player, target)) {
                continue;
            }
            double distance = target.getLocation().distanceSquared(player.getLocation());
            if (distance < nearestDistance) {
                nearestDistance = distance;
                nearest = target;
            }
        }
        return nearest;
    }

    private Player findRandomTarget(Player player, double radius) {
        List<Player> targets = new ArrayList<>();
        for (Entity entity : player.getNearbyEntities(radius, radius, radius)) {
            if (entity instanceof Player target && isValidCombatTarget(player, target)) {
                targets.add(target);
            }
        }
        if (targets.isEmpty()) {
            return null;
        }
        return targets.get((int) (Math.random() * targets.size()));
    }

    private Location findEngineerTurretPlacement(Player player, double maxDistance) {
        Location eye = player.getEyeLocation();
        Vector direction = eye.getDirection().normalize();
        RayTraceResult blockHit = player.getWorld().rayTraceBlocks(eye, direction, maxDistance, FluidCollisionMode.NEVER, true);
        if (blockHit != null && blockHit.getHitBlock() != null && blockHit.getHitBlockFace() != null) {
            Block placementBase = blockHit.getHitBlock().getRelative(blockHit.getHitBlockFace());
            Location candidate = findSafeTeleportLocation(placementBase.getLocation().add(0.5D, 0.0D, 0.5D), 2);
            if (candidate != null && candidate.distanceSquared(player.getLocation()) <= (maxDistance + 1.0D) * (maxDistance + 1.0D)) {
                return candidate;
            }
        }

        Location fallback = eye.clone().add(direction.multiply(Math.max(2.0D, maxDistance * 0.5D)));
        Location candidate = findSafeTeleportLocation(fallback, 3);
        if (candidate == null) {
            return null;
        }
        return candidate.distanceSquared(player.getLocation()) <= (maxDistance + 1.0D) * (maxDistance + 1.0D) ? candidate : null;
    }

    private void registerSummon(Player player, LivingEntity entity) {
        entity.setMetadata(SUMMON_OWNER_METADATA, new FixedMetadataValue(plugin, player.getUniqueId().toString()));
        summonedEntities.computeIfAbsent(player.getUniqueId(), ignored -> new ArrayList<>()).add(entity.getUniqueId());
    }

    private void startSummonTargeting(Player owner, Mob summon, double range) {
        BukkitTask oldTask = summonTargetTasks.remove(summon.getUniqueId());
        if (oldTask != null) {
            oldTask.cancel();
        }

        BukkitTask task = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            if (!owner.isOnline() || !gameService.isParticipant(owner) || summon.isDead() || !summon.isValid()) {
                BukkitTask current = summonTargetTasks.remove(summon.getUniqueId());
                if (current != null) {
                    current.cancel();
                }
                summonAttackCooldowns.remove(summon.getUniqueId());
                return;
            }

            if (summon.getTarget() instanceof Player currentTarget && isValidSummonTarget(owner, currentTarget)) {
                summon.setTarget(currentTarget);
                if (!(summon instanceof Wolf)) {
                    attemptSummonMeleeAttack(owner, summon, currentTarget);
                }
                return;
            }

            Player newTarget = findNearestSummonTarget(owner, summon.getLocation(), range);
            summon.setTarget(newTarget);
            if (newTarget == null) {
                summonAttackCooldowns.remove(summon.getUniqueId());
            }
        }, 0L, 10L);

        summonTargetTasks.put(summon.getUniqueId(), task);
    }

    private void attemptSummonMeleeAttack(Player owner, Mob summon, Player target) {
        if (!summon.hasLineOfSight(target)) {
            return;
        }

        double maxAttackDistance = 3.75D;
        if (!summon.getWorld().equals(target.getWorld())
                || summon.getLocation().distanceSquared(target.getLocation()) > maxAttackDistance * maxAttackDistance) {
            return;
        }

        long now = System.currentTimeMillis();
        long nextAttackTime = summonAttackCooldowns.getOrDefault(summon.getUniqueId(), 0L);
        if (now < nextAttackTime) {
            return;
        }

        double damage = SUMMON_TRUE_DAMAGE_CAP;
        AttributeInstance attackDamage = summon.getAttribute(Attribute.ATTACK_DAMAGE);
        if (attackDamage != null) {
            damage = Math.min(SUMMON_TRUE_DAMAGE_CAP, attackDamage.getValue());
        }

        summon.swingMainHand();
        dealTrueSkillDamage(owner, target, damage);

        Vector push = target.getLocation().toVector().subtract(summon.getLocation().toVector());
        if (push.lengthSquared() > 0.0001D) {
            push.normalize().multiply(0.45D).setY(0.28D);
            target.setVelocity(target.getVelocity().add(push));
        }

        target.playSound(target.getLocation(), Sound.ENTITY_IRON_GOLEM_ATTACK, 0.8F, 0.95F);
        summonAttackCooldowns.put(summon.getUniqueId(), now + 1200L);
    }

    private Player findNearestSummonTarget(Player owner, Location origin, double range) {
        Player nearest = null;
        double nearestDistance = Double.MAX_VALUE;
        for (Entity entity : origin.getWorld().getNearbyEntities(origin, range, range, range)) {
            if (!(entity instanceof Player target) || target.equals(owner) || !gameService.isParticipant(target) || gameService.isInSafeZone(target)) {
                continue;
            }
            double distance = target.getLocation().distanceSquared(origin);
            if (distance < nearestDistance) {
                nearestDistance = distance;
                nearest = target;
            }
        }
        return nearest;
    }

    private boolean isValidSummonTarget(Player owner, Player target) {
        return isValidCombatTarget(owner, target);
    }

    private void clearSummons(Player player) {
        Collection<UUID> entities = summonedEntities.remove(player.getUniqueId());
        if (entities == null) {
            return;
        }
        for (UUID entityId : entities) {
            BukkitTask targetTask = summonTargetTasks.remove(entityId);
            if (targetTask != null) {
                targetTask.cancel();
            }
            summonAttackCooldowns.remove(entityId);
            Entity entity = Bukkit.getEntity(entityId);
            if (entity != null && !entity.isDead()) {
                if (entity instanceof AbstractHorse horse && horse.getPassengers().contains(player)) {
                    player.leaveVehicle();
                }
                entity.remove();
            }
        }
    }

    private boolean handleSummonDamage(EntityDamageByEntityEvent event) {
        if (!(event.getEntity() instanceof Player target)) {
            return false;
        }

        Player owner = resolveSummonOwner(event.getDamager());
        if (owner == null || owner.equals(target)) {
            return false;
        }

        if (!isValidCombatTarget(owner, target)) {
            event.setCancelled(true);
            return true;
        }

        if (event.getDamager() instanceof Projectile) {
            event.setCancelled(true);
            dealTrueSkillDamage(owner, target, Math.min(SUMMON_TRUE_DAMAGE_CAP, event.getDamage()));
            return true;
        }

        if (event.getDamager() instanceof Wolf) {
            return false;
        }

        event.setCancelled(true);
        return true;
    }

    private Player resolveSummonOwner(Entity damager) {
        Entity source = damager;
        if (damager instanceof Projectile projectile && projectile.getShooter() instanceof Entity shooter) {
            source = shooter;
        }
        if (!source.hasMetadata(SUMMON_OWNER_METADATA)) {
            return null;
        }

        String ownerId = source.getMetadata(SUMMON_OWNER_METADATA).get(0).asString();
        try {
            return Bukkit.getPlayer(UUID.fromString(ownerId));
        } catch (IllegalArgumentException exception) {
            return null;
        }
    }

    private void startFlight(Player player, long durationTicks) {
        clearFlight(player);
        player.setAllowFlight(true);
        player.setFlying(true);
        Bukkit.getScheduler().runTask(plugin, () -> {
            if (player.isOnline() && gameService.isParticipant(player)) {
                player.setAllowFlight(true);
                player.setFlying(true);
            }
        });
        BukkitTask task = Bukkit.getScheduler().runTaskLater(plugin, () -> clearFlight(player), durationTicks);
        flightTasks.put(player.getUniqueId(), task);
    }

    private void startPhantomSpectator(Player player, long durationTicks) {
        clearPhantomSpectator(player, false);
        player.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, (int) durationTicks, 1, false, false));
        player.setGameMode(GameMode.SPECTATOR);
        player.setFallDistance(0.0F);
        BukkitTask task = Bukkit.getScheduler().runTaskLater(plugin, () -> clearPhantomSpectator(player, true), durationTicks);
        phantomSpectatorTasks.put(player.getUniqueId(), task);
    }

    private void clearPhantomSpectator(Player player, boolean grantFallImmunity) {
        BukkitTask task = phantomSpectatorTasks.remove(player.getUniqueId());
        if (task != null) {
            task.cancel();
        }
        player.removePotionEffect(PotionEffectType.REGENERATION);
        if (player.getGameMode() == GameMode.SPECTATOR) {
            player.setGameMode(GameMode.SURVIVAL);
            player.setFallDistance(0.0F);
            if (grantFallImmunity) {
                phantomFallImmunity.add(player.getUniqueId());
                player.playSound(player.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 0.9F, 1.15F);
            }
        }
    }

    private void clearFlight(Player player) {
        BukkitTask task = flightTasks.remove(player.getUniqueId());
        if (task != null) {
            task.cancel();
        }
        if (player.getGameMode() != GameMode.CREATIVE && player.getGameMode() != GameMode.SPECTATOR) {
            player.setFlying(false);
            player.setAllowFlight(false);
        }
    }

    private boolean isDragonKnightImmune(EntityDamageEvent event, DamageType damageType) {
        return event.getCause() == EntityDamageEvent.DamageCause.FIRE
                || event.getCause() == EntityDamageEvent.DamageCause.FIRE_TICK
                || event.getCause() == EntityDamageEvent.DamageCause.LAVA
                || event.getCause() == EntityDamageEvent.DamageCause.BLOCK_EXPLOSION
                || event.getCause() == EntityDamageEvent.DamageCause.ENTITY_EXPLOSION
                || damageType == DamageType.FIREBALL
                || damageType == DamageType.UNATTRIBUTED_FIREBALL
                || damageType == DamageType.EXPLOSION
                || damageType == DamageType.PLAYER_EXPLOSION
                || damageType == DamageType.IN_FIRE
                || damageType == DamageType.ON_FIRE
                || damageType == DamageType.LAVA;
    }

    private double getMaxHealth(Player player) {
        AttributeInstance maxHealth = player.getAttribute(Attribute.MAX_HEALTH);
        return maxHealth != null ? maxHealth.getValue() : 20.0D;
    }

    private void schedulePassiveRefresh(Player player) {
        Bukkit.getScheduler().runTask(plugin, () -> {
            if (player.isOnline() && gameService.isParticipant(player)) {
                refreshPassiveEffects(player);
            }
        });
    }

    private void updateBerserkerState(Player player) {
        double thresholdRatio = skillConfig.value(HeroClass.BERSERKER, "berserker_passive", "threshold-ratio", 0.30D);
        double threshold = getMaxHealth(player) * thresholdRatio;
        boolean active = player.getHealth() <= threshold;
        if (active) {
            player.addPotionEffect(new PotionEffect(PotionEffectType.STRENGTH, PotionEffect.INFINITE_DURATION, 0, false, false));
            player.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, PotionEffect.INFINITE_DURATION, 0, false, false));
        } else {
            player.removePotionEffect(PotionEffectType.STRENGTH);
            player.removePotionEffect(PotionEffectType.REGENERATION);
        }
    }

    private boolean isWeapon(ItemStack item) {
        if (item == null) {
            return false;
        }
        String type = item.getType().name();
        return type.endsWith("_SWORD") || type.endsWith("_AXE");
    }

    private boolean isAxe(ItemStack item) {
        return item != null && item.getType().name().endsWith("_AXE");
    }

    private boolean isPoisonSword(ItemStack item) {
        if (item == null || item.getType() != Material.IRON_SWORD) {
            return false;
        }
        ItemMeta meta = item.getItemMeta();
        return meta != null
                && meta.hasDisplayName()
                && "毒剑".equals(PlainTextComponentSerializer.plainText().serialize(meta.displayName()));
    }

    private int findPrimaryWeaponSlot(Player player) {
        for (int slot = 0; slot < 9; slot++) {
            ItemStack item = player.getInventory().getItem(slot);
            if (isWeapon(item)) {
                return slot;
            }
        }
        for (int slot = 9; slot < player.getInventory().getSize(); slot++) {
            ItemStack item = player.getInventory().getItem(slot);
            if (isWeapon(item)) {
                return slot;
            }
        }
        return -1;
    }

    private ItemStack buildThugWeapon(ItemStack originalWeapon, int stacks) {
        ItemStack weapon = originalWeapon.clone();
        ItemMeta meta = weapon.getItemMeta();
        int sharpness = meta.getEnchantLevel(Enchantment.SHARPNESS) + stacks;
        int fireAspect = meta.getEnchantLevel(Enchantment.FIRE_ASPECT) + stacks;
        meta.removeEnchant(Enchantment.SHARPNESS);
        meta.removeEnchant(Enchantment.FIRE_ASPECT);
        if (sharpness > 0) {
            meta.addEnchant(Enchantment.SHARPNESS, sharpness, true);
        }
        if (fireAspect > 0) {
            meta.addEnchant(Enchantment.FIRE_ASPECT, fireAspect, true);
        }
        weapon.setItemMeta(meta);
        return weapon;
    }

    private void resetThugCharge(Player player, boolean playSound) {
        HeroStateTracker.ThugChargeState state = stateTracker.getThugChargeState(player);
        if (state == null) {
            return;
        }
        player.getInventory().setItem(state.slot(), state.originalWeapon().clone());
        stateTracker.clearThugChargeState(player);
        if (playSound) {
            player.playSound(player.getLocation(), Sound.BLOCK_FIRE_EXTINGUISH, 0.8F, 0.9F);
        }
    }

    private void restoreVampireWeapon(Player player, boolean playSound) {
        HeroStateTracker.VampireBloodlustState state = stateTracker.getVampireBloodlust(player);
        if (state == null) {
            return;
        }
        player.getInventory().setItem(state.slot(), state.originalWeapon().clone());
        stateTracker.clearVampireBloodlust(player);
        if (playSound) {
            player.playSound(player.getLocation(), Sound.ENTITY_ELDER_GUARDIAN_CURSE, 0.6F, 1.5F);
        }
    }

    private ItemStack buildRobotAiItem() {
        return HeroItems.named(Material.COMPARATOR, 1, "智能性AI", List.of(
                "右键随机获得一个 Tier 1-3 主动技能",
                "每次仅能使用 1 次"
        ));
    }

    private ItemStack buildRobotBorrowedItem(BorrowedSkillChoice choice) {
        return HeroItems.named(choice.binding().material(), 1, "智能借用：" + skillDisplayName(choice.binding().key()), List.of(
                "来源职业：" + choice.heroName(),
                "成功使用后恢复为智能性AI"
        ));
    }

    private boolean isRobotBorrowedItem(ItemStack item, HeroStateTracker.RobotBorrowedSkillState state) {
        if (item == null) {
            return false;
        }
        return item.isSimilar(state.borrowedItem());
    }

    private List<BorrowedSkillChoice> getRobotSkillPool() {
        List<BorrowedSkillChoice> choices = new ArrayList<>();
        for (HeroDefinition definition : heroRegistry.getAll()) {
            if (definition.getHeroClass() == HeroClass.ROBOT || definition.getTier().ordinal() < HeroTier.TIER_3.ordinal()) {
                continue;
            }
            for (HeroSkillBinding binding : definition.getSkillBindings()) {
                if (isRobotBorrowableBinding(binding.key())) {
                    choices.add(new BorrowedSkillChoice(definition.getHeroClass(), definition.getDisplayName(), binding));
                }
            }
        }
        return choices;
    }

    private boolean isRobotBorrowableBinding(String key) {
        return switch (key) {
            case "vampire_bloodlust",
                    "void_walk",
                    "spatial_pearl",
                    "dragon_knight_fireball",
                    "phantom_flight",
                    "shackle_bell",
                    "cavalry_horse",
                    "achilles_leap",
                    "dragon_breath",
                    "asura_step",
                    "toxic_wave",
                    "sacred_war_guard",
                    "windwalker_rise",
                    "windwalker_dash",
                    "homelander_flight",
                    "homelander_laser",
                    "shadow_bind",
                    "blood_rite",
                    "artillery_shell",
                    "tide_pull",
                    "prism_stance",
                    "geomancer_quake",
                    "rift_anchor",
                    "frostmark_breath",
                    "razor_dash",
                    "engineer_turret" -> true;
            default -> false;
        };
    }

    private void useRobotBorrowedSkill(PlayerInteractEvent event, Player player, ItemStack item, HeroStateTracker.RobotBorrowedSkillState borrowedSkill) {
        event.setCancelled(true);
        if (!canUseRobotBorrowedSkill(player, borrowedSkill)) {
            return;
        }

        gameService.clearCooldown(player, borrowedSkill.binding().key());
        stateTracker.clearCooldownReadyTask(player, borrowedSkill.binding().key());
        dispatchRobotBorrowedSkill(event, player, item, borrowedSkill);
        if (didRobotBorrowedSkillSucceed(player, borrowedSkill)) {
            gameService.clearCooldown(player, borrowedSkill.binding().key());
            stateTracker.clearCooldownReadyTask(player, borrowedSkill.binding().key());
            restoreRobotBorrowedSkill(player, false);
            player.getInventory().setItem(player.getInventory().getHeldItemSlot(), buildRobotAiItem());
            player.playSound(player.getLocation(), Sound.BLOCK_COMPARATOR_CLICK, 0.9F, 0.9F);
        }
    }

    private boolean didRobotBorrowedSkillSucceed(Player player, HeroStateTracker.RobotBorrowedSkillState borrowedSkill) {
        // Borrowed active skills mark a successful cast by starting their own cooldown.
        return gameService.getRemainingCooldownMillis(player, borrowedSkill.binding().key()) > 0L;
    }

    private boolean canUseRobotBorrowedSkill(Player player, HeroStateTracker.RobotBorrowedSkillState borrowedSkill) {
        return switch (borrowedSkill.binding().key()) {
            case "void_walk" -> findVoidWalkDestination(player,
                    skillConfig.value(HeroClass.VOID_WALKER, "void_walk", "max-distance", 15.0D)) != null;
            case "tide_pull" -> findNearestTarget(player,
                    skillConfig.value(HeroClass.TIDE, "tide_pull", "range", 5.0D)) != null;
            case "asura_step" -> findRandomTarget(player,
                    skillConfig.value(HeroClass.ASURA, "asura_step", "range", 12.0D)) != null;
            case "homelander_laser" -> findTargetInSight(player,
                    skillConfig.value(HeroClass.HOMELANDER, "homelander_laser", "range", 15.0D), 0.6D) != null;
            case "shadow_bind" -> findTargetInSight(player,
                    skillConfig.value(HeroClass.SHADOW_BINDER, "shadow_bind", "range", 12.0D), 0.6D) != null;
            case "engineer_turret" -> findEngineerTurretPlacement(player, 8.0D) != null;
            default -> true;
        };
    }

    private void dispatchRobotBorrowedSkill(PlayerInteractEvent event, Player player, ItemStack item, HeroStateTracker.RobotBorrowedSkillState borrowedSkill) {
        switch (borrowedSkill.binding().key()) {
            case "vampire_bloodlust" -> handleVampire(event, player, item);
            case "void_walk" -> handleVoidWalker(event, player, item);
            case "spatial_pearl" -> handleSpatialMage(event, player, item);
            case "dragon_knight_fireball" -> handleDragonKnight(event, player, item);
            case "phantom_flight" -> handlePhantom(event, player, item);
            case "shackle_bell" -> handleShackle(event, player, item);
            case "cavalry_horse" -> handleCavalry(event, player, item);
            case "achilles_leap" -> handleAchilles(event, player, item);
            case "dragon_breath" -> handleDragonBreath(event, player, item);
            case "asura_step" -> handleAsura(event, player, item);
            case "toxic_wave" -> handleToxicLizard(event, player, item);
            case "shadow_bind" -> handleShadowBinder(event, player, item);
            case "blood_rite" -> handleBloodKnight(event, player, item);
            case "artillery_shell" -> handleArtillerist(event, player, item);
            case "sacred_war_guard" -> handleSacredWar(event, player, item);
            case "windwalker_rise", "windwalker_dash" -> handleWindwalker(event, player, item);
            case "homelander_flight", "homelander_laser" -> handleHomelander(event, player, item);
            case "tide_pull" -> handleTide(event, player, item);
            case "prism_stance" -> handlePrism(event, player, item);
            case "geomancer_quake" -> handleGeomancer(event, player, item);
            case "rift_anchor" -> handleRift(event, player, item);
            case "frostmark_breath" -> handleFrostmark(event, player, item);
            case "razor_dash" -> handleRazor(event, player, item);
            case "engineer_turret" -> handleEngineer(event, player, item);
            default -> {
            }
        }
    }

    private void restoreRobotBorrowedSkill(Player player, boolean playSound) {
        HeroStateTracker.RobotBorrowedSkillState state = stateTracker.getRobotBorrowedSkill(player);
        if (state == null) {
            return;
        }

        for (int slot = 0; slot < player.getInventory().getSize(); slot++) {
            ItemStack current = player.getInventory().getItem(slot);
            if (current != null && current.isSimilar(state.borrowedItem())) {
                player.getInventory().setItem(slot, state.baseItem().clone());
                stateTracker.clearRobotBorrowedSkill(player);
                if (playSound) {
                    player.playSound(player.getLocation(), Sound.BLOCK_COMPARATOR_CLICK, 0.9F, 0.9F);
                }
                return;
            }
        }

        stateTracker.clearRobotBorrowedSkill(player);
    }

    private void cancelHomelanderLaser(Player player) {
        BukkitTask task = homelanderLaserTasks.remove(player.getUniqueId());
        if (task != null) {
            task.cancel();
        }
    }

    private boolean isValidCombatTarget(Player source, Player target) {
        return source != null
                && source.isOnline()
                && !source.isDead()
                && gameService.isParticipant(source)
                && !gameService.isInSafeZone(source)
                && target.isOnline()
                && !target.isDead()
                && !target.equals(source)
                && gameService.isParticipant(target)
                && !gameService.isInSafeZone(target)
                && target.getWorld().equals(source.getWorld());
    }

    private Player findTargetInSight(Player player, double range, double raySize) {
        RayTraceResult result = player.getWorld().rayTrace(
                player.getEyeLocation(),
                player.getEyeLocation().getDirection(),
                range,
                FluidCollisionMode.NEVER,
                true,
                raySize,
                entity -> entity instanceof Player target && isValidCombatTarget(player, target)
        );
        return result != null && result.getHitEntity() instanceof Player target ? target : null;
    }

    private void spawnBeam(Location start, Location end, Particle particle) {
        Vector direction = end.toVector().subtract(start.toVector());
        double length = direction.length();
        if (length <= 0.0D) {
            return;
        }
        Vector step = direction.normalize().multiply(0.35D);
        Location cursor = start.clone();
        int iterations = Math.max(1, (int) Math.ceil(length / 0.35D));
        for (int i = 0; i < iterations; i++) {
            cursor.getWorld().spawnParticle(particle, cursor, 1, 0.0D, 0.0D, 0.0D, 0.0D);
            cursor.add(step);
        }
    }

    private Location findVoidWalkDestination(Player player, double maxDistance) {
        Location eye = player.getEyeLocation();
        Vector direction = eye.getDirection().normalize();
        RayTraceResult blockHit = player.getWorld().rayTraceBlocks(eye, direction, maxDistance, FluidCollisionMode.NEVER, true);
        if (blockHit != null && blockHit.getHitBlock() != null && blockHit.getHitBlockFace() != null) {
            Block destinationBlock = blockHit.getHitBlock().getRelative(blockHit.getHitBlockFace());
            return findSafeTeleportLocation(destinationBlock.getLocation().add(0.5D, 0.0D, 0.5D), 3);
        }

        Location approximate = eye.clone().add(direction.multiply(maxDistance));
        return findSafeTeleportLocation(approximate, 4);
    }

    private Location findSafeTeleportLocation(Location approximate, int verticalRange) {
        int blockX = approximate.getBlockX();
        int blockY = approximate.getBlockY();
        int blockZ = approximate.getBlockZ();
        int[] offsets = {0, 1, -1, 2, -2, 3, -3, 4, -4};
        for (int offset : offsets) {
            if (Math.abs(offset) > verticalRange) {
                continue;
            }
            Location candidate = new Location(approximate.getWorld(), blockX + 0.5D, blockY + offset, blockZ + 0.5D);
            if (isSafeTeleport(candidate)) {
                return candidate;
            }
        }
        return null;
    }

    private void triggerArtilleryShell(Player player, Location center) {
        if (gameService.isInSafeZone(player) || gameService.isInSafeZone(center)) {
            return;
        }
        double radius = skillConfig.value(HeroClass.ARTILLERIST, "artillery_shell", "radius", 3.5D);
        double damage = skillConfig.value(HeroClass.ARTILLERIST, "artillery_shell", "damage", 6.0D);
        double knockback = skillConfig.value(HeroClass.ARTILLERIST, "artillery_shell", "knockback-strength", 1.3D);
        center.getWorld().spawnParticle(Particle.EXPLOSION, center, 2, 0.25D, 0.25D, 0.25D, 0.0D);
        center.getWorld().playSound(center, Sound.ENTITY_GENERIC_EXPLODE, 1.0F, 0.9F);
        for (Entity entity : center.getWorld().getNearbyEntities(center, radius, radius, radius)) {
            if (!(entity instanceof Player target) || dealTrueSkillDamage(player, target, damage) <= 0.0D) {
                continue;
            }
            Vector push = target.getLocation().toVector().subtract(center.toVector());
            if (push.lengthSquared() > 0.0001D) {
                push.normalize().multiply(knockback).setY(0.35D);
                target.setVelocity(target.getVelocity().add(push));
            }
        }
    }

    private void triggerDragonKnightFireball(Player player, Location center) {
        if (gameService.isInSafeZone(player) || gameService.isInSafeZone(center)) {
            return;
        }
        double radius = skillConfig.value(HeroClass.DRAGON_KNIGHT, "dragon_knight_fireball", "radius", 4.5D);
        double damage = skillConfig.value(HeroClass.DRAGON_KNIGHT, "dragon_knight_fireball", "damage", 9.0D);
        int fireTicks = (int) Math.round(skillConfig.value(HeroClass.DRAGON_KNIGHT, "dragon_knight_fireball", "fire-seconds", 5.0D) * 20.0D);
        center.getWorld().spawnParticle(Particle.EXPLOSION, center, 2, 0.2D, 0.2D, 0.2D, 0.0D);
        center.getWorld().playSound(center, Sound.ENTITY_GENERIC_EXPLODE, 1.0F, 1.1F);
        for (Entity entity : center.getWorld().getNearbyEntities(center, radius, radius, radius)) {
            if (entity instanceof Player target && dealTrueSkillDamage(player, target, damage) > 0.0D) {
                target.setFireTicks(fireTicks);
            }
        }
    }

    private boolean denySafeZoneOffense(Player player, PlayerInteractEvent event) {
        if (!gameService.isInSafeZone(player)) {
            return false;
        }
        event.setCancelled(true);
        player.sendMessage(Branding.PREFIX + "安全区内不能使用攻击技能。");
        return true;
    }

    private double dealTrueSkillDamage(Player source, Player target, double damage) {
        return dealTrueSkillDamage(source, target, damage, true);
    }

    private double dealTrueSkillDamage(Player source, Player target, double damage, boolean applyRebalanceCap) {
        if (damage <= 0.0D || !isValidCombatTarget(source, target)) {
            return 0.0D;
        }

        if (applyRebalanceCap && damage > TRUE_DAMAGE_REBALANCE_THRESHOLD) {
            damage = TRUE_DAMAGE_REBALANCE_CAP;
        }

        double appliedDamage = Math.min(damage, target.getHealth());
        if (appliedDamage <= 0.0D) {
            return 0.0D;
        }

        gameService.tagCombat(target, source);
        target.setNoDamageTicks(0);
        target.setHealth(Math.max(0.0D, target.getHealth() - damage));
        return appliedDamage;
    }

    private record BorrowedSkillChoice(HeroClass heroClass, String heroName, HeroSkillBinding binding) {
    }
}







