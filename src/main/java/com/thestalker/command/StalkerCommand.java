package com.thestalker.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.thestalker.events.StalkerEvents;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

public class StalkerCommand {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher, StalkerEvents stalkerEvents) {
        LiteralArgumentBuilder<CommandSourceStack> cmd = Commands.literal("stalker")
                .requires(src -> src.hasPermission(2));

        cmd.then(Commands.literal("trigger")
                .then(Commands.literal("close_stalk").executes(ctx -> {
                    CommandSourceStack src = ctx.getSource();
                    ServerPlayer player = src.getPlayerOrException();
                    stalkerEvents.triggerCloseStalk(player, src.getLevel());
                    src.sendSuccess(() -> Component.literal("[Stalker] Triggered: close_stalk"), true);
                    return 1;
                }))
                .then(Commands.literal("distant_stalk").executes(ctx -> {
                    CommandSourceStack src = ctx.getSource();
                    ServerPlayer player = src.getPlayerOrException();
                    stalkerEvents.triggerDistantStalk(player, src.getLevel());
                    src.sendSuccess(() -> Component.literal("[Stalker] Triggered: distant_stalk"), true);
                    return 1;
                }))
                .then(Commands.literal("behind_scare").executes(ctx -> {
                    CommandSourceStack src = ctx.getSource();
                    ServerPlayer player = src.getPlayerOrException();
                    stalkerEvents.triggerBehindScare(player, src.getLevel());
                    src.sendSuccess(() -> Component.literal("[Stalker] Triggered: behind_scare"), true);
                    return 1;
                }))
                .then(Commands.literal("chase").executes(ctx -> {
                    CommandSourceStack src = ctx.getSource();
                    ServerPlayer player = src.getPlayerOrException();
                    stalkerEvents.triggerChase(player, src.getLevel());
                    src.sendSuccess(() -> Component.literal("[Stalker] Triggered: chase"), true);
                    return 1;
                }))
                .then(Commands.literal("break_event").executes(ctx -> {
                    CommandSourceStack src = ctx.getSource();
                    ServerPlayer player = src.getPlayerOrException();
                    stalkerEvents.triggerBreakEvent(player, src.getLevel());
                    src.sendSuccess(() -> Component.literal("[Stalker] Triggered: break_event"), true);
                    return 1;
                })));

        dispatcher.register(cmd);
    }
}
