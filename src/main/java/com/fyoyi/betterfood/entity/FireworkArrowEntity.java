package com.fyoyi.betterfood.entity;

import com.fyoyi.betterfood.item.sundries.Sundries_item;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.FireworkRocketEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3; // 导入 Vec3

public class FireworkArrowEntity extends AbstractArrow {
    private int lifetime = 0;
    private ItemStack fireworkStack = new ItemStack(Items.FIREWORK_ROCKET);

    public FireworkArrowEntity(EntityType<? extends FireworkArrowEntity> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
    }

    public FireworkArrowEntity(Level pLevel, LivingEntity pShooter) {
        super(ModEntities.FIREWORK_ARROW.get(), pShooter, pLevel);
    }

    public void setFireworkProperties(ItemStack pStack) {
        if (pStack.hasTag()) {
            this.fireworkStack = pStack.copy();
            CompoundTag fireworksTag = pStack.getTag().getCompound("Fireworks");
            this.lifetime = fireworksTag.getByte("Flight") * 10 + this.random.nextInt(6) + this.random.nextInt(7);
        }
    }

    @Override
    public void tick() {
        super.tick();
        if (!this.level().isClientSide) {
            if (this.lifetime > 0) {
                this.lifetime--;
            }
            if (this.lifetime <= 0) {
                this.explode();
                this.discard();
            }
        }
    }

    /**
     * 执行爆炸的方法 - 增加了动量传递
     */
    private void explode() {
        if (!this.level().isClientSide && this.fireworkStack.hasTag()) {
            // 1. 捕获箭实体在爆炸前的速度和方向
            Vec3 arrowVelocity = this.getDeltaMovement();

            ItemStack explosionStack = this.fireworkStack.copy();
            explosionStack.getOrCreateTagElement("Fireworks").putByte("Flight", (byte) -2);

            FireworkRocketEntity firework = new FireworkRocketEntity(
                    this.level(),
                    this.getX(), this.getY(), this.getZ(),
                    explosionStack
            );

            // 2. 将箭的动量传递给即将爆炸的烟花实体
            firework.setDeltaMovement(arrowVelocity);

            this.level().addFreshEntity(firework);
        }
    }

    /**
     * 当箭射中实体时调用
     */
    @Override
    protected void onHitEntity(EntityHitResult pResult) {
        super.onHitEntity(pResult);
        this.explode();
        this.discard();
    }

    /**
     * 当箭射中方块时调用
     */
    @Override
    protected void onHitBlock(BlockHitResult pResult) {
        super.onHitBlock(pResult);
        this.explode();
        this.discard();
    }

    // --- 数据持久化 (无需修改) ---
    @Override
    public void addAdditionalSaveData(CompoundTag pCompound) {
        super.addAdditionalSaveData(pCompound);
        pCompound.putInt("Lifetime", this.lifetime);
        pCompound.put("FireworkStack", this.fireworkStack.save(new CompoundTag()));
    }

    @Override
    public void readAdditionalSaveData(CompoundTag pCompound) {
        super.readAdditionalSaveData(pCompound);
        this.lifetime = pCompound.getInt("Lifetime");
        this.fireworkStack = ItemStack.of(pCompound.getCompound("FireworkStack"));
    }

    @Override
    protected ItemStack getPickupItem() {
        return new ItemStack(Sundries_item.FIREWORK_ARROW.get());
    }
}