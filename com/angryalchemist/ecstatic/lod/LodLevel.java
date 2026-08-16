/*    */ package com.angryalchemist.ecstatic.lod;
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ public final class LodLevel
/*    */ {
/*    */   public static final int NONE = -1;
/*    */   public static final int VANILLA = 0;
/*    */   public static final int MAX = 5;
/*    */   
/*    */   public static int scale(int level) {
/* 15 */     if (level < 0 || level > 5) {
/* 16 */       throw new IllegalArgumentException("Invalid LOD level: " + level);
/*    */     }
/* 18 */     return 1 << level;
/*    */   }
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */   
/*    */   public static int sampleSpacingBlocks(int level) {
/* 38 */     switch (level) { case 1: 
/*    */       case 2: 
/*    */       case 3: 
/*    */       case 4:
/*    */       
/*    */       case 5:
/* 44 */        }  throw new IllegalArgumentException("No sparse-sampling spacing defined for LOD level: " + level);
/*    */   }
/*    */ }


/* Location:              C:\Users\Walker\Downloads\ecstatic-forge-1.20.1-1.3.0.jar!\com\angryalchemist\ecstatic\lod\LodLevel.class
 * Java compiler version: 17 (61.0)
 * JD-Core Version:       1.1.3
 */