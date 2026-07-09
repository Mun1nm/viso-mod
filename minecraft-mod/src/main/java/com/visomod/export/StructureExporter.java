package com.visomod.export;

import com.visomod.selection.SelectionManager;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.GZIPOutputStream;

public class StructureExporter {

    public static ExportResult exportSelection(World world, String fileName) throws IOException {
        SelectionManager sm = SelectionManager.getInstance();
        if (!sm.hasCompleteSelection()) {
            throw new IllegalStateException("Seleção incompleta. Defina os pontos A e B com a Varinha de Exportação.");
        }

        BlockPos min = sm.getMinPos();
        BlockPos max = sm.getMaxPos();
        int[] dims = sm.getDimensions();

        ExportData exportData = new ExportData(
                "26.2",
                new int[]{min.getX(), min.getY(), min.getZ()},
                new int[]{max.getX(), max.getY(), max.getZ()},
                dims
        );

        Map<String, Integer> blockToPaletteId = new HashMap<>();
        int nextPaletteId = 0;

        for (int y = min.getY(); y <= max.getY(); y++) {
            for (int z = min.getZ(); z <= max.getZ(); z++) {
                for (int x = min.getX(); x <= max.getX(); x++) {
                    BlockPos pos = new BlockPos(x, y, z);
                    BlockState state = world.getBlockState(pos);
                    if (state.isAir()) {
                        continue; // skip air for compactness
                    }

                    String blockId = state.getBlock().getTranslationKey().replace("block.", "").replace(".", ":");
                    if (!blockId.contains(":")) {
                        blockId = "minecraft:" + blockId;
                    }

                    int paletteId;
                    if (!blockToPaletteId.containsKey(blockId)) {
                        paletteId = nextPaletteId++;
                        blockToPaletteId.put(blockId, paletteId);
                        exportData.palette.put(String.valueOf(paletteId),
                                new ExportData.PaletteEntry(paletteId, blockId, new HashMap<>()));
                    } else {
                        paletteId = blockToPaletteId.get(blockId);
                    }

                    int relX = x - min.getX();
                    int relY = y - min.getY();
                    int relZ = z - min.getZ();

                    exportData.blocks.add(new ExportData.BlockEntry(relX, relY, relZ, paletteId));
                }
            }
        }

        File exportDir = new File("exports");
        if (!exportDir.exists()) {
            exportDir.mkdirs();
        }

        String baseName = fileName.endsWith(".json") || fileName.endsWith(".gz")
                ? fileName.replaceAll("\\.json(\\.gz)?$", "")
                : fileName;

        // 1. Save GZIP Compressed JSON (.json.gz)
        File gzipFile = new File(exportDir, baseName + ".json.gz");
        try (GZIPOutputStream gzipOut = new GZIPOutputStream(new FileOutputStream(gzipFile));
             OutputStreamWriter writer = new OutputStreamWriter(gzipOut, StandardCharsets.UTF_8)) {
            writer.write(exportData.toJson(false));
        }

        // 2. Save uncompressed JSON for developer inspection (.json)
        File jsonFile = new File(exportDir, baseName + ".json");
        try (FileWriter writer = new FileWriter(jsonFile, StandardCharsets.UTF_8)) {
            writer.write(exportData.toJson(true));
        }

        return new ExportResult(gzipFile, jsonFile, exportData.blocks.size(), exportData.palette.size());
    }

    public static class ExportResult {
        public File gzipFile;
        public File jsonFile;
        public int totalBlocks;
        public int paletteSize;

        public ExportResult(File gzipFile, File jsonFile, int totalBlocks, int paletteSize) {
            this.gzipFile = gzipFile;
            this.jsonFile = jsonFile;
            this.totalBlocks = totalBlocks;
            this.paletteSize = paletteSize;
        }
    }
}
