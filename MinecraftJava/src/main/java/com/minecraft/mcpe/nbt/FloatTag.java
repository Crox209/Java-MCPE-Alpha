package com.minecraft.mcpe.nbt;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class FloatTag extends Tag {
    public float value;

    public FloatTag(String name) {
        super(name);
        this.value = 0.0f;
    }

    public FloatTag(String name, float value) {
        super(name);
        this.value = value;
    }

    public byte getId() { return 5; }

    public void write(DataOutputStream out) throws IOException {
        out.writeFloat(value);
    }

    public void read(DataInputStream in) throws IOException {
        value = in.readFloat();
    }

    public Tag clone() {
        return new FloatTag(name, value);
    }
}
