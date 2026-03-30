package com.minecraft.mcpe.entity;

import com.minecraft.mcpe.block.Block;
import com.minecraft.mcpe.world.World;

import java.util.Random;

/**
 * Basic hostile zombie AI.
 */
public class Zombie extends Mob {
    private static final Random RANDOM = new Random();

    private int wanderTicks;
    private double wanderTargetX;
    private double wanderTargetZ;

    public Zombie(World world) {
        super(world);
        this.width = 0.6f;
        this.height = 1.95f;
        this.maxHealth = 20f;
        this.health = 20f;
        this.moveSpeed = 0.05;
        this.attackDamage = 2.0f;
        this.wanderTicks = 0;
        this.wanderTargetX = position.x;
        this.wanderTargetZ = position.z;
    }

    @Override
    public String getMobType() {
        return "Zombie";
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

        if (distance <= 1.5 && attackCooldownTicks <= 0) {
            player.damage(attackDamage);
            knockbackPlayer(player, 0.20, 0.10);
            attackCooldownTicks = 20;
        }

        if (distance <= 18.0) {
            moveToward(player.getPosition().x, player.getPosition().z, 1.0);
        } else {
            wander();
        }
    }

    private void wander() {
        if (wanderTicks <= 0) {
            wanderTicks = 40 + RANDOM.nextInt(80);
            wanderTargetX = position.x + (RANDOM.nextDouble() - 0.5) * 10.0;
            wanderTargetZ = position.z + (RANDOM.nextDouble() - 0.5) * 10.0;
        }
        wanderTicks--;
        moveToward(wanderTargetX, wanderTargetZ, 0.6);
    }

    @Override
    public int[] getDropBlockIds(Random random) {
        // Zombies drop flesh and bones
        int[] drops1 = dropRange(Block.DIRT, 1, 2, random);
        int[] drops2 = maybeDropRange(Block.LOG, 1, 1, 0.3, random);
        int[] result = new int[drops1.length + drops2.length];
        System.arraycopy(drops1, 0, result, 0, drops1.length);
        System.arraycopy(drops2, 0, result, drops1.length, drops2.length);
        return result;
    }
}
