package jdd.war.hero;

import java.util.EnumMap;
import java.util.Map;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;

/**
 * 英雄装备构建工具类 - 消除重复的装备设置代码
 * 提供预定义的装备套装和快速装备方法
 */
public final class HeroEquipmentBuilder {
    private HeroEquipmentBuilder() {
    }

    /**
     * 为玩家装备完整的铁套装
     */
    public static void equipIronSet(Player player) {
        player.getInventory().setHelmet(HeroItems.unbreakable(Material.IRON_HELMET));
        player.getInventory().setChestplate(HeroItems.unbreakable(Material.IRON_CHESTPLATE));
        player.getInventory().setLeggings(HeroItems.unbreakable(Material.IRON_LEGGINGS));
        player.getInventory().setBoots(HeroItems.unbreakable(Material.IRON_BOOTS));
    }

    /**
     * 为玩家装备完整的钻石套装
     */
    public static void equipDiamondSet(Player player) {
        player.getInventory().setHelmet(HeroItems.unbreakable(Material.DIAMOND_HELMET));
        player.getInventory().setChestplate(HeroItems.unbreakable(Material.DIAMOND_CHESTPLATE));
        player.getInventory().setLeggings(HeroItems.unbreakable(Material.DIAMOND_LEGGINGS));
        player.getInventory().setBoots(HeroItems.unbreakable(Material.DIAMOND_BOOTS));
    }

    /**
     * 为玩家装备完整的下界合金套装，可指定附魔
     * 
     * @param player 目标玩家
     * @param enchantments 附魔映射表 (Material -> Enchantment level)
     */
    public static void equipNetheriteSet(Player player, Map<Enchantment, Integer> enchantments) {
        var helmet = HeroItems.unbreakable(Material.NETHERITE_HELMET);
        var chestplate = HeroItems.unbreakable(Material.NETHERITE_CHESTPLATE);
        var leggings = HeroItems.unbreakable(Material.NETHERITE_LEGGINGS);
        var boots = HeroItems.unbreakable(Material.NETHERITE_BOOTS);

        if (enchantments != null && !enchantments.isEmpty()) {
            for (var entry : enchantments.entrySet()) {
                HeroItems.addEnchant(helmet, entry.getKey(), entry.getValue());
                HeroItems.addEnchant(chestplate, entry.getKey(), entry.getValue());
                HeroItems.addEnchant(leggings, entry.getKey(), entry.getValue());
                HeroItems.addEnchant(boots, entry.getKey(), entry.getValue());
            }
        }

        player.getInventory().setHelmet(helmet);
        player.getInventory().setChestplate(chestplate);
        player.getInventory().setLeggings(leggings);
        player.getInventory().setBoots(boots);
    }

    /**
     * 为玩家装备自定义的四件套，可指定附魔
     * 
     * @param player 目标玩家
     * @param helmet 头盔材料
     * @param chestplate 胸甲材料
     * @param leggings 护腿材料
     * @param boots 靴子材料
     * @param enchantments 附魔映射表
     */
    public static void equipCustomArmor(
            Player player,
            Material helmet,
            Material chestplate,
            Material leggings,
            Material boots,
            Map<Enchantment, Integer> enchantments
    ) {
        var helmetItem = HeroItems.unbreakable(helmet);
        var chestplateItem = HeroItems.unbreakable(chestplate);
        var leggingsItem = HeroItems.unbreakable(leggings);
        var bootsItem = HeroItems.unbreakable(boots);

        if (enchantments != null && !enchantments.isEmpty()) {
            for (var entry : enchantments.entrySet()) {
                HeroItems.addEnchant(helmetItem, entry.getKey(), entry.getValue());
                HeroItems.addEnchant(chestplateItem, entry.getKey(), entry.getValue());
                HeroItems.addEnchant(leggingsItem, entry.getKey(), entry.getValue());
                HeroItems.addEnchant(bootsItem, entry.getKey(), entry.getValue());
            }
        }

        player.getInventory().setHelmet(helmetItem);
        player.getInventory().setChestplate(chestplateItem);
        player.getInventory().setLeggings(leggingsItem);
        player.getInventory().setBoots(bootsItem);
    }

    /**
     * 为玩家装备铁套装（便利方法，兼容原有 HeroRegistryBuilder.ironSet）
     */
    public static void applyIronSet(Player player) {
        equipIronSet(player);
    }

    /**
     * 为玩家装备钻石套装（便利方法，兼容原有 HeroRegistryBuilder.diamondSet）
     */
    public static void applyDiamondSet(Player player) {
        equipDiamondSet(player);
    }
}
