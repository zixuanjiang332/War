package jdd.war.hero.definitions;

import java.util.List;
import jdd.war.hero.HeroClass;
import jdd.war.hero.HeroDefinitionGroup;
import jdd.war.hero.HeroItems;
import jdd.war.hero.HeroRegistryBuilder;
import org.bukkit.Color;
import org.bukkit.Material;

public final class MechanicHeroDefinitions implements HeroDefinitionGroup {
    @Override
    public void register(HeroRegistryBuilder builder) {
        builder.register(HeroClass.ENGINEER, "Engineer", Material.DISPENSER, player -> {
            player.getInventory().addItem(HeroItems.describe(HeroItems.unbreakable(Material.IRON_SWORD), "Engineer Main Weapon", List.of(
                    "Skill: Deploy a sentry turret to lock nearby enemies",
                    "Control space with a temporary static summon"
            )));
            player.getInventory().setItem(1, HeroItems.named(Material.DISPENSER, "Deploy Turret", List.of(
                    "Right-click to deploy a sentry skeleton turret",
                    "Only one turret can exist at a time"
            )));
            player.getInventory().setHelmet(HeroItems.unbreakable(Material.IRON_HELMET));
            player.getInventory().setChestplate(HeroItems.unbreakable(Material.CHAINMAIL_CHESTPLATE));
            player.getInventory().setLeggings(HeroItems.unbreakable(Material.IRON_LEGGINGS));
            player.getInventory().setBoots(HeroItems.unbreakable(Material.IRON_BOOTS));
            HeroRegistryBuilder.addStew(player, 3);
        }, HeroRegistryBuilder.bindings(HeroRegistryBuilder.binding("engineer_turret", Material.DISPENSER)),
                "Tier 2",
                "Loadout: Iron Sword, medium armor, 3 stew, turret deployer",
                "Skill: Deploys a stationary sentry turret for area control");

        builder.register(HeroClass.TIDE, "潮汐", Material.NAUTILUS_SHELL, player -> {
            player.getInventory().addItem(HeroItems.unbreakable(Material.DIAMOND_SWORD));
            player.getInventory().setItem(1, HeroItems.named(Material.NAUTILUS_SHELL, "潮汐技能", List.of("拉近目标并附加缓慢")));
            player.getInventory().setHelmet(HeroItems.unbreakable(Material.IRON_HELMET));
            player.getInventory().setChestplate(HeroItems.unbreakable(Material.CHAINMAIL_CHESTPLATE));
            player.getInventory().setLeggings(HeroItems.unbreakable(Material.IRON_LEGGINGS));
            player.getInventory().setBoots(HeroItems.unbreakable(Material.IRON_BOOTS));
            HeroRegistryBuilder.addStew(player, 3);
        }, HeroRegistryBuilder.bindings(HeroRegistryBuilder.binding("tide_pull", Material.NAUTILUS_SHELL)), "Tier 3", "装备：钻石剑，铁头盔，锁链胸甲，铁护腿，铁靴子，鹦鹉螺壳", "技能：拉近附近目标并减速");

        builder.register(HeroClass.PRISM, "棱镜", Material.AMETHYST_SHARD, player -> {
            player.getInventory().addItem(HeroItems.unbreakable(Material.DIAMOND_SWORD));
            player.getInventory().setItem(1, HeroItems.named(Material.AMETHYST_SHARD, "棱镜技能", List.of("进入棱镜姿态", "抵消一次投射物并反击")));
            player.getInventory().setHelmet(HeroItems.leather(Material.LEATHER_HELMET, Color.WHITE));
            player.getInventory().setChestplate(HeroItems.unbreakable(Material.IRON_CHESTPLATE));
            player.getInventory().setLeggings(HeroItems.unbreakable(Material.CHAINMAIL_LEGGINGS));
            player.getInventory().setBoots(HeroItems.unbreakable(Material.IRON_BOOTS));
            HeroRegistryBuilder.addStew(player, 3);
        }, HeroRegistryBuilder.bindings(HeroRegistryBuilder.binding("prism_stance", Material.AMETHYST_SHARD)), "Tier 2", "装备：钻石剑，皮革头盔，铁胸甲，锁链护腿，铁靴子，紫水晶碎片", "技能：抵消一次投射物并反击");

        builder.register(HeroClass.GEOMANCER, "地脉", Material.MAGMA_CREAM, player -> {
            player.getInventory().addItem(HeroItems.unbreakable(Material.IRON_SWORD));
            player.getInventory().setItem(1, HeroItems.named(Material.MAGMA_CREAM, "地脉技能", List.of("延迟引爆裂地冲击", "命中敌人后击飞目标")));
            HeroRegistryBuilder.ironSet(player);
            player.getInventory().setBoots(HeroItems.leather(Material.LEATHER_BOOTS, Color.ORANGE));
            HeroRegistryBuilder.addStew(player, 3);
        }, HeroRegistryBuilder.bindings(HeroRegistryBuilder.binding("geomancer_quake", Material.MAGMA_CREAM)), "Tier 3", "装备：铁剑，铁头盔，铁胸甲，铁护腿，皮革靴子，岩浆膏", "技能：延迟裂地冲击");

        builder.register(HeroClass.RIFT, "时隙", Material.CLOCK, player -> {
            player.getInventory().addItem(HeroItems.unbreakable(Material.DIAMOND_SWORD));
            player.getInventory().setItem(1, HeroItems.named(Material.CLOCK, "时隙技能", List.of("记录当前位置后回溯", "回到记录点并恢复生命")));
            player.getInventory().setHelmet(HeroItems.leather(Material.LEATHER_HELMET, Color.fromRGB(210, 210, 255)));
            player.getInventory().setChestplate(HeroItems.leather(Material.LEATHER_CHESTPLATE, Color.fromRGB(210, 210, 255)));
            player.getInventory().setLeggings(HeroItems.unbreakable(Material.IRON_LEGGINGS));
            player.getInventory().setBoots(HeroItems.unbreakable(Material.IRON_BOOTS));
            HeroRegistryBuilder.addStew(player, 3);
        }, HeroRegistryBuilder.bindings(HeroRegistryBuilder.binding("rift_anchor", Material.CLOCK)), "Tier 2", "装备：钻石剑，皮革头盔，皮革胸甲，铁护腿，铁靴子，时钟", "技能：记录位置后回溯");

        builder.register(HeroClass.FROSTMARK, "霜痕", Material.SNOWBALL, player -> {
            player.getInventory().addItem(HeroItems.unbreakable(Material.DIAMOND_SWORD));
            player.getInventory().setItem(1, HeroItems.named(Material.SNOWBALL, 16, "霜痕技能", List.of("向前喷出冰息", "造成减速与少量伤害")));
            player.getInventory().setHelmet(HeroItems.unbreakable(Material.IRON_HELMET));
            player.getInventory().setChestplate(HeroItems.unbreakable(Material.CHAINMAIL_CHESTPLATE));
            player.getInventory().setLeggings(HeroItems.unbreakable(Material.IRON_LEGGINGS));
            player.getInventory().setBoots(HeroItems.unbreakable(Material.IRON_BOOTS));
            HeroRegistryBuilder.addStew(player, 3);
        }, HeroRegistryBuilder.bindings(HeroRegistryBuilder.binding("frostmark_breath", Material.SNOWBALL)), "Tier 3", "装备：钻石剑，铁头盔，锁链胸甲，铁护腿，铁靴子，雪球", "技能：锥形冰息短控");

        builder.register(HeroClass.RAZOR, "裂锋", Material.WHITE_BANNER, player -> {
            player.getInventory().addItem(HeroItems.unbreakable(Material.DIAMOND_AXE));
            player.getInventory().setItem(1, HeroItems.named(Material.WHITE_BANNER, "裂锋技能", List.of("向前突进并斩击首个目标", "附带短暂虚弱效果")));
            player.getInventory().setHelmet(HeroItems.unbreakable(Material.IRON_HELMET));
            player.getInventory().setChestplate(HeroItems.unbreakable(Material.IRON_CHESTPLATE));
            player.getInventory().setLeggings(HeroItems.leather(Material.LEATHER_LEGGINGS, Color.GRAY));
            player.getInventory().setBoots(HeroItems.unbreakable(Material.IRON_BOOTS));
            HeroRegistryBuilder.addStew(player, 3);
        }, HeroRegistryBuilder.bindings(HeroRegistryBuilder.binding("razor_dash", Material.WHITE_BANNER)), "Tier 2", "装备：钻石斧，铁头盔，铁胸甲，皮革护腿，铁靴子，战旗", "技能：突进斩击首个目标");
    }
}
