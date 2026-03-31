package com.minecraft.mcpe.renderer.model;

import com.minecraft.mcpe.entity.Entity;

public abstract class Model {
    public float onGround;
    public boolean riding;
    
    public abstract void render(Entity entity, float time, float bob, float yRot, float xRot, float scale);
    public abstract void setupAnim(float time, float bob, float yRot, float xRot, float scale);
}
