package com.minecraft.mcpe.entity;

import com.minecraft.mcpe.world.World;

/**
 * Dropped block/item stack entity with gravity and despawn.
 */
public class ItemEntity extends Entity {
    public static final int MAX_STACK_SIZE = 64;

    private int blockId;
    private int count;
    private int pickupDelayTicks;
    private int ageTicks;

    public ItemEntity(World world, int blockId, int count) {
        super(world);
        this.blockId = Math.max(0, blockId);
        this.count = Math.max(1, count);
        this.pickupDelayTicks = 15;
        this.ageTicks = 0;
        this.width = 0.25f;
        this.height = 0.25f;
        this.maxHealth = 5f;
        this.health = 5f;
    }

    public int getBlockId() {
        return blockId;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = Math.max(0, Math.min(MAX_STACK_SIZE, count));
        if (this.count == 0) {
            kill();
        }
    }

    public boolean canMergeWith(ItemEntity other) {
        if (other == null) {
            return false;
        }
        return this.blockId == other.blockId && this.count < MAX_STACK_SIZE && other.count > 0;
    }

    public int mergeFrom(ItemEntity other) {
        if (!canMergeWith(other)) {
            return 0;
        }
        int space = MAX_STACK_SIZE - this.count;
        int transfer = Math.min(space, other.count);
        this.count += transfer;
        other.setCount(other.count - transfer);
        return transfer;
    }

    public boolean canBePickedUp() {
        return pickupDelayTicks <= 0;
    }

    @Override
    public void update() {
        super.update();
        ageTicks++;
        if (pickupDelayTicks > 0) {
            pickupDelayTicks--;
        }

        if (ageTicks > 20 * 60 * 5) {
            kill();
        }
    }
}
