package com.fyoyi.betterfood.item;

import com.fyoyi.betterfood.item.weapon.bow.Bow_item;
import com.fyoyi.betterfood.item.weapon.CrossBow_item;
import com.fyoyi.betterfood.item.weapon.Spear_item;
import com.fyoyi.betterfood.item.weapon.Sword_item;
import com.fyoyi.betterfood.item.tools.RubyTools_item;
import com.fyoyi.betterfood.item.sundries.Sundries_item;
import com.fyoyi.betterfood.better_food;
import net.minecraft.world.item.*;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

public class ModItems {
    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(ForgeRegistries.ITEMS, better_food.MOD_ID);

    public static void init() {
        Sundries_item.init();
        Bow_item.init();
        CrossBow_item.init();
        Spear_item.init();
        Sword_item.init();
        RubyTools_item.init();
    }

    public static void register(IEventBus eventBus){
        ITEMS.register(eventBus);
    }
}
