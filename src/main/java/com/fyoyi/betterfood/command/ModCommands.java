package com.fyoyi.betterfood.command;

import com.fyoyi.betterfood.util.TimeManager; // 引用新的类
import com.mojang.brigadier.arguments.BoolArgumentType;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = "better_food", bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ModCommands {

    @SubscribeEvent
    public static void onRegisterCommands(RegisterCommandsEvent event) {
        event.getDispatcher().register(
                Commands.literal("betterfood")
                        .then(Commands.literal("decay")
                                .then(Commands.argument("enable", BoolArgumentType.bool())
                                        .executes(context -> {
                                            boolean enable = BoolArgumentType.getBool(context, "enable");
                                            // 修改 TimeManager
                                            TimeManager.DECAY_ENABLED = enable;
                                            if (!enable) TimeManager.setPaused(false, context.getSource().getLevel()); // 重置暂停

                                            String status = enable ? "§a开启" : "§c关闭";
                                            context.getSource().sendSuccess(() -> Component.literal("[BetterFood] 腐烂系统已" + status), true);
                                            return 1;
                                        })
                                )
                        )
                        .then(Commands.literal("pause")
                                .then(Commands.argument("pause", BoolArgumentType.bool())
                                        .executes(context -> {
                                            boolean pause = BoolArgumentType.getBool(context, "pause");
                                            // 修改 TimeManager
                                            TimeManager.setPaused(pause, context.getSource().getLevel());

                                            String status = pause ? "§e暂停" : "§a继续";
                                            context.getSource().sendSuccess(() -> Component.literal("[BetterFood] 腐烂计时已" + status), true);
                                            return 1;
                                        })
                                )
                        )
        );
    }
}
