package com.fyoyi.betterfood.block.entity;

import com.fyoyi.betterfood.config.FoodConfig; // 必须导入你的配置类
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class PotBlockEntity extends BlockEntity {
    private final NonNullList<ItemStack> items = NonNullList.withSize(4, ItemStack.EMPTY);

    private int flipTimer = 0;
    public static final int FLIP_ANIMATION_DURATION = 10;
    private int flipCount = 0;

    private static final float[] LAYER_EFFICIENCY = {1.0f, 0.5f, 0.2f, 0.0f};
    private static final float BASE_HEAT_PER_TICK = 0.1f;

    public static final String NBT_COOKED_PROGRESS = "BetterFood_CookedProgress";

    public PotBlockEntity(BlockPos pPos, BlockState pBlockState) {
        super(ModBlockEntities.POT_BE.get(), pPos, pBlockState);
    }

    public static void tick(Level level, BlockPos pos, BlockState state, PotBlockEntity pEntity) {
        if (pEntity.flipTimer > 0) {
            if (pEntity.flipTimer == FLIP_ANIMATION_DURATION / 2) pEntity.cycleItemsOrder();
            pEntity.flipTimer--;
            if (pEntity.flipTimer == 0) {
                pEntity.flipCount++;
                pEntity.markUpdated();
            }
        }

        if (!level.isClientSide) {
            if (isHeated(level, pos)) {
                pEntity.applyHeat();
            }
        }
    }

    private void applyHeat() {
        boolean changed = false;
        for (int i = 0; i < items.size(); i++) {
            ItemStack stack = items.get(i);
            if (stack.isEmpty()) continue;

            float efficiency = (i < LAYER_EFFICIENCY.length) ? LAYER_EFFICIENCY[i] : 0.0f;
            if (efficiency <= 0) continue;

            CompoundTag nbt = stack.getOrCreateTag();
            float current = nbt.getFloat(NBT_COOKED_PROGRESS);

            if (current < 120.0f) {
                current += BASE_HEAT_PER_TICK * efficiency;
                nbt.putFloat(NBT_COOKED_PROGRESS, current);
                changed = true;
            }
        }

        if (changed) {
            setChanged();
            if (this.level.getGameTime() % 20 == 0) {
                this.level.sendBlockUpdated(this.worldPosition, this.getBlockState(), this.getBlockState(), 3);
            }
        }
    }

    private static boolean isHeated(Level level, BlockPos pos) {
        BlockState below = level.getBlockState(pos.below());
        if (below.is(Blocks.FIRE) || below.is(Blocks.SOUL_FIRE) || below.is(Blocks.LAVA) || below.is(Blocks.MAGMA_BLOCK)) return true;
        if (below.getBlock() instanceof CampfireBlock && below.getValue(CampfireBlock.LIT)) return true;
        return false;
    }

    private void cycleItemsOrder() {
        List<ItemStack> temp = new ArrayList<>();
        for (ItemStack stack : items) if (!stack.isEmpty()) temp.add(stack);
        if (temp.size() < 2) return;
        ItemStack bottomItem = temp.remove(0);
        temp.add(bottomItem);
        items.clear();
        for (int i = 0; i < items.size(); i++) items.set(i, ItemStack.EMPTY);
        for (int i = 0; i < temp.size(); i++) items.set(i, temp.get(i));
        markUpdated();
    }

    public void markUpdated() {
        setChanged();
        if (level != null) level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
    }

    // === 【核心修改：数据互通】放入物品时初始化 NBT ===
    public boolean pushItem(ItemStack stack) {
        for (int i = 0; i < items.size(); i++) {
            if (items.get(i).isEmpty()) {
                ItemStack toAdd = stack.copy();
                toAdd.setCount(1);

                CompoundTag nbt = toAdd.getOrCreateTag();

                // 只有当物品没有动态 NBT 时，才去读取静态配置
                if (!nbt.contains(NBT_COOKED_PROGRESS)) {
                    float initialValue = 0.0f;

                    // 读取你在 FoodConfig 里定义的标签
                    Set<String> tags = FoodConfig.getFoodTags(toAdd);
                    for (String tag : tags) {
                        if (tag.startsWith("熟度:")) {
                            try {
                                // 解析 "熟度:70%" -> 70.0
                                String val = tag.substring(3).replace("%", "").trim();
                                initialValue = Float.parseFloat(val);
                            } catch (NumberFormatException e) {
                                initialValue = 0.0f;
                            }
                            break;
                        }
                    }
                    // 写入 NBT，这样后续的加热逻辑就能接上这个数值继续跑
                    nbt.putFloat(NBT_COOKED_PROGRESS, initialValue);
                }

                items.set(i, toAdd);
                markUpdated();
                return true;
            }
        }
        return false;
    }

    // ... 其他标准方法保持不变 ...
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

    @Override
    protected void saveAdditional(CompoundTag pTag) {
        super.saveAdditional(pTag);
        ContainerHelper.saveAllItems(pTag, items);
        pTag.putInt("FlipCount", flipCount);
    }

    @Override
    public void load(CompoundTag pTag) {
        super.load(pTag); items.clear();
        ContainerHelper.loadAllItems(pTag, items);
        if (pTag.contains("FlipCount")) flipCount = pTag.getInt("FlipCount");
    }

    public void saveToItem(ItemStack stack) {
        CompoundTag nbt = new CompoundTag();
        ContainerHelper.saveAllItems(nbt, items);
        nbt.putInt("FlipCount", flipCount);
        boolean hasItem = false;
        for(ItemStack s : items)
            if(!s.isEmpty()) hasItem = true;
        if (hasItem) stack.addTagElement("BlockEntityTag", nbt);
    }

    public int getFlipCount() {
        return flipCount;
    }

    public void triggerFlip() {
        if (this.flipTimer == 0) this.flipTimer = FLIP_ANIMATION_DURATION;
    }

    public float getFlipProgress(float partialTick) {
        if (flipTimer <= 0) return 0.0f;
        return (flipTimer - partialTick) / FLIP_ANIMATION_DURATION;
    }

    public NonNullList<ItemStack> getItems() {
        return items;
    }

    public ItemStack popItem() {
        for(int i=items.size()-1;i>=0;i--)
            if(!items.get(i).isEmpty()) {
                ItemStack s=items.get(i).copy();
                items.set(i,ItemStack.EMPTY);
                markUpdated();
                return s;
            }
        return ItemStack.EMPTY;
    }
}