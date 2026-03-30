package com.minecraft.mcpe.nbt;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class IntArrayTag extends Tag {
    public int[] value;

    public IntArrayTag(String name) {
        super(name);
        this.value = new int[0];
    }

    public IntArrayTag(String name, int[] value) {
        super(name);
        this.value = value == null ? new int[0] : value;
    }

    public byte getId() { return 11; }

    public void write(DataOutputStream out) throws IOException {
        out.writeInt(value.length);
        for (int v : value) {
            out.writeInt(v);
        }
    }

    public void read(DataInputStream in) throws IOException {
        int len = in.readInt();
        value = new int[len];
        for (int i = 0; i < len; i++) {
            value[i] = in.readInt();
        }
    }

    public Tag clone() {
        return new IntArrayTag(name, value.clone());
    }
}
