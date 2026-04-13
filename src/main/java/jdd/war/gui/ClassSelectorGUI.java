package jdd.war.gui;

import java.util.ArrayList;
import java.util.List;
import jdd.war.hero.HeroDefinition;
import jdd.war.hero.HeroRegistry;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public final class ClassSelectorGUI {
    public static final String TITLE = "职业选择";

    private ClassSelectorGUI() {
    }

    public static void open(Player player, HeroRegistry heroRegistry) {
        Inventory inventory = Bukkit.createInventory(null, 54, Component.text(TITLE).color(NamedTextColor.BLUE).decorate(TextDecoration.BOLD));
        for (HeroDefinition hero : heroRegistry.getAll()) {
            inventory.setItem(hero.getMenuSlot(), createHeroItem(hero));
        }

        ItemStack leaveItem = new ItemStack(Material.RED_BED);
        ItemMeta leaveMeta = leaveItem.getItemMeta();
        leaveMeta.displayName(Component.text("返回大厅").decoration(TextDecoration.ITALIC, false));
        leaveMeta.lore(List.of(
                Component.text("点击离开当前战场"),
                Component.text("并返回大厅")
        ));
        leaveItem.setItemMeta(leaveMeta);
        inventory.setItem(53, leaveItem);
        player.openInventory(inventory);
    }

    private static ItemStack createHeroItem(HeroDefinition hero) {
        ItemStack item = new ItemStack(hero.getMenuIcon());
        ItemMeta meta = item.getItemMeta();
        meta.displayName(Component.text(hero.getDisplayName()).decoration(TextDecoration.ITALIC, false));

        List<Component> lore = new ArrayList<>();
        for (String line : hero.getDescription()) {
            lore.add(Component.text(line).decoration(TextDecoration.ITALIC, false));
        }
        lore.add(Component.text("点击选择").color(NamedTextColor.YELLOW).decoration(TextDecoration.ITALIC, false));
        meta.lore(lore);
        item.setItemMeta(meta);
        return item;
    }
}
