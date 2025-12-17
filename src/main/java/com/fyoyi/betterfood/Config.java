/*
 * 配置类
 * 该类负责管理模组的配置选项
 * 使用Forge的配置API来定义和加载配置选项
 */
package com.fyoyi.betterfood;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.config.ModConfigEvent;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

// An example config class. This is not required, but it's a good idea to have one to keep your config organized.
// Demonstrates how to use Forge's config APIs
@Mod.EventBusSubscriber(modid = better_food.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class Config
{
    private static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();

    /*
     * 是否记录泥土方块的配置选项
     */
    private static final ForgeConfigSpec.BooleanValue LOG_DIRT_BLOCK = BUILDER
            .comment("Whether to log the dirt block on common setup")
            .define("logDirtBlock", true);

    /*
     * 魔法数字配置选项
     */
    private static final ForgeConfigSpec.IntValue MAGIC_NUMBER = BUILDER
            .comment("A magic number")
            .defineInRange("magicNumber", 42, 0, Integer.MAX_VALUE);

    /*
     * 魔法数字介绍文本配置选项
     */
    public static final ForgeConfigSpec.ConfigValue<String> MAGIC_NUMBER_INTRODUCTION = BUILDER
            .comment("What you want the introduction message to be for the magic number")
            .define("magicNumberIntroduction", "The magic number is... ");

    // a list of strings that are treated as resource locations for items
    /*
     * 物品字符串列表配置选项
     */
    private static final ForgeConfigSpec.ConfigValue<List<? extends String>> ITEM_STRINGS = BUILDER
            .comment("A list of items to log on common setup.")
            .defineListAllowEmpty("items", List.of("minecraft:iron_ingot"), Config::validateItemName);

    /*
     * 配置规范构建完成后的实例
     */
    static final ForgeConfigSpec SPEC = BUILDER.build();

    /*
     * 是否记录泥土方块的实际值
     */
    public static boolean logDirtBlock;
    
    /*
     * 魔法数字的实际值
     */
    public static int magicNumber;
    
    /*
     * 魔法数字介绍文本的实际值
     */
    public static String magicNumberIntroduction;
    
    /*
     * 物品集合的实际值
     */
    public static Set<Item> items;

    /*
     * 验证物品名称是否有效
     * @param obj 要验证的对象
     * @return boolean 物品名称是否有效
     */
    private static boolean validateItemName(final Object obj)
    {
        return obj instanceof final String itemName && ForgeRegistries.ITEMS.containsKey(ResourceLocation.tryParse( itemName));
    }

    /*
     * 配置加载事件处理函数
     * 在ModConfigEvent.Load事件触发时调用，用于加载配置值
     * @param event ModConfigEvent 事件对象
     */
    @SubscribeEvent
    static void onLoad(final ModConfigEvent event)
    {
        logDirtBlock = LOG_DIRT_BLOCK.get();
        magicNumber = MAGIC_NUMBER.get();
        magicNumberIntroduction = MAGIC_NUMBER_INTRODUCTION.get();

        // convert the list of strings into a set of items
        items = ITEM_STRINGS.get().stream()
                .map(itemName -> ForgeRegistries.ITEMS.getValue(ResourceLocation.tryParse(itemName)))
                .collect(Collectors.toSet());
    }
}