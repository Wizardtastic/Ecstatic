/*     */ package com.angryalchemist.ecstatic.render;
/*     */ 
/*     */ import net.minecraft.client.Minecraft;
/*     */ import net.minecraft.client.gui.GuiGraphics;
/*     */ import net.minecraft.client.gui.components.AbstractSliderButton;
/*     */ import net.minecraft.client.gui.components.Button;
/*     */ import net.minecraft.client.gui.components.Checkbox;
/*     */ import net.minecraft.client.gui.components.CycleButton;
/*     */ import net.minecraft.client.gui.components.events.GuiEventListener;
/*     */ import net.minecraft.client.gui.screens.Screen;
/*     */ import net.minecraft.network.chat.Component;
/*     */ import net.minecraft.util.Mth;
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ public final class LodSettingsScreen
/*     */   extends Screen
/*     */ {
/*     */   private static final int ROW_STRIDE = 32;
/*     */   private static final int WIDGET_HEIGHT = 20;
/*     */   private static final int CHECKBOX_WIDTH = 260;
/*     */   private static final int VIEWPORT_TOP = 50;
/*     */   private static final int FOOTER_HEIGHT = 44;
/*     */   private static final int SCROLLBAR_WIDTH = 6;
/*     */   private static final double SCROLL_SPEED = 16.0D;
/*     */   private static final int ROW_COUNT = 11;
/*     */   private static final int RENDER_DISTANCE_MIN_PERCENT = 25;
/*     */   private static final int RENDER_DISTANCE_MAX_PERCENT = 200;
/*     */   private static final int RENDER_DISTANCE_STEP_PERCENT = 5;
/*     */   private static final int FOG_FALLOFF_MIN_PERCENT = 25;
/*     */   private static final int FOG_FALLOFF_MAX_PERCENT = 300;
/*     */   private static final int FOG_FALLOFF_STEP_PERCENT = 5;
/*     */   private static final int FOG_INTENSITY_MIN_PERCENT = 0;
/*     */   private static final int FOG_INTENSITY_MAX_PERCENT = 100;
/*     */   private static final int FOG_INTENSITY_STEP_PERCENT = 5;
/*  81 */   private final LodSettingsConfig config = LodSettingsConfig.get();
/*     */   
/*     */   private double scrollAmount;
/*     */   private int maxScroll;
/*     */   
/*     */   public LodSettingsScreen() {
/*  87 */     super((Component)Component.m_237113_("Ecstatic Settings"));
/*     */   }
/*     */ 
/*     */   
/*     */   protected void m_7856_() {
/*  92 */     int centerX = this.f_96543_ / 2;
/*  93 */     int viewportTop = 50;
/*  94 */     int viewportBottom = this.f_96544_ - 44;
/*  95 */     int viewportHeight = viewportBottom - viewportTop;
/*  96 */     int contentHeight = 352;
/*  97 */     this.maxScroll = Math.max(0, contentHeight - viewportHeight);
/*  98 */     this.scrollAmount = Mth.m_14008_(this.scrollAmount, 0.0D, this.maxScroll);
/*     */     
/* 100 */     int y = viewportTop - (int)this.scrollAmount;
/*     */ 
/*     */     
/* 103 */     Checkbox frustumCulling = new Checkbox(centerX - 130, y, 260, 20, (Component)Component.m_237113_("Horizon (frustum) culling"), this.config.frustumCullingEnabled())
/*     */       {
/*     */         public void m_5691_() {
/* 106 */           super.m_5691_();
/* 107 */           LodSettingsScreen.this.config.setFrustumCullingEnabled(m_93840_());
/*     */         }
/*     */       };
/* 110 */     frustumCulling.f_93624_ = (y >= viewportTop && y + 20 <= viewportBottom);
/* 111 */     m_142416_((GuiEventListener)frustumCulling);
/*     */     
/* 113 */     y += 32;
/*     */     
/* 115 */     Checkbox oceanPlane = new Checkbox(centerX - 130, y, 260, 20, (Component)Component.m_237113_("LOD water plane (animated distant ocean)"), this.config.oceanPlaneEnabled())
/*     */       {
/*     */         public void m_5691_() {
/* 118 */           super.m_5691_();
/* 119 */           LodSettingsScreen.this.config.setOceanPlaneEnabled(m_93840_());
/*     */         }
/*     */       };
/* 122 */     oceanPlane.f_93624_ = (y >= viewportTop && y + 20 <= viewportBottom);
/* 123 */     m_142416_((GuiEventListener)oceanPlane);
/*     */     
/* 125 */     y += 32;
/*     */     
/* 127 */     Checkbox opaqueWater = new Checkbox(centerX - 130, y, 260, 20, (Component)Component.m_237113_("Opaque water (culls fully submerged terrain)"), this.config.opaqueWaterEnabled())
/*     */       {
/*     */         public void m_5691_() {
/* 130 */           super.m_5691_();
/* 131 */           LodSettingsScreen.this.config.setOpaqueWaterEnabled(m_93840_());
/*     */         }
/*     */       };
/* 134 */     opaqueWater.f_93624_ = (y >= viewportTop && y + 20 <= viewportBottom);
/* 135 */     m_142416_((GuiEventListener)opaqueWater);
/*     */     
/* 137 */     y += 32;
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */     
/* 144 */     CycleButton<Integer> lod1Detail = CycleButton.m_168894_(step -> Component.m_237113_((step.intValue() == 2) ? "LOD1 detail: 2-block steps (coarser, cheaper)" : "LOD1 detail: 1-block steps (finest, default)")).m_168961_((Object[])new Integer[] { Integer.valueOf(1), Integer.valueOf(2) }).m_168948_(Integer.valueOf(this.config.lod1SubStepBlocks())).m_168936_(centerX - 130, y, 260, 20, 
/* 145 */         (Component)Component.m_237113_("LOD1 detail"), (button, step) -> this.config.setLod1SubStepBlocks(step.intValue()));
/*     */     
/* 147 */     lod1Detail.f_93624_ = (y >= viewportTop && y + 20 <= viewportBottom);
/* 148 */     m_142416_((GuiEventListener)lod1Detail);
/*     */     
/* 150 */     y += 32;
/* 151 */     double renderDistanceInitialValue = valueFromPercent(Math.round(this.config.lodRenderDistanceScale() * 100.0F), 25, 200);
/*     */     
/* 153 */     int renderDistanceInitialPercent = percentFromValue(renderDistanceInitialValue, 25, 200, 5);
/*     */ 
/*     */     
/* 156 */     AbstractSliderButton renderDistance = new AbstractSliderButton(centerX - 130, y, 260, 20, (Component)Component.m_237113_("Render distance: " + renderDistanceInitialPercent + "%"), renderDistanceInitialValue)
/*     */       {
/*     */         protected void m_5695_()
/*     */         {
/* 160 */           m_93666_((Component)Component.m_237113_("Render distance: " + LodSettingsScreen.percentFromValue(this.f_93577_, 25, 200, 5) + "%"));
/*     */         }
/*     */ 
/*     */ 
/*     */         
/*     */         protected void m_5697_() {
/* 166 */           LodSettingsScreen.this.config.setLodRenderDistanceScale(LodSettingsScreen.percentFromValue(this.f_93577_, 25, 200, 5) / 100.0F);
/*     */         }
/*     */       };
/*     */     
/* 170 */     renderDistance.f_93624_ = (y >= viewportTop && y + 20 <= viewportBottom);
/* 171 */     m_142416_((GuiEventListener)renderDistance);
/*     */     
/* 173 */     y += 32;
/* 174 */     final int maxThreads = Math.max(1, Runtime.getRuntime().availableProcessors());
/* 175 */     double workerThreadsInitialValue = threadsValueFromCount(this.config.workerThreadCount(), maxThreads);
/* 176 */     int workerThreadsInitialCount = threadsFromValue(workerThreadsInitialValue, maxThreads);
/*     */     
/* 178 */     AbstractSliderButton workerThreads = new AbstractSliderButton(centerX - 130, y, 260, 20, (Component)Component.m_237113_("Worker threads: " + workerThreadsInitialCount + " / " + maxThreads + " detected"), workerThreadsInitialValue)
/*     */       {
/*     */         protected void m_5695_()
/*     */         {
/* 182 */           m_93666_((Component)Component.m_237113_("Worker threads: " + LodSettingsScreen.this.threadsFromValue(this.f_93577_, maxThreads) + " / " + maxThreads + " detected"));
/*     */         }
/*     */ 
/*     */ 
/*     */         
/*     */         protected void m_5697_() {
/* 188 */           LodSettingsScreen.this.config.setWorkerThreadCount(LodSettingsScreen.this.threadsFromValue(this.f_93577_, maxThreads));
/*     */         }
/*     */       };
/* 191 */     workerThreads.f_93624_ = (y >= viewportTop && y + 20 <= viewportBottom);
/* 192 */     m_142416_((GuiEventListener)workerThreads);
/*     */     
/* 194 */     y += 32;
/* 195 */     double fogFalloffInitialValue = valueFromPercent(Math.round(this.config.fogFalloffScale() * 100.0F), 25, 300);
/*     */     
/* 197 */     int fogFalloffInitialPercent = percentFromValue(fogFalloffInitialValue, 25, 300, 5);
/*     */ 
/*     */     
/* 200 */     AbstractSliderButton fogFalloff = new AbstractSliderButton(centerX - 130, y, 260, 20, (Component)Component.m_237113_("Fog falloff: " + fogFalloffInitialPercent + "%"), fogFalloffInitialValue)
/*     */       {
/*     */         protected void m_5695_()
/*     */         {
/* 204 */           m_93666_((Component)Component.m_237113_("Fog falloff: " + LodSettingsScreen.percentFromValue(this.f_93577_, 25, 300, 5) + "%"));
/*     */         }
/*     */ 
/*     */ 
/*     */         
/*     */         protected void m_5697_() {
/* 210 */           LodSettingsScreen.this.config.setFogFalloffScale(LodSettingsScreen.percentFromValue(this.f_93577_, 25, 300, 5) / 100.0F);
/*     */         }
/*     */       };
/*     */     
/* 214 */     fogFalloff.f_93624_ = (y >= viewportTop && y + 20 <= viewportBottom);
/* 215 */     m_142416_((GuiEventListener)fogFalloff);
/*     */     
/* 217 */     y += 32;
/* 218 */     double fogIntensityInitialValue = valueFromPercent(Math.round(this.config.fogIntensity() * 100.0F), 0, 100);
/*     */     
/* 220 */     int fogIntensityInitialPercent = percentFromValue(fogIntensityInitialValue, 0, 100, 5);
/*     */ 
/*     */     
/* 223 */     AbstractSliderButton fogIntensity = new AbstractSliderButton(centerX - 130, y, 260, 20, (Component)Component.m_237113_("Fog intensity: " + fogIntensityInitialPercent + "%"), fogIntensityInitialValue)
/*     */       {
/*     */         protected void m_5695_()
/*     */         {
/* 227 */           m_93666_((Component)Component.m_237113_("Fog intensity: " + LodSettingsScreen.percentFromValue(this.f_93577_, 0, 100, 5) + "%"));
/*     */         }
/*     */ 
/*     */ 
/*     */         
/*     */         protected void m_5697_() {
/* 233 */           LodSettingsScreen.this.config.setFogIntensity(LodSettingsScreen.percentFromValue(this.f_93577_, 0, 100, 5) / 100.0F);
/*     */         }
/*     */       };
/*     */     
/* 237 */     fogIntensity.f_93624_ = (y >= viewportTop && y + 20 <= viewportBottom);
/* 238 */     m_142416_((GuiEventListener)fogIntensity);
/*     */     
/* 240 */     y += 32;
/*     */ 
/*     */     
/* 243 */     Button lightingButton = Button.m_253074_((Component)Component.m_237113_("Lighting..."), b -> Minecraft.m_91087_().m_91152_(new LodLightingScreen(this))).m_252987_(centerX - 130, y, 260, 20).m_253136_();
/* 244 */     lightingButton.f_93624_ = (y >= viewportTop && y + 20 <= viewportBottom);
/* 245 */     m_142416_((GuiEventListener)lightingButton);
/*     */     
/* 247 */     y += 32;
/*     */ 
/*     */     
/* 250 */     Button terrainColorButton = Button.m_253074_((Component)Component.m_237113_("Terrain Colors..."), b -> Minecraft.m_91087_().m_91152_(new LodTerrainColorScreen(this))).m_252987_(centerX - 130, y, 260, 20).m_253136_();
/* 251 */     terrainColorButton.f_93624_ = (y >= viewportTop && y + 20 <= viewportBottom);
/* 252 */     m_142416_((GuiEventListener)terrainColorButton);
/*     */     
/* 254 */     y += 32;
/*     */ 
/*     */     
/* 257 */     Button debugButton = Button.m_253074_((Component)Component.m_237113_("Debug Settings..."), b -> Minecraft.m_91087_().m_91152_(new LodDebugSettingsScreen(this))).m_252987_(centerX - 130, y, 260, 20).m_253136_();
/* 258 */     debugButton.f_93624_ = (y >= viewportTop && y + 20 <= viewportBottom);
/* 259 */     m_142416_((GuiEventListener)debugButton);
/*     */     
/* 261 */     int footerY = this.f_96544_ - 44 + 20;
/* 262 */     m_142416_((GuiEventListener)Button.m_253074_((Component)Component.m_237113_("Save"), b -> onSave())
/* 263 */         .m_252987_(centerX - 105, footerY, 100, 20).m_253136_());
/* 264 */     m_142416_((GuiEventListener)Button.m_253074_((Component)Component.m_237113_("Done"), b -> m_7379_())
/* 265 */         .m_252987_(centerX + 5, footerY, 100, 20).m_253136_());
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
/*     */   public boolean m_6050_(double mouseX, double mouseY, double delta) {
/* 278 */     if (this.maxScroll > 0) {
/* 279 */       this.scrollAmount = Mth.m_14008_(this.scrollAmount - delta * 16.0D, 0.0D, this.maxScroll);
/* 280 */       m_232761_();
/* 281 */       return true;
/*     */     } 
/* 283 */     return super.m_6050_(mouseX, mouseY, delta);
/*     */   }
/*     */ 
/*     */   
/*     */   public void m_88315_(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
/* 288 */     m_280273_(guiGraphics);
/* 289 */     super.m_88315_(guiGraphics, mouseX, mouseY, partialTick);
/* 290 */     guiGraphics.m_280653_(this.f_96547_, this.f_96539_, this.f_96543_ / 2, 15, 16777215);
/*     */     
/* 292 */     if (this.maxScroll > 0) {
/* 293 */       int centerX = this.f_96543_ / 2;
/* 294 */       int trackX = centerX + 130 + 10;
/* 295 */       int trackTop = 50;
/* 296 */       int trackBottom = this.f_96544_ - 44;
/* 297 */       int trackHeight = trackBottom - trackTop;
/* 298 */       guiGraphics.m_280509_(trackX, trackTop, trackX + 6, trackBottom, 1090519039);
/* 299 */       int thumbHeight = Math.max(12, trackHeight * trackHeight / (trackHeight + this.maxScroll));
/* 300 */       int thumbY = trackTop + (int)((trackHeight - thumbHeight) * this.scrollAmount / this.maxScroll);
/* 301 */       guiGraphics.m_280509_(trackX, thumbY, trackX + 6, thumbY + thumbHeight, -4144960);
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
/*     */   private static int percentFromValue(double value, int minPercent, int maxPercent, int stepPercent) {
/* 313 */     int steps = (maxPercent - minPercent) / stepPercent;
/* 314 */     int stepIndex = Mth.m_14045_((int)Math.round(value * steps), 0, steps);
/* 315 */     return minPercent + stepIndex * stepPercent;
/*     */   }
/*     */ 
/*     */   
/*     */   private static double valueFromPercent(int percent, int minPercent, int maxPercent) {
/* 320 */     int clamped = Mth.m_14045_(percent, minPercent, maxPercent);
/* 321 */     return (clamped - minPercent) / (maxPercent - minPercent);
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   private int threadsFromValue(double value, int maxThreads) {
/* 332 */     int steps = Math.max(1, maxThreads - 1);
/* 333 */     int threads = 1 + (int)Math.round(value * steps);
/* 334 */     return Mth.m_14045_(threads, 1, maxThreads);
/*     */   }
/*     */ 
/*     */   
/*     */   private double threadsValueFromCount(int threads, int maxThreads) {
/* 339 */     if (maxThreads <= 1) {
/* 340 */       return 0.0D;
/*     */     }
/* 342 */     int clamped = Mth.m_14045_(threads, 1, maxThreads);
/* 343 */     return (clamped - 1) / (maxThreads - 1);
/*     */   }
/*     */   
/*     */   private void onSave() {
/* 347 */     this.config.save();
/* 348 */     LodRenderer.rebuildAllMeshes();
/*     */   }
/*     */ }


/* Location:              C:\Users\Walker\Downloads\ecstatic-forge-1.20.1-1.3.0.jar!\com\angryalchemist\ecstatic\render\LodSettingsScreen.class
 * Java compiler version: 17 (61.0)
 * JD-Core Version:       1.1.3
 */