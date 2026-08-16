/*    */ package com.angryalchemist.ecstatic.debug;
/*    */ 
/*    */ import net.minecraft.client.Minecraft;
/*    */ import net.minecraft.network.chat.Component;
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ public final class LodDebugCommon
/*    */ {
/*    */   public static void sendMessage(Minecraft client, String message) {
/* 12 */     if (client.f_91074_ != null)
/* 13 */       client.f_91074_.m_5661_((Component)Component.m_237113_("[Ecstatic] " + message), false); 
/*    */   }
/*    */ }


/* Location:              C:\Users\Walker\Downloads\ecstatic-forge-1.20.1-1.3.0.jar!\com\angryalchemist\ecstatic\debug\LodDebugCommon.class
 * Java compiler version: 17 (61.0)
 * JD-Core Version:       1.1.3
 */