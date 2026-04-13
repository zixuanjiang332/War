package jdd.war.listener;

import jdd.war.game.GameService;
import jdd.war.gui.BrawlMenuGUI;
import jdd.war.gui.BrawlMenuHolder;
import jdd.war.gui.ClassSelectorGUI;
import jdd.war.gui.ClassSelectorHolder;
import jdd.war.hero.HeroClass;
import jdd.war.hero.HeroRegistry;
import jdd.war.hero.HeroService;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

public final class MenuListener implements Listener {
    private static final String BRAND_PREFIX = "§9§l[职业战争] §f";

    private final GameService gameService;
    private final HeroService heroService;
    private final HeroRegistry heroRegistry;

    public MenuListener(GameService gameService, HeroService heroService, HeroRegistry heroRegistry) {
        this.gameService = gameService;
        this.heroService = heroService;
        this.heroRegistry = heroRegistry;
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        if (event.getHand() != EquipmentSlot.HAND) {
            return;
        }

        ItemStack item = event.getItem();
        if (item == null || item.getType() == Material.AIR || !event.getAction().isRightClick()) {
            return;
        }

        Player player = event.getPlayer();
        if (item.getType() == Material.COMPASS) {
            event.setCancelled(true);
            BrawlMenuGUI.open(player);
            return;
        }

        if (item.getType() == Material.CHEST) {
            event.setCancelled(true);
            if (!gameService.isParticipant(player)) {
                player.sendMessage(BRAND_PREFIX + "你当前不在职业战争中。");
                return;
            }
            if (!gameService.isInSafeZone(player)) {
                player.sendMessage(BRAND_PREFIX + "只能在安全区更换职业。");
                return;
            }
            heroService.openSelector(player);
        }
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player) || event.getClickedInventory() == null) {
            return;
        }

        ItemStack clicked = event.getCurrentItem();
        if (clicked == null || clicked.getType() == Material.AIR) {
            return;
        }

        if (event.getView().getTopInventory().getHolder() instanceof BrawlMenuHolder) {
            event.setCancelled(true);
            if (event.getClickedInventory() != event.getView().getTopInventory()) {
                return;
            }
            if (clicked.getType() == Material.DIAMOND_SWORD && gameService.joinBrawl(player)) {
                player.closeInventory();
            }
            return;
        }

        if (!(event.getView().getTopInventory().getHolder() instanceof ClassSelectorHolder)) {
            return;
        }

        event.setCancelled(true);
        if (event.getClickedInventory() != event.getView().getTopInventory()) {
            return;
        }
        if (clicked.getType() == Material.RED_BED) {
            gameService.leaveBrawl(player);
            player.closeInventory();
            return;
        }

        HeroClass heroClass = heroRegistry.findByMenuSlot(event.getRawSlot()).orElse(null);
        if (heroClass != null) {
            gameService.selectHero(player, heroClass);
            player.closeInventory();
        }
    }
}
