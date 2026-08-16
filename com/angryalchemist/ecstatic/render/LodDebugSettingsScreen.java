/*     */ package com.angryalchemist.ecstatic.render;
/*     */ 
/*     */ import net.minecraft.client.Minecraft;
/*     */ import net.minecraft.client.gui.GuiGraphics;
/*     */ import net.minecraft.client.gui.components.Button;
/*     */ import net.minecraft.client.gui.components.Checkbox;
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
/*     */ final class LodDebugSettingsScreen
/*     */   extends Screen
/*     */ {
/*     */   private static final int ROW_STRIDE = 32;
/*     */   private static final int WIDGET_HEIGHT = 20;
/*     */   private static final int CHECKBOX_WIDTH = 260;
/*     */   private static final int VIEWPORT_TOP = 50;
/*     */   private static final int FOOTER_HEIGHT = 44;
/*     */   private static final int SCROLLBAR_WIDTH = 6;
/*     */   private static final double SCROLL_SPEED = 16.0D;
/*     */   private static final int ROW_COUNT = 4;
/*     */   private final Screen parent;
/*  44 */   private final LodSettingsConfig config = LodSettingsConfig.get();
/*     */   
/*     */   private double scrollAmount;
/*     */   private int maxScroll;
/*     */   
/*     */   LodDebugSettingsScreen(Screen parent) {
/*  50 */     super((Component)Component.m_237113_("Ecstatic Settings - Debug"));
/*  51 */     this.parent = parent;
/*     */   }
/*     */ 
/*     */   
/*     */   protected void m_7856_() {
/*  56 */     int centerX = this.f_96543_ / 2;
/*  57 */     int viewportTop = 50;
/*  58 */     int viewportBottom = this.f_96544_ - 44;
/*  59 */     int viewportHeight = viewportBottom - viewportTop;
/*  60 */     int contentHeight = 128;
/*  61 */     this.maxScroll = Math.max(0, contentHeight - viewportHeight);
/*  62 */     this.scrollAmount = Mth.m_14008_(this.scrollAmount, 0.0D, this.maxScroll);
/*     */     
/*  64 */     int y = viewportTop - (int)this.scrollAmount;
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */     
/*  70 */     Checkbox debugTools = new Checkbox(centerX - 130, y, 260, 20, (Component)Component.m_237113_("Enable debug tools (F7-F10 keybinds)"), this.config.debugToolsEnabled())
/*     */       {
/*     */         public void m_5691_() {
/*  73 */           super.m_5691_();
/*  74 */           LodDebugSettingsScreen.this.config.setDebugToolsEnabled(m_93840_());
/*     */         }
/*     */       };
/*  77 */     debugTools.f_93624_ = (y >= viewportTop && y + 20 <= viewportBottom);
/*  78 */     m_142416_((GuiEventListener)debugTools);
/*     */     
/*  80 */     y += 32;
/*     */     
/*  82 */     Checkbox litFormat = new Checkbox(centerX - 130, y, 260, 20, (Component)Component.m_237113_("Lit vertex format (heavier, more GPU cost)"), this.config.useLitVertexFormat())
/*     */       {
/*     */         public void m_5691_() {
/*  85 */           super.m_5691_();
/*  86 */           LodDebugSettingsScreen.this.config.setUseLitVertexFormat(m_93840_());
/*     */         }
/*     */       };
/*  89 */     litFormat.f_93624_ = (y >= viewportTop && y + 20 <= viewportBottom);
/*  90 */     m_142416_((GuiEventListener)litFormat);
/*     */     
/*  92 */     y += 32;
/*     */     
/*  94 */     Checkbox shaderWater = new Checkbox(centerX - 130, y, 260, 20, (Component)Component.m_237113_("Shaderpack water at distance (experimental)"), this.config.shaderWaterEnabled())
/*     */       {
/*     */         public void m_5691_() {
/*  97 */           super.m_5691_();
/*  98 */           LodDebugSettingsScreen.this.config.setShaderWaterEnabled(m_93840_());
/*     */         }
/*     */       };
/* 101 */     shaderWater.f_93624_ = (y >= viewportTop && y + 20 <= viewportBottom);
/* 102 */     m_142416_((GuiEventListener)shaderWater);
/*     */     
/* 104 */     y += 32;
/*     */ 
/*     */     
/* 107 */     Checkbox backfaceCulling = new Checkbox(centerX - 130, y, 260, 20, (Component)Component.m_237113_("Backface culling (faster, can show gaps)"), this.config.backfaceCullingEnabled())
/*     */       {
/*     */         public void m_5691_() {
/* 110 */           super.m_5691_();
/* 111 */           LodDebugSettingsScreen.this.config.setBackfaceCullingEnabled(m_93840_());
/*     */         }
/*     */       };
/* 114 */     backfaceCulling.f_93624_ = (y >= viewportTop && y + 20 <= viewportBottom);
/* 115 */     m_142416_((GuiEventListener)backfaceCulling);
/*     */     
/* 117 */     int footerY = this.f_96544_ - 44 + 20;
/* 118 */     m_142416_((GuiEventListener)Button.m_253074_((Component)Component.m_237113_("Save"), b -> onSave())
/* 119 */         .m_252987_(centerX - 105, footerY, 100, 20).m_253136_());
/* 120 */     m_142416_((GuiEventListener)Button.m_253074_((Component)Component.m_237113_("Done"), b -> m_7379_())
/* 121 */         .m_252987_(centerX + 5, footerY, 100, 20).m_253136_());
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   public boolean m_6050_(double mouseX, double mouseY, double delta) {
/* 130 */     if (this.maxScroll > 0) {
/* 131 */       this.scrollAmount = Mth.m_14008_(this.scrollAmount - delta * 16.0D, 0.0D, this.maxScroll);
/* 132 */       m_232761_();
/* 133 */       return true;
/*     */     } 
/* 135 */     return super.m_6050_(mouseX, mouseY, delta);
/*     */   }
/*     */ 
/*     */   
/*     */   public void m_88315_(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
/* 140 */     m_280273_(guiGraphics);
/* 141 */     super.m_88315_(guiGraphics, mouseX, mouseY, partialTick);
/* 142 */     guiGraphics.m_280653_(this.f_96547_, this.f_96539_, this.f_96543_ / 2, 15, 16777215);
/* 143 */     guiGraphics.m_280137_(this.f_96547_, "Lit vertex format is currently broken - leave unchecked", this.f_96543_ / 2, 32, 8421504);
/*     */ 
/*     */ 
/*     */     
/* 147 */     if (this.maxScroll > 0) {
/* 148 */       int centerX = this.f_96543_ / 2;
/* 149 */       int trackX = centerX + 130 + 10;
/* 150 */       int trackTop = 50;
/* 151 */       int trackBottom = this.f_96544_ - 44;
/* 152 */       int trackHeight = trackBottom - trackTop;
/* 153 */       guiGraphics.m_280509_(trackX, trackTop, trackX + 6, trackBottom, 1090519039);
/* 154 */       int thumbHeight = Math.max(12, trackHeight * trackHeight / (trackHeight + this.maxScroll));
/* 155 */       int thumbY = trackTop + (int)((trackHeight - thumbHeight) * this.scrollAmount / this.maxScroll);
/* 156 */       guiGraphics.m_280509_(trackX, thumbY, trackX + 6, thumbY + thumbHeight, -4144960);
/*     */     } 
/*     */   }
/*     */ 
/*     */   
/*     */   public void m_7379_() {
/* 162 */     Minecraft.m_91087_().m_91152_(this.parent);
/*     */   }
/*     */   
/*     */   private void onSave() {
/* 166 */     this.config.save();
/* 167 */     LodRenderer.rebuildAllMeshes();
/*     */   }
/*     */ }


/* Location:              C:\Users\Walker\Downloads\ecstatic-forge-1.20.1-1.3.0.jar!\com\angryalchemist\ecstatic\render\LodDebugSettingsScreen.class
 * Java compiler version: 17 (61.0)
 * JD-Core Version:       1.1.3
 */