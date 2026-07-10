package com.visomod.export;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.GZIPOutputStream;

public class SampleExportGenerator {

    public static void main(String[] args) throws IOException {
        System.out.println("[VisoMod] Gerando arquivo de teste .json.gz...");

        int width = 12;
        int height = 8;
        int depth = 12;

        ExportData data = new ExportData("26.2",
                new int[]{0, 0, 0},
                new int[]{width - 1, height - 1, depth - 1},
                new int[]{width, height, depth},
                "sample_castle"
        );

        String[] paletteNames = {
                "minecraft:stone",
                "minecraft:grass_block",
                "minecraft:dirt",
                "minecraft:oak_log",
                "minecraft:oak_planks",
                "minecraft:glass",
                "minecraft:cobblestone"
        };

        Map<String, Integer> blockToId = new HashMap<>();
        for (int i = 0; i < paletteNames.length; i++) {
            blockToId.put(paletteNames[i], i);
            data.palette.put(String.valueOf(i), new ExportData.PaletteEntry(i, paletteNames[i], new HashMap<>(), null, null));
        }

        // Generate a sample house / castle structure
        for (int y = 0; y < height; y++) {
            for (int z = 0; z < depth; z++) {
                for (int x = 0; x < width; x++) {
                    String block = null;
                    if (y == 0) {
                        block = "minecraft:stone";
                    } else if (x == 0 || x == width - 1 || z == 0 || z == depth - 1) {
                        if (y < 4) {
                            block = "minecraft:cobblestone";
                        } else if (y == 4) {
                            block = "minecraft:oak_log";
                        }
                    } else if (y == 1 && (x + z) % 3 == 0) {
                        block = "minecraft:oak_planks";
                    }

                    if (block != null) {
                        int paletteId = blockToId.get(block);
                        data.blocks.add(new ExportData.BlockEntry(x, y, z, paletteId));
                    }
                }
            }
        }

        File outputDir = new File("sample_exports");
        outputDir.mkdirs();

        File gzFile = new File(outputDir, "test_castle.json.gz");
        try (GZIPOutputStream gzipOut = new GZIPOutputStream(new FileOutputStream(gzFile));
             OutputStreamWriter writer = new OutputStreamWriter(gzipOut, StandardCharsets.UTF_8)) {
            writer.write(data.toJson(false));
        }

        File jsonFile = new File(outputDir, "test_castle.json");
        try (FileWriter writer = new FileWriter(jsonFile, StandardCharsets.UTF_8)) {
            writer.write(data.toJson(true));
        }

        System.out.println("[VisoMod] Exportação de teste concluída:");
        System.out.println("  - Blocos gerados: " + data.blocks.size());
        System.out.println("  - Arquivo GZIP: " + gzFile.getAbsolutePath() + " (" + gzFile.length() + " bytes)");
        System.out.println("  - Arquivo JSON: " + jsonFile.getAbsolutePath() + " (" + jsonFile.length() + " bytes)");
    }
}
