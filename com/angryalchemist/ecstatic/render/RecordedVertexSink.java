/*     */ package com.angryalchemist.ecstatic.render;
/*     */ 
/*     */ import java.util.Arrays;
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
/*     */ final class RecordedVertexSink
/*     */   implements VertexSink
/*     */ {
/*     */   private static final int INITIAL_CAPACITY = 256;
/*     */   
/*     */   enum Kind
/*     */   {
/*  30 */     PLAIN, LIT, TEXTURED, LIT_TEXTURED;
/*     */   }
/*     */ 
/*     */   
/*  34 */   private float[] xs = new float[256];
/*  35 */   private float[] ys = new float[256];
/*  36 */   private float[] zs = new float[256];
/*  37 */   private float[] us = new float[256];
/*  38 */   private float[] vs = new float[256];
/*  39 */   private int[] colors = new int[256];
/*  40 */   private int[] alphas = new int[256];
/*  41 */   private float[] nxs = new float[256];
/*  42 */   private float[] nys = new float[256];
/*  43 */   private float[] nzs = new float[256];
/*  44 */   private int count = 0;
/*     */   
/*     */   private void ensureCapacity() {
/*  47 */     if (this.count < this.xs.length) {
/*     */       return;
/*     */     }
/*  50 */     int newCapacity = this.xs.length * 2;
/*  51 */     this.xs = Arrays.copyOf(this.xs, newCapacity);
/*  52 */     this.ys = Arrays.copyOf(this.ys, newCapacity);
/*  53 */     this.zs = Arrays.copyOf(this.zs, newCapacity);
/*  54 */     this.us = Arrays.copyOf(this.us, newCapacity);
/*  55 */     this.vs = Arrays.copyOf(this.vs, newCapacity);
/*  56 */     this.colors = Arrays.copyOf(this.colors, newCapacity);
/*  57 */     this.alphas = Arrays.copyOf(this.alphas, newCapacity);
/*  58 */     this.nxs = Arrays.copyOf(this.nxs, newCapacity);
/*  59 */     this.nys = Arrays.copyOf(this.nys, newCapacity);
/*  60 */     this.nzs = Arrays.copyOf(this.nzs, newCapacity);
/*     */   }
/*     */ 
/*     */   
/*     */   private void record(float x, float y, float z, float u, float v, int colorRgb, int alpha, float nx, float ny, float nz) {
/*  65 */     ensureCapacity();
/*  66 */     this.xs[this.count] = x;
/*  67 */     this.ys[this.count] = y;
/*  68 */     this.zs[this.count] = z;
/*  69 */     this.us[this.count] = u;
/*  70 */     this.vs[this.count] = v;
/*  71 */     this.colors[this.count] = colorRgb;
/*  72 */     this.alphas[this.count] = alpha;
/*  73 */     this.nxs[this.count] = nx;
/*  74 */     this.nys[this.count] = ny;
/*  75 */     this.nzs[this.count] = nz;
/*  76 */     this.count++;
/*     */   }
/*     */ 
/*     */   
/*     */   public void vertex(float x, float y, float z, int colorRgb, int alpha) {
/*  81 */     record(x, y, z, 0.0F, 0.0F, colorRgb, alpha, 0.0F, 0.0F, 0.0F);
/*     */   }
/*     */ 
/*     */   
/*     */   public void litVertex(float x, float y, float z, int colorRgb, int alpha, float nx, float ny, float nz) {
/*  86 */     record(x, y, z, 0.0F, 0.0F, colorRgb, alpha, nx, ny, nz);
/*     */   }
/*     */ 
/*     */   
/*     */   public void texturedVertex(float x, float y, float z, float u, float v, int colorRgb, int alpha) {
/*  91 */     record(x, y, z, u, v, colorRgb, alpha, 0.0F, 0.0F, 0.0F);
/*     */   }
/*     */ 
/*     */ 
/*     */   
/*     */   public void litTexturedVertex(float x, float y, float z, float u, float v, int colorRgb, int alpha, float nx, float ny, float nz) {
/*  97 */     record(x, y, z, u, v, colorRgb, alpha, nx, ny, nz);
/*     */   }
/*     */   
/*     */   int count() {
/* 101 */     return this.count;
/*     */   }
/*     */   
/*     */   boolean isEmpty() {
/* 105 */     return (this.count == 0);
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
/*     */   void replayInto(VertexSink target, Kind kind) {
/*     */     int i;
/* 118 */     switch (kind) {
/*     */       case PLAIN:
/* 120 */         for (i = 0; i < this.count; i++) {
/* 121 */           target.vertex(this.xs[i], this.ys[i], this.zs[i], this.colors[i], this.alphas[i]);
/*     */         }
/*     */         break;
/*     */       case LIT:
/* 125 */         for (i = 0; i < this.count; i++) {
/* 126 */           target.litVertex(this.xs[i], this.ys[i], this.zs[i], this.colors[i], this.alphas[i], this.nxs[i], this.nys[i], this.nzs[i]);
/*     */         }
/*     */         break;
/*     */       case TEXTURED:
/* 130 */         for (i = 0; i < this.count; i++) {
/* 131 */           target.texturedVertex(this.xs[i], this.ys[i], this.zs[i], this.us[i], this.vs[i], this.colors[i], this.alphas[i]);
/*     */         }
/*     */         break;
/*     */       case LIT_TEXTURED:
/* 135 */         for (i = 0; i < this.count; i++)
/* 136 */           target.litTexturedVertex(this.xs[i], this.ys[i], this.zs[i], this.us[i], this.vs[i], this.colors[i], this.alphas[i], this.nxs[i], this.nys[i], this.nzs[i]); 
/*     */         break;
/*     */     } 
/*     */   }
/*     */ }


/* Location:              C:\Users\Walker\Downloads\ecstatic-forge-1.20.1-1.3.0.jar!\com\angryalchemist\ecstatic\render\RecordedVertexSink.class
 * Java compiler version: 17 (61.0)
 * JD-Core Version:       1.1.3
 */