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

    default HeroTier getTier() {
        List<String> description = getDescription();
        if (description.isEmpty()) {
            return HeroTier.TIER_1;
        }
        return HeroTier.fromDisplayName(description.get(0));
    }

    void apply(Player player);
}
