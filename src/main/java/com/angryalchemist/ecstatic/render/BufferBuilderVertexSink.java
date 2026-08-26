package com.angryalchemist.ecstatic.render;

import com.mojang.blaze3d.vertex.BufferBuilder;

final class BufferBuilderVertexSink implements VertexSink {
    private final BufferBuilder builder;

    BufferBuilderVertexSink(BufferBuilder builder) {
        this.builder = builder;
    }

    @Override
    public void vertex(float x, float y, float z, int colorRgb, int alpha) {
        int r = colorRgb >> 16 & 0xFF;
        int g = colorRgb >> 8 & 0xFF;
        int b = colorRgb & 0xFF;
        this.builder.addVertex(x, y, z).setColor(r, g, b, alpha);
    }

    @Override
    public void litVertex(float x, float y, float z, int colorRgb, int alpha, float nx, float ny, float nz) {
        int r = colorRgb >> 16 & 0xFF;
        int g = colorRgb >> 8 & 0xFF;
        int b = colorRgb & 0xFF;
        this.builder.addVertex(x, y, z).setColor(r, g, b, alpha).setUv(0.5F, 0.5F).setLight(0xF000F0).setNormal(nx, ny, nz);
    }

    @Override
    public void texturedVertex(float x, float y, float z, float u, float v, int colorRgb, int alpha) {
        int r = colorRgb >> 16 & 0xFF;
        int g = colorRgb >> 8 & 0xFF;
        int b = colorRgb & 0xFF;
        this.builder.addVertex(x, y, z).setColor(r, g, b, alpha).setUv(u, v);
    }

    @Override
    public void litTexturedVertex(float x, float y, float z, float u, float v, int colorRgb, int alpha, float nx, float ny, float nz) {
        int r = colorRgb >> 16 & 0xFF;
        int g = colorRgb >> 8 & 0xFF;
        int b = colorRgb & 0xFF;
        this.builder.addVertex(x, y, z).setColor(r, g, b, alpha).setUv(u, v).setLight(0xF000F0).setNormal(nx, ny, nz);
    }
}
