package com.fyoyi.betterfood.util;

import com.fyoyi.betterfood.config.FoodConfig;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class FreshnessHelper {
    public static final String TAG_EXPIRY = "better_food_expiry";

    /**
     * 获取过期时间戳
     * 修改点：检测保质期是否为 -1 (SHELF_LIFE_INFINITE)
     */
    public static long getExpiryTime(Level level, ItemStack stack, boolean createIfMissing) {
        // 1. 如果全局腐烂关闭，视为无限
        if (!TimeManager.DECAY_ENABLED) {
            clearData(stack);
            return Long.MAX_VALUE;
        }

        // 2. 如果配置表里说它不能腐烂 (如腐肉)，视为无限
        if (!FoodConfig.canRot(stack)) return Long.MAX_VALUE;

        // 3. 【新增逻辑】如果配置表里保质期是 -1，视为无限
        long lifetime = FoodConfig.getItemLifetime(stack);
        if (lifetime == FoodConfig.SHELF_LIFE_INFINITE) { // 即 -1
            // 顺便清除可能存在的旧标签，保持干净
            clearData(stack);
            return Long.MAX_VALUE;
        }

        // 4. 如果只读且没标签，视为无限
        if (!createIfMissing && (!stack.hasTag() || !stack.getTag().contains(TAG_EXPIRY))) {
            return Long.MAX_VALUE;
        }

        // 5. 写入逻辑
        CompoundTag tag = stack.getOrCreateTag();
        if (!tag.contains(TAG_EXPIRY)) {
            if (createIfMissing) {
                long expiry = TimeManager.getEffectiveTime(level) + lifetime;
                tag.putLong(TAG_EXPIRY, expiry);
            } else {
                return Long.MAX_VALUE;
            }
        }
        return tag.getLong(TAG_EXPIRY);
    }

    /**
     * 设置过期时间
     */
    public static void setExpiryTime(ItemStack stack, long expiry) {
        // 如果这东西本身是永久保鲜的，就不要给它强加时间戳了
        if (FoodConfig.getItemLifetime(stack) == FoodConfig.SHELF_LIFE_INFINITE) return;

        stack.getOrCreateTag().putLong(TAG_EXPIRY, expiry);
    }

    /**
     * 清除 NBT 数据
     */
    public static void clearData(ItemStack stack) {
        if (stack.hasTag() && stack.getTag().contains(TAG_EXPIRY)) {
            stack.getTag().remove(TAG_EXPIRY);
        }
    }

    /**
     * 获取剩余新鲜度百分比 (0.0 - 1.0)
     */
    public static float getFreshnessPercentage(Level level, ItemStack stack) {
        // 如果保质期是 -1，永远 100% 新鲜
        long lifetime = FoodConfig.getItemLifetime(stack);
        if (lifetime == FoodConfig.SHELF_LIFE_INFINITE) return 1.0F;

        long expiry = getExpiryTime(level, stack, false);
        if (expiry == Long.MAX_VALUE) return 1.0F;

        long now = TimeManager.getEffectiveTime(level);
        long remaining = expiry - now;

        return Math.max(0.0F, Math.min(1.0F, (float) remaining / (float) lifetime));
    }

    /**
     * 判断是否已腐烂
     */
    public static boolean isRotten(Level level, ItemStack stack) {
        if (!TimeManager.DECAY_ENABLED || TimeManager.isPaused()) return false;

        // 如果保质期是 -1，直接返回 false (不腐烂)
        if (FoodConfig.getItemLifetime(stack) == FoodConfig.SHELF_LIFE_INFINITE) return false;

        long expiry = getExpiryTime(level, stack, false);
        if (expiry == Long.MAX_VALUE) return false;

        return TimeManager.getEffectiveTime(level) >= expiry;
    }

    /**
     * 格式化时间显示
     */
    public static String formatDuration(long ticks) {
        if (ticks < 0) return "∞"; // 容错处理

        long totalSeconds = ticks / 20;
        long minutes = totalSeconds / 60;
        long seconds = totalSeconds % 60;
        if (minutes > 0) return minutes + "min" + seconds + "s";
        return seconds + "s";
    }

    /**
     * 烹饪继承逻辑
     */
    public static void applyCookingFreshness(Level level, ItemStack input1, ItemStack input2, ItemStack output) {
        // 如果产物是永久保鲜的，不需要计算
        if (FoodConfig.getItemLifetime(output) == FoodConfig.SHELF_LIFE_INFINITE) return;

        float p1 = getFreshnessPercentage(level, input1);
        float p2 = input2.isEmpty() ? p1 : getFreshnessPercentage(level, input2);

        float avg = (p1 + p2) / 2.0F;
        float newPercent = Math.min(1.0F, avg * 1.2F);

        long outputLifetime = FoodConfig.getItemLifetime(output);
        long newRemainingTicks = (long) (outputLifetime * newPercent);

        long newExpiry = TimeManager.getEffectiveTime(level) + newRemainingTicks;
        setExpiryTime(output, newExpiry);
    }
}
