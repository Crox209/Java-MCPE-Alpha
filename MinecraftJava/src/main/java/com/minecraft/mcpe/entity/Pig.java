package com.minecraft.mcpe.entity;

import com.minecraft.mcpe.world.World;

import java.util.Random;

public class Pig extends Animal {
    public Pig(World world) {
        super(world);
        this.width = 0.9f;
        this.height = 0.9f;
        this.maxHealth = 10f;
        this.health = 10f;
        this.moveSpeed = 0.04;
    }

    @Override
    public String getMobType() {
        return "Pig";
    }

    @Override
    public int[] getDropBlockIds(Random random) {
        // Pigs drop pork and leather
        int[] drops1 = dropRange(com.minecraft.mcpe.block.Block.WOOL, 1, 2, random);
        int[] drops2 = dropRange(com.minecraft.mcpe.block.Block.LOG, 0, 1, random);
        int[] result = new int[drops1.length + drops2.length];
        System.arraycopy(drops1, 0, result, 0, drops1.length);
        System.arraycopy(drops2, 0, result, drops1.length, drops2.length);
        return result;
    }
}
