/*     */ package com.angryalchemist.ecstatic.sample;
/*     */ 
/*     */ import com.angryalchemist.ecstatic.Constants;
/*     */ import java.lang.reflect.Method;
/*     */ import java.util.List;
/*     */ import java.util.Map;
/*     */ import java.util.Set;
/*     */ import java.util.concurrent.ConcurrentHashMap;
/*     */ import net.minecraft.core.Holder;
/*     */ import net.minecraft.core.HolderSet;
/*     */ import net.minecraft.core.QuartPos;
/*     */ import net.minecraft.core.Registry;
/*     */ import net.minecraft.core.registries.Registries;
/*     */ import net.minecraft.resources.ResourceLocation;
/*     */ import net.minecraft.server.level.ServerLevel;
/*     */ import net.minecraft.world.level.LevelHeightAccessor;
/*     */ import net.minecraft.world.level.biome.Biome;
/*     */ import net.minecraft.world.level.biome.BiomeSource;
/*     */ import net.minecraft.world.level.biome.Biomes;
/*     */ import net.minecraft.world.level.block.Blocks;
/*     */ import net.minecraft.world.level.block.state.BlockState;
/*     */ import net.minecraft.world.level.chunk.ChunkGenerator;
/*     */ import net.minecraft.world.level.levelgen.DensityFunction;
/*     */ import net.minecraft.world.level.levelgen.GenerationStep;
/*     */ import net.minecraft.world.level.levelgen.RandomState;
/*     */ import net.minecraft.world.level.levelgen.SurfaceSystem;
/*     */ import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
/*     */ import net.minecraft.world.level.levelgen.feature.Feature;
/*     */ import net.minecraft.world.level.levelgen.placement.PlacedFeature;
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ public final class SurfaceSampler
/*     */ {
/*     */   private static final int MARCH_STEP = 8;
/*     */   public static final int NO_HEIGHT_HINT = -2147483648;
/*     */   private static final int FLOATING_OUTLIER_THRESHOLD_BLOCKS = 24;
/*     */   private static final int GRASS_COLOR_BLEND_RADIUS = 2;
/*  78 */   private static final Map<Biome, Boolean> hasTreesCache = new ConcurrentHashMap<>();
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*  89 */   private static final Set<ResourceLocation> BADLANDS_BIOMES = Set.of(Biomes.f_48159_
/*  90 */       .m_135782_(), Biomes.f_48194_.m_135782_(), Biomes.f_186753_.m_135782_());
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */   
/* 104 */   private static final Method SURFACE_SYSTEM_GET_BAND = resolveGetBandMethod();
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   private static Method resolveGetBandMethod() {
/*     */     try {
/* 111 */       Method method = SurfaceSystem.class.getDeclaredMethod("getBand", new Class[] { int.class, int.class, int.class });
/* 112 */       method.setAccessible(true);
/* 113 */       return method;
/* 114 */     } catch (ReflectiveOperationException e) {
/* 115 */       Constants.LOG.warn("Ecstatic: could not resolve SurfaceSystem#getBand via reflection - badlands terrain will fall back to plain sampled grass color", e);
/*     */       
/* 117 */       return null;
/*     */     } 
/*     */   }
/*     */   
/*     */   public static SurfaceSample sample(ServerLevel level, int blockX, int blockZ) {
/* 122 */     ChunkGenerator generator = level.m_7726_().m_8481_();
/* 123 */     RandomState randomState = level.m_7726_().m_214994_();
/* 124 */     Registry<Biome> biomeRegistry = level.m_9598_().m_175515_(Registries.f_256952_);
/* 125 */     return sample(generator, randomState, biomeRegistry, (LevelHeightAccessor)level, blockX, blockZ, -2147483648);
/*     */   }
/*     */ 
/*     */   
/*     */   public static SurfaceSample sample(ChunkGenerator generator, RandomState randomState, Registry<Biome> biomeRegistry, LevelHeightAccessor heightAccessor, int blockX, int blockZ) {
/* 130 */     return sample(generator, randomState, biomeRegistry, heightAccessor, blockX, blockZ, -2147483648);
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   public static SurfaceSample sample(ChunkGenerator generator, RandomState randomState, Registry<Biome> biomeRegistry, LevelHeightAccessor heightAccessor, int blockX, int blockZ, int expectedHeightHint) {
/* 140 */     DensityFunction finalDensity = randomState.m_224578_().f_209391_();
/* 141 */     int height = findSurfaceHeight(finalDensity, heightAccessor, blockX, blockZ, expectedHeightHint);
/*     */     
/* 143 */     BiomeSource biomeSource = generator.m_62218_();
/* 144 */     Holder<Biome> biomeHolder = biomeSource.m_203407_(
/* 145 */         QuartPos.m_175400_(blockX), QuartPos.m_175400_(height), QuartPos.m_175400_(blockZ), randomState.m_224579_());
/* 146 */     Biome biome = (Biome)biomeHolder.m_203334_();
/* 147 */     int biomeRawId = biomeRegistry.m_7447_(biome);
/* 148 */     ResourceLocation biomeKey = biomeRegistry.m_7981_(biome);
/*     */ 
/*     */     
/* 151 */     int colorRgb = (biomeKey != null && BADLANDS_BIOMES.contains(biomeKey)) ? badlandsColor(randomState, biome, blockX, height, blockZ) : averagedGrassColor(biome, blockX, blockZ);
/* 152 */     boolean hasTrees = ((Boolean)hasTreesCache.computeIfAbsent(biome, SurfaceSampler::biomeHasTrees)).booleanValue();
/*     */     
/* 154 */     return new SurfaceSample(height, biomeRawId, colorRgb, hasTrees);
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   private static boolean biomeHasTrees(Biome biome) {
/* 165 */     List<HolderSet<PlacedFeature>> steps = biome.m_47536_().m_47818_();
/* 166 */     int stepIndex = GenerationStep.Decoration.VEGETAL_DECORATION.ordinal();
/* 167 */     if (stepIndex >= steps.size()) {
/* 168 */       return false;
/*     */     }
/* 170 */     for (Holder<PlacedFeature> placedHolder : steps.get(stepIndex)) {
/*     */       
/* 172 */       boolean isTree = ((PlacedFeature)placedHolder.m_203334_()).m_191781_().anyMatch(configured -> (configured.f_65377_() == Feature.f_65760_));
/* 173 */       if (isTree) {
/* 174 */         return true;
/*     */       }
/*     */     } 
/* 177 */     return false;
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   private static int badlandsColor(RandomState randomState, Biome biome, int blockX, int height, int blockZ) {
/* 188 */     BlockState band = badlandsBand(randomState, blockX, height, blockZ);
/* 189 */     return (band != null) ? terracottaColor(band) : averagedGrassColor(biome, blockX, blockZ);
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
/*     */   private static BlockState badlandsBand(RandomState randomState, int blockX, int blockY, int blockZ) {
/* 207 */     if (SURFACE_SYSTEM_GET_BAND == null) {
/* 208 */       return null;
/*     */     }
/*     */     try {
/* 211 */       return (BlockState)SURFACE_SYSTEM_GET_BAND.invoke(randomState.m_224580_(), new Object[] { Integer.valueOf(blockX), Integer.valueOf(blockY), Integer.valueOf(blockZ) });
/* 212 */     } catch (ReflectiveOperationException|RuntimeException e) {
/* 213 */       return null;
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
/*     */   private static int terracottaColor(BlockState band) {
/* 226 */     if (band.m_60713_(Blocks.f_50288_)) return 10703913; 
/* 227 */     if (band.m_60713_(Blocks.f_50291_)) return 12099634; 
/* 228 */     if (band.m_60713_(Blocks.f_50299_)) return 5059364; 
/* 229 */     if (band.m_60713_(Blocks.f_50301_)) return 9387310; 
/* 230 */     if (band.m_60713_(Blocks.f_50287_)) return 13743267; 
/* 231 */     if (band.m_60713_(Blocks.f_50295_)) return 10257528; 
/* 232 */     return 10117462;
/*     */   }
/*     */   
/*     */   private static int averagedGrassColor(Biome biome, int blockX, int blockZ) {
/* 236 */     int rSum = 0;
/* 237 */     int gSum = 0;
/* 238 */     int bSum = 0;
/* 239 */     int count = 0;
/* 240 */     for (int dz = -2; dz <= 2; dz++) {
/* 241 */       for (int dx = -2; dx <= 2; dx++) {
/* 242 */         int sampleColor = biome.m_47464_((blockX + dx), (blockZ + dz));
/* 243 */         rSum += sampleColor >> 16 & 0xFF;
/* 244 */         gSum += sampleColor >> 8 & 0xFF;
/* 245 */         bSum += sampleColor & 0xFF;
/* 246 */         count++;
/*     */       } 
/*     */     } 
/* 249 */     return rSum / count << 16 | gSum / count << 8 | bSum / count;
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
/*     */   private static int findSurfaceHeight(DensityFunction finalDensity, LevelHeightAccessor heightAccessor, int blockX, int blockZ, int expectedHeightHint) {
/* 261 */     int top = heightAccessor.m_151558_() - 1;
/* 262 */     int bottom = heightAccessor.m_141937_();
/*     */     
/* 264 */     int prevY = top;
/* 265 */     int y = top - 8;
/* 266 */     int fallback = bottom;
/* 267 */     while (y >= bottom) {
/* 268 */       if (isSolid(finalDensity, blockX, y, blockZ)) {
/* 269 */         int candidate = binaryRefine(finalDensity, blockX, blockZ, prevY, y) + 1;
/* 270 */         if (expectedHeightHint == Integer.MIN_VALUE || 
/* 271 */           Math.abs(candidate - expectedHeightHint) <= 24) {
/* 272 */           return candidate;
/*     */         }
/* 274 */         fallback = candidate;
/* 275 */         prevY = candidate - 1;
/* 276 */         y = prevY - 8;
/*     */         continue;
/*     */       } 
/* 279 */       prevY = y;
/* 280 */       y -= 8;
/*     */     } 
/* 282 */     return fallback;
/*     */   }
/*     */ 
/*     */   
/*     */   private static int binaryRefine(DensityFunction finalDensity, int blockX, int blockZ, int highNonSolidY, int lowSolidY) {
/* 287 */     while (highNonSolidY - lowSolidY > 1) {
/* 288 */       int mid = (highNonSolidY + lowSolidY) / 2;
/* 289 */       if (isSolid(finalDensity, blockX, mid, blockZ)) {
/* 290 */         lowSolidY = mid; continue;
/*     */       } 
/* 292 */       highNonSolidY = mid;
/*     */     } 
/*     */     
/* 295 */     return lowSolidY;
/*     */   }
/*     */   
/*     */   private static boolean isSolid(DensityFunction finalDensity, int blockX, int blockY, int blockZ) {
/* 299 */     return (finalDensity.m_207386_((DensityFunction.FunctionContext)new DensityFunction.SinglePointContext(blockX, blockY, blockZ)) > 0.0D);
/*     */   }
/*     */ }


/* Location:              C:\Users\Walker\Downloads\ecstatic-forge-1.20.1-1.3.0.jar!\com\angryalchemist\ecstatic\sample\SurfaceSampler.class
 * Java compiler version: 17 (61.0)
 * JD-Core Version:       1.1.3
 */