/*    */ package com.angryalchemist.ecstatic.debug;
/*    */ 
/*    */ import com.angryalchemist.ecstatic.Constants;
/*    */ import com.angryalchemist.ecstatic.sample.SurfaceSample;
/*    */ import com.angryalchemist.ecstatic.sample.SurfaceSampler;
/*    */ import com.angryalchemist.ecstatic.storage.SavedChunkAccess;
/*    */ import net.minecraft.client.Minecraft;
/*    */ import net.minecraft.client.server.IntegratedServer;
/*    */ import net.minecraft.core.BlockPos;
/*    */ import net.minecraft.core.Holder;
/*    */ import net.minecraft.core.registries.Registries;
/*    */ import net.minecraft.server.MinecraftServer;
/*    */ import net.minecraft.server.level.ServerLevel;
/*    */ import net.minecraft.world.level.biome.Biome;
/*    */ import net.minecraft.world.level.block.state.BlockState;
/*    */ import net.minecraft.world.level.levelgen.Heightmap;
/*    */ import net.minecraft.world.level.storage.LevelResource;
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
/*    */ public final class LodDebugCompare
/*    */ {
/*    */   public static void compareAtPlayer(Minecraft client) {
/* 37 */     IntegratedServer integratedServer = client.m_91092_();
/* 38 */     if (integratedServer == null || client.f_91073_ == null || client.f_91074_ == null) {
/* 39 */       LodDebugCommon.sendMessage(client, "compare: not in a singleplayer world");
/*    */       return;
/*    */     } 
/* 42 */     ServerLevel level = integratedServer.m_129880_(client.f_91073_.m_46472_());
/* 43 */     if (level == null) {
/* 44 */       LodDebugCommon.sendMessage(client, "compare: no matching server level");
/*    */       
/*    */       return;
/*    */     } 
/* 48 */     int blockX = client.f_91074_.m_146903_();
/* 49 */     int blockZ = client.f_91074_.m_146907_();
/*    */     
/* 51 */     SurfaceSample sampled = SurfaceSampler.sample(level, blockX, blockZ);
/*    */     
/* 53 */     int realHeight = level.m_6924_(Heightmap.Types.OCEAN_FLOOR, blockX, blockZ);
/* 54 */     Holder<Biome> realBiomeHolder = level.m_204166_(new BlockPos(blockX, realHeight, blockZ));
/* 55 */     int realBiomeId = level.m_9598_().m_175515_(Registries.f_256952_).m_7447_(realBiomeHolder.m_203334_());
/*    */     
/* 57 */     String message = String.format("compare @ (%d, %d): sampled height=%d biome=%d | real height=%d biome=%d | delta=%d", new Object[] {
/*    */           
/* 59 */           Integer.valueOf(blockX), Integer.valueOf(blockZ), Integer.valueOf(sampled.height()), Integer.valueOf(sampled.biomeRawId()), Integer.valueOf(realHeight), Integer.valueOf(realBiomeId), 
/* 60 */           Integer.valueOf(sampled.height() - realHeight) });
/* 61 */     Constants.LOG.info("Ecstatic debug {}", message);
/* 62 */     LodDebugCommon.sendMessage(client, message);
/*    */     
/* 64 */     LodDebugCommon.sendMessage(client, savedChunkMessage((MinecraftServer)integratedServer, level, blockX, blockZ, realHeight));
/*    */   }
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
/*    */   private static String savedChunkMessage(MinecraftServer server, ServerLevel level, int blockX, int blockZ, int realHeight) {
/*    */     
/* 82 */     try { SavedChunkAccess savedChunks = new SavedChunkAccess(server.m_129843_(LevelResource.f_78182_), level.m_9598_().m_175515_(Registries.f_256952_)); 
/* 83 */       try { if (!savedChunks.hasChunk(blockX >> 4, blockZ >> 4))
/* 84 */         { String str1 = "compare (saved): chunk not found on disk (unexpected - player is standing in it)";
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
/* 97 */           savedChunks.close(); return str1; }  int minY = level.m_141937_(); int scanFrom = Math.min(realHeight + 4, level.m_151558_() - 1); for (int y = scanFrom; y >= minY; y--) { BlockState state = savedChunks.blockAt(blockX, y, blockZ); if (!state.m_60795_()) { String str1 = String.format("compare (saved) @ (%d, %d): real top block=%s at y=%d", new Object[] { Integer.valueOf(blockX), Integer.valueOf(blockZ), state.m_60734_().m_7705_(), Integer.valueOf(y) }); savedChunks.close(); return str1; }  }  String str = String.format("compare (saved) @ (%d, %d): all air from y=%d down to %d", new Object[] { Integer.valueOf(blockX), Integer.valueOf(blockZ), Integer.valueOf(scanFrom), Integer.valueOf(minY) }); savedChunks.close(); return str; } catch (Throwable throwable) { try { savedChunks.close(); } catch (Throwable throwable1) { throwable.addSuppressed(throwable1); }  throw throwable; }  } catch (RuntimeException e)
/* 98 */     { Constants.LOG.warn("Ecstatic debug: saved-chunk read failed", e);
/* 99 */       return "compare (saved): read failed, see log"; }
/*    */   
/*    */   }
/*    */ }


/* Location:              C:\Users\Walker\Downloads\ecstatic-forge-1.20.1-1.3.0.jar!\com\angryalchemist\ecstatic\debug\LodDebugCompare.class
 * Java compiler version: 17 (61.0)
 * JD-Core Version:       1.1.3
 */