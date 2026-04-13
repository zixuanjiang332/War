package jdd.war.hero.definitions;

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
        builder.register(HeroClass.ACHILLES, "飞焰", Material.FIREWORK_ROCKET, player -> {
            player.getInventory().addItem(HeroItems.unbreakable(Material.DIAMOND_SWORD));
            player.getInventory().setItem(1, HeroItems.named(Material.FIREWORK_ROCKET, "飞焰技能"));
            player.getInventory().setHelmet(HeroItems.unbreakable(Material.IRON_HELMET));
            player.getInventory().setChestplate(HeroItems.unbreakable(Material.CHAINMAIL_CHESTPLATE));
            player.getInventory().setLeggings(HeroItems.unbreakable(Material.IRON_LEGGINGS));
            player.getInventory().setBoots(HeroItems.unbreakable(Material.DIAMOND_BOOTS));
            HeroRegistryBuilder.addStew(player, 3);
        }, HeroRegistryBuilder.bindings(HeroRegistryBuilder.binding("achilles_leap", Material.FIREWORK_ROCKET)), "Tier 3", "钻石剑 + 烟花", "右键向前上方弹射");

        builder.register(HeroClass.ABYSS, "深渊", Material.ENDER_EYE, player -> {
            ItemStack sword = HeroItems.unbreakable(Material.DIAMOND_SWORD);
            HeroItems.addEnchant(sword, Enchantment.KNOCKBACK, 2);
            player.getInventory().addItem(sword);
            player.getInventory().setHelmet(HeroItems.unbreakable(Material.IRON_HELMET));
            player.getInventory().setChestplate(HeroItems.unbreakable(Material.IRON_CHESTPLATE));
            player.getInventory().setLeggings(HeroItems.unbreakable(Material.IRON_LEGGINGS));
            player.getInventory().setBoots(HeroItems.leather(Material.LEATHER_BOOTS, Color.BLACK));
            HeroRegistryBuilder.addStew(player, 3);
        }, HeroRegistryBuilder.none(), "Tier 3", "击退 II 钻石剑", "重甲近战");

        builder.register(HeroClass.DRAGON_BREATH, "龙炎", Material.DRAGON_BREATH, player -> {
            player.getInventory().addItem(HeroItems.unbreakable(Material.DIAMOND_SWORD));
            player.getInventory().setItem(1, HeroItems.named(Material.FIRE_CHARGE, "龙炎技能"));
            player.getInventory().setHelmet(HeroItems.leather(Material.LEATHER_HELMET, Color.RED));
            player.getInventory().setChestplate(HeroItems.unbreakable(Material.DIAMOND_CHESTPLATE));
            player.getInventory().setLeggings(HeroItems.unbreakable(Material.IRON_LEGGINGS));
            player.getInventory().setBoots(HeroItems.leather(Material.LEATHER_BOOTS, Color.RED));
            player.addPotionEffect(new PotionEffect(PotionEffectType.FIRE_RESISTANCE, PotionEffect.INFINITE_DURATION, 0, false, false));
            HeroRegistryBuilder.addStew(player, 3);
        }, HeroRegistryBuilder.bindings(HeroRegistryBuilder.binding("dragon_breath", Material.FIRE_CHARGE)), "Tier 2", "钻石剑 + 烈焰弹", "常驻抗火，范围灼烧");

        builder.register(HeroClass.EMBER, "烬弓", Material.BLAZE_POWDER, player -> {
            ItemStack sword = HeroItems.unbreakable(Material.IRON_SWORD);
            HeroItems.addEnchant(sword, Enchantment.FIRE_ASPECT, 1);
            ItemStack bow = HeroItems.unbreakable(Material.BOW);
            HeroItems.addEnchant(bow, Enchantment.FLAME, 1);
            player.getInventory().addItem(sword);
            player.getInventory().addItem(bow);
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
        }, HeroRegistryBuilder.none(), "Tier 2", "火焰附加铁剑 + 火矢弓", "全套火焰保护 V");

        builder.register(HeroClass.FISHERMAN, "钩手", Material.FISHING_ROD, player -> {
            player.getInventory().addItem(HeroItems.unbreakable(Material.DIAMOND_SWORD));
            player.getInventory().addItem(HeroItems.unbreakable(Material.FISHING_ROD));
            player.getInventory().setHelmet(HeroItems.leather(Material.LEATHER_HELMET, Color.BLUE));
            player.getInventory().setChestplate(HeroItems.unbreakable(Material.IRON_CHESTPLATE));
            player.getInventory().setLeggings(HeroItems.unbreakable(Material.IRON_LEGGINGS));
            player.getInventory().setBoots(HeroItems.leather(Material.LEATHER_BOOTS, Color.BLUE));
            HeroRegistryBuilder.addStew(player, 3);
        }, HeroRegistryBuilder.none(), "Tier 3", "钻石剑 + 钓鱼竿", "收杆时强化拉扯");

        builder.register(HeroClass.ALCHEMIST, "药师", Material.BREWING_STAND, player -> {
            player.getInventory().addItem(HeroItems.unbreakable(Material.DIAMOND_SWORD));
            player.getInventory().setHelmet(HeroItems.leather(Material.LEATHER_HELMET, Color.FUCHSIA));
            player.getInventory().setChestplate(HeroItems.leather(Material.LEATHER_CHESTPLATE, Color.FUCHSIA));
            player.getInventory().setLeggings(HeroItems.leather(Material.LEATHER_LEGGINGS, Color.FUCHSIA));
            player.getInventory().setBoots(HeroItems.unbreakable(Material.IRON_BOOTS));
            player.getInventory().addItem(HeroRegistryBuilder.potion(Material.POTION, org.bukkit.potion.PotionType.HEALING, "治疗药水"));
            player.getInventory().addItem(HeroRegistryBuilder.potion(Material.POTION, org.bukkit.potion.PotionType.SWIFTNESS, "迅捷药水"));
            player.getInventory().addItem(HeroRegistryBuilder.potion(Material.SPLASH_POTION, org.bukkit.potion.PotionType.POISON, "剧毒喷溅药水"));
            HeroRegistryBuilder.addStew(player, 3);
        }, HeroRegistryBuilder.none(), "Tier 3", "钻石剑 + 多种药水", "每种药水各两瓶");

        builder.register(HeroClass.ASURA, "修罗", Material.SLIME_BALL, player -> {
            player.getInventory().addItem(HeroItems.unbreakable(Material.DIAMOND_SWORD));
            player.getInventory().setItem(1, HeroItems.named(Material.SLIME_BALL, "修罗技能"));
            player.getInventory().setHelmet(HeroItems.unbreakable(Material.IRON_HELMET));
            player.getInventory().setChestplate(HeroItems.unbreakable(Material.CHAINMAIL_CHESTPLATE));
            player.getInventory().setLeggings(HeroItems.unbreakable(Material.IRON_LEGGINGS));
            player.getInventory().setBoots(HeroItems.unbreakable(Material.IRON_BOOTS));
            HeroRegistryBuilder.addStew(player, 3);
        }, HeroRegistryBuilder.bindings(HeroRegistryBuilder.binding("asura_step", Material.SLIME_BALL)), "Tier 2", "钻石剑 + 粘液球", "随机突袭附近一名目标");

        builder.register(HeroClass.TOXIC_LIZARD, "毒蜥", Material.FERMENTED_SPIDER_EYE, player -> {
            player.getInventory().addItem(HeroItems.unbreakable(Material.DIAMOND_SWORD));
            player.getInventory().setItem(1, HeroItems.named(Material.ENDER_EYE, "毒蜥技能"));
            player.getInventory().setHelmet(HeroItems.leather(Material.LEATHER_HELMET, Color.GREEN));
            player.getInventory().setChestplate(HeroItems.unbreakable(Material.IRON_CHESTPLATE));
            player.getInventory().setLeggings(HeroItems.unbreakable(Material.IRON_LEGGINGS));
            player.getInventory().setBoots(HeroItems.unbreakable(Material.IRON_BOOTS));
            HeroRegistryBuilder.addStew(player, 3);
        }, HeroRegistryBuilder.bindings(HeroRegistryBuilder.binding("toxic_wave", Material.ENDER_EYE)), "Tier 3", "钻石剑 + 末影之眼", "右键对周围玩家施加中毒 I");

        builder.register(HeroClass.WAR_WRAITH, "战魂", Material.SHIELD, player -> {
            ItemStack sword = HeroItems.unbreakable(Material.DIAMOND_SWORD);
            HeroItems.addEnchant(sword, Enchantment.SHARPNESS, 1);
            player.getInventory().addItem(sword);
            player.getInventory().addItem(new ItemStack(Material.GOLDEN_APPLE));
            player.getInventory().addItem(HeroItems.unbreakable(Material.SHIELD));
            HeroRegistryBuilder.ironSet(player);
            HeroRegistryBuilder.addStew(player, 3);
        }, HeroRegistryBuilder.none(), "Tier 2", "锋利 I 钻石剑 + 铁套", "自带金苹果和盾牌");

        builder.register(HeroClass.GODWALKER, "神行", Material.RABBIT_FOOT, player -> {
            ItemStack sword = HeroItems.unbreakable(Material.DIAMOND_SWORD);
            HeroItems.addEnchant(sword, Enchantment.SHARPNESS, 1);
            player.getInventory().addItem(sword);
            player.getInventory().addItem(new ItemStack(Material.GOLDEN_APPLE));
            player.getInventory().addItem(HeroItems.unbreakable(Material.SHIELD));
            HeroRegistryBuilder.ironSet(player);
            player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, PotionEffect.INFINITE_DURATION, 2, false, false));
            player.addPotionEffect(new PotionEffect(PotionEffectType.JUMP_BOOST, PotionEffect.INFINITE_DURATION, 1, false, false));
            HeroRegistryBuilder.addStew(player, 3);
        }, HeroRegistryBuilder.none(), "Tier 1", "锋利 I 钻石剑 + 铁套", "常驻速度 III 与跳跃提升 II");

        builder.register(HeroClass.MUTANT_COMBAT_ZOMBIE, "尸王", Material.ZOMBIE_HEAD, player -> {
            if (player.getAttribute(org.bukkit.attribute.Attribute.MAX_HEALTH) != null) {
                player.getAttribute(org.bukkit.attribute.Attribute.MAX_HEALTH).setBaseValue(80.0D);
            }
            player.setHealth(80.0D);
            ItemStack sword = HeroItems.unbreakable(Material.IRON_SWORD);
            ItemStack leggings = HeroItems.leather(Material.LEATHER_LEGGINGS, Color.GREEN);
            HeroItems.addEnchant(sword, Enchantment.UNBREAKING, 10);
            HeroItems.addEnchant(leggings, Enchantment.UNBREAKING, 10);
            player.getInventory().addItem(sword);
            player.getInventory().setLeggings(leggings);
            player.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, PotionEffect.INFINITE_DURATION, 2, false, false));
            player.addPotionEffect(new PotionEffect(PotionEffectType.MINING_FATIGUE, PotionEffect.INFINITE_DURATION, 2, false, false));
        }, HeroRegistryBuilder.none(), "Tier 1", "生命上限 80", "常驻生命恢复 III，无蘑菇煲");

        builder.register(HeroClass.NIGHTMARE, "梦魇", Material.NETHERITE_AXE, player -> {
            ItemStack axe = HeroItems.unbreakable(Material.DIAMOND_AXE);
            HeroItems.addEnchant(axe, Enchantment.SHARPNESS, 3);
            player.getInventory().addItem(axe);
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
        }, HeroRegistryBuilder.none(), "Tier 2", "锋利 III 钻石斧 + 盾牌", "常驻速度 I");

        builder.register(HeroClass.MARTIAL_ARTIST, "拳皇", Material.BLAZE_POWDER, player -> {
            HeroRegistryBuilder.leatherSet(player, Color.ORANGE);
            player.addPotionEffect(new PotionEffect(PotionEffectType.STRENGTH, PotionEffect.INFINITE_DURATION, 1, false, false));
            player.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE, PotionEffect.INFINITE_DURATION, 1, false, false));
            HeroRegistryBuilder.addStew(player, 3);
        }, HeroRegistryBuilder.none(), "Tier 1", "皮革套", "常驻力量 II 与抗性提升 II");

        builder.register(HeroClass.DEATH_SNIPER, "狙神", Material.CROSSBOW, player -> {
            ItemStack bow = HeroItems.unbreakable(Material.BOW);
            HeroItems.addEnchant(bow, Enchantment.POWER, 3);
            player.getInventory().addItem(HeroItems.unbreakable(Material.DIAMOND_SWORD));
            player.getInventory().addItem(bow);
            player.getInventory().addItem(new ItemStack(Material.ARROW, 64));
            player.getInventory().addItem(new ItemStack(Material.ARROW, 64));
            player.getInventory().addItem(new ItemStack(Material.ARROW, 64));
            player.getInventory().setHelmet(HeroItems.leather(Material.LEATHER_HELMET, Color.RED));
            player.getInventory().setChestplate(HeroItems.unbreakable(Material.IRON_CHESTPLATE));
            player.getInventory().setLeggings(HeroItems.leather(Material.LEATHER_LEGGINGS, Color.RED));
            player.getInventory().setBoots(HeroItems.leather(Material.LEATHER_BOOTS, Color.RED));
            player.addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS, PotionEffect.INFINITE_DURATION, 2, false, false));
            HeroRegistryBuilder.addStew(player, 3);
        }, HeroRegistryBuilder.none(), "Tier 2", "力量 III 弓 + 三组箭", "常驻虚弱 III");
    }
}
