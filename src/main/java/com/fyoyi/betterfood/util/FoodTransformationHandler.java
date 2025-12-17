package com.fyoyi.betterfood.util;

import com.fyoyi.betterfood.config.FoodConfig;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.*;

@Mod.EventBusSubscriber(modid = "better_food", bus = Mod.EventBusSubscriber.Bus.FORGE)
public class FoodTransformationHandler {
    
    // 食物转换映射表 (原料 -> 成品)
    private static final Map<Item, Item> SINGLE_INGREDIENT_TRANSFORMATIONS = new HashMap<>();
    
    // 合成表转换映射表 (原料组合 -> 成品)
    private static final Map<Set<Item>, Item> CRAFTING_TRANSFORMATIONS = new HashMap<>();
    
    // 初始化转换映射表
    static {
        // 原版烹饪转换 (单食材)
        SINGLE_INGREDIENT_TRANSFORMATIONS.put(Items.PORKCHOP, Items.COOKED_PORKCHOP);
        SINGLE_INGREDIENT_TRANSFORMATIONS.put(Items.BEEF, Items.COOKED_BEEF);
        SINGLE_INGREDIENT_TRANSFORMATIONS.put(Items.CHICKEN, Items.COOKED_CHICKEN);
        SINGLE_INGREDIENT_TRANSFORMATIONS.put(Items.MUTTON, Items.COOKED_MUTTON);
        SINGLE_INGREDIENT_TRANSFORMATIONS.put(Items.RABBIT, Items.COOKED_RABBIT);
        SINGLE_INGREDIENT_TRANSFORMATIONS.put(Items.POTATO, Items.BAKED_POTATO);
        SINGLE_INGREDIENT_TRANSFORMATIONS.put(Items.KELP, Items.DRIED_KELP);
        SINGLE_INGREDIENT_TRANSFORMATIONS.put(Items.SALMON, Items.COOKED_SALMON);
        SINGLE_INGREDIENT_TRANSFORMATIONS.put(Items.COD, Items.COOKED_COD);
        
        // 原版合成转换 (多食材)
        // 面包 (3个小麦)
        Set<Item> breadIngredients = new HashSet<>(Arrays.asList(Items.WHEAT, Items.WHEAT, Items.WHEAT));
        CRAFTING_TRANSFORMATIONS.put(breadIngredients, Items.BREAD);
        
        // 饼干 (2个小麦 + 糖)
        Set<Item> cookieIngredients = new HashSet<>(Arrays.asList(Items.WHEAT, Items.WHEAT, Items.SUGAR));
        CRAFTING_TRANSFORMATIONS.put(cookieIngredients, Items.COOKIE);
        
        // 南瓜派 (南瓜 + 糖 + 鸡蛋)
        Set<Item> pumpkinPieIngredients = new HashSet<>(Arrays.asList(Items.PUMPKIN, Items.SUGAR, Items.EGG));
        CRAFTING_TRANSFORMATIONS.put(pumpkinPieIngredients, Items.PUMPKIN_PIE);
        
        // 蛋糕 (3个牛奶 + 2个糖 + 鸡蛋 + 小麦)
        // 注意：HashSet会去重，所以我们需要使用不同的表示方法
        // 这里简化处理，实际应用中可能需要更复杂的匹配逻辑
    }
    
    /**
     * 检查是否是有效的食物转换关系
     * @param input 输入物品
     * @param output 输出物品
     * @return 是否是有效的转换关系
     */
    public static boolean isValidFoodTransformation(Item input, Item output) {
        return SINGLE_INGREDIENT_TRANSFORMATIONS.containsKey(input) && 
               SINGLE_INGREDIENT_TRANSFORMATIONS.get(input) == output;
    }
    
    /**
     * 处理单食材转换（如熔炉烹饪）
     */
    public static void handleSingleIngredientTransformation(Level level, ItemStack input, ItemStack output) {
        if (input.isEmpty() || output.isEmpty()) return;
        
        Item inputItem = input.getItem();
        Item outputItem = output.getItem();
        
        // 检查是否是已知的转换关系
        if (SINGLE_INGREDIENT_TRANSFORMATIONS.containsKey(inputItem) && 
            SINGLE_INGREDIENT_TRANSFORMATIONS.get(inputItem) == outputItem) {
            
            // 继承新鲜值
            inheritFreshness(level, Collections.singletonList(input), output);
        }
    }
    
    /**
     * 处理多食材转换（如合成表）
     */
    public static void handleCraftingTransformation(Level level, List<ItemStack> ingredients, ItemStack result) {
        if (ingredients.isEmpty() || result.isEmpty()) return;
        
        // 构建原料集合（忽略数量）
        Set<Item> ingredientItems = new HashSet<>();
        List<ItemStack> validIngredients = new ArrayList<>();
        
        for (ItemStack ingredient : ingredients) {
            if (!ingredient.isEmpty() && FoodConfig.canRot(ingredient)) {
                ingredientItems.add(ingredient.getItem());
                validIngredients.add(ingredient);
            }
        }
        
        // 检查是否是已知的合成转换关系
        if (CRAFTING_TRANSFORMATIONS.containsKey(ingredientItems) && 
            CRAFTING_TRANSFORMATIONS.get(ingredientItems) == result.getItem()) {
            
            // 继承新鲜值
            inheritFreshness(level, validIngredients, result);
        }
    }
    
    /**
     * 新鲜值继承逻辑
     * @param level 世界对象
     * @param ingredients 原料列表
     * @param result 结果物品
     */
    private static void inheritFreshness(Level level, List<ItemStack> ingredients, ItemStack result) {
        if (ingredients.isEmpty() || result.isEmpty()) return;
        
        // 如果结果物品是永久保鲜的，不需要计算
        if (FoodConfig.getItemLifetime(result) == FoodConfig.SHELF_LIFE_INFINITE) return;
        
        // 1. 计算所有原料的新鲜度百分比平均值
        float totalFreshness = 0;
        int validCount = 0;
        
        for (ItemStack ingredient : ingredients) {
            if (!ingredient.isEmpty() && FoodConfig.canRot(ingredient)) {
                float freshness = FreshnessHelper.getFreshnessPercentage(level, ingredient);
                totalFreshness += freshness;
                validCount++;
            }
        }
        
        if (validCount == 0) return;
        
        float averageFreshness = totalFreshness / validCount;
        
        // 2. 应用1.1倍率
        float inheritedFreshness = averageFreshness * 1.1f;
        
        // 3. 确保不超过上限
        inheritedFreshness = Math.min(inheritedFreshness, 1.0f);
        
        // 4. 转换为实际的过期时间
        long resultLifetime = FoodConfig.getItemLifetime(result);
        long currentTime = TimeManager.getEffectiveTime(level);
        long remainingTicks = (long) (resultLifetime * inheritedFreshness);
        long expiryTime = currentTime + remainingTicks;
        
        // 5. 设置过期时间到结果物品
        FreshnessHelper.setExpiryTime(result, expiryTime);
    }
    
    /**
     * 监听物品合成事件
     */
    @SubscribeEvent
    public static void onItemCrafted(PlayerEvent.ItemCraftedEvent event) {
        // 处理合成表转换
        // 注意：这里需要从事件中正确获取合成矩阵
    }
    
    /**
     * 从合成矩阵中提取原料
     */
    private static List<ItemStack> getCraftingIngredients(net.minecraft.world.inventory.CraftingContainer craftMatrix) {
        List<ItemStack> ingredients = new ArrayList<>();
        for (int i = 0; i < craftMatrix.getContainerSize(); i++) {
            ItemStack stack = craftMatrix.getItem(i);
            if (!stack.isEmpty()) {
                ingredients.add(stack);
            }
        }
        return ingredients;
    }
    
    /**
     * 监听熔炉烧制完成事件
     * 注意：这个事件在Forge中可能不直接提供原料信息，需要特殊处理
     */
    @SubscribeEvent
    public static void onItemSmelted(PlayerEvent.ItemSmeltedEvent event) {
        // 获取玩家和烧制结果
        ItemStack result = event.getSmelting();
        net.minecraft.world.entity.player.Player player = event.getEntity();
        
        // 注意：ItemSmeltedEvent不提供原始输入物品的信息
        // 我们需要通过其他方式追踪输入物品的新鲜度
        // 这里可以实现一个简单的解决方案：
        // 假设玩家背包中最近使用的相同食材就是输入物品
        // 或者我们可以实现一个更复杂的追踪机制
    }
}