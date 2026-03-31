package com.minecraft.mcpe.renderer.model;

import com.minecraft.mcpe.entity.Entity;

public class SheepModel extends QuadrupedModel {
    public ModelPart woolHead, woolBody, woolLeg1, woolLeg2, woolLeg3, woolLeg4;
    
    public SheepModel() {
        super(12);
        head = new ModelPart(0, 0);
        head.addBox(-3.0f, -4.0f, -6.0f, 6, 6, 8, 0.0f);
        head.setPos(0.0f, 6.0f, -8.0f);
        
        body = new ModelPart(28, 8);
        body.addBox(-4.0f, -10.0f, -7.0f, 8, 16, 6, 0.0f);
        body.setPos(0.0f, 5.0f, 2.0f);
        
        // Fur/Wool
        woolHead = new ModelPart(0, 0);
        woolHead.addBox(-3.0f, -4.0f, -4.0f, 6, 6, 6, 0.6f);
        woolHead.setPos(0.0f, 6.0f, -8.0f);
        
        woolBody = new ModelPart(28, 8);
        woolBody.addBox(-4.0f, -10.0f, -7.0f, 8, 16, 6, 1.75f);
        woolBody.setPos(0.0f, 5.0f, 2.0f);
        
        woolLeg1 = new ModelPart(0, 16); woolLeg1.addBox(-2.0f, 0.0f, -2.0f, 4, 6, 4, 0.5f); woolLeg1.setPos(-3.0f, 12.0f, 7.0f);
        woolLeg2 = new ModelPart(0, 16); woolLeg2.addBox(-2.0f, 0.0f, -2.0f, 4, 6, 4, 0.5f); woolLeg2.setPos(3.0f, 12.0f, 7.0f);
        woolLeg3 = new ModelPart(0, 16); woolLeg3.addBox(-2.0f, 0.0f, -2.0f, 4, 6, 4, 0.5f); woolLeg3.setPos(-3.0f, 12.0f, -5.0f);
        woolLeg4 = new ModelPart(0, 16); woolLeg4.addBox(-2.0f, 0.0f, -2.0f, 4, 6, 4, 0.5f); woolLeg4.setPos(3.0f, 12.0f, -5.0f);
    }
    
    @Override
    public void setupAnim(float time, float bob, float yRot, float xRot, float scale) {
        super.setupAnim(time, bob, yRot, xRot, scale);
        woolHead.pitch = head.pitch; woolHead.yaw = head.yaw;
        woolBody.pitch = body.pitch;
        woolLeg1.pitch = leg1.pitch; woolLeg2.pitch = leg2.pitch;
        woolLeg3.pitch = leg3.pitch; woolLeg4.pitch = leg4.pitch;
    }
    
    public void renderWool(float scale) {
        woolHead.render(scale);
        woolBody.render(scale);
        woolLeg1.render(scale);
        woolLeg2.render(scale);
        woolLeg3.render(scale);
        woolLeg4.render(scale);
    }
}
