package com.fyoyi.betterfood.block.cooking_pan;

import com.fyoyi.betterfood.block.entity.ModBlockEntities;
import com.fyoyi.betterfood.block.entity.PotBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items; // 必须导入 Items
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker; // 必须导入
import net.minecraft.world.level.block.entity.BlockEntityType; // 必须导入
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

public class SimpleFoodBlock extends BaseEntityBlock {

    private static final VoxelShape BOTTOM_SHAPE = Block.box(2, 0, 2, 14, 1, 14);
    private static final VoxelShape NORTH_WALL = Block.box(2, 1, 2, 14, 4, 3);
    private static final VoxelShape SOUTH_WALL = Block.box(2, 1, 13, 14, 4, 14);
    private static final VoxelShape WEST_WALL = Block.box(2, 1, 3, 3, 4, 13);
    private static final VoxelShape EAST_WALL = Block.box(13, 1, 3, 14, 4, 13);
    private static final VoxelShape SHAPE = Shapes.or(BOTTOM_SHAPE, NORTH_WALL, SOUTH_WALL, WEST_WALL, EAST_WALL);
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

    // === 【新增关键方法】 注册 Ticker，让 BlockEntity 能每帧运行 ===
    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level pLevel, BlockState pState, BlockEntityType<T> pBlockEntityType) {
        // 我们只需要在客户端运行动画逻辑，但通常为了保险双端都注册
        // 这里的 createTickerHelper 会检查方块实体类型是否匹配
        return createTickerHelper(pBlockEntityType, ModBlockEntities.POT_BE.get(), PotBlockEntity::tick);
    }

    @Override
    public RenderShape getRenderShape(BlockState pState) {
        return RenderShape.MODEL;
    }

    @Override
    public InteractionResult use(BlockState pState, Level pLevel, BlockPos pPos, Player pPlayer, InteractionHand pHand, BlockHitResult pHit) {
        if (pHand != InteractionHand.MAIN_HAND) return InteractionResult.PASS;

        // 1. 蹲下拿锅逻辑
        if (pPlayer.isShiftKeyDown() && pPlayer.getItemInHand(pHand).isEmpty()) {
            if (!pLevel.isClientSide) {
                ItemStack potItem = new ItemStack(this);
                BlockEntity be = pLevel.getBlockEntity(pPos);
                if (be instanceof PotBlockEntity pot) {
                    pot.saveToItem(potItem);
                    pot.getItems().clear(); // 清空防止掉落
                }
                pLevel.removeBlock(pPos, false);
                pPlayer.setItemInHand(pHand, potItem);
                pLevel.playSound(null, pPos, SoundEvents.LANTERN_BREAK, SoundSource.BLOCKS, 1.0F, 1.0F);
            }
            return InteractionResult.SUCCESS;
        }

        // === 2. 【新增】用木棍翻炒逻辑 ===
        ItemStack handStack = pPlayer.getItemInHand(pHand);
        if (handStack.getItem() == Items.STICK) {
            BlockEntity be = pLevel.getBlockEntity(pPos);
            if (be instanceof PotBlockEntity pot) {
                // 触发动画 (双端触发：客户端直接看动画，服务端保证逻辑一致)
                pot.triggerFlip();

                // 播放声音 (模拟炒菜声)
                pLevel.playSound(pPlayer, pPos, SoundEvents.GENERIC_EXTINGUISH_FIRE, SoundSource.BLOCKS, 0.5F, 1.5F);

                // 返回成功，挥动一下手
                return InteractionResult.sidedSuccess(pLevel.isClientSide);
            }
        }

        // 3. 正常放入/取出逻辑
        if (!pLevel.isClientSide) {
            BlockEntity be = pLevel.getBlockEntity(pPos);
            if (be instanceof PotBlockEntity pot) {

                if (!handStack.isEmpty()) {
                    if (handStack.getItem().isEdible()) {
                        boolean success = pot.pushItem(handStack);
                        if (success) {
                            pLevel.playSound(null, pPos, SoundEvents.ITEM_FRAME_ADD_ITEM, SoundSource.BLOCKS, 1.0F, 1.0F);
                            if (!pPlayer.isCreative()) {
                                handStack.shrink(1);
                            }
                        }
                    }
                } else {
                    ItemStack takenItem = pot.popItem();
                    if (!takenItem.isEmpty()) {
                        if (!pPlayer.getInventory().add(takenItem)) {
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
    public void onRemove(BlockState pState, Level pLevel, BlockPos pPos, BlockState pNewState, boolean pIsMoving) {
        if (pState.getBlock() != pNewState.getBlock()) {
            BlockEntity blockEntity = pLevel.getBlockEntity(pPos);
            if (blockEntity instanceof PotBlockEntity) {
                Containers.dropContents(pLevel, pPos, ((PotBlockEntity) blockEntity).getItems());
            }
        }
        super.onRemove(pState, pLevel, pPos, pNewState, pIsMoving);
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