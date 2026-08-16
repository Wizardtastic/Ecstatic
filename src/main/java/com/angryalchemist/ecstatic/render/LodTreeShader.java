package com.angryalchemist.ecstatic.render;

import com.angryalchemist.ecstatic.Constants;
import com.mojang.blaze3d.shaders.Uniform;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import java.io.IOException;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ShaderInstance;

final class LodTreeShader {
    private static final String SHADER_NAME = "ecstatic_lod_tree";
    private static ShaderInstance instance;
    private static boolean loadAttempted;

    private LodTreeShader() {
    }

    static ShaderInstance getOrNull() {
        if (!loadAttempted) {
            loadAttempted = true;

            try {
                instance = new ShaderInstance(Minecraft.getInstance().getResourceManager(), "ecstatic_lod_tree", DefaultVertexFormat.POSITION_COLOR_TEX);
            } catch (IOException | RuntimeException e) {
                Constants.LOG.error("Ecstatic: failed to load the tree billboard shader; falling back to the plain (fog-less) position_color_tex shader", e);
                instance = null;
            }
        }

        return instance;
    }

    static void setFogIntensity(float intensity) {
        if (instance != null) {
            Uniform fogIntensity = instance.getUniform("FogIntensity");
            if (fogIntensity != null) {
                fogIntensity.set(intensity);
            }
        }
    }
}
