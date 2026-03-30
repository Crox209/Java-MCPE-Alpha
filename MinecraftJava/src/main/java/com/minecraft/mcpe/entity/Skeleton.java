package com.minecraft.mcpe.entity;

import com.minecraft.mcpe.block.Block;
import com.minecraft.mcpe.world.World;

import java.util.Random;

public class Skeleton extends Mob {
    private static final Random RANDOM = new Random();

    private int wanderTicks;
    private double wanderTargetX;
    private double wanderTargetZ;

    public Skeleton(World world) {
        super(world);
        this.width = 0.6f;
        this.height = 1.99f;
        this.maxHealth = 20f;
        this.health = 20f;
        this.moveSpeed = 0.05;
        this.attackDamage = 3.0f;
        this.wanderTicks = 0;
        this.wanderTargetX = position.x;
        this.wanderTargetZ = position.z;
    }

    @Override
    public String getMobType() {
        return "Skeleton";
    }

    @Override
    public void updateAI(Player player) {
        if (world.isDayTime() && world.canSeeSky((int) Math.floor(position.x), (int) Math.floor(position.y + 1), (int) Math.floor(position.z))) {
            if (world.getWorldTime() % 40 == 0) {
                damage(1.0f);
            }
        }

        if (player == null || !player.isAlive() || player.isCreative()) {
            wander();
            return;
        }

        double distance = distanceTo(player);
        if (distance > 14.0) {
            wander();
            return;
        }

        if (distance > 6.0) {
            moveToward(player.getPosition().x, player.getPosition().z, 1.0);
        }

        if (distance <= 12.0 && attackCooldownTicks <= 0) {
            player.damage(attackDamage);
            knockbackPlayer(player, 0.18, 0.09);
            attackCooldownTicks = 30;
        }
    }

    private void wander() {
        if (wanderTicks <= 0) {
            wanderTicks = 40 + RANDOM.nextInt(80);
            wanderTargetX = position.x + (RANDOM.nextDouble() - 0.5) * 8.0;
            wanderTargetZ = position.z + (RANDOM.nextDouble() - 0.5) * 8.0;
        }
        wanderTicks--;
        moveToward(wanderTargetX, wanderTargetZ, 0.6);
    }

    @Override
    public int[] getDropBlockIds(Random random) {
        // Skeletons drop bones and arrows
        int[] drops1 = dropRange(Block.SAND, 1, 2, random);
        int[] drops2 = dropRange(Block.COBBLESTONE, 0, 1, random);
        int[] result = new int[drops1.length + drops2.length];
        System.arraycopy(drops1, 0, result, 0, drops1.length);
        System.arraycopy(drops2, 0, result, drops1.length, drops2.length);
        return result;
    }
}
