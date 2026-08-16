/*    */ package com.angryalchemist.ecstatic.render;
/*    */ 
/*    */ import com.mojang.blaze3d.vertex.BufferBuilder;
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
/*    */ final class BufferBuilderVertexSink
/*    */   implements VertexSink
/*    */ {
/*    */   private final BufferBuilder builder;
/*    */   
/*    */   BufferBuilderVertexSink(BufferBuilder builder) {
/* 20 */     this.builder = builder;
/*    */   }
/*    */ 
/*    */   
/*    */   public void vertex(float x, float y, float z, int colorRgb, int alpha) {
/* 25 */     int r = colorRgb >> 16 & 0xFF;
/* 26 */     int g = colorRgb >> 8 & 0xFF;
/* 27 */     int b = colorRgb & 0xFF;
/* 28 */     this.builder.m_5483_(x, y, z).m_6122_(r, g, b, alpha).m_5752_();
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
/*    */   public void litVertex(float x, float y, float z, int colorRgb, int alpha, float nx, float ny, float nz) {
/* 40 */     int r = colorRgb >> 16 & 0xFF;
/* 41 */     int g = colorRgb >> 8 & 0xFF;
/* 42 */     int b = colorRgb & 0xFF;
/* 43 */     this.builder.m_5483_(x, y, z).m_6122_(r, g, b, alpha).m_7421_(0.5F, 0.5F)
/* 44 */       .m_85969_(15728640).m_5601_(nx, ny, nz).m_5752_();
/*    */   }
/*    */ 
/*    */   
/*    */   public void texturedVertex(float x, float y, float z, float u, float v, int colorRgb, int alpha) {
/* 49 */     int r = colorRgb >> 16 & 0xFF;
/* 50 */     int g = colorRgb >> 8 & 0xFF;
/* 51 */     int b = colorRgb & 0xFF;
/* 52 */     this.builder.m_5483_(x, y, z).m_6122_(r, g, b, alpha).m_7421_(u, v).m_5752_();
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
/*    */   public void litTexturedVertex(float x, float y, float z, float u, float v, int colorRgb, int alpha, float nx, float ny, float nz) {
/* 70 */     int r = colorRgb >> 16 & 0xFF;
/* 71 */     int g = colorRgb >> 8 & 0xFF;
/* 72 */     int b = colorRgb & 0xFF;
/* 73 */     this.builder.m_5483_(x, y, z).m_6122_(r, g, b, alpha).m_7421_(u, v).m_85969_(15728640)
/* 74 */       .m_5601_(nx, ny, nz).m_5752_();
/*    */   }
/*    */ }


/* Location:              C:\Users\Walker\Downloads\ecstatic-forge-1.20.1-1.3.0.jar!\com\angryalchemist\ecstatic\render\BufferBuilderVertexSink.class
 * Java compiler version: 17 (61.0)
 * JD-Core Version:       1.1.3
 */