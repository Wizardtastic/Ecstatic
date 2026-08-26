package com.angryalchemist.ecstatic.render;

import com.angryalchemist.ecstatic.Constants;
import com.mojang.blaze3d.shaders.Uniform;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import java.io.IOException;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ShaderInstance;

final class LodFogShader {
    private static final String PLAIN_SHADER_NAME = "ecstatic_lod_terrain_fog";
    private static final String TEXTURED_SHADER_NAME = "ecstatic_lod_terrain_fog_tex";
    private static final String CLOUD_TEXTURED_SHADER_NAME = "ecstatic_cloud_fog_tex";
    private static ShaderInstance plainInstance;
    private static boolean plainLoadAttempted;
    private static ShaderInstance texturedInstance;
    private static boolean texturedLoadAttempted;
    private static ShaderInstance cloudTexturedInstance;
    private static boolean cloudTexturedLoadAttempted;

    private LodFogShader() {
    }

    static ShaderInstance getPlainOrNull() {
        if (!plainLoadAttempted) {
            plainLoadAttempted = true;

            try {
                plainInstance = new ShaderInstance(Minecraft.getInstance().getResourceManager(), "ecstatic_lod_terrain_fog", DefaultVertexFormat.POSITION_COLOR);
            } catch (IOException | RuntimeException e) {
                Constants.LOG.error("Ecstatic: failed to load the cheap-terrain fog shader; falling back to the plain (fog-less) position_color shader", e);
                plainInstance = null;
            }
        }

        return plainInstance;
    }

    static ShaderInstance getCloudTexturedOrNull() {
        if (!cloudTexturedLoadAttempted) {
            cloudTexturedLoadAttempted = true;

            try {
                cloudTexturedInstance = new ShaderInstance(
                        Minecraft.getInstance().getResourceManager(), "ecstatic_cloud_fog_tex", DefaultVertexFormat.POSITION_TEX_COLOR
                );
            } catch (IOException | RuntimeException e) {
                Constants.LOG.error("Ecstatic: failed to load the cloud fog shader; falling back to the plain (fog-less) POSITION_TEX_COLOR shader", e);
                cloudTexturedInstance = null;
            }
        }

        return cloudTexturedInstance;
    }

    static ShaderInstance getTexturedOrNull() {
        if (!texturedLoadAttempted) {
            texturedLoadAttempted = true;

            try {
                texturedInstance = new ShaderInstance(
                        Minecraft.getInstance().getResourceManager(), "ecstatic_lod_terrain_fog_tex", DefaultVertexFormat.POSITION_TEX_COLOR
                );
            } catch (IOException | RuntimeException e) {
                Constants.LOG
                        .error("Ecstatic: failed to load the textured cheap-terrain fog shader; falling back to the plain (fog-less) POSITION_TEX_COLOR shader", e);
                texturedInstance = null;
            }
        }

        return texturedInstance;
    }

    static void setFogIntensity(float intensity) {
        setFogIntensity(plainInstance, intensity);
        setFogIntensity(texturedInstance, intensity);
        setFogIntensity(cloudTexturedInstance, intensity);
    }

    private static void setFogIntensity(ShaderInstance instance, float intensity) {
        if (instance != null) {
            Uniform fogIntensity = instance.getUniform("FogIntensity");
            if (fogIntensity != null) {
                fogIntensity.set(intensity);
            }
        }
    }

    static void setSaturation(float saturation) {
        setSaturationOn(texturedInstance, saturation);
        setSaturationOn(cloudTexturedInstance, saturation);
    }

    private static void setSaturationOn(ShaderInstance instance, float saturation) {
        if (instance != null) {
            Uniform uniform = instance.getUniform("Saturation");
            if (uniform != null) {
                uniform.set(saturation);
            }
        }
    }
}
