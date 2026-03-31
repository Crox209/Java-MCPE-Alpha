package com.minecraft.mcpe.renderer.model;

import static org.lwjgl.opengl.GL11.*;
import java.util.ArrayList;
import java.util.List;

public class ModelPart {
    public float x, y, z;
    public float pitch, yaw, roll;
    
    public boolean mirror = false;
    public boolean visible = true;
    public boolean hidden = false;
    
    // UV origins
    private int u, v;
    private int texWidth = 64;
    private int texHeight = 32;
    
    private boolean compiled = false;
    private int displayList;
    
    private List<Box> boxes = new ArrayList<>();
    private List<ModelPart> children = new ArrayList<>();
    public void addChild(ModelPart part) { children.add(part); }

    
    public ModelPart(int u, int v) {
        this.u = u;
        this.v = v;
    }
    
    public void setPos(float x, float y, float z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }
    
    public void addBox(float ox, float oy, float oz, int w, int h, int d, float expand) {
        boxes.add(new Box(this.u, this.v, ox, oy, oz, w, h, d, expand, this.texWidth, this.texHeight, this.mirror));
    }
    
    public void render(float scale) {
        if (hidden || !visible) return;
        
        if (!compiled) {
            compile(scale);
        }
        
        glTranslatef(x * scale, y * scale, z * scale);
        
        if (roll != 0.0f) glRotatef((float) Math.toDegrees(roll), 0.0f, 0.0f, 1.0f);
        if (yaw != 0.0f) glRotatef((float) Math.toDegrees(yaw), 0.0f, 1.0f, 0.0f);
        if (pitch != 0.0f) glRotatef((float) Math.toDegrees(pitch), 1.0f, 0.0f, 0.0f);
        
        glCallList(displayList);
        
        if (children != null) {
            for (ModelPart child : children) {
                child.render(scale);
            }
        }
        
        if (pitch != 0.0f) glRotatef((float) Math.toDegrees(-pitch), 1.0f, 0.0f, 0.0f);
        if (yaw != 0.0f) glRotatef((float) Math.toDegrees(-yaw), 0.0f, 1.0f, 0.0f);
        if (roll != 0.0f) glRotatef((float) Math.toDegrees(-roll), 0.0f, 0.0f, 1.0f);
        
        glTranslatef(-x * scale, -y * scale, -z * scale);
    }
    
    private void compile(float scale) {
        displayList = glGenLists(1);
        glNewList(displayList, GL_COMPILE);
        
        glBegin(GL_QUADS);
        for (Box b : boxes) {
            b.render(scale);
        }
        glEnd();
        
        glEndList();
        compiled = true;
    }
    
    private static class Box {
        private float[] vertices;
        private float[] uvs;
        
        public Box(int u, int v, float ox, float oy, float oz, int w, int h, int d, float expand, float texW, float texH, boolean mirror) {
            float x1 = ox;
            float y1 = oy;
            float z1 = oz;
            float x2 = ox + w;
            float y2 = oy + h;
            float z2 = oz + d;
            
            x1 -= expand;
            y1 -= expand;
            z1 -= expand;
            x2 += expand;
            y2 += expand;
            z2 += expand;
            
            if (mirror) {
                float tmp = x1; x1 = x2; x2 = tmp;
            }
            
            // Generate cubes correctly... Simplified version
            this.vertices = new float[] {
                // Front
                x1, y1, z2, x2, y1, z2, x2, y2, z2, x1, y2, z2,
                // Back
                x2, y1, z1, x1, y1, z1, x1, y2, z1, x2, y2, z1,
                // Top
                x1, y2, z2, x2, y2, z2, x2, y2, z1, x1, y2, z1,
                // Bottom
                x1, y1, z1, x2, y1, z1, x2, y1, z2, x1, y1, z2,
                // Left
                x1, y1, z1, x1, y1, z2, x1, y2, z2, x1, y2, z1,
                // Right
                x2, y1, z2, x2, y1, z1, x2, y2, z1, x2, y2, z2
            };
            
            // Texture math (simplified Minecraft mapping)
            float uw = 1.0f / texW;
            float uh = 1.0f / texH;
            
            // Format: u, v for each vertex of each face, following the box mapping pattern
            this.uvs = new float[12 * 6];
            
            // Left/Right: depth x height
            // Top/Bottom: width x depth
            // Front/Back: width x height
            
            // Front (u+d .. u+d+w, v+d .. v+d+h)
            fillUVArray(uvs, 0, (u+d)*uw, (u+d+w)*uw, (v+d)*uh, (v+d+h)*uh, mirror);
            // Back (u+d+w+d .. u+d+w+d+w, v+d .. v+d+h)
            fillUVArray(uvs, 8, (u+d+w+d)*uw, (u+d+w+d+w)*uw, (v+d)*uh, (v+d+h)*uh, mirror);
            // Top (u+d .. u+d+w, v .. v+d)
            fillUVArray(uvs, 16, (u+d)*uw, (u+d+w)*uw, v*uh, (v+d)*uh, mirror);
            // Bottom (u+d+w .. u+d+w+w, v .. v+d)
            fillUVArray(uvs, 24, (u+d+w)*uw, (u+d+w+w)*uw, (v)*uh, (v+d)*uh, mirror);
            // Left (u .. u+d, v+d .. v+d+h)
            fillUVArray(uvs, 32, u*uw, (u+d)*uw, (v+d)*uh, (v+d+h)*uh, mirror);
            // Right (u+d+w .. u+d+w+d, v+d .. v+d+h)
            fillUVArray(uvs, 40, (u+d+w)*uw, (u+d+w+d)*uw, (v+d)*uh, (v+d+h)*uh, mirror);
        }
        
        private void fillUVArray(float[] uvs, int offset, float u1, float u2, float v1, float v2, boolean mirror) {
            if (mirror) {
                float tmp = u1; u1 = u2; u2 = tmp;
            }
            uvs[offset] = u1; uvs[offset+1] = v2;
            uvs[offset+2] = u2; uvs[offset+3] = v2;
            uvs[offset+4] = u2; uvs[offset+5] = v1;
            uvs[offset+6] = u1; uvs[offset+7] = v1;
        }
        
        public void render(float scale) {
            for (int i = 0; i < 6; i++) {
                int vo = i * 12;
                int uo = i * 8;
                
                glTexCoord2f(uvs[uo], uvs[uo+1]);
                glVertex3f(vertices[vo] * scale, vertices[vo+1] * scale, vertices[vo+2] * scale);
                
                glTexCoord2f(uvs[uo+2], uvs[uo+3]);
                glVertex3f(vertices[vo+3] * scale, vertices[vo+4] * scale, vertices[vo+5] * scale);
                
                glTexCoord2f(uvs[uo+4], uvs[uo+5]);
                glVertex3f(vertices[vo+6] * scale, vertices[vo+7] * scale, vertices[vo+8] * scale);
                
                glTexCoord2f(uvs[uo+6], uvs[uo+7]);
                glVertex3f(vertices[vo+9] * scale, vertices[vo+10] * scale, vertices[vo+11] * scale);
            }
        }
    }
}
