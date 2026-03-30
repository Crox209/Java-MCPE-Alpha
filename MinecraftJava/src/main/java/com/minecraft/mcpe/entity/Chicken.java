package com.minecraft.mcpe.entity;

import com.minecraft.mcpe.world.World;

import java.util.Random;

public class Chicken extends Animal {
    public Chicken(World world) {
        super(world);
        this.width = 0.5f;
        this.height = 0.8f;
        this.maxHealth = 4f;
        this.health = 4f;
        this.moveSpeed = 0.05;
    }

    @Override
    public String getMobType() {
        return "Chicken";
    }

    @Override
    public int[] getDropBlockIds(Random random) {
        // Chickens drop feathers and eggs
        int[] drops1 = maybeDropRange(com.minecraft.mcpe.block.Block.SAPLING, 1, 1, 0.8, random);
        int[] drops2 = maybeDropRange(com.minecraft.mcpe.block.Block.COBBLESTONE, 1, 1, 0.5, random);
        int[] result = new int[drops1.length + drops2.length];
        System.arraycopy(drops1, 0, result, 0, drops1.length);
        System.arraycopy(drops2, 0, result, drops1.length, drops2.length);
        return result;
    }
}
