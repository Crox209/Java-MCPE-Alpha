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
        
        int texIndexTop = BlockTextureMap.getTopTexture(blockId);
        int texIndexSide = BlockTextureMap.getSideTexture(blockId);
        int texIndexBottom = BlockTextureMap.getBottomTexture(blockId);
        
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
