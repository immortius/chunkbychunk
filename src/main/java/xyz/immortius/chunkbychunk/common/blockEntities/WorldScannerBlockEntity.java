package xyz.immortius.chunkbychunk.common.blockEntities;

import com.google.common.collect.ImmutableMap;
import com.mojang.datafixers.util.Either;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.network.protocol.Packet;
import net.minecraft.server.level.ChunkHolder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.StackedContents;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.*;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkStatus;
import net.minecraft.world.level.material.MaterialColor;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;
import xyz.immortius.chunkbychunk.common.menus.WorldScannerMenu;
import xyz.immortius.chunkbychunk.common.world.SkyChunkGenerator;
import xyz.immortius.chunkbychunk.interop.ChunkByChunkConstants;
import xyz.immortius.chunkbychunk.interop.SidedBlockEntityInteropBase;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

/**
 * World Scanner Block Entity - this consumes crystals in order to scan for a provided block or item hint.
 */
public class WorldScannerBlockEntity extends SidedBlockEntityInteropBase {
    private static final Logger LOGGER = LogManager.getLogger(ChunkByChunkConstants.MOD_ID);

    private static final int ENERGY_PER_CHUNK = 1;
    private static final int ENERGY_PER_TICK = 1;

    public static final int SLOT_INPUT = 0;
    public static final int SLOT_FUEL = 1;
    public static final int NUM_ITEM_SLOTS = 2;

    public static final int DATA_MAP = 0;
    public static final int DATA_ENERGY = 1;
    public static final int DATA_MAX_ENERGY = 2;
    public static final int DATA_SCANNING_X = 3;
    public static final int DATA_SCANNING_Z = 4;
    public static final int NUM_DATA_ITEMS = 5;

    public static final int SCAN_CENTER = 63;

    public static final int NO_MAP = -1;

    private static final Map<Item, Integer> FUEL;

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

    private static final int[] SCAN_COLOR_THRESHOLD = {0, 1, 8, 32, 64, 128, 256, 512, 1024, 2048, 4096, 8192, 16384};

    private NonNullList<ItemStack> items = NonNullList.withSize(NUM_ITEM_SLOTS, ItemStack.EMPTY);

    private static final int[][] SCAN_DIRECTION_OFFSET = {{1, 0}, {0, 1}, {-1, 0}, {0, -1}};
    private static final int[] SCAN_DISTANCE_INCREASE = {0, 1, 0, 1};

    private int map = NO_MAP;
    private int energy;
    private int maxEnergy;
    private int scanX = SCAN_CENTER;
    private int scanZ = SCAN_CENTER;
    private int scanCharge = 0;
    private int scanDirection = 0;
    private int scanLineLength = 1;
    private int scanLineRemaining = 1;

    private CompletableFuture<Either<ChunkAccess, ChunkHolder.ChunkLoadingFailure>> chunkFuture;

    protected final ContainerData dataAccess = new ContainerData() {
        public int get(int id) {
            return switch (id) {
                case DATA_MAP -> map;
                case DATA_ENERGY -> energy;
                case DATA_MAX_ENERGY -> maxEnergy;
                case DATA_SCANNING_X -> scanX;
                case DATA_SCANNING_Z -> scanZ;
                default -> 0;
            };
        }

        public void set(int id, int value) {
            switch (id) {
                case DATA_MAP -> map = value;
                case DATA_ENERGY -> energy = value;
                case DATA_MAX_ENERGY -> maxEnergy = value;
                case DATA_SCANNING_X -> scanX = value;
                case DATA_SCANNING_Z -> scanZ = value;
            }
        }

        public int getCount() {
            return NUM_DATA_ITEMS;
        }
    };

    static {
        FUEL = ImmutableMap.<Item, Integer>builder()
                .put(ChunkByChunkConstants.worldFragmentItem(), 20)
                .put(ChunkByChunkConstants.worldShardItem(), 40)
                .put(ChunkByChunkConstants.worldCrystalItem(), 80)
                .put(ChunkByChunkConstants.worldCoreBlockItem(), 160).build();
    }

    public WorldScannerBlockEntity(BlockPos pos, BlockState state) {
        super(ChunkByChunkConstants.worldScannerEntity(), pos, state);
    }

    @Override
    protected Component getDefaultName() {
        return new TranslatableComponent("container.chunkbychunk.worldscanner");
    }

    @Override
    protected AbstractContainerMenu createMenu(int menuId, Inventory inventory) {
        return new WorldScannerMenu(menuId, inventory, this, this.dataAccess);
    }

    private void iterateScanPosition() {
        scanX += SCAN_DIRECTION_OFFSET[scanDirection][0];
        scanZ += SCAN_DIRECTION_OFFSET[scanDirection][1];
        scanLineRemaining--;
        if (scanLineRemaining == 0) {
            scanLineLength += SCAN_DISTANCE_INCREASE[scanDirection];
            scanLineRemaining = scanLineLength;
            scanDirection = (scanDirection + 1) % SCAN_DIRECTION_OFFSET.length;
        }
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        this.items = NonNullList.withSize(this.getContainerSize(), ItemStack.EMPTY);
        ContainerHelper.loadAllItems(tag, this.items);
        map = tag.getInt("Map");
        energy = tag.getInt("Energy");
        maxEnergy = tag.getInt("MaxEnergy");
        scanX = tag.getInt("CurrentChunkX");
        scanZ = tag.getInt("CurrentChunkZ");
        scanCharge = tag.getInt("ScanCharge");
        scanDirection = tag.getInt("ScanDirection");
        scanLineLength = tag.getInt("ScanLineLength");
        scanLineRemaining = tag.getInt("ScanLineRemaining");
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putInt("Map", map);
        tag.putInt("Energy", energy);
        tag.putInt("MaxEnergy", maxEnergy);
        tag.putInt("CurrentChunkX", scanX);
        tag.putInt("CurrentChunkZ", scanZ);
        tag.putInt("ScanCharge", scanCharge);
        tag.putInt("ScanDirection", scanDirection);
        tag.putInt("ScanLineLength", scanLineLength);
        tag.putInt("ScanLineRemaining", scanLineRemaining);
        ContainerHelper.saveAllItems(tag, this.items);
    }

    public static void serverTick(Level level, BlockPos blockPos, BlockState blockState, WorldScannerBlockEntity entity) {
        ServerLevel serverLevel = (ServerLevel) level;
        boolean changed = false;
        if (entity.scanX > 0) {
            ItemStack targetItem = entity.items.get(SLOT_INPUT);
            ItemStack fuelItem = entity.items.get(SLOT_FUEL);

            if (entity.energy > 0) {
                int consumeAmount = Math.min(entity.energy, ENERGY_PER_TICK);
                entity.scanCharge += consumeAmount;
                entity.energy -= consumeAmount;
                changed = true;
            }

            if (entity.energy == 0 && isFuel(fuelItem)) {
                entity.energy = entity.maxEnergy = getFuelValue(fuelItem);
                fuelItem.shrink(1);
                changed = true;
            }

            if (entity.scanCharge >= ENERGY_PER_CHUNK) {
                ChunkPos originChunkPos = new ChunkPos(blockPos);
                int chunkX = entity.scanX + originChunkPos.x - SCAN_CENTER;
                int chunkZ = entity.scanZ + originChunkPos.z - SCAN_CENTER;

                ServerLevel scanLevel;
                if (serverLevel.getChunkSource().getGenerator() instanceof SkyChunkGenerator) {
                    scanLevel = serverLevel.getServer().getLevel(ChunkByChunkConstants.SKY_CHUNK_GENERATION_LEVEL);
                } else {
                    scanLevel = serverLevel;
                }
                if (entity.map == NO_MAP) {
                    entity.createMap();
                }

                ChunkAccess chunk = scanLevel.getChunk(chunkX, chunkZ);
                ChunkPos chunkPos = chunk.getPos();
                byte color = MaterialColor.NONE.getPackedId(MaterialColor.Brightness.NORMAL);
                Set<Block> scanForBlocks = new HashSet<>();
                if (targetItem.getItem() == Items.WATER_BUCKET) {
                    scanForBlocks.add(Blocks.WATER);
                } else if (targetItem.getItem() == Items.LAVA_BUCKET) {
                    scanForBlocks.add(Blocks.LAVA);
                } else if (targetItem.getItem() == Items.POWDER_SNOW_BUCKET) {
                    scanForBlocks.add(Blocks.POWDER_SNOW);
                } else if (targetItem.getItem() instanceof BlockItem blockItem) {
                    scanForBlocks.add(blockItem.getBlock());
                } else if (targetItem.getItem() == Items.RAW_GOLD) {
                    scanForBlocks.add(Blocks.RAW_GOLD_BLOCK);
                    scanForBlocks.add(Blocks.DEEPSLATE_GOLD_ORE);
                    scanForBlocks.add(Blocks.NETHER_GOLD_ORE);
                } else if (targetItem.getItem() == Items.RAW_IRON) {
                    scanForBlocks.add(Blocks.RAW_IRON_BLOCK);
                    scanForBlocks.add(Blocks.DEEPSLATE_IRON_ORE);
                } else if (targetItem.getItem() == Items.DIAMOND) {
                    scanForBlocks.add(Blocks.DIAMOND_ORE);
                    scanForBlocks.add(Blocks.DEEPSLATE_DIAMOND_ORE);
                } else if (targetItem.getItem() == Items.RAW_COPPER) {
                    scanForBlocks.add(Blocks.COPPER_ORE);
                } else if (targetItem.getItem() == Items.EMERALD) {
                    scanForBlocks.add(Blocks.EMERALD_ORE);
                    scanForBlocks.add(Blocks.DEEPSLATE_EMERALD_ORE);
                } else if (targetItem.getItem() == Items.COAL) {
                    scanForBlocks.add(Blocks.COAL_ORE);
                    scanForBlocks.add(Blocks.DEEPSLATE_COAL_ORE);
                } else if (targetItem.getItem() == Items.LAPIS_LAZULI) {
                    scanForBlocks.add(Blocks.LAPIS_ORE);
                    scanForBlocks.add(Blocks.DEEPSLATE_LAPIS_ORE);
                } else if (targetItem.getItem() == Items.REDSTONE) {
                    scanForBlocks.add(Blocks.REDSTONE_ORE);
                    scanForBlocks.add(Blocks.DEEPSLATE_REDSTONE_ORE);
                }
                if (!scanForBlocks.isEmpty()) {
                    BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos(0, 0, 0);
                    int blockCount = 0;
                    for (pos.setX(chunkPos.getMinBlockX()); pos.getX() <= chunkPos.getMaxBlockX(); pos.setX(pos.getX() + 1)) {
                        for (pos.setY(chunk.getMinBuildHeight()); pos.getY() <= chunk.getMaxBuildHeight(); pos.setY(pos.getY() + 1)) {
                            for (pos.setZ(chunkPos.getMinBlockZ()); pos.getZ() <= chunkPos.getMaxBlockZ(); pos.setZ(pos.getZ() + 1)) {
                                BlockState state = chunk.getBlockState(pos);
                                if (scanForBlocks.contains(state.getBlock())) {
                                    blockCount++;
                                }
                            }
                        }
                    }
                    for (int i = 0; i < SCAN_COLOR_THRESHOLD.length; i++) {
                        color = SCAN_COLOR_PALETTE[i];
                        if (blockCount <= SCAN_COLOR_THRESHOLD[i]) {
                            break;
                        }
                    }
                } else {
                    BlockState state = Blocks.BEDROCK.defaultBlockState();
                    for (int y = chunk.getMaxBuildHeight(); y >= chunk.getMinBuildHeight(); y--) {
                        state = chunk.getBlockState(new BlockPos(chunkPos.getMiddleBlockX(), y, chunkPos.getMiddleBlockZ()));
                        if (!state.isAir()) {
                            break;
                        }
                    }
                    color = state.getMapColor(level, blockPos).getPackedId(MaterialColor.Brightness.NORMAL);
                }

                MapItemSavedData data = entity.getLevel().getMapData(MapItem.makeKey(entity.map));
                data.setColor(entity.scanX, entity.scanZ, color);

                entity.iterateScanPosition();
                entity.scanCharge -= ENERGY_PER_CHUNK;
                changed = true;
            }
        }
        if (changed) {
            setChanged(level, blockPos, blockState);
        }

        // TODO: On changed or infrequently
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
    }

    private void createMap() {
        if (map == NO_MAP) {
            MapItemSavedData data = MapItemSavedData.createFresh(getBlockPos().getX(), getBlockPos().getY(), (byte) 4, true, true, level.dimension());
            map = level.getFreeMapId();
            level.setMapData(MapItem.makeKey(map), data);
        }
    }

    public static boolean isFuel(ItemStack itemStack) {
        return getFuelValue(itemStack) > 0;
    }

    public static int getFuelValue(ItemStack itemStack) {
        return FUEL.getOrDefault(itemStack.getItem(), 0);
    }

    @Override
    public int[] getSlotsForFace(Direction direction) {
        switch (direction) {
            case UP:
                return SLOTS_FOR_UP;
            case DOWN:
                return SLOTS_FOR_DOWN;
            default:
                return SLOTS_FOR_SIDES;
        }
    }

    @Override
    public boolean canPlaceItemThroughFace(int slot, ItemStack itemStack, @Nullable Direction p_19237_) {
        return canPlaceItem(slot, itemStack);
    }

    @Override
    public boolean canTakeItemThroughFace(int slot, ItemStack itemStack, Direction direction) {
        return slot == SLOT_INPUT;
    }

    @Override
    public int getContainerSize() {
        return this.items.size();
    }

    @Override
    public boolean isEmpty() {
        for (ItemStack itemstack : items) {
            if (!itemstack.isEmpty()) {
                return false;
            }
        }
        return true;
    }

    @Override
    public ItemStack getItem(int slot) {
        return items.get(slot);
    }

    @Override
    public ItemStack removeItem(int slot, int split) {
        return ContainerHelper.removeItem(items, slot, split);
    }

    @Override
    public ItemStack removeItemNoUpdate(int slot) {
        return ContainerHelper.takeItem(this.items, slot);
    }

    @Override
    public void setItem(int slot, ItemStack newItem) {
        items.set(slot, newItem);
        if (newItem.getCount() > this.getMaxStackSize()) {
            newItem.setCount(this.getMaxStackSize());
        }
    }

    @Override
    public boolean stillValid(Player player) {
        if (level.getBlockEntity(worldPosition) != this) {
            return false;
        } else {
            return player.distanceToSqr((double) this.worldPosition.getX() + 0.5D, (double) this.worldPosition.getY() + 0.5D, (double) this.worldPosition.getZ() + 0.5D) <= 64.0D;
        }
    }

    @Override
    public void clearContent() {
        items.clear();
    }

    @Override
    public void fillStackedContents(StackedContents contents) {
        for (ItemStack itemstack : items) {
            contents.accountStack(itemstack);
        }
    }

    @Override
    public boolean canPlaceItem(int slot, ItemStack item) {
        return slot == SLOT_INPUT || isFuel(item);
    }

}
