package jdd.war.hero.definitions;

import java.util.List;
import jdd.war.hero.HeroClass;
import jdd.war.hero.HeroDefinitionGroup;
import jdd.war.hero.HeroItems;
import jdd.war.hero.HeroRegistryBuilder;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public final class SkirmisherHeroDefinitions implements HeroDefinitionGroup {
    @Override
    public void register(HeroRegistryBuilder builder) {
        builder.register(HeroClass.IMMORTAL, "不灭者", Material.TOTEM_OF_UNDYING, player -> {
            player.getInventory().addItem(HeroItems.describe(HeroItems.unbreakable(Material.DIAMOND_SWORD), "不灭者·主武器", List.of("技能：蘑菇煲恢复最大生命值的 15%", "擅长持久近战与换血")));
            player.getInventory().setHelmet(HeroItems.leather(Material.LEATHER_HELMET, Color.WHITE));
            player.getInventory().setChestplate(HeroItems.unbreakable(Material.CHAINMAIL_CHESTPLATE));
            player.getInventory().setLeggings(HeroItems.unbreakable(Material.CHAINMAIL_LEGGINGS));
            player.getInventory().setBoots(HeroItems.leather(Material.LEATHER_BOOTS, Color.WHITE));
            if (player.getAttribute(Attribute.MAX_HEALTH) != null) {
                player.getAttribute(Attribute.MAX_HEALTH).setBaseValue(40.0D);
            }
            player.setHealth(40.0D);
        }, HeroRegistryBuilder.none(),
                "Tier 4",
                "装备：钻石剑，白色轻甲",
                "技能：蘑菇煲恢复最大生命值的 15%"
        );

        builder.register(HeroClass.INFERNO_GUARD, "炎狱守卫", Material.BONE, player -> {
            player.getInventory().addItem(HeroItems.unbreakable(Material.DIAMOND_SWORD));
            player.getInventory().setItem(1, HeroItems.named(Material.BONE, 1, "炎狱守卫技能", List.of("召唤三只地狱猎犬协助作战")));
            player.getInventory().setHelmet(HeroItems.unbreakable(Material.IRON_HELMET));
            player.getInventory().setChestplate(HeroItems.unbreakable(Material.IRON_CHESTPLATE));
            player.getInventory().setLeggings(HeroItems.unbreakable(Material.IRON_LEGGINGS));
            player.getInventory().setBoots(HeroItems.leather(Material.LEATHER_BOOTS, Color.RED));
            HeroRegistryBuilder.addStew(player, 3);
        }, HeroRegistryBuilder.bindings(HeroRegistryBuilder.binding("inferno_hounds", Material.BONE)),
                "Tier 3",
                "装备：钻石剑，铁头铁胸铁腿，红靴，骨头",
                "技能：召唤地狱猎犬"
        );

        builder.register(HeroClass.THOR, "雷神", Material.WOODEN_AXE, player -> {
            player.getInventory().addItem(HeroItems.unbreakable(Material.DIAMOND_SWORD));
            player.getInventory().setItem(1, HeroItems.named(Material.WOODEN_AXE, 1, "雷神技能", List.of("右键在近距离范围内落雷")));
            player.getInventory().setHelmet(HeroItems.leather(Material.LEATHER_HELMET, Color.YELLOW));
            player.getInventory().setChestplate(HeroItems.unbreakable(Material.IRON_CHESTPLATE));
            player.getInventory().setLeggings(HeroItems.unbreakable(Material.IRON_LEGGINGS));
            player.getInventory().setBoots(HeroItems.unbreakable(Material.IRON_BOOTS));
            HeroRegistryBuilder.addStew(player, 3);
        }, HeroRegistryBuilder.bindings(HeroRegistryBuilder.binding("thor_lightning", Material.WOODEN_AXE)),
                "Tier 3",
                "装备：钻石剑，轻甲，木斧",
                "技能：范围落雷"
        );

        builder.register(HeroClass.HEAD_REAPER, "头颅收割者", Material.WITHER_SKELETON_SKULL, player -> {
            player.getInventory().addItem(HeroItems.describe(HeroItems.unbreakable(Material.DIAMOND_SWORD), "头颅收割者·主武器", List.of("技能：高处坠落触发范围重击", "利用落差打出爆发伤害")));
            player.getInventory().setHelmet(HeroItems.unbreakable(Material.IRON_HELMET));
            player.getInventory().setChestplate(HeroItems.leather(Material.LEATHER_CHESTPLATE, Color.GRAY));
            player.getInventory().setLeggings(HeroItems.unbreakable(Material.IRON_LEGGINGS));
            player.getInventory().setBoots(HeroItems.unbreakable(Material.IRON_BOOTS));
            HeroRegistryBuilder.addStew(player, 3);
        }, HeroRegistryBuilder.none(),
                "Tier 4",
                "装备：钻石剑，铁头，灰胸，铁腿铁靴",
                "技能：高处坠落触发范围重击"
        );

        builder.register(HeroClass.GHOST, "幽灵", Material.GHAST_TEAR, player -> {
            player.getInventory().addItem(HeroItems.describe(HeroItems.unbreakable(Material.DIAMOND_SWORD), "幽灵·主武器", List.of("技能：常驻隐身", "依靠隐身接近敌人发起偷袭")));
            player.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, PotionEffect.INFINITE_DURATION, 0, false, false));
            HeroRegistryBuilder.addStew(player, 3);
        }, HeroRegistryBuilder.none(),
                "Tier 3",
                "装备：钻石剑，无护甲",
                "技能：常驻隐身"
        );

        builder.register(HeroClass.THUG, "熔拳", Material.BLAZE_POWDER, player -> {
            player.getInventory().addItem(HeroItems.describe(HeroItems.unbreakable(Material.DIAMOND_SWORD), "熔拳·主武器", List.of("右键主武器可蓄力强化", "每层提升锋利与火焰附加，命中 2 次后重置")));
            player.getInventory().setHelmet(HeroItems.unbreakable(Material.DIAMOND_HELMET));
            player.getInventory().setChestplate(HeroItems.leather(Material.LEATHER_CHESTPLATE, Color.RED));
            player.getInventory().setLeggings(HeroItems.unbreakable(Material.IRON_LEGGINGS));
            player.getInventory().setBoots(HeroItems.unbreakable(Material.IRON_BOOTS));
            HeroRegistryBuilder.addStew(player, 3);
        }, HeroRegistryBuilder.none(),
                "Tier 3",
                "装备：钻石剑，钻石头盔，红皮甲，铁腿铁靴",
                "技能：右键主武器蓄力，提升锋利与火焰附加"
        );

        builder.register(HeroClass.PHANTOM, "幻行", Material.BOOK, player -> {
            player.getInventory().addItem(HeroItems.unbreakable(Material.DIAMOND_SWORD));
            player.getInventory().setItem(1, HeroItems.named(Material.BOOK, 1, "幻行技能", List.of("短暂进入旁观者模式 4 秒", "期间持续获得生命恢复 II，结束后免疫一次摔落伤害")));
            HeroRegistryBuilder.leatherSet(player, Color.fromRGB(180, 180, 255));
            player.getInventory().setChestplate(HeroItems.unbreakable(Material.CHAINMAIL_CHESTPLATE));
            player.getInventory().setBoots(HeroItems.unbreakable(Material.IRON_BOOTS));
            HeroRegistryBuilder.addStew(player, 3);
        }, HeroRegistryBuilder.bindings(HeroRegistryBuilder.binding("phantom_flight", Material.BOOK)),
                "Tier 3",
                "装备：钻石剑，皮革套，锁链胸甲，铁靴子，书",
                "技能：短暂旁观并持续回血，结束后获得一次免摔"
        );

        builder.register(HeroClass.SHACKLE, "禁铃", Material.BELL, player -> {
            player.getInventory().addItem(HeroItems.unbreakable(Material.DIAMOND_SWORD));
            player.getInventory().setItem(1, HeroItems.named(Material.BELL, 1, "禁铃技能", List.of("范围减速周围敌人", "附加短暂凋零 I")));
            player.getInventory().setHelmet(HeroItems.leather(Material.LEATHER_HELMET, Color.YELLOW));
            player.getInventory().setChestplate(HeroItems.unbreakable(Material.IRON_CHESTPLATE));
            player.getInventory().setLeggings(HeroItems.unbreakable(Material.IRON_LEGGINGS));
            player.getInventory().setBoots(HeroItems.leather(Material.LEATHER_BOOTS, Color.YELLOW));
            HeroRegistryBuilder.addStew(player, 3);
        }, HeroRegistryBuilder.bindings(HeroRegistryBuilder.binding("shackle_bell", Material.BELL)),
                "Tier 3",
                "装备：钻石剑，黄白轻甲，铃铛",
                "技能：范围减速控制"
        );

        builder.register(HeroClass.VIKING, "维京人", Material.DIAMOND_AXE, player -> {
            player.getInventory().addItem(HeroItems.describe(HeroItems.unbreakable(Material.DIAMOND_AXE), "维京人·主武器", List.of("技能：高压近战斧手", "依靠钻石斧快速压制近战目标")));
            HeroRegistryBuilder.ironSet(player);
            HeroRegistryBuilder.addStew(player, 3);
        }, HeroRegistryBuilder.none(),
                "Tier 4",
                "装备：钻石斧，铁套",
                "技能：高压近战斧手"
        );

        builder.register(HeroClass.CAVALRY, "骑兵", Material.DIAMOND_HORSE_ARMOR, player -> {
            player.getInventory().addItem(HeroItems.unbreakable(Material.DIAMOND_SWORD));
            player.getInventory().setItem(1, HeroItems.named(Material.DIAMOND_HORSE_ARMOR, 1, "骑兵技能", List.of("召唤战马并骑乘", "战马自带钻石马铠")));
            player.getInventory().setHelmet(HeroItems.leather(Material.LEATHER_HELMET, Color.fromRGB(139, 69, 19)));
            player.getInventory().setChestplate(HeroItems.unbreakable(Material.IRON_CHESTPLATE));
            player.getInventory().setLeggings(HeroItems.unbreakable(Material.IRON_LEGGINGS));
            player.getInventory().setBoots(HeroItems.unbreakable(Material.IRON_BOOTS));
            HeroRegistryBuilder.addStew(player, 3);
        }, HeroRegistryBuilder.bindings(HeroRegistryBuilder.binding("cavalry_horse", Material.DIAMOND_HORSE_ARMOR)),
                "Tier 3",
                "装备：钻石剑，棕色头盔，铁胸铁腿铁靴，马铠",
                "技能：召唤战马快速冲锋"
        );
    }
}
