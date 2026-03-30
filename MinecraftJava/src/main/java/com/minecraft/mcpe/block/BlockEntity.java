package com.minecraft.mcpe.block;

import com.minecraft.mcpe.nbt.CompoundTag;
import com.minecraft.mcpe.util.Vector3f;

/**
 * BlockEntity - represents data and state for special blocks (furnace, chest, etc.)
 */
public abstract class BlockEntity {
    protected int x;
    protected int y;
    protected int z;
    protected int blockId;

    public BlockEntity(int x, int y, int z, int blockId) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.blockId = blockId;
    }

    public int getX() { return x; }
    public int getY() { return y; }
    public int getZ() { return z; }
    public int getBlockId() { return blockId; }

    public abstract void save(CompoundTag tag);
    public abstract void load(CompoundTag tag);
}
