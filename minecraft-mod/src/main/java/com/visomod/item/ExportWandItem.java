package com.visomod.item;

import com.visomod.selection.SelectionManager;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.UseOnContext;

public class ExportWandItem extends Item {

    public ExportWandItem(Properties properties) {
        super(properties);
    }

    private static long lastAttackTime = 0;
    private static BlockPos lastAttackPos = null;
    private static long lastUseTime = 0;
    private static BlockPos lastUsePos = null;

    @Override
    public InteractionResult useOn(UseOnContext context) {
        if (context.getHand() != InteractionHand.MAIN_HAND) {
            return InteractionResult.PASS;
        }

        Player player = context.getPlayer();
        if (player != null && !context.getLevel().isClientSide()) {
            BlockPos pos = context.getClickedPos();
            long now = System.currentTimeMillis();
            if (lastUsePos != null && lastUsePos.equals(pos) && (now - lastUseTime) < 250) {
                return InteractionResult.SUCCESS;
            }
            lastUseTime = now;
            lastUsePos = pos;

            SelectionManager.getInstance().setPosB(pos);

            int[] dims = SelectionManager.getInstance().getDimensions();
            sendMsg(player, Component.literal("[VisoMod] ")
                    .withStyle(ChatFormatting.GOLD)
                    .append(Component.translatable("message.visomod.point_b_selected", pos.getX(), pos.getY(), pos.getZ())
                            .withStyle(ChatFormatting.YELLOW)));

            if (SelectionManager.getInstance().hasCompleteSelection()) {
                sendMsg(player, Component.literal("[VisoMod] ")
                        .withStyle(ChatFormatting.GOLD)
                        .append(Component.translatable("message.visomod.region_complete", dims[0], dims[1], dims[2])
                        .withStyle(ChatFormatting.GREEN)));
            }
        }
        return InteractionResult.SUCCESS;
    }

    public static InteractionResult onAttackBlock(Player player, InteractionHand hand, BlockPos pos) {
        if (hand != InteractionHand.MAIN_HAND) {
            return InteractionResult.PASS;
        }

        if (player != null && player.getItemInHand(InteractionHand.MAIN_HAND).getItem() instanceof ExportWandItem) {
            if (!player.level().isClientSide()) {
                long now = System.currentTimeMillis();
                if (lastAttackPos != null && lastAttackPos.equals(pos) && (now - lastAttackTime) < 250) {
                    return InteractionResult.SUCCESS;
                }
                lastAttackTime = now;
                lastAttackPos = pos;

                SelectionManager.getInstance().setPosA(pos);

                int[] dims = SelectionManager.getInstance().getDimensions();
                sendMsg(player, Component.literal("[VisoMod] ")
                        .withStyle(ChatFormatting.GOLD)
                        .append(Component.translatable("message.visomod.point_a_selected", pos.getX(), pos.getY(), pos.getZ())
                                .withStyle(ChatFormatting.AQUA)));

                if (SelectionManager.getInstance().hasCompleteSelection()) {
                    sendMsg(player, Component.literal("[VisoMod] ")
                            .withStyle(ChatFormatting.GOLD)
                            .append(Component.translatable("message.visomod.region_complete", dims[0], dims[1], dims[2])
                            .withStyle(ChatFormatting.GREEN)));
                }
            }
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.PASS;
    }

    /**
     * Sends a chat message to the player.
     * On the server side, Player is usually a ServerPlayer, which has sendSystemMessage.
     * displayClientMessage is only available on LocalPlayer (client-side).
     */
    private static void sendMsg(Player player, Component msg) {
        if (player instanceof ServerPlayer sp) {
            sp.sendSystemMessage(msg);
        }
    }
}
