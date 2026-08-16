/*     */ package com.angryalchemist.ecstatic.render;
/*     */ 
/*     */ import java.util.EnumMap;
/*     */ import java.util.Map;
/*     */ import net.minecraft.client.Minecraft;
/*     */ import net.minecraft.client.gui.GuiGraphics;
/*     */ import net.minecraft.client.gui.components.Button;
/*     */ import net.minecraft.client.gui.components.EditBox;
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
/*     */ final class LodTerrainColorScreen
/*     */   extends Screen
/*     */ {
/*     */   private static final int ROW_STRIDE = 26;
/*     */   private static final int FIELD_WIDTH = 80;
/*     */   private static final int WIDGET_HEIGHT = 20;
/*     */   private static final int SWATCH_SIZE = 20;
/*     */   private static final int TOP = 44;
/*     */   private final Screen parent;
/*  39 */   private final LodSettingsConfig config = LodSettingsConfig.get();
/*  40 */   private final Map<SurfaceMaterial.Kind, EditBox> fields = new EnumMap<>(SurfaceMaterial.Kind.class);
/*     */   
/*     */   LodTerrainColorScreen(Screen parent) {
/*  43 */     super((Component)Component.m_237113_("Ecstatic Settings - Terrain Colors"));
/*  44 */     this.parent = parent;
/*     */   }
/*     */ 
/*     */   
/*     */   protected void m_7856_() {
/*  49 */     this.fields.clear();
/*  50 */     int centerX = this.f_96543_ / 2;
/*  51 */     int y = 44;
/*  52 */     for (SurfaceMaterial.Kind kind : SurfaceMaterial.Kind.values()) {
/*  53 */       EditBox box = (EditBox)m_142416_((GuiEventListener)new EditBox(this.f_96547_, centerX + 10, y, 80, 20, 
/*  54 */             (Component)Component.m_237113_(label(kind))));
/*  55 */       box.m_94199_(6);
/*  56 */       box.m_94144_(toHex(this.config.nearTerrainTint(kind)));
/*  57 */       this.fields.put(kind, box);
/*  58 */       y += 26;
/*     */     } 
/*     */     
/*  61 */     int footerY = this.f_96544_ - 28;
/*  62 */     m_142416_((GuiEventListener)Button.m_253074_((Component)Component.m_237113_("Reset"), b -> onReset())
/*  63 */         .m_252987_(centerX - 158, footerY, 100, 20).m_253136_());
/*  64 */     m_142416_((GuiEventListener)Button.m_253074_((Component)Component.m_237113_("Save"), b -> onSave())
/*  65 */         .m_252987_(centerX - 52, footerY, 100, 20).m_253136_());
/*  66 */     m_142416_((GuiEventListener)Button.m_253074_((Component)Component.m_237113_("Done"), b -> m_7379_())
/*  67 */         .m_252987_(centerX + 54, footerY, 100, 20).m_253136_());
/*     */   }
/*     */ 
/*     */   
/*     */   public void m_88315_(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
/*  72 */     m_280273_(guiGraphics);
/*  73 */     super.m_88315_(guiGraphics, mouseX, mouseY, partialTick);
/*  74 */     guiGraphics.m_280653_(this.f_96547_, this.f_96539_, this.f_96543_ / 2, 14, 16777215);
/*  75 */     guiGraphics.m_280137_(this.f_96547_, "Hex tint, FFFFFF = unchanged. Applies on Save.", this.f_96543_ / 2, 28, 8421504);
/*     */ 
/*     */     
/*  78 */     for (Map.Entry<SurfaceMaterial.Kind, EditBox> entry : this.fields.entrySet()) {
/*  79 */       EditBox box = entry.getValue();
/*  80 */       guiGraphics.m_280488_(this.f_96547_, label(entry.getKey()), box.m_252754_() - 96, box.m_252907_() + 6, 16777215);
/*     */ 
/*     */       
/*  83 */       int rgb = parseHexOr(box.m_94155_(), this.config.nearTerrainTint(entry.getKey()));
/*  84 */       int x0 = box.m_252754_() + box.m_5711_() + 8;
/*  85 */       guiGraphics.m_280509_(x0, box.m_252907_(), x0 + 20, box.m_252907_() + box.m_93694_(), 0xFF000000 | rgb & 0xFFFFFF);
/*     */     } 
/*     */   }
/*     */ 
/*     */   
/*     */   private static String label(SurfaceMaterial.Kind kind) {
/*  91 */     switch (kind) { default: throw new IncompatibleClassChangeError();case GRASS: case DIRT: case STONE: case SAND: case SNOW: case RED_SAND: case TERRACOTTA: break; }  return 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */       
/*  98 */       "Terracotta";
/*     */   }
/*     */ 
/*     */   
/*     */   private void onReset() {
/* 103 */     for (SurfaceMaterial.Kind kind : SurfaceMaterial.Kind.values()) {
/* 104 */       this.config.setNearTerrainTint(kind, 16777215);
/*     */     }
/* 106 */     onSave();
/* 107 */     m_232761_();
/*     */   }
/*     */   
/*     */   private void onSave() {
/* 111 */     for (Map.Entry<SurfaceMaterial.Kind, EditBox> entry : this.fields.entrySet()) {
/*     */ 
/*     */       
/* 114 */       int current = this.config.nearTerrainTint(entry.getKey());
/* 115 */       this.config.setNearTerrainTint(entry.getKey(), parseHexOr(((EditBox)entry.getValue()).m_94155_(), current));
/*     */     } 
/* 117 */     this.config.save();
/* 118 */     LodRenderer.rebuildAllMeshes();
/*     */   }
/*     */   
/*     */   private static int parseHexOr(String text, int fallback) {
/*     */     try {
/* 123 */       return Mth.m_14045_(Integer.parseInt(text.trim(), 16), 0, 16777215);
/* 124 */     } catch (NumberFormatException e) {
/* 125 */       return fallback;
/*     */     } 
/*     */   }
/*     */   
/*     */   private static String toHex(int rgb) {
/* 130 */     return String.format("%06X", new Object[] { Integer.valueOf(rgb & 0xFFFFFF) });
/*     */   }
/*     */ 
/*     */   
/*     */   public void m_7379_() {
/* 135 */     Minecraft.m_91087_().m_91152_(this.parent);
/*     */   }
/*     */ }


/* Location:              C:\Users\Walker\Downloads\ecstatic-forge-1.20.1-1.3.0.jar!\com\angryalchemist\ecstatic\render\LodTerrainColorScreen.class
 * Java compiler version: 17 (61.0)
 * JD-Core Version:       1.1.3
 */