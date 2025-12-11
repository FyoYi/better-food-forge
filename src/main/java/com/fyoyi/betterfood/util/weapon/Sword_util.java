package com.fyoyi.betterfood.util.weapon;

import com.fyoyi.betterfood.item.sundries.Sundries_item;
import com.fyoyi.betterfood.util.ModTiers;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraftforge.common.ForgeTier;

public class Sword_util {

    public static final ForgeTier RUBY_SWORD =
            new ForgeTier(
                    ModTiers.RUBY.getLEVEL(),
                    ModTiers.RUBY.getUSES(),
                    ModTiers.RUBY.getSPEED(),
                    ModTiers.RUBY.getATTACKDAMAGE(),
                    ModTiers.RUBY.getENCHANTMENTVALUE(),
                    BlockTags.SWORD_EFFICIENT,
                    () -> Ingredient.of(Sundries_item.RUBY.get())
            );

    public static final ForgeTier RUBY_WOODEN_SWORD =
            new ForgeTier(
                    ModTiers.RUBY_WOODEN.getLEVEL(),
                    ModTiers.RUBY_WOODEN.getUSES(),
                    ModTiers.RUBY_WOODEN.getSPEED(),
                    ModTiers.RUBY_WOODEN.getATTACKDAMAGE(),
                    ModTiers.RUBY_WOODEN.getENCHANTMENTVALUE(),
                    BlockTags.SWORD_EFFICIENT,
                    () -> Ingredient.of(Sundries_item.RUBY.get())
            );
    public static final ForgeTier RUBY_GOLDEN_SWORD =
            new ForgeTier(
                    ModTiers.RUBY_GOLDEN.getLEVEL(),
                    ModTiers.RUBY_GOLDEN.getUSES(),
                    ModTiers.RUBY_GOLDEN.getSPEED(),
                    ModTiers.RUBY_GOLDEN.getATTACKDAMAGE(),
                    ModTiers.RUBY_GOLDEN.getENCHANTMENTVALUE(),
                    BlockTags.SWORD_EFFICIENT,
                    () -> Ingredient.of(Sundries_item.RUBY.get())
            );
    public static final ForgeTier RUBY_STONE_SWORD =
            new ForgeTier(
                    ModTiers.RUBY_STONE.getLEVEL(),
                    ModTiers.RUBY_STONE.getUSES(),
                    ModTiers.RUBY_STONE.getSPEED(),
                    ModTiers.RUBY_STONE.getATTACKDAMAGE(),
                    ModTiers.RUBY_STONE.getENCHANTMENTVALUE(),
                    BlockTags.SWORD_EFFICIENT,
                    () -> Ingredient.of(Sundries_item.RUBY.get())
            );
    public static final ForgeTier RUBY_IRON_SWORD =
            new ForgeTier(
                    ModTiers.RUBY_IRON.getLEVEL(),
                    ModTiers.RUBY_IRON.getUSES(),
                    ModTiers.RUBY_IRON.getSPEED(),
                    ModTiers.RUBY_IRON.getATTACKDAMAGE(),
                    ModTiers.RUBY_IRON.getENCHANTMENTVALUE(),
                    BlockTags.SWORD_EFFICIENT,
                    () -> Ingredient.of(Sundries_item.RUBY.get())
            );
    public static final ForgeTier RUBY_DIAMOND_SWORD =
            new ForgeTier(
                    ModTiers.RUBY_DIAMOND.getLEVEL(),
                    ModTiers.RUBY_DIAMOND.getUSES(),
                    ModTiers.RUBY_DIAMOND.getSPEED(),
                    ModTiers.RUBY_DIAMOND.getATTACKDAMAGE(),
                    ModTiers.RUBY_DIAMOND.getENCHANTMENTVALUE(),
                    BlockTags.SWORD_EFFICIENT,
                    () -> Ingredient.of(Sundries_item.RUBY.get())
            );
    public static final ForgeTier RUBY_NETTHERITE_SWORD =
            new ForgeTier(
                    ModTiers.RUBY_NETTHERITE.getLEVEL(),
                    ModTiers.RUBY_NETTHERITE.getUSES(),
                    ModTiers.RUBY_NETTHERITE.getSPEED(),
                    ModTiers.RUBY_NETTHERITE.getATTACKDAMAGE(),
                    ModTiers.RUBY_NETTHERITE.getENCHANTMENTVALUE(),
                    BlockTags.SWORD_EFFICIENT,
                    () -> Ingredient.of(Sundries_item.RUBY.get())
            );


}
