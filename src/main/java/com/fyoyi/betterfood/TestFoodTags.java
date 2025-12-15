package com.fyoyi.betterfood;

import com.fyoyi.betterfood.config.FoodConfig;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.Set;

@Mod.EventBusSubscriber(modid = "better_food")
public class TestFoodTags {
    
    private static boolean hasPrinted = false;
    
    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (!hasPrinted && event.player.level().getGameTime() % 100 == 0) {
            ItemStack appleStack = new ItemStack(Items.APPLE, 3);
            Set<String> tags = FoodConfig.getFoodTags(appleStack);
            if (tags != null) {
                System.out.println("[TestFoodTags] Apple tags: " + tags);
            } else {
                System.out.println("[TestFoodTags] Apple tags is null");
            }
            hasPrinted = true;
        }
    }
}