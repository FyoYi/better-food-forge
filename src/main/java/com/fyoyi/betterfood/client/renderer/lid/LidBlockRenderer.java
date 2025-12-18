package com.fyoyi.betterfood.client.renderer.lid;

import com.fyoyi.betterfood.block.entity.PotBlockEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

public class LidBlockRenderer implements BlockEntityRenderer<PotBlockEntity> {
    public LidBlockRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public void render(PotBlockEntity pBlockEntity, float pPartialTick, PoseStack pPoseStack, MultiBufferSource pBufferSource, int pPackedLight, int pPackedOverlay) {
        // 锅盖方块不渲染任何内容，因为锅盖方块本身不包含物品
    }
}