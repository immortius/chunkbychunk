package xyz.immortius.chunkbychunk.common.blockEntities;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.network.protocol.Packet;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.*;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.levelgen.WorldgenRandom;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.level.material.MaterialColor;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;
import xyz.immortius.chunkbychunk.common.menus.WorldScannerMenu;
import xyz.immortius.chunkbychunk.common.util.ChunkUtil;
import xyz.immortius.chunkbychunk.common.util.SpiralIterator;
import xyz.immortius.chunkbychunk.common.world.SkyChunkGenerator;
import xyz.immortius.chunkbychunk.config.ChunkByChunkConfig;
import xyz.immortius.chunkbychunk.interop.CBCInteropMethods;
import xyz.immortius.chunkbychunk.interop.ChunkByChunkConstants;

import java.util.*;

/**
 * World Scanner Block Entity - this consumes crystals in order to scan for a provided block or item hint.
 */
public class WorldScannerBlockEntity extends BaseFueledBlockEntity {
    public static final int SLOT_INPUT = 0;
    public static final int SLOT_FUEL = 1;
    public static final int NUM_ITEM_SLOTS = 2;

    public static final int DATA_MAP = 0;
    public static final int DATA_ENERGY = 1;
    public static final int DATA_MAX_ENERGY = 2;
    public static final int DATA_SCANNING_X = 3;
    public static final int DATA_SCANNING_Z = 4;
    public static final int NUM_DATA_ITEMS = 5;

    public static final int SCAN_CENTER = 15;
    public static final int SCAN_ZOOM = 4;

    public static final int NO_MAP = -1;

    private static final int TICKS_BETWEEN_REPLICATES = 11;

    private static final Map<Item, FuelValueSupplier> FUEL;

    private static final int[] SLOTS_FOR_UP = new int[]{SLOT_INPUT};
    private static final int[] SLOTS_FOR_SIDES = new int[]{SLOT_FUEL};
    private static final int[] SLOTS_FOR_DOWN = new int[]{SLOT_FUEL};

    private static final byte[] SCAN_COLOR_PALETTE = {
            MaterialColor.COLOR_BLACK.getPackedId(MaterialColor.Brightness.NORMAL),
            MaterialColor.NETHER.getPackedId(MaterialColor.Brightness.LOWEST),
            MaterialColor.NETHER.getPackedId(MaterialColor.Brightness.LOW),
            MaterialColor.NETHER.getPackedId(MaterialColor.Brightness.NORMAL),
            MaterialColor.NETHER.getPackedId(MaterialColor.Brightness.HIGH),
            MaterialColor.COLOR_RED.getPackedId(MaterialColor.Brightness.LOWEST),
            MaterialColor.COLOR_RED.getPackedId(MaterialColor.Brightness.LOW),
            MaterialColor.COLOR_RED.getPackedId(MaterialColor.Brightness.NORMAL),
            MaterialColor.COLOR_RED.getPackedId(MaterialColor.Brightness.HIGH),
            MaterialColor.TERRACOTTA_YELLOW.getPackedId(MaterialColor.Brightness.HIGH),
            MaterialColor.COLOR_YELLOW.getPackedId(MaterialColor.Brightness.HIGH),
            MaterialColor.GOLD.getPackedId(MaterialColor.Brightness.HIGH),
            MaterialColor.SNOW.getPackedId(MaterialColor.Brightness.HIGH)
    };

    private static final Multimap<Item, Block> SCAN_ITEM_MAPPINGS = ImmutableMultimap.<Item, Block>builder()
            .putAll(Items.RAW_GOLD, Blocks.GOLD_ORE, Blocks.DEEPSLATE_GOLD_ORE, Blocks.NETHER_GOLD_ORE, Blocks.RAW_GOLD_BLOCK)
            .putAll(Items.RAW_IRON, Blocks.IRON_ORE, Blocks.DEEPSLATE_IRON_ORE, Blocks.RAW_IRON_BLOCK)
            .putAll(Items.RAW_COPPER, Blocks.COPPER_ORE, Blocks.DEEPSLATE_COPPER_ORE, Blocks.RAW_COPPER_BLOCK)
            .putAll(Items.DIAMOND, Blocks.DIAMOND_ORE, Blocks.DEEPSLATE_DIAMOND_ORE)
            .putAll(Items.EMERALD, Blocks.EMERALD_ORE, Blocks.DEEPSLATE_EMERALD_ORE)
            .putAll(Items.COAL, Blocks.COAL_ORE, Blocks.DEEPSLATE_COAL_ORE)
            .putAll(Items.LAPIS_LAZULI, Blocks.LAPIS_ORE, Blocks.DEEPSLATE_LAPIS_ORE)
            .putAll(Items.REDSTONE, Blocks.REDSTONE_ORE, Blocks.DEEPSLATE_REDSTONE_ORE)
            .build();

    private static final int[] SCAN_COLOR_THRESHOLD = {0, 1, 8, 32, 64, 128, 256, 512, 1024, 2048, 4096, 8192, 16384};

    private int map = NO_MAP;
    private int scanCharge = 0;
    private final SpiralIterator scanIterator = new SpiralIterator();
    private int tickUntilReplicate = 0;

    protected final ContainerData dataAccess = new ContainerData() {
        public int get(int id) {
            return switch (id) {
                case DATA_MAP -> map;
                case DATA_ENERGY -> getRemainingFuel();
                case DATA_MAX_ENERGY -> getChargedFuel();
                case DATA_SCANNING_X -> scanIterator.getX();
                case DATA_SCANNING_Z -> scanIterator.getY();
                default -> 0;
            };
        }

        public void set(int id, int value) {
            switch (id) {
                case DATA_MAP -> map = value;
            }
        }

        public int getCount() {
            return NUM_DATA_ITEMS;
        }
    };

    static {
        FUEL = ImmutableMap.<Item, FuelValueSupplier>builder()
                .put(ChunkByChunkConstants.worldFragmentItem(), () -> ChunkByChunkConfig.get().getWorldScannerConfig().getFuelPerFragment())
                .put(ChunkByChunkConstants.worldShardItem(), () -> 4 * ChunkByChunkConfig.get().getWorldScannerConfig().getFuelPerFragment())
                .put(ChunkByChunkConstants.worldCrystalItem(), () -> 16 * ChunkByChunkConfig.get().getWorldScannerConfig().getFuelPerFragment())
                .put(ChunkByChunkConstants.worldCoreBlockItem(), () -> 64 * ChunkByChunkConfig.get().getWorldScannerConfig().getFuelPerFragment()).build();
    }

    public WorldScannerBlockEntity(BlockPos pos, BlockState state) {
        super(ChunkByChunkConstants.worldScannerEntity(), pos, state, NUM_ITEM_SLOTS, SLOT_FUEL, FUEL, Collections.emptyMap());
    }

    @Override
    protected Component getDefaultName() {
        return new TranslatableComponent("container.chunkbychunk.worldscanner");
    }

    @Override
    protected AbstractContainerMenu createMenu(int menuId, Inventory inventory) {
        return new WorldScannerMenu(menuId, inventory, this, this.dataAccess);
    }

    public static boolean isWorldScannerFuel(ItemStack itemStack) {
        return FUEL.getOrDefault(itemStack.getItem(), () -> 0).get() > 0;
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        map = tag.getInt("Map");
        scanIterator.load(tag.getCompound("ScanIterator"));
        scanCharge = tag.getInt("ScanCharge");
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putInt("Map", map);
        tag.put("ScanIterator", scanIterator.createTag());
        tag.putInt("ScanCharge", scanCharge);
    }

    private boolean validTarget() {
        ItemStack targetItem = getItem(SLOT_INPUT);
        if (targetItem.getItem() instanceof BucketItem bucket) {
            return CBCInteropMethods.getBucketContents(bucket) instanceof FlowingFluid;
        } else if (Items.SLIME_BALL.equals(targetItem.getItem())) {
            return true;
        }
        return targetItem.getItem() instanceof BlockItem || SCAN_ITEM_MAPPINGS.keySet().contains(targetItem.getItem());
    }

    public static void serverTick(Level level, BlockPos blockPos, BlockState blockState, WorldScannerBlockEntity entity) {
        ServerLevel serverLevel = (ServerLevel) level;
        boolean changed = false;

        // If we haven't finished the scan and the target is valid
        if (entity.scanIterator.getX() >= 0 && entity.validTarget()) {
            ItemStack targetItem = entity.getItem(SLOT_INPUT);

            // Process fuel
            if (entity.getRemainingFuel() > 0) {
                int consumeAmount = entity.consumeFuel(ChunkByChunkConfig.get().getWorldScannerConfig().getFuelConsumedPerTick());
                entity.scanCharge += consumeAmount;
            }
            changed = entity.checkConsumeFuelItem();

            // If there is enough charge to scan a chunk, do so
            int chunkCost = ChunkByChunkConfig.get().getWorldScannerConfig().getFuelRequiredPerChunk();
            if (entity.scanCharge >= chunkCost) {
                if (entity.map == NO_MAP) {
                    entity.createMap();
                }

                // Get the chunk to scan
                ChunkPos originChunkPos = new ChunkPos(blockPos);
                int chunkX = entity.scanIterator.getX() + originChunkPos.x - SCAN_CENTER;
                int chunkZ = entity.scanIterator.getY() + originChunkPos.z - SCAN_CENTER;
                ServerLevel scanLevel;
                if (serverLevel.getChunkSource().getGenerator() instanceof SkyChunkGenerator) {
                    scanLevel = serverLevel.getServer().getLevel(ChunkByChunkConstants.SKY_CHUNK_GENERATION_LEVEL);
                } else {
                    scanLevel = serverLevel;
                }
                ChunkAccess chunk = scanLevel.getChunk(chunkX, chunkZ);

                int blockCount;
                // Count the targeted blocks
                if (targetItem.getItem().equals(Items.SLIME_BALL) || targetItem.getItem().equals(Items.SLIME_BLOCK)) {
                    if (WorldgenRandom.seedSlimeChunk(chunkX, chunkZ, ((WorldGenLevel) scanLevel).getSeed(), 987234911L).nextInt(10) == 0) {
                        blockCount = 20000;
                    } else {
                        blockCount = 0;
                    }
                } else {
                    Set<Block> scanForBlocks = new HashSet<>();
                    Collection<Block> mappings = SCAN_ITEM_MAPPINGS.get(targetItem.getItem());
                    if (!mappings.isEmpty()) {
                        scanForBlocks.addAll(mappings);
                    } else if (targetItem.getItem() instanceof BucketItem bucket) {
                        scanForBlocks.add(CBCInteropMethods.getBucketContents(bucket).defaultFluidState().createLegacyBlock().getBlock());
                    } else if (targetItem.getItem() instanceof BlockItem blockItem) {
                        scanForBlocks.add(blockItem.getBlock());
                    }
                    blockCount = ChunkUtil.countBlocks(chunk, scanForBlocks);
                }

                // Set the color based on the count
                byte color = MaterialColor.COLOR_BLACK.getPackedId(MaterialColor.Brightness.NORMAL);
                for (int i = 0; i < SCAN_COLOR_THRESHOLD.length; i++) {
                    color = SCAN_COLOR_PALETTE[i];
                    if (blockCount <= SCAN_COLOR_THRESHOLD[i]) {
                        break;
                    }
                }

                // Update the map
                MapItemSavedData data = entity.getLevel().getMapData(MapItem.makeKey(entity.map));
                for (int innerX = 0; innerX < SCAN_ZOOM; innerX++) {
                    for (int innerZ = 0; innerZ < SCAN_ZOOM; innerZ++) {
                        int pixelX = entity.scanIterator.getX() * SCAN_ZOOM + innerX;
                        int pixelY = entity.scanIterator.getY() * SCAN_ZOOM + innerZ;
                        data.setColor(pixelX, pixelY, color);
                    }
                }

                entity.scanIterator.next();
                entity.scanCharge -= chunkCost;
                changed = true;
            }
        }
        if (changed) {
            setChanged(level, blockPos, blockState);
        }

        // Trigger map replication
        if (changed || entity.tickUntilReplicate <= 0) {
            MapItemSavedData mapitemsaveddata = level.getMapData(MapItem.makeKey(entity.map));
            if (mapitemsaveddata != null) {
                for (ServerPlayer serverplayer : serverLevel.players()) {
                    // Add players to tracking
                    mapitemsaveddata.getHoldingPlayer(serverplayer);
                    Packet<?> packet = mapitemsaveddata.getUpdatePacket(entity.map, serverplayer);
                    if (packet != null) {
                        serverplayer.connection.send(packet);
                    }
                }
            }
            entity.tickUntilReplicate = TICKS_BETWEEN_REPLICATES;
        } else {
            entity.tickUntilReplicate--;
        }
    }

    private void createMap() {
        if (map == NO_MAP) {
            ChunkPos pos = new ChunkPos(getBlockPos());

            MapItemSavedData data = MapItemSavedData.createFresh(pos.getMaxBlockX(), pos.getMaxBlockZ(), (byte) 2, false, false, level.dimension()).locked();
            map = level.getFreeMapId();
            level.setMapData(MapItem.makeKey(map), data);
        }
    }

    @Override
    public int[] getSlotsForFace(Direction direction) {
        return switch (direction) {
            case UP -> SLOTS_FOR_UP;
            case DOWN -> SLOTS_FOR_DOWN;
            default -> SLOTS_FOR_SIDES;
        };
    }

    @Override
    public boolean canTakeItemThroughFace(int slot, ItemStack itemStack, Direction direction) {
        return slot == SLOT_INPUT;
    }

    @Override
    public void setItem(int slot, ItemStack newItem) {
        boolean targetUnchanged = true;
        if (slot == SLOT_INPUT) {
            ItemStack itemStack = this.getItem(slot);
            targetUnchanged = !newItem.isEmpty() && newItem.sameItem(itemStack);
        }

        super.setItem(slot, newItem);

        if (!targetUnchanged) {
            resetScan();
        }
    }

    private void resetScan() {
        if (map != NO_MAP) {
            MapItemSavedData data = getLevel().getMapData(MapItem.makeKey(map));
            if (data != null) {
                for (int x = 0; x < MapItem.IMAGE_WIDTH; x++) {
                    for (int y = 0; y < MapItem.IMAGE_HEIGHT; y++) {
                        data.setColor(x, y, MaterialColor.NONE.getPackedId(MaterialColor.Brightness.NORMAL));
                    }
                }
            }
        }

        scanIterator.reset(SCAN_CENTER, SCAN_CENTER);
        setChanged();
    }

}
