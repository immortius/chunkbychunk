package xyz.immortius.chunkbychunk.forge;

import net.minecraftforge.common.ForgeConfigSpec;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import xyz.immortius.chunkbychunk.config.*;
import xyz.immortius.chunkbychunk.config.system.Comment;
import xyz.immortius.chunkbychunk.config.system.IntRange;
import xyz.immortius.chunkbychunk.config.system.Name;
import xyz.immortius.chunkbychunk.interop.ChunkByChunkConstants;

import java.lang.reflect.Field;

/**
 * Forge implementation of config storage. Keeping this for now because forge does some lifting (ensures config is passed to client primarily), but it is duplicating the common config code.
 */
public class ChunkByChunkConfig {
    public static final ForgeConfigSpec GENERAL_SPEC;
    private static final Logger LOGGER = LogManager.getLogger(ChunkByChunkConstants.MOD_ID);

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
    public static ForgeConfigSpec.IntValue worldForgeStrongFuelValue;
    public static ForgeConfigSpec.IntValue worldForgeFuelPerFragment;
    public static ForgeConfigSpec.BooleanValue sealWorld;
    public static ForgeConfigSpec.BooleanValue blockPlacementAllowedOutsideSpawnedChunks;
    public static ForgeConfigSpec.IntValue worldScannerFuelPerFragment;
    public static ForgeConfigSpec.IntValue worldScannerFuelRequiredPerChunk;
    public static ForgeConfigSpec.IntValue worldScannerFuelConsumedPerTick;

    static {
        ForgeConfigSpec.Builder configBuilder = new ForgeConfigSpec.Builder();
        setupConfig(configBuilder);
        GENERAL_SPEC = configBuilder.build();
    }

    private static void setupConfig(ForgeConfigSpec.Builder builder) {
        xyz.immortius.chunkbychunk.config.ChunkByChunkConfig exampleConfig = new xyz.immortius.chunkbychunk.config.ChunkByChunkConfig();
        builder.push("ChunkGeneration");
        sealWorld = buildFrom(builder, GenerationConfig.class, "sealWorld", exampleConfig.getGeneration().sealWorld());
        spawnNewChunkChest = buildFrom(builder, GenerationConfig.class, "spawnNewChunkChest", exampleConfig.getGeneration().spawnNewChunkChest());
        useBedrockChest = buildFrom(builder, GenerationConfig.class, "useBedrockChest", exampleConfig.getGeneration().useBedrockChest());
        minNewChunkChestSpawnDepth = buildFrom(builder, GenerationConfig.class, "minChestSpawnDepth", exampleConfig.getGeneration().getMinChestSpawnDepth());
        maxNewChunkChestSpawnDepth = buildFrom(builder, GenerationConfig.class, "maxChestSpawnDepth", exampleConfig.getGeneration().getMaxChestSpawnDepth());
        chestQuantity = buildFrom(builder, GenerationConfig.class, "chestQuantity", exampleConfig.getGeneration().getChestQuantity());
        chestContents = buildFrom(builder, GenerationConfig.class, "chestContents", exampleConfig.getGeneration().getChestContents());
        initialChunks= buildFrom(builder, GenerationConfig.class, "initialChunks", exampleConfig.getGeneration().getInitialChunks());
        chunkGenXOffset = buildFrom(builder, GenerationConfig.class, "chunkGenXOffset", exampleConfig.getGeneration().getChunkGenXOffset());
        chunkGenZOffset = buildFrom(builder, GenerationConfig.class, "chunkGenZOffset", exampleConfig.getGeneration().getChunkGenZOffset());
        builder.pop();
        builder.push("BedrockChest");
        bedrockChestBlocksRemainingThreshold = buildFrom(builder, BedrockChestConfig.class, "bedrockChestBlocksRemainingThreshold", exampleConfig.getBedrockChest().getBedrockChestBlocksRemainingThreshold());
        builder.pop();
        builder.push("WorldForge");
        worldForgeProductionRate = buildFrom(builder, WorldForgeConfig.class, "productionRate", exampleConfig.getWorldForge().getProductionRate());
        worldForgeSoilFuelValue = buildFrom(builder, WorldForgeConfig.class, "soilFuelValue", exampleConfig.getWorldForge().getSoilFuelValue());
        worldForgeStoneFuelValue = buildFrom(builder, WorldForgeConfig.class, "stoneFuelValue", exampleConfig.getWorldForge().getStoneFuelValue());
        worldForgeStrongFuelValue = buildFrom(builder, WorldForgeConfig.class, "strongFuelValue", exampleConfig.getWorldForge().getStrongFuelValue());
        worldForgeFuelPerFragment = buildFrom(builder, WorldForgeConfig.class, "fragmentFuelCost", exampleConfig.getWorldForge().getFragmentFuelCost());
        builder.pop();
        builder.push("Gameplay");
        blockPlacementAllowedOutsideSpawnedChunks = buildFrom(builder, GameplayConfig.class, "blockPlacementAllowedOutsideSpawnedChunks",exampleConfig.getGameplayConfig().isBlockPlacementAllowedOutsideSpawnedChunks());
        builder.pop();
        builder.push("WorldScanner");
        worldScannerFuelPerFragment = buildFrom(builder, WorldScannerConfig.class, "fuelPerFragment", exampleConfig.getWorldScannerConfig().getFuelPerFragment());
        worldScannerFuelRequiredPerChunk = buildFrom(builder, WorldScannerConfig.class, "fuelRequiredPerChunk", exampleConfig.getWorldScannerConfig().getFuelRequiredPerChunk());
        worldScannerFuelConsumedPerTick = buildFrom(builder, WorldScannerConfig.class, "fuelConsumedPerTick", exampleConfig.getWorldScannerConfig().getFuelConsumedPerTick());
        builder.pop();
    }

    public static int minChestSpawnDepth() {
        return Math.min(minNewChunkChestSpawnDepth.get(), maxNewChunkChestSpawnDepth.get());
    }

    public static int maxChestSpawnDepth() {
        return Math.max(minNewChunkChestSpawnDepth.get(), maxNewChunkChestSpawnDepth.get());
    }

    private static ForgeConfigSpec.BooleanValue buildFrom(ForgeConfigSpec.Builder builder, Class<?> type, String fieldName, boolean defaultValue) {
        try {
            Field field = type.getDeclaredField(fieldName);
            Comment comment = field.getAnnotation(Comment.class);
            if (comment != null) {
                builder.comment(comment.value());
            }
            String name = fieldName;
            Name nameAnnotation = field.getAnnotation(Name.class);
            if (nameAnnotation != null) {
                name = nameAnnotation.value();
            }
            return builder.define(name, defaultValue);
        } catch (NoSuchFieldException e) {
            LOGGER.error("Failed to find field {} in {}", fieldName, type.getName(), e);
            throw new RuntimeException(e);
        }
    }

    private static ForgeConfigSpec.IntValue buildFrom(ForgeConfigSpec.Builder builder, Class<?> type, String fieldName, int defaultValue) {
        try {
            Field field = type.getDeclaredField(fieldName);
            Comment comment = field.getAnnotation(Comment.class);
            if (comment != null) {
                builder.comment(comment.value());
            }
            String name = fieldName;
            Name nameAnnotation = field.getAnnotation(Name.class);
            if (nameAnnotation != null) {
                name = nameAnnotation.value();
            }
            int min = Integer.MIN_VALUE;
            int max = Integer.MAX_VALUE;
            IntRange range = field.getAnnotation(IntRange.class);
            if (range != null) {
                min = range.min();
                max = range.max();
            }
            return builder.defineInRange(name, defaultValue, min, max);
        } catch (NoSuchFieldException e) {
            LOGGER.error("Failed to find field {} in {}", fieldName, type.getName(), e);
            throw new RuntimeException(e);
        }
    }

    private static <T extends Enum<T>> ForgeConfigSpec.EnumValue<T> buildFrom(ForgeConfigSpec.Builder builder, Class<?> type, String fieldName, T defaultValue) {
        try {
            Field field = type.getDeclaredField(fieldName);
            Comment comment = field.getAnnotation(Comment.class);
            if (comment != null) {
                builder.comment(comment.value());
            }
            String name = fieldName;
            Name nameAnnotation = field.getAnnotation(Name.class);
            if (nameAnnotation != null) {
                name = nameAnnotation.value();
            }
            return builder.defineEnum(name, defaultValue);
        } catch (NoSuchFieldException e) {
            LOGGER.error("Failed to find field {} in {}", fieldName, type.getName(), e);
            throw new RuntimeException(e);
        }
    }


}
