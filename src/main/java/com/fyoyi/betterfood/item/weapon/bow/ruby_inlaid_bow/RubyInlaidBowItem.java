package com.fyoyi.betterfood.item.weapon.bow.ruby_inlaid_bow;

import com.fyoyi.betterfood.entity.FireworkArrowEntity;
import com.fyoyi.betterfood.item.sundries.Sundries_item;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.tags.ItemTags; // 我们依然需要这个来识别原版箭
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.*;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;

import java.util.function.Predicate;

public class RubyInlaidBowItem extends BowItem {

    public RubyInlaidBowItem(Item.Properties pProperties) {
        super(pProperties);
    }

    /**
     * 1. 核心修正：我们不再使用自定义标签来识别弹药。
     * 而是用最直接的方式：检查它是否属于原版箭标签，或者它是否就是我们的烟花箭物品。
     * 这是最稳定、100%可靠的方法。
     */
    @Override
    public Predicate<ItemStack> getAllSupportedProjectiles() {
        return (stack) -> stack.is(ItemTags.ARROWS) || stack.is(Sundries_item.FIREWORK_ARROW.get());
    }

    /**
     * 2. 我们仍然需要我们自己的弹药查找方法。
     * 因为原版的 player.getProjectile() 只会查找 ItemTags.ARROWS，它不认识我们的烟花箭。
     */
    private ItemStack findAmmo(Player player) {
        for (int i = 0; i < player.getInventory().getContainerSize(); ++i) {
            ItemStack itemstack = player.getInventory().getItem(i);
            // 这里会调用我们上面修正过的、可靠的 getAllSupportedProjectiles() 方法
            if (this.getAllSupportedProjectiles().test(itemstack)) {
                return itemstack;
            }
        }
        return ItemStack.EMPTY;
    }

    public AbstractArrow customArrow(AbstractArrow arrow) {
        arrow.setBaseDamage(4.0D);
        return arrow;
    }

    @Override
    public void releaseUsing(ItemStack pStack, Level pLevel, LivingEntity pLivingEntity, int pUseRemaining) {
        if (!(pLivingEntity instanceof Player player)) {
            return;
        }

        boolean hasInfinity = player.getAbilities().instabuild || EnchantmentHelper.getItemEnchantmentLevel(Enchantments.INFINITY_ARROWS, pStack) > 0;

        // 3. 使用我们自己的、可靠的 findAmmo 方法
        ItemStack ammoStack = this.findAmmo(player);

        if (ammoStack.isEmpty() && !hasInfinity) {
            return;
        }
        if (ammoStack.isEmpty()) {
            ammoStack = new ItemStack(Items.ARROW);
        }

        int useDuration = this.getUseDuration(pStack) - pUseRemaining;
        float power = getPowerForTime(useDuration);
        if (!((double) power < 0.1D)) {
            boolean isCreativeOrInfinity = player.getAbilities().instabuild || (hasInfinity && getAllSupportedProjectiles().test(ammoStack));

            if (!pLevel.isClientSide) {
                int numberOfArrows = 1;
                // 4. 判断逻辑也改回直接的物品比较，这是最可靠的
                boolean isFireworkArrow = ammoStack.is(Sundries_item.FIREWORK_ARROW.get());
                AbstractArrow arrowToShoot;

                for (int i = 0; i < numberOfArrows; ++i) {
                    if (isFireworkArrow) {
                        FireworkArrowEntity fireworkArrow = new FireworkArrowEntity(pLevel, player);
                        fireworkArrow.setFireworkProperties(ammoStack);
                        arrowToShoot = fireworkArrow;
                    } else {
                        ArrowItem arrowitem = (ArrowItem) (ammoStack.getItem() instanceof ArrowItem ? ammoStack.getItem() : Items.ARROW);
                        arrowToShoot = arrowitem.createArrow(pLevel, ammoStack, player);
                    }

                    float velocityMultiplier = 3.5F;
                    arrowToShoot.shootFromRotation(player, player.getXRot(), player.getYRot(), 0.0F, power * velocityMultiplier, 1.0F);
                    arrowToShoot = this.customArrow(arrowToShoot);

                    if (power == 1.0F) {
                        arrowToShoot.setCritArrow(true);
                    }
                    int powerLevel = EnchantmentHelper.getItemEnchantmentLevel(Enchantments.POWER_ARROWS, pStack);
                    if (powerLevel > 0) {
                        arrowToShoot.setBaseDamage(arrowToShoot.getBaseDamage() + (double) powerLevel * 0.7D + 0.5D);
                    }
                    int punchLevel = EnchantmentHelper.getItemEnchantmentLevel(Enchantments.PUNCH_ARROWS, pStack);
                    if (punchLevel > 0) {
                        arrowToShoot.setKnockback(punchLevel);
                    }
                    if (EnchantmentHelper.getItemEnchantmentLevel(Enchantments.FLAMING_ARROWS, pStack) > 0) {
                        arrowToShoot.setSecondsOnFire(100);
                    }

                    pStack.hurtAndBreak(1, player, (p_40665_) -> {
                        p_40665_.broadcastBreakEvent(player.getUsedItemHand());
                    });
                    pLevel.addFreshEntity(arrowToShoot);
                }
            }
            pLevel.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.ARROW_SHOOT, SoundSource.PLAYERS, 1.0F, 1.0F / (pLevel.getRandom().nextFloat() * 0.4F + 1.2F) + power * 0.5F);

            if (!isCreativeOrInfinity) {
                ammoStack.shrink(1);
                if (ammoStack.isEmpty()) {
                    player.getInventory().removeItem(ammoStack);
                }
            }

            player.awardStat(Stats.ITEM_USED.get(this));
        }
    }
}

