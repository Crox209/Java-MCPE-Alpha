package com.minecraft.mcpe.nbt;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class ByteTag extends Tag {
    public byte value;

    public ByteTag(String name) {
        super(name);
        this.value = 0;
    }

    public ByteTag(String name, byte value) {
        super(name);
        this.value = value;
    }

    public byte getId() { return 1; }

    public void write(DataOutputStream out) throws IOException {
        out.writeByte(value);
    }

    public void read(DataInputStream in) throws IOException {
        value = in.readByte();
    }

    public Tag clone() {
        return new ByteTag(name, value);
    }
}
