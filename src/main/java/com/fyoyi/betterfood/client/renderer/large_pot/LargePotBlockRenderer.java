package com.fyoyi.betterfood.client.renderer.large_pot;

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

public class LargePotBlockRenderer implements BlockEntityRenderer<PotBlockEntity> {

    public LargePotBlockRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public void render(PotBlockEntity pBlockEntity, float pPartialTick, PoseStack pPoseStack, MultiBufferSource pBufferSource, int pPackedLight, int pPackedOverlay) {
        // 1. 获取所有物品列表
        NonNullList<ItemStack> items = pBlockEntity.getItems();
        ItemRenderer itemRenderer = Minecraft.getInstance().getItemRenderer();

        // 2. 检查是否有水
        boolean hasWater = false;
        if (pBlockEntity.getLevel() != null && pBlockEntity.getBlockState().getBlock() instanceof com.fyoyi.betterfood.block.large_pot.LargePotBlock) {
            hasWater = pBlockEntity.getBlockState().getValue(com.fyoyi.betterfood.block.large_pot.LargePotBlock.HAS_WATER);
        }

        // 3. 【关键步骤】统计当前非空物品的总数量
        // 这决定了我们需要画点、线、三角形还是正方形
        int activeCount = 0;
        for (ItemStack s : items) {
            if (!s.isEmpty()) activeCount++;
        }

        // 用于追踪当前渲染的是第几个物品 (用来计算角度分配)
        int currentRenderIndex = 0;

        // 获取平滑的游戏时间，用于动画计算
        long time = pBlockEntity.getLevel().getGameTime();
        float animTime = time + pPartialTick;

        // 4. 遍历物品栏 (0-3)
        for (int i = 0; i < items.size(); i++) {
            ItemStack stack = items.get(i);

            // 如果槽位为空，直接跳过
            if (stack.isEmpty()) continue;

            pPoseStack.pushPose();

            // ==========================================
            // 分支逻辑：无水 vs 有水
            // ==========================================

            if (!hasWater) {
                // --- 【无水状态：堆叠在底部，平躺】 ---

                // 基础位置 (居中)
                float x = 0.5f;
                float z = 0.5f;
                // 高度随索引增加，产生堆叠感
                float y = 0.07f + (i * 0.04f);

                // Z轴旋转数组 (让堆叠看起来稍微乱一点，不那么死板)
                float[] foodRotationZ = {0f, 45f, 90f, 135f};

                pPoseStack.translate(x, y, z);

                // 缩放
                pPoseStack.scale(0.5f, 0.5f, 0.5f);

                // 旋转
                pPoseStack.mulPose(Axis.XP.rotationDegrees(90f)); // 绕X轴90度 = 平躺
                pPoseStack.mulPose(Axis.ZP.rotationDegrees(foodRotationZ[i])); // Z轴随机角度

            } else {
                // --- 【有水状态：直立，绕中心旋转，浮动】 ---

                // A. 计算轨道半径
                // 如果只有1个物品，半径为0 (在正中心)
                // 如果有多个物品，半径为0.25 (散开)
                float radius = (activeCount <= 1) ? 0.0f : 0.2f;

                // B. 计算公转角度
                // 基础速度: animTime * 1.5
                // 分布偏移: (360 / 总数) * 当前第几个
                float orbitAngle = (animTime * 0.5f) + ((360.0f / activeCount) * currentRenderIndex);

                // C. 计算位置 (极坐标 -> 笛卡尔坐标)
                float x = 0.5f + radius * (float)Math.cos(Math.toRadians(orbitAngle));
                float z = 0.5f + radius * (float)Math.sin(Math.toRadians(orbitAngle));

                // D. 计算高度 (带呼吸浮动效果)
                // 0.6f 是基础水面高度，Sin函数模拟波浪
                float y = 0.6f + (float)Math.sin(animTime * 0.1f + i) * 0.07f;

                pPoseStack.translate(x, y, z);

                // 缩放
                pPoseStack.scale(0.4f, 0.4f, 0.4f);

                // E. 旋转逻辑
                // 注意：这里去掉了 Axis.XP 的旋转，所以物品是“直立”的

                // 自转逻辑：
                // -orbitAngle: 抵消公转带来的朝向变化（保持面朝一个绝对方向）
                // +animTime * 2.0f: 加上自转速度
                // 你可以调整 2.0f 这个数字来改变自转快慢
                pPoseStack.mulPose(Axis.YP.rotationDegrees(-orbitAngle + (animTime * 1.0f)));
            }

            // ==========================================

            // 5. 执行渲染
            itemRenderer.renderStatic(stack, ItemDisplayContext.FIXED, pPackedLight, OverlayTexture.NO_OVERLAY, pPoseStack, pBufferSource, pBlockEntity.getLevel(), 0);

            pPoseStack.popPose();

            // 增加计数器 (仅针对有效物品)
            currentRenderIndex++;
        }
    }
}