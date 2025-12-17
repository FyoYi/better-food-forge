package com.fyoyi.betterfood.block;

import com.fyoyi.betterfood.block.entity.PotBlockEntity;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

public class SimpleFoodBlock extends BaseEntityBlock {

    public static final VoxelShape SHAPE = Block.box(2, 0, 2, 14, 6, 14);
    public static final net.minecraft.world.level.block.state.properties.DirectionProperty FACING = net.minecraft.world.level.block.HorizontalDirectionalBlock.FACING;

    public SimpleFoodBlock(Properties pProperties) {
        super(pProperties);
        this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH));
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pPos, BlockState pState) {
        return new PotBlockEntity(pPos, pState);
    }

    @Override
    public RenderShape getRenderShape(BlockState pState) {
        return RenderShape.MODEL;
    }

    // === 【核心修改】支持放入4个物品 ===
    @Override
    public InteractionResult use(BlockState pState, Level pLevel, BlockPos pPos, Player pPlayer, InteractionHand pHand, BlockHitResult pHit) {
        if (pHand != InteractionHand.MAIN_HAND) return InteractionResult.PASS;

        // 1. 蹲下拿锅逻辑 (不变)
        if (pPlayer.isShiftKeyDown() && pPlayer.getItemInHand(pHand).isEmpty()) {
            if (!pLevel.isClientSide) {
                pLevel.removeBlock(pPos, false);
                popResource(pLevel, pPos, new ItemStack(this));
            }
            return InteractionResult.SUCCESS;
        }

        if (!pLevel.isClientSide) {
            BlockEntity be = pLevel.getBlockEntity(pPos);
            if (be instanceof PotBlockEntity pot) {
                ItemStack handItem = pPlayer.getItemInHand(pHand);

                // 情况A：手里有东西 -> 尝试放入 (Push)
                if (!handItem.isEmpty()) {

                    // ======================================================
                    // 【核心修改】判断是否是食物
                    // ======================================================
                    // isEdible() 会检查物品是否拥有 FoodProperties (食物属性)
                    // 这样苹果、面包、猪排能放进去，但石头、剑就放不进去了
                    if (handItem.getItem().isEdible()) {

                        boolean success = pot.pushItem(handItem);

                        if (success) {
                            pLevel.playSound(null, pPos, SoundEvents.ITEM_FRAME_ADD_ITEM, SoundSource.BLOCKS, 1.0F, 1.0F);
                            if (!pPlayer.isCreative()) {
                                handItem.shrink(1);
                            }
                        } else {
                            // 满了，提示一下玩家（可选）
                             pPlayer.displayClientMessage(Component.literal("锅满了！").withStyle(ChatFormatting.GOLD), true);
                        }
                    } else {
                        // 如果不是食物，什么都不做，或者给个提示
                         pPlayer.displayClientMessage(Component.literal("这不能吃！").withStyle(ChatFormatting.GOLD), true);
                    }
                }
                // 情况B：手里没东西 -> 尝试取出 (Pop) (保持不变)
                else {
                    ItemStack takenItem = pot.popItem();
                    if (!takenItem.isEmpty()) {
                        boolean added = pPlayer.getInventory().add(takenItem);
                        if (!added || !takenItem.isEmpty()) {
                            pPlayer.drop(takenItem, false);
                        }
                        pLevel.playSound(null, pPos, SoundEvents.ITEM_FRAME_REMOVE_ITEM, SoundSource.BLOCKS, 1.0F, 1.0F);
                    }
                }
            }
        }

        return InteractionResult.SUCCESS;
    }

    @Override
    public VoxelShape getShape(BlockState pState, BlockGetter pLevel, BlockPos pPos, CollisionContext pContext) {
        return SHAPE;
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext pContext) {
        return this.defaultBlockState().setValue(FACING, pContext.getHorizontalDirection().getOpposite());
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> pBuilder) {
        pBuilder.add(FACING);
    }
}