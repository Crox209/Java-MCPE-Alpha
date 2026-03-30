package com.minecraft.mcpe.entity;

import com.minecraft.mcpe.util.Vector3f;
import com.minecraft.mcpe.world.World;

import java.util.Arrays;

/**
 * Player - represents a player in the world
 */
public class Player extends Entity {
    public static final int HOTBAR_SIZE = 9;
    public static final int INVENTORY_SIZE = 27;
    public static final int MAX_STACK_SIZE = 64;

    private String name;
    private float experience;
    private int level;
    private boolean flying;
    private boolean sneaking;
    private float flySpeed;
    private boolean creative;
    private double walkSpeed;
    private final int[] hotbarBlockIds;
    private final int[] hotbarCounts;
    private final int[] inventoryBlockIds;
    private final int[] inventoryCounts;
    private int selectedSlot;

    public Player(String name, World world) {
        super(world);
        this.name = name;
        this.experience = 0;
        this.level = 0;
        this.flying = false;
        this.sneaking = false;
        this.flySpeed = 0.05f;
        this.creative = false;
        this.walkSpeed = 0.1;
        this.health = 20;
        this.maxHealth = 20;
        this.width = 0.6f;
        this.height = 1.8f;
        this.hotbarBlockIds = new int[HOTBAR_SIZE];
        this.hotbarCounts = new int[HOTBAR_SIZE];
        this.inventoryBlockIds = new int[INVENTORY_SIZE];
        this.inventoryCounts = new int[INVENTORY_SIZE];
        this.selectedSlot = 0;

        int[] defaults = {1, 2, 3, 4, 5, 12, 20, 17, 18};
        for (int i = 0; i < HOTBAR_SIZE; i++) {
            hotbarBlockIds[i] = defaults[i];
            hotbarCounts[i] = MAX_STACK_SIZE;
        }
    }

    public String getName() { return name; }
    public float getExperience() { return experience; }
    public void addExperience(float amount) { this.experience += amount; }
    public int getLevel() { return level; }
    public void setLevel(int level) { this.level = level; }
    public boolean isFlying() { return flying; }
    public void setFlying(boolean flying) { this.flying = flying; }
    public boolean isSneaking() { return sneaking; }
    public void setSneaking(boolean sneaking) { this.sneaking = sneaking; }
    public boolean isCreative() { return creative; }
    public void setCreative(boolean creative) { this.creative = creative; }
    public float getFlySpeed() { return flySpeed; }
    public void setFlySpeed(float speed) { this.flySpeed = speed; }
    public double getWalkSpeed() { return walkSpeed; }
    public void setWalkSpeed(double speed) { this.walkSpeed = speed; }
    public int getSelectedSlot() { return selectedSlot; }

    public void setSelectedSlot(int slot) {
        if (slot < 0 || slot >= HOTBAR_SIZE) {
            return;
        }
        selectedSlot = slot;
    }

    public void scrollSelectedSlot(int delta) {
        if (delta == 0) {
            return;
        }
        selectedSlot = (selectedSlot + delta) % HOTBAR_SIZE;
        if (selectedSlot < 0) {
            selectedSlot += HOTBAR_SIZE;
        }
    }

    public int getSelectedBlockId() {
        return hotbarBlockIds[selectedSlot];
    }

    public int getSelectedBlockCount() {
        if (creative) {
            return MAX_STACK_SIZE;
        }
        return hotbarCounts[selectedSlot];
    }

    public int[] getHotbarBlockIds() {
        return Arrays.copyOf(hotbarBlockIds, hotbarBlockIds.length);
    }

    public int[] getHotbarCounts() {
        return Arrays.copyOf(hotbarCounts, hotbarCounts.length);
    }

    public int[] getInventoryBlockIds() {
        return Arrays.copyOf(inventoryBlockIds, inventoryBlockIds.length);
    }

    public int[] getInventoryCounts() {
        return Arrays.copyOf(inventoryCounts, inventoryCounts.length);
    }

    public int getCombinedSlotBlockId(int slot) {
        if (slot < 0 || slot >= HOTBAR_SIZE + INVENTORY_SIZE) {
            return 0;
        }
        if (slot < HOTBAR_SIZE) {
            return hotbarBlockIds[slot];
        }
        return inventoryBlockIds[slot - HOTBAR_SIZE];
    }

    public int getCombinedSlotCount(int slot) {
        if (slot < 0 || slot >= HOTBAR_SIZE + INVENTORY_SIZE) {
            return 0;
        }
        if (slot < HOTBAR_SIZE) {
            return hotbarCounts[slot];
        }
        return inventoryCounts[slot - HOTBAR_SIZE];
    }

    public boolean quickTransferCombinedSlot(int slot) {
        if (slot < 0 || slot >= HOTBAR_SIZE + INVENTORY_SIZE) {
            return false;
        }

        int[] fromIds;
        int[] fromCounts;
        int fromIndex;
        int[] toIds;
        int[] toCounts;
        int toSize;

        if (slot < HOTBAR_SIZE) {
            fromIds = hotbarBlockIds;
            fromCounts = hotbarCounts;
            fromIndex = slot;
            toIds = inventoryBlockIds;
            toCounts = inventoryCounts;
            toSize = INVENTORY_SIZE;
        } else {
            fromIds = inventoryBlockIds;
            fromCounts = inventoryCounts;
            fromIndex = slot - HOTBAR_SIZE;
            toIds = hotbarBlockIds;
            toCounts = hotbarCounts;
            toSize = HOTBAR_SIZE;
        }

        int blockId = fromIds[fromIndex];
        int remaining = fromCounts[fromIndex];
        if (blockId <= 0 || remaining <= 0) {
            return false;
        }

        // Fill existing stacks first.
        for (int i = 0; i < toSize && remaining > 0; i++) {
            if (toIds[i] != blockId || toCounts[i] >= MAX_STACK_SIZE) {
                continue;
            }
            int space = MAX_STACK_SIZE - toCounts[i];
            int move = Math.min(space, remaining);
            toCounts[i] += move;
            remaining -= move;
        }

        // Then use empty slots.
        for (int i = 0; i < toSize && remaining > 0; i++) {
            if (toCounts[i] > 0 || toIds[i] > 0) {
                continue;
            }
            int move = Math.min(MAX_STACK_SIZE, remaining);
            toIds[i] = blockId;
            toCounts[i] = move;
            remaining -= move;
        }

        int moved = fromCounts[fromIndex] - remaining;
        if (moved <= 0) {
            return false;
        }

        fromCounts[fromIndex] = remaining;
        if (fromCounts[fromIndex] == 0) {
            fromIds[fromIndex] = 0;
            if (slot == selectedSlot) {
                refillSelectedSlotFromInventory();
            }
        }
        return true;
    }

    public boolean quickTransferAllMatchingCombinedSlot(int slot) {
        int blockId = getCombinedSlotBlockId(slot);
        if (blockId <= 0) {
            return false;
        }

        boolean moved = false;
        if (slot < HOTBAR_SIZE) {
            for (int i = 0; i < HOTBAR_SIZE; i++) {
                if (hotbarBlockIds[i] == blockId && hotbarCounts[i] > 0) {
                    moved |= quickTransferCombinedSlot(i);
                }
            }
        } else {
            for (int i = 0; i < INVENTORY_SIZE; i++) {
                if (inventoryBlockIds[i] == blockId && inventoryCounts[i] > 0) {
                    moved |= quickTransferCombinedSlot(HOTBAR_SIZE + i);
                }
            }
        }
        return moved;
    }

    public int getAvailableSpaceForBlock(int blockId) {
        if (blockId <= 0) {
            return 0;
        }
        if (creative) {
            return Integer.MAX_VALUE / 2;
        }

        int space = 0;
        for (int i = 0; i < HOTBAR_SIZE; i++) {
            if (hotbarCounts[i] == 0 || hotbarBlockIds[i] == 0) {
                space += MAX_STACK_SIZE;
            } else if (hotbarBlockIds[i] == blockId) {
                space += MAX_STACK_SIZE - hotbarCounts[i];
            }
        }
        for (int i = 0; i < INVENTORY_SIZE; i++) {
            if (inventoryCounts[i] == 0 || inventoryBlockIds[i] == 0) {
                space += MAX_STACK_SIZE;
            } else if (inventoryBlockIds[i] == blockId) {
                space += MAX_STACK_SIZE - inventoryCounts[i];
            }
        }
        return space;
    }

    public boolean splitCombinedSlot(int slot) {
        if (slot < 0 || slot >= HOTBAR_SIZE + INVENTORY_SIZE) {
            return false;
        }

        int[] ids;
        int[] counts;
        int index;
        int rangeStart;
        int rangeEnd;

        if (slot < HOTBAR_SIZE) {
            ids = hotbarBlockIds;
            counts = hotbarCounts;
            index = slot;
            rangeStart = 0;
            rangeEnd = HOTBAR_SIZE;
        } else {
            ids = inventoryBlockIds;
            counts = inventoryCounts;
            index = slot - HOTBAR_SIZE;
            rangeStart = 0;
            rangeEnd = INVENTORY_SIZE;
        }

        int blockId = ids[index];
        int count = counts[index];
        if (blockId <= 0 || count < 2) {
            return false;
        }

        int target = -1;
        for (int i = rangeStart; i < rangeEnd; i++) {
            if (i == index) {
                continue;
            }
            if (counts[i] == 0 || ids[i] == 0) {
                target = i;
                break;
            }
        }
        if (target < 0) {
            return false;
        }

        int moved = count / 2;
        counts[index] -= moved;
        ids[target] = blockId;
        counts[target] = moved;
        return true;
    }

    public void swapBackpackWithHotbar(int backpackSlot, int hotbarSlot) {
        if (backpackSlot < 0 || backpackSlot >= INVENTORY_SIZE) {
            return;
        }
        if (hotbarSlot < 0 || hotbarSlot >= HOTBAR_SIZE) {
            return;
        }

        int invId = inventoryBlockIds[backpackSlot];
        int invCount = inventoryCounts[backpackSlot];
        inventoryBlockIds[backpackSlot] = hotbarBlockIds[hotbarSlot];
        inventoryCounts[backpackSlot] = hotbarCounts[hotbarSlot];
        hotbarBlockIds[hotbarSlot] = invId;
        hotbarCounts[hotbarSlot] = invCount;

        if (inventoryCounts[backpackSlot] == 0) {
            inventoryBlockIds[backpackSlot] = 0;
        }
        if (hotbarCounts[hotbarSlot] == 0) {
            hotbarBlockIds[hotbarSlot] = 0;
        }
    }

    public void setHotbarState(int[] blockIds, int[] counts, int selectedSlot) {
        if (blockIds == null || counts == null || blockIds.length != HOTBAR_SIZE || counts.length != HOTBAR_SIZE) {
            return;
        }
        for (int i = 0; i < HOTBAR_SIZE; i++) {
            hotbarBlockIds[i] = Math.max(0, blockIds[i]);
            hotbarCounts[i] = Math.max(0, Math.min(MAX_STACK_SIZE, counts[i]));
        }
        setSelectedSlot(selectedSlot);
    }

    public void setInventoryState(int[] blockIds, int[] counts) {
        if (blockIds == null || counts == null || blockIds.length != INVENTORY_SIZE || counts.length != INVENTORY_SIZE) {
            return;
        }
        for (int i = 0; i < INVENTORY_SIZE; i++) {
            inventoryBlockIds[i] = Math.max(0, blockIds[i]);
            inventoryCounts[i] = Math.max(0, Math.min(MAX_STACK_SIZE, counts[i]));
            if (inventoryCounts[i] == 0) {
                inventoryBlockIds[i] = 0;
            }
        }
    }

    public boolean canPlaceSelectedBlock() {
        int blockId = getSelectedBlockId();
        if (blockId <= 0) {
            return false;
        }
        if (creative) {
            return true;
        }
        return hotbarCounts[selectedSlot] > 0;
    }

    public boolean consumeSelectedBlock() {
        if (creative) {
            return true;
        }
        if (hotbarCounts[selectedSlot] <= 0) {
            return false;
        }
        hotbarCounts[selectedSlot]--;
        if (hotbarCounts[selectedSlot] == 0) {
            hotbarBlockIds[selectedSlot] = 0;
            refillSelectedSlotFromInventory();
        }
        return true;
    }

    public boolean addBlockToInventory(int blockId) {
        if (blockId <= 0) {
            return false;
        }
        if (creative) {
            return true;
        }

        for (int i = 0; i < HOTBAR_SIZE; i++) {
            if (hotbarBlockIds[i] == blockId && hotbarCounts[i] < MAX_STACK_SIZE) {
                hotbarCounts[i]++;
                return true;
            }
        }

        for (int i = 0; i < INVENTORY_SIZE; i++) {
            if (inventoryBlockIds[i] == blockId && inventoryCounts[i] < MAX_STACK_SIZE) {
                inventoryCounts[i]++;
                return true;
            }
        }

        for (int i = 0; i < HOTBAR_SIZE; i++) {
            if (hotbarCounts[i] == 0 || hotbarBlockIds[i] == 0) {
                hotbarBlockIds[i] = blockId;
                hotbarCounts[i] = 1;
                return true;
            }
        }

        for (int i = 0; i < INVENTORY_SIZE; i++) {
            if (inventoryCounts[i] == 0 || inventoryBlockIds[i] == 0) {
                inventoryBlockIds[i] = blockId;
                inventoryCounts[i] = 1;
                return true;
            }
        }
        return false;
    }

    public boolean addBlockToInventory(int blockId, int count) {
        if (count <= 0) {
            return true;
        }
        for (int i = 0; i < count; i++) {
            if (!addBlockToInventory(blockId)) {
                return false;
            }
        }
        return true;
    }

    public int getTotalBlockCount(int blockId) {
        if (blockId <= 0) {
            return 0;
        }

        int total = 0;
        for (int i = 0; i < HOTBAR_SIZE; i++) {
            if (hotbarBlockIds[i] == blockId) {
                total += hotbarCounts[i];
            }
        }
        for (int i = 0; i < INVENTORY_SIZE; i++) {
            if (inventoryBlockIds[i] == blockId) {
                total += inventoryCounts[i];
            }
        }
        return total;
    }

    public boolean removeBlockFromInventory(int blockId, int count) {
        if (blockId <= 0 || count <= 0) {
            return false;
        }
        if (creative) {
            return true;
        }
        if (getTotalBlockCount(blockId) < count) {
            return false;
        }

        int remaining = count;
        for (int i = 0; i < HOTBAR_SIZE && remaining > 0; i++) {
            if (hotbarBlockIds[i] != blockId || hotbarCounts[i] <= 0) {
                continue;
            }
            int take = Math.min(remaining, hotbarCounts[i]);
            hotbarCounts[i] -= take;
            if (hotbarCounts[i] == 0) {
                hotbarBlockIds[i] = 0;
            }
            remaining -= take;
        }

        for (int i = 0; i < INVENTORY_SIZE && remaining > 0; i++) {
            if (inventoryBlockIds[i] != blockId || inventoryCounts[i] <= 0) {
                continue;
            }
            int take = Math.min(remaining, inventoryCounts[i]);
            inventoryCounts[i] -= take;
            if (inventoryCounts[i] == 0) {
                inventoryBlockIds[i] = 0;
            }
            remaining -= take;
        }

        if (hotbarCounts[selectedSlot] == 0 && hotbarBlockIds[selectedSlot] == 0) {
            refillSelectedSlotFromInventory();
        }
        return remaining == 0;
    }

    public int dropOneSelectedBlock() {
        int blockId = getSelectedBlockId();
        if (blockId <= 0) {
            return 0;
        }
        if (!creative && !consumeSelectedBlock()) {
            return 0;
        }
        return blockId;
    }

    public int removeSelectedStackForTransfer() {
        int blockId = getSelectedBlockId();
        if (blockId <= 0) {
            return 0;
        }

        if (creative) {
            return MAX_STACK_SIZE;
        }

        int moved = hotbarCounts[selectedSlot];
        if (moved <= 0) {
            return 0;
        }
        hotbarCounts[selectedSlot] = 0;
        hotbarBlockIds[selectedSlot] = 0;
        refillSelectedSlotFromInventory();
        return moved;
    }

    public void pickBlockIntoSelectedSlot(int blockId) {
        if (blockId <= 0) {
            return;
        }
        hotbarBlockIds[selectedSlot] = blockId;
        if (creative) {
            hotbarCounts[selectedSlot] = MAX_STACK_SIZE;
        } else if (hotbarCounts[selectedSlot] == 0) {
            hotbarCounts[selectedSlot] = 1;
        }
    }

    private void refillSelectedSlotFromInventory() {
        for (int i = 0; i < INVENTORY_SIZE; i++) {
            if (inventoryCounts[i] > 0 && inventoryBlockIds[i] > 0) {
                hotbarBlockIds[selectedSlot] = inventoryBlockIds[i];
                hotbarCounts[selectedSlot] = inventoryCounts[i];
                inventoryBlockIds[i] = 0;
                inventoryCounts[i] = 0;
                return;
            }
        }
    }

    public void moveForward(double speed) {
        double yaw = Math.toRadians(rotation.x);
        double dx = Math.sin(yaw) * speed;
        double dz = -Math.cos(yaw) * speed;
        moveAndCollide(dx, 0, dz);
    }

    public void moveBackward(double speed) {
        moveForward(-speed);
    }

    public void moveLeft(double speed) {
        double yaw = Math.toRadians(rotation.x - 90);
        double dx = Math.sin(yaw) * speed;
        double dz = -Math.cos(yaw) * speed;
        moveAndCollide(dx, 0, dz);
    }

    public void moveRight(double speed) {
        moveLeft(-speed);
    }

    public void jump() {
        if (isOnGround() && !isFlying()) {
            velocity.y = 0.42f;
        }
    }

    public void moveUp(double speed) {
        if (isFlying() || isCreative()) {
            position.y += speed;
        }
    }

    public void moveDown(double speed) {
        moveUp(-speed);
    }

    @Override
    public void update() {
        if (!flying) {
            super.update();
        } else {
            // Flying mode - no gravity
            moveAndCollide(velocity.x, velocity.y, velocity.z);
            velocity.x = 0;
            velocity.z = 0;
            velocity.y *= 0.6f;
        }
    }

    @Override
    protected boolean takesFallDamage() {
        return !creative;
    }

    @Override
    public String toString() {
        return "Player{" +
                "name='" + name + '\'' +
                ", position=" + position +
                ", health=" + health +
                ", level=" + level +
                ", creative=" + creative +
                '}';
    }
}
