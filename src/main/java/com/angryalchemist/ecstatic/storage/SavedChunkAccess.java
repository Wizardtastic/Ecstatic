package com.angryalchemist.ecstatic.storage;

import com.angryalchemist.ecstatic.Constants;
import com.mojang.serialization.Codec;
import java.io.DataInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.NbtOps;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.PalettedContainer;
import net.minecraft.world.level.chunk.PalettedContainerRO;
import net.minecraft.world.level.chunk.PalettedContainer.Strategy;
import net.minecraft.world.level.chunk.storage.RegionFile;
import net.minecraft.world.level.chunk.storage.RegionStorageInfo;
import net.minecraft.world.level.Level;

public final class SavedChunkAccess implements AutoCloseable {
    private static final int MAX_OPEN_REGION_FILES = 256;
    private static final int SECTION_SIZE = 16;
    private static final int BIOME_SECTION_SIZE = 4;
    private static final String INVALID_STRUCTURE_ID = "INVALID";
    private static final Codec<PalettedContainer<BlockState>> BLOCK_STATE_CODEC = PalettedContainer.codecRW(
        Block.BLOCK_STATE_REGISTRY, BlockState.CODEC, Strategy.SECTION_STATES, Blocks.AIR.defaultBlockState()
    );
    private final Path regionFolder;
    private final Codec<PalettedContainerRO<Holder<Biome>>> biomeCodec;
    private final Map<Long, RegionFile> openRegionFiles = new LinkedHashMap<>(16, 0.75F, true);
    private final Map<Long, CompoundTag> rawTagCache = new LinkedHashMap<>(16, 0.75F, true);
    private final Map<Long, SavedChunkAccess.ChunkSections> sectionsCache = new LinkedHashMap<>(16, 0.75F, true);
    private static final byte NBT_TAG_COMPOUND = 10;

    public SavedChunkAccess(Path dimensionRoot, Registry<Biome> biomeRegistry) {
        this.regionFolder = dimensionRoot.resolve("region");
        this.biomeCodec = PalettedContainer.codecRO(
            biomeRegistry.asHolderIdMap(), biomeRegistry.holderByNameCodec(), Strategy.SECTION_BIOMES, biomeRegistry.getHolderOrThrow(Biomes.PLAINS)
        );
    }

    public boolean hasChunk(int chunkX, int chunkZ) {
        RegionFile regionFile = this.openRegionFile(chunkX, chunkZ);
        return regionFile != null && regionFile.hasChunk(new ChunkPos(chunkX, chunkZ));
    }

    public boolean hasStructure(int chunkX, int chunkZ) {
        return !this.structureIds(chunkX, chunkZ).isEmpty();
    }

    public SavedChunkAccess.StructureFootprint structureFootprint(int chunkX, int chunkZ) {
        CompoundTag chunkTag = this.rawTag(chunkX, chunkZ);
        if (chunkTag == null) {
            return null;
        }

        CompoundTag starts = chunkTag.getCompound("structures").getCompound("starts");
        Set<String> ids = new HashSet<>();
        int minX = Integer.MAX_VALUE;
        int minY = Integer.MAX_VALUE;
        int minZ = Integer.MAX_VALUE;
        int maxX = Integer.MIN_VALUE;
        int maxY = Integer.MIN_VALUE;
        int maxZ = Integer.MIN_VALUE;

        for (String key : starts.getAllKeys()) {
            CompoundTag start = starts.getCompound(key);
            String id = start.getString("id");
            if (!id.isEmpty() && !"INVALID".equals(id)) {
                ids.add(id);
                ListTag children = start.getList("Children", 10);

                for (int i = 0; i < children.size(); i++) {
                    int[] bb = children.getCompound(i).getIntArray("BB");
                    if (bb.length == 6) {
                        minX = Math.min(minX, bb[0]);
                        minY = Math.min(minY, bb[1]);
                        minZ = Math.min(minZ, bb[2]);
                        maxX = Math.max(maxX, bb[3]);
                        maxY = Math.max(maxY, bb[4]);
                        maxZ = Math.max(maxZ, bb[5]);
                    }
                }
            }
        }

        if (ids.isEmpty()) {
            return null;
        }

        if (minX > maxX || minY > maxY || minZ > maxZ) {
            minX = chunkX << 4;
            minZ = chunkZ << 4;
            maxX = minX + 15;
            maxZ = minZ + 15;
            minY = -64;
            maxY = 320;
        }

        return new SavedChunkAccess.StructureFootprint(ids, minX, minY, minZ, maxX, maxY, maxZ);
    }

    public Set<String> structureIds(int chunkX, int chunkZ) {
        CompoundTag chunkTag = this.rawTag(chunkX, chunkZ);
        if (chunkTag == null) {
            return Set.of();
        }

        CompoundTag starts = chunkTag.getCompound("structures").getCompound("starts");
        Set<String> ids = new HashSet<>();

        for (String key : starts.getAllKeys()) {
            String id = starts.getCompound(key).getString("id");
            if (!id.isEmpty() && !"INVALID".equals(id)) {
                ids.add(id);
            }
        }

        return ids;
    }

    public BlockState blockAt(int worldX, int worldY, int worldZ) {
        SavedChunkAccess.ChunkSections sections = this.chunkSections(worldX >> 4, worldZ >> 4);
        if (sections == null) {
            return Blocks.AIR.defaultBlockState();
        }

        PalettedContainer<BlockState> section = sections.blocksAt(Math.floorDiv(worldY, 16));
        return section == null ? Blocks.AIR.defaultBlockState() : (BlockState)section.get(worldX & 15, worldY & 15, worldZ & 15);
    }

    public Holder<Biome> biomeAt(int worldX, int worldY, int worldZ) {
        SavedChunkAccess.ChunkSections sections = this.chunkSections(worldX >> 4, worldZ >> 4);
        if (sections == null) {
            return null;
        }

        PalettedContainerRO<Holder<Biome>> section = sections.biomesAt(Math.floorDiv(worldY, 16));
        return section == null ? null : (Holder)section.get((worldX & 15) / 4, (worldY & 15) / 4, (worldZ & 15) / 4);
    }

    private SavedChunkAccess.ChunkSections chunkSections(int chunkX, int chunkZ) {
        long key = ChunkPos.asLong(chunkX, chunkZ);
        if (this.sectionsCache.containsKey(key)) {
            return this.sectionsCache.get(key);
        }

        CompoundTag chunkTag = this.rawTag(chunkX, chunkZ);
        SavedChunkAccess.ChunkSections sections = chunkTag == null ? null : SavedChunkAccess.ChunkSections.parse(chunkTag, this.biomeCodec);
        if (this.sectionsCache.size() >= 256) {
            evictOldest(this.sectionsCache);
        }

        this.sectionsCache.put(key, sections);
        return sections;
    }

    private CompoundTag rawTag(int chunkX, int chunkZ) {
        long key = ChunkPos.asLong(chunkX, chunkZ);
        if (this.rawTagCache.containsKey(key)) {
            return this.rawTagCache.get(key);
        }

        CompoundTag tag = this.readRawTag(chunkX, chunkZ);
        if (this.rawTagCache.size() >= 256) {
            evictOldest(this.rawTagCache);
        }

        this.rawTagCache.put(key, tag);
        return tag;
    }

    private CompoundTag readRawTag(int chunkX, int chunkZ) {
        RegionFile regionFile = this.openRegionFile(chunkX, chunkZ);
        if (regionFile == null) {
            return null;
        }

        ChunkPos pos = new ChunkPos(chunkX, chunkZ);

        try (DataInputStream in = regionFile.getChunkDataInputStream(pos)) {
            return in == null ? null : NbtIo.read(in);
        } catch (IOException | RuntimeException e) {
            Constants.LOG.debug("SavedChunkAccess: could not read chunk ({}, {})", new Object[]{chunkX, chunkZ, e});
            return null;
        }
    }

    private RegionFile openRegionFile(int chunkX, int chunkZ) {
        int regionX = chunkX >> 5;
        int regionZ = chunkZ >> 5;
        long key = ChunkPos.asLong(regionX, regionZ);
        RegionFile cached = this.openRegionFiles.get(key);
        if (cached != null) {
            return cached;
        }

        Path path = this.regionFolder.resolve("r." + regionX + "." + regionZ + ".mca");
        if (!Files.exists(path)) {
            return null;
        }

        try {
            RegionFile regionFile = new RegionFile(
                    new RegionStorageInfo("ecstatic", Level.OVERWORLD, "chunk"),
                    path, this.regionFolder, false);
            if (this.openRegionFiles.size() >= 256) {
                this.evictOldestRegionFile();
            }

            this.openRegionFiles.put(key, regionFile);
            return regionFile;
        } catch (IOException e) {
            Constants.LOG.debug("SavedChunkAccess: could not open region file {}", path, e);
            return null;
        }
    }

    private void evictOldestRegionFile() {
        Iterator<Entry<Long, RegionFile>> iterator = this.openRegionFiles.entrySet().iterator();
        if (iterator.hasNext()) {
            RegionFile oldest = iterator.next().getValue();
            iterator.remove();

            try {
                oldest.close();
            } catch (IOException e) {
                Constants.LOG.debug("SavedChunkAccess: error closing evicted region file", e);
            }
        }
    }

    private static <K, V> void evictOldest(Map<K, V> lruMap) {
        Iterator<Entry<K, V>> iterator = lruMap.entrySet().iterator();
        if (iterator.hasNext()) {
            iterator.next();
            iterator.remove();
        }
    }

    @Override
    public void close() {
        for (RegionFile regionFile : this.openRegionFiles.values()) {
            try {
                regionFile.close();
            } catch (IOException e) {
                Constants.LOG.debug("SavedChunkAccess: error closing region file", e);
            }
        }

        this.openRegionFiles.clear();
        this.rawTagCache.clear();
        this.sectionsCache.clear();
    }

    private static final class ChunkSections {
        private final Map<Integer, PalettedContainer<BlockState>> blocksByY;
        private final Map<Integer, PalettedContainerRO<Holder<Biome>>> biomesByY;

        private ChunkSections(Map<Integer, PalettedContainer<BlockState>> blocksByY, Map<Integer, PalettedContainerRO<Holder<Biome>>> biomesByY) {
            this.blocksByY = blocksByY;
            this.biomesByY = biomesByY;
        }

        PalettedContainer<BlockState> blocksAt(int sectionY) {
            return this.blocksByY.get(sectionY);
        }

        PalettedContainerRO<Holder<Biome>> biomesAt(int sectionY) {
            return this.biomesByY.get(sectionY);
        }

        static SavedChunkAccess.ChunkSections parse(CompoundTag chunkTag, Codec<PalettedContainerRO<Holder<Biome>>> biomeCodec) {
            Map<Integer, PalettedContainer<BlockState>> blocksByY = new HashMap<>();
            Map<Integer, PalettedContainerRO<Holder<Biome>>> biomesByY = new HashMap<>();
            ListTag sectionList = chunkTag.getList("sections", 10);

            for (int i = 0; i < sectionList.size(); i++) {
                CompoundTag sectionTag = sectionList.getCompound(i);
                int sectionY = sectionTag.getByte("Y");
                if (sectionTag.contains("block_states", 10)) {
                    PalettedContainer<BlockState> blockContainer = (PalettedContainer<BlockState>)SavedChunkAccess.BLOCK_STATE_CODEC
                        .parse(NbtOps.INSTANCE, sectionTag.getCompound("block_states"))
                        .result()
                        .orElse(null);
                    if (blockContainer != null) {
                        blocksByY.put(sectionY, blockContainer);
                    }
                }

                if (sectionTag.contains("biomes", 10)) {
                    PalettedContainerRO<Holder<Biome>> biomeContainer = (PalettedContainerRO<Holder<Biome>>)biomeCodec.parse(
                            NbtOps.INSTANCE, sectionTag.getCompound("biomes")
                        )
                        .result()
                        .orElse(null);
                    if (biomeContainer != null) {
                        biomesByY.put(sectionY, biomeContainer);
                    }
                }
            }

            return new SavedChunkAccess.ChunkSections(blocksByY, biomesByY);
        }
    }

    public record StructureFootprint(Set<String> ids, int minX, int minY, int minZ, int maxX, int maxY, int maxZ) {
    }
}
