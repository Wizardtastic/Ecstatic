package com.angryalchemist.ecstatic.lod;

public final class LodLevel {
    public static final int NONE = -1;
    public static final int VANILLA = 0;
    public static final int MAX = 5;

    private LodLevel() {
    }

    public static int scale(int level) {
        if (level >= 0 && level <= 5) {
            return 1 << level;
        } else {
            throw new IllegalArgumentException("Invalid LOD level: " + level);
        }
    }

    public static int sampleSpacingBlocks(int level) {
        return switch (level) {
            case 1 -> 2;
            case 2 -> 4;
            case 3 -> 8;
            case 4 -> 16;
            case 5 -> 32;
            default -> throw new IllegalArgumentException("No sparse-sampling spacing defined for LOD level: " + level);
        };
    }
}
