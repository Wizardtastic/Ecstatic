#version 150

#moj_import <fog.glsl>

uniform sampler2D Sampler0;
uniform vec4 ColorModulator;
uniform float FogStart;
uniform float FogEnd;
uniform float FogIntensity;
uniform float Saturation;

in float vertexDistance;
in vec4 vertexColor;
in vec2 texCoord0;

out vec4 fragColor;

void main() {
    vec4 color = texture(Sampler0, texCoord0) * vertexColor;
    color.rgb = mix(color.rgb, vec3(dot(color.rgb, vec3(0.299, 0.587, 0.114))), Saturation);
    float fade = mix(1.0, linear_fog_fade(vertexDistance, FogStart, FogEnd), FogIntensity);
    fragColor = vec4(color.rgb, color.a * fade) * ColorModulator;
}