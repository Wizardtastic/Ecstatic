package com.angryalchemist.ecstatic.render;

import java.util.Arrays;

final class RecordedVertexSink implements VertexSink {
    private static final int INITIAL_CAPACITY = 256;
    private float[] xs = new float[256];
    private float[] ys = new float[256];
    private float[] zs = new float[256];
    private float[] us = new float[256];
    private float[] vs = new float[256];
    private int[] colors = new int[256];
    private int[] alphas = new int[256];
    private float[] nxs = new float[256];
    private float[] nys = new float[256];
    private float[] nzs = new float[256];
    private int count = 0;

    private void ensureCapacity() {
        if (this.count >= this.xs.length) {
            int newCapacity = this.xs.length * 2;
            this.xs = Arrays.copyOf(this.xs, newCapacity);
            this.ys = Arrays.copyOf(this.ys, newCapacity);
            this.zs = Arrays.copyOf(this.zs, newCapacity);
            this.us = Arrays.copyOf(this.us, newCapacity);
            this.vs = Arrays.copyOf(this.vs, newCapacity);
            this.colors = Arrays.copyOf(this.colors, newCapacity);
            this.alphas = Arrays.copyOf(this.alphas, newCapacity);
            this.nxs = Arrays.copyOf(this.nxs, newCapacity);
            this.nys = Arrays.copyOf(this.nys, newCapacity);
            this.nzs = Arrays.copyOf(this.nzs, newCapacity);
        }
    }

    private void record(float x, float y, float z, float u, float v, int colorRgb, int alpha, float nx, float ny, float nz) {
        this.ensureCapacity();
        this.xs[this.count] = x;
        this.ys[this.count] = y;
        this.zs[this.count] = z;
        this.us[this.count] = u;
        this.vs[this.count] = v;
        this.colors[this.count] = colorRgb;
        this.alphas[this.count] = alpha;
        this.nxs[this.count] = nx;
        this.nys[this.count] = ny;
        this.nzs[this.count] = nz;
        this.count++;
    }

    @Override
    public void vertex(float x, float y, float z, int colorRgb, int alpha) {
        this.record(x, y, z, 0.0F, 0.0F, colorRgb, alpha, 0.0F, 0.0F, 0.0F);
    }

    @Override
    public void litVertex(float x, float y, float z, int colorRgb, int alpha, float nx, float ny, float nz) {
        this.record(x, y, z, 0.0F, 0.0F, colorRgb, alpha, nx, ny, nz);
    }

    @Override
    public void texturedVertex(float x, float y, float z, float u, float v, int colorRgb, int alpha) {
        this.record(x, y, z, u, v, colorRgb, alpha, 0.0F, 0.0F, 0.0F);
    }

    @Override
    public void litTexturedVertex(float x, float y, float z, float u, float v, int colorRgb, int alpha, float nx, float ny, float nz) {
        this.record(x, y, z, u, v, colorRgb, alpha, nx, ny, nz);
    }

    int count() {
        return this.count;
    }

    boolean isEmpty() {
        return this.count == 0;
    }

    void replayInto(VertexSink target, RecordedVertexSink.Kind kind) {
        switch (kind) {
            case PLAIN:
                for (int i = 0; i < this.count; i++) {
                    target.vertex(this.xs[i], this.ys[i], this.zs[i], this.colors[i], this.alphas[i]);
                }
                break;
            case LIT:
                for (int i = 0; i < this.count; i++) {
                    target.litVertex(this.xs[i], this.ys[i], this.zs[i], this.colors[i], this.alphas[i], this.nxs[i], this.nys[i], this.nzs[i]);
                }
                break;
            case TEXTURED:
                for (int i = 0; i < this.count; i++) {
                    target.texturedVertex(this.xs[i], this.ys[i], this.zs[i], this.us[i], this.vs[i], this.colors[i], this.alphas[i]);
                }
                break;
            case LIT_TEXTURED:
                for (int i = 0; i < this.count; i++) {
                    target.litTexturedVertex(
                        this.xs[i], this.ys[i], this.zs[i], this.us[i], this.vs[i], this.colors[i], this.alphas[i], this.nxs[i], this.nys[i], this.nzs[i]
                    );
                }
        }
    }

    enum Kind {
        PLAIN,
        LIT,
        TEXTURED,
        LIT_TEXTURED;
    }
}
