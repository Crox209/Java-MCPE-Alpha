package com.minecraft.mcpe.block;

import java.util.HashMap;
import java.util.Map;

/**
 * Registry for all block types
 */
public class BlockRegistry {
    private static final Map<Integer, Block> blocks = new HashMap<>();
    private static final Map<String, Integer> blockNameToId = new HashMap<>();

    static {
        registerDefaultBlocks();
    }

    private static void registerDefaultBlocks() {
        registerBlock(new Block(Block.AIR, "air"));
        registerBlock(new Block(Block.STONE, "stone"));
        registerBlock(new Block(Block.GRASS, "grass"));
        registerBlock(new Block(Block.DIRT, "dirt"));
        registerBlock(new Block(Block.COBBLESTONE, "cobblestone"));
        registerBlock(new Block(Block.WOOD, "wood"));
        registerBlock(new Block(Block.SAPLING, "sapling"));
        registerBlock(new Block(Block.BEDROCK, "bedrock"));
        registerBlock(new Block(Block.WATER, "water"));
        registerBlock(new Block(Block.STATIONARY_WATER, "stationary_water"));
        registerBlock(new Block(Block.LAVA, "lava"));
        registerBlock(new Block(Block.STATIONARY_LAVA, "stationary_lava"));
        registerBlock(new Block(Block.SAND, "sand"));
        registerBlock(new Block(Block.GRAVEL, "gravel"));
        registerBlock(new Block(Block.GOLD_ORE, "gold_ore"));
        registerBlock(new Block(Block.IRON_ORE, "iron_ore"));
        registerBlock(new Block(Block.COAL_ORE, "coal_ore"));
        registerBlock(new Block(Block.LOG, "log"));
        registerBlock(new Block(Block.LEAVES, "leaves"));
        registerBlock(new Block(Block.SPONGE, "sponge"));
        registerBlock(new Block(Block.GLASS, "glass"));
        registerBlock(new Block(Block.LAPIS_ORE, "lapis_ore"));
        registerBlock(new Block(Block.LAPIS_BLOCK, "lapis_block"));
        registerBlock(new Block(Block.DISPENSER, "dispenser"));
        registerBlock(new Block(Block.SANDSTONE, "sandstone"));
        registerBlock(new Block(Block.NOTE_BLOCK, "note_block"));
        registerBlock(new Block(Block.BED, "bed"));
        registerBlock(new Block(Block.POWERED_RAIL, "powered_rail"));
        registerBlock(new Block(Block.DETECTOR_RAIL, "detector_rail"));
        registerBlock(new Block(Block.STICKY_PISTON, "sticky_piston"));
        registerBlock(new Block(Block.WEB, "web"));
        registerBlock(new Block(Block.TALL_GRASS, "tall_grass"));
        registerBlock(new Block(Block.DEAD_SHRUB, "dead_shrub"));
        registerBlock(new Block(Block.PISTON, "piston"));
        registerBlock(new Block(Block.PISTON_HEAD, "piston_head"));
        registerBlock(new Block(Block.WOOL, "wool"));
        registerBlock(new Block(Block.DANDELION, "dandelion"));
        registerBlock(new Block(Block.POPPY, "poppy"));
        registerBlock(new Block(Block.BROWN_MUSHROOM, "brown_mushroom"));
        registerBlock(new Block(Block.RED_MUSHROOM, "red_mushroom"));
        registerBlock(new Block(Block.GOLD_BLOCK, "gold_block"));
        registerBlock(new Block(Block.IRON_BLOCK, "iron_block"));
        registerBlock(new Block(Block.DOUBLE_SLAB, "double_slab"));
        registerBlock(new Block(Block.SLAB, "slab"));
        registerBlock(new Block(Block.BRICK, "brick"));
        registerBlock(new Block(Block.TNT, "tnt"));
        registerBlock(new Block(Block.BOOKSHELF, "bookshelf"));
        registerBlock(new Block(Block.MOSSY_COBBLESTONE, "mossy_cobblestone"));
        registerBlock(new Block(Block.OBSIDIAN, "obsidian"));
        registerBlock(new Block(Block.TORCH, "torch"));
        registerBlock(new Block(Block.FIRE, "fire"));
        registerBlock(new Block(Block.MOB_SPAWNER, "mob_spawner"));
        registerBlock(new Block(Block.WOODEN_STAIRS, "wooden_stairs"));
        registerBlock(new Block(Block.CHEST, "chest"));
        registerBlock(new Block(Block.REDSTONE_WIRE, "redstone_wire"));
        registerBlock(new Block(Block.DIAMOND_ORE, "diamond_ore"));
        registerBlock(new Block(Block.DIAMOND_BLOCK, "diamond_block"));
        registerBlock(new Block(Block.CRAFTING_TABLE, "crafting_table"));
        registerBlock(new Block(Block.WHEAT, "wheat"));
        registerBlock(new Block(Block.FARMLAND, "farmland"));
        registerBlock(new Block(Block.FURNACE, "furnace"));
        registerBlock(new Block(Block.BURNING_FURNACE, "burning_furnace"));
        registerBlock(new Block(Block.SIGN_POST, "sign_post"));
        registerBlock(new Block(Block.WOODEN_DOOR, "wooden_door"));
        registerBlock(new Block(Block.LADDER, "ladder"));
        registerBlock(new Block(Block.RAIL, "rail"));
        registerBlock(new Block(Block.COBBLE_STAIRS, "cobble_stairs"));
        registerBlock(new Block(Block.WALL_SIGN, "wall_sign"));
        registerBlock(new Block(Block.LEVER, "lever"));
        registerBlock(new Block(Block.STONE_PRESSURE_PLATE, "stone_pressure_plate"));
        registerBlock(new Block(Block.IRON_DOOR, "iron_door"));
        registerBlock(new Block(Block.WOODEN_PRESSURE_PLATE, "wooden_pressure_plate"));
        registerBlock(new Block(Block.REDSTONE_ORE, "redstone_ore"));
        registerBlock(new Block(Block.GLOWING_REDSTONE_ORE, "glowing_redstone_ore"));
        registerBlock(new Block(Block.REDSTONE_TORCH_OFF, "redstone_torch_off"));
        registerBlock(new Block(Block.REDSTONE_TORCH_ON, "redstone_torch_on"));
        registerBlock(new Block(Block.STONE_BUTTON, "stone_button"));
        registerBlock(new Block(Block.SNOW, "snow"));
        registerBlock(new Block(Block.ICE, "ice"));
        registerBlock(new Block(Block.SNOW_BLOCK, "snow_block"));
        registerBlock(new Block(Block.CACTUS, "cactus"));
        registerBlock(new Block(Block.CLAY, "clay"));
        registerBlock(new Block(Block.SUGAR_CANE, "sugar_cane"));
        registerBlock(new Block(Block.JUKEBOX, "jukebox"));
        registerBlock(new Block(Block.FENCE, "fence"));
        registerBlock(new Block(Block.PUMPKIN, "pumpkin"));
        registerBlock(new Block(Block.NETHERRACK, "netherrack"));
        registerBlock(new Block(Block.SOUL_SAND, "soul_sand"));
        registerBlock(new Block(Block.GLOWSTONE, "glowstone"));
        registerBlock(new Block(Block.PORTAL, "portal"));
        registerBlock(new Block(Block.JACK_O_LANTERN, "jack_o_lantern"));
        registerBlock(new Block(Block.CAKE, "cake"));
        registerBlock(new Block(Block.REDSTONE_REPEATER_OFF, "redstone_repeater_off"));
        registerBlock(new Block(Block.REDSTONE_REPEATER_ON, "redstone_repeater_on"));
    }

    public static void registerBlock(Block block) {
        blocks.put(block.getId(), block);
        blockNameToId.put(block.getName(), block.getId());
    }

    public static Block getBlock(int id) {
        return blocks.getOrDefault(id, blocks.get(Block.AIR));
    }

    public static Block getBlock(String name) {
        Integer id = blockNameToId.get(name.toLowerCase());
        if (id != null) {
            return blocks.get(id);
        }
        return blocks.get(Block.AIR);
    }

    public static int getBlockId(String name) {
        return blockNameToId.getOrDefault(name.toLowerCase(), Block.AIR);
    }

    public static Map<Integer, Block> getAllBlocks() {
        return new HashMap<>(blocks);
    }
}
