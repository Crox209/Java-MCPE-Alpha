package com.minecraft.mcpe.world.generation;

import java.util.Random;

public class PerlinNoise {
    private int[] p;
    private double offsetX;
    private double offsetY;
    private double offsetZ;

    public PerlinNoise(Random random) {
        this.p = new int[512];
        this.offsetX = random.nextDouble() * 256.0D;
        this.offsetY = random.nextDouble() * 256.0D;
        this.offsetZ = random.nextDouble() * 256.0D;

        for (int i = 0; i < 256; i++) {
            this.p[i] = i;
        }

        for (int i = 0; i < 256; i++) {
            int j = random.nextInt(256 - i) + i;
            int k = this.p[i];
            this.p[i] = this.p[j];
            this.p[j] = k;
            this.p[i + 256] = this.p[i];
        }
    }

    public double getValue(double x, double y) {
        return this.getValue(x, y, 0.0);
    }

    public double getValue(double x, double y, double z) {
        double d0 = x + this.offsetX;
        double d1 = y + this.offsetY;
        double d2 = z + this.offsetZ;

        int i = (int) Math.floor(d0) & 255;
        int j = (int) Math.floor(d1) & 255;
        int k = (int) Math.floor(d2) & 255;

        d0 -= Math.floor(d0);
        d1 -= Math.floor(d1);
        d2 -= Math.floor(d2);

        double u = fade(d0);
        double v = fade(d1);
        double w = fade(d2);

        int A = this.p[i] + j;
        int AA = this.p[A] + k;
        int AB = this.p[A + 1] + k;
        int B = this.p[i + 1] + j;
        int BA = this.p[B] + k;
        int BB = this.p[B + 1] + k;

        return lerp(w, lerp(v, lerp(u, grad(this.p[AA], d0, d1, d2),
                                    grad(this.p[BA], d0 - 1, d1, d2)),
                            lerp(u, grad(this.p[AB], d0, d1 - 1, d2),
                                 grad(this.p[BB], d0 - 1, d1 - 1, d2))),
                    lerp(v, lerp(u, grad(this.p[AA + 1], d0, d1, d2 - 1),
                                 grad(this.p[BA + 1], d0 - 1, d1, d2 - 1)),
                         lerp(u, grad(this.p[AB + 1], d0, d1 - 1, d2 - 1),
                              grad(this.p[BB + 1], d0 - 1, d1 - 1, d2 - 1))));
    }

    private static double fade(double t) {
        return t * t * t * (t * (t * 6.0 - 15.0) + 10.0);
    }

    private static double lerp(double t, double a, double b) {
        return a + t * (b - a);
    }

    private static double grad(int hash, double x, double y, double z) {
        int h = hash & 15;
        double u = h < 8 ? x : y;
        double v = h < 4 ? y : h == 12 || h == 14 ? x : z;
        return ((h & 1) == 0 ? u : -u) + ((h & 2) == 0 ? v : -v);
    }
}
