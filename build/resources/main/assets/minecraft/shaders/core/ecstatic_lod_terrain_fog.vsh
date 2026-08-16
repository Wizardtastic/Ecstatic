#version 150

#moj_import <fog.glsl>

in vec3 Position;
in vec4 Color;

uniform mat4 ModelViewMat;
uniform mat4 ProjMat;

out float vertexDistance;
out vec4 vertexColor;

void main() {
    gl_Position = ProjMat * ModelViewMat * vec4(Position, 1.0);

    // Vanilla's fog_distance() is written for VANILLA's convention: camera-relative vertex
    // positions with a rotation-only ModelViewMat (its terrain passes Position + ChunkOffset).
    // This mod's meshes are world-ABSOLUTE and carry the camera offset inside ModelViewMat
    // instead, and under that convention fog_distance's cylindrical branch is really fucking wrong. It's
    // distY term, length(ModelViewMat * vec4(0.0, pos.y, 0.0, 1.0)), works out to
    // sqrt(camX^2 + (posY-camY)^2 + camZ^2) - the camera's absolute distance from world origin
    // leaks straight into the fog distance, and the max() then pins every vertex to it. That is
    // the real cause of the long-standing "fog gets thicker the further from spawn you get" bug is
    // ten thousand blocks out, every LOD vertex gave a fog distance of ten thousand no matter
    // how close it actually was, so the whole ring sat at full fog.
    //
    // The spherical form below has no such term. ModelViewMat's rotation preserves length, so this
    // is exactly |Position - camera| - the true camera-relative distance. inlined rather than
    // passing FogShape == 0, so there is no path back to the broken branch if vanilla changes the
    // shape it happens to set; FogShape is retired from this shader's uniforms for the same reason.
    vertexDistance = length((ModelViewMat * vec4(Position, 1.0)).xyz);
    vertexColor = Color;
}
