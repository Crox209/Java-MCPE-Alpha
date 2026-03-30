package com.minecraft.mcpe.entity;

import com.minecraft.mcpe.block.Block;
import com.minecraft.mcpe.world.World;

import java.util.Random;

/**
 * Basic passive animal behavior.
 */
public abstract class Animal extends Mob {
    private static final Random RANDOM = new Random();

    private int wanderTicks;
    private double wanderTargetX;
    private double wanderTargetZ;
    private int panicTicks;
    private double panicTargetX;
    private double panicTargetZ;

    protected Animal(World world) {
        super(world);
        this.attackDamage = 0.0f;
        this.moveSpeed = 0.04;
        this.wanderTicks = 0;
        this.wanderTargetX = position.x;
        this.wanderTargetZ = position.z;
        this.panicTicks = 0;
        this.panicTargetX = position.x;
        this.panicTargetZ = position.z;
    }

    @Override
    public void updateAI(Player player) {
        if (player != null && player.isAlive()) {
            double dx = position.x - player.getPosition().x;
            double dz = position.z - player.getPosition().z;
            double distSq = dx * dx + dz * dz;
            if (distSq < 3.5 * 3.5) {
                setPanicFromDirection(dx, dz, 35 + RANDOM.nextInt(25));
            }
        }

        if (panicTicks > 0) {
            panicTicks--;
            moveToward(panicTargetX, panicTargetZ, 1.35);
            return;
        }

        wander();
    }

    @Override
    public boolean isHostile() {
        return false;
    }

    @Override
    public int[] getDropBlockIds(Random random) {
        return new int[] {Block.DIRT};
    }

    @Override
    protected void onDamaged(float amount) {
        double dirX = (RANDOM.nextDouble() - 0.5);
        double dirZ = (RANDOM.nextDouble() - 0.5);
        setPanicFromDirection(dirX, dirZ, 60 + RANDOM.nextInt(30));
    }

    protected void wander() {
        if (wanderTicks <= 0) {
            wanderTicks = 40 + RANDOM.nextInt(100);
            wanderTargetX = position.x + (RANDOM.nextDouble() - 0.5) * 8.0;
            wanderTargetZ = position.z + (RANDOM.nextDouble() - 0.5) * 8.0;
        }
        wanderTicks--;
        moveToward(wanderTargetX, wanderTargetZ, 1.0);
    }

    private void setPanicFromDirection(double dirX, double dirZ, int ticks) {
        double len = Math.sqrt(dirX * dirX + dirZ * dirZ);
        if (len < 0.001) {
            dirX = RANDOM.nextDouble() - 0.5;
            dirZ = RANDOM.nextDouble() - 0.5;
            len = Math.sqrt(dirX * dirX + dirZ * dirZ);
        }
        if (len < 0.001) {
            return;
        }

        panicTicks = Math.max(panicTicks, ticks);
        panicTargetX = position.x + (dirX / len) * 7.0;
        panicTargetZ = position.z + (dirZ / len) * 7.0;
        wanderTicks = 0;
    }
}
