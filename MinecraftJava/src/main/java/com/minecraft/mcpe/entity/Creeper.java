package com.minecraft.mcpe.entity;

import com.minecraft.mcpe.block.Block;
import com.minecraft.mcpe.world.World;

import java.util.Random;

public class Creeper extends Mob {
    private static final double EXPLOSION_DROP_CHANCE = 0.65;
    private static final Random RANDOM = new Random();
    private int fuseTicks;
    private int wanderTicks;
    private double wanderTargetX;
    private double wanderTargetZ;

    public Creeper(World world) {
        super(world);
        this.width = 0.6f;
        this.height = 1.7f;
        this.maxHealth = 20f;
        this.health = 20f;
        this.moveSpeed = 0.045;
        this.attackDamage = 0f;
        this.fuseTicks = -1;
        this.wanderTicks = 0;
        this.wanderTargetX = position.x;
        this.wanderTargetZ = position.z;
    }

    @Override
    public String getMobType() {
        return "Creeper";
    }

    @Override
    public void updateAI(Player player) {
        if (player == null || !player.isAlive() || player.isCreative()) {
            fuseTicks = -1;
            wander();
            return;
        }

        double distance = distanceTo(player);
        if (distance <= 18.0) {
            moveToward(player.getPosition().x, player.getPosition().z, 1.0);
        } else {
            fuseTicks = -1;
            wander();
            return;
        }

        if (distance <= 2.8) {
            if (fuseTicks < 0) {
                fuseTicks = 30;
            } else {
                fuseTicks--;
            }
            if (fuseTicks == 0) {
                explode(player);
            }
        } else {
            fuseTicks = -1;
        }
    }

    private void explode(Player player) {
        int cx = (int) Math.floor(position.x);
        int cy = (int) Math.floor(position.y);
        int cz = (int) Math.floor(position.z);

        int radius = 2;
        for (int x = cx - radius; x <= cx + radius; x++) {
            for (int y = cy - radius; y <= cy + radius; y++) {
                for (int z = cz - radius; z <= cz + radius; z++) {
                    double dx = x - position.x;
                    double dy = y - position.y;
                    double dz = z - position.z;
                    double dist = Math.sqrt(dx * dx + dy * dy + dz * dz);
                    if (dist <= radius) {
                        int blockId = world.getBlock(x, y, z);
                        if (blockId != Block.AIR && blockId != Block.BEDROCK) {
                            if (RANDOM.nextDouble() < EXPLOSION_DROP_CHANCE) {
                                int dropId = getExplosionDropForBlock(blockId);
                                if (dropId != Block.AIR) {
                                    world.spawnItemDrop(dropId, 1, x + 0.5, y + 0.5, z + 0.5);
                                }
                            }
                            world.setBlock(x, y, z, Block.AIR);
                        }
                    }
                }
            }
        }

        double px = player.getPosition().x - position.x;
        double py = player.getPosition().y - position.y;
        double pz = player.getPosition().z - position.z;
        double pDist = Math.sqrt(px * px + py * py + pz * pz);
        if (pDist <= 4.0 && pDist > 0.001) {
            float damage = (float) Math.max(0.0, (4.0 - pDist) * 3.0);
            player.damage(damage);
            double push = (4.0 - pDist) / 4.0;
            player.addVelocity((px / pDist) * push * 0.45, 0.20 * push, (pz / pDist) * push * 0.45);
        }

        for (Entity entity : world.getEntities()) {
            if (entity == this || !(entity instanceof Mob) || !entity.isAlive()) {
                continue;
            }

            double ex = entity.getPosition().x - position.x;
            double ey = entity.getPosition().y - position.y;
            double ez = entity.getPosition().z - position.z;
            double dist = Math.sqrt(ex * ex + ey * ey + ez * ez);
            if (dist > 4.0 || dist <= 0.001) {
                continue;
            }

            float damage = (float) Math.max(0.0, (4.0 - dist) * 3.0);
            entity.damage(damage);
            double push = (4.0 - dist) / 4.0;
            entity.addVelocity((ex / dist) * push * 0.40, 0.18 * push, (ez / dist) * push * 0.40);
        }

        this.kill();
    }

    private void wander() {
        if (wanderTicks <= 0) {
            wanderTicks = 45 + RANDOM.nextInt(80);
            wanderTargetX = position.x + (RANDOM.nextDouble() - 0.5) * 8.0;
            wanderTargetZ = position.z + (RANDOM.nextDouble() - 0.5) * 8.0;
        }
        wanderTicks--;
        moveToward(wanderTargetX, wanderTargetZ, 0.6);
    }

    private int getExplosionDropForBlock(int blockId) {
        if (blockId == Block.STONE) {
            return Block.COBBLESTONE;
        }
        if (blockId == Block.GRASS) {
            return Block.DIRT;
        }
        if (blockId == Block.LEAVES || blockId == Block.TALL_GRASS || blockId == Block.FIRE) {
            return Block.AIR;
        }
        return blockId;
    }

    @Override
    public int[] getDropBlockIds(Random random) {
        // Creepers drop gunpowder
        return dropRange(Block.SAND, 1, 2, random);
    }
}
