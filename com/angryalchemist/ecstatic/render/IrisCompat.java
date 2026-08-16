/*     */ package com.angryalchemist.ecstatic.render;
/*     */ 
/*     */ import com.angryalchemist.ecstatic.platform.Services;
/*     */ import java.lang.reflect.Method;
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ final class IrisCompat
/*     */ {
/*  64 */   private static final boolean PRESENT = (Services.PLATFORM
/*  65 */     .isModLoaded("iris") || Services.PLATFORM.isModLoaded("oculus"));
/*     */   
/*     */   private static final Object NONE_PHASE;
/*     */   private static final Object TERRAIN_SOLID_PHASE;
/*     */   private static final Object TERRAIN_TRANSLUCENT_PHASE;
/*     */   private static final Method GET_PIPELINE_MANAGER;
/*     */   private static final Method GET_PIPELINE_NULLABLE;
/*     */   private static final Method GET_PHASE;
/*     */   private static final Method SET_PHASE;
/*     */   private static final Method IS_SHADER_PACK_IN_USE;
/*     */   private static final Object IRIS_API_INSTANCE;
/*     */   
/*     */   static {
/*  78 */     Object nonePhase = null;
/*  79 */     Object terrainSolidPhase = null;
/*  80 */     Object terrainTranslucentPhase = null;
/*  81 */     Method getPipelineManager = null;
/*  82 */     Method getPipelineNullable = null;
/*  83 */     Method getPhase = null;
/*  84 */     Method setPhase = null;
/*  85 */     Method isShaderPackInUse = null;
/*  86 */     Object irisApiInstance = null;
/*     */     
/*  88 */     if (PRESENT) {
/*     */       try {
/*  90 */         Class<?> irisClass = Class.forName("net.irisshaders.iris.Iris");
/*  91 */         Class<?> pipelineManagerClass = Class.forName("net.irisshaders.iris.pipeline.PipelineManager");
/*  92 */         Class<?> pipelineClass = Class.forName("net.irisshaders.iris.pipeline.WorldRenderingPipeline");
/*     */ 
/*     */         
/*  95 */         Class<? extends Enum> phaseClass = (Class)Class.forName("net.irisshaders.iris.pipeline.WorldRenderingPhase");
/*  96 */         Class<?> irisApiClass = Class.forName("net.irisshaders.iris.api.v0.IrisApi");
/*     */         
/*  98 */         getPipelineManager = irisClass.getMethod("getPipelineManager", new Class[0]);
/*  99 */         getPipelineNullable = pipelineManagerClass.getMethod("getPipelineNullable", new Class[0]);
/* 100 */         getPhase = pipelineClass.getMethod("getPhase", new Class[0]);
/* 101 */         setPhase = pipelineClass.getMethod("setPhase", new Class[] { phaseClass });
/*     */ 
/*     */         
/* 104 */         Object none = Enum.valueOf(phaseClass, "NONE");
/* 105 */         nonePhase = none;
/*     */ 
/*     */         
/* 108 */         Object terrainSolid = Enum.valueOf(phaseClass, "TERRAIN_SOLID");
/* 109 */         terrainSolidPhase = terrainSolid;
/*     */ 
/*     */         
/* 112 */         Object terrainTranslucent = Enum.valueOf(phaseClass, "TERRAIN_TRANSLUCENT");
/* 113 */         terrainTranslucentPhase = terrainTranslucent;
/*     */         
/* 115 */         Method getInstance = irisApiClass.getMethod("getInstance", new Class[0]);
/* 116 */         irisApiInstance = getInstance.invoke(null, new Object[0]);
/* 117 */         isShaderPackInUse = irisApiClass.getMethod("isShaderPackInUse", new Class[0]);
/* 118 */       } catch (ReflectiveOperationException|RuntimeException e) {
/* 119 */         getPipelineManager = null;
/* 120 */         getPipelineNullable = null;
/* 121 */         getPhase = null;
/* 122 */         setPhase = null;
/* 123 */         isShaderPackInUse = null;
/* 124 */         irisApiInstance = null;
/* 125 */         nonePhase = null;
/* 126 */         terrainSolidPhase = null;
/* 127 */         terrainTranslucentPhase = null;
/*     */       } 
/*     */     }
/*     */     
/* 131 */     NONE_PHASE = nonePhase;
/* 132 */     TERRAIN_SOLID_PHASE = terrainSolidPhase;
/* 133 */     TERRAIN_TRANSLUCENT_PHASE = terrainTranslucentPhase;
/* 134 */     GET_PIPELINE_MANAGER = getPipelineManager;
/* 135 */     GET_PIPELINE_NULLABLE = getPipelineNullable;
/* 136 */     GET_PHASE = getPhase;
/* 137 */     SET_PHASE = setPhase;
/* 138 */     IS_SHADER_PACK_IN_USE = isShaderPackInUse;
/* 139 */     IRIS_API_INSTANCE = irisApiInstance;
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
/*     */   static boolean isShaderPackActive() {
/* 156 */     if (IS_SHADER_PACK_IN_USE == null) {
/* 157 */       return false;
/*     */     }
/*     */     try {
/* 160 */       return ((Boolean)IS_SHADER_PACK_IN_USE.invoke(IRIS_API_INSTANCE, new Object[0])).booleanValue();
/* 161 */     } catch (ReflectiveOperationException e) {
/* 162 */       return false;
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
/*     */   static Object beginNeutralPhase() {
/* 178 */     return beginPhase(NONE_PHASE);
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
/*     */   static Object beginTerrainPhase() {
/* 203 */     return beginPhase(TERRAIN_SOLID_PHASE);
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
/*     */   static Object beginTranslucentPhase() {
/* 218 */     return beginPhase(TERRAIN_TRANSLUCENT_PHASE);
/*     */   }
/*     */   
/*     */   private static Object beginPhase(Object targetPhase) {
/* 222 */     if (targetPhase == null || GET_PIPELINE_MANAGER == null || !isShaderPackActive()) {
/* 223 */       return null;
/*     */     }
/*     */     try {
/* 226 */       Object pipelineManager = GET_PIPELINE_MANAGER.invoke(null, new Object[0]);
/* 227 */       Object pipeline = GET_PIPELINE_NULLABLE.invoke(pipelineManager, new Object[0]);
/* 228 */       if (pipeline == null) {
/* 229 */         return null;
/*     */       }
/* 231 */       Object previousPhase = GET_PHASE.invoke(pipeline, new Object[0]);
/* 232 */       SET_PHASE.invoke(pipeline, new Object[] { targetPhase });
/* 233 */       return new Object[] { pipeline, previousPhase };
/* 234 */     } catch (ReflectiveOperationException e) {
/* 235 */       return null;
/*     */     } 
/*     */   }
/*     */ 
/*     */   
/*     */   static void endPhase(Object token) {
/* 241 */     if (!(token instanceof Object[])) {
/*     */       return;
/*     */     }
/* 244 */     Object[] pair = (Object[])token;
/*     */     try {
/* 246 */       SET_PHASE.invoke(pair[0], new Object[] { pair[1] });
/* 247 */     } catch (ReflectiveOperationException reflectiveOperationException) {}
/*     */   }
/*     */ }


/* Location:              C:\Users\Walker\Downloads\ecstatic-forge-1.20.1-1.3.0.jar!\com\angryalchemist\ecstatic\render\IrisCompat.class
 * Java compiler version: 17 (61.0)
 * JD-Core Version:       1.1.3
 */