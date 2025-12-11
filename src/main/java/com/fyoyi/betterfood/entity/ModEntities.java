package com.fyoyi.betterfood.entity;

import com.fyoyi.betterfood.better_food;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModEntities {
    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES =
            DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, better_food.MOD_ID);

    public static final RegistryObject<EntityType<FireworkArrowEntity>> FIREWORK_ARROW =
            ENTITY_TYPES.register("firework_arrow",
                    () -> EntityType.Builder.<FireworkArrowEntity>of(FireworkArrowEntity::new, MobCategory.MISC)
                            .sized(0.5F, 0.5F) // 定义实体碰撞箱大小
                            .clientTrackingRange(4) // 定义客户端追踪范围
                            .updateInterval(20) // 定义更新频率
                            .build("firework_arrow"));

    public static void register(IEventBus eventBus) {
        ENTITY_TYPES.register(eventBus);
    }
}