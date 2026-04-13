package jdd.war.listener;

import com.destroystokyo.paper.event.player.PlayerPickupExperienceEvent;
import io.papermc.paper.event.player.PlayerPickItemEvent;
import jdd.war.bootstrap.PluginConfigManager;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerMoveEvent;

public final class LobbyProtectionListener implements Listener {
    private final PluginConfigManager pluginConfigManager;

    public LobbyProtectionListener(PluginConfigManager pluginConfigManager) {
        this.pluginConfigManager = pluginConfigManager;
    }

    @EventHandler
    public void onLobbyDamage(EntityDamageEvent event) {
        if (event.getEntity() instanceof Player player && isLobbyWorld(player.getWorld()) && pluginConfigManager.isInvulnerableInLobby()) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        if (isLobbyWorld(player.getWorld()) && player.getLocation().getY() < -30.0D) {
            player.teleport(pluginConfigManager.getLobbyLocation());
        }
    }

    @EventHandler
    public void onItemDrop(PlayerDropItemEvent event) {
        if (isLobbyWorld(event.getPlayer().getWorld()) && !pluginConfigManager.canDropItemsInLobby()) {
            event.setCancelled(true);
            sendBlockedMessage(event.getPlayer(), "大厅里不能丢弃物品。");
        }
    }

    @EventHandler
    public void onItemPick(PlayerPickItemEvent event) {
        if (isLobbyWorld(event.getPlayer().getWorld()) && !pluginConfigManager.canPickUpItemsInLobby()) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onFoodChange(FoodLevelChangeEvent event) {
        if (event.getEntity() instanceof Player player && isLobbyWorld(player.getWorld())) {
            event.setCancelled(true);
            player.setFoodLevel(20);
        }
    }

    @EventHandler
    public void onPlaceBlock(BlockPlaceEvent event) {
        if (isLobbyWorld(event.getPlayer().getWorld()) && !pluginConfigManager.canPlaceBlocksInLobby()) {
            event.setCancelled(true);
            sendBlockedMessage(event.getPlayer(), "大厅里不能放置方块。");
        }
    }

    @EventHandler
    public void onBreakBlock(BlockBreakEvent event) {
        if (isLobbyWorld(event.getPlayer().getWorld()) && !pluginConfigManager.canBreakBlocksInLobby()) {
            event.setCancelled(true);
            sendBlockedMessage(event.getPlayer(), "大厅里不能破坏方块。");
        }
    }

    @EventHandler
    public void onExpPickup(PlayerPickupExperienceEvent event) {
        if (isLobbyWorld(event.getPlayer().getWorld()) && !pluginConfigManager.canPickupExpOrbsInLobby()) {
            event.setCancelled(true);
        }
    }

    private boolean isLobbyWorld(World world) {
        return world.equals(pluginConfigManager.getLobbyLocation().getWorld());
    }

    private void sendBlockedMessage(Player player, String message) {
        if (pluginConfigManager.sendActionFailedMessageInLobby()) {
            player.sendMessage("§c" + message);
        }
    }
}
