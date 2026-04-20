package jdd.war.hero;

import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

/**
 * 英雄药水效果构建工具类 - 消除重复的效果添加代码
 * 提供便利的药水效果应用方法
 */
public final class HeroPotionEffectBuilder {
    private HeroPotionEffectBuilder() {
    }

    /**
     * 添加速度效果
     */
    public static void addSpeedEffect(Player player, int level) {
        player.addPotionEffect(new PotionEffect(
                PotionEffectType.SPEED,
                PotionEffect.INFINITE_DURATION,
                Math.max(0, level - 1),
                false,
                false
        ));
    }

    /**
     * 添加抗性效果
     */
    public static void addResistanceEffect(Player player, int level) {
        player.addPotionEffect(new PotionEffect(
                PotionEffectType.RESISTANCE,
                PotionEffect.INFINITE_DURATION,
                Math.max(0, level - 1),
                false,
                false
        ));
    }

    /**
     * 添加力量效果
     */
    public static void addStrengthEffect(Player player, int level) {
        player.addPotionEffect(new PotionEffect(
                PotionEffectType.STRENGTH,
                PotionEffect.INFINITE_DURATION,
                Math.max(0, level - 1),
                false,
                false
        ));
    }

    /**
     * 添加多个永久性效果
     */
    public static void addPermanentEffects(Player player, PotionEffect... effects) {
        for (PotionEffect effect : effects) {
            PotionEffect permanent = new PotionEffect(
                    effect.getType(),
                    PotionEffect.INFINITE_DURATION,
                    effect.getAmplifier(),
                    effect.isAmbient(),
                    effect.hasParticles()
            );
            player.addPotionEffect(permanent);
        }
    }

    /**
     * 便利方法：添加速度 II 效果
     */
    public static void addSpeedII(Player player) {
        addSpeedEffect(player, 2);
    }

    /**
     * 便利方法：添加抗性 I 效果
     */
    public static void addResistanceI(Player player) {
        addResistanceEffect(player, 1);
    }

    /**
     * 便利方法：添加力量 I 效果
     */
    public static void addStrengthI(Player player) {
        addStrengthEffect(player, 1);
    }

    /**
     * 便利方法：添加 OP 职业的完整效果组合（速度 II + 抗性 I + 力量 I）
     */
    public static void addOPEffects(Player player) {
        addSpeedII(player);
        addResistanceI(player);
        addStrengthI(player);
    }
}
