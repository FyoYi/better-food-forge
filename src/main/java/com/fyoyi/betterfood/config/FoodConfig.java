package com.fyoyi.betterfood.config;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.HashMap;
import java.util.Map;

public class FoodConfig {
    // 默认保质期 (10分钟)
    public static final long DEFAULT_LIFETIME = 12000L;

    // 特殊食物保质期表
    private static final Map<Item, Long> CUSTOM_LIFETIMES = new HashMap<>();

    static {
        // --- 在这里配置不同食物的保质期 (需求2) ---
        // 比如：熟肉保质期更短 (5分钟)
        register(Items.COOKED_PORKCHOP, 6000L);
        register(Items.COOKED_BEEF, 6000L);

        // 比如：金苹果保质期极长 (1小时)
        register(Items.GOLDEN_APPLE, 72000L);
    }

    private static void register(Item item, long ticks) {
        CUSTOM_LIFETIMES.put(item, ticks);
    }

    /**
     * 获取某个物品的保质期
     */
    public static long getItemLifetime(ItemStack stack) {
        if (stack.isEmpty()) return 0;
        // 如果表里有，就用表里的；否则用默认的
        return CUSTOM_LIFETIMES.getOrDefault(stack.getItem(), DEFAULT_LIFETIME);
    }

    /**
     * 判断物品是否可以腐烂
     */
    public static boolean canRot(ItemStack stack) {
        // 腐肉永远不腐烂
        if (stack.getItem() == Items.ROTTEN_FLESH) return false;
        // 只有食物才腐烂 (你也可以在这里添加白名单/黑名单)
        return stack.getItem().isEdible();
    }
}