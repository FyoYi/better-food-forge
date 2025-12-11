package com.fyoyi.betterfood.item.sundries;

import com.fyoyi.betterfood.item.ModItems;
import net.minecraft.world.item.Item;
import net.minecraftforge.registries.RegistryObject;

public class Sundries_item {

    public static final RegistryObject<Item> TEXT_ITEM =
            ModItems.ITEMS.register("text_item", () -> new Item(new Item.Properties().
                    stacksTo(64)));//注册红宝石

    public static final RegistryObject<Item> RUBY =
            ModItems.ITEMS.register("ruby", () -> new Item(new Item.Properties().
                    stacksTo(64)));

    public static final RegistryObject<Item> RUBY_GRAINS =
            ModItems.ITEMS.register("ruby_grains", () -> new Item(new Item.Properties().
                    stacksTo(64)));

    public static final RegistryObject<Item> RUBY_COPPER =
            ModItems.ITEMS.register("ruby_copper", () -> new Item(new Item.Properties().
                    stacksTo(64)));

    public static final RegistryObject<Item> FIREWORK_ARROW =
            ModItems.ITEMS.register("firework_arrow",
                    () -> new FireworkArrowItem(new Item.Properties().stacksTo(64)));

//    public static final RegistryObject<Item> FIREWORK_ARROW =
//            ModItems.ITEMS.register("firework_arrow", () -> new Item(new Item.Properties().
//                    stacksTo(64)));

    public static void init() {
        // 这个方法是空的，它的存在就是为了让主类可以调用它来加载这个类
    }

}
