package com.visomod.item;

import com.visomod.selection.SelectionManager;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;

public class ExportWandItem extends Item {

    public ExportWandItem(Settings settings) {
        super(settings);
    }

    @Override
    public ActionResult useOnBlock(ItemUsageContext context) {
        PlayerEntity player = context.getPlayer();
        if (player != null && !context.getWorld().isClient) {
            BlockPos pos = context.getBlockPos();
            SelectionManager.getInstance().setPosB(pos);

            int[] dims = SelectionManager.getInstance().getDimensions();
            player.sendMessage(Text.literal("[VisoMod] ")
                    .formatted(Formatting.GOLD)
                    .append(Text.literal("Ponto B selecionado em (" + pos.getX() + ", " + pos.getY() + ", " + pos.getZ() + ")")
                            .formatted(Formatting.YELLOW)), false);

            if (SelectionManager.getInstance().hasCompleteSelection()) {
                player.sendMessage(Text.literal("[VisoMod] Região completa: " + dims[0] + "×" + dims[1] + "×" + dims[2] + " blocos.")
                        .formatted(Formatting.GREEN), false);
            }
        }
        return ActionResult.SUCCESS;
    }

    public static ActionResult onAttackBlock(PlayerEntity player, BlockPos pos) {
        if (player != null && player.getMainHandStack().getItem() instanceof ExportWandItem) {
            if (!player.getWorld().isClient) {
                SelectionManager.getInstance().setPosA(pos);

                int[] dims = SelectionManager.getInstance().getDimensions();
                player.sendMessage(Text.literal("[VisoMod] ")
                        .formatted(Formatting.GOLD)
                        .append(Text.literal("Ponto A selecionado em (" + pos.getX() + ", " + pos.getY() + ", " + pos.getZ() + ")")
                                .formatted(Formatting.AQUA)), false);

                if (SelectionManager.getInstance().hasCompleteSelection()) {
                    player.sendMessage(Text.literal("[VisoMod] Região completa: " + dims[0] + "×" + dims[1] + "×" + dims[2] + " blocos.")
                            .formatted(Formatting.GREEN), false);
                }
            }
            return ActionResult.SUCCESS;
        }
        return ActionResult.PASS;
    }
}
