package jdd.war.hero;

import java.util.List;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;

public final class HeroItems {
    private HeroItems() {
    }

    public static ItemStack unbreakable(Material material) {
        return unbreakable(new ItemStack(material));
    }

    public static ItemStack unbreakable(ItemStack item) {
        ItemMeta meta = item.getItemMeta();
        meta.setUnbreakable(true);
        meta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE);
        item.setItemMeta(meta);
        return item;
    }

    public static ItemStack named(Material material, String name) {
        return named(material, 1, name, List.of());
    }

    public static ItemStack named(Material material, String name, List<String> lore) {
        return named(material, 1, name, lore);
    }

    public static ItemStack named(Material material, int amount, String name, List<String> lore) {
        ItemStack item = new ItemStack(material, amount);
        return describe(item, name, lore);
    }

    public static ItemStack describe(ItemStack item, String name, List<String> lore) {
        ItemMeta meta = item.getItemMeta();
        meta.displayName(Component.text(name).decoration(TextDecoration.ITALIC, false));
        if (!lore.isEmpty()) {
            meta.lore(lore.stream()
                    .map(line -> Component.text(line).decoration(TextDecoration.ITALIC, false))
                    .toList());
        }
        item.setItemMeta(meta);
        return item;
    }

    public static ItemStack enchantedUnbreakable(Material material, Enchantment enchantment, int level) {
        ItemStack item = unbreakable(material);
        return addEnchant(item, enchantment, level);
    }

    public static ItemStack addEnchant(ItemStack item, Enchantment enchantment, int level) {
        ItemMeta meta = item.getItemMeta();
        meta.addEnchant(enchantment, level, true);
        meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        item.setItemMeta(meta);
        return item;
    }

    public static ItemStack leather(Material material, Color color) {
        ItemStack item = unbreakable(material);
        ItemMeta rawMeta = item.getItemMeta();
        if (rawMeta instanceof LeatherArmorMeta meta) {
            meta.setColor(color);
            item.setItemMeta(meta);
        }
        return item;
    }

    public static ItemStack durableBlade(int sharpness, int remainingDurability) {
        ItemStack item = new ItemStack(Material.GOLDEN_SWORD);
        ItemMeta rawMeta = item.getItemMeta();
        rawMeta.addEnchant(Enchantment.SHARPNESS, sharpness, true);
        rawMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        if (rawMeta instanceof Damageable damageable) {
            damageable.setDamage(Math.max(0, Material.GOLDEN_SWORD.getMaxDurability() - remainingDurability));
            item.setItemMeta(damageable);
            return item;
        }
        item.setItemMeta(rawMeta);
        return item;
    }
}
