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

public final class StarterHeroDefinitions implements HeroDefinitionGroup {
    @Override
    public void register(HeroRegistryBuilder builder) {
        builder.register(HeroClass.WARRIOR, "剑士", Material.IRON_CHESTPLATE, player -> {
            player.getInventory().addItem(HeroItems.unbreakable(Material.DIAMOND_SWORD));
            HeroRegistryBuilder.ironSet(player);
            HeroRegistryBuilder.addStew(player, 3);
        }, HeroRegistryBuilder.none(), "Tier 5", "默认职业", "铁套 + 钻石剑");

        builder.register(HeroClass.NINJA, "夜忍", Material.DIAMOND_BOOTS, player -> {
            player.getInventory().addItem(HeroItems.unbreakable(Material.DIAMOND_SWORD));
            player.getInventory().setHelmet(HeroItems.leather(Material.LEATHER_HELMET, Color.BLACK));
            player.getInventory().setChestplate(HeroItems.unbreakable(Material.CHAINMAIL_CHESTPLATE));
            player.getInventory().setLeggings(HeroItems.unbreakable(Material.CHAINMAIL_LEGGINGS));
            player.getInventory().setBoots(HeroItems.unbreakable(Material.DIAMOND_BOOTS));
            player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, PotionEffect.INFINITE_DURATION, 1, false, false));
            HeroRegistryBuilder.addStew(player, 3);
        }, HeroRegistryBuilder.none(), "Tier 5", "钻石剑 + 轻甲", "常驻速度 II");

        builder.register(HeroClass.TANK_VANGUARD, "前锋", Material.SHIELD, player -> {
            player.getInventory().addItem(HeroItems.unbreakable(Material.IRON_SWORD));
            HeroRegistryBuilder.diamondSet(player);
            player.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, PotionEffect.INFINITE_DURATION, 1, false, false));
            HeroRegistryBuilder.addStew(player, 3);
        }, HeroRegistryBuilder.none(), "Tier 4", "铁剑 + 钻石套", "高处落下可触发跳劈");

        builder.register(HeroClass.SHADOW_ASSASSIN, "影刃", Material.GOLDEN_SWORD, player -> {
            player.getInventory().addItem(HeroItems.unbreakable(Material.DIAMOND_SWORD));
            player.getInventory().addItem(HeroItems.durableBlade(32, 1));
            player.getInventory().setHelmet(HeroItems.leather(Material.LEATHER_HELMET, Color.fromRGB(45, 45, 45)));
            player.getInventory().setChestplate(HeroItems.unbreakable(Material.IRON_CHESTPLATE));
            player.getInventory().setLeggings(HeroItems.leather(Material.LEATHER_LEGGINGS, Color.fromRGB(45, 45, 45)));
            player.getInventory().setBoots(HeroItems.unbreakable(Material.IRON_BOOTS));
            HeroRegistryBuilder.addStew(player, 3);
        }, HeroRegistryBuilder.none(), "Tier 4", "钻石剑 + 附魔金剑", "金剑锋利 32，耐久 1");

        builder.register(HeroClass.MAGE, "法师", Material.SNOWBALL, player -> {
            player.getInventory().addItem(HeroItems.unbreakable(Material.DIAMOND_SWORD));
            player.getInventory().addItem(new ItemStack(Material.SNOWBALL, 16));
            player.getInventory().setHelmet(HeroItems.leather(Material.LEATHER_HELMET, Color.PURPLE));
            player.getInventory().setChestplate(HeroItems.unbreakable(Material.IRON_CHESTPLATE));
            player.getInventory().setLeggings(HeroItems.unbreakable(Material.IRON_LEGGINGS));
            player.getInventory().setBoots(HeroItems.unbreakable(Material.IRON_BOOTS));
            HeroRegistryBuilder.addStew(player, 3);
        }, HeroRegistryBuilder.none(), "Tier 5", "钻石剑 + 雪球", "雪球命中后与目标换位");

        builder.register(HeroClass.CLAW, "蛛猎", Material.COBWEB, player -> {
            player.getInventory().addItem(HeroItems.unbreakable(Material.DIAMOND_SWORD));
            player.getInventory().setItem(1, HeroItems.named(Material.COBWEB, "蛛猎技能"));
            player.getInventory().setHelmet(HeroItems.unbreakable(Material.IRON_HELMET));
            player.getInventory().setChestplate(HeroItems.leather(Material.LEATHER_CHESTPLATE, Color.BLUE));
            player.getInventory().setLeggings(HeroItems.unbreakable(Material.DIAMOND_LEGGINGS));
            player.getInventory().setBoots(HeroItems.unbreakable(Material.DIAMOND_BOOTS));
            HeroRegistryBuilder.addStew(player, 3);
        }, HeroRegistryBuilder.bindings(HeroRegistryBuilder.binding("claw_web", Material.COBWEB)), "Tier 4", "钻石剑 + 蜘蛛网", "发射网球并生成十字蛛网");

        builder.register(HeroClass.BIRDMAN, "风羽", Material.FEATHER, player -> {
            ItemStack bow = HeroItems.unbreakable(Material.BOW);
            HeroItems.addEnchant(bow, Enchantment.INFINITY, 1);
            player.getInventory().addItem(HeroItems.unbreakable(Material.DIAMOND_SWORD));
            player.getInventory().addItem(bow);
            player.getInventory().addItem(new ItemStack(Material.ARROW));
            player.getInventory().setHelmet(HeroItems.leather(Material.LEATHER_HELMET, Color.WHITE));
            player.getInventory().setChestplate(HeroItems.unbreakable(Material.CHAINMAIL_CHESTPLATE));
            player.getInventory().setLeggings(HeroItems.unbreakable(Material.IRON_LEGGINGS));
            player.getInventory().setBoots(HeroItems.unbreakable(Material.IRON_BOOTS));
            HeroRegistryBuilder.addStew(player, 3);
        }, HeroRegistryBuilder.none(), "Tier 4", "钻石剑 + 弓", "射箭时朝箭的方向突进");

        builder.register(HeroClass.DESTROYER, "爆破手", Material.CREEPER_HEAD, player -> {
            player.getInventory().addItem(HeroItems.unbreakable(Material.DIAMOND_SWORD));
            player.getInventory().setItem(1, HeroItems.named(Material.CREEPER_HEAD, "爆破手技能"));
            player.getInventory().setHelmet(HeroItems.leather(Material.LEATHER_HELMET, Color.LIME));
            player.getInventory().setChestplate(HeroItems.unbreakable(Material.DIAMOND_CHESTPLATE));
            player.getInventory().setLeggings(HeroItems.unbreakable(Material.CHAINMAIL_LEGGINGS));
            player.getInventory().setBoots(HeroItems.leather(Material.LEATHER_BOOTS, Color.LIME));
            HeroRegistryBuilder.addStew(player, 3);
        }, HeroRegistryBuilder.bindings(HeroRegistryBuilder.binding("destroyer_blast", Material.CREEPER_HEAD)), "Tier 4", "钻石剑 + 苦力怕头", "右键造成范围物理伤害");

        builder.register(HeroClass.THORNHEART, "棘甲", Material.SWEET_BERRIES, player -> {
            player.getInventory().addItem(HeroItems.unbreakable(Material.DIAMOND_SWORD));
            ItemStack helmet = HeroItems.unbreakable(Material.IRON_HELMET);
            ItemStack chest = HeroItems.unbreakable(Material.IRON_CHESTPLATE);
            ItemStack leggings = HeroItems.leather(Material.LEATHER_LEGGINGS, Color.GREEN);
            ItemStack boots = HeroItems.leather(Material.LEATHER_BOOTS, Color.GREEN);
            HeroItems.addEnchant(helmet, Enchantment.THORNS, 2);
            HeroItems.addEnchant(chest, Enchantment.THORNS, 2);
            HeroItems.addEnchant(leggings, Enchantment.THORNS, 2);
            HeroItems.addEnchant(boots, Enchantment.THORNS, 2);
            player.getInventory().setHelmet(helmet);
            player.getInventory().setChestplate(chest);
            player.getInventory().setLeggings(leggings);
            player.getInventory().setBoots(boots);
            HeroRegistryBuilder.addStew(player, 3);
        }, HeroRegistryBuilder.none(), "Tier 5", "钻石剑 + 荆棘护甲", "全套荆棘 II");

        builder.register(HeroClass.SUMMONER, "召铁", Material.IRON_BLOCK, player -> {
            player.getInventory().addItem(HeroItems.unbreakable(Material.DIAMOND_SWORD));
            player.getInventory().setItem(1, HeroItems.named(Material.IRON_BLOCK, "召铁技能"));
            player.getInventory().setHelmet(HeroItems.unbreakable(Material.IRON_HELMET));
            player.getInventory().setChestplate(HeroItems.unbreakable(Material.CHAINMAIL_CHESTPLATE));
            player.getInventory().setLeggings(HeroItems.unbreakable(Material.IRON_LEGGINGS));
            player.getInventory().setBoots(HeroItems.unbreakable(Material.IRON_BOOTS));
            HeroRegistryBuilder.addStew(player, 3);
        }, HeroRegistryBuilder.bindings(HeroRegistryBuilder.binding("summoner_golem", Material.IRON_BLOCK)), "Tier 4", "钻石剑 + 铁块", "右键召唤并骑乘铁傀儡");

        builder.register(HeroClass.BOWMAN, "弓手", Material.BOW, player -> {
            ItemStack bow = HeroItems.unbreakable(Material.BOW);
            HeroItems.addEnchant(bow, Enchantment.POWER, 1);
            player.getInventory().addItem(HeroItems.unbreakable(Material.DIAMOND_SWORD));
            player.getInventory().addItem(bow);
            player.getInventory().addItem(new ItemStack(Material.ARROW, 64));
            player.getInventory().setHelmet(HeroItems.leather(Material.LEATHER_HELMET, Color.WHITE));
            player.getInventory().setChestplate(HeroItems.leather(Material.LEATHER_CHESTPLATE, Color.WHITE));
            player.getInventory().setLeggings(HeroItems.unbreakable(Material.IRON_LEGGINGS));
            player.getInventory().setBoots(HeroItems.unbreakable(Material.IRON_BOOTS));
            HeroRegistryBuilder.addStew(player, 3);
        }, HeroRegistryBuilder.none(), "Tier 5", "钻石剑 + 力量 I 弓", "自带一组箭");
    }
}
