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
import net.minecraft.util.Mth; // 使用 MathHelper 进行插值
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

public class CookingPanBlockRenderer implements BlockEntityRenderer<PotBlockEntity> {
    public CookingPanBlockRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public void render(PotBlockEntity pBlockEntity, float pPartialTick, PoseStack pPoseStack, MultiBufferSource pBufferSource, int pPackedLight, int pPackedOverlay) {
        NonNullList<ItemStack> items = pBlockEntity.getItems();
        ItemRenderer itemRenderer = Minecraft.getInstance().getItemRenderer();

        // 1. 获取动画进度 (1.0 -> 0.0)
        float flipProgress = pBlockEntity.getFlipProgress(pPartialTick);

        // 我们把进度反转一下：time = 0.0(开始) -> 1.0(结束)
        // 这样方便做插值计算 (Lerp)
        float time = 1.0f - flipProgress;

        // 抛物线跳跃 (Sin波: 0 -> 1 -> 0)
        float jumpHeight = (float) Math.sin(time * Math.PI);

        // 基础种子 (方块位置)
        long posSeed = pBlockEntity.getBlockPos().asLong();
        // 当前翻炒次数
        int currentFlipCount = pBlockEntity.getFlipCount();

        for (int i = 0; i < items.size(); i++) {
            ItemStack stack = items.get(i);
            if (stack.isEmpty()) continue;

            pPoseStack.pushPose();

            // === 【核心修改】平滑随机过渡 ===

            // 1. 计算【起点】随机参数 (Seed = Count)
            RandomSource r1 = RandomSource.create(posSeed + (long) currentFlipCount * 99999L + (long) i * 3129871L);
            float startX = (r1.nextFloat() * 2.0f - 1.0f) * 0.08f;
            float startZ = (r1.nextFloat() * 2.0f - 1.0f) * 0.08f;
            float startRot = r1.nextFloat() * 360.0f;

            // 2. 计算【终点】随机参数 (Seed = Count + 1)
            // 这就是食物落地后将会变成的样子
            RandomSource r2 = RandomSource.create(posSeed + (long) (currentFlipCount + 1) * 99999L + (long) i * 3129871L);
            float endX = (r2.nextFloat() * 2.0f - 1.0f) * 0.08f;
            float endZ = (r2.nextFloat() * 2.0f - 1.0f) * 0.08f;
            float endRot = r2.nextFloat() * 360.0f;

            // 处理旋转角度的插值问题 (防止从350度转到10度时绕一大圈)
            // 如果差值太大，就手动补一圈
            if (endRot - startRot > 180f) endRot -= 360f;
            if (endRot - startRot < -180f) endRot += 360f;


            // 3. 【核心算法】根据时间插值 (Lerp)
            // 如果没在翻炒 (time=1 或 time=0)，这个公式会自动停留在起点或终点
            float currentRandX, currentRandZ, currentRandRot;

            if (flipProgress > 0) {
                // 正在翻炒：从起点平滑移动到终点
                currentRandX = Mth.lerp(time, startX, endX);
                currentRandZ = Mth.lerp(time, startZ, endZ);
                currentRandRot = Mth.lerp(time, startRot, endRot);
            } else {
                // 静止状态：直接使用当前状态 (此时 flipCount 已经在 tick 里 +1 了，所以其实是上面的 startX 变成了最新的)
                currentRandX = startX;
                currentRandZ = startZ;
                currentRandRot = startRot;
            }


            // 4. 计算最终位置
            float x = 0.5f + currentRandX;
            float z = 0.5f + currentRandZ;
            float y = 0.07f + (i * 0.04f); // 基础堆叠高度

            if (flipProgress > 0) {
                // A. 垂直跳跃 + 高度分散
                float spreadY = 0.3f + (i * 0.15f);
                y += jumpHeight * spreadY;

                // B. 水平扩散 (在空中时，把随机偏移量放大，制造“散开”的视觉)
                // 乘一个系数：空中时散得更开，落地时收回来
                // 使用 time 插值可以让它平滑地“散开 -> 移动 -> 聚合”
                float spreadAmount = 1.0f + (jumpHeight * 1.5f); // 空中放大1.5倍间距

                // 重新计算带扩散的 X/Z
                // 注意：这里是在 lerp 后的基准上进行扩散
                float spreadX = currentRandX * spreadAmount;
                float spreadZ = currentRandZ * spreadAmount;

                // 更新 x 和 z
                x = 0.5f + spreadX;
                z = 0.5f + spreadZ;

                // 额外的 Z 轴抛物线轨迹 (让它看起来是抛出来的)
                z += Math.sin(time * Math.PI * 2) * 0.05f;

                pPoseStack.translate(x, y, z);

                // C. 空中翻转 (360度)
                float flipRot = time * 360.0f;
                pPoseStack.mulPose(Axis.XP.rotationDegrees(flipRot));

            } else {
                // 静止
                pPoseStack.translate(x, y, z);
            }

            // 5. 缩放与自转
            pPoseStack.scale(0.5f, 0.5f, 0.5f);
            pPoseStack.mulPose(Axis.XP.rotationDegrees(270f));

            // 应用平滑过渡后的随机自转
            pPoseStack.mulPose(Axis.ZP.rotationDegrees(currentRandRot));

            itemRenderer.renderStatic(stack, ItemDisplayContext.FIXED, pPackedLight, OverlayTexture.NO_OVERLAY, pPoseStack, pBufferSource, pBlockEntity.getLevel(), 0);

            pPoseStack.popPose();
        }
    }
}