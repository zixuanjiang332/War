package jdd.war.game;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import jdd.war.hero.HeroClass;

public final class PlayerSession {
    private final UUID playerId;
    private PlayerState state = PlayerState.LOBBY;
    private HeroClass selectedHero;
    private boolean fallProtectionArmed;
    private boolean lastSafeZoneState;
    private boolean safeZoneStateKnown;
    private final Map<String, Long> cooldowns = new HashMap<>();

    public PlayerSession(UUID playerId) {
        this.playerId = playerId;
    }

    public UUID getPlayerId() {
        return playerId;
    }

    public PlayerState getState() {
        return state;
    }

    public void setState(PlayerState state) {
        this.state = state;
    }

    public HeroClass getSelectedHero() {
        return selectedHero;
    }

    public void setSelectedHero(HeroClass selectedHero) {
        this.selectedHero = selectedHero;
    }

    public void armFallProtection() {
        fallProtectionArmed = true;
    }

    public void clearFallProtection() {
        fallProtectionArmed = false;
    }

    public boolean consumeFallProtection() {
        boolean value = fallProtectionArmed;
        fallProtectionArmed = false;
        return value;
    }

    public boolean updateSafeZoneState(boolean inSafeZone) {
        boolean changed = !safeZoneStateKnown || lastSafeZoneState != inSafeZone;
        lastSafeZoneState = inSafeZone;
        safeZoneStateKnown = true;
        return changed;
    }

    public void clearSafeZoneState() {
        safeZoneStateKnown = false;
        lastSafeZoneState = false;
    }

    public void setCooldown(String key, long expiresAt) {
        cooldowns.put(key, expiresAt);
    }

    public long getCooldown(String key) {
        return cooldowns.getOrDefault(key, 0L);
    }

    public void clearCooldown(String key) {
        cooldowns.remove(key);
    }

    public void clearCooldowns() {
        cooldowns.clear();
    }
}
