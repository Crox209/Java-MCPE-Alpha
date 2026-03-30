package com.minecraft.mcpe.block;

import com.minecraft.mcpe.nbt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * FurnaceBlockEntity - represents a furnace placed in the world
 */
public class FurnaceBlockEntity extends BlockEntity {
    private int[] inputBlockIds;
    private int[] inputCounts;
    private int[] fuelBlockIds;
    private int[] fuelCounts;
    private int[] outputBlockIds;
    private int[] outputCounts;
    
    private boolean active;
    private int ticksRemaining;
    private int ticksTotal;
    private int fuelTicks;
    private List<Integer> queuedRecipeIndexes;
    private int currentRecipeIndex;
    private int jobsRemaining;

    private static final int INVENTORY_SIZE = 3; // input, fuel, output

    public FurnaceBlockEntity(int x, int y, int z) {
        super(x, y, z, Block.FURNACE);
        this.inputBlockIds = new int[INVENTORY_SIZE];
        this.inputCounts = new int[INVENTORY_SIZE];
        this.fuelBlockIds = new int[INVENTORY_SIZE];
        this.fuelCounts = new int[INVENTORY_SIZE];
        this.outputBlockIds = new int[INVENTORY_SIZE];
        this.outputCounts = new int[INVENTORY_SIZE];
        this.active = false;
        this.ticksRemaining = 0;
        this.ticksTotal = 0;
        this.fuelTicks = 0;
        this.queuedRecipeIndexes = new ArrayList<>();
        this.currentRecipeIndex = 0;
        this.jobsRemaining = 0;
    }

    public int[] getInputBlockIds() { return inputBlockIds; }
    public int[] getInputCounts() { return inputCounts; }
    public int[] getFuelBlockIds() { return fuelBlockIds; }
    public int[] getFuelCounts() { return fuelCounts; }
    public int[] getOutputBlockIds() { return outputBlockIds; }
    public int[] getOutputCounts() { return outputCounts; }

    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
    public int getTicksRemaining() { return ticksRemaining; }
    public void setTicksRemaining(int ticks) { this.ticksRemaining = ticks; }
    public int getTicksTotal() { return ticksTotal; }
    public void setTicksTotal(int ticks) { this.ticksTotal = ticks; }
    public int getFuelTicks() { return fuelTicks; }
    public void setFuelTicks(int ticks) { this.fuelTicks = ticks; }

    public List<Integer> getQueuedRecipeIndexes() { return queuedRecipeIndexes; }
    public int getCurrentRecipeIndex() { return currentRecipeIndex; }
    public void setCurrentRecipeIndex(int idx) { this.currentRecipeIndex = idx; }
    public int getJobsRemaining() { return jobsRemaining; }
    public void setJobsRemaining(int jobs) { this.jobsRemaining = jobs; }

    @Override
    public void save(CompoundTag tag) {
        CompoundTag furnaceTag = new CompoundTag("Furnace");
        
        // Save input
        furnaceTag.put("InputBlockIds", new IntArrayTag("InputBlockIds", inputBlockIds.clone()));
        furnaceTag.put("InputCounts", new IntArrayTag("InputCounts", inputCounts.clone()));
        
        // Save fuel
        furnaceTag.put("FuelBlockIds", new IntArrayTag("FuelBlockIds", fuelBlockIds.clone()));
        furnaceTag.put("FuelCounts", new IntArrayTag("FuelCounts", fuelCounts.clone()));
        
        // Save output
        furnaceTag.put("OutputBlockIds", new IntArrayTag("OutputBlockIds", outputBlockIds.clone()));
        furnaceTag.put("OutputCounts", new IntArrayTag("OutputCounts", outputCounts.clone()));
        
        // Save state
        furnaceTag.put("Active", new ByteTag("Active", (byte) (active ? 1 : 0)));
        furnaceTag.put("TicksRemaining", new IntTag("TicksRemaining", ticksRemaining));
        furnaceTag.put("TicksTotal", new IntTag("TicksTotal", ticksTotal));
        furnaceTag.put("FuelTicks", new IntTag("FuelTicks", fuelTicks));
        furnaceTag.put("CurrentRecipeIndex", new IntTag("CurrentRecipeIndex", currentRecipeIndex));
        furnaceTag.put("JobsRemaining", new IntTag("JobsRemaining", jobsRemaining));
        
        if (!queuedRecipeIndexes.isEmpty()) {
            int[] queueArray = new int[queuedRecipeIndexes.size()];
            for (int i = 0; i < queuedRecipeIndexes.size(); i++) {
                queueArray[i] = queuedRecipeIndexes.get(i);
            }
            furnaceTag.put("QueuedRecipes", new IntArrayTag("QueuedRecipes", queueArray));
        }
        
        tag.put("Furnace", furnaceTag);
    }

    @Override
    public void load(CompoundTag tag) {
        Tag rawFurnaceTag = tag.get("Furnace");
        if (!(rawFurnaceTag instanceof CompoundTag)) {
            return;
        }
        CompoundTag furnaceTag = (CompoundTag) rawFurnaceTag;

        // Load input
        Tag inputBlocksTag = furnaceTag.get("InputBlockIds");
        if (inputBlocksTag instanceof IntArrayTag) {
            inputBlockIds = ((IntArrayTag) inputBlocksTag).value.clone();
        }
        Tag inputCountsTag = furnaceTag.get("InputCounts");
        if (inputCountsTag instanceof IntArrayTag) {
            inputCounts = ((IntArrayTag) inputCountsTag).value.clone();
        }

        // Load fuel
        Tag fuelBlocksTag = furnaceTag.get("FuelBlockIds");
        if (fuelBlocksTag instanceof IntArrayTag) {
            fuelBlockIds = ((IntArrayTag) fuelBlocksTag).value.clone();
        }
        Tag fuelCountsTag = furnaceTag.get("FuelCounts");
        if (fuelCountsTag instanceof IntArrayTag) {
            fuelCounts = ((IntArrayTag) fuelCountsTag).value.clone();
        }

        // Load output
        Tag outputBlocksTag = furnaceTag.get("OutputBlockIds");
        if (outputBlocksTag instanceof IntArrayTag) {
            outputBlockIds = ((IntArrayTag) outputBlocksTag).value.clone();
        }
        Tag outputCountsTag = furnaceTag.get("OutputCounts");
        if (outputCountsTag instanceof IntArrayTag) {
            outputCounts = ((IntArrayTag) outputCountsTag).value.clone();
        }

        // Load state
        Tag activeTag = furnaceTag.get("Active");
        if (activeTag instanceof ByteTag) {
            active = ((ByteTag) activeTag).value != 0;
        }
        Tag ticksRemainingTag = furnaceTag.get("TicksRemaining");
        if (ticksRemainingTag instanceof IntTag) {
            ticksRemaining = ((IntTag) ticksRemainingTag).value;
        }
        Tag ticksTotalTag = furnaceTag.get("TicksTotal");
        if (ticksTotalTag instanceof IntTag) {
            ticksTotal = ((IntTag) ticksTotalTag).value;
        }
        Tag fuelTicksTag = furnaceTag.get("FuelTicks");
        if (fuelTicksTag instanceof IntTag) {
            fuelTicks = ((IntTag) fuelTicksTag).value;
        }
        Tag recipeIndexTag = furnaceTag.get("CurrentRecipeIndex");
        if (recipeIndexTag instanceof IntTag) {
            currentRecipeIndex = ((IntTag) recipeIndexTag).value;
        }
        Tag jobsTag = furnaceTag.get("JobsRemaining");
        if (jobsTag instanceof IntTag) {
            jobsRemaining = ((IntTag) jobsTag).value;
        }
        
        Tag queueTag = furnaceTag.get("QueuedRecipes");
        if (queueTag instanceof IntArrayTag) {
            int[] queueArray = ((IntArrayTag) queueTag).value;
            queuedRecipeIndexes.clear();
            for (int idx : queueArray) {
                queuedRecipeIndexes.add(idx);
            }
        }
    }
}
