package com.fyoyi.betterfood.client.renderer.cooking_pan;

import com.fyoyi.betterfood.block.entity.PotBlockEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

public class CookingPanBlockRenderer implements BlockEntityRenderer<PotBlockEntity> {
    public CookingPanBlockRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public void render(PotBlockEntity pBlockEntity, float pPartialTick, PoseStack pPoseStack, MultiBufferSource pBufferSource, int pPackedLight, int pPackedOverlay) {
        // 获取所有 4 个物品
        NonNullList<ItemStack> items = pBlockEntity.getItems();

        ItemRenderer itemRenderer = Minecraft.getInstance().getItemRenderer();

        // 遍历 0 到 3
        for (int i = 0; i < items.size(); i++) {
            ItemStack stack = items.get(i);

            // 如果这个格子没东西，跳过
            if (stack.isEmpty()) continue;

            pPoseStack.pushPose();

            // 基础位置：居中
            float x = 0.5f;
            float z = 0.5f;

            // 高度：根据 i 自动累加，这样它们会叠在一起
            // 0.07f 是起始高度 (锅底)
            // i * 0.04f 表示每个物品往上摞 0.04 的高度
            float y = 0.07f + (i * 0.04f);

            pPoseStack.translate(x, y, z);

            // 缩放
            pPoseStack.scale(0.5f, 0.5f, 0.5f);

            // 旋转：平躺
            pPoseStack.mulPose(Axis.XP.rotationDegrees(270f));

            // 可选：为了让它们看起来不那么死板，可以让每一层旋转一个不同的角度
            // 比如：第一层转0度，第二层转25度，第三层转50度...
            pPoseStack.mulPose(Axis.ZP.rotationDegrees(i * 45.0f));

            // 渲染当前这个物品
            itemRenderer.renderStatic(stack, ItemDisplayContext.FIXED, pPackedLight, OverlayTexture.NO_OVERLAY, pPoseStack, pBufferSource, pBlockEntity.getLevel(), 0);

            pPoseStack.popPose();
        }
    }
}