package com.minecraft.mcpe.entity;

import com.minecraft.mcpe.block.Block;
import com.minecraft.mcpe.world.World;

import java.util.Random;

public class Spider extends Mob {
    private static final Random RANDOM = new Random();
    private int wanderTicks;
    private double wanderTargetX;
    private double wanderTargetZ;

    public Spider(World world) {
        super(world);
        this.width = 1.4f;
        this.height = 0.9f;
        this.maxHealth = 16f;
        this.health = 16f;
        this.moveSpeed = 0.07;
        this.attackDamage = 2.0f;
        this.wanderTicks = 0;
        this.wanderTargetX = position.x;
        this.wanderTargetZ = position.z;
    }

    @Override
    public String getMobType() {
        return "Spider";
    }

    @Override
    public void updateAI(Player player) {
        if (world.isDayTime()) {
            wander();
            return;
        }

        if (player == null || !player.isAlive() || player.isCreative()) {
            wander();
            return;
        }

        double distance = distanceTo(player);
        if (distance <= 16.0) {
            double speedScale = distance > 3.0 ? 1.1 : 0.8;
            moveToward(player.getPosition().x, player.getPosition().z, speedScale);

            if (distance <= 1.7 && attackCooldownTicks <= 0) {
                player.damage(attackDamage);
                knockbackPlayer(player, 0.22, 0.12);
                attackCooldownTicks = 18;
            } else if (distance > 1.7 && distance < 4.0 && attackCooldownTicks <= 0 && RANDOM.nextDouble() < 0.08) {
                velocity.y = 0.34;
                attackCooldownTicks = 20;
            }
        }
    }

    private void wander() {
        if (wanderTicks <= 0) {
            wanderTicks = 35 + RANDOM.nextInt(70);
            wanderTargetX = position.x + (RANDOM.nextDouble() - 0.5) * 9.0;
            wanderTargetZ = position.z + (RANDOM.nextDouble() - 0.5) * 9.0;
        }
        wanderTicks--;
        moveToward(wanderTargetX, wanderTargetZ, 0.65);
    }

    @Override
    public int[] getDropBlockIds(Random random) {
        // Spiders drop string and eyes
        int[] drops1 = dropRange(Block.WOOL, 1, 2, random);
        int[] drops2 = maybeDropRange(Block.CLAY, 1, 1, 0.5, random);
        int[] result = new int[drops1.length + drops2.length];
        System.arraycopy(drops1, 0, result, 0, drops1.length);
        System.arraycopy(drops2, 0, result, drops1.length, drops2.length);
        return result;
    }
}
