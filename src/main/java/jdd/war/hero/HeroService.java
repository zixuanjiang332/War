package jdd.war.hero;

import jdd.war.data.PlayerDataService;
import jdd.war.gui.ClassSelectorGUI;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;

public final class HeroService {
    private final HeroRegistry heroRegistry;
    private final PlayerDataService playerDataService;

    public HeroService(HeroRegistry heroRegistry, PlayerDataService playerDataService) {
        this.heroRegistry = heroRegistry;
        this.playerDataService = playerDataService;
    }

    public void assignHero(Player player, HeroClass heroClass) {
        clearHero(player);
        heroRegistry.get(heroClass).apply(player);
        player.setFoodLevel(20);
        restoreHealth(player);
    }

    public void clearHero(Player player) {
        player.getInventory().clear();
        player.setFireTicks(0);
        player.setGameMode(GameMode.SURVIVAL);

        for (PotionEffect effect : player.getActivePotionEffects()) {
            player.removePotionEffect(effect.getType());
        }

        AttributeInstance maxHealth = player.getAttribute(Attribute.MAX_HEALTH);
        if (maxHealth != null) {
            maxHealth.setBaseValue(20.0D);
        }
        AttributeInstance scale = player.getAttribute(Attribute.SCALE);
        if (scale != null) {
            scale.setBaseValue(1.0D);
        }
        AttributeInstance interactionRange = player.getAttribute(Attribute.ENTITY_INTERACTION_RANGE);
        if (interactionRange != null) {
            interactionRange.setBaseValue(3.0D);
        }

        restoreHealth(player);
        player.setFoodLevel(20);
    }

    public void giveLobbyItems(Player player) {
        player.getInventory().setItem(4, HeroItems.named(Material.COMPASS, "职业战争"));
    }

    public void giveHeroSelectorItem(Player player) {
        player.getInventory().setItem(4, HeroItems.named(Material.CHEST, "选择职业"));
    }

    public void openSelector(Player player) {
        ClassSelectorGUI.open(player, heroRegistry, playerDataService.getOrCreate(player).getKills());
    }

    public String getHeroName(HeroClass heroClass) {
        return heroRegistry.get(heroClass).getDisplayName();
    }

    public HeroTier getHeroTier(HeroClass heroClass) {
        return heroRegistry.get(heroClass).getTier();
    }

    private void restoreHealth(Player player) {
        AttributeInstance maxHealth = player.getAttribute(Attribute.MAX_HEALTH);
        double amount = maxHealth != null ? maxHealth.getValue() : 20.0D;
        player.setHealth(amount);
    }
}
