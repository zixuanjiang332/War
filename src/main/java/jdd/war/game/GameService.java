package jdd.war.game;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import jdd.war.War;
import jdd.war.bootstrap.PluginConfigManager;
import jdd.war.data.PlayerDataService;
import jdd.war.hero.HeroClass;
import jdd.war.hero.HeroService;
import jdd.war.hero.HeroSkillHandler;
import jdd.war.hero.HeroTier;
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
import org.bukkit.scheduler.BukkitTask;

public final class GameService {
    private static final double SAFE_ZONE_RADIUS_SQUARED = 13.0D * 13.0D;
    private static final double SAFE_ZONE_RED_WOOL_RADIUS_SQUARED = 17.0D * 17.0D;
    private static final long COMBAT_TAG_MILLIS = 10_000L;

    private final War plugin;
    private final PluginConfigManager pluginConfigManager;
    private final MapManager mapManager;
    private final HeroService heroService;
    private final PlayerDataService playerDataService;
    private final ScoreboardManager scoreboardManager;
    private final Map<UUID, PlayerSession> sessions = new ConcurrentHashMap<>();
    private final Map<UUID, CombatTag> combatTags = new ConcurrentHashMap<>();
    private final Set<UUID> dirtySidebarPlayers = ConcurrentHashMap.newKeySet();
    private final Set<UUID> dirtyRankPlayers = ConcurrentHashMap.newKeySet();

    private HeroSkillHandler heroSkillHandler;
    private BukkitTask uiFlushTask;

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

    public void startBackgroundTasks() {
        if (uiFlushTask == null) {
            uiFlushTask = Bukkit.getScheduler().runTaskTimer(plugin, this::flushPendingUiUpdates, 20L, 20L);
        }
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
        return isInSafeZone(player.getLocation());
    }

    public boolean isInSafeZone(Location location) {
        Location spawn = mapManager.getSpawnLocation();
        if (spawn == null) {
            return false;
        }
        if (location.getWorld() == null || !location.getWorld().equals(spawn.getWorld())) {
            return false;
        }
        if (location.distanceSquared(spawn) <= SAFE_ZONE_RADIUS_SQUARED) {
            return true;
        }
        return isSafeZoneRedWool(location, spawn);
    }

    public boolean updateSafeZoneTracking(Player player) {
        return getOrCreateSession(player).updateSafeZoneState(isInSafeZone(player));
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
        session.clearSafeZoneState();

        player.teleport(pluginConfigManager.getLobbyLocation());
        player.setGameMode(GameMode.SURVIVAL);
        player.setFoodLevel(20);
        restoreFullHealth(player);
        heroService.giveLobbyItems(player);
        player.sendMessage(Branding.PREFIX + "已返回大厅。");
        refreshScoreboard(player);
        updateVisibility(player);
    }

    public boolean joinBrawl(Player player) {
        Location spawnLocation = mapManager.getSpawnLocation();
        if (spawnLocation == null) {
            player.sendMessage(Branding.PREFIX + "当前没有可用战场。");
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
        session.clearSafeZoneState();

        player.teleport(spawnLocation);
        player.setGameMode(GameMode.SURVIVAL);
        player.setFoodLevel(20);
        restoreFullHealth(player);
        heroService.giveHeroSelectorItem(player);
        player.sendMessage(Branding.PREFIX + "已进入战场，请选择职业。");
        refreshScoreboard(player);
        updateVisibility(player);
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

    public int getPlayerKills(Player player) {
        return playerDataService.getOrCreate(player).getKills();
    }

    public void selectHero(Player player, HeroClass heroClass) {
        if (!isParticipant(player)) {
            player.sendMessage(Branding.PREFIX + "你当前不在职业战争中。");
            return;
        }
        if (heroClass == HeroClass.OP_CLASS && !player.isOp()) {
            player.sendMessage(Branding.PREFIX + "这个职业仅限 OP 使用。");
            return;
        }
        if (!canUseHero(player, heroClass)) {
            int requiredKills = getRequiredKills(heroClass);
            player.sendMessage(Branding.PREFIX + "该职业需要 §b" + requiredKills + "§f 击杀解锁。");
            return;
        }

        clearAbilityState(player);
        heroService.assignHero(player, heroClass);
        if (heroSkillHandler != null) {
            heroSkillHandler.refreshPassiveEffects(player);
        }

        PlayerSession session = getOrCreateSession(player);
        session.setSelectedHero(heroClass);
        session.setState(PlayerState.IN_BRAWL);
        session.clearFallProtection();
        session.clearSafeZoneState();

        player.sendMessage(Branding.PREFIX + "已选择职业：" + heroService.getHeroName(heroClass));
        refreshScoreboard(player);
        updateVisibility(player);
    }

    public boolean canUseHero(Player player, HeroClass heroClass) {
        if (heroClass == HeroClass.OP_CLASS) {
            return player.isOp();
        }
        return playerDataService.getOrCreate(player).getKills() >= getRequiredKills(heroClass);
    }

    public int getRequiredKills(HeroClass heroClass) {
        HeroTier tier = heroService.getHeroTier(heroClass);
        return tier.getRequiredKills();
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
        session.clearSafeZoneState();

        heroService.clearHero(player);
        heroService.giveHeroSelectorItem(player);
        player.sendMessage(Branding.PREFIX + "请选择新的职业。");

        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            if (player.isOnline() && isParticipant(player) && isInSafeZone(player)) {
                heroService.openSelector(player);
            }
        }, 8L);

        refreshScoreboard(player);
        updateVisibility(player);
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
        session.clearSafeZoneState();

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
                    .append(Component.text(" 已阵亡", NamedTextColor.GRAY)));
        }

        markSidebarDirty(victim);
    }

    public void handleQuit(Player player) {
        if (isParticipant(player) && isInCombat(player)) {
            Player killer = getLastAttacker(player);
            if (killer != null && killer.isOnline()) {
                playerDataService.addKill(killer);
                rewardKiller(killer);
                killer.sendMessage(Branding.PREFIX + player.getName() + " 在战斗中离开，击杀已计入。");
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

    public void handlePlayerDataLoaded(Player player) {
        scoreboardManager.refreshPlayerListName(player);
        refreshScoreboard(player);
    }

    public void handleKillStatChanged(Player player, boolean rankChanged) {
        markSidebarDirty(player);
        if (rankChanged) {
            markRankDirty(player);
        }
    }

    public void handleDeathStatChanged(Player player) {
        markSidebarDirty(player);
    }

    public void handlePlayerJoined(Player player) {
        scoreboardManager.refreshPlayerListName(player);
        refreshScoreboard(player);
        updateVisibility(player);
    }

    public void handlePlayerLeft(Player player) {
        scoreboardManager.removeBoard(player);
    }

    public void markSidebarDirty(Player player) {
        dirtySidebarPlayers.add(player.getUniqueId());
    }

    public void markRankDirty(Player player) {
        dirtyRankPlayers.add(player.getUniqueId());
        dirtySidebarPlayers.add(player.getUniqueId());
    }

    public void flushPendingUiUpdates() {
        Set<UUID> ranks = Set.copyOf(dirtyRankPlayers);
        Set<UUID> sidebars = Set.copyOf(dirtySidebarPlayers);

        dirtyRankPlayers.removeAll(ranks);
        dirtySidebarPlayers.removeAll(sidebars);

        for (UUID playerId : ranks) {
            Player player = Bukkit.getPlayer(playerId);
            if (player != null && player.isOnline()) {
                scoreboardManager.refreshPlayerListName(player);
            }
        }

        for (UUID playerId : sidebars) {
            Player player = Bukkit.getPlayer(playerId);
            if (player != null && player.isOnline()) {
                refreshScoreboard(player);
            }
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
            session.clearSafeZoneState();

            heroService.clearHero(player);
            player.teleport(spawnLocation);
            heroService.giveHeroSelectorItem(player);
            player.sendMessage(Branding.PREFIX + "地图已轮换至 §b" + mapName + "§f，请重新选择职业。");

            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                if (player.isOnline() && isParticipant(player) && isInSafeZone(player)) {
                    heroService.openSelector(player);
                }
            }, 8L);
        }

        for (Player player : players) {
            if (player.isOnline()) {
                refreshScoreboard(player);
                updateVisibility(player);
            }
        }
    }

    public void shutdown() {
        if (uiFlushTask != null) {
            uiFlushTask.cancel();
        }
        for (Player player : Bukkit.getOnlinePlayers()) {
            sendToLobby(player);
        }
        sessions.clear();
        combatTags.clear();
        dirtySidebarPlayers.clear();
        dirtyRankPlayers.clear();
        scoreboardManager.closeAll();
    }

    private boolean sharesVisibilityGroup(Player first, Player second) {
        return isParticipant(first) == isParticipant(second);
    }

    private void rewardKiller(Player killer) {
        HeroClass heroClass = getSelectedHero(killer);
        if (heroClass != HeroClass.TITAN) {
            restorePartialHealth(killer, 4.0D);
            killer.getInventory().addItem(new ItemStack(Material.MUSHROOM_STEW));
        }
        if (heroSkillHandler != null) {
            heroSkillHandler.refreshPassiveEffects(killer);
        }
        killer.playSound(killer.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 0.9F, 1.2F);
        killer.playSound(killer.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 0.8F, 1.0F);
        killer.playSound(killer.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 0.7F, 1.4F);
    }

    private void clearAbilityState(Player player) {
        if (heroSkillHandler != null) {
            heroSkillHandler.clearPlayerState(player);
        }
    }

    public long getRemainingCooldownMillis(Player player, String key) {
        long now = System.currentTimeMillis();
        long expireAt = getOrCreateSession(player).getCooldown(key);
        return Math.max(0L, expireAt - now);
    }

    public long tryUseCooldown(Player player, String key, long cooldownMillis) {
        long remaining = getRemainingCooldownMillis(player, key);
        if (remaining > 0L) {
            return remaining;
        }
        getOrCreateSession(player).setCooldown(key, System.currentTimeMillis() + cooldownMillis);
        return 0L;
    }

    public void clearCooldown(Player player, String key) {
        getOrCreateSession(player).clearCooldown(key);
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

    private boolean isSafeZoneRedWool(Location feetLocation, Location spawn) {
        return isSafeZoneRedWoolBlock(spawn, feetLocation.getBlock().getLocation())
                || isSafeZoneRedWoolBlock(spawn, feetLocation.getBlock().getRelative(0, -1, 0).getLocation());
    }

    private boolean isSafeZoneRedWoolBlock(Location spawn, Location blockLocation) {
        return blockLocation.getBlock().getType() == Material.RED_WOOL
                && blockLocation.getWorld() != null
                && blockLocation.getWorld().equals(spawn.getWorld())
                && blockLocation.distanceSquared(spawn) <= SAFE_ZONE_RED_WOOL_RADIUS_SQUARED;
    }

    private record CombatTag(UUID lastAttacker, long timestamp) {
    }
}
