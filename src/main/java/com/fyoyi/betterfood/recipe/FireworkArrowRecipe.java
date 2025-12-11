package com.fyoyi.betterfood.recipe;

import net.minecraft.core.RegistryAccess;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;
import com.fyoyi.betterfood.item.sundries.Sundries_item; // 导入 Sundries_item


public class FireworkArrowRecipe extends CustomRecipe {

    public FireworkArrowRecipe(ResourceLocation id, CraftingBookCategory category) {
        super(id, category);
    }

    @Override
    public boolean matches(CraftingContainer container, Level level) {
        ItemStack fireworkStack = ItemStack.EMPTY;
        ItemStack arrowStack = ItemStack.EMPTY;
        int itemCount = 0;
        for (int i = 0; i < container.getContainerSize(); ++i) {
            ItemStack stackInSlot = container.getItem(i);
            if (!stackInSlot.isEmpty()) {
                itemCount++;
                if (stackInSlot.getItem() == Items.FIREWORK_ROCKET) {
                    if (!fireworkStack.isEmpty()) return false;
                    fireworkStack = stackInSlot;
                } else if (stackInSlot.getItem() == Items.ARROW) {
                    if (!arrowStack.isEmpty()) return false;
                    arrowStack = stackInSlot;
                } else {
                    return false;
                }
            }
        }
        return !fireworkStack.isEmpty() && !arrowStack.isEmpty() && itemCount == 2;
    }

    @Override
    public ItemStack assemble(CraftingContainer container, RegistryAccess registryAccess) {
        ItemStack fireworkStack = ItemStack.EMPTY;
        for (int i = 0; i < container.getContainerSize(); ++i) {
            ItemStack stackInSlot = container.getItem(i);
            if (stackInSlot.getItem() == Items.FIREWORK_ROCKET) {
                fireworkStack = stackInSlot;
                break;
            }
        }
        if (fireworkStack.isEmpty()) {
            return ItemStack.EMPTY;
        }
        ItemStack outputArrow = new ItemStack(Sundries_item.FIREWORK_ARROW.get(), 1);
        CompoundTag fireworkNbt = fireworkStack.getTag();
        if (fireworkNbt != null && fireworkNbt.contains("Fireworks", 10)) {
            CompoundTag arrowNbt = new CompoundTag();
            arrowNbt.put("Fireworks", fireworkNbt.getCompound("Fireworks").copy());
            outputArrow.setTag(arrowNbt);
        }
        return outputArrow;
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return width * height >= 2;
    }

    /**
     * 返回此配方的序列化器。
     * 现在它正确地指向了 ModRecipes 类中定义的 FIREWORK_ARROW 变量。
     */
    @Override
    public RecipeSerializer<?> getSerializer() {
        return ModRecipes.FIREWORK_ARROW.get();
    }
}