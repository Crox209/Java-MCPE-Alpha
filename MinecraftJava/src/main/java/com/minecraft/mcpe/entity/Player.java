package com.minecraft.mcpe.entity;

import com.minecraft.mcpe.util.Vector3f;
import com.minecraft.mcpe.world.World;

import java.util.Arrays;

/**
 * Player - represents a player in the world
 */
public class Player extends Entity {
    public static final int HOTBAR_SIZE = 9;
    public static final int MAX_STACK_SIZE = 64;

    private String name;
    private float experience;
    private int level;
    private boolean flying;
    private float flySpeed;
    private boolean creative;
    private double walkSpeed;
    private final int[] hotbarBlockIds;
    private final int[] hotbarCounts;
    private int selectedSlot;

    public Player(String name, World world) {
        super(world);
        this.name = name;
        this.experience = 0;
        this.level = 0;
        this.flying = false;
        this.flySpeed = 0.05f;
        this.creative = false;
        this.walkSpeed = 0.1;
        this.health = 20;
        this.maxHealth = 20;
        this.width = 0.6f;
        this.height = 1.8f;
        this.hotbarBlockIds = new int[HOTBAR_SIZE];
        this.hotbarCounts = new int[HOTBAR_SIZE];
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

        for (int i = 0; i < HOTBAR_SIZE; i++) {
            if (hotbarCounts[i] == 0 || hotbarBlockIds[i] == 0) {
                hotbarBlockIds[i] = blockId;
                hotbarCounts[i] = 1;
                return true;
            }
        }
        return false;
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
