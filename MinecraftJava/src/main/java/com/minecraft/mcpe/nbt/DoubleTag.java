package com.minecraft.mcpe.nbt;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class DoubleTag extends Tag {
    public double value;

    public DoubleTag(String name) {
        super(name);
        this.value = 0.0;
    }

    public DoubleTag(String name, double value) {
        super(name);
        this.value = value;
    }

    public byte getId() { return 6; }

    public void write(DataOutputStream out) throws IOException {
        out.writeDouble(value);
    }

    public void read(DataInputStream in) throws IOException {
        value = in.readDouble();
    }

    public Tag clone() {
        return new DoubleTag(name, value);
    }
}
