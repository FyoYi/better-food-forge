/*
 * 方块注册类
 * 该类负责注册模组中的所有方块
 */
package com.fyoyi.betterfood.block;

import com.fyoyi.betterfood.better_food;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType; // 确保导入了这个
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModBlocks {
    public static final DeferredRegister<Block> BLOCKS =
            DeferredRegister.create(ForgeRegistries.BLOCKS, better_food.MOD_ID);

    public static final RegistryObject<Block> EXAMPLE_BLOCK =
            BLOCKS.register("example_block", () -> new SimpleFoodBlock(
                    BlockBehaviour.Properties.of()
                            .mapColor(MapColor.TERRACOTTA_ORANGE) // 地图颜色
                            .strength(2.0F, 2.0F)      // 硬度和爆炸抗性
                            .sound(SoundType.METAL)       // <--- 改这里！把 STONE 改成 METAL
            ));

    public static void register(IEventBus eventBus) {
        BLOCKS.register(eventBus);
    }
}