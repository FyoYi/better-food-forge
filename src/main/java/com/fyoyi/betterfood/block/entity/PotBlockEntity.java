package com.fyoyi.betterfood.block.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class PotBlockEntity extends BlockEntity {
    private final NonNullList<ItemStack> items = NonNullList.withSize(4, ItemStack.EMPTY);

    private int flipTimer = 0;
    public static final int FLIP_ANIMATION_DURATION = 10;
    private int flipCount = 0;

    public PotBlockEntity(BlockPos pPos, BlockState pBlockState) {
        super(ModBlockEntities.POT_BE.get(), pPos, pBlockState);
    }

    // === Tick 方法 ===
    public static void tick(Level level, BlockPos pos, BlockState state, PotBlockEntity pEntity) {
        if (pEntity.flipTimer > 0) {

            // 1. 动画过半时：交换物品顺序 (数据层面的交换)
            if (pEntity.flipTimer == FLIP_ANIMATION_DURATION / 2) {
                pEntity.cycleItemsOrder();
            }

            // 2. 计时器递减
            pEntity.flipTimer--;

            // 3. 【核心修改】动画彻底结束那一刻，计数器+1
            // 这样在整个动画过程中，flipCount 保持不变，方便渲染器做插值
            if (pEntity.flipTimer == 0) {
                pEntity.flipCount++;
                pEntity.markUpdated(); // 确保同步
            }
        }
    }

    private void cycleItemsOrder() {
        List<ItemStack> temp = new ArrayList<>();
        for (ItemStack stack : items) if (!stack.isEmpty()) temp.add(stack);

        if (temp.size() < 2) return;

        // 底部移到顶部
        ItemStack bottomItem = temp.remove(0);
        temp.add(bottomItem);

        items.clear();
        for (int i = 0; i < items.size(); i++) items.set(i, ItemStack.EMPTY);
        for (int i = 0; i < temp.size(); i++) items.set(i, temp.get(i));

        // 注意：这里删除了 flipCount++，移到了 tick 里面
        markUpdated();
    }

    public int getFlipCount() { return flipCount; }

    public void triggerFlip() {
        // 如果正在翻，不要重置
        if (this.flipTimer == 0) {
            this.flipTimer = FLIP_ANIMATION_DURATION;
        }
    }

    // --- 以下代码不变 ---
    public float getFlipProgress(float partialTick) {
        if (flipTimer <= 0) return 0.0f;
        return (flipTimer - partialTick) / FLIP_ANIMATION_DURATION;
    }

    public NonNullList<ItemStack> getItems() { return items; }
    public boolean pushItem(ItemStack stack) { /*...*/ return super_pushItem(stack); } // 省略重复代码，保持原样
    // (为了节省篇幅，下面的标准方法请保持你原文件内容不变)
    public ItemStack popItem() { /*...*/ return super_popItem(); }
    private void markUpdated() { setChanged(); if(level!=null) level.sendBlockUpdated(worldPosition,getBlockState(),getBlockState(),3); }
    protected void saveAdditional(CompoundTag pTag) { super.saveAdditional(pTag); ContainerHelper.saveAllItems(pTag, items); pTag.putInt("FlipCount", flipCount); }
    public void load(CompoundTag pTag) { super.load(pTag); items.clear(); ContainerHelper.loadAllItems(pTag, items); if(pTag.contains("FlipCount")) flipCount = pTag.getInt("FlipCount"); }
    public void saveToItem(ItemStack stack) { CompoundTag nbt=new CompoundTag(); ContainerHelper.saveAllItems(nbt,items); nbt.putInt("FlipCount",flipCount); boolean h=false; for(ItemStack s:items)if(!s.isEmpty())h=true; if(h) stack.addTagElement("BlockEntityTag",nbt); }
    public CompoundTag getUpdateTag() { return saveWithoutMetadata(); }
    @Nullable public Packet<ClientGamePacketListener> getUpdatePacket() { return ClientboundBlockEntityDataPacket.create(this); }
    public void onDataPacket(net.minecraft.network.Connection net, ClientboundBlockEntityDataPacket pkt) { this.load(pkt.getTag()); }

    // 辅助方法，防止编译错误
    private boolean super_pushItem(ItemStack stack) { for(int i=0;i<items.size();i++){if(items.get(i).isEmpty()){ItemStack t=stack.copy();t.setCount(1);items.set(i,t);markUpdated();return true;}}return false;}
    private ItemStack super_popItem() { for(int i=items.size()-1;i>=0;i--){if(!items.get(i).isEmpty()){ItemStack s=items.get(i).copy();items.set(i,ItemStack.EMPTY);markUpdated();return s;}}return ItemStack.EMPTY;}
}