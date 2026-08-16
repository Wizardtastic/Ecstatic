/*     */ package com.angryalchemist.ecstatic.render;
/*     */ 
/*     */ import java.util.Objects;
/*     */ import net.minecraft.client.Minecraft;
/*     */ import net.minecraft.client.gui.GuiGraphics;
/*     */ import net.minecraft.client.gui.components.AbstractSliderButton;
/*     */ import net.minecraft.client.gui.components.Button;
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
/*     */ final class LodLightingScreen
/*     */   extends Screen
/*     */ {
/*     */   private static final int ROW_STRIDE = 32;
/*     */   private static final int WIDGET_HEIGHT = 20;
/*     */   private static final int SLIDER_WIDTH = 260;
/*     */   private static final int VIEWPORT_TOP = 50;
/*     */   private static final int FOOTER_HEIGHT = 44;
/*     */   private static final int SCROLLBAR_WIDTH = 6;
/*     */   private static final double SCROLL_SPEED = 16.0D;
/*     */   private static final int ROW_COUNT = 8;
/*     */   private static final int STEP_PERCENT = 1;
/*     */   private final Screen parent;
/*  51 */   private final LodSettingsConfig config = LodSettingsConfig.get();
/*     */   
/*     */   private double scrollAmount;
/*     */   private int maxScroll;
/*     */   
/*     */   LodLightingScreen(Screen parent) {
/*  57 */     super((Component)Component.m_237113_("Ecstatic Settings - Lighting"));
/*  58 */     this.parent = parent;
/*     */   }
/*     */ 
/*     */   
/*     */   protected void m_7856_() {
/*  63 */     int centerX = this.f_96543_ / 2;
/*  64 */     int viewportTop = 50;
/*  65 */     int viewportBottom = this.f_96544_ - 44;
/*  66 */     int viewportHeight = viewportBottom - viewportTop;
/*  67 */     int contentHeight = 256;
/*  68 */     this.maxScroll = Math.max(0, contentHeight - viewportHeight);
/*  69 */     this.scrollAmount = Mth.m_14008_(this.scrollAmount, 0.0D, this.maxScroll);
/*     */     
/*  71 */     int y = viewportTop - (int)this.scrollAmount;
/*     */ 
/*     */     
/*  74 */     Objects.requireNonNull(this.config); y = addSlider(centerX, y, viewportTop, viewportBottom, "Night brightness", this.config.nightBrightness(), this.config::setNightBrightness);
/*     */     
/*  76 */     Objects.requireNonNull(this.config); y = addSlider(centerX, y, viewportTop, viewportBottom, "Day brightness", this.config.dayBrightness(), this.config::setDayBrightness);
/*     */     
/*  78 */     Objects.requireNonNull(this.config); y = addSlider(centerX, y, viewportTop, viewportBottom, "Shading softness (far)", this.config.slopeShadingFloor(), this.config::setSlopeShadingFloor);
/*     */     
/*  80 */     Objects.requireNonNull(this.config); y = addSlider(centerX, y, viewportTop, viewportBottom, "Shading softness (near)", this.config.nearSlopeShadingFloor(), this.config::setNearSlopeShadingFloor);
/*     */     
/*  82 */     Objects.requireNonNull(this.config); y = addSlider(centerX, y, viewportTop, viewportBottom, "Shading softness (structures)", this.config.structureSlopeShadingFloor(), this.config::setStructureSlopeShadingFloor);
/*     */     
/*  84 */     Objects.requireNonNull(this.config); y = addSlider(centerX, y, viewportTop, viewportBottom, "Desaturation", this.config.saturationReduction(), this.config::setSaturationReduction);
/*     */     
/*  86 */     Objects.requireNonNull(this.config); y = addSlider(centerX, y, viewportTop, viewportBottom, "Sun relief", this.config.sunReliefStrength(), this.config::setSunReliefStrength);
/*     */ 
/*     */ 
/*     */     
/*  90 */     Objects.requireNonNull(this.config); addSlider(centerX, y, viewportTop, viewportBottom, "Light temperature (50% neutral)", this.config.lightTemperature(), this.config::setLightTemperature);
/*     */     
/*  92 */     int footerY = this.f_96544_ - 44 + 20;
/*  93 */     m_142416_((GuiEventListener)Button.m_253074_((Component)Component.m_237113_("Save"), b -> onSave())
/*  94 */         .m_252987_(centerX - 105, footerY, 100, 20).m_253136_());
/*  95 */     m_142416_((GuiEventListener)Button.m_253074_((Component)Component.m_237113_("Done"), b -> m_7379_())
/*  96 */         .m_252987_(centerX + 5, footerY, 100, 20).m_253136_());
/*     */   }
/*     */ 
/*     */ 
/*     */   
/*     */   private int addSlider(int centerX, int y, int viewportTop, int viewportBottom, final String label, float initialValue, final FloatSetter setter) {
/* 102 */     int initialPercent = Math.round(initialValue * 100.0F);
/*     */     
/* 104 */     AbstractSliderButton slider = new AbstractSliderButton(centerX - 130, y, 260, 20, (Component)Component.m_237113_(label + ": " + label + "%"), initialValue)
/*     */       {
/*     */         protected void m_5695_() {
/* 107 */           m_93666_((Component)Component.m_237113_(label + ": " + label + "%"));
/*     */         }
/*     */ 
/*     */         
/*     */         protected void m_5697_() {
/* 112 */           setter.set(percent() / 100.0F);
/*     */         }
/*     */         
/*     */         private int percent() {
/* 116 */           int raw = (int)Math.round(this.f_93577_ * 100.0D);
/* 117 */           return Math.round(raw / 1.0F) * 1;
/*     */         }
/*     */       };
/* 120 */     slider.f_93624_ = (y >= viewportTop && y + 20 <= viewportBottom);
/* 121 */     m_142416_((GuiEventListener)slider);
/* 122 */     return y + 32;
/*     */   }
/*     */ 
/*     */   
/*     */   @FunctionalInterface
/*     */   private static interface FloatSetter
/*     */   {
/*     */     void set(float param1Float);
/*     */   }
/*     */ 
/*     */   
/*     */   public boolean m_6050_(double mouseX, double mouseY, double delta) {
/* 134 */     if (this.maxScroll > 0) {
/* 135 */       this.scrollAmount = Mth.m_14008_(this.scrollAmount - delta * 16.0D, 0.0D, this.maxScroll);
/* 136 */       m_232761_();
/* 137 */       return true;
/*     */     } 
/* 139 */     return super.m_6050_(mouseX, mouseY, delta);
/*     */   }
/*     */ 
/*     */   
/*     */   public void m_88315_(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
/* 144 */     m_280273_(guiGraphics);
/* 145 */     super.m_88315_(guiGraphics, mouseX, mouseY, partialTick);
/* 146 */     guiGraphics.m_280653_(this.f_96547_, this.f_96539_, this.f_96543_ / 2, 15, 16777215);
/* 147 */     guiGraphics.m_280137_(this.f_96547_, "Brightness and desaturation preview live; the rest apply on Save", this.f_96543_ / 2, 32, 8421504);
/*     */ 
/*     */ 
/*     */     
/* 151 */     if (this.maxScroll > 0) {
/* 152 */       int centerX = this.f_96543_ / 2;
/* 153 */       int trackX = centerX + 130 + 10;
/* 154 */       int trackTop = 50;
/* 155 */       int trackBottom = this.f_96544_ - 44;
/* 156 */       int trackHeight = trackBottom - trackTop;
/* 157 */       guiGraphics.m_280509_(trackX, trackTop, trackX + 6, trackBottom, 1090519039);
/* 158 */       int thumbHeight = Math.max(12, trackHeight * trackHeight / (trackHeight + this.maxScroll));
/* 159 */       int thumbY = trackTop + (int)((trackHeight - thumbHeight) * this.scrollAmount / this.maxScroll);
/* 160 */       guiGraphics.m_280509_(trackX, thumbY, trackX + 6, thumbY + thumbHeight, -4144960);
/*     */     } 
/*     */   }
/*     */ 
/*     */   
/*     */   public void m_7379_() {
/* 166 */     Minecraft.m_91087_().m_91152_(this.parent);
/*     */   }
/*     */   
/*     */   private void onSave() {
/* 170 */     this.config.save();
/* 171 */     LodRenderer.rebuildAllMeshes();
/*     */   }
/*     */ }


/* Location:              C:\Users\Walker\Downloads\ecstatic-forge-1.20.1-1.3.0.jar!\com\angryalchemist\ecstatic\render\LodLightingScreen.class
 * Java compiler version: 17 (61.0)
 * JD-Core Version:       1.1.3
 */