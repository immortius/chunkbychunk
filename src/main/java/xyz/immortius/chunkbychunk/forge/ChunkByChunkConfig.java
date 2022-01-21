package xyz.immortius.chunkbychunk.forge;

import net.minecraftforge.common.ForgeConfigSpec;

public class ChunkByChunkConfig {
    public static final ForgeConfigSpec GENERAL_SPEC;

    public static ForgeConfigSpec.BooleanValue spawnNewChunkChest;
    public static ForgeConfigSpec.IntValue minNewChunkChestSpawnDepth;
    public static ForgeConfigSpec.IntValue maxNewChunkChestSpawnDepth;
    public static ForgeConfigSpec.IntValue initialChunks;
    public static ForgeConfigSpec.IntValue numChunkSpawners;
    public static ForgeConfigSpec.BooleanValue giveUnstableChunkSpawners;
    public static ForgeConfigSpec.IntValue bedrockChestBlocksRemainingThreshold;
    public static ForgeConfigSpec.IntValue chunkGenXOffset;
    public static ForgeConfigSpec.IntValue chunkGenZOffset;
    public static ForgeConfigSpec.IntValue worldForgeProductionRate;
    public static ForgeConfigSpec.IntValue worldForgeSoilFuelValue;
    public static ForgeConfigSpec.IntValue worldForgeStoneFuelValue;
    public static ForgeConfigSpec.IntValue worldForgeFuelPerFragment;

    static {
        ForgeConfigSpec.Builder configBuilder = new ForgeConfigSpec.Builder();
        setupConfig(configBuilder);
        GENERAL_SPEC = configBuilder.build();
    }

    private static void setupConfig(ForgeConfigSpec.Builder builder) {
        builder.push("ChunkGeneration");
        spawnNewChunkChest = builder.comment("Should chunks include a bedrock chest with a chunk spawner?").define("spawn_new_chunk_chest", true);
        minNewChunkChestSpawnDepth = builder.comment("The minimum depth at which the chunk spawner chest can spawn").defineInRange("min_new_chunk_chest_spawn_depth", -60, -64, 128);
        maxNewChunkChestSpawnDepth = builder.comment("The maximum depth at which the chunk spawner chest can spawn").defineInRange("max_new_chunk_chest_spawn_depth", -60, -64, 128);
        numChunkSpawners = builder.comment("The number of chunk spawners the chunk spawner chest provides").defineInRange("num_chunk_spawners", 1, 1, 64);
        giveUnstableChunkSpawners = builder.comment("Should the chunk spawner chest give unstable chunk spawners instead of normal ones?").define("give_unstable_chunk_spawners", false);

        initialChunks= builder.comment("The number of chunks to spawn initially (up to 9).").defineInRange("initial_chunks", 1, 1, 9);
        chunkGenXOffset = builder.comment("Offsets the spawn of chunk from the standard generator. e.g. an offset of 3 means the (0,0) chunk will be the (3,0) chunk of the world").defineInRange("chunk_gen_x_offset", 0, Short.MIN_VALUE, Short.MAX_VALUE);
        chunkGenZOffset = builder.comment("Offsets the spawn of chunk from the standard generator.").defineInRange("chunk_gen_z_offset", 0, Short.MIN_VALUE, Short.MAX_VALUE);
        builder.pop();
        builder.push("BedrockChest");
        bedrockChestBlocksRemainingThreshold = builder.comment("The number of blocks within the chunk above the bedrock chest allowed to remain before it will open").defineInRange("bedrock_chest_unlock_at_blocks_remaining", 16, 0, Short.MAX_VALUE * 2);
        builder.pop();
        builder.push("WorldForge");
        worldForgeProductionRate = builder.comment("The rate at which the world forge processes consumed blocks, in fuel per tick").defineInRange("production_rate", 1, 1, 256);
        worldForgeSoilFuelValue = builder.comment("The value of fuel provided by soils (dirt, sand, gravel, etc). 0 to disallow use as fuel").defineInRange("soil_fuel_value", 2, 0, 256);
        worldForgeStoneFuelValue = builder.comment("The value of fuel provided by raw stones (cobblestone, deep slate cobblestone, etc). 0 to disallow use as fuel").defineInRange("stone_fuel_value", 4, 0, 256);
        worldForgeFuelPerFragment = builder.comment("The cost in fuel to produce a single world fragment").defineInRange("fragment_fuel_cost", 64, 1, 256);
        builder.pop();
    }

    public static int minChestSpawnDepth() {
        return Math.min(minNewChunkChestSpawnDepth.get(), maxNewChunkChestSpawnDepth.get());
    }

    public static int maxChestSpawnDepth() {
        return Math.max(minNewChunkChestSpawnDepth.get(), maxNewChunkChestSpawnDepth.get());
    }

}
