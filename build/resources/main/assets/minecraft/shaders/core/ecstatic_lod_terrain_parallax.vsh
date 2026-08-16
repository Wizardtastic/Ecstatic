#version 150

#moj_import <fog.glsl>

in vec3 Position;
in vec4 Color;
in ivec2 UV2;
in vec3 Normal;

uniform mat4 ModelViewMat;
uniform mat4 ProjMat;

out vec4 vertexColor;
out vec3 worldPos;
out vec3 worldNormal;
out vec3 eyePos;
out float vertexDistance;
flat out ivec2 lightMapCoord;

void main() {
    vec4 eyePosition = ModelViewMat * vec4(Position, 1.0);
    gl_Position = ProjMat * eyePosition;

    vertexColor = Color;
    worldPos = Position;
    worldNormal = Normal;
    eyePos = eyePosition.xyz;

    vertexDistance = length((ModelViewMat * vec4(Position, 1.0)).xyz);
    lightMapCoord = UV2 / 16;
}
