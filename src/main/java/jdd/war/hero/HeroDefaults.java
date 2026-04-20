package jdd.war.hero;

/**
 * 英雄系统默认常量定义
 * 集中管理所有硬编码的数值常量，便于后期调整和维护
 */
public interface HeroDefaults {
    // 蘑菇煲相关
    int DEFAULT_STEW_COUNT = 3;
    int HEALER_STEW_COUNT = 5;
    int SUPPORT_STEW_COUNT = 4;

    // 药水效果相关
    int INFINITE_DURATION = -1; // Bukkit PotionEffect.INFINITE_DURATION

    // 英雄能力相关
    int SACRED_WAR_HEARTS = 4;
    int SACRED_WAR_COOLDOWN = 25;

    int WINDWALKER_RISE_COOLDOWN = 30;
    int WINDWALKER_DASH_COOLDOWN = 30;

    int HOMELANDER_FLIGHT_DURATION = 6;
    int HOMELANDER_FLIGHT_COOLDOWN = 20;
    int HOMELANDER_LASER_DAMAGE = 12;
    int HOMELANDER_LASER_COOLDOWN = 15;

    int OP_SHARPNESS_LEVEL = 5;
    int OP_PROTECTION_LEVEL = 4;

    // 其他默认值
    int MIN_STEW_COUNT = 1;
    int MAX_EQUIPMENT_ENCHANTMENT_LEVEL = 5;
}
