package com.minecraft.mcpe.entity;

import com.minecraft.mcpe.util.Vector3f;
import com.minecraft.mcpe.world.World;

/**
 * Entity - base class for all entities (players, mobs, items, etc.)
 */
import com.minecraft.mcpe.util.AABB;

public abstract class Entity {
    public AABB bb;
    public boolean onGround = false;
    public double xd, yd, zd;
    public float bbWidth = 0.6f;
    public float bbHeight = 1.8f;

    protected static int nextId = 0;
    
    protected int id;
    protected World world;
    protected Vector3f position;
    protected Vector3f velocity;
    protected Vector3f rotation; // yaw, pitch, roll
    protected float width;
    protected float height;
    protected float health;
    protected float maxHealth;
    
    protected boolean alive;
    protected float fallDistance;
    protected int damageCooldownTicks;

    public Entity(World world) {
        this.id = nextId++;
        this.world = world;
        this.position = new Vector3f();
        this.velocity = new Vector3f();
        this.rotation = new Vector3f();
        this.width = 0.6f;
        this.height = 1.8f;
        this.health = 20;
        this.maxHealth = 20;
        this.onGround = false;
        this.alive = true;
        this.fallDistance = 0;
        this.damageCooldownTicks = 0;
    }

    // Getters and Setters
    public int getId() { return id; }
    public Vector3f getPosition() { return position; }
    public void setPosition(Vector3f pos) { this.position = new Vector3f(pos); }
    public Vector3f getVelocity() { return velocity; }
    public void setVelocity(Vector3f vel) { this.velocity = new Vector3f(vel); }
    public Vector3f getRotation() { return rotation; }
    public void setRotation(Vector3f rot) { this.rotation = new Vector3f(rot); }
    public float getWidth() { return width; }
    public float getHeight() { return height; }
    public float getHealth() { return health; }
    public float getMaxHealth() { return maxHealth; }
    public void setHealth(float health) { this.health = Math.max(0, Math.min(health, maxHealth)); }
    public void damage(float amount) {
        if (amount <= 0 || !isAlive()) {
            return;
        }
        if (!bypassDamageCooldown() && damageCooldownTicks > 0) {
            return;
        }

        this.health -= amount;
        onDamaged(amount);
        if (!bypassDamageCooldown()) {
            this.damageCooldownTicks = 10;
        }
        if (this.health <= 0) {
            this.health = 0;
            kill();
        }
    }
    public void heal(float amount) { this.health = Math.min(health + amount, maxHealth); }
    public boolean isAlive() { return alive && health > 0; }
    public boolean isOnGround() { return onGround; }
    public void addVelocity(double dx, double dy, double dz) {
        velocity.x += dx;
        velocity.y += dy;
        velocity.z += dz;
    }

    public void update() {
        velocity.x = 0;
        velocity.z = 0;

        if (damageCooldownTicks > 0) {
            damageCooldownTicks--;
        }

        // Apply gravity and drag.
        velocity.y -= 0.08f;
        velocity.y *= 0.98f;

        moveAndCollide(velocity.x, velocity.y, velocity.z);

        if (onGround) {
            if (takesFallDamage() && fallDistance > 3.0f) {
                // Roughly match classic fall damage: 1 hp per block over safe distance.
                damage(fallDistance - 3.0f);
            }
            fallDistance = 0;
        } else if (velocity.y < 0) {
            fallDistance += (float) -velocity.y;
        }


    }

    protected boolean takesFallDamage() {
        return false;
    }

    protected boolean bypassDamageCooldown() {
        return false;
    }

    protected void onDamaged(float amount) {
        // Optional override for hit reactions.
    }

    protected void moveAndCollide(double dx, double dy, double dz) {
        onGround = false;

        if (dx != 0 && canOccupy(position.x + dx, position.y, position.z)) {
            position.x += dx;
        }

        if (dz != 0 && canOccupy(position.x, position.y, position.z + dz)) {
            position.z += dz;
        }

        if (dy != 0 && canOccupy(position.x, position.y + dy, position.z)) {
            position.y += dy;
        } else if (dy < 0) {
            velocity.y = 0;
            onGround = true;
            position.y = Math.floor(position.y) + 1.0;
        } else if (dy > 0) {
            velocity.y = 0;
        }
    }

    protected boolean canOccupy(double x, double y, double z) {
        double halfWidth = width / 2.0;
        double minX = x - halfWidth;
        double minY = y;
        double minZ = z - halfWidth;
        double maxX = x + halfWidth;
        double maxY = y + height - 0.01;
        double maxZ = z + halfWidth;
        return world.isAreaEmpty(minX, minY, minZ, maxX, maxY, maxZ);
    }

    public void kill() {
        alive = false;
        health = 0;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "{" +
                "id=" + id +
                ", position=" + position +
                ", health=" + health +
                '}';
    }
}
