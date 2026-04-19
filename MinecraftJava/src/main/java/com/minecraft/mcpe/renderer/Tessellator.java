package com.minecraft.mcpe.renderer;

import org.lwjgl.opengl.GL11;
import java.nio.FloatBuffer;
import org.lwjgl.system.MemoryUtil;

public class Tessellator {
    private static Tessellator instance = new Tessellator();
    private FloatBuffer buffer;
    private int vertices = 0;
    private boolean isDrawing = false;
    
    // Texture coordinates
    private float u, v;
    
    // Colors
    private float r = 1.0f, g = 1.0f, b = 1.0f;
    
    // Position offset
    private float xo, yo, zo;

    public static Tessellator getInstance() {
        return instance;
    }

    private Tessellator() {
        buffer = MemoryUtil.memAllocFloat(3000000); // Allow lots of vertices
    }

    public void startDrawing() {
        buffer.clear();
        vertices = 0;
        isDrawing = true;
    }

    public void color(float r, float g, float b) {
        this.r = r;
        this.g = g;
        this.b = b;
    }

    public void setTextureUV(float u, float v) {
        this.u = u;
        this.v = v;
    }

    public void addTranslation(float x, float y, float z) {
        this.xo = x;
        this.yo = y;
        this.zo = z;
    }

    public void addVertexWithUV(float x, float y, float z, float u, float v) {
        buffer.put(r);
        buffer.put(g);
        buffer.put(b);
        buffer.put(u);
        buffer.put(v);
        buffer.put(x + xo);
        buffer.put(y + yo);
        buffer.put(z + zo);
        vertices++;
    }

    public void draw() {
        if (!isDrawing) return;
        isDrawing = false;
        if (vertices == 0) return;
        
        buffer.flip();
        
        GL11.glBegin(GL11.GL_QUADS);
        for(int i = 0; i < vertices; i++) {
            int o = i * 8;
            GL11.glColor3f(buffer.get(o), buffer.get(o+1), buffer.get(o+2));
            GL11.glTexCoord2f(buffer.get(o+3), buffer.get(o+4));
            GL11.glVertex3f(buffer.get(o+5), buffer.get(o+6), buffer.get(o+7));
        }
        GL11.glEnd();
    }
}