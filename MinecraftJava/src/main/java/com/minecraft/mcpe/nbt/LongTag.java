package com.minecraft.mcpe.nbt;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class LongTag extends Tag {
    public long value;
    
    public LongTag(String name) { super(name); this.value = 0; }
    public LongTag(String name, long value) { super(name); this.value = value; }
    
    public byte getId() { return 4; }
    public void write(DataOutputStream out) throws IOException { out.writeLong(value); }
    public void read(DataInputStream in) throws IOException { value = in.readLong(); }
    public Tag clone() { return new LongTag(name, value); }
}
