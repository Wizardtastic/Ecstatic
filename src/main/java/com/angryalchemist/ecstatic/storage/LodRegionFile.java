package com.angryalchemist.ecstatic.storage;

import com.angryalchemist.ecstatic.lod.RegionCoord;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileChannel.MapMode;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import sun.misc.Unsafe;

public final class LodRegionFile implements AutoCloseable {
    private static final int MAGIC = 1179929668;
    private static final int FORMAT_VERSION = 4;
    private static final int BIOME_RAW_ID_MASK = 32767;
    private static final int HAS_TREES_BIT = 32768;
    private static final short UNWRITTEN_HEIGHT = -32768;
    private static final int OFF_MAGIC = 0;
    private static final int OFF_VERSION = 4;
    private static final int OFF_LOD_LEVEL = 8;
    private static final int OFF_SAMPLE_SPACING = 12;
    private static final int OFF_RECORD_SIZE = 16;
    private static final int OFF_REGION_COUNT = 20;
    private static final int OFF_DATA_AREA_END = 24;
    private static final int HEADER_SIZE = 64;
    private static final int INDEX_ENTRY_SIZE = 16;
    private static final int COLUMN_BYTES = 8;
    public final int lodLevel;
    public final int sampleSpacingBlocks;
    public final int samplesPerAxis;
    public final int recordSize;
    private final RandomAccessFile file;
    private final FileChannel channel;
    private final Map<Long, Long> regionOffsets = new HashMap<>();
    private MappedByteBuffer buffer;
    private long dataAreaEnd;
    private boolean dirty;

    public static LodRegionFile open(Path path, int lodLevel, int sampleSpacingBlocks) throws IOException {
        return new LodRegionFile(path, lodLevel, sampleSpacingBlocks);
    }

    private LodRegionFile(Path path, int lodLevel, int sampleSpacingBlocks) throws IOException {
        this.lodLevel = lodLevel;
        this.sampleSpacingBlocks = sampleSpacingBlocks;
        this.samplesPerAxis = 512 / sampleSpacingBlocks;
        this.recordSize = this.samplesPerAxis * this.samplesPerAxis * 8;
        if (path.getParent() != null) {
            path.getParent().toFile().mkdirs();
        }

        this.file = new RandomAccessFile(path.toFile(), "rw");
        this.channel = this.file.getChannel();
        if (this.isExistingFileUsable()) {
            this.remap(this.file.length());
            this.loadExisting();
        } else {
            this.initializeFresh();
        }
    }

    private void initializeFresh() throws IOException {
        this.file.setLength(64L);
        this.remap(64L);
        this.buffer.putInt(0, 1179929668);
        this.buffer.putInt(4, 4);
        this.buffer.putInt(8, this.lodLevel);
        this.buffer.putInt(12, this.sampleSpacingBlocks);
        this.buffer.putInt(16, this.recordSize);
        this.buffer.putInt(20, 0);
        this.buffer.putLong(24, 64L);
        this.dataAreaEnd = 64L;
        this.regionOffsets.clear();
        this.dirty = true;
        this.flush();
    }

    private boolean isExistingFileUsable() throws IOException {
        if (this.file.length() < 64L) {
            return false;
        }

        this.file.seek(0L);
        if (this.file.readInt() != 1179929668) {
            return false;
        }

        this.file.seek(4L);
        if (this.file.readInt() != 4) {
            return false;
        }

        this.file.seek(8L);
        if (this.file.readInt() != this.lodLevel) {
            return false;
        }

        this.file.seek(12L);
        if (this.file.readInt() != this.sampleSpacingBlocks) {
            return false;
        }

        this.file.seek(16L);
        if (this.file.readInt() != this.recordSize) {
            return false;
        }

        this.file.seek(20L);
        int regionCount = this.file.readInt();
        this.file.seek(24L);
        long dataAreaEndCandidate = this.file.readLong();
        long indexBytes = regionCount * 16L;
        return dataAreaEndCandidate >= 64L && dataAreaEndCandidate + indexBytes <= this.file.length();
    }

    private void loadExisting() {
        int regionCount = this.buffer.getInt(20);
        this.dataAreaEnd = this.buffer.getLong(24);
        this.regionOffsets.clear();
        long pos = this.dataAreaEnd;

        for (int i = 0; i < regionCount; i++) {
            int rx = this.buffer.getInt((int)pos);
            int rz = this.buffer.getInt((int)pos + 4);
            long offset = this.buffer.getLong((int)pos + 8);
            this.regionOffsets.put(key(rx, rz), offset);
            pos += 16L;
        }
    }

    private void remap(long size) throws IOException {
        if (this.buffer != null) {
            unmap(this.buffer);
        }

        this.buffer = this.channel.map(MapMode.READ_WRITE, 0L, size);
    }

    private static void unmap(MappedByteBuffer buffer) {
        try {
            Field theUnsafeField = Unsafe.class.getDeclaredField("theUnsafe");
            theUnsafeField.setAccessible(true);
            Unsafe unsafe = (Unsafe)theUnsafeField.get(null);
            Method invokeCleaner = Unsafe.class.getDeclaredMethod("invokeCleaner", ByteBuffer.class);
            invokeCleaner.invoke(unsafe, buffer);
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException("Failed to unmap region file buffer", e);
        }
    }

    private static long key(int regionX, int regionZ) {
        return (long)regionX << 32 | regionZ & 4294967295L;
    }

    private long offsetFor(RegionCoord region) throws IOException {
        long k = key(region.x(), region.z());
        Long existing = this.regionOffsets.get(k);
        if (existing != null) {
            return existing;
        }

        long offset = this.dataAreaEnd;
        long newDataEnd = offset + this.recordSize;
        if (newDataEnd > this.buffer.capacity()) {
            this.file.setLength(newDataEnd);
            this.remap(newDataEnd);
        }

        for (long p = offset; p < newDataEnd; p += 8L) {
            this.buffer.putShort((int)p, (short)-32768);
            this.buffer.putShort((int)p + 2, (short)0);
            this.buffer.putInt((int)p + 4, 0);
        }

        this.dataAreaEnd = newDataEnd;
        this.regionOffsets.put(k, offset);
        this.dirty = true;
        return offset;
    }

    public synchronized void writeColumn(RegionCoord region, int localX, int localZ, int height, int biomeRawId, int colorRgb, boolean hasTrees) throws IOException {
        long recordOffset = this.offsetFor(region);
        long columnOffset = recordOffset + (localZ * this.samplesPerAxis + localX) * 8L;
        if (this.buffer.getShort((int) columnOffset) != UNWRITTEN_HEIGHT) {
            return; // already sampled
        }

        int packedBiomeRawId = biomeRawId & 32767 | (hasTrees ? 32768 : 0);
        this.buffer.putShort((int)columnOffset, (short)height);
        this.buffer.putShort((int)columnOffset + 2, (short)packedBiomeRawId);
        this.buffer.putInt((int)columnOffset + 4, colorRgb);
        this.dirty = true;
    }

    public synchronized HeightmapColumn readColumn(RegionCoord region, int localX, int localZ) {
        Long recordOffset = this.regionOffsets.get(key(region.x(), region.z()));
        if (recordOffset == null) {
            return null;
        }

        long columnOffset = recordOffset + (localZ * this.samplesPerAxis + localX) * 8L;
        short height = this.buffer.getShort((int)columnOffset);
        if (height == -32768) {
            return null;
        }

        int packedBiomeRawId = this.buffer.getShort((int)columnOffset + 2) & '\uffff';
        int biomeRawId = packedBiomeRawId & 32767;
        boolean hasTrees = (packedBiomeRawId & 32768) != 0;
        int colorRgb = this.buffer.getInt((int)columnOffset + 4);
        return new HeightmapColumn(height, biomeRawId, colorRgb, hasTrees);
    }

    public synchronized boolean hasRegion(RegionCoord region) {
        return this.regionOffsets.containsKey(key(region.x(), region.z()));
    }

    public synchronized boolean isFullySampled(RegionCoord region) {
        Long recordOffset = this.regionOffsets.get(key(region.x(), region.z()));
        if (recordOffset == null) {
            return false;
        }

        long end = recordOffset + this.recordSize;

        for (long p = recordOffset; p < end; p += 8L) {
            if (this.buffer.getShort((int)p) == -32768) {
                return false;
            }
        }

        return true;
    }

    public synchronized Set<RegionCoord> regions() {
        Set<RegionCoord> result = new HashSet<>();

        for (long k : this.regionOffsets.keySet()) {
            result.add(new RegionCoord((int)(k >> 32), (int)k));
        }

        return result;
    }

    public synchronized void flush() throws IOException {
        if (this.dirty) {
            int regionCount = this.regionOffsets.size();
            long indexStart = this.dataAreaEnd;
            long indexEnd = indexStart + regionCount * 16L;
            if (indexEnd > this.buffer.capacity()) {
                this.file.setLength(indexEnd);
                this.remap(indexEnd);
            }

            long pos = indexStart;

            for (Entry<Long, Long> entry : this.regionOffsets.entrySet()) {
                long k = entry.getKey();
                int rx = (int)(k >> 32);
                int rz = (int)k;
                this.buffer.putInt((int)pos, rx);
                this.buffer.putInt((int)pos + 4, rz);
                this.buffer.putLong((int)pos + 8, entry.getValue());
                pos += 16L;
            }

            this.buffer.putInt(20, regionCount);
            this.buffer.putLong(24, this.dataAreaEnd);
            this.buffer.force();
            this.file.setLength(indexEnd);
            this.dirty = false;
        }
    }

    @Override
    public synchronized void close() throws IOException {
        this.flush();
        unmap(this.buffer);
        this.channel.close();
        this.file.close();
    }
}
