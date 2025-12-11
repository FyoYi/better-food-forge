package com.fyoyi.betterfood;

import com.fyoyi.betterfood.creative_tab.ModCreativeModeTabs;
import com.fyoyi.betterfood.item.ModItems;
import com.fyoyi.betterfood.util.FreshnessHelper;
import com.fyoyi.betterfood.util.TimeManager;
import com.fyoyi.betterfood.config.FoodConfig;
import net.minecraft.network.chat.Component;
import net.minecraft.ChatFormatting;
import net.minecraft.world.item.Item;
import net.minecraft.resources.ResourceLocation;
import com.fyoyi.betterfood.item.weapon.bow.Bow_item;
import com.mojang.logging.LogUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterItemDecorationsEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import com.fyoyi.betterfood.entity.ModEntities;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.ForgeRegistries;
import com.fyoyi.betterfood.recipe.ModRecipes;
import org.slf4j.Logger;

@Mod(better_food.MOD_ID)
public class better_food
{
    public static final String MOD_ID = "better_food";
    private static final Logger LOGGER = LogUtils.getLogger();

    public better_food(FMLJavaModLoadingContext context)
    {
        IEventBus modEventBus = context.getModEventBus();

        modEventBus.addListener(this::commonSetup);

        ModItems.init();
        ModItems.register(modEventBus);
        ModRecipes.register(modEventBus);
        ModEntities.register(modEventBus);
        ModCreativeModeTabs.register(modEventBus);

        MinecraftForge.EVENT_BUS.register(this);
        modEventBus.addListener(this::addCreative);

        // 手动注册装饰器
        modEventBus.addListener(ClientModEvents::registerItemDecorations);

        context.registerConfig(ModConfig.Type.COMMON, Config.SPEC);
    }

    private void commonSetup(final FMLCommonSetupEvent event)
    {
        LOGGER.info("HELLO FROM COMMON SETUP");
        if (Config.logDirtBlock)
            LOGGER.info("DIRT BLOCK >> {}", ForgeRegistries.BLOCKS.getKey(Blocks.DIRT));
        LOGGER.info(Config.magicNumberIntroduction + Config.magicNumber);
        Config.items.forEach((item) -> LOGGER.info("ITEM >> {}", item.toString()));
    }

    private void addCreative(BuildCreativeModeTabContentsEvent event)
    {
    }

    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event)
    {
        LOGGER.info("HELLO from server starting");
    }

    // =================================================================
    // 客户端事件内部类
    // =================================================================
    @Mod.EventBusSubscriber(modid = MOD_ID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE)
    public static class ClientModEvents
    {
        @SubscribeEvent
        public static void onClientSetup(FMLClientSetupEvent event)
        {
            LOGGER.info("HELLO FROM CLIENT SETUP");
            event.enqueueWork(() -> {
                Item rubyInlaidBow = Bow_item.RUBY_INLAID_BOW.get();
                ItemProperties.register(rubyInlaidBow, ResourceLocation.fromNamespaceAndPath("minecraft", "pull"), (stack, world, entity, seed) -> {
                    if (entity == null) return 0.0F;
                    return entity.getUseItem() != stack ? 0.0F : (float)(stack.getUseDuration() - entity.getUseItemRemainingTicks()) / 20.0F;
                });
                ItemProperties.register(rubyInlaidBow, ResourceLocation.fromNamespaceAndPath("minecraft", "pulling"), (stack, world, entity, seed) -> {
                    return entity != null && entity.isUsingItem() && entity.getUseItem() == stack ? 1.0F : 0.0F;
                });
            });
        }

        // ========================================================
        // 功能 1：Tooltip (显示保质期文字) - 已修改支持全新物品
        // ========================================================
        @SubscribeEvent
        public static void onItemTooltip(net.minecraftforge.event.entity.player.ItemTooltipEvent event) {
            net.minecraft.world.item.ItemStack stack = event.getItemStack();

            // 只要腐烂开启，且是可腐烂食物，就显示！
            if (TimeManager.DECAY_ENABLED && FoodConfig.canRot(stack)) {

                net.minecraft.world.level.Level level = event.getEntity() != null ? event.getEntity().level() : null;
                if (level == null) return;

                // 1. 总是显示总保质期 (蓝色)
                long lifetime = FoodConfig.getItemLifetime(stack);
                String lifeStr = FreshnessHelper.formatDuration(lifetime);
                event.getToolTip().add(Component.literal("保质期: " + lifeStr).withStyle(ChatFormatting.BLUE));

                // 2. 计算剩余时间
                long remaining;

                // 尝试获取过期时间 (只读)
                long expiry = FreshnessHelper.getExpiryTime(level, stack, false);

                if (expiry == Long.MAX_VALUE) {
                    // 情况 A: 物品是全新的 (没有NBT标签)
                    // 剩余时间 = 总寿命
                    remaining = lifetime;
                } else {
                    // 情况 B: 物品已经开始腐烂
                    long now = TimeManager.getEffectiveTime(level);
                    remaining = Math.max(0, expiry - now);
                }

                // 3. 显示距离腐烂
                String remainStr = FreshnessHelper.formatDuration(remaining);

                // 动态变色 (绿 -> 黄 -> 红)
                ChatFormatting color;
                float percent = (float) remaining / lifetime;

                if (percent > 0.5f) color = ChatFormatting.GREEN;
                else if (percent > 0.2f) color = ChatFormatting.YELLOW;
                else color = ChatFormatting.RED;

                event.getToolTip().add(Component.literal("距离腐烂: " + remainStr).withStyle(color));
            }
        }

        // ========================================================
        // 功能 2：渲染耐久条
        // ========================================================
        public static void registerItemDecorations(RegisterItemDecorationsEvent event) {
            System.out.println(">>> BetterFood DEBUG: 装饰器正在注册！ <<<");

            for (Item item : ForgeRegistries.ITEMS) {
                if (FoodConfig.canRot(item.getDefaultInstance())) {

                    event.register(item, (graphics, font, stack, x, y) -> {
                        if (Minecraft.getInstance().level == null) return false;

                        // 计算百分比
                        float percent = FreshnessHelper.getFreshnessPercentage(Minecraft.getInstance().level, stack);

                        // 只有不满的时候显示条 (percent < 1.0)
                        // 如果你是全新物品，这里 percent 是 1.0，所以不显示条，只显示文字，这是对的。
                        if (percent < 1.0F) {
                            int barWidth = Math.round(13.0F * percent);
                            int color = java.awt.Color.HSBtoRGB(percent / 3.0F, 1.0F, 1.0F);

                            graphics.pose().pushPose();
                            graphics.pose().translate(0, 0, 200);

                            graphics.fill(x + 2, y + 13, x + 15, y + 15, 0xFF000000);
                            graphics.fill(x + 2, y + 13, x + 2 + barWidth, y + 14, color | 0xFF000000);

                            graphics.pose().popPose();
                            return true;
                        }
                        return false;
                    });
                }
            }
        }
    }
}
