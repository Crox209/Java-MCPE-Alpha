package com.minecraft.mcpe.renderer;

import com.minecraft.mcpe.entity.Entity;
import com.minecraft.mcpe.entity.Mob;
import com.minecraft.mcpe.util.Vector3f;
import com.minecraft.mcpe.world.Chunk;
import com.minecraft.mcpe.world.World;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.util.Collection;

/**
 * Simple 2D isometric renderer (for basic Swing rendering)
 * Can be extended to use OpenGL with LWJGL
 */
public class GameRenderer {
    private static final Logger logger = LoggerFactory.getLogger(GameRenderer.class);
    
    private int width;
    private int height;
    private double fov;
    private Vector3f cameraPos;
    private Vector3f cameraRot;

    public GameRenderer(int width, int height) {
        this.width = width;
        this.height = height;
        this.fov = 70.0;
        this.cameraPos = new Vector3f(0, 0, 0);
        this.cameraRot = new Vector3f(0, 0, 0);
    }

    public void setCameraPosition(Vector3f pos) {
        this.cameraPos = new Vector3f(pos);
    }

    public void setCameraRotation(Vector3f rot) {
        this.cameraRot = new Vector3f(rot);
    }

    public void setViewportSize(int width, int height) {
        this.width = Math.max(1, width);
        this.height = Math.max(1, height);
    }

    public void render(Graphics2D g, World world) {
        // Clear background
        g.setColor(new Color(135, 206, 250)); // Sky blue
        g.fillRect(0, 0, width, height);

        // Simple 2D isometric view
        renderTerrain(g, world);
        renderEntities(g, world);
        
        // Draw text info
        g.setColor(Color.WHITE);
        g.drawString("Camera: " + String.format("%.2f", cameraPos.x) + ", " + 
                     String.format("%.2f", cameraPos.y) + ", " + 
                     String.format("%.2f", cameraPos.z), 10, 20);
        g.drawString("Rotation: " + String.format("%.1f°", cameraRot.x) + ", " + 
                     String.format("%.1f°", cameraRot.y), 10, 40);
    }

    private void renderTerrain(Graphics2D g, World world) {
        Collection<Chunk> chunks = world.getLoadedChunks();
        
        int chunkX = (int) (cameraPos.x / Chunk.WIDTH);
        int chunkZ = (int) (cameraPos.z / Chunk.DEPTH);
        
        // Render chunks around camera
        for (int cx = chunkX - 2; cx <= chunkX + 2; cx++) {
            for (int cz = chunkZ - 2; cz <= chunkZ + 2; cz++) {
                Chunk chunk = world.getChunk(cx, cz);
                renderChunk(g, chunk, cx, cz);
            }
        }
    }

    private void renderEntities(Graphics2D g, World world) {
        for (Entity entity : world.getEntities()) {
            int screenX = width / 2 + (int) ((entity.getPosition().x - entity.getPosition().z) * 4 - cameraPos.x * 4 + cameraPos.z * 4);
            int screenY = height / 2 + (int) ((entity.getPosition().x + entity.getPosition().z) * 2 - entity.getPosition().y * 8 - cameraPos.x * 2 - cameraPos.z * 2 + cameraPos.y * 8);

            if (screenX < -24 || screenX > width + 24 || screenY < -24 || screenY > height + 24) {
                continue;
            }

            if (entity instanceof Mob) {
                g.setColor(new Color(30, 130, 30));
            } else {
                g.setColor(new Color(220, 220, 220));
            }
            g.fillOval(screenX - 4, screenY - 10, 10, 10);
            g.setColor(Color.BLACK);
            g.drawOval(screenX - 4, screenY - 10, 10, 10);
        }
    }

    private void renderChunk(Graphics2D g, Chunk chunk, int chunkX, int chunkZ) {
        // Isometric projection
        for (int x = 0; x < Chunk.WIDTH; x++) {
            for (int z = 0; z < Chunk.DEPTH; z++) {
                for (int y = Chunk.HEIGHT - 1; y >= 0; y--) {
                    int blockId = chunk.getBlock(x, y, z);
                    if (blockId != 0) { // Not air
                        renderBlock(g, chunkX, chunkZ, x, y, z, blockId);
                    }
                }
            }
        }
    }

    private void renderBlock(Graphics2D g, int chunkX, int chunkZ, 
                            int localX, int y, int localZ, int blockId) {
        // Convert world coordinates
        int worldX = chunkX * Chunk.WIDTH + localX;
        int worldZ = chunkZ * Chunk.DEPTH + localZ;

        // Simple isometric projection
        int screenX = width / 2 + (int) ((worldX - worldZ) * 4 - cameraPos.x * 4 + cameraPos.z * 4);
        int screenY = height / 2 + (int) ((worldX + worldZ) * 2 - y * 8 - cameraPos.x * 2 - cameraPos.z * 2 + cameraPos.y * 8);

        // Only render if on screen
        if (screenX > -20 && screenX < width + 20 && screenY > -20 && screenY < height + 20) {
            Color color = getBlockColor(blockId);
            g.setColor(color);
            g.fillRect(screenX, screenY, 8, 8);
            g.setColor(Color.DARK_GRAY);
            g.drawRect(screenX, screenY, 8, 8);
        }
    }

    private Color getBlockColor(int blockId) {
        switch (blockId) {
            case 1: return new Color(128, 128, 128); // Stone
            case 2: return new Color(34, 139, 34);   // Grass
            case 3: return new Color(139, 69, 19);   // Dirt
            case 4: return new Color(105, 105, 105); // Cobblestone
            case 5: return new Color(184, 134, 11);  // Wood
            case 12: return new Color(210, 180, 140); // Sand
            case 18: return new Color(0, 100, 0);    // Leaves
            case 7: return new Color(32, 32, 32);    // Bedrock
            default: return new Color(200, 200, 200);
        }
    }
}
