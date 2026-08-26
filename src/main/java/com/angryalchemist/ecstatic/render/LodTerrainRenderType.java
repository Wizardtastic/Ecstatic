package com.angryalchemist.ecstatic.render;

import com.google.common.collect.ImmutableMap;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat.Mode;
import com.mojang.blaze3d.vertex.VertexFormatElement;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.RenderStateShard.ShaderStateShard;
import net.minecraft.client.renderer.RenderStateShard.TextureStateShard;
import net.minecraft.client.renderer.RenderType.CompositeState;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType.CompositeState.CompositeStateBuilder;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.resources.ResourceLocation;

public final class LodTerrainRenderType extends RenderType {
    public static final RenderType TERRAIN = create("ecstatic_terrain", DefaultVertexFormat.POSITION_COLOR, POSITION_COLOR_SHADER, null);
    public static final RenderType TERRAIN_TEXTURED = create(
        "ecstatic_terrain_textured", DefaultVertexFormat.POSITION_TEX_COLOR, RenderStateShard.POSITION_COLOR_SHADER, BLOCK_SHEET_MIPPED
    );
    static final VertexFormat BLOCK_SAFE = VertexFormat.builder()
            .add("Position", VertexFormatElement.POSITION)
            .add("Color", VertexFormatElement.COLOR)
            .add("UV0", VertexFormatElement.UV0)
            .add("UV2", VertexFormatElement.UV2)
            .add("Normal", VertexFormatElement.NORMAL)
            .padding(1)
            .build();
    public static final RenderType TERRAIN_LIT = create(
        "ecstatic_terrain_lit", BLOCK_SAFE, RENDERTYPE_SOLID_SHADER, buildTextureState(flatWhiteTexture()), true
    );
    public static final RenderType TERRAIN_LIT_TEXTURED = create("ecstatic_terrain_lit_textured", BLOCK_SAFE, RENDERTYPE_SOLID_SHADER, BLOCK_SHEET_MIPPED, true);
    public static final RenderType TERRAIN_OPAQUE = create(
        "ecstatic_terrain_opaque", DefaultVertexFormat.POSITION_COLOR, POSITION_COLOR_SHADER, null, false, true
    );
    public static final RenderType TERRAIN_TEXTURED_OPAQUE = create(
        "ecstatic_terrain_textured_opaque", DefaultVertexFormat.POSITION_TEX_COLOR, RenderStateShard.POSITION_COLOR_SHADER, BLOCK_SHEET_MIPPED, false, true
    );
    public static final RenderType TERRAIN_LIT_OPAQUE = create(
        "ecstatic_terrain_lit_opaque", BLOCK_SAFE, RENDERTYPE_SOLID_SHADER, buildTextureState(flatWhiteTexture()), true, true
    );
    public static final RenderType TERRAIN_LIT_TEXTURED_OPAQUE = create(
        "ecstatic_terrain_lit_textured_opaque", BLOCK_SAFE, RENDERTYPE_SOLID_SHADER, BLOCK_SHEET_MIPPED, true, true
    );
    public static final RenderType TERRAIN_OPAQUE_NOCULL = create(
        "ecstatic_terrain_opaque_nocull", DefaultVertexFormat.POSITION_COLOR, POSITION_COLOR_SHADER, null, false, true, false
    );
    public static final RenderType TERRAIN_TEXTURED_OPAQUE_NOCULL = create(
        "ecstatic_terrain_textured_opaque_nocull", DefaultVertexFormat.POSITION_TEX_COLOR, RenderStateShard.POSITION_COLOR_SHADER, BLOCK_SHEET_MIPPED, false, true, false
    );
    public static final RenderType TERRAIN_LIT_OPAQUE_NOCULL = create(
        "ecstatic_terrain_lit_opaque_nocull", BLOCK_SAFE, RENDERTYPE_SOLID_SHADER, buildTextureState(flatWhiteTexture()), true, true, false
    );
    public static final RenderType TERRAIN_LIT_TEXTURED_OPAQUE_NOCULL = create(
        "ecstatic_terrain_lit_textured_opaque_nocull", BLOCK_SAFE, RENDERTYPE_SOLID_SHADER, BLOCK_SHEET_MIPPED, true, true, false
    );
    public static final RenderType TERRAIN_PARALLAX = create(
        "ecstatic_terrain_parallax", BLOCK_SAFE, new ShaderStateShard(LodParallaxShader::getOrNull), buildTextureState(flatWhiteTexture()), true
    );
    public static final RenderType TERRAIN_FOG = create(
        "ecstatic_terrain_fog", DefaultVertexFormat.POSITION_COLOR, new ShaderStateShard(LodFogShader::getPlainOrNull), null
    );
    public static final RenderType TERRAIN_FOG_TEXTURED = create(
        "ecstatic_terrain_fog_textured", DefaultVertexFormat.POSITION_TEX_COLOR, new ShaderStateShard(LodFogShader::getTexturedOrNull), BLOCK_SHEET_MIPPED
    );
    public static final RenderType TERRAIN_FOG_OPAQUE = create(
        "ecstatic_terrain_fog_opaque", DefaultVertexFormat.POSITION_COLOR, new ShaderStateShard(LodFogShader::getPlainOrNull), null, false, true
    );
    public static final RenderType TERRAIN_FOG_TEXTURED_OPAQUE = create(
        "ecstatic_terrain_fog_textured_opaque",
        DefaultVertexFormat.POSITION_TEX_COLOR,
        new ShaderStateShard(LodFogShader::getTexturedOrNull),
        BLOCK_SHEET_MIPPED,
        false,
        true
    );
    public static final RenderType TERRAIN_FOG_OPAQUE_NOCULL = create(
        "ecstatic_terrain_fog_opaque_nocull", DefaultVertexFormat.POSITION_COLOR, new ShaderStateShard(LodFogShader::getPlainOrNull), null, false, true, false
    );
    public static final RenderType TERRAIN_FOG_TEXTURED_OPAQUE_NOCULL = create(
        "ecstatic_terrain_fog_textured_opaque_nocull",
        DefaultVertexFormat.POSITION_TEX_COLOR,
        new ShaderStateShard(LodFogShader::getTexturedOrNull),
        BLOCK_SHEET_MIPPED,
        false,
        true,
        false
    );
    public static final RenderType WATER = create("ecstatic_water", DefaultVertexFormat.POSITION_COLOR, POSITION_COLOR_SHADER, null, false, false, false);
    public static final RenderType WATER_LIT = create(
        "ecstatic_water_lit", BLOCK_SAFE, RENDERTYPE_SOLID_SHADER, buildTextureState(flatWhiteTexture()), true, false, false
    );
    public static final RenderType WATER_TEXTURED = create(
        "ecstatic_water_textured", DefaultVertexFormat.POSITION_TEX_COLOR, RenderStateShard.POSITION_COLOR_SHADER, BLOCK_SHEET_MIPPED, false, false, false
    );
    public static final RenderType WATER_LIT_TEXTURED = create(
        "ecstatic_water_lit_textured", BLOCK_SAFE, RENDERTYPE_SOLID_SHADER, BLOCK_SHEET_MIPPED, true, false, false
    );
    private static ResourceLocation flatWhiteTextureId;

    private static ResourceLocation flatWhiteTexture() {
        if (flatWhiteTextureId == null) {
            NativeImage image = new NativeImage(1, 1, false);
            image.setPixelRGBA(0, 0, -1);
            DynamicTexture texture = new DynamicTexture(image);
            ResourceLocation id = ResourceLocation.fromNamespaceAndPath("ecstatic", "terrain_lit_flat");
            Minecraft.getInstance().getTextureManager().register(id, texture);
            flatWhiteTextureId = id;
        }

        return flatWhiteTextureId;
    }

    private LodTerrainRenderType(
        String name, VertexFormat format, Mode mode, int bufferSize, boolean affectsCrumbling, boolean sortOnUpload, Runnable setupState, Runnable clearState
    ) {
        super(name, format, mode, bufferSize, affectsCrumbling, sortOnUpload, setupState, clearState);
    }

    static RenderType create(String name, VertexFormat format, ShaderStateShard shaderState, TextureStateShard textureState) {
        return create(name, format, shaderState, textureState, false);
    }

    private static RenderType create(String name, VertexFormat format, ShaderStateShard shaderState, TextureStateShard textureState, boolean lit) {
        return create(name, format, shaderState, textureState, lit, false);
    }

    private static RenderType create(
        String name, VertexFormat format, ShaderStateShard shaderState, TextureStateShard textureState, boolean lit, boolean opaqueCulled
    ) {
        return create(name, format, shaderState, textureState, lit, opaqueCulled, opaqueCulled);
    }

    private static RenderType create(
        String name, VertexFormat format, ShaderStateShard shaderState, TextureStateShard textureState, boolean lit, boolean noTransparency, boolean cull
    ) {
        CompositeStateBuilder builder = CompositeState.builder()
            .setShaderState(shaderState)
            .setTransparencyState(noTransparency ? NO_TRANSPARENCY : TRANSLUCENT_TRANSPARENCY)
            .setDepthTestState(LEQUAL_DEPTH_TEST)
            .setCullState(cull ? CULL : NO_CULL)
            .setWriteMaskState(COLOR_DEPTH_WRITE);
        if (textureState != null) {
            builder.setTextureState(textureState);
        }

        if (lit) {
            builder.setLightmapState(LIGHTMAP);
        }

        CompositeState state = builder.createCompositeState(false);

        try {
            Method createMethod = findCreateMethod();
            createMethod.setAccessible(true);
            return (RenderType)createMethod.invoke(null, name, format, Mode.TRIANGLES, 256, state);
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException("Failed to create " + name + " RenderType", e);
        }
    }

    static RenderType createTextured(String name, ResourceLocation texture) {
        return create(name, DefaultVertexFormat.POSITION_TEX_COLOR, RenderStateShard.POSITION_COLOR_SHADER, buildTextureState(texture));
    }

    private static TextureStateShard buildTextureState(ResourceLocation texture) {
        try {
            Constructor<TextureStateShard> constructor = TextureStateShard.class.getDeclaredConstructor(ResourceLocation.class, boolean.class, boolean.class);
            constructor.setAccessible(true);
            return constructor.newInstance(texture, false, false);
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException("Failed to build tree texture state for " + texture, e);
        }
    }

    private static Method findCreateMethod() {
        for (Method method : RenderType.class.getDeclaredMethods()) {
            if (Modifier.isStatic(method.getModifiers())) {
                Class<?>[] params = method.getParameterTypes();
                if (params.length == 5
                    && params[0] == String.class
                    && params[1] == VertexFormat.class
                    && params[2] == Mode.class
                    && params[3] == int.class
                    && params[4] == CompositeState.class) {
                    return method;
                }
            }
        }

        throw new IllegalStateException("Could not find RenderType's 5-arg create(...) overload by signature");
    }
}
