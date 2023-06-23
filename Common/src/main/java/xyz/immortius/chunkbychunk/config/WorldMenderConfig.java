package xyz.immortius.chunkbychunk.config;

import xyz.immortius.chunkbychunk.config.system.Comment;
import xyz.immortius.chunkbychunk.config.system.IntRange;
import xyz.immortius.chunkbychunk.config.system.Name;

/**
 * Configuration specifically for the world forge
 */
public class WorldMenderConfig {
    @Name("cooldown")
    @IntRange(min = 1, max = 72000)
    @Comment("Ticks between chunk spawns per world mender (world menders will not spawn chunks if chunks are already being spawned)")
    private int cooldown = 1;

    public int getCooldown() {
        return cooldown;
    }

    public void setCooldown(int cooldown) {
        this.cooldown = cooldown;
    }
}
