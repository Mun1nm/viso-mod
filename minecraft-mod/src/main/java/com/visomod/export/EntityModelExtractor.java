package com.visomod.export;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.OrderedSubmitNodeCollector;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.Vec3;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.world.level.block.state.BlockState;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Base64;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class EntityModelExtractor {

    public static List<ExportData.QuadData> extractEntity(BlockEntity blockEntity, BlockState state, Map<String, String> texturesMap) {
        List<ExportData.QuadData> quads = new ArrayList<>();
        if (blockEntity == null) return quads;

        try {
            @SuppressWarnings("unchecked")
            BlockEntityRenderer<BlockEntity, BlockEntityRenderState> renderer = (BlockEntityRenderer<BlockEntity, BlockEntityRenderState>) Minecraft.getInstance().getBlockEntityRenderDispatcher().getRenderer(blockEntity);
            if (renderer != null) {
                BlockEntityRenderState renderState = renderer.createRenderState();
                renderer.extractRenderState(blockEntity, renderState, 0.0f, Vec3.ZERO, null);
                
                FakeSubmitNodeCollector collector = new FakeSubmitNodeCollector(quads, texturesMap);
                renderer.submit(renderState, new PoseStack(), collector, null);
                
                collector.finish();
            }
        } catch (Exception e) {
            com.visomod.VisoMod.LOGGER.error("Failed to extract entity model for " + state.getBlock().getName().getString(), e);
        }

        return quads;
    }

    private static class FakeSubmitNodeCollector implements SubmitNodeCollector {
        private final List<ExportData.QuadData> quadsOut;
        private final Map<String, String> texturesMap;
        private FakeVertexConsumer currentConsumer;

        public FakeSubmitNodeCollector(List<ExportData.QuadData> quadsOut, Map<String, String> texturesMap) {
            this.quadsOut = quadsOut;
            this.texturesMap = texturesMap;
        }
        
        private VertexConsumer getBuffer(RenderType renderType, String fallbackTex) {
            if (currentConsumer != null) {
                currentConsumer.finish();
            }
            
            String texName = fallbackTex;
            if (renderType != null) {
                String rtStr = renderType.toString();
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

        @Override
        public OrderedSubmitNodeCollector order(int order) {
            return this;
        }

        @Override public void submitShadow(PoseStack poseStack, float f, java.util.List<net.minecraft.client.renderer.entity.state.EntityRenderState.ShadowPiece> list) {}
        @Override public void submitNameTag(PoseStack poseStack, net.minecraft.world.phys.Vec3 vec3, int i, net.minecraft.network.chat.Component component, boolean bl, int j, CameraRenderState cameraRenderState) {}
        @Override public void submitText(PoseStack poseStack, float f, float g, net.minecraft.util.FormattedCharSequence formattedCharSequence, boolean bl, net.minecraft.client.gui.Font.DisplayMode displayMode, int i, int j, int k, int l) {}
        @Override public void submitFlame(PoseStack poseStack, net.minecraft.client.renderer.entity.state.EntityRenderState entityRenderState, org.joml.Quaternionf quaternionf) {}
        @Override public void submitLeash(PoseStack poseStack, net.minecraft.client.renderer.entity.state.EntityRenderState.LeashState leashState) {}
        @Override public void submitMovingBlock(PoseStack poseStack, net.minecraft.client.renderer.block.MovingBlockRenderState movingBlockRenderState, int i) {}
        @Override public void submitBlockModel(PoseStack poseStack, RenderType renderType, java.util.List<net.minecraft.client.renderer.block.dispatch.BlockStateModelPart> list, int[] ints, int i, int j, int k) {}
        @Override public void submitBreakingBlockModel(PoseStack poseStack, java.util.List<net.minecraft.client.renderer.block.dispatch.BlockStateModelPart> list, int i) {}
        @Override public void submitShapeOutline(PoseStack poseStack, net.minecraft.world.phys.shapes.VoxelShape voxelShape, RenderType renderType, int i, float f, boolean bl) {}
        @Override public void submitItem(PoseStack poseStack, net.minecraft.world.item.ItemDisplayContext itemDisplayContext, int i, int j, int k, int[] ints, java.util.List<net.minecraft.client.resources.model.geometry.BakedQuad> list, net.minecraft.client.renderer.item.ItemStackRenderState.FoilType foilType) {}
        @Override public void submitCustomGeometry(PoseStack poseStack, RenderType renderType, SubmitNodeCollector.CustomGeometryRenderer customGeometryRenderer) {}
        @Override public void submitQuadParticleGroup(net.minecraft.client.renderer.state.level.QuadParticleRenderState quadParticleRenderState) {}
        @Override public void submitGizmoPrimitives(net.minecraft.client.renderer.gizmos.DrawableGizmoPrimitives.Group group, CameraRenderState cameraRenderState, boolean bl) {}

        @Override
        public <S> void submitModel(Model<? super S> model, S object, PoseStack poseStack, RenderType renderType, int i, int j, int k, TextureAtlasSprite textureAtlasSprite, int l, ModelFeatureRenderer.CrumblingOverlay crumblingOverlay) {
            String tex = textureAtlasSprite != null ? textureAtlasSprite.contents().name().toString() : "default";
            VertexConsumer buffer = getBuffer(renderType, tex);
            model.renderToBuffer(poseStack, buffer, i, j, k);
        }

        @Override
        public void submitModelPart(ModelPart part, PoseStack poseStack, RenderType renderType, int i, int j, TextureAtlasSprite textureAtlasSprite) {
            String tex = textureAtlasSprite != null ? textureAtlasSprite.contents().name().toString() : "default";
            VertexConsumer buffer = getBuffer(renderType, tex);
            part.render(poseStack, buffer, i, j, 0xFFFFFFFF);
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
        public VertexConsumer addVertex(float x, float y, float z) {
            if (vertexCount < 4) {
                currentPositions[vertexCount * 3] = x;
                currentPositions[vertexCount * 3 + 1] = y;
                currentPositions[vertexCount * 3 + 2] = z;
            }
            return this;
        }

        @Override
        public VertexConsumer setColor(int r, int g, int b, int a) {
            return this;
        }

        @Override
        public VertexConsumer setColor(int argb) {
            return this;
        }

        @Override
        public VertexConsumer setUv(float u, float v) {
            if (vertexCount < 4) {
                currentUVs[vertexCount * 2] = u;
                currentUVs[vertexCount * 2 + 1] = v;
            }
            return this;
        }

        @Override
        public VertexConsumer setUv1(int u, int v) {
            return this;
        }

        @Override
        public VertexConsumer setUv2(int u, int v) {
            return this;
        }

        @Override
        public VertexConsumer setNormal(float x, float y, float z) {
            vertexCount++;
            if (vertexCount == 4) {
                quadsOut.add(new ExportData.QuadData(currentPositions.clone(), currentUVs.clone(), texName, -1));
                vertexCount = 0;
            }
            return this;
        }

        @Override
        public VertexConsumer setLineWidth(float width) {
            return this;
        }

        public void finish() {
        }
    }
}
