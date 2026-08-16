/*     */ package com.angryalchemist.ecstatic.client;
/*     */ 
/*     */ import com.angryalchemist.ecstatic.debug.LodDebugCommon;
/*     */ import com.angryalchemist.ecstatic.debug.LodDebugCompare;
/*     */ import com.angryalchemist.ecstatic.debug.LodDebugState;
/*     */ import com.angryalchemist.ecstatic.render.BiomeStyleScreen;
/*     */ import com.angryalchemist.ecstatic.render.LodRenderer;
/*     */ import com.angryalchemist.ecstatic.render.LodSettingsScreen;
/*     */ import net.minecraft.client.Camera;
/*     */ import net.minecraft.client.Minecraft;
/*     */ import net.minecraft.client.gui.screens.Screen;
/*     */ import net.minecraft.client.renderer.FogRenderer;
/*     */ import net.minecraft.util.Mth;
/*     */ import net.minecraft.world.effect.MobEffects;
/*     */ import net.minecraft.world.entity.Entity;
/*     */ import net.minecraft.world.entity.LivingEntity;
/*     */ import net.minecraft.world.level.material.FogType;
/*     */ import net.minecraft.world.phys.Vec3;
/*     */ import net.minecraftforge.api.distmarker.Dist;
/*     */ import net.minecraftforge.client.event.RenderLevelStageEvent;
/*     */ import net.minecraftforge.client.event.ViewportEvent;
/*     */ import net.minecraftforge.event.TickEvent;
/*     */ import net.minecraftforge.eventbus.api.SubscribeEvent;
/*     */ import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
/*     */ import org.joml.Matrix4f;
/*     */ import org.joml.Matrix4fc;
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ @EventBusSubscriber(modid = "ecstatic", value = {Dist.CLIENT})
/*     */ public final class ForgeClientRenderEvents
/*     */ {
/*     */   private static final float FOG_DISABLE_MULTIPLIER = 100.0F;
/*     */   
/*     */   @SubscribeEvent
/*     */   public static void onRenderLevelStage(RenderLevelStageEvent event) {
/*  64 */     if (event.getStage() == RenderLevelStageEvent.Stage.AFTER_PARTICLES) {
/*  65 */       LodRenderer.captureTrueRotation(new Matrix4f((Matrix4fc)event.getPoseStack().m_85850_().m_252922_()));
/*  66 */       LodRenderer.render(event.getProjectionMatrix(), event.getCamera());
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
/*     */   @SubscribeEvent
/*     */   public static void onRenderFog(ViewportEvent.RenderFog event) {
/*  95 */     if (event.getMode() != FogRenderer.FogMode.FOG_TERRAIN || event.getType() != FogType.NONE) {
/*     */       return;
/*     */     }
/*  98 */     if (!LodRenderer.isOverworldLoaded()) {
/*     */       return;
/*     */     }
/* 101 */     Camera camera = event.getCamera();
/* 102 */     Entity entity = camera.m_90592_();
/* 103 */     if (entity instanceof LivingEntity) { LivingEntity living = (LivingEntity)entity; if (living
/* 104 */         .m_21023_(MobEffects.f_19610_) || living.m_21023_(MobEffects.f_216964_))
/*     */         return;  }
/*     */     
/* 107 */     Minecraft client = Minecraft.m_91087_();
/* 108 */     Vec3 pos = camera.m_90583_();
/*     */     
/* 110 */     boolean isFoggy = (client.f_91073_.m_104583_().m_5781_(Mth.m_14107_(pos.f_82479_), Mth.m_14107_(pos.f_82480_)) || client.f_91065_.m_93090_().m_93715_());
/* 111 */     if (isFoggy) {
/*     */       return;
/*     */     }
/* 114 */     float farPlaneDistance = event.getFarPlaneDistance();
/* 115 */     event.setNearPlaneDistance(farPlaneDistance * 100.0F);
/* 116 */     event.setFarPlaneDistance(farPlaneDistance * 100.0F * 2.0F);
/* 117 */     event.setCanceled(true);
/*     */   }
/*     */   
/*     */   @SubscribeEvent
/*     */   public static void onClientTick(TickEvent.ClientTickEvent event) {
/* 122 */     if (event.phase != TickEvent.Phase.END) {
/*     */       return;
/*     */     }
/* 125 */     Minecraft client = Minecraft.m_91087_();
/*     */ 
/*     */ 
/*     */ 
/*     */     
/* 130 */     boolean debugTools = LodDebugState.isEnabled();
/* 131 */     while (ForgeClientKeyBindings.CYCLE_VERTEX_FORMAT_KEY.m_90859_()) {
/* 132 */       if (!debugTools) {
/*     */         continue;
/*     */       }
/* 135 */       int override = LodDebugState.cycleVertexFormatOverride();
/* 136 */       LodDebugCommon.sendMessage(client, "vertex format override: " + vertexFormatOverrideLabel(override));
/* 137 */       LodRenderer.rebuildAllMeshes();
/*     */     } 
/* 139 */     while (ForgeClientKeyBindings.CYCLE_FORCED_LOD_KEY.m_90859_()) {
/* 140 */       if (!debugTools) {
/*     */         continue;
/*     */       }
/* 143 */       int level = LodDebugState.cycleForcedLevel();
/* 144 */       LodDebugCommon.sendMessage(client, "forced level: " + ((level == 0) ? "off (LOD1-4)" : ("LOD" + level + " only")));
/*     */     } 
/* 146 */     while (ForgeClientKeyBindings.TOGGLE_REFERENCE_QUAD_KEY.m_90859_()) {
/* 147 */       if (!debugTools) {
/*     */         continue;
/*     */       }
/* 150 */       boolean enabled = LodDebugState.toggleReferenceQuad();
/* 151 */       LodDebugCommon.sendMessage(client, "debug reference quad: " + (enabled ? "on (look down)" : "off"));
/*     */     } 
/* 153 */     while (ForgeClientKeyBindings.COMPARE_SAMPLE_KEY.m_90859_()) {
/* 154 */       if (!debugTools) {
/*     */         continue;
/*     */       }
/* 157 */       LodDebugCompare.compareAtPlayer(client);
/*     */     } 
/* 159 */     while (ForgeClientKeyBindings.OPEN_BIOME_STYLE_KEY.m_90859_()) {
/* 160 */       if (client.f_91080_ == null) {
/* 161 */         client.m_91152_((Screen)new BiomeStyleScreen());
/*     */       }
/*     */     } 
/* 164 */     while (ForgeClientKeyBindings.OPEN_SETTINGS_KEY.m_90859_()) {
/* 165 */       if (client.f_91080_ == null) {
/* 166 */         client.m_91152_((Screen)new LodSettingsScreen());
/*     */       }
/*     */     } 
/*     */   }
/*     */ 
/*     */   
/*     */   static String vertexFormatOverrideLabel(int override) {
/* 173 */     switch (override) { case 1: case 2:  }  return 
/*     */ 
/*     */       
/* 176 */       "auto (config default / forced LIT if a shaderpack is active)";
/*     */   }
/*     */ }


/* Location:              C:\Users\Walker\Downloads\ecstatic-forge-1.20.1-1.3.0.jar!\com\angryalchemist\ecstatic\client\ForgeClientRenderEvents.class
 * Java compiler version: 17 (61.0)
 * JD-Core Version:       1.1.3
 */