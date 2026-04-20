package jdd.war.hero.definitions;

import java.util.List;
import jdd.war.hero.HeroClass;
import jdd.war.hero.HeroDefinitionGroup;
import jdd.war.hero.HeroItems;
import jdd.war.hero.HeroRegistryBuilder;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public final class PowerHeroDefinitions implements HeroDefinitionGroup {
    @Override
    public void register(HeroRegistryBuilder builder) {
        builder.register(HeroClass.ACHILLES, "跟腱", Material.FIREWORK_ROCKET, player -> {
            player.getInventory().addItem(HeroItems.unbreakable(Material.DIAMOND_SWORD));
            player.getInventory().setItem(1, HeroItems.named(Material.FIREWORK_ROCKET, "跟腱技能", List.of("向前上方弹射一段距离")));
            player.getInventory().setHelmet(HeroItems.unbreakable(Material.IRON_HELMET));
            player.getInventory().setChestplate(HeroItems.unbreakable(Material.CHAINMAIL_CHESTPLATE));
            player.getInventory().setLeggings(HeroItems.unbreakable(Material.IRON_LEGGINGS));
            player.getInventory().setBoots(HeroItems.unbreakable(Material.DIAMOND_BOOTS));
            HeroRegistryBuilder.addStew(player, 3);
        }, HeroRegistryBuilder.bindings(HeroRegistryBuilder.binding("achilles_leap", Material.FIREWORK_ROCKET)),
                "Tier 3",
                "装备：钻石剑、铁头盔、锁链胸甲、铁护腿、钻石靴子、烟花",
                "技能：向前上方弹射突进");

        builder.register(HeroClass.ABYSS, "深渊", Material.ENDER_EYE, player -> {
            ItemStack sword = HeroItems.unbreakable(Material.DIAMOND_SWORD);
            HeroItems.addEnchant(sword, Enchantment.KNOCKBACK, 2);
            player.getInventory().addItem(HeroItems.describe(sword, "深渊·主武器", List.of("技能：重装击退近战", "利用高击退压开敌人节奏")));
            player.getInventory().setHelmet(HeroItems.unbreakable(Material.IRON_HELMET));
            player.getInventory().setChestplate(HeroItems.unbreakable(Material.IRON_CHESTPLATE));
            player.getInventory().setLeggings(HeroItems.unbreakable(Material.IRON_LEGGINGS));
            player.getInventory().setBoots(HeroItems.leather(Material.LEATHER_BOOTS, Color.BLACK));
            HeroRegistryBuilder.addStew(player, 3);
        }, HeroRegistryBuilder.none(),
                "Tier 3",
                "装备：击退 II 钻石剑、铁头盔、铁胸甲、铁护腿、黑色皮革靴子",
                "技能：重装击退近战");

        builder.register(HeroClass.DRAGON_BREATH, "龙息", Material.DRAGON_BREATH, player -> {
            player.getInventory().addItem(HeroItems.unbreakable(Material.DIAMOND_SWORD));
            player.getInventory().setItem(1, HeroItems.named(Material.FIRE_CHARGE, "龙息技能", List.of("喷出两段龙息冲击", "常驻抗火")));
            player.getInventory().setHelmet(HeroItems.leather(Material.LEATHER_HELMET, Color.RED));
            player.getInventory().setChestplate(HeroItems.unbreakable(Material.DIAMOND_CHESTPLATE));
            player.getInventory().setLeggings(HeroItems.unbreakable(Material.IRON_LEGGINGS));
            player.getInventory().setBoots(HeroItems.leather(Material.LEATHER_BOOTS, Color.RED));
            player.addPotionEffect(new PotionEffect(PotionEffectType.FIRE_RESISTANCE, PotionEffect.INFINITE_DURATION, 0, false, false));
            HeroRegistryBuilder.addStew(player, 3);
        }, HeroRegistryBuilder.bindings(HeroRegistryBuilder.binding("dragon_breath", Material.FIRE_CHARGE)),
                "Tier 2",
                "装备：钻石剑、红色皮革头盔、钻石胸甲、铁护腿、红色皮革靴子、烈焰弹",
                "技能：两段范围喷息");

        builder.register(HeroClass.EMBER, "余烬", Material.BLAZE_POWDER, player -> {
            ItemStack sword = HeroItems.unbreakable(Material.IRON_SWORD);
            HeroItems.addEnchant(sword, Enchantment.FIRE_ASPECT, 1);
            ItemStack bow = HeroItems.unbreakable(Material.BOW);
            HeroItems.addEnchant(bow, Enchantment.FLAME, 1);
            player.getInventory().addItem(HeroItems.describe(sword, "余烬·主武器", List.of("技能：全套火焰保护 V", "近战命中可附带燃烧")));
            player.getInventory().addItem(HeroItems.describe(bow, "余烬·长弓", List.of("技能：火矢远程压制", "利用火焰持续消耗目标")));
            player.getInventory().addItem(new ItemStack(Material.ARROW, 64));

            ItemStack helmet = HeroItems.leather(Material.LEATHER_HELMET, Color.ORANGE);
            ItemStack chest = HeroItems.unbreakable(Material.IRON_CHESTPLATE);
            ItemStack leggings = HeroItems.leather(Material.LEATHER_LEGGINGS, Color.ORANGE);
            ItemStack boots = HeroItems.unbreakable(Material.IRON_BOOTS);
            HeroItems.addEnchant(helmet, Enchantment.FIRE_PROTECTION, 5);
            HeroItems.addEnchant(chest, Enchantment.FIRE_PROTECTION, 5);
            HeroItems.addEnchant(leggings, Enchantment.FIRE_PROTECTION, 5);
            HeroItems.addEnchant(boots, Enchantment.FIRE_PROTECTION, 5);
            player.getInventory().setHelmet(helmet);
            player.getInventory().setChestplate(chest);
            player.getInventory().setLeggings(leggings);
            player.getInventory().setBoots(boots);
            HeroRegistryBuilder.addStew(player, 3);
        }, HeroRegistryBuilder.none(),
                "Tier 2",
                "装备：火附加铁剑、火矢弓、橙色皮革头盔、铁胸甲、橙色皮革护腿、铁靴子、一组箭",
                "技能：全套火焰保护 V");

        builder.register(HeroClass.FISHERMAN, "渔夫", Material.FISHING_ROD, player -> {
            ItemStack rod = HeroItems.unbreakable(Material.FISHING_ROD);
            HeroItems.addEnchant(rod, Enchantment.KNOCKBACK, 2);
            player.getInventory().addItem(HeroItems.describe(HeroItems.unbreakable(Material.DIAMOND_SWORD), "渔夫·主武器", List.of("技能：收杆时更强地拖拽目标", "把敌人拉近后进行近战压制")));
            player.getInventory().addItem(HeroItems.describe(rod, "渔夫·鱼竿", List.of("收杆可强力拖拽敌人", "近战附带击退 II 防止被贴身")));
            player.getInventory().setHelmet(HeroItems.leather(Material.LEATHER_HELMET, Color.BLUE));
            player.getInventory().setChestplate(HeroItems.unbreakable(Material.IRON_CHESTPLATE));
            player.getInventory().setLeggings(HeroItems.unbreakable(Material.IRON_LEGGINGS));
            player.getInventory().setBoots(HeroItems.leather(Material.LEATHER_BOOTS, Color.BLUE));
            HeroRegistryBuilder.addStew(player, 3);
        }, HeroRegistryBuilder.none(),
                "Tier 3",
                "装备：钻石剑、蓝色皮革头盔、铁胸甲、铁护腿、蓝色皮革靴子、击退 II 钓鱼竿",
                "技能：收杆时更强地拖拽目标");

        builder.register(HeroClass.ALCHEMIST, "炼金术士", Material.BREWING_STAND, player -> {
            player.getInventory().addItem(HeroItems.describe(HeroItems.unbreakable(Material.DIAMOND_SWORD), "炼金术士·主武器", List.of("技能：依靠药水灵活作战", "配合药水切换攻防节奏")));
            player.getInventory().setHelmet(HeroItems.leather(Material.LEATHER_HELMET, Color.FUCHSIA));
            player.getInventory().setChestplate(HeroItems.unbreakable(Material.IRON_CHESTPLATE));
            player.getInventory().setLeggings(HeroItems.leather(Material.LEATHER_LEGGINGS, Color.FUCHSIA));
            player.getInventory().setBoots(HeroItems.unbreakable(Material.IRON_BOOTS));
            player.getInventory().addItem(HeroRegistryBuilder.potion(Material.POTION, org.bukkit.potion.PotionType.HEALING, "治疗药水"));
            player.getInventory().addItem(HeroRegistryBuilder.potion(Material.POTION, org.bukkit.potion.PotionType.SWIFTNESS, "迅捷药水"));
            player.getInventory().addItem(HeroRegistryBuilder.potion(Material.SPLASH_POTION, org.bukkit.potion.PotionType.POISON, "剧毒喷溅药水"));
            HeroRegistryBuilder.addStew(player, 3);
        }, HeroRegistryBuilder.none(),
                "Tier 3",
                "装备：钻石剑、粉色皮革头盔、铁胸甲、粉色皮革护腿、铁靴子、多种药水",
                "技能：依靠药水灵活作战");

        builder.register(HeroClass.ASURA, "阿修罗", Material.SLIME_BALL, player -> {
            player.getInventory().addItem(HeroItems.unbreakable(Material.DIAMOND_SWORD));
            player.getInventory().setItem(1, HeroItems.named(Material.SLIME_BALL, "阿修罗技能", List.of("传送到附近随机目标身边")));
            player.getInventory().setHelmet(HeroItems.unbreakable(Material.IRON_HELMET));
            player.getInventory().setChestplate(HeroItems.unbreakable(Material.CHAINMAIL_CHESTPLATE));
            player.getInventory().setLeggings(HeroItems.unbreakable(Material.IRON_LEGGINGS));
            player.getInventory().setBoots(HeroItems.unbreakable(Material.IRON_BOOTS));
            HeroRegistryBuilder.addStew(player, 3);
        }, HeroRegistryBuilder.bindings(HeroRegistryBuilder.binding("asura_step", Material.SLIME_BALL)),
                "Tier 2",
                "装备：钻石剑、铁头盔、锁链胸甲、铁护腿、铁靴子、粘液球",
                "技能：随机锁定附近目标并突进");

        builder.register(HeroClass.TOXIC_LIZARD, "毒蜥", Material.FERMENTED_SPIDER_EYE, player -> {
            player.getInventory().addItem(HeroItems.unbreakable(Material.DIAMOND_SWORD));
            player.getInventory().setItem(1, HeroItems.named(Material.ENDER_EYE, "毒蜥技能", List.of("对周围敌人施加中毒 I", "持续 7 秒")));
            player.getInventory().setHelmet(HeroItems.leather(Material.LEATHER_HELMET, Color.GREEN));
            player.getInventory().setChestplate(HeroItems.unbreakable(Material.IRON_CHESTPLATE));
            player.getInventory().setLeggings(HeroItems.unbreakable(Material.IRON_LEGGINGS));
            player.getInventory().setBoots(HeroItems.unbreakable(Material.IRON_BOOTS));
            HeroRegistryBuilder.addStew(player, 3);
        }, HeroRegistryBuilder.bindings(HeroRegistryBuilder.binding("toxic_wave", Material.ENDER_EYE)),
                "Tier 5",
                "装备：钻石剑、绿色皮革头盔、铁胸甲、铁护腿、铁靴子、末影之眼",
                "技能：范围施加中毒 I 7 秒");

        builder.register(HeroClass.WAR_WRAITH, "战争亡魂", Material.SHIELD, player -> {
            ItemStack sword = HeroItems.unbreakable(Material.DIAMOND_SWORD);
            HeroItems.addEnchant(sword, Enchantment.SHARPNESS, 1);
            player.getInventory().addItem(HeroItems.describe(sword, "战争亡魂·主武器", List.of("技能：高容错正面推进", "金苹果与盾牌提高容错")));
            player.getInventory().addItem(new ItemStack(Material.GOLDEN_APPLE));
            player.getInventory().addItem(HeroItems.unbreakable(Material.SHIELD));
            HeroRegistryBuilder.ironSet(player);
            HeroRegistryBuilder.addStew(player, 3);
        }, HeroRegistryBuilder.none(),
                "Tier 2",
                "装备：锋利 I 钻石剑、铁套、金苹果、盾牌",
                "技能：高容错正面推进");

        builder.register(HeroClass.GODWALKER, "神行者", Material.RABBIT_FOOT, player -> {
            ItemStack sword = HeroItems.unbreakable(Material.DIAMOND_SWORD);
            HeroItems.addEnchant(sword, Enchantment.SHARPNESS, 1);
            player.getInventory().addItem(HeroItems.describe(sword, "神行者·主武器", List.of("技能：常驻速度 III 与跳跃 III", "高机动近战职业")));
            player.getInventory().addItem(HeroItems.unbreakable(Material.SHIELD));
            HeroRegistryBuilder.leatherSet(player, Color.WHITE);
            player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, PotionEffect.INFINITE_DURATION, 2, false, false));
            player.addPotionEffect(new PotionEffect(PotionEffectType.JUMP_BOOST, PotionEffect.INFINITE_DURATION, 2, false, false));
            HeroRegistryBuilder.addStew(player, 3);
        }, HeroRegistryBuilder.none(),
                "Tier 1",
                "装备：锋利 I 钻石剑、白色皮革套、盾牌",
                "技能：常驻速度 III 与跳跃 III");

        builder.register(HeroClass.MUTANT_COMBAT_ZOMBIE, "异变作战僵尸", Material.ZOMBIE_HEAD, player -> {
            if (player.getAttribute(org.bukkit.attribute.Attribute.MAX_HEALTH) != null) {
                player.getAttribute(org.bukkit.attribute.Attribute.MAX_HEALTH).setBaseValue(80.0D);
            }
            player.setHealth(80.0D);
            ItemStack sword = HeroItems.unbreakable(Material.IRON_SWORD);
            ItemStack leggings = HeroItems.leather(Material.LEATHER_LEGGINGS, Color.GREEN);
            HeroItems.addEnchant(sword, Enchantment.UNBREAKING, 10);
            HeroItems.addEnchant(leggings, Enchantment.UNBREAKING, 10);
            player.getInventory().addItem(HeroItems.describe(sword, "异变作战僵尸·主武器", List.of("技能：生命上限 80，常驻生命恢复 III", "没有蘑菇煲，依靠高血量推进")));
            player.getInventory().setLeggings(leggings);
            player.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, PotionEffect.INFINITE_DURATION, 2, false, false));
            player.addPotionEffect(new PotionEffect(PotionEffectType.MINING_FATIGUE, PotionEffect.INFINITE_DURATION, 2, false, false));
        }, HeroRegistryBuilder.none(),
                "Tier 1",
                "装备：附魔铁剑、附魔皮革护腿",
                "技能：生命上限 80，常驻生命恢复 III，无蘑菇煲");

        builder.register(HeroClass.NIGHTMARE, "梦魇", Material.NETHERITE_AXE, player -> {
            ItemStack axe = HeroItems.unbreakable(Material.DIAMOND_AXE);
            HeroItems.addEnchant(axe, Enchantment.SHARPNESS, 3);
            player.getInventory().addItem(HeroItems.describe(axe, "梦魇·主武器", List.of("技能：常驻速度 I", "利用高爆发斧头快速压血线")));
            player.getInventory().addItem(HeroItems.unbreakable(Material.SHIELD));
            player.getInventory().setHelmet(HeroItems.leather(Material.LEATHER_HELMET, Color.BLACK));
            ItemStack chest = HeroItems.leather(Material.LEATHER_CHESTPLATE, Color.BLACK);
            ItemStack leggings = HeroItems.unbreakable(Material.CHAINMAIL_LEGGINGS);
            HeroItems.addEnchant(chest, Enchantment.PROJECTILE_PROTECTION, 6);
            HeroItems.addEnchant(leggings, Enchantment.PROJECTILE_PROTECTION, 6);
            player.getInventory().setChestplate(chest);
            player.getInventory().setLeggings(leggings);
            player.getInventory().setBoots(HeroItems.unbreakable(Material.CHAINMAIL_BOOTS));
            player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, PotionEffect.INFINITE_DURATION, 0, false, false));
            HeroRegistryBuilder.addStew(player, 3);
        }, HeroRegistryBuilder.none(),
                "Tier 2",
                "装备：锋利 III 钻石斧、黑色皮革头盔、附魔黑色皮革胸甲、附魔锁链护腿、锁链靴子、盾牌",
                "技能：常驻速度 I");

        builder.register(HeroClass.MARTIAL_ARTIST, "格斗大师", Material.BLAZE_POWDER, player -> {
            HeroRegistryBuilder.leatherSet(player, Color.ORANGE);
            player.addPotionEffect(new PotionEffect(PotionEffectType.STRENGTH, PotionEffect.INFINITE_DURATION, 1, false, false));
            player.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE, PotionEffect.INFINITE_DURATION, 1, false, false));
            HeroRegistryBuilder.addStew(player, 3);
        }, HeroRegistryBuilder.none(),
                "Tier 1",
                "装备：橙色皮革套",
                "技能：常驻力量 II 与抗性提升 II");

        builder.register(HeroClass.DEATH_SNIPER, "死亡狙击手", Material.CROSSBOW, player -> {
            ItemStack bow = HeroItems.unbreakable(Material.BOW);
            HeroItems.addEnchant(bow, Enchantment.POWER, 3);
            player.getInventory().addItem(HeroItems.describe(HeroItems.unbreakable(Material.DIAMOND_SWORD), "死亡狙击手·副武器", List.of("技能：常驻虚弱 III", "近身时用于自保")));
            player.getInventory().addItem(HeroItems.describe(bow, "死亡狙击手·长弓", List.of("技能：力量 III 远程压制", "三组箭保证持续火力")));
            player.getInventory().addItem(new ItemStack(Material.ARROW, 64));
            player.getInventory().addItem(new ItemStack(Material.ARROW, 64));
            player.getInventory().addItem(new ItemStack(Material.ARROW, 64));
            player.getInventory().setHelmet(HeroItems.leather(Material.LEATHER_HELMET, Color.RED));
            player.getInventory().setChestplate(HeroItems.unbreakable(Material.IRON_CHESTPLATE));
            player.getInventory().setLeggings(HeroItems.leather(Material.LEATHER_LEGGINGS, Color.RED));
            player.getInventory().setBoots(HeroItems.leather(Material.LEATHER_BOOTS, Color.RED));
            player.addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS, PotionEffect.INFINITE_DURATION, 2, false, false));
            HeroRegistryBuilder.addStew(player, 3);
        }, HeroRegistryBuilder.none(),
                "Tier 2",
                "装备：力量 III 弓、钻石剑、红色皮革头盔、铁胸甲、红色皮革护腿、红色皮革靴子、三组箭",
                "技能：常驻虚弱 III");
    }
}
