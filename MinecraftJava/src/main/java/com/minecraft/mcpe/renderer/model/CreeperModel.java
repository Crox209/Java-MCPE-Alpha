package com.minecraft.mcpe.renderer.model;

import com.minecraft.mcpe.entity.Entity;

public class CreeperModel extends Model {
    public ModelPart head, body, leg1, leg2, leg3, leg4;
    
    public CreeperModel() {
        float yo = 4.0f;
        head = new ModelPart(0, 0);
        head.addBox(-4.0f, -8.0f, -4.0f, 8, 8, 8, 0.0f);
        head.setPos(0.0f, yo, 0.0f);
        
        body = new ModelPart(16, 16);
        body.addBox(-4.0f, 0.0f, -2.0f, 8, 12, 4, 0.0f);
        body.setPos(0.0f, yo, 0.0f);
        
        leg1 = new ModelPart(0, 16);
        leg1.addBox(-2.0f, 0.0f, -2.0f, 4, 6, 4, 0.0f);
        leg1.setPos(-2.0f, 12.0f + yo, 4.0f);
        
        leg2 = new ModelPart(0, 16);
        leg2.addBox(-2.0f, 0.0f, -2.0f, 4, 6, 4, 0.0f);
        leg2.setPos(2.0f, 12.0f + yo, 4.0f);
        
        leg3 = new ModelPart(0, 16);
        leg3.addBox(-2.0f, 0.0f, -2.0f, 4, 6, 4, 0.0f);
        leg3.setPos(-2.0f, 12.0f + yo, -4.0f);
        
        leg4 = new ModelPart(0, 16);
        leg4.addBox(-2.0f, 0.0f, -2.0f, 4, 6, 4, 0.0f);
        leg4.setPos(2.0f, 12.0f + yo, -4.0f);
    }
    
    @Override
    public void render(Entity entity, float time, float bob, float yRot, float xRot, float scale) {
        setupAnim(time, bob, yRot, xRot, scale);
        head.render(scale);
        body.render(scale);
        leg1.render(scale);
        leg2.render(scale);
        leg3.render(scale);
        leg4.render(scale);
    }
    
    @Override
    public void setupAnim(float time, float bob, float yRot, float xRot, float scale) {
        head.pitch = (float) Math.toRadians(xRot);
        head.yaw = (float) Math.toRadians(yRot);
        float cos = (float) Math.cos(time * 0.6662f) * 1.4f * bob;
        leg1.pitch = cos;
        leg2.pitch = -cos;
        leg3.pitch = -cos;
        leg4.pitch = cos;
    }
}
