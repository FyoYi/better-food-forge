package com.fyoyi.betterfood.item.weapon;

import com.fyoyi.betterfood.item.ModItems;
import com.fyoyi.betterfood.util.weapon.Sword_util;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.SwordItem;
import net.minecraftforge.registries.RegistryObject;

public class Sword_item {

    public static final RegistryObject<Item> RUBY_SWORD =
            ModItems.ITEMS.register("ruby_sword", () -> new SwordItem(
                    Sword_util.RUBY_SWORD,
                    2,
                    -2.2F,
                    new Item.Properties().stacksTo(1)
            ));

    public static final RegistryObject<Item> RUBY_INLAID_WOODEN_SWORD =
            ModItems.ITEMS.register("ruby_inlaid_wooden_sword", () -> new SwordItem(
                    Sword_util.RUBY_WOODEN_SWORD,
                    2,
                    -2.2F,
                    new Item.Properties().stacksTo(1)
            ));

    public static final RegistryObject<Item> RUBY_INLAID_GOLDEN_SWORD =
            ModItems.ITEMS.register("ruby_inlaid_golden_sword", () -> new SwordItem(
                    Sword_util.RUBY_GOLDEN_SWORD,
                    2,
                    -2.2F,
                    new Item.Properties().stacksTo(1)
            ));

    public static final RegistryObject<Item> RUBY_INLAID_STONE_SWORD =
            ModItems.ITEMS.register("ruby_inlaid_stone_sword", () -> new SwordItem(
                    Sword_util.RUBY_STONE_SWORD,
                    2,
                    -2.2F,
                    new Item.Properties().stacksTo(1)
            ));

    public static final RegistryObject<Item> RUBY_INLAID_IRON_SWORD =
            ModItems.ITEMS.register("ruby_inlaid_iron_sword", () -> new SwordItem(
                    Sword_util.RUBY_IRON_SWORD,
                    2,
                    -2.2F,
                    new Item.Properties().stacksTo(1)
            ));

    public static final RegistryObject<Item> RUBY_INLAID_DIAMOND_SWORD =
            ModItems.ITEMS.register("ruby_inlaid_diamond_sword", () -> new SwordItem(
                    Sword_util.RUBY_DIAMOND_SWORD,
                    2,
                    -2.2F,
                    new Item.Properties().stacksTo(1)
            ));

    public static final RegistryObject<Item> RUBY_INLAID_NETHERITE_SWORD =
            ModItems.ITEMS.register("ruby_inlaid_netherite_sword", () -> new SwordItem(
                    Sword_util.RUBY_NETTHERITE_SWORD,
                    2,
                    -2.2F,
                    new Item.Properties().stacksTo(1)
            ));

    public static void init() {
        // 这个方法是空的，它的存在就是为了让主类可以调用它来加载这个类
    }
}
