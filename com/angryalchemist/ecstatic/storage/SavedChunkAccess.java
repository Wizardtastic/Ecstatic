/*     */ package com.angryalchemist.ecstatic.storage;
/*     */ 
/*     */ import com.angryalchemist.ecstatic.Constants;
/*     */ import com.mojang.serialization.Codec;
/*     */ import com.mojang.serialization.DynamicOps;
/*     */ import java.io.DataInputStream;
/*     */ import java.io.IOException;
/*     */ import java.nio.file.Files;
/*     */ import java.nio.file.Path;
/*     */ import java.util.HashMap;
/*     */ import java.util.HashSet;
/*     */ import java.util.Iterator;
/*     */ import java.util.LinkedHashMap;
/*     */ import java.util.Map;
/*     */ import java.util.Set;
/*     */ import net.minecraft.core.Holder;
/*     */ import net.minecraft.core.IdMap;
/*     */ import net.minecraft.core.Registry;
/*     */ import net.minecraft.nbt.CompoundTag;
/*     */ import net.minecraft.nbt.ListTag;
/*     */ import net.minecraft.nbt.NbtIo;
/*     */ import net.minecraft.nbt.NbtOps;
/*     */ import net.minecraft.world.level.ChunkPos;
/*     */ import net.minecraft.world.level.biome.Biome;
/*     */ import net.minecraft.world.level.biome.Biomes;
/*     */ import net.minecraft.world.level.block.Block;
/*     */ import net.minecraft.world.level.block.Blocks;
/*     */ import net.minecraft.world.level.block.state.BlockState;
/*     */ import net.minecraft.world.level.chunk.PalettedContainer;
/*     */ import net.minecraft.world.level.chunk.PalettedContainerRO;
/*     */ import net.minecraft.world.level.chunk.storage.RegionFile;
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
/*     */ public final class SavedChunkAccess
/*     */   implements AutoCloseable
/*     */ {
/*     */   private static final int MAX_OPEN_REGION_FILES = 256;
/*     */   private static final int SECTION_SIZE = 16;
/*     */   private static final int BIOME_SECTION_SIZE = 4;
/*     */   private static final String INVALID_STRUCTURE_ID = "INVALID";
/*  88 */   private static final Codec<PalettedContainer<BlockState>> BLOCK_STATE_CODEC = PalettedContainer.m_238371_((IdMap)Block.f_49791_, BlockState.f_61039_, PalettedContainer.Strategy.f_188137_, Blocks.f_50016_
/*     */       
/*  90 */       .m_49966_());
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   private final Path regionFolder;
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   private final Codec<PalettedContainerRO<Holder<Biome>>> biomeCodec;
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */   
/* 108 */   private final Map<Long, RegionFile> openRegionFiles = new LinkedHashMap<>(16, 0.75F, true);
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */   
/* 117 */   private final Map<Long, CompoundTag> rawTagCache = new LinkedHashMap<>(16, 0.75F, true);
/* 118 */   private final Map<Long, ChunkSections> sectionsCache = new LinkedHashMap<>(16, 0.75F, true);
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   private static final byte NBT_TAG_COMPOUND = 10;
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   public SavedChunkAccess(Path dimensionRoot, Registry<Biome> biomeRegistry) {
/* 131 */     this.regionFolder = dimensionRoot.resolve("region");
/* 132 */     this.biomeCodec = PalettedContainer.m_238418_(biomeRegistry.m_206115_(), biomeRegistry.m_206110_(), PalettedContainer.Strategy.f_188138_, biomeRegistry
/* 133 */         .m_246971_(Biomes.f_48202_));
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   public boolean hasChunk(int chunkX, int chunkZ) {
/* 142 */     RegionFile regionFile = openRegionFile(chunkX, chunkZ);
/* 143 */     return (regionFile != null && regionFile.m_63682_(new ChunkPos(chunkX, chunkZ)));
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
/*     */   public boolean hasStructure(int chunkX, int chunkZ) {
/* 156 */     return !structureIds(chunkX, chunkZ).isEmpty();
/*     */   }
/*     */ 
/*     */   
/*     */   public static final class StructureFootprint
/*     */     extends Record
/*     */   {
/*     */     private final Set<String> ids;
/*     */     
/*     */     private final int minX;
/*     */     
/*     */     private final int minY;
/*     */     
/*     */     private final int minZ;
/*     */     
/*     */     private final int maxX;
/*     */     
/*     */     private final int maxY;
/*     */     private final int maxZ;
/*     */     
/*     */     public final String toString() {
/*     */       // Byte code:
/*     */       //   0: aload_0
/*     */       //   1: <illegal opcode> toString : (Lcom/angryalchemist/ecstatic/storage/SavedChunkAccess$StructureFootprint;)Ljava/lang/String;
/*     */       //   6: areturn
/*     */       // Line number table:
/*     */       //   Java source line number -> byte code offset
/*     */       //   #185	-> 0
/*     */       // Local variable table:
/*     */       //   start	length	slot	name	descriptor
/*     */       //   0	7	0	this	Lcom/angryalchemist/ecstatic/storage/SavedChunkAccess$StructureFootprint;
/*     */     }
/*     */     
/*     */     public final int hashCode() {
/*     */       // Byte code:
/*     */       //   0: aload_0
/*     */       //   1: <illegal opcode> hashCode : (Lcom/angryalchemist/ecstatic/storage/SavedChunkAccess$StructureFootprint;)I
/*     */       //   6: ireturn
/*     */       // Line number table:
/*     */       //   Java source line number -> byte code offset
/*     */       //   #185	-> 0
/*     */       // Local variable table:
/*     */       //   start	length	slot	name	descriptor
/*     */       //   0	7	0	this	Lcom/angryalchemist/ecstatic/storage/SavedChunkAccess$StructureFootprint;
/*     */     }
/*     */     
/*     */     public StructureFootprint(Set<String> ids, int minX, int minY, int minZ, int maxX, int maxY, int maxZ) {
/* 185 */       this.ids = ids; this.minX = minX; this.minY = minY; this.minZ = minZ; this.maxX = maxX; this.maxY = maxY; this.maxZ = maxZ; } public final boolean equals(Object o) { // Byte code:
/*     */       //   0: aload_0
/*     */       //   1: aload_1
/*     */       //   2: <illegal opcode> equals : (Lcom/angryalchemist/ecstatic/storage/SavedChunkAccess$StructureFootprint;Ljava/lang/Object;)Z
/*     */       //   7: ireturn
/*     */       // Line number table:
/*     */       //   Java source line number -> byte code offset
/*     */       //   #185	-> 0
/*     */       // Local variable table:
/*     */       //   start	length	slot	name	descriptor
/*     */       //   0	8	0	this	Lcom/angryalchemist/ecstatic/storage/SavedChunkAccess$StructureFootprint;
/* 185 */       //   0	8	1	o	Ljava/lang/Object; } public Set<String> ids() { return this.ids; } public int minX() { return this.minX; } public int minY() { return this.minY; } public int minZ() { return this.minZ; } public int maxX() { return this.maxX; } public int maxY() { return this.maxY; } public int maxZ() { return this.maxZ; }
/*     */   
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   public StructureFootprint structureFootprint(int chunkX, int chunkZ) {
/* 196 */     CompoundTag chunkTag = rawTag(chunkX, chunkZ);
/* 197 */     if (chunkTag == null) {
/* 198 */       return null;
/*     */     }
/* 200 */     CompoundTag starts = chunkTag.m_128469_("structures").m_128469_("starts");
/* 201 */     Set<String> ids = new HashSet<>();
/* 202 */     int minX = Integer.MAX_VALUE;
/* 203 */     int minY = Integer.MAX_VALUE;
/* 204 */     int minZ = Integer.MAX_VALUE;
/* 205 */     int maxX = Integer.MIN_VALUE;
/* 206 */     int maxY = Integer.MIN_VALUE;
/* 207 */     int maxZ = Integer.MIN_VALUE;
/* 208 */     for (String key : starts.m_128431_()) {
/* 209 */       CompoundTag start = starts.m_128469_(key);
/* 210 */       String id = start.m_128461_("id");
/*     */ 
/*     */       
/* 213 */       if (id.isEmpty() || "INVALID".equals(id)) {
/*     */         continue;
/*     */       }
/* 216 */       ids.add(id);
/* 217 */       ListTag children = start.m_128437_("Children", 10);
/* 218 */       for (int i = 0; i < children.size(); i++) {
/* 219 */         int[] bb = children.m_128728_(i).m_128465_("BB");
/* 220 */         if (bb.length == 6) {
/*     */ 
/*     */           
/* 223 */           minX = Math.min(minX, bb[0]);
/* 224 */           minY = Math.min(minY, bb[1]);
/* 225 */           minZ = Math.min(minZ, bb[2]);
/* 226 */           maxX = Math.max(maxX, bb[3]);
/* 227 */           maxY = Math.max(maxY, bb[4]);
/* 228 */           maxZ = Math.max(maxZ, bb[5]);
/*     */         } 
/*     */       } 
/* 231 */     }  if (ids.isEmpty()) {
/* 232 */       return null;
/*     */     }
/* 234 */     if (minX > maxX || minY > maxY || minZ > maxZ) {
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */       
/* 243 */       minX = chunkX << 4;
/* 244 */       minZ = chunkZ << 4;
/* 245 */       maxX = minX + 15;
/* 246 */       maxZ = minZ + 15;
/* 247 */       minY = -64;
/* 248 */       maxY = 320;
/*     */     } 
/* 250 */     return new StructureFootprint(ids, minX, minY, minZ, maxX, maxY, maxZ);
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   public Set<String> structureIds(int chunkX, int chunkZ) {
/* 257 */     CompoundTag chunkTag = rawTag(chunkX, chunkZ);
/* 258 */     if (chunkTag == null) {
/* 259 */       return Set.of();
/*     */     }
/* 261 */     CompoundTag starts = chunkTag.m_128469_("structures").m_128469_("starts");
/* 262 */     Set<String> ids = new HashSet<>();
/* 263 */     for (String key : starts.m_128431_()) {
/* 264 */       String id = starts.m_128469_(key).m_128461_("id");
/* 265 */       if (!id.isEmpty() && !"INVALID".equals(id)) {
/* 266 */         ids.add(id);
/*     */       }
/*     */     } 
/* 269 */     return ids;
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
/*     */   public BlockState blockAt(int worldX, int worldY, int worldZ) {
/* 281 */     ChunkSections sections = chunkSections(worldX >> 4, worldZ >> 4);
/* 282 */     if (sections == null) {
/* 283 */       return Blocks.f_50016_.m_49966_();
/*     */     }
/* 285 */     PalettedContainer<BlockState> section = sections.blocksAt(Math.floorDiv(worldY, 16));
/* 286 */     if (section == null) {
/* 287 */       return Blocks.f_50016_.m_49966_();
/*     */     }
/* 289 */     return (BlockState)section.m_63087_(worldX & 0xF, worldY & 0xF, worldZ & 0xF);
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   public Holder<Biome> biomeAt(int worldX, int worldY, int worldZ) {
/* 299 */     ChunkSections sections = chunkSections(worldX >> 4, worldZ >> 4);
/* 300 */     if (sections == null) {
/* 301 */       return null;
/*     */     }
/* 303 */     PalettedContainerRO<Holder<Biome>> section = sections.biomesAt(Math.floorDiv(worldY, 16));
/* 304 */     if (section == null) {
/* 305 */       return null;
/*     */     }
/* 307 */     return (Holder<Biome>)section.m_63087_((worldX & 0xF) / 4, (worldY & 0xF) / 4, (worldZ & 0xF) / 4);
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   private ChunkSections chunkSections(int chunkX, int chunkZ) {
/* 318 */     long key = ChunkPos.m_45589_(chunkX, chunkZ);
/* 319 */     if (this.sectionsCache.containsKey(Long.valueOf(key))) {
/* 320 */       return this.sectionsCache.get(Long.valueOf(key));
/*     */     }
/*     */     
/* 323 */     CompoundTag chunkTag = rawTag(chunkX, chunkZ);
/* 324 */     ChunkSections sections = (chunkTag == null) ? null : ChunkSections.parse(chunkTag, this.biomeCodec);
/* 325 */     if (this.sectionsCache.size() >= 256) {
/* 326 */       evictOldest(this.sectionsCache);
/*     */     }
/* 328 */     this.sectionsCache.put(Long.valueOf(key), sections);
/* 329 */     return sections;
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
/*     */   private CompoundTag rawTag(int chunkX, int chunkZ) {
/* 343 */     long key = ChunkPos.m_45589_(chunkX, chunkZ);
/* 344 */     if (this.rawTagCache.containsKey(Long.valueOf(key))) {
/* 345 */       return this.rawTagCache.get(Long.valueOf(key));
/*     */     }
/*     */     
/* 348 */     CompoundTag tag = readRawTag(chunkX, chunkZ);
/* 349 */     if (this.rawTagCache.size() >= 256) {
/* 350 */       evictOldest(this.rawTagCache);
/*     */     }
/* 352 */     this.rawTagCache.put(Long.valueOf(key), tag);
/* 353 */     return tag;
/*     */   }
/*     */   
/*     */   private CompoundTag readRawTag(int chunkX, int chunkZ) {
/* 357 */     RegionFile regionFile = openRegionFile(chunkX, chunkZ);
/* 358 */     if (regionFile == null) {
/* 359 */       return null;
/*     */     }
/* 361 */     ChunkPos pos = new ChunkPos(chunkX, chunkZ); 
/* 362 */     try { DataInputStream in = regionFile.m_63645_(pos); 
/* 363 */       try { CompoundTag compoundTag = (in == null) ? null : NbtIo.m_128928_(in);
/* 364 */         if (in != null) in.close();  return compoundTag; } catch (Throwable throwable) { if (in != null) try { in.close(); } catch (Throwable throwable1) { throwable.addSuppressed(throwable1); }   throw throwable; }  } catch (IOException|RuntimeException e)
/*     */     
/*     */     { 
/*     */ 
/*     */ 
/*     */       
/* 370 */       Constants.LOG.debug("SavedChunkAccess: could not read chunk ({}, {})", new Object[] { Integer.valueOf(chunkX), Integer.valueOf(chunkZ), e });
/* 371 */       return null; }
/*     */   
/*     */   }
/*     */   
/*     */   private RegionFile openRegionFile(int chunkX, int chunkZ) {
/* 376 */     int regionX = chunkX >> 5;
/* 377 */     int regionZ = chunkZ >> 5;
/* 378 */     long key = ChunkPos.m_45589_(regionX, regionZ);
/* 379 */     RegionFile cached = this.openRegionFiles.get(Long.valueOf(key));
/* 380 */     if (cached != null) {
/* 381 */       return cached;
/*     */     }
/*     */     
/* 384 */     Path path = this.regionFolder.resolve("r." + regionX + "." + regionZ + ".mca");
/* 385 */     if (!Files.exists(path, new java.nio.file.LinkOption[0])) {
/* 386 */       return null;
/*     */     }
/*     */     try {
/* 389 */       RegionFile regionFile = new RegionFile(path, this.regionFolder, false);
/* 390 */       if (this.openRegionFiles.size() >= 256) {
/* 391 */         evictOldestRegionFile();
/*     */       }
/* 393 */       this.openRegionFiles.put(Long.valueOf(key), regionFile);
/* 394 */       return regionFile;
/* 395 */     } catch (IOException e) {
/* 396 */       Constants.LOG.debug("SavedChunkAccess: could not open region file {}", path, e);
/* 397 */       return null;
/*     */     } 
/*     */   }
/*     */   
/*     */   private void evictOldestRegionFile() {
/* 402 */     Iterator<Map.Entry<Long, RegionFile>> iterator = this.openRegionFiles.entrySet().iterator();
/* 403 */     if (!iterator.hasNext()) {
/*     */       return;
/*     */     }
/* 406 */     RegionFile oldest = (RegionFile)((Map.Entry)iterator.next()).getValue();
/* 407 */     iterator.remove();
/*     */     try {
/* 409 */       oldest.close();
/* 410 */     } catch (IOException e) {
/* 411 */       Constants.LOG.debug("SavedChunkAccess: error closing evicted region file", e);
/*     */     } 
/*     */   }
/*     */   
/*     */   private static <K, V> void evictOldest(Map<K, V> lruMap) {
/* 416 */     Iterator<Map.Entry<K, V>> iterator = lruMap.entrySet().iterator();
/* 417 */     if (iterator.hasNext()) {
/* 418 */       iterator.next();
/* 419 */       iterator.remove();
/*     */     } 
/*     */   }
/*     */ 
/*     */ 
/*     */   
/*     */   public void close() {
/* 426 */     for (RegionFile regionFile : this.openRegionFiles.values()) {
/*     */       try {
/* 428 */         regionFile.close();
/* 429 */       } catch (IOException e) {
/* 430 */         Constants.LOG.debug("SavedChunkAccess: error closing region file", e);
/*     */       } 
/*     */     } 
/* 433 */     this.openRegionFiles.clear();
/* 434 */     this.rawTagCache.clear();
/* 435 */     this.sectionsCache.clear();
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   private static final class ChunkSections
/*     */   {
/*     */     private final Map<Integer, PalettedContainer<BlockState>> blocksByY;
/*     */ 
/*     */ 
/*     */     
/*     */     private final Map<Integer, PalettedContainerRO<Holder<Biome>>> biomesByY;
/*     */ 
/*     */ 
/*     */ 
/*     */     
/*     */     private ChunkSections(Map<Integer, PalettedContainer<BlockState>> blocksByY, Map<Integer, PalettedContainerRO<Holder<Biome>>> biomesByY) {
/* 453 */       this.blocksByY = blocksByY;
/* 454 */       this.biomesByY = biomesByY;
/*     */     }
/*     */     
/*     */     PalettedContainer<BlockState> blocksAt(int sectionY) {
/* 458 */       return this.blocksByY.get(Integer.valueOf(sectionY));
/*     */     }
/*     */     
/*     */     PalettedContainerRO<Holder<Biome>> biomesAt(int sectionY) {
/* 462 */       return this.biomesByY.get(Integer.valueOf(sectionY));
/*     */     }
/*     */     
/*     */     static ChunkSections parse(CompoundTag chunkTag, Codec<PalettedContainerRO<Holder<Biome>>> biomeCodec) {
/* 466 */       Map<Integer, PalettedContainer<BlockState>> blocksByY = new HashMap<>();
/* 467 */       Map<Integer, PalettedContainerRO<Holder<Biome>>> biomesByY = new HashMap<>();
/* 468 */       ListTag sectionList = chunkTag.m_128437_("sections", 10);
/* 469 */       for (int i = 0; i < sectionList.size(); i++) {
/* 470 */         CompoundTag sectionTag = sectionList.m_128728_(i);
/* 471 */         int sectionY = sectionTag.m_128445_("Y");
/*     */         
/* 473 */         if (sectionTag.m_128425_("block_states", 10)) {
/*     */ 
/*     */           
/* 476 */           PalettedContainer<BlockState> blockContainer = SavedChunkAccess.BLOCK_STATE_CODEC.parse((DynamicOps)NbtOps.f_128958_, sectionTag.m_128469_("block_states")).result().orElse(null);
/* 477 */           if (blockContainer != null) {
/* 478 */             blocksByY.put(Integer.valueOf(sectionY), blockContainer);
/*     */           }
/*     */         } 
/*     */ 
/*     */         
/* 483 */         if (sectionTag.m_128425_("biomes", 10)) {
/*     */ 
/*     */           
/* 486 */           PalettedContainerRO<Holder<Biome>> biomeContainer = biomeCodec.parse((DynamicOps)NbtOps.f_128958_, sectionTag.m_128469_("biomes")).result().orElse(null);
/* 487 */           if (biomeContainer != null) {
/* 488 */             biomesByY.put(Integer.valueOf(sectionY), biomeContainer);
/*     */           }
/*     */         } 
/*     */       } 
/* 492 */       return new ChunkSections(blocksByY, biomesByY);
/*     */     }
/*     */   }
/*     */ }


/* Location:              C:\Users\Walker\Downloads\ecstatic-forge-1.20.1-1.3.0.jar!\com\angryalchemist\ecstatic\storage\SavedChunkAccess.class
 * Java compiler version: 17 (61.0)
 * JD-Core Version:       1.1.3
 */