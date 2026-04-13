package jdd.war.command;

import jdd.war.game.GameService;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public final class LeaveCommand implements CommandExecutor {
    private final GameService gameService;

    public LeaveCommand(GameService gameService) {
        this.gameService = gameService;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("只有玩家可以使用这个命令。");
            return true;
        }
        if (!gameService.isParticipant(player)) {
            player.sendMessage("§c你当前不在职业战争中。");
            return true;
        }
        if (gameService.isInCombat(player)) {
            player.sendMessage("§c战斗中无法离开。");
            return true;
        }
        gameService.leaveBrawl(player);
        return true;
    }
}
