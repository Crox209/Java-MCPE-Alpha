package com.minecraft.mcpe.block;

import com.minecraft.mcpe.nbt.*;

/**
 * CraftingTableBlockEntity - represents a crafting table placed in the world
 */
public class CraftingTableBlockEntity extends BlockEntity {
    private int[] gridBlockIds;
    private int[] gridCounts;
    private int[] outputBlockIds;
    private int[] outputCounts;
    
    private static final int GRID_SIZE = 9; // 3x3 grid
    private static final int OUTPUT_SIZE = 1;
    private static final int MAX_STACK_SIZE = 64;

    public CraftingTableBlockEntity(int x, int y, int z) {
        super(x, y, z, Block.CRAFTING_TABLE);
        this.gridBlockIds = new int[GRID_SIZE];
        this.gridCounts = new int[GRID_SIZE];
        this.outputBlockIds = new int[OUTPUT_SIZE];
        this.outputCounts = new int[OUTPUT_SIZE];
    }

    public int[] getGridBlockIds() { return gridBlockIds; }
    public int[] getGridCounts() { return gridCounts; }
    public int[] getOutputBlockIds() { return outputBlockIds; }
    public int[] getOutputCounts() { return outputCounts; }

    public int getGridBlockId(int slot) {
        if (slot < 0 || slot >= GRID_SIZE) return 0;
        return gridBlockIds[slot];
    }

    public int getGridCount(int slot) {
        if (slot < 0 || slot >= GRID_SIZE) return 0;
        return gridCounts[slot];
    }

    public int getOutputBlockId() {
        return outputBlockIds[0];
    }

    public int getOutputCount() {
        return outputCounts[0];
    }

    public void setGridItem(int slot, int blockId, int count) {
        if (slot < 0 || slot >= GRID_SIZE) return;
        gridBlockIds[slot] = blockId;
        gridCounts[slot] = Math.min(count, MAX_STACK_SIZE);
    }

    public void setOutputItem(int blockId, int count) {
        outputBlockIds[0] = blockId;
        outputCounts[0] = Math.min(count, MAX_STACK_SIZE);
    }

    public void clearGrid() {
        for (int i = 0; i < GRID_SIZE; i++) {
            gridBlockIds[i] = 0;
            gridCounts[i] = 0;
        }
    }

    public void clearOutput() {
        outputBlockIds[0] = 0;
        outputCounts[0] = 0;
    }

    @Override
    public void save(CompoundTag tag) {
        CompoundTag tableTag = new CompoundTag("CraftingTable");
        tableTag.put("GridBlockIds", new IntArrayTag("GridBlockIds", gridBlockIds.clone()));
        tableTag.put("GridCounts", new IntArrayTag("GridCounts", gridCounts.clone()));
        tableTag.put("OutputBlockIds", new IntArrayTag("OutputBlockIds", outputBlockIds.clone()));
        tableTag.put("OutputCounts", new IntArrayTag("OutputCounts", outputCounts.clone()));
        tag.put("CraftingTable", tableTag);
    }

    @Override
    public void load(CompoundTag tag) {
        Tag rawTableTag = tag.get("CraftingTable");
        if (!(rawTableTag instanceof CompoundTag)) {
            return;
        }
        CompoundTag tableTag = (CompoundTag) rawTableTag;

        Tag gridBlocksTag = tableTag.get("GridBlockIds");
        if (gridBlocksTag instanceof IntArrayTag) {
            gridBlockIds = ((IntArrayTag) gridBlocksTag).value.clone();
        }
        
        Tag gridCountsTag = tableTag.get("GridCounts");
        if (gridCountsTag instanceof IntArrayTag) {
            gridCounts = ((IntArrayTag) gridCountsTag).value.clone();
        }

        Tag outputBlocksTag = tableTag.get("OutputBlockIds");
        if (outputBlocksTag instanceof IntArrayTag) {
            outputBlockIds = ((IntArrayTag) outputBlocksTag).value.clone();
        }

        Tag outputCountsTag = tableTag.get("OutputCounts");
        if (outputCountsTag instanceof IntArrayTag) {
            outputCounts = ((IntArrayTag) outputCountsTag).value.clone();
        }
    }
}
