package jdd.war.hero.definitions;

import jdd.war.hero.HeroClass;
import jdd.war.hero.HeroDefinitionGroup;
import jdd.war.hero.HeroItems;
import jdd.war.hero.HeroRegistryBuilder;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public final class SpecialHeroDefinitions implements HeroDefinitionGroup {
    @Override
    public void register(HeroRegistryBuilder builder) {
        builder.register(HeroClass.SACRED_WAR, "圣战", Material.GOLD_NUGGET, player -> {
            player.getInventory().addItem(HeroItems.unbreakable(Material.IRON_SWORD));
            player.getInventory().setItem(1, HeroItems.named(Material.GOLD_NUGGET, "圣战技能"));
            HeroRegistryBuilder.ironSet(player);
            HeroRegistryBuilder.addStew(player, 3);
        }, HeroRegistryBuilder.bindings(HeroRegistryBuilder.binding("sacred_war_guard", Material.GOLD_NUGGET)), "Tier 3", "铁剑 + 铁套", "右键恢复 2 颗黄心", "技能冷却 15 秒");

        builder.register(HeroClass.WINDWALKER, "风行", Material.FEATHER, player -> {
            player.getInventory().addItem(HeroItems.unbreakable(Material.DIAMOND_SWORD));
            player.getInventory().setItem(1, HeroItems.named(Material.FEATHER, "风行·升空"));
            player.getInventory().setItem(2, HeroItems.named(Material.SUGAR, "风行·突进"));
            player.getInventory().setHelmet(HeroItems.leather(Material.LEATHER_HELMET, Color.WHITE));
            player.getInventory().setChestplate(HeroItems.unbreakable(Material.CHAINMAIL_CHESTPLATE));
            player.getInventory().setLeggings(HeroItems.unbreakable(Material.IRON_LEGGINGS));
            player.getInventory().setBoots(HeroItems.unbreakable(Material.IRON_BOOTS));
            HeroRegistryBuilder.addStew(player, 3);
        }, HeroRegistryBuilder.bindings(
                HeroRegistryBuilder.binding("windwalker_rise", Material.FEATHER),
                HeroRegistryBuilder.binding("windwalker_dash", Material.SUGAR)
        ), "Tier 2", "钻石剑 + 轻甲", "羽毛升空并震伤周围玩家", "糖触发突进并震伤周围玩家");

        builder.register(HeroClass.HOMELANDER, "祖国人", Material.ELYTRA, player -> {
            player.getInventory().addItem(HeroItems.unbreakable(Material.DIAMOND_SWORD));
            player.getInventory().setItem(1, HeroItems.named(Material.PHANTOM_MEMBRANE, "祖国人技能"));
            HeroRegistryBuilder.ironSet(player);
            HeroRegistryBuilder.addStew(player, 3);
        }, HeroRegistryBuilder.bindings(HeroRegistryBuilder.binding("homelander_flight", Material.PHANTOM_MEMBRANE)), "Tier 1", "钻石剑 + 铁套", "右键获得 8 秒飞行");

        builder.register(HeroClass.OP_CLASS, "OP", Material.NETHERITE_HELMET, player -> {
            var sword = HeroItems.unbreakable(Material.NETHERITE_SWORD);
            HeroItems.addEnchant(sword, Enchantment.SHARPNESS, 5);
            player.getInventory().addItem(sword);

            var helmet = HeroItems.unbreakable(Material.NETHERITE_HELMET);
            var chestplate = HeroItems.unbreakable(Material.NETHERITE_CHESTPLATE);
            var leggings = HeroItems.unbreakable(Material.NETHERITE_LEGGINGS);
            var boots = HeroItems.unbreakable(Material.NETHERITE_BOOTS);
            HeroItems.addEnchant(helmet, Enchantment.PROTECTION, 4);
            HeroItems.addEnchant(chestplate, Enchantment.PROTECTION, 4);
            HeroItems.addEnchant(leggings, Enchantment.PROTECTION, 4);
            HeroItems.addEnchant(boots, Enchantment.PROTECTION, 4);
            player.getInventory().setHelmet(helmet);
            player.getInventory().setChestplate(chestplate);
            player.getInventory().setLeggings(leggings);
            player.getInventory().setBoots(boots);
            player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, PotionEffect.INFINITE_DURATION, 1, false, false));
            player.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE, PotionEffect.INFINITE_DURATION, 0, false, false));
            player.addPotionEffect(new PotionEffect(PotionEffectType.STRENGTH, PotionEffect.INFINITE_DURATION, 0, false, false));
        }, HeroRegistryBuilder.none(), "OP 限定", "下界合金剑 + 下界合金套", "全套保护 IV，锋利 V", "常驻速度 II / 抗性 I / 力量 I", "仅 OP 可选");
    }
}
