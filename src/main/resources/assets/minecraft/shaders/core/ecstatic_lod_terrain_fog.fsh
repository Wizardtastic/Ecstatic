#version 150

#moj_import <fog.glsl>

uniform vec4 ColorModulator;
uniform float FogStart;
uniform float FogEnd;
uniform float FogIntensity;

in float vertexDistance;
in vec4 vertexColor;

out vec4 fragColor;

void main() {
    if (vertexColor.a == 0.0) {
        discard;
    }
    float fade = mix(1.0, linear_fog_fade(vertexDistance, FogStart, FogEnd), FogIntensity);
    fragColor = vec4(vertexColor.rgb, vertexColor.a * fade) * ColorModulator;
}