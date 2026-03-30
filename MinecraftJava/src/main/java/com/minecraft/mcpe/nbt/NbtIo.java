package com.minecraft.mcpe.nbt;

import java.io.*;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

/**
 * NBT I/O utilities for reading/writing NBT files
 */
public class NbtIo {

    public static Tag read(File file) throws IOException {
        try (FileInputStream fis = new FileInputStream(file);
             GZIPInputStream gis = new GZIPInputStream(fis);
             DataInputStream dis = new DataInputStream(gis)) {
            return Tag.readTag(dis);
        }
    }

    public static void write(File file, CompoundTag tag) throws IOException {
        try (FileOutputStream fos = new FileOutputStream(file);
             GZIPOutputStream gos = new GZIPOutputStream(fos);
             DataOutputStream dos = new DataOutputStream(gos)) {
            dos.writeByte(tag.getId());
            dos.writeUTF(tag.getName());
            tag.write(dos);
            dos.flush();
        }
    }

    public static Tag readUncompressed(File file) throws IOException {
        try (FileInputStream fis = new FileInputStream(file);
             DataInputStream dis = new DataInputStream(fis)) {
            return Tag.readTag(dis);
        }
    }

    public static void writeUncompressed(File file, CompoundTag tag) throws IOException {
        try (FileOutputStream fos = new FileOutputStream(file);
             DataOutputStream dos = new DataOutputStream(fos)) {
            dos.writeByte(tag.getId());
            dos.writeUTF(tag.getName());
            tag.write(dos);
            dos.flush();
        }
    }
}
