package com.minecraft.mcpe.nbt;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class LongArrayTag extends Tag {
    public long[] value;

    public LongArrayTag(String name) {
        super(name);
        this.value = new long[0];
    }

    public LongArrayTag(String name, long[] value) {
        super(name);
        this.value = value == null ? new long[0] : value;
    }

    public byte getId() { return 12; }

    public void write(DataOutputStream out) throws IOException {
        out.writeInt(value.length);
        for (long v : value) {
            out.writeLong(v);
        }
    }

    public void read(DataInputStream in) throws IOException {
        int len = in.readInt();
        value = new long[len];
        for (int i = 0; i < len; i++) {
            value[i] = in.readLong();
        }
    }

    public Tag clone() {
        return new LongArrayTag(name, value.clone());
    }
}
