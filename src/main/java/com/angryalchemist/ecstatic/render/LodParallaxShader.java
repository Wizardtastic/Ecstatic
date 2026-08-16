package com.angryalchemist.ecstatic.render;

import com.angryalchemist.ecstatic.Constants;
import com.mojang.blaze3d.shaders.Uniform;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import java.io.IOException;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ShaderInstance;

final class LodParallaxShader {
    private static final String SHADER_NAME = "ecstatic_lod_terrain_parallax";
    private static ShaderInstance instance;
    private static boolean loadAttempted;

    private LodParallaxShader() {
    }

    static ShaderInstance getOrNull() {
        if (!loadAttempted) {
            loadAttempted = true;

            try {
                instance = new ShaderInstance(Minecraft.getInstance().getResourceManager(), "ecstatic_lod_terrain_parallax", DefaultVertexFormat.BLOCK);
            } catch (IOException | RuntimeException e) {
                Constants.LOG.error("Ecstatic: failed to load the parallax terrain shader; falling back to the plain lit terrain shader", e);
                instance = null;
            }
        }

        return instance;
    }

    static void setSunDirection(float x, float y, float z) {
        if (instance != null) {
            Uniform sunDir = instance.getUniform("SunDir");
            if (sunDir != null) {
                sunDir.set(x, y, z);
            }
        }
    }
}
