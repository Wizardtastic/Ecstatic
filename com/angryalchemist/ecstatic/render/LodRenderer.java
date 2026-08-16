/*      */ package com.angryalchemist.ecstatic.render;
/*      */ 
/*      */ import com.angryalchemist.ecstatic.Constants;
/*      */ import com.angryalchemist.ecstatic.Ecstatic;
/*      */ import com.angryalchemist.ecstatic.debug.LodDebugState;
/*      */ import com.angryalchemist.ecstatic.lod.RegionCoord;
/*      */ import com.angryalchemist.ecstatic.lod.RegionLodCoordinator;
/*      */ import com.angryalchemist.ecstatic.lod.RingConfig;
/*      */ import com.angryalchemist.ecstatic.storage.LodRegionFile;
/*      */ import com.angryalchemist.ecstatic.storage.LodStoragePaths;
/*      */ import com.mojang.blaze3d.systems.RenderSystem;
/*      */ import com.mojang.math.Axis;
/*      */ import java.nio.file.Path;
/*      */ import java.util.ArrayList;
/*      */ import java.util.HashMap;
/*      */ import java.util.Iterator;
/*      */ import java.util.LinkedHashSet;
/*      */ import java.util.List;
/*      */ import java.util.Map;
/*      */ import java.util.Set;
/*      */ import java.util.concurrent.ConcurrentLinkedQueue;
/*      */ import java.util.concurrent.TimeUnit;
/*      */ import net.minecraft.client.Camera;
/*      */ import net.minecraft.client.Minecraft;
/*      */ import net.minecraft.client.multiplayer.ClientLevel;
/*      */ import net.minecraft.client.server.IntegratedServer;
/*      */ import net.minecraft.core.Registry;
/*      */ import net.minecraft.core.registries.Registries;
/*      */ import net.minecraft.resources.ResourceKey;
/*      */ import net.minecraft.server.MinecraftServer;
/*      */ import net.minecraft.util.Mth;
/*      */ import net.minecraft.world.entity.Entity;
/*      */ import net.minecraft.world.entity.LivingEntity;
/*      */ import net.minecraft.world.entity.player.Player;
/*      */ import net.minecraft.world.level.Level;
/*      */ import net.minecraft.world.level.biome.Biome;
/*      */ import net.minecraft.world.level.material.FogType;
/*      */ import net.minecraft.world.level.storage.LevelResource;
/*      */ import net.minecraft.world.phys.Vec3;
/*      */ import org.joml.FrustumIntersection;
/*      */ import org.joml.Matrix4f;
/*      */ import org.joml.Matrix4fc;
/*      */ import org.joml.Quaternionfc;
/*      */ import org.joml.Vector3f;
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ public final class LodRenderer
/*      */ {
/*      */   private static final int BOOTSTRAP_LEVEL = 0;
/*   69 */   private static final int[] LOD_LEVELS = new int[] { 0, 1, 2, 3, 4, 5 };
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */   
/*      */   private static final int ALPHA_FADE_WIDTH_CHUNKS = 12;
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */   
/*      */   static final int EDGE_SAFETY_MARGIN_CHUNKS = 4;
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */   
/*      */   private static final int FADE_REFRESH_THRESHOLD_CHUNKS = 2;
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */   
/*  149 */   private static final long FADE_REFRESH_TIME_BUDGET_NANOS = TimeUnit.MICROSECONDS.toNanos(1000L);
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */   
/*  190 */   private static final long COORDINATOR_BUILD_TIME_BUDGET_NANOS = TimeUnit.MICROSECONDS.toNanos(1500L);
/*      */ 
/*      */ 
/*      */   
/*      */   private static final int SHADER_ACTIVE_BUDGET_DIVISOR = 3;
/*      */ 
/*      */ 
/*      */   
/*      */   private static final float SKY_DARKEN_MIN = 0.2F;
/*      */ 
/*      */ 
/*      */   
/*      */   private static final float SKY_DARKEN_MAX = 1.0F;
/*      */ 
/*      */ 
/*      */   
/*      */   private static final float MAX_FOG_AT_FAR_EDGE = 0.45F;
/*      */ 
/*      */   
/*      */   private static final float PLACEHOLDER_FOG_GAP_SCALE = 0.5F;
/*      */ 
/*      */   
/*      */   private static volatile Matrix4f capturedTrueRotation;
/*      */ 
/*      */   
/*      */   private static MinecraftServer loadedServer;
/*      */ 
/*      */   
/*      */   private static ResourceKey<Level> loadedDimension;
/*      */ 
/*      */   
/*      */   private static Path loadedStorageDir;
/*      */ 
/*      */ 
/*      */   
/*      */   private static long frameBudgetNanos(long normalBudgetNanos) {
/*  226 */     if (!IrisCompat.isShaderPackActive()) {
/*  227 */       return normalBudgetNanos;
/*      */     }
/*  229 */     return normalBudgetNanos / 3L;
/*      */   }
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */   
/*  289 */   private static final Map<Integer, Map<RegionCoord, LodRegionMesh>> meshesByLevel = new HashMap<>();
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */   
/*  297 */   private static final Map<RegionCoord, Integer> meshLevelByRegion = new HashMap<>();
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */   
/*  317 */   private static final Map<RegionCoord, Integer> latestDispatchedLevel = new HashMap<>();
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */   
/*  326 */   private static final ConcurrentLinkedQueue<GeometryBuildResult> pendingCoordinatorBuilds = new ConcurrentLinkedQueue<>();
/*      */   
/*  328 */   private static final ConcurrentLinkedQueue<GeometryBuildResult> pendingFadeBuilds = new ConcurrentLinkedQueue<>();
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */   
/*      */   private static Vec3 lastFadeRefreshOrigin;
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */   
/*  355 */   private static final Set<PendingMeshRefresh> pendingFadeRefreshes = new LinkedHashSet<>();
/*      */   private static final class PendingMeshRefresh extends Record { private final int level; private final RegionCoord region;
/*  357 */     private PendingMeshRefresh(int level, RegionCoord region) { this.level = level; this.region = region; } public final String toString() { // Byte code:
/*      */       //   0: aload_0
/*      */       //   1: <illegal opcode> toString : (Lcom/angryalchemist/ecstatic/render/LodRenderer$PendingMeshRefresh;)Ljava/lang/String;
/*      */       //   6: areturn
/*      */       // Line number table:
/*      */       //   Java source line number -> byte code offset
/*      */       //   #357	-> 0
/*      */       // Local variable table:
/*      */       //   start	length	slot	name	descriptor
/*  357 */       //   0	7	0	this	Lcom/angryalchemist/ecstatic/render/LodRenderer$PendingMeshRefresh; } public int level() { return this.level; } public final int hashCode() { // Byte code:
/*      */       //   0: aload_0
/*      */       //   1: <illegal opcode> hashCode : (Lcom/angryalchemist/ecstatic/render/LodRenderer$PendingMeshRefresh;)I
/*      */       //   6: ireturn
/*      */       // Line number table:
/*      */       //   Java source line number -> byte code offset
/*      */       //   #357	-> 0
/*      */       // Local variable table:
/*      */       //   start	length	slot	name	descriptor
/*      */       //   0	7	0	this	Lcom/angryalchemist/ecstatic/render/LodRenderer$PendingMeshRefresh; } public final boolean equals(Object o) { // Byte code:
/*      */       //   0: aload_0
/*      */       //   1: aload_1
/*      */       //   2: <illegal opcode> equals : (Lcom/angryalchemist/ecstatic/render/LodRenderer$PendingMeshRefresh;Ljava/lang/Object;)Z
/*      */       //   7: ireturn
/*      */       // Line number table:
/*      */       //   Java source line number -> byte code offset
/*      */       //   #357	-> 0
/*      */       // Local variable table:
/*      */       //   start	length	slot	name	descriptor
/*      */       //   0	8	0	this	Lcom/angryalchemist/ecstatic/render/LodRenderer$PendingMeshRefresh;
/*  357 */       //   0	8	1	o	Ljava/lang/Object; } public RegionCoord region() { return this.region; }
/*      */      } private static final class GeometryBuildResult extends Record { private final RegionCoord region; private final int level; private final LodRegionMesh.RecordedRegionMesh recorded;
/*  359 */     private GeometryBuildResult(RegionCoord region, int level, LodRegionMesh.RecordedRegionMesh recorded) { this.region = region; this.level = level; this.recorded = recorded; } public final String toString() { // Byte code:
/*      */       //   0: aload_0
/*      */       //   1: <illegal opcode> toString : (Lcom/angryalchemist/ecstatic/render/LodRenderer$GeometryBuildResult;)Ljava/lang/String;
/*      */       //   6: areturn
/*      */       // Line number table:
/*      */       //   Java source line number -> byte code offset
/*      */       //   #359	-> 0
/*      */       // Local variable table:
/*      */       //   start	length	slot	name	descriptor
/*      */       //   0	7	0	this	Lcom/angryalchemist/ecstatic/render/LodRenderer$GeometryBuildResult; } public final int hashCode() { // Byte code:
/*      */       //   0: aload_0
/*      */       //   1: <illegal opcode> hashCode : (Lcom/angryalchemist/ecstatic/render/LodRenderer$GeometryBuildResult;)I
/*      */       //   6: ireturn
/*      */       // Line number table:
/*      */       //   Java source line number -> byte code offset
/*      */       //   #359	-> 0
/*      */       // Local variable table:
/*      */       //   start	length	slot	name	descriptor
/*      */       //   0	7	0	this	Lcom/angryalchemist/ecstatic/render/LodRenderer$GeometryBuildResult; } public final boolean equals(Object o) { // Byte code:
/*      */       //   0: aload_0
/*      */       //   1: aload_1
/*      */       //   2: <illegal opcode> equals : (Lcom/angryalchemist/ecstatic/render/LodRenderer$GeometryBuildResult;Ljava/lang/Object;)Z
/*      */       //   7: ireturn
/*      */       // Line number table:
/*      */       //   Java source line number -> byte code offset
/*      */       //   #359	-> 0
/*      */       // Local variable table:
/*      */       //   start	length	slot	name	descriptor
/*      */       //   0	8	0	this	Lcom/angryalchemist/ecstatic/render/LodRenderer$GeometryBuildResult;
/*  359 */       //   0	8	1	o	Ljava/lang/Object; } public RegionCoord region() { return this.region; } public int level() { return this.level; } public LodRegionMesh.RecordedRegionMesh recorded() { return this.recorded; }
/*      */      }
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */   
/*      */   public static void captureTrueRotation(Matrix4f matrix) {
/*  380 */     capturedTrueRotation = matrix;
/*      */   }
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */   
/*      */   private static Matrix4f trueRotationMatrix(Camera camera) {
/*  391 */     Matrix4f captured = capturedTrueRotation;
/*  392 */     if (captured != null) {
/*  393 */       return captured;
/*      */     }
/*  395 */     Matrix4f fallback = new Matrix4f();
/*  396 */     fallback.rotate((Quaternionfc)Axis.f_252529_.m_252977_(camera.m_90589_()));
/*  397 */     fallback.rotate((Quaternionfc)Axis.f_252436_.m_252977_(camera.m_90590_() + 180.0F));
/*  398 */     return fallback;
/*      */   }
/*      */   
/*      */   public static void render(Matrix4f projectionMatrix, Camera camera) {
/*  402 */     IntegratedServer integratedServer = Minecraft.m_91087_().m_91092_();
/*  403 */     ClientLevel clientLevel = (Minecraft.m_91087_()).f_91073_;
/*  404 */     if (integratedServer == null || clientLevel == null) {
/*      */       return;
/*      */     }
/*      */     
/*  408 */     ResourceKey<Level> dimension = clientLevel.m_46472_();
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */     
/*  420 */     if (!dimension.equals(Level.f_46428_)) {
/*      */       return;
/*      */     }
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */     
/*  428 */     if (integratedServer != loadedServer || !dimension.equals(loadedDimension)) {
/*  429 */       reload((MinecraftServer)integratedServer, dimension, clientLevel);
/*      */     }
/*      */     
/*  432 */     Vec3 cameraPos = camera.m_90583_();
/*      */     
/*  434 */     Registry<Biome> biomeRegistry = clientLevel.m_9598_().m_175515_(Registries.f_256952_);
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */     
/*  440 */     tickCoordinator(cameraPos, biomeRegistry);
/*  441 */     tickBootstrap(cameraPos, biomeRegistry);
/*  442 */     drainPendingCoordinatorBuilds();
/*  443 */     maybeRefreshFade(cameraPos, biomeRegistry);
/*  444 */     drainPendingFadeBuilds();
/*      */     
/*  446 */     if (LodDebugState.isReferenceQuadEnabled()) {
/*  447 */       LodDebugReferenceQuad.render(camera, projectionMatrix);
/*      */     }
/*      */     
/*  450 */     float partialTick = Minecraft.m_91087_().m_91296_();
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */     
/*  479 */     Matrix4f modelViewMatrix = new Matrix4f();
/*  480 */     applyVanillaCameraBob(modelViewMatrix, partialTick);
/*  481 */     modelViewMatrix.mul((Matrix4fc)trueRotationMatrix(camera));
/*      */ 
/*      */ 
/*      */ 
/*      */     
/*  486 */     Matrix4f rotationOnlyMatrix = new Matrix4f((Matrix4fc)modelViewMatrix);
/*  487 */     modelViewMatrix.translate(-((float)cameraPos.f_82479_), -((float)cameraPos.f_82480_), -((float)cameraPos.f_82481_));
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */     
/*  494 */     float dayFraction = clamp01((clientLevel.m_104805_(partialTick) - 0.2F) / 0.8F);
/*      */     
/*  496 */     float night = LodSettingsConfig.get().nightBrightness();
/*  497 */     float day = LodSettingsConfig.get().dayBrightness();
/*  498 */     float brightness = night + (day - night) * dayFraction;
/*  499 */     RenderSystem.setShaderColor(brightness, brightness, brightness, 1.0F);
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */     
/*  510 */     int clientRenderDistanceChunks = (Minecraft.m_91087_()).f_91066_.m_193772_();
/*      */     
/*  512 */     Matrix4f lodProjectionMatrix = LodFarPlaneProjection.withExtendedFarPlane(projectionMatrix, 
/*  513 */         extendedFarPlaneBlocks(clientRenderDistanceChunks));
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */     
/*  531 */     Matrix4f viewProjectionMatrix = (new Matrix4f((Matrix4fc)lodProjectionMatrix)).mul((Matrix4fc)modelViewMatrix);
/*  532 */     FrustumIntersection frustum = new FrustumIntersection((Matrix4fc)viewProjectionMatrix);
/*  533 */     boolean cullingEnabled = LodSettingsConfig.get().frustumCullingEnabled();
/*  534 */     float minBuildHeight = clientLevel.m_141937_();
/*  535 */     float maxBuildHeight = clientLevel.m_151558_();
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */     
/*  543 */     int forcedLevel = LodDebugState.forcedLevel();
/*  544 */     List<LodRegionMesh> visibleMeshes = new ArrayList<>();
/*  545 */     List<LodRegionMesh> visiblePlaceholderMeshes = new ArrayList<>();
/*  546 */     for (int level : LOD_LEVELS) {
/*  547 */       if (forcedLevel == 0 || level == forcedLevel) {
/*      */ 
/*      */         
/*  550 */         Map<RegionCoord, LodRegionMesh> meshes = meshesByLevel.get(Integer.valueOf(level));
/*  551 */         if (meshes != null) {
/*      */ 
/*      */           
/*  554 */           List<LodRegionMesh> target = (level == 0) ? visiblePlaceholderMeshes : visibleMeshes;
/*  555 */           if (!cullingEnabled) {
/*  556 */             target.addAll(meshes.values());
/*      */           } else {
/*      */             
/*  559 */             for (Map.Entry<RegionCoord, LodRegionMesh> entry : meshes.entrySet()) {
/*  560 */               if (isRegionVisible(frustum, entry.getKey(), minBuildHeight, maxBuildHeight)) {
/*  561 */                 target.add(entry.getValue());
/*      */               }
/*      */             } 
/*      */           } 
/*      */         } 
/*      */       } 
/*      */     } 
/*      */     
/*  569 */     Vector3f sunDirection = LodOceanPlane.sunDirection(clientLevel.m_46942_(partialTick) * 360.0F);
/*  570 */     LodParallaxShader.setSunDirection(sunDirection.x, sunDirection.y, sunDirection.z);
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */     
/*  577 */     LodWaterShader.setSunDirection(sunDirection.x, sunDirection.y, sunDirection.z);
/*  578 */     LodWaterShader.setGameTime(RenderSystem.getShaderGameTime());
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */     
/*  621 */     float savedFogStart = RenderSystem.getShaderFogStart();
/*  622 */     float savedFogEnd = RenderSystem.getShaderFogEnd();
/*  623 */     RingConfig ringConfigForFog = Ecstatic.currentRingConfig(clientRenderDistanceChunks);
/*  624 */     float fogStartBlocks = clientRenderDistanceChunks * 16.0F;
/*  625 */     float lod4EdgeBlocks = ringConfigForFog.outerBoundary(5) * 16.0F;
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */     
/*  638 */     float fogGapBlocks = (lod4EdgeBlocks - fogStartBlocks) / 0.45F * LodSettingsConfig.get().fogFalloffScale();
/*  639 */     float fogEndBlocks = fogStartBlocks + fogGapBlocks;
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */     
/*  646 */     float placeholderFogEndBlocks = fogStartBlocks + fogGapBlocks * 0.5F;
/*  647 */     RenderSystem.setShaderFogStart(fogStartBlocks);
/*  648 */     RenderSystem.setShaderFogEnd(fogEndBlocks);
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */     
/*  654 */     float fogIntensity = LodSettingsConfig.get().fogIntensity();
/*  655 */     LodFogShader.setFogIntensity(fogIntensity);
/*  656 */     LodTreeShader.setFogIntensity(fogIntensity);
/*  657 */     LodWaterShader.setFogIntensity(fogIntensity);
/*      */ 
/*      */ 
/*      */ 
/*      */     
/*  662 */     LodFogShader.setSaturation(LodSettingsConfig.get().saturationReduction());
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */     
/*  671 */     boolean cameraUnderwater = (camera.m_167685_() == FogType.WATER);
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */     
/*  678 */     if (!cameraUnderwater) {
/*  679 */       Object terrainPhaseToken = IrisCompat.beginTerrainPhase();
/*  680 */       LodRegionMesh.renderTerrainLit(visibleMeshes, modelViewMatrix, lodProjectionMatrix);
/*  681 */       RenderSystem.setShaderFogEnd(placeholderFogEndBlocks);
/*  682 */       LodRegionMesh.renderTerrainLit(visiblePlaceholderMeshes, modelViewMatrix, lodProjectionMatrix);
/*  683 */       RenderSystem.setShaderFogEnd(fogEndBlocks);
/*  684 */       IrisCompat.endPhase(terrainPhaseToken);
/*      */     } 
/*      */     
/*  687 */     Object neutralPhaseToken = IrisCompat.beginNeutralPhase();
/*  688 */     if (!cameraUnderwater) {
/*  689 */       LodRegionMesh.renderTerrainCheap(visibleMeshes, modelViewMatrix, lodProjectionMatrix);
/*      */ 
/*      */ 
/*      */ 
/*      */       
/*  694 */       LodRegionMesh.renderTrees(visibleMeshes, modelViewMatrix, lodProjectionMatrix);
/*      */       
/*  696 */       RenderSystem.setShaderFogEnd(placeholderFogEndBlocks);
/*  697 */       LodRegionMesh.renderTerrainCheap(visiblePlaceholderMeshes, modelViewMatrix, lodProjectionMatrix);
/*  698 */       RenderSystem.setShaderFogEnd(fogEndBlocks);
/*      */     } 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */     
/*  713 */     LodRegionMesh.renderWaterCheap(visibleMeshes, modelViewMatrix, lodProjectionMatrix);
/*      */     
/*  715 */     RenderSystem.setShaderFogEnd(placeholderFogEndBlocks);
/*  716 */     LodRegionMesh.renderWaterCheap(visiblePlaceholderMeshes, modelViewMatrix, lodProjectionMatrix);
/*  717 */     RenderSystem.setShaderFogEnd(fogEndBlocks);
/*      */     
/*  719 */     IrisCompat.endPhase(neutralPhaseToken);
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */     
/*  729 */     Object translucentPhaseToken = IrisCompat.beginTranslucentPhase();
/*  730 */     LodRegionMesh.renderWaterLit(visibleMeshes, modelViewMatrix, lodProjectionMatrix);
/*  731 */     RenderSystem.setShaderFogEnd(placeholderFogEndBlocks);
/*  732 */     LodRegionMesh.renderWaterLit(visiblePlaceholderMeshes, modelViewMatrix, lodProjectionMatrix);
/*  733 */     RenderSystem.setShaderFogEnd(fogEndBlocks);
/*  734 */     IrisCompat.endPhase(translucentPhaseToken);
/*      */     
/*  736 */     RenderSystem.setShaderFogStart(savedFogStart);
/*  737 */     RenderSystem.setShaderFogEnd(savedFogEnd);
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */     
/*  744 */     int lod2OuterBlocks = ringConfigForFog.outerBoundary(2) * 16;
/*  745 */     LodCloudExtension.render(rotationOnlyMatrix, lodProjectionMatrix, clientLevel, camera, partialTick, lod2OuterBlocks);
/*      */ 
/*      */ 
/*      */ 
/*      */     
/*  750 */     LodBedrockPlane.render(modelViewMatrix, lodProjectionMatrix, clientLevel, cameraPos);
/*      */ 
/*      */ 
/*      */ 
/*      */     
/*  755 */     LodStructureIslands.render(modelViewMatrix, lodProjectionMatrix, clientLevel, cameraPos);
/*      */     
/*  757 */     RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
/*      */   }
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */   
/*      */   private static boolean isRegionVisible(FrustumIntersection frustum, RegionCoord region, float minBuildHeight, float maxBuildHeight) {
/*  775 */     float minX = region.originBlockX();
/*  776 */     float minZ = region.originBlockZ();
/*  777 */     float maxX = minX + 512.0F;
/*  778 */     float maxZ = minZ + 512.0F;
/*  779 */     return frustum.testAab(minX, minBuildHeight, minZ, maxX, maxBuildHeight, maxZ);
/*      */   }
/*      */   
/*      */   private static float clamp01(float v) {
/*  783 */     return Math.max(0.0F, Math.min(1.0F, v));
/*      */   }
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */   
/*      */   private static void applyVanillaCameraBob(Matrix4f modelViewMatrix, float partialTick) {
/*  815 */     applyHurtBob(modelViewMatrix, partialTick);
/*  816 */     if (((Boolean)(Minecraft.m_91087_()).f_91066_.m_231830_().m_231551_()).booleanValue()) {
/*  817 */       applyViewBob(modelViewMatrix, partialTick);
/*      */     }
/*      */   }
/*      */   
/*      */   private static void applyHurtBob(Matrix4f modelViewMatrix, float partialTick) {
/*      */     LivingEntity livingEntity;
/*  823 */     Entity entity = Minecraft.m_91087_().m_91288_(); if (entity instanceof LivingEntity) { livingEntity = (LivingEntity)entity; }
/*      */     else
/*      */     { return; }
/*  826 */      if (livingEntity.m_21224_()) {
/*  827 */       float deathTime = Math.min(livingEntity.f_20919_ + partialTick, 20.0F);
/*  828 */       modelViewMatrix.rotate((Quaternionfc)Axis.f_252403_.m_252977_(40.0F - 8000.0F / (deathTime + 200.0F)));
/*      */     } 
/*  830 */     float hurtTime = livingEntity.f_20916_ - partialTick;
/*  831 */     if (hurtTime < 0.0F) {
/*      */       return;
/*      */     }
/*  834 */     hurtTime /= livingEntity.f_20917_;
/*  835 */     hurtTime = Mth.m_14031_(hurtTime * hurtTime * hurtTime * hurtTime * 3.1415927F);
/*  836 */     float hurtDir = livingEntity.m_264297_();
/*  837 */     modelViewMatrix.rotate((Quaternionfc)Axis.f_252436_.m_252977_(-hurtDir));
/*  838 */     modelViewMatrix.rotate((Quaternionfc)Axis.f_252403_.m_252977_(-hurtTime * 14.0F));
/*  839 */     modelViewMatrix.rotate((Quaternionfc)Axis.f_252436_.m_252977_(hurtDir));
/*      */   }
/*      */   
/*      */   private static void applyViewBob(Matrix4f modelViewMatrix, float partialTick) {
/*      */     Player player;
/*  844 */     Entity entity = Minecraft.m_91087_().m_91288_(); if (entity instanceof Player) { player = (Player)entity; }
/*      */     else
/*      */     { return; }
/*  847 */      float walkDistDelta = player.f_19787_ - player.f_19867_;
/*  848 */     float bobPhase = -(player.f_19787_ + walkDistDelta * partialTick);
/*  849 */     float bobAmount = Mth.m_14179_(partialTick, player.f_36099_, player.f_36100_);
/*  850 */     modelViewMatrix.translate(Mth.m_14031_(bobPhase * 3.1415927F) * bobAmount * 0.5F, 
/*  851 */         -Math.abs(Mth.m_14089_(bobPhase * 3.1415927F) * bobAmount), 0.0F);
/*  852 */     modelViewMatrix.rotate((Quaternionfc)Axis.f_252403_.m_252977_(Mth.m_14031_(bobPhase * 3.1415927F) * bobAmount * 3.0F));
/*  853 */     modelViewMatrix.rotate((Quaternionfc)Axis.f_252529_.m_252977_(
/*  854 */           Math.abs(Mth.m_14089_(bobPhase * 3.1415927F - 0.2F) * bobAmount) * 5.0F));
/*      */   }
/*      */   
/*      */   private static void reload(MinecraftServer server, ResourceKey<Level> dimension, ClientLevel clientLevel) {
/*  858 */     unload();
/*  859 */     loadedServer = server;
/*  860 */     loadedDimension = dimension;
/*  861 */     loadedStorageDir = LodStoragePaths.dimensionStorageDir(server.m_129843_(LevelResource.f_78182_), dimension);
/*      */     
/*  863 */     int renderDistanceChunks = (Minecraft.m_91087_()).f_91066_.m_193772_();
/*  864 */     float farPlaneBlocks = renderDistanceChunks * 16.0F * 4.0F;
/*  865 */     Constants.LOG.info("Ecstatic renderer: client render distance={} chunks, graphics mode={}, vanilla far clip plane ~= {} blocks (LOD rings beyond this are frustum-clipped)", new Object[] {
/*      */           
/*  867 */           Integer.valueOf(renderDistanceChunks), (Minecraft.m_91087_()).f_91066_.m_232060_().m_231551_(), Float.valueOf(farPlaneBlocks)
/*      */         });
/*      */   }
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */   
/*      */   private static float extendedFarPlaneBlocks(int clientRenderDistanceChunks) {
/*  878 */     RingConfig ringConfig = Ecstatic.currentRingConfig(clientRenderDistanceChunks);
/*  879 */     float ringFarBlocks = (ringConfig.outerBoundary(5) + ringConfig.hysteresisChunks) * 16.0F;
/*  880 */     return Math.max(ringFarBlocks, 4096.0F) * 1.05F;
/*      */   }
/*      */   
/*      */   private static LodRegionMesh.FadeParams fadeParamsAt(Vec3 origin) {
/*  884 */     int clientRenderDistanceChunks = (Minecraft.m_91087_()).f_91066_.m_193772_();
/*  885 */     int ring1StartChunks = RingConfig.ring1StartChunks(clientRenderDistanceChunks);
/*  886 */     float fadeStartBlocks = ring1StartChunks * 16.0F;
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */     
/*  893 */     float fadeEndBlocks = Math.min(fadeStartBlocks + 192.0F, 
/*  894 */         Math.max(fadeStartBlocks + 16.0F, (clientRenderDistanceChunks - 4) * 16.0F));
/*  895 */     return new LodRegionMesh.FadeParams((int)origin.f_82479_, (int)origin.f_82481_, fadeStartBlocks, fadeEndBlocks);
/*      */   }
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */   
/*      */   private static void tickBootstrap(Vec3 cameraPos, Registry<Biome> biomeRegistry) {
/*  914 */     RegionLodCoordinator coordinator = Ecstatic.activeCoordinator();
/*  915 */     if (coordinator == null) {
/*      */       return;
/*      */     }
/*  918 */     List<RegionCoord> readyRegions = coordinator.drainBootstrapReady();
/*  919 */     if (readyRegions.isEmpty()) {
/*      */       return;
/*      */     }
/*  922 */     LodRegionMesh.FadeParams fade = fadeParamsAt(cameraPos);
/*  923 */     LodRegionFile file = coordinator.bootstrapFile();
/*  924 */     double playerChunkX = cameraPos.f_82479_ / 16.0D;
/*  925 */     double playerChunkZ = cameraPos.f_82481_ / 16.0D;
/*      */     
/*  927 */     for (RegionCoord region : readyRegions) {
/*  928 */       latestDispatchedLevel.put(region, Integer.valueOf(0));
/*  929 */       double distanceChunks = region.distanceChunksTo(playerChunkX, playerChunkZ);
/*  930 */       coordinator.submitBackgroundTask(0, distanceChunks, () -> {
/*      */             try {
/*      */               LodRegionMesh.RecordedRegionMesh recorded = LodRegionMesh.buildGeometry(file, region, fade, biomeRegistry);
/*      */               pendingCoordinatorBuilds.add(new GeometryBuildResult(region, 0, recorded));
/*  934 */             } catch (Exception e) {
/*      */               Constants.LOG.error("Ecstatic failed to build bootstrap geometry for region ({}, {})", new Object[] { Integer.valueOf(region.x()), Integer.valueOf(region.z()), e });
/*      */             } 
/*      */           });
/*      */     } 
/*      */   }
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */   
/*      */   private static void tickCoordinator(Vec3 cameraPos, Registry<Biome> biomeRegistry) {
/*  963 */     RegionLodCoordinator coordinator = Ecstatic.activeCoordinator();
/*  964 */     if (coordinator == null) {
/*      */       return;
/*      */     }
/*      */     
/*  968 */     double playerChunkX = cameraPos.f_82479_ / 16.0D;
/*  969 */     double playerChunkZ = cameraPos.f_82481_ / 16.0D;
/*  970 */     coordinator.tick(playerChunkX, playerChunkZ);
/*      */     
/*  972 */     List<RegionLodCoordinator.RegionReadyResult> readyResults = coordinator.drainReady();
/*  973 */     if (readyResults.isEmpty()) {
/*      */       return;
/*      */     }
/*  976 */     LodRegionMesh.FadeParams fade = fadeParamsAt(cameraPos);
/*      */     
/*  978 */     for (RegionLodCoordinator.RegionReadyResult result : readyResults) {
/*  979 */       RegionCoord region = result.region();
/*  980 */       if (result.level() == -1) {
/*  981 */         latestDispatchedLevel.remove(region);
/*  982 */         evictMesh(region);
/*      */         continue;
/*      */       } 
/*  985 */       int level = result.level();
/*  986 */       latestDispatchedLevel.put(region, Integer.valueOf(level));
/*  987 */       LodRegionFile file = coordinator.fileForLevel(level);
/*  988 */       double distanceChunks = region.distanceChunksTo(playerChunkX, playerChunkZ);
/*  989 */       coordinator.submitBackgroundTask(level, distanceChunks, () -> {
/*      */             try {
/*      */               LodRegionMesh.RecordedRegionMesh recorded = LodRegionMesh.buildGeometry(file, region, fade, biomeRegistry);
/*      */               pendingCoordinatorBuilds.add(new GeometryBuildResult(region, level, recorded));
/*  993 */             } catch (Exception e) {
/*      */               Constants.LOG.error("Ecstatic failed to build geometry for region ({}, {}) at LOD{}", new Object[] { Integer.valueOf(region.x()), Integer.valueOf(region.z()), Integer.valueOf(level), e });
/*      */             } 
/*      */           });
/*      */     } 
/*      */   }
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */   
/*      */   private static void drainPendingCoordinatorBuilds() {
/* 1011 */     if (pendingCoordinatorBuilds.isEmpty()) {
/*      */       return;
/*      */     }
/*      */ 
/*      */     
/* 1016 */     long deadlineNanos = System.nanoTime() + frameBudgetNanos(COORDINATOR_BUILD_TIME_BUDGET_NANOS);
/* 1017 */     int processed = 0;
/* 1018 */     while (!pendingCoordinatorBuilds.isEmpty() && (processed == 0 || System.nanoTime() < deadlineNanos)) {
/* 1019 */       processed++;
/* 1020 */       applyGeometryBuildResult(pendingCoordinatorBuilds.poll());
/*      */     } 
/*      */   }
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */   
/*      */   private static void evictMesh(RegionCoord region) {
/* 1035 */     Integer oldLevel = meshLevelByRegion.remove(region);
/* 1036 */     if (oldLevel == null) {
/*      */       return;
/*      */     }
/* 1039 */     Map<RegionCoord, LodRegionMesh> oldMeshes = meshesByLevel.get(oldLevel);
/* 1040 */     if (oldMeshes == null) {
/*      */       return;
/*      */     }
/* 1043 */     LodRegionMesh oldMesh = oldMeshes.remove(region);
/* 1044 */     if (oldMesh != null) {
/* 1045 */       oldMesh.close();
/*      */     }
/*      */   }
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */   
/*      */   private static void applyGeometryBuildResult(GeometryBuildResult result) {
/* 1057 */     Integer expectedLevel = latestDispatchedLevel.get(result.region());
/* 1058 */     if (expectedLevel == null || expectedLevel.intValue() != result.level()) {
/*      */       return;
/*      */     }
/* 1061 */     latestDispatchedLevel.remove(result.region());
/* 1062 */     evictMesh(result.region());
/* 1063 */     LodRegionMesh mesh = LodRegionMesh.upload(result.recorded());
/* 1064 */     ((Map<RegionCoord, LodRegionMesh>)meshesByLevel.computeIfAbsent(Integer.valueOf(result.level()), k -> new HashMap<>())).put(result.region(), mesh);
/* 1065 */     meshLevelByRegion.put(result.region(), Integer.valueOf(result.level()));
/*      */   }
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */   
/*      */   private static void maybeRefreshFade(Vec3 cameraPos, Registry<Biome> biomeRegistry) {
/* 1077 */     if (lastFadeRefreshOrigin == null) {
/* 1078 */       lastFadeRefreshOrigin = cameraPos;
/*      */     } else {
/* 1080 */       double thresholdBlocks = 32.0D;
/* 1081 */       if (cameraPos.m_82557_(lastFadeRefreshOrigin) >= thresholdBlocks * thresholdBlocks) {
/* 1082 */         lastFadeRefreshOrigin = cameraPos;
/* 1083 */         queuePendingFadeRefresh(0);
/* 1084 */         queuePendingFadeRefresh(1);
/*      */       } 
/*      */     } 
/* 1087 */     drainPendingFadeRefreshes(cameraPos, biomeRegistry);
/*      */   }
/*      */ 
/*      */   
/*      */   private static void queuePendingFadeRefresh(int level) {
/* 1092 */     Map<RegionCoord, LodRegionMesh> meshes = meshesByLevel.get(Integer.valueOf(level));
/* 1093 */     if (meshes == null || meshes.isEmpty()) {
/*      */       return;
/*      */     }
/* 1096 */     for (RegionCoord region : meshes.keySet()) {
/* 1097 */       pendingFadeRefreshes.add(new PendingMeshRefresh(level, region));
/*      */     }
/*      */   }
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */   
/*      */   private static void drainPendingFadeRefreshes(Vec3 cameraPos, Registry<Biome> biomeRegistry) {
/* 1119 */     if (pendingFadeRefreshes.isEmpty()) {
/*      */       return;
/*      */     }
/* 1122 */     RegionLodCoordinator coordinator = Ecstatic.activeCoordinator();
/* 1123 */     LodRegionMesh.FadeParams fade = fadeParamsAt(cameraPos);
/* 1124 */     double playerChunkX = cameraPos.f_82479_ / 16.0D;
/* 1125 */     double playerChunkZ = cameraPos.f_82481_ / 16.0D;
/* 1126 */     Iterator<PendingMeshRefresh> iterator = pendingFadeRefreshes.iterator();
/* 1127 */     while (iterator.hasNext()) {
/* 1128 */       PendingMeshRefresh pending = iterator.next();
/* 1129 */       iterator.remove();
/* 1130 */       LodRegionFile file = levelFile(pending.level());
/* 1131 */       if (file == null) {
/*      */         continue;
/*      */       }
/* 1134 */       Map<RegionCoord, LodRegionMesh> meshes = meshesByLevel.get(Integer.valueOf(pending.level()));
/* 1135 */       if (meshes == null || !meshes.containsKey(pending.region())) {
/*      */         continue;
/*      */       }
/* 1138 */       RegionCoord region = pending.region();
/* 1139 */       int level = pending.level();
/* 1140 */       latestDispatchedLevel.put(region, Integer.valueOf(level));
/* 1141 */       double distanceChunks = region.distanceChunksTo(playerChunkX, playerChunkZ);
/* 1142 */       Runnable buildTask = () -> {
/*      */           try {
/*      */             LodRegionMesh.RecordedRegionMesh recorded = LodRegionMesh.buildGeometry(file, region, fade, biomeRegistry);
/*      */             pendingFadeBuilds.add(new GeometryBuildResult(region, level, recorded));
/* 1146 */           } catch (Exception e) {
/*      */             Constants.LOG.error("Ecstatic failed to build fade-refresh geometry for region ({}, {}) at LOD{}", new Object[] { Integer.valueOf(region.x()), Integer.valueOf(region.z()), Integer.valueOf(level), e });
/*      */           } 
/*      */         };
/*      */       
/* 1151 */       if (coordinator != null) {
/* 1152 */         coordinator.submitBackgroundTask(level, distanceChunks, buildTask);
/*      */         
/*      */         continue;
/*      */       } 
/* 1156 */       buildTask.run();
/*      */     } 
/*      */   }
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */   
/*      */   private static void drainPendingFadeBuilds() {
/* 1168 */     if (pendingFadeBuilds.isEmpty()) {
/*      */       return;
/*      */     }
/* 1171 */     long deadlineNanos = System.nanoTime() + frameBudgetNanos(FADE_REFRESH_TIME_BUDGET_NANOS);
/* 1172 */     int processed = 0;
/* 1173 */     while (!pendingFadeBuilds.isEmpty() && (processed == 0 || System.nanoTime() < deadlineNanos)) {
/* 1174 */       processed++;
/* 1175 */       applyGeometryBuildResult(pendingFadeBuilds.poll());
/*      */     } 
/*      */   }
/*      */ 
/*      */   
/*      */   private static LodRegionFile levelFile(int level) {
/* 1181 */     RegionLodCoordinator coordinator = Ecstatic.activeCoordinator();
/* 1182 */     if (coordinator == null) {
/* 1183 */       return null;
/*      */     }
/* 1185 */     return (level == 0) ? coordinator.bootstrapFile() : coordinator.fileForLevel(level);
/*      */   }
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */   
/*      */   public static boolean isOverworldLoaded() {
/* 1197 */     ClientLevel clientLevel = (Minecraft.m_91087_()).f_91073_;
/* 1198 */     return (clientLevel != null && clientLevel.m_46472_().equals(Level.f_46428_));
/*      */   }
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */   
/*      */   public static void rebuildAllMeshes() {
/* 1210 */     ClientLevel clientLevel = (Minecraft.m_91087_()).f_91073_;
/* 1211 */     if (clientLevel == null) {
/*      */       return;
/*      */     }
/* 1214 */     Registry<Biome> biomeRegistry = clientLevel.m_9598_().m_175515_(Registries.f_256952_);
/* 1215 */     Camera camera = (Minecraft.m_91087_()).f_91063_.m_109153_();
/* 1216 */     LodRegionMesh.FadeParams fade = fadeParamsAt(camera.m_90583_());
/*      */     
/* 1218 */     RegionLodCoordinator coordinator = Ecstatic.activeCoordinator();
/* 1219 */     if (coordinator != null) {
/* 1220 */       refreshLevelMeshes(0, coordinator.bootstrapFile(), fade, biomeRegistry);
/* 1221 */       for (int level = 1; level <= 5; level++) {
/* 1222 */         refreshLevelMeshes(level, coordinator.fileForLevel(level), fade, biomeRegistry);
/*      */       }
/*      */     } 
/*      */   }
/*      */ 
/*      */   
/*      */   private static void refreshLevelMeshes(int level, LodRegionFile file, LodRegionMesh.FadeParams fade, Registry<Biome> biomeRegistry) {
/* 1229 */     if (file == null) {
/*      */       return;
/*      */     }
/* 1232 */     Map<RegionCoord, LodRegionMesh> meshes = meshesByLevel.get(Integer.valueOf(level));
/* 1233 */     if (meshes == null || meshes.isEmpty()) {
/*      */       return;
/*      */     }
/* 1236 */     meshes.replaceAll((region, oldMesh) -> {
/*      */           LodRegionMesh newMesh = LodRegionMesh.build(file, region, fade, biomeRegistry);
/*      */           oldMesh.close();
/*      */           return newMesh;
/*      */         });
/*      */   }
/*      */   
/*      */   private static void unload() {
/* 1244 */     for (Map<RegionCoord, LodRegionMesh> meshes : meshesByLevel.values()) {
/* 1245 */       for (LodRegionMesh mesh : meshes.values()) {
/* 1246 */         mesh.close();
/*      */       }
/*      */     } 
/* 1249 */     meshesByLevel.clear();
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */     
/* 1255 */     meshLevelByRegion.clear();
/* 1256 */     loadedDimension = null;
/* 1257 */     loadedStorageDir = null;
/* 1258 */     lastFadeRefreshOrigin = null;
/* 1259 */     pendingFadeRefreshes.clear();
/*      */ 
/*      */ 
/*      */ 
/*      */     
/* 1264 */     pendingCoordinatorBuilds.clear();
/* 1265 */     pendingFadeBuilds.clear();
/* 1266 */     latestDispatchedLevel.clear();
/*      */   }
/*      */ }


/* Location:              C:\Users\Walker\Downloads\ecstatic-forge-1.20.1-1.3.0.jar!\com\angryalchemist\ecstatic\render\LodRenderer.class
 * Java compiler version: 17 (61.0)
 * JD-Core Version:       1.1.3
 */