/*    */ package com.angryalchemist.ecstatic.lod;
/*    */ public final class RegionCoord extends Record { private final int x; private final int z; public static final int SIZE_BLOCKS = 512; public static final int SIZE_CHUNKS = 32; public final String toString() {
/*    */     // Byte code:
/*    */     //   0: aload_0
/*    */     //   1: <illegal opcode> toString : (Lcom/angryalchemist/ecstatic/lod/RegionCoord;)Ljava/lang/String;
/*    */     //   6: areturn
/*    */     // Line number table:
/*    */     //   Java source line number -> byte code offset
/*    */     //   #7	-> 0
/*    */     // Local variable table:
/*    */     //   start	length	slot	name	descriptor
/*    */     //   0	7	0	this	Lcom/angryalchemist/ecstatic/lod/RegionCoord;
/*    */   } public final int hashCode() {
/*    */     // Byte code:
/*    */     //   0: aload_0
/*    */     //   1: <illegal opcode> hashCode : (Lcom/angryalchemist/ecstatic/lod/RegionCoord;)I
/*    */     //   6: ireturn
/*    */     // Line number table:
/*    */     //   Java source line number -> byte code offset
/*    */     //   #7	-> 0
/*    */     // Local variable table:
/*    */     //   start	length	slot	name	descriptor
/*    */     //   0	7	0	this	Lcom/angryalchemist/ecstatic/lod/RegionCoord;
/*    */   }
/*  7 */   public RegionCoord(int x, int z) { this.x = x; this.z = z; } public final boolean equals(Object o) { // Byte code:
/*    */     //   0: aload_0
/*    */     //   1: aload_1
/*    */     //   2: <illegal opcode> equals : (Lcom/angryalchemist/ecstatic/lod/RegionCoord;Ljava/lang/Object;)Z
/*    */     //   7: ireturn
/*    */     // Line number table:
/*    */     //   Java source line number -> byte code offset
/*    */     //   #7	-> 0
/*    */     // Local variable table:
/*    */     //   start	length	slot	name	descriptor
/*    */     //   0	8	0	this	Lcom/angryalchemist/ecstatic/lod/RegionCoord;
/*  7 */     //   0	8	1	o	Ljava/lang/Object; } public int x() { return this.x; } public int z() { return this.z; }
/*    */ 
/*    */ 
/*    */ 
/*    */   
/*    */   public static RegionCoord fromBlock(int blockX, int blockZ) {
/* 13 */     return new RegionCoord(Math.floorDiv(blockX, 512), Math.floorDiv(blockZ, 512));
/*    */   }
/*    */   
/*    */   public static RegionCoord fromChunk(int chunkX, int chunkZ) {
/* 17 */     return new RegionCoord(Math.floorDiv(chunkX, 32), Math.floorDiv(chunkZ, 32));
/*    */   }
/*    */   
/*    */   public int originBlockX() {
/* 21 */     return this.x * 512;
/*    */   }
/*    */   
/*    */   public int originBlockZ() {
/* 25 */     return this.z * 512;
/*    */   }
/*    */   
/*    */   public int originChunkX() {
/* 29 */     return this.x * 32;
/*    */   }
/*    */   
/*    */   public int originChunkZ() {
/* 33 */     return this.z * 32;
/*    */   }
/*    */ 
/*    */   
/*    */   public double distanceChunksTo(double playerChunkX, double playerChunkZ) {
/* 38 */     int minX = originChunkX();
/* 39 */     int maxX = minX + 32;
/* 40 */     int minZ = originChunkZ();
/* 41 */     int maxZ = minZ + 32;
/* 42 */     double dx = Math.max(0.0D, Math.max(minX - playerChunkX, playerChunkX - maxX));
/* 43 */     double dz = Math.max(0.0D, Math.max(minZ - playerChunkZ, playerChunkZ - maxZ));
/* 44 */     return Math.sqrt(dx * dx + dz * dz);
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
/*    */   public double farthestDistanceChunksTo(double playerChunkX, double playerChunkZ) {
/* 57 */     int minX = originChunkX();
/* 58 */     int maxX = minX + 32;
/* 59 */     int minZ = originChunkZ();
/* 60 */     int maxZ = minZ + 32;
/* 61 */     double dx = Math.max(playerChunkX - minX, maxX - playerChunkX);
/* 62 */     double dz = Math.max(playerChunkZ - minZ, maxZ - playerChunkZ);
/* 63 */     return Math.sqrt(dx * dx + dz * dz);
/*    */   } }


/* Location:              C:\Users\Walker\Downloads\ecstatic-forge-1.20.1-1.3.0.jar!\com\angryalchemist\ecstatic\lod\RegionCoord.class
 * Java compiler version: 17 (61.0)
 * JD-Core Version:       1.1.3
 */