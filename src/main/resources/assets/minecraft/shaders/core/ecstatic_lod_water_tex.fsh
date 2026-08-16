#version 150

#moj_import <fog.glsl>

uniform sampler2D Sampler0;
uniform vec4 ColorModulator;
uniform mat4 ModelViewMat;
uniform vec3 SunDir;
uniform float FogStart;
uniform float FogEnd;
uniform vec4 FogColor;
uniform float FogIntensity;

in vec4 vertexColor;
in vec3 eyePos;
in float vertexDistance;
in vec2 texCoord0;

out vec4 fragColor;

#define SPECULAR_SHININESS 180.0
// How far above the horizon the sun/moon must climb before its glint fades in.
#define LIGHT_HORIZON_FADE 0.08

void main() {
    vec4 texColor = texture(Sampler0, texCoord0);
    vec3 baseColor = vertexColor.rgb * texColor.rgb;
    float baseAlpha = vertexColor.a;

    vec3 n = vec3(0.0, 1.0, 0.0);

    mat3 invRot = transpose(mat3(ModelViewMat));
    vec3 viewDirWorld = normalize(invRot * eyePos);

    float cosTheta = clamp(dot(-viewDirWorld, n), 0.0, 1.0);
    float fresnel = 0.02 + 0.98 * pow(1.0 - cosTheta, 5.0);

    vec3 reflectDir = reflect(viewDirWorld, n);
    float sunVis = smoothstep(0.0, LIGHT_HORIZON_FADE, SunDir.y);
    float moonVis = smoothstep(0.0, LIGHT_HORIZON_FADE, -SunDir.y);
    float sunSpec = pow(clamp(dot(reflectDir, SunDir), 0.0, 1.0), SPECULAR_SHININESS) * sunVis;
    float moonSpec = pow(clamp(dot(reflectDir, -SunDir), 0.0, 1.0), SPECULAR_SHININESS) * moonVis * 0.25;

    vec3 color = mix(baseColor, FogColor.rgb, fresnel * 0.45);
    color += vec3(1.0, 0.97, 0.88) * sunSpec + vec3(0.72, 0.80, 1.0) * moonSpec;

    float boost = fresnel * 0.35 + (sunSpec + moonSpec) * 0.5;
    float alpha = clamp(baseAlpha * (1.0 + boost), 0.0, 1.0);

    vec4 shaded = vec4(color, alpha) * ColorModulator;
    fragColor = mix(shaded, linear_fog(shaded, vertexDistance, FogStart, FogEnd, FogColor), FogIntensity);
}
