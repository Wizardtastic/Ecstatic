/*     */ package com.angryalchemist.ecstatic.render;
/*     */ 
/*     */ import com.angryalchemist.ecstatic.Constants;
/*     */ import com.mojang.blaze3d.shaders.Uniform;
/*     */ import com.mojang.blaze3d.vertex.DefaultVertexFormat;
/*     */ import java.io.IOException;
/*     */ import net.minecraft.client.Minecraft;
/*     */ import net.minecraft.client.renderer.ShaderInstance;
/*     */ import net.minecraft.server.packs.resources.ResourceProvider;
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
/*     */ final class LodWaterShader
/*     */ {
/*     */   private static final String PLAIN_SHADER_NAME = "ecstatic_lod_water";
/*     */   private static final String TEXTURED_SHADER_NAME = "ecstatic_lod_water_tex";
/*     */   private static ShaderInstance plainInstance;
/*     */   private static boolean plainLoadAttempted;
/*     */   private static ShaderInstance texturedInstance;
/*     */   private static boolean texturedLoadAttempted;
/*     */   
/*     */   static ShaderInstance getPlainOrNull() {
/*  58 */     if (!plainLoadAttempted) {
/*  59 */       plainLoadAttempted = true;
/*     */       try {
/*  61 */         plainInstance = new ShaderInstance((ResourceProvider)Minecraft.m_91087_().m_91098_(), "ecstatic_lod_water", DefaultVertexFormat.f_85815_);
/*     */       }
/*  63 */       catch (IOException|RuntimeException e) {
/*  64 */         Constants.LOG.error("Ecstatic: failed to load the plain ocean-plane water shader; falling back to the plain position_color shader with CPU per-vertex specular", e);
/*     */         
/*  66 */         plainInstance = null;
/*     */       } 
/*     */     } 
/*  69 */     return plainInstance;
/*     */   }
/*     */ 
/*     */   
/*     */   static ShaderInstance getTexturedOrNull() {
/*  74 */     if (!texturedLoadAttempted) {
/*  75 */       texturedLoadAttempted = true;
/*     */       try {
/*  77 */         texturedInstance = new ShaderInstance((ResourceProvider)Minecraft.m_91087_().m_91098_(), "ecstatic_lod_water_tex", DefaultVertexFormat.f_85818_);
/*     */       }
/*  79 */       catch (IOException|RuntimeException e) {
/*  80 */         Constants.LOG.error("Ecstatic: failed to load the textured ocean-plane water shader; falling back to the plain position_color_tex shader", e);
/*     */         
/*  82 */         texturedInstance = null;
/*     */       } 
/*     */     } 
/*  85 */     return texturedInstance;
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
/*     */   static void setSunDirection(float x, float y, float z) {
/*  97 */     setSunDirection(plainInstance, x, y, z);
/*  98 */     setSunDirection(texturedInstance, x, y, z);
/*     */   }
/*     */   
/*     */   private static void setSunDirection(ShaderInstance instance, float x, float y, float z) {
/* 102 */     if (instance == null) {
/*     */       return;
/*     */     }
/* 105 */     Uniform sunDir = instance.m_173348_("SunDir");
/* 106 */     if (sunDir != null) {
/* 107 */       sunDir.m_5889_(x, y, z);
/*     */     }
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
/*     */ 
/*     */   
/*     */   static void setGameTime(float dayFraction) {
/* 122 */     setGameTime(plainInstance, dayFraction);
/* 123 */     setGameTime(texturedInstance, dayFraction);
/*     */   }
/*     */   
/*     */   private static void setGameTime(ShaderInstance instance, float dayFraction) {
/* 127 */     if (instance == null) {
/*     */       return;
/*     */     }
/* 130 */     Uniform gameTime = instance.m_173348_("GameTime");
/* 131 */     if (gameTime != null) {
/* 132 */       gameTime.m_5985_(dayFraction);
/*     */     }
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   static void setFogIntensity(float intensity) {
/* 143 */     setFogIntensity(plainInstance, intensity);
/* 144 */     setFogIntensity(texturedInstance, intensity);
/*     */   }
/*     */   
/*     */   private static void setFogIntensity(ShaderInstance instance, float intensity) {
/* 148 */     if (instance == null) {
/*     */       return;
/*     */     }
/* 151 */     Uniform fogIntensity = instance.m_173348_("FogIntensity");
/* 152 */     if (fogIntensity != null)
/* 153 */       fogIntensity.m_5985_(intensity); 
/*     */   }
/*     */ }


/* Location:              C:\Users\Walker\Downloads\ecstatic-forge-1.20.1-1.3.0.jar!\com\angryalchemist\ecstatic\render\LodWaterShader.class
 * Java compiler version: 17 (61.0)
 * JD-Core Version:       1.1.3
 */