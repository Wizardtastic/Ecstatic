/*     */ package com.angryalchemist.ecstatic.render;
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
/*     */ 
/*     */ final class Sprite
/*     */   extends Record
/*     */ {
/*     */   private final float u0;
/*     */   private final float u1;
/*     */   private final float v0;
/*     */   private final float v1;
/*     */   private final boolean tinted;
/*     */   
/*     */   public final String toString() {
/*     */     // Byte code:
/*     */     //   0: aload_0
/*     */     //   1: <illegal opcode> toString : (Lcom/angryalchemist/ecstatic/render/SurfaceMaterial$Sprite;)Ljava/lang/String;
/*     */     //   6: areturn
/*     */     // Line number table:
/*     */     //   Java source line number -> byte code offset
/*     */     //   #113	-> 0
/*     */     // Local variable table:
/*     */     //   start	length	slot	name	descriptor
/*     */     //   0	7	0	this	Lcom/angryalchemist/ecstatic/render/SurfaceMaterial$Sprite;
/*     */   }
/*     */   
/*     */   public final int hashCode() {
/*     */     // Byte code:
/*     */     //   0: aload_0
/*     */     //   1: <illegal opcode> hashCode : (Lcom/angryalchemist/ecstatic/render/SurfaceMaterial$Sprite;)I
/*     */     //   6: ireturn
/*     */     // Line number table:
/*     */     //   Java source line number -> byte code offset
/*     */     //   #113	-> 0
/*     */     // Local variable table:
/*     */     //   start	length	slot	name	descriptor
/*     */     //   0	7	0	this	Lcom/angryalchemist/ecstatic/render/SurfaceMaterial$Sprite;
/*     */   }
/*     */   
/*     */   public final boolean equals(Object o) {
/*     */     // Byte code:
/*     */     //   0: aload_0
/*     */     //   1: aload_1
/*     */     //   2: <illegal opcode> equals : (Lcom/angryalchemist/ecstatic/render/SurfaceMaterial$Sprite;Ljava/lang/Object;)Z
/*     */     //   7: ireturn
/*     */     // Line number table:
/*     */     //   Java source line number -> byte code offset
/*     */     //   #113	-> 0
/*     */     // Local variable table:
/*     */     //   start	length	slot	name	descriptor
/*     */     //   0	8	0	this	Lcom/angryalchemist/ecstatic/render/SurfaceMaterial$Sprite;
/*     */     //   0	8	1	o	Ljava/lang/Object;
/*     */   }
/*     */   
/*     */   Sprite(float u0, float u1, float v0, float v1, boolean tinted) {
/* 113 */     this.u0 = u0; this.u1 = u1; this.v0 = v0; this.v1 = v1; this.tinted = tinted; } public float u0() { return this.u0; } public float u1() { return this.u1; } public float v0() { return this.v0; } public float v1() { return this.v1; } public boolean tinted() { return this.tinted; }
/*     */ 
/*     */ }


/* Location:              C:\Users\Walker\Downloads\ecstatic-forge-1.20.1-1.3.0.jar!\com\angryalchemist\ecstatic\render\SurfaceMaterial$Sprite.class
 * Java compiler version: 17 (61.0)
 * JD-Core Version:       1.1.3
 */