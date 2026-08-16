/*     */ package com.angryalchemist.ecstatic.render;
/*     */ 
/*     */ import com.angryalchemist.ecstatic.Constants;
/*     */ import java.awt.FileDialog;
/*     */ import java.awt.Frame;
/*     */ import java.io.IOException;
/*     */ import java.nio.file.Path;
/*     */ import java.nio.file.Paths;
/*     */ import java.util.Locale;
/*     */ import java.util.Objects;
/*     */ import java.util.function.Consumer;
/*     */ import java.util.function.IntConsumer;
/*     */ import net.minecraft.client.Minecraft;
/*     */ import net.minecraft.client.gui.GuiGraphics;
/*     */ import net.minecraft.client.gui.components.Button;
/*     */ import net.minecraft.client.gui.components.Checkbox;
/*     */ import net.minecraft.client.gui.components.EditBox;
/*     */ import net.minecraft.client.gui.components.events.GuiEventListener;
/*     */ import net.minecraft.client.gui.screens.Screen;
/*     */ import net.minecraft.network.chat.Component;
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ public final class BiomeStyleScreen
/*     */   extends Screen
/*     */ {
/*     */   private static final int ROW_HEIGHT = 24;
/*     */   private static final int SWATCH_SIZE = 20;
/*     */   private static final int FIELD_WIDTH = 80;
/*     */   private static final int GROUP_ROW_Y = 40;
/*     */   private static final int WATER_COLUMN_GAP = 20;
/*  37 */   private final BiomeStyleConfig config = BiomeStyleConfig.get();
/*  38 */   private TreeStyle.Group group = TreeStyle.Group.DEFAULT;
/*     */   private EditBox trunkTintBox;
/*     */   private EditBox foliageTintBox;
/*     */   private EditBox groundTintBox;
/*     */   private Checkbox groundTintEnabledCheckbox;
/*     */   private Button trunkTextureButton;
/*     */   private Button foliageTextureButton;
/*     */   private EditBox waterColorBox;
/*     */   private EditBox waterOpacityBox;
/*     */   private EditBox snowHeightBox;
/*     */   private int waterLabelY;
/*     */   
/*     */   public BiomeStyleScreen() {
/*  51 */     super((Component)Component.m_237113_("Biome Style Editor"));
/*     */   }
/*     */ 
/*     */   
/*     */   protected void m_7856_() {
/*  56 */     int centerX = this.f_96543_ / 2;
/*  57 */     int y = 40;
/*     */     
/*  59 */     m_142416_((GuiEventListener)Button.m_253074_((Component)Component.m_237113_("<"), b -> cycleGroup(-1))
/*  60 */         .m_252987_(centerX - 110, y, 20, 20).m_253136_());
/*  61 */     m_142416_((GuiEventListener)Button.m_253074_((Component)Component.m_237113_(">"), b -> cycleGroup(1))
/*  62 */         .m_252987_(centerX + 90, y, 20, 20).m_253136_());
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */     
/*  70 */     int leftColumnX = centerX - 60 - 80 - 20 - 20;
/*     */     
/*  72 */     y += 36;
/*  73 */     int trunkRowY = y;
/*  74 */     this.trunkTintBox = (EditBox)m_142416_((GuiEventListener)new EditBox(this.f_96547_, centerX - 60, y, 80, 20, 
/*  75 */           (Component)Component.m_237113_("Trunk Tint")));
/*  76 */     this.trunkTintBox.m_94199_(7);
/*  77 */     this.trunkTintBox.m_94144_(toHex(this.config.trunkTint(this.group)));
/*  78 */     this.trunkTintBox.m_94151_(value -> applyHex(value, ()));
/*     */     
/*  80 */     y += 24;
/*  81 */     int foliageRowY = y;
/*  82 */     this.foliageTintBox = (EditBox)m_142416_((GuiEventListener)new EditBox(this.f_96547_, centerX - 60, y, 80, 20, 
/*  83 */           (Component)Component.m_237113_("Foliage Tint")));
/*  84 */     this.foliageTintBox.m_94199_(7);
/*  85 */     this.foliageTintBox.m_94144_(toHex(this.config.foliageTint(this.group)));
/*  86 */     this.foliageTintBox.m_94151_(value -> applyHex(value, ()));
/*     */     
/*  88 */     y += 24;
/*  89 */     int groundRowY = y;
/*  90 */     this.groundTintBox = (EditBox)m_142416_((GuiEventListener)new EditBox(this.f_96547_, centerX - 60, y, 80, 20, 
/*  91 */           (Component)Component.m_237113_("Ground Tint")));
/*  92 */     this.groundTintBox.m_94199_(7);
/*  93 */     this.groundTintBox.m_94144_(toHex((this.config.entry(this.group)).groundTint));
/*  94 */     this.groundTintBox.m_94151_(value -> applyHex(value, ()));
/*     */ 
/*     */ 
/*     */ 
/*     */     
/*  99 */     this.groundTintEnabledCheckbox = (Checkbox)m_142416_((GuiEventListener)new Checkbox(this.groundTintBox
/* 100 */           .m_252754_() + this.groundTintBox.m_5711_() + 20 + 16, groundRowY, 60, 20, 
/* 101 */           (Component)Component.m_237113_("Tint"), this.config.groundTintEnabled(this.group))
/*     */         {
/*     */           public void m_5691_() {
/* 104 */             super.m_5691_();
/* 105 */             BiomeStyleScreen.this.config.setGroundTintEnabled(BiomeStyleScreen.this.group, m_93840_());
/*     */           }
/*     */         });
/*     */ 
/*     */ 
/*     */ 
/*     */     
/* 112 */     this.waterLabelY = trunkRowY + 6;
/* 113 */     this.waterColorBox = (EditBox)m_142416_((GuiEventListener)new EditBox(this.f_96547_, leftColumnX, foliageRowY, 80, 20, 
/* 114 */           (Component)Component.m_237113_("Water Color")));
/* 115 */     this.waterColorBox.m_94199_(7);
/* 116 */     this.waterColorBox.m_94144_(toHex(this.config.waterColor()));
/* 117 */     this.waterColorBox.m_94151_(value -> {
/*     */           Objects.requireNonNull(this.config); applyHex(value, this.config::setWaterColor);
/* 119 */         }); this.waterOpacityBox = (EditBox)m_142416_((GuiEventListener)new EditBox(this.f_96547_, leftColumnX, groundRowY, 80, 20, 
/* 120 */           (Component)Component.m_237113_("Water Opacity")));
/* 121 */     this.waterOpacityBox.m_94199_(3);
/* 122 */     this.waterOpacityBox.m_94144_(toPercent(this.config.waterAlpha()));
/* 123 */     this.waterOpacityBox.m_94151_(this::applyWaterOpacityPercent);
/*     */ 
/*     */ 
/*     */ 
/*     */     
/* 128 */     int rightColumnX = this.groundTintEnabledCheckbox.m_252754_() + this.groundTintEnabledCheckbox.m_5711_() + 20;
/* 129 */     this.snowHeightBox = (EditBox)m_142416_((GuiEventListener)new EditBox(this.f_96547_, rightColumnX, foliageRowY, 80, 20, 
/* 130 */           (Component)Component.m_237113_("Snow Height")));
/* 131 */     this.snowHeightBox.m_94199_(3);
/* 132 */     this.snowHeightBox.m_94144_(String.valueOf(Math.round(this.config.snowHeightPercent())));
/* 133 */     this.snowHeightBox.m_94151_(this::applySnowHeightPercent);
/*     */     
/* 135 */     y += 36;
/* 136 */     this.trunkTextureButton = (Button)m_142416_((GuiEventListener)Button.m_253074_(trunkTextureLabel(), b -> onTrunkTextureButton())
/* 137 */         .m_252987_(centerX - 100, y, 150, 20).m_253136_());
/* 138 */     m_142416_((GuiEventListener)Button.m_253074_((Component)Component.m_237113_("Clear"), b -> onClearTrunkTexture())
/* 139 */         .m_252987_(centerX + 55, y, 50, 20).m_253136_());
/*     */     
/* 141 */     y += 24;
/* 142 */     this.foliageTextureButton = (Button)m_142416_((GuiEventListener)Button.m_253074_(foliageTextureLabel(), b -> onFoliageTextureButton())
/* 143 */         .m_252987_(centerX - 100, y, 150, 20).m_253136_());
/* 144 */     m_142416_((GuiEventListener)Button.m_253074_((Component)Component.m_237113_("Clear"), b -> onClearFoliageTexture())
/* 145 */         .m_252987_(centerX + 55, y, 50, 20).m_253136_());
/*     */     
/* 147 */     y += 36;
/* 148 */     m_142416_((GuiEventListener)Button.m_253074_((Component)Component.m_237113_("Save"), b -> onSave())
/* 149 */         .m_252987_(centerX - 105, y, 100, 20).m_253136_());
/* 150 */     m_142416_((GuiEventListener)Button.m_253074_((Component)Component.m_237113_("Done"), b -> m_7379_())
/* 151 */         .m_252987_(centerX + 5, y, 100, 20).m_253136_());
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   public void m_88315_(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
/* 159 */     m_280273_(guiGraphics);
/* 160 */     super.m_88315_(guiGraphics, mouseX, mouseY, partialTick);
/* 161 */     guiGraphics.m_280653_(this.f_96547_, this.f_96539_, this.f_96543_ / 2, 15, 16777215);
/* 162 */     guiGraphics.m_280137_(this.f_96547_, titleCase(this.group.name()), this.f_96543_ / 2, 46, 16777215);
/*     */     
/* 164 */     drawSwatch(guiGraphics, this.trunkTintBox, this.config.trunkTint(this.group));
/* 165 */     drawSwatch(guiGraphics, this.foliageTintBox, this.config.foliageTint(this.group));
/* 166 */     drawSwatch(guiGraphics, this.groundTintBox, (this.config.entry(this.group)).groundTint);
/*     */     
/* 168 */     int waterColumnCenterX = this.waterColorBox.m_252754_() + this.waterColorBox.m_5711_() / 2;
/* 169 */     guiGraphics.m_280137_(this.f_96547_, "Water", waterColumnCenterX, this.waterLabelY, 16777215);
/* 170 */     drawSwatch(guiGraphics, this.waterColorBox, this.config.waterColor());
/*     */     
/* 172 */     int snowColumnCenterX = this.snowHeightBox.m_252754_() + this.snowHeightBox.m_5711_() / 2;
/* 173 */     guiGraphics.m_280137_(this.f_96547_, "Snow %", snowColumnCenterX, this.waterLabelY, 16777215);
/*     */   }
/*     */   
/*     */   private void drawSwatch(GuiGraphics guiGraphics, EditBox box, int rgb) {
/* 177 */     int x0 = box.m_252754_() + box.m_5711_() + 8;
/* 178 */     int y0 = box.m_252907_();
/* 179 */     guiGraphics.m_280509_(x0, y0, x0 + 20, y0 + box.m_93694_(), 0xFF000000 | rgb & 0xFFFFFF);
/*     */   }
/*     */   
/*     */   private void cycleGroup(int delta) {
/* 183 */     TreeStyle.Group[] groups = TreeStyle.Group.values();
/* 184 */     this.group = groups[Math.floorMod(this.group.ordinal() + delta, groups.length)];
/* 185 */     m_232761_();
/*     */   }
/*     */   
/*     */   private void onTrunkTextureButton() {
/* 189 */     pickPngFile(path -> {
/*     */           try {
/*     */             this.config.setTrunkTexture(this.group, path);
/* 192 */           } catch (IOException e) {
/*     */             Constants.LOG.error("Failed to set trunk texture for {}", this.group, e);
/*     */           } 
/*     */           this.trunkTextureButton.m_93666_(trunkTextureLabel());
/*     */         });
/*     */   }
/*     */   
/*     */   private void onFoliageTextureButton() {
/* 200 */     pickPngFile(path -> {
/*     */           try {
/*     */             this.config.setFoliageTexture(this.group, path);
/* 203 */           } catch (IOException e) {
/*     */             Constants.LOG.error("Failed to set foliage texture for {}", this.group, e);
/*     */           } 
/*     */           this.foliageTextureButton.m_93666_(foliageTextureLabel());
/*     */         });
/*     */   }
/*     */   
/*     */   private void onClearTrunkTexture() {
/* 211 */     this.config.clearTrunkTexture(this.group);
/* 212 */     this.trunkTextureButton.m_93666_(trunkTextureLabel());
/*     */   }
/*     */   
/*     */   private void onClearFoliageTexture() {
/* 216 */     this.config.clearFoliageTexture(this.group);
/* 217 */     this.foliageTextureButton.m_93666_(foliageTextureLabel());
/*     */   }
/*     */   
/*     */   private void onSave() {
/* 221 */     this.config.save();
/* 222 */     LodRenderer.rebuildAllMeshes();
/*     */   }
/*     */   
/*     */   private Component trunkTextureLabel() {
/* 226 */     return (Component)Component.m_237113_("Trunk: " + (((this.config.entry(this.group)).trunkTexture != null) ? "Custom" : "Default"));
/*     */   }
/*     */   
/*     */   private Component foliageTextureLabel() {
/* 230 */     return (Component)Component.m_237113_("Foliage: " + (((this.config.entry(this.group)).foliageTexture != null) ? "Custom" : "Default"));
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   private static void pickPngFile(Consumer<Path> onPicked) {
/* 240 */     Thread thread = new Thread(() -> { FileDialog dialog = new FileDialog((Frame)null, "Select PNG Texture", 0); dialog.setFile("*.png"); dialog.setVisible(true); String file = dialog.getFile(); String dir = dialog.getDirectory(); dialog.dispose(); if (file == null) return;  Path path = Paths.get(dir, new String[] { file }); Minecraft.m_91087_().execute(()); }"ecstatic-texture-picker");
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */     
/* 253 */     thread.setDaemon(true);
/* 254 */     thread.start();
/*     */   }
/*     */ 
/*     */   
/*     */   private void applyWaterOpacityPercent(String value) {
/*     */     try {
/* 260 */       float percent = Float.parseFloat(value.trim());
/* 261 */       this.config.setWaterAlpha(percent / 100.0F);
/* 262 */     } catch (NumberFormatException numberFormatException) {}
/*     */   }
/*     */ 
/*     */ 
/*     */   
/*     */   private static String toPercent(float alpha01) {
/* 268 */     return String.valueOf(Math.round(alpha01 * 100.0F));
/*     */   }
/*     */ 
/*     */   
/*     */   private void applySnowHeightPercent(String value) {
/*     */     try {
/* 274 */       float percent = Float.parseFloat(value.trim());
/* 275 */       this.config.setSnowHeightPercent(percent);
/* 276 */     } catch (NumberFormatException numberFormatException) {}
/*     */   }
/*     */ 
/*     */ 
/*     */   
/*     */   private static void applyHex(String value, IntConsumer setter) {
/* 282 */     Integer parsed = parseHex(value);
/* 283 */     if (parsed != null) {
/* 284 */       setter.accept(parsed.intValue());
/*     */     }
/*     */   }
/*     */   
/*     */   private static Integer parseHex(String value) {
/* 289 */     String trimmed = value.trim();
/* 290 */     if (trimmed.startsWith("#")) {
/* 291 */       trimmed = trimmed.substring(1);
/*     */     }
/* 293 */     if (trimmed.length() != 6) {
/* 294 */       return null;
/*     */     }
/*     */     try {
/* 297 */       return Integer.valueOf(Integer.parseInt(trimmed, 16) & 0xFFFFFF);
/* 298 */     } catch (NumberFormatException e) {
/* 299 */       return null;
/*     */     } 
/*     */   }
/*     */   
/*     */   private static String toHex(int rgb) {
/* 304 */     return String.format("%06X", new Object[] { Integer.valueOf(rgb & 0xFFFFFF) });
/*     */   }
/*     */   
/*     */   private static String titleCase(String enumName) {
/* 308 */     String[] words = enumName.toLowerCase(Locale.ROOT).split("_");
/* 309 */     StringBuilder result = new StringBuilder();
/* 310 */     for (String word : words) {
/* 311 */       if (result.length() > 0) {
/* 312 */         result.append(' ');
/*     */       }
/* 314 */       result.append(Character.toUpperCase(word.charAt(0))).append(word.substring(1));
/*     */     } 
/* 316 */     return result.toString();
/*     */   }
/*     */ }


/* Location:              C:\Users\Walker\Downloads\ecstatic-forge-1.20.1-1.3.0.jar!\com\angryalchemist\ecstatic\render\BiomeStyleScreen.class
 * Java compiler version: 17 (61.0)
 * JD-Core Version:       1.1.3
 */