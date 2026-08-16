package com.angryalchemist.ecstatic.lod;

public record RegionCoord(int x, int z) {
    public static final int SIZE_BLOCKS = 512;
    public static final int SIZE_CHUNKS = 32;

    public static RegionCoord fromBlock(int blockX, int blockZ) {
        return new RegionCoord(Math.floorDiv(blockX, 512), Math.floorDiv(blockZ, 512));
    }

    public static RegionCoord fromChunk(int chunkX, int chunkZ) {
        return new RegionCoord(Math.floorDiv(chunkX, 32), Math.floorDiv(chunkZ, 32));
    }

    public int originBlockX() {
        return this.x * 512;
    }

    public int originBlockZ() {
        return this.z * 512;
    }

    public int originChunkX() {
        return this.x * 32;
    }

    public int originChunkZ() {
        return this.z * 32;
    }

    public double distanceChunksTo(double playerChunkX, double playerChunkZ) {
        int minX = this.originChunkX();
        int maxX = minX + 32;
        int minZ = this.originChunkZ();
        int maxZ = minZ + 32;
        double dx = Math.max(0.0, Math.max(minX - playerChunkX, playerChunkX - maxX));
        double dz = Math.max(0.0, Math.max(minZ - playerChunkZ, playerChunkZ - maxZ));
        return Math.sqrt(dx * dx + dz * dz);
    }

    public double farthestDistanceChunksTo(double playerChunkX, double playerChunkZ) {
        int minX = this.originChunkX();
        int maxX = minX + 32;
        int minZ = this.originChunkZ();
        int maxZ = minZ + 32;
        double dx = Math.max(playerChunkX - minX, maxX - playerChunkX);
        double dz = Math.max(playerChunkZ - minZ, maxZ - playerChunkZ);
        return Math.sqrt(dx * dx + dz * dz);
    }
}
