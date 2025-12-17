package com.fyoyi.betterfood.util;

import com.fyoyi.betterfood.config.FoodConfig;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.AbstractFurnaceBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.HashMap;
import java.util.Map;

@Mod.EventBusSubscriber(modid = "better_food", bus = Mod.EventBusSubscriber.Bus.FORGE)
public class FurnaceTrackingHandler {
    
    // 存储熔炉输入槽位的物品信息，用于追踪新鲜度
    private static final Map<BlockPos, ItemStack> FURNACE_INPUT_TRACKING = new HashMap<>();
    
    /**
     * 监听世界刻事件，检查熔炉状态变化
     */
    @SubscribeEvent
    public static void onWorldTick(TickEvent.LevelTickEvent event) {
        if (event.phase == TickEvent.Phase.END && !event.level.isClientSide) {
            // 每隔一定时间检查一次熔炉状态
            if (event.level.getGameTime() % 20 == 0) { // 每秒检查一次
                checkFurnaces(event.level);
            }
        }
    }
    
    /**
     * 检查世界中的熔炉状态
     */
    private static void checkFurnaces(Level level) {
        // 注意：这种方法在大型世界中可能会影响性能
        // 在实际应用中，可能需要更高效的追踪机制
    }
    
    /**
     * 记录熔炉输入槽位的物品
     * 这个方法需要在物品被放入熔炉时调用
     */
    public static void trackFurnaceInput(BlockPos pos, ItemStack input) {
        if (!input.isEmpty() && FoodConfig.canRot(input)) {
            FURNACE_INPUT_TRACKING.put(pos, input.copy()); // 复制物品以保存其当前状态
        }
    }
    
    /**
     * 处理熔炉烹饪完成事件
     * 当熔炉完成烹饪时调用此方法
     */
    public static void handleFurnaceSmeltingComplete(Level level, BlockPos pos, ItemStack result) {
        if (!result.isEmpty() && FoodConfig.canRot(result)) {
            // 获取之前记录的输入物品
            ItemStack input = FURNACE_INPUT_TRACKING.get(pos);
            if (input != null && !input.isEmpty()) {
                // 检查是否是有效的食物转换
                if (FoodTransformationHandler.isValidFoodTransformation(input.getItem(), result.getItem())) {
                    // 继承新鲜度
                    inheritFreshnessFromInput(level, input, result);
                }
            }
            
            // 移除记录（烹饪完成后不再需要）
            FURNACE_INPUT_TRACKING.remove(pos);
        }
    }
    
    /**
     * 从输入物品继承新鲜度到结果物品
     */
    private static void inheritFreshnessFromInput(Level level, ItemStack input, ItemStack result) {
        // 使用FoodTransformationHandler中的新鲜度继承逻辑
        FoodTransformationHandler.handleSingleIngredientTransformation(level, input, result);
    }
    
    /**
     * 清理不再存在的熔炉记录
     */
    public static void cleanUpTracking(Level level) {
        FURNACE_INPUT_TRACKING.entrySet().removeIf(entry -> {
            BlockPos pos = entry.getKey();
            BlockEntity be = level.getBlockEntity(pos);
            return !(be instanceof AbstractFurnaceBlockEntity);
        });
    }
}