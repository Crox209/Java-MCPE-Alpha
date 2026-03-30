package com.minecraft.mcpe.block;

/**
 * Block - represents a single block type in Minecraft
 */
public class Block {
    public static final int AIR = 0;
    public static final int STONE = 1;
    public static final int GRASS = 2;
    public static final int DIRT = 3;
    public static final int COBBLESTONE = 4;
    public static final int WOOD = 5;
    public static final int SAPLING = 6;
    public static final int BEDROCK = 7;
    public static final int WATER = 8;
    public static final int STATIONARY_WATER = 9;
    public static final int LAVA = 10;
    public static final int STATIONARY_LAVA = 11;
    public static final int SAND = 12;
    public static final int GRAVEL = 13;
    public static final int GOLD_ORE = 14;
    public static final int IRON_ORE = 15;
    public static final int COAL_ORE = 16;
    public static final int LOG = 17;
    public static final int LEAVES = 18;
    public static final int SPONGE = 19;
    public static final int GLASS = 20;
    public static final int LAPIS_ORE = 21;
    public static final int LAPIS_BLOCK = 22;
    public static final int DISPENSER = 23;
    public static final int SANDSTONE = 24;
    public static final int NOTE_BLOCK = 25;
    public static final int BED = 26;
    public static final int POWERED_RAIL = 27;
    public static final int DETECTOR_RAIL = 28;
    public static final int STICKY_PISTON = 29;
    public static final int WEB = 30;
    public static final int TALL_GRASS = 31;
    public static final int DEAD_SHRUB = 32;
    public static final int PISTON = 33;
    public static final int PISTON_HEAD = 34;
    public static final int WOOL = 35;
    public static final int DANDELION = 37;
    public static final int POPPY = 38;
    public static final int BROWN_MUSHROOM = 39;
    public static final int RED_MUSHROOM = 40;
    public static final int GOLD_BLOCK = 41;
    public static final int IRON_BLOCK = 42;
    public static final int DOUBLE_SLAB = 43;
    public static final int SLAB = 44;
    public static final int BRICK = 45;
    public static final int TNT = 46;
    public static final int BOOKSHELF = 47;
    public static final int MOSSY_COBBLESTONE = 48;
    public static final int OBSIDIAN = 49;
    public static final int TORCH = 50;
    public static final int FIRE = 51;
    public static final int MOB_SPAWNER = 52;
    public static final int WOODEN_STAIRS = 53;
    public static final int CHEST = 54;
    public static final int REDSTONE_WIRE = 55;
    public static final int DIAMOND_ORE = 56;
    public static final int DIAMOND_BLOCK = 57;
    public static final int CRAFTING_TABLE = 58;
    public static final int WHEAT = 59;
    public static final int FARMLAND = 60;
    public static final int FURNACE = 61;
    public static final int BURNING_FURNACE = 62;
    public static final int SIGN_POST = 63;
    public static final int WOODEN_DOOR = 64;
    public static final int LADDER = 65;
    public static final int RAIL = 66;
    public static final int COBBLE_STAIRS = 67;
    public static final int WALL_SIGN = 68;
    public static final int LEVER = 69;
    public static final int STONE_PRESSURE_PLATE = 70;
    public static final int IRON_DOOR = 71;
    public static final int WOODEN_PRESSURE_PLATE = 72;
    public static final int REDSTONE_ORE = 73;
    public static final int GLOWING_REDSTONE_ORE = 74;
    public static final int REDSTONE_TORCH_OFF = 75;
    public static final int REDSTONE_TORCH_ON = 76;
    public static final int STONE_BUTTON = 77;
    public static final int SNOW = 78;
    public static final int ICE = 79;
    public static final int SNOW_BLOCK = 80;
    public static final int CACTUS = 81;
    public static final int CLAY = 82;
    public static final int SUGAR_CANE = 83;
    public static final int JUKEBOX = 84;
    public static final int FENCE = 85;
    public static final int PUMPKIN = 86;
    public static final int NETHERRACK = 87;
    public static final int SOUL_SAND = 88;
    public static final int GLOWSTONE = 89;
    public static final int PORTAL = 90;
    public static final int JACK_O_LANTERN = 91;
    public static final int CAKE = 92;
    public static final int REDSTONE_REPEATER_OFF = 93;
    public static final int REDSTONE_REPEATER_ON = 94;

    private int id;
    private int data;
    private String name;
    private boolean solid;
    private float hardness;
    private float resistance;
    private int lightLevel;

    public Block(int id, String name) {
        this.id = id;
        this.name = name;
        this.data = 0;
        this.solid = true;
        this.hardness = 0;
        this.resistance = 0;
        this.lightLevel = 0;
    }

    public Block(int id, int data, String name) {
        this(id, name);
        this.data = data;
    }

    // Getters and Setters
    public int getId() { return id; }
    public int getData() { return data; }
    public void setData(int data) { this.data = data; }
    public String getName() { return name; }
    public boolean isSolid() { return solid; }
    public void setSolid(boolean solid) { this.solid = solid; }
    public float getHardness() { return hardness; }
    public void setHardness(float hardness) { this.hardness = hardness; }
    public float getResistance() { return resistance; }
    public void setResistance(float resistance) { this.resistance = resistance; }
    public int getLightLevel() { return lightLevel; }
    public void setLightLevel(int level) { this.lightLevel = level; }

    public boolean isTransparent() {
        switch (id) {
            case AIR:
            case WATER:
            case STATIONARY_WATER:
            case LAVA:
            case STATIONARY_LAVA:
            case GLASS:
            case LEAVES:
            case WEB:
            case FENCE:
                return true;
            default:
                return false;
        }
    }

    public boolean isLiquid() {
        return id == WATER || id == STATIONARY_WATER || id == LAVA || id == STATIONARY_LAVA;
    }

    public static boolean isSolidBlockId(int blockId) {
        switch (blockId) {
            case AIR:
            case WATER:
            case STATIONARY_WATER:
            case LAVA:
            case STATIONARY_LAVA:
            case TALL_GRASS:
            case DEAD_SHRUB:
            case DANDELION:
            case POPPY:
            case BROWN_MUSHROOM:
            case RED_MUSHROOM:
            case FIRE:
            case SNOW:
            case SUGAR_CANE:
            case PORTAL:
            case TORCH:
            case REDSTONE_WIRE:
            case REDSTONE_TORCH_OFF:
            case REDSTONE_TORCH_ON:
                return false;
            default:
                return true;
        }
    }

    @Override
    public String toString() {
        return "Block{" +
                "id=" + id +
                ", data=" + data +
                ", name='" + name + '\'' +
                '}';
    }
}
