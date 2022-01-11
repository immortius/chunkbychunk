package xyz.immortius.chunkbychunk.common.blocks;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;

import java.util.Random;

/**
 * Spawns a pseudo-random chunk from the generation dimension (position determined from block position).
 * Pseudo-random used so that the same chunk is forced, generated and unforced.
 */
public class UnstableSpawnChunkBlock extends BaseSpawnChunkBlock {

    public UnstableSpawnChunkBlock(Properties blockProperties) {
        super(blockProperties);
    }

    @Override
    protected ChunkPos getSourceChunk(Level targetLevel, BlockPos targetBlockPos) {
        Random random = new Random(targetBlockPos.asLong());
        return new ChunkPos(random.nextInt(Short.MIN_VALUE, Short.MAX_VALUE), random.nextInt(Short.MIN_VALUE, Short.MAX_VALUE));
    }


}
