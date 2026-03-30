package com.minecraft.mcpe.nbt;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class EndTag extends Tag {
    public EndTag(String name) {
        super(name);
    }

    public byte getId() { return 0; }

    public void write(DataOutputStream out) throws IOException {
        // End tag has no payload.
    }

    public void read(DataInputStream in) throws IOException {
        // End tag has no payload.
    }

    public Tag clone() {
        return new EndTag(name);
    }
}
