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
/*     */ final class LodFogShader
/*     */ {
/*     */   private static final String PLAIN_SHADER_NAME = "ecstatic_lod_terrain_fog";
/*     */   private static final String TEXTURED_SHADER_NAME = "ecstatic_lod_terrain_fog_tex";
/*     */   private static ShaderInstance plainInstance;
/*     */   private static boolean plainLoadAttempted;
/*     */   private static ShaderInstance texturedInstance;
/*     */   private static boolean texturedLoadAttempted;
/*     */   
/*     */   static ShaderInstance getPlainOrNull() {
/*  53 */     if (!plainLoadAttempted) {
/*  54 */       plainLoadAttempted = true;
/*     */       try {
/*  56 */         plainInstance = new ShaderInstance((ResourceProvider)Minecraft.m_91087_().m_91098_(), "ecstatic_lod_terrain_fog", DefaultVertexFormat.f_85815_);
/*     */       }
/*  58 */       catch (IOException|RuntimeException e) {
/*  59 */         Constants.LOG.error("Ecstatic: failed to load the cheap-terrain fog shader; falling back to the plain (fog-less) position_color shader", e);
/*     */         
/*  61 */         plainInstance = null;
/*     */       } 
/*     */     } 
/*  64 */     return plainInstance;
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   static ShaderInstance getTexturedOrNull() {
/*  72 */     if (!texturedLoadAttempted) {
/*  73 */       texturedLoadAttempted = true;
/*     */       try {
/*  75 */         texturedInstance = new ShaderInstance((ResourceProvider)Minecraft.m_91087_().m_91098_(), "ecstatic_lod_terrain_fog_tex", DefaultVertexFormat.f_85818_);
/*     */       }
/*  77 */       catch (IOException|RuntimeException e) {
/*  78 */         Constants.LOG.error("Ecstatic: failed to load the textured cheap-terrain fog shader; falling back to the plain (fog-less) position_color_tex shader", e);
/*     */         
/*  80 */         texturedInstance = null;
/*     */       } 
/*     */     } 
/*  83 */     return texturedInstance;
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
/*     */   static void setFogIntensity(float intensity) {
/*  96 */     setFogIntensity(plainInstance, intensity);
/*  97 */     setFogIntensity(texturedInstance, intensity);
/*     */   }
/*     */   
/*     */   private static void setFogIntensity(ShaderInstance instance, float intensity) {
/* 101 */     if (instance == null) {
/*     */       return;
/*     */     }
/* 104 */     Uniform fogIntensity = instance.m_173348_("FogIntensity");
/* 105 */     if (fogIntensity != null) {
/* 106 */       fogIntensity.m_5985_(intensity);
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
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   static void setSaturation(float saturation) {
/* 127 */     if (texturedInstance == null) {
/*     */       return;
/*     */     }
/* 130 */     Uniform uniform = texturedInstance.m_173348_("Saturation");
/* 131 */     if (uniform != null)
/* 132 */       uniform.m_5985_(saturation); 
/*     */   }
/*     */ }


/* Location:              C:\Users\Walker\Downloads\ecstatic-forge-1.20.1-1.3.0.jar!\com\angryalchemist\ecstatic\render\LodFogShader.class
 * Java compiler version: 17 (61.0)
 * JD-Core Version:       1.1.3
 */