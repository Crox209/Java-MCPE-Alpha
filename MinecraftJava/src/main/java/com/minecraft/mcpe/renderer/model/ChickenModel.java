package com.minecraft.mcpe.renderer.model;

import com.minecraft.mcpe.entity.Entity;

public class ChickenModel extends Model {
    public ModelPart head, beak, redThing, body, leg1, leg2, wing1, wing2;
    
    public ChickenModel() {
        int yo = 16;
        head = new ModelPart(0, 0);
        head.addBox(-2.0f, -6.0f, -2.0f, 4, 6, 3, 0.0f);
        head.setPos(0.0f, -1.0f + yo, -4.0f);
        
        beak = new ModelPart(14, 0);
        beak.addBox(-2.0f, -4.0f, -4.0f, 4, 2, 2, 0.0f);
        beak.setPos(0.0f, -1.0f + yo, -4.0f);
        
        redThing = new ModelPart(14, 4);
        redThing.addBox(-1.0f, -2.0f, -3.0f, 2, 2, 2, 0.0f);
        redThing.setPos(0.0f, -1.0f + yo, -4.0f);
        
        body = new ModelPart(0, 9);
        body.addBox(-3.0f, -4.0f, -3.0f, 6, 8, 6, 0.0f);
        body.setPos(0.0f, 0.0f + yo, 0.0f);
        
        leg1 = new ModelPart(26, 0);
        leg1.addBox(-1.0f, 0.0f, -3.0f, 3, 5, 3, 0.0f);
        leg1.setPos(-2.0f, 3.0f + yo, 1.0f);
        
        leg2 = new ModelPart(26, 0);
        leg2.addBox(-1.0f, 0.0f, -3.0f, 3, 5, 3, 0.0f);
        leg2.setPos(1.0f, 3.0f + yo, 1.0f);
        
        wing1 = new ModelPart(24, 13);
        wing1.addBox(0.0f, 0.0f, -3.0f, 1, 4, 6, 0.0f);
        wing1.setPos(-4.0f, -3.0f + yo, 0.0f);
        
        wing2 = new ModelPart(24, 13);
        wing2.addBox(-1.0f, 0.0f, -3.0f, 1, 4, 6, 0.0f);
        wing2.setPos(4.0f, -3.0f + yo, 0.0f);
    }
    
    @Override
    public void render(Entity entity, float time, float bob, float yRot, float xRot, float scale) {
        setupAnim(time, bob, yRot, xRot, scale);
        head.render(scale);
        beak.render(scale);
        redThing.render(scale);
        body.render(scale);
        leg1.render(scale);
        leg2.render(scale);
        wing1.render(scale);
        wing2.render(scale);
    }
    
    @Override
    public void setupAnim(float time, float bob, float yRot, float xRot, float scale) {
        head.pitch = (float) Math.toRadians(xRot);
        head.yaw = (float) Math.toRadians(yRot);
        beak.pitch = head.pitch; beak.yaw = head.yaw;
        redThing.pitch = head.pitch; redThing.yaw = head.yaw;
        body.pitch = (float) Math.toRadians(90.0f);
        
        float cos = (float) Math.cos(time * 0.6662f) * 1.4f * bob;
        leg1.pitch = cos;
        leg2.pitch = -cos;
        wing1.roll = bob;
        wing2.roll = -bob;
    }
}
