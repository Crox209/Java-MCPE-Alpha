package com.minecraft.mcpe.nbt;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class CompoundTag extends Tag {
    public Map<String, Tag> value;
    
    public CompoundTag(String name) {
        super(name);
        this.value = new HashMap<>();
    }
    
    public byte getId() { return 10; }
    
    public void put(String key, Tag tag) {
        tag.setName(key);
        value.put(key, tag);
    }
    
    public Tag get(String key) {
        return value.get(key);
    }
    
    public void write(DataOutputStream out) throws IOException {
        for (Tag tag : value.values()) {
            out.writeByte(tag.getId());
            out.writeUTF(tag.getName());
            tag.write(out);
        }
        out.writeByte(0); // End tag
    }
    
    public void read(DataInputStream in) throws IOException {
        while (true) {
            byte id = in.readByte();
            if (id == 0) break;
            
            String tagName = in.readUTF();
            Tag tag = Tag.createTag(id);
            tag.setName(tagName);
            tag.read(in);
            value.put(tagName, tag);
        }
    }
    
    public Tag clone() {
        CompoundTag clone = new CompoundTag(name);
        for (Map.Entry<String, Tag> entry : value.entrySet()) {
            clone.value.put(entry.getKey(), entry.getValue().clone());
        }
        return clone;
    }
}
