package com.fyoyi.betterfood.item;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Equipable; // 关键接口：允许物品被装备
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;

import java.util.UUID;

// 1. 实现 Equipable 接口，让右键穿戴功能生效
public class PotBlockItem extends BlockItem implements Equipable {

    // 定义固定的 UUID，防止属性叠加错误
    protected static final UUID BASE_ATTACK_DAMAGE_UUID = UUID.fromString("CB3F55D3-645C-4F38-A497-9C13A33DB5CF");
    protected static final UUID BASE_ATTACK_SPEED_UUID = UUID.fromString("FA233E1C-4180-4865-B01B-BCCE9785ACA3");
    protected static final UUID ARMOR_MODIFIER_UUID = UUID.fromString("632897F2-39C1-47A6-808B-740700392EF5");

    private final Multimap<Attribute, AttributeModifier> defaultModifiers;

    public PotBlockItem(Block block, Properties properties) {
        super(block, properties);

        // 初始化基础属性 (主要用于主手武器属性)
        ImmutableMultimap.Builder<Attribute, AttributeModifier> builder = ImmutableMultimap.builder();
        // 武器伤害 (3.0 + 基础1 = 4点)
        builder.put(Attributes.ATTACK_DAMAGE, new AttributeModifier(BASE_ATTACK_DAMAGE_UUID, "Weapon modifier", 3.0D, AttributeModifier.Operation.ADDITION));
        // 攻击速度 (类似于剑)
        builder.put(Attributes.ATTACK_SPEED, new AttributeModifier(BASE_ATTACK_SPEED_UUID, "Weapon modifier", -2.4D, AttributeModifier.Operation.ADDITION));
        this.defaultModifiers = builder.build();
    }

    // 2. 核心：指定这个物品只能装备在【头部】
    @Override
    public EquipmentSlot getEquipmentSlot() {
        return EquipmentSlot.HEAD;
    }

    // 3. 定义装备时的音效 (穿上金属装备的声音)
    @Override
    public SoundEvent getEquipSound() {
        return SoundEvents.ARMOR_EQUIP_IRON;
    }

    // 4. 精确控制属性：只有在对应位置才生效
    @Override
    public Multimap<Attribute, AttributeModifier> getDefaultAttributeModifiers(EquipmentSlot slot) {
        // 如果是在【主手】，应用武器属性
        if (slot == EquipmentSlot.MAINHAND) {
            return this.defaultModifiers;
        }

        // 如果是在【头部】，应用护甲属性
        if (slot == EquipmentSlot.HEAD) {
            ImmutableMultimap.Builder<Attribute, AttributeModifier> builder = ImmutableMultimap.builder();
            // 提供 3 点护甲 (相当于铁头盔)
            builder.put(Attributes.ARMOR, new AttributeModifier(ARMOR_MODIFIER_UUID, "Armor modifier", 3.0D, AttributeModifier.Operation.ADDITION));
            return builder.build();
        }

        // 其他位置 (胸、腿、鞋) 不给任何属性，直接调用父类空属性
        return super.getDefaultAttributeModifiers(slot);
    }
}