package com.minecraft.mcpe.renderer;

import com.minecraft.mcpe.block.Block;

public final class BlockTextureMap {
    private BlockTextureMap() {
    }

    public static int getTopTexture(int blockId) {
        return getTextureForFace(blockId, 1);
    }

    public static int getBottomTexture(int blockId) {
        return getTextureForFace(blockId, 0);
    }

    public static int getSideTexture(int blockId) {
        return getTextureForFace(blockId, 2);
    }

    public static int getTextureForFace(int blockId, int face) {
        switch (blockId) {
            case Block.GRASS:
                if (face == 1) return 0;
                if (face == 0) return 2;
                return 3;
            case Block.STONE:
                return 1;
            case Block.DIRT:
                return 2;
            case Block.COBBLESTONE:
                return 16;
            case Block.WOOD:
                return 4;
            case Block.LOG:
                return (face == 1 || face == 0) ? 21 : 20;
            case Block.LEAVES:
                return 52;
            case Block.SAND:
                return 18;
            case Block.GRAVEL:
                return 19;
            case Block.BEDROCK:
                return 17;
            case Block.GLASS:
                return 49;
            case Block.BRICK:
                return 7;
            case Block.WOOL:
                return 64;
            case Block.NETHERRACK:
                return 103;
            default:
                return 1;
        }
    }
}