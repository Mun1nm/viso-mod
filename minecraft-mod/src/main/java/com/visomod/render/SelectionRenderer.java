package com.visomod.render;

import com.visomod.selection.SelectionManager;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/**
 * Renders the selected region bounding box in the game world.
 * In Fabric API 0.154.2+26.2, WorldRenderEvents was replaced by LevelRenderEvents.
 * WorldRenderContext was replaced by LevelRenderContext.
 */
public class SelectionRenderer implements LevelRenderEvents.EndMain {

    @Override
    public void endMain(LevelRenderContext context) {
        SelectionManager sm = SelectionManager.getInstance();
        if (!sm.hasCompleteSelection()) {
            return;
        }

        BlockPos min = sm.getMinPos();
        BlockPos max = sm.getMaxPos();
        if (min == null || max == null) {
            return;
        }

        // Get camera position via Minecraft instance (LevelRenderContext doesn't expose camera directly)
        Vec3 cameraPos = Minecraft.getInstance().gameRenderer.mainCamera().position();
        double minX = min.getX() - cameraPos.x;
        double minY = min.getY() - cameraPos.y;
        double minZ = min.getZ() - cameraPos.z;
        double maxX = max.getX() + 1 - cameraPos.x;
        double maxY = max.getY() + 1 - cameraPos.y;
        double maxZ = max.getZ() + 1 - cameraPos.z;

        AABB box = new AABB(minX, minY, minZ, maxX, maxY, maxZ);
        renderBoundingBox(context, box);
    }

    private void renderBoundingBox(LevelRenderContext context, AABB box) {
        // Selection bounding box outline visualizer (placeholder for future rendering)
    }
}
