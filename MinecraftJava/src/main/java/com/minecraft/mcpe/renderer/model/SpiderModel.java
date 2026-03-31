package com.minecraft.mcpe.renderer.model;

import com.minecraft.mcpe.entity.Entity;

public class SpiderModel extends Model {
    public ModelPart head, body0, body1;
    public ModelPart[] legs = new ModelPart[8];
    
    public SpiderModel() {
        float yo = 18.0f + 6.0f - 9.0f;
        head = new ModelPart(32, 4);
        head.addBox(-4.0f, -4.0f, -8.0f, 8, 8, 8, 0.0f);
        head.setPos(0.0f, yo, -3.0f);
        
        body0 = new ModelPart(0, 0);
        body0.addBox(-3.0f, -3.0f, -3.0f, 6, 6, 6, 0.0f);
        body0.setPos(0.0f, yo, 0.0f);
        
        body1 = new ModelPart(0, 12);
        body1.addBox(-5.0f, -4.0f, -6.0f, 10, 8, 12, 0.0f);
        body1.setPos(0.0f, yo, 9.0f);
        
        for (int i=0; i<8; i++) {
            legs[i] = new ModelPart(18, 0);
        }
        
        legs[0].addBox(-15.0f, -1.0f, -1.0f, 16, 2, 2, 0.0f); legs[0].setPos(-4.0f, yo, 2.0f);
        legs[1].addBox(-1.0f, -1.0f, -1.0f, 16, 2, 2, 0.0f); legs[1].setPos(4.0f, yo, 2.0f);
        legs[2].addBox(-15.0f, -1.0f, -1.0f, 16, 2, 2, 0.0f); legs[2].setPos(-4.0f, yo, 1.0f);
        legs[3].addBox(-1.0f, -1.0f, -1.0f, 16, 2, 2, 0.0f); legs[3].setPos(4.0f, yo, 1.0f);
        legs[4].addBox(-15.0f, -1.0f, -1.0f, 16, 2, 2, 0.0f); legs[4].setPos(-4.0f, yo, 0.0f);
        legs[5].addBox(-1.0f, -1.0f, -1.0f, 16, 2, 2, 0.0f); legs[5].setPos(4.0f, yo, 0.0f);
        legs[6].addBox(-15.0f, -1.0f, -1.0f, 16, 2, 2, 0.0f); legs[6].setPos(-4.0f, yo, -1.0f);
        legs[7].addBox(-1.0f, -1.0f, -1.0f, 16, 2, 2, 0.0f); legs[7].setPos(4.0f, yo, -1.0f);
    }
    
    @Override
    public void render(Entity entity, float time, float bob, float yRot, float xRot, float scale) {
        setupAnim(time, bob, yRot, xRot, scale);
        head.render(scale);
        body0.render(scale);
        body1.render(scale);
        for(ModelPart leg : legs) leg.render(scale);
    }
    
    @Override
    public void setupAnim(float time, float bob, float yRot, float xRot, float scale) {
        head.pitch = (float) Math.toRadians(xRot);
        head.yaw = (float) Math.toRadians(yRot);
        
        float sr = (float) Math.PI / 4.0f;
        legs[0].roll = -sr; legs[1].roll = sr;
        legs[2].roll = -sr * 0.74f; legs[3].roll = sr * 0.74f;
        legs[4].roll = -sr * 0.74f; legs[5].roll = sr * 0.74f;
        legs[6].roll = -sr; legs[7].roll = sr;
        
        float ur = (float) Math.PI / 8.0f;
        float ur2 = sr;
        
        legs[0].yaw = ur2; legs[1].yaw = -ur2;
        legs[2].yaw = ur; legs[3].yaw = -ur;
        legs[4].yaw = -ur; legs[5].yaw = ur;
        legs[6].yaw = -ur2; legs[7].yaw = ur2;
        
        float r = bob;
        float c0 = -(float) Math.cos(time * 0.6662f * 2.0f + Math.PI * 0.0f) * 0.4f * r;
        float c1 = -(float) Math.cos(time * 0.6662f * 2.0f + Math.PI * 1.0f) * 0.4f * r;
        float c2 = -(float) Math.cos(time * 0.6662f * 2.0f + Math.PI * 0.5f) * 0.4f * r;
        float c3 = -(float) Math.cos(time * 0.6662f * 2.0f + Math.PI * 1.5f) * 0.4f * r;
        
        float s0 = Math.abs((float) Math.sin(time * 0.6662f + Math.PI * 0.0f) * 0.4f) * r;
        float s1 = Math.abs((float) Math.sin(time * 0.6662f + Math.PI * 1.0f) * 0.4f) * r;
        float s2 = Math.abs((float) Math.sin(time * 0.6662f + Math.PI * 0.5f) * 0.4f) * r;
        float s3 = Math.abs((float) Math.sin(time * 0.6662f + Math.PI * 1.5f) * 0.4f) * r;
        
        legs[0].yaw += c0; legs[1].yaw -= c0;
        legs[2].yaw += c1; legs[3].yaw -= c1;
        legs[4].yaw += c2; legs[5].yaw -= c2;
        legs[6].yaw += c3; legs[7].yaw -= c3;
        
        legs[0].roll += s0; legs[1].roll -= s0;
        legs[2].roll += s1; legs[3].roll -= s1;
        legs[4].roll += s2; legs[5].roll -= s2;
        legs[6].roll += s3; legs[7].roll -= s3;
    }
}
