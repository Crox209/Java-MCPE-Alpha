package com.minecraft.mcpe;
public class TestPaths {
    public static void main(String[] args) {
        System.out.println("Terrain via getResource: " + TestPaths.class.getResource("/assets/images/terrain.png"));
        System.out.println("GUI via getResource: " + TestPaths.class.getResource("/assets/gui/gui.png"));
    }
}
