package com.visomod.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.visomod.export.StructureExporter;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;

public class ExportCommand {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("exportar")
                // In Minecraft 26.2, hasPermission/hasPermissionLevel no longer exist.
                // Use .requires(source -> true) to allow all players.
                .requires(source -> true)
                .then(Commands.argument("nome_arquivo", StringArgumentType.word())
                        .executes(ExportCommand::executeExport)));
    }

    private static int executeExport(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();
        String fileName = StringArgumentType.getString(context, "nome_arquivo");

        try {
            StructureExporter.ExportResult result = StructureExporter.exportSelection(
                    source.getLevel(), fileName
            );

            source.sendSuccess(() -> Component.literal("[VisoMod] ")
                    .withStyle(ChatFormatting.GOLD)
                    .append(Component.literal("Estrutura exportada com sucesso! ")
                            .withStyle(ChatFormatting.GREEN))
                    .append(Component.literal(result.totalBlocks + " blocos -> ")
                            .withStyle(ChatFormatting.YELLOW))
                    .append(Component.literal(result.htmlFile.getName() + " (HTML Standalone)")
                            .withStyle(ChatFormatting.AQUA)), false);

            return 1;
        } catch (Exception e) {
            source.sendFailure(Component.literal("[VisoMod] Erro na exportação: " + e.getMessage()));
            return 0;
        }
    }
}
