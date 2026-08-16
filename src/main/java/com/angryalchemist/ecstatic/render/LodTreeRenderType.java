package com.angryalchemist.ecstatic.render;

import java.util.HashMap;
import java.util.Map;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;

final class LodTreeRenderType {
    private static final Map<ResourceLocation, RenderType> CACHE = new HashMap<>();

    private LodTreeRenderType() {
    }

    static RenderType forTexture(ResourceLocation texture) {
        return CACHE.computeIfAbsent(texture, id -> LodTerrainRenderType.createTextured("ecstatic_tree_" + id.getPath().replace('/', '_'), id));
    }
}
