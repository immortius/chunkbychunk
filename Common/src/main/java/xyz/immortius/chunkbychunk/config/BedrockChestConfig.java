package xyz.immortius.chunkbychunk.config;

import xyz.immortius.chunkbychunk.config.system.Comment;
import xyz.immortius.chunkbychunk.config.system.IntRange;
import xyz.immortius.chunkbychunk.config.system.Name;

/**
 * Bedrock Chest configuration
 */
public class BedrockChestConfig {
    @Name("bedrock_chest_unlock_at_blocks_remaining")
    @Comment("The number of blocks within the chunk above the bedrock chest allowed to remain before it will open")
    @IntRange(min = 0, max = Short.MAX_VALUE * 2)
    private int bedrockChestBlocksRemainingThreshold = 16;

    public int getBedrockChestBlocksRemainingThreshold() {
        return bedrockChestBlocksRemainingThreshold;
    }

    public void setBedrockChestBlocksRemainingThreshold(int bedrockChestBlocksRemainingThreshold) {
        this.bedrockChestBlocksRemainingThreshold = bedrockChestBlocksRemainingThreshold;
    }
}
