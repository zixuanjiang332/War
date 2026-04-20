package jdd.war.hero.definitions;

import java.util.List;
import jdd.war.hero.HeroClass;
import jdd.war.hero.HeroDefinitionGroup;
import jdd.war.hero.HeroItems;
import jdd.war.hero.HeroRegistryBuilder;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public final class ExpansionHeroDefinitions implements HeroDefinitionGroup {
    @Override
    public void register(HeroRegistryBuilder builder) {
        builder.register(HeroClass.SCOUT, "猎鹰", Material.BOW, player -> {
            ItemStack bow = HeroItems.unbreakable(Material.BOW);
            HeroItems.addEnchant(bow, Enchantment.PUNCH, 1);
            player.getInventory().addItem(HeroItems.describe(HeroItems.unbreakable(Material.IRON_SWORD), "猎鹰·副武器", List.of(
                    "技能：常驻速度 I 与跳跃提升 I",
                    "机动射手的近战补刀武器"
            )));
            player.getInventory().addItem(HeroItems.describe(bow, "猎鹰·长弓", List.of(
                    "技能：击退 I 远程压制",
                    "依靠机动性拉扯目标"
            )));
            player.getInventory().addItem(new ItemStack(Material.ARROW, 64));
            player.getInventory().setHelmet(HeroItems.leather(Material.LEATHER_HELMET, Color.fromRGB(200, 200, 200)));
            player.getInventory().setChestplate(HeroItems.unbreakable(Material.IRON_CHESTPLATE));
            player.getInventory().setLeggings(HeroItems.leather(Material.LEATHER_LEGGINGS, Color.fromRGB(200, 200, 200)));
            player.getInventory().setBoots(HeroItems.unbreakable(Material.IRON_BOOTS));
            player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, PotionEffect.INFINITE_DURATION, 0, false, false));
            player.addPotionEffect(new PotionEffect(PotionEffectType.JUMP_BOOST, PotionEffect.INFINITE_DURATION, 0, false, false));
            HeroRegistryBuilder.addStew(player, 3);
        }, HeroRegistryBuilder.none(),
                "Tier 5",
                "装备：击退 I 弓、铁剑、1 组箭、铁胸甲、铁靴子",
                "技能：常驻速度 I 与跳跃提升 I"
        );

        builder.register(HeroClass.MEDIC, "医护兵", Material.SPLASH_POTION, player -> {
            ItemStack sword = HeroItems.unbreakable(Material.IRON_SWORD);
            HeroItems.addEnchant(sword, Enchantment.KNOCKBACK, 1);
            player.getInventory().addItem(HeroItems.describe(sword, "医护兵·主武器", List.of(
                    "技能：蘑菇煲恢复量提升到 50%",
                    "近战支援职业，兼顾续航与击退"
            )));
            player.getInventory().setHelmet(HeroItems.unbreakable(Material.GOLDEN_HELMET));
            player.getInventory().setChestplate(HeroItems.unbreakable(Material.GOLDEN_CHESTPLATE));
            player.getInventory().setLeggings(HeroItems.unbreakable(Material.IRON_LEGGINGS));
            player.getInventory().setBoots(HeroItems.unbreakable(Material.IRON_BOOTS));
            HeroRegistryBuilder.addStew(player, 5);
            ItemStack splashHeal = HeroRegistryBuilder.potion(Material.SPLASH_POTION, org.bukkit.potion.PotionType.HEALING, "瞬疗药水");
            splashHeal.setAmount(3);
            player.getInventory().addItem(splashHeal);
        }, HeroRegistryBuilder.none(),
                "Tier 5",
                "装备：击退 I 铁剑、金头金胸、铁腿铁靴、5 蘑菇煲、3 瞬疗药水",
                "技能：蘑菇煲恢复量提升到 50%"
        );

        builder.register(HeroClass.LUMBERJACK, "伐木工", Material.IRON_AXE, player -> {
            ItemStack axe = HeroItems.unbreakable(Material.IRON_AXE);
            HeroItems.addEnchant(axe, Enchantment.SHARPNESS, 2);
            player.getInventory().addItem(HeroItems.describe(axe, "伐木工·主武器", List.of(
                    "技能：斧击有概率附加缓慢 II",
                    "利用减速持续追击敌人"
            )));
            player.getInventory().setHelmet(HeroItems.unbreakable(Material.IRON_HELMET));
            player.getInventory().setChestplate(HeroItems.leather(Material.LEATHER_CHESTPLATE, Color.fromRGB(139, 69, 19)));
            player.getInventory().setLeggings(HeroItems.unbreakable(Material.IRON_LEGGINGS));
            player.getInventory().setBoots(HeroItems.unbreakable(Material.IRON_BOOTS));
            HeroRegistryBuilder.addStew(player, 3);
        }, HeroRegistryBuilder.none(),
                "Tier 4",
                "装备：锋利 II 铁斧、铁头、皮革胸甲、铁腿、铁靴",
                "技能：斧击有概率附加缓慢 II"
        );

        builder.register(HeroClass.BERSERKER, "狂战士", Material.DIAMOND_AXE, player -> {
            player.getInventory().addItem(HeroItems.describe(HeroItems.unbreakable(Material.DIAMOND_AXE), "狂战士·主武器", List.of(
                    "技能：低于 30% 血量时获得力量 I 与生命恢复 I",
                    "血越低越凶猛"
            )));
            player.getInventory().setHelmet(HeroItems.unbreakable(Material.IRON_HELMET));
            player.getInventory().setChestplate(HeroItems.leather(Material.LEATHER_CHESTPLATE, Color.YELLOW));
            player.getInventory().setLeggings(HeroItems.unbreakable(Material.IRON_LEGGINGS));
            player.getInventory().setBoots(HeroItems.unbreakable(Material.IRON_BOOTS));
            HeroRegistryBuilder.addStew(player, 3);
        }, HeroRegistryBuilder.none(),
                "Tier 4",
                "装备：钻石斧、黄色皮革胸甲、铁头、铁腿、铁靴",
                "技能：低于 30% 血量时获得力量 I 与生命恢复 I"
        );

        builder.register(HeroClass.VAMPIRE, "吸血鬼", Material.REDSTONE, player -> {
            player.getInventory().addItem(HeroItems.describe(HeroItems.unbreakable(Material.IRON_SWORD), "吸血鬼·主武器", List.of(
                    "技能：右键进入 10 秒嗜血状态",
                    "期间近战命中回复造成伤害的 50%"
            )));
            player.getInventory().setItem(1, HeroItems.named(Material.REDSTONE, 1, "嗜血", List.of(
                    "右键进入 10 秒嗜血状态",
                    "期间每次近战命中回复造成伤害的 50%"
            )));
            player.getInventory().setHelmet(HeroItems.leather(Material.LEATHER_HELMET, Color.BLACK));
            player.getInventory().setChestplate(HeroItems.unbreakable(Material.IRON_CHESTPLATE));
            player.getInventory().setLeggings(HeroItems.leather(Material.LEATHER_LEGGINGS, Color.BLACK));
            player.getInventory().setBoots(HeroItems.leather(Material.LEATHER_BOOTS, Color.BLACK));
            HeroRegistryBuilder.addStew(player, 3);
        }, HeroRegistryBuilder.bindings(HeroRegistryBuilder.binding("vampire_bloodlust", Material.REDSTONE)),
                "Tier 3",
                "装备：铁剑、铁胸甲、黑色皮革头腿靴、3 蘑菇煲、红石粉",
                "技能：短时间内大幅吸血"
        );

        builder.register(HeroClass.VOID_WALKER, "虚空行者", Material.CHORUS_FRUIT, player -> {
            player.getInventory().addItem(HeroItems.describe(HeroItems.unbreakable(Material.DIAMOND_SWORD), "虚空行者·主武器", List.of(
                    "技能：普攻概率附加失明与凋零",
                    "右键可朝准星位置短距传送"
            )));
            player.getInventory().setItem(1, HeroItems.named(Material.CHORUS_FRUIT, 1, "虚空跃迁", List.of(
                    "右键传送到准星指向的位置",
                    "最大 15 格，穿墙会失败"
            )));
            player.getInventory().setHelmet(HeroItems.unbreakable(Material.IRON_HELMET));
            player.getInventory().setChestplate(HeroItems.unbreakable(Material.IRON_CHESTPLATE));
            player.getInventory().setLeggings(HeroItems.leather(Material.LEATHER_LEGGINGS, Color.BLACK));
            player.getInventory().setBoots(HeroItems.leather(Material.LEATHER_BOOTS, Color.BLACK));
            HeroRegistryBuilder.addStew(player, 3);
        }, HeroRegistryBuilder.bindings(HeroRegistryBuilder.binding("void_walk", Material.CHORUS_FRUIT)),
                "Tier 3",
                "装备：钻石剑、铁头铁胸、黑色皮革腿靴、紫颂果",
                "技能：普攻概率附加失明与凋零，右键短距传送"
        );

        builder.register(HeroClass.SPATIAL_MAGE, "空间法师", Material.ENDER_PEARL, player -> {
            ItemStack bow = HeroItems.unbreakable(Material.BOW);
            HeroItems.addEnchant(bow, Enchantment.INFINITY, 1);
            player.getInventory().addItem(HeroItems.describe(HeroItems.unbreakable(Material.IRON_SWORD), "空间法师·副武器", List.of(
                    "技能：无限使用末影珍珠",
                    "珍珠落地会震荡周围敌人"
            )));
            player.getInventory().addItem(HeroItems.describe(bow, "空间法师·长弓", List.of(
                    "技能：无限 I 长弓",
                    "配合珍珠进行拉扯与追击"
            )));
            player.getInventory().addItem(new ItemStack(Material.ARROW, 1));
            player.getInventory().setItem(1, HeroItems.named(Material.ENDER_PEARL, 1, "空间跃迁", List.of(
                    "无限使用末影珍珠",
                    "落地时对周围敌人造成范围震荡伤害"
            )));
            player.getInventory().setHelmet(HeroItems.unbreakable(Material.IRON_HELMET));
            player.getInventory().setChestplate(HeroItems.unbreakable(Material.DIAMOND_CHESTPLATE));
            player.getInventory().setBoots(HeroItems.unbreakable(Material.IRON_BOOTS));
            HeroRegistryBuilder.addStew(player, 3);
        }, HeroRegistryBuilder.bindings(HeroRegistryBuilder.binding("spatial_pearl", Material.ENDER_PEARL)),
                "Tier 3",
                "装备：铁剑、无限 I 弓、1 箭、铁头、钻石胸、铁靴、末影珍珠",
                "技能：珍珠落地无伤并震荡周围敌人"
        );

        builder.register(HeroClass.DRAGON_KNIGHT, "龙骑士", Material.BLAZE_ROD, player -> {
            ItemStack sword = HeroItems.unbreakable(Material.GOLDEN_SWORD);
            HeroItems.addEnchant(sword, Enchantment.SHARPNESS, 3);
            player.getInventory().addItem(HeroItems.describe(sword, "龙骑士·主武器", List.of(
                    "技能：免疫火焰、岩浆和爆炸伤害",
                    "右键发射火球造成范围爆炸"
            )));
            player.getInventory().setItem(1, HeroItems.named(Material.BLAZE_ROD, 1, "火球", List.of(
                    "右键发射恶魂火球",
                    "命中后小范围爆炸并点燃敌人"
            )));
            player.getInventory().setHelmet(HeroItems.unbreakable(Material.DIAMOND_HELMET));
            player.getInventory().setChestplate(HeroItems.unbreakable(Material.DIAMOND_CHESTPLATE));
            player.getInventory().setLeggings(HeroItems.unbreakable(Material.IRON_LEGGINGS));
            player.getInventory().setBoots(HeroItems.unbreakable(Material.IRON_BOOTS));
            HeroRegistryBuilder.addStew(player, 3);
        }, HeroRegistryBuilder.bindings(HeroRegistryBuilder.binding("dragon_knight_fireball", Material.BLAZE_ROD)),
                "Tier 3",
                "装备：锋利 III 金剑、钻石头钻石胸、铁腿铁靴、烈焰棒",
                "技能：免疫火焰、岩浆和爆炸伤害，右键发射火球"
        );

        builder.register(HeroClass.POISON_STINGER, "毒刺", Material.IRON_SWORD, player -> {
            ItemStack sword = HeroItems.unbreakable(Material.IRON_SWORD);
            HeroItems.addEnchant(sword, Enchantment.SHARPNESS, 1);
            ItemMeta meta = sword.getItemMeta();
            meta.displayName(Component.text("毒剑").decoration(TextDecoration.ITALIC, false));
            meta.lore(List.of(
                    Component.text("右键主武器开启 10 秒涂毒").decoration(TextDecoration.ITALIC, false),
                    Component.text("命中附加中毒 III 2 秒并获得生命恢复 II 2 秒").decoration(TextDecoration.ITALIC, false)
            ));
            sword.setItemMeta(meta);
            player.getInventory().addItem(sword);
            player.getInventory().setHelmet(HeroItems.unbreakable(Material.DIAMOND_HELMET));
            player.getInventory().setChestplate(HeroItems.leather(Material.LEATHER_CHESTPLATE, Color.LIME));
            player.getInventory().setLeggings(HeroItems.unbreakable(Material.IRON_LEGGINGS));
            player.getInventory().setBoots(HeroItems.unbreakable(Material.IRON_BOOTS));
            HeroRegistryBuilder.addStew(player, 3);
        }, HeroRegistryBuilder.none(),
                "Tier 3",
                "装备：锋利 I 毒剑、绿色皮革胸甲、钻石头、铁腿、铁靴",
                "技能：右键主武器开启涂毒，命中附毒并为自己回血"
        );

        builder.register(HeroClass.ROBOT, "机器人", Material.COMPARATOR, player -> {
            ItemStack sword = HeroItems.unbreakable(Material.DIAMOND_SWORD);
            HeroItems.addEnchant(sword, Enchantment.SHARPNESS, 1);
            player.getInventory().addItem(HeroItems.describe(sword, "机器人·主武器", List.of(
                    "技能：随机借用 Tier 1-3 的主动技能",
                    "每次借来的技能只能成功使用 1 次"
            )));
            player.getInventory().setItem(1, HeroItems.named(Material.COMPARATOR, 1, "智能性AI", List.of(
                    "右键随机获得一个 Tier 1-3 主动技能",
                    "借来的技能成功施放一次后恢复 AI 物品"
            )));
            player.getInventory().setHelmet(HeroItems.unbreakable(Material.DIAMOND_HELMET));
            player.getInventory().setChestplate(HeroItems.unbreakable(Material.IRON_CHESTPLATE));
            player.getInventory().setLeggings(HeroItems.unbreakable(Material.GOLDEN_LEGGINGS));
            player.getInventory().setBoots(HeroItems.unbreakable(Material.GOLDEN_BOOTS));
            player.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE, PotionEffect.INFINITE_DURATION, 0, false, false));
            HeroRegistryBuilder.addStew(player, 3);
        }, HeroRegistryBuilder.bindings(HeroRegistryBuilder.binding("smart_ai", Material.COMPARATOR)),
                "Tier 1",
                "装备：锋利 I 钻石剑、钻石头、铁胸、金腿金靴、智能性AI",
                "技能：随机借用一个 Tier 1-3 职业的主动技能一次"
        );

        builder.register(HeroClass.TITAN, "泰坦巨兽", Material.IRON_SWORD, player -> {
            ItemStack sword = HeroItems.unbreakable(Material.IRON_SWORD);
            HeroItems.addEnchant(sword, Enchantment.SHARPNESS, 5);
            player.getInventory().addItem(HeroItems.describe(sword, "泰坦巨兽·主武器", List.of(
                    "技能：体型放大并提升攻击距离",
                    "常驻生命恢复 II、抗性 I、缓慢 I"
            )));
            player.getInventory().setHelmet(HeroItems.unbreakable(Material.IRON_HELMET));
            player.getInventory().setChestplate(HeroItems.unbreakable(Material.DIAMOND_CHESTPLATE));
            player.getInventory().setLeggings(HeroItems.unbreakable(Material.IRON_LEGGINGS));
            player.getInventory().setBoots(HeroItems.unbreakable(Material.IRON_BOOTS));
            if (player.getAttribute(Attribute.MAX_HEALTH) != null) {
                player.getAttribute(Attribute.MAX_HEALTH).setBaseValue(40.0D);
            }
            if (player.getAttribute(Attribute.SCALE) != null) {
                player.getAttribute(Attribute.SCALE).setBaseValue(2.0D);
            }
            if (player.getAttribute(Attribute.ENTITY_INTERACTION_RANGE) != null) {
                player.getAttribute(Attribute.ENTITY_INTERACTION_RANGE).setBaseValue(4.0D);
            }
            player.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, PotionEffect.INFINITE_DURATION, 1, false, false));
            player.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE, PotionEffect.INFINITE_DURATION, 0, false, false));
            player.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, PotionEffect.INFINITE_DURATION, 0, false, false));
            player.setHealth(40.0D);
        }, HeroRegistryBuilder.none(),
                "Tier 1",
                "装备：锋利 V 铁剑、钻石胸甲，其余铁甲",
                "技能：体型放大、攻击距离提升，常驻生命恢复 II、抗性 I、缓慢 I"
        );

        builder.register(HeroClass.SHADOW_BINDER, "影缚者", Material.LEAD, player -> {
            ItemStack sword = HeroItems.unbreakable(Material.IRON_SWORD);
            HeroItems.addEnchant(sword, Enchantment.SHARPNESS, 1);
            player.getInventory().addItem(HeroItems.describe(sword, "影缚者·主武器", List.of(
                    "技能：单体禁锢与持续压制",
                    "依靠减速、虚弱和凋零打乱节奏"
            )));
            player.getInventory().setItem(1, HeroItems.named(Material.LEAD, 1, "影缚", List.of(
                    "右键锁定准星内目标",
                    "施加高额缓慢、虚弱与凋零 I"
            )));
            player.getInventory().setHelmet(HeroItems.unbreakable(Material.IRON_HELMET));
            player.getInventory().setChestplate(HeroItems.unbreakable(Material.CHAINMAIL_CHESTPLATE));
            player.getInventory().setLeggings(HeroItems.unbreakable(Material.IRON_LEGGINGS));
            player.getInventory().setBoots(HeroItems.unbreakable(Material.IRON_BOOTS));
            HeroRegistryBuilder.addStew(player, 3);
        }, HeroRegistryBuilder.bindings(HeroRegistryBuilder.binding("shadow_bind", Material.LEAD)),
                "Tier 3",
                "装备：锋利 I 铁剑、铁头、锁链胸、铁腿、铁靴、拴绳",
                "技能：单体控制并附加减速、虚弱、凋零 I"
        );

        builder.register(HeroClass.BLOOD_KNIGHT, "血骑士", Material.NETHER_WART, player -> {
            ItemStack sword = HeroItems.unbreakable(Material.DIAMOND_SWORD);
            HeroItems.addEnchant(sword, Enchantment.SHARPNESS, 1);
            player.getInventory().addItem(HeroItems.describe(sword, "血骑士·主武器", List.of(
                    "技能：消耗生命换取短时爆发",
                    "适合切入后连续压制目标"
            )));
            player.getInventory().setItem(1, HeroItems.named(Material.NETHER_WART, 1, "血祭", List.of(
                    "右键消耗生命进入强化状态",
                    "短时间内获得力量、速度与抗性"
            )));
            player.getInventory().setHelmet(HeroItems.unbreakable(Material.IRON_HELMET));
            player.getInventory().setChestplate(HeroItems.unbreakable(Material.IRON_CHESTPLATE));
            player.getInventory().setLeggings(HeroItems.unbreakable(Material.CHAINMAIL_LEGGINGS));
            player.getInventory().setBoots(HeroItems.unbreakable(Material.DIAMOND_BOOTS));
            HeroRegistryBuilder.addStew(player, 3);
        }, HeroRegistryBuilder.bindings(HeroRegistryBuilder.binding("blood_rite", Material.NETHER_WART)),
                "Tier 2",
                "装备：锋利 I 钻石剑、铁头铁胸、锁链腿、钻石靴、地狱疣",
                "技能：消耗生命，短时获得力量、速度与抗性"
        );

        builder.register(HeroClass.ARTILLERIST, "炮术师", Material.FIREWORK_STAR, player -> {
            ItemStack sword = HeroItems.unbreakable(Material.IRON_SWORD);
            HeroItems.addEnchant(sword, Enchantment.SHARPNESS, 1);
            player.getInventory().addItem(HeroItems.describe(sword, "炮术师·主武器", List.of(
                    "技能：抛射炮弹打范围压制",
                    "适合在中距离封走位和补伤害"
            )));
            player.getInventory().setItem(1, HeroItems.named(Material.FIREWORK_STAR, 1, "炮击弹", List.of(
                    "右键发射一枚缓速炮弹",
                    "命中后范围爆炸并击退敌人"
            )));
            player.getInventory().setHelmet(HeroItems.unbreakable(Material.IRON_HELMET));
            player.getInventory().setChestplate(HeroItems.unbreakable(Material.IRON_CHESTPLATE));
            player.getInventory().setLeggings(HeroItems.unbreakable(Material.CHAINMAIL_LEGGINGS));
            player.getInventory().setBoots(HeroItems.unbreakable(Material.IRON_BOOTS));
            HeroRegistryBuilder.addStew(player, 3);
        }, HeroRegistryBuilder.bindings(HeroRegistryBuilder.binding("artillery_shell", Material.FIREWORK_STAR)),
                "Tier 3",
                "装备：锋利 I 铁剑、铁头铁胸、锁链腿、铁靴、烟火之星",
                "技能：发射炮弹，范围爆炸并击退敌人"
        );

        builder.register(HeroClass.GRAVEKEEPER, "守墓人", Material.SKELETON_SKULL, player -> {
            ItemStack sword = HeroItems.unbreakable(Material.IRON_SWORD);
            HeroItems.addEnchant(sword, Enchantment.SHARPNESS, 1);
            player.getInventory().addItem(HeroItems.describe(sword, "守墓人·主武器", List.of(
                    "技能：召唤墓地亡灵协助射击",
                    "依靠召唤物持续牵制远处敌人"
            )));
            player.getInventory().setItem(1, HeroItems.named(Material.SKELETON_SKULL, 1, "墓军", List.of(
                    "右键召唤 2 只骷髅弓手",
                    "骷髅会优先攻击附近敌方玩家"
            )));
            player.getInventory().setHelmet(HeroItems.unbreakable(Material.IRON_HELMET));
            player.getInventory().setChestplate(HeroItems.unbreakable(Material.IRON_CHESTPLATE));
            player.getInventory().setLeggings(HeroItems.unbreakable(Material.CHAINMAIL_LEGGINGS));
            player.getInventory().setBoots(HeroItems.unbreakable(Material.IRON_BOOTS));
            HeroRegistryBuilder.addStew(player, 3);
        }, HeroRegistryBuilder.bindings(HeroRegistryBuilder.binding("grave_legion", Material.SKELETON_SKULL)),
                "Tier 4",
                "装备：锋利 I 铁剑、铁头铁胸、锁链腿、铁靴、骷髅头",
                "技能：召唤 2 只骷髅弓手协助作战"
        );
    }
}
