package com.fyoyi.betterfood.config;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.HashMap;
import java.util.Map;

public class FoodConfig {

    public static final long TICKS_PER_DAY = 24000L;
    public static final long SHELF_LIFE_DEFAULT = 7 * TICKS_PER_DAY;
    public static final long SHELF_LIFE_INFINITE = -1L;

    // === 预设时间常量 (分钟) ===
    // 这里的单位是分钟，方便指令调用
    public static final float MIN_MINUTES = (2.5f * 20); // 50分钟
    public static final float SHORT_MINUTES = (5.0f * 20); // 100分钟
    public static final float MEDIUM_MINUTES = (12.0f * 20); // 240分钟
    public static final float LONG_MINUTES = (50.0f * 20); // 1000分钟

    private static final Map<Item, Long> CUSTOM_LIFETIMES = new HashMap<>();

    public static void clear() {
        CUSTOM_LIFETIMES.clear();
    }

    public static void register(Item item, long ticks) {
        CUSTOM_LIFETIMES.put(item, ticks);
    }

    // === 【新增】移除配置 ===
    public static void remove(Item item) {
        CUSTOM_LIFETIMES.remove(item);
    }

    public static long getItemLifetime(ItemStack stack) {
        if (stack.isEmpty()) return 0;
        if (CUSTOM_LIFETIMES.containsKey(stack.getItem())) {
            return CUSTOM_LIFETIMES.get(stack.getItem());
        }
        return SHELF_LIFE_DEFAULT;
    }

    public static boolean canRot(ItemStack stack) {
        if (stack.getItem() == Items.ROTTEN_FLESH) return false;
        if (CUSTOM_LIFETIMES.containsKey(stack.getItem())) return true;
        return stack.getItem().isEdible();
    }
}