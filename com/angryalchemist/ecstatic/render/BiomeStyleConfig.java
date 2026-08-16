/*     */ package com.angryalchemist.ecstatic.render;
/*     */ 
/*     */ import com.angryalchemist.ecstatic.Constants;
/*     */ import com.google.gson.Gson;
/*     */ import com.google.gson.GsonBuilder;
/*     */ import com.google.gson.JsonElement;
/*     */ import com.google.gson.JsonObject;
/*     */ import com.mojang.blaze3d.platform.NativeImage;
/*     */ import java.io.IOException;
/*     */ import java.io.InputStream;
/*     */ import java.io.Reader;
/*     */ import java.io.Writer;
/*     */ import java.nio.file.CopyOption;
/*     */ import java.nio.file.Files;
/*     */ import java.nio.file.Path;
/*     */ import java.nio.file.StandardCopyOption;
/*     */ import java.nio.file.attribute.FileAttribute;
/*     */ import java.util.EnumMap;
/*     */ import java.util.LinkedHashMap;
/*     */ import java.util.Locale;
/*     */ import java.util.Map;
/*     */ import net.minecraft.client.Minecraft;
/*     */ import net.minecraft.client.renderer.texture.AbstractTexture;
/*     */ import net.minecraft.client.renderer.texture.DynamicTexture;
/*     */ import net.minecraft.resources.ResourceLocation;
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ final class BiomeStyleConfig
/*     */ {
/*     */   private static final String WATER_KEY = "WATER";
/*     */   private static final int DEFAULT_WATER_COLOR = 2312061;
/*     */   private static final float DEFAULT_WATER_ALPHA = 0.7F;
/*     */   private static final String SNOW_KEY = "SNOW";
/*     */   private static final float DEFAULT_SNOW_HEIGHT_PERCENT = 48.0F;
/*     */   private static final String ICE_KEY = "ICE";
/*     */   private static final int DEFAULT_ICE_COLOR = 11062752;
/*     */   
/*     */   static final class Entry
/*     */   {
/*     */     int trunkTint;
/*     */     int foliageTint;
/*  46 */     int groundTint = 16777215;
/*     */ 
/*     */ 
/*     */     
/*     */     boolean groundTintEnabled = true;
/*     */ 
/*     */ 
/*     */     
/*     */     String trunkTexture;
/*     */ 
/*     */ 
/*     */     
/*     */     String foliageTexture;
/*     */ 
/*     */ 
/*     */ 
/*     */     
/*     */     Entry() {}
/*     */ 
/*     */ 
/*     */ 
/*     */     
/*     */     Entry(int trunkTint, int foliageTint) {
/*  69 */       this.trunkTint = trunkTint;
/*  70 */       this.foliageTint = foliageTint;
/*     */     }
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   static final class WaterEntry
/*     */   {
/*     */     int color;
/*     */ 
/*     */     
/*     */     float alpha;
/*     */ 
/*     */ 
/*     */     
/*     */     WaterEntry(int color, float alpha) {
/*  87 */       this.color = color;
/*  88 */       this.alpha = alpha;
/*     */     }
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   static final class SnowEntry
/*     */   {
/*     */     float heightPercent;
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */     
/*     */     SnowEntry(float heightPercent) {
/* 107 */       this.heightPercent = heightPercent;
/*     */     }
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   static final class IceEntry
/*     */   {
/*     */     int color;
/*     */ 
/*     */ 
/*     */ 
/*     */     
/*     */     IceEntry(int color) {
/* 122 */       this.color = color;
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
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */   
/* 160 */   private static final Gson GSON = (new GsonBuilder()).setPrettyPrinting().create();
/*     */   
/*     */   private static BiomeStyleConfig instance;
/*     */   
/* 164 */   private final Map<TreeStyle.Group, Entry> entries = new EnumMap<>(TreeStyle.Group.class);
/* 165 */   private final Map<TreeStyle.Group, DynamicTexture> trunkTextures = new EnumMap<>(TreeStyle.Group.class);
/* 166 */   private final Map<TreeStyle.Group, DynamicTexture> foliageTextures = new EnumMap<>(TreeStyle.Group.class);
/* 167 */   private final Map<TreeStyle.Group, ResourceLocation> trunkTextureIds = new EnumMap<>(TreeStyle.Group.class);
/* 168 */   private final Map<TreeStyle.Group, ResourceLocation> foliageTextureIds = new EnumMap<>(TreeStyle.Group.class);
/* 169 */   private WaterEntry water = new WaterEntry(2312061, 0.7F);
/* 170 */   private SnowEntry snow = new SnowEntry(48.0F);
/* 171 */   private IceEntry ice = new IceEntry(11062752);
/*     */   
/*     */   private BiomeStyleConfig() {
/* 174 */     for (TreeStyle.Group group : TreeStyle.Group.values()) {
/* 175 */       TreeStyle style = TreeStyle.forGroup(group);
/* 176 */       Entry entry = new Entry(style.trunkColor, style.foliageColor);
/* 177 */       if (group == TreeStyle.Group.BEACH)
/*     */       {
/*     */         
/* 180 */         entry.groundTintEnabled = false;
/*     */       }
/* 182 */       this.entries.put(group, entry);
/*     */     } 
/* 184 */     load();
/* 185 */     for (TreeStyle.Group group : TreeStyle.Group.values()) {
/* 186 */       reloadTextures(group);
/*     */     }
/*     */   }
/*     */   
/*     */   static synchronized BiomeStyleConfig get() {
/* 191 */     if (instance == null) {
/* 192 */       instance = new BiomeStyleConfig();
/*     */     }
/* 194 */     return instance;
/*     */   }
/*     */   
/*     */   Entry entry(TreeStyle.Group group) {
/* 198 */     return this.entries.get(group);
/*     */   }
/*     */   
/*     */   int trunkTint(TreeStyle.Group group) {
/* 202 */     return ((Entry)this.entries.get(group)).trunkTint;
/*     */   }
/*     */   
/*     */   int foliageTint(TreeStyle.Group group) {
/* 206 */     return ((Entry)this.entries.get(group)).foliageTint;
/*     */   }
/*     */ 
/*     */   
/*     */   boolean groundTintEnabled(TreeStyle.Group group) {
/* 211 */     return ((Entry)this.entries.get(group)).groundTintEnabled;
/*     */   }
/*     */   
/*     */   void setGroundTintEnabled(TreeStyle.Group group, boolean enabled) {
/* 215 */     ((Entry)this.entries.get(group)).groundTintEnabled = enabled;
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   int applyGroundTint(TreeStyle.Group group, int baseColor) {
/* 223 */     Entry entry = this.entries.get(group);
/* 224 */     int tint = entry.groundTint;
/* 225 */     if (!entry.groundTintEnabled || tint == 16777215) {
/* 226 */       return baseColor;
/*     */     }
/* 228 */     int br = baseColor >> 16 & 0xFF;
/* 229 */     int bg = baseColor >> 8 & 0xFF;
/* 230 */     int bb = baseColor & 0xFF;
/* 231 */     int tr = tint >> 16 & 0xFF;
/* 232 */     int tg = tint >> 8 & 0xFF;
/* 233 */     int tb = tint & 0xFF;
/* 234 */     int r = br * tr / 255;
/* 235 */     int g = bg * tg / 255;
/* 236 */     int b = bb * tb / 255;
/* 237 */     return r << 16 | g << 8 | b;
/*     */   }
/*     */   
/*     */   int waterColor() {
/* 241 */     return this.water.color;
/*     */   }
/*     */   
/*     */   float waterAlpha() {
/* 245 */     return this.water.alpha;
/*     */   }
/*     */   
/*     */   void setWaterColor(int rgb) {
/* 249 */     this.water.color = rgb;
/*     */   }
/*     */   
/*     */   void setWaterAlpha(float alpha) {
/* 253 */     this.water.alpha = Math.max(0.0F, Math.min(1.0F, alpha));
/*     */   }
/*     */ 
/*     */   
/*     */   float snowHeightPercent() {
/* 258 */     return this.snow.heightPercent;
/*     */   }
/*     */   
/*     */   void setSnowHeightPercent(float percent) {
/* 262 */     this.snow.heightPercent = Math.max(0.0F, Math.min(100.0F, percent));
/*     */   }
/*     */   
/*     */   int iceColor() {
/* 266 */     return this.ice.color;
/*     */   }
/*     */   
/*     */   void setIceColor(int rgb) {
/* 270 */     this.ice.color = rgb;
/*     */   }
/*     */ 
/*     */   
/*     */   ResourceLocation trunkTextureId(TreeStyle.Group group) {
/* 275 */     return this.trunkTextureIds.get(group);
/*     */   }
/*     */   
/*     */   ResourceLocation foliageTextureId(TreeStyle.Group group) {
/* 279 */     return this.foliageTextureIds.get(group);
/*     */   }
/*     */ 
/*     */   
/*     */   void setTrunkTexture(TreeStyle.Group group, Path sourceFile) throws IOException {
/* 284 */     String fileName = group.name().toLowerCase(Locale.ROOT) + "_trunk.png";
/* 285 */     Files.createDirectories(texturesDir(), (FileAttribute<?>[])new FileAttribute[0]);
/* 286 */     Files.copy(sourceFile, texturesDir().resolve(fileName), new CopyOption[] { StandardCopyOption.REPLACE_EXISTING });
/* 287 */     ((Entry)this.entries.get(group)).trunkTexture = fileName;
/* 288 */     reloadTextures(group);
/*     */   }
/*     */   
/*     */   void setFoliageTexture(TreeStyle.Group group, Path sourceFile) throws IOException {
/* 292 */     String fileName = group.name().toLowerCase(Locale.ROOT) + "_foliage.png";
/* 293 */     Files.createDirectories(texturesDir(), (FileAttribute<?>[])new FileAttribute[0]);
/* 294 */     Files.copy(sourceFile, texturesDir().resolve(fileName), new CopyOption[] { StandardCopyOption.REPLACE_EXISTING });
/* 295 */     ((Entry)this.entries.get(group)).foliageTexture = fileName;
/* 296 */     reloadTextures(group);
/*     */   }
/*     */   
/*     */   void clearTrunkTexture(TreeStyle.Group group) {
/* 300 */     ((Entry)this.entries.get(group)).trunkTexture = null;
/* 301 */     reloadTextures(group);
/*     */   }
/*     */   
/*     */   void clearFoliageTexture(TreeStyle.Group group) {
/* 305 */     ((Entry)this.entries.get(group)).foliageTexture = null;
/* 306 */     reloadTextures(group);
/*     */   }
/*     */   
/*     */   void save() {
/*     */     
/* 311 */     try { Files.createDirectories(configDir(), (FileAttribute<?>[])new FileAttribute[0]);
/* 312 */       Writer writer = Files.newBufferedWriter(configFile(), new java.nio.file.OpenOption[0]); 
/* 313 */       try { GSON.toJson(toNameKeyedMap(), writer);
/* 314 */         if (writer != null) writer.close();  } catch (Throwable throwable) { if (writer != null)
/* 315 */           try { writer.close(); } catch (Throwable throwable1) { throwable.addSuppressed(throwable1); }   throw throwable; }  } catch (IOException e)
/* 316 */     { Constants.LOG.error("Failed to save biome style config", e); }
/*     */   
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
/*     */   private void load() {
/* 330 */     Path file = configFile();
/* 331 */     if (!Files.exists(file, new java.nio.file.LinkOption[0]))
/*     */       return; 
/*     */     
/* 334 */     try { Reader reader = Files.newBufferedReader(file); 
/* 335 */       try { JsonObject root = (JsonObject)GSON.fromJson(reader, JsonObject.class);
/* 336 */         if (root == null)
/*     */         
/*     */         { 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */           
/* 371 */           if (reader != null) reader.close();  return; }  for (Map.Entry<String, JsonElement> jsonEntry : (Iterable<Map.Entry<String, JsonElement>>)root.entrySet()) { if (((String)jsonEntry.getKey()).equals("WATER")) { WaterEntry loadedWater = (WaterEntry)GSON.fromJson(jsonEntry.getValue(), WaterEntry.class); if (loadedWater != null) this.water = loadedWater;  continue; }  if (((String)jsonEntry.getKey()).equals("SNOW")) { SnowEntry loadedSnow = (SnowEntry)GSON.fromJson(jsonEntry.getValue(), SnowEntry.class); if (loadedSnow != null) this.snow = loadedSnow;  continue; }  if (((String)jsonEntry.getKey()).equals("ICE")) { IceEntry loadedIce = (IceEntry)GSON.fromJson(jsonEntry.getValue(), IceEntry.class); if (loadedIce != null) this.ice = loadedIce;  continue; }  try { TreeStyle.Group group = TreeStyle.Group.valueOf(jsonEntry.getKey()); Entry loadedEntry = (Entry)GSON.fromJson(jsonEntry.getValue(), Entry.class); if (loadedEntry != null) this.entries.put(group, loadedEntry);  } catch (IllegalArgumentException illegalArgumentException) {} }  if (reader != null) reader.close();  } catch (Throwable throwable) { if (reader != null) try { reader.close(); } catch (Throwable throwable1) { throwable.addSuppressed(throwable1); }   throw throwable; }  } catch (IOException e)
/* 372 */     { Constants.LOG.error("Failed to load biome style config", e); }
/*     */   
/*     */   }
/*     */   
/*     */   private Map<String, Object> toNameKeyedMap() {
/* 377 */     Map<String, Object> out = new LinkedHashMap<>();
/* 378 */     for (Map.Entry<TreeStyle.Group, Entry> e : this.entries.entrySet()) {
/* 379 */       out.put(((TreeStyle.Group)e.getKey()).name(), e.getValue());
/*     */     }
/* 381 */     out.put("WATER", this.water);
/* 382 */     out.put("SNOW", this.snow);
/* 383 */     out.put("ICE", this.ice);
/* 384 */     return out;
/*     */   }
/*     */   
/*     */   private void reloadTextures(TreeStyle.Group group) {
/* 388 */     this.trunkTextureIds.put(group, loadTexture(group, "trunk", ((Entry)this.entries.get(group)).trunkTexture, this.trunkTextures));
/* 389 */     this.foliageTextureIds.put(group, loadTexture(group, "foliage", ((Entry)this.entries.get(group)).foliageTexture, this.foliageTextures));
/*     */   }
/*     */ 
/*     */   
/*     */   private ResourceLocation loadTexture(TreeStyle.Group group, String part, String fileName, Map<TreeStyle.Group, DynamicTexture> cache) {
/* 394 */     DynamicTexture previous = cache.remove(group);
/* 395 */     if (previous != null) {
/* 396 */       previous.close();
/*     */     }
/* 398 */     if (fileName == null) {
/* 399 */       return null;
/*     */     }
/* 401 */     Path path = texturesDir().resolve(fileName);
/* 402 */     if (!Files.exists(path, new java.nio.file.LinkOption[0]))
/* 403 */       return null; 
/*     */     
/* 405 */     try { InputStream in = Files.newInputStream(path, new java.nio.file.OpenOption[0]); 
/* 406 */       try { NativeImage image = NativeImage.m_85058_(in);
/* 407 */         DynamicTexture texture = new DynamicTexture(image);
/*     */         
/* 409 */         ResourceLocation id = new ResourceLocation("ecstatic", "biome_style/" + group.name().toLowerCase(Locale.ROOT) + "_" + part);
/* 410 */         Minecraft.m_91087_().m_91097_().m_118495_(id, (AbstractTexture)texture);
/* 411 */         cache.put(group, texture);
/* 412 */         ResourceLocation resourceLocation1 = id;
/* 413 */         if (in != null) in.close();  return resourceLocation1; } catch (Throwable throwable) { if (in != null) try { in.close(); } catch (Throwable throwable1) { throwable.addSuppressed(throwable1); }   throw throwable; }  } catch (IOException e)
/* 414 */     { Constants.LOG.error("Failed to load {} texture for {}", new Object[] { part, group, e });
/* 415 */       return null; }
/*     */   
/*     */   }
/*     */   
/*     */   private Path configDir() {
/* 420 */     return (Minecraft.m_91087_()).f_91069_.toPath().resolve("config").resolve("ecstatic");
/*     */   }
/*     */   
/*     */   private Path texturesDir() {
/* 424 */     return configDir().resolve("textures");
/*     */   }
/*     */   
/*     */   private Path configFile() {
/* 428 */     return configDir().resolve("biome_styles.json");
/*     */   }
/*     */ }


/* Location:              C:\Users\Walker\Downloads\ecstatic-forge-1.20.1-1.3.0.jar!\com\angryalchemist\ecstatic\render\BiomeStyleConfig.class
 * Java compiler version: 17 (61.0)
 * JD-Core Version:       1.1.3
 */