package jdd.war.gui;

import java.util.ArrayList;
import java.util.List;
import jdd.war.hero.HeroDefinition;
import jdd.war.hero.HeroRegistry;
import jdd.war.hero.HeroTier;
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
    public static final String TITLE = "选择职业";
    public static final int PREVIOUS_SLOT = 45;
    public static final int PAGE_INFO_SLOT = 49;
    public static final int NEXT_SLOT = 53;
    public static final int LEAVE_SLOT = 48;

    private static final int[] HERO_SLOTS = {
            10, 11, 12, 13, 14, 15, 16,
            19, 20, 21, 22, 23, 24, 25,
            28, 29, 30, 31, 32, 33, 34
    };

    private ClassSelectorGUI() {
    }

    public static void open(Player player, HeroRegistry heroRegistry, int kills) {
        open(player, heroRegistry, HeroTier.highestUnlocked(kills), kills);
    }

    public static void open(Player player, HeroRegistry heroRegistry, HeroTier tier, int kills) {
        ClassSelectorHolder holder = new ClassSelectorHolder(tier);
        Inventory inventory = Bukkit.createInventory(holder, 54, Component.text(TITLE).color(NamedTextColor.BLUE).decorate(TextDecoration.BOLD));
        holder.bindInventory(inventory);

        List<HeroDefinition> heroes = heroRegistry.getAllByTier(tier);
        for (int index = 0; index < heroes.size() && index < HERO_SLOTS.length; index++) {
            HeroDefinition hero = heroes.get(index);
            int slot = HERO_SLOTS[index];
            inventory.setItem(slot, createHeroItem(hero, kills));
            holder.bindHero(slot, hero.getHeroClass());
        }

        if (tier.previous() != null) {
            inventory.setItem(PREVIOUS_SLOT, createNavItem(Material.ARROW, "上一页", tier.previous().getDisplayName()));
        }
        inventory.setItem(PAGE_INFO_SLOT, createPageInfoItem(tier, kills));
        if (tier.next() != null) {
            inventory.setItem(NEXT_SLOT, createNavItem(Material.ARROW, "下一页", tier.next().getDisplayName()));
        }
        inventory.setItem(LEAVE_SLOT, createLeaveItem());
        player.openInventory(inventory);
    }

    private static ItemStack createHeroItem(HeroDefinition hero, int kills) {
        ItemStack item = new ItemStack(hero.getMenuIcon());
        ItemMeta meta = item.getItemMeta();
        meta.displayName(Component.text(hero.getDisplayName()).decoration(TextDecoration.ITALIC, false));

        boolean unlocked = hero.getTier().isUnlocked(kills);
        List<Component> lore = new ArrayList<>();
        for (String line : hero.getDescription()) {
            lore.add(Component.text(line).decoration(TextDecoration.ITALIC, false));
        }
        lore.add(Component.empty());
        lore.add(Component.text("解锁要求: " + hero.getTier().getRequiredKills() + " 击杀")
                .color(unlocked ? NamedTextColor.AQUA : NamedTextColor.RED)
                .decoration(TextDecoration.ITALIC, false));
        lore.add(Component.text(unlocked ? "点击选择" : "尚未解锁")
                .color(unlocked ? NamedTextColor.YELLOW : NamedTextColor.RED)
                .decoration(TextDecoration.ITALIC, false));
        meta.lore(lore);
        item.setItemMeta(meta);
        return item;
    }

    private static ItemStack createNavItem(Material material, String title, String targetTier) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        meta.displayName(Component.text(title, NamedTextColor.YELLOW).decorate(TextDecoration.BOLD).decoration(TextDecoration.ITALIC, false));
        meta.lore(List.of(
                Component.text("前往 " + targetTier, NamedTextColor.GRAY).decoration(TextDecoration.ITALIC, false)
        ));
        item.setItemMeta(meta);
        return item;
    }

    private static ItemStack createPageInfoItem(HeroTier tier, int kills) {
        ItemStack item = new ItemStack(Material.BOOK);
        ItemMeta meta = item.getItemMeta();
        meta.displayName(Component.text(tier.getDisplayName(), NamedTextColor.AQUA).decorate(TextDecoration.BOLD).decoration(TextDecoration.ITALIC, false));
        meta.lore(List.of(
                Component.text("当前击杀: " + kills, NamedTextColor.GRAY).decoration(TextDecoration.ITALIC, false),
                Component.text("本页要求: " + tier.getRequiredKills(), NamedTextColor.GRAY).decoration(TextDecoration.ITALIC, false)
        ));
        item.setItemMeta(meta);
        return item;
    }

    private static ItemStack createLeaveItem() {
        ItemStack leaveItem = new ItemStack(Material.RED_BED);
        ItemMeta leaveMeta = leaveItem.getItemMeta();
        leaveMeta.displayName(Component.text("返回大厅").decoration(TextDecoration.ITALIC, false));
        leaveMeta.lore(List.of(
                Component.text("点击离开当前战场").decoration(TextDecoration.ITALIC, false),
                Component.text("并返回大厅").decoration(TextDecoration.ITALIC, false)
        ));
        leaveItem.setItemMeta(leaveMeta);
        return leaveItem;
    }
}
