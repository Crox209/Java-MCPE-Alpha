package com.minecraft.mcpe.entity;

import com.minecraft.mcpe.world.World;

import java.util.Random;

public class Cow extends Animal {
    public Cow(World world) {
        super(world);
        this.width = 0.9f;
        this.height = 1.4f;
        this.maxHealth = 10f;
        this.health = 10f;
        this.moveSpeed = 0.035;
    }

    @Override
    public String getMobType() {
        return "Cow";
    }

    @Override
    public int[] getDropBlockIds(Random random) {
        // Cows drop leather and beef
        int[] drops1 = dropRange(com.minecraft.mcpe.block.Block.WOOL, 1, 2, random);
        int[] drops2 = dropRange(com.minecraft.mcpe.block.Block.DIRT, 0, 1, random);
        int[] result = new int[drops1.length + drops2.length];
        System.arraycopy(drops1, 0, result, 0, drops1.length);
        System.arraycopy(drops2, 0, result, drops1.length, drops2.length);
        return result;
    }
}
