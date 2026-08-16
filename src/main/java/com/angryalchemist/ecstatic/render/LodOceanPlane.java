package com.angryalchemist.ecstatic.render;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexBuffer;
import com.mojang.blaze3d.vertex.VertexBuffer.Usage;
import com.mojang.blaze3d.vertex.VertexFormat.Mode;
import com.mojang.math.Axis;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;

public final class LodOceanPlane {
    private static final float SEA_LEVEL_Y = 62.8F;
    private static final int ANGULAR_SEGMENTS = 256;
    static final float OUTER_RADIUS_BLOCKS = 4096.0F;
    private static final int HIGHLIGHT_COLOR = 16777210;
    private static final float SPECULAR_SHININESS = 64.0F;
    private static final float SPECULAR_ALPHA_BOOST = 0.4F;
    private static final float SUN_HORIZON_FADE = 0.1F;
    private static final double REBUILD_DISTANCE_THRESHOLD_BLOCKS = 24.0;
    private static final float REBUILD_SUN_ANGLE_THRESHOLD_DEGREES = 0.25F;
    private static VertexBuffer vertexBuffer;
    private static Vec3 lastRebuildCameraPos;
    private static float lastRebuildSunAngleDeg;
    private static int lastRebuildWaterColor;
    private static float lastRebuildWaterAlpha;
    private static boolean everRebuilt;

    private LodOceanPlane() {
    }

    public static void render(ClientLevel level, Camera camera, Matrix4f modelViewMatrix, Matrix4f projectionMatrix, float partialTick, float innerRadiusBlocks) {
        if (vertexBuffer == null) {
            vertexBuffer = new VertexBuffer(Usage.DYNAMIC);
        }

        Vec3 cameraPos = camera.getPosition();
        float sunAngleDeg = level.getTimeOfDay(partialTick) * 360.0F;
        int shallowColor = BiomeStyleConfig.get().waterColor();
        float shallowAlpha = BiomeStyleConfig.get().waterAlpha();
        ShaderInstance waterShader = LodWaterShader.getPlainOrNull();
        boolean gpuShaded = waterShader != null;
        vertexBuffer.bind();
        if (needsRebuild(cameraPos, sunAngleDeg, shallowColor, shallowAlpha, gpuShaded)) {
            Vector3f sunDirection = gpuShaded ? null : sunDirection(sunAngleDeg);
            BufferBuilder builder = Tesselator.getInstance().getBuilder();
            builder.begin(Mode.TRIANGLES, DefaultVertexFormat.POSITION_COLOR);

            for (int i = 0; i < 256; i++) {
                double angle0 = i / 256.0 * (Math.PI * 2);
                double angle1 = (i + 1) / 256.0 * (Math.PI * 2);
                float cos0 = (float)Math.cos(angle0);
                float sin0 = (float)Math.sin(angle0);
                float cos1 = (float)Math.cos(angle1);
                float sin1 = (float)Math.sin(angle1);
                float innerX0 = (float)cameraPos.x + innerRadiusBlocks * cos0;
                float innerZ0 = (float)cameraPos.z + innerRadiusBlocks * sin0;
                float innerX1 = (float)cameraPos.x + innerRadiusBlocks * cos1;
                float innerZ1 = (float)cameraPos.z + innerRadiusBlocks * sin1;
                float outerX0 = (float)cameraPos.x + 4096.0F * cos0;
                float outerZ0 = (float)cameraPos.z + 4096.0F * sin0;
                float outerX1 = (float)cameraPos.x + 4096.0F * cos1;
                float outerZ1 = (float)cameraPos.z + 4096.0F * sin1;
                int innerColor0 = vertexColor(innerX0, innerZ0, cameraPos, sunDirection, shallowColor, shallowAlpha);
                int innerColor1 = vertexColor(innerX1, innerZ1, cameraPos, sunDirection, shallowColor, shallowAlpha);
                int outerColor0 = vertexColor(outerX0, outerZ0, cameraPos, sunDirection, shallowColor, shallowAlpha);
                int outerColor1 = vertexColor(outerX1, outerZ1, cameraPos, sunDirection, shallowColor, shallowAlpha);
                vertex(builder, innerX0, 62.8F, innerZ0, innerColor0);
                vertex(builder, outerX0, 62.8F, outerZ0, outerColor0);
                vertex(builder, outerX1, 62.8F, outerZ1, outerColor1);
                vertex(builder, innerX0, 62.8F, innerZ0, innerColor0);
                vertex(builder, outerX1, 62.8F, outerZ1, outerColor1);
                vertex(builder, innerX1, 62.8F, innerZ1, innerColor1);
            }

            vertexBuffer.upload(builder.end());
            lastRebuildCameraPos = cameraPos;
            lastRebuildSunAngleDeg = sunAngleDeg;
            lastRebuildWaterColor = shallowColor;
            lastRebuildWaterAlpha = shallowAlpha;
            everRebuilt = true;
        }

        LodOceanRenderType.OCEAN.setupRenderState();
        if (gpuShaded) {
            Vector3f sunDirection = sunDirection(sunAngleDeg);
            LodWaterShader.setSunDirection(sunDirection.x, sunDirection.y, sunDirection.z);
            LodWaterShader.setGameTime(RenderSystem.getShaderGameTime());
            RenderSystem.setShader(() -> waterShader);
        } else {
            RenderSystem.setShader(GameRenderer::getPositionColorShader);
        }

        vertexBuffer.drawWithShader(modelViewMatrix, projectionMatrix, RenderSystem.getShader());
        LodOceanRenderType.OCEAN.clearRenderState();
        VertexBuffer.unbind();
    }

    private static boolean needsRebuild(Vec3 cameraPos, float sunAngleDeg, int shallowColor, float shallowAlpha, boolean gpuShaded) {
        if (!everRebuilt) {
            return true;
        } else if (lastRebuildCameraPos.distanceToSqr(cameraPos) > 576.0) {
            return true;
        } else {
            return !gpuShaded && Math.abs(sunAngleDeg - lastRebuildSunAngleDeg) > 0.25F
                ? true
                : shallowColor != lastRebuildWaterColor || shallowAlpha != lastRebuildWaterAlpha;
        }
    }

    static Vector3f sunDirection(float sunAngleDeg) {
        Matrix4f sunRot = new Matrix4f().rotate(Axis.YP.rotationDegrees(-90.0F)).rotate(Axis.XP.rotationDegrees(sunAngleDeg));
        Vector4f sunDir4 = sunRot.transform(new Vector4f(0.0F, 1.0F, 0.0F, 0.0F));
        return new Vector3f(sunDir4.x, sunDir4.y, sunDir4.z).normalize();
    }

    private static int vertexColor(float x, float z, Vec3 cameraPos, Vector3f sunDirection, int shallowColor, float shallowAlpha) {
        if (sunDirection != null && !(sunDirection.y <= 0.0F)) {
            Vector3f viewDir = new Vector3f((float)(x - cameraPos.x), (float)(62.8F - cameraPos.y), (float)(z - cameraPos.z)).normalize();
            Vector3f reflectDir = reflect(viewDir, new Vector3f(0.0F, 1.0F, 0.0F));
            float alignment = Math.max(0.0F, reflectDir.dot(sunDirection));
            float specular = (float)Math.pow(alignment, 64.0) * smoothstep(0.0F, 0.1F, sunDirection.y);
            int color = blend(shallowColor, 16777210, specular);
            float alpha = clamp01(shallowAlpha + specular * 0.4F);
            return withAlpha(color, alpha);
        } else {
            return withAlpha(shallowColor, shallowAlpha);
        }
    }

    private static Vector3f reflect(Vector3f incident, Vector3f normal) {
        float dot = incident.dot(normal);
        return new Vector3f(incident.x - 2.0F * dot * normal.x, incident.y - 2.0F * dot * normal.y, incident.z - 2.0F * dot * normal.z);
    }

    private static void vertex(BufferBuilder builder, float x, float y, float z, int colorRgba) {
        int r = colorRgba >> 24 & 0xFF;
        int g = colorRgba >> 16 & 0xFF;
        int b = colorRgba >> 8 & 0xFF;
        int a = colorRgba & 0xFF;
        builder.vertex(x, y, z).color(r, g, b, a).endVertex();
    }

    private static int withAlpha(int colorRgb, float alpha) {
        return colorRgb << 8 | clampByte(Math.round(alpha * 255.0F));
    }

    private static int blend(int colorA, int colorB, float t) {
        t = clamp01(t);
        int ar = colorA >> 16 & 0xFF;
        int ag = colorA >> 8 & 0xFF;
        int ab = colorA & 0xFF;
        int br = colorB >> 16 & 0xFF;
        int bg = colorB >> 8 & 0xFF;
        int bb = colorB & 0xFF;
        int r = clampByte(Math.round(ar + (br - ar) * t));
        int g = clampByte(Math.round(ag + (bg - ag) * t));
        int b = clampByte(Math.round(ab + (bb - ab) * t));
        return r << 16 | g << 8 | b;
    }

    private static float smoothstep(float edge0, float edge1, float x) {
        float t = clamp01((x - edge0) / (edge1 - edge0));
        return t * t * (3.0F - 2.0F * t);
    }

    private static float clamp01(float v) {
        return Math.max(0.0F, Math.min(1.0F, v));
    }

    private static int clampByte(int v) {
        return Math.max(0, Math.min(255, v));
    }
}
