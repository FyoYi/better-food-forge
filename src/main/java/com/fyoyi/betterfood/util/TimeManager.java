package com.fyoyi.betterfood.util;

import net.minecraft.world.level.Level;

public class TimeManager {
    // 全局腐烂开关
    public static boolean DECAY_ENABLED = true;

    // 暂停逻辑
    private static boolean IS_PAUSED = false;
    private static long PAUSE_START_TIME = 0L;
    private static long TOTAL_PAUSED_DURATION = 0L;

    /**
     * 获取当前的"有效游戏时间"
     * @param level 世界
     * @return 扣除暂停时间后的时间戳
     */
    public static long getEffectiveTime(Level level) {
        if (level == null) return 0;
        long rawTime = level.getGameTime();

        if (IS_PAUSED) {
            return PAUSE_START_TIME - TOTAL_PAUSED_DURATION;
        } else {
            return rawTime - TOTAL_PAUSED_DURATION;
        }
    }

    /**
     * 设置暂停状态
     */
    public static void setPaused(boolean paused, Level level) {
        if (IS_PAUSED == paused) return;
        IS_PAUSED = paused;
        if (paused) {
            PAUSE_START_TIME = level.getGameTime();
        } else {
            long duration = level.getGameTime() - PAUSE_START_TIME;
            TOTAL_PAUSED_DURATION += duration;
        }
    }

    public static boolean isPaused() {
        return IS_PAUSED;
    }
}
