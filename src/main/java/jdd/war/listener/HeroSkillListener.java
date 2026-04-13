package jdd.war.listener;

import jdd.war.game.GameService;
import jdd.war.hero.HeroSkillHandler;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;

public final class HeroSkillListener implements Listener {
    private final GameService gameService;
    private final HeroSkillHandler heroSkillHandler;

    public HeroSkillListener(GameService gameService, HeroSkillHandler heroSkillHandler) {
        this.gameService = gameService;
        this.heroSkillHandler = heroSkillHandler;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onInteract(PlayerInteractEvent event) {
        if (gameService.isParticipant(event.getPlayer())) {
            heroSkillHandler.handleInteract(event);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = false)
    public void onDamage(EntityDamageEvent event) {
        heroSkillHandler.handleDamage(event);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBowShoot(EntityShootBowEvent event) {
        heroSkillHandler.handleBowShoot(event);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onProjectileLaunch(ProjectileLaunchEvent event) {
        heroSkillHandler.handleProjectileLaunch(event);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onProjectileHit(ProjectileHitEvent event) {
        heroSkillHandler.handleProjectileHit(event);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onDamageByEntity(EntityDamageByEntityEvent event) {
        heroSkillHandler.handleDamageByPlayer(event);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onFish(PlayerFishEvent event) {
        heroSkillHandler.handleFish(event);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onSneak(PlayerToggleSneakEvent event) {
        heroSkillHandler.handleSneak(event);
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        heroSkillHandler.clearPlayerState(event.getPlayer());
    }

    @EventHandler
    public void onDeath(PlayerDeathEvent event) {
        heroSkillHandler.clearPlayerState(event.getEntity());
    }
}
