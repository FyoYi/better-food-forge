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
    // 列表必须初始化，否则崩溃
    private final NonNullList<ItemStack> items = NonNullList.withSize(4, ItemStack.EMPTY);

    public PotBlockEntity(BlockPos pPos, BlockState pBlockState) {
        super(ModBlockEntities.POT_BE.get(), pPos, pBlockState);
    }

    public NonNullList<ItemStack> getItems() {
        return items;
    }

    public boolean pushItem(ItemStack stack) {
        for (int i = 0; i < items.size(); i++) {
            if (items.get(i).isEmpty()) {
                ItemStack toAdd = stack.copy();
                toAdd.setCount(1);
                items.set(i, toAdd);
                markUpdated();
                return true;
            }
        }
        return false;
    }

    public ItemStack popItem() {
        for (int i = items.size() - 1; i >= 0; i--) {
            if (!items.get(i).isEmpty()) {
                ItemStack stack = items.get(i).copy();
                items.set(i, ItemStack.EMPTY);
                markUpdated();
                return stack;
            }
        }
        return ItemStack.EMPTY;
    }

    private void markUpdated() {
        setChanged();
        if (level != null) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        }
    }

    // === 【关键】NBT 保存与读取 ===
    // 统一使用 ContainerHelper，它会自动处理 "Items" 这个 key，不要自己加 "inventory" 前缀了

    @Override
    protected void saveAdditional(CompoundTag pTag) {
        super.saveAdditional(pTag);
        ContainerHelper.saveAllItems(pTag, items);
    }

    @Override
    public void load(CompoundTag pTag) {
        super.load(pTag);
        items.clear();
        ContainerHelper.loadAllItems(pTag, items);
    }

    // 把当前数据存进一个物品里
    public void saveToItem(ItemStack stack) {
        CompoundTag nbt = new CompoundTag();
        ContainerHelper.saveAllItems(nbt, items); // 存入 nbt

        // 只有里面真有东西才存 tag，保持物品整洁
        boolean hasItem = false;
        for(ItemStack s : items) if(!s.isEmpty()) hasItem = true;

        if (hasItem) {
            stack.addTagElement("BlockEntityTag", nbt);
        }
    }

    // 网络同步
    @Override
    public CompoundTag getUpdateTag() { return saveWithoutMetadata(); }
    @Nullable
    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() { return ClientboundBlockEntityDataPacket.create(this); }
    @Override
    public void onDataPacket(net.minecraft.network.Connection net, ClientboundBlockEntityDataPacket pkt) { this.load(pkt.getTag()); }
}