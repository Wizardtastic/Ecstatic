/*      */ package com.angryalchemist.ecstatic.render;
/*      */ 
/*      */ import com.angryalchemist.ecstatic.Constants;
/*      */ import com.angryalchemist.ecstatic.debug.LodDebugState;
/*      */ import com.angryalchemist.ecstatic.lod.LodLevel;
/*      */ import com.angryalchemist.ecstatic.lod.RegionCoord;
/*      */ import com.angryalchemist.ecstatic.storage.HeightmapColumn;
/*      */ import com.angryalchemist.ecstatic.storage.LodRegionFile;
/*      */ import com.mojang.blaze3d.systems.RenderSystem;
/*      */ import com.mojang.blaze3d.vertex.BufferBuilder;
/*      */ import com.mojang.blaze3d.vertex.DefaultVertexFormat;
/*      */ import com.mojang.blaze3d.vertex.Tesselator;
/*      */ import com.mojang.blaze3d.vertex.VertexBuffer;
/*      */ import com.mojang.blaze3d.vertex.VertexFormat;
/*      */ import java.util.ArrayList;
/*      */ import java.util.LinkedHashMap;
/*      */ import java.util.List;
/*      */ import java.util.Map;
/*      */ import java.util.function.Supplier;
/*      */ import net.minecraft.client.Minecraft;
/*      */ import net.minecraft.client.multiplayer.ClientLevel;
/*      */ import net.minecraft.client.renderer.GameRenderer;
/*      */ import net.minecraft.client.renderer.RenderType;
/*      */ import net.minecraft.client.renderer.ShaderInstance;
/*      */ import net.minecraft.core.BlockPos;
/*      */ import net.minecraft.core.Direction;
/*      */ import net.minecraft.core.Registry;
/*      */ import net.minecraft.resources.ResourceLocation;
/*      */ import net.minecraft.world.level.ChunkPos;
/*      */ import net.minecraft.world.level.biome.Biome;
/*      */ import net.minecraft.world.level.block.Blocks;
/*      */ import org.joml.Matrix4f;
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ public final class LodRegionMesh
/*      */   implements AutoCloseable
/*      */ {
/*      */   private static final int SEA_LEVEL_Y = 63;
/*      */   private static final float WATER_SURFACE_Y = 62.8F;
/*      */   private static final float ICE_ALPHA = 0.92F;
/*      */   private static final int FALLBACK_MIN_BUILD_HEIGHT = -64;
/*      */   private static final int FALLBACK_MAX_BUILD_HEIGHT = 320;
/*      */   private static final float WATER_DEPTH_FOR_FULL_TINT = 20.0F;
/*      */   private static final float MIN_SLOPE_BRIGHTNESS = 0.72F;
/*      */   private static final float DIRT_SLOPE_START = 0.3F;
/*      */   private static final float DIRT_SLOPE_FULL = 0.9F;
/*      */   private static final float STONE_SLOPE_START = 0.9F;
/*      */   private static final float STONE_SLOPE_FULL = 1.6F;
/*      */   private static final float BEVEL_MAX_HEIGHT_BLOCKS = 3.0F;
/*      */   private static final float SUBDIVIDE_MIN_RANGE_BLOCKS = 1.0F;
/*  142 */   private static final float[] AO_BRIGHTNESS_BY_OCCLUDER_COUNT = new float[] { 1.0F, 0.92F, 0.82F, 0.72F };
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */   
/*      */   private static final int SNOW_COLOR = 15789021;
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */   
/*      */   private static final float SNOW_MAX_BLEND = 0.8F;
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */   
/*      */   private static final int WATER_TINT_COLOR = 3103882;
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */   
/*      */   private static final int DIRT_COLOR = 7165246;
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */   
/*      */   private static final int STONE_COLOR = 9079426;
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */   
/*      */   private static final int SAND_COLOR = 14274464;
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */   
/*      */   static final float UP_SHADE = 1.0F;
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */   
/*      */   static final float NORTH_SOUTH_SHADE = 0.8F;
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */   
/*      */   static final float WEST_EAST_SHADE = 0.6F;
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */   
/*      */   static final float DOWN_SHADE = 0.5F;
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */   
/*      */   private static final float SUN_DIR_X = -0.4082F;
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */   
/*      */   private static final float SUN_DIR_Y = 0.8165F;
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */   
/*      */   private static final float SUN_DIR_Z = -0.4082F;
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */   
/*      */   private static final int BLOCKY_COLOR_QUANTIZE_STEP = 16;
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */   
/*      */   private static final float MAX_BAKED_BRIGHTNESS = 1.35F;
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */   
/*      */   private static final float TEMPERATURE_MAX_CHANNEL_SHIFT = 0.2F;
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */   
/*      */   static final float FLAT_UV = 0.5F;
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */   
/*      */   private static final int MAX_TREE_LOD_LEVEL = 3;
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */   
/*      */   private static final int MAX_BOX_TREE_LOD_LEVEL = 2;
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */   
/*      */   static final float TREE_DENSITY_PER_BLOCK_AREA = 0.006666667F;
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */   
/*      */   private static final float TREE_DENSITY_MULTIPLIER = 2.0F;
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */   
/*      */   private static final float TREE_MAX_SLOPE = 0.3F;
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */   
/*      */   private static final float TRUNK_HALF_WIDTH = 0.5F;
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */   
/*      */   private static final float CANOPY_BASE_HEIGHT_FRACTION = 0.35F;
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */   
/*      */   private static final int FULL_ALPHA = 255;
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */   
/*  302 */   private static final SurfaceMaterial.Sprite FULL_SPRITE = new SurfaceMaterial.Sprite(0.0F, 1.0F, 0.0F, 1.0F, false); private final boolean terrainTextured; private final VertexBuffer terrainBufferOpaque; private final VertexBuffer terrainBufferFade; private final boolean terrainLit;
/*      */   private final VertexBuffer terrainBufferWater;
/*      */   private final boolean waterLit;
/*      */   private final List<TreeMeshPart> treeParts;
/*      */   private static SurfaceMaterial.Sprite cachedWaterSprite;
/*      */   private static final int SHALLOW_WATER_COLOR = 6469340;
/*      */   private static final float WATER_DEPTH_FOR_FULL_COLOR = 40.0F;
/*      */   private static final float SEAFLOOR_DEPTH_BELOW_SURFACE = 3.0F;
/*      */   
/*      */   public static final class FadeParams extends Record { private final int spawnBlockX;
/*      */     private final int spawnBlockZ;
/*      */     private final float fadeStartBlocks;
/*      */     private final float fadeEndBlocks;
/*      */     
/*  316 */     public FadeParams(int spawnBlockX, int spawnBlockZ, float fadeStartBlocks, float fadeEndBlocks) { this.spawnBlockX = spawnBlockX; this.spawnBlockZ = spawnBlockZ; this.fadeStartBlocks = fadeStartBlocks; this.fadeEndBlocks = fadeEndBlocks; } public final String toString() { // Byte code:
/*      */       //   0: aload_0
/*      */       //   1: <illegal opcode> toString : (Lcom/angryalchemist/ecstatic/render/LodRegionMesh$FadeParams;)Ljava/lang/String;
/*      */       //   6: areturn
/*      */       // Line number table:
/*      */       //   Java source line number -> byte code offset
/*      */       //   #316	-> 0
/*      */       // Local variable table:
/*      */       //   start	length	slot	name	descriptor
/*  316 */       //   0	7	0	this	Lcom/angryalchemist/ecstatic/render/LodRegionMesh$FadeParams; } public int spawnBlockX() { return this.spawnBlockX; } public final int hashCode() { // Byte code:
/*      */       //   0: aload_0
/*      */       //   1: <illegal opcode> hashCode : (Lcom/angryalchemist/ecstatic/render/LodRegionMesh$FadeParams;)I
/*      */       //   6: ireturn
/*      */       // Line number table:
/*      */       //   Java source line number -> byte code offset
/*      */       //   #316	-> 0
/*      */       // Local variable table:
/*      */       //   start	length	slot	name	descriptor
/*      */       //   0	7	0	this	Lcom/angryalchemist/ecstatic/render/LodRegionMesh$FadeParams; } public final boolean equals(Object o) { // Byte code:
/*      */       //   0: aload_0
/*      */       //   1: aload_1
/*      */       //   2: <illegal opcode> equals : (Lcom/angryalchemist/ecstatic/render/LodRegionMesh$FadeParams;Ljava/lang/Object;)Z
/*      */       //   7: ireturn
/*      */       // Line number table:
/*      */       //   Java source line number -> byte code offset
/*      */       //   #316	-> 0
/*      */       // Local variable table:
/*      */       //   start	length	slot	name	descriptor
/*      */       //   0	8	0	this	Lcom/angryalchemist/ecstatic/render/LodRegionMesh$FadeParams;
/*  316 */       //   0	8	1	o	Ljava/lang/Object; } public int spawnBlockZ() { return this.spawnBlockZ; } public float fadeStartBlocks() { return this.fadeStartBlocks; } public float fadeEndBlocks() { return this.fadeEndBlocks; }
/*      */      int alphaAt(float worldX, float worldZ) {
/*  318 */       float dx = worldX - this.spawnBlockX;
/*  319 */       float dz = worldZ - this.spawnBlockZ;
/*  320 */       float distanceBlocks = (float)Math.sqrt((dx * dx + dz * dz));
/*  321 */       float t = LodRegionMesh.smoothstep(this.fadeStartBlocks, this.fadeEndBlocks, distanceBlocks);
/*  322 */       return Math.round(t * 255.0F);
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
/*      */     boolean visibleAt(float worldX, float worldZ) {
/*  335 */       float dx = worldX - this.spawnBlockX;
/*  336 */       float dz = worldZ - this.spawnBlockZ;
/*  337 */       float distanceBlocks = (float)Math.sqrt((dx * dx + dz * dz));
/*  338 */       return (distanceBlocks >= this.fadeEndBlocks);
/*      */     } }
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */   
/*      */   private static final class TreeMeshPart
/*      */     extends Record
/*      */   {
/*      */     private final RenderType renderType;
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */     
/*      */     private final VertexBuffer buffer;
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */     
/*      */     private TreeMeshPart(RenderType renderType, VertexBuffer buffer)
/*      */     {
/*  404 */       this.renderType = renderType; this.buffer = buffer; } public final String toString() { // Byte code:
/*      */       //   0: aload_0
/*      */       //   1: <illegal opcode> toString : (Lcom/angryalchemist/ecstatic/render/LodRegionMesh$TreeMeshPart;)Ljava/lang/String;
/*      */       //   6: areturn
/*      */       // Line number table:
/*      */       //   Java source line number -> byte code offset
/*      */       //   #404	-> 0
/*      */       // Local variable table:
/*      */       //   start	length	slot	name	descriptor
/*      */       //   0	7	0	this	Lcom/angryalchemist/ecstatic/render/LodRegionMesh$TreeMeshPart; } public final int hashCode() { // Byte code:
/*      */       //   0: aload_0
/*      */       //   1: <illegal opcode> hashCode : (Lcom/angryalchemist/ecstatic/render/LodRegionMesh$TreeMeshPart;)I
/*      */       //   6: ireturn
/*      */       // Line number table:
/*      */       //   Java source line number -> byte code offset
/*      */       //   #404	-> 0
/*      */       // Local variable table:
/*      */       //   start	length	slot	name	descriptor
/*      */       //   0	7	0	this	Lcom/angryalchemist/ecstatic/render/LodRegionMesh$TreeMeshPart; } public final boolean equals(Object o) { // Byte code:
/*      */       //   0: aload_0
/*      */       //   1: aload_1
/*      */       //   2: <illegal opcode> equals : (Lcom/angryalchemist/ecstatic/render/LodRegionMesh$TreeMeshPart;Ljava/lang/Object;)Z
/*      */       //   7: ireturn
/*      */       // Line number table:
/*      */       //   Java source line number -> byte code offset
/*      */       //   #404	-> 0
/*      */       // Local variable table:
/*      */       //   start	length	slot	name	descriptor
/*      */       //   0	8	0	this	Lcom/angryalchemist/ecstatic/render/LodRegionMesh$TreeMeshPart;
/*  404 */       //   0	8	1	o	Ljava/lang/Object; } public RenderType renderType() { return this.renderType; } public VertexBuffer buffer() { return this.buffer; }
/*      */   
/*      */   }
/*      */ 
/*      */   
/*      */   static final class RecordedPart
/*      */     extends Record
/*      */   {
/*      */     private final RecordedVertexSink sink;
/*      */     
/*      */     private final RecordedVertexSink.Kind kind;
/*      */     
/*      */     private final VertexFormat format;
/*      */     
/*      */     RecordedPart(RecordedVertexSink sink, RecordedVertexSink.Kind kind, VertexFormat format) {
/*  419 */       this.sink = sink; this.kind = kind; this.format = format; } public final String toString() { // Byte code:
/*      */       //   0: aload_0
/*      */       //   1: <illegal opcode> toString : (Lcom/angryalchemist/ecstatic/render/LodRegionMesh$RecordedPart;)Ljava/lang/String;
/*      */       //   6: areturn
/*      */       // Line number table:
/*      */       //   Java source line number -> byte code offset
/*      */       //   #419	-> 0
/*      */       // Local variable table:
/*      */       //   start	length	slot	name	descriptor
/*      */       //   0	7	0	this	Lcom/angryalchemist/ecstatic/render/LodRegionMesh$RecordedPart; } public final int hashCode() { // Byte code:
/*      */       //   0: aload_0
/*      */       //   1: <illegal opcode> hashCode : (Lcom/angryalchemist/ecstatic/render/LodRegionMesh$RecordedPart;)I
/*      */       //   6: ireturn
/*      */       // Line number table:
/*      */       //   Java source line number -> byte code offset
/*      */       //   #419	-> 0
/*      */       // Local variable table:
/*      */       //   start	length	slot	name	descriptor
/*      */       //   0	7	0	this	Lcom/angryalchemist/ecstatic/render/LodRegionMesh$RecordedPart; } public final boolean equals(Object o) { // Byte code:
/*      */       //   0: aload_0
/*      */       //   1: aload_1
/*      */       //   2: <illegal opcode> equals : (Lcom/angryalchemist/ecstatic/render/LodRegionMesh$RecordedPart;Ljava/lang/Object;)Z
/*      */       //   7: ireturn
/*      */       // Line number table:
/*      */       //   Java source line number -> byte code offset
/*      */       //   #419	-> 0
/*      */       // Local variable table:
/*      */       //   start	length	slot	name	descriptor
/*      */       //   0	8	0	this	Lcom/angryalchemist/ecstatic/render/LodRegionMesh$RecordedPart;
/*  419 */       //   0	8	1	o	Ljava/lang/Object; } public RecordedVertexSink sink() { return this.sink; } public RecordedVertexSink.Kind kind() { return this.kind; } public VertexFormat format() { return this.format; }
/*      */      }
/*      */   private static final class RecordedTreePart extends Record { private final RenderType renderType; private final LodRegionMesh.RecordedPart part;
/*      */     
/*  423 */     private RecordedTreePart(RenderType renderType, LodRegionMesh.RecordedPart part) { this.renderType = renderType; this.part = part; } public final String toString() { // Byte code:
/*      */       //   0: aload_0
/*      */       //   1: <illegal opcode> toString : (Lcom/angryalchemist/ecstatic/render/LodRegionMesh$RecordedTreePart;)Ljava/lang/String;
/*      */       //   6: areturn
/*      */       // Line number table:
/*      */       //   Java source line number -> byte code offset
/*      */       //   #423	-> 0
/*      */       // Local variable table:
/*      */       //   start	length	slot	name	descriptor
/*      */       //   0	7	0	this	Lcom/angryalchemist/ecstatic/render/LodRegionMesh$RecordedTreePart; } public final int hashCode() { // Byte code:
/*      */       //   0: aload_0
/*      */       //   1: <illegal opcode> hashCode : (Lcom/angryalchemist/ecstatic/render/LodRegionMesh$RecordedTreePart;)I
/*      */       //   6: ireturn
/*      */       // Line number table:
/*      */       //   Java source line number -> byte code offset
/*      */       //   #423	-> 0
/*      */       // Local variable table:
/*      */       //   start	length	slot	name	descriptor
/*      */       //   0	7	0	this	Lcom/angryalchemist/ecstatic/render/LodRegionMesh$RecordedTreePart; } public final boolean equals(Object o) { // Byte code:
/*      */       //   0: aload_0
/*      */       //   1: aload_1
/*      */       //   2: <illegal opcode> equals : (Lcom/angryalchemist/ecstatic/render/LodRegionMesh$RecordedTreePart;Ljava/lang/Object;)Z
/*      */       //   7: ireturn
/*      */       // Line number table:
/*      */       //   Java source line number -> byte code offset
/*      */       //   #423	-> 0
/*      */       // Local variable table:
/*      */       //   start	length	slot	name	descriptor
/*      */       //   0	8	0	this	Lcom/angryalchemist/ecstatic/render/LodRegionMesh$RecordedTreePart;
/*  423 */       //   0	8	1	o	Ljava/lang/Object; } public RenderType renderType() { return this.renderType; } public LodRegionMesh.RecordedPart part() { return this.part; }
/*      */      }
/*      */ 
/*      */ 
/*      */   
/*      */   static final class RecordedRegionMesh
/*      */     extends Record
/*      */   {
/*      */     private final LodRegionMesh.RecordedPart terrainOpaque;
/*      */     
/*      */     private final LodRegionMesh.RecordedPart terrainFade;
/*      */     
/*      */     private final boolean terrainTextured;
/*      */     
/*      */     private final boolean terrainLit;
/*      */     
/*      */     private final LodRegionMesh.RecordedPart water;
/*      */     
/*      */     private final boolean waterLit;
/*      */     private final List<LodRegionMesh.RecordedTreePart> treeParts;
/*      */     
/*      */     RecordedRegionMesh(LodRegionMesh.RecordedPart terrainOpaque, LodRegionMesh.RecordedPart terrainFade, boolean terrainTextured, boolean terrainLit, LodRegionMesh.RecordedPart water, boolean waterLit, List<LodRegionMesh.RecordedTreePart> treeParts)
/*      */     {
/*  446 */       this.terrainOpaque = terrainOpaque; this.terrainFade = terrainFade; this.terrainTextured = terrainTextured; this.terrainLit = terrainLit; this.water = water; this.waterLit = waterLit; this.treeParts = treeParts; } public final String toString() { // Byte code:
/*      */       //   0: aload_0
/*      */       //   1: <illegal opcode> toString : (Lcom/angryalchemist/ecstatic/render/LodRegionMesh$RecordedRegionMesh;)Ljava/lang/String;
/*      */       //   6: areturn
/*      */       // Line number table:
/*      */       //   Java source line number -> byte code offset
/*      */       //   #446	-> 0
/*      */       // Local variable table:
/*      */       //   start	length	slot	name	descriptor
/*      */       //   0	7	0	this	Lcom/angryalchemist/ecstatic/render/LodRegionMesh$RecordedRegionMesh; } public final int hashCode() { // Byte code:
/*      */       //   0: aload_0
/*      */       //   1: <illegal opcode> hashCode : (Lcom/angryalchemist/ecstatic/render/LodRegionMesh$RecordedRegionMesh;)I
/*      */       //   6: ireturn
/*      */       // Line number table:
/*      */       //   Java source line number -> byte code offset
/*      */       //   #446	-> 0
/*      */       // Local variable table:
/*      */       //   start	length	slot	name	descriptor
/*      */       //   0	7	0	this	Lcom/angryalchemist/ecstatic/render/LodRegionMesh$RecordedRegionMesh; } public final boolean equals(Object o) { // Byte code:
/*      */       //   0: aload_0
/*      */       //   1: aload_1
/*      */       //   2: <illegal opcode> equals : (Lcom/angryalchemist/ecstatic/render/LodRegionMesh$RecordedRegionMesh;Ljava/lang/Object;)Z
/*      */       //   7: ireturn
/*      */       // Line number table:
/*      */       //   Java source line number -> byte code offset
/*      */       //   #446	-> 0
/*      */       // Local variable table:
/*      */       //   start	length	slot	name	descriptor
/*      */       //   0	8	0	this	Lcom/angryalchemist/ecstatic/render/LodRegionMesh$RecordedRegionMesh;
/*  446 */       //   0	8	1	o	Ljava/lang/Object; } public LodRegionMesh.RecordedPart terrainOpaque() { return this.terrainOpaque; } public LodRegionMesh.RecordedPart terrainFade() { return this.terrainFade; } public boolean terrainTextured() { return this.terrainTextured; } public boolean terrainLit() { return this.terrainLit; } public LodRegionMesh.RecordedPart water() { return this.water; } public boolean waterLit() { return this.waterLit; } public List<LodRegionMesh.RecordedTreePart> treeParts() { return this.treeParts; }
/*      */   
/*      */   }
/*      */ 
/*      */ 
/*      */ 
/*      */   
/*      */   private LodRegionMesh(VertexBuffer terrainBufferOpaque, VertexBuffer terrainBufferFade, boolean terrainTextured, boolean terrainLit, VertexBuffer terrainBufferWater, boolean waterLit, List<TreeMeshPart> treeParts) {
/*  454 */     this.terrainBufferOpaque = terrainBufferOpaque;
/*  455 */     this.terrainBufferFade = terrainBufferFade;
/*  456 */     this.terrainTextured = terrainTextured;
/*  457 */     this.terrainLit = terrainLit;
/*  458 */     this.terrainBufferWater = terrainBufferWater;
/*  459 */     this.waterLit = waterLit;
/*  460 */     this.treeParts = treeParts;
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
/*      */   public static LodRegionMesh build(LodRegionFile regionFile, RegionCoord region, FadeParams fade, Registry<Biome> biomeRegistry) {
/*  474 */     return upload(buildGeometry(regionFile, region, fade, biomeRegistry));
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
/*      */   public static RecordedRegionMesh buildGeometry(LodRegionFile regionFile, RegionCoord region, FadeParams fade, Registry<Biome> biomeRegistry) {
/*      */     RecordedPart terrainOpaque, terrainFade;
/*  497 */     boolean textured = (regionFile.lodLevel <= 2);
/*      */ 
/*      */ 
/*      */     
/*  501 */     boolean litFormat = resolveLitFormat();
/*      */ 
/*      */ 
/*      */ 
/*      */     
/*  506 */     LodSettingsConfig config = LodSettingsConfig.get();
/*  507 */     boolean cullSubmergedTerrain = (config.oceanPlaneEnabled() && config.opaqueWaterEnabled());
/*      */ 
/*      */ 
/*      */ 
/*      */     
/*  512 */     int[] opaqueVertexCount = { 0 };
/*  513 */     int[] fadeVertexCount = { 0 };
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */     
/*  521 */     boolean beyondFadeBand = regionBeyondFadeBand(region, regionFile.sampleSpacingBlocks, fade);
/*  522 */     if (textured) {
/*  523 */       terrainOpaque = recordSteppedTerrain(regionFile, region, fade, biomeRegistry, litFormat, true, cullSubmergedTerrain, opaqueVertexCount);
/*      */       
/*  525 */       terrainFade = beyondFadeBand ? null : recordSteppedTerrain(regionFile, region, fade, biomeRegistry, litFormat, false, cullSubmergedTerrain, fadeVertexCount);
/*      */     } else {
/*      */       
/*  528 */       terrainOpaque = recordFacetedTerrain(regionFile, region, fade, biomeRegistry, litFormat, true, cullSubmergedTerrain, opaqueVertexCount);
/*      */       
/*  530 */       terrainFade = beyondFadeBand ? null : recordFacetedTerrain(regionFile, region, fade, biomeRegistry, litFormat, false, cullSubmergedTerrain, fadeVertexCount);
/*      */     } 
/*      */ 
/*      */     
/*  534 */     List<RecordedTreePart> treeParts = List.of();
/*  535 */     int[] treeVertexCount = { 0 };
/*  536 */     if (regionFile.lodLevel >= 1 && regionFile.lodLevel <= 3) {
/*  537 */       treeParts = recordTrees(regionFile, region, fade, biomeRegistry, treeVertexCount, regionFile.lodLevel);
/*      */     }
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */     
/*  547 */     RecordedPart water = null;
/*  548 */     boolean waterLit = false;
/*  549 */     int[] waterVertexCount = { 0 };
/*  550 */     if (config.oceanPlaneEnabled()) {
/*  551 */       waterLit = resolveWaterLitFormat();
/*  552 */       water = recordWater(regionFile, region, fade, waterLit, config.opaqueWaterEnabled(), biomeRegistry, waterVertexCount);
/*      */     } 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */     
/*  561 */     Constants.LOG.debug("Ecstatic mesh: region ({}, {}) level {} origin ({}, {}) recorded {} opaque + {} fade-band + {} tree + {} water vertices", new Object[] {
/*      */ 
/*      */           
/*  564 */           Integer.valueOf(region.x()), Integer.valueOf(region.z()), Integer.valueOf(regionFile.lodLevel), Integer.valueOf(region.originBlockX()), Integer.valueOf(region.originBlockZ()), 
/*  565 */           Integer.valueOf(opaqueVertexCount[0]), Integer.valueOf(fadeVertexCount[0]), Integer.valueOf(treeVertexCount[0]), Integer.valueOf(waterVertexCount[0])
/*      */         });
/*  567 */     return new RecordedRegionMesh(terrainOpaque, terrainFade, textured, litFormat, water, waterLit, treeParts);
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
/*      */   public static LodRegionMesh upload(RecordedRegionMesh recorded) {
/*  579 */     VertexBuffer terrainBufferOpaque = uploadPart(recorded.terrainOpaque());
/*  580 */     VertexBuffer terrainBufferFade = (recorded.terrainFade() == null) ? null : uploadPart(recorded.terrainFade());
/*  581 */     VertexBuffer terrainBufferWater = (recorded.water() == null) ? null : uploadPart(recorded.water());
/*      */     
/*  583 */     List<TreeMeshPart> treeParts = new ArrayList<>(recorded.treeParts().size());
/*  584 */     for (RecordedTreePart treePart : recorded.treeParts()) {
/*  585 */       VertexBuffer buffer = uploadPart(treePart.part());
/*  586 */       if (buffer != null) {
/*  587 */         treeParts.add(new TreeMeshPart(treePart.renderType(), buffer));
/*      */       }
/*      */     } 
/*      */     
/*  591 */     return new LodRegionMesh(terrainBufferOpaque, terrainBufferFade, recorded.terrainTextured(), recorded
/*  592 */         .terrainLit(), terrainBufferWater, recorded.waterLit(), treeParts);
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
/*      */   static VertexBuffer uploadPart(RecordedPart part) {
/*  604 */     BufferBuilder builder = Tesselator.m_85913_().m_85915_();
/*  605 */     builder.m_166779_(VertexFormat.Mode.TRIANGLES, part.format());
/*  606 */     BufferBuilderVertexSink glSink = new BufferBuilderVertexSink(builder);
/*  607 */     part.sink().replayInto(glSink, part.kind());
/*  608 */     return upload(builder, part.sink().count());
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
/*      */   private static boolean regionBeyondFadeBand(RegionCoord region, int spacing, FadeParams fade) {
/*  630 */     int minX = region.originBlockX() - spacing;
/*  631 */     int maxX = region.originBlockX() + 512 + spacing;
/*  632 */     int minZ = region.originBlockZ() - spacing;
/*  633 */     int maxZ = region.originBlockZ() + 512 + spacing;
/*  634 */     float closestX = Math.max(minX, Math.min(fade.spawnBlockX(), maxX));
/*  635 */     float closestZ = Math.max(minZ, Math.min(fade.spawnBlockZ(), maxZ));
/*  636 */     float dx = fade.spawnBlockX() - closestX;
/*  637 */     float dz = fade.spawnBlockZ() - closestZ;
/*  638 */     float minDistance = (float)Math.sqrt((dx * dx + dz * dz));
/*  639 */     return (minDistance >= fade.fadeEndBlocks());
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
/*      */   private static boolean resolveLitFormat() {
/*  660 */     int override = LodDebugState.vertexFormatOverride();
/*  661 */     if (override == 1) {
/*  662 */       return true;
/*      */     }
/*  664 */     if (override == 2) {
/*  665 */       return false;
/*      */     }
/*  667 */     return LodSettingsConfig.get().useLitVertexFormat();
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
/*      */   private static boolean resolveWaterLitFormat() {
/*  685 */     int override = LodDebugState.vertexFormatOverride();
/*  686 */     if (override == 1) {
/*  687 */       return true;
/*      */     }
/*  689 */     if (override == 2) {
/*  690 */       return false;
/*      */     }
/*  692 */     if (LodSettingsConfig.get().useLitVertexFormat()) {
/*  693 */       return true;
/*      */     }
/*  695 */     return (LodSettingsConfig.get().shaderWaterEnabled() && IrisCompat.isShaderPackActive());
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
/*      */   private static RecordedPart recordFacetedTerrain(LodRegionFile regionFile, RegionCoord region, FadeParams fade, Registry<Biome> biomeRegistry, boolean litFormat, boolean opaquePass, boolean cullSubmergedTerrain, int[] vertexCount) {
/*  729 */     int samplesPerAxis = regionFile.samplesPerAxis;
/*  730 */     int spacing = regionFile.sampleSpacingBlocks;
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */     
/*  736 */     VertexFormat format = litFormat ? LodTerrainRenderType.BLOCK_SAFE : DefaultVertexFormat.f_85815_;
/*      */ 
/*      */ 
/*      */ 
/*      */     
/*  741 */     RecordedVertexSink sink = new RecordedVertexSink();
/*  742 */     RecordedVertexSink.Kind kind = litFormat ? RecordedVertexSink.Kind.LIT : RecordedVertexSink.Kind.PLAIN;
/*      */     
/*  744 */     int last = samplesPerAxis - 1;
/*  745 */     for (int lz = 0; lz < last; lz++) {
/*  746 */       for (int j = 0; j < last; j++) {
/*  747 */         emitFacetedCell(sink, regionFile, spacing, samplesPerAxis, vertexCount, fade, biomeRegistry, litFormat, opaquePass, cullSubmergedTerrain, region, j, lz, region, j + 1, lz, region, j, lz + 1, region, j + 1, lz + 1);
/*      */       }
/*      */     } 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */     
/*  758 */     RegionCoord plusX = new RegionCoord(region.x() + 1, region.z());
/*  759 */     for (int i = 0; i < last; i++) {
/*  760 */       emitFacetedCell(sink, regionFile, spacing, samplesPerAxis, vertexCount, fade, biomeRegistry, litFormat, opaquePass, cullSubmergedTerrain, region, last, i, plusX, 0, i, region, last, i + 1, plusX, 0, i + 1);
/*      */     }
/*      */ 
/*      */ 
/*      */ 
/*      */     
/*  766 */     RegionCoord plusZ = new RegionCoord(region.x(), region.z() + 1);
/*  767 */     for (int lx = 0; lx < last; lx++) {
/*  768 */       emitFacetedCell(sink, regionFile, spacing, samplesPerAxis, vertexCount, fade, biomeRegistry, litFormat, opaquePass, cullSubmergedTerrain, region, lx, last, region, lx + 1, last, plusZ, lx, 0, plusZ, lx + 1, 0);
/*      */     }
/*      */ 
/*      */ 
/*      */ 
/*      */     
/*  774 */     RegionCoord plusXZ = new RegionCoord(region.x() + 1, region.z() + 1);
/*  775 */     emitFacetedCell(sink, regionFile, spacing, samplesPerAxis, vertexCount, fade, biomeRegistry, litFormat, opaquePass, cullSubmergedTerrain, region, last, last, plusX, 0, last, plusZ, last, 0, plusXZ, 0, 0);
/*      */ 
/*      */ 
/*      */ 
/*      */     
/*  780 */     return new RecordedPart(sink, kind, format);
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
/*      */   private static RecordedPart recordWater(LodRegionFile regionFile, RegionCoord region, FadeParams fade, boolean litFormat, boolean opaqueWaterEnabled, Registry<Biome> biomeRegistry, int[] vertexCount) {
/*  801 */     int spacing = regionFile.sampleSpacingBlocks;
/*  802 */     int samplesPerAxis = regionFile.samplesPerAxis;
/*  803 */     int waterColor = BiomeStyleConfig.get().waterColor();
/*  804 */     float waterAlpha = opaqueWaterEnabled ? 1.0F : BiomeStyleConfig.get().waterAlpha();
/*  805 */     int iceColor = BiomeStyleConfig.get().iceColor();
/*  806 */     int subdivisions = waterTextureSubdivisions(regionFile.lodLevel);
/*  807 */     SurfaceMaterial.Sprite waterSprite = waterSprite();
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */     
/*  814 */     VertexFormat format = litFormat ? LodTerrainRenderType.BLOCK_SAFE : DefaultVertexFormat.f_85818_;
/*  815 */     RecordedVertexSink sink = new RecordedVertexSink();
/*  816 */     RecordedVertexSink.Kind kind = litFormat ? RecordedVertexSink.Kind.LIT_TEXTURED : RecordedVertexSink.Kind.TEXTURED;
/*      */     
/*  818 */     int last = samplesPerAxis - 1;
/*  819 */     for (int lz = 0; lz < last; lz++) {
/*  820 */       for (int j = 0; j < last; j++) {
/*  821 */         emitWaterCell(sink, regionFile, spacing, vertexCount, fade, litFormat, waterColor, waterAlpha, iceColor, biomeRegistry, subdivisions, waterSprite, opaqueWaterEnabled, region, j, lz, region, j + 1, lz, region, j, lz + 1, region, j + 1, lz + 1);
/*      */       }
/*      */     } 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */     
/*  829 */     RegionCoord plusX = new RegionCoord(region.x() + 1, region.z());
/*  830 */     for (int i = 0; i < last; i++) {
/*  831 */       emitWaterCell(sink, regionFile, spacing, vertexCount, fade, litFormat, waterColor, waterAlpha, iceColor, biomeRegistry, subdivisions, waterSprite, opaqueWaterEnabled, region, last, i, plusX, 0, i, region, last, i + 1, plusX, 0, i + 1);
/*      */     }
/*      */ 
/*      */ 
/*      */     
/*  836 */     RegionCoord plusZ = new RegionCoord(region.x(), region.z() + 1);
/*  837 */     for (int lx = 0; lx < last; lx++) {
/*  838 */       emitWaterCell(sink, regionFile, spacing, vertexCount, fade, litFormat, waterColor, waterAlpha, iceColor, biomeRegistry, subdivisions, waterSprite, opaqueWaterEnabled, region, lx, last, region, lx + 1, last, plusZ, lx, 0, plusZ, lx + 1, 0);
/*      */     }
/*      */ 
/*      */ 
/*      */     
/*  843 */     RegionCoord plusXZ = new RegionCoord(region.x() + 1, region.z() + 1);
/*  844 */     emitWaterCell(sink, regionFile, spacing, vertexCount, fade, litFormat, waterColor, waterAlpha, iceColor, biomeRegistry, subdivisions, waterSprite, opaqueWaterEnabled, region, last, last, plusX, 0, last, plusZ, last, 0, plusXZ, 0, 0);
/*      */ 
/*      */ 
/*      */     
/*  848 */     return new RecordedPart(sink, kind, format);
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
/*      */   private static int waterTextureSubdivisions(int lodLevel) {
/*  870 */     switch (LodLevel.sampleSpacingBlocks(Math.max(1, lodLevel))) { case 8: case 16: case 32:  }  return 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */       
/*  877 */       1;
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
/*      */   private static SurfaceMaterial.Sprite waterSprite() {
/*  898 */     if (cachedWaterSprite == null) {
/*  899 */       cachedWaterSprite = SurfaceMaterial.resolveSprite(Blocks.f_49990_.m_49966_(), Direction.UP);
/*      */     }
/*  901 */     return cachedWaterSprite;
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
/*      */   private static void emitWaterCell(VertexSink sink, LodRegionFile regionFile, int spacing, int[] vertexCount, FadeParams fade, boolean litFormat, int waterColor, float baseAlpha, int iceColor, Registry<Biome> biomeRegistry, int subdivisions, SurfaceMaterial.Sprite sprite, boolean opaqueWaterEnabled, RegionCoord r00, int lx00, int lz00, RegionCoord r10, int lx10, int lz10, RegionCoord r01, int lx01, int lz01, RegionCoord r11, int lx11, int lz11) {
/*  950 */     HeightmapColumn c00 = regionFile.readColumn(r00, lx00, lz00);
/*  951 */     HeightmapColumn c10 = regionFile.readColumn(r10, lx10, lz10);
/*  952 */     HeightmapColumn c01 = regionFile.readColumn(r01, lx01, lz01);
/*  953 */     HeightmapColumn c11 = regionFile.readColumn(r11, lx11, lz11);
/*  954 */     if (c00 == null || c10 == null || c01 == null || c11 == null) {
/*      */       return;
/*      */     }
/*  957 */     if (c00.height() >= 63 && c10.height() >= 63 && c01
/*  958 */       .height() >= 63 && c11.height() >= 63) {
/*      */       return;
/*      */     }
/*      */     
/*  962 */     float x0 = (r00.originBlockX() + lx00 * spacing);
/*  963 */     float z0 = (r00.originBlockZ() + lz00 * spacing);
/*  964 */     float x1 = (r10.originBlockX() + lx10 * spacing);
/*  965 */     float z1 = (r01.originBlockZ() + lz01 * spacing);
/*      */     
/*  967 */     boolean frozen00 = isFrozenWater(biomeRegistry, c00, (int)x0, (int)z0);
/*  968 */     boolean frozen10 = isFrozenWater(biomeRegistry, c10, (int)x1, (int)z0);
/*  969 */     boolean frozen01 = isFrozenWater(biomeRegistry, c01, (int)x0, (int)z1);
/*  970 */     boolean frozen11 = isFrozenWater(biomeRegistry, c11, (int)x1, (int)z1);
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */     
/*  977 */     int color00 = frozen00 ? iceColor : waterColorAt(waterColor, c00);
/*  978 */     int color10 = frozen10 ? iceColor : waterColorAt(waterColor, c10);
/*  979 */     int color01 = frozen01 ? iceColor : waterColorAt(waterColor, c01);
/*  980 */     int color11 = frozen11 ? iceColor : waterColorAt(waterColor, c11);
/*      */     
/*  982 */     int a00 = fadedWaterAlpha(fade, x0, z0, frozen00 ? 0.92F : baseAlpha);
/*  983 */     int a10 = fadedWaterAlpha(fade, x1, z0, frozen10 ? 0.92F : baseAlpha);
/*  984 */     int a01 = fadedWaterAlpha(fade, x0, z1, frozen01 ? 0.92F : baseAlpha);
/*  985 */     int a11 = fadedWaterAlpha(fade, x1, z1, frozen11 ? 0.92F : baseAlpha);
/*  986 */     if (a00 == 0 && a10 == 0 && a01 == 0 && a11 == 0) {
/*      */       return;
/*      */     }
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */     
/*  996 */     if (!opaqueWaterEnabled) {
/*  997 */       emitSeafloorTile(sink, litFormat, sprite, fade, waterColor, x0, z0, x1, z1, c00, c10, c01, c11, vertexCount);
/*      */     }
/*      */ 
/*      */     
/* 1001 */     for (int i = 0; i < subdivisions; i++) {
/* 1002 */       float u0 = i / subdivisions;
/* 1003 */       float u1 = (i + 1) / subdivisions;
/* 1004 */       float sx0 = x0 + (x1 - x0) * u0;
/* 1005 */       float sx1 = x0 + (x1 - x0) * u1;
/*      */       
/* 1007 */       for (int j = 0; j < subdivisions; j++) {
/* 1008 */         float v0 = j / subdivisions;
/* 1009 */         float v1 = (j + 1) / subdivisions;
/* 1010 */         float sz0 = z0 + (z1 - z0) * v0;
/* 1011 */         float sz1 = z0 + (z1 - z0) * v1;
/*      */         
/* 1013 */         int sa00 = bilinearAlpha(a00, a10, a01, a11, u0, v0);
/* 1014 */         int sa10 = bilinearAlpha(a00, a10, a01, a11, u1, v0);
/* 1015 */         int sa01 = bilinearAlpha(a00, a10, a01, a11, u0, v1);
/* 1016 */         int sa11 = bilinearAlpha(a00, a10, a01, a11, u1, v1);
/* 1017 */         if (sa00 != 0 || sa10 != 0 || sa01 != 0 || sa11 != 0) {
/*      */ 
/*      */ 
/*      */           
/* 1021 */           int sc00 = bilinearColor(color00, color10, color01, color11, u0, v0);
/* 1022 */           int sc10 = bilinearColor(color00, color10, color01, color11, u1, v0);
/* 1023 */           int sc01 = bilinearColor(color00, color10, color01, color11, u0, v1);
/* 1024 */           int sc11 = bilinearColor(color00, color10, color01, color11, u1, v1);
/*      */           
/* 1026 */           emitWaterTile(sink, litFormat, sprite, sx0, sz0, sx1, sz1, sc00, sc10, sc01, sc11, sa00, sa10, sa01, sa11, vertexCount);
/*      */         } 
/*      */       } 
/*      */     } 
/*      */   }
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */   
/*      */   private static void emitWaterTile(VertexSink sink, boolean litFormat, SurfaceMaterial.Sprite sprite, float x0, float z0, float x1, float z1, int color00, int color10, int color01, int color11, int a00, int a10, int a01, int a11, int[] vertexCount) {
/* 1037 */     if (litFormat) {
/* 1038 */       sink.litTexturedVertex(x0, 62.8F, z0, sprite.u0(), sprite.v1(), color00, a00, 0.0F, 1.0F, 0.0F);
/* 1039 */       sink.litTexturedVertex(x1, 62.8F, z0, sprite.u1(), sprite.v1(), color10, a10, 0.0F, 1.0F, 0.0F);
/* 1040 */       sink.litTexturedVertex(x1, 62.8F, z1, sprite.u1(), sprite.v0(), color11, a11, 0.0F, 1.0F, 0.0F);
/*      */       
/* 1042 */       sink.litTexturedVertex(x0, 62.8F, z0, sprite.u0(), sprite.v1(), color00, a00, 0.0F, 1.0F, 0.0F);
/* 1043 */       sink.litTexturedVertex(x1, 62.8F, z1, sprite.u1(), sprite.v0(), color11, a11, 0.0F, 1.0F, 0.0F);
/* 1044 */       sink.litTexturedVertex(x0, 62.8F, z1, sprite.u0(), sprite.v0(), color01, a01, 0.0F, 1.0F, 0.0F);
/*      */     } else {
/* 1046 */       sink.texturedVertex(x0, 62.8F, z0, sprite.u0(), sprite.v1(), color00, a00);
/* 1047 */       sink.texturedVertex(x1, 62.8F, z0, sprite.u1(), sprite.v1(), color10, a10);
/* 1048 */       sink.texturedVertex(x1, 62.8F, z1, sprite.u1(), sprite.v0(), color11, a11);
/*      */       
/* 1050 */       sink.texturedVertex(x0, 62.8F, z0, sprite.u0(), sprite.v1(), color00, a00);
/* 1051 */       sink.texturedVertex(x1, 62.8F, z1, sprite.u1(), sprite.v0(), color11, a11);
/* 1052 */       sink.texturedVertex(x0, 62.8F, z1, sprite.u0(), sprite.v0(), color01, a01);
/*      */     } 
/* 1054 */     vertexCount[0] = vertexCount[0] + 6;
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
/*      */   private static int waterColorAt(int configuredDeepColor, HeightmapColumn column) {
/* 1086 */     float depthFactor = clamp01((63 - column.height()) / 40.0F);
/* 1087 */     return blend(6469340, configuredDeepColor, depthFactor);
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
/*      */   private static void emitSeafloorTile(VertexSink sink, boolean litFormat, SurfaceMaterial.Sprite sprite, FadeParams fade, int waterColor, float x0, float z0, float x1, float z1, HeightmapColumn c00, HeightmapColumn c10, HeightmapColumn c01, HeightmapColumn c11, int[] vertexCount) {
/* 1121 */     int color00 = waterColorAt(waterColor, c00);
/* 1122 */     int color10 = waterColorAt(waterColor, c10);
/* 1123 */     int color01 = waterColorAt(waterColor, c01);
/* 1124 */     int color11 = waterColorAt(waterColor, c11);
/*      */ 
/*      */ 
/*      */ 
/*      */     
/* 1129 */     int a00 = fadedWaterAlpha(fade, x0, z0, 1.0F);
/* 1130 */     int a10 = fadedWaterAlpha(fade, x1, z0, 1.0F);
/* 1131 */     int a01 = fadedWaterAlpha(fade, x0, z1, 1.0F);
/* 1132 */     int a11 = fadedWaterAlpha(fade, x1, z1, 1.0F);
/* 1133 */     if (a00 == 0 && a10 == 0 && a01 == 0 && a11 == 0) {
/*      */       return;
/*      */     }
/*      */     
/* 1137 */     float u = (sprite.u0() + sprite.u1()) * 0.5F;
/* 1138 */     float v = (sprite.v0() + sprite.v1()) * 0.5F;
/* 1139 */     float y = 59.8F;
/*      */     
/* 1141 */     if (litFormat) {
/* 1142 */       sink.litTexturedVertex(x0, y, z0, u, v, color00, a00, 0.0F, 1.0F, 0.0F);
/* 1143 */       sink.litTexturedVertex(x1, y, z0, u, v, color10, a10, 0.0F, 1.0F, 0.0F);
/* 1144 */       sink.litTexturedVertex(x1, y, z1, u, v, color11, a11, 0.0F, 1.0F, 0.0F);
/*      */       
/* 1146 */       sink.litTexturedVertex(x0, y, z0, u, v, color00, a00, 0.0F, 1.0F, 0.0F);
/* 1147 */       sink.litTexturedVertex(x1, y, z1, u, v, color11, a11, 0.0F, 1.0F, 0.0F);
/* 1148 */       sink.litTexturedVertex(x0, y, z1, u, v, color01, a01, 0.0F, 1.0F, 0.0F);
/*      */     } else {
/* 1150 */       sink.texturedVertex(x0, y, z0, u, v, color00, a00);
/* 1151 */       sink.texturedVertex(x1, y, z0, u, v, color10, a10);
/* 1152 */       sink.texturedVertex(x1, y, z1, u, v, color11, a11);
/*      */       
/* 1154 */       sink.texturedVertex(x0, y, z0, u, v, color00, a00);
/* 1155 */       sink.texturedVertex(x1, y, z1, u, v, color11, a11);
/* 1156 */       sink.texturedVertex(x0, y, z1, u, v, color01, a01);
/*      */     } 
/* 1158 */     vertexCount[0] = vertexCount[0] + 6;
/*      */   }
/*      */ 
/*      */   
/*      */   private static int bilinearColor(int c00, int c10, int c01, int c11, float u, float v) {
/* 1163 */     int top = blend(c00, c10, u);
/* 1164 */     int bottom = blend(c01, c11, u);
/* 1165 */     return blend(top, bottom, v);
/*      */   }
/*      */ 
/*      */   
/*      */   private static int bilinearAlpha(int a00, int a10, int a01, int a11, float u, float v) {
/* 1170 */     float top = a00 + (a10 - a00) * u;
/* 1171 */     float bottom = a01 + (a11 - a01) * u;
/* 1172 */     return Math.round(top + (bottom - top) * v);
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
/*      */   private static boolean isFrozenWater(Registry<Biome> biomeRegistry, HeightmapColumn column, int blockX, int blockZ) {
/* 1184 */     if (biomeRegistry == null) {
/* 1185 */       return false;
/*      */     }
/* 1187 */     Biome biome = (Biome)biomeRegistry.m_7942_(column.biomeRawId());
/* 1188 */     return (biome != null && biome.m_198904_(new BlockPos(blockX, 62, blockZ)));
/*      */   }
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */   
/*      */   private static int fadedWaterAlpha(FadeParams fade, float worldX, float worldZ, float baseAlpha) {
/* 1198 */     return Math.round(baseAlpha * fade.alphaAt(worldX, worldZ));
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
/*      */   private static boolean allSubmerged(HeightmapColumn... columns) {
/* 1213 */     for (HeightmapColumn column : columns) {
/* 1214 */       if (column == null || column.height() >= 63) {
/* 1215 */         return false;
/*      */       }
/*      */     } 
/* 1218 */     return true;
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
/*      */   private static RecordedPart recordSteppedTerrain(LodRegionFile regionFile, RegionCoord region, FadeParams fade, Registry<Biome> biomeRegistry, boolean litFormat, boolean opaquePass, boolean cullSubmergedTerrain, int[] vertexCount) {
/* 1261 */     int samplesPerAxis = regionFile.samplesPerAxis;
/* 1262 */     int spacing = regionFile.sampleSpacingBlocks;
/* 1263 */     float half = spacing / 2.0F;
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */     
/* 1269 */     int subStep = LodSettingsConfig.get().lod1SubStepBlocks();
/* 1270 */     int sub = (subStep >= 2) ? Math.max(1, spacing / 2) : spacing;
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */     
/* 1277 */     boolean blockyTops = (spacing <= 2);
/*      */ 
/*      */ 
/*      */ 
/*      */     
/* 1282 */     VertexFormat format = litFormat ? LodTerrainRenderType.BLOCK_SAFE : DefaultVertexFormat.f_85818_;
/*      */ 
/*      */ 
/*      */     
/* 1286 */     RecordedVertexSink sink = new RecordedVertexSink();
/*      */     
/* 1288 */     RecordedVertexSink.Kind kind = litFormat ? RecordedVertexSink.Kind.LIT_TEXTURED : RecordedVertexSink.Kind.TEXTURED;
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */     
/* 1299 */     EdgeColumn[][] edgeGrid = new EdgeColumn[samplesPerAxis][samplesPerAxis];
/*      */     
/* 1301 */     for (int lz = 0; lz < samplesPerAxis; lz++) {
/* 1302 */       for (int lx = 0; lx < samplesPerAxis; lx++) {
/* 1303 */         HeightmapColumn column = regionFile.readColumn(region, lx, lz);
/* 1304 */         if (column != null) {
/*      */ 
/*      */ 
/*      */           
/* 1308 */           float cx = (region.originBlockX() + lx * spacing);
/* 1309 */           float cz = (region.originBlockZ() + lz * spacing);
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */           
/* 1318 */           long structureChunk = ChunkPos.m_45589_(Math.floorDiv((int)cx, 16), Math.floorDiv((int)cz, 16));
/* 1319 */           if (!LodStructureIslands.renderedChunks().contains(Long.valueOf(structureChunk))) {
/*      */ 
/*      */ 
/*      */             
/* 1323 */             boolean columnFullyOpaque = (fade.alphaAt(cx, cz) == 255);
/* 1324 */             if (columnFullyOpaque == opaquePass)
/*      */             
/*      */             { 
/*      */               
/* 1328 */               HeightmapColumn west = columnAt(regionFile, region, lx - 1, lz, samplesPerAxis);
/* 1329 */               HeightmapColumn east = columnAt(regionFile, region, lx + 1, lz, samplesPerAxis);
/* 1330 */               HeightmapColumn south = columnAt(regionFile, region, lx, lz - 1, samplesPerAxis);
/* 1331 */               HeightmapColumn north = columnAt(regionFile, region, lx, lz + 1, samplesPerAxis);
/*      */               
/* 1333 */               float dhdx = centralDifference(west, east, column, spacing);
/* 1334 */               float dhdz = centralDifference(south, north, column, spacing);
/* 1335 */               float slope = (float)Math.sqrt((dhdx * dhdx + dhdz * dhdz));
/* 1336 */               SurfaceMaterial material = SurfaceMaterial.classify(column, biomeRegistry, slope, (int)cx, (int)cz);
/*      */ 
/*      */ 
/*      */               
/* 1340 */               int tintColor = applyGroundTint(column, biomeRegistry, column.colorRgb());
/* 1341 */               tintColor = multiplyColor(tintColor, LodSettingsConfig.get().nearTerrainTint(material.kind()));
/*      */               
/* 1343 */               float x0 = cx - half;
/* 1344 */               float x1 = cx + half;
/* 1345 */               float z0 = cz - half;
/* 1346 */               float z1 = cz + half;
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */               
/* 1357 */               HeightmapColumn northwest = columnAt(regionFile, region, lx - 1, lz + 1, samplesPerAxis);
/* 1358 */               HeightmapColumn northeast = columnAt(regionFile, region, lx + 1, lz + 1, samplesPerAxis);
/* 1359 */               HeightmapColumn southwest = columnAt(regionFile, region, lx - 1, lz - 1, samplesPerAxis);
/* 1360 */               HeightmapColumn southeast = columnAt(regionFile, region, lx + 1, lz - 1, samplesPerAxis);
/*      */ 
/*      */ 
/*      */ 
/*      */               
/* 1365 */               if (!cullSubmergedTerrain || 
/* 1366 */                 !allSubmerged(new HeightmapColumn[] { column, east, north, northeast
/* 1367 */                   }) || !allSubmerged(new HeightmapColumn[] { west, column, northwest, north
/* 1368 */                   }) || !allSubmerged(new HeightmapColumn[] { south, southeast, column, east
/* 1369 */                   }) || !allSubmerged(new HeightmapColumn[] { southwest, south, west, column }))
/*      */               
/*      */               { 
/*      */                 
/* 1373 */                 float ySW, ySE, yNE, yNW, skirtBottom = lowestNeighborHeight(column.height(), new HeightmapColumn[] { west, east, south, north, northwest, northeast, southwest, southeast });
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */                 
/* 1385 */                 if (blockyTops) {
/*      */ 
/*      */ 
/*      */                   
/* 1389 */                   ySW = ySE = yNE = yNW = column.height();
/*      */                 } else {
/* 1391 */                   ySW = blendedCornerHeight(column.height(), new HeightmapColumn[] { west, south, southwest });
/* 1392 */                   ySE = blendedCornerHeight(column.height(), new HeightmapColumn[] { east, south, southeast });
/* 1393 */                   yNE = blendedCornerHeight(column.height(), new HeightmapColumn[] { east, north, northeast });
/* 1394 */                   yNW = blendedCornerHeight(column.height(), new HeightmapColumn[] { west, north, northwest });
/*      */                 } 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */                 
/* 1402 */                 float minCorner = Math.min(Math.min(ySW, ySE), Math.min(yNE, yNW));
/* 1403 */                 float maxCorner = Math.max(Math.max(ySW, ySE), Math.max(yNE, yNW));
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */                 
/* 1423 */                 float slopeShade = bakedSurfaceBrightness(west, east, south, north, column, spacing, 
/* 1424 */                     LodSettingsConfig.get().nearSlopeShadingFloor());
/* 1425 */                 float aoSW = cornerAo(column.height(), west, south, southwest) * slopeShade;
/* 1426 */                 float aoSE = cornerAo(column.height(), east, south, southeast) * slopeShade;
/* 1427 */                 float aoNE = cornerAo(column.height(), east, north, northeast) * slopeShade;
/* 1428 */                 float aoNW = cornerAo(column.height(), west, north, northwest) * slopeShade;
/*      */                 
/* 1430 */                 if (sub > 1 && maxCorner - minCorner >= 1.0F)
/* 1431 */                 { emitSubdividedColumn(sink, x0, z0, x1, z1, ySW, ySE, yNE, yNW, column.height(), west, east, south, north, skirtBottom, material, tintColor, sub, fade, litFormat, vertexCount, aoSW, aoSE, aoNE, aoNW);
/*      */                    }
/*      */                 
/*      */                 else
/*      */                 
/*      */                 { 
/* 1437 */                   emitBoxTop(sink, x0, z0, x1, z1, ySW, ySE, yNE, yNW, material.topSprite(), tintColor, 1.0F * aoSW, 1.0F * aoSE, 1.0F * aoNE, 1.0F * aoNW, fade, litFormat, vertexCount);
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */                   
/* 1447 */                   edgeGrid[lx][lz] = new EdgeColumn(column.height(), ySW, ySE, yNE, yNW, skirtBottom, tintColor, material
/* 1448 */                       .sideSprite()); }  }  } 
/*      */           } 
/*      */         } 
/*      */       } 
/* 1452 */     }  emitMergedSkirts(sink, regionFile, region, edgeGrid, samplesPerAxis, spacing, half, fade, litFormat, vertexCount);
/*      */ 
/*      */     
/* 1455 */     return new RecordedPart(sink, kind, format);
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
/*      */   private static int quantizeColor(int colorRgb) {
/* 1487 */     int r = quantizeChannel(colorRgb >> 16 & 0xFF);
/* 1488 */     int g = quantizeChannel(colorRgb >> 8 & 0xFF);
/* 1489 */     int b = quantizeChannel(colorRgb & 0xFF);
/* 1490 */     return r << 16 | g << 8 | b;
/*      */   }
/*      */   
/*      */   private static int quantizeChannel(int channel) {
/* 1494 */     int step = 16;
/* 1495 */     return clampByte(Math.round(channel / step) * step);
/*      */   }
/*      */   private static final class EdgeColumn extends Record { private final int height; private final float ySW; private final float ySE; private final float yNE; private final float yNW; private final float skirtBottom; private final int tintColor; private final SurfaceMaterial.Sprite sideSprite;
/* 1498 */     private EdgeColumn(int height, float ySW, float ySE, float yNE, float yNW, float skirtBottom, int tintColor, SurfaceMaterial.Sprite sideSprite) { this.height = height; this.ySW = ySW; this.ySE = ySE; this.yNE = yNE; this.yNW = yNW; this.skirtBottom = skirtBottom; this.tintColor = tintColor; this.sideSprite = sideSprite; } public final String toString() { // Byte code:
/*      */       //   0: aload_0
/*      */       //   1: <illegal opcode> toString : (Lcom/angryalchemist/ecstatic/render/LodRegionMesh$EdgeColumn;)Ljava/lang/String;
/*      */       //   6: areturn
/*      */       // Line number table:
/*      */       //   Java source line number -> byte code offset
/*      */       //   #1498	-> 0
/*      */       // Local variable table:
/*      */       //   start	length	slot	name	descriptor
/*      */       //   0	7	0	this	Lcom/angryalchemist/ecstatic/render/LodRegionMesh$EdgeColumn; } public final int hashCode() { // Byte code:
/*      */       //   0: aload_0
/*      */       //   1: <illegal opcode> hashCode : (Lcom/angryalchemist/ecstatic/render/LodRegionMesh$EdgeColumn;)I
/*      */       //   6: ireturn
/*      */       // Line number table:
/*      */       //   Java source line number -> byte code offset
/*      */       //   #1498	-> 0
/*      */       // Local variable table:
/*      */       //   start	length	slot	name	descriptor
/*      */       //   0	7	0	this	Lcom/angryalchemist/ecstatic/render/LodRegionMesh$EdgeColumn; } public final boolean equals(Object o) { // Byte code:
/*      */       //   0: aload_0
/*      */       //   1: aload_1
/*      */       //   2: <illegal opcode> equals : (Lcom/angryalchemist/ecstatic/render/LodRegionMesh$EdgeColumn;Ljava/lang/Object;)Z
/*      */       //   7: ireturn
/*      */       // Line number table:
/*      */       //   Java source line number -> byte code offset
/*      */       //   #1498	-> 0
/*      */       // Local variable table:
/*      */       //   start	length	slot	name	descriptor
/*      */       //   0	8	0	this	Lcom/angryalchemist/ecstatic/render/LodRegionMesh$EdgeColumn;
/* 1498 */       //   0	8	1	o	Ljava/lang/Object; } public int height() { return this.height; } public float ySW() { return this.ySW; } public float ySE() { return this.ySE; } public float yNE() { return this.yNE; } public float yNW() { return this.yNW; } public float skirtBottom() { return this.skirtBottom; } public int tintColor() { return this.tintColor; } public SurfaceMaterial.Sprite sideSprite() { return this.sideSprite; }
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
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */   
/*      */   private static void emitMergedSkirts(VertexSink sink, LodRegionFile regionFile, RegionCoord region, EdgeColumn[][] edgeGrid, int samplesPerAxis, int spacing, float half, FadeParams fade, boolean litFormat, int[] vertexCount) {
/* 1540 */     emitMergedWestSkirts(sink, regionFile, region, edgeGrid, samplesPerAxis, spacing, half, fade, litFormat, vertexCount);
/* 1541 */     emitMergedEastSkirts(sink, regionFile, region, edgeGrid, samplesPerAxis, spacing, half, fade, litFormat, vertexCount);
/* 1542 */     emitMergedSouthSkirts(sink, regionFile, region, edgeGrid, samplesPerAxis, spacing, half, fade, litFormat, vertexCount);
/* 1543 */     emitMergedNorthSkirts(sink, regionFile, region, edgeGrid, samplesPerAxis, spacing, half, fade, litFormat, vertexCount);
/*      */   }
/*      */ 
/*      */ 
/*      */   
/*      */   private static void emitMergedWestSkirts(VertexSink sink, LodRegionFile regionFile, RegionCoord region, EdgeColumn[][] edgeGrid, int samplesPerAxis, int spacing, float half, FadeParams fade, boolean litFormat, int[] vertexCount) {
/* 1549 */     for (int lx = 0; lx < samplesPerAxis; lx++) {
/* 1550 */       float x0 = (region.originBlockX() + lx * spacing) - half;
/* 1551 */       boolean runActive = false;
/* 1552 */       int runStart = 0;
/* 1553 */       float runHeight = 0.0F;
/* 1554 */       float runBottom = 0.0F;
/* 1555 */       int runTint = 0;
/* 1556 */       SurfaceMaterial.Sprite runSideSprite = null;
/*      */       
/* 1558 */       for (int lz = 0; lz < samplesPerAxis; lz++) {
/* 1559 */         EdgeColumn col = edgeGrid[lx][lz];
/* 1560 */         boolean needed = false;
/* 1561 */         boolean flat = false;
/* 1562 */         if (col != null) {
/* 1563 */           HeightmapColumn west = columnAt(regionFile, region, lx - 1, lz, samplesPerAxis);
/* 1564 */           needed = (west != null && west.height() < col.height());
/* 1565 */           flat = (needed && col.ySW() == col.yNW());
/*      */         } 
/*      */         
/* 1568 */         if (runActive) {
/*      */           
/* 1570 */           boolean canExtend = (flat && col.ySW() == runHeight && col.skirtBottom() == runBottom && col.tintColor() == runTint && col.sideSprite() == runSideSprite);
/* 1571 */           if (canExtend) {
/*      */             continue;
/*      */           }
/* 1574 */           float zStart = (region.originBlockZ() + runStart * spacing) - half;
/* 1575 */           float zEnd = (region.originBlockZ() + (lz - 1) * spacing) + half;
/* 1576 */           emitSkirtQuad(sink, x0, zStart, x0, zEnd, runHeight, runHeight, runBottom, runSideSprite, runTint, 0.6F, fade, -1.0F, 0.0F, 0.0F, litFormat, vertexCount);
/*      */ 
/*      */           
/* 1579 */           runActive = false;
/*      */         } 
/*      */         
/* 1582 */         if (needed && flat) {
/* 1583 */           runActive = true;
/* 1584 */           runStart = lz;
/* 1585 */           runHeight = col.ySW();
/* 1586 */           runBottom = col.skirtBottom();
/* 1587 */           runTint = col.tintColor();
/* 1588 */           runSideSprite = col.sideSprite();
/* 1589 */         } else if (needed) {
/* 1590 */           float z0 = (region.originBlockZ() + lz * spacing) - half;
/* 1591 */           float z1 = (region.originBlockZ() + lz * spacing) + half;
/* 1592 */           emitSkirtQuad(sink, x0, z0, x0, z1, col.ySW(), col.yNW(), col.skirtBottom(), col
/* 1593 */               .sideSprite(), col.tintColor(), 0.6F, fade, -1.0F, 0.0F, 0.0F, litFormat, vertexCount);
/*      */         } 
/*      */         continue;
/*      */       } 
/* 1597 */       if (runActive) {
/* 1598 */         float zStart = (region.originBlockZ() + runStart * spacing) - half;
/* 1599 */         float zEnd = (region.originBlockZ() + (samplesPerAxis - 1) * spacing) + half;
/* 1600 */         emitSkirtQuad(sink, x0, zStart, x0, zEnd, runHeight, runHeight, runBottom, runSideSprite, runTint, 0.6F, fade, -1.0F, 0.0F, 0.0F, litFormat, vertexCount);
/*      */       } 
/*      */     } 
/*      */   }
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */   
/*      */   private static void emitMergedEastSkirts(VertexSink sink, LodRegionFile regionFile, RegionCoord region, EdgeColumn[][] edgeGrid, int samplesPerAxis, int spacing, float half, FadeParams fade, boolean litFormat, int[] vertexCount) {
/* 1610 */     for (int lx = 0; lx < samplesPerAxis; lx++) {
/* 1611 */       float x1 = (region.originBlockX() + lx * spacing) + half;
/* 1612 */       boolean runActive = false;
/* 1613 */       int runStart = 0;
/* 1614 */       float runHeight = 0.0F;
/* 1615 */       float runBottom = 0.0F;
/* 1616 */       int runTint = 0;
/* 1617 */       SurfaceMaterial.Sprite runSideSprite = null;
/*      */       
/* 1619 */       for (int lz = 0; lz < samplesPerAxis; lz++) {
/* 1620 */         EdgeColumn col = edgeGrid[lx][lz];
/* 1621 */         boolean needed = false;
/* 1622 */         boolean flat = false;
/* 1623 */         if (col != null) {
/* 1624 */           HeightmapColumn east = columnAt(regionFile, region, lx + 1, lz, samplesPerAxis);
/* 1625 */           needed = (east != null && east.height() < col.height());
/* 1626 */           flat = (needed && col.ySE() == col.yNE());
/*      */         } 
/*      */         
/* 1629 */         if (runActive) {
/*      */           
/* 1631 */           boolean canExtend = (flat && col.ySE() == runHeight && col.skirtBottom() == runBottom && col.tintColor() == runTint && col.sideSprite() == runSideSprite);
/* 1632 */           if (canExtend) {
/*      */             continue;
/*      */           }
/* 1635 */           float zStart = (region.originBlockZ() + runStart * spacing) - half;
/* 1636 */           float zEnd = (region.originBlockZ() + (lz - 1) * spacing) + half;
/* 1637 */           emitSkirtQuad(sink, x1, zStart, x1, zEnd, runHeight, runHeight, runBottom, runSideSprite, runTint, 0.6F, fade, 1.0F, 0.0F, 0.0F, litFormat, vertexCount);
/*      */ 
/*      */           
/* 1640 */           runActive = false;
/*      */         } 
/*      */         
/* 1643 */         if (needed && flat) {
/* 1644 */           runActive = true;
/* 1645 */           runStart = lz;
/* 1646 */           runHeight = col.ySE();
/* 1647 */           runBottom = col.skirtBottom();
/* 1648 */           runTint = col.tintColor();
/* 1649 */           runSideSprite = col.sideSprite();
/* 1650 */         } else if (needed) {
/* 1651 */           float z0 = (region.originBlockZ() + lz * spacing) - half;
/* 1652 */           float z1 = (region.originBlockZ() + lz * spacing) + half;
/* 1653 */           emitSkirtQuad(sink, x1, z0, x1, z1, col.ySE(), col.yNE(), col.skirtBottom(), col
/* 1654 */               .sideSprite(), col.tintColor(), 0.6F, fade, 1.0F, 0.0F, 0.0F, litFormat, vertexCount);
/*      */         } 
/*      */         continue;
/*      */       } 
/* 1658 */       if (runActive) {
/* 1659 */         float zStart = (region.originBlockZ() + runStart * spacing) - half;
/* 1660 */         float zEnd = (region.originBlockZ() + (samplesPerAxis - 1) * spacing) + half;
/* 1661 */         emitSkirtQuad(sink, x1, zStart, x1, zEnd, runHeight, runHeight, runBottom, runSideSprite, runTint, 0.6F, fade, 1.0F, 0.0F, 0.0F, litFormat, vertexCount);
/*      */       } 
/*      */     } 
/*      */   }
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */   
/*      */   private static void emitMergedSouthSkirts(VertexSink sink, LodRegionFile regionFile, RegionCoord region, EdgeColumn[][] edgeGrid, int samplesPerAxis, int spacing, float half, FadeParams fade, boolean litFormat, int[] vertexCount) {
/* 1671 */     for (int lz = 0; lz < samplesPerAxis; lz++) {
/* 1672 */       float z0 = (region.originBlockZ() + lz * spacing) - half;
/* 1673 */       boolean runActive = false;
/* 1674 */       int runStart = 0;
/* 1675 */       float runHeight = 0.0F;
/* 1676 */       float runBottom = 0.0F;
/* 1677 */       int runTint = 0;
/* 1678 */       SurfaceMaterial.Sprite runSideSprite = null;
/*      */       
/* 1680 */       for (int lx = 0; lx < samplesPerAxis; lx++) {
/* 1681 */         EdgeColumn col = edgeGrid[lx][lz];
/* 1682 */         boolean needed = false;
/* 1683 */         boolean flat = false;
/* 1684 */         if (col != null) {
/* 1685 */           HeightmapColumn south = columnAt(regionFile, region, lx, lz - 1, samplesPerAxis);
/* 1686 */           needed = (south != null && south.height() < col.height());
/* 1687 */           flat = (needed && col.ySW() == col.ySE());
/*      */         } 
/*      */         
/* 1690 */         if (runActive) {
/*      */           
/* 1692 */           boolean canExtend = (flat && col.ySW() == runHeight && col.skirtBottom() == runBottom && col.tintColor() == runTint && col.sideSprite() == runSideSprite);
/* 1693 */           if (canExtend) {
/*      */             continue;
/*      */           }
/* 1696 */           float xStart = (region.originBlockX() + runStart * spacing) - half;
/* 1697 */           float xEnd = (region.originBlockX() + (lx - 1) * spacing) + half;
/* 1698 */           emitSkirtQuad(sink, xStart, z0, xEnd, z0, runHeight, runHeight, runBottom, runSideSprite, runTint, 0.8F, fade, 0.0F, 0.0F, -1.0F, litFormat, vertexCount);
/*      */ 
/*      */           
/* 1701 */           runActive = false;
/*      */         } 
/*      */         
/* 1704 */         if (needed && flat) {
/* 1705 */           runActive = true;
/* 1706 */           runStart = lx;
/* 1707 */           runHeight = col.ySW();
/* 1708 */           runBottom = col.skirtBottom();
/* 1709 */           runTint = col.tintColor();
/* 1710 */           runSideSprite = col.sideSprite();
/* 1711 */         } else if (needed) {
/* 1712 */           float x0 = (region.originBlockX() + lx * spacing) - half;
/* 1713 */           float x1 = (region.originBlockX() + lx * spacing) + half;
/* 1714 */           emitSkirtQuad(sink, x0, z0, x1, z0, col.ySW(), col.ySE(), col.skirtBottom(), col
/* 1715 */               .sideSprite(), col.tintColor(), 0.8F, fade, 0.0F, 0.0F, -1.0F, litFormat, vertexCount);
/*      */         } 
/*      */         continue;
/*      */       } 
/* 1719 */       if (runActive) {
/* 1720 */         float xStart = (region.originBlockX() + runStart * spacing) - half;
/* 1721 */         float xEnd = (region.originBlockX() + (samplesPerAxis - 1) * spacing) + half;
/* 1722 */         emitSkirtQuad(sink, xStart, z0, xEnd, z0, runHeight, runHeight, runBottom, runSideSprite, runTint, 0.8F, fade, 0.0F, 0.0F, -1.0F, litFormat, vertexCount);
/*      */       } 
/*      */     } 
/*      */   }
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */   
/*      */   private static void emitMergedNorthSkirts(VertexSink sink, LodRegionFile regionFile, RegionCoord region, EdgeColumn[][] edgeGrid, int samplesPerAxis, int spacing, float half, FadeParams fade, boolean litFormat, int[] vertexCount) {
/* 1732 */     for (int lz = 0; lz < samplesPerAxis; lz++) {
/* 1733 */       float z1 = (region.originBlockZ() + lz * spacing) + half;
/* 1734 */       boolean runActive = false;
/* 1735 */       int runStart = 0;
/* 1736 */       float runHeight = 0.0F;
/* 1737 */       float runBottom = 0.0F;
/* 1738 */       int runTint = 0;
/* 1739 */       SurfaceMaterial.Sprite runSideSprite = null;
/*      */       
/* 1741 */       for (int lx = 0; lx < samplesPerAxis; lx++) {
/* 1742 */         EdgeColumn col = edgeGrid[lx][lz];
/* 1743 */         boolean needed = false;
/* 1744 */         boolean flat = false;
/* 1745 */         if (col != null) {
/* 1746 */           HeightmapColumn north = columnAt(regionFile, region, lx, lz + 1, samplesPerAxis);
/* 1747 */           needed = (north != null && north.height() < col.height());
/* 1748 */           flat = (needed && col.yNW() == col.yNE());
/*      */         } 
/*      */         
/* 1751 */         if (runActive) {
/*      */           
/* 1753 */           boolean canExtend = (flat && col.yNW() == runHeight && col.skirtBottom() == runBottom && col.tintColor() == runTint && col.sideSprite() == runSideSprite);
/* 1754 */           if (canExtend) {
/*      */             continue;
/*      */           }
/* 1757 */           float xStart = (region.originBlockX() + runStart * spacing) - half;
/* 1758 */           float xEnd = (region.originBlockX() + (lx - 1) * spacing) + half;
/* 1759 */           emitSkirtQuad(sink, xStart, z1, xEnd, z1, runHeight, runHeight, runBottom, runSideSprite, runTint, 0.8F, fade, 0.0F, 0.0F, 1.0F, litFormat, vertexCount);
/*      */ 
/*      */           
/* 1762 */           runActive = false;
/*      */         } 
/*      */         
/* 1765 */         if (needed && flat) {
/* 1766 */           runActive = true;
/* 1767 */           runStart = lx;
/* 1768 */           runHeight = col.yNW();
/* 1769 */           runBottom = col.skirtBottom();
/* 1770 */           runTint = col.tintColor();
/* 1771 */           runSideSprite = col.sideSprite();
/* 1772 */         } else if (needed) {
/* 1773 */           float x0 = (region.originBlockX() + lx * spacing) - half;
/* 1774 */           float x1 = (region.originBlockX() + lx * spacing) + half;
/* 1775 */           emitSkirtQuad(sink, x0, z1, x1, z1, col.yNW(), col.yNE(), col.skirtBottom(), col
/* 1776 */               .sideSprite(), col.tintColor(), 0.8F, fade, 0.0F, 0.0F, 1.0F, litFormat, vertexCount);
/*      */         } 
/*      */         continue;
/*      */       } 
/* 1780 */       if (runActive) {
/* 1781 */         float xStart = (region.originBlockX() + runStart * spacing) - half;
/* 1782 */         float xEnd = (region.originBlockX() + (samplesPerAxis - 1) * spacing) + half;
/* 1783 */         emitSkirtQuad(sink, xStart, z1, xEnd, z1, runHeight, runHeight, runBottom, runSideSprite, runTint, 0.8F, fade, 0.0F, 0.0F, 1.0F, litFormat, vertexCount);
/*      */       } 
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
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */   
/*      */   private static void emitSubdividedColumn(VertexSink sink, float x0, float z0, float x1, float z1, float ySW, float ySE, float yNE, float yNW, int columnHeight, HeightmapColumn west, HeightmapColumn east, HeightmapColumn south, HeightmapColumn north, float skirtBottom, SurfaceMaterial material, int tintColor, int sub, FadeParams fade, boolean litFormat, int[] vertexCount, float aoSW, float aoSE, float aoNE, float aoNW) {
/* 1830 */     float subSize = (x1 - x0) / sub;
/* 1831 */     SurfaceMaterial.Sprite top = material.topSprite();
/* 1832 */     SurfaceMaterial.Sprite side = material.sideSprite();
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */     
/* 1838 */     int[][] subH = new int[sub][sub];
/* 1839 */     for (int sz = 0; sz < sub; sz++) {
/* 1840 */       for (int sx = 0; sx < sub; sx++) {
/* 1841 */         float u = (sx + 0.5F) / sub;
/* 1842 */         float v = (sz + 0.5F) / sub;
/* 1843 */         subH[sx][sz] = Math.round(bilinearHeight(ySW, ySE, yNE, yNW, u, v));
/*      */       } 
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
/*      */     
/* 1860 */     boolean westLower = (west != null && west.height() < columnHeight);
/* 1861 */     boolean eastLower = (east != null && east.height() < columnHeight);
/* 1862 */     boolean southLower = (south != null && south.height() < columnHeight);
/* 1863 */     boolean northLower = (north != null && north.height() < columnHeight);
/*      */     
/* 1865 */     for (int i = 0; i < sub; i++) {
/* 1866 */       for (int sx = 0; sx < sub; sx++) {
/* 1867 */         float sx0 = x0 + sx * subSize;
/* 1868 */         float sx1 = sx0 + subSize;
/* 1869 */         float sz0 = z0 + i * subSize;
/* 1870 */         float sz1 = sz0 + subSize;
/* 1871 */         float h = subH[sx][i];
/*      */ 
/*      */ 
/*      */         
/* 1875 */         float ao = bilinearHeight(aoSW, aoSE, aoNE, aoNW, (sx + 0.5F) / sub, (i + 0.5F) / sub);
/* 1876 */         emitBoxTop(sink, sx0, sz0, sx1, sz1, h, h, h, h, top, tintColor, 1.0F * ao, 1.0F * ao, 1.0F * ao, 1.0F * ao, fade, litFormat, vertexCount);
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */         
/* 1882 */         if (sx > 0) {
/* 1883 */           if (subH[sx - 1][i] < h) {
/* 1884 */             emitSkirtQuad(sink, sx0, sz0, sx0, sz1, h, h, subH[sx - 1][i], side, tintColor, 0.6F, fade, -1.0F, 0.0F, 0.0F, litFormat, vertexCount);
/*      */           }
/*      */         }
/* 1887 */         else if (westLower) {
/* 1888 */           emitSkirtQuad(sink, sx0, sz0, sx0, sz1, h, h, skirtBottom, side, tintColor, 0.6F, fade, -1.0F, 0.0F, 0.0F, litFormat, vertexCount);
/*      */         } 
/*      */ 
/*      */ 
/*      */         
/* 1893 */         if (sx < sub - 1) {
/* 1894 */           if (subH[sx + 1][i] < h) {
/* 1895 */             emitSkirtQuad(sink, sx1, sz0, sx1, sz1, h, h, subH[sx + 1][i], side, tintColor, 0.6F, fade, 1.0F, 0.0F, 0.0F, litFormat, vertexCount);
/*      */           }
/*      */         }
/* 1898 */         else if (eastLower) {
/* 1899 */           emitSkirtQuad(sink, sx1, sz0, sx1, sz1, h, h, skirtBottom, side, tintColor, 0.6F, fade, 1.0F, 0.0F, 0.0F, litFormat, vertexCount);
/*      */         } 
/*      */ 
/*      */ 
/*      */         
/* 1904 */         if (i > 0) {
/* 1905 */           if (subH[sx][i - 1] < h) {
/* 1906 */             emitSkirtQuad(sink, sx0, sz0, sx1, sz0, h, h, subH[sx][i - 1], side, tintColor, 0.8F, fade, 0.0F, 0.0F, -1.0F, litFormat, vertexCount);
/*      */           }
/*      */         }
/* 1909 */         else if (southLower) {
/* 1910 */           emitSkirtQuad(sink, sx0, sz0, sx1, sz0, h, h, skirtBottom, side, tintColor, 0.8F, fade, 0.0F, 0.0F, -1.0F, litFormat, vertexCount);
/*      */         } 
/*      */ 
/*      */ 
/*      */         
/* 1915 */         if (i < sub - 1) {
/* 1916 */           if (subH[sx][i + 1] < h) {
/* 1917 */             emitSkirtQuad(sink, sx0, sz1, sx1, sz1, h, h, subH[sx][i + 1], side, tintColor, 0.8F, fade, 0.0F, 0.0F, 1.0F, litFormat, vertexCount);
/*      */           }
/*      */         }
/* 1920 */         else if (northLower) {
/* 1921 */           emitSkirtQuad(sink, sx0, sz1, sx1, sz1, h, h, skirtBottom, side, tintColor, 0.8F, fade, 0.0F, 0.0F, 1.0F, litFormat, vertexCount);
/*      */         } 
/*      */       } 
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
/*      */   private static float bilinearHeight(float ySW, float ySE, float yNE, float yNW, float u, float v) {
/* 1935 */     float southEdge = ySW + (ySE - ySW) * u;
/* 1936 */     float northEdge = yNW + (yNE - yNW) * u;
/* 1937 */     return southEdge + (northEdge - southEdge) * v;
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
/*      */   private static float blendedCornerHeight(int centerHeight, HeightmapColumn... cornerNeighbors) {
/* 1949 */     float sum = centerHeight;
/* 1950 */     int count = 1;
/* 1951 */     for (HeightmapColumn neighbor : cornerNeighbors) {
/* 1952 */       if (neighbor != null && Math.abs(neighbor.height() - centerHeight) <= 3.0F) {
/* 1953 */         sum += neighbor.height();
/* 1954 */         count++;
/*      */       } 
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
/* 1966 */     return Math.round(sum / count);
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
/*      */   static float cornerAo(int centerHeight, HeightmapColumn cardinal1, HeightmapColumn cardinal2, HeightmapColumn diagonal) {
/*      */     int occluderCount;
/* 1989 */     boolean side1 = (cardinal1 != null && cardinal1.height() > centerHeight);
/* 1990 */     boolean side2 = (cardinal2 != null && cardinal2.height() > centerHeight);
/*      */     
/* 1992 */     if (side1 && side2) {
/* 1993 */       occluderCount = 3;
/*      */     } else {
/* 1995 */       boolean corner = (diagonal != null && diagonal.height() > centerHeight);
/* 1996 */       occluderCount = (side1 ? 1 : 0) + (side2 ? 1 : 0) + (corner ? 1 : 0);
/*      */     } 
/* 1998 */     return AO_BRIGHTNESS_BY_OCCLUDER_COUNT[occluderCount];
/*      */   }
/*      */ 
/*      */   
/*      */   private static float lowestNeighborHeight(int columnHeight, HeightmapColumn... neighbors) {
/* 2003 */     int lowest = columnHeight;
/* 2004 */     for (HeightmapColumn neighbor : neighbors) {
/* 2005 */       if (neighbor != null && neighbor.height() < lowest) {
/* 2006 */         lowest = neighbor.height();
/*      */       }
/*      */     } 
/* 2009 */     return lowest;
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
/*      */   private static List<RecordedTreePart> recordTrees(LodRegionFile regionFile, RegionCoord region, FadeParams fade, Registry<Biome> biomeRegistry, int[] vertexCount, int lodLevel) {
/* 2039 */     List<TreePlacement> placements = findTreePlacements(regionFile, region, regionFile.sampleSpacingBlocks, regionFile.samplesPerAxis, biomeRegistry, lodLevel);
/*      */     
/* 2041 */     if (placements.isEmpty()) {
/* 2042 */       return List.of();
/*      */     }
/*      */     
/* 2045 */     BiomeStyleConfig config = BiomeStyleConfig.get();
/* 2046 */     List<RecordedTreePart> parts = new ArrayList<>();
/*      */     
/* 2048 */     for (TreeStyle.Group group : TreeStyle.Group.values()) {
/* 2049 */       ResourceLocation trunkTexture = config.trunkTextureId(group);
/* 2050 */       if (trunkTexture != null) {
/* 2051 */         recordTexturedTreePart(parts, placements, fade, group, TreePart.TRUNK, trunkTexture, config
/* 2052 */             .trunkTint(group), vertexCount);
/*      */       }
/* 2054 */       ResourceLocation foliageTexture = config.foliageTextureId(group);
/* 2055 */       if (foliageTexture != null) {
/* 2056 */         recordTexturedTreePart(parts, placements, fade, group, TreePart.FOLIAGE, foliageTexture, config
/* 2057 */             .foliageTint(group), vertexCount);
/*      */       }
/*      */     } 
/*      */     
/* 2061 */     recordDefaultTreePart(parts, placements, fade, config, vertexCount);
/*      */     
/* 2063 */     return parts;
/*      */   }
/*      */   
/* 2066 */   private enum TreePart { TRUNK, FOLIAGE; }
/*      */ 
/*      */   
/*      */   private static final class TreePlacement extends Record {
/*      */     private final int blockX;
/*      */     private final int blockZ;
/*      */     private final int groundY;
/*      */     private final TreeStyle style;
/*      */     private final boolean boxGeometry;
/*      */     
/* 2076 */     private TreePlacement(int blockX, int blockZ, int groundY, TreeStyle style, boolean boxGeometry) { this.blockX = blockX; this.blockZ = blockZ; this.groundY = groundY; this.style = style; this.boxGeometry = boxGeometry; } public final String toString() { // Byte code:
/*      */       //   0: aload_0
/*      */       //   1: <illegal opcode> toString : (Lcom/angryalchemist/ecstatic/render/LodRegionMesh$TreePlacement;)Ljava/lang/String;
/*      */       //   6: areturn
/*      */       // Line number table:
/*      */       //   Java source line number -> byte code offset
/*      */       //   #2076	-> 0
/*      */       // Local variable table:
/*      */       //   start	length	slot	name	descriptor
/*      */       //   0	7	0	this	Lcom/angryalchemist/ecstatic/render/LodRegionMesh$TreePlacement; } public final int hashCode() { // Byte code:
/*      */       //   0: aload_0
/*      */       //   1: <illegal opcode> hashCode : (Lcom/angryalchemist/ecstatic/render/LodRegionMesh$TreePlacement;)I
/*      */       //   6: ireturn
/*      */       // Line number table:
/*      */       //   Java source line number -> byte code offset
/*      */       //   #2076	-> 0
/*      */       // Local variable table:
/*      */       //   start	length	slot	name	descriptor
/*      */       //   0	7	0	this	Lcom/angryalchemist/ecstatic/render/LodRegionMesh$TreePlacement; } public final boolean equals(Object o) { // Byte code:
/*      */       //   0: aload_0
/*      */       //   1: aload_1
/*      */       //   2: <illegal opcode> equals : (Lcom/angryalchemist/ecstatic/render/LodRegionMesh$TreePlacement;Ljava/lang/Object;)Z
/*      */       //   7: ireturn
/*      */       // Line number table:
/*      */       //   Java source line number -> byte code offset
/*      */       //   #2076	-> 0
/*      */       // Local variable table:
/*      */       //   start	length	slot	name	descriptor
/*      */       //   0	8	0	this	Lcom/angryalchemist/ecstatic/render/LodRegionMesh$TreePlacement;
/* 2076 */       //   0	8	1	o	Ljava/lang/Object; } public int blockX() { return this.blockX; } public int blockZ() { return this.blockZ; } public int groundY() { return this.groundY; } public TreeStyle style() { return this.style; } public boolean boxGeometry() { return this.boxGeometry; }
/*      */   
/*      */   }
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */   
/*      */   private static void recordTexturedTreePart(List<RecordedTreePart> parts, List<TreePlacement> placements, FadeParams fade, TreeStyle.Group group, TreePart part, ResourceLocation texture, int tintColor, int[] vertexCount) {
/* 2086 */     RecordedVertexSink sink = new RecordedVertexSink();
/* 2087 */     int[] partVertexCount = { 0 };
/* 2088 */     for (TreePlacement placement : placements) {
/* 2089 */       if ((placement.style()).group != group) {
/*      */         continue;
/*      */       }
/* 2092 */       if (!fade.visibleAt(placement.blockX(), placement.blockZ())) {
/*      */         continue;
/*      */       }
/* 2095 */       if (part == TreePart.TRUNK) {
/* 2096 */         emitTexturedTrunk(sink, placement, tintColor, 255, partVertexCount); continue;
/*      */       } 
/* 2098 */       emitTexturedFoliage(sink, placement, tintColor, 255, partVertexCount);
/*      */     } 
/*      */     
/* 2101 */     parts.add(new RecordedTreePart(LodTreeRenderType.forTexture(texture), new RecordedPart(sink, RecordedVertexSink.Kind.TEXTURED, DefaultVertexFormat.f_85818_)));
/*      */     
/* 2103 */     vertexCount[0] = vertexCount[0] + partVertexCount[0];
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
/*      */   private static void recordDefaultTreePart(List<RecordedTreePart> parts, List<TreePlacement> placements, FadeParams fade, BiomeStyleConfig config, int[] vertexCount) {
/* 2118 */     RecordedVertexSink sink = new RecordedVertexSink();
/* 2119 */     int[] partVertexCount = { 0 };
/* 2120 */     for (TreePlacement placement : placements) {
/* 2121 */       if (!fade.visibleAt(placement.blockX(), placement.blockZ())) {
/*      */         continue;
/*      */       }
/* 2124 */       TreeStyle.Group group = (placement.style()).group;
/* 2125 */       if (config.trunkTextureId(group) == null) {
/* 2126 */         emitBlockTrunk(sink, placement, config.trunkTint(group), 255, partVertexCount);
/*      */       }
/* 2128 */       if (config.foliageTextureId(group) == null) {
/* 2129 */         emitBlockFoliage(sink, placement, config.foliageTint(group), 255, partVertexCount);
/*      */       }
/*      */     } 
/* 2132 */     parts.add(new RecordedTreePart(LodTerrainRenderType.TERRAIN_TEXTURED, new RecordedPart(sink, RecordedVertexSink.Kind.TEXTURED, DefaultVertexFormat.f_85818_)));
/*      */     
/* 2134 */     vertexCount[0] = vertexCount[0] + partVertexCount[0];
/*      */   }
/*      */ 
/*      */   
/*      */   private static VertexBuffer upload(BufferBuilder builder, int vertexCount) {
/* 2139 */     BufferBuilder.RenderedBuffer rendered = builder.m_231175_();
/* 2140 */     if (vertexCount == 0) {
/* 2141 */       return null;
/*      */     }
/* 2143 */     VertexBuffer vertexBuffer = new VertexBuffer(VertexBuffer.Usage.STATIC);
/* 2144 */     vertexBuffer.m_85921_();
/* 2145 */     vertexBuffer.m_231221_(rendered);
/* 2146 */     VertexBuffer.m_85931_();
/* 2147 */     return vertexBuffer;
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
/*      */   static void emitBoxTop(VertexSink sink, float x0, float z0, float x1, float z1, float ySW, float ySE, float yNE, float yNW, SurfaceMaterial.Sprite sprite, int tintColor, float shadeSW, float shadeSE, float shadeNE, float shadeNW, FadeParams fade, boolean litFormat, int[] vertexCount) {
/* 2177 */     int alpha00 = fade.alphaAt(x0, z0);
/* 2178 */     int alpha10 = fade.alphaAt(x1, z0);
/* 2179 */     int alpha11 = fade.alphaAt(x1, z1);
/* 2180 */     int alpha01 = fade.alphaAt(x0, z1);
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */     
/* 2189 */     int baseColor = applyLightTemperature((sprite == null || sprite.tinted()) ? tintColor : 16777215);
/* 2190 */     int color00 = shade(baseColor, shadeSW);
/* 2191 */     int color10 = shade(baseColor, shadeSE);
/* 2192 */     int color11 = shade(baseColor, shadeNE);
/* 2193 */     int color01 = shade(baseColor, shadeNW);
/*      */     
/* 2195 */     if (litFormat) {
/* 2196 */       float[] crossA = triangleCross(x0, ySW, z0, x1, ySE, z0, x1, yNE, z1);
/* 2197 */       float[] crossB = triangleCross(x0, ySW, z0, x1, yNE, z1, x0, yNW, z1);
/* 2198 */       float[] n00 = normalizeSum(crossA, crossB);
/* 2199 */       float[] n10 = normalize(crossA);
/* 2200 */       float[] n11 = normalizeSum(crossA, crossB);
/* 2201 */       float[] n01 = normalize(crossB);
/*      */       
/* 2203 */       if (sprite == null) {
/* 2204 */         sink.litVertex(x0, ySW, z0, color00, alpha00, n00[0], n00[1], n00[2]);
/* 2205 */         sink.litVertex(x1, ySE, z0, color10, alpha10, n10[0], n10[1], n10[2]);
/* 2206 */         sink.litVertex(x1, yNE, z1, color11, alpha11, n11[0], n11[1], n11[2]);
/*      */         
/* 2208 */         sink.litVertex(x0, ySW, z0, color00, alpha00, n00[0], n00[1], n00[2]);
/* 2209 */         sink.litVertex(x1, yNE, z1, color11, alpha11, n11[0], n11[1], n11[2]);
/* 2210 */         sink.litVertex(x0, yNW, z1, color01, alpha01, n01[0], n01[1], n01[2]);
/*      */       } else {
/* 2212 */         sink.litTexturedVertex(x0, ySW, z0, sprite.u0(), sprite.v0(), color00, alpha00, n00[0], n00[1], n00[2]);
/* 2213 */         sink.litTexturedVertex(x1, ySE, z0, sprite.u1(), sprite.v0(), color10, alpha10, n10[0], n10[1], n10[2]);
/* 2214 */         sink.litTexturedVertex(x1, yNE, z1, sprite.u1(), sprite.v1(), color11, alpha11, n11[0], n11[1], n11[2]);
/*      */         
/* 2216 */         sink.litTexturedVertex(x0, ySW, z0, sprite.u0(), sprite.v0(), color00, alpha00, n00[0], n00[1], n00[2]);
/* 2217 */         sink.litTexturedVertex(x1, yNE, z1, sprite.u1(), sprite.v1(), color11, alpha11, n11[0], n11[1], n11[2]);
/* 2218 */         sink.litTexturedVertex(x0, yNW, z1, sprite.u0(), sprite.v1(), color01, alpha01, n01[0], n01[1], n01[2]);
/*      */       } 
/* 2220 */     } else if (sprite == null) {
/* 2221 */       sink.vertex(x0, ySW, z0, color00, alpha00);
/* 2222 */       sink.vertex(x1, ySE, z0, color10, alpha10);
/* 2223 */       sink.vertex(x1, yNE, z1, color11, alpha11);
/*      */       
/* 2225 */       sink.vertex(x0, ySW, z0, color00, alpha00);
/* 2226 */       sink.vertex(x1, yNE, z1, color11, alpha11);
/* 2227 */       sink.vertex(x0, yNW, z1, color01, alpha01);
/*      */     } else {
/* 2229 */       sink.texturedVertex(x0, ySW, z0, sprite.u0(), sprite.v0(), color00, alpha00);
/* 2230 */       sink.texturedVertex(x1, ySE, z0, sprite.u1(), sprite.v0(), color10, alpha10);
/* 2231 */       sink.texturedVertex(x1, yNE, z1, sprite.u1(), sprite.v1(), color11, alpha11);
/*      */       
/* 2233 */       sink.texturedVertex(x0, ySW, z0, sprite.u0(), sprite.v0(), color00, alpha00);
/* 2234 */       sink.texturedVertex(x1, yNE, z1, sprite.u1(), sprite.v1(), color11, alpha11);
/* 2235 */       sink.texturedVertex(x0, yNW, z1, sprite.u0(), sprite.v1(), color01, alpha01);
/*      */     } 
/* 2237 */     vertexCount[0] = vertexCount[0] + 6;
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
/*      */   static void emitBoxBottom(VertexSink sink, float x0, float z0, float x1, float z1, float y, SurfaceMaterial.Sprite sprite, int tintColor, float shadeFactor, FadeParams fade, boolean litFormat, int[] vertexCount) {
/* 2258 */     int alpha00 = fade.alphaAt(x0, z0);
/* 2259 */     int alpha10 = fade.alphaAt(x1, z0);
/* 2260 */     int alpha11 = fade.alphaAt(x1, z1);
/* 2261 */     int alpha01 = fade.alphaAt(x0, z1);
/* 2262 */     int color = shade(applyLightTemperature(sprite.tinted() ? tintColor : 16777215), shadeFactor);
/*      */     
/* 2264 */     if (litFormat) {
/* 2265 */       sink.litTexturedVertex(x0, y, z0, sprite.u0(), sprite.v0(), color, alpha00, 0.0F, -1.0F, 0.0F);
/* 2266 */       sink.litTexturedVertex(x1, y, z1, sprite.u1(), sprite.v1(), color, alpha11, 0.0F, -1.0F, 0.0F);
/* 2267 */       sink.litTexturedVertex(x1, y, z0, sprite.u1(), sprite.v0(), color, alpha10, 0.0F, -1.0F, 0.0F);
/*      */       
/* 2269 */       sink.litTexturedVertex(x0, y, z0, sprite.u0(), sprite.v0(), color, alpha00, 0.0F, -1.0F, 0.0F);
/* 2270 */       sink.litTexturedVertex(x0, y, z1, sprite.u0(), sprite.v1(), color, alpha01, 0.0F, -1.0F, 0.0F);
/* 2271 */       sink.litTexturedVertex(x1, y, z1, sprite.u1(), sprite.v1(), color, alpha11, 0.0F, -1.0F, 0.0F);
/*      */     } else {
/* 2273 */       sink.texturedVertex(x0, y, z0, sprite.u0(), sprite.v0(), color, alpha00);
/* 2274 */       sink.texturedVertex(x1, y, z1, sprite.u1(), sprite.v1(), color, alpha11);
/* 2275 */       sink.texturedVertex(x1, y, z0, sprite.u1(), sprite.v0(), color, alpha10);
/*      */       
/* 2277 */       sink.texturedVertex(x0, y, z0, sprite.u0(), sprite.v0(), color, alpha00);
/* 2278 */       sink.texturedVertex(x0, y, z1, sprite.u0(), sprite.v1(), color, alpha01);
/* 2279 */       sink.texturedVertex(x1, y, z1, sprite.u1(), sprite.v1(), color, alpha11);
/*      */     } 
/* 2281 */     vertexCount[0] = vertexCount[0] + 6;
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
/*      */   static void emitSkirtQuad(VertexSink sink, float x0, float z0, float x1, float z1, float yTop0, float yTop1, float yBottom, SurfaceMaterial.Sprite sprite, int tintColor, float shadeFactor, FadeParams fade, float nx, float ny, float nz, boolean litFormat, int[] vertexCount) {
/* 2319 */     int alpha0 = fade.alphaAt(x0, z0);
/* 2320 */     int alpha1 = fade.alphaAt(x1, z1);
/*      */ 
/*      */ 
/*      */ 
/*      */     
/* 2325 */     int color = shade(applyLightTemperature((sprite == null || sprite.tinted()) ? tintColor : 16777215), shadeFactor * 
/* 2326 */         wallSunRelief(nx, nz));
/*      */ 
/*      */     
/* 2329 */     boolean forward = ((z1 - z0) * nx - (x1 - x0) * nz < 0.0F);
/*      */     
/* 2331 */     if (litFormat) {
/* 2332 */       if (sprite == null) {
/* 2333 */         if (forward) {
/* 2334 */           sink.litVertex(x0, yTop0, z0, color, alpha0, nx, ny, nz);
/* 2335 */           sink.litVertex(x1, yTop1, z1, color, alpha1, nx, ny, nz);
/* 2336 */           sink.litVertex(x1, yBottom, z1, color, alpha1, nx, ny, nz);
/*      */           
/* 2338 */           sink.litVertex(x0, yTop0, z0, color, alpha0, nx, ny, nz);
/* 2339 */           sink.litVertex(x1, yBottom, z1, color, alpha1, nx, ny, nz);
/* 2340 */           sink.litVertex(x0, yBottom, z0, color, alpha0, nx, ny, nz);
/*      */         } else {
/* 2342 */           sink.litVertex(x0, yTop0, z0, color, alpha0, nx, ny, nz);
/* 2343 */           sink.litVertex(x1, yBottom, z1, color, alpha1, nx, ny, nz);
/* 2344 */           sink.litVertex(x1, yTop1, z1, color, alpha1, nx, ny, nz);
/*      */           
/* 2346 */           sink.litVertex(x0, yTop0, z0, color, alpha0, nx, ny, nz);
/* 2347 */           sink.litVertex(x0, yBottom, z0, color, alpha0, nx, ny, nz);
/* 2348 */           sink.litVertex(x1, yBottom, z1, color, alpha1, nx, ny, nz);
/*      */         } 
/* 2350 */       } else if (forward) {
/* 2351 */         sink.litTexturedVertex(x0, yTop0, z0, sprite.u0(), sprite.v0(), color, alpha0, nx, ny, nz);
/* 2352 */         sink.litTexturedVertex(x1, yTop1, z1, sprite.u1(), sprite.v0(), color, alpha1, nx, ny, nz);
/* 2353 */         sink.litTexturedVertex(x1, yBottom, z1, sprite.u1(), sprite.v1(), color, alpha1, nx, ny, nz);
/*      */         
/* 2355 */         sink.litTexturedVertex(x0, yTop0, z0, sprite.u0(), sprite.v0(), color, alpha0, nx, ny, nz);
/* 2356 */         sink.litTexturedVertex(x1, yBottom, z1, sprite.u1(), sprite.v1(), color, alpha1, nx, ny, nz);
/* 2357 */         sink.litTexturedVertex(x0, yBottom, z0, sprite.u0(), sprite.v1(), color, alpha0, nx, ny, nz);
/*      */       } else {
/* 2359 */         sink.litTexturedVertex(x0, yTop0, z0, sprite.u0(), sprite.v0(), color, alpha0, nx, ny, nz);
/* 2360 */         sink.litTexturedVertex(x1, yBottom, z1, sprite.u1(), sprite.v1(), color, alpha1, nx, ny, nz);
/* 2361 */         sink.litTexturedVertex(x1, yTop1, z1, sprite.u1(), sprite.v0(), color, alpha1, nx, ny, nz);
/*      */         
/* 2363 */         sink.litTexturedVertex(x0, yTop0, z0, sprite.u0(), sprite.v0(), color, alpha0, nx, ny, nz);
/* 2364 */         sink.litTexturedVertex(x0, yBottom, z0, sprite.u0(), sprite.v1(), color, alpha0, nx, ny, nz);
/* 2365 */         sink.litTexturedVertex(x1, yBottom, z1, sprite.u1(), sprite.v1(), color, alpha1, nx, ny, nz);
/*      */       } 
/* 2367 */     } else if (sprite == null) {
/* 2368 */       if (forward) {
/* 2369 */         sink.vertex(x0, yTop0, z0, color, alpha0);
/* 2370 */         sink.vertex(x1, yTop1, z1, color, alpha1);
/* 2371 */         sink.vertex(x1, yBottom, z1, color, alpha1);
/*      */         
/* 2373 */         sink.vertex(x0, yTop0, z0, color, alpha0);
/* 2374 */         sink.vertex(x1, yBottom, z1, color, alpha1);
/* 2375 */         sink.vertex(x0, yBottom, z0, color, alpha0);
/*      */       } else {
/* 2377 */         sink.vertex(x0, yTop0, z0, color, alpha0);
/* 2378 */         sink.vertex(x1, yBottom, z1, color, alpha1);
/* 2379 */         sink.vertex(x1, yTop1, z1, color, alpha1);
/*      */         
/* 2381 */         sink.vertex(x0, yTop0, z0, color, alpha0);
/* 2382 */         sink.vertex(x0, yBottom, z0, color, alpha0);
/* 2383 */         sink.vertex(x1, yBottom, z1, color, alpha1);
/*      */       }
/*      */     
/* 2386 */     } else if (forward) {
/* 2387 */       sink.texturedVertex(x0, yTop0, z0, sprite.u0(), sprite.v0(), color, alpha0);
/* 2388 */       sink.texturedVertex(x1, yTop1, z1, sprite.u1(), sprite.v0(), color, alpha1);
/* 2389 */       sink.texturedVertex(x1, yBottom, z1, sprite.u1(), sprite.v1(), color, alpha1);
/*      */       
/* 2391 */       sink.texturedVertex(x0, yTop0, z0, sprite.u0(), sprite.v0(), color, alpha0);
/* 2392 */       sink.texturedVertex(x1, yBottom, z1, sprite.u1(), sprite.v1(), color, alpha1);
/* 2393 */       sink.texturedVertex(x0, yBottom, z0, sprite.u0(), sprite.v1(), color, alpha0);
/*      */     } else {
/* 2395 */       sink.texturedVertex(x0, yTop0, z0, sprite.u0(), sprite.v0(), color, alpha0);
/* 2396 */       sink.texturedVertex(x1, yBottom, z1, sprite.u1(), sprite.v1(), color, alpha1);
/* 2397 */       sink.texturedVertex(x1, yTop1, z1, sprite.u1(), sprite.v0(), color, alpha1);
/*      */       
/* 2399 */       sink.texturedVertex(x0, yTop0, z0, sprite.u0(), sprite.v0(), color, alpha0);
/* 2400 */       sink.texturedVertex(x0, yBottom, z0, sprite.u0(), sprite.v1(), color, alpha0);
/* 2401 */       sink.texturedVertex(x1, yBottom, z1, sprite.u1(), sprite.v1(), color, alpha1);
/*      */     } 
/*      */     
/* 2404 */     vertexCount[0] = vertexCount[0] + 6;
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
/*      */   private static float[] triangleCross(float ax, float ay, float az, float bx, float by, float bz, float cx, float cy, float cz) {
/* 2417 */     float e1x = bx - ax;
/* 2418 */     float e1y = by - ay;
/* 2419 */     float e1z = bz - az;
/* 2420 */     float e2x = cx - ax;
/* 2421 */     float e2y = cy - ay;
/* 2422 */     float e2z = cz - az;
/* 2423 */     float nx = e1y * e2z - e1z * e2y;
/* 2424 */     float ny = e1z * e2x - e1x * e2z;
/* 2425 */     float nz = e1x * e2y - e1y * e2x;
/* 2426 */     if (ny < 0.0F) {
/* 2427 */       nx = -nx;
/* 2428 */       ny = -ny;
/* 2429 */       nz = -nz;
/*      */     } 
/* 2431 */     return new float[] { nx, ny, nz };
/*      */   }
/*      */   
/*      */   private static float[] normalize(float[] v) {
/* 2435 */     float len = (float)Math.sqrt((v[0] * v[0] + v[1] * v[1] + v[2] * v[2]));
/* 2436 */     if (len < 1.0E-6F) {
/* 2437 */       return new float[] { 0.0F, 1.0F, 0.0F };
/*      */     }
/* 2439 */     return new float[] { v[0] / len, v[1] / len, v[2] / len };
/*      */   }
/*      */ 
/*      */   
/*      */   private static float[] normalizeSum(float[] a, float[] b) {
/* 2444 */     return normalize(new float[] { a[0] + b[0], a[1] + b[1], a[2] + b[2] });
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
/*      */   private static void emitFacetedCell(VertexSink sink, LodRegionFile regionFile, int spacing, int samplesPerAxis, int[] vertexCount, FadeParams fade, Registry<Biome> biomeRegistry, boolean litFormat, boolean opaquePass, boolean cullSubmergedTerrain, RegionCoord r00, int lx00, int lz00, RegionCoord r10, int lx10, int lz10, RegionCoord r01, int lx01, int lz01, RegionCoord r11, int lx11, int lz11) {
/* 2490 */     HeightmapColumn c00 = regionFile.readColumn(r00, lx00, lz00);
/* 2491 */     HeightmapColumn c10 = regionFile.readColumn(r10, lx10, lz10);
/* 2492 */     HeightmapColumn c01 = regionFile.readColumn(r01, lx01, lz01);
/* 2493 */     HeightmapColumn c11 = regionFile.readColumn(r11, lx11, lz11);
/* 2494 */     if (c00 == null || c10 == null || c01 == null || c11 == null) {
/*      */       return;
/*      */     }
/*      */ 
/*      */ 
/*      */     
/* 2500 */     if (cullSubmergedTerrain && allSubmerged(new HeightmapColumn[] { c00, c10, c01, c11 })) {
/*      */       return;
/*      */     }
/*      */     
/* 2504 */     float x0 = (r00.originBlockX() + lx00 * spacing);
/* 2505 */     float z0 = (r00.originBlockZ() + lz00 * spacing);
/* 2506 */     float x1 = (r10.originBlockX() + lx10 * spacing);
/* 2507 */     float z1 = (r01.originBlockZ() + lz01 * spacing);
/*      */     
/* 2509 */     int alpha00 = fade.alphaAt(x0, z0);
/* 2510 */     int alpha10 = fade.alphaAt(x1, z0);
/* 2511 */     int alpha01 = fade.alphaAt(x0, z1);
/* 2512 */     int alpha11 = fade.alphaAt(x1, z1);
/*      */     
/* 2514 */     boolean cellFullyOpaque = (alpha00 == 255 && alpha10 == 255 && alpha01 == 255 && alpha11 == 255);
/*      */     
/* 2516 */     if (cellFullyOpaque != opaquePass) {
/*      */       return;
/*      */     }
/*      */     
/* 2520 */     float h00 = c00.height();
/* 2521 */     float h10 = c10.height();
/* 2522 */     float h01 = c01.height();
/* 2523 */     float h11 = c11.height();
/*      */     
/* 2525 */     int color00 = vertexColor(regionFile, r00, lx00, lz00, c00, samplesPerAxis, spacing, biomeRegistry);
/* 2526 */     int color10 = vertexColor(regionFile, r10, lx10, lz10, c10, samplesPerAxis, spacing, biomeRegistry);
/* 2527 */     int color01 = vertexColor(regionFile, r01, lx01, lz01, c01, samplesPerAxis, spacing, biomeRegistry);
/* 2528 */     int color11 = vertexColor(regionFile, r11, lx11, lz11, c11, samplesPerAxis, spacing, biomeRegistry);
/* 2529 */     int flatColor = quantizeColor(bilinearColor(color00, color10, color01, color11, 0.5F, 0.5F));
/*      */     
/* 2531 */     if (litFormat) {
/*      */ 
/*      */ 
/*      */       
/* 2535 */       float[] crossA = triangleCross(x0, h00, z0, x1, h10, z0, x1, h11, z1);
/* 2536 */       float[] crossB = triangleCross(x0, h00, z0, x1, h11, z1, x0, h01, z1);
/* 2537 */       float[] faceNormal = normalizeSum(crossA, crossB);
/*      */       
/* 2539 */       sink.litVertex(x0, h00, z0, flatColor, alpha00, faceNormal[0], faceNormal[1], faceNormal[2]);
/* 2540 */       sink.litVertex(x1, h10, z0, flatColor, alpha10, faceNormal[0], faceNormal[1], faceNormal[2]);
/* 2541 */       sink.litVertex(x1, h11, z1, flatColor, alpha11, faceNormal[0], faceNormal[1], faceNormal[2]);
/*      */       
/* 2543 */       sink.litVertex(x0, h00, z0, flatColor, alpha00, faceNormal[0], faceNormal[1], faceNormal[2]);
/* 2544 */       sink.litVertex(x1, h11, z1, flatColor, alpha11, faceNormal[0], faceNormal[1], faceNormal[2]);
/* 2545 */       sink.litVertex(x0, h01, z1, flatColor, alpha01, faceNormal[0], faceNormal[1], faceNormal[2]);
/*      */     } else {
/* 2547 */       sink.vertex(x0, h00, z0, flatColor, alpha00);
/* 2548 */       sink.vertex(x1, h10, z0, flatColor, alpha10);
/* 2549 */       sink.vertex(x1, h11, z1, flatColor, alpha11);
/*      */       
/* 2551 */       sink.vertex(x0, h00, z0, flatColor, alpha00);
/* 2552 */       sink.vertex(x1, h11, z1, flatColor, alpha11);
/* 2553 */       sink.vertex(x0, h01, z1, flatColor, alpha01);
/*      */     } 
/* 2555 */     vertexCount[0] = vertexCount[0] + 6;
/*      */   }
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */   
/*      */   private static HeightmapColumn columnAt(LodRegionFile regionFile, RegionCoord region, int lx, int lz, int samplesPerAxis) {
/* 2566 */     int rx = region.x();
/* 2567 */     int rz = region.z();
/* 2568 */     if (lx < 0) {
/* 2569 */       rx--;
/* 2570 */       lx += samplesPerAxis;
/* 2571 */     } else if (lx >= samplesPerAxis) {
/* 2572 */       rx++;
/* 2573 */       lx -= samplesPerAxis;
/*      */     } 
/* 2575 */     if (lz < 0) {
/* 2576 */       rz--;
/* 2577 */       lz += samplesPerAxis;
/* 2578 */     } else if (lz >= samplesPerAxis) {
/* 2579 */       rz++;
/* 2580 */       lz -= samplesPerAxis;
/*      */     } 
/* 2582 */     return regionFile.readColumn(new RegionCoord(rx, rz), lx, lz);
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
/*      */   private static List<TreePlacement> findTreePlacements(LodRegionFile regionFile, RegionCoord region, int spacing, int samplesPerAxis, Registry<Biome> biomeRegistry, int lodLevel) {
/* 2603 */     List<TreePlacement> placements = new ArrayList<>();
/* 2604 */     float positionRandomRange = (spacing <= 4) ? (spacing * 0.2F) : (spacing * 0.4F);
/* 2605 */     float snowStart = snowStartHeightBlocks();
/* 2606 */     boolean boxGeometry = (lodLevel <= 2);
/*      */     
/* 2608 */     for (int lz = 0; lz < samplesPerAxis; lz++) {
/* 2609 */       for (int lx = 0; lx < samplesPerAxis; lx++) {
/* 2610 */         HeightmapColumn column = regionFile.readColumn(region, lx, lz);
/* 2611 */         if (column != null && column.height() > 63 && column.height() <= snowStart)
/*      */         {
/*      */           
/* 2614 */           if (column.hasTrees()) {
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */             
/* 2621 */             float perBlockAreaDensity = TreeDensityEstimator.densityPerBlockArea(biomeRegistry, column.biomeRawId()) * 2.0F;
/*      */             
/* 2623 */             float density = clamp01((spacing * spacing) * perBlockAreaDensity);
/*      */             
/* 2625 */             int blockX = region.originBlockX() + lx * spacing;
/* 2626 */             int blockZ = region.originBlockZ() + lz * spacing;
/* 2627 */             if (hashRandom01(blockX, blockZ) < density)
/*      */             
/*      */             { 
/*      */               
/* 2631 */               HeightmapColumn west = columnAt(regionFile, region, lx - 1, lz, samplesPerAxis);
/* 2632 */               HeightmapColumn east = columnAt(regionFile, region, lx + 1, lz, samplesPerAxis);
/* 2633 */               HeightmapColumn south = columnAt(regionFile, region, lx, lz - 1, samplesPerAxis);
/* 2634 */               HeightmapColumn north = columnAt(regionFile, region, lx, lz + 1, samplesPerAxis);
/* 2635 */               float dhdx = centralDifference(west, east, column, spacing);
/* 2636 */               float dhdz = centralDifference(south, north, column, spacing);
/* 2637 */               float slope = (float)Math.sqrt((dhdx * dhdx + dhdz * dhdz));
/* 2638 */               if (slope <= 0.3F)
/*      */               
/*      */               { 
/*      */ 
/*      */                 
/* 2643 */                 float offsetX = (hashRandom01(blockX ^ 0x12345678, blockZ) - 0.5F) * 2.0F * positionRandomRange;
/* 2644 */                 float offsetZ = (hashRandom01(blockX, blockZ ^ 0x87654321) - 0.5F) * 2.0F * positionRandomRange;
/* 2645 */                 int finalBlockX = Math.round(blockX + offsetX);
/* 2646 */                 int finalBlockZ = Math.round(blockZ + offsetZ);
/*      */                 
/* 2648 */                 TreeStyle style = TreeStyle.forBiome(biomeRegistry, column.biomeRawId());
/* 2649 */                 placements.add(new TreePlacement(finalBlockX, finalBlockZ, column.height(), style, boxGeometry)); }  } 
/*      */           }  } 
/*      */       } 
/* 2652 */     }  return placements;
/*      */   }
/*      */ 
/*      */   
/*      */   private static void emitTexturedTrunk(VertexSink sink, TreePlacement placement, int trunkTint, int alpha, int[] vertexCount) {
/* 2657 */     float cx = placement.blockX() + 0.5F;
/* 2658 */     float cz = placement.blockZ() + 0.5F;
/* 2659 */     float trunkTop = placement.groundY() + (placement.style()).trunkHeight;
/* 2660 */     if (placement.boxGeometry()) {
/*      */ 
/*      */       
/* 2663 */       emitBox(sink, cx - 0.5F, cz - 0.5F, cx + 0.5F, cz + 0.5F, placement
/* 2664 */           .groundY(), trunkTop, FULL_SPRITE, trunkTint, alpha, false, vertexCount);
/*      */     } else {
/*      */       
/* 2667 */       emitCrossedQuadsAny(sink, cx, cz, placement.groundY(), trunkTop, 0.5F, trunkTint, alpha, FULL_SPRITE, vertexCount);
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
/*      */   private static void emitBlockTrunk(VertexSink sink, TreePlacement placement, int trunkTint, int alpha, int[] vertexCount) {
/* 2682 */     float cx = placement.blockX() + 0.5F;
/* 2683 */     float cz = placement.blockZ() + 0.5F;
/* 2684 */     float trunkTop = placement.groundY() + (placement.style()).trunkHeight;
/* 2685 */     SurfaceMaterial.Sprite sprite = placement.style().trunkSprite();
/* 2686 */     int color = sprite.tinted() ? trunkTint : 16777215;
/* 2687 */     if (placement.boxGeometry()) {
/* 2688 */       emitBox(sink, cx - 0.5F, cz - 0.5F, cx + 0.5F, cz + 0.5F, placement
/* 2689 */           .groundY(), trunkTop, sprite, color, alpha, false, vertexCount);
/*      */     } else {
/* 2691 */       emitCrossedQuadsAny(sink, cx, cz, placement.groundY(), trunkTop, 0.5F, color, alpha, sprite, vertexCount);
/*      */     } 
/*      */   }
/*      */ 
/*      */ 
/*      */   
/*      */   private static void emitTexturedFoliage(VertexSink sink, TreePlacement placement, int foliageTint, int alpha, int[] vertexCount) {
/* 2698 */     float cx = placement.blockX() + 0.5F;
/* 2699 */     float cz = placement.blockZ() + 0.5F;
/* 2700 */     emitCanopy(sink, cx, cz, canopyBaseY(placement), placement.style(), foliageTint, alpha, FULL_SPRITE, placement
/* 2701 */         .boxGeometry(), vertexCount);
/*      */   }
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */   
/*      */   private static void emitBlockFoliage(VertexSink sink, TreePlacement placement, int foliageTint, int alpha, int[] vertexCount) {
/* 2712 */     float cx = placement.blockX() + 0.5F;
/* 2713 */     float cz = placement.blockZ() + 0.5F;
/* 2714 */     SurfaceMaterial.Sprite sprite = placement.style().foliageSprite();
/* 2715 */     int color = sprite.tinted() ? foliageTint : 16777215;
/* 2716 */     emitCanopy(sink, cx, cz, canopyBaseY(placement), placement.style(), color, alpha, sprite, placement
/* 2717 */         .boxGeometry(), vertexCount);
/*      */   }
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */   
/*      */   private static float canopyBaseY(TreePlacement placement) {
/* 2725 */     return placement.groundY() + (placement.style()).trunkHeight * 0.35F;
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
/*      */   private static void emitCanopy(VertexSink sink, float cx, float cz, float baseY, TreeStyle style, int colorRgb, int alpha, SurfaceMaterial.Sprite sprite, boolean boxGeometry, int[] vertexCount) {
/* 2741 */     switch (style.shape) {
/*      */       
/*      */       case CONICAL:
/* 2744 */         heights = new float[] { 0.0F, 0.4F, 0.75F, 1.0F };
/* 2745 */         radii = new float[] { 1.0F, 0.65F, 0.3F };
/* 2746 */         if (boxGeometry) {
/* 2747 */           emitCanopyBoxTiers(sink, cx, cz, baseY, style, colorRgb, alpha, sprite, heights, radii, vertexCount);
/*      */         } else {
/* 2749 */           emitCanopyTiers(sink, cx, cz, baseY, style, colorRgb, alpha, sprite, heights, radii, vertexCount);
/*      */         } 
/*      */         return;
/*      */       
/*      */       case FLAT_TOP:
/* 2754 */         if (boxGeometry) {
/* 2755 */           float r = style.canopyRadius;
/* 2756 */           emitBox(sink, cx - r, cz - r, cx + r, cz + r, baseY, baseY + style.canopyHeight, sprite, colorRgb, alpha, true, vertexCount);
/*      */         } else {
/*      */           
/* 2759 */           emitCrossedQuadsAny(sink, cx, cz, baseY, baseY + style.canopyHeight, style.canopyRadius, colorRgb, alpha, sprite, vertexCount);
/*      */         } 
/*      */         return;
/*      */     } 
/*      */ 
/*      */ 
/*      */     
/* 2766 */     float[] heights = { 0.0F, 0.5F, 1.0F };
/* 2767 */     float[] radii = { 1.0F, 0.6F };
/* 2768 */     if (boxGeometry) {
/* 2769 */       emitCanopyBoxTiers(sink, cx, cz, baseY, style, colorRgb, alpha, sprite, heights, radii, vertexCount);
/*      */     } else {
/* 2771 */       emitCanopyTiers(sink, cx, cz, baseY, style, colorRgb, alpha, sprite, heights, radii, vertexCount);
/*      */     } 
/*      */   }
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */   
/*      */   private static void emitCanopyTiers(VertexSink sink, float cx, float cz, float baseY, TreeStyle style, int colorRgb, int alpha, SurfaceMaterial.Sprite sprite, float[] heightFractions, float[] radiusFractions, int[] vertexCount) {
/* 2780 */     for (int i = 0; i < radiusFractions.length; i++) {
/* 2781 */       float yBottom = baseY + heightFractions[i] * style.canopyHeight;
/* 2782 */       float yTop = baseY + heightFractions[i + 1] * style.canopyHeight;
/* 2783 */       float radius = radiusFractions[i] * style.canopyRadius;
/* 2784 */       emitCrossedQuadsAny(sink, cx, cz, yBottom, yTop, radius, colorRgb, alpha, sprite, vertexCount);
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
/*      */   private static void emitCanopyBoxTiers(VertexSink sink, float cx, float cz, float baseY, TreeStyle style, int colorRgb, int alpha, SurfaceMaterial.Sprite sprite, float[] heightFractions, float[] radiusFractions, int[] vertexCount) {
/* 2801 */     for (int i = 0; i < radiusFractions.length; i++) {
/* 2802 */       float yBottom = baseY + heightFractions[i] * style.canopyHeight;
/* 2803 */       float yTop = baseY + heightFractions[i + 1] * style.canopyHeight;
/* 2804 */       float radius = radiusFractions[i] * style.canopyRadius;
/* 2805 */       emitBox(sink, cx - radius, cz - radius, cx + radius, cz + radius, yBottom, yTop, sprite, colorRgb, alpha, true, vertexCount);
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
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */   
/*      */   private static void emitBox(VertexSink sink, float x0, float z0, float x1, float z1, float yBottom, float yTop, SurfaceMaterial.Sprite sprite, int tintColor, int alpha, boolean withTop, int[] vertexCount) {
/* 2842 */     int southNorthColor = shade(tintColor, 0.8F);
/* 2843 */     int westEastColor = shade(tintColor, 0.6F);
/*      */ 
/*      */     
/* 2846 */     emitQuadTextured(sink, x0, yBottom, z0, x1, yBottom, z0, x1, yTop, z0, x0, yTop, z0, sprite, southNorthColor, alpha);
/* 2847 */     emitQuadTextured(sink, x1, yBottom, z1, x0, yBottom, z1, x0, yTop, z1, x1, yTop, z1, sprite, southNorthColor, alpha);
/* 2848 */     emitQuadTextured(sink, x0, yBottom, z1, x0, yBottom, z0, x0, yTop, z0, x0, yTop, z1, sprite, westEastColor, alpha);
/* 2849 */     emitQuadTextured(sink, x1, yBottom, z0, x1, yBottom, z1, x1, yTop, z1, x1, yTop, z0, sprite, westEastColor, alpha);
/* 2850 */     int quads = 4;
/* 2851 */     if (withTop) {
/*      */ 
/*      */ 
/*      */       
/* 2855 */       int topColor = shade(tintColor, 1.0F);
/* 2856 */       emitQuadTextured(sink, x0, yTop, z0, x1, yTop, z0, x1, yTop, z1, x0, yTop, z1, sprite, topColor, alpha);
/* 2857 */       quads = 5;
/*      */     } 
/* 2859 */     vertexCount[0] = vertexCount[0] + quads * 6;
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
/*      */   private static void emitCrossedQuadsAny(VertexSink sink, float cx, float cz, float yBottom, float yTop, float halfWidth, int colorRgb, int alpha, SurfaceMaterial.Sprite sprite, int[] vertexCount) {
/* 2871 */     if (sprite != null) {
/* 2872 */       emitCrossedQuadsTextured(sink, cx, cz, yBottom, yTop, halfWidth, sprite, colorRgb, alpha, vertexCount);
/*      */     } else {
/* 2874 */       emitCrossedQuads(sink, cx, cz, yBottom, yTop, halfWidth, colorRgb, alpha, vertexCount);
/*      */     } 
/*      */   }
/*      */ 
/*      */ 
/*      */   
/*      */   private static void emitCrossedQuads(VertexSink sink, float cx, float cz, float yBottom, float yTop, float halfWidth, int colorRgb, int alpha, int[] vertexCount) {
/* 2881 */     emitQuad(sink, cx - halfWidth, yBottom, cz, cx + halfWidth, yBottom, cz, cx + halfWidth, yTop, cz, cx - halfWidth, yTop, cz, colorRgb, alpha);
/*      */     
/* 2883 */     emitQuad(sink, cx, yBottom, cz - halfWidth, cx, yBottom, cz + halfWidth, cx, yTop, cz + halfWidth, cx, yTop, cz - halfWidth, colorRgb, alpha);
/*      */     
/* 2885 */     vertexCount[0] = vertexCount[0] + 12;
/*      */   }
/*      */ 
/*      */ 
/*      */   
/*      */   private static void emitQuad(VertexSink sink, float x0, float y0, float z0, float x1, float y1, float z1, float x2, float y2, float z2, float x3, float y3, float z3, int colorRgb, int alpha) {
/* 2891 */     sink.vertex(x0, y0, z0, colorRgb, alpha);
/* 2892 */     sink.vertex(x1, y1, z1, colorRgb, alpha);
/* 2893 */     sink.vertex(x2, y2, z2, colorRgb, alpha);
/*      */     
/* 2895 */     sink.vertex(x0, y0, z0, colorRgb, alpha);
/* 2896 */     sink.vertex(x2, y2, z2, colorRgb, alpha);
/* 2897 */     sink.vertex(x3, y3, z3, colorRgb, alpha);
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
/*      */   private static void emitCrossedQuadsTextured(VertexSink sink, float cx, float cz, float yBottom, float yTop, float halfWidth, SurfaceMaterial.Sprite sprite, int tintColor, int alpha, int[] vertexCount) {
/* 2909 */     emitQuadTextured(sink, cx - halfWidth, yBottom, cz, cx + halfWidth, yBottom, cz, cx + halfWidth, yTop, cz, cx - halfWidth, yTop, cz, sprite, tintColor, alpha);
/*      */     
/* 2911 */     emitQuadTextured(sink, cx, yBottom, cz - halfWidth, cx, yBottom, cz + halfWidth, cx, yTop, cz + halfWidth, cx, yTop, cz - halfWidth, sprite, tintColor, alpha);
/*      */     
/* 2913 */     vertexCount[0] = vertexCount[0] + 12;
/*      */   }
/*      */ 
/*      */ 
/*      */   
/*      */   private static void emitQuadTextured(VertexSink sink, float x0, float y0, float z0, float x1, float y1, float z1, float x2, float y2, float z2, float x3, float y3, float z3, SurfaceMaterial.Sprite sprite, int colorRgb, int alpha) {
/* 2919 */     sink.texturedVertex(x0, y0, z0, sprite.u0(), sprite.v1(), colorRgb, alpha);
/* 2920 */     sink.texturedVertex(x1, y1, z1, sprite.u1(), sprite.v1(), colorRgb, alpha);
/* 2921 */     sink.texturedVertex(x2, y2, z2, sprite.u1(), sprite.v0(), colorRgb, alpha);
/*      */     
/* 2923 */     sink.texturedVertex(x0, y0, z0, sprite.u0(), sprite.v1(), colorRgb, alpha);
/* 2924 */     sink.texturedVertex(x2, y2, z2, sprite.u1(), sprite.v0(), colorRgb, alpha);
/* 2925 */     sink.texturedVertex(x3, y3, z3, sprite.u0(), sprite.v0(), colorRgb, alpha);
/*      */   }
/*      */ 
/*      */   
/*      */   private static float hashRandom01(int x, int z) {
/* 2930 */     long h = x * -7046029254386353131L ^ z * -4417276706812531889L;
/* 2931 */     h ^= h >>> 33L;
/* 2932 */     h *= -49064778989728563L;
/* 2933 */     h ^= h >>> 33L;
/* 2934 */     return (float)(h >>> 40L) / 1.6777216E7F;
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
/*      */   private static int vertexColor(LodRegionFile regionFile, RegionCoord region, int lx, int lz, HeightmapColumn column, int samplesPerAxis, int spacing, Registry<Biome> biomeRegistry) {
/* 2946 */     HeightmapColumn west = columnAt(regionFile, region, lx - 1, lz, samplesPerAxis);
/* 2947 */     HeightmapColumn east = columnAt(regionFile, region, lx + 1, lz, samplesPerAxis);
/* 2948 */     HeightmapColumn south = columnAt(regionFile, region, lx, lz - 1, samplesPerAxis);
/* 2949 */     HeightmapColumn north = columnAt(regionFile, region, lx, lz + 1, samplesPerAxis);
/*      */     
/* 2951 */     float dhdx = centralDifference(west, east, column, spacing);
/* 2952 */     float dhdz = centralDifference(south, north, column, spacing);
/*      */     
/* 2954 */     float brightness = bakedSurfaceBrightness(west, east, south, north, column, spacing, 
/* 2955 */         LodSettingsConfig.get().slopeShadingFloor());
/*      */     
/* 2957 */     float slope = (float)Math.sqrt((dhdx * dhdx + dhdz * dhdz));
/* 2958 */     float dirtBlend = smoothstep(0.3F, 0.9F, slope);
/* 2959 */     float stoneBlend = smoothstep(0.9F, 1.6F, slope);
/*      */     
/* 2961 */     int blockX = region.originBlockX() + lx * spacing;
/* 2962 */     int blockZ = region.originBlockZ() + lz * spacing;
/* 2963 */     Biome biome = (biomeRegistry != null) ? (Biome)biomeRegistry.m_7942_(column.biomeRawId()) : null;
/* 2964 */     ResourceLocation biomeKey = (biome != null && biomeRegistry != null) ? biomeRegistry.m_7981_(biome) : null;
/*      */ 
/*      */ 
/*      */ 
/*      */     
/* 2969 */     boolean coldEnoughToSnow = (biome != null && biome.m_198904_(new BlockPos(blockX, column.height(), blockZ)));
/* 2970 */     boolean biomeSand = SurfaceMaterial.isSandBiome(biomeKey);
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */     
/* 2976 */     float sandBlend = biomeSand ? (1.0F - smoothstep(0.0F, 0.3F, slope)) : 0.0F;
/*      */     
/* 2978 */     int color = terrainColor(column, biomeSand, sandBlend, dirtBlend, stoneBlend, coldEnoughToSnow);
/* 2979 */     color = applyGroundTint(column, biomeRegistry, color);
/* 2980 */     color = desaturate(color, LodSettingsConfig.get().saturationReduction());
/*      */ 
/*      */ 
/*      */     
/* 2984 */     color = applyLightTemperature(color);
/* 2985 */     return shade(color, brightness);
/*      */   }
/*      */ 
/*      */   
/*      */   private static int multiplyColor(int baseColor, int tint) {
/* 2990 */     if (tint == 16777215) {
/* 2991 */       return baseColor;
/*      */     }
/* 2993 */     int r = (baseColor >> 16 & 0xFF) * (tint >> 16 & 0xFF) / 255;
/* 2994 */     int g = (baseColor >> 8 & 0xFF) * (tint >> 8 & 0xFF) / 255;
/* 2995 */     int b = (baseColor & 0xFF) * (tint & 0xFF) / 255;
/* 2996 */     return r << 16 | g << 8 | b;
/*      */   }
/*      */ 
/*      */   
/*      */   private static int applyGroundTint(HeightmapColumn column, Registry<Biome> biomeRegistry, int baseColor) {
/* 3001 */     TreeStyle.Group group = (TreeStyle.forBiome(biomeRegistry, column.biomeRawId())).group;
/* 3002 */     return BiomeStyleConfig.get().applyGroundTint(group, baseColor);
/*      */   }
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */   
/*      */   static float centralDifference(HeightmapColumn negative, HeightmapColumn positive, HeightmapColumn center, int spacing) {
/* 3012 */     if (negative != null && positive != null) {
/* 3013 */       return (positive.height() - negative.height()) / 2.0F * spacing;
/*      */     }
/* 3015 */     if (positive != null) {
/* 3016 */       return (positive.height() - center.height()) / spacing;
/*      */     }
/* 3018 */     if (negative != null) {
/* 3019 */       return (center.height() - negative.height()) / spacing;
/*      */     }
/* 3021 */     return 0.0F;
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
/*      */   private static float bakedSurfaceBrightness(HeightmapColumn west, HeightmapColumn east, HeightmapColumn south, HeightmapColumn north, HeightmapColumn center, int spacing, float slopeFloor) {
/* 3045 */     return bakedSurfaceBrightness(centralDifference(west, east, center, spacing), 
/* 3046 */         centralDifference(south, north, center, spacing), slopeFloor);
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
/*      */   static float bakedSurfaceBrightness(float dhdx, float dhdz, float slopeFloor) {
/* 3060 */     float normalLength = (float)Math.sqrt((dhdx * dhdx + 1.0F + dhdz * dhdz));
/*      */ 
/*      */ 
/*      */ 
/*      */     
/* 3065 */     float brightness = slopeFloor + (1.0F - slopeFloor) / normalLength;
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */     
/* 3077 */     float ndotl = (-dhdx * -0.4082F + 0.8165F - dhdz * -0.4082F) / normalLength;
/* 3078 */     float relief = ndotl - 0.8165F;
/* 3079 */     brightness *= 1.0F + relief * LodSettingsConfig.get().sunReliefStrength();
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */     
/* 3085 */     return Math.max(0.0F, Math.min(1.35F, brightness));
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
/*      */   static float wallSunRelief(float nx, float nz) {
/* 3112 */     float ndotl = nx * -0.4082F + nz * -0.4082F;
/* 3113 */     float relief = ndotl - 0.8165F;
/* 3114 */     return Math.max(0.0F, 
/* 3115 */         Math.min(1.35F, 1.0F + relief * LodSettingsConfig.get().sunReliefStrength()));
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
/*      */   private static int applyLightTemperature(int colorRgb) {
/* 3130 */     float temperature = LodSettingsConfig.get().lightTemperature();
/*      */     
/* 3132 */     if (temperature == 0.5F) {
/* 3133 */       return colorRgb;
/*      */     }
/* 3135 */     float warmth = (temperature - 0.5F) * 2.0F;
/* 3136 */     float redScale = 1.0F + warmth * 0.2F;
/* 3137 */     float blueScale = 1.0F - warmth * 0.2F;
/* 3138 */     int r = clampByte(Math.round((colorRgb >> 16 & 0xFF) * redScale));
/* 3139 */     int g = colorRgb >> 8 & 0xFF;
/* 3140 */     int b = clampByte(Math.round((colorRgb & 0xFF) * blueScale));
/* 3141 */     return r << 16 | g << 8 | b;
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
/*      */   static float snowStartHeightBlocks() {
/* 3166 */     ClientLevel level = (Minecraft.m_91087_()).f_91073_;
/* 3167 */     int minHeight = (level != null) ? level.m_141937_() : -64;
/* 3168 */     int maxHeight = (level != null) ? level.m_151558_() : 320;
/* 3169 */     float percent = BiomeStyleConfig.get().snowHeightPercent();
/* 3170 */     return minHeight + (maxHeight - minHeight) * percent / 100.0F;
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
/*      */   private static int terrainColor(HeightmapColumn column, boolean biomeSand, float sandBlend, float dirtBlend, float stoneBlend, boolean coldEnoughToSnow) {
/* 3195 */     int color = column.colorRgb();
/* 3196 */     if (biomeSand) {
/* 3197 */       int tintedSand = multiplyColor(14274464, LodSettingsConfig.get().nearTerrainTint(SurfaceMaterial.Kind.SAND));
/* 3198 */       color = blend(color, tintedSand, sandBlend);
/*      */     } 
/* 3200 */     color = blend(color, 7165246, dirtBlend);
/* 3201 */     color = blend(color, 9079426, stoneBlend);
/* 3202 */     int height = column.height();
/* 3203 */     if (height < 63) {
/* 3204 */       float depthFactor = clamp01((63 - height) / 20.0F);
/* 3205 */       color = blend(color, 3103882, depthFactor);
/* 3206 */     } else if (coldEnoughToSnow) {
/* 3207 */       float snowFactor = 0.8F * (1.0F - stoneBlend);
/* 3208 */       color = blend(color, 15789021, snowFactor);
/*      */     } 
/* 3210 */     return color;
/*      */   }
/*      */ 
/*      */   
/*      */   private static int desaturate(int colorRgb, float amount) {
/* 3215 */     int r = colorRgb >> 16 & 0xFF;
/* 3216 */     int g = colorRgb >> 8 & 0xFF;
/* 3217 */     int b = colorRgb & 0xFF;
/* 3218 */     int luminance = Math.round(0.299F * r + 0.587F * g + 0.114F * b);
/* 3219 */     int nr = clampByte(Math.round(r + (luminance - r) * amount));
/* 3220 */     int ng = clampByte(Math.round(g + (luminance - g) * amount));
/* 3221 */     int nb = clampByte(Math.round(b + (luminance - b) * amount));
/* 3222 */     return nr << 16 | ng << 8 | nb;
/*      */   }
/*      */   
/*      */   private static int shade(int colorRgb, float brightness) {
/* 3226 */     int r = clampByte(Math.round((colorRgb >> 16 & 0xFF) * brightness));
/* 3227 */     int g = clampByte(Math.round((colorRgb >> 8 & 0xFF) * brightness));
/* 3228 */     int b = clampByte(Math.round((colorRgb & 0xFF) * brightness));
/* 3229 */     return r << 16 | g << 8 | b;
/*      */   }
/*      */   
/*      */   private static int blend(int colorA, int colorB, float t) {
/* 3233 */     t = clamp01(t);
/* 3234 */     int ar = colorA >> 16 & 0xFF;
/* 3235 */     int ag = colorA >> 8 & 0xFF;
/* 3236 */     int ab = colorA & 0xFF;
/* 3237 */     int br = colorB >> 16 & 0xFF;
/* 3238 */     int bg = colorB >> 8 & 0xFF;
/* 3239 */     int bb = colorB & 0xFF;
/* 3240 */     int r = clampByte(Math.round(ar + (br - ar) * t));
/* 3241 */     int g = clampByte(Math.round(ag + (bg - ag) * t));
/* 3242 */     int b = clampByte(Math.round(ab + (bb - ab) * t));
/* 3243 */     return r << 16 | g << 8 | b;
/*      */   }
/*      */   
/*      */   private static float smoothstep(float edge0, float edge1, float x) {
/* 3247 */     float t = clamp01((x - edge0) / (edge1 - edge0));
/* 3248 */     return t * t * (3.0F - 2.0F * t);
/*      */   }
/*      */   
/*      */   private static float clamp01(float v) {
/* 3252 */     return Math.max(0.0F, Math.min(1.0F, v));
/*      */   }
/*      */   
/*      */   private static int clampByte(int v) {
/* 3256 */     return Math.max(0, Math.min(255, v));
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
/*      */   public static void renderTerrainLit(List<LodRegionMesh> meshes, Matrix4f modelViewMatrix, Matrix4f projectionMatrix) {
/* 3291 */     List<VertexBuffer> plainLitOpaque = new ArrayList<>();
/* 3292 */     List<VertexBuffer> plainLitFade = new ArrayList<>();
/* 3293 */     List<VertexBuffer> texturedLitOpaque = new ArrayList<>();
/* 3294 */     List<VertexBuffer> texturedLitFade = new ArrayList<>();
/*      */     
/* 3296 */     for (LodRegionMesh mesh : meshes) {
/* 3297 */       if (!mesh.terrainLit) {
/*      */         continue;
/*      */       }
/* 3300 */       if (mesh.terrainBufferOpaque != null) {
/* 3301 */         (mesh.terrainTextured ? texturedLitOpaque : plainLitOpaque).add(mesh.terrainBufferOpaque);
/*      */       }
/* 3303 */       if (mesh.terrainBufferFade != null) {
/* 3304 */         (mesh.terrainTextured ? texturedLitFade : plainLitFade).add(mesh.terrainBufferFade);
/*      */       }
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
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */     
/* 3327 */     boolean parallaxShaderReady = (!IrisCompat.isShaderPackActive() && LodParallaxShader.getOrNull() != null);
/*      */ 
/*      */ 
/*      */     
/* 3331 */     boolean backfaceCullingEnabled = LodSettingsConfig.get().backfaceCullingEnabled();
/*      */ 
/*      */ 
/*      */     
/* 3335 */     RenderType plainLitOpaqueRenderType = parallaxShaderReady ? LodTerrainRenderType.TERRAIN_PARALLAX : (backfaceCullingEnabled ? LodTerrainRenderType.TERRAIN_LIT_OPAQUE : LodTerrainRenderType.TERRAIN_LIT_OPAQUE_NOCULL);
/*      */     
/* 3337 */     Supplier<ShaderInstance> plainLitShader = parallaxShaderReady ? LodParallaxShader::getOrNull : GameRenderer::m_172640_;
/* 3338 */     renderBucket(plainLitOpaque, plainLitOpaqueRenderType, plainLitShader, modelViewMatrix, projectionMatrix);
/* 3339 */     renderBucket(plainLitFade, LodTerrainRenderType.TERRAIN_LIT, GameRenderer::m_172640_, modelViewMatrix, projectionMatrix);
/*      */     
/* 3341 */     renderBucket(texturedLitOpaque, 
/* 3342 */         backfaceCullingEnabled ? LodTerrainRenderType.TERRAIN_LIT_TEXTURED_OPAQUE : 
/* 3343 */         LodTerrainRenderType.TERRAIN_LIT_TEXTURED_OPAQUE_NOCULL, GameRenderer::m_172640_, modelViewMatrix, projectionMatrix);
/*      */     
/* 3345 */     renderBucket(texturedLitFade, LodTerrainRenderType.TERRAIN_LIT_TEXTURED, GameRenderer::m_172640_, modelViewMatrix, projectionMatrix);
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
/*      */   public static void renderTerrainCheap(List<LodRegionMesh> meshes, Matrix4f modelViewMatrix, Matrix4f projectionMatrix) {
/* 3358 */     List<VertexBuffer> plainCheapOpaque = new ArrayList<>();
/* 3359 */     List<VertexBuffer> plainCheapFade = new ArrayList<>();
/* 3360 */     List<VertexBuffer> texturedCheapOpaque = new ArrayList<>();
/* 3361 */     List<VertexBuffer> texturedCheapFade = new ArrayList<>();
/*      */     
/* 3363 */     for (LodRegionMesh mesh : meshes) {
/* 3364 */       if (mesh.terrainLit) {
/*      */         continue;
/*      */       }
/* 3367 */       if (mesh.terrainBufferOpaque != null) {
/* 3368 */         (mesh.terrainTextured ? texturedCheapOpaque : plainCheapOpaque).add(mesh.terrainBufferOpaque);
/*      */       }
/* 3370 */       if (mesh.terrainBufferFade != null) {
/* 3371 */         (mesh.terrainTextured ? texturedCheapFade : plainCheapFade).add(mesh.terrainBufferFade);
/*      */       }
/*      */     } 
/*      */ 
/*      */ 
/*      */     
/* 3377 */     boolean backfaceCullingEnabled = LodSettingsConfig.get().backfaceCullingEnabled();
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */     
/* 3388 */     boolean plainFogReady = (LodFogShader.getPlainOrNull() != null);
/* 3389 */     boolean texturedFogReady = (LodFogShader.getTexturedOrNull() != null);
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */     
/* 3395 */     RenderType plainOpaqueType = plainFogReady ? (backfaceCullingEnabled ? LodTerrainRenderType.TERRAIN_FOG_OPAQUE : LodTerrainRenderType.TERRAIN_FOG_OPAQUE_NOCULL) : (backfaceCullingEnabled ? LodTerrainRenderType.TERRAIN_OPAQUE : LodTerrainRenderType.TERRAIN_OPAQUE_NOCULL);
/*      */     
/* 3397 */     Supplier<ShaderInstance> plainOpaqueShader = plainFogReady ? LodFogShader::getPlainOrNull : GameRenderer::m_172811_;
/* 3398 */     renderBucket(plainCheapOpaque, plainOpaqueType, plainOpaqueShader, modelViewMatrix, projectionMatrix);
/*      */     
/* 3400 */     RenderType plainFadeType = plainFogReady ? LodTerrainRenderType.TERRAIN_FOG : LodTerrainRenderType.TERRAIN;
/*      */     
/* 3402 */     Supplier<ShaderInstance> plainFadeShader = plainFogReady ? LodFogShader::getPlainOrNull : GameRenderer::m_172811_;
/* 3403 */     renderBucket(plainCheapFade, plainFadeType, plainFadeShader, modelViewMatrix, projectionMatrix);
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */     
/* 3409 */     RenderType texturedOpaqueType = texturedFogReady ? (backfaceCullingEnabled ? LodTerrainRenderType.TERRAIN_FOG_TEXTURED_OPAQUE : LodTerrainRenderType.TERRAIN_FOG_TEXTURED_OPAQUE_NOCULL) : (backfaceCullingEnabled ? LodTerrainRenderType.TERRAIN_TEXTURED_OPAQUE : LodTerrainRenderType.TERRAIN_TEXTURED_OPAQUE_NOCULL);
/*      */     
/* 3411 */     Supplier<ShaderInstance> texturedOpaqueShader = texturedFogReady ? LodFogShader::getTexturedOrNull : GameRenderer::m_172814_;
/* 3412 */     renderBucket(texturedCheapOpaque, texturedOpaqueType, texturedOpaqueShader, modelViewMatrix, projectionMatrix);
/*      */ 
/*      */     
/* 3415 */     RenderType texturedFadeType = texturedFogReady ? LodTerrainRenderType.TERRAIN_FOG_TEXTURED : LodTerrainRenderType.TERRAIN_TEXTURED;
/*      */     
/* 3417 */     Supplier<ShaderInstance> texturedFadeShader = texturedFogReady ? LodFogShader::getTexturedOrNull : GameRenderer::m_172814_;
/* 3418 */     renderBucket(texturedCheapFade, texturedFadeType, texturedFadeShader, modelViewMatrix, projectionMatrix);
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
/*      */   public static void renderWaterLit(List<LodRegionMesh> meshes, Matrix4f modelViewMatrix, Matrix4f projectionMatrix) {
/* 3437 */     List<VertexBuffer> litWater = new ArrayList<>();
/* 3438 */     for (LodRegionMesh mesh : meshes) {
/* 3439 */       if (mesh.waterLit && mesh.terrainBufferWater != null) {
/* 3440 */         litWater.add(mesh.terrainBufferWater);
/*      */       }
/*      */     } 
/* 3443 */     renderBucket(litWater, LodTerrainRenderType.WATER_LIT_TEXTURED, GameRenderer::m_172640_, modelViewMatrix, projectionMatrix);
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
/*      */   public static void renderWaterCheap(List<LodRegionMesh> meshes, Matrix4f modelViewMatrix, Matrix4f projectionMatrix) {
/* 3466 */     List<VertexBuffer> cheapWater = new ArrayList<>();
/* 3467 */     for (LodRegionMesh mesh : meshes) {
/* 3468 */       if (!mesh.waterLit && mesh.terrainBufferWater != null) {
/* 3469 */         cheapWater.add(mesh.terrainBufferWater);
/*      */       }
/*      */     } 
/* 3472 */     if (cheapWater.isEmpty()) {
/*      */       return;
/*      */     }
/* 3475 */     boolean waterShaderReady = (LodWaterShader.getTexturedOrNull() != null);
/*      */     
/* 3477 */     Supplier<ShaderInstance> shader = waterShaderReady ? LodWaterShader::getTexturedOrNull : GameRenderer::m_172814_;
/* 3478 */     renderBucket(cheapWater, LodTerrainRenderType.WATER_TEXTURED, shader, modelViewMatrix, projectionMatrix);
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
/*      */   public static void renderTrees(List<LodRegionMesh> meshes, Matrix4f modelViewMatrix, Matrix4f projectionMatrix) {
/* 3504 */     Map<RenderType, List<VertexBuffer>> treeBuckets = new LinkedHashMap<>();
/*      */     
/* 3506 */     for (LodRegionMesh mesh : meshes) {
/* 3507 */       for (TreeMeshPart part : mesh.treeParts) {
/* 3508 */         ((List<VertexBuffer>)treeBuckets.computeIfAbsent(part.renderType(), key -> new ArrayList())).add(part.buffer());
/*      */       }
/*      */     } 
/* 3511 */     if (treeBuckets.isEmpty()) {
/*      */       return;
/*      */     }
/*      */     
/* 3515 */     boolean treeShaderReady = (LodTreeShader.getOrNull() != null);
/*      */     
/* 3517 */     Supplier<ShaderInstance> shader = treeShaderReady ? LodTreeShader::getOrNull : GameRenderer::m_172814_;
/*      */     
/* 3519 */     for (Map.Entry<RenderType, List<VertexBuffer>> entry : treeBuckets.entrySet()) {
/* 3520 */       renderBucket(entry.getValue(), entry.getKey(), shader, modelViewMatrix, projectionMatrix);
/*      */     }
/*      */   }
/*      */ 
/*      */ 
/*      */   
/*      */   private static void renderBucket(List<VertexBuffer> buffers, RenderType renderType, Supplier<ShaderInstance> shader, Matrix4f modelViewMatrix, Matrix4f projectionMatrix) {
/* 3527 */     if (buffers.isEmpty()) {
/*      */       return;
/*      */     }
/*      */ 
/*      */ 
/*      */     
/* 3533 */     renderType.m_110185_();
/* 3534 */     RenderSystem.setShader(shader);
/* 3535 */     for (VertexBuffer buffer : buffers) {
/* 3536 */       buffer.m_85921_();
/* 3537 */       buffer.m_253207_(modelViewMatrix, projectionMatrix, RenderSystem.getShader());
/* 3538 */       VertexBuffer.m_85931_();
/*      */     } 
/* 3540 */     renderType.m_110188_();
/*      */   }
/*      */ 
/*      */   
/*      */   public void close() {
/* 3545 */     if (this.terrainBufferOpaque != null) {
/* 3546 */       this.terrainBufferOpaque.close();
/*      */     }
/* 3548 */     if (this.terrainBufferFade != null) {
/* 3549 */       this.terrainBufferFade.close();
/*      */     }
/* 3551 */     if (this.terrainBufferWater != null) {
/* 3552 */       this.terrainBufferWater.close();
/*      */     }
/* 3554 */     for (TreeMeshPart part : this.treeParts)
/* 3555 */       part.buffer().close(); 
/*      */   }
/*      */ }


/* Location:              C:\Users\Walker\Downloads\ecstatic-forge-1.20.1-1.3.0.jar!\com\angryalchemist\ecstatic\render\LodRegionMesh.class
 * Java compiler version: 17 (61.0)
 * JD-Core Version:       1.1.3
 */