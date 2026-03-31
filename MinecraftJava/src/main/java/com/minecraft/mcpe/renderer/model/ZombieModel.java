package com.minecraft.mcpe.renderer.model;

import com.minecraft.mcpe.entity.Entity;

public class ZombieModel extends Model {
    public ModelPart head;
    public ModelPart headwear;
    public ModelPart body;
    public ModelPart rightArm;
    public ModelPart leftArm;
    public ModelPart rightLeg;
    public ModelPart leftLeg;
    
    public ZombieModel() {
        head = new ModelPart(0, 0);
        head.addBox(-4.0f, -8.0f, -4.0f, 8, 8, 8, 0.0f);
        head.setPos(0.0f, 0.0f, 0.0f);
        
        headwear = new ModelPart(32, 0);
        headwear.addBox(-4.0f, -8.0f, -4.0f, 8, 8, 8, 0.5f);
        headwear.setPos(0.0f, 0.0f, 0.0f);
        
        body = new ModelPart(16, 16);
        body.addBox(-4.0f, 0.0f, -2.0f, 8, 12, 4, 0.0f);
        body.setPos(0.0f, 0.0f, 0.0f);
        
        rightArm = new ModelPart(40, 16);
        rightArm.addBox(-3.0f, -2.0f, -2.0f, 4, 12, 4, 0.0f);
        rightArm.setPos(-5.0f, 2.0f, 0.0f);
        
        leftArm = new ModelPart(40, 16);
        leftArm.mirror = true;
        leftArm.addBox(-1.0f, -2.0f, -2.0f, 4, 12, 4, 0.0f);
        leftArm.setPos(5.0f, 2.0f, 0.0f);
        
        rightLeg = new ModelPart(0, 16);
        rightLeg.addBox(-2.0f, 0.0f, -2.0f, 4, 12, 4, 0.0f);
        rightLeg.setPos(-2.0f, 12.0f, 0.0f);
        
        leftLeg = new ModelPart(0, 16);
        leftLeg.mirror = true;
        leftLeg.addBox(-2.0f, 0.0f, -2.0f, 4, 12, 4, 0.0f);
        leftLeg.setPos(2.0f, 12.0f, 0.0f);
    }
    
    @Override
    public void render(Entity entity, float time, float bob, float yRot, float xRot, float scale) {
        setupAnim(time, bob, yRot, xRot, scale);
        head.render(scale);
        body.render(scale);
        rightArm.render(scale);
        leftArm.render(scale);
        rightLeg.render(scale);
        leftLeg.render(scale);
    }
    
    @Override
    public void setupAnim(float time, float bob, float yRot, float xRot, float scale) {
        head.yaw = (float) Math.toRadians(yRot);
        head.pitch = (float) Math.toRadians(xRot);
        headwear.yaw = head.yaw;
        headwear.pitch = head.pitch;
        
        rightArm.pitch = (float) Math.toRadians(-90.0f) + (float) Math.cos(time * 0.6662f) * 1.4f * bob;
        leftArm.pitch = (float) Math.toRadians(-90.0f) - (float) Math.cos(time * 0.6662f) * 1.4f * bob;
        
        rightLeg.pitch = (float) Math.cos(time * 0.6662f) * 1.4f * bob;
        leftLeg.pitch = (float) -Math.cos(time * 0.6662f) * 1.4f * bob;
    }
}
