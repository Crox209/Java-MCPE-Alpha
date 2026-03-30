package com.minecraft.mcpe.entity;

import java.util.Arrays;
import java.util.Random;

import com.minecraft.mcpe.world.World;

/**
 * Base class for hostile/passive mobs.
 */
public abstract class Mob extends Entity {
    protected double moveSpeed;
    protected float attackDamage;
    protected int attackCooldownTicks;

    public Mob(World world) {
        super(world);
        this.moveSpeed = 0.06;
        this.attackDamage = 2.0f;
        this.attackCooldownTicks = 0;
    }

    public abstract String getMobType();

    public abstract void updateAI(Player player);

    public boolean isHostile() {
        return true;
    }

    public int[] getDropBlockIds(Random random) {
        return new int[] {};
    }

    protected int[] dropRange(int blockId, int minCount, int maxCount, Random random) {
        if (blockId <= 0 || maxCount <= 0 || maxCount < minCount) {
            return new int[] {};
        }
        int count = minCount + random.nextInt(maxCount - minCount + 1);
        if (count <= 0) {
            return new int[] {};
        }

        int[] drops = new int[count];
        Arrays.fill(drops, blockId);
        return drops;
    }

    protected int[] maybeDropRange(int blockId, int minCount, int maxCount, double chance, Random random) {
        if (random.nextDouble() > chance) {
            return new int[] {};
        }
        return dropRange(blockId, minCount, maxCount, random);
    }

    protected void moveToward(double targetX, double targetZ, double speedScale) {
        double dx = targetX - position.x;
        double dz = targetZ - position.z;
        double len = Math.sqrt(dx * dx + dz * dz);
        if (len < 0.0001) {
            return;
        }

        double nx = dx / len;
        double nz = dz / len;
        moveAndCollide(nx * moveSpeed * speedScale, 0, nz * moveSpeed * speedScale);
    }

    protected double distanceTo(Player player) {
        double dx = player.getPosition().x - position.x;
        double dy = player.getPosition().y - position.y;
        double dz = player.getPosition().z - position.z;
        return Math.sqrt(dx * dx + dy * dy + dz * dz);
    }

    protected void knockbackPlayer(Player player, double horizontalStrength, double verticalStrength) {
        double dx = player.getPosition().x - position.x;
        double dz = player.getPosition().z - position.z;
        double len = Math.sqrt(dx * dx + dz * dz);
        if (len < 0.001) {
            return;
        }
        player.addVelocity((dx / len) * horizontalStrength, verticalStrength, (dz / len) * horizontalStrength);
    }

    @Override
    public void update() {
        super.update();
        if (attackCooldownTicks > 0) {
            attackCooldownTicks--;
        }
    }

    @Override
    protected boolean takesFallDamage() {
        return true;
    }
}
