package com.fyoyi.betterfood.client;

import com.fyoyi.betterfood.better_food;
import com.fyoyi.betterfood.client.renderer.FireworkArrowRenderer;
import com.fyoyi.betterfood.entity.ModEntities;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = better_food.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ClientModEvents {
    @SubscribeEvent
    public static void registerRenderers(final EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(ModEntities.FIREWORK_ARROW.get(), FireworkArrowRenderer::new);
    }
}