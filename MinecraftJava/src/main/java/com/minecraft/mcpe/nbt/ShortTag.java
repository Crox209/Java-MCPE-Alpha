package com.minecraft.mcpe.nbt;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class ShortTag extends Tag {
    public short value;

    public ShortTag(String name) {
        super(name);
        this.value = 0;
    }

    public ShortTag(String name, short value) {
        super(name);
        this.value = value;
    }

    public byte getId() { return 2; }

    public void write(DataOutputStream out) throws IOException {
        out.writeShort(value);
    }

    public void read(DataInputStream in) throws IOException {
        value = in.readShort();
    }

    public Tag clone() {
        return new ShortTag(name, value);
    }
}
