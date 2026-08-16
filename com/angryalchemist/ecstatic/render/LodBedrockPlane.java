/*     */ package com.angryalchemist.ecstatic.render;
/*     */ 
/*     */ import com.mojang.blaze3d.systems.RenderSystem;
/*     */ import com.mojang.blaze3d.vertex.BufferBuilder;
/*     */ import com.mojang.blaze3d.vertex.DefaultVertexFormat;
/*     */ import com.mojang.blaze3d.vertex.Tesselator;
/*     */ import com.mojang.blaze3d.vertex.VertexBuffer;
/*     */ import com.mojang.blaze3d.vertex.VertexFormat;
/*     */ import net.minecraft.client.multiplayer.ClientLevel;
/*     */ import net.minecraft.client.renderer.GameRenderer;
/*     */ import net.minecraft.core.Direction;
/*     */ import net.minecraft.world.level.block.Blocks;
/*     */ import net.minecraft.world.phys.Vec3;
/*     */ import org.joml.Matrix4f;
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
/*     */ final class LodBedrockPlane
/*     */ {
/*     */   private static final float RADIUS_BLOCKS = 2048.0F;
/*     */   private static final float TILE_BLOCKS = 16.0F;
/*     */   private static final float REBUILD_MARGIN_BLOCKS = 512.0F;
/*     */   private static VertexBuffer buffer;
/*  62 */   private static double builtCenterX = Double.NaN;
/*  63 */   private static double builtCenterZ = Double.NaN;
/*     */ 
/*     */   
/*     */   private static SurfaceMaterial.Sprite cachedSprite;
/*     */ 
/*     */   
/*     */   static void render(Matrix4f modelViewMatrix, Matrix4f projectionMatrix, ClientLevel level, Vec3 cameraPos) {
/*  70 */     float y = level.m_141937_() - 1.0F;
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
/*  85 */     if (cameraPos.f_82480_ >= y) {
/*     */       return;
/*     */     }
/*     */ 
/*     */     
/*  90 */     boolean needsRebuild = (buffer == null || Double.isNaN(builtCenterX) || Math.abs(cameraPos.f_82479_ - builtCenterX) > 1536.0D || Math.abs(cameraPos.f_82481_ - builtCenterZ) > 1536.0D);
/*  91 */     if (needsRebuild) {
/*  92 */       rebuild(cameraPos.f_82479_, cameraPos.f_82481_, y);
/*     */     }
/*  94 */     if (buffer == null) {
/*     */       return;
/*     */     }
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */     
/* 103 */     LodTerrainRenderType.TERRAIN_TEXTURED_OPAQUE_NOCULL.m_110185_();
/* 104 */     RenderSystem.setShader(GameRenderer::m_172814_);
/* 105 */     buffer.m_85921_();
/* 106 */     buffer.m_253207_(modelViewMatrix, projectionMatrix, RenderSystem.getShader());
/* 107 */     VertexBuffer.m_85931_();
/* 108 */     LodTerrainRenderType.TERRAIN_TEXTURED_OPAQUE_NOCULL.m_110188_();
/*     */   }
/*     */   
/*     */   private static void rebuild(double centerX, double centerZ, float y) {
/* 112 */     if (buffer != null) {
/* 113 */       buffer.close();
/*     */     }
/* 115 */     if (cachedSprite == null) {
/* 116 */       cachedSprite = SurfaceMaterial.resolveSprite(Blocks.f_50752_.m_49966_(), Direction.UP);
/*     */     }
/* 118 */     builtCenterX = centerX;
/* 119 */     builtCenterZ = centerZ;
/*     */     
/* 121 */     buffer = new VertexBuffer(VertexBuffer.Usage.STATIC);
/* 122 */     BufferBuilder builder = Tesselator.m_85913_().m_85915_();
/* 123 */     builder.m_166779_(VertexFormat.Mode.QUADS, DefaultVertexFormat.f_85818_);
/*     */     
/* 125 */     int tilesPerAxis = (int)Math.ceil(128.0D);
/* 126 */     float originX = (float)(Math.floor(centerX / 16.0D) * 16.0D) - tilesPerAxis * 16.0F;
/* 127 */     float originZ = (float)(Math.floor(centerZ / 16.0D) * 16.0D) - tilesPerAxis * 16.0F;
/* 128 */     int tileCountPerAxis = tilesPerAxis * 2;
/*     */     
/* 130 */     for (int i = 0; i < tileCountPerAxis; i++) {
/* 131 */       float x0 = originX + i * 16.0F;
/* 132 */       float x1 = x0 + 16.0F;
/* 133 */       for (int j = 0; j < tileCountPerAxis; j++) {
/* 134 */         float z0 = originZ + j * 16.0F;
/* 135 */         float z1 = z0 + 16.0F;
/* 136 */         emitTile(builder, x0, y, z0, x1, z1);
/*     */       } 
/*     */     } 
/*     */     
/* 140 */     buffer.m_85921_();
/* 141 */     buffer.m_231221_(builder.m_231175_());
/* 142 */     VertexBuffer.m_85931_();
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
/*     */   private static void emitTile(BufferBuilder builder, float x0, float y, float z0, float x1, float z1) {
/* 158 */     SurfaceMaterial.Sprite sprite = cachedSprite;
/* 159 */     builder.m_5483_(x0, y, z1).m_6122_(255, 255, 255, 255).m_7421_(sprite.u0(), sprite.v1()).m_5752_();
/* 160 */     builder.m_5483_(x1, y, z1).m_6122_(255, 255, 255, 255).m_7421_(sprite.u1(), sprite.v1()).m_5752_();
/* 161 */     builder.m_5483_(x1, y, z0).m_6122_(255, 255, 255, 255).m_7421_(sprite.u1(), sprite.v0()).m_5752_();
/* 162 */     builder.m_5483_(x0, y, z0).m_6122_(255, 255, 255, 255).m_7421_(sprite.u0(), sprite.v0()).m_5752_();
/*     */   }
/*     */ }


/* Location:              C:\Users\Walker\Downloads\ecstatic-forge-1.20.1-1.3.0.jar!\com\angryalchemist\ecstatic\render\LodBedrockPlane.class
 * Java compiler version: 17 (61.0)
 * JD-Core Version:       1.1.3
 */