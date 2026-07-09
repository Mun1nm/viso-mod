package com.visomod.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.visomod.export.StructureExporter;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public class ExportCommand {

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(CommandManager.literal("exportar")
                .requires(source -> source.hasPermissionLevel(0)) // Allow players to export their selection
                .then(CommandManager.argument("nome_arquivo", StringArgumentType.word())
                        .executes(ExportCommand::executeExport)));
    }

    private static int executeExport(CommandContext<ServerCommandSource> context) {
        ServerCommandSource source = context.getSource();
        String fileName = StringArgumentType.getString(context, "nome_arquivo");

        try {
            StructureExporter.ExportResult result = StructureExporter.exportSelection(
                    source.getWorld(), fileName
            );

            source.sendFeedback(() -> Text.literal("[VisoMod] ")
                    .formatted(Formatting.GOLD)
                    .append(Text.literal("Estrutura exportada com sucesso! ")
                            .formatted(Formatting.GREEN))
                    .append(Text.literal(result.totalBlocks + " blocos (" + result.paletteSize + " tipos) -> " +
                            result.gzipFile.getName()).formatted(Formatting.AQUA)), false);

            return 1;
        } catch (Exception e) {
            source.sendError(Text.literal("[VisoMod] Erro na exportação: " + e.getMessage()));
            return 0;
        }
    }
}
