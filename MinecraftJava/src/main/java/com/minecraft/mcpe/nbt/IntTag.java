package com.minecraft.mcpe.nbt;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class IntTag extends Tag {
    public int value;
    
    public IntTag(String name) { super(name); this.value = 0; }
    public IntTag(String name, int value) { super(name); this.value = value; }
    
    public byte getId() { return 3; }
    public void write(DataOutputStream out) throws IOException { out.writeInt(value); }
    public void read(DataInputStream in) throws IOException { value = in.readInt(); }
    public Tag clone() { return new IntTag(name, value); }
}
