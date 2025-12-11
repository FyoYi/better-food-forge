package com.fyoyi.betterfood.util;

import com.fyoyi.betterfood.config.FoodConfig;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class FreshnessHelper {
    public static final String TAG_EXPIRY = "better_food_expiry";

    /**
     * 获取过期时间戳 (如果不存在且 create=true，则根据 FoodConfig 生成)
     */
    public static long getExpiryTime(Level level, ItemStack stack, boolean createIfMissing) {
        if (!TimeManager.DECAY_ENABLED) {
            clearData(stack);
            return Long.MAX_VALUE;
        }

        if (!FoodConfig.canRot(stack)) return Long.MAX_VALUE;

        // 如果只读且没标签
        if (!createIfMissing && (!stack.hasTag() || !stack.getTag().contains(TAG_EXPIRY))) {
            return Long.MAX_VALUE;
        }

        CompoundTag tag = stack.getOrCreateTag();
        if (!tag.contains(TAG_EXPIRY)) {
            if (createIfMissing) {
                // === 关键修改：从 Config 获取该物品特定的保质期 ===
                long lifetime = FoodConfig.getItemLifetime(stack);
                long expiry = TimeManager.getEffectiveTime(level) + lifetime;
                tag.putLong(TAG_EXPIRY, expiry);
            } else {
                return Long.MAX_VALUE;
            }
        }
        return tag.getLong(TAG_EXPIRY);
    }

    public static void setExpiryTime(ItemStack stack, long expiry) {
        stack.getOrCreateTag().putLong(TAG_EXPIRY, expiry);
    }

    public static void clearData(ItemStack stack) {
        if (stack.hasTag() && stack.getTag().contains(TAG_EXPIRY)) {
            stack.getTag().remove(TAG_EXPIRY);
        }
    }

    /**
     * 获取剩余新鲜度百分比 (0.0 - 1.0)
     */
    public static float getFreshnessPercentage(Level level, ItemStack stack) {
        long expiry = getExpiryTime(level, stack, false);
        if (expiry == Long.MAX_VALUE) return 1.0F;

        long now = TimeManager.getEffectiveTime(level);
        long remaining = expiry - now;
        long lifetime = FoodConfig.getItemLifetime(stack); // 获取物品的总寿命

        return Math.max(0.0F, Math.min(1.0F, (float) remaining / (float) lifetime));
    }

    /**
     * 判断是否已腐烂
     */
    public static boolean isRotten(Level level, ItemStack stack) {
        if (!TimeManager.DECAY_ENABLED || TimeManager.isPaused()) return false;
        long expiry = getExpiryTime(level, stack, false);
        if (expiry == Long.MAX_VALUE) return false;
        return TimeManager.getEffectiveTime(level) >= expiry;
    }

    /**
     * 格式化时间显示
     */
    public static String formatDuration(long ticks) {
        if (ticks < 0) ticks = 0;
        long totalSeconds = ticks / 20;
        long minutes = totalSeconds / 60;
        long seconds = totalSeconds % 60;
        if (minutes > 0) return minutes + "min" + seconds + "s";
        return seconds + "s";
    }

    // =========================================================
    // 为【需求1：烹饪系统】预留的方法
    // =========================================================
    public static void applyCookingFreshness(Level level, ItemStack input1, ItemStack input2, ItemStack output) {
        // 1. 获取输入的剩余百分比
        float p1 = getFreshnessPercentage(level, input1);
        float p2 = input2.isEmpty() ? p1 : getFreshnessPercentage(level, input2);

        // 2. 计算平均值并乘 1.2
        float avg = (p1 + p2) / 2.0F;
        float newPercent = Math.min(1.0F, avg * 1.2F);

        // 3. 根据输出产物的总寿命，反推新的过期时间
        long outputLifetime = FoodConfig.getItemLifetime(output);
        long newRemainingTicks = (long) (outputLifetime * newPercent);

        // 4. 写入 NBT
        long newExpiry = TimeManager.getEffectiveTime(level) + newRemainingTicks;
        setExpiryTime(output, newExpiry);
    }
}
