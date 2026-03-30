package com.minecraft.mcpe.entity;

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
        if (player == null || !player.isAlive()) {
            wander();
            return;
        }

        double distance = distanceTo(player);

        if (distance <= 1.5 && attackCooldownTicks <= 0) {
            player.damage(attackDamage);
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
}
