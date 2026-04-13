package jdd.war.hero.definitions;

import java.util.List;
import jdd.war.hero.HeroClass;
import jdd.war.hero.HeroDefinitionGroup;
import jdd.war.hero.HeroItems;
import jdd.war.hero.HeroRegistryBuilder;
import org.bukkit.Color;
import org.bukkit.Material;

public final class MechanicHeroDefinitions implements HeroDefinitionGroup {
    @Override
    public void register(HeroRegistryBuilder builder) {
        builder.register(HeroClass.TIDE, "潮汐", Material.NAUTILUS_SHELL, player -> {
            player.getInventory().addItem(HeroItems.unbreakable(Material.DIAMOND_SWORD));
            player.getInventory().setItem(1, HeroItems.named(Material.NAUTILUS_SHELL, "潮汐技能"));
            player.getInventory().setHelmet(HeroItems.unbreakable(Material.IRON_HELMET));
            player.getInventory().setChestplate(HeroItems.leather(Material.LEATHER_CHESTPLATE, Color.BLUE));
            player.getInventory().setLeggings(HeroItems.unbreakable(Material.IRON_LEGGINGS));
            player.getInventory().setBoots(HeroItems.unbreakable(Material.IRON_BOOTS));
            HeroRegistryBuilder.addStew(player, 3);
        }, HeroRegistryBuilder.bindings(HeroRegistryBuilder.binding("tide_pull", Material.NAUTILUS_SHELL)), "Tier 3", "钻石剑 + 鹦鹉螺壳", "拉近目标并附加缓慢");

        builder.register(HeroClass.PRISM, "棱镜", Material.AMETHYST_SHARD, player -> {
            player.getInventory().addItem(HeroItems.unbreakable(Material.DIAMOND_SWORD));
            player.getInventory().setItem(1, HeroItems.named(Material.AMETHYST_SHARD, "棱镜技能"));
            player.getInventory().setHelmet(HeroItems.leather(Material.LEATHER_HELMET, Color.WHITE));
            player.getInventory().setChestplate(HeroItems.unbreakable(Material.IRON_CHESTPLATE));
            player.getInventory().setLeggings(HeroItems.unbreakable(Material.CHAINMAIL_LEGGINGS));
            player.getInventory().setBoots(HeroItems.unbreakable(Material.IRON_BOOTS));
            HeroRegistryBuilder.addStew(player, 3);
        }, HeroRegistryBuilder.bindings(HeroRegistryBuilder.binding("prism_stance", Material.AMETHYST_SHARD)), "Tier 2", "钻石剑 + 紫水晶碎片", "进入棱镜姿态并反制投射物");

        builder.register(HeroClass.GEOMANCER, "地脉", Material.MAGMA_CREAM, player -> {
            player.getInventory().addItem(HeroItems.unbreakable(Material.IRON_SWORD));
            player.getInventory().setItem(1, HeroItems.named(Material.MAGMA_CREAM, "地脉技能"));
            HeroRegistryBuilder.ironSet(player);
            player.getInventory().setBoots(HeroItems.leather(Material.LEATHER_BOOTS, Color.ORANGE));
            HeroRegistryBuilder.addStew(player, 3);
        }, HeroRegistryBuilder.bindings(HeroRegistryBuilder.binding("geomancer_quake", Material.MAGMA_CREAM)), "Tier 3", "铁剑 + 铁套", "延迟裂地冲击并击飞目标");

        builder.register(HeroClass.RIFT, "时隙", Material.CLOCK, player -> {
            player.getInventory().addItem(HeroItems.unbreakable(Material.DIAMOND_SWORD));
            player.getInventory().setItem(1, HeroItems.named(Material.CLOCK, "时隙技能"));
            player.getInventory().setHelmet(HeroItems.leather(Material.LEATHER_HELMET, Color.fromRGB(210, 210, 255)));
            player.getInventory().setChestplate(HeroItems.leather(Material.LEATHER_CHESTPLATE, Color.fromRGB(210, 210, 255)));
            player.getInventory().setLeggings(HeroItems.unbreakable(Material.IRON_LEGGINGS));
            player.getInventory().setBoots(HeroItems.unbreakable(Material.IRON_BOOTS));
            HeroRegistryBuilder.addStew(player, 3);
        }, HeroRegistryBuilder.bindings(HeroRegistryBuilder.binding("rift_anchor", Material.CLOCK)), "Tier 2", "钻石剑 + 时钟", "记录位置并回溯回血");

        builder.register(HeroClass.FROSTMARK, "霜痕", Material.SNOWBALL, player -> {
            player.getInventory().addItem(HeroItems.unbreakable(Material.DIAMOND_SWORD));
            player.getInventory().setItem(1, HeroItems.named(Material.SNOWBALL, 16, "霜痕技能", List.of()));
            player.getInventory().setHelmet(HeroItems.unbreakable(Material.IRON_HELMET));
            player.getInventory().setChestplate(HeroItems.leather(Material.LEATHER_CHESTPLATE, Color.AQUA));
            player.getInventory().setLeggings(HeroItems.unbreakable(Material.IRON_LEGGINGS));
            player.getInventory().setBoots(HeroItems.leather(Material.LEATHER_BOOTS, Color.AQUA));
            HeroRegistryBuilder.addStew(player, 3);
        }, HeroRegistryBuilder.bindings(HeroRegistryBuilder.binding("frostmark_breath", Material.SNOWBALL)), "Tier 3", "钻石剑 + 雪球", "前方锥形冰息和短控");

        builder.register(HeroClass.RAZOR, "裂锋", Material.WHITE_BANNER, player -> {
            player.getInventory().addItem(HeroItems.unbreakable(Material.DIAMOND_AXE));
            player.getInventory().setItem(1, HeroItems.named(Material.WHITE_BANNER, "裂锋技能"));
            player.getInventory().setHelmet(HeroItems.unbreakable(Material.IRON_HELMET));
            player.getInventory().setChestplate(HeroItems.unbreakable(Material.IRON_CHESTPLATE));
            player.getInventory().setLeggings(HeroItems.leather(Material.LEATHER_LEGGINGS, Color.GRAY));
            player.getInventory().setBoots(HeroItems.unbreakable(Material.IRON_BOOTS));
            HeroRegistryBuilder.addStew(player, 3);
        }, HeroRegistryBuilder.bindings(HeroRegistryBuilder.binding("razor_dash", Material.WHITE_BANNER)), "Tier 2", "钻石斧 + 战旗", "向前突进并斩中第一目标");
    }
}
