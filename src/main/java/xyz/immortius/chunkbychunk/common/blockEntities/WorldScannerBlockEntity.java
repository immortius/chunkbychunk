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
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ChunkHolder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.StackedContents;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkStatus;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.material.MaterialColor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;
import xyz.immortius.chunkbychunk.common.menus.WorldScannerMenu;
import xyz.immortius.chunkbychunk.common.world.SkyChunkGenerator;
import xyz.immortius.chunkbychunk.interop.ChunkByChunkConstants;
import xyz.immortius.chunkbychunk.interop.ChunkByChunkSettings;
import xyz.immortius.chunkbychunk.interop.SidedBlockEntityInteropBase;

import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * World Scanner Block Entity - this consumes crystals in order to scan for a provided block or item hint.
 */
public class WorldScannerBlockEntity extends SidedBlockEntityInteropBase {
    private static final Logger LOGGER = LogManager.getLogger(ChunkByChunkConstants.MOD_ID);

    private static final int ENERGY_PER_CHUNK = 5;
    private static final int ENERGY_PER_TICK = 1;

    public static final int SLOT_INPUT = 0;
    public static final int SLOT_FUEL = 1;
    public static final int NUM_ITEM_SLOTS = 2;

    public static final int DATA_ENERGY = 0;
    public static final int DATA_MAX_ENERGY = 1;
    public static final int DATA_SCANNING_X = 2;
    public static final int DATA_SCANNING_Z = 3;
    public static final int DATA_BLOCK_X = 4;
    public static final int DATA_BLOCK_Y = 5;
    public static final int DATA_BLOCK_Z = 6;
    public static final int NUM_DATA_ITEMS = 7;

    public static final int SCAN_RANGE = 74;
    public static final int MAP_DIMENSION = 2 * SCAN_RANGE + 1;

    private static final Map<Item, Integer> FUEL;

    private static final int[] SLOTS_FOR_UP = new int[]{SLOT_INPUT};
    private static final int[] SLOTS_FOR_SIDES = new int[]{SLOT_FUEL};
    private static final int[] SLOTS_FOR_DOWN = new int[]{SLOT_FUEL};

    private static final MaterialColor[] SCAN_COLOR_RANGE = new MaterialColor[] { MaterialColor.COLOR_BLACK, MaterialColor.COLOR_RED, MaterialColor.COLOR_ORANGE, MaterialColor.COLOR_YELLOW, MaterialColor.SNOW };

    private NonNullList<ItemStack> items = NonNullList.withSize(NUM_ITEM_SLOTS, ItemStack.EMPTY);

    private byte[] map = new byte[MAP_DIMENSION * MAP_DIMENSION];
    private int energy;
    private int maxEnergy;
    private int scanX = 0;
    private int scanZ = 0;
    private int scanCharge = 0;

    private CompletableFuture<Either<ChunkAccess, ChunkHolder.ChunkLoadingFailure>> chunkFuture;

    protected final ContainerData dataAccess = new ContainerData() {
        public int get(int id) {
            return switch (id) {
                case DATA_ENERGY -> energy;
                case DATA_MAX_ENERGY -> maxEnergy;
                case DATA_SCANNING_X -> scanX;
                case DATA_SCANNING_Z -> scanZ;
                case DATA_BLOCK_X -> getBlockPos().getX();
                case DATA_BLOCK_Y -> getBlockPos().getY();
                case DATA_BLOCK_Z -> getBlockPos().getZ();
                default -> 0;
            };
        }

        public void set(int id, int value) {
            switch (id) {
                case DATA_ENERGY -> energy = value;
                case DATA_MAX_ENERGY -> maxEnergy = value;
                case DATA_SCANNING_X ->  scanX = value;
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
        clearMap();
    }

    private void clearMap() {
        Arrays.fill(map, MaterialColor.COLOR_BLACK.getPackedId(MaterialColor.Brightness.NORMAL));
    }

    @Override
    protected Component getDefaultName() {
        return new TranslatableComponent("container.chunkbychunk.worldscanner");
    }

    @Override
    protected AbstractContainerMenu createMenu(int menuId, Inventory inventory) {
        return new WorldScannerMenu(menuId, inventory, this, this.dataAccess);
    }

    public byte[] getMap() {
        return map;
    }

    @Nullable
    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public CompoundTag getUpdateTag() {
        LOGGER.info("Writing map");
        CompoundTag tag = super.getUpdateTag();
        saveAdditional(tag);
        return tag;
    }

    private void iterateScanPosition() {
        int dist = Math.max(Math.abs(scanX), Math.abs(scanZ));
        if (dist == 0) {
            // Origin moves north
            scanZ--;
        } else if (scanZ == -dist) {
            // Scanning along north side
            if (scanX == -1) {
                // Spiral complete, move north
                scanX++;
                scanZ--;
            } else if (scanX == dist) {
                // Begin southward scan
                scanZ++;
            } else {
                scanX++;
            }
        } else if (scanX == dist) {
            // Scanning along east side
            if (scanZ == dist) {
                // Begin westward scan
                scanX--;
            } else {
                scanZ++;
            }
        } else if (scanZ == dist) {
            // Scanning along south side
            if (scanX == -dist) {
                scanZ--;
            } else {
                scanX--;
            }
        } else {
            scanZ--;
        }
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        this.items = NonNullList.withSize(this.getContainerSize(), ItemStack.EMPTY);
        ContainerHelper.loadAllItems(tag, this.items);
        map = tag.getByteArray("Map");
        energy = tag.getInt("Energy");
        maxEnergy = tag.getInt("MaxEnergy");
        scanX = tag.getInt("CurrentChunkX");
        scanZ = tag.getInt("CurrentChunkZ");
        scanCharge = tag.getInt("ScanCharge");

    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putByteArray("Map", map);
        tag.putInt("Energy", energy);
        tag.putInt("MaxEnergy", maxEnergy);
        tag.putInt("CurrentChunkX", scanX);
        tag.putInt("CurrentChunkZ", scanZ);
        tag.putInt("ScanCharge", scanCharge);
        ContainerHelper.saveAllItems(tag, this.items);
    }

    public static void serverTick(Level level, BlockPos blockPos, BlockState blockState, WorldScannerBlockEntity entity) {
        boolean changed = false;
        int dist = Math.max(Math.abs(entity.scanX), Math.abs(entity.scanZ));
        if (dist < SCAN_RANGE) {
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
                CompletableFuture<Either<ChunkAccess, ChunkHolder.ChunkLoadingFailure>> chunkFuture = entity.chunkFuture;
                if (chunkFuture == null) {
                    ChunkPos originChunkPos = new ChunkPos(blockPos);
                    int chunkX = entity.scanX + originChunkPos.x;
                    int chunkZ = entity.scanZ + originChunkPos.z;

                    ServerLevel serverLevel = (ServerLevel) level;
                    if (serverLevel.getChunkSource().getGenerator() instanceof SkyChunkGenerator) {
                        chunkFuture = serverLevel.getServer().getLevel(ChunkByChunkConstants.SKY_CHUNK_GENERATION_LEVEL).getChunkSource().getChunkFuture(chunkX, chunkZ, ChunkStatus.FULL, true);
                    } else {
                        chunkFuture = serverLevel.getChunkSource().getChunkFuture(chunkX, chunkZ, ChunkStatus.FULL, true);
                    }
                }
                if (chunkFuture.isDone()) {
                    entity.chunkFuture = null;
                    ChunkAccess chunk = chunkFuture.getNow(null).left().orElse(null);
                    if (chunk != null) {
                        ChunkPos chunkPos = chunk.getPos();

//                        BlockState state = Blocks.BEDROCK.defaultBlockState();
//                        for (int y = chunk.getMaxBuildHeight(); y >= chunk.getMinBuildHeight(); y--) {
//                            state = chunk.getBlockState(new BlockPos(chunkPos.getMiddleBlockX(), y, chunkPos.getMiddleBlockZ()));
//                            if (!state.isAir()) {
//                                break;
//                            }
//                        }
                        int lava = 0;
                        for (int x = chunkPos.getMinBlockX(); x <= chunkPos.getMaxBlockX(); x++) {
                            for (int y = chunk.getMinBuildHeight(); y <= chunk.getMaxBuildHeight(); y++) {
                                for (int z = chunkPos.getMinBlockZ(); z <= chunkPos.getMaxBlockZ(); z++) {
                                    BlockState state = chunk.getBlockState(new BlockPos(x, y, z));
                                    if (state.getBlock() == Blocks.LAVA) {
                                        lava++;
                                    }
                                }
                            }
                        }
                        MaterialColor color;
                        if (lava == 0) {
                            color = SCAN_COLOR_RANGE[0];
                        } else if (lava < 9) {
                            color = SCAN_COLOR_RANGE[1];
                        } else if (lava < 33) {
                           color = SCAN_COLOR_RANGE[2];
                        } else if (lava < 129) {
                            color = SCAN_COLOR_RANGE[3];
                        } else {
                            color = SCAN_COLOR_RANGE[4];
                        }
                        entity.map[scanPositionToMapIndex(entity.scanX, entity.scanZ)] = color.getPackedId(MaterialColor.Brightness.NORMAL);
                        entity.iterateScanPosition();
                        entity.scanCharge -= ENERGY_PER_CHUNK;
                        changed = true;
                    }
                }
            }
        }
        if (changed) {
            setChanged(level, blockPos, blockState);
        }
    }

    public static int scanPositionToMapIndex(int x, int z) {
        return x + SCAN_RANGE + (z + SCAN_RANGE) * MAP_DIMENSION;
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
