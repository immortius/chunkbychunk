/**
 * This package contains a simple configuration system and the ChunkByChunk specific config classes.
 * Configuration is defined as a single root class ({@link xyz.immortius.chunkbychunk.config.ChunkByChunkConfig}) which
 * defines sections by referencing other config classes that contain only values. Annotations are used to define the
 * textual name, comments, and any other restrictions for the values. The default value of the variables in the classes
 * become the default config values.
 *
 * The physical format is kept the same as forge's toml format, just for compatibility
 */
package xyz.immortius.chunkbychunk.config;

