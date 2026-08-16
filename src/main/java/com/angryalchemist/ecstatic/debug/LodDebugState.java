package com.angryalchemist.ecstatic.debug;

public final class LodDebugState {
    private static volatile boolean enabled = false;
    private static volatile boolean fogDisabled = false;
    private static volatile int forcedLevel = 0;
    private static volatile boolean referenceQuadEnabled = false;
    private static volatile int vertexFormatOverride = 0;

    private LodDebugState() {
    }

    public static boolean isEnabled() {
        return enabled;
    }

    public static void setEnabled(boolean value) {
        enabled = value;
        if (!value) {
            fogDisabled = false;
            forcedLevel = 0;
            referenceQuadEnabled = false;
            vertexFormatOverride = 0;
        }
    }

    public static boolean isFogDisabled() {
        return enabled && fogDisabled;
    }

    public static boolean toggleFog() {
        fogDisabled = !fogDisabled;
        return fogDisabled;
    }

    public static boolean isReferenceQuadEnabled() {
        return enabled && referenceQuadEnabled;
    }

    public static boolean toggleReferenceQuad() {
        referenceQuadEnabled = !referenceQuadEnabled;
        return referenceQuadEnabled;
    }

    public static int forcedLevel() {
        return enabled ? forcedLevel : 0;
    }

    public static int cycleForcedLevel() {
        forcedLevel = switch (forcedLevel) {
            case 0 -> 1;
            case 1 -> 2;
            case 2 -> 3;
            case 3 -> 4;
            default -> 0;
        };
        return forcedLevel;
    }

    public static int vertexFormatOverride() {
        return enabled ? vertexFormatOverride : 0;
    }

    public static int cycleVertexFormatOverride() {
        vertexFormatOverride = switch (vertexFormatOverride) {
            case 0 -> 1;
            case 1 -> 2;
            default -> 0;
        };
        return vertexFormatOverride;
    }
}
