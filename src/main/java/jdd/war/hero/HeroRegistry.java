package jdd.war.hero;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;
import net.kyori.adventure.text.Component;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.potion.PotionType;

public final class HeroRegistry {
    private final Map<HeroClass, HeroDefinition> heroes = new EnumMap<>(HeroClass.class);
    private final Map<Integer, HeroClass> slotIndex = new HashMap<>();

    public HeroRegistry() {
        registerDefaults();
    }

    public HeroDefinition get(HeroClass heroClass) {
        HeroDefinition definition = heroes.get(heroClass);
        if (definition == null) {
            throw new IllegalArgumentException("Unknown hero class: " + heroClass);
        }
        return definition;
    }

    public List<HeroDefinition> getAll() {
        List<HeroDefinition> list = new ArrayList<>(heroes.values());
        list.sort(Comparator.comparingInt(HeroDefinition::getMenuSlot));
        return list;
    }

    public Optional<HeroClass> findByMenuSlot(int slot) {
        return Optional.ofNullable(slotIndex.get(slot));
    }

    private void registerDefaults() {
        int slot = 0;

        register(slot++, HeroClass.WARRIOR, "剑士", Material.IRON_CHESTPLATE, player -> {
            player.getInventory().addItem(HeroItems.unbreakable(Material.DIAMOND_SWORD));
            ironSet(player);
            addStew(player, 3);
        }, "Tier 5", "默认职业", "铁套 + 钻石剑");

        register(slot++, HeroClass.NINJA, "夜忍", Material.DIAMOND_BOOTS, player -> {
            player.getInventory().addItem(HeroItems.unbreakable(Material.DIAMOND_SWORD));
            player.getInventory().setHelmet(HeroItems.leather(Material.LEATHER_HELMET, Color.BLACK));
            player.getInventory().setChestplate(HeroItems.unbreakable(Material.CHAINMAIL_CHESTPLATE));
            player.getInventory().setLeggings(HeroItems.unbreakable(Material.CHAINMAIL_LEGGINGS));
            player.getInventory().setBoots(HeroItems.unbreakable(Material.DIAMOND_BOOTS));
            player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, PotionEffect.INFINITE_DURATION, 1, false, false));
            addStew(player, 3);
        }, "Tier 5", "钻石剑 + 轻甲", "常驻速度 II");

        register(slot++, HeroClass.TANK_VANGUARD, "前锋", Material.SHIELD, player -> {
            player.getInventory().addItem(HeroItems.unbreakable(Material.IRON_SWORD));
            diamondSet(player);
            player.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, PotionEffect.INFINITE_DURATION, 1, false, false));
            addStew(player, 3);
        }, "Tier 4", "铁剑 + 钻石套", "从高处落下可触发跳劈");

        register(slot++, HeroClass.SHADOW_ASSASSIN, "影刃", Material.GOLDEN_SWORD, player -> {
            player.getInventory().addItem(HeroItems.unbreakable(Material.DIAMOND_SWORD));
            player.getInventory().addItem(HeroItems.durableBlade(32, 1));
            player.getInventory().setHelmet(HeroItems.leather(Material.LEATHER_HELMET, Color.fromRGB(45, 45, 45)));
            player.getInventory().setChestplate(HeroItems.unbreakable(Material.IRON_CHESTPLATE));
            player.getInventory().setLeggings(HeroItems.leather(Material.LEATHER_LEGGINGS, Color.fromRGB(45, 45, 45)));
            player.getInventory().setBoots(HeroItems.unbreakable(Material.IRON_BOOTS));
            addStew(player, 3);
        }, "Tier 4", "钻石剑 + 附魔金剑", "金剑锋利 32，耐久 1");

        register(slot++, HeroClass.MAGE, "法师", Material.SNOWBALL, player -> {
            player.getInventory().addItem(HeroItems.unbreakable(Material.DIAMOND_SWORD));
            player.getInventory().addItem(new ItemStack(Material.SNOWBALL, 16));
            player.getInventory().setHelmet(HeroItems.leather(Material.LEATHER_HELMET, Color.PURPLE));
            player.getInventory().setChestplate(HeroItems.unbreakable(Material.IRON_CHESTPLATE));
            player.getInventory().setLeggings(HeroItems.unbreakable(Material.IRON_LEGGINGS));
            player.getInventory().setBoots(HeroItems.unbreakable(Material.IRON_BOOTS));
            addStew(player, 3);
        }, "Tier 5", "钻石剑 + 雪球", "雪球命中后与目标交换位置");

        register(slot++, HeroClass.CLAW, "蛛猎", Material.COBWEB, player -> {
            player.getInventory().addItem(HeroItems.unbreakable(Material.DIAMOND_SWORD));
            player.getInventory().setItem(1, HeroItems.named(Material.COBWEB, "蛛猎技能"));
            player.getInventory().setHelmet(HeroItems.unbreakable(Material.IRON_HELMET));
            player.getInventory().setChestplate(HeroItems.leather(Material.LEATHER_CHESTPLATE, Color.BLUE));
            player.getInventory().setLeggings(HeroItems.unbreakable(Material.DIAMOND_LEGGINGS));
            player.getInventory().setBoots(HeroItems.unbreakable(Material.DIAMOND_BOOTS));
            addStew(player, 3);
        }, "Tier 4", "钻石剑 + 蜘蛛网", "发射网球并生成十字蛛网");

        register(slot++, HeroClass.BIRDMAN, "风羽", Material.FEATHER, player -> {
            ItemStack bow = HeroItems.unbreakable(Material.BOW);
            HeroItems.addEnchant(bow, Enchantment.INFINITY, 1);
            player.getInventory().addItem(HeroItems.unbreakable(Material.DIAMOND_SWORD));
            player.getInventory().addItem(bow);
            player.getInventory().addItem(new ItemStack(Material.ARROW));
            player.getInventory().setHelmet(HeroItems.leather(Material.LEATHER_HELMET, Color.WHITE));
            player.getInventory().setChestplate(HeroItems.unbreakable(Material.CHAINMAIL_CHESTPLATE));
            player.getInventory().setLeggings(HeroItems.unbreakable(Material.IRON_LEGGINGS));
            player.getInventory().setBoots(HeroItems.unbreakable(Material.IRON_BOOTS));
            addStew(player, 3);
        }, "Tier 4", "钻石剑 + 弓", "射箭时朝箭的方向突进");

        register(slot++, HeroClass.DESTROYER, "爆破手", Material.CREEPER_HEAD, player -> {
            player.getInventory().addItem(HeroItems.unbreakable(Material.DIAMOND_SWORD));
            player.getInventory().setItem(1, HeroItems.named(Material.CREEPER_HEAD, "爆破手技能"));
            player.getInventory().setHelmet(HeroItems.leather(Material.LEATHER_HELMET, Color.LIME));
            player.getInventory().setChestplate(HeroItems.unbreakable(Material.DIAMOND_CHESTPLATE));
            player.getInventory().setLeggings(HeroItems.unbreakable(Material.CHAINMAIL_LEGGINGS));
            player.getInventory().setBoots(HeroItems.leather(Material.LEATHER_BOOTS, Color.LIME));
            addStew(player, 3);
        }, "Tier 4", "钻石剑 + 苦力怕头", "右键造成范围物理伤害");

        register(slot++, HeroClass.THORNHEART, "棘甲", Material.SWEET_BERRIES, player -> {
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
            addStew(player, 3);
        }, "Tier 5", "钻石剑 + 荆棘护甲", "全套荆棘 II");

        register(slot++, HeroClass.SUMMONER, "召铁", Material.IRON_BLOCK, player -> {
            player.getInventory().addItem(HeroItems.unbreakable(Material.DIAMOND_SWORD));
            player.getInventory().setItem(1, HeroItems.named(Material.IRON_BLOCK, "召铁技能"));
            player.getInventory().setHelmet(HeroItems.unbreakable(Material.IRON_HELMET));
            player.getInventory().setChestplate(HeroItems.unbreakable(Material.CHAINMAIL_CHESTPLATE));
            player.getInventory().setLeggings(HeroItems.unbreakable(Material.IRON_LEGGINGS));
            player.getInventory().setBoots(HeroItems.unbreakable(Material.IRON_BOOTS));
            addStew(player, 3);
        }, "Tier 4", "钻石剑 + 铁块", "右键召唤并骑乘铁傀儡");

        register(slot++, HeroClass.BOWMAN, "弓手", Material.BOW, player -> {
            ItemStack bow = HeroItems.unbreakable(Material.BOW);
            HeroItems.addEnchant(bow, Enchantment.POWER, 1);
            player.getInventory().addItem(HeroItems.unbreakable(Material.DIAMOND_SWORD));
            player.getInventory().addItem(bow);
            player.getInventory().addItem(new ItemStack(Material.ARROW, 64));
            player.getInventory().setHelmet(HeroItems.leather(Material.LEATHER_HELMET, Color.WHITE));
            player.getInventory().setChestplate(HeroItems.leather(Material.LEATHER_CHESTPLATE, Color.WHITE));
            player.getInventory().setLeggings(HeroItems.unbreakable(Material.IRON_LEGGINGS));
            player.getInventory().setBoots(HeroItems.unbreakable(Material.IRON_BOOTS));
            addStew(player, 3);
        }, "Tier 5", "钻石剑 + 力量 I 弓", "自带一组箭");

        register(slot++, HeroClass.IMMORTAL, "不朽", Material.TOTEM_OF_UNDYING, player -> {
            player.getInventory().addItem(HeroItems.unbreakable(Material.DIAMOND_SWORD));
            player.getInventory().setHelmet(HeroItems.leather(Material.LEATHER_HELMET, Color.WHITE));
            player.getInventory().setChestplate(HeroItems.unbreakable(Material.IRON_CHESTPLATE));
            player.getInventory().setLeggings(HeroItems.unbreakable(Material.CHAINMAIL_LEGGINGS));
            player.getInventory().setBoots(HeroItems.leather(Material.LEATHER_BOOTS, Color.WHITE));
            if (player.getAttribute(Attribute.MAX_HEALTH) != null) {
                player.getAttribute(Attribute.MAX_HEALTH).setBaseValue(40.0D);
            }
            player.setHealth(40.0D);
            addStew(player, 5);
        }, "Tier 4", "生命上限 +20", "蘑菇煲数量更多");

        register(slot++, HeroClass.INFERNO_GUARD, "炎狼", Material.BONE, player -> {
            player.getInventory().addItem(HeroItems.unbreakable(Material.DIAMOND_SWORD));
            player.getInventory().setItem(1, HeroItems.named(Material.BONE, "炎狼技能"));
            player.getInventory().setHelmet(HeroItems.unbreakable(Material.IRON_HELMET));
            player.getInventory().setChestplate(HeroItems.unbreakable(Material.IRON_CHESTPLATE));
            player.getInventory().setLeggings(HeroItems.unbreakable(Material.IRON_LEGGINGS));
            player.getInventory().setBoots(HeroItems.leather(Material.LEATHER_BOOTS, Color.RED));
            addStew(player, 3);
        }, "Tier 3", "钻石剑 + 骨头", "右键召唤三只地狱猎犬");

        register(slot++, HeroClass.THOR, "雷斧", Material.WOODEN_AXE, player -> {
            player.getInventory().addItem(HeroItems.unbreakable(Material.DIAMOND_SWORD));
            player.getInventory().addItem(HeroItems.unbreakable(Material.WOODEN_AXE));
            player.getInventory().setHelmet(HeroItems.leather(Material.LEATHER_HELMET, Color.YELLOW));
            player.getInventory().setChestplate(HeroItems.unbreakable(Material.IRON_CHESTPLATE));
            player.getInventory().setLeggings(HeroItems.unbreakable(Material.IRON_LEGGINGS));
            player.getInventory().setBoots(HeroItems.unbreakable(Material.IRON_BOOTS));
            addStew(player, 3);
        }, "Tier 3", "钻石剑 + 木斧", "木斧命中后触发范围雷击");

        register(slot++, HeroClass.HEAD_REAPER, "裂颅", Material.WITHER_SKELETON_SKULL, player -> {
            player.getInventory().addItem(HeroItems.unbreakable(Material.DIAMOND_SWORD));
            player.getInventory().setHelmet(HeroItems.unbreakable(Material.IRON_HELMET));
            player.getInventory().setChestplate(HeroItems.leather(Material.LEATHER_CHESTPLATE, Color.GRAY));
            player.getInventory().setLeggings(HeroItems.unbreakable(Material.IRON_LEGGINGS));
            player.getInventory().setBoots(HeroItems.unbreakable(Material.IRON_BOOTS));
            addStew(player, 3);
        }, "Tier 4", "钻石剑 + 轻甲", "从三格以上落下可造成额外伤害");

        register(slot++, HeroClass.GHOST, "幽魂", Material.GHAST_TEAR, player -> {
            player.getInventory().addItem(HeroItems.unbreakable(Material.DIAMOND_SWORD));
            player.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, PotionEffect.INFINITE_DURATION, 0, false, false));
            addStew(player, 3);
        }, "Tier 3", "钻石剑", "常驻隐身");

        register(slot++, HeroClass.THUG, "熔拳", Material.IRON_BARS, player -> {
            player.getInventory().addItem(HeroItems.unbreakable(Material.DIAMOND_SWORD));
            player.getInventory().setItem(1, HeroItems.named(Material.IRON_BARS, "熔拳技能"));
            player.getInventory().setHelmet(HeroItems.unbreakable(Material.DIAMOND_HELMET));
            player.getInventory().setChestplate(HeroItems.leather(Material.LEATHER_CHESTPLATE, Color.RED));
            player.getInventory().setLeggings(HeroItems.unbreakable(Material.IRON_LEGGINGS));
            player.getInventory().setBoots(HeroItems.unbreakable(Material.IRON_BOOTS));
            addStew(player, 3);
        }, "Tier 3", "钻石剑 + 铁栏杆", "锁定附近目标并爆发伤害");

        register(slot++, HeroClass.PHANTOM, "幻行", Material.BOOK, player -> {
            player.getInventory().addItem(HeroItems.unbreakable(Material.DIAMOND_SWORD));
            player.getInventory().setItem(1, HeroItems.named(Material.BOOK, "幻行技能"));
            leatherSet(player, Color.fromRGB(180, 180, 255));
            addStew(player, 3);
        }, "Tier 3", "钻石剑 + 皮革套", "右键获得短暂飞行");

        register(slot++, HeroClass.SHACKLE, "冰钟", Material.BELL, player -> {
            player.getInventory().addItem(HeroItems.unbreakable(Material.DIAMOND_SWORD));
            player.getInventory().setItem(1, HeroItems.named(Material.BELL, "冰钟技能"));
            player.getInventory().setHelmet(HeroItems.leather(Material.LEATHER_HELMET, Color.YELLOW));
            player.getInventory().setChestplate(HeroItems.unbreakable(Material.IRON_CHESTPLATE));
            player.getInventory().setLeggings(HeroItems.unbreakable(Material.IRON_LEGGINGS));
            player.getInventory().setBoots(HeroItems.leather(Material.LEATHER_BOOTS, Color.YELLOW));
            addStew(player, 3);
        }, "Tier 3", "钻石剑 + 钟", "右键对周围玩家施加重度缓慢");

        register(slot++, HeroClass.VIKING, "维京", Material.DIAMOND_AXE, player -> {
            player.getInventory().addItem(HeroItems.unbreakable(Material.DIAMOND_AXE));
            ironSet(player);
            addStew(player, 3);
        }, "Tier 4", "钻石斧 + 铁套");

        register(slot++, HeroClass.CAVALRY, "骑士", Material.DIAMOND_HORSE_ARMOR, player -> {
            player.getInventory().addItem(HeroItems.unbreakable(Material.DIAMOND_SWORD));
            player.getInventory().setItem(1, HeroItems.named(Material.DIAMOND_HORSE_ARMOR, "骑士技能"));
            player.getInventory().setHelmet(HeroItems.leather(Material.LEATHER_HELMET, Color.fromRGB(139, 69, 19)));
            player.getInventory().setChestplate(HeroItems.unbreakable(Material.IRON_CHESTPLATE));
            player.getInventory().setLeggings(HeroItems.unbreakable(Material.IRON_LEGGINGS));
            player.getInventory().setBoots(HeroItems.unbreakable(Material.IRON_BOOTS));
            addStew(player, 3);
        }, "Tier 3", "钻石剑 + 马铠", "右键召唤并骑乘战马");

        register(slot++, HeroClass.ACHILLES, "飞焰", Material.FIREWORK_ROCKET, player -> {
            player.getInventory().addItem(HeroItems.unbreakable(Material.DIAMOND_SWORD));
            player.getInventory().setItem(1, HeroItems.named(Material.FIREWORK_ROCKET, "飞焰技能"));
            player.getInventory().setHelmet(HeroItems.unbreakable(Material.IRON_HELMET));
            player.getInventory().setChestplate(HeroItems.unbreakable(Material.CHAINMAIL_CHESTPLATE));
            player.getInventory().setLeggings(HeroItems.unbreakable(Material.IRON_LEGGINGS));
            player.getInventory().setBoots(HeroItems.unbreakable(Material.DIAMOND_BOOTS));
            addStew(player, 3);
        }, "Tier 3", "钻石剑 + 烟花", "右键朝前上方弹射");

        register(slot++, HeroClass.ABYSS, "深渊", Material.ENDER_EYE, player -> {
            ItemStack sword = HeroItems.unbreakable(Material.DIAMOND_SWORD);
            HeroItems.addEnchant(sword, Enchantment.KNOCKBACK, 2);
            player.getInventory().addItem(sword);
            player.getInventory().setHelmet(HeroItems.unbreakable(Material.IRON_HELMET));
            player.getInventory().setChestplate(HeroItems.unbreakable(Material.IRON_CHESTPLATE));
            player.getInventory().setLeggings(HeroItems.unbreakable(Material.IRON_LEGGINGS));
            player.getInventory().setBoots(HeroItems.leather(Material.LEATHER_BOOTS, Color.BLACK));
            addStew(player, 3);
        }, "Tier 3", "击退 II 钻石剑", "重甲近战");

        register(slot++, HeroClass.DRAGON_BREATH, "龙炎", Material.DRAGON_BREATH, player -> {
            player.getInventory().addItem(HeroItems.unbreakable(Material.DIAMOND_SWORD));
            player.getInventory().setItem(1, HeroItems.named(Material.FIRE_CHARGE, "龙炎技能"));
            player.getInventory().setHelmet(HeroItems.leather(Material.LEATHER_HELMET, Color.RED));
            player.getInventory().setChestplate(HeroItems.unbreakable(Material.DIAMOND_CHESTPLATE));
            player.getInventory().setLeggings(HeroItems.unbreakable(Material.IRON_LEGGINGS));
            player.getInventory().setBoots(HeroItems.leather(Material.LEATHER_BOOTS, Color.RED));
            player.addPotionEffect(new PotionEffect(PotionEffectType.FIRE_RESISTANCE, PotionEffect.INFINITE_DURATION, 0, false, false));
            addStew(player, 3);
        }, "Tier 2", "钻石剑 + 烈焰弹", "常驻抗火，范围灼烧");

        register(slot++, HeroClass.EMBER, "烬弓", Material.BLAZE_POWDER, player -> {
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
            addStew(player, 3);
        }, "Tier 2", "火焰附加铁剑 + 火矢弓", "全套火焰保护 V");

        register(slot++, HeroClass.FISHERMAN, "钩手", Material.FISHING_ROD, player -> {
            player.getInventory().addItem(HeroItems.unbreakable(Material.DIAMOND_SWORD));
            player.getInventory().addItem(HeroItems.unbreakable(Material.FISHING_ROD));
            player.getInventory().setHelmet(HeroItems.leather(Material.LEATHER_HELMET, Color.BLUE));
            player.getInventory().setChestplate(HeroItems.unbreakable(Material.IRON_CHESTPLATE));
            player.getInventory().setLeggings(HeroItems.unbreakable(Material.IRON_LEGGINGS));
            player.getInventory().setBoots(HeroItems.leather(Material.LEATHER_BOOTS, Color.BLUE));
            addStew(player, 3);
        }, "Tier 3", "钻石剑 + 钓鱼竿", "收杆时强化拉扯");

        register(slot++, HeroClass.ALCHEMIST, "药师", Material.BREWING_STAND, player -> {
            player.getInventory().addItem(HeroItems.unbreakable(Material.DIAMOND_SWORD));
            player.getInventory().setHelmet(HeroItems.leather(Material.LEATHER_HELMET, Color.FUCHSIA));
            player.getInventory().setChestplate(HeroItems.leather(Material.LEATHER_CHESTPLATE, Color.FUCHSIA));
            player.getInventory().setLeggings(HeroItems.leather(Material.LEATHER_LEGGINGS, Color.FUCHSIA));
            player.getInventory().setBoots(HeroItems.unbreakable(Material.IRON_BOOTS));
            player.getInventory().addItem(potion(Material.POTION, PotionType.HEALING, "治疗药水"));
            player.getInventory().addItem(potion(Material.POTION, PotionType.SWIFTNESS, "迅捷药水"));
            player.getInventory().addItem(potion(Material.SPLASH_POTION, PotionType.POISON, "剧毒喷溅药水"));
            addStew(player, 3);
        }, "Tier 3", "钻石剑 + 多种药水", "每种药水各两瓶");

        register(slot++, HeroClass.ASURA, "修罗", Material.SLIME_BALL, player -> {
            player.getInventory().addItem(HeroItems.unbreakable(Material.DIAMOND_SWORD));
            player.getInventory().setItem(1, HeroItems.named(Material.SLIME_BALL, "修罗技能"));
            player.getInventory().setHelmet(HeroItems.unbreakable(Material.IRON_HELMET));
            player.getInventory().setChestplate(HeroItems.unbreakable(Material.CHAINMAIL_CHESTPLATE));
            player.getInventory().setLeggings(HeroItems.unbreakable(Material.IRON_LEGGINGS));
            player.getInventory().setBoots(HeroItems.unbreakable(Material.IRON_BOOTS));
            addStew(player, 3);
        }, "Tier 2", "钻石剑 + 粘液球", "随机突袭附近一名目标");

        register(slot++, HeroClass.TOXIC_LIZARD, "毒蜥", Material.FERMENTED_SPIDER_EYE, player -> {
            player.getInventory().addItem(HeroItems.unbreakable(Material.DIAMOND_SWORD));
            player.getInventory().setItem(1, HeroItems.named(Material.ENDER_EYE, "毒蜥技能"));
            player.getInventory().setHelmet(HeroItems.leather(Material.LEATHER_HELMET, Color.GREEN));
            player.getInventory().setChestplate(HeroItems.unbreakable(Material.IRON_CHESTPLATE));
            player.getInventory().setLeggings(HeroItems.unbreakable(Material.IRON_LEGGINGS));
            player.getInventory().setBoots(HeroItems.unbreakable(Material.IRON_BOOTS));
            addStew(player, 3);
        }, "Tier 3", "钻石剑 + 末影之眼", "右键对周围玩家施加中毒 I");

        register(slot++, HeroClass.WAR_WRAITH, "战魂", Material.SHIELD, player -> {
            ItemStack sword = HeroItems.unbreakable(Material.DIAMOND_SWORD);
            HeroItems.addEnchant(sword, Enchantment.SHARPNESS, 1);
            player.getInventory().addItem(sword);
            player.getInventory().addItem(new ItemStack(Material.GOLDEN_APPLE));
            player.getInventory().addItem(HeroItems.unbreakable(Material.SHIELD));
            ironSet(player);
            addStew(player, 3);
        }, "Tier 2", "锋利 I 钻石剑 + 铁套", "自带金苹果和盾牌");

        register(slot++, HeroClass.GODWALKER, "神行", Material.RABBIT_FOOT, player -> {
            ItemStack sword = HeroItems.unbreakable(Material.DIAMOND_SWORD);
            HeroItems.addEnchant(sword, Enchantment.SHARPNESS, 1);
            player.getInventory().addItem(sword);
            player.getInventory().addItem(new ItemStack(Material.GOLDEN_APPLE));
            player.getInventory().addItem(HeroItems.unbreakable(Material.SHIELD));
            ironSet(player);
            player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, PotionEffect.INFINITE_DURATION, 2, false, false));
            player.addPotionEffect(new PotionEffect(PotionEffectType.JUMP_BOOST, PotionEffect.INFINITE_DURATION, 1, false, false));
            addStew(player, 3);
        }, "Tier 1", "锋利 I 钻石剑 + 铁套", "常驻速度 III 与跳跃提升 II");

        register(slot++, HeroClass.MUTANT_COMBAT_ZOMBIE, "尸王", Material.ZOMBIE_HEAD, player -> {
            if (player.getAttribute(Attribute.MAX_HEALTH) != null) {
                player.getAttribute(Attribute.MAX_HEALTH).setBaseValue(80.0D);
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
        }, "Tier 1", "生命上限 80", "常驻生命恢复 III，无蘑菇煲");

        register(slot++, HeroClass.NIGHTMARE, "梦魇", Material.NETHERITE_AXE, player -> {
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
            addStew(player, 3);
        }, "Tier 2", "锋利 III 钻石斧 + 盾牌", "常驻速度 I");

        register(slot++, HeroClass.MARTIAL_ARTIST, "拳皇", Material.BLAZE_POWDER, player -> {
            leatherSet(player, Color.ORANGE);
            player.addPotionEffect(new PotionEffect(PotionEffectType.STRENGTH, PotionEffect.INFINITE_DURATION, 1, false, false));
            player.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE, PotionEffect.INFINITE_DURATION, 1, false, false));
            addStew(player, 3);
        }, "Tier 1", "皮革套", "常驻力量 II 与抗性提升 II");

        register(slot++, HeroClass.DEATH_SNIPER, "狙神", Material.CROSSBOW, player -> {
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
            addStew(player, 3);
        }, "Tier 2", "力量 III 弓 + 三组箭", "常驻虚弱 III");

        register(slot++, HeroClass.SACRED_WAR, "圣战", Material.GOLD_NUGGET, player -> {
            player.getInventory().addItem(HeroItems.unbreakable(Material.IRON_SWORD));
            player.getInventory().setItem(1, HeroItems.named(Material.GOLD_NUGGET, "圣战技能"));
            ironSet(player);
            addStew(player, 3);
        }, "Tier 3", "铁剑 + 铁套", "右键恢复 2 颗黄心", "技能冷却 15 秒");

        register(slot++, HeroClass.WINDWALKER, "风行", Material.FEATHER, player -> {
            player.getInventory().addItem(HeroItems.unbreakable(Material.DIAMOND_SWORD));
            player.getInventory().setItem(1, HeroItems.named(Material.FEATHER, "风行·升空"));
            player.getInventory().setItem(2, HeroItems.named(Material.SUGAR, "风行·突进"));
            player.getInventory().setHelmet(HeroItems.leather(Material.LEATHER_HELMET, Color.WHITE));
            player.getInventory().setChestplate(HeroItems.unbreakable(Material.CHAINMAIL_CHESTPLATE));
            player.getInventory().setLeggings(HeroItems.unbreakable(Material.IRON_LEGGINGS));
            player.getInventory().setBoots(HeroItems.unbreakable(Material.IRON_BOOTS));
            addStew(player, 3);
        }, "Tier 2", "钻石剑 + 轻甲", "羽毛升空并震伤周围玩家", "糖触发突进并震伤周围玩家", "两个技能冷却均为 30 秒");

        register(slot++, HeroClass.HOMELANDER, "祖国人", Material.ELYTRA, player -> {
            player.getInventory().addItem(HeroItems.unbreakable(Material.DIAMOND_SWORD));
            player.getInventory().setItem(1, HeroItems.named(Material.PHANTOM_MEMBRANE, "祖国人技能"));
            ironSet(player);
            addStew(player, 3);
        }, "Tier 1", "钻石剑 + 铁套", "右键获得 8 秒飞行", "技能冷却 30 秒");

        register(slot++, HeroClass.OP_CLASS, "OP", Material.NETHERITE_HELMET, player -> {
            ItemStack sword = HeroItems.unbreakable(Material.NETHERITE_SWORD);
            HeroItems.addEnchant(sword, Enchantment.SHARPNESS, 5);
            player.getInventory().addItem(sword);

            ItemStack helmet = HeroItems.unbreakable(Material.NETHERITE_HELMET);
            ItemStack chestplate = HeroItems.unbreakable(Material.NETHERITE_CHESTPLATE);
            ItemStack leggings = HeroItems.unbreakable(Material.NETHERITE_LEGGINGS);
            ItemStack boots = HeroItems.unbreakable(Material.NETHERITE_BOOTS);
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
        }, "OP 限定", "下界合金剑 + 下界合金套", "全套保护 IV，锋利 V", "常驻速度 II / 抗性 I / 力量 I", "仅 OP 可选");
    }

    private void register(int slot, HeroClass heroClass, String name, Material icon, Consumer<Player> applicator, String... description) {
        HeroDefinition definition = new SimpleHeroDefinition(heroClass, name, icon, slot, List.of(description), applicator);
        heroes.put(heroClass, definition);
        slotIndex.put(slot, heroClass);
    }

    private static void addStew(Player player, int count) {
        if (count > 0) {
            player.getInventory().addItem(new ItemStack(Material.MUSHROOM_STEW, count));
        }
    }

    private static void ironSet(Player player) {
        player.getInventory().setHelmet(HeroItems.unbreakable(Material.IRON_HELMET));
        player.getInventory().setChestplate(HeroItems.unbreakable(Material.IRON_CHESTPLATE));
        player.getInventory().setLeggings(HeroItems.unbreakable(Material.IRON_LEGGINGS));
        player.getInventory().setBoots(HeroItems.unbreakable(Material.IRON_BOOTS));
    }

    private static void diamondSet(Player player) {
        player.getInventory().setHelmet(HeroItems.unbreakable(Material.DIAMOND_HELMET));
        player.getInventory().setChestplate(HeroItems.unbreakable(Material.DIAMOND_CHESTPLATE));
        player.getInventory().setLeggings(HeroItems.unbreakable(Material.DIAMOND_LEGGINGS));
        player.getInventory().setBoots(HeroItems.unbreakable(Material.DIAMOND_BOOTS));
    }

    private static void leatherSet(Player player, Color color) {
        player.getInventory().setHelmet(HeroItems.leather(Material.LEATHER_HELMET, color));
        player.getInventory().setChestplate(HeroItems.leather(Material.LEATHER_CHESTPLATE, color));
        player.getInventory().setLeggings(HeroItems.leather(Material.LEATHER_LEGGINGS, color));
        player.getInventory().setBoots(HeroItems.leather(Material.LEATHER_BOOTS, color));
    }

    private static ItemStack potion(Material material, PotionType type, String name) {
        ItemStack item = new ItemStack(material, 2);
        PotionMeta meta = (PotionMeta) item.getItemMeta();
        meta.setBasePotionType(type);
        meta.displayName(Component.text(name));
        item.setItemMeta(meta);
        return item;
    }
}
