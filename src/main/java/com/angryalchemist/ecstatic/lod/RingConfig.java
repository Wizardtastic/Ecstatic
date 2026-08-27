package com.angryalchemist.ecstatic.lod;

public final class RingConfig {
    private final int[] outerBoundaryChunks = new int[6];
    public final int hysteresisChunks;
    
    public static final int RING_START_MARGIN_CHUNKS = 20;

    private static final int DEFAULT_LOD1_WIDTH = 16;
    private static final int DEFAULT_LOD2_WIDTH = 32;
    private static final int DEFAULT_LOD3_WIDTH = 64;
    private static final int DEFAULT_LOD4_WIDTH = 128;
    private static final int DEFAULT_LOD5_WIDTH = 1024;
    private static final int DEFAULT_HYSTERESIS_CHUNKS = 4;

    public RingConfig(int renderDistanceChunks, int lod1Width, int lod2Width, int lod3Width, int lod4Width, int lod5Width, int hysteresisChunks) {
        this.outerBoundaryChunks[0] = Math.max(0, renderDistanceChunks);
        this.outerBoundaryChunks[1] = this.outerBoundaryChunks[0] + Math.max(0, lod1Width); // sanitized inputs
        this.outerBoundaryChunks[2] = this.outerBoundaryChunks[1] + Math.max(0, lod2Width);
        this.outerBoundaryChunks[3] = this.outerBoundaryChunks[2] + Math.max(0, lod3Width);
        this.outerBoundaryChunks[4] = this.outerBoundaryChunks[3] + Math.max(0, lod4Width);
        this.outerBoundaryChunks[5] = this.outerBoundaryChunks[4] + Math.max(0, lod5Width);
        this.hysteresisChunks = hysteresisChunks;
    }

    public static RingConfig defaults(int renderDistanceChunks) {
        return new RingConfig(renderDistanceChunks, DEFAULT_LOD1_WIDTH, DEFAULT_LOD2_WIDTH, DEFAULT_LOD3_WIDTH, DEFAULT_LOD4_WIDTH, DEFAULT_LOD5_WIDTH, DEFAULT_HYSTERESIS_CHUNKS); // ring sizes and the hysteresis band around them respectively at 100% scale
    }

    public static RingConfig scaled(
        int renderDistanceChunks, int lod1Width, int lod2Width, int lod3Width, int lod4Width, int lod5Width, int hysteresisChunks, float widthScale
    ) {
        return new RingConfig(
            renderDistanceChunks,
            scaleWidth(lod1Width, widthScale),
            scaleWidth(lod2Width, widthScale),
            scaleWidth(lod3Width, widthScale),
            scaleWidth(lod4Width, widthScale),
            scaleWidth(lod5Width, widthScale),
            hysteresisChunks
        );
    }

    private static int scaleWidth(int baseWidth, float widthScale) {
        return Math.max(1, Math.round(baseWidth * widthScale));
    }

    public static int ring1StartChunks(int clientRenderDistanceChunks) { // where to start rendering the first LOD
        return Math.max(2, clientRenderDistanceChunks - RING_START_MARGIN_CHUNKS); 
    }

    public int outerBoundary(int level) {
        return this.outerBoundaryChunks[level];
    }

    public int resolveLevel(double distanceChunks, int previousLevel) {
        int level = previousLevel;

        while (level < 5 && distanceChunks > this.outerBoundary(level) + this.hysteresisChunks) {
            level++;
        }

        while (level > 0 && distanceChunks < this.outerBoundary(level - 1) - this.hysteresisChunks) {
            level--;
        }

        return level;
    }

    public int classify(double distanceChunks) {
        for (int level = 0; level <= 5; level++) {
            if (distanceChunks <= this.outerBoundary(level)) {
                return level;
            }
        }

        return 5; // renders the highest LOD in edge cases 
    }
}
