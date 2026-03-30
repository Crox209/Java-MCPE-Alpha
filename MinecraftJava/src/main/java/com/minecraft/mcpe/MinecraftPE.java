package com.minecraft.mcpe;

import com.minecraft.mcpe.block.Block;
import com.minecraft.mcpe.block.BlockEntity;
import com.minecraft.mcpe.block.ChestBlockEntity;
import com.minecraft.mcpe.block.CraftingTableBlockEntity;
import com.minecraft.mcpe.block.FurnaceBlockEntity;
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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

/**
 * Main Minecraft PE Java Edition class
 * Converted from C++ implementation
 */
public class MinecraftPE extends JFrame {
    private static final Logger logger = LoggerFactory.getLogger(MinecraftPE.class);
    private static final Random RANDOM = new Random();
    
    private static final String VERSION = "0.1.0 (Java)";
    private static final int DEFAULT_WIDTH = 1024;
    private static final int DEFAULT_HEIGHT = 768;
    private static final int TARGET_FPS = 60;
    private static final int INVENTORY_COLUMNS = 9;
    private static final int INVENTORY_ROWS = 4;

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
    private boolean inventoryOpen;
    private int inventoryCursor;
    private int selectedRecipeIndex;
    private int selectedSmeltIndex;
    private boolean smeltingMode;
    private boolean furnaceActive;
    private int furnaceRecipeIndex;
    private int furnaceJobsRemaining;
    private final List<Integer> furnaceQueuedRecipeIndexes;
    private int furnaceTicksRemaining;
    private int furnaceTicksTotal;
    private int furnaceFuelTicks;
    private int selectedCraftCategory;
    private int selectedSmeltCategory;
    private int lastCraftedBlockId;
    private int lastCraftedCount;
    private int lastCraftMessageTicks;
    private int miningTargetX;
    private int miningTargetY;
    private int miningTargetZ;
    private int miningHits;
    private int miningRequiredHits;
    private long lastMiningStepMs;

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
        inventoryOpen = false;
        inventoryCursor = 0;
        selectedRecipeIndex = 0;
        selectedSmeltIndex = 0;
        smeltingMode = false;
        furnaceActive = false;
        furnaceRecipeIndex = 0;
        furnaceJobsRemaining = 0;
        furnaceQueuedRecipeIndexes = new ArrayList<>();
        furnaceTicksRemaining = 0;
        furnaceTicksTotal = 0;
        furnaceFuelTicks = 0;
        selectedCraftCategory = 0;
        selectedSmeltCategory = 0;
        lastCraftedBlockId = 0;
        lastCraftedCount = 0;
        lastCraftMessageTicks = 0;
        miningTargetX = Integer.MIN_VALUE;
        miningTargetY = Integer.MIN_VALUE;
        miningTargetZ = Integer.MIN_VALUE;
        miningHits = 0;
        miningRequiredHits = 1;
        lastMiningStepMs = 0L;
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

        tickFurnace();

        if (lastCraftMessageTicks > 0) {
            lastCraftMessageTicks--;
        }

        // Holding left mouse mines blocks over time (non-instant block breaking).
        if (!inventoryOpen && inputHandler.isLeftMousePressed()) {
            RaycastHit heldHit = raycastBlock();
            tryMineBlockStep(heldHit);
        } else {
            resetMiningProgress();
        }
        
        // Update renderer position
        renderer.setViewportSize(gamePanel.getWidth(), gamePanel.getHeight());
        renderer.setCameraPosition(player.getPosition());
        renderer.setCameraRotation(player.getRotation());
    }

    private void tickFurnace() {
        if (!furnaceActive) {
            return;
        }

        furnaceTicksRemaining--;
        if (furnaceTicksRemaining > 0) {
            return;
        }

        List<SmeltRecipe> recipes = getSmeltRecipes();
        if (furnaceRecipeIndex >= 0 && furnaceRecipeIndex < recipes.size()) {
            SmeltRecipe recipe = recipes.get(furnaceRecipeIndex);
            player.addBlockToInventory(recipe.resultBlockId, recipe.resultCount);
            lastCraftedBlockId = recipe.resultBlockId;
            lastCraftedCount = recipe.resultCount;
            lastCraftMessageTicks = 90;
        }

        furnaceJobsRemaining--;
        if (!furnaceQueuedRecipeIndexes.isEmpty()) {
            furnaceRecipeIndex = furnaceQueuedRecipeIndexes.remove(0);
            furnaceTicksRemaining = furnaceTicksTotal;
            furnaceJobsRemaining = 1 + furnaceQueuedRecipeIndexes.size();
            return;
        }

        furnaceActive = false;
        furnaceJobsRemaining = 0;
        furnaceTicksRemaining = 0;
        furnaceTicksTotal = 0;
    }

    private void handleInput(double deltaTime) {
        if (inputHandler.isKeyReleased(KeyEvent.VK_E)) {
            inventoryOpen = !inventoryOpen;
        }

        if (inputHandler.isKeyReleased(KeyEvent.VK_BACK_SPACE)) {
            inventoryOpen = false;
        }

        if (inventoryOpen) {
            handleInventoryInput();
            inputHandler.resetMouseDelta();
            return;
        }

        float moveSpeed = 0.18f;
        boolean sneaking = !player.isFlying() && inputHandler.isKeyPressed(KeyEvent.VK_SHIFT);
        player.setSneaking(sneaking);
        if (sneaking) {
            moveSpeed *= 0.35f;
        }
        
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

        if (inputHandler.isKeyReleased(KeyEvent.VK_Q)) {
            int droppedId = player.dropOneSelectedBlock();
            if (droppedId > 0) {
                double yaw = Math.toRadians(player.getRotation().x);
                double forwardX = Math.sin(yaw);
                double forwardZ = -Math.cos(yaw);
                world.spawnItemDrop(
                    droppedId,
                    1,
                    player.getPosition().x + forwardX * 0.8,
                    player.getPosition().y + 1.2,
                    player.getPosition().z + forwardZ * 0.8);
            }
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
                resetMiningProgress();
                hitMob.damage(5.0f);
                double kx = hitMob.getPosition().x - player.getPosition().x;
                double kz = hitMob.getPosition().z - player.getPosition().z;
                double kLen = Math.sqrt(kx * kx + kz * kz);
                if (kLen > 0.001) {
                    hitMob.addVelocity((kx / kLen) * 0.32, 0.18, (kz / kLen) * 0.32);
                }
                if (!hitMob.isAlive()) {
                    hitMob.kill();
                }
            } else {
            RaycastHit hit = raycastBlock();
            tryMineBlockStep(hit);
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
            if (hit != null) {
                int targetedBlockId = world.getBlock(hit.hitX, hit.hitY, hit.hitZ);
                if (handleRightClickInteraction(hit, targetedBlockId)) {
                    inputHandler.resetMouseDelta();
                    return;
                }
            }

            if (hit != null && hit.placeX != Integer.MIN_VALUE) {
                int selectedBlockId = player.getSelectedBlockId();
                if (player.canPlaceSelectedBlock()
                        && world.getBlock(hit.placeX, hit.placeY, hit.placeZ) == Block.AIR
                        && (!Block.isSolidBlockId(selectedBlockId) || !intersectsPlayerBlock(hit.placeX, hit.placeY, hit.placeZ))) {
                    if (player.consumeSelectedBlock()) {
                        placeBlockAtHit(hit, selectedBlockId);
                    }
                }
            }
        }
        
        inputHandler.resetMouseDelta();
    }

    private void handleInventoryInput() {
        if (inputHandler.isKeyReleased(KeyEvent.VK_LEFT)) {
            inventoryCursor = (inventoryCursor + Player.HOTBAR_SIZE + Player.INVENTORY_SIZE - 1) % (Player.HOTBAR_SIZE + Player.INVENTORY_SIZE);
        }
        if (inputHandler.isKeyReleased(KeyEvent.VK_RIGHT)) {
            inventoryCursor = (inventoryCursor + 1) % (Player.HOTBAR_SIZE + Player.INVENTORY_SIZE);
        }
        if (inputHandler.isKeyReleased(KeyEvent.VK_UP)) {
            inventoryCursor = (inventoryCursor + Player.HOTBAR_SIZE + Player.INVENTORY_SIZE - INVENTORY_COLUMNS) % (Player.HOTBAR_SIZE + Player.INVENTORY_SIZE);
        }
        if (inputHandler.isKeyReleased(KeyEvent.VK_DOWN)) {
            inventoryCursor = (inventoryCursor + INVENTORY_COLUMNS) % (Player.HOTBAR_SIZE + Player.INVENTORY_SIZE);
        }

        if (inputHandler.isKeyReleased(KeyEvent.VK_1)) player.setSelectedSlot(0);
        if (inputHandler.isKeyReleased(KeyEvent.VK_2)) player.setSelectedSlot(1);
        if (inputHandler.isKeyReleased(KeyEvent.VK_3)) player.setSelectedSlot(2);
        if (inputHandler.isKeyReleased(KeyEvent.VK_4)) player.setSelectedSlot(3);
        if (inputHandler.isKeyReleased(KeyEvent.VK_5)) player.setSelectedSlot(4);
        if (inputHandler.isKeyReleased(KeyEvent.VK_6)) player.setSelectedSlot(5);
        if (inputHandler.isKeyReleased(KeyEvent.VK_7)) player.setSelectedSlot(6);
        if (inputHandler.isKeyReleased(KeyEvent.VK_8)) player.setSelectedSlot(7);
        if (inputHandler.isKeyReleased(KeyEvent.VK_9)) player.setSelectedSlot(8);

        if (inputHandler.isKeyReleased(KeyEvent.VK_ENTER)) {
            if (inventoryCursor < Player.HOTBAR_SIZE) {
                player.setSelectedSlot(inventoryCursor);
            } else {
                player.swapBackpackWithHotbar(inventoryCursor - Player.HOTBAR_SIZE, player.getSelectedSlot());
            }
        }

        if (inputHandler.isKeyReleased(KeyEvent.VK_PAGE_UP)) {
            if (smeltingMode) {
                List<SmeltRecipe> smelts = getVisibleSmeltRecipes();
                if (!smelts.isEmpty()) {
                    selectedSmeltIndex = (selectedSmeltIndex + smelts.size() - 1) % smelts.size();
                }
            } else {
                List<CraftRecipe> crafts = getVisibleCraftRecipes();
                if (!crafts.isEmpty()) {
                    selectedRecipeIndex = (selectedRecipeIndex + crafts.size() - 1) % crafts.size();
                }
            }
        }
        if (inputHandler.isKeyReleased(KeyEvent.VK_PAGE_DOWN)) {
            if (smeltingMode) {
                List<SmeltRecipe> smelts = getVisibleSmeltRecipes();
                if (!smelts.isEmpty()) {
                    selectedSmeltIndex = (selectedSmeltIndex + 1) % smelts.size();
                }
            } else {
                List<CraftRecipe> crafts = getVisibleCraftRecipes();
                if (!crafts.isEmpty()) {
                    selectedRecipeIndex = (selectedRecipeIndex + 1) % crafts.size();
                }
            }
        }

        if (inputHandler.isKeyReleased(KeyEvent.VK_TAB)) {
            smeltingMode = !smeltingMode;
        }

        if (inputHandler.isKeyReleased(KeyEvent.VK_OPEN_BRACKET)) {
            if (smeltingMode) {
                selectedSmeltCategory = (selectedSmeltCategory + getSmeltCategoryCount() - 1) % getSmeltCategoryCount();
                selectedSmeltIndex = 0;
            } else {
                selectedCraftCategory = (selectedCraftCategory + getCraftCategoryCount() - 1) % getCraftCategoryCount();
                selectedRecipeIndex = 0;
            }
        }
        if (inputHandler.isKeyReleased(KeyEvent.VK_CLOSE_BRACKET)) {
            if (smeltingMode) {
                selectedSmeltCategory = (selectedSmeltCategory + 1) % getSmeltCategoryCount();
                selectedSmeltIndex = 0;
            } else {
                selectedCraftCategory = (selectedCraftCategory + 1) % getCraftCategoryCount();
                selectedRecipeIndex = 0;
            }
        }

        if (inputHandler.isKeyReleased(KeyEvent.VK_X)) {
            boolean moveAllMatching = inputHandler.isKeyPressed(KeyEvent.VK_SHIFT);
            if (moveAllMatching) {
                player.quickTransferAllMatchingCombinedSlot(inventoryCursor);
            } else {
                player.quickTransferCombinedSlot(inventoryCursor);
            }
        }

        if (inputHandler.isKeyReleased(KeyEvent.VK_Z)) {
            player.splitCombinedSlot(inventoryCursor);
        }

        if (inputHandler.isKeyReleased(KeyEvent.VK_Q)) {
            boolean bulk = inputHandler.isKeyPressed(KeyEvent.VK_SHIFT);
            if (smeltingMode) {
                smeltSelectedRecipe(bulk);
            } else {
                craftSelectedRecipe(bulk);
            }
        }
    }

    private List<CraftRecipe> getCraftRecipes() {
        List<CraftRecipe> recipes = new ArrayList<>();
        // Basic Recipes (category 1)
        recipes.add(new CraftRecipe("1 Wood -> 4 Planks", new int[] {Block.WOOD}, new int[] {1}, Block.WOOD, 4, 1));
        recipes.add(new CraftRecipe("2 Planks -> 4 Sticks", new int[] {Block.WOOD}, new int[] {2}, Block.TORCH, 4, 1));
        recipes.add(new CraftRecipe("3 Planks -> Crafting Table", new int[] {Block.WOOD}, new int[] {3}, Block.CRAFTING_TABLE, 1, 1));
        recipes.add(new CraftRecipe("2 Sticks -> Ladder", new int[] {Block.TORCH}, new int[] {2}, Block.LADDER, 1, 1));
        recipes.add(new CraftRecipe("1 Coal -> Torch x4", new int[] {Block.COAL_ORE}, new int[] {1}, Block.TORCH, 4, 1));
        recipes.add(new CraftRecipe("4 Planks -> Door", new int[] {Block.WOOD}, new int[] {4}, Block.WOODEN_DOOR, 1, 1));
        
        // Blocks Recipes (category 2)
        recipes.add(new CraftRecipe("3 Cobble -> Furnace", new int[] {Block.COBBLESTONE}, new int[] {3}, Block.FURNACE, 1, 2));
        recipes.add(new CraftRecipe("2 Sand -> Glass", new int[] {Block.SAND}, new int[] {2}, Block.GLASS, 1, 2));
        recipes.add(new CraftRecipe("3 Dirt -> Grass", new int[] {Block.DIRT}, new int[] {3}, Block.GRASS, 1, 2));
        recipes.add(new CraftRecipe("8 Planks -> Chest", new int[] {Block.WOOD}, new int[] {8}, Block.CHEST, 1, 2));
        recipes.add(new CraftRecipe("4 Planks -> Fence", new int[] {Block.WOOD}, new int[] {4}, Block.FENCE, 1, 2));
        recipes.add(new CraftRecipe("6 Planks -> Stairs", new int[] {Block.WOOD}, new int[] {6}, Block.WOODEN_STAIRS, 3, 2));
        recipes.add(new CraftRecipe("9 Ingots -> Iron Block", new int[] {Block.IRON_ORE}, new int[] {9}, Block.IRON_BLOCK, 1, 2));
        recipes.add(new CraftRecipe("9 Gold -> Gold Block", new int[] {Block.GOLD_ORE}, new int[] {9}, Block.GOLD_BLOCK, 1, 2));
        recipes.add(new CraftRecipe("9 Diamond -> Diamond Block", new int[] {Block.DIAMOND_ORE}, new int[] {9}, Block.DIAMOND_BLOCK, 1, 2));
        recipes.add(new CraftRecipe("4 Wool -> Bed", new int[] {Block.WOOL}, new int[] {4}, Block.BED, 1, 2));
        
        return recipes;
    }

    private int getCraftCategoryCount() {
        return 3;
    }

    private String getCraftCategoryName(int category) {
        if (category == 1) return "BASIC";
        if (category == 2) return "BLOCKS";
        return "ALL";
    }

    private List<CraftRecipe> getVisibleCraftRecipes() {
        List<CraftRecipe> all = getCraftRecipes();
        if (selectedCraftCategory == 0) {
            return all;
        }
        List<CraftRecipe> filtered = new ArrayList<>();
        for (CraftRecipe recipe : all) {
            if (recipe.category == selectedCraftCategory) {
                filtered.add(recipe);
            }
        }
        return filtered;
    }

    private void craftSelectedRecipe(boolean bulk) {
        List<CraftRecipe> recipes = getVisibleCraftRecipes();
        if (recipes.isEmpty()) {
            return;
        }

        CraftRecipe recipe = recipes.get(selectedRecipeIndex);
        if (!canCraftRecipe(recipe)) {
            lastCraftedBlockId = 0;
            lastCraftedCount = 0;
            lastCraftMessageTicks = 60;
            return;
        }

        int craftsToRun = 1;
        if (bulk) {
            craftsToRun = getMaxCraftCount(recipe);
        }
        int maxByOutput = player.getAvailableSpaceForBlock(recipe.resultBlockId) / recipe.resultCount;
        craftsToRun = Math.min(craftsToRun, maxByOutput);
        if (craftsToRun <= 0) {
            lastCraftedBlockId = 0;
            lastCraftedCount = 0;
            lastCraftMessageTicks = 60;
            return;
        }

        for (int c = 0; c < craftsToRun; c++) {
            for (int i = 0; i < recipe.ingredientBlockIds.length; i++) {
                player.removeBlockFromInventory(recipe.ingredientBlockIds[i], recipe.ingredientCounts[i]);
            }
        }

        player.addBlockToInventory(recipe.resultBlockId, recipe.resultCount * craftsToRun);
        lastCraftedBlockId = recipe.resultBlockId;
        lastCraftedCount = recipe.resultCount * craftsToRun;
        lastCraftMessageTicks = 90;
    }

    private int getMaxCraftCount(CraftRecipe recipe) {
        int max = Integer.MAX_VALUE;
        for (int i = 0; i < recipe.ingredientBlockIds.length; i++) {
            int have = player.getTotalBlockCount(recipe.ingredientBlockIds[i]);
            int canMake = have / recipe.ingredientCounts[i];
            max = Math.min(max, canMake);
        }
        return Math.max(1, max);
    }

    private boolean canCraftRecipe(CraftRecipe recipe) {
        for (int i = 0; i < recipe.ingredientBlockIds.length; i++) {
            if (player.getTotalBlockCount(recipe.ingredientBlockIds[i]) < recipe.ingredientCounts[i]) {
                return false;
            }
        }
        return true;
    }

    private List<SmeltRecipe> getSmeltRecipes() {
        List<SmeltRecipe> recipes = new ArrayList<>();
        // Blocks (category 1)
        recipes.add(new SmeltRecipe("Cobble -> Stone", Block.COBBLESTONE, 1, Block.STONE, 1, 1));
        recipes.add(new SmeltRecipe("Sand -> Glass", Block.SAND, 1, Block.GLASS, 1, 1));
        recipes.add(new SmeltRecipe("Clay -> Brick", Block.CLAY, 1, Block.BRICK, 1, 1));
        recipes.add(new SmeltRecipe("Log -> Charcoal", Block.LOG, 1, Block.COAL_ORE, 1, 1));
        recipes.add(new SmeltRecipe("Cactus -> Green Dye", Block.CACTUS, 1, Block.SAPLING, 1, 1));
        
        // Ores (category 2)
        recipes.add(new SmeltRecipe("Iron Ore -> Iron Ingot", Block.IRON_ORE, 1, Block.IRON_BLOCK, 1, 2));
        recipes.add(new SmeltRecipe("Gold Ore -> Gold Ingot", Block.GOLD_ORE, 1, Block.GOLD_BLOCK, 1, 2));
        recipes.add(new SmeltRecipe("Diamond Ore -> Diamond", Block.DIAMOND_ORE, 1, Block.DIAMOND_BLOCK, 1, 2));
        recipes.add(new SmeltRecipe("Lapis Ore -> Lapis", Block.LAPIS_ORE, 1, Block.LAPIS_BLOCK, 1, 2));
        recipes.add(new SmeltRecipe("Redstone Ore -> Redstone", Block.REDSTONE_ORE, 1, Block.REDSTONE_WIRE, 1, 2));
        recipes.add(new SmeltRecipe("Netherrack -> Stone", Block.NETHERRACK, 1, Block.STONE, 1, 2));
        
        return recipes;
    }

    private int getSmeltCategoryCount() {
        return 3;
    }

    private String getSmeltCategoryName(int category) {
        if (category == 1) return "BLOCKS";
        if (category == 2) return "ORES";
        return "ALL";
    }

    private List<SmeltRecipe> getVisibleSmeltRecipes() {
        List<SmeltRecipe> all = getSmeltRecipes();
        if (selectedSmeltCategory == 0) {
            return all;
        }
        List<SmeltRecipe> filtered = new ArrayList<>();
        for (SmeltRecipe recipe : all) {
            if (recipe.category == selectedSmeltCategory) {
                filtered.add(recipe);
            }
        }
        return filtered;
    }

    private void smeltSelectedRecipe(boolean bulk) {
        List<SmeltRecipe> recipes = getVisibleSmeltRecipes();
        if (recipes.isEmpty()) {
            return;
        }

        SmeltRecipe recipe = recipes.get(selectedSmeltIndex);
        if (!canSmeltRecipe(recipe)) {
            lastCraftedBlockId = 0;
            lastCraftedCount = 0;
            lastCraftMessageTicks = 60;
            return;
        }

        int jobsToQueue = 1;
        if (bulk) {
            jobsToQueue = getMaxSmeltJobs(recipe);
        }
        int maxByOutput = player.getAvailableSpaceForBlock(recipe.resultBlockId) / recipe.resultCount;
        jobsToQueue = Math.min(jobsToQueue, maxByOutput);

        int queued = 0;
        for (int i = 0; i < jobsToQueue; i++) {
            if (player.getTotalBlockCount(recipe.inputBlockId) < recipe.inputCount || !reserveFuelForJob()) {
                break;
            }
            player.removeBlockFromInventory(recipe.inputBlockId, recipe.inputCount);
            if (!furnaceActive) {
                furnaceActive = true;
                furnaceRecipeIndex = selectedSmeltIndex;
                furnaceTicksTotal = 120;
                furnaceTicksRemaining = furnaceTicksTotal;
            } else {
                furnaceQueuedRecipeIndexes.add(selectedSmeltIndex);
            }
            queued++;
        }

        furnaceJobsRemaining = (furnaceActive ? 1 : 0) + furnaceQueuedRecipeIndexes.size();
        if (queued == 0) {
            lastCraftedBlockId = 0;
            lastCraftedCount = 0;
            lastCraftMessageTicks = 60;
            return;
        }
        lastCraftedBlockId = 0;
        lastCraftedCount = 0;
        lastCraftMessageTicks = 90;
    }

    private int getMaxSmeltJobs(SmeltRecipe recipe) {
        int byInput = player.getTotalBlockCount(recipe.inputBlockId) / recipe.inputCount;
        int possibleFuelTicks = furnaceFuelTicks
                + player.getTotalBlockCount(Block.COAL_ORE) * 480
                + player.getTotalBlockCount(Block.LOG) * 240
                + player.getTotalBlockCount(Block.WOOD) * 120
                + player.getTotalBlockCount(Block.SAPLING) * 80;
        int byFuel = possibleFuelTicks / 120;
        int max = Math.min(byInput, byFuel);
        return Math.max(1, max);
    }

    private boolean canSmeltRecipe(SmeltRecipe recipe) {
        return player.getTotalBlockCount(recipe.inputBlockId) >= recipe.inputCount && canReserveFuelForJob();
    }

    private boolean hasAnyFuel() {
        return player.getTotalBlockCount(Block.COAL_ORE) > 0
                || player.getTotalBlockCount(Block.LOG) > 0
                || player.getTotalBlockCount(Block.WOOD) > 0
                || player.getTotalBlockCount(Block.SAPLING) > 0;
    }

    private boolean canReserveFuelForJob() {
        int possible = furnaceFuelTicks;
        possible += player.getTotalBlockCount(Block.COAL_ORE) * 480;
        possible += player.getTotalBlockCount(Block.LOG) * 240;
        possible += player.getTotalBlockCount(Block.WOOD) * 120;
        possible += player.getTotalBlockCount(Block.SAPLING) * 80;
        return possible >= 120;
    }

    private boolean reserveFuelForJob() {
        int needed = 120;

        while (furnaceFuelTicks < needed) {
            if (!consumeOneFuelItem()) {
                return false;
            }
        }

        furnaceFuelTicks -= needed;
        return true;
    }

    private void consumeOneFuel() {
        reserveFuelForJob();
    }

    private boolean consumeOneFuelItem() {
        if (player.removeBlockFromInventory(Block.COAL_ORE, 1)) {
            furnaceFuelTicks += 480;
            return true;
        }
        if (player.removeBlockFromInventory(Block.LOG, 1)) {
            furnaceFuelTicks += 240;
            return true;
        }
        if (player.removeBlockFromInventory(Block.WOOD, 1)) {
            furnaceFuelTicks += 120;
            return true;
        }
        if (player.removeBlockFromInventory(Block.SAPLING, 1)) {
            furnaceFuelTicks += 80;
            return true;
        }
        return false;
    }

    private int getBreakDropBlockId(int blockId) {
        if (blockId == Block.STONE) {
            return Block.COBBLESTONE;
        }
        if (blockId == Block.GRASS) {
            return Block.DIRT;
        }
        if (blockId == Block.LEAVES) {
            return RANDOM.nextDouble() < 0.12 ? Block.SAPLING : Block.AIR;
        }
        if (blockId == Block.TALL_GRASS || blockId == Block.DEAD_SHRUB || blockId == Block.SNOW || blockId == Block.FIRE) {
            return Block.AIR;
        }
        return blockId;
    }

    private void placeBlockAtHit(RaycastHit hit, int blockId) {
        if (blockId == Block.WOODEN_DOOR || blockId == Block.IRON_DOOR) {
            if (hit.placeY + 1 >= 128) {
                return;
            }
            if (world.getBlock(hit.placeX, hit.placeY + 1, hit.placeZ) != Block.AIR) {
                return;
            }
            world.setBlock(hit.placeX, hit.placeY, hit.placeZ, blockId);
            world.setBlock(hit.placeX, hit.placeY + 1, hit.placeZ, blockId);
            return;
        }
        if (blockId == Block.FURNACE) {
            world.createFurnace(hit.placeX, hit.placeY, hit.placeZ);
            return;
        }
        if (blockId == Block.CHEST) {
            world.createChest(hit.placeX, hit.placeY, hit.placeZ);
            return;
        }
        if (blockId == Block.CRAFTING_TABLE) {
            world.createCraftingTable(hit.placeX, hit.placeY, hit.placeZ);
            return;
        }
        world.setBlock(hit.placeX, hit.placeY, hit.placeZ, blockId);
    }

    private boolean handleRightClickInteraction(RaycastHit hit, int targetedBlockId) {
        if (targetedBlockId == Block.WOODEN_DOOR) {
            if (world.toggleDoor(hit.hitX, hit.hitY, hit.hitZ)) {
                lastCraftedBlockId = targetedBlockId;
                lastCraftedCount = 1;
                lastCraftMessageTicks = 40;
                return true;
            }
        }

        if (targetedBlockId == Block.IRON_DOOR) {
            // Iron doors require redstone in classic behavior.
            return true;
        }

        if (targetedBlockId == Block.BED) {
            if (player.isSneaking()) {
                return false;
            }
            if (world.isNightTime()) {
                int currentDay = world.getWorldTime() / 24000;
                world.setWorldTime((currentDay + 1) * 24000 + 1000);
                player.heal(4.0f);
                lastCraftedBlockId = Block.BED;
                lastCraftedCount = 1;
                lastCraftMessageTicks = 90;
            } else {
                lastCraftedBlockId = Block.BED;
                lastCraftedCount = 0;
                lastCraftMessageTicks = 60;
            }
            return true;
        }

        if (targetedBlockId == Block.FURNACE) {
            if (player.isSneaking()) {
                return false;
            }
            inventoryOpen = true;
            smeltingMode = true;
            selectedSmeltIndex = Math.max(0, selectedSmeltIndex);
            return true;
        }
        if (targetedBlockId == Block.CRAFTING_TABLE) {
            if (player.isSneaking()) {
                return false;
            }
            inventoryOpen = true;
            smeltingMode = false;
            selectedRecipeIndex = Math.max(0, selectedRecipeIndex);
            return true;
        }
        if (targetedBlockId == Block.CHEST) {
            if (world.isSolidBlock(hit.hitX, hit.hitY + 1, hit.hitZ)) {
                return true;
            }

            BlockEntity entity = world.getBlockEntity(hit.hitX, hit.hitY, hit.hitZ);
            if (!(entity instanceof ChestBlockEntity)) {
                return true;
            }

            ChestBlockEntity chest = (ChestBlockEntity) entity;
            int selectedBlockId = player.getSelectedBlockId();
            int selectedCount = player.getSelectedBlockCount();

            if (player.isSneaking() && selectedBlockId > 0 && selectedCount > 0) {
                int moved = player.getTotalBlockCount(selectedBlockId);
                if (moved > 0 && player.removeBlockFromInventory(selectedBlockId, moved)) {
                    chest.addItem(selectedBlockId, moved);
                    lastCraftedBlockId = selectedBlockId;
                    lastCraftedCount = moved;
                    lastCraftMessageTicks = 90;
                }
                return true;
            }

            if (selectedBlockId > 0 && selectedCount > 0) {
                int moved = player.removeSelectedStackForTransfer();
                if (moved > 0) {
                    chest.addItem(selectedBlockId, moved);
                    lastCraftedBlockId = selectedBlockId;
                    lastCraftedCount = moved;
                    lastCraftMessageTicks = 90;
                }
            } else {
                for (int slot = 0; slot < 27; slot++) {
                    int chestBlockId = chest.getSlotBlockId(slot);
                    int chestCount = chest.getSlotCount(slot);
                    if (chestBlockId <= 0 || chestCount <= 0) {
                        continue;
                    }
                    int toMove = Math.min(chestCount, player.getAvailableSpaceForBlock(chestBlockId));
                    if (toMove <= 0) {
                        continue;
                    }
                    if (chest.removeItem(chestBlockId, toMove)) {
                        player.addBlockToInventory(chestBlockId, toMove);
                        lastCraftedBlockId = chestBlockId;
                        lastCraftedCount = toMove;
                        lastCraftMessageTicks = 90;
                        break;
                    }
                }
            }
            return true;
        }

        return false;
    }

    private void dropBlockEntityContents(int x, int y, int z) {
        BlockEntity entity = world.getBlockEntity(x, y, z);
        if (entity instanceof ChestBlockEntity) {
            ChestBlockEntity chest = (ChestBlockEntity) entity;
            for (int i = 0; i < 27; i++) {
                int blockId = chest.getSlotBlockId(i);
                int count = chest.getSlotCount(i);
                if (blockId > 0 && count > 0) {
                    world.spawnItemDrop(blockId, count, x + 0.5, y + 0.5, z + 0.5);
                }
            }
        } else if (entity instanceof FurnaceBlockEntity) {
            FurnaceBlockEntity furnace = (FurnaceBlockEntity) entity;
            for (int i = 0; i < furnace.getInputBlockIds().length; i++) {
                int inId = furnace.getInputBlockIds()[i];
                int inCount = furnace.getInputCounts()[i];
                if (inId > 0 && inCount > 0) {
                    world.spawnItemDrop(inId, inCount, x + 0.5, y + 0.5, z + 0.5);
                }
                int fuelId = furnace.getFuelBlockIds()[i];
                int fuelCount = furnace.getFuelCounts()[i];
                if (fuelId > 0 && fuelCount > 0) {
                    world.spawnItemDrop(fuelId, fuelCount, x + 0.5, y + 0.5, z + 0.5);
                }
                int outId = furnace.getOutputBlockIds()[i];
                int outCount = furnace.getOutputCounts()[i];
                if (outId > 0 && outCount > 0) {
                    world.spawnItemDrop(outId, outCount, x + 0.5, y + 0.5, z + 0.5);
                }
            }
        } else if (entity instanceof CraftingTableBlockEntity) {
            CraftingTableBlockEntity table = (CraftingTableBlockEntity) entity;
            for (int i = 0; i < table.getGridBlockIds().length; i++) {
                int blockId = table.getGridBlockIds()[i];
                int count = table.getGridCounts()[i];
                if (blockId > 0 && count > 0) {
                    world.spawnItemDrop(blockId, count, x + 0.5, y + 0.5, z + 0.5);
                }
            }
            if (table.getOutputBlockId() > 0 && table.getOutputCount() > 0) {
                world.spawnItemDrop(table.getOutputBlockId(), table.getOutputCount(), x + 0.5, y + 0.5, z + 0.5);
            }
        }
    }

    private int getBreakDropCount(int blockId) {
        if (blockId == Block.LAPIS_ORE) {
            return 4 + RANDOM.nextInt(5);
        }
        return 1;
    }

    private int getBreakHitsRequired(int blockId) {
        if (player.isCreative()) {
            return 1;
        }
        int required;
        if (blockId == Block.OBSIDIAN || blockId == Block.IRON_BLOCK || blockId == Block.DIAMOND_BLOCK) {
            required = 7;
        } else if (blockId == Block.STONE || blockId == Block.COBBLESTONE || blockId == Block.BRICK
                || blockId == Block.FURNACE || blockId == Block.CHEST || blockId == Block.CRAFTING_TABLE) {
            required = 4;
        } else if (blockId == Block.LOG || blockId == Block.WOOD || blockId == Block.WOODEN_DOOR || blockId == Block.BED) {
            required = 3;
        } else {
            required = 2;
        }

        // Tool tier emulation: selected hotbar blocks act as pseudo tools for mining speed.
        int heldId = player.getSelectedBlockId();
        if (heldId == Block.DIAMOND_BLOCK) {
            required -= 2;
        } else if (heldId == Block.IRON_BLOCK || heldId == Block.GOLD_BLOCK) {
            required -= 1;
        } else if (heldId == Block.WOOD || heldId == Block.LOG) {
            required += 1;
        }

        if (blockId == Block.OBSIDIAN && heldId != Block.DIAMOND_BLOCK) {
            return 7;
        }
        return Math.max(1, required);
    }

    private void resetMiningProgress() {
        miningTargetX = Integer.MIN_VALUE;
        miningTargetY = Integer.MIN_VALUE;
        miningTargetZ = Integer.MIN_VALUE;
        miningHits = 0;
        miningRequiredHits = 1;
    }

    private void tryMineBlockStep(RaycastHit hit) {
        long nowMs = System.currentTimeMillis();
        if (nowMs - lastMiningStepMs < 120) {
            return;
        }
        lastMiningStepMs = nowMs;

        if (hit == null) {
            resetMiningProgress();
            return;
        }

        int blockId = world.getBlock(hit.hitX, hit.hitY, hit.hitZ);
        if (!canBreakBlock(blockId)) {
            resetMiningProgress();
            return;
        }

        boolean sameTarget = miningTargetX == hit.hitX && miningTargetY == hit.hitY && miningTargetZ == hit.hitZ;
        if (!sameTarget) {
            miningTargetX = hit.hitX;
            miningTargetY = hit.hitY;
            miningTargetZ = hit.hitZ;
            miningHits = 0;
            miningRequiredHits = getBreakHitsRequired(blockId);
        }

        miningHits++;
        if (miningHits < miningRequiredHits) {
            return;
        }

        if (blockId == Block.WOODEN_DOOR || blockId == Block.IRON_DOOR) {
            breakDoorPair(hit.hitX, hit.hitY, hit.hitZ, blockId);
            resetMiningProgress();
            return;
        }

        dropBlockEntityContents(hit.hitX, hit.hitY, hit.hitZ);
        world.setBlock(hit.hitX, hit.hitY, hit.hitZ, Block.AIR);
        int dropId = getBreakDropBlockId(blockId);
        int dropCount = getBreakDropCount(blockId);
        if (dropId != Block.AIR && dropCount > 0) {
            world.spawnItemDrop(dropId, dropCount, hit.hitX + 0.5, hit.hitY + 0.5, hit.hitZ + 0.5);
        }
        resetMiningProgress();
    }

    private void breakDoorPair(int x, int y, int z, int blockId) {
        int baseY = y;
        if (world.getBlock(x, y - 1, z) == blockId) {
            baseY = y - 1;
        }

        if (world.getBlock(x, baseY, z) == blockId) {
            world.setBlock(x, baseY, z, Block.AIR);
        }
        if (world.getBlock(x, baseY + 1, z) == blockId) {
            world.setBlock(x, baseY + 1, z, Block.AIR);
        }

        int dropId = getBreakDropBlockId(blockId);
        if (dropId != Block.AIR) {
            world.spawnItemDrop(dropId, 1, x + 0.5, baseY + 0.5, z + 0.5);
        }
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
            playerTag.put("InventoryBlockIds", new IntArrayTag("InventoryBlockIds", player.getInventoryBlockIds()));
            playerTag.put("InventoryCounts", new IntArrayTag("InventoryCounts", player.getInventoryCounts()));
            playerTag.put("Creative", new ByteTag("Creative", (byte) (player.isCreative() ? 1 : 0)));
            playerTag.put("Flying", new ByteTag("Flying", (byte) (player.isFlying() ? 1 : 0)));
            playerTag.put("FurnaceActive", new ByteTag("FurnaceActive", (byte) (furnaceActive ? 1 : 0)));
            playerTag.put("FurnaceRecipeIndex", new IntTag("FurnaceRecipeIndex", furnaceRecipeIndex));
            playerTag.put("FurnaceJobsRemaining", new IntTag("FurnaceJobsRemaining", furnaceJobsRemaining));
            playerTag.put("FurnaceQueue", new IntArrayTag("FurnaceQueue", toIntArray(furnaceQueuedRecipeIndexes)));
            playerTag.put("FurnaceTicksRemaining", new IntTag("FurnaceTicksRemaining", furnaceTicksRemaining));
            playerTag.put("FurnaceTicksTotal", new IntTag("FurnaceTicksTotal", furnaceTicksTotal));
            playerTag.put("FurnaceFuelTicks", new IntTag("FurnaceFuelTicks", furnaceFuelTicks));
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

                int[] defaultInvBlocks = player.getInventoryBlockIds();
                int[] defaultInvCounts = player.getInventoryCounts();
                int[] savedInvBlocks = getIntArray(playerTag, "InventoryBlockIds", defaultInvBlocks);
                int[] savedInvCounts = getIntArray(playerTag, "InventoryCounts", defaultInvCounts);
                player.setInventoryState(savedInvBlocks, savedInvCounts);

                furnaceActive = getByte(playerTag, "FurnaceActive", (byte) 0) != 0;
                furnaceRecipeIndex = getInt(playerTag, "FurnaceRecipeIndex", 0);
                furnaceJobsRemaining = Math.max(0, getInt(playerTag, "FurnaceJobsRemaining", 0));
                furnaceQueuedRecipeIndexes.clear();
                int[] queue = getIntArray(playerTag, "FurnaceQueue", new int[0]);
                for (int q : queue) {
                    if (q >= 0) {
                        furnaceQueuedRecipeIndexes.add(q);
                    }
                }
                furnaceTicksRemaining = Math.max(0, getInt(playerTag, "FurnaceTicksRemaining", 0));
                furnaceTicksTotal = Math.max(0, getInt(playerTag, "FurnaceTicksTotal", 0));
                furnaceFuelTicks = Math.max(0, getInt(playerTag, "FurnaceFuelTicks", 0));
                if (furnaceJobsRemaining <= 0 || furnaceTicksTotal <= 0 || furnaceTicksRemaining <= 0) {
                    furnaceActive = false;
                    furnaceJobsRemaining = 0;
                    furnaceQueuedRecipeIndexes.clear();
                    furnaceTicksRemaining = 0;
                    furnaceTicksTotal = 0;
                }
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

    private static int[] toIntArray(List<Integer> values) {
        if (values == null || values.isEmpty()) {
            return new int[0];
        }
        int[] out = new int[values.size()];
        for (int i = 0; i < values.size(); i++) {
            out[i] = values.get(i);
        }
        return out;
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
                          " | Flying: " + player.isFlying() +
                          " | Sneak: " + player.isSneaking(), 10, 100);
            g2d.drawString("Hotbar Slot: " + (player.getSelectedSlot() + 1) +
                    " | Block: " + player.getSelectedBlockId() +
                    " | Count: " + player.getSelectedBlockCount(), 10, 120);
            g2d.drawString("Mobs Loaded: " + world.getEntities().size() +
                    " | Inventory Used: " + countUsedInventorySlots() + "/" + Player.INVENTORY_SIZE, 10, 140);
            if (miningHits > 0 && miningRequiredHits > 0) {
                int percent = Math.max(0, Math.min(100, (miningHits * 100) / miningRequiredHits));
                g2d.drawString("Mining: " + percent + "% (" + miningHits + "/" + miningRequiredHits + ")", 10, 160);
            }
                g2d.drawString("W/A/S/D Move | Hold LMB Mine | RMB Use/Place/Toggle Door | Shift+RMB Chest=bulk store | MMB Pick | Q Drop/Craft | E Inventory", 10, getHeight() - 10);

                if (inventoryOpen) {
                drawInventoryOverlay(g2d);
                }
        }

        private int countUsedInventorySlots() {
            int used = 0;
            int[] counts = player.getInventoryCounts();
            for (int count : counts) {
                if (count > 0) {
                    used++;
                }
            }
            return used;
        }

        private void drawInventoryOverlay(Graphics2D g2d) {
            int width = getWidth();
            int height = getHeight();

            g2d.setColor(new Color(0, 0, 0, 160));
            g2d.fillRect(0, 0, width, height);

            int panelW = 630;
            int panelH = 250;
            int panelX = (width - panelW) / 2;
            int panelY = (height - panelH) / 2;

            g2d.setColor(new Color(34, 34, 34, 220));
            g2d.fillRoundRect(panelX, panelY, panelW, panelH, 14, 14);
            g2d.setColor(Color.WHITE);
            g2d.drawRoundRect(panelX, panelY, panelW, panelH, 14, 14);
            g2d.drawString("Inventory (E close, Arrows, Enter, X move, Shift+X move all, Z split, Tab mode, [/] category, PgUp/PgDn, Q/Shift+Q action)", panelX + 16, panelY + 22);

            int cellW = 62;
            int cellH = 48;
            int startX = panelX + 16;
            int startY = panelY + 34;
            int totalSlots = Player.HOTBAR_SIZE + Player.INVENTORY_SIZE;

            for (int i = 0; i < totalSlots; i++) {
                int row = i / INVENTORY_COLUMNS;
                int col = i % INVENTORY_COLUMNS;
                int x = startX + col * (cellW + 5);
                int y = startY + row * (cellH + 5);

                boolean selected = i == inventoryCursor;
                g2d.setColor(selected ? new Color(235, 188, 68) : new Color(70, 70, 70));
                g2d.fillRoundRect(x, y, cellW, cellH, 8, 8);
                g2d.setColor(Color.BLACK);
                g2d.drawRoundRect(x, y, cellW, cellH, 8, 8);

                int blockId = player.getCombinedSlotBlockId(i);
                int count = player.getCombinedSlotCount(i);

                g2d.setColor(Color.WHITE);
                g2d.drawString("S" + (i + 1), x + 5, y + 14);
                if (i == player.getSelectedSlot()) {
                    g2d.drawString("[SEL]", x + 30, y + 14);
                }
                if (blockId > 0 && count > 0) {
                    g2d.drawString("ID " + blockId, x + 5, y + 30);
                    g2d.drawString("x" + count, x + 5, y + 44);
                } else {
                    g2d.drawString("(empty)", x + 5, y + 34);
                }
            }

            drawCraftingPanel(g2d, panelX, panelY, panelW, panelH);
        }

        private void drawCraftingPanel(Graphics2D g2d, int panelX, int panelY, int panelW, int panelH) {
            String modeLabel = smeltingMode ? "SMELTING" : "CRAFTING";
            String categoryLabel = smeltingMode ? getSmeltCategoryName(selectedSmeltCategory) : getCraftCategoryName(selectedCraftCategory);

            int barX = panelX + 16;
            int barY = panelY + panelH - 48;
            int barW = panelW - 32;
            int barH = 34;

            g2d.setColor(new Color(20, 20, 20, 230));
            g2d.fillRoundRect(barX, barY, barW, barH, 8, 8);
            g2d.setColor(Color.WHITE);
            g2d.drawRoundRect(barX, barY, barW, barH, 8, 8);

            g2d.drawString("Mode: " + modeLabel + " | Cat: " + categoryLabel, barX + barW - 220, barY + 15);

            if (!smeltingMode) {
                List<CraftRecipe> recipes = getVisibleCraftRecipes();
                if (recipes.isEmpty()) {
                    return;
                }
                if (selectedRecipeIndex < 0 || selectedRecipeIndex >= recipes.size()) {
                    selectedRecipeIndex = 0;
                }

                CraftRecipe recipe = recipes.get(selectedRecipeIndex);
                boolean craftable = canCraftRecipe(recipe);

                g2d.drawString("Recipe " + (selectedRecipeIndex + 1) + "/" + recipes.size() + ": " + recipe.name +
                        " -> ID " + recipe.resultBlockId + " x" + recipe.resultCount +
                        (craftable ? " [READY]" : " [MISSING]"), barX + 8, barY + 15);

                StringBuilder needs = new StringBuilder("Needs: ");
                for (int i = 0; i < recipe.ingredientBlockIds.length; i++) {
                    if (i > 0) {
                        needs.append(", ");
                    }
                    int id = recipe.ingredientBlockIds[i];
                    int required = recipe.ingredientCounts[i];
                    int have = player.getTotalBlockCount(id);
                    needs.append("ID ").append(id).append(" x").append(required).append(" (have ").append(have).append(")");
                }
                g2d.drawString(needs.toString(), barX + 8, barY + 29);
            } else {
                List<SmeltRecipe> recipes = getVisibleSmeltRecipes();
                if (recipes.isEmpty()) {
                    return;
                }
                if (selectedSmeltIndex < 0 || selectedSmeltIndex >= recipes.size()) {
                    selectedSmeltIndex = 0;
                }

                SmeltRecipe recipe = recipes.get(selectedSmeltIndex);
                boolean smeltable = canSmeltRecipe(recipe);
                g2d.drawString("Recipe " + (selectedSmeltIndex + 1) + "/" + recipes.size() + ": " + recipe.name +
                        " -> ID " + recipe.resultBlockId + " x" + recipe.resultCount +
                        (smeltable ? " [READY]" : " [MISSING]"), barX + 8, barY + 15);

                int haveInput = player.getTotalBlockCount(recipe.inputBlockId);
                String fuelState = hasAnyFuel() ? "Fuel: YES" : "Fuel: NO";
                g2d.drawString("Needs: ID " + recipe.inputBlockId + " x" + recipe.inputCount + " (have " + haveInput + ") | " + fuelState,
                        barX + 8, barY + 29);

                if (furnaceActive && furnaceTicksTotal > 0) {
                    int progress = (int) Math.round((1.0 - (furnaceTicksRemaining / (double) furnaceTicksTotal)) * 100.0);
                    progress = Math.max(0, Math.min(100, progress));
                    g2d.drawString("Furnace: ACTIVE " + progress + "% | Queue " + furnaceJobsRemaining + " | FuelTicks " + furnaceFuelTicks,
                            barX + barW - 330, barY + 29);
                } else {
                    g2d.drawString("Furnace: IDLE | FuelTicks " + furnaceFuelTicks, barX + barW - 220, barY + 29);
                }
            }

            if (lastCraftMessageTicks > 0) {
                g2d.setColor(lastCraftedBlockId > 0 ? new Color(152, 236, 152) : new Color(236, 152, 152));
                String message;
                if (lastCraftedBlockId > 0) {
                    message = (smeltingMode ? "Smelted" : "Crafted") + " ID " + lastCraftedBlockId + " x" + lastCraftedCount;
                } else if (smeltingMode && furnaceActive) {
                    message = "Smelt queued";
                } else {
                    message = (smeltingMode ? "Smelt" : "Craft") + " failed (missing items or fuel)";
                }
                g2d.drawString(message, barX + barW - 220, barY + 15);
            }
        }
    }

    private static class CraftRecipe {
        private final String name;
        private final int[] ingredientBlockIds;
        private final int[] ingredientCounts;
        private final int resultBlockId;
        private final int resultCount;
        private final int category;

        private CraftRecipe(String name, int[] ingredientBlockIds, int[] ingredientCounts, int resultBlockId, int resultCount, int category) {
            this.name = name;
            this.ingredientBlockIds = ingredientBlockIds;
            this.ingredientCounts = ingredientCounts;
            this.resultBlockId = resultBlockId;
            this.resultCount = resultCount;
            this.category = category;
        }
    }

    private static class SmeltRecipe {
        private final String name;
        private final int inputBlockId;
        private final int inputCount;
        private final int resultBlockId;
        private final int resultCount;
        private final int category;

        private SmeltRecipe(String name, int inputBlockId, int inputCount, int resultBlockId, int resultCount, int category) {
            this.name = name;
            this.inputBlockId = inputBlockId;
            this.inputCount = inputCount;
            this.resultBlockId = resultBlockId;
            this.resultCount = resultCount;
            this.category = category;
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
