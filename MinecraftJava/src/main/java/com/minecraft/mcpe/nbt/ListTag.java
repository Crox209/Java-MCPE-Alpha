package com.minecraft.mcpe.nbt;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ListTag extends Tag {
    public byte elementType;
    public List<Tag> value;

    public ListTag(String name) {
        super(name);
        this.elementType = 0;
        this.value = new ArrayList<>();
    }

    public byte getId() { return 9; }

    public void write(DataOutputStream out) throws IOException {
        out.writeByte(elementType);
        out.writeInt(value.size());
        for (Tag tag : value) {
            tag.write(out);
        }
    }

    public void read(DataInputStream in) throws IOException {
        elementType = in.readByte();
        int size = in.readInt();
        value = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            Tag tag = Tag.createTag(elementType);
            tag.read(in);
            value.add(tag);
        }
    }

    public Tag clone() {
        ListTag clone = new ListTag(name);
        clone.elementType = elementType;
        for (Tag tag : value) {
            clone.value.add(tag.clone());
        }
        return clone;
    }
}
