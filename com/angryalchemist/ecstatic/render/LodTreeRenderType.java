/*    */ package com.angryalchemist.ecstatic.render;
/*    */ 
/*    */ import java.util.HashMap;
/*    */ import java.util.Map;
/*    */ import net.minecraft.client.renderer.RenderType;
/*    */ import net.minecraft.resources.ResourceLocation;
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ final class LodTreeRenderType
/*    */ {
/* 17 */   private static final Map<ResourceLocation, RenderType> CACHE = new HashMap<>();
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */   
/*    */   static RenderType forTexture(ResourceLocation texture) {
/* 24 */     return CACHE.computeIfAbsent(texture, id -> LodTerrainRenderType.createTextured("ecstatic_tree_" + id.m_135815_().replace('/', '_'), id));
/*    */   }
/*    */ }


/* Location:              C:\Users\Walker\Downloads\ecstatic-forge-1.20.1-1.3.0.jar!\com\angryalchemist\ecstatic\render\LodTreeRenderType.class
 * Java compiler version: 17 (61.0)
 * JD-Core Version:       1.1.3
 */