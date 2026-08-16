/*   */ package com.angryalchemist.ecstatic.sample;public final class SurfaceSample extends Record { private final int height; private final int biomeRawId; private final int colorRgb; private final boolean hasTrees;
/*   */   
/* 3 */   public SurfaceSample(int height, int biomeRawId, int colorRgb, boolean hasTrees) { this.height = height; this.biomeRawId = biomeRawId; this.colorRgb = colorRgb; this.hasTrees = hasTrees; } public final String toString() { // Byte code:
/*   */     //   0: aload_0
/*   */     //   1: <illegal opcode> toString : (Lcom/angryalchemist/ecstatic/sample/SurfaceSample;)Ljava/lang/String;
/*   */     //   6: areturn
/*   */     // Line number table:
/*   */     //   Java source line number -> byte code offset
/*   */     //   #3	-> 0
/*   */     // Local variable table:
/*   */     //   start	length	slot	name	descriptor
/* 3 */     //   0	7	0	this	Lcom/angryalchemist/ecstatic/sample/SurfaceSample; } public int height() { return this.height; } public final int hashCode() { // Byte code:
/*   */     //   0: aload_0
/*   */     //   1: <illegal opcode> hashCode : (Lcom/angryalchemist/ecstatic/sample/SurfaceSample;)I
/*   */     //   6: ireturn
/*   */     // Line number table:
/*   */     //   Java source line number -> byte code offset
/*   */     //   #3	-> 0
/*   */     // Local variable table:
/*   */     //   start	length	slot	name	descriptor
/*   */     //   0	7	0	this	Lcom/angryalchemist/ecstatic/sample/SurfaceSample; } public final boolean equals(Object o) { // Byte code:
/*   */     //   0: aload_0
/*   */     //   1: aload_1
/*   */     //   2: <illegal opcode> equals : (Lcom/angryalchemist/ecstatic/sample/SurfaceSample;Ljava/lang/Object;)Z
/*   */     //   7: ireturn
/*   */     // Line number table:
/*   */     //   Java source line number -> byte code offset
/*   */     //   #3	-> 0
/*   */     // Local variable table:
/*   */     //   start	length	slot	name	descriptor
/*   */     //   0	8	0	this	Lcom/angryalchemist/ecstatic/sample/SurfaceSample;
/* 3 */     //   0	8	1	o	Ljava/lang/Object; } public int biomeRawId() { return this.biomeRawId; } public int colorRgb() { return this.colorRgb; } public boolean hasTrees() { return this.hasTrees; }
/*   */    }


/* Location:              C:\Users\Walker\Downloads\ecstatic-forge-1.20.1-1.3.0.jar!\com\angryalchemist\ecstatic\sample\SurfaceSample.class
 * Java compiler version: 17 (61.0)
 * JD-Core Version:       1.1.3
 */