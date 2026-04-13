package jdd.war.hero;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import net.kyori.adventure.text.Component;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionType;

public final class HeroRegistryBuilder {
    private final Map<HeroClass, HeroDefinition> heroes = new EnumMap<>(HeroClass.class);
    private final Map<Integer, HeroClass> slotIndex = new HashMap<>();
    private int nextSlot;

    public void register(
            HeroClass heroClass,
            String name,
            Material icon,
            Consumer<Player> applicator,
            List<HeroSkillBinding> skillBindings,
            String... description
    ) {
        if (heroes.containsKey(heroClass)) {
            throw new IllegalStateException("Duplicate hero registration: " + heroClass);
        }

        validateHero(name, skillBindings);
        int slot = nextSlot++;
        HeroDefinition definition = new SimpleHeroDefinition(
                heroClass,
                name,
                icon,
                slot,
                List.of(description),
                skillBindings,
                applicator
        );
        heroes.put(heroClass, definition);
        slotIndex.put(slot, heroClass);
    }

    public Map<HeroClass, HeroDefinition> heroes() {
        return heroes;
    }

    public Map<Integer, HeroClass> slotIndex() {
        return slotIndex;
    }

    private void validateHero(String name, List<HeroSkillBinding> skillBindings) {
        if (name == null || name.isBlank()) {
            throw new IllegalStateException("Hero display name cannot be blank.");
        }
        Set<Material> boundItems = new HashSet<>();
        for (HeroSkillBinding binding : skillBindings) {
            if (binding.material() == null || binding.material() == Material.AIR) {
                throw new IllegalStateException("Hero skill binding material cannot be empty: " + name);
            }
            if (!boundItems.add(binding.material())) {
                throw new IllegalStateException("Duplicate skill item binding in hero: " + name + " -> " + binding.material());
            }
        }
    }

    public static List<HeroSkillBinding> bindings(HeroSkillBinding... bindings) {
        return List.of(bindings);
    }

    public static List<HeroSkillBinding> none() {
        return List.of();
    }

    public static HeroSkillBinding binding(String key, Material material) {
        return new HeroSkillBinding(key, material);
    }

    public static void addStew(Player player, int count) {
        if (count > 0) {
            player.getInventory().addItem(new ItemStack(Material.MUSHROOM_STEW, count));
        }
    }

    public static void ironSet(Player player) {
        player.getInventory().setHelmet(HeroItems.unbreakable(Material.IRON_HELMET));
        player.getInventory().setChestplate(HeroItems.unbreakable(Material.IRON_CHESTPLATE));
        player.getInventory().setLeggings(HeroItems.unbreakable(Material.IRON_LEGGINGS));
        player.getInventory().setBoots(HeroItems.unbreakable(Material.IRON_BOOTS));
    }

    public static void diamondSet(Player player) {
        player.getInventory().setHelmet(HeroItems.unbreakable(Material.DIAMOND_HELMET));
        player.getInventory().setChestplate(HeroItems.unbreakable(Material.DIAMOND_CHESTPLATE));
        player.getInventory().setLeggings(HeroItems.unbreakable(Material.DIAMOND_LEGGINGS));
        player.getInventory().setBoots(HeroItems.unbreakable(Material.DIAMOND_BOOTS));
    }

    public static void leatherSet(Player player, Color color) {
        player.getInventory().setHelmet(HeroItems.leather(Material.LEATHER_HELMET, color));
        player.getInventory().setChestplate(HeroItems.leather(Material.LEATHER_CHESTPLATE, color));
        player.getInventory().setLeggings(HeroItems.leather(Material.LEATHER_LEGGINGS, color));
        player.getInventory().setBoots(HeroItems.leather(Material.LEATHER_BOOTS, color));
    }

    public static ItemStack potion(Material material, PotionType type, String name) {
        ItemStack item = new ItemStack(material, 2);
        PotionMeta meta = (PotionMeta) item.getItemMeta();
        meta.setBasePotionType(type);
        meta.displayName(Component.text(name));
        item.setItemMeta(meta);
        return item;
    }
}
