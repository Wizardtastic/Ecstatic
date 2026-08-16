/*     */ package com.angryalchemist.ecstatic;
/*     */ 
/*     */ import com.angryalchemist.ecstatic.lod.RegionLodCoordinator;
/*     */ import com.angryalchemist.ecstatic.lod.RingConfig;
/*     */ import com.angryalchemist.ecstatic.platform.Services;
/*     */ import com.angryalchemist.ecstatic.render.LodSettingsConfig;
/*     */ import com.angryalchemist.ecstatic.sample.FixedHeightAccessor;
/*     */ import com.angryalchemist.ecstatic.storage.LodStoragePaths;
/*     */ import java.io.IOException;
/*     */ import java.nio.file.Path;
/*     */ import net.minecraft.core.Registry;
/*     */ import net.minecraft.core.registries.Registries;
/*     */ import net.minecraft.server.MinecraftServer;
/*     */ import net.minecraft.server.level.ServerLevel;
/*     */ import net.minecraft.world.level.LevelHeightAccessor;
/*     */ import net.minecraft.world.level.biome.Biome;
/*     */ import net.minecraft.world.level.chunk.ChunkGenerator;
/*     */ import net.minecraft.world.level.levelgen.RandomState;
/*     */ import net.minecraft.world.level.storage.LevelResource;
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
/*     */ public class Ecstatic
/*     */ {
/*     */   private static final int LOD1_RING_WIDTH_CHUNKS = 28;
/*     */   private static final int LOD2_RING_WIDTH_CHUNKS = 32;
/*     */   private static final int LOD3_RING_WIDTH_CHUNKS = 64;
/*     */   private static final int LOD4_RING_WIDTH_CHUNKS = 12;
/*     */   private static final int LOD5_RING_WIDTH_CHUNKS = 24;
/*     */   private static final int RING_HYSTERESIS_CHUNKS = 4;
/*     */   private static volatile MinecraftServer startingServer;
/*     */   private static volatile RegionLodCoordinator activeCoordinator;
/*     */   
/*     */   public static RingConfig currentRingConfig(int clientRenderDistanceChunks) {
/*  62 */     int ring1StartChunks = RingConfig.ring1StartChunks(clientRenderDistanceChunks);
/*  63 */     return RingConfig.scaled(ring1StartChunks, 28, 32, 64, 12, 24, 4, 
/*     */         
/*  65 */         LodSettingsConfig.get().lodRenderDistanceScale());
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
/*     */   public static RegionLodCoordinator activeCoordinator() {
/*  88 */     return activeCoordinator;
/*     */   }
/*     */   
/*     */   public static void init() {
/*  92 */     Constants.LOG.info("Ecstatic initializing on {} ({})", Services.PLATFORM
/*  93 */         .getPlatformName(), Services.PLATFORM.getEnvironmentName());
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
/*     */   public static synchronized void onServerStarted(ServerLevel overworld) {
/* 114 */     MinecraftServer server = overworld.m_7654_();
/* 115 */     if (startingServer == server) {
/* 116 */       Constants.LOG.warn("Ecstatic dynamic coordinator already starting for this server, skipping duplicate run");
/*     */       return;
/*     */     } 
/* 119 */     startingServer = server;
/*     */     
/* 121 */     Path storageDir = LodStoragePaths.dimensionStorageDir(overworld
/* 122 */         .m_7654_().m_129843_(LevelResource.f_78182_), overworld.m_46472_());
/*     */ 
/*     */     
/* 125 */     int workerThreadCount = LodSettingsConfig.get().workerThreadCount();
/*     */     
/* 127 */     ChunkGenerator generator = overworld.m_7726_().m_8481_();
/* 128 */     RandomState randomState = overworld.m_7726_().m_214994_();
/* 129 */     Registry<Biome> biomeRegistry = overworld.m_9598_().m_175515_(Registries.f_256952_);
/* 130 */     FixedHeightAccessor fixedHeightAccessor = new FixedHeightAccessor(overworld.m_141937_(), overworld.m_151558_());
/*     */     
/* 132 */     Thread startupThread = new Thread(() -> {
/*     */ 
/*     */           
/*     */           try {
/*     */             activeCoordinator = new RegionLodCoordinator(generator, randomState, biomeRegistry, heightAccessor, storageDir, 28, 32, 64, 12, 24, 4, workerThreadCount);
/* 137 */           } catch (IOException e) {
/*     */             Constants.LOG.error("Ecstatic failed to start the dynamic region coordinator", e);
/*     */           } finally {
/*     */             startingServer = null;
/*     */           } 
/*     */         }"Ecstatic-CoordinatorStartup");
/* 143 */     startupThread.setDaemon(true);
/* 144 */     startupThread.start();
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   public static synchronized void onServerStopping(MinecraftServer server) {
/* 155 */     RegionLodCoordinator coordinator = activeCoordinator;
/* 156 */     if (coordinator != null) {
/* 157 */       activeCoordinator = null;
/* 158 */       coordinator.shutdown();
/*     */     } 
/*     */   }
/*     */ }


/* Location:              C:\Users\Walker\Downloads\ecstatic-forge-1.20.1-1.3.0.jar!\com\angryalchemist\ecstatic\Ecstatic.class
 * Java compiler version: 17 (61.0)
 * JD-Core Version:       1.1.3
 */