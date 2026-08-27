package com.angryalchemist.ecstatic.render;

import com.angryalchemist.ecstatic.Constants;
import java.io.IOException;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ShaderInstance;

final class LodLitShader {
    private static final String SHADER_NAME = "ecstatic_lod_terrain_lit";
    private static ShaderInstance instance;
    private static boolean loadAttempted;

    private LodLitShader() {
    }

    static ShaderInstance getOrNull() {
        if (!loadAttempted) {
            loadAttempted = true;

            try {
                instance = new ShaderInstance(Minecraft.getInstance().getResourceManager(), SHADER_NAME, LodTerrainRenderType.BLOCK_SAFE);
            } catch (IOException | RuntimeException e) {
                Constants.LOG.error("Ecstatic: failed to load the lit terrain shader; falling back to the vanilla solid shader", e);
                instance = null;
            }
        }

        return instance;
    }
}