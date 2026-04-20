package jdd.war.listener;

import io.papermc.paper.event.player.AsyncChatEvent;
import jdd.war.data.PlayerDataService;
import jdd.war.game.PlayerTier;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

public final class ChatListener implements Listener {
    private final PlayerDataService playerDataService;

    public ChatListener(PlayerDataService playerDataService) {
        this.playerDataService = playerDataService;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onChat(AsyncChatEvent event) {
        Player player = event.getPlayer();
        PlayerTier tier = PlayerTier.fromKills(playerDataService.getOrCreate(player).getKills());
        Component prefix = LegacyComponentSerializer.legacySection().deserialize(tier.getPrefix());

        event.renderer((source, sourceDisplayName, message, viewer) -> prefix
                .append(Component.text(player.getName()))
                .append(Component.text(" §8» §f"))
                .append(message));
    }
}
