/*    */ package com.angryalchemist.ecstatic.render;
/*    */ 
/*    */ import com.angryalchemist.ecstatic.Constants;
/*    */ import com.mojang.blaze3d.shaders.Uniform;
/*    */ import com.mojang.blaze3d.vertex.DefaultVertexFormat;
/*    */ import java.io.IOException;
/*    */ import net.minecraft.client.Minecraft;
/*    */ import net.minecraft.client.renderer.ShaderInstance;
/*    */ import net.minecraft.server.packs.resources.ResourceProvider;
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
/*    */ 
/*    */ 
/*    */ 
/*    */ final class LodParallaxShader
/*    */ {
/*    */   private static final String SHADER_NAME = "ecstatic_lod_terrain_parallax";
/*    */   private static ShaderInstance instance;
/*    */   private static boolean loadAttempted;
/*    */   
/*    */   static ShaderInstance getOrNull() {
/* 65 */     if (!loadAttempted) {
/* 66 */       loadAttempted = true;
/*    */       try {
/* 68 */         instance = new ShaderInstance((ResourceProvider)Minecraft.m_91087_().m_91098_(), "ecstatic_lod_terrain_parallax", DefaultVertexFormat.f_85811_);
/*    */       }
/* 70 */       catch (IOException|RuntimeException e) {
/* 71 */         Constants.LOG.error("Ecstatic: failed to load the parallax terrain shader; falling back to the plain lit terrain shader", e);
/*    */         
/* 73 */         instance = null;
/*    */       } 
/*    */     } 
/* 76 */     return instance;
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
/*    */   static void setSunDirection(float x, float y, float z) {
/* 94 */     if (instance == null) {
/*    */       return;
/*    */     }
/* 97 */     Uniform sunDir = instance.m_173348_("SunDir");
/* 98 */     if (sunDir != null)
/* 99 */       sunDir.m_5889_(x, y, z); 
/*    */   }
/*    */ }


/* Location:              C:\Users\Walker\Downloads\ecstatic-forge-1.20.1-1.3.0.jar!\com\angryalchemist\ecstatic\render\LodParallaxShader.class
 * Java compiler version: 17 (61.0)
 * JD-Core Version:       1.1.3
 */