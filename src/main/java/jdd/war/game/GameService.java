package jdd.war.game;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import jdd.war.War;
import jdd.war.bootstrap.PluginConfigManager;
import jdd.war.data.PlayerDataService;
import jdd.war.hero.HeroClass;
import jdd.war.hero.HeroService;
import jdd.war.hero.HeroSkillHandler;
import jdd.war.map.MapManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.ItemStack;

public final class GameService {
    private static final double SAFE_ZONE_RADIUS_SQUARED = 15.0D * 15.0D;
    private static final long COMBAT_TAG_MILLIS = 10_000L;
    private static final String BRAND_PREFIX = "§9§l[职业战争] §f";

    private final War plugin;
    private final PluginConfigManager pluginConfigManager;
    private final MapManager mapManager;
    private final HeroService heroService;
    private final PlayerDataService playerDataService;
    private final ScoreboardManager scoreboardManager;
    private final Map<UUID, PlayerSession> sessions = new ConcurrentHashMap<>();
    private final Map<UUID, CombatTag> combatTags = new ConcurrentHashMap<>();

    private HeroSkillHandler heroSkillHandler;

    public GameService(
            War plugin,
            PluginConfigManager pluginConfigManager,
            MapManager mapManager,
            HeroService heroService,
            PlayerDataService playerDataService,
            ScoreboardManager scoreboardManager
    ) {
        this.plugin = plugin;
        this.pluginConfigManager = pluginConfigManager;
        this.mapManager = mapManager;
        this.heroService = heroService;
        this.playerDataService = playerDataService;
        this.scoreboardManager = scoreboardManager;
    }

    public void setHeroSkillHandler(HeroSkillHandler heroSkillHandler) {
        this.heroSkillHandler = heroSkillHandler;
    }

    public PlayerSession getOrCreateSession(Player player) {
        return sessions.computeIfAbsent(player.getUniqueId(), PlayerSession::new);
    }

    public PlayerState getState(Player player) {
        return getOrCreateSession(player).getState();
    }

    public boolean isParticipant(Player player) {
        PlayerState state = getState(player);
        return state == PlayerState.IN_BRAWL || state == PlayerState.RESPAWNING;
    }

    public boolean isInLobby(Player player) {
        return getState(player) == PlayerState.LOBBY;
    }

    public boolean isInBrawlWorld(Player player) {
        return mapManager.getActiveWorld() != null && player.getWorld().equals(mapManager.getActiveWorld());
    }

    public boolean isInSafeZone(Player player) {
        if (!isParticipant(player) || !isInBrawlWorld(player)) {
            return false;
        }
        Location spawn = mapManager.getSpawnLocation();
        return spawn != null && player.getLocation().distanceSquared(spawn) <= SAFE_ZONE_RADIUS_SQUARED;
    }

    public void sendToLobby(Player player) {
        clearCombatTag(player);
        clearAbilityState(player);
        heroService.clearHero(player);

        PlayerSession session = getOrCreateSession(player);
        session.setState(PlayerState.LOBBY);
        session.setSelectedHero(null);
        session.clearCooldowns();
        session.clearFallProtection();

        player.teleport(pluginConfigManager.getLobbyLocation());
        player.setGameMode(GameMode.SURVIVAL);
        player.setFoodLevel(20);
        restoreFullHealth(player);
        heroService.giveLobbyItems(player);
        player.sendMessage(BRAND_PREFIX + "已返回大厅。");
        refreshAllUi();
    }

    public boolean joinBrawl(Player player) {
        Location spawnLocation = mapManager.getSpawnLocation();
        if (spawnLocation == null) {
            player.sendMessage(BRAND_PREFIX + "当前没有可用战场。");
            return false;
        }

        clearCombatTag(player);
        clearAbilityState(player);
        heroService.clearHero(player);

        PlayerSession session = getOrCreateSession(player);
        session.setState(PlayerState.RESPAWNING);
        session.setSelectedHero(null);
        session.clearCooldowns();
        session.clearFallProtection();

        player.teleport(spawnLocation);
        player.setGameMode(GameMode.SURVIVAL);
        player.setFoodLevel(20);
        restoreFullHealth(player);
        heroService.giveHeroSelectorItem(player);
        player.sendMessage(BRAND_PREFIX + "已进入战场，请选择职业。");
        refreshAllUi();
        return true;
    }

    public void leaveBrawl(Player player) {
        sendToLobby(player);
    }

    public void armFallProtection(Player player) {
        if (isParticipant(player) && isInSafeZone(player)) {
            getOrCreateSession(player).armFallProtection();
        }
    }

    public boolean consumeFallProtection(Player player) {
        return getOrCreateSession(player).consumeFallProtection();
    }

    public HeroClass getSelectedHero(Player player) {
        return getOrCreateSession(player).getSelectedHero();
    }

    public String getHeroDisplayName(Player player) {
        HeroClass heroClass = getSelectedHero(player);
        return heroClass == null ? "未选择" : heroService.getHeroName(heroClass);
    }

    public String getStateDisplayName(Player player) {
        return switch (getState(player)) {
            case LOBBY -> "大厅";
            case RESPAWNING -> "待选职业";
            case IN_BRAWL -> "战斗中";
        };
    }

    public String getCurrentMapName() {
        return mapManager.getActiveMapName();
    }

    public String getNextMapName() {
        return mapManager.getNextMapName();
    }

    public String getRotationCountdownText() {
        long remaining = mapManager.getMillisUntilNextRotation();
        long minutes = remaining / 60_000L;
        long seconds = (remaining % 60_000L) / 1_000L;
        return String.format("%02d:%02d", minutes, seconds);
    }

    public int getBattlePlayerCount() {
        int count = 0;
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (isParticipant(player)) {
                count++;
            }
        }
        return count;
    }

    public void selectHero(Player player, HeroClass heroClass) {
        if (!isParticipant(player)) {
            player.sendMessage(BRAND_PREFIX + "你当前不在职业战争中。");
            return;
        }
        if (heroClass == HeroClass.OP_CLASS && !player.isOp()) {
            player.sendMessage(BRAND_PREFIX + "这个职业仅限 OP 使用。");
            return;
        }

        clearAbilityState(player);
        heroService.assignHero(player, heroClass);

        PlayerSession session = getOrCreateSession(player);
        session.setSelectedHero(heroClass);
        session.setState(PlayerState.IN_BRAWL);
        session.clearFallProtection();

        player.sendMessage(BRAND_PREFIX + "已选择职业：§b" + heroService.getHeroName(heroClass));
        refreshAllUi();
    }

    public void handleRespawn(PlayerRespawnEvent event) {
        Player player = event.getPlayer();
        if (!isParticipant(player)) {
            event.setRespawnLocation(pluginConfigManager.getLobbyLocation());
            return;
        }

        Location spawnLocation = mapManager.getSpawnLocation();
        event.setRespawnLocation(spawnLocation != null ? spawnLocation : pluginConfigManager.getLobbyLocation());
        Bukkit.getScheduler().runTask(plugin, () -> prepareRespawn(player));
    }

    public void prepareRespawn(Player player) {
        clearAbilityState(player);

        PlayerSession session = getOrCreateSession(player);
        session.setState(PlayerState.RESPAWNING);
        session.setSelectedHero(null);
        session.clearCooldowns();
        session.clearFallProtection();

        heroService.clearHero(player);
        heroService.giveHeroSelectorItem(player);
        player.sendMessage(BRAND_PREFIX + "请选择新的职业。");

        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            if (player.isOnline() && isParticipant(player) && isInSafeZone(player)) {
                heroService.openSelector(player);
            }
        }, 8L);

        refreshAllUi();
    }

    public void handleBattleDeath(PlayerDeathEvent event) {
        Player victim = event.getEntity();
        if (!isParticipant(victim)) {
            return;
        }

        event.getDrops().clear();
        playerDataService.addDeath(victim);
        clearAbilityState(victim);

        PlayerSession session = getOrCreateSession(victim);
        session.setState(PlayerState.RESPAWNING);
        session.setSelectedHero(null);
        session.clearCooldowns();
        session.clearFallProtection();

        Player killer = getLastAttacker(victim);
        if (killer == null) {
            killer = victim.getKiller();
        }

        clearCombatTag(victim);

        if (killer != null && killer.isOnline() && !killer.equals(victim)) {
            clearCombatTag(killer);
            playerDataService.addKill(killer);
            rewardKiller(killer);
            event.deathMessage(Component.text("[职业战争] ", NamedTextColor.BLUE, TextDecoration.BOLD)
                    .append(Component.text(killer.getName(), NamedTextColor.AQUA))
                    .append(Component.text(" 击杀了 ", NamedTextColor.GRAY))
                    .append(Component.text(victim.getName(), NamedTextColor.WHITE, TextDecoration.BOLD)));
        } else {
            event.deathMessage(Component.text("[职业战争] ", NamedTextColor.BLUE, TextDecoration.BOLD)
                    .append(Component.text(victim.getName(), NamedTextColor.WHITE))
                    .append(Component.text(" 被淘汰了", NamedTextColor.GRAY)));
        }

        refreshAllUi();
    }

    public void handleQuit(Player player) {
        if (isParticipant(player) && isInCombat(player)) {
            Player killer = getLastAttacker(player);
            if (killer != null && killer.isOnline()) {
                playerDataService.addKill(killer);
                rewardKiller(killer);
                killer.sendMessage(BRAND_PREFIX + player.getName() + " 在战斗中离开，击杀已计入。");
            }
            playerDataService.addDeath(player);
        }

        clearAbilityState(player);
        clearCombatTag(player);
        sessions.remove(player.getUniqueId());
    }

    public void tagCombat(Player victim, Player attacker) {
        long now = System.currentTimeMillis();
        combatTags.put(victim.getUniqueId(), new CombatTag(attacker.getUniqueId(), now));
        combatTags.put(attacker.getUniqueId(), new CombatTag(victim.getUniqueId(), now));
    }

    public boolean isInCombat(Player player) {
        CombatTag tag = combatTags.get(player.getUniqueId());
        return tag != null && (System.currentTimeMillis() - tag.timestamp()) < COMBAT_TAG_MILLIS;
    }

    public Player getLastAttacker(Player player) {
        CombatTag tag = combatTags.get(player.getUniqueId());
        if (tag == null) {
            return null;
        }
        if ((System.currentTimeMillis() - tag.timestamp()) >= COMBAT_TAG_MILLIS) {
            combatTags.remove(player.getUniqueId());
            return null;
        }
        return Bukkit.getPlayer(tag.lastAttacker());
    }

    public void clearCombatTag(Player player) {
        combatTags.remove(player.getUniqueId());
    }

    public void refreshScoreboard(Player player) {
        scoreboardManager.updateBoard(player);
    }

    public void refreshAllScoreboards() {
        for (Player online : Bukkit.getOnlinePlayers()) {
            scoreboardManager.updateBoard(online);
        }
    }

    public void refreshAllUi() {
        refreshAllScoreboards();
        for (Player online : Bukkit.getOnlinePlayers()) {
            updateVisibility(online);
        }
    }

    public void updateVisibility(Player viewer) {
        for (Player target : Bukkit.getOnlinePlayers()) {
            if (viewer.equals(target)) {
                continue;
            }

            boolean shouldSee = sharesVisibilityGroup(viewer, target);
            if (shouldSee) {
                viewer.showPlayer(plugin, target);
                target.showPlayer(plugin, viewer);
            } else {
                viewer.hidePlayer(plugin, target);
                target.hidePlayer(plugin, viewer);
            }
        }
    }

    public List<Player> getOnlineParticipants() {
        List<Player> players = new ArrayList<>();
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (isParticipant(player)) {
                players.add(player);
            }
        }
        return players;
    }

    public void transferParticipantsToLobby(Collection<Player> players) {
        for (Player player : players) {
            if (player.isOnline()) {
                sendToLobby(player);
            }
        }
    }

    public void moveParticipantsToNewMap(Collection<Player> players, String mapName) {
        Location spawnLocation = mapManager.getSpawnLocation();
        if (spawnLocation == null) {
            return;
        }

        for (Player player : players) {
            if (!player.isOnline()) {
                continue;
            }

            clearAbilityState(player);

            PlayerSession session = getOrCreateSession(player);
            session.setState(PlayerState.RESPAWNING);
            session.setSelectedHero(null);
            session.clearCooldowns();
            session.clearFallProtection();

            heroService.clearHero(player);
            player.teleport(spawnLocation);
            heroService.giveHeroSelectorItem(player);
            player.sendMessage(BRAND_PREFIX + "地图已轮换至 §b" + mapName + "§f，请重新选择职业。");

            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                if (player.isOnline() && isParticipant(player) && isInSafeZone(player)) {
                    heroService.openSelector(player);
                }
            }, 8L);
        }

        refreshAllUi();
    }

    public void shutdown() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            sendToLobby(player);
        }
        sessions.clear();
        combatTags.clear();
    }

    private boolean sharesVisibilityGroup(Player first, Player second) {
        return isParticipant(first) == isParticipant(second);
    }

    private void rewardKiller(Player killer) {
        restorePartialHealth(killer, 4.0D);
        killer.getInventory().addItem(new ItemStack(Material.MUSHROOM_STEW));
        killer.playSound(killer.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 0.9F, 1.2F);
        killer.playSound(killer.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 0.8F, 1.0F);
        killer.playSound(killer.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 0.7F, 1.4F);
    }

    private void clearAbilityState(Player player) {
        if (heroSkillHandler != null) {
            heroSkillHandler.clearPlayerState(player);
        }
    }

    private void restoreFullHealth(Player player) {
        AttributeInstance maxHealth = player.getAttribute(Attribute.MAX_HEALTH);
        if (maxHealth != null) {
            player.setHealth(maxHealth.getValue());
        }
    }

    private void restorePartialHealth(Player player, double amount) {
        AttributeInstance maxHealth = player.getAttribute(Attribute.MAX_HEALTH);
        double max = maxHealth != null ? maxHealth.getValue() : 20.0D;
        player.setHealth(Math.min(max, player.getHealth() + amount));
    }

    private record CombatTag(UUID lastAttacker, long timestamp) {
    }
}
