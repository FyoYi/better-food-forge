package com.fyoyi.betterfood.item.large_pot;

import com.fyoyi.betterfood.client.renderer.large_pot.LargePotItemRenderer;
import net.minecraft.ChatFormatting;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;

import java.util.List;
import java.util.function.Consumer;

public class LargePotBlockItem extends BlockItem {
    public LargePotBlockItem(Block block, Properties properties) {
        super(block, properties);
    }



    @Override
    public void appendHoverText(ItemStack stack, Level level, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, level, tooltip, flag);

        // 显示盖子状态
        boolean hasLid = false;
        if (stack.hasTag()) {
            CompoundTag tag = stack.getTagElement("BlockEntityTag");
            if (tag != null && tag.contains("has_lid")) {
                hasLid = tag.getBoolean("has_lid");
            }
        }
        tooltip.add(Component.literal("盖子: " + (hasLid ? "已盖上" : "未盖上")).withStyle(ChatFormatting.GOLD));

        // 显示水状态
        boolean hasWater = false;
        if (stack.hasTag()) {
            CompoundTag tag = stack.getTagElement("BlockEntityTag");
            if (tag != null && tag.contains("has_water")) {
                hasWater = tag.getBoolean("has_water");
            }
        }
        tooltip.add(Component.literal("水: " + (hasWater ? "已加入" : "未加入")).withStyle(ChatFormatting.BLUE));

        // 显示食物信息
        CompoundTag tag = stack.getTagElement("BlockEntityTag");
        if (tag != null) {
            NonNullList<ItemStack> items = NonNullList.withSize(4, ItemStack.EMPTY);
            ContainerHelper.loadAllItems(tag, items);

            int count = 0;
            for (ItemStack item : items) {
                if (!item.isEmpty()) {
                    count++;
                    tooltip.add(Component.literal("- ").append(item.getHoverName()).withStyle(ChatFormatting.GRAY));
                }
            }

            if (count > 0) {
                tooltip.add(Component.literal("包含 " + count + " 个食材").withStyle(ChatFormatting.GREEN));
            } else {
                tooltip.add(Component.literal("空").withStyle(ChatFormatting.GRAY));
            }
        } else {
            tooltip.add(Component.literal("空").withStyle(ChatFormatting.GRAY));
        }
    }

    @Override
    public void initializeClient(Consumer<IClientItemExtensions> consumer) {
        consumer.accept(new IClientItemExtensions() {
            @Override
            public BlockEntityWithoutLevelRenderer getCustomRenderer() {
                return new LargePotItemRenderer();
            }
        });
    }
}