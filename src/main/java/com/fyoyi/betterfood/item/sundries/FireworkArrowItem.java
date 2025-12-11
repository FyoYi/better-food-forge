package com.fyoyi.betterfood.item.sundries;

import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

public class FireworkArrowItem extends Item {

    public FireworkArrowItem(Properties pProperties) {
        super(pProperties);
    }

    /**
     * 这个方法负责添加物品的描述文本（鼠标悬停时显示）。
     * 这是实现“继承描述”的关键。
     */
    @Override
    public void appendHoverText(ItemStack pStack, @Nullable Level pLevel, List<Component> pTooltipComponents, TooltipFlag pIsAdvanced) {

        Items.FIREWORK_ROCKET.appendHoverText(pStack, pLevel, pTooltipComponents, pIsAdvanced);
    }
}
