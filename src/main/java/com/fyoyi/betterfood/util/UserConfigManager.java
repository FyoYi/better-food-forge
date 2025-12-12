package com.fyoyi.betterfood.util;

import com.fyoyi.betterfood.config.FoodConfig;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraftforge.fml.loading.FMLPaths;
import net.minecraftforge.registries.ForgeRegistries;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

public class UserConfigManager {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path CONFIG_DIR = FMLPaths.CONFIGDIR.get().resolve("better_food");
    private static final File CONFIG_FILE = CONFIG_DIR.resolve("user_overrides.json").toFile();

    // 保存/更新
    public static void saveOverride(Item item, long ticks) {
        modifyJson(json -> {
            String key = ForgeRegistries.ITEMS.getKey(item).toString();
            json.addProperty(key, ticks);
        });
    }

    // === 【新增】移除单个条目 ===
    public static void removeOverride(Item item) {
        modifyJson(json -> {
            String key = ForgeRegistries.ITEMS.getKey(item).toString();
            if (json.has(key)) {
                json.remove(key);
            }
        });
    }

    // === 【新增】重置所有 (删除文件) ===
    public static void clearAllOverrides() {
        try {
            if (CONFIG_FILE.exists()) {
                Files.delete(CONFIG_FILE.toPath());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // 内部辅助方法：读取 -> 修改 -> 写入
    private static void modifyJson(java.util.function.Consumer<JsonObject> modifier) {
        try {
            if (!Files.exists(CONFIG_DIR)) Files.createDirectories(CONFIG_DIR);
            JsonObject json = new JsonObject();
            if (CONFIG_FILE.exists()) {
                try (FileReader reader = new FileReader(CONFIG_FILE)) {
                    JsonObject existing = GSON.fromJson(reader, JsonObject.class);
                    if (existing != null) json = existing;
                }
            }
            modifier.accept(json);
            try (FileWriter writer = new FileWriter(CONFIG_FILE)) {
                GSON.toJson(json, writer);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // 加载
    public static void loadOverrides() {
        if (!CONFIG_FILE.exists()) return;
        try (FileReader reader = new FileReader(CONFIG_FILE)) {
            JsonObject json = GSON.fromJson(reader, JsonObject.class);
            if (json == null) return;
            System.out.println("[BetterFood] 正在加载用户自定义配置...");
            for (Map.Entry<String, com.google.gson.JsonElement> entry : json.entrySet()) {
                ResourceLocation id = new ResourceLocation(entry.getKey());
                if (ForgeRegistries.ITEMS.containsKey(id)) {
                    Item item = ForgeRegistries.ITEMS.getValue(id);
                    FoodConfig.register(item, entry.getValue().getAsLong());
                }
            }
        } catch (IOException e) {
            System.err.println("[BetterFood] 读取失败: " + e.getMessage());
        }
    }
}