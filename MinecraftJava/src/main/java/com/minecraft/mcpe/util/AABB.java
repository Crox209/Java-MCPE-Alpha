package com.minecraft.mcpe.util;

public class AABB {
    public double x0, y0, z0;
    public double x1, y1, z1;

    public AABB(double x0, double y0, double z0, double x1, double y1, double z1) {
        this.x0 = x0;
        this.y0 = y0;
        this.z0 = z0;
        this.x1 = x1;
        this.y1 = y1;
        this.z1 = z1;
    }

    public AABB expand(double xa, double ya, double za) {
        double nx0 = this.x0;
        double ny0 = this.y0;
        double nz0 = this.z0;
        double nx1 = this.x1;
        double ny1 = this.y1;
        double nz1 = this.z1;
        if (xa < 0.0D) {
            nx0 += xa;
        }
        if (xa > 0.0D) {
            nx1 += xa;
        }
        if (ya < 0.0D) {
            ny0 += ya;
        }
        if (ya > 0.0D) {
            ny1 += ya;
        }
        if (za < 0.0D) {
            nz0 += za;
        }
        if (za > 0.0D) {
            nz1 += za;
        }
        return new AABB(nx0, ny0, nz0, nx1, ny1, nz1);
    }

    public AABB grow(double xa, double ya, double za) {
        double nx0 = this.x0 - xa;
        double ny0 = this.y0 - ya;
        double nz0 = this.z0 - za;
        double nx1 = this.x1 + xa;
        double ny1 = this.y1 + ya;
        double nz1 = this.z1 + za;
        return new AABB(nx0, ny0, nz0, nx1, ny1, nz1);
    }

    public double clipXCollide(AABB c, double xa) {
        if (c.y1 <= this.y0 || c.y0 >= this.y1) {
            return xa;
        }
        if (c.z1 <= this.z0 || c.z0 >= this.z1) {
            return xa;
        }
        if (xa > 0.0D && c.x1 <= this.x0) {
            double max = this.x0 - c.x1;
            if (max < xa) {
                xa = max;
            }
        }
        if (xa < 0.0D && c.x0 >= this.x1) {
            double max = this.x1 - c.x0;
            if (max > xa) {
                xa = max;
            }
        }
        return xa;
    }

    public double clipYCollide(AABB c, double ya) {
        if (c.x1 <= this.x0 || c.x0 >= this.x1) {
            return ya;
        }
        if (c.z1 <= this.z0 || c.z0 >= this.z1) {
            return ya;
        }
        if (ya > 0.0D && c.y1 <= this.y0) {
            double max = this.y0 - c.y1;
            if (max < ya) {
                ya = max;
            }
        }
        if (ya < 0.0D && c.y0 >= this.y1) {
            double max = this.y1 - c.y0;
            if (max > ya) {
                ya = max;
            }
        }
        return ya;
    }

    public double clipZCollide(AABB c, double za) {
        if (c.x1 <= this.x0 || c.x0 >= this.x1) {
            return za;
        }
        if (c.y1 <= this.y0 || c.y0 >= this.y1) {
            return za;
        }
        if (za > 0.0D && c.z1 <= this.z0) {
            double max = this.z0 - c.z1;
            if (max < za) {
                za = max;
            }
        }
        if (za < 0.0D && c.z0 >= this.z1) {
            double max = this.z1 - c.z0;
            if (max > za) {
                za = max;
            }
        }
        return za;
    }

    public boolean intersects(AABB c) {
        return c.x1 > this.x0 && c.x0 < this.x1 ? (c.y1 > this.y0 && c.y0 < this.y1 ? c.z1 > this.z0 && c.z0 < this.z1 : false) : false;
    }

    public void move(double xa, double ya, double za) {
        this.x0 += xa;
        this.y0 += ya;
        this.z0 += za;
        this.x1 += xa;
        this.y1 += ya;
        this.z1 += za;
    }
}
