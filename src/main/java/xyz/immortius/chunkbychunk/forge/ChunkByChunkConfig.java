package xyz.immortius.chunkbychunk.forge;

import net.minecraftforge.common.ForgeConfigSpec;
import xyz.immortius.chunkbychunk.config.ChunkRewardChestContent;

/**
 * Forge implementation of config storage. Keeping this for now because forge does some lifting (ensures config is passed to client primarily), but it is duplicating the common config code.
 */
public class ChunkByChunkConfig {
    public static final ForgeConfigSpec GENERAL_SPEC;

    public static ForgeConfigSpec.BooleanValue spawnNewChunkChest;
    public static ForgeConfigSpec.BooleanValue useBedrockChest;
    public static ForgeConfigSpec.IntValue minNewChunkChestSpawnDepth;
    public static ForgeConfigSpec.IntValue maxNewChunkChestSpawnDepth;
    public static ForgeConfigSpec.IntValue initialChunks;
    public static ForgeConfigSpec.IntValue chestQuantity;
    public static ForgeConfigSpec.EnumValue<ChunkRewardChestContent> chestContents;
    public static ForgeConfigSpec.IntValue bedrockChestBlocksRemainingThreshold;
    public static ForgeConfigSpec.IntValue chunkGenXOffset;
    public static ForgeConfigSpec.IntValue chunkGenZOffset;
    public static ForgeConfigSpec.IntValue worldForgeProductionRate;
    public static ForgeConfigSpec.IntValue worldForgeSoilFuelValue;
    public static ForgeConfigSpec.IntValue worldForgeStoneFuelValue;
    public static ForgeConfigSpec.IntValue worldForgeFuelPerFragment;
    public static ForgeConfigSpec.BooleanValue sealWorld;
    public static ForgeConfigSpec.BooleanValue blockPlacementAllowedOutsideSpawnedChunks;

    static {
        ForgeConfigSpec.Builder configBuilder = new ForgeConfigSpec.Builder();
        setupConfig(configBuilder);
        GENERAL_SPEC = configBuilder.build();
    }

    private static void setupConfig(ForgeConfigSpec.Builder builder) {
        xyz.immortius.chunkbychunk.config.ChunkByChunkConfig exampleConfig = new xyz.immortius.chunkbychunk.config.ChunkByChunkConfig();
        builder.push("ChunkGeneration");
        sealWorld = builder.comment("Should empty chunks be generated as a bedrock outline").define("seal_world", exampleConfig.getGeneration().sealWorld());
        spawnNewChunkChest = builder.comment("Should chunks include a chest with items to enable obtaining more chunks?").define("spawn_new_chunk_chest", exampleConfig.getGeneration().spawnNewChunkChest());
        useBedrockChest = builder.comment("Should the chest be a bedrock chest rather than a standard chest?").define("use_bedrock_chest", exampleConfig.getGeneration().useBedrockChest());
        minNewChunkChestSpawnDepth = builder.comment("The minimum depth at which the bedrock chest can spawn").defineInRange("min_new_chunk_chest_spawn_depth", exampleConfig.getGeneration().getMinChestSpawnDepth(), -64, 128);
        maxNewChunkChestSpawnDepth = builder.comment("The maximum depth at which the bedrock chest can spawn").defineInRange("max_new_chunk_chest_spawn_depth", exampleConfig.getGeneration().getMaxChestSpawnDepth(), -64, 128);
        chestQuantity = builder.comment("The number of items the bedrock chest provides").defineInRange("chest_quantity", exampleConfig.getGeneration().getChestQuantity(), 1, 64);
        chestContents = builder.comment("The type of items the bedrock chest provides").defineEnum("chest_contents", exampleConfig.getGeneration().getChestContents());
        initialChunks= builder.comment("The number of chunks to spawn initially (up to 9).").defineInRange("initial_chunks", exampleConfig.getGeneration().getInitialChunks(), 1, 9);
        chunkGenXOffset = builder.comment("Offsets the spawn of chunk from the standard generator. e.g. an offset of 3 means the (0,0) chunk will be the (3,0) chunk of the world").defineInRange("chunk_gen_x_offset", exampleConfig.getGeneration().getChunkGenXOffset(), Short.MIN_VALUE, Short.MAX_VALUE);
        chunkGenZOffset = builder.comment("Offsets the spawn of chunk from the standard generator.").defineInRange("chunk_gen_z_offset", exampleConfig.getGeneration().getChunkGenZOffset(), Short.MIN_VALUE, Short.MAX_VALUE);
        builder.pop();
        builder.push("BedrockChest");
        bedrockChestBlocksRemainingThreshold = builder.comment("The number of blocks within the chunk above the bedrock chest allowed to remain before it will open").defineInRange("bedrock_chest_unlock_at_blocks_remaining", exampleConfig.getBedrockChest().getBedrockChestBlocksRemainingThreshold(), 0, Short.MAX_VALUE * 2);
        builder.pop();
        builder.push("WorldForge");
        worldForgeProductionRate = builder.comment("The rate at which the world forge processes consumed blocks, in fuel per tick").defineInRange("production_rate", exampleConfig.getWorldForge().getProductionRate(), 1, 256);
        worldForgeSoilFuelValue = builder.comment("The value of fuel provided by soils (dirt, sand, gravel, etc). 0 to disallow use as fuel").defineInRange("soil_fuel_value", exampleConfig.getWorldForge().getSoilFuelValue(), 0, 256);
        worldForgeStoneFuelValue = builder.comment("The value of fuel provided by raw stones (cobblestone, deep slate cobblestone, etc). 0 to disallow use as fuel").defineInRange("stone_fuel_value", exampleConfig.getWorldForge().getStoneFuelValue(), 0, 256);
        worldForgeFuelPerFragment = builder.comment("The cost in fuel to produce a single world fragment").defineInRange("fragment_fuel_cost", exampleConfig.getWorldForge().getFragmentFuelCost(), 1, 256);
        builder.pop();
        builder.push("Gameplay");
        blockPlacementAllowedOutsideSpawnedChunks = builder.comment("Can blocks be placed outside spawned chunks").define("block_placement_allowed_outside_spawned_chunks", exampleConfig.getGameplayConfig().isBlockPlacementAllowedOutsideSpawnedChunks());
        builder.pop();
    }

    public static int minChestSpawnDepth() {
        return Math.min(minNewChunkChestSpawnDepth.get(), maxNewChunkChestSpawnDepth.get());
    }

    public static int maxChestSpawnDepth() {
        return Math.max(minNewChunkChestSpawnDepth.get(), maxNewChunkChestSpawnDepth.get());
    }


}
