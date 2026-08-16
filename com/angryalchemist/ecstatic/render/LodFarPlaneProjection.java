/*    */ package com.angryalchemist.ecstatic.render;
/*    */ 
/*    */ import org.joml.Matrix4f;
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
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ final class LodFarPlaneProjection
/*    */ {
/*    */   private static final float NEAR_PLANE_BLOCKS = 0.05F;
/*    */   
/*    */   static Matrix4f withExtendedFarPlane(Matrix4f source, float farPlaneBlocks) {
/* 54 */     float h = 1.0F / source.m11();
/* 55 */     float aspect = source.m11() / source.m00();
/* 56 */     float fovyRadians = 2.0F * (float)Math.atan(h);
/* 57 */     return (new Matrix4f()).perspective(fovyRadians, aspect, 0.05F, farPlaneBlocks);
/*    */   }
/*    */ }


/* Location:              C:\Users\Walker\Downloads\ecstatic-forge-1.20.1-1.3.0.jar!\com\angryalchemist\ecstatic\render\LodFarPlaneProjection.class
 * Java compiler version: 17 (61.0)
 * JD-Core Version:       1.1.3
 */