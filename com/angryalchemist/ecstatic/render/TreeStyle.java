/*     */ package com.angryalchemist.ecstatic.render;
/*     */ import java.util.Set;
/*     */ import net.minecraft.core.Registry;
/*     */ import net.minecraft.resources.ResourceLocation;
/*     */ import net.minecraft.world.level.biome.Biome;
/*     */ import net.minecraft.world.level.biome.Biomes;
/*     */ import net.minecraft.world.level.block.Blocks;
/*     */ import net.minecraft.world.level.block.state.BlockState;
/*     */ 
/*     */ final class TreeStyle {
/*     */   final Group group;
/*     */   final int trunkColor;
/*     */   final int foliageColor;
/*     */   final float trunkHeight;
/*     */   final float canopyHeight;
/*     */   final float canopyRadius;
/*     */   final CanopyShape shape;
/*     */   private final BlockState trunkBlockState;
/*     */   private final BlockState foliageBlockState;
/*     */   private SurfaceMaterial.Sprite trunkSprite;
/*     */   private SurfaceMaterial.Sprite foliageSprite;
/*     */   
/*     */   enum CanopyShape {
/*  24 */     ROUND, CONICAL, FLAT_TOP;
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   enum Group
/*     */   {
/*  32 */     DEFAULT, CONIFER, BIRCH, JUNGLE, SAVANNA, DARK_FOREST, SWAMP, CHERRY, BEACH;
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
/*     */   private TreeStyle(Group group, int trunkColor, int foliageColor, float trunkHeight, float canopyHeight, float canopyRadius, CanopyShape shape, BlockState trunkBlockState, BlockState foliageBlockState) {
/*  56 */     this.group = group;
/*  57 */     this.trunkColor = trunkColor;
/*  58 */     this.foliageColor = foliageColor;
/*  59 */     this.trunkHeight = trunkHeight;
/*  60 */     this.canopyHeight = canopyHeight;
/*  61 */     this.canopyRadius = canopyRadius;
/*  62 */     this.shape = shape;
/*  63 */     this.trunkBlockState = trunkBlockState;
/*  64 */     this.foliageBlockState = foliageBlockState;
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
/*     */   SurfaceMaterial.Sprite trunkSprite() {
/*  78 */     if (this.trunkSprite == null) {
/*  79 */       this.trunkSprite = SurfaceMaterial.resolveSprite(this.trunkBlockState, Direction.NORTH);
/*     */     }
/*  81 */     return this.trunkSprite;
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
/*     */   SurfaceMaterial.Sprite foliageSprite() {
/*  94 */     if (this.foliageSprite == null) {
/*  95 */       this.foliageSprite = SurfaceMaterial.resolveSprite(this.foliageBlockState, Direction.NORTH);
/*     */     }
/*  97 */     return this.foliageSprite;
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
/* 120 */   private static final TreeStyle DEFAULT = new TreeStyle(Group.DEFAULT, 4861719, 6985529, 6.0F, 4.0F, 2.5F, CanopyShape.ROUND, Blocks.f_49999_
/* 121 */       .m_49966_(), Blocks.f_50050_.m_49966_());
/* 122 */   private static final TreeStyle CONIFER = new TreeStyle(Group.CONIFER, 3877404, 6127969, 6.0F, 6.5F, 2.9F, CanopyShape.CONICAL, Blocks.f_50000_
/* 123 */       .m_49966_(), Blocks.f_50051_.m_49966_());
/* 124 */   private static final TreeStyle BIRCH = new TreeStyle(Group.BIRCH, 11778739, 9219676, 5.5F, 4.0F, 3.1F, CanopyShape.ROUND, Blocks.f_50001_
/* 125 */       .m_49966_(), Blocks.f_50052_.m_49966_());
/* 126 */   private static final TreeStyle JUNGLE = new TreeStyle(Group.JUNGLE, 4862745, 5220410, 8.0F, 6.0F, 4.4F, CanopyShape.ROUND, Blocks.f_50002_
/* 127 */       .m_49966_(), Blocks.f_50053_.m_49966_());
/* 128 */   private static final TreeStyle SAVANNA = new TreeStyle(Group.SAVANNA, 6048302, 10391114, 6.5F, 2.2F, 4.7F, CanopyShape.FLAT_TOP, Blocks.f_50003_
/* 129 */       .m_49966_(), Blocks.f_50054_.m_49966_());
/*     */ 
/*     */   
/* 132 */   private static final TreeStyle DARK_FOREST = new TreeStyle(Group.DARK_FOREST, 3811352, 5208634, 6.0F, 5.5F, 4.7F, CanopyShape.ROUND, Blocks.f_50004_
/* 133 */       .m_49966_(), Blocks.f_50055_.m_49966_());
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */   
/* 140 */   private static final TreeStyle SWAMP = new TreeStyle(Group.SWAMP, 4865322, 6057538, 5.0F, 4.0F, 3.8F, CanopyShape.ROUND, Blocks.f_49999_
/* 141 */       .m_49966_(), Blocks.f_50050_.m_49966_());
/* 142 */   private static final TreeStyle CHERRY = new TreeStyle(Group.CHERRY, 6046776, 15180996, 4.5F, 4.0F, 3.5F, CanopyShape.ROUND, Blocks.f_271170_
/* 143 */       .m_49966_(), Blocks.f_271115_.m_49966_());
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */   
/* 151 */   private static final TreeStyle BEACH = new TreeStyle(Group.BEACH, 9073493, 12757352, 3.5F, 3.0F, 2.6F, CanopyShape.FLAT_TOP, Blocks.f_49999_
/* 152 */       .m_49966_(), Blocks.f_50050_.m_49966_());
/*     */   
/* 154 */   private static final Set<ResourceLocation> CONIFER_BIOMES = Set.of(Biomes.f_48206_
/* 155 */       .m_135782_(), Biomes.f_186763_.m_135782_(), Biomes.f_186764_
/* 156 */       .m_135782_(), Biomes.f_48152_.m_135782_(), Biomes.f_186755_
/* 157 */       .m_135782_());
/* 158 */   private static final Set<ResourceLocation> BIRCH_BIOMES = Set.of(Biomes.f_48149_
/* 159 */       .m_135782_(), Biomes.f_186762_.m_135782_());
/* 160 */   private static final Set<ResourceLocation> JUNGLE_BIOMES = Set.of(Biomes.f_48222_
/* 161 */       .m_135782_(), Biomes.f_48197_.m_135782_(), Biomes.f_186769_.m_135782_());
/* 162 */   private static final Set<ResourceLocation> SAVANNA_BIOMES = Set.of(Biomes.f_48157_
/* 163 */       .m_135782_(), Biomes.f_48158_.m_135782_(), Biomes.f_186768_.m_135782_());
/* 164 */   private static final Set<ResourceLocation> DARK_FOREST_BIOMES = Set.of(Biomes.f_48151_.m_135782_());
/* 165 */   private static final Set<ResourceLocation> SWAMP_BIOMES = Set.of(Biomes.f_48207_
/* 166 */       .m_135782_(), Biomes.f_220595_.m_135782_());
/* 167 */   private static final Set<ResourceLocation> CHERRY_BIOMES = Set.of(Biomes.f_271432_.m_135782_());
/* 168 */   private static final Set<ResourceLocation> BEACH_BIOMES = Set.of(Biomes.f_48217_
/* 169 */       .m_135782_(), Biomes.f_48148_.m_135782_());
/*     */   
/*     */   static TreeStyle forBiome(Registry<Biome> biomeRegistry, int biomeRawId) {
/* 172 */     if (biomeRegistry == null) {
/* 173 */       return DEFAULT;
/*     */     }
/* 175 */     Biome biome = (Biome)biomeRegistry.m_7942_(biomeRawId);
/* 176 */     if (biome == null) {
/* 177 */       return DEFAULT;
/*     */     }
/* 179 */     ResourceLocation key = biomeRegistry.m_7981_(biome);
/* 180 */     if (key == null) {
/* 181 */       return DEFAULT;
/*     */     }
/* 183 */     if (CONIFER_BIOMES.contains(key)) return CONIFER; 
/* 184 */     if (BIRCH_BIOMES.contains(key)) return BIRCH; 
/* 185 */     if (JUNGLE_BIOMES.contains(key)) return JUNGLE; 
/* 186 */     if (SAVANNA_BIOMES.contains(key)) return SAVANNA; 
/* 187 */     if (DARK_FOREST_BIOMES.contains(key)) return DARK_FOREST; 
/* 188 */     if (SWAMP_BIOMES.contains(key)) return SWAMP; 
/* 189 */     if (CHERRY_BIOMES.contains(key)) return CHERRY; 
/* 190 */     if (BEACH_BIOMES.contains(key)) return BEACH; 
/* 191 */     return DEFAULT;
/*     */   }
/*     */ 
/*     */   
/*     */   static TreeStyle forGroup(Group group) {
/* 196 */     switch (group) { default: throw new IncompatibleClassChangeError();case CONIFER: case BIRCH: case JUNGLE: case SAVANNA: case DARK_FOREST: case SWAMP: case CHERRY: case BEACH: case DEFAULT: break; }  return 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */       
/* 205 */       DEFAULT;
/*     */   }
/*     */ }


/* Location:              C:\Users\Walker\Downloads\ecstatic-forge-1.20.1-1.3.0.jar!\com\angryalchemist\ecstatic\render\TreeStyle.class
 * Java compiler version: 17 (61.0)
 * JD-Core Version:       1.1.3
 */