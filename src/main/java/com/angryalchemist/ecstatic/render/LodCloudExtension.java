package com.angryalchemist.ecstatic.render;

import com.mojang.blaze3d.platform.GlStateManager.DestFactor;
import com.mojang.blaze3d.platform.GlStateManager.SourceFactor;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexBuffer;
import com.mojang.blaze3d.vertex.VertexBuffer.Usage;
import com.mojang.blaze3d.vertex.VertexFormat.Mode;
import net.minecraft.client.Camera;
import net.minecraft.client.CloudStatus;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;

final class LodCloudExtension {
    private static final ResourceLocation CLOUDS_LOCATION = ResourceLocation.withDefaultNamespace("textures/environment/clouds.png");
    private static final float SCALE_BLOCKS_PER_UNIT = 12.0F;
    private static final float TEXEL = 0.00390625F;
    private static final int TILE_UNITS = 32;
    private static final int WRAP_UNITS = 2048;
    private static final float ALPHA = 0.8F;
    private static final float WHITE = 1.0F;
    private static VertexBuffer buffer;
    private static int lastGridX = Integer.MIN_VALUE;
    private static int lastGridZ = Integer.MIN_VALUE;
    private static int lastHalfExtentUnits = -1;

    private LodCloudExtension() {
    }

    static void render(Matrix4f rotationOnlyMatrix, Matrix4f projectionMatrix, ClientLevel level, Camera camera, float partialTick, int outerRadiusBlocks) {
        if (Minecraft.getInstance().options.getCloudsType() != CloudStatus.OFF) {
            float cloudHeight = level.effects().getCloudHeight();
            if (!Float.isNaN(cloudHeight)) {
                Vec3 cameraPos = camera.getPosition();
                double scroll = ((double)level.getGameTime() + partialTick) * 0.03;
                double d2 = (cameraPos.x + scroll) / 12.0;
                double d3 = cloudHeight - cameraPos.y + 0.33;
                double d4 = cameraPos.z / 12.0 + 0.33;
                d2 -= Math.floor(d2 / 2048.0) * 2048.0;
                d4 -= Math.floor(d4 / 2048.0) * 2048.0;
                float fracX = (float)(d2 - Math.floor(d2));
                float fracZ = (float)(d4 - Math.floor(d4));
                int gridX = (int)Math.floor(d2);
                int gridZ = (int)Math.floor(d4);
                int requestedHalfUnits = (int)Math.ceil(outerRadiusBlocks / 12.0F);
                int halfExtentUnits = (ceilDiv(requestedHalfUnits) + 1) * 32;
                if (buffer == null || gridX != lastGridX || gridZ != lastGridZ || halfExtentUnits != lastHalfExtentUnits) {
                    rebuild(gridX, gridZ, halfExtentUnits);
                    lastGridX = gridX;
                    lastGridZ = gridZ;
                    lastHalfExtentUnits = halfExtentUnits;
                }

                if (buffer != null) {
                    Vec3 cloudColor = level.getCloudColor(partialTick);
                    RenderSystem.disableCull();
                    RenderSystem.enableBlend();
                    RenderSystem.enableDepthTest();
                    RenderSystem.blendFuncSeparate(SourceFactor.SRC_ALPHA, DestFactor.ONE_MINUS_SRC_ALPHA, SourceFactor.ONE, DestFactor.ONE_MINUS_SRC_ALPHA);
                    RenderSystem.depthMask(true);
                    RenderSystem.setShader(GameRenderer::getPositionTexColorShader);
                    RenderSystem.setShaderTexture(0, CLOUDS_LOCATION);
                    RenderSystem.setShaderColor((float)cloudColor.x, (float)cloudColor.y, (float)cloudColor.z, 1.0F);
                    Matrix4f transform = new Matrix4f(rotationOnlyMatrix);
                    transform.scale(12.0F, 1.0F, 12.0F);
                    transform.translate(-fracX, (float)d3, -fracZ);
                    buffer.bind();
                    ShaderInstance shader = RenderSystem.getShader();
                    assert shader != null;
                    buffer.drawWithShader(transform, projectionMatrix, shader);
                    VertexBuffer.unbind();
                    RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
                    RenderSystem.enableCull();
                    RenderSystem.disableBlend();
                    RenderSystem.defaultBlendFunc();
                }
            }
        }
    }

    private static int ceilDiv(int numerator) {
        return (numerator + 32 - 1) / 32;
    }

    private static void rebuild(int gridX, int gridZ, int halfExtentUnits) {
        if (buffer != null) {
            buffer.close();
        }

        buffer = new VertexBuffer(Usage.STATIC);
        BufferBuilder builder = Tesselator.getInstance().begin(Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR);
        float uOffset = gridX * 0.00390625F;
        float vOffset = gridZ * 0.00390625F;

        for (int tileX = -halfExtentUnits; tileX < halfExtentUnits; tileX += 32) {
            for (int tileZ = -halfExtentUnits; tileZ < halfExtentUnits; tileZ += 32) {
                boolean coveredByVanilla = tileX >= -32 && tileX < 32 && tileZ >= -32 && tileZ < 32;
                if (!coveredByVanilla) {
                    emitTile(builder, tileX, tileZ, uOffset, vOffset);
                }
            }
        }

        buffer.bind();
        buffer.upload(builder.buildOrThrow());
        VertexBuffer.unbind();
    }

    private static void emitTile(BufferBuilder builder, int x, int z, float uOffset, float vOffset) {
        float x1 = x + 32;
        float z1 = z + 32;
        builder.addVertex((float) x, 0.0F, z1).setUv((float) x * 0.00390625F + uOffset, z1 * 0.00390625F + vOffset).setColor(1.0F, 1.0F, 1.0F, 0.8F);
        builder.addVertex(x1, 0.0F, z1).setUv(x1 * 0.00390625F + uOffset, z1 * 0.00390625F + vOffset).setColor(1.0F, 1.0F, 1.0F, 0.8F);
        builder.addVertex(x1, 0.0F, (float) z).setUv(x1 * 0.00390625F + uOffset, (float) z * 0.00390625F + vOffset).setColor(1.0F, 1.0F, 1.0F, 0.8F);
        builder.addVertex((float) x, 0.0F, (float) z).setUv((float) x * 0.00390625F + uOffset, (float) z * 0.00390625F + vOffset).setColor(1.0F, 1.0F, 1.0F, 0.8F);
    }
}
