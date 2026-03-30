package com.minecraft.mcpe.nbt;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.*;

/**
 * NBT (Named Binary Tag) - Minecraft's binary format for structured data
 * Converted from C++ implementation
 */
public abstract class Tag implements Cloneable {
    protected String name;

    public Tag(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public abstract byte getId();

    public abstract void write(DataOutputStream out) throws IOException;

    public abstract void read(DataInputStream in) throws IOException;

    @Override
    public abstract Tag clone();

    public static Tag readTag(DataInputStream in) throws IOException {
        byte id = in.readByte();
        String tagName = in.readUTF();

        Tag tag = createTag(id);
        tag.setName(tagName);
        tag.read(in);
        return tag;
    }

    public static Tag createTag(byte id) {
        switch (id) {
            case 1: return new ByteTag("");
            case 2: return new ShortTag("");
            case 3: return new IntTag("");
            case 4: return new LongTag("");
            case 5: return new FloatTag("");
            case 6: return new DoubleTag("");
            case 7: return new ByteArrayTag("");
            case 8: return new StringTag("");
            case 9: return new ListTag("");
            case 10: return new CompoundTag("");
            case 11: return new IntArrayTag("");
            case 12: return new LongArrayTag("");
            default: return new EndTag("");
        }
    }
}
