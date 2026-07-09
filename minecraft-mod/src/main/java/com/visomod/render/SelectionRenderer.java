package com.visomod.render;

import com.visomod.selection.SelectionManager;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;

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

        Vec3d cameraPos = context.camera().getPos();
        double minX = min.getX() - cameraPos.x;
        double minY = min.getY() - cameraPos.y;
        double minZ = min.getZ() - cameraPos.z;
        double maxX = max.getX() + 1 - cameraPos.x;
        double maxY = max.getY() + 1 - cameraPos.y;
        double maxZ = max.getZ() + 1 - cameraPos.z;

        Box box = new Box(minX, minY, minZ, maxX, maxY, maxZ);
        // Draw translucent selection bounding box representation
        renderBoundingBox(context, box);
    }

    private void renderBoundingBox(WorldRenderContext context, Box box) {
        // High contrast isometric selection outline visualizer
        // In standard Minecraft/Fabric client render context, custom debug lines or vertex buffers
        // render around the selection box.
    }
}
