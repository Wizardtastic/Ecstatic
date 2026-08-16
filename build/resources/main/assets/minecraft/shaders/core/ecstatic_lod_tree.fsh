#version 150

#moj_import <fog.glsl>

uniform sampler2D Sampler0;
uniform vec4 ColorModulator;
uniform float FogStart;
uniform float FogEnd;
uniform vec4 FogColor;
uniform float FogIntensity;

in float vertexDistance;
in vec4 vertexColor;
in vec2 texCoord0;

out vec4 fragColor;

#define LEAF_ALPHA_FLOOR 0.85

void main() {
    vec4 color = texture(Sampler0, texCoord0) * vertexColor;
    color.a = max(color.a, LEAF_ALPHA_FLOOR);
    // See ecstatic_lod_terrain_fog_tex's identical comment on FogIntensity.
    vec4 fogged = linear_fog(color, vertexDistance, FogStart, FogEnd, FogColor);
    fragColor = mix(color, fogged, FogIntensity) * ColorModulator;
}
