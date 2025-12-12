package com.fyoyi.betterfood.util;

import com.fyoyi.betterfood.config.FoodConfig;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.item.Item;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.Map;

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
                long ticks = jsonObject.get("ticks").getAsLong();

                for (JsonElement itemElement : jsonObject.getAsJsonArray("items")) {
                    String itemIdStr = itemElement.getAsString();
                    ResourceLocation itemId = new ResourceLocation(itemIdStr);

                    if (ForgeRegistries.ITEMS.containsKey(itemId)) {
                        Item item = ForgeRegistries.ITEMS.getValue(itemId);
                        FoodConfig.register(item, ticks);
                    }
                }
            } catch (Exception e) {
                System.err.println("[BetterFood] 解析失败: " + entry.getKey());
            }
        }

        // 3. 【核心新增】最后加载用户的本地覆盖配置 (优先级最高)
        UserConfigManager.loadOverrides();

        System.out.println("[BetterFood] 所有保质期配置加载完毕。");
    }
}