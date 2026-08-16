/*     */ package com.angryalchemist.ecstatic.render;
/*     */ 
/*     */ import com.angryalchemist.ecstatic.Constants;
/*     */ import com.angryalchemist.ecstatic.lod.StructureChunkLocator;
/*     */ import com.angryalchemist.ecstatic.storage.HeightmapColumn;
/*     */ import com.angryalchemist.ecstatic.storage.SavedChunkAccess;
/*     */ import com.mojang.blaze3d.systems.RenderSystem;
/*     */ import com.mojang.blaze3d.vertex.DefaultVertexFormat;
/*     */ import com.mojang.blaze3d.vertex.VertexBuffer;
/*     */ import java.util.HashMap;
/*     */ import java.util.HashSet;
/*     */ import java.util.Iterator;
/*     */ import java.util.LinkedHashSet;
/*     */ import java.util.List;
/*     */ import java.util.Map;
/*     */ import java.util.Set;
/*     */ import java.util.TreeSet;
/*     */ import java.util.concurrent.atomic.AtomicBoolean;
/*     */ import net.minecraft.client.Minecraft;
/*     */ import net.minecraft.client.multiplayer.ClientLevel;
/*     */ import net.minecraft.client.renderer.GameRenderer;
/*     */ import net.minecraft.client.renderer.RenderType;
/*     */ import net.minecraft.client.server.IntegratedServer;
/*     */ import net.minecraft.core.Direction;
/*     */ import net.minecraft.core.Holder;
/*     */ import net.minecraft.core.Registry;
/*     */ import net.minecraft.core.registries.Registries;
/*     */ import net.minecraft.server.MinecraftServer;
/*     */ import net.minecraft.server.level.ServerLevel;
/*     */ import net.minecraft.world.level.ChunkPos;
/*     */ import net.minecraft.world.level.biome.Biome;
/*     */ import net.minecraft.world.level.block.state.BlockState;
/*     */ import net.minecraft.world.level.storage.LevelResource;
/*     */ import net.minecraft.world.phys.Vec3;
/*     */ import org.joml.Matrix4f;
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
/*     */ final class LodStructureIslands
/*     */ {
/* 111 */   private static final Set<String> EXCLUDED_STRUCTURE_IDS = Set.of("minecraft:mineshaft", "minecraft:mineshaft_mesa", "minecraft:stronghold", "minecraft:ancient_city", "minecraft:buried_treasure", "minecraft:trail_ruins");
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
/*     */   private static final int BAND_BEYOND_RENDER_DISTANCE_CHUNKS = 12;
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
/*     */   private static final int MAX_CHUNKS_PER_STRUCTURE = 64;
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
/*     */   private static final int SCAN_TOP_Y = 192;
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
/*     */   private static final int Y_MARGIN_BLOCKS = 4;
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
/* 166 */   private static final LodRegionMesh.FadeParams NO_FADE = new LodRegionMesh.FadeParams(-1073741824, -1073741824, 0.0F, 1.0F);
/*     */   
/*     */   private static volatile VertexBuffer buffer;
/*     */   
/* 170 */   private static int builtChunkX = Integer.MIN_VALUE;
/* 171 */   private static int builtChunkZ = Integer.MIN_VALUE;
/* 172 */   private static final AtomicBoolean buildInFlight = new AtomicBoolean(false);
/*     */ 
/*     */ 
/*     */   
/*     */   private static volatile LodRegionMesh.RecordedPart pendingUpload;
/*     */ 
/*     */   
/* 179 */   private static volatile Set<Long> renderedChunks = Set.of();
/*     */   
/*     */   private static final int SCAN_MISS = -2147483648;
/*     */ 
/*     */   
/*     */   static void render(Matrix4f modelViewMatrix, Matrix4f projectionMatrix, ClientLevel level, Vec3 cameraPos) {
/* 185 */     IntegratedServer integratedServer = Minecraft.m_91087_().m_91092_();
/* 186 */     if (integratedServer == null) {
/*     */       return;
/*     */     }
/*     */ 
/*     */ 
/*     */ 
/*     */     
/* 193 */     int chunkX = Math.floorDiv((int)Math.floor(cameraPos.f_82479_), 16);
/* 194 */     int chunkZ = Math.floorDiv((int)Math.floor(cameraPos.f_82481_), 16);
/* 195 */     if ((chunkX != builtChunkX || chunkZ != builtChunkZ) && buildInFlight.compareAndSet(false, true)) {
/* 196 */       builtChunkX = chunkX;
/* 197 */       builtChunkZ = chunkZ;
/*     */ 
/*     */ 
/*     */       
/* 201 */       dispatchBuild(chunkX, chunkZ, (Minecraft.m_91087_()).f_91066_.m_193772_(), (MinecraftServer)integratedServer, level);
/*     */     } 
/*     */ 
/*     */     
/* 205 */     LodRegionMesh.RecordedPart finished = pendingUpload;
/* 206 */     if (finished != null) {
/* 207 */       pendingUpload = null;
/* 208 */       VertexBuffer old = buffer;
/* 209 */       buffer = LodRegionMesh.uploadPart(finished);
/* 210 */       if (old != null) {
/* 211 */         old.close();
/*     */       }
/*     */     } 
/*     */     
/* 215 */     VertexBuffer current = buffer;
/* 216 */     if (current == null) {
/*     */       return;
/*     */     }
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
/* 229 */     boolean fogReady = (LodFogShader.getTexturedOrNull() != null);
/*     */ 
/*     */     
/* 232 */     RenderType renderType = fogReady ? LodTerrainRenderType.TERRAIN_FOG_TEXTURED_OPAQUE_NOCULL : LodTerrainRenderType.TERRAIN_TEXTURED_OPAQUE_NOCULL;
/* 233 */     renderType.m_110185_();
/* 234 */     RenderSystem.setShader(fogReady ? LodFogShader::getTexturedOrNull : GameRenderer::m_172814_);
/* 235 */     current.m_85921_();
/* 236 */     current.m_253207_(modelViewMatrix, projectionMatrix, RenderSystem.getShader());
/* 237 */     VertexBuffer.m_85931_();
/* 238 */     renderType.m_110188_();
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
/*     */   static Set<Long> renderedChunks() {
/* 253 */     return renderedChunks;
/*     */   }
/*     */ 
/*     */ 
/*     */   
/*     */   private static void dispatchBuild(int centerChunkX, int centerChunkZ, int renderDistanceChunks, MinecraftServer server, ClientLevel clientLevel) {
/* 259 */     Thread worker = new Thread(() -> {
/*     */           try {
/*     */             ServerLevel level = server.m_129880_(clientLevel.m_46472_());
/*     */             
/*     */             if (level == null) {
/*     */               return;
/*     */             }
/*     */             LodRegionMesh.RecordedPart part = buildGeometry(centerChunkX, centerChunkZ, renderDistanceChunks, server, level);
/*     */             pendingUpload = part;
/* 268 */           } catch (RuntimeException e) {
/*     */             Constants.LOG.warn("Ecstatic: structure island build failed", e);
/*     */           } finally {
/*     */             buildInFlight.set(false);
/*     */           } 
/*     */         }"Ecstatic-StructureIsland-Worker");
/* 274 */     worker.setDaemon(true);
/* 275 */     worker.start();
/*     */   }
/*     */ 
/*     */ 
/*     */   
/*     */   private static LodRegionMesh.RecordedPart buildGeometry(int centerChunkX, int centerChunkZ, int renderDistanceChunks, MinecraftServer server, ServerLevel level) {
/* 281 */     RecordedVertexSink sink = new RecordedVertexSink();
/* 282 */     int[] vertexCount = { 0 };
/*     */     
/* 284 */     Registry<Biome> biomeRegistry = level.m_9598_().m_175515_(Registries.f_256952_);
/* 285 */     SavedChunkAccess savedChunks = new SavedChunkAccess(server.m_129843_(LevelResource.f_78182_), biomeRegistry);
/*     */ 
/*     */ 
/*     */ 
/*     */     
/* 290 */     try { int outerRadiusChunks = renderDistanceChunks + 12;
/* 291 */       int minChunkX = centerChunkX - outerRadiusChunks;
/* 292 */       int maxChunkX = centerChunkX + outerRadiusChunks;
/* 293 */       int minChunkZ = centerChunkZ - outerRadiusChunks;
/* 294 */       int maxChunkZ = centerChunkZ + outerRadiusChunks;
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */       
/* 300 */       List<ChunkPos> candidates = StructureChunkLocator.candidateStartChunks(level, minChunkX, minChunkZ, maxChunkX, maxChunkZ);
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */       
/* 309 */       Set<Long> chunksToRender = new LinkedHashSet<>();
/*     */ 
/*     */ 
/*     */       
/* 313 */       Map<Long, int[]> yRangeByChunk = (Map)new HashMap<>();
/* 314 */       Set<String> foundStructureIds = new TreeSet<>();
/* 315 */       for (ChunkPos candidate : candidates) {
/*     */         
/* 317 */         SavedChunkAccess.StructureFootprint footprint = savedChunks.structureFootprint(candidate.f_45578_, candidate.f_45579_);
/* 318 */         if (footprint == null) {
/*     */           continue;
/*     */         }
/*     */ 
/*     */         
/* 323 */         Set<String> ids = new HashSet<>(footprint.ids());
/* 324 */         ids.removeAll(EXCLUDED_STRUCTURE_IDS);
/* 325 */         if (ids.isEmpty()) {
/*     */           continue;
/*     */         }
/* 328 */         foundStructureIds.addAll(ids);
/* 329 */         int footprintMinY = Math.max(level.m_141937_(), footprint.minY() - 4);
/* 330 */         int footprintMaxY = Math.min(192, footprint.maxY() + 4);
/* 331 */         int fromChunkX = footprint.minX() >> 4;
/* 332 */         int toChunkX = footprint.maxX() >> 4;
/* 333 */         int fromChunkZ = footprint.minZ() >> 4;
/* 334 */         int toChunkZ = footprint.maxZ() >> 4;
/* 335 */         int added = 0;
/* 336 */         for (int cx = fromChunkX; cx <= toChunkX && added < 64; cx++) {
/* 337 */           for (int cz = fromChunkZ; cz <= toChunkZ && added < 64; cz++) {
/* 338 */             if (cx >= minChunkX && cx <= maxChunkX && cz >= minChunkZ && cz <= maxChunkZ)
/*     */             {
/*     */ 
/*     */ 
/*     */ 
/*     */               
/* 344 */               if (!withinRenderDistance(cx, cz, centerChunkX, centerChunkZ, renderDistanceChunks)) {
/*     */ 
/*     */                 
/* 347 */                 long packed = ChunkPos.m_45589_(cx, cz);
/* 348 */                 chunksToRender.add(Long.valueOf(packed));
/* 349 */                 yRangeByChunk.merge(Long.valueOf(packed), new int[] { footprintMinY, footprintMaxY }, (a, b) -> new int[] { Math.min(a[0], b[0]), Math.max(a[1], b[1]) });
/*     */                 
/* 351 */                 added++;
/*     */               } 
/*     */             }
/*     */           } 
/*     */         } 
/*     */       } 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */       
/* 362 */       for (Iterator<Long> iterator = chunksToRender.iterator(); iterator.hasNext(); ) { long packed = ((Long)iterator.next()).longValue();
/* 363 */         int[] range = yRangeByChunk.get(Long.valueOf(packed));
/* 364 */         emitChunk(sink, savedChunks, ChunkPos.m_45592_(packed), ChunkPos.m_45602_(packed), range[0], range[1], vertexCount); }
/*     */ 
/*     */       
/* 367 */       int structureChunkCount = chunksToRender.size();
/*     */ 
/*     */ 
/*     */       
/* 371 */       renderedChunks = Set.copyOf(chunksToRender);
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
/* 383 */       Constants.LOG.info("Ecstatic structure islands: center chunk ({}, {}), ring {}-{} chunks, {} placement candidate(s) -> {} chunk(s) rendered ({}), {} vertices", new Object[] {
/*     */             
/* 385 */             Integer.valueOf(centerChunkX), Integer.valueOf(centerChunkZ), Integer.valueOf(renderDistanceChunks), Integer.valueOf(outerRadiusChunks), Integer.valueOf(candidates.size()), 
/* 386 */             Integer.valueOf(structureChunkCount), 
/* 387 */             foundStructureIds.isEmpty() ? "none" : String.join(", ", (Iterable)foundStructureIds), Integer.valueOf(vertexCount[0]) });
/* 388 */       savedChunks.close(); } catch (Throwable throwable) { try { savedChunks.close(); } catch (Throwable throwable1) { throwable.addSuppressed(throwable1); }
/*     */        throw throwable; }
/* 390 */      return new LodRegionMesh.RecordedPart(sink, RecordedVertexSink.Kind.TEXTURED, DefaultVertexFormat.f_85818_);
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
/*     */   private static boolean withinRenderDistance(int chunkX, int chunkZ, int centerChunkX, int centerChunkZ, int renderDistanceChunks) {
/* 419 */     int inner = Math.max(0, renderDistanceChunks - 4);
/* 420 */     int dx = chunkX - centerChunkX;
/* 421 */     int dz = chunkZ - centerChunkZ;
/* 422 */     return (dx * dx + dz * dz <= inner * inner);
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
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   private static void emitChunk(VertexSink sink, SavedChunkAccess savedChunks, int chunkX, int chunkZ, int minY, int maxY, int[] vertexCount) {
/* 445 */     int originX = chunkX << 4;
/* 446 */     int originZ = chunkZ << 4;
/* 447 */     int span = 18;
/* 448 */     int[][] heights = new int[span][span];
/* 449 */     int heightScanTop = Math.min(192, maxY);
/*     */     int lx;
/* 451 */     for (lx = 0; lx < span; lx++) {
/* 452 */       for (int lz = 0; lz < span; lz++) {
/* 453 */         int worldX = originX - 1 + lx;
/* 454 */         int worldZ = originZ - 1 + lz;
/* 455 */         int foundY = Integer.MIN_VALUE;
/* 456 */         for (int y = heightScanTop; y >= minY; y--) {
/* 457 */           if (isSolid(savedChunks, worldX, y, worldZ)) {
/* 458 */             foundY = y;
/*     */             break;
/*     */           } 
/*     */         } 
/* 462 */         heights[lx][lz] = foundY;
/*     */       } 
/*     */     } 
/*     */     
/* 466 */     for (lx = 1; lx < span - 1; lx++) {
/* 467 */       for (int lz = 1; lz < span - 1; lz++) {
/* 468 */         if (heights[lx][lz] != Integer.MIN_VALUE) {
/*     */ 
/*     */           
/* 471 */           int worldX = originX - 1 + lx;
/* 472 */           int worldZ = originZ - 1 + lz;
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */           
/* 479 */           HeightmapColumn west = neighborColumn(heights, lx - 1, lz);
/* 480 */           HeightmapColumn east = neighborColumn(heights, lx + 1, lz);
/* 481 */           HeightmapColumn south = neighborColumn(heights, lx, lz - 1);
/* 482 */           HeightmapColumn north = neighborColumn(heights, lx, lz + 1);
/* 483 */           HeightmapColumn center = neighborColumn(heights, lx, lz);
/* 484 */           float slopeShade = LodRegionMesh.bakedSurfaceBrightness(
/* 485 */               LodRegionMesh.centralDifference(west, east, center, 1), 
/* 486 */               LodRegionMesh.centralDifference(south, north, center, 1), 
/* 487 */               LodSettingsConfig.get().structureSlopeShadingFloor());
/*     */           
/* 489 */           for (int y = heightScanTop; y >= minY; y--) {
/* 490 */             BlockState state = savedChunks.blockAt(worldX, y, worldZ);
/* 491 */             if (!state.m_60795_() && state.m_60819_().m_76178_())
/*     */             {
/*     */               
/* 494 */               emitVoxel(sink, savedChunks, worldX, y, worldZ, state, slopeShade, vertexCount); } 
/*     */           } 
/*     */         } 
/*     */       } 
/*     */     } 
/*     */   }
/*     */   
/*     */   private static boolean isSolid(SavedChunkAccess savedChunks, int x, int y, int z) {
/* 502 */     BlockState state = savedChunks.blockAt(x, y, z);
/* 503 */     return (!state.m_60795_() && state.m_60819_().m_76178_());
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
/*     */   private static void emitVoxel(VertexSink sink, SavedChunkAccess savedChunks, int x, int y, int z, BlockState state, float slopeShade, int[] vertexCount) {
/* 531 */     boolean upExposed = !isSolid(savedChunks, x, y + 1, z);
/* 532 */     boolean downExposed = !isSolid(savedChunks, x, y - 1, z);
/* 533 */     boolean northExposed = !isSolid(savedChunks, x, y, z - 1);
/* 534 */     boolean southExposed = !isSolid(savedChunks, x, y, z + 1);
/* 535 */     boolean westExposed = !isSolid(savedChunks, x - 1, y, z);
/* 536 */     boolean eastExposed = !isSolid(savedChunks, x + 1, y, z);
/* 537 */     if (!upExposed && !downExposed && !northExposed && !southExposed && !westExposed && !eastExposed) {
/*     */       return;
/*     */     }
/*     */ 
/*     */ 
/*     */     
/* 543 */     float x0 = x - 0.5F;
/* 544 */     float x1 = x + 0.5F;
/* 545 */     float z0 = z - 0.5F;
/* 546 */     float z1 = z + 0.5F;
/* 547 */     float yBottom = y;
/* 548 */     float yTop = y + 1.0F;
/*     */     
/* 550 */     if (upExposed) {
/* 551 */       SurfaceMaterial.Sprite sprite = SurfaceMaterial.resolveSprite(state, Direction.UP);
/* 552 */       int tint = resolveTint(savedChunks, sprite, x, y, z);
/* 553 */       float shade = 1.0F * slopeShade;
/* 554 */       LodRegionMesh.emitBoxTop(sink, x0, z0, x1, z1, yTop, yTop, yTop, yTop, sprite, tint, shade, shade, shade, shade, NO_FADE, false, vertexCount);
/*     */     } 
/*     */     
/* 557 */     if (downExposed) {
/* 558 */       SurfaceMaterial.Sprite sprite = SurfaceMaterial.resolveSprite(state, Direction.DOWN);
/* 559 */       int tint = resolveTint(savedChunks, sprite, x, y, z);
/* 560 */       LodRegionMesh.emitBoxBottom(sink, x0, z0, x1, z1, yBottom, sprite, tint, 0.5F * slopeShade, NO_FADE, false, vertexCount);
/*     */     } 
/*     */     
/* 563 */     if (westExposed) {
/* 564 */       SurfaceMaterial.Sprite sprite = SurfaceMaterial.resolveSprite(state, Direction.WEST);
/* 565 */       int tint = resolveTint(savedChunks, sprite, x, y, z);
/* 566 */       LodRegionMesh.emitSkirtQuad(sink, x0, z0, x0, z1, yTop, yTop, yBottom, sprite, tint, 0.6F * slopeShade, NO_FADE, -1.0F, 0.0F, 0.0F, false, vertexCount);
/*     */     } 
/*     */     
/* 569 */     if (eastExposed) {
/* 570 */       SurfaceMaterial.Sprite sprite = SurfaceMaterial.resolveSprite(state, Direction.EAST);
/* 571 */       int tint = resolveTint(savedChunks, sprite, x, y, z);
/* 572 */       LodRegionMesh.emitSkirtQuad(sink, x1, z0, x1, z1, yTop, yTop, yBottom, sprite, tint, 0.6F * slopeShade, NO_FADE, 1.0F, 0.0F, 0.0F, false, vertexCount);
/*     */     } 
/*     */     
/* 575 */     if (northExposed) {
/* 576 */       SurfaceMaterial.Sprite sprite = SurfaceMaterial.resolveSprite(state, Direction.NORTH);
/* 577 */       int tint = resolveTint(savedChunks, sprite, x, y, z);
/* 578 */       LodRegionMesh.emitSkirtQuad(sink, x0, z0, x1, z0, yTop, yTop, yBottom, sprite, tint, 0.8F * slopeShade, NO_FADE, 0.0F, 0.0F, -1.0F, false, vertexCount);
/*     */     } 
/*     */     
/* 581 */     if (southExposed) {
/* 582 */       SurfaceMaterial.Sprite sprite = SurfaceMaterial.resolveSprite(state, Direction.SOUTH);
/* 583 */       int tint = resolveTint(savedChunks, sprite, x, y, z);
/* 584 */       LodRegionMesh.emitSkirtQuad(sink, x0, z1, x1, z1, yTop, yTop, yBottom, sprite, tint, 0.8F * slopeShade, NO_FADE, 0.0F, 0.0F, 1.0F, false, vertexCount);
/*     */     } 
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
/*     */   private static int resolveTint(SavedChunkAccess savedChunks, SurfaceMaterial.Sprite sprite, int x, int y, int z) {
/* 600 */     if (!sprite.tinted()) {
/* 601 */       return 16777215;
/*     */     }
/* 603 */     Holder<Biome> biome = savedChunks.biomeAt(x, y, z);
/* 604 */     return (biome != null) ? ((Biome)biome.m_203334_()).m_47464_(x, z) : 16777215;
/*     */   }
/*     */ 
/*     */   
/*     */   private static HeightmapColumn neighborColumn(int[][] heights, int lx, int lz) {
/* 609 */     if (lx < 0 || lz < 0 || lx >= heights.length || lz >= (heights[0]).length) {
/* 610 */       return null;
/*     */     }
/* 612 */     int height = heights[lx][lz];
/* 613 */     if (height == Integer.MIN_VALUE) {
/* 614 */       return null;
/*     */     }
/* 616 */     return new HeightmapColumn(height, 0, 16777215, false);
/*     */   }
/*     */ }


/* Location:              C:\Users\Walker\Downloads\ecstatic-forge-1.20.1-1.3.0.jar!\com\angryalchemist\ecstatic\render\LodStructureIslands.class
 * Java compiler version: 17 (61.0)
 * JD-Core Version:       1.1.3
 */