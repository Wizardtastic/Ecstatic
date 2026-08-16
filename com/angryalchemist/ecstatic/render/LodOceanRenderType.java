/*    */ package com.angryalchemist.ecstatic.render;
/*    */ 
/*    */ import com.mojang.blaze3d.vertex.DefaultVertexFormat;
/*    */ import com.mojang.blaze3d.vertex.VertexFormat;
/*    */ import java.lang.reflect.Method;
/*    */ import java.lang.reflect.Modifier;
/*    */ import net.minecraft.client.renderer.RenderType;
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
/*    */ public final class LodOceanRenderType
/*    */   extends RenderType
/*    */ {
/* 32 */   public static final RenderType OCEAN = create();
/*    */ 
/*    */   
/*    */   private LodOceanRenderType(String name, VertexFormat format, VertexFormat.Mode mode, int bufferSize, boolean affectsCrumbling, boolean sortOnUpload, Runnable setupState, Runnable clearState) {
/* 36 */     super(name, format, mode, bufferSize, affectsCrumbling, sortOnUpload, setupState, clearState);
/*    */   }
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */   
/*    */   private static RenderType create() {
/* 46 */     RenderType.CompositeState state = RenderType.CompositeState.m_110628_().m_173292_(f_173104_).m_110685_(f_110139_).m_110663_(f_110113_).m_110661_(f_110110_).m_110687_(f_110115_).m_110691_(false);
/*    */     try {
/* 48 */       Method createMethod = findCreateMethod();
/* 49 */       createMethod.setAccessible(true);
/* 50 */       return (RenderType)createMethod.invoke(null, new Object[] { "ecstatic_ocean", DefaultVertexFormat.f_85815_, VertexFormat.Mode.TRIANGLES, 
/* 51 */             Integer.valueOf(256), state });
/* 52 */     } catch (ReflectiveOperationException e) {
/* 53 */       throw new RuntimeException("Failed to create ecstatic_ocean RenderType", e);
/*    */     } 
/*    */   }
/*    */   
/*    */   private static Method findCreateMethod() {
/* 58 */     for (Method method : RenderType.class.getDeclaredMethods()) {
/* 59 */       if (Modifier.isStatic(method.getModifiers())) {
/*    */ 
/*    */         
/* 62 */         Class<?>[] params = method.getParameterTypes();
/* 63 */         if (params.length == 5 && params[0] == String.class && params[1] == VertexFormat.class && params[2] == VertexFormat.Mode.class && params[3] == int.class && params[4] == RenderType.CompositeState.class)
/*    */         {
/*    */ 
/*    */ 
/*    */ 
/*    */           
/* 69 */           return method; } 
/*    */       } 
/*    */     } 
/* 72 */     throw new IllegalStateException("Could not find RenderType's 5-arg create(...) overload by signature");
/*    */   }
/*    */ }


/* Location:              C:\Users\Walker\Downloads\ecstatic-forge-1.20.1-1.3.0.jar!\com\angryalchemist\ecstatic\render\LodOceanRenderType.class
 * Java compiler version: 17 (61.0)
 * JD-Core Version:       1.1.3
 */