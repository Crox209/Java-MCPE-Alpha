package com.minecraft.mcpe.renderer.model;

import com.minecraft.mcpe.entity.Entity;

public class SkeletonModel extends ZombieModel {
    public SkeletonModel() {
        super();
        
        rightArm = new ModelPart(40, 16);
        rightArm.addBox(-1.0f, -2.0f, -1.0f, 2, 12, 2, 0.0f);
        rightArm.setPos(-5.0f, 2.0f, 0.0f);
        
        leftArm = new ModelPart(40, 16);
        leftArm.mirror = true;
        leftArm.addBox(-1.0f, -2.0f, -1.0f, 2, 12, 2, 0.0f);
        leftArm.setPos(5.0f, 2.0f, 0.0f);
        
        rightLeg = new ModelPart(0, 16);
        rightLeg.addBox(-1.0f, 0.0f, -1.0f, 2, 12, 2, 0.0f);
        rightLeg.setPos(-2.0f, 12.0f, 0.0f);
        
        leftLeg = new ModelPart(0, 16);
        leftLeg.mirror = true;
        leftLeg.addBox(-1.0f, 0.0f, -1.0f, 2, 12, 2, 0.0f);
        leftLeg.setPos(2.0f, 12.0f, 0.0f);
    }
}
