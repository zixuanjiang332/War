package jdd.war.hero;

import jdd.war.gui.ClassSelectorGUI;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;

public final class HeroService {
    private final HeroRegistry heroRegistry;

    public HeroService(HeroRegistry heroRegistry) {
        this.heroRegistry = heroRegistry;
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
        ClassSelectorGUI.open(player, heroRegistry);
    }

    public String getHeroName(HeroClass heroClass) {
        return heroRegistry.get(heroClass).getDisplayName();
    }

    private void restoreHealth(Player player) {
        AttributeInstance maxHealth = player.getAttribute(Attribute.MAX_HEALTH);
        double amount = maxHealth != null ? maxHealth.getValue() : 20.0D;
        player.setHealth(amount);
    }
}
