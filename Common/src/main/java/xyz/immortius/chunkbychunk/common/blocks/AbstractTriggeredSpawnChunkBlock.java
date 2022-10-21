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
import xyz.immortius.chunkbychunk.common.world.BaseSkyChunkGenerator;
import xyz.immortius.chunkbychunk.common.world.SpawnChunkHelper;

import java.util.function.Function;

/**
 * Base type for blocks that trigger the spawn of a chunk (after a short delay to allow entity spawn).
 * These blocks are used to force the loading of the chunk that will be spawned in the generation dimension.
 */
public abstract class AbstractTriggeredSpawnChunkBlock extends BaseEntityBlock {

    private final Function<BlockPos, ChunkPos> sourceChunkFunc;
    private final ResourceKey<Level> sourceLevelKey;

    /**
     * @param sourceLevel The level to spawn chunks from
     * @param blockProperties
     * @param sourceChunkFunc The function to map from target block pos to source chunk position
     */
    public AbstractTriggeredSpawnChunkBlock(ResourceKey<Level> sourceLevel, Properties blockProperties, Function<BlockPos, ChunkPos> sourceChunkFunc) {
        super(blockProperties);
        this.sourceChunkFunc = sourceChunkFunc;
        this.sourceLevelKey = sourceLevel;
    }

    public ResourceKey<Level> getSourceLevel() {
        return sourceLevelKey;
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.INVISIBLE;
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter getter, BlockPos pos, CollisionContext collisionContext) {
        return Shapes.empty();
    }

    @Override
    public void onPlace(BlockState state, Level level, BlockPos pos, BlockState prevState, boolean p_60570_) {
        super.onPlace(state, level, pos, prevState, p_60570_);
        if (!level.isClientSide()) {
            ServerLevel targetLevel = (ServerLevel) level;
            
            if (targetLevel.getChunkSource().getGenerator() instanceof BaseSkyChunkGenerator chunkGenerator) {
                ServerLevel sourceLevel = level.getServer().getLevel(sourceLevelKey);
                if (sourceLevel != null && SpawnChunkHelper.isValidForChunkSpawn(targetLevel)) {
                    ChunkPos sourceChunkPos = sourceChunkFunc.apply(pos);
                    ChunkPos targetChunkPos = new ChunkPos(pos);
                    sourceLevel.setChunkForced(sourceChunkPos.x, sourceChunkPos.z, true);
                    targetLevel.setChunkForced(targetChunkPos.x, targetChunkPos.z, true);
                }
            }
        }
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState prevState, boolean p_60519_) {
        super.onRemove(state, level, pos, prevState, p_60519_);
        if (!level.isClientSide()) {
            ServerLevel targetLevel = (ServerLevel) level;
            if (targetLevel.getChunkSource().getGenerator() instanceof BaseSkyChunkGenerator chunkGenerator) {
                ServerLevel sourceLevel = level.getServer().getLevel(chunkGenerator.getGenerationLevel());
                if (sourceLevel != null && SpawnChunkHelper.isValidForChunkSpawn(targetLevel)) {
                    ChunkPos sourceChunkPos = sourceChunkFunc.apply(pos);
                    ChunkPos targetChunkPos = new ChunkPos(pos);
                    sourceLevel.setChunkForced(sourceChunkPos.x, sourceChunkPos.z, false);
                    targetLevel.setChunkForced(targetChunkPos.x, targetChunkPos.z, false);
                }
            }
        }
    }
}
