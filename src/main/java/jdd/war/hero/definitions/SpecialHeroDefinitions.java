package jdd.war.hero.definitions;

import java.util.List;
import java.util.Map;
import jdd.war.hero.HeroClass;
import jdd.war.hero.HeroDefaults;
import jdd.war.hero.HeroDefinitionGroup;
import jdd.war.hero.HeroEquipmentBuilder;
import jdd.war.hero.HeroItems;
import jdd.war.hero.HeroPotionEffectBuilder;
import jdd.war.hero.HeroRegistryBuilder;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;

public final class SpecialHeroDefinitions implements HeroDefinitionGroup {
    @Override
    public void register(HeroRegistryBuilder builder) {
        builder.register(HeroClass.SACRED_WAR, "圣战", Material.GOLD_NUGGET, player -> {
            player.getInventory().addItem(HeroItems.describe(HeroItems.unbreakable(Material.DIAMOND_SWORD), "圣战·主武器", List.of("技能：右键技能物品获得 4 颗黄心", "适合正面换血推进")));
            player.getInventory().setItem(1, HeroItems.named(Material.GOLD_NUGGET, 1, "圣战技能", List.of("右键获得 4 颗黄心", "冷却：25秒")));
            HeroEquipmentBuilder.equipIronSet(player);
            HeroRegistryBuilder.addStew(player, HeroDefaults.DEFAULT_STEW_COUNT);
        }, HeroRegistryBuilder.bindings(HeroRegistryBuilder.binding("sacred_war_guard", Material.GOLD_NUGGET)),
                "Tier 3",
                "装备：钻石剑，铁套，金粒",
                "技能：为自己提供吸收护盾"
        );

        builder.register(HeroClass.WINDWALKER, "风行", Material.FEATHER, player -> {
            player.getInventory().addItem(HeroItems.describe(HeroItems.unbreakable(Material.DIAMOND_SWORD), "风行·主武器", List.of("技能：升空与突进两段机动", "依靠位移打拉扯与收割")));
            player.getInventory().setItem(1, HeroItems.named(Material.FEATHER, 1, "风行·升空", List.of("原地升空并震伤周围敌人", "冷却：30秒")));
            player.getInventory().setItem(2, HeroItems.named(Material.SUGAR, 1, "风行·突进", List.of("向前突进并震伤周围敌人", "冷却：30秒")));
            player.getInventory().setHelmet(HeroItems.leather(Material.LEATHER_HELMET, Color.WHITE));
            player.getInventory().setChestplate(HeroItems.unbreakable(Material.CHAINMAIL_CHESTPLATE));
            player.getInventory().setLeggings(HeroItems.unbreakable(Material.IRON_LEGGINGS));
            player.getInventory().setBoots(HeroItems.unbreakable(Material.IRON_BOOTS));
            HeroRegistryBuilder.addStew(player, HeroDefaults.DEFAULT_STEW_COUNT);
        }, HeroRegistryBuilder.bindings(
                HeroRegistryBuilder.binding("windwalker_rise", Material.FEATHER),
                HeroRegistryBuilder.binding("windwalker_dash", Material.SUGAR)
        ),
                "Tier 2",
                "装备：钻石剑，皮革头盔，锁链胸甲，铁护腿，铁靴子，羽毛，糖",
                "技能：升空与突进两段机动"
        );

        builder.register(HeroClass.HOMELANDER, "祖国人", Material.PHANTOM_MEMBRANE, player -> {
            player.getInventory().addItem(HeroItems.describe(HeroItems.unbreakable(Material.DIAMOND_SWORD), "祖国人·主武器", List.of("技能：短时飞行与锁定镭射眼", "永久免疫摔落伤害")));
            player.getInventory().setItem(1, HeroItems.named(Material.PHANTOM_MEMBRANE, 1, "祖国人·飞行", List.of("短暂飞行 6 秒", "永久免疫摔落伤害")));
            player.getInventory().setItem(2, HeroItems.named(Material.REDSTONE_TORCH, 1, "祖国人·镭射眼", List.of("锁定准星目标 2 秒", "随后造成 12 点伤害")));
            HeroEquipmentBuilder.equipIronSet(player);
            HeroRegistryBuilder.addStew(player, HeroDefaults.DEFAULT_STEW_COUNT);
        }, HeroRegistryBuilder.bindings(
                HeroRegistryBuilder.binding("homelander_flight", Material.PHANTOM_MEMBRANE),
                HeroRegistryBuilder.binding("homelander_laser", Material.REDSTONE_TORCH)
        ),
                "Tier 1",
                "装备：钻石剑，铁套，幻翼膜，红石火把",
                "技能：短时飞行与锁定镭射眼"
        );

        builder.register(HeroClass.OP_CLASS, "OP", Material.NETHERITE_HELMET, player -> {
            var sword = HeroItems.unbreakable(Material.NETHERITE_SWORD);
            HeroItems.addEnchant(sword, Enchantment.SHARPNESS, HeroDefaults.OP_SHARPNESS_LEVEL);
            player.getInventory().addItem(HeroItems.describe(sword, "OP·主武器", List.of("技能：保护 IV，锋利 V，速度 II，抗性 I，力量 I", "仅限 OP 使用")) );

            Map<Enchantment, Integer> enchants = Map.of(Enchantment.PROTECTION, HeroDefaults.OP_PROTECTION_LEVEL);
            HeroEquipmentBuilder.equipNetheriteSet(player, enchants);
            HeroPotionEffectBuilder.addOPEffects(player);
        }, HeroRegistryBuilder.none(),
                "OP 限定",
                "装备：下界合金剑，下界合金套",
                "技能：保护 IV，锋利 V，速度 II，抗性 I，力量 I"
        );
    }
}
