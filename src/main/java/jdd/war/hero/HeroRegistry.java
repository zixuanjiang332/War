package jdd.war.hero;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import jdd.war.hero.definitions.ExpansionHeroDefinitions;
import jdd.war.hero.definitions.MechanicHeroDefinitions;
import jdd.war.hero.definitions.PowerHeroDefinitions;
import jdd.war.hero.definitions.SkirmisherHeroDefinitions;
import jdd.war.hero.definitions.SpecialHeroDefinitions;
import jdd.war.hero.definitions.StarterHeroDefinitions;

public final class HeroRegistry {
    private final Map<HeroClass, HeroDefinition> heroes;
    private final Map<Integer, HeroClass> slotIndex;

    public HeroRegistry() {
        HeroRegistryBuilder builder = new HeroRegistryBuilder();
        List<HeroDefinitionGroup> groups = List.of(
                new StarterHeroDefinitions(),
                new ExpansionHeroDefinitions(),
                new SkirmisherHeroDefinitions(),
                new PowerHeroDefinitions(),
                new SpecialHeroDefinitions(),
                new MechanicHeroDefinitions()
        );
        for (HeroDefinitionGroup group : groups) {
            group.register(builder);
        }
        validate(builder.heroes());
        heroes = new EnumMap<>(builder.heroes());
        slotIndex = new HashMap<>(builder.slotIndex());
    }

    public HeroDefinition get(HeroClass heroClass) {
        HeroDefinition definition = heroes.get(heroClass);
        if (definition == null) {
            throw new IllegalArgumentException("Unknown hero class: " + heroClass);
        }
        return definition;
    }

    public List<HeroDefinition> getAll() {
        List<HeroDefinition> list = new ArrayList<>(heroes.values());
        list.sort(Comparator.comparingInt(HeroDefinition::getMenuSlot));
        return list;
    }

    public List<HeroDefinition> getAllByTier(HeroTier tier) {
        return getAll().stream()
                .filter(hero -> hero.getTier() == tier)
                .toList();
    }

    public Optional<HeroClass> findByMenuSlot(int slot) {
        return Optional.ofNullable(slotIndex.get(slot));
    }

    private void validate(Map<HeroClass, HeroDefinition> registeredHeroes) {
        Set<String> names = new HashSet<>();
        for (HeroClass heroClass : HeroClass.values()) {
            HeroDefinition definition = registeredHeroes.get(heroClass);
            if (definition == null) {
                throw new IllegalStateException("Missing hero registration: " + heroClass);
            }
            if (!names.add(definition.getDisplayName())) {
                throw new IllegalStateException("Duplicate hero display name: " + definition.getDisplayName());
            }
        }
    }
}
