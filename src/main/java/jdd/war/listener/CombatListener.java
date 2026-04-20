package jdd.war.listener;

import java.util.UUID;
import jdd.war.game.GameService;
import jdd.war.hero.HeroSkillHandler;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerMoveEvent;

public final class CombatListener implements Listener {
    private static final String BRAND_PREFIX = "§9§l[职业战争] §f";

    private final GameService gameService;

    public CombatListener(GameService gameService) {
        this.gameService = gameService;
    }

    @EventHandler
    public void onMove(PlayerMoveEvent event) {
        if (event.getTo() == null || !gameService.isParticipant(event.getPlayer())) {
            return;
        }
        if (event.getFrom().getBlockX() == event.getTo().getBlockX()
                && event.getFrom().getBlockY() == event.getTo().getBlockY()
                && event.getFrom().getBlockZ() == event.getTo().getBlockZ()) {
            return;
        }
        if (gameService.updateSafeZoneTracking(event.getPlayer()) && gameService.isInSafeZone(event.getPlayer())) {
            gameService.armFallProtection(event.getPlayer());
        }
    }

    @EventHandler
    public void onFoodChange(FoodLevelChangeEvent event) {
        if (event.getEntity() instanceof Player player && gameService.isParticipant(player) && gameService.isInBrawlWorld(player)) {
            event.setCancelled(true);
            player.setFoodLevel(20);
        }
    }

    @EventHandler
    public void onEntityDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player player) || !gameService.isParticipant(player) || event.isCancelled()) {
            return;
        }

        if (gameService.isInSafeZone(player)) {
            event.setCancelled(true);
            return;
        }

        if (event.getCause() == EntityDamageEvent.DamageCause.FALL && gameService.consumeFallProtection(player)) {
            event.setCancelled(true);
            player.sendMessage(BRAND_PREFIX + "安全区摔落保护已触发。");
        }
    }

    @EventHandler
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        if (!(event.getEntity() instanceof Player victim)) {
            return;
        }

        Player attacker = null;
        if (event.getDamager() instanceof Player player) {
            attacker = player;
        } else if (event.getDamager() instanceof Arrow arrow && arrow.getShooter() instanceof Player player) {
            attacker = player;
        } else {
            attacker = resolveSummonOwner(event.getDamager());
        }

        if (attacker == null || attacker.equals(victim)) {
            return;
        }

        if (!gameService.isParticipant(attacker) || !gameService.isParticipant(victim)) {
            event.setCancelled(true);
            return;
        }

        if (gameService.isInSafeZone(attacker) || gameService.isInSafeZone(victim)) {
            attacker.sendMessage(BRAND_PREFIX + "安全区内不能攻击。");
            event.setCancelled(true);
            return;
        }

        gameService.tagCombat(victim, attacker);
    }

    @EventHandler
    public void onDeath(PlayerDeathEvent event) {
        gameService.handleBattleDeath(event);
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        if (gameService.isParticipant(event.getPlayer()) && event.getPlayer().getGameMode() != GameMode.CREATIVE) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        if (gameService.isParticipant(event.getPlayer()) && event.getPlayer().getGameMode() != GameMode.CREATIVE) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onDrop(PlayerDropItemEvent event) {
        if (gameService.isParticipant(event.getPlayer())) {
            event.setCancelled(true);
        }
    }

    private Player resolveSummonOwner(Entity damager) {
        if (!damager.hasMetadata(HeroSkillHandler.SUMMON_OWNER_METADATA)) {
            return null;
        }

        String ownerId = damager.getMetadata(HeroSkillHandler.SUMMON_OWNER_METADATA).get(0).asString();
        try {
            return Bukkit.getPlayer(UUID.fromString(ownerId));
        } catch (IllegalArgumentException exception) {
            return null;
        }
    }
}
