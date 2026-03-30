package com.minecraft.mcpe.nbt;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class StringTag extends Tag {
    public String value;

    public StringTag(String name) {
        super(name);
        this.value = "";
    }

    public StringTag(String name, String value) {
        super(name);
        this.value = value == null ? "" : value;
    }

    public byte getId() { return 8; }

    public void write(DataOutputStream out) throws IOException {
        out.writeUTF(value);
    }

    public void read(DataInputStream in) throws IOException {
        value = in.readUTF();
    }

    public Tag clone() {
        return new StringTag(name, value);
    }
}
