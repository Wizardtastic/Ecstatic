package com.angryalchemist.ecstatic.render;

import com.angryalchemist.ecstatic.Constants;
import com.mojang.blaze3d.shaders.Uniform;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import java.io.IOException;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ShaderInstance;

final class LodWaterShader {
    private static final String PLAIN_SHADER_NAME = "ecstatic_lod_water";
    private static final String TEXTURED_SHADER_NAME = "ecstatic_lod_water_tex";
    private static ShaderInstance plainInstance;
    private static boolean plainLoadAttempted;
    private static ShaderInstance texturedInstance;
    private static boolean texturedLoadAttempted;

    private LodWaterShader() {
    }

    static ShaderInstance getPlainOrNull() {
        if (!plainLoadAttempted) {
            plainLoadAttempted = true;

            try {
                plainInstance = new ShaderInstance(Minecraft.getInstance().getResourceManager(), "ecstatic_lod_water", DefaultVertexFormat.POSITION_COLOR);
            } catch (IOException | RuntimeException e) {
                Constants.LOG
                    .error(
                        "Ecstatic: failed to load the plain ocean-plane water shader; falling back to the plain position_color shader with CPU per-vertex specular",
                        e
                    );
                plainInstance = null;
            }
        }

        return plainInstance;
    }

    static ShaderInstance getTexturedOrNull() {
        if (!texturedLoadAttempted) {
            texturedLoadAttempted = true;

            try {
                texturedInstance = new ShaderInstance(
                    Minecraft.getInstance().getResourceManager(), "ecstatic_lod_water_tex", DefaultVertexFormat.POSITION_COLOR_TEX
                );
            } catch (IOException | RuntimeException e) {
                Constants.LOG.error("Ecstatic: failed to load the textured ocean-plane water shader; falling back to the plain position_color_tex shader", e);
                texturedInstance = null;
            }
        }

        return texturedInstance;
    }

    static void setSunDirection(float x, float y, float z) {
        setSunDirection(plainInstance, x, y, z);
        setSunDirection(texturedInstance, x, y, z);
    }

    private static void setSunDirection(ShaderInstance instance, float x, float y, float z) {
        if (instance != null) {
            Uniform sunDir = instance.getUniform("SunDir");
            if (sunDir != null) {
                sunDir.set(x, y, z);
            }
        }
    }

    static void setGameTime(float dayFraction) {
        setGameTime(plainInstance, dayFraction);
        setGameTime(texturedInstance, dayFraction);
    }

    private static void setGameTime(ShaderInstance instance, float dayFraction) {
        if (instance != null) {
            Uniform gameTime = instance.getUniform("GameTime");
            if (gameTime != null) {
                gameTime.set(dayFraction);
            }
        }
    }

    static void setFogIntensity(float intensity) {
        setFogIntensity(plainInstance, intensity);
        setFogIntensity(texturedInstance, intensity);
    }

    private static void setFogIntensity(ShaderInstance instance, float intensity) {
        if (instance != null) {
            Uniform fogIntensity = instance.getUniform("FogIntensity");
            if (fogIntensity != null) {
                fogIntensity.set(intensity);
            }
        }
    }
}
