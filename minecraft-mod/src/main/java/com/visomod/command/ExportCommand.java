package com.visomod.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.visomod.export.StructureExporter;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;

import java.io.File;

public class ExportCommand {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("exportar")
                .requires(source -> true)
                // /exportar <nome_arquivo> (uses wand selection)
                .then(Commands.argument("nome_arquivo", StringArgumentType.word())
                        .executes(ExportCommand::executeExportSelection))
                // /exportar <pos1> <pos2> <nome_arquivo> (uses specified coords)
                .then(Commands.argument("pos1", BlockPosArgument.blockPos())
                        .then(Commands.argument("pos2", BlockPosArgument.blockPos())
                                .then(Commands.argument("nome_arquivo_coord", StringArgumentType.word())
                                        .executes(ExportCommand::executeExportCoords))))
        );
    }

    private static int executeExportSelection(CommandContext<CommandSourceStack> context) {
        String fileName = StringArgumentType.getString(context, "nome_arquivo");
        try {
            StructureExporter.ExportResult result = StructureExporter.exportSelection(
                    Minecraft.getInstance().level, fileName
            );
            sendSuccessMessage(context.getSource(), result);
        } catch (Exception e) {
            sendErrorMessage(context.getSource(), e);
        }
        return 1;
    }

    private static int executeExportCoords(CommandContext<CommandSourceStack> context) {
        String fileName = StringArgumentType.getString(context, "nome_arquivo_coord");
        try {
            BlockPos pos1 = BlockPosArgument.getLoadedBlockPos(context, "pos1");
            BlockPos pos2 = BlockPosArgument.getLoadedBlockPos(context, "pos2");

            BlockPos min = new BlockPos(
                    Math.min(pos1.getX(), pos2.getX()),
                    Math.min(pos1.getY(), pos2.getY()),
                    Math.min(pos1.getZ(), pos2.getZ())
            );
            BlockPos max = new BlockPos(
                    Math.max(pos1.getX(), pos2.getX()),
                    Math.max(pos1.getY(), pos2.getY()),
                    Math.max(pos1.getZ(), pos2.getZ())
            );

            StructureExporter.ExportResult result = StructureExporter.exportSelection(
                    Minecraft.getInstance().level, fileName, min, max
            );
            sendSuccessMessage(context.getSource(), result);
        } catch (Exception e) {
            sendErrorMessage(context.getSource(), e);
        }
        return 1;
    }

    private static void sendSuccessMessage(CommandSourceStack source, StructureExporter.ExportResult result) {
        File htmlFile = result.htmlFile;
        ClickEvent clickEvent = new ClickEvent.OpenFile(htmlFile.getAbsolutePath());

        Component fileLink = Component.literal("[Clique para abrir no navegador]")
                .withStyle(style -> style
                        .withColor(ChatFormatting.AQUA)
                        .withUnderlined(true)
                        .withClickEvent(clickEvent));

        Component message = Component.literal("[VisoMod] ")
                .withStyle(ChatFormatting.GOLD)
                .append(Component.literal("Estrutura exportada com sucesso! ")
                        .withStyle(ChatFormatting.GREEN))
                .append(Component.literal(result.totalBlocks + " blocos -> ")
                        .withStyle(ChatFormatting.YELLOW))
                .append(fileLink);
        
        Minecraft.getInstance().player.sendSystemMessage(message);
    }

    private static void sendErrorMessage(CommandSourceStack source, Exception e) {
        Component message = Component.literal("[Erro VisoMod] Falha na exportação: " + e.getMessage())
                .withStyle(ChatFormatting.RED);
        Minecraft.getInstance().player.sendSystemMessage(message);
        e.printStackTrace();
    }
}
