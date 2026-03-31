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
        
        GL11.glEnableClientState(GL11.GL_VERTEX_ARRAY);
        GL11.glEnableClientState(GL11.GL_TEXTURE_COORD_ARRAY);
        GL11.glEnableClientState(GL11.GL_COLOR_ARRAY);
        
        // 8 floats per vertex: r, g, b, u, v, x, y, z
        int stride = 8 * 4; // 4 bytes per float
        
        buffer.position(0);
        GL11.glColorPointer(3, GL11.GL_FLOAT, stride, buffer);
        
        buffer.position(3);
        GL11.glTexCoordPointer(2, GL11.GL_FLOAT, stride, buffer);
        
        buffer.position(5);
        GL11.glVertexPointer(3, GL11.GL_FLOAT, stride, buffer);
        
        GL11.glDrawArrays(GL11.GL_QUADS, 0, vertices);
        
        GL11.glDisableClientState(GL11.GL_VERTEX_ARRAY);
        GL11.glDisableClientState(GL11.GL_TEXTURE_COORD_ARRAY);
        GL11.glDisableClientState(GL11.GL_COLOR_ARRAY);
    }
}