package jdd.war.hero;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import jdd.war.War;
import jdd.war.game.GameService;
import jdd.war.game.PlayerSession;
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
import org.bukkit.util.Vector;

public final class HeroSkillHandler {
    private static final double DEFAULT_SKILL_RANGE = 5.0D;
    public static final String SUMMON_OWNER_METADATA = "class_war_summon_owner";

    private final War plugin;
    private final GameService gameService;
    private final Map<UUID, ProjectileSkill> projectileSkills = new ConcurrentHashMap<>();
    private final Map<UUID, List<UUID>> summonedEntities = new ConcurrentHashMap<>();
    private final Map<UUID, BukkitTask> flightTasks = new ConcurrentHashMap<>();

    public HeroSkillHandler(War plugin, GameService gameService) {
        this.plugin = plugin;
        this.gameService = gameService;
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
        if (onCooldown(player, "birdman_dash", 10_000L)) {
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
        if (!(event.getDamager() instanceof Player player) || !gameService.isParticipant(player) || gameService.isInSafeZone(player)) {
            return;
        }
        if (gameService.getSelectedHero(player) != HeroClass.THOR || player.getInventory().getItemInMainHand().getType() != Material.WOODEN_AXE) {
            return;
        }
        if (onCooldown(player, "thor_lightning", 14_000L)) {
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
        if (onCooldown(player, "claw_web", 12_000L)) {
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
        if (onCooldown(player, "destroyer_blast", 16_000L)) {
            return;
        }
        damageNearbyPlayers(player, 4.5D, 6.0D);
        player.getWorld().spawnParticle(Particle.EXPLOSION, player.getLocation(), 2, 0.4D, 0.2D, 0.4D, 0.0D);
        player.playSound(player.getLocation(), Sound.ENTITY_GENERIC_EXPLODE, 0.9F, 1.0F);
    }

    private void handleSummoner(PlayerInteractEvent event, Player player, ItemStack item) {
        if (!event.getAction().isRightClick() || item.getType() != Material.IRON_BLOCK) {
            return;
        }
        event.setCancelled(true);
        if (onCooldown(player, "summoner_golem", 24_000L)) {
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
        if (onCooldown(player, "inferno_hounds", 28_000L)) {
            return;
        }
        clearSummons(player);
        for (int i = 0; i < 3; i++) {
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
        Player target = findNearestTarget(player, 8.0D);
        if (target == null) {
            return;
        }
        if (onCooldown(player, "thug_burst", 14_000L)) {
            return;
        }
        target.getWorld().spawnParticle(Particle.LAVA, target.getLocation().add(0.0D, 1.0D, 0.0D), 24, 0.6D, 0.8D, 0.6D, 0.02D);
        target.damage(7.0D, player);
        player.playSound(player.getLocation(), Sound.BLOCK_LAVA_POP, 1.0F, 1.0F);
    }

    private void handlePhantom(PlayerInteractEvent event, Player player, ItemStack item) {
        if (!event.getAction().isRightClick() || item.getType() != Material.BOOK) {
            return;
        }
        event.setCancelled(true);
        if (onCooldown(player, "phantom_flight", 20_000L)) {
            return;
        }
        clearFlight(player);
        player.setAllowFlight(true);
        player.setFlying(true);
        player.playSound(player.getLocation(), Sound.ENTITY_PHANTOM_FLAP, 0.8F, 1.2F);
        BukkitTask task = Bukkit.getScheduler().runTaskLater(plugin, () -> clearFlight(player), 80L);
        flightTasks.put(player.getUniqueId(), task);
    }

    private void handleShackle(PlayerInteractEvent event, Player player, ItemStack item) {
        if (!event.getAction().isRightClick() || item.getType() != Material.BELL) {
            return;
        }
        event.setCancelled(true);
        if (onCooldown(player, "shackle_bell", 18_000L)) {
            return;
        }
        for (Entity entity : player.getNearbyEntities(6.0D, 6.0D, 6.0D)) {
            if (entity instanceof Player target && !target.equals(player) && !gameService.isInSafeZone(target)) {
                target.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 60, 4, false, true));
            }
        }
        player.playSound(player.getLocation(), Sound.BLOCK_BELL_USE, 1.0F, 0.8F);
    }

    private void handleCavalry(PlayerInteractEvent event, Player player, ItemStack item) {
        if (!event.getAction().isRightClick() || item.getType() != Material.DIAMOND_HORSE_ARMOR) {
            return;
        }
        event.setCancelled(true);
        if (onCooldown(player, "cavalry_horse", 20_000L)) {
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
        if (onCooldown(player, "achilles_leap", 12_000L)) {
            return;
        }
        Vector launch = player.getLocation().getDirection().normalize().multiply(1.35D).setY(0.9D);
        player.setVelocity(launch);
        player.setFallDistance(0.0F);
        player.playSound(player.getLocation(), Sound.ENTITY_FIREWORK_ROCKET_LAUNCH, 1.0F, 1.1F);
    }

    private void handleDragonBreath(PlayerInteractEvent event, Player player, ItemStack item) {
        if (!event.getAction().isRightClick() || item.getType() != Material.FIRE_CHARGE) {
            return;
        }
        event.setCancelled(true);
        if (onCooldown(player, "dragon_breath", 20_000L)) {
            return;
        }
        for (Entity entity : player.getNearbyEntities(3.5D, 3.0D, 3.5D)) {
            if (entity instanceof Player target && !target.equals(player) && !gameService.isInSafeZone(target)) {
                target.damage(7.0D, player);
                target.setFireTicks(100);
            }
        }
        player.playSound(player.getLocation(), Sound.ENTITY_GHAST_SHOOT, 1.0F, 1.0F);
    }

    private void handleAsura(PlayerInteractEvent event, Player player, ItemStack item) {
        if (!event.getAction().isRightClick() || item.getType() != Material.SLIME_BALL) {
            return;
        }
        event.setCancelled(true);
        Player target = findRandomTarget(player, 12.0D);
        if (target == null) {
            return;
        }
        if (onCooldown(player, "asura_step", 16_000L)) {
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
        if (onCooldown(player, "toxic_wave", 16_000L)) {
            return;
        }
        for (Entity entity : player.getNearbyEntities(DEFAULT_SKILL_RANGE, DEFAULT_SKILL_RANGE, DEFAULT_SKILL_RANGE)) {
            if (entity instanceof Player target && !target.equals(player) && !gameService.isInSafeZone(target)) {
                target.addPotionEffect(new PotionEffect(PotionEffectType.POISON, 100, 0, false, true));
            }
        }
        player.playSound(player.getLocation(), Sound.ENTITY_SILVERFISH_HURT, 1.0F, 0.8F);
    }

    private void handleSacredWar(PlayerInteractEvent event, Player player, ItemStack item) {
        if (!event.getAction().isRightClick() || item.getType() != Material.GOLD_NUGGET) {
            return;
        }
        event.setCancelled(true);
        if (onCooldown(player, "sacred_war_guard", 15_000L)) {
            return;
        }
        player.setAbsorptionAmount(Math.max(player.getAbsorptionAmount(), 4.0D));
        player.playSound(player.getLocation(), Sound.ITEM_TOTEM_USE, 0.9F, 1.25F);
        player.playSound(player.getLocation(), Sound.BLOCK_AMETHYST_BLOCK_RESONATE, 0.6F, 1.5F);
    }

    private void handleWindwalker(PlayerInteractEvent event, Player player, ItemStack item) {
        if (!event.getAction().isRightClick()) {
            return;
        }

        if (item.getType() == Material.FEATHER) {
            event.setCancelled(true);
            if (onCooldown(player, "windwalker_rise", 30_000L)) {
                return;
            }
            damageNearbyPlayers(player, 3.0D, 3.0D);
            player.setVelocity(new Vector(0.0D, 1.0D, 0.0D));
            player.setFallDistance(0.0F);
            player.addPotionEffect(new PotionEffect(PotionEffectType.SLOW_FALLING, 40, 0, false, false));
            player.playSound(player.getLocation(), Sound.ENTITY_BREEZE_WIND_BURST, 1.0F, 1.2F);
            return;
        }

        if (item.getType() == Material.SUGAR) {
            event.setCancelled(true);
            if (onCooldown(player, "windwalker_dash", 30_000L)) {
                return;
            }
            Vector launch = player.getLocation().getDirection().setY(0.0D).normalize().multiply(1.6D).setY(0.2D);
            player.setVelocity(launch);
            player.setFallDistance(0.0F);
            player.playSound(player.getLocation(), Sound.ENTITY_WIND_CHARGE_WIND_BURST, 1.0F, 1.0F);
            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                if (player.isOnline() && gameService.isParticipant(player)) {
                    damageNearbyPlayersAt(player, player.getLocation(), 3.0D, 3.0D);
                    player.getWorld().spawnParticle(Particle.CLOUD, player.getLocation().add(0.0D, 0.5D, 0.0D), 18, 0.5D, 0.2D, 0.5D, 0.02D);
                }
            }, 6L);
        }
    }

    private void handleHomelander(PlayerInteractEvent event, Player player, ItemStack item) {
        if (!event.getAction().isRightClick() || item.getType() != Material.PHANTOM_MEMBRANE) {
            return;
        }
        event.setCancelled(true);
        if (onCooldown(player, "homelander_flight", 30_000L)) {
            return;
        }
        clearFlight(player);
        player.setAllowFlight(true);
        player.setFlying(true);
        player.setFallDistance(0.0F);
        player.playSound(player.getLocation(), Sound.ENTITY_ENDER_DRAGON_FLAP, 1.0F, 1.2F);
        BukkitTask task = Bukkit.getScheduler().runTaskLater(plugin, () -> clearFlight(player), 160L);
        flightTasks.put(player.getUniqueId(), task);
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

    private boolean onCooldown(Player player, String key, long cooldownMillis) {
        PlayerSession session = gameService.getOrCreateSession(player);
        long now = System.currentTimeMillis();
        long expireAt = session.getCooldown(key);
        if (expireAt > now) {
            long seconds = (expireAt - now + 999L) / 1000L;
            player.sendMessage("§c技能冷却：还需 " + seconds + " 秒");
            return true;
        }
        session.setCooldown(key, now + cooldownMillis);
        return false;
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
