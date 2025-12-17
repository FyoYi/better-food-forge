package com.fyoyi.betterfood.block.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public class PotBlockEntity extends BlockEntity {
    // 【修改点】容量为 4 的列表，不再是单个物品
    private final NonNullList<ItemStack> items = NonNullList.withSize(4, ItemStack.EMPTY);

    public PotBlockEntity(BlockPos pPos, BlockState pBlockState) {
        super(ModBlockEntities.POT_BE.get(), pPos, pBlockState);
    }

    // 获取所有物品的列表 (给渲染器用)
    public NonNullList<ItemStack> getItems() {
        return items;
    }

    // === 逻辑：尝试放入物品 (Push) ===
    // 返回 true 表示放入成功
    public boolean pushItem(ItemStack stack) {
        for (int i = 0; i < items.size(); i++) {
            if (items.get(i).isEmpty()) {
                // 找到第一个空位，放进去
                // copy() 很重要，防止引用问题
                ItemStack toAdd = stack.copy();
                toAdd.setCount(1);
                items.set(i, toAdd);
                markUpdated();
                return true;
            }
        }
        return false; // 满了
    }

    // === 逻辑：尝试取出最上面的物品 (Pop) ===
    // 返回取出的物品，如果空则返回 ItemStack.EMPTY
    public ItemStack popItem() {
        // 从后往前遍历 (堆栈逻辑：后进先出)
        for (int i = items.size() - 1; i >= 0; i--) {
            if (!items.get(i).isEmpty()) {
                ItemStack stack = items.get(i).copy();
                items.set(i, ItemStack.EMPTY); // 清空该格子
                markUpdated();
                return stack;
            }
        }
        return ItemStack.EMPTY; // 空的
    }

    // 统一的数据更新方法
    private void markUpdated() {
        setChanged();
        if (level != null) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        }
    }

    // === NBT 保存 (使用 ContainerHelper 简化列表保存) ===
    @Override
    protected void saveAdditional(CompoundTag pTag) {
        super.saveAdditional(pTag);
        ContainerHelper.saveAllItems(pTag, items);
    }

    // === NBT 读取 ===
    @Override
    public void load(CompoundTag pTag) {
        super.load(pTag);
        items.clear(); // 先清空，防止叠加
        ContainerHelper.loadAllItems(pTag, items);
    }

    // === 网络同步 ===
    @Override
    public CompoundTag getUpdateTag() {
        return saveWithoutMetadata();
    }

    @Nullable
    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public void onDataPacket(net.minecraft.network.Connection net, ClientboundBlockEntityDataPacket pkt) {
        this.load(pkt.getTag());
    }
}