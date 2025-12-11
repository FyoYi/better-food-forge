package com.fyoyi.betterfood.util.tools;

import com.fyoyi.betterfood.util.ModTiers;
import com.fyoyi.betterfood.item.sundries.Sundries_item;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraftforge.common.ForgeTier;

public class RubyTools_util {

    public static final ForgeTier RUBY_PICKAXE =
            new ForgeTier(
                    ModTiers.RUBY.getLEVEL(),
                    ModTiers.RUBY.getUSES(),
                    ModTiers.RUBY.getSPEED(),
                    ModTiers.RUBY.getATTACKDAMAGE(),
                    ModTiers.RUBY.getENCHANTMENTVALUE(),
                    BlockTags.NEEDS_IRON_TOOL,
                    () -> Ingredient.of(Sundries_item.RUBY.get())
            );

    public static final ForgeTier RUBY_AXE =
            new ForgeTier(
                    ModTiers.RUBY.getLEVEL(),
                    ModTiers.RUBY.getUSES(),
                    ModTiers.RUBY.getSPEED(),
                    ModTiers.RUBY.getATTACKDAMAGE(),
                    ModTiers.RUBY.getENCHANTMENTVALUE(),
                    BlockTags.MINEABLE_WITH_AXE,
                    () -> Ingredient.of(Sundries_item.RUBY.get())
            );

    public static final ForgeTier RUBY_SHOVEL =
            new ForgeTier(
                    ModTiers.RUBY.getLEVEL(),
                    ModTiers.RUBY.getUSES(),
                    ModTiers.RUBY.getSPEED(),
                    ModTiers.RUBY.getATTACKDAMAGE(),
                    ModTiers.RUBY.getENCHANTMENTVALUE(),
                    BlockTags.MINEABLE_WITH_SHOVEL,
                    () -> Ingredient.of(Sundries_item.RUBY.get())
            );

    public static final ForgeTier RUBY_HOE =
            new ForgeTier(
                    ModTiers.RUBY.getLEVEL(),
                    ModTiers.RUBY.getUSES(),
                    ModTiers.RUBY.getSPEED(),
                    ModTiers.RUBY.getATTACKDAMAGE(),
                    ModTiers.RUBY.getENCHANTMENTVALUE(),
                    BlockTags.MINEABLE_WITH_HOE,
                    () -> Ingredient.of(Sundries_item.RUBY.get())
            );


}
