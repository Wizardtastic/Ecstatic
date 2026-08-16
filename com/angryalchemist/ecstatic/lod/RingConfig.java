/*     */ package com.angryalchemist.ecstatic.lod;
/*     */ 
/*     */ 
/*     */ public final class RingConfig
/*     */ {
/*   6 */   private final int[] outerBoundaryChunks = new int[6];
/*     */   public final int hysteresisChunks;
/*     */   public static final int RING_START_MARGIN_CHUNKS = 12;
/*     */   
/*     */   public RingConfig(int renderDistanceChunks, int lod1Width, int lod2Width, int lod3Width, int lod4Width, int lod5Width, int hysteresisChunks) {
/*  11 */     this.outerBoundaryChunks[0] = renderDistanceChunks;
/*  12 */     this.outerBoundaryChunks[1] = this.outerBoundaryChunks[0] + lod1Width;
/*  13 */     this.outerBoundaryChunks[2] = this.outerBoundaryChunks[1] + lod2Width;
/*  14 */     this.outerBoundaryChunks[3] = this.outerBoundaryChunks[2] + lod3Width;
/*  15 */     this.outerBoundaryChunks[4] = this.outerBoundaryChunks[3] + lod4Width;
/*  16 */     this.outerBoundaryChunks[5] = this.outerBoundaryChunks[4] + lod5Width;
/*  17 */     this.hysteresisChunks = hysteresisChunks;
/*     */   }
/*     */   
/*     */   public static RingConfig defaults(int renderDistanceChunks) {
/*  21 */     return new RingConfig(renderDistanceChunks, 16, 32, 64, 128, 256, 4);
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
/*     */   public static RingConfig scaled(int renderDistanceChunks, int lod1Width, int lod2Width, int lod3Width, int lod4Width, int lod5Width, int hysteresisChunks, float widthScale) {
/*  35 */     return new RingConfig(renderDistanceChunks, 
/*  36 */         scaleWidth(lod1Width, widthScale), scaleWidth(lod2Width, widthScale), 
/*  37 */         scaleWidth(lod3Width, widthScale), scaleWidth(lod4Width, widthScale), 
/*  38 */         scaleWidth(lod5Width, widthScale), hysteresisChunks);
/*     */   }
/*     */   
/*     */   private static int scaleWidth(int baseWidth, float widthScale) {
/*  42 */     return Math.max(1, Math.round(baseWidth * widthScale));
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
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   public static int ring1StartChunks(int clientRenderDistanceChunks) {
/*  72 */     return Math.max(2, clientRenderDistanceChunks - 12);
/*     */   }
/*     */   
/*     */   public int outerBoundary(int level) {
/*  76 */     return this.outerBoundaryChunks[level];
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   public int resolveLevel(double distanceChunks, int previousLevel) {
/*  85 */     int level = previousLevel;
/*  86 */     while (level < 5 && distanceChunks > (outerBoundary(level) + this.hysteresisChunks)) {
/*  87 */       level++;
/*     */     }
/*  89 */     while (level > 0 && distanceChunks < (outerBoundary(level - 1) - this.hysteresisChunks)) {
/*  90 */       level--;
/*     */     }
/*  92 */     return level;
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   public int classify(double distanceChunks) {
/* 100 */     for (int level = 0; level <= 5; level++) {
/* 101 */       if (distanceChunks <= outerBoundary(level)) {
/* 102 */         return level;
/*     */       }
/*     */     } 
/* 105 */     return -1;
/*     */   }
/*     */ }


/* Location:              C:\Users\Walker\Downloads\ecstatic-forge-1.20.1-1.3.0.jar!\com\angryalchemist\ecstatic\lod\RingConfig.class
 * Java compiler version: 17 (61.0)
 * JD-Core Version:       1.1.3
 */