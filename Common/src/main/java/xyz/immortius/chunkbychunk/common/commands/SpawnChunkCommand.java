package xyz.immortius.chunkbychunk.common.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.tree.LiteralCommandNode;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.coordinates.Coordinates;
import net.minecraft.commands.arguments.coordinates.Vec3Argument;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.Vec3;
import xyz.immortius.chunkbychunk.common.blocks.TriggeredSpawnRandomChunkBlock;
import xyz.immortius.chunkbychunk.common.world.SpawnChunkHelper;
import xyz.immortius.chunkbychunk.interop.Services;

public class SpawnChunkCommand {

    private static final SimpleCommandExceptionType INVALID_POSITION = new SimpleCommandExceptionType(Component.translatable("commands.chunkbychunk.spawnchunk.invalidPosition"));
    private static final SimpleCommandExceptionType INVALID_LEVEL = new SimpleCommandExceptionType(Component.translatable("commands.chunkbychunk.spawnchunk.invalidlevel"));
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
    }

    private static int spawnChunk(CommandSourceStack stack, ServerLevel level, Coordinates specifiedCoords, boolean random) throws CommandSyntaxException {
        Vec3 vec3 = specifiedCoords.getPosition(stack);
        BlockPos pos = new BlockPos(vec3.x, level.getMaxBuildHeight() - 1, vec3.z);
        ChunkPos chunkPos = new ChunkPos(pos);

        if (!SpawnChunkHelper.isValidForChunkSpawn(level)) {
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
            level.setBlock(pos, Services.PLATFORM.triggeredSpawnRandomChunkBlock().defaultBlockState(), Block.UPDATE_ALL);
        } else {
            SpawnChunkHelper.spawnChunkBlocks(level, chunkPos);
            level.setBlock(pos, Services.PLATFORM.triggeredSpawnChunkBlock().defaultBlockState(), Block.UPDATE_ALL);
        }
        return 1;
    }
}
