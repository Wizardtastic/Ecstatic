package com.angryalchemist.ecstatic.lod;

public final class RingConfig {
    private final int[] outerBoundaryChunks = new int[6];
    public final int hysteresisChunks;
    public static final int RING_START_MARGIN_CHUNKS = 12;

    public RingConfig(int renderDistanceChunks, int lod1Width, int lod2Width, int lod3Width, int lod4Width, int lod5Width, int hysteresisChunks) {
        this.outerBoundaryChunks[0] = renderDistanceChunks;
        this.outerBoundaryChunks[1] = this.outerBoundaryChunks[0] + lod1Width;
        this.outerBoundaryChunks[2] = this.outerBoundaryChunks[1] + lod2Width;
        this.outerBoundaryChunks[3] = this.outerBoundaryChunks[2] + lod3Width;
        this.outerBoundaryChunks[4] = this.outerBoundaryChunks[3] + lod4Width;
        this.outerBoundaryChunks[5] = this.outerBoundaryChunks[4] + lod5Width;
        this.hysteresisChunks = hysteresisChunks;
    }

    public static RingConfig defaults(int renderDistanceChunks) {
        return new RingConfig(renderDistanceChunks, 16, 32, 64, 128, 256, 4);
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

    public static int ring1StartChunks(int clientRenderDistanceChunks) {
        return Math.max(2, clientRenderDistanceChunks - 12);
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

        return -1;
    }
}
