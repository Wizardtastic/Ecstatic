package com.angryalchemist.ecstatic.render;

import org.joml.Matrix4f;

final class LodFarPlaneProjection {
    private static final float NEAR_PLANE_BLOCKS = 0.05F;

    private LodFarPlaneProjection() {
    }

    static Matrix4f withExtendedFarPlane(Matrix4f source, float farPlaneBlocks) {
        float h = 1.0F / source.m11();
        float aspect = source.m11() / source.m00();
        float fovyRadians = 2.0F * (float)Math.atan(h);
        return new Matrix4f().perspective(fovyRadians, aspect, NEAR_PLANE_BLOCKS, farPlaneBlocks);
    }
}
