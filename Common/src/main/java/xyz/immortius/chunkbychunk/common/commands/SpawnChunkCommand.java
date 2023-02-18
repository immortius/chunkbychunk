package xyz.immortius.chunkbychunk.common.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import com.mojang.brigadier.tree.LiteralCommandNode;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.coordinates.Coordinates;
import net.minecraft.commands.arguments.coordinates.Vec3Argument;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.Vec3;
import xyz.immortius.chunkbychunk.common.ChunkByChunkConstants;
import xyz.immortius.chunkbychunk.common.blocks.TriggeredSpawnRandomChunkBlock;
import xyz.immortius.chunkbychunk.common.world.SkyChunkGenerator;
import xyz.immortius.chunkbychunk.common.world.SpawnChunkHelper;
import xyz.immortius.chunkbychunk.interop.Services;

import java.util.concurrent.CompletableFuture;

public class SpawnChunkCommand {

    private static final SimpleCommandExceptionType INVALID_POSITION = new SimpleCommandExceptionType(Component.translatable("commands.chunkbychunk.spawnchunk.invalidPosition"));
    private static final SimpleCommandExceptionType INVALID_LEVEL = new SimpleCommandExceptionType(Component.translatable("commands.chunkbychunk.spawnchunk.invalidlevel"));
    private static final SimpleCommandExceptionType INVALID_THEME = new SimpleCommandExceptionType(Component.translatable("commands.chunkbychunk.spawnchunk.invalidtheme"));
    private static final SimpleCommandExceptionType NON_EMPTY_CHUNK = new SimpleCommandExceptionType(Component.translatable("commands.chunkbychunk.spawnchunk.nonemptychunk"));

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        LiteralCommandNode<CommandSourceStack> spawnChunkCommand = dispatcher.register(Commands.literal("chunkbychunk:spawnChunk")
                .requires(x -> x.hasPermission(2))
                .then(Commands.argument("location", Vec3Argument.vec3())
                .executes((cmd) -> spawnChunk(cmd.getSource(), cmd.getSource().getLevel(), Vec3Argument.getCoordinates(cmd, "location"), false))));

        LiteralCommandNode<CommandSourceStack> spawnRandomChunkCommand = dispatcher.register(Commands.literal("chunkbychunk:spawnRandomChunk")
                .requires(x -> x.hasPermission(2))
                .then(Commands.argument("location", Vec3Argument.vec3())
                        .executes((cmd) -> spawnChunk(cmd.getSource(), cmd.getSource().getLevel(), Vec3Argument.getCoordinates(cmd, "location"), true))));

        LiteralCommandNode<CommandSourceStack> spawnBiomeChunkCommand = dispatcher.register(Commands.literal("chunkbychunk:spawnThemedChunk")
                .requires(x -> x.hasPermission(2))
                .then(Commands.argument("theme", StringArgumentType.word()).suggests(new BiomeThemeSuggestionProvider())
                        .then(Commands.argument("location", Vec3Argument.vec3())
                                .executes((cmd) -> spawnThemedChunk(cmd.getSource(), cmd.getSource().getLevel(), StringArgumentType.getString(cmd, "theme"), Vec3Argument.getCoordinates(cmd, "location"))))))
                ;
    }

    private static class BiomeThemeSuggestionProvider implements SuggestionProvider<CommandSourceStack> {

        @Override
        public CompletableFuture<Suggestions> getSuggestions(CommandContext<CommandSourceStack> context, SuggestionsBuilder builder) {
            ChunkByChunkConstants.BIOME_THEMES.forEach(builder::suggest);
            return builder.buildFuture();
        }
    }

    private static int spawnChunk(CommandSourceStack stack, ServerLevel level, Coordinates specifiedCoords, boolean random) throws CommandSyntaxException {
        Vec3 vec3 = specifiedCoords.getPosition(stack);
        BlockPos pos = new BlockPos(vec3.x, level.getMaxBuildHeight() - 1, vec3.z);
        ChunkPos chunkPos = new ChunkPos(pos);

        if (!(level.getChunkSource().getGenerator() instanceof SkyChunkGenerator)) {
            throw INVALID_LEVEL.create();
        }
        if (!Level.isInSpawnableBounds(pos)) {
            throw INVALID_POSITION.create();
        }
        if (!SpawnChunkHelper.isEmptyChunk(level, chunkPos)) {
            throw NON_EMPTY_CHUNK.create();
        }

        if (random) {
            ChunkPos sourceChunk = TriggeredSpawnRandomChunkBlock.getSourceChunk(pos);
            SpawnChunkHelper.spawnChunkBlocks(level, chunkPos, sourceChunk);
            level.setBlock(pos, Services.PLATFORM.triggeredSpawnRandomChunkBlock().defaultBlockState(), Block.UPDATE_NONE);
        } else {
            SpawnChunkHelper.spawnChunkBlocks(level, chunkPos);
            level.setBlock(pos, Services.PLATFORM.triggeredSpawnChunkBlock().defaultBlockState(), Block.UPDATE_NONE);
        }
        return 1;
    }

    private static int spawnThemedChunk(CommandSourceStack stack, ServerLevel level, String biome, Coordinates specifiedCoords) throws CommandSyntaxException {
        Vec3 vec3 = specifiedCoords.getPosition(stack);
        BlockPos pos = new BlockPos(vec3.x, level.getMaxBuildHeight() - 1, vec3.z);
        ChunkPos chunkPos = new ChunkPos(pos);

        if (level.getChunkSource().getGenerator() instanceof SkyChunkGenerator skyChunkGenerator) {
            ResourceKey<Level> biomeDimension = skyChunkGenerator.getBiomeDimension(biome);
            if (biomeDimension == null) {
                throw INVALID_THEME.create();
            }
            if (!Level.isInSpawnableBounds(pos)) {
                throw INVALID_POSITION.create();
            }
            if (!SpawnChunkHelper.isEmptyChunk(level, chunkPos)) {
                throw NON_EMPTY_CHUNK.create();
            }

            ServerLevel sourceLevel = level.getServer().getLevel(biomeDimension);
            if (sourceLevel == null) {
                throw INVALID_THEME.create();
            }
            SpawnChunkHelper.spawnChunkBlocks(level, chunkPos, sourceLevel, chunkPos);
            level.setBlock(pos, level.getServer().registryAccess().registry(Registries.BLOCK).get().get(new ResourceLocation(ChunkByChunkConstants.MOD_ID, biome + ChunkByChunkConstants.TRIGGERED_BIOME_CHUNK_BLOCK_SUFFIX)).defaultBlockState(), Block.UPDATE_NONE);
            return 1;
        } else {
            throw INVALID_LEVEL.create();
        }
    }


}
