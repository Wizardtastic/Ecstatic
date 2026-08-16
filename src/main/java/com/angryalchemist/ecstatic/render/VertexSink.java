package com.angryalchemist.ecstatic.render;

interface VertexSink {
    void vertex(float var1, float var2, float var3, int var4, int var5);

    void litVertex(float var1, float var2, float var3, int var4, int var5, float var6, float var7, float var8);

    void texturedVertex(float var1, float var2, float var3, float var4, float var5, int var6, int var7);

    void litTexturedVertex(float var1, float var2, float var3, float var4, float var5, int var6, int var7, float var8, float var9, float var10);
}
