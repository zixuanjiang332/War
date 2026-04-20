package jdd.war.gui;

import java.util.HashMap;
import java.util.Map;
import jdd.war.hero.HeroClass;
import jdd.war.hero.HeroTier;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

public final class ClassSelectorHolder implements InventoryHolder {
    private final HeroTier tier;
    private final Map<Integer, HeroClass> slotMap = new HashMap<>();
    private Inventory inventory;

    public ClassSelectorHolder(HeroTier tier) {
        this.tier = tier;
    }

    public HeroTier getTier() {
        return tier;
    }

    public void bindInventory(Inventory inventory) {
        this.inventory = inventory;
    }

    public void bindHero(int slot, HeroClass heroClass) {
        slotMap.put(slot, heroClass);
    }

    public HeroClass getHero(int slot) {
        return slotMap.get(slot);
    }

    @Override
    public Inventory getInventory() {
        return inventory;
    }
}
