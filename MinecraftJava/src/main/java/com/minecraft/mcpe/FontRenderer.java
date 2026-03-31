package com.minecraft.mcpe;

import static org.lwjgl.opengl.GL11.*;
import org.lwjgl.stb.STBImage;
import org.lwjgl.system.MemoryStack;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;

public class FontRenderer {
    private int fontTexture;
    private int[] charWidths = new int[256];

    public FontRenderer(int fontTextureId) {
        fontTexture = fontTextureId;
        // In a true 1-to-1, we would parse the image pixels to get character widths.
        // For now, we will assume a fixed width or basic widths.
        for (int i = 0; i < 256; i++) {
            charWidths[i] = 8; // Default to 8 width for all
        }
    }

    private int loadTexture(String path) {
        int textureID = glGenTextures();
        glBindTexture(GL_TEXTURE_2D, textureID);
        try (MemoryStack stack = MemoryStack.stackPush()) {
            IntBuffer w = stack.mallocInt(1);
            IntBuffer h = stack.mallocInt(1);
            IntBuffer comp = stack.mallocInt(1);

            // STBImage expects the path to be accessible as a physical file, or we'd load it from resources.
            // Using a simple load for now.
            ByteBuffer image = STBImage.stbi_load(path, w, h, comp, 4);
            if (image != null) {
                glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, w.get(0), h.get(0), 0, GL_RGBA, GL_UNSIGNED_BYTE, image);
                glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
                glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
                STBImage.stbi_image_free(image);
            }
        }
        return textureID;
    }

    public void drawString(String text, float x, float y, int color) {
        glEnable(GL_TEXTURE_2D);
        glBindTexture(GL_TEXTURE_2D, fontTexture);
        
        float r = ((color >> 16) & 255) / 255.0f;
        float g = ((color >> 8) & 255) / 255.0f;
        float b = (color & 255) / 255.0f;
        float a = ((color >> 24) & 255) / 255.0f;
        if (a == 0.0f) a = 1.0f; // Default solid
        
        glColor4f(r, g, b, a);
        glBegin(GL_QUADS);
        
        float currentX = x;
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            if (c > 255) c = '?';
            
            float u = (c % 16) * 8.0f / 128.0f;
            float v = (c / 16) * 8.0f / 128.0f;
            float charW = 8.0f / 128.0f;
            float charH = 8.0f / 128.0f;
            
            glTexCoord2f(u, v); glVertex2f(currentX, y);
            glTexCoord2f(u + charW, v); glVertex2f(currentX + 8, y);
            glTexCoord2f(u + charW, v + charH); glVertex2f(currentX + 8, y + 8);
            glTexCoord2f(u, v + charH); glVertex2f(currentX, y + 8);
            
            currentX += charWidths[c];
        }
        glEnd();
    }
}
