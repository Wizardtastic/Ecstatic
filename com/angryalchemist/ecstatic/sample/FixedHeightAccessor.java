/*    */ package com.angryalchemist.ecstatic.sample;
/*    */ 
/*    */ import net.minecraft.world.level.LevelHeightAccessor;
/*    */ 
/*    */ public final class FixedHeightAccessor extends Record implements LevelHeightAccessor {
/*    */   private final int minBuildHeight;
/*    */   private final int maxBuildHeight;
/*    */   
/*    */   public FixedHeightAccessor(int minBuildHeight, int maxBuildHeight) {
/* 10 */     this.minBuildHeight = minBuildHeight; this.maxBuildHeight = maxBuildHeight; } public final String toString() { // Byte code:
/*    */     //   0: aload_0
/*    */     //   1: <illegal opcode> toString : (Lcom/angryalchemist/ecstatic/sample/FixedHeightAccessor;)Ljava/lang/String;
/*    */     //   6: areturn
/*    */     // Line number table:
/*    */     //   Java source line number -> byte code offset
/*    */     //   #10	-> 0
/*    */     // Local variable table:
/*    */     //   start	length	slot	name	descriptor
/*    */     //   0	7	0	this	Lcom/angryalchemist/ecstatic/sample/FixedHeightAccessor; } public final int hashCode() { // Byte code:
/*    */     //   0: aload_0
/*    */     //   1: <illegal opcode> hashCode : (Lcom/angryalchemist/ecstatic/sample/FixedHeightAccessor;)I
/*    */     //   6: ireturn
/*    */     // Line number table:
/*    */     //   Java source line number -> byte code offset
/*    */     //   #10	-> 0
/*    */     // Local variable table:
/*    */     //   start	length	slot	name	descriptor
/*    */     //   0	7	0	this	Lcom/angryalchemist/ecstatic/sample/FixedHeightAccessor; } public final boolean equals(Object o) { // Byte code:
/*    */     //   0: aload_0
/*    */     //   1: aload_1
/*    */     //   2: <illegal opcode> equals : (Lcom/angryalchemist/ecstatic/sample/FixedHeightAccessor;Ljava/lang/Object;)Z
/*    */     //   7: ireturn
/*    */     // Line number table:
/*    */     //   Java source line number -> byte code offset
/*    */     //   #10	-> 0
/*    */     // Local variable table:
/*    */     //   start	length	slot	name	descriptor
/*    */     //   0	8	0	this	Lcom/angryalchemist/ecstatic/sample/FixedHeightAccessor;
/* 10 */     //   0	8	1	o	Ljava/lang/Object; } public int minBuildHeight() { return this.minBuildHeight; } public int maxBuildHeight() { return this.maxBuildHeight; }
/*    */ 
/*    */   
/*    */   public int m_141928_() {
/* 14 */     return this.maxBuildHeight - this.minBuildHeight;
/*    */   }
/*    */ 
/*    */   
/*    */   public int m_141937_() {
/* 19 */     return this.minBuildHeight;
/*    */   }
/*    */ 
/*    */   
/*    */   public int m_151558_() {
/* 24 */     return this.maxBuildHeight;
/*    */   }
/*    */ }


/* Location:              C:\Users\Walker\Downloads\ecstatic-forge-1.20.1-1.3.0.jar!\com\angryalchemist\ecstatic\sample\FixedHeightAccessor.class
 * Java compiler version: 17 (61.0)
 * JD-Core Version:       1.1.3
 */