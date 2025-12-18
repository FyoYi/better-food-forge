package com.fyoyi.betterfood.client.renderer;

import com.fyoyi.betterfood.block.ModBlocks;
import com.fyoyi.betterfood.block.entity.PotBlockEntity;
import com.fyoyi.betterfood.client.renderer.cooking_pan.CookingPanBlockRenderer;
import com.fyoyi.betterfood.client.renderer.large_pot.LargePotBlockRenderer;
import com.fyoyi.betterfood.client.renderer.lid.LidBlockRenderer;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.world.level.block.Block;

public class PotRendererDispatcher implements BlockEntityRenderer<PotBlockEntity> {
    private final CookingPanBlockRenderer cookingPanRenderer;
    private final LargePotBlockRenderer largePotRenderer;
    private final LidBlockRenderer lidRenderer;

    public PotRendererDispatcher(BlockEntityRendererProvider.Context context) {
        this.cookingPanRenderer = new CookingPanBlockRenderer(context);
        this.largePotRenderer = new LargePotBlockRenderer(context);
        this.lidRenderer = new LidBlockRenderer(context);
    }

    @Override
    public void render(PotBlockEntity pBlockEntity, float pPartialTick, PoseStack pPoseStack, MultiBufferSource pBufferSource, int pPackedLight, int pPackedOverlay) {
        // 根据方块类型选择对应的渲染器
        Block block = pBlockEntity.getBlockState().getBlock();
        
        if (block == ModBlocks.COOKING_PAN.get()) {
            cookingPanRenderer.render(pBlockEntity, pPartialTick, pPoseStack, pBufferSource, pPackedLight, pPackedOverlay);
        } else if (block == ModBlocks.LARGE_POT.get()) {
            largePotRenderer.render(pBlockEntity, pPartialTick, pPoseStack, pBufferSource, pPackedLight, pPackedOverlay);
        } else if (block == ModBlocks.LID.get()) {
            lidRenderer.render(pBlockEntity, pPartialTick, pPoseStack, pBufferSource, pPackedLight, pPackedOverlay);
        }
    }
}