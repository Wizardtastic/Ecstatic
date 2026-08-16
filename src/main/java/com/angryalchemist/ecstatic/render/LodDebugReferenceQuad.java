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
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;

public final class LodDebugReferenceQuad {
    private static VertexBuffer vertexBuffer;

    private LodDebugReferenceQuad() {
    }

    public static void render(Camera camera, Matrix4f projectionMatrix) {
        if (vertexBuffer == null) {
            vertexBuffer = new VertexBuffer(Usage.DYNAMIC);
        }

        Vec3 cameraPos = camera.getPosition();
        BufferBuilder builder = Tesselator.getInstance().getBuilder();
        builder.begin(Mode.TRIANGLES, DefaultVertexFormat.POSITION_COLOR);
        float x0 = (float)cameraPos.x - 10.0F;
        float x1 = (float)cameraPos.x + 10.0F;
        float y = (float)cameraPos.y - 2.0F;
        float z0 = (float)cameraPos.z - 10.0F;
        float z1 = (float)cameraPos.z + 10.0F;
        vertex(builder, x0, y, z0);
        vertex(builder, x1, y, z0);
        vertex(builder, x1, y, z1);
        vertex(builder, x0, y, z0);
        vertex(builder, x1, y, z1);
        vertex(builder, x0, y, z1);
        vertexBuffer.bind();
        vertexBuffer.upload(builder.end());
        Matrix4f modelViewMatrix = new Matrix4f();
        modelViewMatrix.rotate(Axis.XP.rotationDegrees(camera.getXRot()));
        modelViewMatrix.rotate(Axis.YP.rotationDegrees(camera.getYRot() + 180.0F));
        modelViewMatrix.translate(-((float)cameraPos.x), -((float)cameraPos.y), -((float)cameraPos.z));
        RenderSystem.disableCull();
        LodTerrainRenderType.TERRAIN.setupRenderState();
        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        vertexBuffer.drawWithShader(modelViewMatrix, projectionMatrix, RenderSystem.getShader());
        LodTerrainRenderType.TERRAIN.clearRenderState();
        VertexBuffer.unbind();
    }

    private static void vertex(BufferBuilder builder, float x, float y, float z) {
        builder.vertex(x, y, z).color(255, 0, 255, 255).endVertex();
    }
}
