package com.minecraft.mcpe.entity;

import com.minecraft.mcpe.world.World;

import java.util.Random;

public class Sheep extends Animal {
    public Sheep(World world) {
        super(world);
        this.width = 0.9f;
        this.height = 1.3f;
        this.maxHealth = 8f;
        this.health = 8f;
        this.moveSpeed = 0.035;
    }

    @Override
    public String getMobType() {
        return "Sheep";
    }

    @Override
    public int[] getDropBlockIds(Random random) {
        // Sheep drop wool
        return maybeDropRange(com.minecraft.mcpe.block.Block.WOOL, 1, 3, 1.0, random);
    }
}
