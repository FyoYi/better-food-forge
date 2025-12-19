package com.fyoyi.betterfood.util;

import com.fyoyi.betterfood.config.FoodConfig;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.item.Item;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraft.world.effect.MobEffect;

import java.util.Map;
import java.util.List;
import java.util.ArrayList;
import java.util.Set;
import java.util.HashSet;

public class FoodExpiryManager extends SimpleJsonResourceReloadListener {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();

    public FoodExpiryManager() {
        super(GSON, "better_food_expiry");
    }

    @Override
    protected void apply(Map<ResourceLocation, JsonElement> objectIn, ResourceManager resourceManagerIn, ProfilerFiller profilerIn) {
        // 1. 清空旧数据
        FoodConfig.clear();
        System.out.println("[BetterFood] 开始加载数据包配置...");

        // 2. 加载数据包里的 JSON (默认配置)
        for (Map.Entry<ResourceLocation, JsonElement> entry : objectIn.entrySet()) {
            try {
                JsonObject jsonObject = entry.getValue().getAsJsonObject();
                
                // 检查是否是新的食物组结构
                if (jsonObject.has("food_groups")) {
                    // 处理新的食物组结构
                    JsonObject foodGroups = jsonObject.getAsJsonObject("food_groups");
                    processFoodGroups(foodGroups);
                } else {
                    // 处理旧的直接结构
                    long ticks = jsonObject.get("ticks").getAsLong();
                    processFoodItems(jsonObject.getAsJsonArray("items"), ticks);
                }
            } catch (Exception e) {
                System.err.println("[BetterFood] 解析失败: " + entry.getKey());
                e.printStackTrace();
            }
        }

        // 3. 注册默认食物属性
        registerDefaultFoodAttributes();
        
        // 4. 【核心新增】最后加载用户的本地覆盖配置 (优先级最高)
        UserConfigManager.loadOverrides();

        System.out.println("[BetterFood] 所有保质期配置加载完毕。注册的食物数量: " + FoodConfig.getRegisteredFoodCount());
    }

    /**
     * 处理食物组结构
     */
    private void processFoodGroups(JsonObject foodGroups) {
        for (Map.Entry<String, JsonElement> groupEntry : foodGroups.entrySet()) {
            JsonObject group = groupEntry.getValue().getAsJsonObject();
            long ticks = group.get("ticks").getAsLong();
            JsonArray items = group.getAsJsonArray("items");
            processFoodItems(items, ticks);
        }
    }

    /**
     * 处理食物项
     */
    private void processFoodItems(JsonArray items, long ticks) {
        for (JsonElement itemElement : items) {
            // 支持两种格式：
            // 1. 字符串格式 (旧版): "minecraft:apple"
            // 2. 对象格式 (新版): {"item": "minecraft:apple", "effects": [...], "tags": [...]}
            
            if (itemElement.isJsonPrimitive()) {
                // 旧格式：只有物品ID
                String itemIdStr = itemElement.getAsString();
                Item item = parseItemId(itemIdStr);
                if (item != null) {
                    FoodConfig.register(item, ticks);
                }
            } else if (itemElement.isJsonObject()) {
                // 新格式：包含物品ID、效果和标签
                JsonObject itemObj = itemElement.getAsJsonObject();
                String itemIdStr = itemObj.get("item").getAsString();
                Item item = parseItemId(itemIdStr);
                
                if (item != null) {
                    // 注册保质期
                    FoodConfig.register(item, ticks);
                    
                    // 如果有效果配置，也一起注册
                    if (itemObj.has("effects")) {
                        List<FoodConfig.EffectBonus> effects = new ArrayList<>();
                        for (JsonElement effectElement : itemObj.getAsJsonArray("effects")) {
                            JsonObject effectObj = effectElement.getAsJsonObject();
                            
                            // 解析效果类型
                            String effectType = effectObj.get("effect").getAsString();
                            MobEffect mobEffect = parseEffect(effectType);
                            if (mobEffect == null) {
                System.err.println("[BetterFood] 未知效果类型: " + effectType);
                                continue;
                            }
                            
                            // 解析参数
                            float chance = effectObj.get("chance").getAsFloat();
                            int duration = effectObj.get("duration").getAsInt();
                            int amplifier = effectObj.has("amplifier") ? effectObj.get("amplifier").getAsInt() : 0;
                            
                            effects.add(new FoodConfig.EffectBonus(mobEffect, chance, duration, amplifier));
                        }
                        
                        if (!effects.isEmpty()) {
                            FoodConfig.registerBonus(item, effects);
                            System.out.println("[BetterFood] 已为 " + itemIdStr + " 注册 " + effects.size() + " 个新鲜奖励效果");
                        }
                    }
                    
                    // 如果有标签配置，也一起注册（兼容旧格式）
                    if (itemObj.has("tags")) {
                        Set<String> tags = new HashSet<>();
                        for (JsonElement tagElement : itemObj.getAsJsonArray("tags")) {
                            tags.add(tagElement.getAsString());
                        }
                        
                        if (!tags.isEmpty()) {
                            FoodConfig.registerTags(item, tags);
                            System.out.println("[BetterFood] 已为 " + itemIdStr + " 注册 " + tags.size() + " 个食物点: " + tags);
                        }
                    }
                    
                    // 如果有新的属性配置，也一起注册
                    if (itemObj.has("classification") || itemObj.has("features") || itemObj.has("nature")) {
                        Set<String> attributes = new HashSet<>();
                        
                        // 添加分类
                        if (itemObj.has("classification")) {
                            String classification = itemObj.get("classification").getAsString();
                            attributes.add("分类:" + classification);
                        }
                        
                        // 添加特点
                        if (itemObj.has("features")) {
                            JsonArray features = itemObj.getAsJsonArray("features");
                            for (JsonElement featureElement : features) {
                                attributes.add("特点:" + featureElement.getAsString());
                            }
                        }
                        
                        // 添加性质
                        if (itemObj.has("nature")) {
                            String nature = itemObj.get("nature").getAsString();
                            attributes.add("熟度:" + nature);
                        }
                        
                        if (!attributes.isEmpty()) {
                            FoodConfig.registerTags(item, attributes);
                            System.out.println("[BetterFood] 已为 " + itemIdStr + " 注册属性: " + attributes);
                        }
                    }
                }
            }
        }
    }

    private Item parseItemId(String itemIdStr) {
        ResourceLocation rl = ResourceLocation.tryParse(itemIdStr);
        if (rl == null) {
            System.err.println("[BetterFood] 无效的物品ID: " + itemIdStr);
            return null;
        }
        Item item = ForgeRegistries.ITEMS.getValue(rl);
        if (item == null) {
            System.err.println("[BetterFood] 未找到物品: " + itemIdStr);
        }
        return item;
    }

    /**
     * 注册默认食物属性
     */
    private void registerDefaultFoodAttributes() {
        // 为腐肉注册默认属性：分类：肉类 特点：怪物肉 性质：生食
        Item rottenFlesh = net.minecraft.world.item.Items.ROTTEN_FLESH;
        Set<String> tags = new HashSet<>();
        tags.add("分类:肉类");
        tags.add("特点:怪物肉");
        tags.add("熟度:0%");
        FoodConfig.registerTags(rottenFlesh, tags);
        System.out.println("[BetterFood] 已为腐肉注册默认属性: 分类:肉类, 特点:怪物肉, 熟度:0%");
    }
    
    private MobEffect parseEffect(String effectStr) {
        ResourceLocation rl = ResourceLocation.tryParse(effectStr);
        if (rl == null) {
            // 尝试添加 minecraft 命名空间前缀
            rl = ResourceLocation.tryParse("minecraft:" + effectStr);
            if (rl == null) {
                return null;
            }
        }
        return ForgeRegistries.MOB_EFFECTS.getValue(rl);
    }
}