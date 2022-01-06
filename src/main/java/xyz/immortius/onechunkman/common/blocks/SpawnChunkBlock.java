package xyz.immortius.onechunkman.common.blocks;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.mojang.datafixers.util.Unit;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ChunkHolder;
import net.minecraft.server.level.ServerChunkCache;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.thread.ProcessorMailbox;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkStatus;
import net.minecraft.world.level.chunk.ImposterProtoChunk;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.BlockHitResult;
import xyz.immortius.onechunkman.common.world.OneChunkGenerator;

import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 *
 */
public class SpawnChunkBlock extends Block {

    public SpawnChunkBlock(Properties blockProperties) {
        super(blockProperties);
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (level.isClientSide()) {
            return InteractionResult.SUCCESS;
        }
        BlockPos bedrockCheckBlock = new ChunkPos(pos).getBlockAt(8, -64, 8);

        if (!Blocks.BEDROCK.equals(level.getBlockState(bedrockCheckBlock).getBlock())) {
            if (level instanceof ServerLevel) {
                ServerChunkCache serverChunkCache = (ServerChunkCache) level.getChunkSource();
                if (serverChunkCache.getGenerator() instanceof OneChunkGenerator generator) {
                    ChunkPos chunkPos = new ChunkPos(pos);
                    generator.allowChunk(chunkPos);
                    reloadChunk((ServerLevel) level, chunkPos);
                } else {
                    ServerLevel overworldLevel = level.getServer().getLevel(Level.OVERWORLD);
                    copyChunk(overworldLevel, (ServerLevel) level, new ChunkPos(pos));
                }

            }
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.PASS;
    }

    private void copyChunk(ServerLevel from, ServerLevel to, ChunkPos chunkPos)
    {
        BlockPos.betweenClosed(chunkPos.getMinBlockX(), from.getMinBuildHeight(), chunkPos.getMinBlockZ(), chunkPos.getMaxBlockX(), from.getMaxBuildHeight(), chunkPos.getMaxBlockZ()).forEach(pos -> {
            to.setBlock(pos, from.getBlockState(pos), Block.UPDATE_ALL);
        });
    }

    private void createChunk(ServerLevel level, ServerLevel overworldLevel, ChunkPos spawnChunkPos) {
        ProcessorMailbox<Runnable> processormailbox = ProcessorMailbox.create(Util.backgroundExecutor(), "worldgen-resetchunks");

        for (ChunkStatus chunkstatus : ImmutableList.of(ChunkStatus.EMPTY, ChunkStatus.STRUCTURE_STARTS, ChunkStatus.STRUCTURE_REFERENCES, ChunkStatus.BIOMES, ChunkStatus.NOISE, ChunkStatus.SURFACE, ChunkStatus.CARVERS, ChunkStatus.LIQUID_CARVERS, ChunkStatus.FEATURES, ChunkStatus.LIGHT, ChunkStatus.SPAWN, ChunkStatus.HEIGHTMAPS, ChunkStatus.FULL)) {
            CompletableFuture<Unit> completablefuture = CompletableFuture.supplyAsync(() -> {
                return Unit.INSTANCE;
            }, processormailbox::tell);

            List<ChunkAccess> list = Lists.newArrayList();
            int regenRange = Math.max(1, chunkstatus.getRange());

            for (int z = spawnChunkPos.z - regenRange; z <= spawnChunkPos.z + regenRange; ++z) {
                for (int x = spawnChunkPos.x - regenRange; x <= spawnChunkPos.x + regenRange; ++x) {
                    ChunkAccess chunkaccess;
                    if (x != spawnChunkPos.x || z != spawnChunkPos.z) {
                        chunkaccess = overworldLevel.getChunk(x, z, chunkstatus.getParent(), true);
                    } else {
                        chunkaccess = level.getChunk(x, z, chunkstatus.getParent(), true);
                    }
                    ChunkAccess chunkaccess1;
                    if (chunkaccess instanceof ImposterProtoChunk) {
                        chunkaccess1 = new ImposterProtoChunk(((ImposterProtoChunk) chunkaccess).getWrapped(), true);
                    } else if (chunkaccess instanceof LevelChunk) {
                        chunkaccess1 = new ImposterProtoChunk((LevelChunk) chunkaccess, true);
                    } else {
                        chunkaccess1 = chunkaccess;
                    }

                    list.add(chunkaccess1);
                }
            }

            completablefuture = completablefuture.thenComposeAsync((unit) -> {
                return chunkstatus.generate(processormailbox::tell, level, overworldLevel.getChunkSource().getGenerator(), level.getStructureManager(), level.getChunkSource().getLightEngine(), (chunkAccess) -> {
                    throw new UnsupportedOperationException("Not creating full chunks here");
                }, list, true).thenApply((optionalChunkAccess) -> {
                    if (chunkstatus == ChunkStatus.NOISE) {
                        optionalChunkAccess.left().ifPresent((access) -> {
                            Heightmap.primeHeightmaps(access, ChunkStatus.POST_FEATURES);
                        });
                    }

                    return Unit.INSTANCE;
                });
            }, processormailbox::tell);
            level.getServer().managedBlock(completablefuture::isDone);
        }

        for (int z = spawnChunkPos.z - 1; z <= spawnChunkPos.z + 1; ++z) {
            for (int x = spawnChunkPos.x - 1; x <= spawnChunkPos.x + 1; ++x) {
                ChunkPos updatedChunkPos = new ChunkPos(x, z);
                LevelChunk levelchunk2 = level.getChunkSource().getChunk(x, z, false);
                if (levelchunk2 != null) {
                    for (BlockPos blockpos1 : BlockPos.betweenClosed(updatedChunkPos.getMinBlockX(), level.getMinBuildHeight(), updatedChunkPos.getMinBlockZ(), updatedChunkPos.getMaxBlockX(), level.getMaxBuildHeight() - 1, updatedChunkPos.getMaxBlockZ())) {
                        level.getChunkSource().blockChanged(blockpos1);
                    }
                }
            }
        }

    }

    private void reloadChunk(ServerLevel serverlevel, ChunkPos spawnChunkPos) {
        ServerChunkCache serverchunkcache = serverlevel.getChunkSource();
        serverchunkcache.chunkMap.debugReloadGenerator();

        for (BlockPos blockpos : BlockPos.betweenClosed(spawnChunkPos.getMinBlockX(), serverlevel.getMinBuildHeight(), spawnChunkPos.getMinBlockZ(), spawnChunkPos.getMaxBlockX(), serverlevel.getMaxBuildHeight() - 1, spawnChunkPos.getMaxBlockZ())) {
            serverlevel.setBlock(blockpos, Blocks.AIR.defaultBlockState(), Block.UPDATE_KNOWN_SHAPE);
        }

        ProcessorMailbox<Runnable> processormailbox = ProcessorMailbox.create(Util.backgroundExecutor(), "worldgen-resetchunks");

        for (ChunkStatus chunkstatus : ImmutableList.of(ChunkStatus.STRUCTURE_STARTS, ChunkStatus.STRUCTURE_REFERENCES, ChunkStatus.BIOMES, ChunkStatus.NOISE, ChunkStatus.SURFACE, ChunkStatus.CARVERS, ChunkStatus.LIQUID_CARVERS, ChunkStatus.FEATURES, ChunkStatus.LIGHT, ChunkStatus.SPAWN, ChunkStatus.HEIGHTMAPS, ChunkStatus.FULL)) {
            CompletableFuture<Unit> completablefuture = CompletableFuture.supplyAsync(() -> {
                return Unit.INSTANCE;
            }, processormailbox::tell);

            LevelChunk levelchunk1 = serverchunkcache.getChunk(spawnChunkPos.x, spawnChunkPos.z, false);
            if (levelchunk1 != null) {
                List<ChunkAccess> list = Lists.newArrayList();
                int regenRange = Math.max(1, chunkstatus.getRange());

                for (int z = spawnChunkPos.z - regenRange; z <= spawnChunkPos.z + regenRange; ++z) {
                    for (int x = spawnChunkPos.x - regenRange; x <= spawnChunkPos.x + regenRange; ++x) {
                        ChunkAccess chunkaccess = serverchunkcache.getChunk(x, z, chunkstatus.getParent(), true);
                        ChunkAccess chunkaccess1;
                        if (chunkaccess instanceof ImposterProtoChunk) {
                            chunkaccess1 = new ImposterProtoChunk(((ImposterProtoChunk) chunkaccess).getWrapped(), true);
                        } else if (chunkaccess instanceof LevelChunk) {
                            chunkaccess1 = new ImposterProtoChunk((LevelChunk) chunkaccess, true);
                        } else {
                            chunkaccess1 = chunkaccess;
                        }

                        list.add(chunkaccess1);
                    }
                }

                completablefuture = completablefuture.thenComposeAsync((unit) -> {
                    return chunkstatus.generate(processormailbox::tell, serverlevel, serverchunkcache.getGenerator(), serverlevel.getStructureManager(), serverchunkcache.getLightEngine(), (chunkAccess) -> {
                        throw new UnsupportedOperationException("Not creating full chunks here");
                    }, list, true).thenApply((optionalChunkAccess) -> {
                        if (chunkstatus == ChunkStatus.NOISE) {
                            optionalChunkAccess.left().ifPresent((access) -> {
                                Heightmap.primeHeightmaps(access, ChunkStatus.POST_FEATURES);
                            });
                        }

                        return Unit.INSTANCE;
                    });
                }, processormailbox::tell);
            }
            serverlevel.getServer().managedBlock(completablefuture::isDone);
        }

        for (int z = spawnChunkPos.z - 1; z <= spawnChunkPos.z + 1; ++z) {
            for (int x = spawnChunkPos.x - 1; x <= spawnChunkPos.x + 1; ++x) {
                ChunkPos updatedChunkPos = new ChunkPos(x, z);
                LevelChunk levelchunk2 = serverchunkcache.getChunk(x, z, false);
                if (levelchunk2 != null) {
                    for (BlockPos blockpos1 : BlockPos.betweenClosed(updatedChunkPos.getMinBlockX(), serverlevel.getMinBuildHeight(), updatedChunkPos.getMinBlockZ(), updatedChunkPos.getMaxBlockX(), serverlevel.getMaxBuildHeight() - 1, updatedChunkPos.getMaxBlockZ())) {
                        serverchunkcache.blockChanged(blockpos1);
                    }
                }
            }
        }

    }
}
