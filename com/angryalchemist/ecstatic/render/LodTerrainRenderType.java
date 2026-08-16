/*     */ package com.angryalchemist.ecstatic.render;
/*     */ 
/*     */ import com.google.common.collect.ImmutableMap;
/*     */ import com.mojang.blaze3d.platform.NativeImage;
/*     */ import com.mojang.blaze3d.vertex.DefaultVertexFormat;
/*     */ import com.mojang.blaze3d.vertex.VertexFormat;
/*     */ import java.lang.reflect.Constructor;
/*     */ import java.lang.reflect.Method;
/*     */ import java.lang.reflect.Modifier;
/*     */ import net.minecraft.client.Minecraft;
/*     */ import net.minecraft.client.renderer.RenderStateShard;
/*     */ import net.minecraft.client.renderer.RenderType;
/*     */ import net.minecraft.client.renderer.texture.AbstractTexture;
/*     */ import net.minecraft.client.renderer.texture.DynamicTexture;
/*     */ import net.minecraft.resources.ResourceLocation;
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
/*     */ public final class LodTerrainRenderType
/*     */   extends RenderType
/*     */ {
/*  36 */   public static final RenderType TERRAIN = create("ecstatic_terrain", DefaultVertexFormat.f_85815_, f_173104_, (RenderStateShard.TextureStateShard)null);
/*     */   
/*  38 */   public static final RenderType TERRAIN_TEXTURED = create("ecstatic_terrain_textured", DefaultVertexFormat.f_85818_, f_173101_, f_110145_);
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
/*  79 */   static final VertexFormat BLOCK_SAFE = new VertexFormat(
/*  80 */       ImmutableMap.builder()
/*  81 */       .put("Position", DefaultVertexFormat.f_85804_)
/*  82 */       .put("Color", DefaultVertexFormat.f_85805_)
/*  83 */       .put("UV0", DefaultVertexFormat.f_85806_)
/*  84 */       .put("UV2", DefaultVertexFormat.f_85808_)
/*  85 */       .put("Normal", DefaultVertexFormat.f_85809_)
/*  86 */       .put("Padding", DefaultVertexFormat.f_85810_)
/*  87 */       .build());
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
/* 112 */   public static final RenderType TERRAIN_LIT = create("ecstatic_terrain_lit", BLOCK_SAFE, f_173105_, 
/* 113 */       buildTextureState(flatWhiteTexture()), true);
/* 114 */   public static final RenderType TERRAIN_LIT_TEXTURED = create("ecstatic_terrain_lit_textured", BLOCK_SAFE, f_173105_, f_110145_, true);
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
/* 147 */   public static final RenderType TERRAIN_OPAQUE = create("ecstatic_terrain_opaque", DefaultVertexFormat.f_85815_, f_173104_, (RenderStateShard.TextureStateShard)null, false, true);
/*     */   
/* 149 */   public static final RenderType TERRAIN_TEXTURED_OPAQUE = create("ecstatic_terrain_textured_opaque", DefaultVertexFormat.f_85818_, f_173101_, f_110145_, false, true);
/*     */   
/* 151 */   public static final RenderType TERRAIN_LIT_OPAQUE = create("ecstatic_terrain_lit_opaque", BLOCK_SAFE, f_173105_, 
/* 152 */       buildTextureState(flatWhiteTexture()), true, true);
/* 153 */   public static final RenderType TERRAIN_LIT_TEXTURED_OPAQUE = create("ecstatic_terrain_lit_textured_opaque", BLOCK_SAFE, f_173105_, f_110145_, true, true);
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
/* 178 */   public static final RenderType TERRAIN_OPAQUE_NOCULL = create("ecstatic_terrain_opaque_nocull", DefaultVertexFormat.f_85815_, f_173104_, (RenderStateShard.TextureStateShard)null, false, true, false);
/*     */   
/* 180 */   public static final RenderType TERRAIN_TEXTURED_OPAQUE_NOCULL = create("ecstatic_terrain_textured_opaque_nocull", DefaultVertexFormat.f_85818_, f_173101_, f_110145_, false, true, false);
/*     */   
/* 182 */   public static final RenderType TERRAIN_LIT_OPAQUE_NOCULL = create("ecstatic_terrain_lit_opaque_nocull", BLOCK_SAFE, f_173105_, 
/* 183 */       buildTextureState(flatWhiteTexture()), true, true, false);
/* 184 */   public static final RenderType TERRAIN_LIT_TEXTURED_OPAQUE_NOCULL = create("ecstatic_terrain_lit_textured_opaque_nocull", BLOCK_SAFE, f_173105_, f_110145_, true, true, false);
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
/* 203 */   public static final RenderType TERRAIN_PARALLAX = create("ecstatic_terrain_parallax", BLOCK_SAFE, new RenderStateShard.ShaderStateShard(LodParallaxShader::getOrNull), 
/*     */       
/* 205 */       buildTextureState(flatWhiteTexture()), true);
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
/* 220 */   public static final RenderType TERRAIN_FOG = create("ecstatic_terrain_fog", DefaultVertexFormat.f_85815_, new RenderStateShard.ShaderStateShard(LodFogShader::getPlainOrNull), (RenderStateShard.TextureStateShard)null);
/*     */   
/* 222 */   public static final RenderType TERRAIN_FOG_TEXTURED = create("ecstatic_terrain_fog_textured", DefaultVertexFormat.f_85818_, new RenderStateShard.ShaderStateShard(LodFogShader::getTexturedOrNull), f_110145_);
/*     */ 
/*     */   
/* 225 */   public static final RenderType TERRAIN_FOG_OPAQUE = create("ecstatic_terrain_fog_opaque", DefaultVertexFormat.f_85815_, new RenderStateShard.ShaderStateShard(LodFogShader::getPlainOrNull), (RenderStateShard.TextureStateShard)null, false, true);
/*     */ 
/*     */   
/* 228 */   public static final RenderType TERRAIN_FOG_TEXTURED_OPAQUE = create("ecstatic_terrain_fog_textured_opaque", DefaultVertexFormat.f_85818_, new RenderStateShard.ShaderStateShard(LodFogShader::getTexturedOrNull), f_110145_, false, true);
/*     */ 
/*     */   
/* 231 */   public static final RenderType TERRAIN_FOG_OPAQUE_NOCULL = create("ecstatic_terrain_fog_opaque_nocull", DefaultVertexFormat.f_85815_, new RenderStateShard.ShaderStateShard(LodFogShader::getPlainOrNull), (RenderStateShard.TextureStateShard)null, false, true, false);
/*     */ 
/*     */   
/* 234 */   public static final RenderType TERRAIN_FOG_TEXTURED_OPAQUE_NOCULL = create("ecstatic_terrain_fog_textured_opaque_nocull", DefaultVertexFormat.f_85818_, new RenderStateShard.ShaderStateShard(LodFogShader::getTexturedOrNull), f_110145_, false, true, false);
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
/* 263 */   public static final RenderType WATER = create("ecstatic_water", DefaultVertexFormat.f_85815_, f_173104_, (RenderStateShard.TextureStateShard)null, false, false, false);
/*     */   
/* 265 */   public static final RenderType WATER_LIT = create("ecstatic_water_lit", BLOCK_SAFE, f_173105_, 
/* 266 */       buildTextureState(flatWhiteTexture()), true, false, false);
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
/* 278 */   public static final RenderType WATER_TEXTURED = create("ecstatic_water_textured", DefaultVertexFormat.f_85818_, f_173101_, f_110145_, false, false, false);
/*     */   
/* 280 */   public static final RenderType WATER_LIT_TEXTURED = create("ecstatic_water_lit_textured", BLOCK_SAFE, f_173105_, f_110145_, true, false, false);
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   private static ResourceLocation flatWhiteTextureId;
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   private static ResourceLocation flatWhiteTexture() {
/* 294 */     if (flatWhiteTextureId == null) {
/* 295 */       NativeImage image = new NativeImage(1, 1, false);
/* 296 */       image.m_84988_(0, 0, -1);
/* 297 */       DynamicTexture texture = new DynamicTexture(image);
/* 298 */       ResourceLocation id = new ResourceLocation("ecstatic", "terrain_lit_flat");
/* 299 */       Minecraft.m_91087_().m_91097_().m_118495_(id, (AbstractTexture)texture);
/* 300 */       flatWhiteTextureId = id;
/*     */     } 
/* 302 */     return flatWhiteTextureId;
/*     */   }
/*     */ 
/*     */   
/*     */   private LodTerrainRenderType(String name, VertexFormat format, VertexFormat.Mode mode, int bufferSize, boolean affectsCrumbling, boolean sortOnUpload, Runnable setupState, Runnable clearState) {
/* 307 */     super(name, format, mode, bufferSize, affectsCrumbling, sortOnUpload, setupState, clearState);
/*     */   }
/*     */ 
/*     */ 
/*     */   
/*     */   static RenderType create(String name, VertexFormat format, RenderStateShard.ShaderStateShard shaderState, RenderStateShard.TextureStateShard textureState) {
/* 313 */     return create(name, format, shaderState, textureState, false);
/*     */   }
/*     */ 
/*     */   
/*     */   private static RenderType create(String name, VertexFormat format, RenderStateShard.ShaderStateShard shaderState, RenderStateShard.TextureStateShard textureState, boolean lit) {
/* 318 */     return create(name, format, shaderState, textureState, lit, false);
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
/*     */   private static RenderType create(String name, VertexFormat format, RenderStateShard.ShaderStateShard shaderState, RenderStateShard.TextureStateShard textureState, boolean lit, boolean opaqueCulled) {
/* 332 */     return create(name, format, shaderState, textureState, lit, opaqueCulled, opaqueCulled);
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
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   private static RenderType create(String name, VertexFormat format, RenderStateShard.ShaderStateShard shaderState, RenderStateShard.TextureStateShard textureState, boolean lit, boolean noTransparency, boolean cull) {
/* 361 */     RenderType.CompositeState.CompositeStateBuilder builder = RenderType.CompositeState.m_110628_().m_173292_(shaderState).m_110685_(noTransparency ? f_110134_ : f_110139_).m_110663_(f_110113_).m_110661_(cull ? f_110158_ : f_110110_).m_110687_(f_110114_);
/* 362 */     if (textureState != null) {
/* 363 */       builder.m_173290_((RenderStateShard.EmptyTextureStateShard)textureState);
/*     */     }
/* 365 */     if (lit)
/*     */     {
/*     */ 
/*     */ 
/*     */       
/* 370 */       builder.m_110671_(f_110152_);
/*     */     }
/* 372 */     RenderType.CompositeState state = builder.m_110691_(false);
/*     */     try {
/* 374 */       Method createMethod = findCreateMethod();
/* 375 */       createMethod.setAccessible(true);
/* 376 */       return (RenderType)createMethod.invoke(null, new Object[] { name, format, VertexFormat.Mode.TRIANGLES, Integer.valueOf(256), state });
/* 377 */     } catch (ReflectiveOperationException e) {
/* 378 */       throw new RuntimeException("Failed to create " + name + " RenderType", e);
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
/*     */   static RenderType createTextured(String name, ResourceLocation texture) {
/* 394 */     return create(name, DefaultVertexFormat.f_85818_, f_173101_, 
/* 395 */         buildTextureState(texture));
/*     */   }
/*     */   
/*     */   private static RenderStateShard.TextureStateShard buildTextureState(ResourceLocation texture) {
/*     */     try {
/* 400 */       Constructor<RenderStateShard.TextureStateShard> constructor = RenderStateShard.TextureStateShard.class.getDeclaredConstructor(new Class[] { ResourceLocation.class, boolean.class, boolean.class });
/*     */       
/* 402 */       constructor.setAccessible(true);
/* 403 */       return constructor.newInstance(new Object[] { texture, Boolean.valueOf(false), Boolean.valueOf(false) });
/* 404 */     } catch (ReflectiveOperationException e) {
/* 405 */       throw new RuntimeException("Failed to build tree texture state for " + String.valueOf(texture), e);
/*     */     } 
/*     */   }
/*     */   
/*     */   private static Method findCreateMethod() {
/* 410 */     for (Method method : RenderType.class.getDeclaredMethods()) {
/* 411 */       if (Modifier.isStatic(method.getModifiers())) {
/*     */ 
/*     */         
/* 414 */         Class<?>[] params = method.getParameterTypes();
/* 415 */         if (params.length == 5 && params[0] == String.class && params[1] == VertexFormat.class && params[2] == VertexFormat.Mode.class && params[3] == int.class && params[4] == RenderType.CompositeState.class)
/*     */         {
/*     */ 
/*     */ 
/*     */ 
/*     */           
/* 421 */           return method; } 
/*     */       } 
/*     */     } 
/* 424 */     throw new IllegalStateException("Could not find RenderType's 5-arg create(...) overload by signature");
/*     */   }
/*     */ }


/* Location:              C:\Users\Walker\Downloads\ecstatic-forge-1.20.1-1.3.0.jar!\com\angryalchemist\ecstatic\render\LodTerrainRenderType.class
 * Java compiler version: 17 (61.0)
 * JD-Core Version:       1.1.3
 */