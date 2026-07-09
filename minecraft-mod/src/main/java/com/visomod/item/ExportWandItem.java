package com.visomod.item;

import com.visomod.selection.SelectionManager;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.UseOnContext;

public class ExportWandItem extends Item {

    public ExportWandItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Player player = context.getPlayer();
        if (player != null && !context.getLevel().isClientSide()) {
            BlockPos pos = context.getClickedPos();
            SelectionManager.getInstance().setPosB(pos);

            int[] dims = SelectionManager.getInstance().getDimensions();
            player.displayClientMessage(Component.literal("[VisoMod] ")
                    .withStyle(ChatFormatting.GOLD)
                    .append(Component.literal("Ponto B selecionado em (" + pos.getX() + ", " + pos.getY() + ", " + pos.getZ() + ")")
                            .withStyle(ChatFormatting.YELLOW)), false);

            if (SelectionManager.getInstance().hasCompleteSelection()) {
                player.displayClientMessage(Component.literal("[VisoMod] Região completa: " + dims[0] + "×" + dims[1] + "×" + dims[2] + " blocos.")
                        .withStyle(ChatFormatting.GREEN), false);
            }
        }
        return InteractionResult.SUCCESS;
    }

    public static InteractionResult onAttackBlock(Player player, BlockPos pos) {
        if (player != null && player.getItemInHand(InteractionHand.MAIN_HAND).getItem() instanceof ExportWandItem) {
            if (!player.level().isClientSide()) {
                SelectionManager.getInstance().setPosA(pos);

                int[] dims = SelectionManager.getInstance().getDimensions();
                player.displayClientMessage(Component.literal("[VisoMod] ")
                        .withStyle(ChatFormatting.GOLD)
                        .append(Component.literal("Ponto A selecionado em (" + pos.getX() + ", " + pos.getY() + ", " + pos.getZ() + ")")
                                .withStyle(ChatFormatting.AQUA)), false);

                if (SelectionManager.getInstance().hasCompleteSelection()) {
                    player.displayClientMessage(Component.literal("[VisoMod] Região completa: " + dims[0] + "×" + dims[1] + "×" + dims[2] + " blocos.")
                            .withStyle(ChatFormatting.GREEN), false);
                }
            }
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.PASS;
    }
}
