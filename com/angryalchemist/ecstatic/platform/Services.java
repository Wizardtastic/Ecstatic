/*    */ package com.angryalchemist.ecstatic.platform;
/*    */ 
/*    */ import com.angryalchemist.ecstatic.Constants;
/*    */ import com.angryalchemist.ecstatic.platform.services.IPlatformHelper;
/*    */ import java.util.ServiceLoader;
/*    */ 
/*    */ 
/*    */ public class Services
/*    */ {
/* 10 */   public static final IPlatformHelper PLATFORM = load(IPlatformHelper.class);
/*    */ 
/*    */ 
/*    */   
/*    */   public static <T> T load(Class<T> clazz) {
/* 15 */     T loadedService = (T)ServiceLoader.<T>load(clazz).findFirst().orElseThrow(() -> new NullPointerException("Failed to load service for " + clazz.getName()));
/* 16 */     Constants.LOG.debug("Loaded {} for service {}", loadedService, clazz);
/* 17 */     return loadedService;
/*    */   }
/*    */ }


/* Location:              C:\Users\Walker\Downloads\ecstatic-forge-1.20.1-1.3.0.jar!\com\angryalchemist\ecstatic\platform\Services.class
 * Java compiler version: 17 (61.0)
 * JD-Core Version:       1.1.3
 */