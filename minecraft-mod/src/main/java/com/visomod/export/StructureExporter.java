package com.visomod.export;

import com.visomod.selection.SelectionManager;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

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

    public static ExportResult exportSelection(Level world, String fileName) throws IOException {
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
                dims,
                fileName
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

                    String blockId = BuiltInRegistries.BLOCK.getKey(state.getBlock()).toString();

                    StringBuilder stateKeyBuilder = new StringBuilder(blockId);
                    Map<String, String> properties = new HashMap<>();
                    for (net.minecraft.world.level.block.state.properties.Property<?> prop : state.getProperties()) {
                        String pName = prop.getName();
                        String pVal = state.getValue(prop).toString();
                        properties.put(pName, pVal);
                        stateKeyBuilder.append(";").append(pName).append("=").append(pVal);
                    }
                    String stateKey = stateKeyBuilder.toString();

                    int paletteId;
                    if (!blockToPaletteId.containsKey(stateKey)) {
                        paletteId = nextPaletteId++;
                        blockToPaletteId.put(stateKey, paletteId);
                        
                        String base64 = null;
                        try {
                            net.minecraft.client.renderer.texture.TextureAtlasSprite sprite = net.minecraft.client.Minecraft.getInstance().getModelManager().getBlockStateModelSet().getParticleMaterial(state).sprite();
                            if (sprite != null) {
                                net.minecraft.resources.Identifier spriteId = sprite.contents().name();
                                net.minecraft.resources.Identifier textureRes = net.minecraft.resources.Identifier.fromNamespaceAndPath(spriteId.getNamespace(), "textures/" + spriteId.getPath() + ".png");
                                
                                java.util.Optional<net.minecraft.server.packs.resources.Resource> resourceOpt = net.minecraft.client.Minecraft.getInstance().getResourceManager().getResource(textureRes);
                                if (resourceOpt.isPresent()) {
                                    try (java.io.InputStream is = resourceOpt.get().open()) {
                                        byte[] bytes = is.readAllBytes();
                                        base64 = "data:image/png;base64," + java.util.Base64.getEncoder().encodeToString(bytes);
                                    }
                                }
                            }
                        } catch (Exception e) {
                            com.visomod.VisoMod.LOGGER.error("Failed to extract texture for " + stateKey, e);
                        }

                        exportData.palette.put(String.valueOf(paletteId),
                                new ExportData.PaletteEntry(paletteId, blockId, properties, base64));
                    } else {
                        paletteId = blockToPaletteId.get(stateKey);
                    }

                    exportData.blocks.add(new ExportData.BlockEntry(
                            x - min.getX(),
                            y - min.getY(),
                            z - min.getZ(),
                            paletteId
                    ));
                }
            }
        }

        // Save raw JSON copy and compressed GZIP file in exports directory
        File exportsDir = new File("exports");
        if (!exportsDir.exists()) {
            exportsDir.mkdirs();
        }

        String rawJson = exportData.toJson(true);
        File rawJsonFile = new File(exportsDir, fileName + ".json");
        try (FileWriter writer = new FileWriter(rawJsonFile, StandardCharsets.UTF_8)) {
            writer.write(rawJson);
        }

        File gzipFile = new File(exportsDir, fileName + ".json.gz");
        try (FileOutputStream fos = new FileOutputStream(gzipFile);
             GZIPOutputStream gzos = new GZIPOutputStream(fos);
             OutputStreamWriter osw = new OutputStreamWriter(gzos, StandardCharsets.UTF_8)) {
            osw.write(exportData.toJson(false));
        }

        File htmlFile = HtmlExporter.generateStandaloneHtml(exportsDir, fileName, rawJson);

        return new ExportResult(rawJsonFile, gzipFile, htmlFile, exportData.blocks.size(), blockToPaletteId.size());
    }

    public static class ExportResult {
        public final File rawJsonFile;
        public final File gzipFile;
        public final File htmlFile;
        public final int totalBlocks;
        public final int paletteSize;

        public ExportResult(File rawJsonFile, File gzipFile, File htmlFile, int totalBlocks, int paletteSize) {
            this.rawJsonFile = rawJsonFile;
            this.gzipFile = gzipFile;
            this.htmlFile = htmlFile;
            this.totalBlocks = totalBlocks;
            this.paletteSize = paletteSize;
        }
    }
}
