package com.minecraft.mcpe.world;

import com.minecraft.mcpe.block.Block;
import com.minecraft.mcpe.entity.Entity;
import com.minecraft.mcpe.entity.Mob;
import com.minecraft.mcpe.entity.Player;
import com.minecraft.mcpe.entity.Zombie;
import com.minecraft.mcpe.nbt.*;
import com.minecraft.mcpe.util.Vector3f;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ConcurrentHashMap;

/**
 * World - manages chunks and world data
 */
public class World {
    private static final Logger logger = LoggerFactory.getLogger(World.class);
    
    private String name;
    private long seed;
    private int gameMode; // 0 = Survival, 1 = Creative
    private Map<Long, Chunk> chunks;
    private Vector3f spawnPoint;
    private int worldTime;
    private int dayTime;
    private List<Entity> entities;
    private Random random;

    public World(String name, long seed) {
        this.name = name;
        this.seed = seed;
        this.gameMode = 0; // Survival
        this.chunks = new ConcurrentHashMap<>();
        this.spawnPoint = new Vector3f(0, 64, 0);
        this.worldTime = 0;
        this.dayTime = 0;
        this.entities = new CopyOnWriteArrayList<>();
        this.random = new Random(seed);
    }

    public String getName() { return name; }
    public long getSeed() { return seed; }
    public int getGameMode() { return gameMode; }
    public void setGameMode(int mode) { this.gameMode = mode; }
    public Vector3f getSpawnPoint() { return spawnPoint; }
    public void setSpawnPoint(Vector3f pos) { this.spawnPoint = pos; }
    public int getWorldTime() { return worldTime; }
    public void setWorldTime(int time) { this.worldTime = time; }
    public int getDayTime() { return dayTime; }
    public void setDayTime(int time) { this.dayTime = time; }

    private long getChunkKey(int x, int z) {
        return ((long) x << 32) | (z & 0xFFFFFFFFL);
    }

    public Chunk getChunk(int x, int z) {
        long key = getChunkKey(x, z);
        return chunks.computeIfAbsent(key, k -> {
            logger.debug("Creating new chunk at ({}, {})", x, z);
            return new Chunk(x, z);
        });
    }

    public void setBlock(float x, float y, float z, int blockId) {
        int blockX = (int) Math.floor(x);
        int blockY = (int) Math.floor(y);
        int blockZ = (int) Math.floor(z);
        setBlock(blockX, blockY, blockZ, blockId);
    }

    public void setBlock(int x, int y, int z, int blockId) {
        int chunkX = (int) Math.floor(x / (float) Chunk.WIDTH);
        int chunkZ = (int) Math.floor(z / (float) Chunk.DEPTH);
        int localX = x - (chunkX * Chunk.WIDTH);
        int localZ = z - (chunkZ * Chunk.DEPTH);

        if (y >= 0 && y < Chunk.HEIGHT) {
            Chunk chunk = getChunk(chunkX, chunkZ);
            chunk.setBlock(localX, y, localZ, blockId);
        }
    }

    public int getBlock(float x, float y, float z) {
        return getBlock((int) Math.floor(x), (int) Math.floor(y), (int) Math.floor(z));
    }

    public int getBlock(int x, int y, int z) {
        int chunkX = (int) Math.floor(x / (float) Chunk.WIDTH);
        int chunkZ = (int) Math.floor(z / (float) Chunk.DEPTH);
        int localX = x - (chunkX * Chunk.WIDTH);
        int localZ = z - (chunkZ * Chunk.DEPTH);

        if (y < 0 || y >= Chunk.HEIGHT) {
            return Block.AIR;
        }

        Chunk chunk = chunks.get(getChunkKey(chunkX, chunkZ));
        if (chunk == null) {
            return Block.AIR;
        }

        return chunk.getBlock(localX, y, localZ);
    }

    public boolean isSolidBlock(int x, int y, int z) {
        int blockId = getBlock(x, y, z);
        return Block.isSolidBlockId(blockId);
    }

    public boolean isAreaEmpty(double minX, double minY, double minZ, double maxX, double maxY, double maxZ) {
        int x0 = (int) Math.floor(minX);
        int y0 = (int) Math.floor(minY);
        int z0 = (int) Math.floor(minZ);
        int x1 = (int) Math.floor(maxX);
        int y1 = (int) Math.floor(maxY);
        int z1 = (int) Math.floor(maxZ);

        for (int x = x0; x <= x1; x++) {
            for (int y = y0; y <= y1; y++) {
                for (int z = z0; z <= z1; z++) {
                    if (isSolidBlock(x, y, z)) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    public void generateTerrain() {
        logger.info("Generating terrain for world: {}", name);
        
        // Generate a simple flat terrain
        Random random = new Random(seed);
        
        for (int chunkX = -5; chunkX <= 5; chunkX++) {
            for (int chunkZ = -5; chunkZ <= 5; chunkZ++) {
                Chunk chunk = getChunk(chunkX, chunkZ);
                
                for (int x = 0; x < Chunk.WIDTH; x++) {
                    for (int z = 0; z < Chunk.DEPTH; z++) {
                        // Bedrock layer
                        chunk.setBlock(x, 0, z, Block.BEDROCK);
                        
                        // Stone layers
                        for (int y = 1; y < 64; y++) {
                            chunk.setBlock(x, y, z, Block.STONE);
                        }
                        
                        // Dirt layers
                        for (int y = 61; y < 63; y++) {
                            chunk.setBlock(x, y, z, Block.DIRT);
                        }
                        
                        // Grass on top
                        chunk.setBlock(x, 63, z, Block.GRASS);
                        
                        // Sometimes add trees and vegetation
                        if (random.nextDouble() < 0.02) {
                            // Tree
                            int treeHeight = 4 + random.nextInt(3);
                            for (int y = 64; y < 64 + treeHeight; y++) {
                                chunk.setBlock(x, y, z, Block.LOG);
                            }
                            // Leaves
                            for (int dx = -2; dx <= 2; dx++) {
                                for (int dz = -2; dz <= 2; dz++) {
                                    for (int dy = 0; dy < treeHeight; dy++) {
                                        setBlock(chunkX * Chunk.WIDTH + x + dx, 64 + treeHeight + dy, 
                                                 chunkZ * Chunk.DEPTH + z + dz, Block.LEAVES);
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        
        logger.info("Terrain generation complete");

        if (entities.isEmpty()) {
            spawnInitialMobs();
        }
    }

    public void update() {
        update(null);
    }

    public void update(Player player) {
        worldTime++;
        dayTime = worldTime % 24000;

        for (Entity entity : entities) {
            if (!entity.isAlive()) {
                entities.remove(entity);
                continue;
            }

            if (entity instanceof Mob) {
                ((Mob) entity).updateAI(player);
                entity.update();
            } else {
                entity.update();
            }
        }

        if (player != null && worldTime % 200 == 0 && entities.size() < 24) {
            maybeSpawnMobNearPlayer(player);
        }
    }

    public void addEntity(Entity entity) {
        if (entity != null) {
            entities.add(entity);
        }
    }

    public Collection<Entity> getEntities() {
        return Collections.unmodifiableList(entities);
    }

    private void spawnInitialMobs() {
        for (int i = 0; i < 8; i++) {
            double x = spawnPoint.x + (random.nextDouble() - 0.5) * 48.0;
            double z = spawnPoint.z + (random.nextDouble() - 0.5) * 48.0;
            int y = findGroundY((int) Math.floor(x), (int) Math.floor(z));
            Zombie zombie = new Zombie(this);
            zombie.setPosition(new Vector3f(x, y + 1, z));
            entities.add(zombie);
        }
    }

    private void maybeSpawnMobNearPlayer(Player player) {
        double angle = random.nextDouble() * Math.PI * 2.0;
        double distance = 14.0 + random.nextDouble() * 10.0;
        double x = player.getPosition().x + Math.cos(angle) * distance;
        double z = player.getPosition().z + Math.sin(angle) * distance;
        int y = findGroundY((int) Math.floor(x), (int) Math.floor(z));

        if (isSolidBlock((int) Math.floor(x), y, (int) Math.floor(z))
                && isAreaEmpty(x - 0.3, y + 1.0, z - 0.3, x + 0.3, y + 2.95, z + 0.3)) {
            Zombie zombie = new Zombie(this);
            zombie.setPosition(new Vector3f(x, y + 1, z));
            entities.add(zombie);
        }
    }

    private int findGroundY(int x, int z) {
        for (int y = Chunk.HEIGHT - 2; y >= 1; y--) {
            if (isSolidBlock(x, y, z) && !isSolidBlock(x, y + 1, z)) {
                return y;
            }
        }
        return 63;
    }

    public void unloadChunk(int x, int z) {
        chunks.remove(getChunkKey(x, z));
    }

    public void unloadFarChunks(int centerX, int centerZ, int renderDistance) {
        chunks.keySet().removeIf(key -> {
            int chunkX = (int) (key >> 32);
            int chunkZ = (int) (long) key;
            int distance = Math.max(Math.abs(chunkX - centerX), Math.abs(chunkZ - centerZ));
            return distance > renderDistance + 1;
        });
    }

    public Collection<Chunk> getLoadedChunks() {
        return chunks.values();
    }

    public CompoundTag toNBT() {
        CompoundTag root = new CompoundTag("Root");
        CompoundTag data = new CompoundTag("Data");
        ListTag chunksTag = new ListTag("Chunks");
        chunksTag.elementType = 10;
        ListTag entitiesTag = new ListTag("Entities");
        entitiesTag.elementType = 10;
        
        data.put("SpawnX", new IntTag("SpawnX", (int) spawnPoint.x));
        data.put("SpawnY", new IntTag("SpawnY", (int) spawnPoint.y));
        data.put("SpawnZ", new IntTag("SpawnZ", (int) spawnPoint.z));
        data.put("GameMode", new IntTag("GameMode", gameMode));
        data.put("Time", new LongTag("Time", worldTime));
        data.put("Seed", new LongTag("Seed", seed));

        for (Chunk chunk : chunks.values()) {
            CompoundTag chunkTag = chunk.toNBT();
            chunkTag.setName("");
            chunksTag.value.add(chunkTag);
        }

        for (Entity entity : entities) {
            if (!(entity instanceof Mob)) {
                continue;
            }
            Mob mob = (Mob) entity;
            CompoundTag mobTag = new CompoundTag("");
            mobTag.put("Type", new StringTag("Type", mob.getMobType()));
            mobTag.put("PosX", new FloatTag("PosX", (float) mob.getPosition().x));
            mobTag.put("PosY", new FloatTag("PosY", (float) mob.getPosition().y));
            mobTag.put("PosZ", new FloatTag("PosZ", (float) mob.getPosition().z));
            mobTag.put("Health", new FloatTag("Health", mob.getHealth()));
            entitiesTag.value.add(mobTag);
        }
        
        root.put("Data", data);
        root.put("Chunks", chunksTag);
        root.put("Entities", entitiesTag);
        return root;
    }

    public static World fromNBT(CompoundTag root) {
        CompoundTag data = (CompoundTag) root.get("Data");
        long seed = 0L;
        String worldName = "World";
        World world;

        if (data != null) {
            Tag seedTag = data.get("Seed");
            if (seedTag instanceof LongTag) {
                seed = ((LongTag) seedTag).value;
            }
            world = new World(worldName, seed);

            Tag gameModeTag = data.get("GameMode");
            if (gameModeTag instanceof IntTag) {
                world.setGameMode(((IntTag) gameModeTag).value);
            }

            Tag timeTag = data.get("Time");
            if (timeTag instanceof LongTag) {
                world.setWorldTime((int) ((LongTag) timeTag).value);
            }

            int spawnX = 0;
            int spawnY = 64;
            int spawnZ = 0;
            Tag sx = data.get("SpawnX");
            Tag sy = data.get("SpawnY");
            Tag sz = data.get("SpawnZ");
            if (sx instanceof IntTag) spawnX = ((IntTag) sx).value;
            if (sy instanceof IntTag) spawnY = ((IntTag) sy).value;
            if (sz instanceof IntTag) spawnZ = ((IntTag) sz).value;
            world.setSpawnPoint(new Vector3f(spawnX, spawnY, spawnZ));
        } else {
            world = new World(worldName, seed);
        }

        Tag chunksRaw = root.get("Chunks");
        if (chunksRaw instanceof ListTag) {
            ListTag chunksTag = (ListTag) chunksRaw;
            for (Tag tag : chunksTag.value) {
                if (!(tag instanceof CompoundTag)) {
                    continue;
                }
                CompoundTag chunkTag = (CompoundTag) tag;
                int chunkX = 0;
                int chunkZ = 0;
                Tag xTag = chunkTag.get("x");
                Tag zTag = chunkTag.get("z");
                if (xTag instanceof IntTag) chunkX = ((IntTag) xTag).value;
                if (zTag instanceof IntTag) chunkZ = ((IntTag) zTag).value;

                Chunk chunk = new Chunk(chunkX, chunkZ, chunkTag);
                world.chunks.put(world.getChunkKey(chunkX, chunkZ), chunk);
            }
        }

        Tag entitiesRaw = root.get("Entities");
        if (entitiesRaw instanceof ListTag) {
            ListTag entitiesTag = (ListTag) entitiesRaw;
            for (Tag tag : entitiesTag.value) {
                if (!(tag instanceof CompoundTag)) {
                    continue;
                }
                CompoundTag mobTag = (CompoundTag) tag;
                Tag typeTag = mobTag.get("Type");
                if (!(typeTag instanceof StringTag)) {
                    continue;
                }
                String type = ((StringTag) typeTag).value;
                Entity mob = null;
                if ("Zombie".equals(type)) {
                    mob = new Zombie(world);
                }

                if (mob == null) {
                    continue;
                }

                float x = 0f;
                float y = 65f;
                float z = 0f;
                float health = mob.getMaxHealth();

                Tag xTag = mobTag.get("PosX");
                Tag yTag = mobTag.get("PosY");
                Tag zTag = mobTag.get("PosZ");
                Tag healthTag = mobTag.get("Health");
                if (xTag instanceof FloatTag) x = ((FloatTag) xTag).value;
                if (yTag instanceof FloatTag) y = ((FloatTag) yTag).value;
                if (zTag instanceof FloatTag) z = ((FloatTag) zTag).value;
                if (healthTag instanceof FloatTag) health = ((FloatTag) healthTag).value;

                mob.setPosition(new Vector3f(x, y, z));
                mob.setHealth(health);
                if (mob.isAlive()) {
                    world.entities.add(mob);
                }
            }
        }

        return world;
    }
}
