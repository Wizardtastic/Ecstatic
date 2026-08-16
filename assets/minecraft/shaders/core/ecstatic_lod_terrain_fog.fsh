#version 150

#moj_import <fog.glsl>

uniform vec4 ColorModulator;
uniform float FogStart;
uniform float FogEnd;
uniform vec4 FogColor;
uniform float FogIntensity;

in float vertexDistance;
in vec4 vertexColor;

out vec4 fragColor;

void main() {
    if (vertexColor.a == 0.0) {
        discard;
    }
    // FogIntensity (LodSettingsConfig.Data#fogIntensity) is a straight mix back toward the pre-fog
    // color, independent of FogStart/FogEnd's own distance shape - 1.0 (default) reproduces the
    // original always-fogged result, 0.0 disables the effect entirely.
    vec4 fogged = linear_fog(vertexColor, vertexDistance, FogStart, FogEnd, FogColor);
    fragColor = mix(vertexColor, fogged, FogIntensity) * ColorModulator;
}
