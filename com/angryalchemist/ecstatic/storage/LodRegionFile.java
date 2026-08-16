/*     */ package com.angryalchemist.ecstatic.storage;
/*     */ 
/*     */ import com.angryalchemist.ecstatic.lod.RegionCoord;
/*     */ import java.io.IOException;
/*     */ import java.io.RandomAccessFile;
/*     */ import java.lang.reflect.Field;
/*     */ import java.lang.reflect.Method;
/*     */ import java.nio.ByteBuffer;
/*     */ import java.nio.MappedByteBuffer;
/*     */ import java.nio.channels.FileChannel;
/*     */ import java.nio.file.Path;
/*     */ import java.util.HashMap;
/*     */ import java.util.HashSet;
/*     */ import java.util.Iterator;
/*     */ import java.util.Map;
/*     */ import java.util.Set;
/*     */ import sun.misc.Unsafe;
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ public final class LodRegionFile
/*     */   implements AutoCloseable
/*     */ {
/*     */   private static final int MAGIC = 1179929668;
/*     */   private static final int FORMAT_VERSION = 4;
/*     */   private static final int BIOME_RAW_ID_MASK = 32767;
/*     */   private static final int HAS_TREES_BIT = 32768;
/*     */   private static final short UNWRITTEN_HEIGHT = -32768;
/*     */   private static final int OFF_MAGIC = 0;
/*     */   private static final int OFF_VERSION = 4;
/*     */   private static final int OFF_LOD_LEVEL = 8;
/*     */   private static final int OFF_SAMPLE_SPACING = 12;
/*     */   private static final int OFF_RECORD_SIZE = 16;
/*     */   private static final int OFF_REGION_COUNT = 20;
/*     */   private static final int OFF_DATA_AREA_END = 24;
/*     */   private static final int HEADER_SIZE = 64;
/*     */   private static final int INDEX_ENTRY_SIZE = 16;
/*     */   private static final int COLUMN_BYTES = 8;
/*     */   public final int lodLevel;
/*     */   public final int sampleSpacingBlocks;
/*     */   public final int samplesPerAxis;
/*     */   public final int recordSize;
/*     */   private final RandomAccessFile file;
/*     */   private final FileChannel channel;
/*  75 */   private final Map<Long, Long> regionOffsets = new HashMap<>();
/*     */   private MappedByteBuffer buffer;
/*     */   private long dataAreaEnd;
/*     */   private boolean dirty;
/*     */   
/*     */   public static LodRegionFile open(Path path, int lodLevel, int sampleSpacingBlocks) throws IOException {
/*  81 */     return new LodRegionFile(path, lodLevel, sampleSpacingBlocks);
/*     */   }
/*     */   
/*     */   private LodRegionFile(Path path, int lodLevel, int sampleSpacingBlocks) throws IOException {
/*  85 */     this.lodLevel = lodLevel;
/*  86 */     this.sampleSpacingBlocks = sampleSpacingBlocks;
/*  87 */     this.samplesPerAxis = 512 / sampleSpacingBlocks;
/*  88 */     this.recordSize = this.samplesPerAxis * this.samplesPerAxis * 8;
/*     */     
/*  90 */     if (path.getParent() != null) {
/*  91 */       path.getParent().toFile().mkdirs();
/*     */     }
/*  93 */     this.file = new RandomAccessFile(path.toFile(), "rw");
/*  94 */     this.channel = this.file.getChannel();
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */     
/* 102 */     if (isExistingFileUsable()) {
/* 103 */       remap(this.file.length());
/* 104 */       loadExisting();
/*     */     } else {
/* 106 */       initializeFresh();
/*     */     } 
/*     */   }
/*     */   
/*     */   private void initializeFresh() throws IOException {
/* 111 */     this.file.setLength(64L);
/* 112 */     remap(64L);
/* 113 */     this.buffer.putInt(0, 1179929668);
/* 114 */     this.buffer.putInt(4, 4);
/* 115 */     this.buffer.putInt(8, this.lodLevel);
/* 116 */     this.buffer.putInt(12, this.sampleSpacingBlocks);
/* 117 */     this.buffer.putInt(16, this.recordSize);
/* 118 */     this.buffer.putInt(20, 0);
/* 119 */     this.buffer.putLong(24, 64L);
/* 120 */     this.dataAreaEnd = 64L;
/* 121 */     this.regionOffsets.clear();
/* 122 */     this.dirty = true;
/* 123 */     flush();
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   private boolean isExistingFileUsable() throws IOException {
/* 132 */     if (this.file.length() < 64L) {
/* 133 */       return false;
/*     */     }
/* 135 */     this.file.seek(0L);
/* 136 */     if (this.file.readInt() != 1179929668) {
/* 137 */       return false;
/*     */     }
/* 139 */     this.file.seek(4L);
/* 140 */     if (this.file.readInt() != 4) {
/* 141 */       return false;
/*     */     }
/* 143 */     this.file.seek(8L);
/* 144 */     if (this.file.readInt() != this.lodLevel) {
/* 145 */       return false;
/*     */     }
/* 147 */     this.file.seek(12L);
/* 148 */     if (this.file.readInt() != this.sampleSpacingBlocks) {
/* 149 */       return false;
/*     */     }
/* 151 */     this.file.seek(16L);
/* 152 */     if (this.file.readInt() != this.recordSize) {
/* 153 */       return false;
/*     */     }
/* 155 */     this.file.seek(20L);
/* 156 */     int regionCount = this.file.readInt();
/* 157 */     this.file.seek(24L);
/* 158 */     long dataAreaEndCandidate = this.file.readLong();
/* 159 */     long indexBytes = regionCount * 16L;
/* 160 */     return (dataAreaEndCandidate >= 64L && dataAreaEndCandidate + indexBytes <= this.file.length());
/*     */   }
/*     */ 
/*     */   
/*     */   private void loadExisting() {
/* 165 */     int regionCount = this.buffer.getInt(20);
/* 166 */     this.dataAreaEnd = this.buffer.getLong(24);
/* 167 */     this.regionOffsets.clear();
/* 168 */     long pos = this.dataAreaEnd;
/* 169 */     for (int i = 0; i < regionCount; i++) {
/* 170 */       int rx = this.buffer.getInt((int)pos);
/* 171 */       int rz = this.buffer.getInt((int)pos + 4);
/* 172 */       long offset = this.buffer.getLong((int)pos + 8);
/* 173 */       this.regionOffsets.put(Long.valueOf(key(rx, rz)), Long.valueOf(offset));
/* 174 */       pos += 16L;
/*     */     } 
/*     */   }
/*     */   
/*     */   private void remap(long size) throws IOException {
/* 179 */     if (this.buffer != null) {
/* 180 */       unmap(this.buffer);
/*     */     }
/* 182 */     this.buffer = this.channel.map(FileChannel.MapMode.READ_WRITE, 0L, size);
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   private static void unmap(MappedByteBuffer buffer) {
/*     */     try {
/* 196 */       Field theUnsafeField = Unsafe.class.getDeclaredField("theUnsafe");
/* 197 */       theUnsafeField.setAccessible(true);
/* 198 */       Unsafe unsafe = (Unsafe)theUnsafeField.get(null);
/* 199 */       Method invokeCleaner = Unsafe.class.getDeclaredMethod("invokeCleaner", new Class[] { ByteBuffer.class });
/* 200 */       invokeCleaner.invoke(unsafe, new Object[] { buffer });
/* 201 */     } catch (ReflectiveOperationException e) {
/* 202 */       throw new RuntimeException("Failed to unmap region file buffer", e);
/*     */     } 
/*     */   }
/*     */   
/*     */   private static long key(int regionX, int regionZ) {
/* 207 */     return regionX << 32L | regionZ & 0xFFFFFFFFL;
/*     */   }
/*     */ 
/*     */   
/*     */   private long offsetFor(RegionCoord region) throws IOException {
/* 212 */     long k = key(region.x(), region.z());
/* 213 */     Long existing = this.regionOffsets.get(Long.valueOf(k));
/* 214 */     if (existing != null) {
/* 215 */       return existing.longValue();
/*     */     }
/* 217 */     long offset = this.dataAreaEnd;
/* 218 */     long newDataEnd = offset + this.recordSize;
/* 219 */     if (newDataEnd > this.buffer.capacity()) {
/* 220 */       this.file.setLength(newDataEnd);
/* 221 */       remap(newDataEnd);
/*     */     }  long p;
/* 223 */     for (p = offset; p < newDataEnd; p += 8L) {
/* 224 */       this.buffer.putShort((int)p, -32768);
/* 225 */       this.buffer.putShort((int)p + 2, (short)0);
/* 226 */       this.buffer.putInt((int)p + 4, 0);
/*     */     } 
/* 228 */     this.dataAreaEnd = newDataEnd;
/* 229 */     this.regionOffsets.put(Long.valueOf(k), Long.valueOf(offset));
/* 230 */     this.dirty = true;
/* 231 */     return offset;
/*     */   }
/*     */ 
/*     */   
/*     */   public synchronized void writeColumn(RegionCoord region, int localX, int localZ, int height, int biomeRawId, int colorRgb, boolean hasTrees) throws IOException {
/* 236 */     long recordOffset = offsetFor(region);
/* 237 */     long columnOffset = recordOffset + (localZ * this.samplesPerAxis + localX) * 8L;
/* 238 */     int packedBiomeRawId = biomeRawId & 0x7FFF | (hasTrees ? 32768 : 0);
/* 239 */     this.buffer.putShort((int)columnOffset, (short)height);
/* 240 */     this.buffer.putShort((int)columnOffset + 2, (short)packedBiomeRawId);
/* 241 */     this.buffer.putInt((int)columnOffset + 4, colorRgb);
/* 242 */     this.dirty = true;
/*     */   }
/*     */   
/*     */   public synchronized HeightmapColumn readColumn(RegionCoord region, int localX, int localZ) {
/* 246 */     Long recordOffset = this.regionOffsets.get(Long.valueOf(key(region.x(), region.z())));
/* 247 */     if (recordOffset == null) {
/* 248 */       return null;
/*     */     }
/* 250 */     long columnOffset = recordOffset.longValue() + (localZ * this.samplesPerAxis + localX) * 8L;
/* 251 */     short height = this.buffer.getShort((int)columnOffset);
/* 252 */     if (height == Short.MIN_VALUE) {
/* 253 */       return null;
/*     */     }
/* 255 */     int packedBiomeRawId = this.buffer.getShort((int)columnOffset + 2) & 0xFFFF;
/* 256 */     int biomeRawId = packedBiomeRawId & 0x7FFF;
/* 257 */     boolean hasTrees = ((packedBiomeRawId & 0x8000) != 0);
/* 258 */     int colorRgb = this.buffer.getInt((int)columnOffset + 4);
/* 259 */     return new HeightmapColumn(height, biomeRawId, colorRgb, hasTrees);
/*     */   }
/*     */   
/*     */   public synchronized boolean hasRegion(RegionCoord region) {
/* 263 */     return this.regionOffsets.containsKey(Long.valueOf(key(region.x(), region.z())));
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   public synchronized boolean isFullySampled(RegionCoord region) {
/* 279 */     Long recordOffset = this.regionOffsets.get(Long.valueOf(key(region.x(), region.z())));
/* 280 */     if (recordOffset == null) {
/* 281 */       return false;
/*     */     }
/* 283 */     long end = recordOffset.longValue() + this.recordSize; long p;
/* 284 */     for (p = recordOffset.longValue(); p < end; p += 8L) {
/* 285 */       if (this.buffer.getShort((int)p) == Short.MIN_VALUE) {
/* 286 */         return false;
/*     */       }
/*     */     } 
/* 289 */     return true;
/*     */   }
/*     */   
/*     */   public synchronized Set<RegionCoord> regions() {
/* 293 */     Set<RegionCoord> result = new HashSet<>();
/* 294 */     for (Iterator<Long> iterator = this.regionOffsets.keySet().iterator(); iterator.hasNext(); ) { long k = ((Long)iterator.next()).longValue();
/* 295 */       result.add(new RegionCoord((int)(k >> 32L), (int)k)); }
/*     */     
/* 297 */     return result;
/*     */   }
/*     */   
/*     */   public synchronized void flush() throws IOException {
/* 301 */     if (!this.dirty) {
/*     */       return;
/*     */     }
/* 304 */     int regionCount = this.regionOffsets.size();
/* 305 */     long indexStart = this.dataAreaEnd;
/* 306 */     long indexEnd = indexStart + regionCount * 16L;
/* 307 */     if (indexEnd > this.buffer.capacity()) {
/* 308 */       this.file.setLength(indexEnd);
/* 309 */       remap(indexEnd);
/*     */     } 
/* 311 */     long pos = indexStart;
/* 312 */     for (Map.Entry<Long, Long> entry : this.regionOffsets.entrySet()) {
/* 313 */       long k = ((Long)entry.getKey()).longValue();
/* 314 */       int rx = (int)(k >> 32L);
/* 315 */       int rz = (int)k;
/* 316 */       this.buffer.putInt((int)pos, rx);
/* 317 */       this.buffer.putInt((int)pos + 4, rz);
/* 318 */       this.buffer.putLong((int)pos + 8, ((Long)entry.getValue()).longValue());
/* 319 */       pos += 16L;
/*     */     } 
/* 321 */     this.buffer.putInt(20, regionCount);
/* 322 */     this.buffer.putLong(24, this.dataAreaEnd);
/* 323 */     this.buffer.force();
/* 324 */     this.file.setLength(indexEnd);
/* 325 */     this.dirty = false;
/*     */   }
/*     */ 
/*     */   
/*     */   public synchronized void close() throws IOException {
/* 330 */     flush();
/* 331 */     unmap(this.buffer);
/* 332 */     this.channel.close();
/* 333 */     this.file.close();
/*     */   }
/*     */ }


/* Location:              C:\Users\Walker\Downloads\ecstatic-forge-1.20.1-1.3.0.jar!\com\angryalchemist\ecstatic\storage\LodRegionFile.class
 * Java compiler version: 17 (61.0)
 * JD-Core Version:       1.1.3
 */