package com.minecraft.mcpe.renderer.model;

import com.minecraft.mcpe.entity.Entity;

public class CowModel extends QuadrupedModel {
    public CowModel() {
        super(12);
        head = new ModelPart(0, 0);
        head.addBox(-4.0f, -4.0f, -6.0f, 8, 8, 6, 0.0f);
        head.setPos(0.0f, 4.0f, -8.0f);
        
        // Horns
        
        // It's fine to just use addBox on a new ModelPart if we need different UVs
        ModelPart horn1 = new ModelPart(22, 0);
        horn1.addBox(-5.0f, -5.0f, -4.0f, 1, 3, 1, 0.0f);
        head.addChild(horn1);
        
        ModelPart horn2 = new ModelPart(22, 0);
        horn2.addBox(4.0f, -5.0f, -4.0f, 1, 3, 1, 0.0f);
        head.addChild(horn2);
        
        body = new ModelPart(18, 4);
        body.addBox(-6.0f, -10.0f, -7.0f, 12, 18, 10, 0.0f);
        body.setPos(0.0f, 5.0f, 2.0f);
        
        ModelPart udder = new ModelPart(52, 0);
        udder.addBox(-2.0f, 2.0f, -8.0f, 4, 6, 1, 0.0f);
        body.addChild(udder);
        
        leg1.x -= 1.0f; leg1.z += 0.0f;
        leg2.x += 1.0f; leg2.z += 0.0f;
        leg3.x -= 1.0f; leg3.z -= 1.0f;
        leg4.x += 1.0f; leg4.z -= 1.0f;
        
        zHeadOffs += 2.0f;
    }
}
