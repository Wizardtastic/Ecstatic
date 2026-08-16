/*    */ package com.angryalchemist.ecstatic;
/*    */ 
/*    */ import net.minecraftforge.common.MinecraftForge;
/*    */ import net.minecraftforge.event.server.ServerStartedEvent;
/*    */ import net.minecraftforge.event.server.ServerStoppingEvent;
/*    */ import net.minecraftforge.eventbus.api.SubscribeEvent;
/*    */ import net.minecraftforge.fml.common.Mod;
/*    */ 
/*    */ @Mod("ecstatic")
/*    */ public class EcstaticForge
/*    */ {
/*    */   public EcstaticForge() {
/* 13 */     Ecstatic.init();
/* 14 */     MinecraftForge.EVENT_BUS.register(this);
/*    */   }
/*    */   
/*    */   @SubscribeEvent
/*    */   public void onServerStarted(ServerStartedEvent event) {
/* 19 */     Ecstatic.onServerStarted(event.getServer().m_129783_());
/*    */   }
/*    */   
/*    */   @SubscribeEvent
/*    */   public void onServerStopping(ServerStoppingEvent event) {
/* 24 */     Ecstatic.onServerStopping(event.getServer());
/*    */   }
/*    */ }


/* Location:              C:\Users\Walker\Downloads\ecstatic-forge-1.20.1-1.3.0.jar!\com\angryalchemist\ecstatic\EcstaticForge.class
 * Java compiler version: 17 (61.0)
 * JD-Core Version:       1.1.3
 */