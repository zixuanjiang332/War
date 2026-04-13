package jdd.war.hero;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import jdd.war.War;
import jdd.war.game.Branding;
import jdd.war.game.GameService;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.AbstractHorse;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Horse;
import org.bukkit.entity.IronGolem;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.Snowball;
import org.bukkit.entity.Wolf;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.inventory.HorseInventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;

public final class HeroSkillHandler {
    private static final double DEFAULT_SKILL_RANGE = 5.0D;
    public static final String SUMMON_OWNER_METADATA = "class_war_summon_owner";

    private final War plugin;
    private final GameService gameService;
    private final HeroSkillConfig skillConfig;
    private final HeroStateTracker stateTracker;
    private final Map<UUID, ProjectileSkill> projectileSkills = new ConcurrentHashMap<>();
    private final Map<UUID, List<UUID>> summonedEntities = new ConcurrentHashMap<>();
    private final Map<UUID, BukkitTask> flightTasks = new ConcurrentHashMap<>();

    public HeroSkillHandler(War plugin, GameService gameService, HeroSkillConfig skillConfig) {
        this.plugin = plugin;
        this.gameService = gameService;
        this.skillConfig = skillConfig;
        this.stateTracker = new HeroStateTracker(plugin);
    }

    public void handleInteract(PlayerInteractEvent event) {
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

        switch (heroClass) {
            case CLAW -> handleClaw(event, player, item);
            case DESTROYER -> handleDestroyer(event, player, item);
            case SUMMONER -> handleSummoner(event, player, item);
            case INFERNO_GUARD -> handleInfernoGuard(event, player, item);
            case THUG -> handleThug(event, player, item);
            case PHANTOM -> handlePhantom(event, player, item);
            case SHACKLE -> handleShackle(event, player, item);
            case CAVALRY -> handleCavalry(event, player, item);
            case ACHILLES -> handleAchilles(event, player, item);
            case DRAGON_BREATH -> handleDragonBreath(event, player, item);
            case ASURA -> handleAsura(event, player, item);
            case TOXIC_LIZARD -> handleToxicLizard(event, player, item);
            case SACRED_WAR -> handleSacredWar(event, player, item);
            case WINDWALKER -> handleWindwalker(event, player, item);
            case HOMELANDER -> handleHomelander(event, player, item);
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
        if (gameService.isInSafeZone(player)) {
            return;
        }

        HeroClass heroClass = gameService.getSelectedHero(player);
        if (heroClass == null || event.getCause() != EntityDamageEvent.DamageCause.FALL) {
            return;
        }

        if (heroClass == HeroClass.TANK_VANGUARD) {
            handleLandingSlam(player, event, 4.5D, Math.min(10.0D, 2.5D + player.getFallDistance() * 1.2D));
        } else if (heroClass == HeroClass.HEAD_REAPER) {
            handleLandingSlam(player, event, 3.0D, Math.min(8.0D, 1.5D + player.getFallDistance()));
        }
    }

    public void handleBowShoot(EntityShootBowEvent event) {
        if (!(event.getEntity() instanceof Player player) || !gameService.isParticipant(player)) {
            return;
        }
        if (gameService.getSelectedHero(player) != HeroClass.BIRDMAN || !(event.getProjectile() instanceof Arrow arrow)) {
            return;
        }
        if (onCooldown(player, "birdman_dash", 10_000L, "风羽")) {
            return;
        }

        Vector push = arrow.getVelocity().normalize().multiply(1.5D).setY(0.75D);
        player.setVelocity(push);
        player.setFallDistance(0.0F);
        player.playSound(player.getLocation(), Sound.ENTITY_ENDER_DRAGON_FLAP, 0.8F, 1.5F);
    }

    public void handleProjectileLaunch(ProjectileLaunchEvent event) {
        if (!(event.getEntity() instanceof Snowball snowball) || !(snowball.getShooter() instanceof Player player)) {
            return;
        }
        if (!gameService.isParticipant(player)) {
            return;
        }
        if (gameService.getSelectedHero(player) == HeroClass.MAGE) {
            projectileSkills.put(snowball.getUniqueId(), ProjectileSkill.MAGE_SWAP);
        }
    }

    public void handleProjectileHit(ProjectileHitEvent event) {
        ProjectileSkill skill = projectileSkills.remove(event.getEntity().getUniqueId());
        if (skill == null || !(event.getEntity().getShooter() instanceof Player player) || !player.isOnline()) {
            return;
        }

        if (skill == ProjectileSkill.MAGE_SWAP && event.getHitEntity() instanceof Player target) {
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

        if (skill == ProjectileSkill.CLAW_WEB) {
            Location center = event.getHitEntity() != null
                    ? event.getHitEntity().getLocation()
                    : event.getHitBlock() != null
                    ? event.getHitBlock().getLocation().add(0.5D, 0.5D, 0.5D)
                    : event.getEntity().getLocation();
            spawnTemporaryWebCross(center);
        }
    }

    public void handleDamageByPlayer(EntityDamageByEntityEvent event) {
        if (event.getEntity() instanceof Player victim
                && gameService.isParticipant(victim)
                && gameService.getSelectedHero(victim) == HeroClass.PRISM
                && isInterceptableProjectile(event.getDamager())
                && stateTracker.consumePrism(victim)) {
            event.setCancelled(true);
            if (event.getDamager() instanceof Projectile projectile) {
                if (projectile.getShooter() instanceof Player attacker && !gameService.isInSafeZone(attacker)) {
                    double damage = skillConfig.value(HeroClass.PRISM, "prism_stance", "retaliation-damage", 4.0D);
                    attacker.damage(damage, victim);
                }
                projectile.remove();
            }
            victim.getWorld().spawnParticle(Particle.ENCHANT, victim.getLocation().add(0.0D, 1.0D, 0.0D), 18, 0.5D, 0.7D, 0.5D, 0.01D);
            victim.playSound(victim.getLocation(), Sound.BLOCK_AMETHYST_CLUSTER_BREAK, 1.0F, 1.2F);
            return;
        }

        if (!(event.getDamager() instanceof Player player) || !gameService.isParticipant(player) || gameService.isInSafeZone(player)) {
            return;
        }
        if (gameService.getSelectedHero(player) != HeroClass.THOR || player.getInventory().getItemInMainHand().getType() != Material.WOODEN_AXE) {
            return;
        }
        if (onCooldown(player, "thor_lightning", 14_000L, "雷斧")) {
            return;
        }

        player.getWorld().strikeLightningEffect(player.getLocation());
        for (Entity entity : player.getNearbyEntities(4.0D, 4.0D, 4.0D)) {
            if (entity instanceof Player target && !target.equals(player) && !gameService.isInSafeZone(target)) {
                target.damage(6.0D, player);
            }
        }
        player.playSound(player.getLocation(), Sound.ENTITY_LIGHTNING_BOLT_THUNDER, 0.8F, 1.0F);
    }

    public void handleFish(PlayerFishEvent event) {
        Player player = event.getPlayer();
        if (!gameService.isParticipant(player) || gameService.getSelectedHero(player) != HeroClass.FISHERMAN) {
            return;
        }
        if (event.getCaught() instanceof Player target && !gameService.isInSafeZone(target)) {
            Vector pull = player.getLocation().toVector().subtract(target.getLocation().toVector()).normalize().multiply(1.25D);
            pull.setY(0.35D);
            target.setVelocity(pull);
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

    public void clearPlayerState(Player player) {
        stateTracker.clearPlayerState(player);
        clearFlight(player);
        clearSummons(player);
        projectileSkills.entrySet().removeIf(entry -> {
            Entity entity = Bukkit.getEntity(entry.getKey());
            return entity == null || (entity instanceof Projectile projectile && projectile.getShooter() == player);
        });
    }

    private void handleClaw(PlayerInteractEvent event, Player player, ItemStack item) {
        if (item.getType() != Material.COBWEB) {
            return;
        }
        event.setCancelled(true);
        if (onCooldown(player, "claw_web", skillConfig.cooldownMillis(HeroClass.CLAW, "claw_web", 12.0D), "蛛猎技能")) {
            return;
        }
        Snowball snowball = player.launchProjectile(Snowball.class);
        snowball.setVelocity(player.getEyeLocation().getDirection().multiply(1.7D));
        projectileSkills.put(snowball.getUniqueId(), ProjectileSkill.CLAW_WEB);
        player.playSound(player.getLocation(), Sound.ENTITY_SPIDER_AMBIENT, 0.8F, 1.2F);
    }

    private void handleDestroyer(PlayerInteractEvent event, Player player, ItemStack item) {
        if (!event.getAction().isRightClick() || item.getType() != Material.CREEPER_HEAD) {
            return;
        }
        event.setCancelled(true);
        if (onCooldown(player, "destroyer_blast", skillConfig.cooldownMillis(HeroClass.DESTROYER, "destroyer_blast", 16.0D), "爆破头")) {
            return;
        }
        damageNearbyPlayers(
                player,
                skillConfig.value(HeroClass.DESTROYER, "destroyer_blast", "radius", 4.5D),
                skillConfig.value(HeroClass.DESTROYER, "destroyer_blast", "damage", 6.0D)
        );
        player.getWorld().spawnParticle(Particle.EXPLOSION, player.getLocation(), 2, 0.4D, 0.2D, 0.4D, 0.0D);
        player.playSound(player.getLocation(), Sound.ENTITY_GENERIC_EXPLODE, 0.9F, 1.0F);
    }

    private void handleSummoner(PlayerInteractEvent event, Player player, ItemStack item) {
        if (!event.getAction().isRightClick() || item.getType() != Material.IRON_BLOCK) {
            return;
        }
        event.setCancelled(true);
        if (onCooldown(player, "summoner_golem", skillConfig.cooldownMillis(HeroClass.SUMMONER, "summoner_golem", 24.0D), "召铁")) {
            return;
        }
        clearSummons(player);
        IronGolem golem = player.getWorld().spawn(player.getLocation(), IronGolem.class);
        golem.setPlayerCreated(true);
        golem.addPassenger(player);
        registerSummon(player, golem);
        player.playSound(player.getLocation(), Sound.ENTITY_IRON_GOLEM_REPAIR, 1.0F, 1.0F);
    }

    private void handleInfernoGuard(PlayerInteractEvent event, Player player, ItemStack item) {
        if (!event.getAction().isRightClick() || item.getType() != Material.BONE) {
            return;
        }
        event.setCancelled(true);
        if (onCooldown(player, "inferno_hounds", skillConfig.cooldownMillis(HeroClass.INFERNO_GUARD, "inferno_hounds", 28.0D), "炎狼")) {
            return;
        }
        clearSummons(player);
        int count = skillConfig.intValue(HeroClass.INFERNO_GUARD, "inferno_hounds", "count", 3);
        for (int i = 0; i < count; i++) {
            Wolf wolf = player.getWorld().spawn(player.getLocation(), Wolf.class);
            wolf.setOwner(player);
            wolf.setCustomName("地狱猎犬");
            wolf.addPotionEffect(new PotionEffect(PotionEffectType.FIRE_RESISTANCE, 20 * 20, 0));
            wolf.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 20 * 20, 0));
            registerSummon(player, wolf);
        }
        player.playSound(player.getLocation(), Sound.ENTITY_WOLF_AMBIENT, 1.0F, 0.7F);
    }

    private void handleThug(PlayerInteractEvent event, Player player, ItemStack item) {
        if (!event.getAction().isRightClick() || item.getType() != Material.IRON_BARS) {
            return;
        }
        event.setCancelled(true);
        Player target = findNearestTarget(player, skillConfig.value(HeroClass.THUG, "thug_burst", "range", 8.0D));
        if (target == null) {
            return;
        }
        if (onCooldown(player, "thug_burst", skillConfig.cooldownMillis(HeroClass.THUG, "thug_burst", 14.0D), "熔拳")) {
            return;
        }
        target.getWorld().spawnParticle(Particle.LAVA, target.getLocation().add(0.0D, 1.0D, 0.0D), 24, 0.6D, 0.8D, 0.6D, 0.02D);
        target.damage(skillConfig.value(HeroClass.THUG, "thug_burst", "damage", 7.0D), player);
        player.playSound(player.getLocation(), Sound.BLOCK_LAVA_POP, 1.0F, 1.0F);
    }

    private void handlePhantom(PlayerInteractEvent event, Player player, ItemStack item) {
        if (!event.getAction().isRightClick() || item.getType() != Material.BOOK) {
            return;
        }
        event.setCancelled(true);
        if (onCooldown(player, "phantom_flight", skillConfig.cooldownMillis(HeroClass.PHANTOM, "phantom_flight", 20.0D), "幻行")) {
            return;
        }
        clearFlight(player);
        player.setAllowFlight(true);
        player.setFlying(true);
        player.playSound(player.getLocation(), Sound.ENTITY_PHANTOM_FLAP, 0.8F, 1.2F);
        startFlight(player, Math.round(skillConfig.value(HeroClass.PHANTOM, "phantom_flight", "duration-seconds", 4.0D) * 20.0D));
    }

    private void handleShackle(PlayerInteractEvent event, Player player, ItemStack item) {
        if (!event.getAction().isRightClick() || item.getType() != Material.BELL) {
            return;
        }
        event.setCancelled(true);
        if (onCooldown(player, "shackle_bell", skillConfig.cooldownMillis(HeroClass.SHACKLE, "shackle_bell", 18.0D), "冰钟")) {
            return;
        }
        double radius = skillConfig.value(HeroClass.SHACKLE, "shackle_bell", "radius", 6.0D);
        int durationTicks = (int) Math.round(skillConfig.value(HeroClass.SHACKLE, "shackle_bell", "duration-seconds", 3.0D) * 20.0D);
        int amplifier = skillConfig.intValue(HeroClass.SHACKLE, "shackle_bell", "amplifier", 4);
        for (Entity entity : player.getNearbyEntities(radius, radius, radius)) {
            if (entity instanceof Player target && !target.equals(player) && !gameService.isInSafeZone(target)) {
                target.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, durationTicks, amplifier, false, true));
            }
        }
        player.playSound(player.getLocation(), Sound.BLOCK_BELL_USE, 1.0F, 0.8F);
    }

    private void handleCavalry(PlayerInteractEvent event, Player player, ItemStack item) {
        if (!event.getAction().isRightClick() || item.getType() != Material.DIAMOND_HORSE_ARMOR) {
            return;
        }
        event.setCancelled(true);
        if (onCooldown(player, "cavalry_horse", skillConfig.cooldownMillis(HeroClass.CAVALRY, "cavalry_horse", 20.0D), "骑士")) {
            return;
        }
        clearSummons(player);
        Horse horse = player.getWorld().spawn(player.getLocation(), Horse.class);
        horse.setTamed(true);
        horse.setOwner(player);
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
        event.setCancelled(true);
        if (onCooldown(player, "achilles_leap", skillConfig.cooldownMillis(HeroClass.ACHILLES, "achilles_leap", 12.0D), "飞焰")) {
            return;
        }
        Vector launch = player.getLocation().getDirection().normalize()
                .multiply(skillConfig.value(HeroClass.ACHILLES, "achilles_leap", "horizontal-strength", 1.35D))
                .setY(skillConfig.value(HeroClass.ACHILLES, "achilles_leap", "vertical-strength", 0.9D));
        player.setVelocity(launch);
        player.setFallDistance(0.0F);
        player.playSound(player.getLocation(), Sound.ENTITY_FIREWORK_ROCKET_LAUNCH, 1.0F, 1.1F);
    }

    private void handleDragonBreath(PlayerInteractEvent event, Player player, ItemStack item) {
        if (!event.getAction().isRightClick() || item.getType() != Material.FIRE_CHARGE) {
            return;
        }
        event.setCancelled(true);
        if (onCooldown(player, "dragon_breath", skillConfig.cooldownMillis(HeroClass.DRAGON_BREATH, "dragon_breath", 20.0D), "龙炎")) {
            return;
        }
        double radius = skillConfig.value(HeroClass.DRAGON_BREATH, "dragon_breath", "radius", 3.5D);
        double damage = skillConfig.value(HeroClass.DRAGON_BREATH, "dragon_breath", "damage", 7.0D);
        int fireTicks = (int) Math.round(skillConfig.value(HeroClass.DRAGON_BREATH, "dragon_breath", "fire-seconds", 5.0D) * 20.0D);
        for (Entity entity : player.getNearbyEntities(radius, radius, radius)) {
            if (entity instanceof Player target && !target.equals(player) && !gameService.isInSafeZone(target)) {
                target.damage(damage, player);
                target.setFireTicks(fireTicks);
            }
        }
        player.playSound(player.getLocation(), Sound.ENTITY_GHAST_SHOOT, 1.0F, 1.0F);
    }

    private void handleAsura(PlayerInteractEvent event, Player player, ItemStack item) {
        if (!event.getAction().isRightClick() || item.getType() != Material.SLIME_BALL) {
            return;
        }
        event.setCancelled(true);
        Player target = findRandomTarget(player, skillConfig.value(HeroClass.ASURA, "asura_step", "range", 12.0D));
        if (target == null) {
            return;
        }
        if (onCooldown(player, "asura_step", skillConfig.cooldownMillis(HeroClass.ASURA, "asura_step", 16.0D), "修罗")) {
            return;
        }
        player.teleport(target.getLocation().clone().add(1.0D, 0.0D, 0.0D));
        player.playSound(player.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 1.0F, 1.0F);
    }

    private void handleToxicLizard(PlayerInteractEvent event, Player player, ItemStack item) {
        if (!event.getAction().isRightClick() || item.getType() != Material.ENDER_EYE) {
            return;
        }
        event.setCancelled(true);
        if (onCooldown(player, "toxic_wave", skillConfig.cooldownMillis(HeroClass.TOXIC_LIZARD, "toxic_wave", 16.0D), "毒蜥")) {
            return;
        }
        int durationTicks = (int) Math.round(skillConfig.value(HeroClass.TOXIC_LIZARD, "toxic_wave", "duration-seconds", 5.0D) * 20.0D);
        int amplifier = skillConfig.intValue(HeroClass.TOXIC_LIZARD, "toxic_wave", "amplifier", 0);
        for (Entity entity : player.getNearbyEntities(DEFAULT_SKILL_RANGE, DEFAULT_SKILL_RANGE, DEFAULT_SKILL_RANGE)) {
            if (entity instanceof Player target && !target.equals(player) && !gameService.isInSafeZone(target)) {
                target.addPotionEffect(new PotionEffect(PotionEffectType.POISON, durationTicks, amplifier, false, true));
            }
        }
        player.playSound(player.getLocation(), Sound.ENTITY_SILVERFISH_HURT, 1.0F, 0.8F);
    }

    private void handleSacredWar(PlayerInteractEvent event, Player player, ItemStack item) {
        if (!event.getAction().isRightClick() || item.getType() != Material.GOLD_NUGGET) {
            return;
        }
        event.setCancelled(true);
        if (onCooldown(player, "sacred_war_guard", skillConfig.cooldownMillis(HeroClass.SACRED_WAR, "sacred_war_guard", 15.0D), "圣战")) {
            return;
        }
        player.setAbsorptionAmount(Math.max(player.getAbsorptionAmount(),
                skillConfig.value(HeroClass.SACRED_WAR, "sacred_war_guard", "absorption", 4.0D)));
        player.playSound(player.getLocation(), Sound.ITEM_TOTEM_USE, 0.9F, 1.25F);
        player.playSound(player.getLocation(), Sound.BLOCK_AMETHYST_BLOCK_RESONATE, 0.6F, 1.5F);
    }

    private void handleWindwalker(PlayerInteractEvent event, Player player, ItemStack item) {
        if (!event.getAction().isRightClick()) {
            return;
        }

        if (item.getType() == Material.FEATHER) {
            event.setCancelled(true);
            if (onCooldown(player, "windwalker_rise", skillConfig.cooldownMillis(HeroClass.WINDWALKER, "windwalker_rise", 30.0D), "风行·升空")) {
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
            if (onCooldown(player, "windwalker_dash", skillConfig.cooldownMillis(HeroClass.WINDWALKER, "windwalker_dash", 30.0D), "风行·突进")) {
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
        if (!event.getAction().isRightClick() || item.getType() != Material.PHANTOM_MEMBRANE) {
            return;
        }
        event.setCancelled(true);
        if (onCooldown(player, "homelander_flight", skillConfig.cooldownMillis(HeroClass.HOMELANDER, "homelander_flight", 30.0D), "祖国人")) {
            return;
        }
        clearFlight(player);
        player.setAllowFlight(true);
        player.setFlying(true);
        player.setFallDistance(0.0F);
        player.playSound(player.getLocation(), Sound.ENTITY_ENDER_DRAGON_FLAP, 1.0F, 1.2F);
        startFlight(player, Math.round(skillConfig.value(HeroClass.HOMELANDER, "homelander_flight", "duration-seconds", 8.0D) * 20.0D));
    }

    private void handleTide(PlayerInteractEvent event, Player player, ItemStack item) {
        if (!event.getAction().isRightClick() || item.getType() != Material.NAUTILUS_SHELL) {
            return;
        }
        event.setCancelled(true);
        if (onCooldown(player, "tide_pull", skillConfig.cooldownMillis(HeroClass.TIDE, "tide_pull", 22.0D), "潮汐")) {
            return;
        }
        Player target = findNearestTarget(player, skillConfig.value(HeroClass.TIDE, "tide_pull", "range", 5.0D));
        if (target == null) {
            return;
        }
        Vector pull = player.getLocation().toVector().subtract(target.getLocation().toVector()).normalize()
                .multiply(skillConfig.value(HeroClass.TIDE, "tide_pull", "pull-strength", 1.15D));
        pull.setY(0.25D);
        target.setVelocity(pull);
        target.damage(skillConfig.value(HeroClass.TIDE, "tide_pull", "damage", 3.0D), player);
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
        event.setCancelled(true);
        if (stateTracker.hasPrism(player)) {
            return;
        }
        if (onCooldown(player, "prism_stance", skillConfig.cooldownMillis(HeroClass.PRISM, "prism_stance", 28.0D), "棱镜")) {
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
        event.setCancelled(true);
        if (onCooldown(player, "geomancer_quake", skillConfig.cooldownMillis(HeroClass.GEOMANCER, "geomancer_quake", 20.0D), "地脉")) {
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
                if (entity instanceof Player target && !target.equals(player) && !gameService.isInSafeZone(target)) {
                    target.damage(skillConfig.value(HeroClass.GEOMANCER, "geomancer_quake", "damage", 4.0D), player);
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
        event.setCancelled(true);
        if (stateTracker.hasTimeAnchor(player)) {
            returnToAnchor(player);
            return;
        }
        if (onCooldown(player, "rift_anchor", skillConfig.cooldownMillis(HeroClass.RIFT, "rift_anchor", 30.0D), "时隙")) {
            return;
        }
        stateTracker.setTimeAnchor(player, player.getLocation().clone(),
                Math.round(skillConfig.value(HeroClass.RIFT, "rift_anchor", "anchor-seconds", 6.0D) * 20.0D),
                () -> {
                    if (player.isOnline()) {
                        returnToAnchor(player);
                    }
                });
        player.sendMessage(Branding.PREFIX + "时隙已记录当前位置。");
        player.playSound(player.getLocation(), Sound.BLOCK_RESPAWN_ANCHOR_SET_SPAWN, 0.8F, 1.2F);
    }

    private void handleFrostmark(PlayerInteractEvent event, Player player, ItemStack item) {
        if (!event.getAction().isRightClick() || item.getType() != Material.SNOWBALL) {
            return;
        }
        event.setCancelled(true);
        if (onCooldown(player, "frostmark_breath", skillConfig.cooldownMillis(HeroClass.FROSTMARK, "frostmark_breath", 18.0D), "霜痕")) {
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
                target.damage(damage, player);
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
        event.setCancelled(true);
        if (onCooldown(player, "razor_dash", skillConfig.cooldownMillis(HeroClass.RAZOR, "razor_dash", 25.0D), "裂锋")) {
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
            target.damage(skillConfig.value(HeroClass.RAZOR, "razor_dash", "damage", 5.0D), player);
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
        double maxHealth = player.getAttribute(org.bukkit.attribute.Attribute.MAX_HEALTH) != null
                ? player.getAttribute(org.bukkit.attribute.Attribute.MAX_HEALTH).getValue()
                : 20.0D;
        if (player.getHealth() >= maxHealth) {
            return;
        }
        event.setCancelled(true);
        player.setHealth(Math.min(maxHealth, player.getHealth() + (maxHealth * 0.35D)));
        stew.setAmount(Math.max(0, stew.getAmount() - 1));
        player.playSound(player.getLocation(), Sound.ENTITY_GENERIC_DRINK, 1.0F, 1.0F);
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
            if (entity instanceof Player target && !target.equals(player) && !gameService.isInSafeZone(target)) {
                target.damage(damage, player);
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

    private boolean onCooldown(Player player, String key, long cooldownMillis, String readyName) {
        long remaining = gameService.tryUseCooldown(player, key, cooldownMillis);
        if (remaining > 0L) {
            long seconds = (remaining + 999L) / 1000L;
            player.sendMessage(Branding.PREFIX + "技能冷却：§b" + seconds + "§f秒");
            return true;
        }
        stateTracker.scheduleCooldownReady(player, key, readyName, Math.max(1L, cooldownMillis / 50L));
        return false;
    }

    private boolean isInterceptableProjectile(Entity entity) {
        if (!(entity instanceof Projectile projectile)) {
            return false;
        }
        return projectile instanceof Arrow
                || projectile instanceof Snowball
                || entity.getType().name().contains("FISHING")
                || entity.getType().name().contains("WIND_CHARGE");
    }

    private void damageNearbyPlayers(Player player, double radius, double damage) {
        for (Entity entity : player.getNearbyEntities(radius, radius, radius)) {
            if (entity instanceof Player target && !target.equals(player) && !gameService.isInSafeZone(target)) {
                target.damage(damage, player);
            }
        }
    }

    private void damageNearbyPlayersAt(Player source, Location center, double radius, double damage) {
        for (Entity entity : center.getWorld().getNearbyEntities(center, radius, radius, radius)) {
            if (entity instanceof Player target && !target.equals(source) && !gameService.isInSafeZone(target)) {
                target.damage(damage, source);
            }
        }
    }

    private Player findNearestTarget(Player player, double radius) {
        Player nearest = null;
        double nearestDistance = Double.MAX_VALUE;
        for (Entity entity : player.getNearbyEntities(radius, radius, radius)) {
            if (!(entity instanceof Player target) || target.equals(player) || gameService.isInSafeZone(target)) {
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
            if (entity instanceof Player target && !target.equals(player) && !gameService.isInSafeZone(target)) {
                targets.add(target);
            }
        }
        if (targets.isEmpty()) {
            return null;
        }
        return targets.get((int) (Math.random() * targets.size()));
    }

    private void registerSummon(Player player, LivingEntity entity) {
        entity.setMetadata(SUMMON_OWNER_METADATA, new FixedMetadataValue(plugin, player.getUniqueId().toString()));
        summonedEntities.computeIfAbsent(player.getUniqueId(), ignored -> new ArrayList<>()).add(entity.getUniqueId());
    }

    private void clearSummons(Player player) {
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

    private void startFlight(Player player, long durationTicks) {
        clearFlight(player);
        BukkitTask task = Bukkit.getScheduler().runTaskLater(plugin, () -> clearFlight(player), durationTicks);
        flightTasks.put(player.getUniqueId(), task);
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

    private enum ProjectileSkill {
        MAGE_SWAP,
        CLAW_WEB
    }
}
