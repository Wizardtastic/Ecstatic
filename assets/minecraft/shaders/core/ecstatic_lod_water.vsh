#version 150

#moj_import <fog.glsl>

in vec3 Position;
in vec4 Color;

uniform mat4 ModelViewMat;
uniform mat4 ProjMat;

out vec4 vertexColor;
out vec3 eyePos;
out float vertexDistance;

void main() {
    vec4 eyePosition = ModelViewMat * vec4(Position, 1.0);
    gl_Position = ProjMat * eyePosition;

    vertexColor = Color;
    eyePos = eyePosition.xyz;
    vertexDistance = length((ModelViewMat * vec4(Position, 1.0)).xyz);
}
