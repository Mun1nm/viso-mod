package com.visomod.export;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.resources.Identifier;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class EntityModelExtractor {

    public static List<ExportData.QuadData> extractEntity(BlockEntity blockEntity, BlockState state, Map<String, String> texturesMap) {
        List<ExportData.QuadData> quads = new ArrayList<>();
        if (blockEntity == null) return quads;

        try {
            @SuppressWarnings("unchecked")
            BlockEntityRenderer<BlockEntity> renderer = (BlockEntityRenderer<BlockEntity>) Minecraft.getInstance().getBlockEntityRenderDispatcher().getRenderer(blockEntity);
            if (renderer != null) {
                PoseStack poseStack = new PoseStack();
                // O renderizador de entidade assume a origem no bloco
                FakeBufferSource fakeBufferSource = new FakeBufferSource(quads, texturesMap);
                
                renderer.render(blockEntity, 0.0f, poseStack, fakeBufferSource, 15728880, 655360);
                
                fakeBufferSource.finish();
            }
        } catch (Exception e) {
            com.visomod.VisoMod.LOGGER.error("Failed to extract entity model for " + state.getBlock().getName().getString(), e);
        }

        return quads;
    }

    private static class FakeBufferSource implements MultiBufferSource {
        private final List<ExportData.QuadData> quadsOut;
        private final Map<String, String> texturesMap;
        private FakeVertexConsumer currentConsumer;

        public FakeBufferSource(List<ExportData.QuadData> quadsOut, Map<String, String> texturesMap) {
            this.quadsOut = quadsOut;
            this.texturesMap = texturesMap;
        }

        @Override
        public VertexConsumer getBuffer(RenderType renderType) {
            if (currentConsumer != null) {
                currentConsumer.finish();
            }
            
            String texName = "default";
            String rtStr = renderType.toString();
            
            // Regex para extrair ResourceLocation/Identifier (ex: minecraft:textures/entity/chest/normal.png)
            Pattern p = Pattern.compile("Optional\\[(.*?)\\]");
            Matcher m = p.matcher(rtStr);
            if (m.find()) {
                texName = m.group(1);
            } else {
                Pattern p2 = Pattern.compile("texture\\[(.*?)\\]");
                Matcher m2 = p2.matcher(rtStr);
                if (m2.find()) {
                    texName = m2.group(1);
                }
            }
            
            if (!texName.equals("default") && !texturesMap.containsKey(texName)) {
                try {
                    Identifier texId = Identifier.tryParse(texName);
                    if (texId != null) {
                        java.util.Optional<net.minecraft.server.packs.resources.Resource> res = Minecraft.getInstance().getResourceManager().getResource(texId);
                        if (res.isPresent()) {
                            java.io.InputStream is = res.get().open();
                            java.io.ByteArrayOutputStream buffer = new java.io.ByteArrayOutputStream();
                            int nRead;
                            byte[] data = new byte[16384];
                            while ((nRead = is.read(data, 0, data.length)) != -1) {
                                buffer.write(data, 0, nRead);
                            }
                            is.close();
                            String base64 = java.util.Base64.getEncoder().encodeToString(buffer.toByteArray());
                            texturesMap.put(texName, "data:image/png;base64," + base64);
                        }
                    }
                } catch (Exception e) {
                    com.visomod.VisoMod.LOGGER.error("Failed to load entity texture: " + texName, e);
                }
            }

            currentConsumer = new FakeVertexConsumer(quadsOut, texName);
            return currentConsumer;
        }

        public void finish() {
            if (currentConsumer != null) {
                currentConsumer.finish();
                currentConsumer = null;
            }
        }
    }

    private static class FakeVertexConsumer implements VertexConsumer {
        private final List<ExportData.QuadData> quadsOut;
        private final String texName;
        
        private float[] currentPositions = new float[12];
        private float[] currentUVs = new float[8];
        private int vertexCount = 0;

        public FakeVertexConsumer(List<ExportData.QuadData> quadsOut, String texName) {
            this.quadsOut = quadsOut;
            this.texName = texName;
        }

        @Override
        public VertexConsumer vertex(float x, float y, float z) {
            if (vertexCount < 4) {
                currentPositions[vertexCount * 3] = x;
                currentPositions[vertexCount * 3 + 1] = y;
                currentPositions[vertexCount * 3 + 2] = z;
            }
            return this;
        }

        @Override
        public VertexConsumer color(int r, int g, int b, int a) {
            return this;
        }

        @Override
        public VertexConsumer uv(float u, float v) {
            if (vertexCount < 4) {
                currentUVs[vertexCount * 2] = u;
                currentUVs[vertexCount * 2 + 1] = v;
            }
            return this;
        }

        @Override
        public VertexConsumer overlayCoords(int u, int v) {
            return this;
        }

        @Override
        public VertexConsumer uv2(int u, int v) {
            return this;
        }

        @Override
        public VertexConsumer normal(float x, float y, float z) {
            return this;
        }
        
        @Override
        public void endVertex() {
            vertexCount++;
            if (vertexCount == 4) {
                quadsOut.add(new ExportData.QuadData(currentPositions.clone(), currentUVs.clone(), texName, -1));
                vertexCount = 0;
            }
        }

        public void finish() {
        }
    }
}
