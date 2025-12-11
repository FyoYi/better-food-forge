package com.fyoyi.betterfood.creative_tab;

import com.fyoyi.betterfood.better_food;
import com.fyoyi.betterfood.item.sundries.Sundries_item;
import com.fyoyi.betterfood.item.weapon.Sword_item;
import com.fyoyi.betterfood.item.tools.RubyTools_item;
import com.fyoyi.betterfood.item.weapon.bow.Bow_item;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

public class ModCreativeModeTabs {
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, better_food.MOD_ID);

    public static final RegistryObject<CreativeModeTab> TEXT_TAB =
            CREATIVE_MODE_TABS.register("text_tab", () -> CreativeModeTab.builder()
                    .icon(() -> new ItemStack(Sundries_item.RUBY.get()))
                    .title(Component.translatable("ItemGroup.text_tab"))
                    .displayItems((pParameters, pOutput) -> {
                        pOutput.accept(Sundries_item.RUBY.get());
                        pOutput.accept(Sundries_item.RUBY_GRAINS.get());
                        pOutput.accept(Sundries_item.RUBY_COPPER.get());
                        pOutput.accept(Sword_item.RUBY_SWORD.get());
                        pOutput.accept(RubyTools_item.RUBY_PICKAXE.get());
                        pOutput.accept(RubyTools_item.RUBY_AXE.get());
                        pOutput.accept(RubyTools_item.RUBY_HOE.get());
                        pOutput.accept(RubyTools_item.RUBY_SHOVEL.get());
//                        pOutput.accept(Sword_item.RUBY_INLAID_WOODEN_SWORD.get());
//                        pOutput.accept(Sword_item.RUBY_INLAID_STONE_SWORD.get());
//                        pOutput.accept(Sword_item.RUBY_INLAID_GOLDEN_SWORD.get());
//                        pOutput.accept(Sword_item.RUBY_INLAID_IRON_SWORD.get());
//                        pOutput.accept(Sword_item.RUBY_INLAID_DIAMOND_SWORD.get());
                        pOutput.accept(Sword_item.RUBY_INLAID_NETHERITE_SWORD.get());
                        pOutput.accept(Bow_item.RUBY_INLAID_BOW.get());
                        //pOutput.accept(Sword_item.FIREWORK_ARROW.get());
                    }).build());

//    public static final RegistryObject<CreativeModeTab> TEXT_TAB =
//            CREATIVE_MODE_TABS.register("text_tab", () -> CreativeModeTab.builder()
//                    .icon(() -> new ItemStack(ModItems.RUBY.get()))
//                    .title(Component.translatable("itemGroup.text_tab"))
//                    .displayItems((pParameters, pOutput) -> {
//                        pOutput.accept(ModItems.RUBY.get());
//
//                    }).withTabsBefore(TEXT_TAB.getKey()).build());

    public static void register(IEventBus eventBus){
        CREATIVE_MODE_TABS.register(eventBus);
    }
}
