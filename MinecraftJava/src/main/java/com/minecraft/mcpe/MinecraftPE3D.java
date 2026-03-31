
package com.minecraft.mcpe;

import com.minecraft.mcpe.renderer.Tessellator;
import com.minecraft.mcpe.block.Block;

import org.lwjgl.glfw.*;
import org.lwjgl.opengl.*;
import org.lwjgl.system.*;
import org.lwjgl.stb.STBImage;

import com.minecraft.mcpe.world.World;
import com.minecraft.mcpe.world.Chunk;
import com.minecraft.mcpe.renderer.ChunkRenderer;
import com.minecraft.mcpe.entity.Mob;
import com.minecraft.mcpe.entity.Player;
import com.minecraft.mcpe.entity.Cow;
import com.minecraft.mcpe.entity.Sheep;
import com.minecraft.mcpe.entity.Chicken;
import com.minecraft.mcpe.entity.Spider;
import com.minecraft.mcpe.entity.Creeper;
import com.minecraft.mcpe.entity.Skeleton;
import com.minecraft.mcpe.renderer.model.CowModel;
import com.minecraft.mcpe.renderer.model.SheepModel;
import com.minecraft.mcpe.renderer.model.ChickenModel;
import com.minecraft.mcpe.renderer.model.CreeperModel;
import com.minecraft.mcpe.renderer.model.SpiderModel;
import com.minecraft.mcpe.renderer.model.SkeletonModel;
import com.minecraft.mcpe.entity.Pig;
import com.minecraft.mcpe.entity.Zombie;
import com.minecraft.mcpe.entity.Entity;
import com.minecraft.mcpe.renderer.model.PigModel;
import com.minecraft.mcpe.renderer.model.ZombieModel;
import com.minecraft.mcpe.renderer.model.Model;
import java.util.ArrayList;
import java.util.List;


import java.nio.*;
import java.io.InputStream;
import java.io.IOException;
import java.io.ByteArrayOutputStream;
import java.net.URL;

import static org.lwjgl.glfw.Callbacks.*;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.system.MemoryStack.*;
import static org.lwjgl.system.MemoryUtil.*;

import java.util.List;
import java.util.ArrayList;

public class MinecraftPE3D {
    private float bobPhase = 0.0f;
    private float bobO = 0.0f;

    private World world;
    private List<ChunkRenderer> chunkRenderers = new ArrayList<>();
    private long window;
    private int width = 1280;
    private int height = 720;

    // Camera & Physics
    private double yd = 0.0;
    private boolean onGround = false;
    private float camX = 0.0f, camY = 80.0f, camZ = 5.0f;
    private float yaw = 0.0f, pitch = 0.0f;

    // Input state
    private boolean[] keys = new boolean[1024];

    private int terrainTexture;
    private int guiTexture;
    private int selectedHotbarSlot = 0;
    private int[] hotbarBlocks = {1, 4, 3, 5, 17, 20, 45, 87, 35}; // Stone, Cobble, Dirt, Wood, Log, Glass, Netherrack, Netherrack, Wool
    private int iconsTexture;
    private FontRenderer fontRenderer;
    private List<Entity> entities = new ArrayList<>();
    private Player localPlayer;
    private Model pigModel = new PigModel();
    private Model zombieModel = new ZombieModel();
    private Model cowModel = new CowModel();
    private Model sheepModel = new SheepModel();
    private Model chickenModel = new ChickenModel();
    private Model creeperModel = new CreeperModel();
    private Model spiderModel = new SpiderModel();
    private Model skeletonModel = new SkeletonModel();
    
    private int cowTexture;
    private int sheepTexture;
    private int sheepFurTexture;
    private int chickenTexture;
    private int creeperTexture;
    private int spiderTexture;
    private int skeletonTexture;

    private int pigTexture;
    private int zombieTexture;


    public void run() {
        System.out.println("Starting Minecraft PE 3D (LWJGL " + org.lwjgl.Version.getVersion() + ")!");

        init();
                // Spawn some pigs and zombies
        for (int i = 0; i < 5; i++) {
            Pig pig = new Pig(world);
            pig.getPosition().x = camX + (Math.random() * 10 - 5); pig.getPosition().y = camY + 2; pig.getPosition().z = camZ + (Math.random() * 10 - 5);
            entities.add(pig);
        }
        for (int i = 0; i < 3; i++) {
            Zombie zombie = new Zombie(world);
            zombie.getPosition().x = camX + (Math.random() * 10 - 5); zombie.getPosition().y = camY + 2; zombie.getPosition().z = camZ + (Math.random() * 10 - 5);
            entities.add(zombie);
        }
        for (int i = 0; i < 2; i++) {
            Cow cow = new Cow(world); cow.getPosition().x = camX + (Math.random() * 10 - 5); cow.getPosition().y = camY + 2; cow.getPosition().z = camZ + (Math.random() * 10 - 5); entities.add(cow);
            Sheep sheep = new Sheep(world); sheep.getPosition().x = camX + (Math.random() * 10 - 5); sheep.getPosition().y = camY + 2; sheep.getPosition().z = camZ + (Math.random() * 10 - 5); entities.add(sheep);
            Chicken chicken = new Chicken(world); chicken.getPosition().x = camX + (Math.random() * 10 - 5); chicken.getPosition().y = camY + 2; chicken.getPosition().z = camZ + (Math.random() * 10 - 5); entities.add(chicken);
            Creeper creeper = new Creeper(world); creeper.getPosition().x = camX + (Math.random() * 10 - 5); creeper.getPosition().y = camY + 2; creeper.getPosition().z = camZ + (Math.random() * 10 - 5); entities.add(creeper);
            Spider spider = new Spider(world); spider.getPosition().x = camX + (Math.random() * 10 - 5); spider.getPosition().y = camY + 2; spider.getPosition().z = camZ + (Math.random() * 10 - 5); entities.add(spider);
            Skeleton skeleton = new Skeleton(world); skeleton.getPosition().x = camX + (Math.random() * 10 - 5); skeleton.getPosition().y = camY + 2; skeleton.getPosition().z = camZ + (Math.random() * 10 - 5); entities.add(skeleton);
        }


        loop();

        glfwFreeCallbacks(window);
        glfwDestroyWindow(window);
        glfwTerminate();
        glfwSetErrorCallback(null).free();
    }

    private void init() {
        GLFWErrorCallback.createPrint(System.err).set();

        if (!glfwInit()) throw new IllegalStateException("Unable to initialize GLFW");

        glfwDefaultWindowHints();
        glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE);
        glfwWindowHint(GLFW_RESIZABLE, GLFW_TRUE);

        window = glfwCreateWindow(width, height, "Minecraft PE - Java Edition (3D Alpha)", NULL, NULL);
        if (window == NULL) throw new RuntimeException("Failed to create the GLFW window");

        glfwSetKeyCallback(window, (window, key, scancode, action, mods) -> {
            if (key == GLFW_KEY_ESCAPE && action == GLFW_RELEASE) {
                glfwSetWindowShouldClose(window, true);
            }
            if (key >= 0 && key < 1024) {
                if (action == GLFW_PRESS) keys[key] = true;
                else if (action == GLFW_RELEASE) keys[key] = false;
            }
        });

        // Mouse look callback
        final double[] lastMouseX = {width / 2.0};
        final double[] lastMouseY = {height / 2.0};
        final boolean[] firstMouse = {true};

                // Scroll callback for hotbar selection
        glfwSetScrollCallback(window, (window, xoffset, yoffset) -> {
            if (yoffset > 0) {
                selectedHotbarSlot = (selectedHotbarSlot - 1);
                if (selectedHotbarSlot < 0) selectedHotbarSlot = 8;
            } else if (yoffset < 0) {
                selectedHotbarSlot = (selectedHotbarSlot + 1) % 9;
            }
        });

        glfwSetCursorPosCallback(window, (window, xpos, ypos) -> {
            if (firstMouse[0]) {
                lastMouseX[0] = xpos;
                lastMouseY[0] = ypos;
                firstMouse[0] = false;
            }

            double xoffset = xpos - lastMouseX[0];
            double yoffset = lastMouseY[0] - ypos; // reversed since y-coordinates go from bottom to top
            lastMouseX[0] = xpos;
            lastMouseY[0] = ypos;

            float sensitivity = 0.1f;
            yaw += xoffset * sensitivity;
            pitch += yoffset * sensitivity;

            if (pitch > 89.0f) pitch = 89.0f;
            if (pitch < -89.0f) pitch = -89.0f;
        });

        glfwSetMouseButtonCallback(window, (win, button, action, mods) -> {
            if (action == GLFW_PRESS) {
                if (button == GLFW_MOUSE_BUTTON_LEFT) {
                    interactBlock(false); // Break block
                } else if (button == GLFW_MOUSE_BUTTON_RIGHT) {
                    interactBlock(true); // Place cobblestone
                }
            }
        });

        glfwSetInputMode(window, GLFW_CURSOR, GLFW_CURSOR_DISABLED); // Lock mouse

        glfwSetFramebufferSizeCallback(window, (window, w, h) -> {
            this.width = w;
            this.height = h;
            glViewport(0, 0, w, h);
            setupProjection();
        });

        try (MemoryStack stack = stackPush()) {
            IntBuffer pWidth = stack.mallocInt(1);
            IntBuffer pHeight = stack.mallocInt(1);
            glfwGetWindowSize(window, pWidth, pHeight);
            GLFWVidMode vidmode = glfwGetVideoMode(glfwGetPrimaryMonitor());
            glfwSetWindowPos(window, (vidmode.width() - pWidth.get(0)) / 2, (vidmode.height() - pHeight.get(0)) / 2);
        }

        glfwMakeContextCurrent(window);
        glfwSwapInterval(1);
        glfwShowWindow(window);
        
        System.out.println("Generating World...");
        world = new World("world", 12345L);
        // The World generates chunks around 0,0 let's grab them and render
        for (Chunk c : world.getLoadedChunks()) {
            ChunkRenderer cr = new ChunkRenderer(c);
            cr.compile();
            chunkRenderers.add(cr);
        }
        System.out.println("Finished generating!");
    }

        private int loadGenericTexture(String path) {
        try (org.lwjgl.system.MemoryStack stack = org.lwjgl.system.MemoryStack.stackPush()) {
            java.nio.IntBuffer w = stack.mallocInt(1);
            java.nio.IntBuffer h = stack.mallocInt(1);
            java.nio.IntBuffer comp = stack.mallocInt(1);

            java.net.URL resource = MinecraftPE3D.class.getResource(path);
            if (resource == null) {
                System.err.println("Could not find " + path);
                return 0;
            }
            
            java.io.InputStream is = resource.openStream();
            java.io.ByteArrayOutputStream buffer = new java.io.ByteArrayOutputStream();
            int nRead;
            byte[] data = new byte[16384];
            while ((nRead = is.read(data, 0, data.length)) != -1) {
                buffer.write(data, 0, nRead);
            }
            byte[] bytes = buffer.toByteArray();
            java.nio.ByteBuffer imageBuffer = org.lwjgl.system.MemoryUtil.memAlloc(bytes.length);
            imageBuffer.put(bytes);
            imageBuffer.flip();
            
            org.lwjgl.stb.STBImage.stbi_set_flip_vertically_on_load(false); // UI/Mobs generally not flipped or handled per model
            java.nio.ByteBuffer image = org.lwjgl.stb.STBImage.stbi_load_from_memory(imageBuffer, w, h, comp, 4);
            org.lwjgl.system.MemoryUtil.memFree(imageBuffer);
            
            if (image == null) {
                return 0;
            }
            
            int texId = org.lwjgl.opengl.GL11.glGenTextures();
            org.lwjgl.opengl.GL11.glBindTexture(org.lwjgl.opengl.GL11.GL_TEXTURE_2D, texId);
            org.lwjgl.opengl.GL11.glTexParameteri(org.lwjgl.opengl.GL11.GL_TEXTURE_2D, org.lwjgl.opengl.GL11.GL_TEXTURE_MIN_FILTER, org.lwjgl.opengl.GL11.GL_NEAREST);
            org.lwjgl.opengl.GL11.glTexParameteri(org.lwjgl.opengl.GL11.GL_TEXTURE_2D, org.lwjgl.opengl.GL11.GL_TEXTURE_MAG_FILTER, org.lwjgl.opengl.GL11.GL_NEAREST);
            org.lwjgl.opengl.GL11.glTexImage2D(org.lwjgl.opengl.GL11.GL_TEXTURE_2D, 0, org.lwjgl.opengl.GL11.GL_RGBA, w.get(0), h.get(0), 0, org.lwjgl.opengl.GL11.GL_RGBA, org.lwjgl.opengl.GL11.GL_UNSIGNED_BYTE, image);
            org.lwjgl.stb.STBImage.stbi_image_free(image);
            return texId;
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

    private void loadTexture() {
        try (MemoryStack stack = stackPush()) {
            IntBuffer w = stack.mallocInt(1);
            IntBuffer h = stack.mallocInt(1);
            IntBuffer comp = stack.mallocInt(1);

            // Read the resource
            STBImage.stbi_set_flip_vertically_on_load(true);
            
            // To load from jar/resources, we need to read to a ByteBuffer
            ByteBuffer image = null;
            try {
                URL resource = MinecraftPE3D.class.getResource("/assets/images/terrain.png");
                if (resource == null) {
                    System.err.println("Could not find terrain.png!");
                    return;
                }
                
                InputStream is = resource.openStream();
                ByteArrayOutputStream buffer = new ByteArrayOutputStream();
                int nRead;
                byte[] data = new byte[16384];
                while ((nRead = is.read(data, 0, data.length)) != -1) {
                    buffer.write(data, 0, nRead);
                }
                
                byte[] bytes = buffer.toByteArray();
                ByteBuffer imageBuffer = MemoryUtil.memAlloc(bytes.length);
                imageBuffer.put(bytes);
                imageBuffer.flip();
                
                image = STBImage.stbi_load_from_memory(imageBuffer, w, h, comp, 4);
                MemoryUtil.memFree(imageBuffer);
            } catch (Exception e) {
                e.printStackTrace();
            }

            if (image == null) {
                System.err.println("Failed to load texture file: " + STBImage.stbi_failure_reason());
                return;
            }

            terrainTexture = glGenTextures();
            glBindTexture(GL_TEXTURE_2D, terrainTexture);

            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);

            glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, w.get(0), h.get(0), 0, GL_RGBA, GL_UNSIGNED_BYTE, image);
            STBImage.stbi_image_free(image);
        }
        guiTexture = loadGenericTexture("/assets/gui/gui.png");
        iconsTexture = loadGenericTexture("/assets/gui/icons.png");
        fontRenderer = new FontRenderer(loadGenericTexture("/assets/gui/default8.png"));
        
        pigTexture = loadGenericTexture("/assets/images/mob/pig.png");
        zombieTexture = loadGenericTexture("/assets/images/mob/zombie.png");
        cowTexture = loadGenericTexture("/assets/images/mob/cow.png");
        sheepTexture = loadGenericTexture("/assets/images/mob/sheep.png");
        sheepFurTexture = loadGenericTexture("/assets/images/mob/sheep_fur.png");
        chickenTexture = loadGenericTexture("/assets/images/mob/chicken.png");
        creeperTexture = loadGenericTexture("/assets/images/mob/creeper.png");
        spiderTexture = loadGenericTexture("/assets/images/mob/spider.png");
        skeletonTexture = loadGenericTexture("/assets/images/mob/skeleton.png");
    }

    private void setupProjection() {
        glMatrixMode(GL_PROJECTION);
        glLoadIdentity();
        float aspect = (float) width / height;
        float fov = 70.0f;
        float zNear = 0.05f;
        float zFar = 1000.0f;
        float fH = (float) Math.tan(fov / 360.0f * Math.PI) * zNear;
        float fW = fH * aspect;
        glFrustum(-fW, fW, -fH, fH, zNear, zFar);
        glMatrixMode(GL_MODELVIEW);
    }


    private void updateChunk(int bx, int bz) {
        int cx = (int) Math.floor(bx / 16.0);
        int cz = (int) Math.floor(bz / 16.0);
        
        for (com.minecraft.mcpe.renderer.ChunkRenderer cr : chunkRenderers) {
            if (cr.getChunk().getX() == cx && cr.getChunk().getZ() == cz) {
                cr.markDirty();
            }
        }
    }

    private void interactBlock(boolean place) {
        float px = camX, py = camY, pz = camZ;
        float cosYaw = (float)Math.cos(Math.toRadians(yaw));
        float sinYaw = (float)Math.sin(Math.toRadians(yaw));
        float cosPitch = (float)Math.cos(Math.toRadians(pitch));
        float sinPitch = (float)Math.sin(Math.toRadians(pitch));
        
        float vx = sinYaw * cosPitch;
        float vy = -sinPitch;
        float vz = -cosYaw * cosPitch;
        
        int lastBx = -1, lastBy = -1, lastBz = -1;
        boolean hasLast = false;
        
        for (float d = 0.0f; d < 6.0f; d += 0.05f) {
            int bx = (int) Math.floor(px + vx * d);
            int by = (int) Math.floor(py + vy * d);
            int bz = (int) Math.floor(pz + vz * d);
            
            if (world.getBlock(bx, by, bz) != 0) {
                if (place) {
                    if (hasLast) {
                        try {
                            world.setBlock(lastBx, lastBy, lastBz, hotbarBlocks[selectedHotbarSlot]);
                            updateChunk(lastBx, lastBz);
                            // Also try to update adjacent chunks in case it's on a boundary
                            updateChunk(lastBx + 1, lastBz);
                            updateChunk(lastBx - 1, lastBz);
                            updateChunk(lastBx, lastBz + 1);
                            updateChunk(lastBx, lastBz - 1);
                        } catch (Exception e) {}
                    }
                } else {
                    world.setBlock(bx, by, bz, 0); // AIR ID = 0
                    updateChunk(bx, bz);
                    // Update adjacent chunks
                    updateChunk(bx + 1, bz);
                    updateChunk(bx - 1, bz);
                    updateChunk(bx, bz + 1);
                    updateChunk(bx, bz - 1);
                }
                break;
            }
            lastBx = bx;
            lastBy = by;
            lastBz = bz;
            hasLast = true;
        }
    }

            private int getBlockTexture(int blockId) {
        switch (blockId) {
            case 1: return 1; // Stone
            case 2: return 0; // Grass
            case 3: return 2; // Dirt
            case 4: return 16; // Cobblestone
            case 5: return 4; // Wood planks
            case 17: return 20; // Log
            case 20: return 49; // Glass
            case 45: return 7; // Brick
            case 87: return 103; // Netherrack
            case 35: return 64; // Wool
            default: return 1;
        }
    }

    private void renderHeldItem(long currentTime) {
        glClear(GL_DEPTH_BUFFER_BIT); // Clear depth so hand renders over world
        
        glMatrixMode(GL_PROJECTION);
        glPushMatrix();
        glLoadIdentity();
        float fov = 70.0f;
        float aspect = (float) width / (float) height;
        float zNear = 0.05f;
        float zFar = 1000.0f;
        float fH = (float) Math.tan(fov / 360f * Math.PI) * zNear;
        float fW = fH * aspect;
        glFrustum(-fW, fW, -fH, fH, zNear, zFar);
        
        glMatrixMode(GL_MODELVIEW);
        glPushMatrix();
        glLoadIdentity();

        // Calculate Bobbing
        float bob = bobO + (bobPhase - bobO) * 0.5f; // rough interpolation
        float swingX = (float)Math.sin(bob * Math.PI * 2.0f) * 0.05f;
        float swingY = (float)Math.abs(Math.cos(bob * Math.PI * 2.0f)) * 0.05f;

        // Position hand in bottom right
        glTranslatef(0.4f + swingX, -0.4f - swingY, -0.8f);
        glRotatef(30.0f, 0.0f, 1.0f, 0.0f);
        glRotatef(-20.0f, 1.0f, 0.0f, 0.0f);
        
        glScalef(0.4f, 0.4f, 0.4f);
        
        // Render current block using Tessellator
        int blockId = hotbarBlocks[selectedHotbarSlot];
        int texIdx = getBlockTexture(blockId);
        
        glBindTexture(GL_TEXTURE_2D, terrainTexture);
        glEnable(GL_DEPTH_TEST);
        Tessellator t = Tessellator.getInstance();
        t.startDrawing();
        t.color(1.0f, 1.0f, 1.0f);
        
        // Just draw a basic cube centered at 0,0,0
        float s = 0.5f;
        float texU = (texIdx % 16) * 16.0f / 256.0f;
        float texV = (texIdx / 16) * 16.0f / 256.0f;
        float texSize = 16.0f / 256.0f;
        
        t.addVertexWithUV(-s, -s, s, texU, texV+texSize); t.addVertexWithUV(s, -s, s, texU+texSize, texV+texSize); t.addVertexWithUV(s, s, s, texU+texSize, texV); t.addVertexWithUV(-s, s, s, texU, texV); // Front
        t.addVertexWithUV(-s, -s, -s, texU, texV+texSize); t.addVertexWithUV(-s, s, -s, texU+texSize, texV+texSize); t.addVertexWithUV(s, s, -s, texU+texSize, texV); t.addVertexWithUV(s, -s, -s, texU, texV); // Back
        t.addVertexWithUV(-s, s, -s, texU, texV+texSize); t.addVertexWithUV(-s, s, s, texU+texSize, texV+texSize); t.addVertexWithUV(s, s, s, texU+texSize, texV); t.addVertexWithUV(s, s, -s, texU, texV); // Top
        t.addVertexWithUV(-s, -s, -s, texU, texV+texSize); t.addVertexWithUV(s, -s, -s, texU+texSize, texV+texSize); t.addVertexWithUV(s, -s, s, texU+texSize, texV); t.addVertexWithUV(-s, -s, s, texU, texV); // Bottom
        t.addVertexWithUV(-s, -s, -s, texU, texV+texSize); t.addVertexWithUV(-s, -s, s, texU+texSize, texV+texSize); t.addVertexWithUV(-s, s, s, texU+texSize, texV); t.addVertexWithUV(-s, s, -s, texU, texV); // Left
        t.addVertexWithUV(s, -s, -s, texU, texV+texSize); t.addVertexWithUV(s, s, -s, texU+texSize, texV+texSize); t.addVertexWithUV(s, s, s, texU+texSize, texV); t.addVertexWithUV(s, -s, s, texU, texV); // Right
        t.draw();

        glMatrixMode(GL_PROJECTION);
        glPopMatrix();
        glMatrixMode(GL_MODELVIEW);
        glPopMatrix();
    }

    private void renderGUI() {
        glMatrixMode(GL_PROJECTION);
        glPushMatrix();
        glLoadIdentity();
        glOrtho(0, width, height, 0, -1000, 1000);
        glMatrixMode(GL_MODELVIEW);
        glPushMatrix();
        glLoadIdentity();

        glDisable(GL_DEPTH_TEST);
        glEnable(GL_BLEND);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);

        // Draw Crosshair
        glDisable(GL_TEXTURE_2D);
        glColor4f(1.0f, 1.0f, 1.0f, 1.0f);
        glBegin(GL_LINES);
        glVertex2f(width / 2.0f - 10, height / 2.0f);
        glVertex2f(width / 2.0f + 10, height / 2.0f);
        glVertex2f(width / 2.0f, height / 2.0f - 10);
        glVertex2f(width / 2.0f, height / 2.0f + 10);
        glEnd();

        glEnable(GL_TEXTURE_2D);
        
        // Draw Hotbar Base
        glBindTexture(GL_TEXTURE_2D, guiTexture);
        float hotbarScale = 2.0f;
        float hotbarWidth = 182 * hotbarScale;
        float hotbarHeight = 22 * hotbarScale;
        float startX = (width - hotbarWidth) / 2.0f;
        float startY = height - hotbarHeight;

        glColor4f(1.0f, 1.0f, 1.0f, 1.0f);
        glBegin(GL_QUADS);
        glTexCoord2f(0.0f, 0.0f); glVertex2f(startX, startY);
        glTexCoord2f(182.0f / 256.0f, 0.0f); glVertex2f(startX + hotbarWidth, startY);
        glTexCoord2f(182.0f / 256.0f, 22.0f / 256.0f); glVertex2f(startX + hotbarWidth, startY + hotbarHeight);
        glTexCoord2f(0.0f, 22.0f / 256.0f); glVertex2f(startX, startY + hotbarHeight);
        glEnd();

        // Draw Selection Box
        float selStartX = startX - (1 * hotbarScale) + (selectedHotbarSlot * 20 * hotbarScale);
        float selStartY = startY - (1 * hotbarScale);
        float selWidth = 24 * hotbarScale;
        float selHeight = 24 * hotbarScale;
        
        glBegin(GL_QUADS);
        glTexCoord2f(0.0f, 22.0f / 256.0f); glVertex2f(selStartX, selStartY);
        glTexCoord2f(24.0f / 256.0f, 22.0f / 256.0f); glVertex2f(selStartX + selWidth, selStartY);
        glTexCoord2f(24.0f / 256.0f, 46.0f / 256.0f); glVertex2f(selStartX + selWidth, selStartY + selHeight);
        glTexCoord2f(0.0f, 46.0f / 256.0f); glVertex2f(selStartX, selStartY + selHeight);
        glEnd();

                // Draw Hearts (Health)
        glBindTexture(GL_TEXTURE_2D, iconsTexture);
        float heartStartX = startX;
        float heartStartY = startY - (12 * hotbarScale);
        for (int i = 0; i < 10; i++) {
            float hx = heartStartX + (i * 8 * hotbarScale);
            
            // Empty Heart Background
            glBegin(GL_QUADS);
            glTexCoord2f(16.0f / 256.0f, 0.0f); glVertex2f(hx, heartStartY);
            glTexCoord2f(25.0f / 256.0f, 0.0f); glVertex2f(hx + 9 * hotbarScale, heartStartY);
            glTexCoord2f(25.0f / 256.0f, 9.0f / 256.0f); glVertex2f(hx + 9 * hotbarScale, heartStartY + 9 * hotbarScale);
            glTexCoord2f(16.0f / 256.0f, 9.0f / 256.0f); glVertex2f(hx, heartStartY + 9 * hotbarScale);
            glEnd();
            
            // Full Heart Foreground
            glBegin(GL_QUADS);
            glTexCoord2f(52.0f / 256.0f, 0.0f); glVertex2f(hx, heartStartY);
            glTexCoord2f(61.0f / 256.0f, 0.0f); glVertex2f(hx + 9 * hotbarScale, heartStartY);
            glTexCoord2f(61.0f / 256.0f, 9.0f / 256.0f); glVertex2f(hx + 9 * hotbarScale, heartStartY + 9 * hotbarScale);
            glTexCoord2f(52.0f / 256.0f, 9.0f / 256.0f); glVertex2f(hx, heartStartY + 9 * hotbarScale);
            glEnd();
        }
        // Draw Version Text
        if (fontRenderer != null) {
            fontRenderer.drawString("v0.1.1 alpha", 2, 2, 0xFFFFFFFF);
        }

        glDisable(GL_BLEND);
        glEnable(GL_DEPTH_TEST);

        glMatrixMode(GL_PROJECTION);
        glPopMatrix();
        glMatrixMode(GL_MODELVIEW);
        glPopMatrix();
    }




    private void processInput(float deltaTime) {
        float speed = 4.3f * deltaTime; 
        
        float cosYaw = (float)Math.cos(Math.toRadians(yaw));
        float sinYaw = (float)Math.sin(Math.toRadians(yaw));

        float xd = 0, zd = 0;
        
        if (keys[GLFW_KEY_W]) {
            xd += sinYaw * speed;
            zd -= cosYaw * speed;
        }
        if (keys[GLFW_KEY_S]) {
            xd -= sinYaw * speed;
            zd += cosYaw * speed;
        }
        if (keys[GLFW_KEY_A]) {
            xd -= cosYaw * speed;
            zd -= sinYaw * speed;
        }
        if (keys[GLFW_KEY_D]) {
            xd += cosYaw * speed;
            zd += sinYaw * speed;
        }

        // Apply friction/gravity simple
        yd -= 0.08 * deltaTime * 60.0; // Gravity

        if (keys[GLFW_KEY_SPACE] && onGround) {
            yd = 0.5; // Jump
        }

        // Advanced World Collision (Very basic raycast downwards to find floor)
        // Check block below camera
        int blockX = (int) Math.floor(camX);
        int blockY = (int) Math.floor(camY - 1.6f); // 1.6 is player eye height
        int blockZ = (int) Math.floor(camZ);
        
        int blockId = 0;
        if (world != null) {
            blockId = world.getBlock(blockX, blockY, blockZ);
        }

        camY += yd;
        
        // If we hit a solid block
        if (blockId != 0 && camY - 1.6f <= blockY + 1) {
            camY = blockY + 1 + 1.6f;
            yd = 0;
            onGround = true;
        } else {
            onGround = false;
        }

        camZ += zd;

        bobO = bobPhase;
        double speedSqr = xd * xd + zd * zd;
        if (speedSqr > 0.0) {
            bobPhase += Math.sqrt(speedSqr) * 2.0f;
        } else {
            bobPhase += 0.0f;
        }
    }


    private void loop() {
        GL.createCapabilities();
        glClearColor(0.53f, 0.81f, 0.98f, 1.0f);
        
        glEnable(GL_DEPTH_TEST);
        glEnable(GL_TEXTURE_2D);
        
        glEnable(GL_CULL_FACE);
        glCullFace(GL_BACK);

        // Alpha MCPE Fog Setup
        glEnable(GL_FOG);
        glFogi(GL_FOG_MODE, GL_LINEAR);
        float[] fogColor = {0.53f, 0.81f, 0.98f, 1.0f}; // Same as clear color
        glFogfv(GL_FOG_COLOR, fogColor);
        glFogf(GL_FOG_START, 20.0f);
        glFogf(GL_FOG_END, 45.0f);

        loadTexture();
        setupProjection();

        long lastTime = System.nanoTime();

        while (!glfwWindowShouldClose(window)) {
            long currentTime = System.nanoTime();
            float deltaTime = (currentTime - lastTime) / 1000000000.0f;
            lastTime = currentTime;

            processInput(deltaTime);

            glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

            glLoadIdentity();
            // Apply camera rotations
            glRotatef(-pitch, 1.0f, 0.0f, 0.0f);
            glRotatef(-yaw, 0.0f, 1.0f, 0.0f);
            // Apply camera translation
            glTranslatef(-camX, -camY, -camZ);

            // Bind the terrain texture
            glBindTexture(GL_TEXTURE_2D, terrainTexture);

            // Render the World chunks
            for (ChunkRenderer cr : chunkRenderers) {
                cr.render();
            }

                        // GUI overlay
            
            // Sync dummy player with camera for mob aggro
            localPlayer.getPosition().x = camX;
            localPlayer.getPosition().y = camY;
            localPlayer.getPosition().z = camZ;
            
            // Render Entities
            glDisable(GL_CULL_FACE);
            for (Entity e : entities) {
                e.update();
                glPushMatrix();
                glTranslatef((float)e.getPosition().x, (float)e.getPosition().y, (float)e.getPosition().z);
                
                // MCPE basic animation math based on position
                float time = (float) glfwGetTime() * 10.0f;
                float walkAnim = e.getVelocity().length() > 0.01f ? 1.0f : 0.0f;
                
                if (e instanceof Pig) {
                    glBindTexture(GL_TEXTURE_2D, pigTexture);
                    pigModel.render(e, time * walkAnim, walkAnim, (float)e.getRotation().y, (float)e.getRotation().x, 0.0625f);
                } else if (e instanceof Zombie) {
                    glBindTexture(GL_TEXTURE_2D, zombieTexture);
                    zombieModel.render(e, time * walkAnim, walkAnim, (float)e.getRotation().y, (float)e.getRotation().x, 0.0625f);
                } else if (e instanceof Cow) {
                    glBindTexture(GL_TEXTURE_2D, cowTexture);
                    cowModel.render(e, time * walkAnim, walkAnim, (float)e.getRotation().y, (float)e.getRotation().x, 0.0625f);
                } else if (e instanceof Sheep) {
                    glBindTexture(GL_TEXTURE_2D, sheepTexture);
                    ((SheepModel)sheepModel).render(e, time * walkAnim, walkAnim, (float)e.getRotation().y, (float)e.getRotation().x, 0.0625f);
                    if (true) {
                        glBindTexture(GL_TEXTURE_2D, sheepFurTexture);
                        ((SheepModel)sheepModel).renderWool(0.0625f);
                    }
                } else if (e instanceof Chicken) {
                    glBindTexture(GL_TEXTURE_2D, chickenTexture);
                    chickenModel.render(e, time * walkAnim, walkAnim, (float)e.getRotation().y, (float)e.getRotation().x, 0.0625f);
                } else if (e instanceof Creeper) {
                    glBindTexture(GL_TEXTURE_2D, creeperTexture);
                    creeperModel.render(e, time * walkAnim, walkAnim, (float)e.getRotation().y, (float)e.getRotation().x, 0.0625f);
                } else if (e instanceof Spider) {
                    glBindTexture(GL_TEXTURE_2D, spiderTexture);
                    spiderModel.render(e, time * walkAnim, walkAnim, (float)e.getRotation().y, (float)e.getRotation().x, 0.0625f);
                } else if (e instanceof Skeleton) {
                    glBindTexture(GL_TEXTURE_2D, skeletonTexture);
                    skeletonModel.render(e, time * walkAnim, walkAnim, (float)e.getRotation().y, (float)e.getRotation().x, 0.0625f);
                }
                glPopMatrix();
            }
            glEnable(GL_CULL_FACE);

            renderHeldItem(currentTime);
            renderGUI();

            glfwSwapBuffers(window);
            glfwPollEvents();
        }
    }

    public static void main(String[] args) {
        new MinecraftPE3D().run();
    }
}
