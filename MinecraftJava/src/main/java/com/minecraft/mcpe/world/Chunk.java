package com.minecraft.mcpe.world;

import com.minecraft.mcpe.block.Block;
import com.minecraft.mcpe.nbt.*;

/**
 * Chunk - 16x16x128 block storage
 * Converted from C++ implementation
 */
public class Chunk {
    public static final int WIDTH = 16;
    public static final int HEIGHT = 128;
    public static final int DEPTH = 16;
    
    private final int x, z;
    private byte[] blocks;
    private byte[] data;
    private byte[] skyLight;
    private byte[] blockLight;
    private boolean dirty;

    public Chunk(int x, int z) {
        this.x = x;
        this.z = z;
        this.blocks = new byte[WIDTH * HEIGHT * DEPTH];
        this.data = new byte[WIDTH * HEIGHT * DEPTH / 2];
        this.skyLight = new byte[WIDTH * HEIGHT * DEPTH / 2];
        this.blockLight = new byte[WIDTH * HEIGHT * DEPTH / 2];
        this.dirty = false;
        
        // Initialize with air
        for (int i = 0; i < blocks.length; i++) {
            blocks[i] = Block.AIR;
        }
    }

    public Chunk(int x, int z, CompoundTag tag) {
        this(x, z);
        loadFromNBT(tag);
    }

    public int getX() { return x; }
    public int getZ() { return z; }
    
    public void setBlock(int x, int y, int z, int blockId) {
        if (isValidCoord(x, y, z)) {
            int index = getIndex(x, y, z);
            blocks[index] = (byte) blockId;
            dirty = true;
        }
    }

    public int getBlock(int x, int y, int z) {
        if (isValidCoord(x, y, z)) {
            return blocks[getIndex(x, y, z)] & 0xFF;
        }
        return Block.AIR;
    }

    public void setBlockData(int x, int y, int z, int value) {
        if (isValidCoord(x, y, z)) {
            int index = getIndex(x, y, z);
            int byteIndex = index / 2;
            int nibble = index % 2;
            
            byte current = data[byteIndex];
            if (nibble == 0) {
                data[byteIndex] = (byte) ((current & 0xF0) | (value & 0x0F));
            } else {
                data[byteIndex] = (byte) ((current & 0x0F) | ((value & 0x0F) << 4));
            }
            dirty = true;
        }
    }

    public int getBlockData(int x, int y, int z) {
        if (isValidCoord(x, y, z)) {
            int index = getIndex(x, y, z);
            int byteIndex = index / 2;
            int nibble = index % 2;
            
            byte current = data[byteIndex];
            if (nibble == 0) {
                return current & 0x0F;
            } else {
                return (current >> 4) & 0x0F;
            }
        }
        return 0;
    }

    public void setSkyLight(int x, int y, int z, int value) {
        setNibble(skyLight, x, y, z, value);
    }

    public int getSkyLight(int x, int y, int z) {
        return getNibble(skyLight, x, y, z);
    }

    public void setBlockLight(int x, int y, int z, int value) {
        setNibble(blockLight, x, y, z, value);
    }

    public int getBlockLight(int x, int y, int z) {
        return getNibble(blockLight, x, y, z);
    }

    private void setNibble(byte[] array, int x, int y, int z, int value) {
        if (isValidCoord(x, y, z)) {
            int index = getIndex(x, y, z);
            int byteIndex = index / 2;
            int nibble = index % 2;
            
            byte current = array[byteIndex];
            if (nibble == 0) {
                array[byteIndex] = (byte) ((current & 0xF0) | (value & 0x0F));
            } else {
                array[byteIndex] = (byte) ((current & 0x0F) | ((value & 0x0F) << 4));
            }
            dirty = true;
        }
    }

    private int getNibble(byte[] array, int x, int y, int z) {
        if (isValidCoord(x, y, z)) {
            int index = getIndex(x, y, z);
            int byteIndex = index / 2;
            int nibble = index % 2;
            
            byte current = array[byteIndex];
            if (nibble == 0) {
                return current & 0x0F;
            } else {
                return (current >> 4) & 0x0F;
            }
        }
        return 0;
    }

    private boolean isValidCoord(int x, int y, int z) {
        return x >= 0 && x < WIDTH && y >= 0 && y < HEIGHT && z >= 0 && z < DEPTH;
    }

    private int getIndex(int x, int y, int z) {
        return y * WIDTH * DEPTH + z * WIDTH + x;
    }

    public boolean isDirty() { return dirty; }
    public void setDirty(boolean dirty) { this.dirty = dirty; }

    public CompoundTag toNBT() {
        CompoundTag tag = new CompoundTag("Level");
        tag.put("x", new IntTag("x", x));
        tag.put("z", new IntTag("z", z));
        tag.put("Blocks", new ByteArrayTag("Blocks", blocks));
        tag.put("Data", new ByteArrayTag("Data", data));
        tag.put("SkyLight", new ByteArrayTag("SkyLight", skyLight));
        tag.put("BlockLight", new ByteArrayTag("BlockLight", blockLight));
        return tag;
    }

    public void loadFromNBT(CompoundTag tag) {
        Tag blocksTag = tag.get("Blocks");
        if (blocksTag instanceof ByteArrayTag) {
            blocks = ((ByteArrayTag) blocksTag).value.clone();
        }

        Tag dataTag = tag.get("Data");
        if (dataTag instanceof ByteArrayTag) {
            data = ((ByteArrayTag) dataTag).value.clone();
        }

        Tag skyLightTag = tag.get("SkyLight");
        if (skyLightTag instanceof ByteArrayTag) {
            skyLight = ((ByteArrayTag) skyLightTag).value.clone();
        }

        Tag blockLightTag = tag.get("BlockLight");
        if (blockLightTag instanceof ByteArrayTag) {
            blockLight = ((ByteArrayTag) blockLightTag).value.clone();
        }
    }
}
