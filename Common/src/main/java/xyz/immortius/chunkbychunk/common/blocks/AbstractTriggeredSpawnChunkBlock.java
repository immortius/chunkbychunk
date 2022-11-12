package xyz.immortius.chunkbychunk.common.blocks;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import xyz.immortius.chunkbychunk.common.ChunkByChunkConstants;
import xyz.immortius.chunkbychunk.common.world.SkyChunkGenerator;
import xyz.immortius.chunkbychunk.common.world.SpawnChunkHelper;

import java.util.function.Function;

/**
 * Base type for blocks that trigger the spawn of a chunk (after a short delay to allow entity spawn).
 * These blocks are used to force the loading of the chunk that will be spawned in the generation dimension.
 */
public abstract class AbstractTriggeredSpawnChunkBlock extends BaseEntityBlock {

    private final Function<BlockPos, ChunkPos> sourceChunkFunc;
    private final Function<ServerLevel, ResourceKey<Level>> sourceLevelFunc;

    public static ResourceKey<Level> getSkyGenerationSourceLevel(ServerLevel target) {
        if (target.getChunkSource().getGenerator() instanceof SkyChunkGenerator generator) {
            return generator.getGenerationLevel();
        }
        return null;
    }

    /**
     * @param sourceLevel The level to spawn chunks from
     * @param sourceChunkFunc The function to map from target block pos to source chunk position
     * @param blockProperties
     */
    public AbstractTriggeredSpawnChunkBlock(ResourceKey<Level> sourceLevel, Function<BlockPos, ChunkPos> sourceChunkFunc, Properties blockProperties) {
        super(blockProperties);
        this.sourceChunkFunc = sourceChunkFunc;
        this.sourceLevelFunc = (unused) -> sourceLevel;
    }

    /**
     * @param sourceLevelFunc A method to determine the level to spawn chunks from
     * @param sourceChunkFunc The function to map from target block pos to source chunk position
     * @param blockProperties
     */
    public AbstractTriggeredSpawnChunkBlock(Function<ServerLevel, ResourceKey<Level>> sourceLevelFunc, Function<BlockPos, ChunkPos> sourceChunkFunc, Properties blockProperties) {
        super(blockProperties);
        this.sourceChunkFunc = sourceChunkFunc;
        this.sourceLevelFunc = sourceLevelFunc;
    }

    public ResourceKey<Level> getSourceLevel(ServerLevel level) {
        return sourceLevelFunc.apply(level);
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.INVISIBLE;
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter getter, BlockPos pos, CollisionContext collisionContext) {
        return Shapes.empty();
    }

    public abstract boolean validForLevel(ServerLevel level);

    @Override
    public void onPlace(BlockState state, Level level, BlockPos pos, BlockState prevState, boolean p_60570_) {
        super.onPlace(state, level, pos, prevState, p_60570_);
        if (!level.isClientSide()) {
            ServerLevel targetLevel = (ServerLevel) level;
            ServerLevel sourceLevel = level.getServer().getLevel(sourceLevelFunc.apply(targetLevel));
            if (sourceLevel != null && validForLevel(targetLevel)) {
                ChunkPos sourceChunkPos = sourceChunkFunc.apply(pos);
                ChunkPos targetChunkPos = new ChunkPos(pos);
                sourceLevel.setChunkForced(sourceChunkPos.x, sourceChunkPos.z, true);
                targetLevel.setChunkForced(targetChunkPos.x, targetChunkPos.z, true);
            } else {
                ChunkByChunkConstants.LOGGER.warn("Invalid triggered spawn chunk block detected at {}:{}", level.dimension(), pos);
            }
        }
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState prevState, boolean p_60519_) {
        super.onRemove(state, level, pos, prevState, p_60519_);
        if (!level.isClientSide()) {
            ServerLevel targetLevel = (ServerLevel) level;
            ServerLevel sourceLevel = level.getServer().getLevel(sourceLevelFunc.apply(targetLevel));
            if (sourceLevel != null && validForLevel(targetLevel)) {
                ChunkPos sourceChunkPos = sourceChunkFunc.apply(pos);
                ChunkPos targetChunkPos = new ChunkPos(pos);
                sourceLevel.setChunkForced(sourceChunkPos.x, sourceChunkPos.z, false);
                targetLevel.setChunkForced(targetChunkPos.x, targetChunkPos.z, false);
            }
        }
    }
}
