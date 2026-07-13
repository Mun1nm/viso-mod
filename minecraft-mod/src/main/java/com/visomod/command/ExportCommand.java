package com.visomod.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.visomod.export.StructureExporter;
import com.visomod.selection.SelectionManager;
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
        dispatcher.register(Commands.literal("export")
                .requires(source -> true)
                .then(Commands.argument("nome_arquivo", StringArgumentType.word())
                        .executes(context -> executeExportSelection(context, false)))
                .then(Commands.argument("pos1", BlockPosArgument.blockPos())
                        .then(Commands.argument("pos2", BlockPosArgument.blockPos())
                                .then(Commands.argument("nome_arquivo_coord", StringArgumentType.word())
                                        .executes(context -> executeExportCoords(context, false)))))
        );

        dispatcher.register(Commands.literal("exportdebug")
                .requires(source -> true)
                .then(Commands.argument("nome_arquivo", StringArgumentType.word())
                        .executes(context -> executeExportSelection(context, true)))
                .then(Commands.argument("pos1", BlockPosArgument.blockPos())
                        .then(Commands.argument("pos2", BlockPosArgument.blockPos())
                                .then(Commands.argument("nome_arquivo_coord", StringArgumentType.word())
                                        .executes(context -> executeExportCoords(context, true)))))
        );
    }

    private static int executeExportSelection(CommandContext<CommandSourceStack> context, boolean generateDebugJson) {
        String fileName = StringArgumentType.getString(context, "nome_arquivo");
        if (!SelectionManager.getInstance().hasCompleteSelection()) {
            Minecraft.getInstance().player.sendSystemMessage(
                    Component.translatable("command.visomod.prefix").withStyle(ChatFormatting.RED)
                    .append(Component.literal(" "))
                    .append(Component.translatable("command.visomod.export.error.incomplete").withStyle(ChatFormatting.RED))
            );
            return 0;
        }
        try {
            StructureExporter.ExportResult result = StructureExporter.exportSelection(
                    Minecraft.getInstance().level, fileName, generateDebugJson
            );
            sendSuccessMessage(context.getSource(), result);
        } catch (Exception e) {
            sendErrorMessage(context.getSource(), e);
        }
        return 1;
    }

    private static int executeExportCoords(CommandContext<CommandSourceStack> context, boolean generateDebugJson) {
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
                    Minecraft.getInstance().level, fileName, min, max, generateDebugJson
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

        Component fileLink = Component.translatable("command.visomod.export.open_browser")
                .withStyle(style -> style
                        .withColor(ChatFormatting.AQUA)
                        .withUnderlined(true)
                        .withClickEvent(clickEvent));

        Component message = Component.translatable("command.visomod.prefix").withStyle(ChatFormatting.GREEN)
                .append(Component.literal(" "))
                .append(Component.translatable("command.visomod.export.success")
                .withStyle(ChatFormatting.GREEN))
                .append(Component.literal("\n"))
                .append(Component.translatable("command.visomod.export.blocks", result.totalBlocks)
                .withStyle(ChatFormatting.YELLOW))
                .append(Component.literal("\n"))
                .append(fileLink);

        source.sendSuccess(() -> message, false);
    }

    private static void sendErrorMessage(CommandSourceStack source, Exception e) {
        net.minecraft.network.chat.MutableComponent errorMessage;
        if (e.getMessage() != null && e.getMessage().startsWith("command.visomod.")) {
            errorMessage = Component.translatable(e.getMessage());
        } else {
            errorMessage = Component.translatable("command.visomod.export.error", e.getMessage() != null ? e.getMessage() : "Unknown error");
        }
        
        Component message = Component.translatable("command.visomod.prefix").withStyle(ChatFormatting.RED)
                .append(Component.literal(" "))
                .append(errorMessage.withStyle(ChatFormatting.RED));
        source.sendFailure(message);
        e.printStackTrace();
    }
}
