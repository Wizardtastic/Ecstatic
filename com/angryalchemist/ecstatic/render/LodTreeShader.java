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
/*    */ final class LodTreeShader
/*    */ {
/*    */   private static final String SHADER_NAME = "ecstatic_lod_tree";
/*    */   private static ShaderInstance instance;
/*    */   private static boolean loadAttempted;
/*    */   
/*    */   static ShaderInstance getOrNull() {
/* 43 */     if (!loadAttempted) {
/* 44 */       loadAttempted = true;
/*    */       try {
/* 46 */         instance = new ShaderInstance((ResourceProvider)Minecraft.m_91087_().m_91098_(), "ecstatic_lod_tree", DefaultVertexFormat.f_85818_);
/*    */       }
/* 48 */       catch (IOException|RuntimeException e) {
/* 49 */         Constants.LOG.error("Ecstatic: failed to load the tree billboard shader; falling back to the plain (fog-less) position_color_tex shader", e);
/*    */         
/* 51 */         instance = null;
/*    */       } 
/*    */     } 
/* 54 */     return instance;
/*    */   }
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */   
/*    */   static void setFogIntensity(float intensity) {
/* 64 */     if (instance == null) {
/*    */       return;
/*    */     }
/* 67 */     Uniform fogIntensity = instance.m_173348_("FogIntensity");
/* 68 */     if (fogIntensity != null)
/* 69 */       fogIntensity.m_5985_(intensity); 
/*    */   }
/*    */ }


/* Location:              C:\Users\Walker\Downloads\ecstatic-forge-1.20.1-1.3.0.jar!\com\angryalchemist\ecstatic\render\LodTreeShader.class
 * Java compiler version: 17 (61.0)
 * JD-Core Version:       1.1.3
 */