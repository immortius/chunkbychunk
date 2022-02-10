package xyz.immortius.chunkbychunk.config;

import xyz.immortius.chunkbychunk.config.system.Comment;
import xyz.immortius.chunkbychunk.config.system.IntRange;
import xyz.immortius.chunkbychunk.config.system.Name;

public class WorldScannerConfig {

    @Name("fuel_per_fragment")
    @IntRange(min = 1, max = 512)
    @Comment("The amount of fuel provided by each world fragment (and then scaled up for world shard, crystal and core")
    private int fuelPerFragment = 32;

    @Name("fuel_required_per_chunk")
    @Comment("The amount of fuel required to scan each chunk")
    @IntRange(min = 1, max = Short.MAX_VALUE * 2)
    private int fuelRequiredPerChunk = 32;

    @Name("fuel_consumed_per_tick")
    @Comment("The amount of fuel consumed each tick")
    @IntRange(min = 1, max = Short.MAX_VALUE * 2)
    private int fuelConsumedPerTick = 1;

    public int getFuelConsumedPerTick() {
        return fuelConsumedPerTick;
    }

    public int getFuelPerFragment() {
        return fuelPerFragment;
    }

    public int getFuelRequiredPerChunk() {
        return fuelRequiredPerChunk;
    }
}
