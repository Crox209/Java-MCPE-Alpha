package com.minecraft.mcpe;

import com.minecraft.mcpe.block.Block;
import com.minecraft.mcpe.client.InputHandler;
import com.minecraft.mcpe.entity.Entity;
import com.minecraft.mcpe.entity.Mob;
import com.minecraft.mcpe.entity.Player;
import com.minecraft.mcpe.nbt.CompoundTag;
import com.minecraft.mcpe.nbt.IntTag;
import com.minecraft.mcpe.nbt.IntArrayTag;
import com.minecraft.mcpe.nbt.ByteTag;
import com.minecraft.mcpe.nbt.FloatTag;
import com.minecraft.mcpe.nbt.Tag;
import com.minecraft.mcpe.nbt.NbtIo;
import com.minecraft.mcpe.renderer.GameRenderer;
import com.minecraft.mcpe.util.Vector3f;
import com.minecraft.mcpe.world.World;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;

/**
 * Main Minecraft PE Java Edition class
 * Converted from C++ implementation
 */
public class MinecraftPE extends JFrame {
    private static final Logger logger = LoggerFactory.getLogger(MinecraftPE.class);
    
    private static final String VERSION = "0.1.0 (Java)";
    private static final int DEFAULT_WIDTH = 1024;
    private static final int DEFAULT_HEIGHT = 768;
    private static final int TARGET_FPS = 60;

    private GamePanel gamePanel;
    private World world;
    private Player player;
    private GameRenderer renderer;
    private InputHandler inputHandler;
    private boolean running;
    private int fps;
    private int frameCount;
    private long lastTime;
    private long lastAutoSaveMs;

    private static final File WORLDS_DIR = new File("saves/worlds");
    private File currentWorldFile;

    public MinecraftPE() {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setTitle("Minecraft PE - Java Edition v" + VERSION);
        setSize(DEFAULT_WIDTH, DEFAULT_HEIGHT);
        setLocationRelativeTo(null);
        setResizable(true);

        // Initialize components
        inputHandler = new InputHandler();
        gamePanel = new GamePanel();
        gamePanel.addKeyListener(inputHandler);
        gamePanel.addMouseListener(inputHandler);
        gamePanel.addMouseMotionListener(inputHandler);
        gamePanel.addMouseWheelListener(inputHandler);
        gamePanel.setFocusable(true);
        gamePanel.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentShown(ComponentEvent e) {
                gamePanel.requestFocusInWindow();
            }
        });
        
        add(gamePanel);

        // Window listener
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                shutdown();
            }
        });

        setVisible(true);
        gamePanel.requestFocusInWindow();

        // Initialize game
        initializeGame();
        
        // Start game loop
        running = true;
        lastAutoSaveMs = System.currentTimeMillis();
        startGameLoop();
    }

    private void initializeGame() {
        logger.info("Initializing Minecraft PE Java Edition v{}", VERSION);

        currentWorldFile = chooseWorldFile();
        if (currentWorldFile == null) {
            currentWorldFile = getUniqueWorldFile("World");
        }

        if (!loadGame()) {
            world = new World("World", System.currentTimeMillis());
            world.generateTerrain();

            player = new Player("Steve", world);
            player.setPosition(new Vector3f(0, 65, 0));
        }
        
        // Create renderer
        renderer = new GameRenderer(DEFAULT_WIDTH, DEFAULT_HEIGHT);
        
        logger.info("Game initialized successfully");
    }

    private void startGameLoop() {
        Thread gameThread = new Thread(() -> {
            lastTime = System.nanoTime();
            frameCount = 0;
            long fpsTimer = System.currentTimeMillis();

            while (running) {
                try {
                    long now = System.nanoTime();
                    double deltaTime = (now - lastTime) / 1_000_000_000.0;
                    lastTime = now;

                    // Update
                    update(deltaTime);
                    
                    // Render
                    gamePanel.repaint();

                    // Frame rate control
                    frameCount++;
                    long currentTime = System.currentTimeMillis();
                    if (currentTime - fpsTimer >= 1000) {
                        fps = frameCount;
                        frameCount = 0;
                        fpsTimer = currentTime;
                        setTitle("Minecraft PE - Java Edition v" + VERSION + " [" + fps + " FPS]");
                    }

                    // Sleep to maintain target FPS
                    long targetNanoTime = 1_000_000_000 / TARGET_FPS;
                    long elapsedNano = System.nanoTime() - now;
                    if (elapsedNano < targetNanoTime) {
                        Thread.sleep((targetNanoTime - elapsedNano) / 1_000_000);
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        });
        gameThread.setName("GameLoop");
        gameThread.start();
    }

    private void update(double deltaTime) {
        // Handle input
        handleInput(deltaTime);
        
        // Update world
        world.update(player);

        long nowMs = System.currentTimeMillis();
        if (nowMs - lastAutoSaveMs >= 30_000) {
            saveGame();
            lastAutoSaveMs = nowMs;
        }
        
        // Update player
        player.update();

        if (!player.isAlive()) {
            player.setHealth(player.getMaxHealth());
            Vector3f spawn = world.getSpawnPoint();
            player.setPosition(new Vector3f(spawn.x, spawn.y + 1, spawn.z));
            player.setVelocity(new Vector3f(0, 0, 0));
        }
        
        // Update renderer position
        renderer.setViewportSize(gamePanel.getWidth(), gamePanel.getHeight());
        renderer.setCameraPosition(player.getPosition());
        renderer.setCameraRotation(player.getRotation());
    }

    private void handleInput(double deltaTime) {
        float moveSpeed = 0.18f;
        
        // Forward/Backward
        if (inputHandler.isKeyPressed(KeyEvent.VK_W)) {
            player.moveForward(moveSpeed);
        }
        if (inputHandler.isKeyPressed(KeyEvent.VK_S)) {
            player.moveBackward(moveSpeed);
        }
        
        // Left/Right
        if (inputHandler.isKeyPressed(KeyEvent.VK_A)) {
            player.moveLeft(moveSpeed);
        }
        if (inputHandler.isKeyPressed(KeyEvent.VK_D)) {
            player.moveRight(moveSpeed);
        }
        
        // Vertical movement and jump
        if (player.isFlying()) {
            if (inputHandler.isKeyPressed(KeyEvent.VK_SPACE)) {
                player.moveUp(player.getFlySpeed());
            }
            if (inputHandler.isKeyPressed(KeyEvent.VK_SHIFT)) {
                player.moveDown(player.getFlySpeed());
            }
        } else if (inputHandler.isKeyPressed(KeyEvent.VK_SPACE) && player.isOnGround()) {
            player.jump();
        }
        
        // Flying
        if (inputHandler.isKeyReleased(KeyEvent.VK_F)) {
            player.setFlying(!player.isFlying());
        }

        // Save / Load
        if (inputHandler.isKeyReleased(KeyEvent.VK_F5)) {
            saveGame();
        }
        if (inputHandler.isKeyReleased(KeyEvent.VK_F9)) {
            loadGame();
        }
        
        // Creative mode
        if (inputHandler.isKeyReleased(KeyEvent.VK_C)) {
            player.setCreative(!player.isCreative());
            if (player.isCreative()) {
                player.setFlying(true);
            }
        }

        // Hotbar style block selection via number keys
        if (inputHandler.isKeyReleased(KeyEvent.VK_1)) player.setSelectedSlot(0);
        if (inputHandler.isKeyReleased(KeyEvent.VK_2)) player.setSelectedSlot(1);
        if (inputHandler.isKeyReleased(KeyEvent.VK_3)) player.setSelectedSlot(2);
        if (inputHandler.isKeyReleased(KeyEvent.VK_4)) player.setSelectedSlot(3);
        if (inputHandler.isKeyReleased(KeyEvent.VK_5)) player.setSelectedSlot(4);
        if (inputHandler.isKeyReleased(KeyEvent.VK_6)) player.setSelectedSlot(5);
        if (inputHandler.isKeyReleased(KeyEvent.VK_7)) player.setSelectedSlot(6);
        if (inputHandler.isKeyReleased(KeyEvent.VK_8)) player.setSelectedSlot(7);
        if (inputHandler.isKeyReleased(KeyEvent.VK_9)) player.setSelectedSlot(8);

        int wheel = inputHandler.consumeMouseWheelDelta();
        if (wheel != 0) {
            player.scrollSelectedSlot(wheel);
        }
        
        // Mouse look
        if (inputHandler.getMouseDX() != 0 || inputHandler.getMouseDY() != 0) {
            player.getRotation().x += inputHandler.getMouseDX() * 0.5f;
            player.getRotation().y -= inputHandler.getMouseDY() * 0.5f;
            
            // Clamp pitch
            if (player.getRotation().y > 90) player.getRotation().y = 90;
            if (player.getRotation().y < -90) player.getRotation().y = -90;
        }
        
        // Left click - break block
        if (inputHandler.consumeLeftClick()) {
            Mob hitMob = raycastMob();
            if (hitMob != null) {
                hitMob.damage(5.0f);
                if (!hitMob.isAlive()) {
                    hitMob.kill();
                }
            } else {
            RaycastHit hit = raycastBlock();
            if (hit != null) {
                int brokenId = world.getBlock(hit.hitX, hit.hitY, hit.hitZ);
                if (canBreakBlock(brokenId)) {
                    world.setBlock(hit.hitX, hit.hitY, hit.hitZ, Block.AIR);
                    player.addBlockToInventory(brokenId);
                }
            }
            }
        }

        // Middle click - pick block
        if (inputHandler.consumeMiddleClick()) {
            RaycastHit hit = raycastBlock();
            if (hit != null) {
                int blockId = world.getBlock(hit.hitX, hit.hitY, hit.hitZ);
                if (blockId != Block.AIR) {
                    player.pickBlockIntoSelectedSlot(blockId);
                }
            }
        }
        
        // Right click - place block
        if (inputHandler.consumeRightClick()) {
            RaycastHit hit = raycastBlock();
            if (hit != null && hit.placeX != Integer.MIN_VALUE) {
                int selectedBlockId = player.getSelectedBlockId();
                if (player.canPlaceSelectedBlock()
                        && world.getBlock(hit.placeX, hit.placeY, hit.placeZ) == Block.AIR
                        && (!Block.isSolidBlockId(selectedBlockId) || !intersectsPlayerBlock(hit.placeX, hit.placeY, hit.placeZ))) {
                    if (player.consumeSelectedBlock()) {
                        world.setBlock(hit.placeX, hit.placeY, hit.placeZ, selectedBlockId);
                    }
                }
            }
        }
        
        inputHandler.resetMouseDelta();
    }

    private RaycastHit raycastBlock() {
        // Simple raycast from player position
        Vector3f start = new Vector3f(player.getPosition());
        start.y += 1.62;
        Vector3f direction = new Vector3f(
            Math.sin(Math.toRadians(player.getRotation().x)) * Math.cos(Math.toRadians(player.getRotation().y)),
            -Math.sin(Math.toRadians(player.getRotation().y)),
            -Math.cos(Math.toRadians(player.getRotation().x)) * Math.cos(Math.toRadians(player.getRotation().y))
        );

        int lastAirX = Integer.MIN_VALUE;
        int lastAirY = Integer.MIN_VALUE;
        int lastAirZ = Integer.MIN_VALUE;

        for (float i = 0.5f; i < 6.0f; i += 0.1f) {
            Vector3f pos = new Vector3f(start);
            pos.x += direction.x * i;
            pos.y += direction.y * i;
            pos.z += direction.z * i;

            int blockX = (int) Math.floor(pos.x);
            int blockY = (int) Math.floor(pos.y);
            int blockZ = (int) Math.floor(pos.z);

            int blockId = world.getBlock(blockX, blockY, blockZ);
            if (blockId != Block.AIR) {
                return new RaycastHit(blockX, blockY, blockZ, lastAirX, lastAirY, lastAirZ);
            }

            lastAirX = blockX;
            lastAirY = blockY;
            lastAirZ = blockZ;
        }
        return null;
    }

    private Mob raycastMob() {
        Vector3f start = new Vector3f(player.getPosition());
        start.y += 1.62;
        Vector3f direction = new Vector3f(
            Math.sin(Math.toRadians(player.getRotation().x)) * Math.cos(Math.toRadians(player.getRotation().y)),
            -Math.sin(Math.toRadians(player.getRotation().y)),
            -Math.cos(Math.toRadians(player.getRotation().x)) * Math.cos(Math.toRadians(player.getRotation().y))
        );

        Mob nearest = null;
        double nearestDist = Double.MAX_VALUE;

        for (Entity entity : world.getEntities()) {
            if (!(entity instanceof Mob) || !entity.isAlive()) {
                continue;
            }

            double toX = entity.getPosition().x - start.x;
            double toY = (entity.getPosition().y + entity.getHeight() * 0.5) - start.y;
            double toZ = entity.getPosition().z - start.z;

            double t = toX * direction.x + toY * direction.y + toZ * direction.z;
            if (t < 0 || t > 3.0) {
                continue;
            }

            double closestX = start.x + direction.x * t;
            double closestY = start.y + direction.y * t;
            double closestZ = start.z + direction.z * t;

            double dx = entity.getPosition().x - closestX;
            double dy = (entity.getPosition().y + entity.getHeight() * 0.5) - closestY;
            double dz = entity.getPosition().z - closestZ;
            double distanceSq = dx * dx + dy * dy + dz * dz;

            if (distanceSq <= 0.7 * 0.7 && t < nearestDist) {
                nearest = (Mob) entity;
                nearestDist = t;
            }
        }

        return nearest;
    }

    private void saveGame() {
        try {
            File saveFile = getCurrentSaveFile();
            File parent = saveFile.getParentFile();
            if (parent != null && !parent.exists() && !parent.mkdirs()) {
                logger.warn("Could not create save directory: {}", parent.getAbsolutePath());
            }

            CompoundTag root = world.toNBT();
            CompoundTag playerTag = new CompoundTag("Player");
            playerTag.put("PosX", new FloatTag("PosX", (float) player.getPosition().x));
            playerTag.put("PosY", new FloatTag("PosY", (float) player.getPosition().y));
            playerTag.put("PosZ", new FloatTag("PosZ", (float) player.getPosition().z));
            playerTag.put("Yaw", new FloatTag("Yaw", (float) player.getRotation().x));
            playerTag.put("Pitch", new FloatTag("Pitch", (float) player.getRotation().y));
            playerTag.put("SelectedSlot", new IntTag("SelectedSlot", player.getSelectedSlot()));
            playerTag.put("HotbarBlockIds", new IntArrayTag("HotbarBlockIds", player.getHotbarBlockIds()));
            playerTag.put("HotbarCounts", new IntArrayTag("HotbarCounts", player.getHotbarCounts()));
            playerTag.put("Creative", new ByteTag("Creative", (byte) (player.isCreative() ? 1 : 0)));
            playerTag.put("Flying", new ByteTag("Flying", (byte) (player.isFlying() ? 1 : 0)));
            root.put("Player", playerTag);

            NbtIo.write(saveFile, root);
            logger.info("Saved game to {}", saveFile.getAbsolutePath());
        } catch (IOException e) {
            logger.error("Failed to save game", e);
        }
    }

    private boolean loadGame() {
        File saveFile = getCurrentSaveFile();
        if (!saveFile.exists()) {
            return false;
        }

        try {
            Tag rootTag = NbtIo.read(saveFile);
            if (!(rootTag instanceof CompoundTag)) {
                logger.warn("Save file did not contain a valid world root tag.");
                return false;
            }

            CompoundTag root = (CompoundTag) rootTag;
            world = World.fromNBT(root);
            player = new Player("Steve", world);

            Tag rawPlayerTag = root.get("Player");
            if (rawPlayerTag instanceof CompoundTag) {
                CompoundTag playerTag = (CompoundTag) rawPlayerTag;
                float posX = getFloat(playerTag, "PosX", 0f);
                float posY = getFloat(playerTag, "PosY", 65f);
                float posZ = getFloat(playerTag, "PosZ", 0f);
                float yaw = getFloat(playerTag, "Yaw", 0f);
                float pitch = getFloat(playerTag, "Pitch", 0f);

                player.setPosition(new Vector3f(posX, posY, posZ));
                player.setRotation(new Vector3f(yaw, pitch, 0));
                player.setCreative(getByte(playerTag, "Creative", (byte) 0) != 0);
                player.setFlying(getByte(playerTag, "Flying", (byte) 0) != 0);

                int[] defaultBlocks = player.getHotbarBlockIds();
                int[] defaultCounts = player.getHotbarCounts();
                int[] savedBlocks = getIntArray(playerTag, "HotbarBlockIds", defaultBlocks);
                int[] savedCounts = getIntArray(playerTag, "HotbarCounts", defaultCounts);
                int selectedSlot = getInt(playerTag, "SelectedSlot", 0);
                player.setHotbarState(savedBlocks, savedCounts, selectedSlot);
            }

            logger.info("Loaded game from {}", saveFile.getAbsolutePath());
            return true;
        } catch (Exception e) {
            logger.error("Failed to load game", e);
            return false;
        }
    }

    private File getCurrentSaveFile() {
        if (currentWorldFile == null) {
            currentWorldFile = getUniqueWorldFile("World");
        }
        return currentWorldFile;
    }

    private File chooseWorldFile() {
        ensureWorldsDir();

        File[] worlds = listWorldFiles();
        File selected = worlds.length > 0 ? worlds[0] : null;

        while (true) {
            worlds = listWorldFiles();
            String[] labels;
            if (worlds.length == 0) {
                labels = new String[] {"<No worlds yet>"};
            } else {
                labels = new String[worlds.length];
                for (int i = 0; i < worlds.length; i++) {
                    labels[i] = worldDisplayName(worlds[i]);
                }
                if (selected == null) {
                    selected = worlds[0];
                }
            }

            JList<String> worldList = new JList<>(labels);
            worldList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
            if (selected != null && worlds.length > 0) {
                int selectedIndex = 0;
                for (int i = 0; i < worlds.length; i++) {
                    if (worlds[i].equals(selected)) {
                        selectedIndex = i;
                        break;
                    }
                }
                worldList.setSelectedIndex(selectedIndex);
            }

            int action = JOptionPane.showOptionDialog(
                this,
                new JScrollPane(worldList),
                "World Selection",
                JOptionPane.DEFAULT_OPTION,
                JOptionPane.PLAIN_MESSAGE,
                null,
                new Object[] {"Play", "Create", "Delete", "Import", "Export", "Cancel"},
                "Play");

            if (action == 0) { // Play
                int idx = worldList.getSelectedIndex();
                if (idx >= 0 && worlds.length > 0) {
                    return worlds[idx];
                }
                return getUniqueWorldFile("World");
            }

            if (action == 1) { // Create
                String name = JOptionPane.showInputDialog(this, "Enter world name:", "Create World", JOptionPane.PLAIN_MESSAGE);
                if (name != null) {
                    File created = createWorldFile(name);
                    if (created != null) {
                        selected = created;
                    }
                }
                continue;
            }

            if (action == 2) { // Delete
                int idx = worldList.getSelectedIndex();
                if (idx >= 0 && worlds.length > 0) {
                    File target = worlds[idx];
                    int confirm = JOptionPane.showConfirmDialog(
                        this,
                        "Delete world '" + worldDisplayName(target) + "'?",
                        "Confirm Delete",
                        JOptionPane.YES_NO_OPTION);
                    if (confirm == JOptionPane.YES_OPTION) {
                        if (!target.delete()) {
                            JOptionPane.showMessageDialog(this, "Failed to delete world file.", "Delete Failed", JOptionPane.ERROR_MESSAGE);
                        } else if (target.equals(selected)) {
                            selected = null;
                        }
                    }
                }
                continue;
            }

            if (action == 3) { // Import
                importWorldFromFile();
                continue;
            }

            if (action == 4) { // Export
                int idx = worldList.getSelectedIndex();
                if (idx >= 0 && worlds.length > 0) {
                    exportWorldToFile(worlds[idx]);
                }
                continue;
            }

            return selected;
        }
    }

    private void ensureWorldsDir() {
        if (!WORLDS_DIR.exists() && !WORLDS_DIR.mkdirs()) {
            logger.warn("Could not create worlds directory: {}", WORLDS_DIR.getAbsolutePath());
        }
    }

    private File[] listWorldFiles() {
        ensureWorldsDir();
        File[] files = WORLDS_DIR.listFiles((dir, name) -> name.toLowerCase().endsWith(".nbt"));
        if (files == null) {
            return new File[0];
        }
        Arrays.sort(files, (a, b) -> a.getName().compareToIgnoreCase(b.getName()));
        return files;
    }

    private File createWorldFile(String nameInput) {
        String base = sanitizeWorldName(nameInput);
        if (base.isEmpty()) {
            JOptionPane.showMessageDialog(this, "World name cannot be empty.", "Invalid Name", JOptionPane.WARNING_MESSAGE);
            return null;
        }
        File file = getUniqueWorldFile(base);
        try {
            if (!file.createNewFile()) {
                JOptionPane.showMessageDialog(this, "Could not create world file.", "Create Failed", JOptionPane.ERROR_MESSAGE);
                return null;
            }
            if (!file.delete()) {
                logger.warn("Temporary world file could not be deleted after create: {}", file.getAbsolutePath());
            }
            return file;
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Failed to create world: " + e.getMessage(), "Create Failed", JOptionPane.ERROR_MESSAGE);
            return null;
        }
    }

    private File getUniqueWorldFile(String baseName) {
        ensureWorldsDir();
        String safe = sanitizeWorldName(baseName);
        if (safe.isEmpty()) {
            safe = "World";
        }
        File candidate = new File(WORLDS_DIR, safe + ".nbt");
        int index = 2;
        while (candidate.exists()) {
            candidate = new File(WORLDS_DIR, safe + "_" + index + ".nbt");
            index++;
        }
        return candidate;
    }

    private String sanitizeWorldName(String input) {
        if (input == null) {
            return "";
        }
        return input.trim().replaceAll("[^a-zA-Z0-9 _-]", "_").replace(' ', '_');
    }

    private String worldDisplayName(File file) {
        String name = file.getName();
        if (name.toLowerCase().endsWith(".nbt")) {
            return name.substring(0, name.length() - 4);
        }
        return name;
    }

    private void importWorldFromFile() {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Import World (.nbt)");
        int result = chooser.showOpenDialog(this);
        if (result != JFileChooser.APPROVE_OPTION) {
            return;
        }

        File source = chooser.getSelectedFile();
        if (source == null || !source.exists()) {
            JOptionPane.showMessageDialog(this, "Selected file does not exist.", "Import Failed", JOptionPane.ERROR_MESSAGE);
            return;
        }

        String base = source.getName();
        if (base.toLowerCase().endsWith(".nbt")) {
            base = base.substring(0, base.length() - 4);
        }
        File destination = getUniqueWorldFile(base);
        try {
            Files.copy(source.toPath(), destination.toPath(), StandardCopyOption.REPLACE_EXISTING);
            JOptionPane.showMessageDialog(this, "Imported world as '" + worldDisplayName(destination) + "'.", "Import Complete", JOptionPane.INFORMATION_MESSAGE);
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Failed to import world: " + e.getMessage(), "Import Failed", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void exportWorldToFile(File sourceWorld) {
        if (sourceWorld == null || !sourceWorld.exists()) {
            JOptionPane.showMessageDialog(this, "Selected world does not exist.", "Export Failed", JOptionPane.ERROR_MESSAGE);
            return;
        }

        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Export World");
        File desktop = Path.of(System.getProperty("user.home"), "Desktop").toFile();
        if (desktop.exists() && desktop.isDirectory()) {
            chooser.setCurrentDirectory(desktop);
        }
        chooser.setSelectedFile(new File(sourceWorld.getName()));

        int result = chooser.showSaveDialog(this);
        if (result != JFileChooser.APPROVE_OPTION) {
            return;
        }

        File destination = chooser.getSelectedFile();
        try {
            Files.copy(sourceWorld.toPath(), destination.toPath(), StandardCopyOption.REPLACE_EXISTING);
            JOptionPane.showMessageDialog(this, "Exported world to:\n" + destination.getAbsolutePath(), "Export Complete", JOptionPane.INFORMATION_MESSAGE);
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Failed to export world: " + e.getMessage(), "Export Failed", JOptionPane.ERROR_MESSAGE);
        }
    }

    private static int getInt(CompoundTag tag, String key, int fallback) {
        Tag raw = tag.get(key);
        if (raw instanceof IntTag) {
            return ((IntTag) raw).value;
        }
        return fallback;
    }

    private static float getFloat(CompoundTag tag, String key, float fallback) {
        Tag raw = tag.get(key);
        if (raw instanceof FloatTag) {
            return ((FloatTag) raw).value;
        }
        return fallback;
    }

    private static byte getByte(CompoundTag tag, String key, byte fallback) {
        Tag raw = tag.get(key);
        if (raw instanceof ByteTag) {
            return ((ByteTag) raw).value;
        }
        return fallback;
    }

    private static int[] getIntArray(CompoundTag tag, String key, int[] fallback) {
        Tag raw = tag.get(key);
        if (raw instanceof IntArrayTag) {
            int[] value = ((IntArrayTag) raw).value;
            if (value != null) {
                return value.clone();
            }
        }
        return fallback.clone();
    }

    private boolean canBreakBlock(int blockId) {
        if (blockId == Block.AIR) {
            return false;
        }
        if (blockId == Block.BEDROCK && !player.isCreative()) {
            return false;
        }
        return true;
    }

    private boolean intersectsPlayerBlock(int x, int y, int z) {
        double blockMinX = x;
        double blockMinY = y;
        double blockMinZ = z;
        double blockMaxX = x + 1.0;
        double blockMaxY = y + 1.0;
        double blockMaxZ = z + 1.0;

        double halfWidth = player.getWidth() / 2.0;
        double playerMinX = player.getPosition().x - halfWidth;
        double playerMinY = player.getPosition().y;
        double playerMinZ = player.getPosition().z - halfWidth;
        double playerMaxX = player.getPosition().x + halfWidth;
        double playerMaxY = player.getPosition().y + player.getHeight();
        double playerMaxZ = player.getPosition().z + halfWidth;

        return playerMinX < blockMaxX && playerMaxX > blockMinX
            && playerMinY < blockMaxY && playerMaxY > blockMinY
            && playerMinZ < blockMaxZ && playerMaxZ > blockMinZ;
    }

    private void shutdown() {
        logger.info("Shutting down Minecraft PE");
        saveGame();
        running = false;
        System.exit(0);
    }

    /**
     * Game panel for rendering
     */
    private class GamePanel extends JPanel {
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2d = (Graphics2D) g;
            
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            
            renderer.render(g2d, world);
            
            // Draw player position and stats
            g2d.setColor(Color.WHITE);
            g2d.setFont(new Font("Arial", Font.PLAIN, 12));
            g2d.drawString(String.format("Player: (%.1f, %.1f, %.1f)", 
                player.getPosition().x, player.getPosition().y, player.getPosition().z), 10, 60);
            g2d.drawString("Health: " + player.getHealth() + " / " + player.getMaxHealth(), 10, 80);
            g2d.drawString("Mode: " + (player.isCreative() ? "Creative" : "Survival") + 
                          " | Flying: " + player.isFlying(), 10, 100);
            g2d.drawString("Hotbar Slot: " + (player.getSelectedSlot() + 1) +
                    " | Block: " + player.getSelectedBlockId() +
                    " | Count: " + player.getSelectedBlockCount(), 10, 120);
            g2d.drawString("Mobs Loaded: " + world.getEntities().size(), 10, 140);
            g2d.drawString("W/A/S/D Move | LMB Break | RMB Place | MMB Pick | 1-9/Wheel Slots", 10, getHeight() - 10);
        }
    }

    private static class RaycastHit {
        private final int hitX;
        private final int hitY;
        private final int hitZ;
        private final int placeX;
        private final int placeY;
        private final int placeZ;

        private RaycastHit(int hitX, int hitY, int hitZ, int placeX, int placeY, int placeZ) {
            this.hitX = hitX;
            this.hitY = hitY;
            this.hitZ = hitZ;
            this.placeX = placeX;
            this.placeY = placeY;
            this.placeZ = placeZ;
        }
    }

    public static void main(String[] args) {
        if (GraphicsEnvironment.isHeadless()) {
            runHeadless();
            return;
        }

        SwingUtilities.invokeLater(MinecraftPE::new);
    }

    private static void runHeadless() {
        logger.info("Headless environment detected. Starting simulation mode.");

        World world = new World("HeadlessWorld", System.currentTimeMillis());
        world.generateTerrain();

        Player player = new Player("Steve", world);
        player.setPosition(new Vector3f(0, 65, 0));

        long start = System.currentTimeMillis();
        for (int tick = 0; tick < 600; tick++) {
            world.update(player);
            player.update();

            if (tick % 120 == 0) {
                logger.info("Headless tick {} pos=({}, {}, {}) worldTime={}",
                    tick,
                    String.format("%.2f", player.getPosition().x),
                    String.format("%.2f", player.getPosition().y),
                    String.format("%.2f", player.getPosition().z),
                    world.getWorldTime());
            }
        }

        long elapsed = System.currentTimeMillis() - start;
        logger.info("Headless simulation complete in {} ms", elapsed);
    }
}
