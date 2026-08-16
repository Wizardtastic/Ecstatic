/*     */ package com.angryalchemist.ecstatic.render;
/*     */ 
/*     */ import com.angryalchemist.ecstatic.storage.HeightmapColumn;
/*     */ import java.util.List;
/*     */ import java.util.Set;
/*     */ import net.minecraft.client.Minecraft;
/*     */ import net.minecraft.client.renderer.block.model.BakedQuad;
/*     */ import net.minecraft.client.renderer.texture.TextureAtlasSprite;
/*     */ import net.minecraft.client.resources.model.BakedModel;
/*     */ import net.minecraft.core.BlockPos;
/*     */ import net.minecraft.core.Direction;
/*     */ import net.minecraft.core.Registry;
/*     */ import net.minecraft.resources.ResourceLocation;
/*     */ import net.minecraft.util.RandomSource;
/*     */ import net.minecraft.world.level.biome.Biome;
/*     */ import net.minecraft.world.level.biome.Biomes;
/*     */ import net.minecraft.world.level.block.Blocks;
/*     */ import net.minecraft.world.level.block.state.BlockState;
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ final class SurfaceMaterial
/*     */ {
/*     */   private static final float DIRT_SLOPE_START = 0.6F;
/*     */   private static final float STONE_SLOPE_START = 1.25F;
/*  83 */   private static final Set<ResourceLocation> SAND_BIOMES = Set.of(Biomes.f_48203_
/*  84 */       .m_135782_(), Biomes.f_48217_.m_135782_(), Biomes.f_48148_.m_135782_(), Biomes.f_48166_
/*  85 */       .m_135782_(), Biomes.f_48167_.m_135782_(), Biomes.f_48170_.m_135782_());
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*  94 */   private static final Set<ResourceLocation> BADLANDS_BIOMES = Set.of(Biomes.f_48159_
/*  95 */       .m_135782_(), Biomes.f_48194_.m_135782_(), Biomes.f_186753_.m_135782_());
/*     */   
/*  97 */   static final SurfaceMaterial GRASS = new SurfaceMaterial(Kind.GRASS, Blocks.f_50440_.m_49966_(), Blocks.f_50493_.m_49966_());
/*  98 */   static final SurfaceMaterial DIRT = new SurfaceMaterial(Kind.DIRT, Blocks.f_50493_.m_49966_(), Blocks.f_50493_.m_49966_());
/*  99 */   static final SurfaceMaterial STONE = new SurfaceMaterial(Kind.STONE, Blocks.f_50069_.m_49966_(), Blocks.f_50069_.m_49966_());
/* 100 */   static final SurfaceMaterial SAND = new SurfaceMaterial(Kind.SAND, Blocks.f_49992_.m_49966_(), Blocks.f_49992_.m_49966_());
/* 101 */   static final SurfaceMaterial SNOW = new SurfaceMaterial(Kind.SNOW, Blocks.f_50127_.m_49966_(), Blocks.f_50127_.m_49966_());
/* 102 */   static final SurfaceMaterial RED_SAND = new SurfaceMaterial(Kind.RED_SAND, Blocks.f_49993_.m_49966_(), Blocks.f_49993_.m_49966_());
/* 103 */   static final SurfaceMaterial TERRACOTTA = new SurfaceMaterial(Kind.TERRACOTTA, Blocks.f_50352_.m_49966_(), Blocks.f_50352_.m_49966_());
/*     */   private final Kind kind;
/*     */   private final BlockState topState;
/*     */   private final BlockState sideState;
/*     */   private Sprite topSprite;
/*     */   private Sprite sideSprite;
/*     */   
/* 110 */   enum Kind { GRASS, DIRT, STONE, SAND, SNOW, RED_SAND, TERRACOTTA; }
/*     */   static final class Sprite extends Record { private final float u0; private final float u1; private final float v0; private final float v1; private final boolean tinted;
/*     */     
/* 113 */     Sprite(float u0, float u1, float v0, float v1, boolean tinted) { this.u0 = u0; this.u1 = u1; this.v0 = v0; this.v1 = v1; this.tinted = tinted; } public final String toString() { // Byte code:
/*     */       //   0: aload_0
/*     */       //   1: <illegal opcode> toString : (Lcom/angryalchemist/ecstatic/render/SurfaceMaterial$Sprite;)Ljava/lang/String;
/*     */       //   6: areturn
/*     */       // Line number table:
/*     */       //   Java source line number -> byte code offset
/*     */       //   #113	-> 0
/*     */       // Local variable table:
/*     */       //   start	length	slot	name	descriptor
/* 113 */       //   0	7	0	this	Lcom/angryalchemist/ecstatic/render/SurfaceMaterial$Sprite; } public float u0() { return this.u0; } public final int hashCode() { // Byte code:
/*     */       //   0: aload_0
/*     */       //   1: <illegal opcode> hashCode : (Lcom/angryalchemist/ecstatic/render/SurfaceMaterial$Sprite;)I
/*     */       //   6: ireturn
/*     */       // Line number table:
/*     */       //   Java source line number -> byte code offset
/*     */       //   #113	-> 0
/*     */       // Local variable table:
/*     */       //   start	length	slot	name	descriptor
/*     */       //   0	7	0	this	Lcom/angryalchemist/ecstatic/render/SurfaceMaterial$Sprite; } public final boolean equals(Object o) { // Byte code:
/*     */       //   0: aload_0
/*     */       //   1: aload_1
/*     */       //   2: <illegal opcode> equals : (Lcom/angryalchemist/ecstatic/render/SurfaceMaterial$Sprite;Ljava/lang/Object;)Z
/*     */       //   7: ireturn
/*     */       // Line number table:
/*     */       //   Java source line number -> byte code offset
/*     */       //   #113	-> 0
/*     */       // Local variable table:
/*     */       //   start	length	slot	name	descriptor
/*     */       //   0	8	0	this	Lcom/angryalchemist/ecstatic/render/SurfaceMaterial$Sprite;
/* 113 */       //   0	8	1	o	Ljava/lang/Object; } public float u1() { return this.u1; } public float v0() { return this.v0; } public float v1() { return this.v1; } public boolean tinted() { return this.tinted; }
/*     */      }
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   private SurfaceMaterial(Kind kind, BlockState topState, BlockState sideState) {
/* 122 */     this.kind = kind;
/* 123 */     this.topState = topState;
/* 124 */     this.sideState = sideState;
/*     */   }
/*     */   
/*     */   Kind kind() {
/* 128 */     return this.kind;
/*     */   }
/*     */   
/*     */   Sprite topSprite() {
/* 132 */     if (this.topSprite == null) {
/* 133 */       this.topSprite = resolveSprite(this.topState, Direction.UP);
/*     */     }
/* 135 */     return this.topSprite;
/*     */   }
/*     */   
/*     */   Sprite sideSprite() {
/* 139 */     if (this.sideSprite == null) {
/* 140 */       this.sideSprite = resolveSprite(this.sideState, Direction.NORTH);
/*     */     }
/* 142 */     return this.sideSprite;
/*     */   }
/*     */ 
/*     */   
/*     */   static Sprite resolveSprite(BlockState state, Direction face) {
/* 147 */     BakedModel model = Minecraft.m_91087_().m_91289_().m_110910_(state);
/* 148 */     List<BakedQuad> quads = model.m_213637_(state, face, RandomSource.m_216327_());
/* 149 */     if (quads.isEmpty()) {
/* 150 */       quads = model.m_213637_(state, null, RandomSource.m_216327_());
/*     */     }
/* 152 */     if (quads.isEmpty()) {
/* 153 */       TextureAtlasSprite particle = model.m_6160_();
/* 154 */       return new Sprite(particle.m_118409_(), particle.m_118410_(), particle.m_118411_(), particle.m_118412_(), false);
/*     */     } 
/* 156 */     BakedQuad quad = quads.get(0);
/* 157 */     TextureAtlasSprite sprite = quad.m_173410_();
/* 158 */     return new Sprite(sprite.m_118409_(), sprite.m_118410_(), sprite.m_118411_(), sprite.m_118412_(), (quad.m_111305_() >= 0));
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   static SurfaceMaterial classify(HeightmapColumn column, Registry<Biome> biomeRegistry, float slope, int blockX, int blockZ) {
/* 167 */     Biome biome = biomeOf(biomeRegistry, column.biomeRawId());
/* 168 */     ResourceLocation biomeKey = (biome != null) ? biomeRegistry.m_7981_(biome) : null;
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */     
/* 186 */     boolean tooSteepForSnow = (slope >= 1.25F);
/* 187 */     boolean coldEnoughToSnow = (biome != null && biome.m_198904_(new BlockPos(blockX, column.height(), blockZ)));
/* 188 */     if (!tooSteepForSnow && coldEnoughToSnow) {
/* 189 */       return SNOW;
/*     */     }
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */     
/* 198 */     if (biomeKey != null && BADLANDS_BIOMES.contains(biomeKey)) {
/* 199 */       return (slope >= 0.6F) ? TERRACOTTA : RED_SAND;
/*     */     }
/*     */ 
/*     */ 
/*     */     
/* 204 */     if (isSandBiome(biomeKey) && slope < 0.6F) {
/* 205 */       return SAND;
/*     */     }
/* 207 */     if (slope >= 1.25F) {
/* 208 */       return STONE;
/*     */     }
/* 210 */     if (slope >= 0.6F) {
/* 211 */       return DIRT;
/*     */     }
/* 213 */     return GRASS;
/*     */   }
/*     */ 
/*     */   
/*     */   static boolean isSandBiome(ResourceLocation biomeKey) {
/* 218 */     return (biomeKey != null && SAND_BIOMES.contains(biomeKey));
/*     */   }
/*     */   
/*     */   private static Biome biomeOf(Registry<Biome> biomeRegistry, int biomeRawId) {
/* 222 */     return (biomeRegistry != null) ? (Biome)biomeRegistry.m_7942_(biomeRawId) : null;
/*     */   }
/*     */ }


/* Location:              C:\Users\Walker\Downloads\ecstatic-forge-1.20.1-1.3.0.jar!\com\angryalchemist\ecstatic\render\SurfaceMaterial.class
 * Java compiler version: 17 (61.0)
 * JD-Core Version:       1.1.3
 */