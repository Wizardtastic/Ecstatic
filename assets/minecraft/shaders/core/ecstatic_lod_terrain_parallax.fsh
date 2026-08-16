// kinda sorta deprecated
#version 150

#moj_import <fog.glsl>

uniform sampler2D Sampler2;
uniform vec4 ColorModulator;
uniform mat4 ModelViewMat;
uniform vec3 SunDir;
uniform float FogStart;
uniform float FogEnd;
uniform vec4 FogColor;

in vec4 vertexColor;
in vec3 worldPos;
in vec3 worldNormal;
in vec3 eyePos;
in float vertexDistance;
flat in ivec2 lightMapCoord;

out vec4 fragColor;


#define TEXTURE_SCALE 0.05
#define DOMAIN_WARP_STRENGTH 1.5
#define PARALLAX_DEPTH 1.5
#define MAX_STEPS 4
#define NEAR_FADE_BLOCKS 64.0
#define FAR_FADE_BLOCKS 512.0


float hash12(vec2 p) {
    vec3 p3 = fract(vec3(p.xyx) * 0.1031);
    p3 += dot(p3, p3.yzx + 33.33);
    return fract((p3.x + p3.y) * p3.z);
}

float valueNoise(vec2 p) {
    vec2 i = floor(p);
    vec2 f = fract(p);
    float a = hash12(i);
    float b = hash12(i + vec2(1.0, 0.0));
    float c = hash12(i + vec2(0.0, 1.0));
    float d = hash12(i + vec2(1.0, 1.0));
    vec2 u = f * f * (3.0 - 2.0 * f);
    return mix(mix(a, b, u.x), mix(c, d, u.x), u.y);
}


float fbm(vec2 p) {
    float sum = 0.0;
    float amp = 0.5;
    float freq = 1.0;
    for (int i = 0; i < 3; i++) {
        sum += amp * valueNoise(p * freq);
        freq *= 2.02;
        amp *= 0.5;
    }
    return sum;
}


float ridgedNoise(vec2 p) {
    float sum = 0.0;
    float amp = 0.5;
    float freq = 1.0;
    float weight = 1.0;
    for (int i = 0; i < 4; i++) {
        float n = 1.0 - abs(valueNoise(p * freq) * 2.0 - 1.0);
        n = n * n * weight;
        weight = clamp(n * 2.0, 0.0, 1.0);
        sum += n * amp;
        freq *= 2.02;
        amp *= 0.5;
    }
    return sum;
}

float voronoiInverted(vec2 p) {
    vec2 i = floor(p);
    vec2 f = fract(p);
    float minDist = 2.0;
    for (int y = -1; y <= 1; y++) {
        for (int x = -1; x <= 1; x++) {
            vec2 cellOffset = vec2(float(x), float(y));
            vec2 cellPoint = cellOffset
                    + vec2(hash12(i + cellOffset), hash12(i + cellOffset + vec2(37.0, 17.0)));
            float d = length(cellPoint - f);
            minDist = min(minDist, d);
        }
    }
    return 1.0 - clamp(minDist, 0.0, 1.0);
}

vec2 domainWarp(vec2 p) {
    vec2 q = vec2(fbm(p), fbm(p + vec2(5.2, 1.3)));
    return p + (q - 0.5) * DOMAIN_WARP_STRENGTH;
}

float terrainDetail(vec2 p, float slope) {
    vec2 warped = domainWarp(p);
    float ridged = ridgedNoise(warped);
    float voronoi = voronoiInverted(warped);
    float base = mix(voronoi, ridged, clamp(slope, 0.0, 1.0));
    float grit = fbm(warped * 4.0) * slope;
    return clamp(base + grit * 0.3, 0.0, 1.0);
}

float triplanarDetail(vec3 p, vec3 n, float slope) {
    vec3 blendWeights = pow(abs(n), vec3(4.0));
    blendWeights /= max(blendWeights.x + blendWeights.y + blendWeights.z, 1.0e-5);

    float dx = terrainDetail(p.yz * TEXTURE_SCALE, slope);
    float dy = terrainDetail(p.xz * TEXTURE_SCALE, slope);
    float dz = terrainDetail(p.xy * TEXTURE_SCALE, slope);
    return dx * blendWeights.x + dy * blendWeights.y + dz * blendWeights.z;
}

void main() {
    vec3 n = normalize(worldNormal);
    float slope = clamp(1.0 - n.y, 0.0, 1.0);

    float cameraDistance = length(eyePos);
    float distanceFade = 1.0 - smoothstep(NEAR_FADE_BLOCKS, FAR_FADE_BLOCKS, cameraDistance);

    mat3 invRot = transpose(mat3(ModelViewMat));
    vec3 viewDirWorld = normalize(invRot * eyePos);


    float facing = clamp(dot(n, -viewDirWorld), 0.0, 1.0);

    int steps = int(float(MAX_STEPS) * distanceFade * facing + 0.5);

    vec3 samplePos = worldPos;
    float height = triplanarDetail(samplePos, n, slope);

    if (steps > 0) {
        vec3 stepVec = -viewDirWorld * (PARALLAX_DEPTH / float(MAX_STEPS));
        for (int i = 0; i < MAX_STEPS; i++) {
            if (i >= steps) {
                break;
            }
            float expected = 1.0 - float(i + 1) / float(MAX_STEPS);
            if (height >= expected) {
                break;
            }
            samplePos += stepVec;
            height = triplanarDetail(samplePos, n, slope);
        }
    }


    float shadow = 1.0;
    if (steps > 0 && dot(n, SunDir) > 0.0) {
        vec3 shadowStep = SunDir * (PARALLAX_DEPTH / float(MAX_STEPS));
        vec3 shadowPos = samplePos;
        float baseHeight = height;
        for (int i = 0; i < MAX_STEPS; i++) {
            if (i >= steps) {
                break;
            }
            shadowPos += shadowStep;
            float h = triplanarDetail(shadowPos, n, slope);
            float expected = baseHeight + float(i + 1) / float(MAX_STEPS);
            if (h > expected) {
                shadow = 0.4;
                break;
            }
        }
    } else if (dot(n, SunDir) <= 0.0) {
        shadow = 0.6;
    }


    vec3 rockColor = vec3(0.42, 0.40, 0.38);
    vec3 detailTint = mix(vec3(1.05, 1.03, 0.97), rockColor, clamp(slope * 1.2, 0.0, 1.0));
    vec3 albedo = mix(vec3(1.0), detailTint, 0.5 + 0.5 * height);
    albedo *= mix(1.0, shadow, 0.6);


    vec4 lightColor = texelFetch(Sampler2, lightMapCoord, 0);
    float lightMax = max(lightColor.r, max(lightColor.g, lightColor.b));
    vec3 light = mix(vec3(1.0), lightColor.rgb, step(0.01, lightMax));

    vec3 outColor = vertexColor.rgb * albedo * light;
    vec4 shaded = vec4(outColor, vertexColor.a) * ColorModulator;
    fragColor = linear_fog(shaded, vertexDistance, FogStart, FogEnd, FogColor);
}
