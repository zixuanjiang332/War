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

public final class StarterHeroDefinitions implements HeroDefinitionGroup {
    @Override
    public void register(HeroRegistryBuilder builder) {
        builder.register(HeroClass.WARRIOR, "新手剑", Material.IRON_CHESTPLATE, player -> {
            player.getInventory().addItem(HeroItems.describe(HeroItems.unbreakable(Material.DIAMOND_SWORD), "新手剑·主武器", List.of("技能：默认近战职业", "没有主动技能，依靠基础属性作战")));
            HeroRegistryBuilder.ironSet(player);
            HeroRegistryBuilder.addStew(player, 3);
        }, HeroRegistryBuilder.none(), "Tier 5", "装备：钻石剑，铁套", "技能：默认近战职业");

        builder.register(HeroClass.NINJA, "忍者", Material.DIAMOND_BOOTS, player -> {
            player.getInventory().addItem(HeroItems.describe(HeroItems.unbreakable(Material.DIAMOND_SWORD), "忍者·主武器", List.of("技能：常驻速度 II", "依靠高速贴身近战作战")));
            player.getInventory().setHelmet(HeroItems.leather(Material.LEATHER_HELMET, Color.BLACK));
            player.getInventory().setChestplate(HeroItems.unbreakable(Material.CHAINMAIL_CHESTPLATE));
            player.getInventory().setLeggings(HeroItems.unbreakable(Material.CHAINMAIL_LEGGINGS));
            player.getInventory().setBoots(HeroItems.unbreakable(Material.DIAMOND_BOOTS));
            player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, PotionEffect.INFINITE_DURATION, 1, false, false));
            HeroRegistryBuilder.addStew(player, 3);
        }, HeroRegistryBuilder.none(), "Tier 5", "装备：钻石剑，皮革头盔，锁链胸甲，锁链护腿，钻石靴子", "技能：常驻速度 II");

        builder.register(HeroClass.TANK_VANGUARD, "坦克", Material.DIAMOND_CHESTPLATE, player -> {
            player.getInventory().addItem(HeroItems.describe(HeroItems.unbreakable(Material.IRON_SWORD), "坦克·主武器", List.of("技能：常驻缓慢 II", "高护甲正面推进职业")));
            HeroRegistryBuilder.diamondSet(player);
            player.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, PotionEffect.INFINITE_DURATION, 1, false, false));
            HeroRegistryBuilder.addStew(player, 3);
        }, HeroRegistryBuilder.none(), "Tier 4", "装备：铁剑，钻石套", "技能：常驻缓慢 II");

        builder.register(HeroClass.SHADOW_ASSASSIN, "暗影刺客", Material.GOLDEN_SWORD, player -> {
            player.getInventory().addItem(HeroItems.describe(HeroItems.unbreakable(Material.DIAMOND_SWORD), "暗影刺客·主武器", List.of("技能：副手金剑爆发更高", "切换副武器可打出更高伤害")));
            player.getInventory().addItem(HeroItems.describe(HeroItems.durableBlade(36, 1), "暗影刺客·副武器", List.of("技能：高爆发金剑", "耐久极低，适合关键一击")));
            player.getInventory().setHelmet(HeroItems.leather(Material.LEATHER_HELMET, Color.fromRGB(45, 45, 45)));
            player.getInventory().setChestplate(HeroItems.unbreakable(Material.IRON_CHESTPLATE));
            player.getInventory().setLeggings(HeroItems.leather(Material.LEATHER_LEGGINGS, Color.fromRGB(45, 45, 45)));
            player.getInventory().setBoots(HeroItems.unbreakable(Material.IRON_BOOTS));
            HeroRegistryBuilder.addStew(player, 3);
        }, HeroRegistryBuilder.none(), "Tier 4", "装备：钻石剑，锋利金剑，皮革头盔，铁胸甲，皮革护腿，铁靴子", "技能：副手金剑爆发更高");

        builder.register(HeroClass.MAGE, "魔导师", Material.SNOWBALL, player -> {
            player.getInventory().addItem(HeroItems.unbreakable(Material.DIAMOND_SWORD));
            player.getInventory().setItem(1, HeroItems.named(Material.SNOWBALL, 16, "魔导师技能", List.of("命中玩家后与目标交换位置")));
            player.getInventory().setHelmet(HeroItems.leather(Material.LEATHER_HELMET, Color.PURPLE));
            player.getInventory().setChestplate(HeroItems.unbreakable(Material.IRON_CHESTPLATE));
            player.getInventory().setLeggings(HeroItems.unbreakable(Material.IRON_LEGGINGS));
            player.getInventory().setBoots(HeroItems.unbreakable(Material.IRON_BOOTS));
            HeroRegistryBuilder.addStew(player, 3);
        }, HeroRegistryBuilder.none(), "Tier 5", "装备：钻石剑，皮革头盔，铁胸甲，铁护腿，铁靴子，雪球", "技能：雪球命中后交换位置");

        builder.register(HeroClass.CLAW, "陷阱大师", Material.COBWEB, player -> {
            player.getInventory().addItem(HeroItems.unbreakable(Material.DIAMOND_SWORD));
            player.getInventory().setItem(1, HeroItems.named(Material.COBWEB, 1, "利爪技能", List.of("发射蛛网弹，命中后生成蛛网", "冷却：配置文件控制")));
            player.getInventory().setHelmet(HeroItems.unbreakable(Material.IRON_HELMET));
            player.getInventory().setChestplate(HeroItems.leather(Material.LEATHER_CHESTPLATE, Color.BLUE));
            player.getInventory().setLeggings(HeroItems.unbreakable(Material.DIAMOND_LEGGINGS));
            player.getInventory().setBoots(HeroItems.unbreakable(Material.DIAMOND_BOOTS));
            HeroRegistryBuilder.addStew(player, 3);
        }, HeroRegistryBuilder.bindings(HeroRegistryBuilder.binding("claw_web", Material.COBWEB)), "Tier 4", "装备：钻石剑，铁头盔，蓝色皮革胸甲，钻石护腿，钻石靴子，蜘蛛网", "技能：蛛网弹命中后封锁目标周围");

        builder.register(HeroClass.BIRDMAN, "鸟人", Material.FEATHER, player -> {
            ItemStack bow = HeroItems.unbreakable(Material.BOW);
            HeroItems.addEnchant(bow, Enchantment.INFINITY, 1);
            player.getInventory().addItem(HeroItems.describe(HeroItems.unbreakable(Material.DIAMOND_SWORD), "鸟人·主武器", List.of("技能：射箭时随箭短暂突进", "近战与机动配合压制敌人")));
            player.getInventory().addItem(HeroItems.describe(bow, "鸟人·长弓", List.of("技能：射箭时短暂突进", "利用位移快速拉近或脱离")));
            player.getInventory().addItem(new ItemStack(Material.ARROW));
            player.getInventory().setHelmet(HeroItems.leather(Material.LEATHER_HELMET, Color.WHITE));
            player.getInventory().setChestplate(HeroItems.unbreakable(Material.CHAINMAIL_CHESTPLATE));
            player.getInventory().setLeggings(HeroItems.unbreakable(Material.IRON_LEGGINGS));
            player.getInventory().setBoots(HeroItems.unbreakable(Material.IRON_BOOTS));
            HeroRegistryBuilder.addStew(player, 3);
        }, HeroRegistryBuilder.none(), "Tier 4", "装备：钻石剑，皮革头盔，锁链胸甲，铁护腿，铁靴子，弓，一支箭", "技能：射箭时随箭短暂突进");

        builder.register(HeroClass.DESTROYER, "毁灭者", Material.CREEPER_HEAD, player -> {
            player.getInventory().addItem(HeroItems.unbreakable(Material.DIAMOND_SWORD));
            player.getInventory().setItem(1, HeroItems.named(Material.CREEPER_HEAD, 1, "毁灭者技能", List.of("右键爆震周围敌人", "造成伤害并击飞目标")));
            player.getInventory().setHelmet(HeroItems.leather(Material.LEATHER_HELMET, Color.LIME));
            player.getInventory().setChestplate(HeroItems.unbreakable(Material.DIAMOND_CHESTPLATE));
            player.getInventory().setLeggings(HeroItems.unbreakable(Material.CHAINMAIL_LEGGINGS));
            player.getInventory().setBoots(HeroItems.leather(Material.LEATHER_BOOTS, Color.LIME));
            HeroRegistryBuilder.addStew(player, 3);
        }, HeroRegistryBuilder.bindings(HeroRegistryBuilder.binding("destroyer_blast", Material.CREEPER_HEAD)), "Tier 4", "装备：钻石剑，皮革头盔，钻石胸甲，锁链护腿，皮革靴子，苦力怕头", "技能：范围爆震并击飞周围敌人");

        builder.register(HeroClass.THORNHEART, "荆棘之心", Material.SWEET_BERRIES, player -> {
            player.getInventory().addItem(HeroItems.describe(HeroItems.unbreakable(Material.DIAMOND_SWORD), "荆棘之心·主武器", List.of("技能：全套荆棘 II", "适合贴身换血反伤")));
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
        }, HeroRegistryBuilder.none(), "Tier 5", "装备：钻石剑，铁头盔，铁胸甲，皮革护腿，皮革靴子", "技能：全套荆棘 II");

        builder.register(HeroClass.SUMMONER, "召唤师", Material.IRON_BLOCK, player -> {
            player.getInventory().addItem(HeroItems.unbreakable(Material.DIAMOND_SWORD));
            player.getInventory().setItem(1, HeroItems.named(Material.IRON_BLOCK, 1, "召唤师技能", List.of("召唤铁傀儡并骑乘", "铁傀儡存在时间：15秒")));
            player.getInventory().setHelmet(HeroItems.unbreakable(Material.IRON_HELMET));
            player.getInventory().setChestplate(HeroItems.unbreakable(Material.CHAINMAIL_CHESTPLATE));
            player.getInventory().setLeggings(HeroItems.unbreakable(Material.IRON_LEGGINGS));
            player.getInventory().setBoots(HeroItems.unbreakable(Material.IRON_BOOTS));
            HeroRegistryBuilder.addStew(player, 3);
        }, HeroRegistryBuilder.bindings(HeroRegistryBuilder.binding("summoner_golem", Material.IRON_BLOCK)), "Tier 4", "装备：钻石剑，铁头盔，锁链胸甲，铁护腿，铁靴子，铁块", "技能：召唤并骑乘铁傀儡");

        builder.register(HeroClass.BOWMAN, "新手弓", Material.BOW, player -> {
            ItemStack bow = HeroItems.unbreakable(Material.BOW);
            HeroItems.addEnchant(bow, Enchantment.POWER, 1);
            player.getInventory().addItem(HeroItems.describe(HeroItems.unbreakable(Material.DIAMOND_SWORD), "新手弓·副武器", List.of("技能：远程消耗型职业", "被近身时用于自保")));
            player.getInventory().addItem(HeroItems.describe(bow, "新手弓·长弓", List.of("技能：远程消耗", "利用力量 I 稳定压低敌人血量")));
            player.getInventory().addItem(new ItemStack(Material.ARROW, 64));
            player.getInventory().setHelmet(HeroItems.leather(Material.LEATHER_HELMET, Color.WHITE));
            player.getInventory().setChestplate(HeroItems.leather(Material.LEATHER_CHESTPLATE, Color.WHITE));
            player.getInventory().setLeggings(HeroItems.unbreakable(Material.IRON_LEGGINGS));
            player.getInventory().setBoots(HeroItems.unbreakable(Material.IRON_BOOTS));
            HeroRegistryBuilder.addStew(player, 3);
        }, HeroRegistryBuilder.none(), "Tier 5", "装备：钻石剑，力量 I 弓，皮革头盔，皮革胸甲，铁护腿，铁靴子，一组箭", "技能：远程消耗型职业");
    }
}
