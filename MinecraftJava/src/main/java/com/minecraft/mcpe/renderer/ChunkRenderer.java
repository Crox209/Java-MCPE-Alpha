package com.minecraft.mcpe.renderer;

import com.minecraft.mcpe.world.Chunk;
import com.minecraft.mcpe.block.Block;
import org.lwjgl.opengl.GL11;

public class ChunkRenderer {
    private Chunk chunk;
    private int displayList = -1;
    private boolean isDirty = true;
    
    public ChunkRenderer(Chunk chunk) {
        this.chunk = chunk;
    }
    
    public Chunk getChunk() { return chunk; }

    public void markDirty() {
        this.isDirty = true;
    }
    
    public void compile() {
        if (displayList == -1) {
            displayList = GL11.glGenLists(1);
        }
        
        GL11.glNewList(displayList, GL11.GL_COMPILE);
        
        Tessellator t = Tessellator.getInstance();
        t.startDrawing();
        
        int startX = chunk.getX() * Chunk.WIDTH;
        int startZ = chunk.getZ() * Chunk.DEPTH;
        
        for (int x = 0; x < Chunk.WIDTH; x++) {
            for (int y = 0; y < Chunk.HEIGHT; y++) {
                for (int z = 0; z < Chunk.DEPTH; z++) {
                    int blockId = chunk.getBlock(x, y, z);
                    
                    if (blockId != Block.AIR) {
                        int worldX = startX + x;
                        int worldZ = startZ + z;
                        
                        renderBlock(t, chunk, x, y, z, worldX, y, worldZ, blockId);
                    }
                }
            }
        }
        
        t.draw();
        GL11.glEndList();
        this.isDirty = false;
    }
    
    private void renderBlock(Tessellator t, Chunk c, int lx, int ly, int lz, int wx, int wy, int wz, int blockId) {
        // Texture coordinate logic (Very simple for now. terrain.png is 256x256, blocks are 16x16)
        // Grass top: (0,0) index 0
        // Grass side: (3,0) index 3
        // Dirt: (2,0) index 2
        // Stone: (1,0) index 1
        
        int texIndexTop = 0;
        int texIndexSide = 0;
        int texIndexBottom = 0;
        
        if (blockId == Block.GRASS) {
            texIndexTop = 0;
            texIndexSide = 3;
            texIndexBottom = 2;
        } else if (blockId == Block.DIRT) {
            texIndexTop = 2; texIndexSide = 2; texIndexBottom = 2;
        } else if (blockId == Block.STONE) {
            texIndexTop = 1; texIndexSide = 1; texIndexBottom = 1;
        } else if (blockId == Block.COBBLESTONE) {
            texIndexTop = 16; texIndexSide = 16; texIndexBottom = 16;
        } else if (blockId == Block.WOOD) { // Log
            texIndexTop = 21; texIndexSide = 20; texIndexBottom = 21;
        } else if (blockId == Block.LEAVES) {
            texIndexTop = 52; texIndexSide = 52; texIndexBottom = 52; 
        } else if (blockId == Block.SAND) {
            texIndexTop = 18; texIndexSide = 18; texIndexBottom = 18;
        } else {
            // Default generic texture index for unknown
            texIndexTop = 1; texIndexSide = 1; texIndexBottom = 1;
        }
        
        t.addTranslation(wx, wy, wz);
        
        float tw = 1.0f / 16.0f;
        
        // Simple Culling
        boolean renderTop = ly == Chunk.HEIGHT - 1 || c.getBlock(lx, ly + 1, lz) == Block.AIR || c.getBlock(lx, ly + 1, lz) == Block.LEAVES;
        boolean renderBottom = ly == 0 || c.getBlock(lx, ly - 1, lz) == Block.AIR || c.getBlock(lx, ly - 1, lz) == Block.LEAVES;
        boolean renderLeft = lx == 0 || c.getBlock(lx - 1, ly, lz) == Block.AIR || c.getBlock(lx - 1, ly, lz) == Block.LEAVES;
        boolean renderRight = lx == Chunk.WIDTH - 1 || c.getBlock(lx + 1, ly, lz) == Block.AIR || c.getBlock(lx + 1, ly, lz) == Block.LEAVES;
        boolean renderFront = lz == Chunk.DEPTH - 1 || c.getBlock(lx, ly, lz + 1) == Block.AIR || c.getBlock(lx, ly, lz + 1) == Block.LEAVES;
        boolean renderBack = lz == 0 || c.getBlock(lx, ly, lz - 1) == Block.AIR || c.getBlock(lx, ly, lz - 1) == Block.LEAVES;

        // TOP
        if (renderTop) {
            t.color(1.0f, 1.0f, 1.0f);
            float u0 = (texIndexTop % 16) * tw;
            float v0 = (texIndexTop / 16) * tw;
            // Hack for early grass color
            if (blockId == Block.GRASS) t.color(0.5f, 0.85f, 0.4f);
            t.addVertexWithUV(-0.5f, 0.5f, -0.5f, u0, v0 + tw);
            t.addVertexWithUV(-0.5f, 0.5f,  0.5f, u0, v0);
            t.addVertexWithUV( 0.5f, 0.5f,  0.5f, u0 + tw, v0);
            t.addVertexWithUV( 0.5f, 0.5f, -0.5f, u0 + tw, v0 + tw);
            if (blockId == Block.GRASS) t.color(1.0f, 1.0f, 1.0f);
        }
        
        // BOTTOM
        if (renderBottom) {
            t.color(0.5f, 0.5f, 0.5f);
            float u0 = (texIndexBottom % 16) * tw;
            float v0 = (texIndexBottom / 16) * tw;
            t.addVertexWithUV(-0.5f, -0.5f, -0.5f, u0 + tw, v0 + tw);
            t.addVertexWithUV( 0.5f, -0.5f, -0.5f, u0, v0 + tw);
            t.addVertexWithUV( 0.5f, -0.5f,  0.5f, u0, v0);
            t.addVertexWithUV(-0.5f, -0.5f,  0.5f, u0 + tw, v0);
        }

        // FRONT (z + 0.5)
        if (renderFront) {
            t.color(0.8f, 0.8f, 0.8f);
            float u0 = (texIndexSide % 16) * tw;
            float v0 = (texIndexSide / 16) * tw;
            t.addVertexWithUV(-0.5f, -0.5f, 0.5f, u0, v0 + tw);
            t.addVertexWithUV( 0.5f, -0.5f, 0.5f, u0 + tw, v0 + tw);
            t.addVertexWithUV( 0.5f,  0.5f, 0.5f, u0 + tw, v0);
            t.addVertexWithUV(-0.5f,  0.5f, 0.5f, u0, v0);
        }

        // BACK (z - 0.5)
        if (renderBack) {
            t.color(0.8f, 0.8f, 0.8f);
            float u0 = (texIndexSide % 16) * tw;
            float v0 = (texIndexSide / 16) * tw;
            t.addVertexWithUV( 0.5f, -0.5f, -0.5f, u0, v0 + tw);
            t.addVertexWithUV(-0.5f, -0.5f, -0.5f, u0 + tw, v0 + tw);
            t.addVertexWithUV(-0.5f,  0.5f, -0.5f, u0 + tw, v0);
            t.addVertexWithUV( 0.5f,  0.5f, -0.5f, u0, v0);
        }

        // LEFT (x - 0.5)
        if (renderLeft) {
            t.color(0.6f, 0.6f, 0.6f);
            float u0 = (texIndexSide % 16) * tw;
            float v0 = (texIndexSide / 16) * tw;
            t.addVertexWithUV(-0.5f, -0.5f, -0.5f, u0, v0 + tw);
            t.addVertexWithUV(-0.5f, -0.5f,  0.5f, u0 + tw, v0 + tw);
            t.addVertexWithUV(-0.5f,  0.5f,  0.5f, u0 + tw, v0);
            t.addVertexWithUV(-0.5f,  0.5f, -0.5f, u0, v0);
        }

        // RIGHT (x + 0.5)
        if (renderRight) {
            t.color(0.6f, 0.6f, 0.6f);
            float u0 = (texIndexSide % 16) * tw;
            float v0 = (texIndexSide / 16) * tw;
            t.addVertexWithUV( 0.5f, -0.5f,  0.5f, u0, v0 + tw);
            t.addVertexWithUV( 0.5f, -0.5f, -0.5f, u0 + tw, v0 + tw);
            t.addVertexWithUV( 0.5f,  0.5f, -0.5f, u0 + tw, v0);
            t.addVertexWithUV( 0.5f,  0.5f,  0.5f, u0, v0);
        }
        
        t.addTranslation(-wx, -wy, -wz); // reset translation for the next block
    }
    
    public void render() {
        if (isDirty) {
            compile();
        }
        if (displayList != -1) {
            GL11.glCallList(displayList);
        }
    }
}
