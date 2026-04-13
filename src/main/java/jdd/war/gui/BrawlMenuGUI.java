package jdd.war.gui;

import java.util.List;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public final class BrawlMenuGUI {
    public static final String TITLE = "职业战争";

    private BrawlMenuGUI() {
    }

    public static void open(Player player) {
        Inventory inventory = Bukkit.createInventory(new BrawlMenuHolder(), 9, Component.text(TITLE).color(NamedTextColor.BLUE).decorate(TextDecoration.BOLD));
        ItemStack joinItem = new ItemStack(Material.DIAMOND_SWORD);
        ItemMeta meta = joinItem.getItemMeta();
        meta.displayName(Component.text("进入战场").color(NamedTextColor.YELLOW).decorate(TextDecoration.BOLD));
        meta.lore(List.of(
                Component.text("点击加入当前战场"),
                Component.text("进入后可在安全区选择职业")
        ));
        joinItem.setItemMeta(meta);
        inventory.setItem(4, joinItem);
        player.openInventory(inventory);
    }
}
