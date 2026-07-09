package com.visomod.selection;

import net.minecraft.core.BlockPos;

public class SelectionManager {
    private static final SelectionManager INSTANCE = new SelectionManager();

    private BlockPos posA = null;
    private BlockPos posB = null;

    private SelectionManager() {}

    public static SelectionManager getInstance() {
        return INSTANCE;
    }

    public void setPosA(BlockPos pos) {
        this.posA = pos.immutable();
    }

    public void setPosB(BlockPos pos) {
        this.posB = pos.immutable();
    }

    public BlockPos getPosA() {
        return posA;
    }

    public BlockPos getPosB() {
        return posB;
    }

    public boolean hasCompleteSelection() {
        return posA != null && posB != null;
    }

    public BlockPos getMinPos() {
        if (!hasCompleteSelection()) return null;
        return new BlockPos(
                Math.min(posA.getX(), posB.getX()),
                Math.min(posA.getY(), posB.getY()),
                Math.min(posA.getZ(), posB.getZ())
        );
    }

    public BlockPos getMaxPos() {
        if (!hasCompleteSelection()) return null;
        return new BlockPos(
                Math.max(posA.getX(), posB.getX()),
                Math.max(posA.getY(), posB.getY()),
                Math.max(posA.getZ(), posB.getZ())
        );
    }

    public int[] getDimensions() {
        if (!hasCompleteSelection()) return new int[]{0, 0, 0};
        BlockPos min = getMinPos();
        BlockPos max = getMaxPos();
        return new int[]{
                max.getX() - min.getX() + 1,
                max.getY() - min.getY() + 1,
                max.getZ() - min.getZ() + 1
        };
    }

    public void clearSelection() {
        posA = null;
        posB = null;
    }
}
