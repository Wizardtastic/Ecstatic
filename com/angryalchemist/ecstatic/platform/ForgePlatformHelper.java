/*    */ package com.angryalchemist.ecstatic.platform;
/*    */ 
/*    */ import com.angryalchemist.ecstatic.platform.services.IPlatformHelper;
/*    */ import net.minecraftforge.fml.ModList;
/*    */ import net.minecraftforge.fml.loading.FMLLoader;
/*    */ 
/*    */ 
/*    */ public class ForgePlatformHelper
/*    */   implements IPlatformHelper
/*    */ {
/*    */   public String getPlatformName() {
/* 12 */     return "Forge";
/*    */   }
/*    */ 
/*    */ 
/*    */   
/*    */   public boolean isModLoaded(String modId) {
/* 18 */     return ModList.get().isLoaded(modId);
/*    */   }
/*    */ 
/*    */ 
/*    */   
/*    */   public boolean isDevelopmentEnvironment() {
/* 24 */     return !FMLLoader.isProduction();
/*    */   }
/*    */ }


/* Location:              C:\Users\Walker\Downloads\ecstatic-forge-1.20.1-1.3.0.jar!\com\angryalchemist\ecstatic\platform\ForgePlatformHelper.class
 * Java compiler version: 17 (61.0)
 * JD-Core Version:       1.1.3
 */