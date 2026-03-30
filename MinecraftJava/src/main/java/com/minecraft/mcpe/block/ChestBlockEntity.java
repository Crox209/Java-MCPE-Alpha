package com.minecraft.mcpe.block;

import com.minecraft.mcpe.nbt.*;

/**
 * ChestBlockEntity - represents a chest placed in the world with inventory storage
 */
public class ChestBlockEntity extends BlockEntity {
    private int[] storageBlockIds;
    private int[] storageCounts;
    private static final int SLOT_COUNT = 27; // 3x9 inventory
    private static final int MAX_STACK_SIZE = 64;

    public ChestBlockEntity(int x, int y, int z) {
        super(x, y, z, Block.CHEST);
        this.storageBlockIds = new int[SLOT_COUNT];
        this.storageCounts = new int[SLOT_COUNT];
    }

    public int[] getStorageBlockIds() { return storageBlockIds; }
    public int[] getStorageCounts() { return storageCounts; }

    public int getSlotBlockId(int slot) {
        if (slot < 0 || slot >= SLOT_COUNT) return 0;
        return storageBlockIds[slot];
    }

    public int getSlotCount(int slot) {
        if (slot < 0 || slot >= SLOT_COUNT) return 0;
        return storageCounts[slot];
    }

    public void addItem(int blockId, int count) {
        int remaining = count;
        
        // Fill existing stacks first
        for (int i = 0; i < SLOT_COUNT && remaining > 0; i++) {
            if (storageBlockIds[i] != blockId || storageCounts[i] >= MAX_STACK_SIZE) {
                continue;
            }
            int space = MAX_STACK_SIZE - storageCounts[i];
            int add = Math.min(space, remaining);
            storageCounts[i] += add;
            remaining -= add;
        }
        
        // Use empty slots
        for (int i = 0; i < SLOT_COUNT && remaining > 0; i++) {
            if (storageCounts[i] > 0) {
                continue;
            }
            int add = Math.min(MAX_STACK_SIZE, remaining);
            storageBlockIds[i] = blockId;
            storageCounts[i] = add;
            remaining -= add;
        }
    }

    public boolean removeItem(int blockId, int count) {
        int removed = 0;
        
        // Remove from storage
        for (int i = 0; i < SLOT_COUNT && removed < count; i++) {
            if (storageBlockIds[i] != blockId) continue;
            int canRemove = Math.min(storageCounts[i], count - removed);
            storageCounts[i] -= canRemove;
            removed += canRemove;
            if (storageCounts[i] == 0) {
                storageBlockIds[i] = 0;
            }
        }
        
        return removed >= count;
    }

    public int getTotalCount(int blockId) {
        int total = 0;
        for (int i = 0; i < SLOT_COUNT; i++) {
            if (storageBlockIds[i] == blockId) {
                total += storageCounts[i];
            }
        }
        return total;
    }

    @Override
    public void save(CompoundTag tag) {
        CompoundTag chestTag = new CompoundTag("Chest");
        chestTag.put("StorageBlockIds", new IntArrayTag("StorageBlockIds", storageBlockIds.clone()));
        chestTag.put("StorageCounts", new IntArrayTag("StorageCounts", storageCounts.clone()));
        tag.put("Chest", chestTag);
    }

    @Override
    public void load(CompoundTag tag) {
        Tag rawChestTag = tag.get("Chest");
        if (!(rawChestTag instanceof CompoundTag)) {
            return;
        }
        CompoundTag chestTag = (CompoundTag) rawChestTag;

        Tag storageBlocksTag = chestTag.get("StorageBlockIds");
        if (storageBlocksTag instanceof IntArrayTag) {
            storageBlockIds = ((IntArrayTag) storageBlocksTag).value.clone();
        }
        
        Tag storageCountsTag = chestTag.get("StorageCounts");
        if (storageCountsTag instanceof IntArrayTag) {
            storageCounts = ((IntArrayTag) storageCountsTag).value.clone();
        }
    }
}
