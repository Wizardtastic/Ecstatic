/*     */ package com.angryalchemist.ecstatic.render;
/*     */ 
/*     */ import com.angryalchemist.ecstatic.Constants;
/*     */ import com.angryalchemist.ecstatic.debug.LodDebugState;
/*     */ import com.google.gson.Gson;
/*     */ import com.google.gson.GsonBuilder;
/*     */ import java.io.IOException;
/*     */ import java.io.Reader;
/*     */ import java.io.Writer;
/*     */ import java.nio.file.Files;
/*     */ import java.nio.file.Path;
/*     */ import java.nio.file.attribute.FileAttribute;
/*     */ import net.minecraft.client.Minecraft;
/*     */ import net.minecraft.util.Mth;
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
/*     */ 
/*     */ public final class LodSettingsConfig
/*     */ {
/*     */   static final class Data
/*     */   {
/*     */     boolean useLitVertexFormat = false;
/*     */     boolean shaderWaterEnabled = false;
/*     */     boolean frustumCullingEnabled = true;
/*     */     boolean oceanPlaneEnabled = true;
/*     */     boolean opaqueWaterEnabled = true;
/*     */     boolean backfaceCullingEnabled = false;
/*     */     boolean debugToolsEnabled = false;
/* 173 */     float nightBrightness = 0.26F;
/* 174 */     float dayBrightness = 0.77F;
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */     
/* 182 */     float slopeShadingFloor = 0.64F;
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
/* 202 */     float nearSlopeShadingFloor = 0.25F;
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
/* 217 */     float structureSlopeShadingFloor = 0.72F;
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
/* 233 */     float saturationReduction = 0.3F;
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */     
/* 241 */     float sunReliefStrength = 0.21F;
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */     
/* 251 */     float lightTemperature = 0.84F;
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
/* 265 */     int tintGrass = 16777215;
/*     */ 
/*     */ 
/*     */     
/* 269 */     int tintDirt = 13092807;
/* 270 */     int tintStone = 16777215;
/* 271 */     int tintSand = 16777215;
/* 272 */     int tintSnow = 16777215;
/* 273 */     int tintRedSand = 16777215;
/* 274 */     int tintTerracotta = 16777215;
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
/* 294 */     int lod1SubStepBlocks = 2;
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
/* 312 */     float lodRenderDistanceScale = 0.9F;
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
/* 334 */     int workerThreadCount = Math.max(1, Math.round(Runtime.getRuntime().availableProcessors() * 0.8F));
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
/* 348 */     float fogFalloffScale = 1.25F;
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
/* 364 */     float fogIntensity = 1.0F;
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
/* 378 */   private static final Gson GSON = (new GsonBuilder()).setPrettyPrinting().create();
/*     */   
/*     */   private static LodSettingsConfig instance;
/*     */   
/* 382 */   private Data data = new Data();
/*     */   
/*     */   private LodSettingsConfig() {
/* 385 */     load();
/*     */   }
/*     */   
/*     */   public static synchronized LodSettingsConfig get() {
/* 389 */     if (instance == null) {
/* 390 */       instance = new LodSettingsConfig();
/*     */     }
/* 392 */     return instance;
/*     */   }
/*     */   
/*     */   boolean useLitVertexFormat() {
/* 396 */     return this.data.useLitVertexFormat;
/*     */   }
/*     */   
/*     */   void setUseLitVertexFormat(boolean useLitVertexFormat) {
/* 400 */     this.data.useLitVertexFormat = useLitVertexFormat;
/*     */   }
/*     */   
/*     */   boolean shaderWaterEnabled() {
/* 404 */     return this.data.shaderWaterEnabled;
/*     */   }
/*     */   
/*     */   void setShaderWaterEnabled(boolean shaderWaterEnabled) {
/* 408 */     this.data.shaderWaterEnabled = shaderWaterEnabled;
/*     */   }
/*     */   
/*     */   boolean frustumCullingEnabled() {
/* 412 */     return this.data.frustumCullingEnabled;
/*     */   }
/*     */   
/*     */   void setFrustumCullingEnabled(boolean frustumCullingEnabled) {
/* 416 */     this.data.frustumCullingEnabled = frustumCullingEnabled;
/*     */   }
/*     */   
/*     */   boolean oceanPlaneEnabled() {
/* 420 */     return this.data.oceanPlaneEnabled;
/*     */   }
/*     */   
/*     */   void setOceanPlaneEnabled(boolean oceanPlaneEnabled) {
/* 424 */     this.data.oceanPlaneEnabled = oceanPlaneEnabled;
/*     */   }
/*     */   
/*     */   boolean opaqueWaterEnabled() {
/* 428 */     return this.data.opaqueWaterEnabled;
/*     */   }
/*     */   
/*     */   void setOpaqueWaterEnabled(boolean opaqueWaterEnabled) {
/* 432 */     this.data.opaqueWaterEnabled = opaqueWaterEnabled;
/*     */   }
/*     */   
/*     */   boolean backfaceCullingEnabled() {
/* 436 */     return this.data.backfaceCullingEnabled;
/*     */   }
/*     */   
/*     */   float nightBrightness() {
/* 440 */     return this.data.nightBrightness;
/*     */   }
/*     */   
/*     */   void setNightBrightness(float value) {
/* 444 */     this.data.nightBrightness = Mth.m_14036_(value, 0.0F, 1.0F);
/*     */   }
/*     */   
/*     */   float dayBrightness() {
/* 448 */     return this.data.dayBrightness;
/*     */   }
/*     */   
/*     */   void setDayBrightness(float value) {
/* 452 */     this.data.dayBrightness = Mth.m_14036_(value, 0.0F, 1.0F);
/*     */   }
/*     */   
/*     */   float slopeShadingFloor() {
/* 456 */     return this.data.slopeShadingFloor;
/*     */   }
/*     */   
/*     */   void setSlopeShadingFloor(float value) {
/* 460 */     this.data.slopeShadingFloor = Mth.m_14036_(value, 0.0F, 1.0F);
/*     */   }
/*     */   
/*     */   float nearSlopeShadingFloor() {
/* 464 */     return this.data.nearSlopeShadingFloor;
/*     */   }
/*     */   
/*     */   void setNearSlopeShadingFloor(float value) {
/* 468 */     this.data.nearSlopeShadingFloor = Mth.m_14036_(value, 0.0F, 1.0F);
/*     */   }
/*     */   
/*     */   float structureSlopeShadingFloor() {
/* 472 */     return this.data.structureSlopeShadingFloor;
/*     */   }
/*     */   
/*     */   void setStructureSlopeShadingFloor(float value) {
/* 476 */     this.data.structureSlopeShadingFloor = Mth.m_14036_(value, 0.0F, 1.0F);
/*     */   }
/*     */   
/*     */   float saturationReduction() {
/* 480 */     return this.data.saturationReduction;
/*     */   }
/*     */   
/*     */   void setSaturationReduction(float value) {
/* 484 */     this.data.saturationReduction = Mth.m_14036_(value, 0.0F, 1.0F);
/*     */   }
/*     */   
/*     */   float sunReliefStrength() {
/* 488 */     return this.data.sunReliefStrength;
/*     */   }
/*     */   
/*     */   void setSunReliefStrength(float value) {
/* 492 */     this.data.sunReliefStrength = Mth.m_14036_(value, 0.0F, 1.0F);
/*     */   }
/*     */   
/*     */   float lightTemperature() {
/* 496 */     return this.data.lightTemperature;
/*     */   }
/*     */   
/*     */   void setLightTemperature(float value) {
/* 500 */     this.data.lightTemperature = Mth.m_14036_(value, 0.0F, 1.0F);
/*     */   }
/*     */ 
/*     */   
/*     */   int nearTerrainTint(SurfaceMaterial.Kind kind) {
/* 505 */     switch (kind) { default: throw new IncompatibleClassChangeError();case GRASS: case DIRT: case STONE: case SAND: case SNOW: case RED_SAND: case TERRACOTTA: break; }  return 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */       
/* 512 */       this.data.tintTerracotta;
/*     */   }
/*     */ 
/*     */   
/*     */   void setNearTerrainTint(SurfaceMaterial.Kind kind, int rgb) {
/* 517 */     int value = rgb & 0xFFFFFF;
/* 518 */     switch (kind) { case GRASS:
/* 519 */         this.data.tintGrass = value; break;
/* 520 */       case DIRT: this.data.tintDirt = value; break;
/* 521 */       case STONE: this.data.tintStone = value; break;
/* 522 */       case SAND: this.data.tintSand = value; break;
/* 523 */       case SNOW: this.data.tintSnow = value; break;
/* 524 */       case RED_SAND: this.data.tintRedSand = value; break;
/* 525 */       case TERRACOTTA: this.data.tintTerracotta = value;
/*     */         break; }
/*     */   
/*     */   }
/*     */   boolean debugToolsEnabled() {
/* 530 */     return this.data.debugToolsEnabled;
/*     */   }
/*     */ 
/*     */   
/*     */   void setDebugToolsEnabled(boolean debugToolsEnabled) {
/* 535 */     this.data.debugToolsEnabled = debugToolsEnabled;
/* 536 */     LodDebugState.setEnabled(debugToolsEnabled);
/*     */   }
/*     */   
/*     */   void setBackfaceCullingEnabled(boolean backfaceCullingEnabled) {
/* 540 */     this.data.backfaceCullingEnabled = backfaceCullingEnabled;
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   int lod1SubStepBlocks() {
/* 549 */     return (this.data.lod1SubStepBlocks == 2) ? 2 : 1;
/*     */   }
/*     */   
/*     */   void setLod1SubStepBlocks(int lod1SubStepBlocks) {
/* 553 */     this.data.lod1SubStepBlocks = (lod1SubStepBlocks == 2) ? 2 : 1;
/*     */   }
/*     */   
/*     */   public float lodRenderDistanceScale() {
/* 557 */     return Mth.m_14036_(this.data.lodRenderDistanceScale, 0.25F, 2.0F);
/*     */   }
/*     */   
/*     */   void setLodRenderDistanceScale(float lodRenderDistanceScale) {
/* 561 */     this.data.lodRenderDistanceScale = Mth.m_14036_(lodRenderDistanceScale, 0.25F, 2.0F);
/*     */   }
/*     */   
/*     */   public int workerThreadCount() {
/* 565 */     int maxThreads = Math.max(1, Runtime.getRuntime().availableProcessors());
/* 566 */     return Mth.m_14045_(this.data.workerThreadCount, 1, maxThreads);
/*     */   }
/*     */   
/*     */   void setWorkerThreadCount(int workerThreadCount) {
/* 570 */     int maxThreads = Math.max(1, Runtime.getRuntime().availableProcessors());
/* 571 */     this.data.workerThreadCount = Mth.m_14045_(workerThreadCount, 1, maxThreads);
/*     */   }
/*     */   
/*     */   float fogFalloffScale() {
/* 575 */     return Mth.m_14036_(this.data.fogFalloffScale, 0.25F, 3.0F);
/*     */   }
/*     */   
/*     */   void setFogFalloffScale(float fogFalloffScale) {
/* 579 */     this.data.fogFalloffScale = Mth.m_14036_(fogFalloffScale, 0.25F, 3.0F);
/*     */   }
/*     */   
/*     */   float fogIntensity() {
/* 583 */     return Mth.m_14036_(this.data.fogIntensity, 0.0F, 1.0F);
/*     */   }
/*     */   
/*     */   void setFogIntensity(float fogIntensity) {
/* 587 */     this.data.fogIntensity = Mth.m_14036_(fogIntensity, 0.0F, 1.0F);
/*     */   }
/*     */   
/*     */   void save() {
/*     */     
/* 592 */     try { Files.createDirectories(configDir(), (FileAttribute<?>[])new FileAttribute[0]);
/* 593 */       Writer writer = Files.newBufferedWriter(configFile(), new java.nio.file.OpenOption[0]); 
/* 594 */       try { GSON.toJson(this.data, writer);
/* 595 */         if (writer != null) writer.close();  } catch (Throwable throwable) { if (writer != null)
/* 596 */           try { writer.close(); } catch (Throwable throwable1) { throwable.addSuppressed(throwable1); }   throw throwable; }  } catch (IOException e)
/* 597 */     { Constants.LOG.error("Failed to save LOD settings config", e); }
/*     */   
/*     */   }
/*     */   
/*     */   private void load() {
/* 602 */     Path file = configFile();
/* 603 */     if (!Files.exists(file, new java.nio.file.LinkOption[0]))
/*     */       return; 
/*     */     
/* 606 */     try { Reader reader = Files.newBufferedReader(file); 
/* 607 */       try { Data loaded = (Data)GSON.fromJson(reader, Data.class);
/* 608 */         if (loaded != null) {
/* 609 */           this.data = loaded;
/*     */         }
/*     */ 
/*     */ 
/*     */         
/* 614 */         LodDebugState.setEnabled(this.data.debugToolsEnabled);
/* 615 */         if (reader != null) reader.close();  } catch (Throwable throwable) { if (reader != null) try { reader.close(); } catch (Throwable throwable1) { throwable.addSuppressed(throwable1); }   throw throwable; }  } catch (IOException e)
/* 616 */     { Constants.LOG.error("Failed to load LOD settings config", e); }
/*     */   
/*     */   }
/*     */   
/*     */   private Path configDir() {
/* 621 */     return (Minecraft.m_91087_()).f_91069_.toPath().resolve("config").resolve("ecstatic");
/*     */   }
/*     */   
/*     */   private Path configFile() {
/* 625 */     return configDir().resolve("settings.json");
/*     */   }
/*     */ }


/* Location:              C:\Users\Walker\Downloads\ecstatic-forge-1.20.1-1.3.0.jar!\com\angryalchemist\ecstatic\render\LodSettingsConfig.class
 * Java compiler version: 17 (61.0)
 * JD-Core Version:       1.1.3
 */