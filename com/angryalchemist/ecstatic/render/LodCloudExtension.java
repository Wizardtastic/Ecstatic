/*     */ package com.angryalchemist.ecstatic.render;
/*     */ 
/*     */ import com.mojang.blaze3d.platform.GlStateManager;
/*     */ import com.mojang.blaze3d.systems.RenderSystem;
/*     */ import com.mojang.blaze3d.vertex.BufferBuilder;
/*     */ import com.mojang.blaze3d.vertex.DefaultVertexFormat;
/*     */ import com.mojang.blaze3d.vertex.Tesselator;
/*     */ import com.mojang.blaze3d.vertex.VertexBuffer;
/*     */ import com.mojang.blaze3d.vertex.VertexFormat;
/*     */ import net.minecraft.client.Camera;
/*     */ import net.minecraft.client.CloudStatus;
/*     */ import net.minecraft.client.Minecraft;
/*     */ import net.minecraft.client.multiplayer.ClientLevel;
/*     */ import net.minecraft.client.renderer.GameRenderer;
/*     */ import net.minecraft.client.renderer.ShaderInstance;
/*     */ import net.minecraft.resources.ResourceLocation;
/*     */ import net.minecraft.world.phys.Vec3;
/*     */ import org.joml.Matrix4f;
/*     */ import org.joml.Matrix4fc;
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
/*     */ final class LodCloudExtension
/*     */ {
/*  53 */   private static final ResourceLocation CLOUDS_LOCATION = new ResourceLocation("textures/environment/clouds.png");
/*     */ 
/*     */ 
/*     */   
/*     */   private static final float SCALE_BLOCKS_PER_UNIT = 12.0F;
/*     */ 
/*     */ 
/*     */   
/*     */   private static final float TEXEL = 0.00390625F;
/*     */ 
/*     */ 
/*     */   
/*     */   private static final int TILE_UNITS = 32;
/*     */ 
/*     */   
/*     */   private static final int WRAP_UNITS = 2048;
/*     */ 
/*     */   
/*     */   private static final float ALPHA = 0.8F;
/*     */ 
/*     */   
/*     */   private static final float WHITE = 1.0F;
/*     */ 
/*     */   
/*     */   private static VertexBuffer buffer;
/*     */ 
/*     */   
/*  80 */   private static int lastGridX = Integer.MIN_VALUE;
/*  81 */   private static int lastGridZ = Integer.MIN_VALUE;
/*  82 */   private static int lastHalfExtentUnits = -1;
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
/*     */   static void render(Matrix4f rotationOnlyMatrix, Matrix4f projectionMatrix, ClientLevel level, Camera camera, float partialTick, int outerRadiusBlocks) {
/*  98 */     if ((Minecraft.m_91087_()).f_91066_.m_92174_() == CloudStatus.OFF) {
/*     */       return;
/*     */     }
/* 101 */     float cloudHeight = level.m_104583_().m_108871_();
/* 102 */     if (Float.isNaN(cloudHeight)) {
/*     */       return;
/*     */     }
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */     
/* 113 */     Vec3 cameraPos = camera.m_90583_();
/* 114 */     double scroll = (level.m_46467_() + partialTick) * 0.03D;
/* 115 */     double d2 = (cameraPos.f_82479_ + scroll) / 12.0D;
/* 116 */     double d3 = cloudHeight - cameraPos.f_82480_ + 0.33D;
/* 117 */     double d4 = cameraPos.f_82481_ / 12.0D + 0.33D;
/* 118 */     d2 -= Math.floor(d2 / 2048.0D) * 2048.0D;
/* 119 */     d4 -= Math.floor(d4 / 2048.0D) * 2048.0D;
/* 120 */     float fracX = (float)(d2 - Math.floor(d2));
/* 121 */     float fracZ = (float)(d4 - Math.floor(d4));
/* 122 */     int gridX = (int)Math.floor(d2);
/* 123 */     int gridZ = (int)Math.floor(d4);
/*     */ 
/*     */ 
/*     */     
/* 127 */     int requestedHalfUnits = (int)Math.ceil((outerRadiusBlocks / 12.0F));
/* 128 */     int halfExtentUnits = (ceilDiv(requestedHalfUnits, 32) + 1) * 32;
/*     */     
/* 130 */     if (buffer == null || gridX != lastGridX || gridZ != lastGridZ || halfExtentUnits != lastHalfExtentUnits) {
/* 131 */       rebuild(gridX, gridZ, halfExtentUnits);
/* 132 */       lastGridX = gridX;
/* 133 */       lastGridZ = gridZ;
/* 134 */       lastHalfExtentUnits = halfExtentUnits;
/*     */     } 
/* 136 */     if (buffer == null) {
/*     */       return;
/*     */     }
/*     */     
/* 140 */     Vec3 cloudColor = level.m_104808_(partialTick);
/*     */     
/* 142 */     RenderSystem.disableCull();
/* 143 */     RenderSystem.enableBlend();
/* 144 */     RenderSystem.enableDepthTest();
/* 145 */     RenderSystem.blendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
/*     */     
/* 147 */     RenderSystem.depthMask(true);
/* 148 */     RenderSystem.setShader(GameRenderer::m_172838_);
/* 149 */     RenderSystem.setShaderTexture(0, CLOUDS_LOCATION);
/* 150 */     RenderSystem.setShaderColor((float)cloudColor.f_82479_, (float)cloudColor.f_82480_, (float)cloudColor.f_82481_, 1.0F);
/*     */     
/* 152 */     Matrix4f transform = new Matrix4f((Matrix4fc)rotationOnlyMatrix);
/* 153 */     transform.scale(12.0F, 1.0F, 12.0F);
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */     
/* 160 */     transform.translate(-fracX, (float)d3, -fracZ);
/*     */     
/* 162 */     buffer.m_85921_();
/* 163 */     ShaderInstance shader = RenderSystem.getShader();
/* 164 */     buffer.m_253207_(transform, projectionMatrix, shader);
/* 165 */     VertexBuffer.m_85931_();
/*     */     
/* 167 */     RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
/* 168 */     RenderSystem.enableCull();
/* 169 */     RenderSystem.disableBlend();
/* 170 */     RenderSystem.defaultBlendFunc();
/*     */   }
/*     */   
/*     */   private static int ceilDiv(int numerator, int denominator) {
/* 174 */     return (numerator + denominator - 1) / denominator;
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
/*     */   private static void rebuild(int gridX, int gridZ, int halfExtentUnits) {
/* 187 */     if (buffer != null) {
/* 188 */       buffer.close();
/*     */     }
/* 190 */     buffer = new VertexBuffer(VertexBuffer.Usage.STATIC);
/* 191 */     BufferBuilder builder = Tesselator.m_85913_().m_85915_();
/* 192 */     builder.m_166779_(VertexFormat.Mode.QUADS, DefaultVertexFormat.f_85822_);
/* 193 */     float uOffset = gridX * 0.00390625F;
/* 194 */     float vOffset = gridZ * 0.00390625F;
/* 195 */     for (int tileX = -halfExtentUnits; tileX < halfExtentUnits; tileX += 32) {
/* 196 */       for (int tileZ = -halfExtentUnits; tileZ < halfExtentUnits; tileZ += 32) {
/* 197 */         boolean coveredByVanilla = (tileX >= -32 && tileX < 32 && tileZ >= -32 && tileZ < 32);
/*     */         
/* 199 */         if (!coveredByVanilla) {
/* 200 */           emitTile(builder, tileX, tileZ, uOffset, vOffset);
/*     */         }
/*     */       } 
/*     */     } 
/* 204 */     buffer.m_85921_();
/* 205 */     buffer.m_231221_(builder.m_231175_());
/* 206 */     VertexBuffer.m_85931_();
/*     */   }
/*     */ 
/*     */   
/*     */   private static void emitTile(BufferBuilder builder, int x, int z, float uOffset, float vOffset) {
/* 211 */     float x0 = x;
/* 212 */     float x1 = (x + 32);
/* 213 */     float z0 = z;
/* 214 */     float z1 = (z + 32);
/* 215 */     builder.m_5483_(x0, 0.0D, z1).m_7421_(x0 * 0.00390625F + uOffset, z1 * 0.00390625F + vOffset).m_85950_(1.0F, 1.0F, 1.0F, 0.8F).m_5601_(0.0F, -1.0F, 0.0F).m_5752_();
/* 216 */     builder.m_5483_(x1, 0.0D, z1).m_7421_(x1 * 0.00390625F + uOffset, z1 * 0.00390625F + vOffset).m_85950_(1.0F, 1.0F, 1.0F, 0.8F).m_5601_(0.0F, -1.0F, 0.0F).m_5752_();
/* 217 */     builder.m_5483_(x1, 0.0D, z0).m_7421_(x1 * 0.00390625F + uOffset, z0 * 0.00390625F + vOffset).m_85950_(1.0F, 1.0F, 1.0F, 0.8F).m_5601_(0.0F, -1.0F, 0.0F).m_5752_();
/* 218 */     builder.m_5483_(x0, 0.0D, z0).m_7421_(x0 * 0.00390625F + uOffset, z0 * 0.00390625F + vOffset).m_85950_(1.0F, 1.0F, 1.0F, 0.8F).m_5601_(0.0F, -1.0F, 0.0F).m_5752_();
/*     */   }
/*     */ }


/* Location:              C:\Users\Walker\Downloads\ecstatic-forge-1.20.1-1.3.0.jar!\com\angryalchemist\ecstatic\render\LodCloudExtension.class
 * Java compiler version: 17 (61.0)
 * JD-Core Version:       1.1.3
 */