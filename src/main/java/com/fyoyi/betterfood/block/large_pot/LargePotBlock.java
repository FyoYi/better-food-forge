package com.fyoyi.betterfood.block.large_pot;

import com.fyoyi.betterfood.block.entity.PotBlockEntity;
import com.fyoyi.betterfood.item.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

public class LargePotBlock extends BaseEntityBlock {

    private static final VoxelShape BOTTOM_SHAPE = Block.box(1, 0, 1, 15, 1, 15);
    private static final VoxelShape NORTH_WALL = Block.box(1, 1, 1, 15, 12, 3);
    private static final VoxelShape SOUTH_WALL = Block.box(1, 1, 13, 15, 12, 15);
    private static final VoxelShape WEST_WALL = Block.box(1, 1, 3, 3, 12, 13);
    private static final VoxelShape EAST_WALL = Block.box(13, 1, 3, 15, 12, 13);
    private static final VoxelShape SHAPE = Shapes.or(BOTTOM_SHAPE, NORTH_WALL, SOUTH_WALL, WEST_WALL, EAST_WALL);

    public static final DirectionProperty FACING = net.minecraft.world.level.block.HorizontalDirectionalBlock.FACING;
    public static final BooleanProperty HAS_LID = BooleanProperty.create("has_lid");
    public static final BooleanProperty HAS_WATER = BooleanProperty.create("has_water");

    public LargePotBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH).setValue(HAS_LID, false).setValue(HAS_WATER, false));
    }

    @Override
    public RenderShape getRenderShape(BlockState pState) {
        return RenderShape.MODEL;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pPos, BlockState pState) {
        return new PotBlockEntity(pPos, pState);
    }

    @Override
    public InteractionResult use(BlockState pState, Level pLevel, BlockPos pPos, Player pPlayer, InteractionHand pHand, BlockHitResult pHit) {
        if (pHand != InteractionHand.MAIN_HAND) return InteractionResult.PASS;

        ItemStack handItem = pPlayer.getItemInHand(pHand);
        boolean hasLid = pState.getValue(HAS_LID);
        boolean hasWater = pState.getValue(HAS_WATER);

        // 1. 安装锅盖
        if (handItem.getItem() == ModItems.LID.get() && !hasLid) {
            if (!pLevel.isClientSide) {
                pLevel.setBlock(pPos, pState.setValue(HAS_LID, true), 3);
                if (!pPlayer.isCreative()) handItem.shrink(1);
                pLevel.playSound(null, pPos, SoundEvents.IRON_TRAPDOOR_CLOSE, SoundSource.BLOCKS, 1.0F, 1.0F);
            }
            return InteractionResult.SUCCESS;
        }

        // 1.5. 加水
        if (handItem.getItem() == Items.WATER_BUCKET && !hasWater) {
            if (!pLevel.isClientSide) {
                pLevel.setBlock(pPos, pState.setValue(HAS_WATER, true), 3);
                if (!pPlayer.isCreative()) {
                    pPlayer.setItemInHand(pHand, new ItemStack(Items.BUCKET));
                }
                pLevel.playSound(null, pPos, SoundEvents.BUCKET_EMPTY, SoundSource.BLOCKS, 1.0F, 1.0F);
            }
            return InteractionResult.SUCCESS;
        }

        // 1.6. 倒水
        if (hasWater && handItem.getItem() == Items.BUCKET) {
            if (!pLevel.isClientSide) {
                pLevel.setBlock(pPos, pState.setValue(HAS_WATER, false), 3);
                if (!pPlayer.isCreative()) {
                    handItem.shrink(1);
                    if (!pPlayer.getInventory().add(new ItemStack(Items.WATER_BUCKET))) {
                        pPlayer.drop(new ItemStack(Items.WATER_BUCKET), false);
                    }
                }
                pLevel.playSound(null, pPos, SoundEvents.BUCKET_FILL, SoundSource.BLOCKS, 1.0F, 1.0F);
            }
            return InteractionResult.SUCCESS;
        }

        // 2. 端起锅
        if (pPlayer.isShiftKeyDown() && hasLid && handItem.isEmpty()) {
            if (!pLevel.isClientSide) {
                ItemStack potItem = new ItemStack(this);
                BlockEntity be = pLevel.getBlockEntity(pPos);
                if (be instanceof PotBlockEntity pot) {
                    pot.saveToItem(potItem);
                    CompoundTag blockEntityTag = potItem.getOrCreateTagElement("BlockEntityTag");
                    blockEntityTag.putBoolean("has_lid", true);
                    blockEntityTag.putBoolean("has_water", pState.getValue(HAS_WATER));
                    pot.getItems().clear();
                }
                pLevel.setBlock(pPos, Blocks.AIR.defaultBlockState(), 3);
                pPlayer.setItemInHand(pHand, potItem);
                pLevel.playSound(null, pPos, SoundEvents.LANTERN_BREAK, SoundSource.BLOCKS, 1.0F, 1.0F);
            }
            return InteractionResult.SUCCESS;
        }

        // 3. 取下锅盖
        if (hasLid) {
            if (!pLevel.isClientSide) {
                pLevel.setBlock(pPos, pState.setValue(HAS_LID, false), 3);
                ItemStack lidItem = new ItemStack(ModItems.LID.get());
                if (!pPlayer.getInventory().add(lidItem)) pPlayer.drop(lidItem, false);
                pLevel.playSound(null, pPos, SoundEvents.IRON_TRAPDOOR_OPEN, SoundSource.BLOCKS, 1.0F, 1.0F);
            }
            return InteractionResult.SUCCESS;
        }

        // 4. 端起无盖锅
        if (pPlayer.isShiftKeyDown() && handItem.isEmpty()) {
            if (!pLevel.isClientSide) {
                ItemStack potItem = new ItemStack(this);
                BlockEntity be = pLevel.getBlockEntity(pPos);
                if (be instanceof PotBlockEntity pot) {
                    pot.saveToItem(potItem);
                    CompoundTag blockEntityTag = potItem.getOrCreateTagElement("BlockEntityTag");
                    blockEntityTag.putBoolean("has_water", pState.getValue(HAS_WATER));
                    pot.getItems().clear();
                }
                pLevel.setBlock(pPos, Blocks.AIR.defaultBlockState(), 3);
                pPlayer.setItemInHand(pHand, potItem);
                pLevel.playSound(null, pPos, SoundEvents.LANTERN_BREAK, SoundSource.BLOCKS, 1.0F, 1.0F);
            }
            return InteractionResult.SUCCESS;
        }

        // 5. 存取物品
        if (!pLevel.isClientSide) {
            BlockEntity be = pLevel.getBlockEntity(pPos);
            if (be instanceof PotBlockEntity pot) {
                if (!handItem.isEmpty()) {
                    // === 【核心修复】必须是可食用的物品才能放入 ===
                    if (handItem.getItem().isEdible()) {
                        boolean success = pot.pushItem(handItem);
                        if (success) {
                            pLevel.playSound(null, pPos, SoundEvents.ITEM_FRAME_ADD_ITEM, SoundSource.BLOCKS, 1.0F, 1.0F);
                            if (!pPlayer.isCreative()) handItem.shrink(1);
                        }
                    }
                } else {
                    ItemStack takenItem = pot.popItem();
                    if (!takenItem.isEmpty()) {
                        if (!pPlayer.getInventory().add(takenItem)) pPlayer.drop(takenItem, false);
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
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, HAS_LID, HAS_WATER);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        ItemStack stack = context.getItemInHand();
        CompoundTag tag = stack.getTagElement("BlockEntityTag");
        boolean hasLid = false;
        boolean hasWater = false;
        if (tag != null) {
            if (tag.contains("has_lid")) hasLid = tag.getBoolean("has_lid");
            if (tag.contains("has_water")) hasWater = tag.getBoolean("has_water");
        }

        return this.defaultBlockState()
                .setValue(FACING, context.getHorizontalDirection().getOpposite())
                .setValue(HAS_LID, hasLid)
                .setValue(HAS_WATER, hasWater);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }
}