package com.minecraft.mcpe.nbt;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class ByteArrayTag extends Tag {
    public byte[] value;
    
    public ByteArrayTag(String name) { super(name); this.value = new byte[0]; }
    public ByteArrayTag(String name, byte[] value) { super(name); this.value = value; }
    
    public byte getId() { return 7; }
    public void write(DataOutputStream out) throws IOException {
        out.writeInt(value.length);
        out.write(value);
    }
    public void read(DataInputStream in) throws IOException {
        int len = in.readInt();
        value = new byte[len];
        in.readFully(value);
    }
    public Tag clone() { return new ByteArrayTag(name, value.clone()); }
}
