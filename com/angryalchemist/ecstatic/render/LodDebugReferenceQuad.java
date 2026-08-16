/*    */ package com.angryalchemist.ecstatic.render;
/*    */ 
/*    */ import com.mojang.blaze3d.systems.RenderSystem;
/*    */ import com.mojang.blaze3d.vertex.BufferBuilder;
/*    */ import com.mojang.blaze3d.vertex.DefaultVertexFormat;
/*    */ import com.mojang.blaze3d.vertex.Tesselator;
/*    */ import com.mojang.blaze3d.vertex.VertexBuffer;
/*    */ import com.mojang.blaze3d.vertex.VertexFormat;
/*    */ import com.mojang.math.Axis;
/*    */ import net.minecraft.client.Camera;
/*    */ import net.minecraft.client.renderer.GameRenderer;
/*    */ import net.minecraft.world.phys.Vec3;
/*    */ import org.joml.Matrix4f;
/*    */ import org.joml.Quaternionfc;
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
/*    */ public final class LodDebugReferenceQuad
/*    */ {
/*    */   private static VertexBuffer vertexBuffer;
/*    */   
/*    */   public static void render(Camera camera, Matrix4f projectionMatrix) {
/* 31 */     if (vertexBuffer == null) {
/* 32 */       vertexBuffer = new VertexBuffer(VertexBuffer.Usage.DYNAMIC);
/*    */     }
/*    */     
/* 35 */     Vec3 cameraPos = camera.m_90583_();
/*    */     
/* 37 */     BufferBuilder builder = Tesselator.m_85913_().m_85915_();
/* 38 */     builder.m_166779_(VertexFormat.Mode.TRIANGLES, DefaultVertexFormat.f_85815_);
/*    */     
/* 40 */     float x0 = (float)cameraPos.f_82479_ - 10.0F;
/* 41 */     float x1 = (float)cameraPos.f_82479_ + 10.0F;
/* 42 */     float y = (float)cameraPos.f_82480_ - 2.0F;
/* 43 */     float z0 = (float)cameraPos.f_82481_ - 10.0F;
/* 44 */     float z1 = (float)cameraPos.f_82481_ + 10.0F;
/*    */     
/* 46 */     vertex(builder, x0, y, z0);
/* 47 */     vertex(builder, x1, y, z0);
/* 48 */     vertex(builder, x1, y, z1);
/*    */     
/* 50 */     vertex(builder, x0, y, z0);
/* 51 */     vertex(builder, x1, y, z1);
/* 52 */     vertex(builder, x0, y, z1);
/*    */     
/* 54 */     vertexBuffer.m_85921_();
/* 55 */     vertexBuffer.m_231221_(builder.m_231175_());
/*    */     
/* 57 */     Matrix4f modelViewMatrix = new Matrix4f();
/* 58 */     modelViewMatrix.rotate((Quaternionfc)Axis.f_252529_.m_252977_(camera.m_90589_()));
/* 59 */     modelViewMatrix.rotate((Quaternionfc)Axis.f_252436_.m_252977_(camera.m_90590_() + 180.0F));
/* 60 */     modelViewMatrix.translate(-((float)cameraPos.f_82479_), -((float)cameraPos.f_82480_), -((float)cameraPos.f_82481_));
/*    */     
/* 62 */     RenderSystem.disableCull();
/* 63 */     LodTerrainRenderType.TERRAIN.m_110185_();
/* 64 */     RenderSystem.setShader(GameRenderer::m_172811_);
/* 65 */     vertexBuffer.m_253207_(modelViewMatrix, projectionMatrix, RenderSystem.getShader());
/* 66 */     LodTerrainRenderType.TERRAIN.m_110188_();
/*    */     
/* 68 */     VertexBuffer.m_85931_();
/*    */   }
/*    */   
/*    */   private static void vertex(BufferBuilder builder, float x, float y, float z) {
/* 72 */     builder.m_5483_(x, y, z).m_6122_(255, 0, 255, 255).m_5752_();
/*    */   }
/*    */ }


/* Location:              C:\Users\Walker\Downloads\ecstatic-forge-1.20.1-1.3.0.jar!\com\angryalchemist\ecstatic\render\LodDebugReferenceQuad.class
 * Java compiler version: 17 (61.0)
 * JD-Core Version:       1.1.3
 */