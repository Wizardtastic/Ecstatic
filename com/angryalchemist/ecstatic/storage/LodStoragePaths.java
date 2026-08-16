/*     */ package com.angryalchemist.ecstatic.storage;
/*     */ 
/*     */ import com.angryalchemist.ecstatic.Constants;
/*     */ import java.io.IOException;
/*     */ import java.nio.file.DirectoryStream;
/*     */ import java.nio.file.Files;
/*     */ import java.nio.file.Path;
/*     */ import net.minecraft.resources.ResourceKey;
/*     */ import net.minecraft.world.level.Level;
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ public final class LodStoragePaths
/*     */ {
/*     */   private static final int MIN_FINGERPRINT_FILES_PRESENT = 1;
/*     */   
/*     */   public static Path dimensionStorageDir(Path worldRoot, ResourceKey<Level> dimension) {
/*  58 */     String dimensionDirName = dimension.m_135782_().m_135827_() + "_" + dimension.m_135782_().m_135827_();
/*  59 */     Path current = worldRoot.resolve("ecstatic").resolve(dimensionDirName);
/*  60 */     if (Files.isDirectory(current, new java.nio.file.LinkOption[0])) {
/*  61 */       return current;
/*     */     }
/*  63 */     Path legacy = findLegacyDimensionStorageDir(worldRoot, dimensionDirName);
/*  64 */     return (legacy != null) ? legacy : current;
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
/*     */   private static Path findLegacyDimensionStorageDir(Path worldRoot, String dimensionDirName) {
/*  86 */     if (!Files.isDirectory(worldRoot, new java.nio.file.LinkOption[0]))
/*  87 */       return null; 
/*     */     
/*  89 */     try { DirectoryStream<Path> topLevel = Files.newDirectoryStream(worldRoot); 
/*  90 */       try { for (Path candidate : topLevel)
/*  91 */         { if (!Files.isDirectory(candidate, new java.nio.file.LinkOption[0]) || candidate.getFileName().toString().equals("ecstatic")) {
/*     */             continue;
/*     */           }
/*  94 */           Path candidateDimensionDir = candidate.resolve(dimensionDirName);
/*  95 */           if (looksLikeRegionStorageDir(candidateDimensionDir))
/*  96 */           { Path path = candidateDimensionDir;
/*     */ 
/*     */             
/*  99 */             if (topLevel != null) topLevel.close();  return path; }  }  if (topLevel != null) topLevel.close();  } catch (Throwable throwable) { if (topLevel != null) try { topLevel.close(); } catch (Throwable throwable1) { throwable.addSuppressed(throwable1); }   throw throwable; }  } catch (IOException e)
/* 100 */     { Constants.LOG.warn("Ecstatic: failed to scan {} for pre-existing LOD data; treating as a fresh install for this dimension", worldRoot, e); }
/*     */ 
/*     */     
/* 103 */     return null;
/*     */   }
/*     */ 
/*     */   
/*     */   private static boolean looksLikeRegionStorageDir(Path dimensionDir) {
/* 108 */     if (!Files.isDirectory(dimensionDir, new java.nio.file.LinkOption[0])) {
/* 109 */       return false;
/*     */     }
/* 111 */     int present = 0;
/* 112 */     for (int level = 0; level <= 5; level++) {
/* 113 */       if (Files.isRegularFile(regionFile(dimensionDir, level), new java.nio.file.LinkOption[0])) {
/* 114 */         present++;
/*     */       }
/*     */     } 
/* 117 */     return (present >= 1);
/*     */   }
/*     */   
/*     */   public static Path regionFile(Path dimensionStorageDir, int lodLevel) {
/* 121 */     return dimensionStorageDir.resolve("lod" + lodLevel + ".dat");
/*     */   }
/*     */ }


/* Location:              C:\Users\Walker\Downloads\ecstatic-forge-1.20.1-1.3.0.jar!\com\angryalchemist\ecstatic\storage\LodStoragePaths.class
 * Java compiler version: 17 (61.0)
 * JD-Core Version:       1.1.3
 */