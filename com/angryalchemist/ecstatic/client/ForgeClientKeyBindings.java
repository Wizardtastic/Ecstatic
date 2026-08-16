/*    */ package com.angryalchemist.ecstatic.client;
/*    */ 
/*    */ import com.mojang.blaze3d.platform.InputConstants;
/*    */ import net.minecraft.client.KeyMapping;
/*    */ import net.minecraftforge.api.distmarker.Dist;
/*    */ import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
/*    */ import net.minecraftforge.eventbus.api.SubscribeEvent;
/*    */ import net.minecraftforge.fml.common.Mod;
/*    */ import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ @EventBusSubscriber(modid = "ecstatic", value = {Dist.CLIENT}, bus = Mod.EventBusSubscriber.Bus.MOD)
/*    */ public final class ForgeClientKeyBindings
/*    */ {
/* 22 */   public static final KeyMapping CYCLE_VERTEX_FORMAT_KEY = new KeyMapping("key.ecstatic.cycle_vertex_format", InputConstants.Type.KEYSYM, 298, "key.categories.ecstatic");
/*    */   
/* 24 */   public static final KeyMapping CYCLE_FORCED_LOD_KEY = new KeyMapping("key.ecstatic.cycle_forced_lod", InputConstants.Type.KEYSYM, 299, "key.categories.ecstatic");
/*    */   
/* 26 */   public static final KeyMapping TOGGLE_REFERENCE_QUAD_KEY = new KeyMapping("key.ecstatic.toggle_reference_quad", InputConstants.Type.KEYSYM, 297, "key.categories.ecstatic");
/*    */   
/* 28 */   public static final KeyMapping COMPARE_SAMPLE_KEY = new KeyMapping("key.ecstatic.compare_sample", InputConstants.Type.KEYSYM, 296, "key.categories.ecstatic");
/*    */   
/* 30 */   public static final KeyMapping OPEN_BIOME_STYLE_KEY = new KeyMapping("key.ecstatic.open_biome_style", InputConstants.Type.KEYSYM, 295, "key.categories.ecstatic");
/*    */   
/* 32 */   public static final KeyMapping OPEN_SETTINGS_KEY = new KeyMapping("key.ecstatic.open_settings", InputConstants.Type.KEYSYM, 293, "key.categories.ecstatic");
/*    */ 
/*    */ 
/*    */ 
/*    */   
/*    */   @SubscribeEvent
/*    */   public static void onRegisterKeyMappings(RegisterKeyMappingsEvent event) {
/* 39 */     event.register(CYCLE_VERTEX_FORMAT_KEY);
/* 40 */     event.register(CYCLE_FORCED_LOD_KEY);
/* 41 */     event.register(TOGGLE_REFERENCE_QUAD_KEY);
/* 42 */     event.register(COMPARE_SAMPLE_KEY);
/* 43 */     event.register(OPEN_BIOME_STYLE_KEY);
/* 44 */     event.register(OPEN_SETTINGS_KEY);
/*    */   }
/*    */ }


/* Location:              C:\Users\Walker\Downloads\ecstatic-forge-1.20.1-1.3.0.jar!\com\angryalchemist\ecstatic\client\ForgeClientKeyBindings.class
 * Java compiler version: 17 (61.0)
 * JD-Core Version:       1.1.3
 */