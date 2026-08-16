/*     */ package com.angryalchemist.ecstatic.debug;
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ public final class LodDebugState
/*     */ {
/*     */   private static volatile boolean enabled = false;
/*     */   
/*     */   public static boolean isEnabled() {
/*  27 */     return enabled;
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   public static void setEnabled(boolean value) {
/*  35 */     enabled = value;
/*  36 */     if (!value) {
/*  37 */       fogDisabled = false;
/*  38 */       forcedLevel = 0;
/*  39 */       referenceQuadEnabled = false;
/*  40 */       vertexFormatOverride = 0;
/*     */     } 
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   private static volatile boolean fogDisabled = false;
/*     */ 
/*     */   
/*  50 */   private static volatile int forcedLevel = 0;
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   private static volatile boolean referenceQuadEnabled = false;
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*  61 */   private static volatile int vertexFormatOverride = 0;
/*     */   
/*     */   public static boolean isFogDisabled() {
/*  64 */     return (enabled && fogDisabled);
/*     */   }
/*     */   
/*     */   public static boolean toggleFog() {
/*  68 */     fogDisabled = !fogDisabled;
/*  69 */     return fogDisabled;
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   public static boolean isReferenceQuadEnabled() {
/*  79 */     return (enabled && referenceQuadEnabled);
/*     */   }
/*     */   
/*     */   public static boolean toggleReferenceQuad() {
/*  83 */     referenceQuadEnabled = !referenceQuadEnabled;
/*  84 */     return referenceQuadEnabled;
/*     */   }
/*     */ 
/*     */   
/*     */   public static int forcedLevel() {
/*  89 */     return enabled ? forcedLevel : 0;
/*     */   }
/*     */   
/*     */   public static int cycleForcedLevel() {
/*  93 */     switch (forcedLevel) { case 0: 
/*     */       case 1: 
/*     */       case 2:
/*     */       
/*     */       case 3:
/*  98 */        }  forcedLevel = 0;
/*     */     
/* 100 */     return forcedLevel;
/*     */   }
/*     */ 
/*     */   
/*     */   public static int vertexFormatOverride() {
/* 105 */     return enabled ? vertexFormatOverride : 0;
/*     */   }
/*     */   
/*     */   public static int cycleVertexFormatOverride() {
/* 109 */     switch (vertexFormatOverride) { case 0:
/*     */       
/*     */       case 1:
/* 112 */        }  vertexFormatOverride = 0;
/*     */     
/* 114 */     return vertexFormatOverride;
/*     */   }
/*     */ }


/* Location:              C:\Users\Walker\Downloads\ecstatic-forge-1.20.1-1.3.0.jar!\com\angryalchemist\ecstatic\debug\LodDebugState.class
 * Java compiler version: 17 (61.0)
 * JD-Core Version:       1.1.3
 */