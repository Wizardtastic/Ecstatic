package com.angryalchemist.ecstatic.render;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexBuffer;
import com.mojang.blaze3d.vertex.VertexBuffer.Usage;
import com.mojang.blaze3d.vertex.VertexFormat.Mode;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;

final class LodBedrockPlane {
    private static final float RADIUS_BLOCKS = 2048.0F;
    private static final float TILE_BLOCKS = 16.0F;
    private static final float REBUILD_MARGIN_BLOCKS = 512.0F;
    private static VertexBuffer buffer;
    private static double builtCenterX = Double.NaN;
    private static double builtCenterZ = Double.NaN;
    private static SurfaceMaterial.Sprite cachedSprite;

    private LodBedrockPlane() {
    }

    static void render(Matrix4f modelViewMatrix, Matrix4f projectionMatrix, ClientLevel level, Vec3 cameraPos) {
        float y = level.getMinBuildHeight() - 1.0F;
        if (!(cameraPos.y >= y)) {
            boolean needsRebuild = buffer == null
                || Double.isNaN(builtCenterX)
                || Math.abs(cameraPos.x - builtCenterX) > 1536.0
                || Math.abs(cameraPos.z - builtCenterZ) > 1536.0;
            if (needsRebuild) {
                rebuild(cameraPos.x, cameraPos.z, y);
            }

            if (buffer != null) {
                LodTerrainRenderType.TERRAIN_TEXTURED_OPAQUE_NOCULL.setupRenderState();
                RenderSystem.setShader(GameRenderer::getPositionColorTexShader);
                buffer.bind();
                buffer.drawWithShader(modelViewMatrix, projectionMatrix, RenderSystem.getShader());
                VertexBuffer.unbind();
                LodTerrainRenderType.TERRAIN_TEXTURED_OPAQUE_NOCULL.clearRenderState();
            }
        }
    }

    private static void rebuild(double centerX, double centerZ, float y) {
        if (buffer != null) {
            buffer.close();
        }

        if (cachedSprite == null) {
            cachedSprite = SurfaceMaterial.resolveSprite(Blocks.BEDROCK.defaultBlockState(), Direction.UP);
        }

        builtCenterX = centerX;
        builtCenterZ = centerZ;
        buffer = new VertexBuffer(Usage.STATIC);
        BufferBuilder builder = Tesselator.getInstance().getBuilder();
        builder.begin(Mode.QUADS, DefaultVertexFormat.POSITION_COLOR_TEX);
        int tilesPerAxis = (int)Math.ceil(128.0);
        float originX = (float)(Math.floor(centerX / 16.0) * 16.0) - tilesPerAxis * 16.0F;
        float originZ = (float)(Math.floor(centerZ / 16.0) * 16.0) - tilesPerAxis * 16.0F;
        int tileCountPerAxis = tilesPerAxis * 2;

        for (int i = 0; i < tileCountPerAxis; i++) {
            float x0 = originX + i * 16.0F;
            float x1 = x0 + 16.0F;

            for (int j = 0; j < tileCountPerAxis; j++) {
                float z0 = originZ + j * 16.0F;
                float z1 = z0 + 16.0F;
                emitTile(builder, x0, y, z0, x1, z1);
            }
        }

        buffer.bind();
        buffer.upload(builder.end());
        VertexBuffer.unbind();
    }

    private static void emitTile(BufferBuilder builder, float x0, float y, float z0, float x1, float z1) {
        SurfaceMaterial.Sprite sprite = cachedSprite;
        builder.vertex(x0, y, z1).color(255, 255, 255, 255).uv(sprite.u0(), sprite.v1()).endVertex();
        builder.vertex(x1, y, z1).color(255, 255, 255, 255).uv(sprite.u1(), sprite.v1()).endVertex();
        builder.vertex(x1, y, z0).color(255, 255, 255, 255).uv(sprite.u1(), sprite.v0()).endVertex();
        builder.vertex(x0, y, z0).color(255, 255, 255, 255).uv(sprite.u0(), sprite.v0()).endVertex();
    }
}
