package com.fyoyi.betterfood.client.renderer;

import com.fyoyi.betterfood.entity.FireworkArrowEntity;
import net.minecraft.client.renderer.entity.ArrowRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;

public class FireworkArrowRenderer extends ArrowRenderer<FireworkArrowEntity> {

    public static final ResourceLocation TEXTURE = new ResourceLocation("minecraft:textures/entity/projectiles/arrow.png");

    public FireworkArrowRenderer(EntityRendererProvider.Context pContext) {
        super(pContext);
    }

    @Override
    public ResourceLocation getTextureLocation(FireworkArrowEntity pEntity) {
        return TEXTURE;
    }
}