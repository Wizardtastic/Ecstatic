/*     */ package com.angryalchemist.ecstatic.render;
/*     */ 
/*     */ import com.mojang.blaze3d.systems.RenderSystem;
/*     */ import com.mojang.blaze3d.vertex.BufferBuilder;
/*     */ import com.mojang.blaze3d.vertex.DefaultVertexFormat;
/*     */ import com.mojang.blaze3d.vertex.Tesselator;
/*     */ import com.mojang.blaze3d.vertex.VertexBuffer;
/*     */ import com.mojang.blaze3d.vertex.VertexFormat;
/*     */ import com.mojang.math.Axis;
/*     */ import net.minecraft.client.Camera;
/*     */ import net.minecraft.client.multiplayer.ClientLevel;
/*     */ import net.minecraft.client.renderer.GameRenderer;
/*     */ import net.minecraft.client.renderer.ShaderInstance;
/*     */ import net.minecraft.world.phys.Vec3;
/*     */ import org.joml.Matrix4f;
/*     */ import org.joml.Quaternionfc;
/*     */ import org.joml.Vector3f;
/*     */ import org.joml.Vector3fc;
/*     */ import org.joml.Vector4f;
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
/*     */ public final class LodOceanPlane
/*     */ {
/*     */   private static final float SEA_LEVEL_Y = 62.8F;
/*     */   private static final int ANGULAR_SEGMENTS = 256;
/*     */   static final float OUTER_RADIUS_BLOCKS = 4096.0F;
/*     */   private static final int HIGHLIGHT_COLOR = 16777210;
/*     */   private static final float SPECULAR_SHININESS = 64.0F;
/*     */   private static final float SPECULAR_ALPHA_BOOST = 0.4F;
/*     */   private static final float SUN_HORIZON_FADE = 0.1F;
/*     */   private static final double REBUILD_DISTANCE_THRESHOLD_BLOCKS = 24.0D;
/*     */   private static final float REBUILD_SUN_ANGLE_THRESHOLD_DEGREES = 0.25F;
/*     */   private static VertexBuffer vertexBuffer;
/*     */   private static Vec3 lastRebuildCameraPos;
/*     */   private static float lastRebuildSunAngleDeg;
/*     */   private static int lastRebuildWaterColor;
/*     */   private static float lastRebuildWaterAlpha;
/*     */   private static boolean everRebuilt;
/*     */   
/*     */   public static void render(ClientLevel level, Camera camera, Matrix4f modelViewMatrix, Matrix4f projectionMatrix, float partialTick, float innerRadiusBlocks) {
/* 120 */     if (vertexBuffer == null) {
/* 121 */       vertexBuffer = new VertexBuffer(VertexBuffer.Usage.DYNAMIC);
/*     */     }
/*     */     
/* 124 */     Vec3 cameraPos = camera.m_90583_();
/* 125 */     float sunAngleDeg = level.m_46942_(partialTick) * 360.0F;
/* 126 */     int shallowColor = BiomeStyleConfig.get().waterColor();
/* 127 */     float shallowAlpha = BiomeStyleConfig.get().waterAlpha();
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */     
/* 133 */     ShaderInstance waterShader = LodWaterShader.getPlainOrNull();
/* 134 */     boolean gpuShaded = (waterShader != null);
/*     */     
/* 136 */     vertexBuffer.m_85921_();
/*     */     
/* 138 */     if (needsRebuild(cameraPos, sunAngleDeg, shallowColor, shallowAlpha, gpuShaded)) {
/*     */ 
/*     */ 
/*     */       
/* 142 */       Vector3f sunDirection = gpuShaded ? null : sunDirection(sunAngleDeg);
/*     */       
/* 144 */       BufferBuilder builder = Tesselator.m_85913_().m_85915_();
/* 145 */       builder.m_166779_(VertexFormat.Mode.TRIANGLES, DefaultVertexFormat.f_85815_);
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */       
/* 151 */       for (int i = 0; i < 256; i++) {
/* 152 */         double angle0 = i / 256.0D * 6.283185307179586D;
/* 153 */         double angle1 = (i + 1) / 256.0D * 6.283185307179586D;
/* 154 */         float cos0 = (float)Math.cos(angle0);
/* 155 */         float sin0 = (float)Math.sin(angle0);
/* 156 */         float cos1 = (float)Math.cos(angle1);
/* 157 */         float sin1 = (float)Math.sin(angle1);
/*     */         
/* 159 */         float innerX0 = (float)cameraPos.f_82479_ + innerRadiusBlocks * cos0;
/* 160 */         float innerZ0 = (float)cameraPos.f_82481_ + innerRadiusBlocks * sin0;
/* 161 */         float innerX1 = (float)cameraPos.f_82479_ + innerRadiusBlocks * cos1;
/* 162 */         float innerZ1 = (float)cameraPos.f_82481_ + innerRadiusBlocks * sin1;
/* 163 */         float outerX0 = (float)cameraPos.f_82479_ + 4096.0F * cos0;
/* 164 */         float outerZ0 = (float)cameraPos.f_82481_ + 4096.0F * sin0;
/* 165 */         float outerX1 = (float)cameraPos.f_82479_ + 4096.0F * cos1;
/* 166 */         float outerZ1 = (float)cameraPos.f_82481_ + 4096.0F * sin1;
/*     */         
/* 168 */         int innerColor0 = vertexColor(innerX0, innerZ0, cameraPos, sunDirection, shallowColor, shallowAlpha);
/* 169 */         int innerColor1 = vertexColor(innerX1, innerZ1, cameraPos, sunDirection, shallowColor, shallowAlpha);
/* 170 */         int outerColor0 = vertexColor(outerX0, outerZ0, cameraPos, sunDirection, shallowColor, shallowAlpha);
/* 171 */         int outerColor1 = vertexColor(outerX1, outerZ1, cameraPos, sunDirection, shallowColor, shallowAlpha);
/*     */         
/* 173 */         vertex(builder, innerX0, 62.8F, innerZ0, innerColor0);
/* 174 */         vertex(builder, outerX0, 62.8F, outerZ0, outerColor0);
/* 175 */         vertex(builder, outerX1, 62.8F, outerZ1, outerColor1);
/*     */         
/* 177 */         vertex(builder, innerX0, 62.8F, innerZ0, innerColor0);
/* 178 */         vertex(builder, outerX1, 62.8F, outerZ1, outerColor1);
/* 179 */         vertex(builder, innerX1, 62.8F, innerZ1, innerColor1);
/*     */       } 
/*     */       
/* 182 */       vertexBuffer.m_231221_(builder.m_231175_());
/*     */       
/* 184 */       lastRebuildCameraPos = cameraPos;
/* 185 */       lastRebuildSunAngleDeg = sunAngleDeg;
/* 186 */       lastRebuildWaterColor = shallowColor;
/* 187 */       lastRebuildWaterAlpha = shallowAlpha;
/* 188 */       everRebuilt = true;
/*     */     } 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */     
/* 196 */     LodOceanRenderType.OCEAN.m_110185_();
/* 197 */     if (gpuShaded) {
/* 198 */       Vector3f sunDirection = sunDirection(sunAngleDeg);
/* 199 */       LodWaterShader.setSunDirection(sunDirection.x, sunDirection.y, sunDirection.z);
/* 200 */       LodWaterShader.setGameTime(RenderSystem.getShaderGameTime());
/* 201 */       RenderSystem.setShader(() -> waterShader);
/*     */     } else {
/* 203 */       RenderSystem.setShader(GameRenderer::m_172811_);
/*     */     } 
/* 205 */     vertexBuffer.m_253207_(modelViewMatrix, projectionMatrix, RenderSystem.getShader());
/* 206 */     LodOceanRenderType.OCEAN.m_110188_();
/*     */     
/* 208 */     VertexBuffer.m_85931_();
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
/*     */   private static boolean needsRebuild(Vec3 cameraPos, float sunAngleDeg, int shallowColor, float shallowAlpha, boolean gpuShaded) {
/* 224 */     if (!everRebuilt) {
/* 225 */       return true;
/*     */     }
/* 227 */     if (lastRebuildCameraPos.m_82557_(cameraPos) > 576.0D)
/*     */     {
/* 229 */       return true;
/*     */     }
/* 231 */     if (!gpuShaded && Math.abs(sunAngleDeg - lastRebuildSunAngleDeg) > 0.25F) {
/* 232 */       return true;
/*     */     }
/* 234 */     return (shallowColor != lastRebuildWaterColor || shallowAlpha != lastRebuildWaterAlpha);
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
/*     */   static Vector3f sunDirection(float sunAngleDeg) {
/* 251 */     Matrix4f sunRot = (new Matrix4f()).rotate((Quaternionfc)Axis.f_252436_.m_252977_(-90.0F)).rotate((Quaternionfc)Axis.f_252529_.m_252977_(sunAngleDeg));
/* 252 */     Vector4f sunDir4 = sunRot.transform(new Vector4f(0.0F, 1.0F, 0.0F, 0.0F));
/* 253 */     return (new Vector3f(sunDir4.x, sunDir4.y, sunDir4.z)).normalize();
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   private static int vertexColor(float x, float z, Vec3 cameraPos, Vector3f sunDirection, int shallowColor, float shallowAlpha) {
/* 264 */     if (sunDirection == null || sunDirection.y <= 0.0F) {
/* 265 */       return withAlpha(shallowColor, shallowAlpha);
/*     */     }
/*     */ 
/*     */     
/* 269 */     Vector3f viewDir = (new Vector3f((float)(x - cameraPos.f_82479_), (float)(62.79999923706055D - cameraPos.f_82480_), (float)(z - cameraPos.f_82481_))).normalize();
/* 270 */     Vector3f reflectDir = reflect(viewDir, new Vector3f(0.0F, 1.0F, 0.0F));
/* 271 */     float alignment = Math.max(0.0F, reflectDir.dot((Vector3fc)sunDirection));
/*     */     
/* 273 */     float specular = (float)Math.pow(alignment, 64.0D) * smoothstep(0.0F, 0.1F, sunDirection.y);
/*     */     
/* 275 */     int color = blend(shallowColor, 16777210, specular);
/* 276 */     float alpha = clamp01(shallowAlpha + specular * 0.4F);
/* 277 */     return withAlpha(color, alpha);
/*     */   }
/*     */   
/*     */   private static Vector3f reflect(Vector3f incident, Vector3f normal) {
/* 281 */     float dot = incident.dot((Vector3fc)normal);
/* 282 */     return new Vector3f(incident.x - 2.0F * dot * normal.x, incident.y - 2.0F * dot * normal.y, incident.z - 2.0F * dot * normal.z);
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   private static void vertex(BufferBuilder builder, float x, float y, float z, int colorRgba) {
/* 289 */     int r = colorRgba >> 24 & 0xFF;
/* 290 */     int g = colorRgba >> 16 & 0xFF;
/* 291 */     int b = colorRgba >> 8 & 0xFF;
/* 292 */     int a = colorRgba & 0xFF;
/* 293 */     builder.m_5483_(x, y, z).m_6122_(r, g, b, a).m_5752_();
/*     */   }
/*     */   
/*     */   private static int withAlpha(int colorRgb, float alpha) {
/* 297 */     return colorRgb << 8 | clampByte(Math.round(alpha * 255.0F));
/*     */   }
/*     */   
/*     */   private static int blend(int colorA, int colorB, float t) {
/* 301 */     t = clamp01(t);
/* 302 */     int ar = colorA >> 16 & 0xFF;
/* 303 */     int ag = colorA >> 8 & 0xFF;
/* 304 */     int ab = colorA & 0xFF;
/* 305 */     int br = colorB >> 16 & 0xFF;
/* 306 */     int bg = colorB >> 8 & 0xFF;
/* 307 */     int bb = colorB & 0xFF;
/* 308 */     int r = clampByte(Math.round(ar + (br - ar) * t));
/* 309 */     int g = clampByte(Math.round(ag + (bg - ag) * t));
/* 310 */     int b = clampByte(Math.round(ab + (bb - ab) * t));
/* 311 */     return r << 16 | g << 8 | b;
/*     */   }
/*     */   
/*     */   private static float smoothstep(float edge0, float edge1, float x) {
/* 315 */     float t = clamp01((x - edge0) / (edge1 - edge0));
/* 316 */     return t * t * (3.0F - 2.0F * t);
/*     */   }
/*     */   
/*     */   private static float clamp01(float v) {
/* 320 */     return Math.max(0.0F, Math.min(1.0F, v));
/*     */   }
/*     */   
/*     */   private static int clampByte(int v) {
/* 324 */     return Math.max(0, Math.min(255, v));
/*     */   }
/*     */ }


/* Location:              C:\Users\Walker\Downloads\ecstatic-forge-1.20.1-1.3.0.jar!\com\angryalchemist\ecstatic\render\LodOceanPlane.class
 * Java compiler version: 17 (61.0)
 * JD-Core Version:       1.1.3
 */