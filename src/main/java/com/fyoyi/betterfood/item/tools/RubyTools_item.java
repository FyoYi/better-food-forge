package com.fyoyi.betterfood.item.tools;

import com.fyoyi.betterfood.item.ModItems;
import com.fyoyi.betterfood.util.tools.RubyTools_util;
import net.minecraft.world.item.*;
import net.minecraftforge.registries.RegistryObject;

public class RubyTools_item {

    public static final RegistryObject<Item> RUBY_PICKAXE =
            ModItems.ITEMS.register("ruby_pickaxe",() -> new PickaxeItem(
                    RubyTools_util.RUBY_PICKAXE,
                    0,
                    -2.8F,
                    new Item.Properties().stacksTo(1)
            ));

    public static final RegistryObject<Item> RUBY_HOE =
            ModItems.ITEMS.register("ruby_hoe",() -> new HoeItem(
                    RubyTools_util.RUBY_HOE,
                    0,
                    0.0F,
                    new Item.Properties().stacksTo(1)
            ));

    public static final RegistryObject<Item> RUBY_AXE =
            ModItems.ITEMS.register("ruby_axe",() -> new AxeItem(
                    RubyTools_util.RUBY_AXE,
                    5,
                    -3.0F,
                    new Item.Properties().stacksTo(1)
            ));

    public static final RegistryObject<Item> RUBY_SHOVEL =
            ModItems.ITEMS.register("ruby_shovel",() -> new ShovelItem(
                    RubyTools_util.RUBY_SHOVEL,
                    0,
                    -3.0F,
                    new Item.Properties().stacksTo(1)
            ));

    public static void init() {
        // 这个方法是空的，它的存在就是为了让主类可以调用它来加载这个类
    }
}
