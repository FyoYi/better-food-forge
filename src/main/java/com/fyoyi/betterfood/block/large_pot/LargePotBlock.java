package com.fyoyi.betterfood.block.large_pot;

import com.fyoyi.betterfood.block.entity.PotBlockEntity;
import com.fyoyi.betterfood.item.ModItems; // 确保导入了这个
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
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

public class LargePotBlock extends BaseEntityBlock {

    // 碰撞箱 (保持不变)
    private static final VoxelShape BOTTOM_SHAPE = Block.box(1, 0, 1, 15, 1, 15);
    private static final VoxelShape NORTH_WALL = Block.box(1, 1, 1, 15, 12, 3);
    private static final VoxelShape SOUTH_WALL = Block.box(1, 1, 13, 15, 12, 15);
    private static final VoxelShape WEST_WALL = Block.box(1, 1, 3, 3, 12, 13);
    private static final VoxelShape EAST_WALL = Block.box(13, 1, 3, 15, 12, 13);
    private static final VoxelShape SHAPE = Shapes.or(BOTTOM_SHAPE, NORTH_WALL, SOUTH_WALL, WEST_WALL, EAST_WALL);

    public static final DirectionProperty FACING = net.minecraft.world.level.block.HorizontalDirectionalBlock.FACING;
    // 【新增】是否有盖子
    public static final BooleanProperty HAS_LID = BooleanProperty.create("has_lid");
    // 【新增】是否有水
    public static final BooleanProperty HAS_WATER = BooleanProperty.create("has_water");

    public LargePotBlock(Properties properties) {
        super(properties);
        // 默认无盖无水
        this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH).setValue(HAS_LID, false).setValue(HAS_WATER, false));
    }

    // 【重要】确保模型渲染
    @Override
    public RenderShape getRenderShape(BlockState pState) {
        return RenderShape.MODEL;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pPos, BlockState pState) {
        return new PotBlockEntity(pPos, pState);
    }

    // === 核心交互逻辑 ===
    @Override
    public InteractionResult use(BlockState pState, Level pLevel, BlockPos pPos, Player pPlayer, InteractionHand pHand, BlockHitResult pHit) {
        if (pHand != InteractionHand.MAIN_HAND) return InteractionResult.PASS;

        ItemStack handItem = pPlayer.getItemInHand(pHand);
        boolean hasLid = pState.getValue(HAS_LID);
        boolean hasWater = pState.getValue(HAS_WATER);

        // 1. 【安装锅盖】手里拿锅盖 + 锅没盖子
        if (handItem.getItem() == ModItems.LID.get() && !hasLid) {
            if (!pLevel.isClientSide) {
                // 设置为有盖状态
                pLevel.setBlock(pPos, pState.setValue(HAS_LID, true), 3);
                // 消耗玩家手里的锅盖
                if (!pPlayer.isCreative()) {
                    handItem.shrink(1);
                }
                pLevel.playSound(null, pPos, SoundEvents.IRON_TRAPDOOR_CLOSE, SoundSource.BLOCKS, 1.0F, 1.0F);
            }
            return InteractionResult.SUCCESS;
        }

        // 1.5. 【添加水】手里拿水桶 + 锅没水
        if (handItem.getItem() == Items.WATER_BUCKET && !hasWater) {
            if (!pLevel.isClientSide) {
                // 设置为有水状态
                pLevel.setBlock(pPos, pState.setValue(HAS_WATER, true), 3);
                // 给玩家空桶
                if (!pPlayer.isCreative()) {
                    ItemStack emptyBucket = new ItemStack(Items.BUCKET);
                    if (handItem.getCount() == 1) {
                        // 如果手上只有一个水桶，直接替换
                        pPlayer.setItemInHand(pHand, emptyBucket);
                    } else {
                        // 如果手上有多个水桶，减少一个并尝试添加空桶到背包
                        handItem.shrink(1);
                        if (!pPlayer.getInventory().add(emptyBucket)) {
                            pPlayer.drop(emptyBucket, false);
                        }
                    }
                }
                pLevel.playSound(null, pPos, SoundEvents.BUCKET_EMPTY, SoundSource.BLOCKS, 1.0F, 1.0F);
            }
            return InteractionResult.SUCCESS;
        }

        // 3.5. 【倒出水】锅有水 + 手里是空桶
        if (hasWater && handItem.getItem() == Items.BUCKET) {
            if (!pLevel.isClientSide) {
                // 设置为无水状态
                pLevel.setBlock(pPos, pState.setValue(HAS_WATER, false), 3);
                // 给玩家水桶
                if (!pPlayer.isCreative()) {
                    ItemStack waterBucket = new ItemStack(Items.WATER_BUCKET);
                    if (handItem.getCount() == 1) {
                        // 如果手上只有一个空桶，直接替换
                        pPlayer.setItemInHand(pHand, waterBucket);
                    } else {
                        // 如果手上有多个空桶，减少一个并尝试添加到背包
                        handItem.shrink(1);
                        if (!pPlayer.getInventory().add(waterBucket)) {
                            pPlayer.drop(waterBucket, false);
                        }
                    }
                }
                pLevel.playSound(null, pPos, SoundEvents.BUCKET_FILL, SoundSource.BLOCKS, 1.0F, 1.0F);
            }
            return InteractionResult.SUCCESS;
        }

        // 2. 【端起锅】蹲下右键 + 锅有盖子 -> 端起整个锅（包括锅盖）
        if (pPlayer.isShiftKeyDown() && hasLid && handItem.isEmpty()) {
            if (!pLevel.isClientSide) {
                ItemStack potItem = new ItemStack(this);

                BlockEntity be = pLevel.getBlockEntity(pPos);
                if (be instanceof PotBlockEntity pot) {
                    // 保存数据到物品，包括标记有盖子
                    pot.saveToItem(potItem); // 使用公共方法保存数据
                    
                    // 获取现有的BlockEntityTag并添加has_lid和has_water属性
                    CompoundTag blockEntityTag = potItem.getTagElement("BlockEntityTag");
                    if (blockEntityTag == null) {
                        blockEntityTag = new CompoundTag();
                    }
                    blockEntityTag.putBoolean("has_lid", true); // 标记有盖子
                    blockEntityTag.putBoolean("has_water", pState.getValue(HAS_WATER)); // 标记是否有水
                    potItem.addTagElement("BlockEntityTag", blockEntityTag);

                    // 清空方块里的物品
                    pot.getItems().clear();
                }

                // 销毁方块但不触发掉落
                pLevel.setBlock(pPos, net.minecraft.world.level.block.Blocks.AIR.defaultBlockState(), 3);

                // 给玩家锅（带盖子）
                pPlayer.setItemInHand(pHand, potItem);
                pLevel.playSound(null, pPos, SoundEvents.LANTERN_BREAK, SoundSource.BLOCKS, 1.0F, 1.0F);
            }
            return InteractionResult.SUCCESS;
        }

        // 3. 【取下锅盖】锅有盖子 -> 右键取下
        // (注：如果有盖子，优先级最高，无法存取食物，必须先开盖)
        if (hasLid) {
            if (!pLevel.isClientSide) {
                // 变回无盖状态
                pLevel.setBlock(pPos, pState.setValue(HAS_LID, false), 3);

                // 给玩家锅盖
                ItemStack lidItem = new ItemStack(ModItems.LID.get());
                if (!pPlayer.getInventory().add(lidItem)) {
                    pPlayer.drop(lidItem, false);
                }
                pLevel.playSound(null, pPos, SoundEvents.IRON_TRAPDOOR_OPEN, SoundSource.BLOCKS, 1.0F, 1.0F);
            }
            return InteractionResult.SUCCESS;
        }

        // 4. 【存取食物/搬运】只有无盖时才能操作
        // 复用 SimpleFoodBlock 的逻辑
        if (pPlayer.isShiftKeyDown() && handItem.isEmpty()) {
            if (!pLevel.isClientSide) {
                ItemStack potItem = new ItemStack(this);

                BlockEntity be = pLevel.getBlockEntity(pPos);
                if (be instanceof PotBlockEntity pot) {
                    // 1. 保存数据到物品
                    pot.saveToItem(potItem);
                    
                    // 2. 保存水的状态
                    CompoundTag blockEntityTag = potItem.getTagElement("BlockEntityTag");
                    if (blockEntityTag == null) {
                        blockEntityTag = new CompoundTag();
                    }
                    blockEntityTag.putBoolean("has_water", pState.getValue(HAS_WATER)); // 标记是否有水
                    potItem.addTagElement("BlockEntityTag", blockEntityTag);

                    pot.getItems().clear();
                }

                // 3. 销毁方块但不触发掉落
                pLevel.setBlock(pPos, Blocks.AIR.defaultBlockState(), 3);

                // 4. 给玩家锅
                pPlayer.setItemInHand(pHand, potItem);
                pLevel.playSound(null, pPos, SoundEvents.LANTERN_BREAK, SoundSource.BLOCKS, 1.0F, 1.0F);
            }
            return InteractionResult.SUCCESS;
        }

        // === 5. 正常放入/取出逻辑 ===
        if (!pLevel.isClientSide) {
            BlockEntity be = pLevel.getBlockEntity(pPos);
            if (be instanceof PotBlockEntity pot) {
                if (!handItem.isEmpty()) {
                    if (handItem.getItem().isEdible()) {
                        boolean success = pot.pushItem(handItem);
                        if (success) {
                            pLevel.playSound(null, pPos, SoundEvents.ITEM_FRAME_ADD_ITEM, SoundSource.BLOCKS, 1.0F, 1.0F);
                            if (!pPlayer.isCreative()) {
                                handItem.shrink(1);
                            }
                        }
                    }
                } else {
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

    // 破坏时掉落锅盖
    @Override
    public void onRemove(BlockState pState, Level pLevel, BlockPos pPos, BlockState pNewState, boolean pIsMoving) {
        if (pState.getBlock() != pNewState.getBlock()) {
            // 掉落内部食物
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
        // 从物品中读取是否有盖子和水的状态
        ItemStack stack = context.getItemInHand();
        CompoundTag tag = stack.getTagElement("BlockEntityTag");
        boolean hasLid = false;
        boolean hasWater = false;
        if (tag != null) {
            if (tag.contains("has_lid")) {
                hasLid = tag.getBoolean("has_lid");
            }
            if (tag.contains("has_water")) {
                hasWater = tag.getBoolean("has_water");
            }
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