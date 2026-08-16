/*    */ package com.angryalchemist.ecstatic.render;
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
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ final class Entry
/*    */ {
/*    */   int trunkTint;
/*    */   int foliageTint;
/* 46 */   int groundTint = 16777215;
/*    */ 
/*    */ 
/*    */   
/*    */   boolean groundTintEnabled = true;
/*    */ 
/*    */ 
/*    */   
/*    */   String trunkTexture;
/*    */ 
/*    */ 
/*    */   
/*    */   String foliageTexture;
/*    */ 
/*    */ 
/*    */ 
/*    */   
/*    */   Entry() {}
/*    */ 
/*    */ 
/*    */ 
/*    */   
/*    */   Entry(int trunkTint, int foliageTint) {
/* 69 */     this.trunkTint = trunkTint;
/* 70 */     this.foliageTint = foliageTint;
/*    */   }
/*    */ }


/* Location:              C:\Users\Walker\Downloads\ecstatic-forge-1.20.1-1.3.0.jar!\com\angryalchemist\ecstatic\render\BiomeStyleConfig$Entry.class
 * Java compiler version: 17 (61.0)
 * JD-Core Version:       1.1.3
 */