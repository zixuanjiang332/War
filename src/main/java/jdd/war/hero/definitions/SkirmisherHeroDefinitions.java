package jdd.war.hero.definitions;

import jdd.war.hero.HeroClass;
import jdd.war.hero.HeroDefinitionGroup;
import jdd.war.hero.HeroItems;
import jdd.war.hero.HeroRegistryBuilder;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public final class SkirmisherHeroDefinitions implements HeroDefinitionGroup {
    @Override
    public void register(HeroRegistryBuilder builder) {
        builder.register(HeroClass.IMMORTAL, "不朽", Material.TOTEM_OF_UNDYING, player -> {
            player.getInventory().addItem(HeroItems.unbreakable(Material.DIAMOND_SWORD));
            player.getInventory().setHelmet(HeroItems.leather(Material.LEATHER_HELMET, Color.WHITE));
            player.getInventory().setChestplate(HeroItems.unbreakable(Material.IRON_CHESTPLATE));
            player.getInventory().setLeggings(HeroItems.unbreakable(Material.CHAINMAIL_LEGGINGS));
            player.getInventory().setBoots(HeroItems.leather(Material.LEATHER_BOOTS, Color.WHITE));
            if (player.getAttribute(Attribute.MAX_HEALTH) != null) {
                player.getAttribute(Attribute.MAX_HEALTH).setBaseValue(40.0D);
            }
            player.setHealth(40.0D);
            HeroRegistryBuilder.addStew(player, 5);
        }, HeroRegistryBuilder.none(), "Tier 4", "生命上限 +20", "蘑菇煲数量更多");

        builder.register(HeroClass.INFERNO_GUARD, "炎狼", Material.BONE, player -> {
            player.getInventory().addItem(HeroItems.unbreakable(Material.DIAMOND_SWORD));
            player.getInventory().setItem(1, HeroItems.named(Material.BONE, "炎狼技能"));
            player.getInventory().setHelmet(HeroItems.unbreakable(Material.IRON_HELMET));
            player.getInventory().setChestplate(HeroItems.unbreakable(Material.IRON_CHESTPLATE));
            player.getInventory().setLeggings(HeroItems.unbreakable(Material.IRON_LEGGINGS));
            player.getInventory().setBoots(HeroItems.leather(Material.LEATHER_BOOTS, Color.RED));
            HeroRegistryBuilder.addStew(player, 3);
        }, HeroRegistryBuilder.bindings(HeroRegistryBuilder.binding("inferno_hounds", Material.BONE)), "Tier 3", "钻石剑 + 骨头", "右键召唤三只地狱猎犬");

        builder.register(HeroClass.THOR, "雷斧", Material.WOODEN_AXE, player -> {
            player.getInventory().addItem(HeroItems.unbreakable(Material.DIAMOND_SWORD));
            player.getInventory().addItem(HeroItems.unbreakable(Material.WOODEN_AXE));
            player.getInventory().setHelmet(HeroItems.leather(Material.LEATHER_HELMET, Color.YELLOW));
            player.getInventory().setChestplate(HeroItems.unbreakable(Material.IRON_CHESTPLATE));
            player.getInventory().setLeggings(HeroItems.unbreakable(Material.IRON_LEGGINGS));
            player.getInventory().setBoots(HeroItems.unbreakable(Material.IRON_BOOTS));
            HeroRegistryBuilder.addStew(player, 3);
        }, HeroRegistryBuilder.none(), "Tier 3", "钻石剑 + 木斧", "木斧命中后触发范围雷击");

        builder.register(HeroClass.HEAD_REAPER, "裂颅", Material.WITHER_SKELETON_SKULL, player -> {
            player.getInventory().addItem(HeroItems.unbreakable(Material.DIAMOND_SWORD));
            player.getInventory().setHelmet(HeroItems.unbreakable(Material.IRON_HELMET));
            player.getInventory().setChestplate(HeroItems.leather(Material.LEATHER_CHESTPLATE, Color.GRAY));
            player.getInventory().setLeggings(HeroItems.unbreakable(Material.IRON_LEGGINGS));
            player.getInventory().setBoots(HeroItems.unbreakable(Material.IRON_BOOTS));
            HeroRegistryBuilder.addStew(player, 3);
        }, HeroRegistryBuilder.none(), "Tier 4", "钻石剑 + 轻甲", "从三格以上落下可造成额外伤害");

        builder.register(HeroClass.GHOST, "幽魂", Material.GHAST_TEAR, player -> {
            player.getInventory().addItem(HeroItems.unbreakable(Material.DIAMOND_SWORD));
            player.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, PotionEffect.INFINITE_DURATION, 0, false, false));
            HeroRegistryBuilder.addStew(player, 3);
        }, HeroRegistryBuilder.none(), "Tier 3", "钻石剑", "常驻隐身");

        builder.register(HeroClass.THUG, "熔拳", Material.IRON_BARS, player -> {
            player.getInventory().addItem(HeroItems.unbreakable(Material.DIAMOND_SWORD));
            player.getInventory().setItem(1, HeroItems.named(Material.IRON_BARS, "熔拳技能"));
            player.getInventory().setHelmet(HeroItems.unbreakable(Material.DIAMOND_HELMET));
            player.getInventory().setChestplate(HeroItems.leather(Material.LEATHER_CHESTPLATE, Color.RED));
            player.getInventory().setLeggings(HeroItems.unbreakable(Material.IRON_LEGGINGS));
            player.getInventory().setBoots(HeroItems.unbreakable(Material.IRON_BOOTS));
            HeroRegistryBuilder.addStew(player, 3);
        }, HeroRegistryBuilder.bindings(HeroRegistryBuilder.binding("thug_burst", Material.IRON_BARS)), "Tier 3", "钻石剑 + 铁栏杆", "锁定附近目标并爆发伤害");

        builder.register(HeroClass.PHANTOM, "幻行", Material.BOOK, player -> {
            player.getInventory().addItem(HeroItems.unbreakable(Material.DIAMOND_SWORD));
            player.getInventory().setItem(1, HeroItems.named(Material.BOOK, "幻行技能"));
            HeroRegistryBuilder.leatherSet(player, Color.fromRGB(180, 180, 255));
            HeroRegistryBuilder.addStew(player, 3);
        }, HeroRegistryBuilder.bindings(HeroRegistryBuilder.binding("phantom_flight", Material.BOOK)), "Tier 3", "钻石剑 + 皮革套", "右键获得短暂飞行");

        builder.register(HeroClass.SHACKLE, "冰钟", Material.BELL, player -> {
            player.getInventory().addItem(HeroItems.unbreakable(Material.DIAMOND_SWORD));
            player.getInventory().setItem(1, HeroItems.named(Material.BELL, "冰钟技能"));
            player.getInventory().setHelmet(HeroItems.leather(Material.LEATHER_HELMET, Color.YELLOW));
            player.getInventory().setChestplate(HeroItems.unbreakable(Material.IRON_CHESTPLATE));
            player.getInventory().setLeggings(HeroItems.unbreakable(Material.IRON_LEGGINGS));
            player.getInventory().setBoots(HeroItems.leather(Material.LEATHER_BOOTS, Color.YELLOW));
            HeroRegistryBuilder.addStew(player, 3);
        }, HeroRegistryBuilder.bindings(HeroRegistryBuilder.binding("shackle_bell", Material.BELL)), "Tier 3", "钻石剑 + 钟", "右键对周围玩家施加重度缓慢");

        builder.register(HeroClass.VIKING, "维京", Material.DIAMOND_AXE, player -> {
            player.getInventory().addItem(HeroItems.unbreakable(Material.DIAMOND_AXE));
            HeroRegistryBuilder.ironSet(player);
            HeroRegistryBuilder.addStew(player, 3);
        }, HeroRegistryBuilder.none(), "Tier 4", "钻石斧 + 铁套");

        builder.register(HeroClass.CAVALRY, "骑士", Material.DIAMOND_HORSE_ARMOR, player -> {
            player.getInventory().addItem(HeroItems.unbreakable(Material.DIAMOND_SWORD));
            player.getInventory().setItem(1, HeroItems.named(Material.DIAMOND_HORSE_ARMOR, "骑士技能"));
            player.getInventory().setHelmet(HeroItems.leather(Material.LEATHER_HELMET, Color.fromRGB(139, 69, 19)));
            player.getInventory().setChestplate(HeroItems.unbreakable(Material.IRON_CHESTPLATE));
            player.getInventory().setLeggings(HeroItems.unbreakable(Material.IRON_LEGGINGS));
            player.getInventory().setBoots(HeroItems.unbreakable(Material.IRON_BOOTS));
            HeroRegistryBuilder.addStew(player, 3);
        }, HeroRegistryBuilder.bindings(HeroRegistryBuilder.binding("cavalry_horse", Material.DIAMOND_HORSE_ARMOR)), "Tier 3", "钻石剑 + 马铠", "右键召唤并骑乘战马");
    }
}
