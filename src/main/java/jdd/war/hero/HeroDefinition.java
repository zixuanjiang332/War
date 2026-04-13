package jdd.war.hero;

import java.util.List;
import org.bukkit.Material;
import org.bukkit.entity.Player;

public interface HeroDefinition {
    HeroClass getHeroClass();

    String getDisplayName();

    Material getMenuIcon();

    int getMenuSlot();

    List<String> getDescription();

    List<HeroSkillBinding> getSkillBindings();

    void apply(Player player);
}
