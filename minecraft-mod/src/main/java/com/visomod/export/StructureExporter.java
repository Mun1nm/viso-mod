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
        return exportSelection(world, fileName, sm.getMinPos(), sm.getMaxPos());
    }

    public static ExportResult exportSelection(Level world, String fileName, BlockPos min, BlockPos max) throws IOException {
        int[] dims = new int[]{
                max.getX() - min.getX() + 1,
                max.getY() - min.getY() + 1,
                max.getZ() - min.getZ() + 1
        };

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
                        
                        java.util.List<ExportData.QuadData> quads = new java.util.ArrayList<>();
                        Map<String, String> textures = new HashMap<>();
                        try {
                            net.minecraft.client.renderer.block.BlockStateModelSet modelSet = net.minecraft.client.Minecraft.getInstance().getModelManager().getBlockStateModelSet();
                            net.minecraft.client.renderer.block.dispatch.BlockStateModel stateModel = modelSet.get(state);
                            java.util.List<net.minecraft.client.renderer.block.dispatch.BlockStateModelPart> parts = new java.util.ArrayList<>();
                            stateModel.collectParts(net.minecraft.util.RandomSource.create(42), parts);

                            java.util.List<net.minecraft.core.Direction> dirs = new java.util.ArrayList<>();
                            dirs.add(null);
                            for (net.minecraft.core.Direction d : net.minecraft.core.Direction.values()) {
                                dirs.add(d);
                            }

                            for (net.minecraft.client.renderer.block.dispatch.BlockStateModelPart part : parts) {
                                for (net.minecraft.core.Direction d : dirs) {
                                    java.util.List<net.minecraft.client.resources.model.geometry.BakedQuad> bakedQuads = part.getQuads(d);
                                    for (net.minecraft.client.resources.model.geometry.BakedQuad q : bakedQuads) {
                                        float[] qPos = new float[12];
                                        float[] uv = new float[8];

                                        for (int i = 0; i < 4; i++) {
                                            org.joml.Vector3fc vertexPos = q.position(i);
                                            qPos[i * 3] = vertexPos.x();
                                            qPos[i * 3 + 1] = vertexPos.y();
                                            qPos[i * 3 + 2] = vertexPos.z();

                                            long packed = q.packedUV(i);
                                            float rawV = Float.intBitsToFloat((int) (packed & 0xFFFFFFFFL));
                                            float rawU = Float.intBitsToFloat((int) (packed >>> 32));
                                            uv[i * 2] = rawU;
                                            uv[i * 2 + 1] = rawV;

                                        }

                                        net.minecraft.client.renderer.texture.TextureAtlasSprite sprite = q.materialInfo() != null ? q.materialInfo().sprite() : null;
                                        String texName = "default";
                                        if (sprite != null) {
                                            net.minecraft.resources.Identifier spriteId = sprite.contents().name();
                                            texName = spriteId.toString();

                                            float u0 = sprite.getU0();
                                            float u1 = sprite.getU1();
                                            float v0 = sprite.getV0();
                                            float v1 = sprite.getV1();

                                            for (int i = 0; i < 4; i++) {
                                                float originalU = uv[i * 2];
                                                float originalV = uv[i * 2 + 1];
                                                if (u1 != u0) uv[i * 2] = (originalU - u0) / (u1 - u0);
                                                if (v1 != v0) uv[i * 2 + 1] = (originalV - v0) / (v1 - v0);
                                            }

                                            if (!textures.containsKey(texName)) {
                                                try {
                                                    Object contentsObj = sprite.contents();
                                                    java.lang.reflect.Field nativeImageArrayField = null;
                                                    for (java.lang.reflect.Field f : contentsObj.getClass().getDeclaredFields()) {
                                                        if (f.getType().isArray() && !f.getType().getComponentType().isPrimitive()) {
                                                            nativeImageArrayField = f;
                                                            break;
                                                        }
                                                    }
                                                    if (nativeImageArrayField != null) {
                                                        nativeImageArrayField.setAccessible(true);
                                                        Object[] mipLevels = (Object[]) nativeImageArrayField.get(contentsObj);
                                                        if (mipLevels != null && mipLevels.length > 0) {
                                                            Object nativeImage = mipLevels[0];
                                                            int width = (int) nativeImage.getClass().getMethod("getWidth").invoke(nativeImage);
                                                            int height = (int) nativeImage.getClass().getMethod("getHeight").invoke(nativeImage);
                                                            int[] pixels = (int[]) nativeImage.getClass().getMethod("getPixelsABGR").invoke(nativeImage);

                                                            java.awt.image.BufferedImage img = new java.awt.image.BufferedImage(width, height, java.awt.image.BufferedImage.TYPE_INT_ARGB);
                                                            for (int py = 0; py < height; py++) {
                                                                for (int px = 0; px < width; px++) {
                                                                    int abgr = pixels[py * width + px];
                                                                    int a = (abgr >> 24) & 0xFF;
                                                                    int b = (abgr >> 16) & 0xFF;
                                                                    int g = (abgr >> 8) & 0xFF;
                                                                    int r = abgr & 0xFF;
                                                                    int argb = (a << 24) | (r << 16) | (g << 8) | b;
                                                                    img.setRGB(px, py, argb);
                                                                }
                                                            }
                                                            java.io.ByteArrayOutputStream baos = new java.io.ByteArrayOutputStream();
                                                            javax.imageio.ImageIO.write(img, "png", baos);
                                                            byte[] bytes = baos.toByteArray();
                                                            String base64 = "data:image/png;base64," + java.util.Base64.getEncoder().encodeToString(bytes);
                                                            textures.put(texName, base64);
                                                        }
                                                    }
                                                } catch (Exception e) {
                                                    com.visomod.VisoMod.LOGGER.error("Failed to dump NativeImage for " + texName, e);
                                                }
                                            }
                                        }
                                        quads.add(new ExportData.QuadData(qPos, uv, texName));
                                    }
                                }
                            }
                        } catch (Exception e) {
                            com.visomod.VisoMod.LOGGER.error("Failed to extract BakedModel for " + stateKey, e);
                        }

                        ExportData.ModelData modelData = null;
                        if (!quads.isEmpty()) {
                            modelData = new ExportData.ModelData(textures, quads);
                        }

                        exportData.palette.put(String.valueOf(paletteId),
                                new ExportData.PaletteEntry(paletteId, blockId, properties, modelData, state.isSolidRender()));
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
