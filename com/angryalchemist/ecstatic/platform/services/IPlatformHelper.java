/*    */ package com.angryalchemist.ecstatic.platform.services;
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ public interface IPlatformHelper
/*    */ {
/*    */   String getPlatformName();
/*    */   
/*    */   boolean isModLoaded(String paramString);
/*    */   
/*    */   boolean isDevelopmentEnvironment();
/*    */   
/*    */   default String getEnvironmentName() {
/* 34 */     return isDevelopmentEnvironment() ? "development" : "production";
/*    */   }
/*    */ }


/* Location:              C:\Users\Walker\Downloads\ecstatic-forge-1.20.1-1.3.0.jar!\com\angryalchemist\ecstatic\platform\services\IPlatformHelper.class
 * Java compiler version: 17 (61.0)
 * JD-Core Version:       1.1.3
 */