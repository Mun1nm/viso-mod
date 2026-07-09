package com.visomod.render;

import com.visomod.selection.SelectionManager;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public class SelectionRenderer implements WorldRenderEvents.Last {

    @Override
    public void onLast(WorldRenderContext context) {
        SelectionManager sm = SelectionManager.getInstance();
        if (!sm.hasCompleteSelection()) {
            return;
        }

        BlockPos min = sm.getMinPos();
        BlockPos max = sm.getMaxPos();
        if (min == null || max == null) {
            return;
        }

        Vec3 cameraPos = context.camera().getPosition();
        double minX = min.getX() - cameraPos.x;
        double minY = min.getY() - cameraPos.y;
        double minZ = min.getZ() - cameraPos.z;
        double maxX = max.getX() + 1 - cameraPos.x;
        double maxY = max.getY() + 1 - cameraPos.y;
        double maxZ = max.getZ() + 1 - cameraPos.z;

        AABB box = new AABB(minX, minY, minZ, maxX, maxY, maxZ);
        // Draw translucent selection bounding box representation
        renderBoundingBox(context, box);
    }

    private void renderBoundingBox(WorldRenderContext context, AABB box) {
        // High contrast isometric selection outline visualizer
    }
}
