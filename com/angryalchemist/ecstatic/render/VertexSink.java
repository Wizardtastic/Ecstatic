package com.angryalchemist.ecstatic.render;

interface VertexSink {
  void vertex(float paramFloat1, float paramFloat2, float paramFloat3, int paramInt1, int paramInt2);
  
  void litVertex(float paramFloat1, float paramFloat2, float paramFloat3, int paramInt1, int paramInt2, float paramFloat4, float paramFloat5, float paramFloat6);
  
  void texturedVertex(float paramFloat1, float paramFloat2, float paramFloat3, float paramFloat4, float paramFloat5, int paramInt1, int paramInt2);
  
  void litTexturedVertex(float paramFloat1, float paramFloat2, float paramFloat3, float paramFloat4, float paramFloat5, int paramInt1, int paramInt2, float paramFloat6, float paramFloat7, float paramFloat8);
}


/* Location:              C:\Users\Walker\Downloads\ecstatic-forge-1.20.1-1.3.0.jar!\com\angryalchemist\ecstatic\render\VertexSink.class
 * Java compiler version: 17 (61.0)
 * JD-Core Version:       1.1.3
 */