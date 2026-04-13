package jdd.war.hero;

import java.util.List;
import java.util.function.Consumer;
import org.bukkit.Material;
import org.bukkit.entity.Player;

public final class SimpleHeroDefinition implements HeroDefinition {
    private final HeroClass heroClass;
    private final String displayName;
    private final Material menuIcon;
    private final int menuSlot;
    private final List<String> description;
    private final Consumer<Player> applicator;

    public SimpleHeroDefinition(
            HeroClass heroClass,
            String displayName,
            Material menuIcon,
            int menuSlot,
            List<String> description,
            Consumer<Player> applicator
    ) {
        this.heroClass = heroClass;
        this.displayName = displayName;
        this.menuIcon = menuIcon;
        this.menuSlot = menuSlot;
        this.description = description;
        this.applicator = applicator;
    }

    @Override
    public HeroClass getHeroClass() {
        return heroClass;
    }

    @Override
    public String getDisplayName() {
        return displayName;
    }

    @Override
    public Material getMenuIcon() {
        return menuIcon;
    }

    @Override
    public int getMenuSlot() {
        return menuSlot;
    }

    @Override
    public List<String> getDescription() {
        return description;
    }

    @Override
    public void apply(Player player) {
        applicator.accept(player);
    }
}
