/*     */ package com.angryalchemist.ecstatic.lod;
/*     */ 
/*     */ import java.util.ArrayList;
/*     */ import java.util.LinkedHashSet;
/*     */ import java.util.List;
/*     */ import java.util.Set;
/*     */ import net.minecraft.core.Holder;
/*     */ import net.minecraft.server.level.ServerLevel;
/*     */ import net.minecraft.world.level.ChunkPos;
/*     */ import net.minecraft.world.level.chunk.ChunkGeneratorStructureState;
/*     */ import net.minecraft.world.level.levelgen.structure.StructureSet;
/*     */ import net.minecraft.world.level.levelgen.structure.placement.RandomSpreadStructurePlacement;
/*     */ import net.minecraft.world.level.levelgen.structure.placement.StructurePlacement;
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ public final class StructureChunkLocator
/*     */ {
/*     */   public static List<ChunkPos> candidateStartChunks(ServerLevel level, int minChunkX, int minChunkZ, int maxChunkX, int maxChunkZ) {
/*  61 */     ChunkGeneratorStructureState state = level.m_7726_().m_255415_();
/*  62 */     long seed = state.m_254887_();
/*     */ 
/*     */ 
/*     */     
/*  66 */     Set<ChunkPos> candidates = new LinkedHashSet<>();
/*     */     
/*  68 */     for (Holder<StructureSet> holder : (Iterable<Holder<StructureSet>>)state.m_255252_()) {
/*  69 */       StructurePlacement placement = ((StructureSet)holder.m_203334_()).f_210004_();
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */       
/*  76 */       if (placement instanceof RandomSpreadStructurePlacement) { RandomSpreadStructurePlacement spread = (RandomSpreadStructurePlacement)placement;
/*     */ 
/*     */         
/*  79 */         int spacing = spread.m_205003_();
/*  80 */         if (spacing <= 0) {
/*     */           continue;
/*     */         }
/*     */ 
/*     */ 
/*     */ 
/*     */         
/*  87 */         int minCellX = Math.floorDiv(minChunkX, spacing);
/*  88 */         int maxCellX = Math.floorDiv(maxChunkX, spacing);
/*  89 */         int minCellZ = Math.floorDiv(minChunkZ, spacing);
/*  90 */         int maxCellZ = Math.floorDiv(maxChunkZ, spacing);
/*  91 */         for (int cellX = minCellX; cellX <= maxCellX; cellX++) {
/*  92 */           for (int cellZ = minCellZ; cellZ <= maxCellZ; cellZ++) {
/*     */ 
/*     */             
/*  95 */             ChunkPos candidate = spread.m_227008_(seed, cellX * spacing, cellZ * spacing);
/*  96 */             if (candidate.f_45578_ >= minChunkX && candidate.f_45578_ <= maxChunkX && candidate.f_45579_ >= minChunkZ && candidate.f_45579_ <= maxChunkZ)
/*     */             {
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */               
/* 103 */               if (placement.m_255071_(state, candidate.f_45578_, candidate.f_45579_))
/* 104 */                 candidates.add(candidate); 
/*     */             }
/*     */           } 
/*     */         }  }
/*     */     
/*     */     } 
/* 110 */     return new ArrayList<>(candidates);
/*     */   }
/*     */ }


/* Location:              C:\Users\Walker\Downloads\ecstatic-forge-1.20.1-1.3.0.jar!\com\angryalchemist\ecstatic\lod\StructureChunkLocator.class
 * Java compiler version: 17 (61.0)
 * JD-Core Version:       1.1.3
 */