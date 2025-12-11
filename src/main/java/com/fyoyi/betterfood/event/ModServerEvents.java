package com.fyoyi.betterfood.event;

import com.fyoyi.betterfood.config.FoodConfig;
import com.fyoyi.betterfood.util.FreshnessHelper;
import com.fyoyi.betterfood.util.TimeManager;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Container;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickAction;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraftforge.event.ItemStackedOnOtherEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerContainerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@Mod.EventBusSubscriber(modid = "better_food", bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ModServerEvents {

    // 1. 玩家背包检查 (保持不变)
    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase == TickEvent.Phase.END && !event.player.level().isClientSide) {
            if (event.player.level().getGameTime() % 20 == 0) {
                checkInventoryForRot(event.player.level(), event.player.getInventory());
                AbstractContainerMenu menu = event.player.containerMenu;
                if (menu != null) {
                    for (Slot slot : menu.slots) {
                        if (slot.hasItem()) checkAndReplace(event.player.level(), slot);
                    }
                }
            }
        }
    }

    // 2. 全局扫描 (保持不变)
    @SubscribeEvent
    public static void onLevelTick(TickEvent.LevelTickEvent event) {
        if (event.phase == TickEvent.Phase.END && !event.level.isClientSide && event.level instanceof ServerLevel serverLevel) {
            if (event.level.getGameTime() % 40 == 0) {
                for (Entity entity : serverLevel.getAllEntities()) {
                    if (entity instanceof ItemEntity itemEntity) {
                        ItemStack stack = itemEntity.getItem();
                        if (FoodConfig.canRot(stack)) {
                            if (TimeManager.DECAY_ENABLED && FreshnessHelper.isRotten(serverLevel, stack)) {
                                itemEntity.setItem(new ItemStack(Items.ROTTEN_FLESH, stack.getCount()));
                            } else {
                                FreshnessHelper.getExpiryTime(serverLevel, stack, true);
                            }
                        }
                    }
                }
                Set<ChunkPos> processedChunks = new HashSet<>();
                for (Player player : serverLevel.players()) {
                    ChunkPos pPos = player.chunkPosition();
                    for (int x = -2; x <= 2; x++) {
                        for (int z = -2; z <= 2; z++) {
                            ChunkPos cPos = new ChunkPos(pPos.x + x, pPos.z + z);
                            if (processedChunks.add(cPos) && serverLevel.hasChunk(cPos.x, cPos.z)) {
                                processChunkBlockEntities(serverLevel, serverLevel.getChunk(cPos.x, cPos.z));
                            }
                        }
                    }
                }
            }
        }
    }

    private static void processChunkBlockEntities(ServerLevel level, LevelChunk chunk) {
        Map<BlockPos, BlockEntity> blockEntities = chunk.getBlockEntities();
        for (BlockEntity be : blockEntities.values()) {
            if (be instanceof Container container) {
                checkInventoryForRot(level, container);
            }
        }
    }

    // 3. 打开箱子检查 (保持不变)
    @SubscribeEvent
    public static void onContainerOpen(PlayerContainerEvent.Open event) {
        if (!event.getEntity().level().isClientSide) {
            AbstractContainerMenu menu = event.getContainer();
            for (Slot slot : menu.slots) {
                if (slot.hasItem()) checkAndReplace(event.getEntity().level(), slot);
            }
        }
    }

    private static void checkAndReplace(net.minecraft.world.level.Level level, Slot slot) {
        ItemStack stack = slot.getItem();
        if (FoodConfig.canRot(stack)) {
            if (TimeManager.DECAY_ENABLED && FreshnessHelper.isRotten(level, stack)) {
                slot.set(new ItemStack(Items.ROTTEN_FLESH, stack.getCount()));
            } else {
                FreshnessHelper.getExpiryTime(level, stack, true);
            }
        }
    }

    private static void checkInventoryForRot(net.minecraft.world.level.Level level, Container container) {
        for (int i = 0; i < container.getContainerSize(); i++) {
            ItemStack stack = container.getItem(i);
            if (FoodConfig.canRot(stack)) {
                if (TimeManager.DECAY_ENABLED && FreshnessHelper.isRotten(level, stack)) {
                    container.setItem(i, new ItemStack(Items.ROTTEN_FLESH, stack.getCount()));
                    container.setChanged();
                } else {
                    FreshnessHelper.getExpiryTime(level, stack, true);
                }
            }
        }
    }

    // ==========================================================
    // 4. 堆叠逻辑 (最终修复版：右键差异合并)
    // ==========================================================
    @SubscribeEvent
    public static void onItemStackedOnOther(ItemStackedOnOtherEvent event) {
        if (!TimeManager.DECAY_ENABLED) return;

        ItemStack cursorStack = event.getCarriedItem();
        ItemStack slotStack = event.getStackedOnItem();
        ClickAction action = event.getClickAction();
        net.minecraft.world.level.Level level = event.getPlayer().level();

        // 1. 基本检查
        if (FoodConfig.canRot(cursorStack) &&
                FoodConfig.canRot(slotStack) &&
                ItemStack.isSameItem(cursorStack, slotStack)) {

            // 2. 如果新鲜度（NBT）完全一样
            if (ItemStack.isSameItemSameTags(cursorStack, slotStack)) {
                // 直接返回！
                // 左键 -> 原版会堆叠
                // 右键 -> 原版会放 1 个
                // 这符合你说的"一样就放一个"的要求
                return;
            }

            // --- 以下处理：新鲜度不同 (NBT不同) 的情况 ---

            // 3. 左键 (PRIMARY) -> 交换
            if (action == ClickAction.PRIMARY) {
                // 直接返回！
                // 原版检测到 NBT 不同，会执行"交换"操作。
                return;
            }

            // 4. 右键 (SECONDARY) -> 全部合并 (Merge All)
            if (action == ClickAction.SECONDARY) {
                int maxStack = slotStack.getMaxStackSize();
                int currentSlotCount = slotStack.getCount();
                int space = maxStack - currentSlotCount;

                // 如果满了，不处理（原版通常也无反应）
                if (space <= 0) return;

                // >>> 核心修改：尽可能多放 (Math.min(cursor, space)) <<<
                // 之前的错误是写成了 1，现在改成放全部！
                int amountToMove = Math.min(cursorStack.getCount(), space);

                if (amountToMove > 0) {
                    long expiryCursor = FreshnessHelper.getExpiryTime(level, cursorStack, true);
                    long expirySlot = FreshnessHelper.getExpiryTime(level, slotStack, true);

                    // 加权平均 (所有移入的 + 原有的)
                    long weightSlot = expirySlot * currentSlotCount;
                    long weightIncoming = expiryCursor * amountToMove;
                    long newAverageExpiry = (weightSlot + weightIncoming) / (currentSlotCount + amountToMove);

                    // 修改物品
                    slotStack.grow(amountToMove);
                    FreshnessHelper.setExpiryTime(slotStack, newAverageExpiry);

                    cursorStack.shrink(amountToMove);

                    // 拦截原版交换逻辑
                    event.setCanceled(true);

                    // 强制刷新，防止鬼畜
                    event.getSlot().setChanged();
                    if (event.getPlayer() instanceof net.minecraft.server.level.ServerPlayer serverPlayer) {
                        serverPlayer.containerMenu.sendAllDataToRemote();
                    }
                }
            }
        }
    }
}