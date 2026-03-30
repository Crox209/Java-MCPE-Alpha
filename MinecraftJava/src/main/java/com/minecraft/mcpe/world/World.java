package com.minecraft.mcpe.world;

import com.minecraft.mcpe.block.Block;
import com.minecraft.mcpe.block.BlockEntity;
import com.minecraft.mcpe.block.FurnaceBlockEntity;
import com.minecraft.mcpe.block.ChestBlockEntity;
import com.minecraft.mcpe.block.CraftingTableBlockEntity;
import com.minecraft.mcpe.entity.Entity;
import com.minecraft.mcpe.entity.Mob;
import com.minecraft.mcpe.entity.Player;
import com.minecraft.mcpe.entity.ItemEntity;
import com.minecraft.mcpe.entity.Cow;
import com.minecraft.mcpe.entity.Pig;
import com.minecraft.mcpe.entity.Sheep;
import com.minecraft.mcpe.entity.Chicken;
import com.minecraft.mcpe.entity.Skeleton;
import com.minecraft.mcpe.entity.Spider;
import com.minecraft.mcpe.entity.Creeper;
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
    private Map<Long, BlockEntity> blockEntities;
    private Set<Long> openDoorPositions;
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
        this.blockEntities = new ConcurrentHashMap<>();
        this.openDoorPositions = ConcurrentHashMap.newKeySet();
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
    public void setWorldTime(int time) {
        this.worldTime = time;
        this.dayTime = Math.floorMod(time, 24000);
    }
    public int getDayTime() { return dayTime; }
    public void setDayTime(int time) { this.dayTime = time; }

    public boolean isDayTime() {
        return dayTime < 13000 || dayTime > 23000;
    }

    public boolean isNightTime() {
        return !isDayTime();
    }

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

    private long getBlockEntityKey(int x, int y, int z) {
        return ((long) x << 40) | ((long) y << 20) | (z & 0xFFFFF);
    }

    private long getBlockPosKey(int x, int y, int z) {
        return getBlockEntityKey(x, y, z);
    }

    public BlockEntity getBlockEntity(int x, int y, int z) {
        return blockEntities.get(getBlockEntityKey(x, y, z));
    }

    public void setBlockEntity(int x, int y, int z, BlockEntity entity) {
        if (entity == null) {
            blockEntities.remove(getBlockEntityKey(x, y, z));
        } else {
            blockEntities.put(getBlockEntityKey(x, y, z), entity);
        }
    }

    public FurnaceBlockEntity createFurnace(int x, int y, int z) {
        FurnaceBlockEntity furnace = new FurnaceBlockEntity(x, y, z);
        setBlock(x, y, z, Block.FURNACE);
        setBlockEntity(x, y, z, furnace);
        return furnace;
    }

    public ChestBlockEntity createChest(int x, int y, int z) {
        ChestBlockEntity chest = new ChestBlockEntity(x, y, z);
        setBlock(x, y, z, Block.CHEST);
        setBlockEntity(x, y, z, chest);
        return chest;
    }

    public CraftingTableBlockEntity createCraftingTable(int x, int y, int z) {
        CraftingTableBlockEntity table = new CraftingTableBlockEntity(x, y, z);
        setBlock(x, y, z, Block.CRAFTING_TABLE);
        setBlockEntity(x, y, z, table);
        return table;
    }

    public Collection<BlockEntity> getBlockEntities() {
        return Collections.unmodifiableCollection(blockEntities.values());
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

            // Keep block-entity map consistent with block changes.
            if (blockId == Block.AIR) {
                setBlockEntity(x, y, z, null);
                openDoorPositions.remove(getBlockPosKey(x, y, z));
            } else {
                BlockEntity existing = getBlockEntity(x, y, z);
                if (existing != null && existing.getBlockId() != blockId) {
                    setBlockEntity(x, y, z, null);
                }
                if (blockId != Block.WOODEN_DOOR && blockId != Block.IRON_DOOR) {
                    openDoorPositions.remove(getBlockPosKey(x, y, z));
                }
            }
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
        if ((blockId == Block.WOODEN_DOOR || blockId == Block.IRON_DOOR) && isDoorOpen(x, y, z)) {
            return false;
        }
        return Block.isSolidBlockId(blockId);
    }

    public boolean isDoorOpen(int x, int y, int z) {
        return openDoorPositions.contains(getBlockPosKey(x, y, z));
    }

    public boolean toggleDoor(int x, int y, int z) {
        int blockId = getBlock(x, y, z);
        if (blockId != Block.WOODEN_DOOR && blockId != Block.IRON_DOOR) {
            return false;
        }

        int baseY = y;
        if (getBlock(x, y - 1, z) == blockId) {
            baseY = y - 1;
        }

        long lowerKey = getBlockPosKey(x, baseY, z);
        long upperKey = getBlockPosKey(x, baseY + 1, z);
        boolean currentlyOpen = openDoorPositions.contains(lowerKey) || openDoorPositions.contains(upperKey);

        if (currentlyOpen) {
            openDoorPositions.remove(lowerKey);
            openDoorPositions.remove(upperKey);
        } else {
            openDoorPositions.add(lowerKey);
            if (getBlock(x, baseY + 1, z) == blockId) {
                openDoorPositions.add(upperKey);
            }
        }
        return true;
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

    public boolean canSeeSky(int x, int y, int z) {
        for (int yy = y + 1; yy < Chunk.HEIGHT; yy++) {
            if (isSolidBlock(x, yy, z)) {
                return false;
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
                if (entity instanceof Mob) {
                    spawnDropsForMob((Mob) entity);
                }
                entities.remove(entity);
                continue;
            }

            if (player != null && shouldDespawnEntity(entity, player)) {
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

        if (player != null) {
            mergeNearbyItemEntities();
            processItemPickups(player);
        }

        int mobCount = getMobCount();
        int hostileCount = getHostileMobCount();
        int passiveCount = mobCount - hostileCount;

        if (player != null && worldTime % 120 == 0) {
            if (isNightTime() && hostileCount < 28) {
                maybeSpawnMobNearPlayer(player);
            } else if (isDayTime() && passiveCount < 18) {
                maybeSpawnMobNearPlayer(player);
            }
        }

        if (player != null && worldTime % 200 == 0 && mobCount < 32) {
            maybeSpawnMobNearPlayer(player);
        }
    }

    public void addEntity(Entity entity) {
        if (entity != null) {
            entities.add(entity);
        }
    }

    public void spawnItemDrop(int blockId, int count, double x, double y, double z) {
        if (blockId <= 0 || count <= 0) {
            return;
        }

        int remaining = count;
        while (remaining > 0) {
            int stack = Math.min(remaining, ItemEntity.MAX_STACK_SIZE);
            ItemEntity drop = new ItemEntity(this, blockId, stack);
            drop.setPosition(new Vector3f(
                x + (random.nextDouble() - 0.5) * 0.3,
                y + 0.2,
                z + (random.nextDouble() - 0.5) * 0.3));
            drop.setVelocity(new Vector3f(
                (random.nextDouble() - 0.5) * 0.06,
                0.12 + random.nextDouble() * 0.04,
                (random.nextDouble() - 0.5) * 0.06));
            addEntity(drop);
            remaining -= stack;
        }
    }

    public Collection<Entity> getEntities() {
        return Collections.unmodifiableList(entities);
    }

    private void spawnInitialMobs() {
        for (int i = 0; i < 16; i++) {
            double x = spawnPoint.x + (random.nextDouble() - 0.5) * 48.0;
            double z = spawnPoint.z + (random.nextDouble() - 0.5) * 48.0;
            int y = findGroundY((int) Math.floor(x), (int) Math.floor(z));
            Mob mob = createRandomMobForTime(isNightTime() ? "hostile" : "passive");
            mob.setPosition(new Vector3f(x, y + 1, z));
            entities.add(mob);
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
            Mob mob = createRandomMobForTime(isNightTime() ? "hostile" : "passive");
            mob.setPosition(new Vector3f(x, y + 1, z));
            entities.add(mob);
        }
    }

    private Mob createRandomMobForTime(String group) {
        int roll = random.nextInt(100);
        if ("hostile".equals(group)) {
            if (roll < 45) return new Zombie(this);
            if (roll < 70) return new Skeleton(this);
            if (roll < 90) return new Spider(this);
            return new Creeper(this);
        }

        if (roll < 30) return new Cow(this);
        if (roll < 55) return new Pig(this);
        if (roll < 80) return new Sheep(this);
        return new Chicken(this);
    }

    private int findGroundY(int x, int z) {
        for (int y = Chunk.HEIGHT - 2; y >= 1; y--) {
            if (isSolidBlock(x, y, z) && !isSolidBlock(x, y + 1, z)) {
                return y;
            }
        }
        return 63;
    }

    private void spawnDropsForMob(Mob mob) {
        if (mob == null) {
            return;
        }

        int[] dropBlockIds = mob.getDropBlockIds(random);
        for (int blockId : dropBlockIds) {
            if (blockId <= 0) {
                continue;
            }
            spawnItemDrop(blockId, 1, mob.getPosition().x, mob.getPosition().y, mob.getPosition().z);
        }
    }

    private int getMobCount() {
        int count = 0;
        for (Entity entity : entities) {
            if (entity instanceof Mob && entity.isAlive()) {
                count++;
            }
        }
        return count;
    }

    private int getHostileMobCount() {
        int count = 0;
        for (Entity entity : entities) {
            if (entity instanceof Mob && entity.isAlive() && ((Mob) entity).isHostile()) {
                count++;
            }
        }
        return count;
    }

    private boolean shouldDespawnEntity(Entity entity, Player player) {
        double dx = entity.getPosition().x - player.getPosition().x;
        double dy = entity.getPosition().y - player.getPosition().y;
        double dz = entity.getPosition().z - player.getPosition().z;
        double distSq = dx * dx + dy * dy + dz * dz;

        if (entity instanceof ItemEntity) {
            ItemEntity item = (ItemEntity) entity;
            if (item.getCount() <= 0) {
                return true;
            }
            return distSq > 72.0 * 72.0;
        }

        if (entity instanceof Mob) {
            return distSq > 96.0 * 96.0;
        }

        return false;
    }

    private void mergeNearbyItemEntities() {
        if (worldTime % 20 != 0) {
            return;
        }

        for (Entity a : entities) {
            if (!(a instanceof ItemEntity) || !a.isAlive()) {
                continue;
            }
            ItemEntity ia = (ItemEntity) a;

            for (Entity b : entities) {
                if (a == b || !(b instanceof ItemEntity) || !b.isAlive()) {
                    continue;
                }
                ItemEntity ib = (ItemEntity) b;
                if (!ia.canMergeWith(ib)) {
                    continue;
                }

                double dx = ia.getPosition().x - ib.getPosition().x;
                double dy = ia.getPosition().y - ib.getPosition().y;
                double dz = ia.getPosition().z - ib.getPosition().z;
                double distSq = dx * dx + dy * dy + dz * dz;
                if (distSq <= 0.75 * 0.75) {
                    ia.mergeFrom(ib);
                }
            }
        }
    }

    private void processItemPickups(Player player) {
        for (Entity entity : entities) {
            if (!(entity instanceof ItemEntity) || !entity.isAlive()) {
                continue;
            }

            ItemEntity item = (ItemEntity) entity;
            if (!item.canBePickedUp()) {
                continue;
            }

            double dx = player.getPosition().x - item.getPosition().x;
            double dy = (player.getPosition().y + 1.0) - item.getPosition().y;
            double dz = player.getPosition().z - item.getPosition().z;
            double distSq = dx * dx + dy * dy + dz * dz;

            if (distSq <= 6.0 * 6.0 && distSq > 0.001) {
                double inv = 1.0 / Math.sqrt(distSq);
                double pull = 0.06;
                item.setVelocity(new Vector3f(
                    dx * inv * pull,
                    Math.max(item.getVelocity().y, 0.02),
                    dz * inv * pull));
            }

            if (distSq > 1.8 * 1.8) {
                continue;
            }

            int remaining = item.getCount();
            while (remaining > 0 && player.addBlockToInventory(item.getBlockId())) {
                remaining--;
            }
            item.setCount(remaining);
        }
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
        ListTag blockEntitiesTag = new ListTag("BlockEntities");
        blockEntitiesTag.elementType = 10;
        ListTag openDoorsTag = new ListTag("OpenDoors");
        openDoorsTag.elementType = 10;
        
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
            if (entity instanceof Mob) {
                Mob mob = (Mob) entity;
                CompoundTag mobTag = new CompoundTag("");
                mobTag.put("Type", new StringTag("Type", mob.getMobType()));
                mobTag.put("PosX", new FloatTag("PosX", (float) mob.getPosition().x));
                mobTag.put("PosY", new FloatTag("PosY", (float) mob.getPosition().y));
                mobTag.put("PosZ", new FloatTag("PosZ", (float) mob.getPosition().z));
                mobTag.put("Health", new FloatTag("Health", mob.getHealth()));
                entitiesTag.value.add(mobTag);
            } else if (entity instanceof ItemEntity) {
                ItemEntity item = (ItemEntity) entity;
                CompoundTag itemTag = new CompoundTag("");
                itemTag.put("Type", new StringTag("Type", "ItemEntity"));
                itemTag.put("PosX", new FloatTag("PosX", (float) item.getPosition().x));
                itemTag.put("PosY", new FloatTag("PosY", (float) item.getPosition().y));
                itemTag.put("PosZ", new FloatTag("PosZ", (float) item.getPosition().z));
                itemTag.put("BlockId", new IntTag("BlockId", item.getBlockId()));
                itemTag.put("Count", new IntTag("Count", item.getCount()));
                entitiesTag.value.add(itemTag);
            }
        }

        for (BlockEntity blockEntity : blockEntities.values()) {
            if (blockEntity == null) {
                continue;
            }

            CompoundTag blockEntityTag = new CompoundTag("");
            String type = "Unknown";
            if (blockEntity instanceof FurnaceBlockEntity) {
                type = "Furnace";
            } else if (blockEntity instanceof ChestBlockEntity) {
                type = "Chest";
            } else if (blockEntity instanceof CraftingTableBlockEntity) {
                type = "CraftingTable";
            }
            blockEntityTag.put("Type", new StringTag("Type", type));
            blockEntityTag.put("X", new IntTag("X", blockEntity.getX()));
            blockEntityTag.put("Y", new IntTag("Y", blockEntity.getY()));
            blockEntityTag.put("Z", new IntTag("Z", blockEntity.getZ()));

            CompoundTag blockEntityDataTag = new CompoundTag("Data");
            blockEntity.save(blockEntityDataTag);
            blockEntityTag.put("Data", blockEntityDataTag);
            blockEntitiesTag.value.add(blockEntityTag);
        }

        for (Long key : openDoorPositions) {
            int x = (int) (key >> 40);
            int y = (int) ((key >> 20) & 0xFFFFF);
            int z = (int) (key & 0xFFFFF);
            if (z >= 0x80000) {
                z -= 0x100000;
            }

            CompoundTag doorTag = new CompoundTag("");
            doorTag.put("X", new IntTag("X", x));
            doorTag.put("Y", new IntTag("Y", y));
            doorTag.put("Z", new IntTag("Z", z));
            openDoorsTag.value.add(doorTag);
        }
        
        root.put("Data", data);
        root.put("Chunks", chunksTag);
        root.put("Entities", entitiesTag);
        root.put("BlockEntities", blockEntitiesTag);
        root.put("OpenDoors", openDoorsTag);
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
                } else if ("Skeleton".equals(type)) {
                    mob = new Skeleton(world);
                } else if ("Spider".equals(type)) {
                    mob = new Spider(world);
                } else if ("Creeper".equals(type)) {
                    mob = new Creeper(world);
                } else if ("Cow".equals(type)) {
                    mob = new Cow(world);
                } else if ("Pig".equals(type)) {
                    mob = new Pig(world);
                } else if ("Sheep".equals(type)) {
                    mob = new Sheep(world);
                } else if ("Chicken".equals(type)) {
                    mob = new Chicken(world);
                } else if ("ItemEntity".equals(type)) {
                    int blockId = 0;
                    int count = 1;
                    Tag blockIdTag = mobTag.get("BlockId");
                    Tag countTag = mobTag.get("Count");
                    if (blockIdTag instanceof IntTag) blockId = ((IntTag) blockIdTag).value;
                    if (countTag instanceof IntTag) count = ((IntTag) countTag).value;
                    mob = new ItemEntity(world, blockId, count);
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

        Tag blockEntitiesRaw = root.get("BlockEntities");
        if (blockEntitiesRaw instanceof ListTag) {
            ListTag blockEntitiesTag = (ListTag) blockEntitiesRaw;
            for (Tag tag : blockEntitiesTag.value) {
                if (!(tag instanceof CompoundTag)) {
                    continue;
                }

                CompoundTag blockEntityTag = (CompoundTag) tag;
                Tag typeTag = blockEntityTag.get("Type");
                Tag xTag = blockEntityTag.get("X");
                Tag yTag = blockEntityTag.get("Y");
                Tag zTag = blockEntityTag.get("Z");
                Tag dataTag = blockEntityTag.get("Data");

                if (!(typeTag instanceof StringTag) || !(xTag instanceof IntTag)
                        || !(yTag instanceof IntTag) || !(zTag instanceof IntTag)
                        || !(dataTag instanceof CompoundTag)) {
                    continue;
                }

                String type = ((StringTag) typeTag).value;
                int x = ((IntTag) xTag).value;
                int y = ((IntTag) yTag).value;
                int z = ((IntTag) zTag).value;
                CompoundTag beData = (CompoundTag) dataTag;

                BlockEntity blockEntity = null;
                if ("Furnace".equals(type)) {
                    blockEntity = new FurnaceBlockEntity(x, y, z);
                } else if ("Chest".equals(type)) {
                    blockEntity = new ChestBlockEntity(x, y, z);
                } else if ("CraftingTable".equals(type)) {
                    blockEntity = new CraftingTableBlockEntity(x, y, z);
                }

                if (blockEntity == null) {
                    continue;
                }

                blockEntity.load(beData);
                world.setBlockEntity(x, y, z, blockEntity);
            }
        }

        Tag openDoorsRaw = root.get("OpenDoors");
        if (openDoorsRaw instanceof ListTag) {
            ListTag openDoorsTag = (ListTag) openDoorsRaw;
            for (Tag tag : openDoorsTag.value) {
                if (!(tag instanceof CompoundTag)) {
                    continue;
                }
                CompoundTag doorTag = (CompoundTag) tag;
                Tag xTag = doorTag.get("X");
                Tag yTag = doorTag.get("Y");
                Tag zTag = doorTag.get("Z");
                if (!(xTag instanceof IntTag) || !(yTag instanceof IntTag) || !(zTag instanceof IntTag)) {
                    continue;
                }

                int x = ((IntTag) xTag).value;
                int y = ((IntTag) yTag).value;
                int z = ((IntTag) zTag).value;
                int blockId = world.getBlock(x, y, z);
                if (blockId == Block.WOODEN_DOOR || blockId == Block.IRON_DOOR) {
                    world.openDoorPositions.add(world.getBlockPosKey(x, y, z));
                }
            }
        }

        return world;
    }
}
