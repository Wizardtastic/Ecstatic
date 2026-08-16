/*     */ package com.angryalchemist.ecstatic.render;
/*     */ 
/*     */ import java.lang.reflect.Field;
/*     */ import java.util.List;
/*     */ import java.util.Map;
/*     */ import java.util.concurrent.ConcurrentHashMap;
/*     */ import net.minecraft.core.Holder;
/*     */ import net.minecraft.core.HolderSet;
/*     */ import net.minecraft.core.Registry;
/*     */ import net.minecraft.util.Mth;
/*     */ import net.minecraft.util.valueproviders.IntProvider;
/*     */ import net.minecraft.world.level.biome.Biome;
/*     */ import net.minecraft.world.level.levelgen.GenerationStep;
/*     */ import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
/*     */ import net.minecraft.world.level.levelgen.feature.Feature;
/*     */ import net.minecraft.world.level.levelgen.placement.CountPlacement;
/*     */ import net.minecraft.world.level.levelgen.placement.PlacedFeature;
/*     */ import net.minecraft.world.level.levelgen.placement.PlacementModifier;
/*     */ import net.minecraft.world.level.levelgen.placement.RarityFilter;
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ final class TreeDensityEstimator
/*     */ {
/*     */   private static final float BLOCKS_PER_CHUNK_AREA = 256.0F;
/*     */   private static final float MIN_DENSITY_PER_BLOCK_AREA = 6.6666666E-4F;
/*     */   private static final float MAX_DENSITY_PER_BLOCK_AREA = 0.06666667F;
/*  58 */   private static final Field COUNT_PLACEMENT_COUNT_FIELD = resolveField(CountPlacement.class, "count");
/*  59 */   private static final Field RARITY_FILTER_CHANCE_FIELD = resolveField(RarityFilter.class, "chance");
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*  66 */   private static final Map<Biome, Float> densityCache = new ConcurrentHashMap<>();
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   static float densityPerBlockArea(Registry<Biome> biomeRegistry, int biomeRawId) {
/*  73 */     if (biomeRegistry == null) {
/*  74 */       return 0.006666667F;
/*     */     }
/*  76 */     Biome biome = (Biome)biomeRegistry.m_7942_(biomeRawId);
/*  77 */     if (biome == null) {
/*  78 */       return 0.006666667F;
/*     */     }
/*  80 */     return ((Float)densityCache.computeIfAbsent(biome, TreeDensityEstimator::estimate)).floatValue();
/*     */   }
/*     */   
/*     */   private static float estimate(Biome biome) {
/*  84 */     List<HolderSet<PlacedFeature>> steps = biome.m_47536_().m_47818_();
/*  85 */     int stepIndex = GenerationStep.Decoration.VEGETAL_DECORATION.ordinal();
/*  86 */     if (stepIndex >= steps.size()) {
/*  87 */       return 0.006666667F;
/*     */     }
/*     */     
/*  90 */     float expectedTreesPerChunk = 0.0F;
/*  91 */     boolean anyTree = false;
/*  92 */     for (Holder<PlacedFeature> placedHolder : steps.get(stepIndex)) {
/*  93 */       PlacedFeature placedFeature = (PlacedFeature)placedHolder.m_203334_();
/*  94 */       boolean isTree = placedFeature.m_191781_().anyMatch(configured -> (configured.f_65377_() == Feature.f_65760_));
/*  95 */       if (!isTree) {
/*     */         continue;
/*     */       }
/*  98 */       anyTree = true;
/*  99 */       expectedTreesPerChunk += expectedCount(placedFeature);
/*     */     } 
/*     */     
/* 102 */     if (!anyTree || expectedTreesPerChunk <= 0.0F) {
/* 103 */       return 0.006666667F;
/*     */     }
/* 105 */     float densityPerBlockArea = expectedTreesPerChunk / 256.0F;
/* 106 */     return Mth.m_14036_(densityPerBlockArea, 6.6666666E-4F, 0.06666667F);
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
/*     */   private static float expectedCount(PlacedFeature placedFeature) {
/* 118 */     float expected = 1.0F;
/* 119 */     for (PlacementModifier modifier : placedFeature.f_191776_()) {
/* 120 */       if (COUNT_PLACEMENT_COUNT_FIELD != null && modifier instanceof CountPlacement) {
/*     */         try {
/* 122 */           IntProvider provider = (IntProvider)COUNT_PLACEMENT_COUNT_FIELD.get(modifier);
/* 123 */           expected *= (provider.m_142739_() + provider.m_142737_()) / 2.0F;
/* 124 */         } catch (ReflectiveOperationException|ClassCastException reflectiveOperationException) {}
/*     */         continue;
/*     */       } 
/* 127 */       if (RARITY_FILTER_CHANCE_FIELD != null && modifier instanceof RarityFilter) {
/*     */         try {
/* 129 */           int chance = ((Integer)RARITY_FILTER_CHANCE_FIELD.get(modifier)).intValue();
/* 130 */           if (chance > 0) {
/* 131 */             expected /= chance;
/*     */           }
/* 133 */         } catch (ReflectiveOperationException|ClassCastException reflectiveOperationException) {}
/*     */       }
/*     */     } 
/*     */ 
/*     */     
/* 138 */     return expected;
/*     */   }
/*     */   
/*     */   private static Field resolveField(Class<?> owner, String name) {
/*     */     try {
/* 143 */       Field field = owner.getDeclaredField(name);
/* 144 */       field.setAccessible(true);
/* 145 */       return field;
/* 146 */     } catch (ReflectiveOperationException|SecurityException e) {
/* 147 */       return null;
/*     */     } 
/*     */   }
/*     */ }


/* Location:              C:\Users\Walker\Downloads\ecstatic-forge-1.20.1-1.3.0.jar!\com\angryalchemist\ecstatic\render\TreeDensityEstimator.class
 * Java compiler version: 17 (61.0)
 * JD-Core Version:       1.1.3
 */