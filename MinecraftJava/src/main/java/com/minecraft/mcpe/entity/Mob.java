package com.minecraft.mcpe.entity;

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

    @Override
    public void update() {
        super.update();
        if (attackCooldownTicks > 0) {
            attackCooldownTicks--;
        }
    }
}
